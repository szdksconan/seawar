package foxu.sea.port;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.ds.SWDSManager;
import foxu.fight.FightScene;
import foxu.sea.AllianceBossNpcIsland;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.WarManicActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceBoss;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.task.TaskEventExecute;

/** 联盟boss挑战端口 1017 */
public class AllianceBossPort extends AccessPort
{

	/** 每次挑战获得的声望 */
	public static final int HONOR=5;
	/** 每天每个玩家可以攻打的次数 */
	public static final int MAX_FIGHT_NUM=5;

	/** 增值物品sid */
	public static final int REDUCE_PROP=2001,STOP_DROP=2002;

	/** type ATTACK_BOSS=1攻击BOSS GET_AWARD=2获取奖励品 */
	public static final int ATTACK_BOSS=1,GET_BOSS_HP=2,GET_AWARD=3;

	/** 数据获取类 */
	CreatObjectFactory objectFactory;

	/** dsmanager */
	SWDSManager dsmanager;
	Object lock=new Object();

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		Alliance alliance=null;
		// 检查有无联盟
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			alliance=(Alliance)objectFactory.getAllianceMemCache().loadOnly(
				player.getAttributes(PublicConst.ALLIANCE_ID));
		}
		if(alliance==null)
			throw new DataAccessException(0,"you have no alliance");
		// 检查是否刷新
		alliance.getBoss().checkFlush();
		// 攻击boss
		if(type==ATTACK_BOSS)
		{
			// boss Sid
			int bossSid=data.readUnsignedShort();
			AllianceBossNpcIsland island=alliance.getBoss().getBossGroup(
				bossSid);
			if(island==null)
			{
				throw new DataAccessException(0,"sid fail");
			}
			if(island.getLimitLevel()>alliance.getAllianceLevel())
				throw new DataAccessException(0,"alliance level limit");
			synchronized(lock)
			{
			// boss已经打过
			if(island.getFleetGroup().nowTotalNum()<=0)
			{
				throw new DataAccessException(0,"boss dead");
			}
			// 次数限制
			int time=0;
			int nowDay=SeaBackKit.getDayOfYear();
			if(player.getAttributes(PublicConst.ALLIANCE_BOSS_FIGHT)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_BOSS_FIGHT)
					.equals(""))
			{
				int value=Integer.parseInt(player
					.getAttributes(PublicConst.ALLIANCE_BOSS_FIGHT));
				int day=value>>16;
				int times=value<<16>>16;
				if(nowDay==day)
				{
					time=times;
				}
			}
			// 大于次数
			if(time>=MAX_FIGHT_NUM)
			{
				throw new DataAccessException(0,"today boss fight max");
			}
			int count=data.readUnsignedByte();
			int props[]=new int[count];
			for(int i=0;i<props.length;i++)
			{
				props[i]=data.readUnsignedShort();
				if(props[i]!=REDUCE_PROP&&props[i]!=STOP_DROP)
					throw new DataAccessException(0,"propSid is wrong");
			}
			data.readUnsignedByte();//神统
			boolean reduce=false;
			boolean stop=false;
			// 检查是否有物品
			for(int i=0;i<props.length;i++)
			{
				if(props[i]==0) continue;
				if(!player.getBundle().checkDecrProp(props[i]))
				{
					throw new DataAccessException(0,"prop not enough");
				}
				else
				{
					if(props[i]==REDUCE_PROP)
					{
						reduce=true;
					}
					else if(props[i]==STOP_DROP)
					{
						stop=true;
					}
				}
			}
			
			IntList list=new IntList();
			FleetGroup group=new FleetGroup();
			group.getOfficerFleetAttr().initOfficers(player);
			int length=data.readUnsignedByte();
			if(length==0)
				throw new DataAccessException(0,"you have no ship fight");
			// 打关卡混编 可以加上城防的兵力
			String str=SeaBackKit.checkShipNumLimit(list,length,data,player,player.getIsland().getMainGroup(),0);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
//			 扣除物品
			for(int i=0;i<props.length;i++)
			{
				if(props[i]==0) continue;
				player.getBundle().decrProp(props[i]);
			}
			if(reduce||stop)
			{
				JBackKit.sendResetBunld(player);
			}
			// 组建舰队
			creatFleetGroup(list,player,group,false);
			Object[] object=island.bossFight(group,reduce,stop);
			
			int exp=island.getFleetGroup().hurtTroopsExp(player,
				TimeKit.getSecondTime());
			// 经验值活动
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			//战争狂人
			WarManicActivity activity=(WarManicActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
			if(activity!=null&&activity.isActive(TimeKit.getSecondTime()))
			{
				activity.addPScore(WarManicActivity.ELITE,island
					.getFleetGroup().hurtListNum(),player);
			}
			island.getFleetGroup().resetLastNum();
			FightScene scene=(FightScene)object[0];
			// 如果胜利 添加联盟事件
			if(scene.getSuccessTeam()==0)
			{
				// 联盟事件
				AllianceEvent event=new AllianceEvent(
					AllianceEvent.ALLIANCE_BOSS_FIGHT,player.getName(),
					island.getSid()+"","",TimeKit.getSecondTime());
				alliance.addEvent(event);
				// 如果联盟等级小于最大等级，才获得经验
				boolean canGetExp=alliance.getAllianceLevel()<PublicConst.MAX_ALLIANCE_LEVEL;
				// 添加联盟经验
				if(canGetExp)
				{
					int addLevel=alliance.incrExp(island.getAllianceExp());
					// 增加经验
					if(addLevel>0)
					{
						// 联盟事件
						event=new AllianceEvent(
							AllianceEvent.ALLIANCE_EVENT_LEVEL,"","",alliance
								.getAllianceLevel()
								+"",TimeKit.getSecondTime());
						alliance.addEvent(event);
						// 提示
						String message=InterTransltor.getInstance()
							.getTransByKey(PublicConst.SERVER_LOCALE,
								"alliance_level_up");
						message=TextKit.replace(message,"%",alliance.getName());
						message=TextKit.replace(message,"%",alliance
							.getAllianceLevel()
							+"");
						SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
							message);
					}
					
				}
				ByteBuffer send=new ByteBuffer();
				// 广播
				send.clear();
				send.writeShort(AlliancePort.ALLIANCE_PORT);
				send.writeByte(AlliancePort.EXP_LEVEL_CHANGE);
				send.writeInt(alliance.getAllianceExp());
				send.writeByte(alliance.getAllianceLevel());
				sendAllAlliancePlayers(send,alliance);
			}
			data.clear();
			FightShowEventRecord record=(FightShowEventRecord)object[1];
			ByteBuffer fight=record.getRecord();
			// 增加奖励
			player.incrExp(exp,objectFactory);
			player.incrHonorExp(HONOR);
			data.writeInt(exp);
			data.writeByte(HONOR);
			SeaBackKit.conFightRecord(data,fight,player.getName(),
				player.getLevel(),island.getName(),
				island.getIslandLevel(),PublicConst.FIGHT_TYPE_8,player,
				null,group,island.getFleetGroup(),true,null,null);
			time++;
			int value=nowDay<<16|time;
			player.setAttribute(PublicConst.ALLIANCE_BOSS_FIGHT,value+"");
			
			int totalNum=island.getFleetGroup().nowTotalNum();
			// 广播给联盟在线玩家
			ByteBuffer send=new ByteBuffer();
			send.clear();
			send.writeShort(AlliancePort.ALLIANCE_PORT);
			send.writeByte(AlliancePort.BOSS_SEND_VALUE);
			send.writeShort(island.getSid());
			send.writeByte(alliance.getBoss().getPercent(island.getSid(),
				totalNum));
			sendAllAlliancePlayers(send,alliance);
			//刷新经验，声望
			JBackKit.sendExp(player);
			JBackKit.resetHonor(player);
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_ALLIANCE_TASK_EVENT,null,player,null);
			}
			
		}
		// 获取兵力
		else if(type==GET_BOSS_HP)
		{
			// boss Sid
			int bossSid=data.readUnsignedShort();
			AllianceBossNpcIsland island=alliance.getBoss().getBossGroup(
				bossSid);
			if(island==null)
			{
				throw new DataAccessException(0,"sid fail");
			}
			FleetGroup group=island.getFleetGroup();
			Fleet fleet[]=group.getArray();
			data.clear();
			for(int i=0;i<fleet.length;i++)
			{
				int num=0;
				if(fleet[i]!=null&&fleet[i].getNum()>0)
				{
					num=fleet[i].getNum();
				}
				data.writeShort(num);
			}
		}
		// 获取奖励品
		else if(type==GET_AWARD)
		{
			// boss Sid
			int bossSid=data.readUnsignedShort();
			AllianceBossNpcIsland island=alliance.getBoss().getBossGroup(
				bossSid);
			if(island==null)
			{
				throw new DataAccessException(0,"sid fail");
			}
			int awardSid=island.getAwardSid();
			if(alliance.getBoss().hasBossAward(player.getId(),
				island.getSid()))
			{
				throw new DataAccessException(0,"has been got award");
			}
			// 检查贡献度
			int giveValue=0;
			DonateRank rank=(DonateRank)alliance.getGiveValue().get(player.getId());
			if(rank!=null)
				giveValue=(int)rank.getTotleValue();
			int index=-1;
			for(int i=0;i<AllianceBoss.BOSS_SIDS.length;i++)
			{
				if(bossSid==AllianceBoss.BOSS_SIDS[i])
				{
					index=i;
					break;
				}
			}
			if(index>=0
				&&giveValue<PublicConst.ALLIANCE_GIVE_VALUE_LIMIT[index])
			{
				String message=InterTransltor.getInstance().getTransByKey(
					player.getLocale(),"alliance_gave_value_not_enough");
				message=TextKit.replace(message,"%",String
					.valueOf(PublicConst.ALLIANCE_GIVE_VALUE_LIMIT[index]));
				throw new DataAccessException(0,message);
			}
			alliance.getBoss().addBossAward(player.getId(),island.getSid());
			data.clear();
			Award award=(Award)Award.factory.getSample(awardSid);
			award.awardLenth(data,player,null,null,new int[]{EquipmentTrack.FROM_BOSS});
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GET_ALLIANCE_TASK_EVENT,null,player,null);
		}
		return data;
	}

	/** 扣除城防舰队指定sid船只 */
	public void reduceShips(Player player,int shipSid,int num)
	{
		FleetGroup group=player.getIsland().getMainGroup();
		Fleet fleet[]=group.getArray();
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null) continue;
			if(fleet[i].getShip().getSid()==shipSid&&fleet[i].getNum()>0)
			{
				int reduceNum=group.reduceShipByLocation(fleet[i]
					.getLocation(),num);
				if(reduceNum>=num) return;
				reduceShips(player,shipSid,(num-reduceNum));
				break;
			}
		}
	}

	/** 组建舰队 */
	public boolean creatFleetGroup(IntList list,Player player,
		FleetGroup group,boolean reduce)
	{
		boolean resetMainGroup=false;
		for(int i=1;i<list.size();i+=3)
		{
			int shipSid=list.get(i);
			int num=list.get(i+1);
			if(num<=0) continue;
			int location=list.get(i+2);
			if(reduce)
			{
				int reduceNum=player.getIsland().reduceShipBySid(shipSid,
					num,player.getIsland().getTroops());
				if(reduceNum<num)
				{
					resetMainGroup=true;
					// 扣除城防里面的
					reduceShips(player,shipSid,(num-reduceNum));
				}
			}
			Fleet fleet=new Fleet();
			fleet.setPlayter(player);
			fleet.initNum(num);
			fleet.setLocation(location);
			fleet.setShip((Ship)Ship.factory.newSample(shipSid));
			group.setFleet(location,fleet);
		}
		if(resetMainGroup) JBackKit.resetMainGroup(player);
		return true;
	}

	/** 联盟广播 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance)
	{
		SessionMap smap=dsmanager.getSessionMap();
		Session[] sessions=smap.getSessions();
		Player player=null;
		Connect con=null;
		IntList list=alliance.getPlayerList();
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					for(int j=0;j<list.size();j++)
					{
						if(player.getId()==list.get(j))
						{
							con.send(data);
						}
					}
				}
			}
		}
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public SWDSManager getDsmanager()
	{
		return dsmanager;
	}

	public void setDsmanager(SWDSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}
	
}
