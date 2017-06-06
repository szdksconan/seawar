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
 * ��������ս����
 * 
 * @author lhj
 * 
 */
public class BattleIsland extends Sample
{

	Logger log=LogFactory.getLogger(BattleIsland.class);
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	/** ս���Ƚ��� **/
	FightScoreCompare fightScompare=new FightScoreCompare();
	/** �������� **/
	String name;
	/** ʤ�������� **/
	int win_Id;
	/** ʧ�ܵ����� **/
	int lose_Id;
	/** ʱ�� **/
	int output;
	/** Ѻע�ĵ׼� **/
	int lowPrice;
	/** ����id - �������� **/
	int[] rankValue={0,0,0,0};
	/** ����ʤ���Ľ��� **/
	int awardSid;
	/**����ʧ�ܵĽ���**/
	int loseSid;
	/** ����ʤ�������˷��ŵĿƼ��� **/
	int winScience;
	/** ����ʧ�ܵ����˷��ŵĿƼ��� **/
	int loseScience;
	/** ��Ȩ�����˷��ŵĿƼ��� **/
	int abstainScience;
	/** �������ľ���״̬(�費��Ҫ��������ս) **/
	boolean state=false;
	/** ���ֵ�˳�� ��ΪrankList���������Եڶ����ȳ��� **/
	boolean attack=true;
	/** ��������ս���� playerAllianceFight **/
	IntKeyHashMap allianceFightPlayer=new IntKeyHashMap();
	/** ������һ����ս���е���� **/
	IntList firstList=new IntList();
	/** �����ڶ�����ս���е���� **/
	IntList lastlist=new IntList();

	/** ��⾺��ֵ **/
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

	/** ���˽���Ѻע **/
	public String addBetIsland(int materialNum,Alliance alliance,
		CreatObjectFactory objectFactory,Player player)
	{
		String result=checkMaterial(alliance,materialNum);
		if(result!=null) return result;
		addBetValue(materialNum,alliance,objectFactory,player);
		return null;
	}
	/** ��ʼ���� **/
	public void addBetValue(int materialNum,Alliance alliance,
		CreatObjectFactory objectFactory,Player player)
	{
		// �۳���������
		alliance.reduceMaterial(materialNum);
		/** ���� **/
		objectFactory.createSciencePointTrack(SciencePointTrack.BET_ISLAND,
			alliance.getId(),materialNum,alliance.getMaterial(),
			SciencePointTrack.REDUCE,player.getId(),
			SciencePointTrack.MATERIAL,getSid()+","
				+getRankByAllianceId(alliance.getId()));
		//ˢ�����а�
		sortRankValue();
		//����ս��¼��־
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
				/** ���� **/
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
		// �����ʼ�
		sendMessage(alliance,player,all_materialNum,lose,oldvaue,objectFactory);
	}

	/** �������˿Ƽ��� **/
	public void addAllianceSciencePoint(CreatObjectFactory objectFactory)
	{
		if(win_Id==0) return;
		Alliance alliance=objectFactory.getAlliance(win_Id,true);
		if(alliance==null) return;
		alliance.addSciencepoint(output);
		JBackKit.sendAllianceWarResource(objectFactory,alliance);
		/** �Ƽ��� **/
		objectFactory.createSciencePointTrack(
			SciencePointTrack.FROM_GIVE_VALUES,alliance.getId(),
			PublicConst.ADD_SCIENCEPOINT,alliance.getSciencepoint(),
			SciencePointTrack.ADD,0,
			SciencePointTrack.SCIENCE_POINT,getSid()+"");
	}

	/** ��֤�����ҿ��Լ�������ս **/
	public boolean isHavePlayer(int playerId)
	{
		if(allianceFightPlayer.get(playerId)==null) return false;
		return true;
	}

	/** ����������� **/
	public void addAllianceFightPlayer(PlayerAllianceFight p_fight)
	{
		allianceFightPlayer.put(p_fight.getPlayerId(),p_fight);
	}

	/** ��ȡ����ս�������� **/
	public PlayerAllianceFight getPlayerFight(int playerId)
	{
		return (PlayerAllianceFight)allianceFightPlayer.get(playerId);
	}

	/** ��ȡ�����л�����ս������� **/
	public PlayerAllianceFight[] getPlayerFight(int allianceId,
		IntList fightList)
	{
		if(allianceFightPlayer.size()==0) return null;
		addFightList(allianceId,fightList);
		// �п���һ���Ѿ�û����ҿ���ս����
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

	/** ��ӻ��ܹ�ս������� **/
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

	/** ͨ������id��ȡ��ǰ�ľ���ֵ **/
	public int getRankByAllianceId(int aid)
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(aid==rankValue[i]) return rankValue[i+1];
		}
		return 0;
	}

	/** ��ȡ��ǰ����������ʽ� **/
	public int getRankLowPrice(int allianceId)
	{
		int num=getRankByAllianceId(allianceId);
		if(num!=0) return num;
		// ���ص�������id ���±�
		return rankValue[getLowIndex()+1]==0?lowPrice
			:rankValue[getLowIndex()+1];
	}

	/** ���þ���ֵ(�ӱ�) **/
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
	/** ��ȡ��ǰ����;���ֵ���±� **/
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

	/** ��ȡ�ı�λ�±� **/
	public int getTargetIndex(int id)
	{
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]==id) return i;
		}
		return getLowIndex();
	}
	/** ���л����˾�����Ϣ **/
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
			// ��������
			data.writeUTF(alliance.getName());
			// ����ֵ
			data.writeInt(rankValue[i+1]);
		}
	}
	/** ��ȡ��ǰ�����˾���ĳ��� **/
	public int getAlliancesBetLength()
	{
		int num=0;
		for(int i=0;i<rankValue.length;i+=2)
		{
			if(rankValue[i]!=0) num++;
		}
		return num;
	}
	/** ��ȡ�ַ�����ʽ�����˾�����Ϣ **/
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

	/**����ս��������**/
	public void sendFightEndTask(CreatObjectFactory objectFactory)
	{
		battleIslandWin();
		//��ռ��־
		saveFightOverLog(objectFactory);
		sendAward(objectFactory);
		setFightEndMessage(objectFactory);
		setFightEndSystemMessage(objectFactory);
		rebackPlayerShips(objectFactory);
		// �����ҵĴ�ֻ��¼
		clearRecord();
		JBackKit.sendAllianceBetInfo(null,objectFactory);
	}

	/** ��������Ӯ�� **/
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
	/** ����ս�����Ժ󷢽��� **/
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
				/** �Ƽ��� **/
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
				/** �Ƽ��� **/
				factory.createSciencePointTrack(
					SciencePointTrack.FROM_ALLIANCE_FIGHT,lostAlliance.getId(),
					result,lostAlliance.getSciencepoint(),
					SciencePointTrack.ADD,0,
					SciencePointTrack.SCIENCE_POINT,getSid()+"");
			}
			JBackKit.sendAllianceWarResource(factory,lostAlliance);
		}
	}
	/** ���ظ���ҵĴ�ֻ��Ϣ **/
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
			// ������������ս�������Ժ���ʣ��Ĵ�ֻ
			for(int j=0;j<shipList.size();j+=3)
			{
				player.getIsland().addTroop(shipList.get(j),
					shipList.get(j+1),player.getIsland().getTroops());
			}
			// ��ֻ��־
			objectFactory.addShipTrack(0,ShipCheckData.ALLIANCE_BACK_SHIP,
				player,shipList,null,false);
			JBackKit.sendResetTroops(player);
		}
	}

	/** �ж����Ƿ���Ҫ��������ս **/
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

	/** ���л���ֻ��Ϣ **/
	public void showBytesWriteShips(ByteBuffer data,int playerId)
	{
		PlayerAllianceFight pfight=getPlayerFight(playerId);
		IntList list=pfight.getList();
		// �����ұ���̭��0
		if(pfight.isOut()||list==null||list.size()==0)
		{
			data.writeByte(0);
			return;
		}
		// ��λ��
		int length=list.size()/3;
		data.writeByte(length);
		for(int i=0;i<list.size();i+=3)
		{
			// sid
			data.writeShort(list.get(i));
			data.writeShort(list.get(i+1));
			// λ��
			data.writeByte(list.get(i+2));
		}
	}

	/** ���˾������� **/
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

	/** ��֤�Ƿ����� **/
	public boolean sort()
	{
		int endValue=rankValue[rankValue.length-1];
		for(int i=0;i<rankValue.length-1;i+=2)
		{
			if(endValue>rankValue[i+1]) return true;
		}
		return false;
	}

	/** �Ƿ���Ҫ��������ս **/
	public boolean checkNeedFight()
	{
		if(firstList.size()==0||lastlist.size()==0) return true;
		return false;
	}

	/** ���˲������Ҹ�����ҵ�ս������������ **/
	public boolean fightRankList(int allianceId,IntList fightList)
	{
		PlayerAllianceFight[] fights=getPlayerFight(allianceId,fightList);
		if(fights==null) return false;
		SetKit.sort(fights,fightScompare);// ս������
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

	/** �����¼ **/
	public void clearRecord()
	{
		firstList.clear();
		lastlist.clear();
		allianceFightPlayer.clear();
	}

	/** �жϵ�ǰ�����Ƿ�����ұ��� **/
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
	
	/**��������ڵ�����Ƿ���û�г��ֵ�**/
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

	/** ����ϵͳ��Ϣ��֪�������x ���� ռ���˵��� **/
	public void setFightEndSystemMessage(CreatObjectFactory objectFactory)
	{
		if(win_Id==0) return;
		Alliance alliance=objectFactory.getAlliance(win_Id,false);
		if(alliance==null) return;
		// ����ϵͳ��Ϣ
		String messcontent=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_fight_system_content");
		messcontent=TextKit.replace(messcontent,"%",alliance.getName());
		String islandName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_battle_island_"+getSid());
		messcontent=TextKit.replace(messcontent,"%",islandName);
		SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),messcontent);
	}

	/** �����Ϣ���� **/
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

	/** ���л� **/
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

	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		state=data.readBoolean();
		attack=data.readBoolean();
		win_Id=data.readInt();
		return this;
	}

	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeBoolean(state);
		data.writeBoolean(attack);
		data.writeInt(win_Id);
	}

	/** ���л�������Ϣ **/
	public void showBytesBattleInfo(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		showByteBattleBet(data,objectFactory);
		showBytesWriteAllianceFight(data,objectFactory);
	}

	/** ���л����굺�����Ϣ **/
	public void showByteBattleBet(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		data.writeInt(getSid());
		data.writeInt(getSid());
		// ʱ��
		data.writeInt(getOutput());
		Alliance alliance=objectFactory.getAlliance(win_Id,false);
		data.writeUTF(alliance==null?"":alliance.getName());
		// ���л����˾�����Ϣ
		showBytesRankValue(data,objectFactory);
	}

	/** ÿ����������ս����Ϣ **/
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

	/** ���л� ���˾�����Ϣ �����˵Ĳ�����Ա **/
	public void showByteAllianceFight(ByteBuffer data,int allianceId,
		CreatObjectFactory objectFactory)
	{
		Alliance alliance=objectFactory.getAlliance(allianceId,false);
		data.writeInt(allianceId);
		data.writeUTF(alliance.getName());
		int top=data.top();
		data.writeShort(0);
		int length=0;
		// ���л���Ҳ�����Ա
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

	/** �����¼ **/
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

	/** ��������Ժ��ʼ� **/
	public void sendBetEndMessage(CreatObjectFactory objectFactory)
	{
		// �ʼ�����
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_bet_message");
		// �ʼ�����
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

	/** ����սս�������Ժ󹫲� ����ռ�е�����ϵͳ�ʼ� **/
	public void setFightEndMessage(CreatObjectFactory factory)
	{
		if(rankValue[0] ==0 && rankValue[2]==0) return ;
		String title=null;
		String content=null;
		if(win_Id!=0)
		{
			Alliance winAlliance=factory.getAlliance(win_Id,false);
			// �ʼ�����
			title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_fight_win_title");
			// �ʼ�����
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
			// �ʼ�����
			title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_fight_lose_title");
			// �ʼ�����
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
			// �ʼ�����
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

	/** ��ȡ�ʼ������� */
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
	/** ����(ʧ��)���� **/
	public void sendMessage(Alliance alliance,Player player,int all_matrerial,
		Alliance lose,int add,CreatObjectFactory objectFactory)
	{
		// �������� ��ע����Ѻע����,��Դ�������˵���Դ��
		AllianceEvent event=new AllianceEvent(
			AllianceEvent.ALLIANCE_EVENT_BET_SUCCESS,player.getName(),"",
			getSid()+","+all_matrerial,TimeKit.getSecondTime());
		alliance.addEvent(event);
		if(lose!=null)
		{
			// �ʼ�����
			String title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_bet_lose");
			// �ʼ�����
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
	
	
	/**�Ϸ��������������Ϣ**/
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
	
	/**��¼�����ϵ������Ϣ**/
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
	
	/**ս�������Ժ��¼**/
	public void saveFightOverLog(CreatObjectFactory factory)
	{
		factory.createAllianceFightRecordTrack(win_Id,lose_Id,"","",
			Stage.STAGE_FOUR,getSid(),
			AllianceFightRecordTrack.SAVE_BATTLE_INFO,0,0);
	}

	/**��������ľ�����Ϣ**/
	public void saveBetLogTime(CreatObjectFactory factory)
	{
		factory.createAllianceFightRecordTrack(0,0,"",getRankValues(),Stage.STAGE_TWO,getSid(),
			AllianceFightRecordTrack.SAVE_BET_INFO,0,0);
	}
	
	/**�������˵�id ��������**/
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
