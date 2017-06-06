package foxu.sea.arena;

import java.util.ArrayList;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.net.DataAccessException;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.FightScene;
import foxu.fight.FightScene.FightEvent;
import foxu.sea.HCityNpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.checkpoint.CheckPoint;
import foxu.sea.checkpoint.HCityCheckPoint;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.OfficerBattleHQ;
import foxu.sea.task.TaskEventExecute;

/***
 * 联合舰队管理类
 * 
 * @author lihongji
 * 
 */
public class CombinedFleetManager
{

	CreatObjectFactory objectFactory;

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	// 得到随机种子
	public ByteBuffer getRandom(Player player,ByteBuffer data)
	{

		IntKeyHashMap getrandom=player.getGetrandom();
		int checkPointSid=data.readUnsignedShort();// 读取当前关卡的sid
		data.clear();
		if(getrandom.get(checkPointSid)!=null)
			data.writeInt((Integer)getrandom.get(checkPointSid));
		else
		{
			int random=MathKit.randomInt();
			getrandom.put(checkPointSid,random);
			data.writeInt((Integer)getrandom.get(checkPointSid));
		}

		return data;
	}
	/**
	 * 联合舰队攻打关卡
	 * 
	 * @param player
	 * @param data
	 * @param type
	 * @param arenaManager
	 * @return
	 */
	public ByteBuffer comBinedFleetFight(Player player,ByteBuffer data,
		Integer type)
	{
		int cEnergy=player.getIsland().gotEnergy(TimeKit.getSecondTime());// 精力
		if(cEnergy<=0) throw new DataAccessException(0,"energy is 0");
		int checkPointSid=data.readUnsignedShort();// 读取当前关卡的sid
		CheckPoint checkPoint=(CheckPoint)CheckPoint.factory
			.newSample(checkPointSid);
		if(checkPoint==null)
			throw new DataAccessException(0,"checkPoint is null");
		int inde=checkPoint.getIndex();//得到当前的index
		int chapter=checkPoint.getChapter();
		HCityCheckPoint point=player.getHeritagePoint();// 创建一个遗忘都市 关卡的对象
		int attckTime=TimeKit.getSecondTime();// 当前攻打的时间
		int aleaderattckTime=point.getAttckTime();// 原来攻打的时间
		boolean flash=SeaBackKit.isSameWeek(attckTime,aleaderattckTime);
		if(!flash)
		{
			point.clear();
			point.setAttckTime(attckTime);
		}
		int [] attacklist=player.getHeritagePoint().getAttackList();
		if(chapter<=attacklist.length)
		{
			int code=attacklist[chapter-1];
			if(((code>>>inde)&1)==1)
			{
				throw new DataAccessException(0,"checkPointSid is attact");
			}
		}
		FleetGroup[] fleetlists=getPlayerlistFleetGroup(player);// 得到几组舰队群

		if(point.getCheckPointSid()<checkPointSid)// 判断
		{
			throw new DataAccessException(0,"you can not fight the sid");
		}
		int shipdestorylength=0;// 联合舰队损失舰船的坑位长度
		FleetGroup playergroup=null;// 玩家舰队
		FleetGroup npcgroup=null;// npc的舰队
		FightScene scene=null;
		int groups=getplayerLength(player.getHeritagePoint().getSetshipFleets());
		int successStar=0;
		int playerrecode=0;// 玩家的记录
		int npcrecode=0;// npc的记录
		HCityNpcIsland ncf=(HCityNpcIsland)checkPoint.getIsland();// 得到配置文件中的sid相同的NpcCreatFleet对象
		int npcfleetlength=ncf.getGroup();// 得到NPC有几组舰队
		SeaBackKit.resetPlayerSkill(player,objectFactory);// 重新计算技能
//		FightScene[] scenes=new FightScene[10];// todo
		ArrayList<FightScene> scenes=new ArrayList<FightScene>();
		// IntList scenes=new IntList();
		int sc=0;
		FleetGroup[] npcFleetGroups=ncf.createFleetGroup(player,
			checkPointSid,ncf.getGroup());
		FleetGroup[] destoryFleetGroups=new FleetGroup[groups];
		FightShowEventRecord[] record=new FightShowEventRecord[10];
		while(playerrecode<groups&&npcrecode<npcfleetlength)
		{
			if(playergroup==null) playergroup=fleetlists[playerrecode];
			
			if(npcgroup==null)
			{
				npcgroup=npcFleetGroups[npcrecode];
				ncf.setFleetGroup(npcgroup);
			}

			Object[] object=checkPoint.fight(playergroup);// 进入关卡战斗
			scene=(FightScene)object[0];
			FightShowEventRecord r=(FightShowEventRecord)object[1];// 得到record
			scenes.add(scene);
			record[sc]=r;
			sc++;
			destoryFleetGroups[playerrecode]=playergroup;
			if(scene.getSuccessTeam()==0)
			{
				npcrecode++;
				npcgroup=null;
			}
			else
			{
				playerrecode++;
				playergroup=null;
			}
		}
		data.clear();
		ByteBuffer[] fight=new ByteBuffer[sc];

		for(int i=0;i<sc;i++)
		{
			fight[i]=record[i].getRecord();
			int top=fight[i].top();
			if(i<sc-1)
			{
				fight[i].setTop(top-2);
				fight[i].writeByte(FightEvent.FIGHT_CONTINUE.ordinal());
				fight[i].setTop(top);
			}
			if(i>0)
			{
				fight[i].setOffset(1);
			}
		}
		if(scenes.get(sc-1).getSuccessTeam()==0)
		{
			// 如果本关有星数记录说明已经成功攻打过，不需要推送系统消息
//			boolean isFinished=point.getStar(chapter,inde)>0;
			IntKeyHashMap getrandom=player.getGetrandom();
			int randomseed=MathKit.randomInt();
			getrandom.put(checkPointSid,randomseed);
			// player.setGetrandom(getrandom);
			int shiplength=getlocationlength(player.getHeritagePoint().getSetshipFleets());
			shipdestorylength=getdestoryshipLength(playerrecode,
				destoryFleetGroups,shipdestorylength);
			// 是否要刷新
			
		
			// 奖励
			checkPoint.heritagefightSuccess(player,shiplength,
				shipdestorylength,data,objectFactory,type);
			player.setPlunderResource(player.getHeritagePoint()
				.getAllstars()+player.getSelfCheckPoint().getAllstars());//改变星星的排行
			int[] chapterwards=player.getHeritagePoint().getCharteraward();
			int key=(chapter-1)/32;
			int index=(chapter-1)%32;
			boolean flager=key>=chapterwards.length?true
				:((chapterwards[key]>>>index)&1)==0;
			if(flager)
				flager=player.getHeritagePoint().CheckAttacklist(chapter-1);
			// 是否有章节奖励
			SeaBackKit.combindeFightRecord(groups,npcfleetlength,data,fight,
				player.getName(),player.getLevel(),checkPoint.getName(),
				checkPoint.getPointLevel(),checkPoint.getFightType(),player,
				null,fleetlists[0],npcFleetGroups[0]);
			if(flager)
			{
				data.writeByte(1);
//				if(key>=chapterwards.length)
//				{
//					int[] temp=new int[key+1];
//					System.arraycopy(chapterwards,0,temp,0,
//						chapterwards.length);
//					chapterwards=temp;
//				}
//				chapterwards[key]+=1<<index;
				// 发放遗忘都市章节奖励
//				checkPoint.heritagAward(chapter,player,shiplength,
//					shipdestorylength,data,objectFactory,type);
//				if(!isFinished)
//				{
//					//发送系统消息
//					String message=InterTransltor.getInstance()
//									.getTransByKey(PublicConst.SERVER_LOCALE,
//										"success_hei_check_point");
//								String checkPointString=InterTransltor.getInstance()
//									.getTransByKey(PublicConst.SERVER_LOCALE,
//										"success_hei_check_"+HCityCheckPoint.award[chapter-1]);
//								message=TextKit
//									.replace(message,"%",player.getName());
//								message=TextKit
//									.replace(message,"%",checkPointString);
//								SeaBackKit.sendSystemMsg(objectFactory
//									.getDsmanager(),message);
//				}
			}
			else
			{
				data.writeByte(0);
			}
			//战斗胜利时，扣除精力
			player.reDuceEnergy();
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.HCITY_POINT_EVENT,null,player,null);
		}
		else
		{
			data.writeShort(checkPointSid);
			data.writeByte(successStar);
			SeaBackKit.combindeFightRecord(groups,npcfleetlength,data,fight,
				player.getName(),player.getLevel(),checkPoint.getName(),
				checkPoint.getPointLevel(),checkPoint.getFightType(),player,
				null,fleetlists[0],npcFleetGroups[0]);
			data.writeByte(0);
		}
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.ATTACK_POINT_TASK_EVENT,null,player,
			null);
		return data;

	}
	/***
	 * 得到损失船坑位的长度
	 * 
	 * @param playerrecode
	 * @param fleetlists
	 * @param shipdestorylength
	 * @return
	 */
	public int getdestoryshipLength(int playerrecode,
		FleetGroup[] fleetlists,int shipdestorylength)
	{
		for(int j=0;j<=playerrecode;j++)
		{
			Fleet[] fleetnow=fleetlists[j].getArray();
			for(int i=0;i<fleetnow.length;i++)
			{

				if(fleetnow[i]!=null)
				{
					if(fleetnow[i].getNum()==0)
					{
						shipdestorylength+=1;
					}
				}
			}
		}
		// }
		return shipdestorylength;
	}

	/** 检查是否舰队数量足够 */
	public String fleetscheckShipNumLimit(IntKeyHashMap map,IntList list,
		ByteBuffer data,Player player,FleetGroup mainGroup,int commanderNum)
	{
		IntKeyHashMap haveNums=new IntKeyHashMap();
		int index=player.getPlayerType()-PublicConst.COMMANDER_OPEN_LEVEL;
		if(commanderNum>0)
		{
			if(index<0)
			{
				return "playerType limit";// 判断军衔
			}
			if(PublicConst.CLEVEL_CNUM[index]<commanderNum)
			{
				return "commander book max";// 使用数量过多
			}
			if(!player.checkPropEnough(PublicConst.COMMANDER_LEVEL_UP_SID,
				commanderNum))
			{
				return "commander book limit";// 判断统御书是否够
			}
		}
		int comShipNum=commanderNum*PublicConst.COMMANDER_NUM;// 统御书额外带兵量
		// 军官提供的额外带兵量
		comShipNum+=(int)mainGroup.getOfficerFleetAttr()
			.getCommonAttr(OfficerBattleHQ.ARMY,Ship.ALL_SHIP,
				PublicConst.EXTRA_SHIP,true,0);
		int g=data.readUnsignedByte();
		int[] time=new int[g];
		for(int j=0;j<g;j++)
		{
			int times=data.readUnsignedByte();
			if(times==0) continue;
			time[j]=times;
			for(int i=0;i<times;i++)
			{
				int location=data.readUnsignedByte();
				int shipSid=data.readUnsignedShort();
				int num=data.readUnsignedShort();
				// 不能带城防兵力
				Ship ship=(Ship)Ship.factory.getSample(shipSid);
				if(ship==null
					||(ship.getPlayerType()==Ship.POSITION_AIR
						||ship.getPlayerType()==Ship.POSITION_MISSILE||ship
						.getPlayerType()==Ship.POSITION_FIRE))
					return "sid error";
				// 判断舰队是否足够
				Integer haveNum=(Integer)haveNums.get(shipSid);
				if(haveNum==null)
				{
					haveNum=player.getIsland().getShipsBySid(shipSid,
						player.getIsland().getTroops());
					if(mainGroup!=null)
					{
						haveNum+=mainGroup.getShipBySid(shipSid);
					}
					haveNums.put(shipSid,haveNum);
				}

				if(haveNum<num) return "ship num is limit";
				// 从总数里减去
				haveNums.put(shipSid,haveNum-num);
				int locationExtra=(int)mainGroup.getOfficerFleetAttr()
					.getCommonAttr(OfficerBattleHQ.ARMY,
						OfficerBattleHQ.CURRENT_LOCATION,
						PublicConst.EXTRA_SHIP,true,location);
				/** 带兵数量的限制 */
				if(num>player.getShipNum()+comShipNum+locationExtra)
					return "you can not take more";
				list.add(location);
				list.add(shipSid);
				list.add(num);
			}
		}
		map.put(0,time);
		return null;
	}

	/***
	 * Hashmap转换成List<FleetGroup> 舰队
	 * @param player
	 * @return
	 */
	public FleetGroup[] getPlayerlistFleetGroup(Player player)
	{
		IntKeyHashMap map=player.getHeritagePoint().getSetshipFleets();
		int[] times=(int[])map.get(0);
		int groups=getplayerLength(map);
		FleetGroup[] fleetlists=new FleetGroup[groups];
		int flag=1;
		int fl=0;
		for(int i=0;i<times.length;i++)
		{
			FleetGroup fleetgroup=new FleetGroup();
			fleetgroup.getOfficerFleetAttr().initOfficers(player);
			int location;
			int num;
			int sid;
			for(int j=0;j<times[i];j++)
			{
				if(times[i]==0) continue;
				int[] ships=(int[])map.get(flag);
				Fleet fleet=new Fleet();
				location=ships[0];
				sid=ships[1];
				num=ships[2];
				if(sid==0) continue;
				fleet.setPlayter(player);
				fleet.initNum(num);
				fleet.setLocation(location);
				fleet.setShip((Ship)Ship.factory.newSample(sid));
				fleetgroup.setFleet(location,fleet);
				flag++;
			}
			if(times[i]!=0)
			{
				fleetlists[fl]=fleetgroup;
				fl++;
			}
		}
		return fleetlists;
	}
	public int getplayerLength(IntKeyHashMap map)
	{
		int[] s=(int[])map.get(0);
		int s1=0;
		for(int i=0;i<s.length;i++)
		{
			if(s[i]>0) s1++;
		}
		return s1;
	}
	public int getlocationlength(IntKeyHashMap map)
	{
		int[] s=(int[])map.get(0);
		int le=0;
		for(int i=0;i<s.length;i++)
		{
			if(s[i]>0)
			{
				le+=s[i];
			}
		}
		return le;
	}

}
