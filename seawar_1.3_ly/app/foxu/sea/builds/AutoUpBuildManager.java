package foxu.sea.builds;

import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.builds.produce.ResourcesProduce;
import foxu.sea.kit.JBackKit;

/**
 * 建筑自动升级管理器
 * 
 * @author Alan
 * 
 */
public class AutoUpBuildManager implements TimerListener
{

	/** 建筑管理器 */
	BuildManager manager;
	/** 按激活时间(key)存放的玩家对象数组(value),定时器用 */
	private IntKeyHashMap autoBuildMap;
	/** 当前自动升级的玩家(key)及所在的对象数组与激活时间(value),查询用 */
	private IntKeyHashMap autoPlayerMap;
	TimerEvent autoLevelUp;
	/** 定时器毫秒级倍数 */
	public static final long MILLTIME=1000;
	/** 强制激活间隔 1分钟 */
	public static final int FORCE_CHECK=60;
	/** 定时器下一次激活的时间 */
	int nextTime;
	/** 是否初始化完毕 */
	boolean isInited=false;

	public void init(BuildManager manager)
	{
		this.manager=manager;
		autoBuildMap=new IntKeyHashMap();
		autoPlayerMap=new IntKeyHashMap();
		Object[] players=manager.objectFactory.getPlayerCache()
			.getCacheMap().valueArray();
		for(int i=0;i<players.length;i++)
		{
			Player player=((PlayerSave)players[i]).getData();
			//////////////
//			player.setAutoLevelUp(true);
//			Service autoService=new Service();
//			autoService.setServiceType(PublicConst.AUTO_BUILD_BUFF);
//			autoService.setServiceTime(300);
//			player.addService(autoService,TimeKit.getSecondTime());
			//////////////
			if(player.getAutoLevelUp()&&checkAutoBuild(player))
			{
				player.getIsland().pushAll(TimeKit.getSecondTime(),
					manager.objectFactory);
				// 如果资源增加会触发自动升级，自动升级方法执行时会将当前玩家新加至激活时间点数组
				// 如果数组存在则进行过资源增加
				if(autoPlayerMap.get(player.getId())==null)
					addAutoPlayer(player);
			}
		}
		autoLevelUp=new TimerEvent(this,"auto_level_up",0,true);
		setNextTime();
		TimerCenter.getSecondTimer().add(autoLevelUp);
		isInited=true;
	}
	/** 检查自动升级服务是否可用 */
	public static boolean checkAutoBuild(Player player)
	{
		Service autoService=(Service)player
			.getServiceByType(PublicConst.AUTO_BUILD_BUFF);
		if(autoService==null||autoService.isOver(TimeKit.getSecondTime()))
		{
			return false;
		}
		return true;
	}

	/** 添加建筑自动升级玩家 */
	public void addAutoPlayer(Player player)
	{
		player.setAutoLevelUp(true);
		autoLevelUp(player);
		PlayerEntry entryAfter=(PlayerEntry)autoPlayerMap
			.get(player.getId());
		if(isInited&&entryAfter.nextTime<nextTime)
			setNextTime(entryAfter.nextTime);
	}

	/** 移除建筑自动升级玩家 */
	public void removeAutoPlayer(Player player)
	{
		player.setAutoLevelUp(false);
		PlayerEntry entry=(PlayerEntry)autoPlayerMap.get(player.getId());
		if(entry!=null) entry.arrayBelong.remove(player);
	}

	/** 维护自动升级队列 */
	private synchronized void autoLevelUpArray()
	{
//		System.out.println("--------------维护集合---数量："+autoBuildMap.size()
//			+"----"+TimeKit.getMillisTime());
		ObjectArray autoPlayers=(ObjectArray)autoBuildMap.remove(nextTime);
		if(autoPlayers!=null&&autoPlayers.size()>0)
		{
//			System.out.println("当前集合:"+autoPlayers.size());
			while(autoPlayers.size()>0)
			{
				Player player=(Player)autoPlayers.getArray()[0];
				if(checkAutoBuild(player))
				{
					player.getIsland().pushAll(TimeKit.getSecondTime(),
						manager.objectFactory);
					// 如果资源增加会触发自动升级，自动升级方法执行时会将当前玩家从当前时间点数组移除
					// 如果还存在玩家则没有进行过资源增加
					if(autoPlayers.size()>0
						&&((Player)autoPlayers.getArray()[0]).getId()==player
							.getId())
					{
						autoLevelUp(player);
					}
				}
				else
				{
					autoPlayers.remove(player);
					autoPlayerMap.remove(player.getId());
				}
			}
		}
		setNextTime();
//		System.out.println("--------------维护完毕---下次激活："
//			+autoLevelUp.getNextTime()+"----"+TimeKit.getMillisTime());
	}

	/** 建筑自动升级 */
	private synchronized void autoLevelUp(Player player)
	{
		PlayerBuild[] buildArray=player.getIsland().getAutoBuildArray();
		IntList autoBuilds=new IntList();
		for(int i=0;i<buildArray.length;i++)
		{
			if(manager.isBuildDequeFull(player))
			{
				break;
			}
			String str=manager.checkbuildUpLevel(player,
				buildArray[i].getIndex());
			if(str!=null)
			{
				continue;
			}
			int index=buildArray[i].getIndex();
			manager.buildUpLevel(player,index);
			autoBuilds.add(index);
		}
		int playerNextTime=getPlayerNextTime(player);
		// 从旧数组中移除
		PlayerEntry entry=(PlayerEntry)autoPlayerMap.get(player.getId());
		if(entry!=null)
			entry.arrayBelong.remove(player);
		else
			entry=new PlayerEntry();
		// 加入新数组
		ObjectArray oa=(ObjectArray)autoBuildMap.get(playerNextTime);
		if(oa==null)
		{
			oa=new ObjectArray();
			autoBuildMap.put(playerNextTime,oa);
		}
		oa.add(player);
		entry.nextTime=playerNextTime;
		entry.arrayBelong=oa;
		autoPlayerMap.put(player.getId(),entry);
		JBackKit.sendBuildServiceInfo(player,autoBuilds.getArray(),
			autoBuilds.size());
	}

	/** 获取当前玩家下次激活的时间 */
	private int getPlayerNextTime(Player player)
	{
		int playerNextTime=0;
		// 再次排序，如果有正在升级建筑确保队首为升级建筑，避免有不可升级建筑占位
		PlayerBuild[] buildArray=player.getIsland().getAutoBuildArray();
		if(!manager.isBuildDequeFull(player))
		{
			// 队列未满
			playerNextTime=TimeKit.getSecondTime()
				+ResourcesProduce.ONE_MINUTE;
			if(player.getIsland().checkNowBuildingByIndex(
				buildArray[0].index)
				&&buildArray[0].getBuildCompleteTime()<playerNextTime)
			{
				playerNextTime=buildArray[0].getBuildCompleteTime();
				// 建筑即将完成，小于1分钟
			}
		}
		else
			playerNextTime=buildArray[0].getBuildCompleteTime();
		return playerNextTime;
	}

	/** 队列线程调用时，全队遍历设置下一次激活时间 */
	private void setNextTime()
	{
		int checkTime=getNextTime();
		setNextTime(checkTime);
	}

	/** 其他条件触发自动升级时，强制设置激活时间 */
	private void setNextTime(int checkTime)
	{
		if(checkTime<=0) checkTime=TimeKit.getSecondTime()+FORCE_CHECK;
		nextTime=checkTime;
		long nextChceckTime=checkTime*MILLTIME;
		autoLevelUp.setNextTime(nextChceckTime);
	}

	/** 获取下一次激活时间 */
	private int getNextTime()
	{
		int length=autoBuildMap.keyArray().length;
		int nextTime=0;
//		int playerSize=0;
		IntList zeroList=new IntList();
		for(int i=0;i<length;i++)
		{
			ObjectArray oa=(ObjectArray)autoBuildMap.get(autoBuildMap
				.keyArray()[i]);
			if(oa!=null&&oa.size()>0)
			{
//				playerSize+=oa.size();
				if(nextTime==0)
				{
					nextTime=autoBuildMap.keyArray()[i];
					continue;
				}
				else if(nextTime>autoBuildMap.keyArray()[i])
				{
					nextTime=autoBuildMap.keyArray()[i];
				}
			}
			else
			{
				zeroList.add(autoBuildMap.keyArray()[i]);
			}
		}
//		System.out.println("----------------playerSize:"+playerSize);\
		for(int i=0;i<zeroList.size();i++){
			autoBuildMap.remove(zeroList.get(i));
		}
		return nextTime;
	}

	/** 自动升级队列包含玩家立即检测 */
	public void containedPlayer2Up(Player player)
	{
		PlayerEntry entryBefore=(PlayerEntry)autoPlayerMap.get(player
			.getId());
		if(entryBefore!=null&&checkAutoBuild(player)
			&&entryBefore.arrayBelong.contain(player))
		{
			autoLevelUp(player);
			PlayerEntry entryAfter=(PlayerEntry)autoPlayerMap.get(player
				.getId());
			if(isInited&&entryAfter.nextTime<nextTime)
				setNextTime(entryAfter.nextTime);
		}
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		autoLevelUpArray();
	}

	/** 存储下次激活时间与所属数组的玩家元素 */
	class PlayerEntry
	{

		/** 激活时间 */
		int nextTime;
		/** 所属数组 */
		ObjectArray arrayBelong=new ObjectArray();
	}
}
