	package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.alliance.alliancebattle.compare.FightScoreCompare;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.shipdata.ShipCheckData;

/***
 * 联盟争夺战岛屿
 * 
 * @author lhj
 * 
 */
public class BattleIsland extends Sample
{

	Logger log=LogFactory.getLogger(BattleIsland.class);
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	/** 战力比较器 **/
	FightScoreCompare fightScompare=new FightScoreCompare();
	/** 岛屿名称 **/
	String name;
	/** 胜利的联盟 **/
	int win_Id;
	/** 失败的联盟 **/
	int lose_Id;
	/** 时产 **/
	int output;
	/** 押注的底价 **/
	int lowPrice;
	/** 联盟id - 物资数量 **/
	int[] rankValue={0,0,0,0};
	/** 参与胜利的奖励 **/
	int awardSid;
	/**参与失败的奖励**/
	int loseSid;
	/** 竞争胜利的联盟发放的科技点 **/
	int winScience;
	/** 竞争失败的联盟发放的科技点 **/
	int loseScience;
	/** 弃权的联盟发放的科技点 **/
	int abstainScience;
	/** 这个岛屿的竞争状态(需不需要进行联盟战) **/
	boolean state=false;
	/** 出手的顺序 因为rankList是逆序所以第二个先出手 **/
	boolean attack=true;
	/** 参与联盟战的人 playerAllianceFight **/
	IntKeyHashMap allianceFightPlayer=new IntKeyHashMap();
	/** 排名第一联盟战斗中的玩家 **/
	IntList firstList=new IntList();
	/** 排名第二联盟战斗中的玩家 **/
	IntList lastlist=new IntList();

	/** 检测竞标值 **/
	public String checkMaterial(Alliance alliance,int material)
	{
		if(material<=0) return "error material";
		if(!alliance.enoughMaterial(material))
		{
			return "material limit";
		}
		int all_material=material+getRankByAllianceId(alliance.getId());
		if(all_material<lowPrice) return "bet less";
		if(all_material<=rankValue[getLowIndex()+1]) return "bet less";
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(all_material==rankValue[i+1]) return "bet same";
		}
		return null;
	}

	/** 联盟进行押注 **/
	public String addBetIsland(int materialNum,Alliance alliance,
		CreatObjectFactory objectFactory,Player player)
	{
		String result=checkMaterial(alliance,materialNum);
		if(result!=null) return result;
		addBetValue(materialNum,alliance,objectFactory,player);
		return null;
	}
	/** 开始竞标 **/
	public void addBetValue(int materialNum,Alliance alliance,
		CreatObjectFactory objectFactory,Player player)
	{
		// 扣除联盟物资
		alliance.reduceMaterial(materialNum);
		/** 物资 **/
		objectFactory.createSciencePointTrack(SciencePointTrack.BET_ISLAND,
			alliance.getId(),materialNum,alliance.getMaterial(),
			SciencePointTrack.REDUCE,player.getId(),
			SciencePointTrack.MATERIAL,getSid()+","
				+getRankByAllianceId(alliance.getId()));
		//刷新排行榜
		sortRankValue();
		//联盟战记录日志
//		saveBetValue(objectFactory,alliance.getId(),materialNum);
		JBackKit.sendAllianceWarResource(objectFactory,alliance);
		int all_materialNum=materialNum
			+getRankByAllianceId(alliance.getId());
		int index=getTargetIndex(alliance.getId());
		int oldvaue=rankValue[index+1];
		Alliance lose=null;
		if(rankValue[index]!=0)
		{
			if(rankValue[index]!=alliance.getId())
			{
				lose=objectFactory.getAlliance(rankValue[index],true);
				lose.addMaterial(oldvaue);
				lose.setBetBattleIsland(0);
				/** 物资 **/
				objectFactory.createSciencePointTrack(
					SciencePointTrack.REBACK_BY_BEYOND,lose.getId(),oldvaue,
					lose.getMaterial(),SciencePointTrack.ADD,0,
					SciencePointTrack.MATERIAL,getSid()+"");
				JBackKit.sendOutOfRank(objectFactory,lose);
				JBackKit.sendAllianceWarResource(objectFactory,lose);
				JBackKit.sendOutOfRank(objectFactory,lose);
			}
		}
		alliance.setBetBattleIsland(getSid());
		rankValue[index]=alliance.getId();
		rankValue[index+1]=all_materialNum;
		// 发送邮件
		sendMessage(alliance,player,all_materialNum,lose,oldvaue,objectFactory);
	}

	/** 增加联盟科技点 **/
	public void addAllianceSciencePoint(CreatObjectFactory objectFactory)
	{
		if(win_Id==0) return;
		Alliance alliance=objectFactory.getAlliance(win_Id,true);
		if(alliance==null) return;
		alliance.addSciencepoint(output);
		JBackKit.sendAllianceWarResource(objectFactory,alliance);
		/** 科技点 **/
		objectFactory.createSciencePointTrack(
			SciencePointTrack.FROM_GIVE_VALUES,alliance.getId(),
			PublicConst.ADD_SCIENCEPOINT,alliance.getSciencepoint(),
			SciencePointTrack.ADD,0,
			SciencePointTrack.SCIENCE_POINT,getSid()+"");
	}

	/** 验证这个玩家可以加入联盟战 **/
	public boolean isHavePlayer(int playerId)
	{
		if(allianceFightPlayer.get(playerId)==null) return false;
		return true;
	}

	/** 保存玩家数据 **/
	public void addAllianceFightPlayer(PlayerAllianceFight p_fight)
	{
		allianceFightPlayer.put(p_fight.getPlayerId(),p_fight);
	}

	/** 获取联盟战的中数据 **/
	public PlayerAllianceFight getPlayerFight(int playerId)
	{
		return (PlayerAllianceFight)allianceFightPlayer.get(playerId);
	}

	/** 获取联盟中还可以战力的玩家 **/
	public PlayerAllianceFight[] getPlayerFight(int allianceId,
		IntList fightList)
	{
		if(allianceFightPlayer.size()==0) return null;
		addFightList(allianceId,fightList);
		// 有可能一方已经没有玩家可以战斗了
		if(fightList.size()==0) return null;
		PlayerAllianceFight[] fights=new PlayerAllianceFight[fightList
			.size()];
		for(int i=0;i<fightList.size();i++)
		{
			fights[i]=(PlayerAllianceFight)allianceFightPlayer.get(fightList
				.get(i));
		}
		return fights;
	}

	/** 添加还能够战斗的玩家 **/
	public void addFightList(int allianceId,IntList fightList)
	{
		if(allianceFightPlayer.size()==0) return;
		Object[] fights=allianceFightPlayer.valueArray();
		for(int i=0;i<fights.length;i++)
		{
			PlayerAllianceFight fight=(PlayerAllianceFight)fights[i];
			if(fight==null) continue;
			if(allianceId!=fight.getAllianceId()) continue;
			if(!fight.isOut()) fightList.add(fight.getPlayerId());
		}
	}

	/** 通过联盟id获取当前的竞标值 **/
	public int getRankByAllianceId(int aid)
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(aid==rankValue[i]) return rankValue[i+1];
		}
		return 0;
	}

	/** 获取当前岛屿的启拍资金 **/
	public int getRankLowPrice(int allianceId)
	{
		int num=getRankByAllianceId(allianceId);
		if(num!=0) return num;
		// 返回的是联盟id 的下标
		return rankValue[getLowIndex()+1]==0?lowPrice
			:rankValue[getLowIndex()+1];
	}

	/** 设置竞标值(加标) **/
	public void setAddRankVaule(int value,int aid)
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(aid==rankValue[i])
			{
				rankValue[i+1]+=value;
			}
		}
	}
	/** 获取当前的最低竞标值得下标 **/
	public int getLowIndex()
	{
		int index=0;
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[index+1]>rankValue[i+1])
			{
				index=i;
			}
		}
		return index;
	}

	/** 获取改变位下标 **/
	public int getTargetIndex(int id)
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]==id) return i;
		}
		return getLowIndex();
	}
	/** 序列化联盟竞标信息 **/
	public void showBytesRankValue(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		sortRankValue();
		int length=getAlliancesBetLength();
		data.writeByte(length);
		if(length==0) return;
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]==0) continue;
			int allianceId=rankValue[i];
			Alliance alliance=objectFactory.getAlliance(allianceId,false);
			// id
			data.writeInt(allianceId);
			// 联盟名称
			data.writeUTF(alliance.getName());
			// 竞标值
			data.writeInt(rankValue[i+1]);
		}
	}
	/** 获取当前的联盟竞标的长度 **/
	public int getAlliancesBetLength()
	{
		int num=0;
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]!=0) num++;
		}
		return num;
	}
	/** 获取字符换格式的联盟竞标信息 **/
	public String getRankValues()
	{
		StringBuffer rankValues=new StringBuffer();
		for(int i=0;i<rankValue.length;i++)
		{
			if(i==0)
				rankValues.append(rankValue[i]);
			else
				rankValues.append(":"+rankValue[i]);
		}
		return rankValues.toString();
	}

	/**联盟战结束任务**/
	public void sendFightEndTask(CreatObjectFactory objectFactory)
	{
		battleIslandWin();
		//攻占日志
		saveFightOverLog(objectFactory);
		sendAward(objectFactory);
		setFightEndMessage(objectFactory);
		setFightEndSystemMessage(objectFactory);
		rebackPlayerShips(objectFactory);
		// 清除玩家的船只记录
		clearRecord();
		JBackKit.sendAllianceBetInfo(null,objectFactory);
	}

	/** 设置最后的赢家 **/
	public void battleIslandWin()
	{
		if(rankValue[0]==0&&rankValue[2]==0) return;
		if(rankValue[0]!=0)
		{
			if(checkJoin(rankValue[0]))
			{
				if(checkPlayerAlive(rankValue[0]))
					win_Id=rankValue[0];
				else
					lose_Id=rankValue[0];
			}
		}
		if(rankValue[2]!=0)
		{
			if(checkJoin(rankValue[2]))
			{
				if(checkPlayerAlive(rankValue[2]))
					win_Id=rankValue[2];
				else
					lose_Id=rankValue[2];
			}
		}
	}
	/** 联盟战结束以后发奖励 **/
	public void sendAward(CreatObjectFactory factory)
	{
		Alliance winAlliance=(Alliance)factory.getAllianceMemCache()
			.loadOnly(win_Id+"");
		Alliance lostAlliance=(Alliance)factory.getAllianceMemCache()
			.loadOnly(lose_Id+"");
		int result=0;
		if(winAlliance!=null)
		{
			if(checkJoin(win_Id))
			{
				winAlliance.addSciencepoint(winScience);
				result=winScience;
			}
			else
			{
				winAlliance.addSciencepoint(abstainScience);
				result=abstainScience;
			}
			if(result!=0)
			{
				/** 科技点 **/
				factory.createSciencePointTrack(
					SciencePointTrack.FROM_ALLIANCE_FIGHT,winAlliance.getId(),
					result,winAlliance.getSciencepoint(),
					SciencePointTrack.ADD,0,
					SciencePointTrack.SCIENCE_POINT,getSid()+"");
				result=0;
			}
			JBackKit.sendAllianceWarResource(factory,winAlliance);
		}
		if(lostAlliance!=null)
		{
			if(checkJoin(lose_Id))
			{
				lostAlliance.addSciencepoint(loseScience);
				result=loseScience;
			}
			else
			{
				lostAlliance.addSciencepoint(abstainScience);
				result=abstainScience;
			}
			if(result!=0)
			{
				/** 科技点 **/
				factory.createSciencePointTrack(
					SciencePointTrack.FROM_ALLIANCE_FIGHT,lostAlliance.getId(),
					result,lostAlliance.getSciencepoint(),
					SciencePointTrack.ADD,0,
					SciencePointTrack.SCIENCE_POINT,getSid()+"");
			}
			JBackKit.sendAllianceWarResource(factory,lostAlliance);
		}
	}
	/** 返回给玩家的船只信息 **/
	public void rebackPlayerShips(CreatObjectFactory objectFactory)
	{
		if(allianceFightPlayer.size()==0) return;
		Object[] objects=allianceFightPlayer.valueArray();
		for(int i=0;i<objects.length;i++)
		{
			if(objects[i]==null) continue;
			PlayerAllianceFight pfight=(PlayerAllianceFight)objects[i];
			if(pfight==null) continue;
			Player player=objectFactory.getPlayerCache().load(pfight.getPlayerId()+"");
			if(player==null) continue;
			IntList shipList=pfight.getList();
			// 给玩家添加联盟战斗结束以后还有剩余的船只
			for(int j=0;j<shipList.size();j+=3)
			{
				player.getIsland().addTroop(shipList.get(j),
					shipList.get(j+1),player.getIsland().getTroops());
			}
			// 船只日志
			objectFactory.addShipTrack(0,ShipCheckData.ALLIANCE_BACK_SHIP,
				player,shipList,null,false);
			JBackKit.sendResetTroops(player);
		}
	}

	/** 判断下是否需要设置联盟战 **/
	public void setIslandState()
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]!=0)
			{
				state=true;
				return;
			}
		}
	}

	/** 序列化船只信息 **/
	public void showBytesWriteShips(ByteBuffer data,int playerId)
	{
		PlayerAllianceFight pfight=getPlayerFight(playerId);
		IntList list=pfight.getList();
		// 如果玩家被淘汰则发0
		if(pfight.isOut()||list==null||list.size()==0)
		{
			data.writeByte(0);
			return;
		}
		// 坑位数
		int length=list.size()/3;
		data.writeByte(length);
		for(int i=0;i<list.size();i+=3)
		{
			// sid
			data.writeShort(list.get(i));
			data.writeShort(list.get(i+1));
			// 位置
			data.writeByte(list.get(i+2));
		}
	}

	/** 联盟竞标排序 **/
	public void sortRankValue()
	{
		if(sort())
		{
			int value1=0;
			int value2=0;
			for(int i=0;i<rankValue.length;i+=2)
			{
				for(int j=0;j<(rankValue.length/2)-1;j+=2)
				{
					if(rankValue[j+1]<rankValue[j+3])
					{
						value1=rankValue[j];
						value2=rankValue[j+1];
						rankValue[j]=rankValue[j+2];
						rankValue[j+1]=rankValue[j+3];
						rankValue[j+2]=value1;
						rankValue[j+3]=value2;
					}
				}
			}
		}
	}

	/** 验证是否排序 **/
	public boolean sort()
	{
		int endValue=rankValue[rankValue.length-1];
		for(int i=0;i<rankValue.length-1;i+=2)
		{
			if(endValue>rankValue[i+1]) return true;
		}
		return false;
	}

	/** 是否需要进行联盟战 **/
	public boolean checkNeedFight()
	{
		if(firstList.size()==0||lastlist.size()==0) return true;
		return false;
	}

	/** 联盟参与的玩家根据玩家的战斗力进行排序 **/
	public boolean fightRankList(int allianceId,IntList fightList)
	{
		PlayerAllianceFight[] fights=getPlayerFight(allianceId,fightList);
		if(fights==null) return false;
		SetKit.sort(fights,fightScompare);// 战力排序
		if(fightList.size()==0) return false;
		fightList.clear();
		for(int i=0;i<fights.length;i++)
		{
			fightList.add(fights[i].getPlayerId());
		}
		return true;
	}

	public void bytesWriteFirstList(ByteBuffer data)
	{
		if(firstList==null||firstList.size()==0)
		{
			data.writeShort(0);
			return;
		}
		data.writeShort(firstList.size());
		for(int i=0;i<firstList.size();i++)
		{
			data.writeInt(firstList.get(i));
		}
	}

	public void bytesReadFirstList(ByteBuffer data)
	{
		int le=data.readUnsignedShort();
		if(le<0) return;
		for(int i=0;i<le;i++)
		{
			firstList.add(data.readInt());
		}
	}

	public void bytesReadLastList(ByteBuffer data)
	{
		int le=data.readUnsignedShort();
		if(le<0) return;
		for(int i=0;i<le;i++)
		{
			lastlist.add(data.readInt());
		}
	}

	public void bytesWriteLastList(ByteBuffer data)
	{
		if(lastlist==null||lastlist.size()==0)
		{
			data.writeShort(0);
			return;
		}
		data.writeShort(lastlist.size());
		for(int i=0;i<lastlist.size();i++)
		{
			data.writeInt(lastlist.get(i));
		}
	}

	/** 清除记录 **/
	public void clearRecord()
	{
		firstList.clear();
		lastlist.clear();
		allianceFightPlayer.clear();
	}

	/** 判断当前联盟是否有玩家报名 **/
	public boolean checkJoin(int allianceId)
	{
		Object[] objects=allianceFightPlayer.valueArray();
		if(objects.length==0) return false;
		for(int i=0;i<objects.length;i++)
		{
			if(objects[i]==null) continue;
			PlayerAllianceFight pFight=(PlayerAllianceFight)objects[i];
			if(pFight.getAllianceId()==allianceId) return true;
		}
		return false;
	}
	
	/**检测联盟内的玩家是否还有没有出局的**/
	public boolean checkPlayerAlive(int allianceId)
	{
		Object[] objects=allianceFightPlayer.valueArray();
		if(objects.length==0) return false;
		for(int i=0;i<objects.length;i++)
		{
			if(objects[i]==null) continue;
			PlayerAllianceFight pFight=(PlayerAllianceFight)objects[i];
			if(pFight.getAllianceId()==allianceId && !pFight.out)
				return true;
		}
		return false;
	}

	/** 发送系统消息告知所有玩家x 联盟 占领了岛屿 **/
	public void setFightEndSystemMessage(CreatObjectFactory objectFactory)
	{
		if(win_Id==0) return;
		Alliance alliance=objectFactory.getAlliance(win_Id,false);
		if(alliance==null) return;
		// 发送系统信息
		String messcontent=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_fight_system_content");
		messcontent=TextKit.replace(messcontent,"%",alliance.getName());
		String islandName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_battle_island_"+getSid());
		messcontent=TextKit.replace(messcontent,"%",islandName);
		SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),messcontent);
	}

	/** 玩家信息序列 **/
	public void bytesWriteAlliancePlayerInfo(ByteBuffer data)
	{
		Object[] playerFights=allianceFightPlayer.valueArray();
		int size=playerFights.length;
		data.writeInt(size);
		for(int i=0;i<size;i++)
		{
			PlayerAllianceFight playerFight=(PlayerAllianceFight)playerFights[i];
			playerFight.bytesWrite(data);
		}
	}

	/** 序列化 **/
	public void bytesReadAlliancePlayerInfo(ByteBuffer data)
	{
		int size=data.readInt();
		for(int i=0;i<size;i++)
		{
			PlayerAllianceFight playFight=new PlayerAllianceFight();
			playFight=playFight.bytesRead(data);
			allianceFightPlayer.put(playFight.getPlayerId(),playFight);
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		state=data.readBoolean();
		attack=data.readBoolean();
		win_Id=data.readInt();
		return this;
	}

	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeBoolean(state);
		data.writeBoolean(attack);
		data.writeInt(win_Id);
	}

	/** 序列化岛屿信息 **/
	public void showBytesBattleInfo(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		showByteBattleBet(data,objectFactory);
		showBytesWriteAllianceFight(data,objectFactory);
	}

	/** 序列化竞标岛屿的信息 **/
	public void showByteBattleBet(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		data.writeInt(getSid());
		data.writeInt(getSid());
		// 时产
		data.writeInt(getOutput());
		Alliance alliance=objectFactory.getAlliance(win_Id,false);
		data.writeUTF(alliance==null?"":alliance.getName());
		// 序列化联盟竞标信息
		showBytesRankValue(data,objectFactory);
	}

	/** 每个岛屿联盟战的信息 **/
	public void showBytesWriteAllianceFight(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		sortRankValue();
		int length=getAlliancesBetLength();
		data.writeByte(length);
		if(length==0) return;
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]==0) continue;
			showByteAllianceFight(data,rankValue[i],objectFactory);
		}
	}

	/** 序列化 联盟竞标信息 和联盟的参赛人员 **/
	public void showByteAllianceFight(ByteBuffer data,int allianceId,
		CreatObjectFactory objectFactory)
	{
		Alliance alliance=objectFactory.getAlliance(allianceId,false);
		data.writeInt(allianceId);
		data.writeUTF(alliance.getName());
		int top=data.top();
		data.writeShort(0);
		int length=0;
		// 序列化玩家参赛人员
		Object object[]=allianceFightPlayer.valueArray();
		for(int j=0;j<object.length;j++)
		{
			PlayerAllianceFight pfight=(PlayerAllianceFight)object[j];
			if(pfight.getAllianceId()!=allianceId) continue;
			pfight.showBytesWriteInfo(data,objectFactory);
			length++;
		}
		if(length!=0)
		{
			int nowtop=data.top();
			data.setTop(top);
			data.writeShort(length);
			data.setTop(nowtop);
		}
	}

	/** 清除记录 **/
	public void clear(CreatObjectFactory objectFactory)
	{
		state=false;
		attack=true;
		win_Id=0;
		lose_Id=0;
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]!=0)
			{
				Alliance alliance=objectFactory.getAlliance(rankValue[i],
					true);
				rankValue[i]=0;
				rankValue[i+1]=0;
				if(alliance==null) continue;
				alliance.clear();
			}
		}
		firstList.clear();
		lastlist.clear();
	}

	/** 竞标结束以后发邮件 **/
	public void sendBetEndMessage(CreatObjectFactory objectFactory)
	{
		// 邮件主题
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_bet_message");
		// 邮件内容
		String content=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_bet_win_content");
		String other="";
		if(getAlliancesBetLength()>=rankValue.length/2)
		{
			other=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_bet_win_content_thing");
			String endContent=getMessageContent(content,other,0,
				objectFactory,2);
			Alliance alliance=objectFactory.getAlliance(rankValue[0],false);
			SeaBackKit.sendAllianceMessage(title,endContent,
				alliance.getPlayerList(),Message.SYSTEM_ONE_TYPE,null,0);
			endContent=getMessageContent(content,other,2,objectFactory,0);
			alliance=objectFactory.getAlliance(rankValue[2],false);
			SeaBackKit.sendAllianceMessage(title,endContent,
				alliance.getPlayerList(),Message.SYSTEM_ONE_TYPE,null,0);
			return;
		}
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]!=0)
			{
				String endContent=getMessageContent(content,other,i,
					objectFactory,0);
				Alliance alliance=objectFactory.getAlliance(rankValue[i],
					false);
				SeaBackKit.sendAllianceMessage(title,endContent,
					alliance.getPlayerList(),Message.SYSTEM_ONE_TYPE,null,0);
			}
		}
	}

	/** 联盟战战斗结束以后公布 岛屿占有的联盟系统邮件 **/
	public void setFightEndMessage(CreatObjectFactory factory)
	{
		if(rankValue[0] ==0 && rankValue[2]==0) return ;
		String title=null;
		String content=null;
		if(win_Id!=0)
		{
			Alliance winAlliance=factory.getAlliance(win_Id,false);
			// 邮件主题
			title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_fight_win_title");
			// 邮件内容
			content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_fight_win_content");
			String islandName=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_battle_island_"+getSid());
			title=TextKit.replace(title,"%",islandName);
			content=TextKit.replace(content,"%",islandName);
			content=TextKit.replace(content,"%",winScience+"");

			SeaBackKit.sendAllianceMessage(title,content,
				winAlliance.getPlayerList(),0,allianceFightPlayer,awardSid);
		}
		if(lose_Id!=0)
		{
			Alliance loseAlliance=factory.getAlliance(lose_Id,false);
			// 邮件主题
			title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_fight_lose_title");
			// 邮件内容
			content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_fight_lose_content");
			String islandName=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_battle_island_"+getSid());
			title=TextKit.replace(title,"%",islandName);
			content=TextKit.replace(content,"%",islandName);
			content=TextKit.replace(content,"%",loseScience+"");
			SeaBackKit.sendAllianceMessage(title,content,
				loseAlliance.getPlayerList(),0,allianceFightPlayer,loseSid);
		}
		if(win_Id==0 || lose_Id==0)
		{
			// 邮件主题
				title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_battle_abstain");
				content=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"alliance_battle_abstain_content");
				String islandName=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"alliance_battle_island_"+getSid());
				content=TextKit.replace(content,"%",islandName);
				content=TextKit.replace(content,"%",abstainScience+"");
			if(rankValue[0] !=0 && !checkJoin(rankValue[0]))
			{
				Alliance alliance=factory.getAlliance(rankValue[0] ,false);
				SeaBackKit.sendAllianceMessage(title,content,
					alliance.getPlayerList(),0,null,0);
			}
			if(rankValue[2]!=0 && !checkJoin(rankValue[2]))
			{
				Alliance alliance=factory.getAlliance(rankValue[2] ,false);
				SeaBackKit.sendAllianceMessage(title,content,
					alliance.getPlayerList(),0,null,0);
			}
		}
	}

	/** 获取邮件的内容 */
	public String getMessageContent(String content,String exterCont,
		int index,CreatObjectFactory objectfactory,int uindex)
	{
		String islandName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_battle_island_"+getSid());
		content=TextKit.replace(content,"%",islandName);
		if(exterCont.trim().length()!=0)
		{
			Alliance alliance1=objectfactory.getAlliance(rankValue[uindex],
				false);
			exterCont=TextKit.replace(exterCont,"%",alliance1.getName());
			content=TextKit.replace(content,"%",exterCont);
			return content;
		}
		content=TextKit.replace(content,"%","");
		return content;
	}
	/** 竞标(失败)发邮 **/
	public void sendMessage(Alliance alliance,Player player,int all_matrerial,
		Alliance lose,int add,CreatObjectFactory objectFactory)
	{
		// 岛屿名称 加注还是押注类型,资源量，联盟的资源量
		AllianceEvent event=new AllianceEvent(
			AllianceEvent.ALLIANCE_EVENT_BET_SUCCESS,player.getName(),"",
			getSid()+","+all_matrerial,TimeKit.getSecondTime());
		alliance.addEvent(event);
		if(lose!=null)
		{
			// 邮件主题
			String title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_bet_lose");
			// 邮件内容
			String content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_bet_lose_content");
			String islandName=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_battle_island_"+getSid());
			content=TextKit.replace(content,"%",islandName);
			content=TextKit.replace(content,"%",alliance.getName());
			content=TextKit.replace(content,"%",all_matrerial+"");
			content=TextKit.replace(content,"%",add+"");
			SeaBackKit.sendAllianceMessage(title,content,
				lose.getVicePlayers(),lose.getMasterPlayerId(),null,0);
		}
	}
	
	
	/**合服返回玩家物资信息**/
	public void rebackAllianceMaterial(CreatObjectFactory factory)
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]!=0)
			{
				Alliance alliance=factory.getAlliance(rankValue[i],true);
				if(alliance!=null)
				{
					alliance.addMaterial(rankValue[i+1]);
					factory.createSciencePointTrack(
						SciencePointTrack.REBACK_BY_COMBINED,
						alliance.getId(),rankValue[i+1],
						alliance.getSciencepoint(),SciencePointTrack.ADD,0,
						SciencePointTrack.MATERIAL,getSid()+"");
				}
			}
		}
	}
	
	/**记录岛屿上的玩家信息**/
	public String recordPlayersLog()
	{
		StringBuffer sb=new StringBuffer();
		Object object[]=allianceFightPlayer.valueArray();
		for(int j=0;j<object.length;j++)
		{
			PlayerAllianceFight pfight=(PlayerAllianceFight)object[j];
			if(sb.length()==0)
				sb.append(pfight.getPlayerId()+":"+pfight.getAllianceId());
			else
				sb.append(":"+pfight.getPlayerId()+":"
					+pfight.getAllianceId());
		}
		return sb.toString();
	}
	
	/**战斗结束以后记录**/
	public void saveFightOverLog(CreatObjectFactory factory)
	{
		factory.createAllianceFightRecordTrack(win_Id,lose_Id,"","",
			Stage.STAGE_FOUR,getSid(),
			AllianceFightRecordTrack.SAVE_BATTLE_INFO,0,0);
	}

	/**保存结束的竞标信息**/
	public void saveBetLogTime(CreatObjectFactory factory)
	{
		factory.createAllianceFightRecordTrack(0,0,"",getRankValues(),Stage.STAGE_TWO,getSid(),
			AllianceFightRecordTrack.SAVE_BET_INFO,0,0);
	}
	
	/**根据联盟的id 设置物资**/
	public void setChangeRankValue(int aid)
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]!=aid) continue;
			rankValue[i]=0;
			rankValue[i+1]=0;
		}

	}
	public int getWin_Id()
	{
		return win_Id;
	}

	public void setWin_Id(int win_Id)
	{
		this.win_Id=win_Id;
	}

	public int getlose_Id()
	{
		return lose_Id;
	}

	public void setlose_Id(int lose_Id)
	{
		this.lose_Id=lose_Id;
	}

	public IntList getFirstList()
	{
		return firstList;
	}

	public void setFirstList(IntList firstList)
	{
		this.firstList=firstList;
	}

	public IntList getlastlist()
	{
		return lastlist;
	}

	public void setlastlist(IntList lastlist)
	{
		this.lastlist=lastlist;
	}

	public boolean isState()
	{
		return state;
	}

	public void setState(boolean state)
	{
		this.state=state;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

	public boolean isAttack()
	{
		return attack;
	}

	public void setAttack(boolean attack)
	{
		this.attack=attack;
	}

	public int getOutput()
	{
		return output;
	}

	public void setOutput(int output)
	{
		this.output=output;
	}

	public int[] getRankValue()
	{
		return rankValue;
	}

	public void setRankValue(int[] rankValue)
	{
		this.rankValue=rankValue;
	}

	
	public int getLoseSid()
	{
		return loseSid;
	}

	
	public void setLoseSid(int loseSid)
	{
		this.loseSid=loseSid;
	}
	
	
}
