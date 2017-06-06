package foxu.combine;

import java.util.ArrayList;
import java.util.Iterator;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.BattleGroundSave;
import foxu.dcaccess.datasave.BattleIslandSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.alliance.alliancefight.BattleGround;
import foxu.sea.event.FightEvent;
import foxu.sea.kit.FightKit;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.util.TimeKit;


public class CombineManager
{
	public static final String CANOT_REMOVE_PLAYER="can_not_remove_player";
	
	private static Logger log=LogFactory.getLogger(CombineManager.class);
	
	ArrayList<DataTable> tableList=new ArrayList<DataTable>();
	
	Server server1;
	
	Server server2;
	
	Server server3;
	
	CreatObjectFactory objectFactory;
	
	AllianceFightManager allianceFightManager;
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	public void setAllianceFightManager(AllianceFightManager allianceFightManager)
	{
		this.allianceFightManager=allianceFightManager;
	}

	public void addTable(DataTable table)
	{
		tableList.add(table);
	}
	
	public Server getServer1()
	{
		return server1;
	}

	
	public void setServer1(Server server1)
	{
		this.server1=server1;
	}

	
	public Server getServer2()
	{
		return server2;
	}

	
	public void setServer2(Server server2)
	{
		this.server2=server2;
	}

	
	public Server getServer3()
	{
		return server3;
	}

	
	public void setServer3(Server server3)
	{
		this.server3=server3;
	}
	
	/**
	 * 
	 * 开始合并数据
	 */
	public void startCombine()
	{
		Iterator<DataTable> iter=tableList.iterator();
		while(iter.hasNext())
		{
			DataTable table=iter.next();
			log.info("开始处理====="+table);
			table.process(server1,server2,server3);
		}
	}
	
	public void preprocessData()
	{
		// 预处理玩家数据，处理所有派出去的舰队
		preprocessPlayers();
		// 处理联盟超级舰队
		preprocessAllianceFight();
		//预处理新联盟战
		preprocessAllianceBattleFight();
	}
	
	/**
	 * 处理玩家数据
	 */
	private void preprocessPlayers()
	{
		Object[] objs=objectFactory.getPlayerCache().getCacheMap()
						.valueArray();
		for(int i=0;i<objs.length;i++)
		{
			PlayerSave save=(PlayerSave)objs[i];
			Player player=(Player)save.getData();
			// 推算所有战斗事件
			pushEvent(player,objectFactory);
			// 不能删除的player打上标记
			canotRemove(player,objectFactory);
		}
	}
	
	private void canotRemove(Player player,CreatObjectFactory objectFactory)
	{
		// 会长不能移除
		String aidStr =player.getAttributes(PublicConst.ALLIANCE_ID);
		if(aidStr!=null&&!aidStr.isEmpty())
		{
			
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadOnly(aidStr);
			if(alliance!=null)
			{
				if(alliance.getMasterPlayerId()==player.getId())
				{
					player.setAttribute(CANOT_REMOVE_PLAYER,"true");
				}
			}
			// 玩家联盟不存在，清除掉
			else
			{
				
			}
		}
	}
	
	/**
	 * 推送事件
	 * @param player
	 * @param objectFactory
	 */
	public void pushEvent(Player player,CreatObjectFactory objectFactory)
	{
		int checkTime=TimeKit.getSecondTime()+86400*10;
		int islandIndex=objectFactory.getIslandCache().getPlayerIsLandId(player.getId());
		Object[] object=FightKit.pushFightEvent(islandIndex,objectFactory);
		if(object!=null)
		{
			// 让中途的事件全部到达
			checkFightEvent(object,objectFactory,player,checkTime);
			// 让所有的驻守事件返航
			returnBack(object,objectFactory,player);
			// 让所有返航的事件到达
			checkTime+=86400*10;
			checkFightEvent(object,objectFactory,player,checkTime);
		}
	}
	
	/** 战斗事件的处理 */
	private void checkFightEvent(Object events[],
		CreatObjectFactory objectFactory,Player player,int checkTime)
	{
		// 时间排序后的事件处理
		for(int i=0;i<events.length;i++)
		{
			FightEvent dataA=(FightEvent)events[i];
			synchronized(dataA)
			{
				if(dataA.getEventState()==FightEvent.HOLD_ON) continue;
				if(dataA.getDelete()==FightEvent.DELETE_TYPE) continue;
				FightKit.checkFightEvent(dataA,player,objectFactory,checkTime);
				// 加入改变列表 等待储存
				objectFactory.getEventCache().load(String.valueOf(dataA.getId()));
			}
		}
	}
	
	/**
	 * 让所有驻守事件返航
	 * @param events
	 * @param objectFactory
	 * @param player
	 */
	private void returnBack(Object[] events,
		CreatObjectFactory objectFactory,Player player)
	{
		// 时间排序后的事件处理
		for(int i=0;i<events.length;i++)
		{
			FightEvent back=(FightEvent)events[i];
			synchronized(back)
			{
				if(back.getEventState()!=FightEvent.HOLD_ON) continue;
				if(back.getDelete()==FightEvent.DELETE_TYPE)continue;
				if(back.getPlayerId()!=player.getId())
				{
					// 找到玩家岛屿
					int islandId=objectFactory.getIslandCache()
						.getPlayerIsLandId(player.getId());
					mustang.set.ArrayList fightEventList=objectFactory.getEventCache()
						.getFightEventListById(islandId);
					if(fightEventList!=null)
					{
						fightEventList.remove(back);
						// 刷新被驻守方的事件
						//JBackKit.deleteFightEvent(player,back);
					}
					continue;
				}
				log.info("event return back:"+back);
				// 设置状态
				back.setEventState(FightEvent.RETRUN_BACK);
				NpcIsland beIsland=objectFactory.getIslandCache().load(
					back.getAttackIslandIndex()+"");
				// 计算时间
				NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
					player.getId());
				int needTime=FightKit.needTime(island.getIndex(),beIsland
					.getIndex());
				int nowTime=TimeKit.getSecondTime();
				if(beIsland.getPlayerId()==0)
				{
					// 结算资源
					beIsland.setResource(player,back.getResources(),(nowTime-back
						.getCreatAt())/60,back.getFleetGroup(),0);
				}
				else
				{
					Player beholdPlayer=objectFactory.getPlayerById(beIsland
						.getPlayerId());
					int beeventId=0;
					if(beholdPlayer
						.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)!=null
						&&!beholdPlayer.getAttributes(
							PublicConst.ALLIANCE_DEFND_ATT).equals(""))
					{
						beeventId=Integer.parseInt(beholdPlayer
							.getAttributes(PublicConst.ALLIANCE_DEFND_ATT));
					}
					if(beeventId==back.getId())
					{
						beholdPlayer.setAttribute(
							PublicConst.ALLIANCE_DEFND_ATT,null);
					}
					// 刷新被驻守方的事件
					//JBackKit.deleteFightEvent(beholdPlayer,back);
				}
				// 清楚临时事件
				beIsland.setTempAttackEventId(0);
				// 设置时间
				back.setCreatAt(nowTime);
				// 设置需要时间
				back.setNeedTime(needTime,player,nowTime);
				//JBackKit.sendFightEvent(player,back,objectFactory);
				// 检查自己的事件
				objectFactory.getEventCache().removeHoldOnEvent(
					island.getIndex(),beIsland.getIndex(),player);
			}
		}
	}
	
	/**
	 * 撤离据点
	 */
	private void preprocessAllianceFight()
	{
		Object[] objs=objectFactory.getBattleGroundMemCache().getCacheMap().valueArray();
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<objs.length;i++)
		{
			BattleGroundSave save=(BattleGroundSave)objs[i];
			BattleGround battleGround=(BattleGround)save.getData();
			int allianceId=battleGround.getId();
			// 如果联盟id为0，说明没有联盟驻守
			if(allianceId==0)
				continue;
			// 获得联盟
			Alliance alliance=objectFactory.getAlliance(allianceId,true);
			
			if(alliance==null)
				continue;
			// 获取会长
			Player player=objectFactory.getPlayerById(alliance.getMasterPlayerId());
			allianceFightManager.retreatGround(player,data);
		}
	}
	
	/**清除联盟岛屿上的信息**/
	private void preprocessAllianceBattleFight()
	{
		int stage=objectFactory.getBattleFight().getAllianceStage().getStage();
		Object[] objects=objectFactory.getBattleIslandMemCache()
			.loadBattleIslands(true);
		int resourceCount=objectFactory.getBattleFight().getAllianceStage()
			.getResourceTimes();
		if(objects==null) return;
		for(int i=0;i<objects.length;i++)
		{
			BattleIsland bIlsland=(BattleIsland)((BattleIslandSave)objects[i])
				.getData();
			if(stage==Stage.STAGE_ONE)
			{
				if(resourceCount<0)
					resourceCount=0;
				while(resourceCount<5)
				{
					bIlsland.addAllianceSciencePoint(objectFactory);
					resourceCount++;
				}
			}
			else if(stage==Stage.STAGE_TWO)
			{
				bIlsland.rebackAllianceMaterial(objectFactory);
			}
			else if(stage==Stage.STAGE_THREE)
			{
				bIlsland.rebackAllianceMaterial(objectFactory);
				//返回玩家船只
				bIlsland.rebackPlayerShips(objectFactory);
				bIlsland.clearRecord();
			}
			else if(stage==Stage.STAGE_FOUR)
			{
				objectFactory.getBattleFight().fight(300,bIlsland);
				bIlsland.sendFightEndTask(objectFactory);
				int award=0;
				while(award<5)
				{
					bIlsland.addAllianceSciencePoint(objectFactory);
					award++;
				}
			}
			bIlsland.clear(objectFactory);
			// 是否需要清除玩家身上的邮件
			objectFactory.getMessageCache().clearAllianceMessage();
		}
	}
	
}
