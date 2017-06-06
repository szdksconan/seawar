package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.Science;
import foxu.sea.Ship;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.builds.produce.CommandProduce;
import foxu.sea.builds.produce.ResourcesProduce;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.checkpoint.Chapter;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.messgae.SystemMessageState;
import foxu.sea.proplist.Prop;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.task.TaskEventExecute;

/**
 * 岛屿类 包括玩家拥有的建筑和兵力 author:icetiger
 */
public class Island
{

	public static final int ISLAND_DEFAULT_LEVEL=1;
	/** *初始建筑位置 最大建筑位置 */
	public static final int BUILD_NUM=2,BUILD_MAX=7;
	/** 默认建筑队列 */
	public static final int PRODUCE_NUM=1,PRODUCE_MAX=7;
	/** 前台需要的岛屿sid */
	int islandSid=1;
	/** 建筑个数 初始是2 */
	int buildNum=BUILD_NUM;
	/** 建筑等待队列 默认为1个 当前建筑的东西 */
	int produceNum=PRODUCE_NUM;
	/** Serialization fileds */
	/** 拥有的建筑 */
	ObjectArray builds=new ObjectArray();
	/** 拥有的兵力和城防兵力 */
	ObjectArray troops=new ObjectArray();
	/** 伤兵 等待回复的伤兵 */
	ObjectArray hurtsTroops=new ObjectArray();
	/** 岛屿等级 */
	int islandLevel=ISLAND_DEFAULT_LEVEL;
	/** 岛屿状态保护，正常，虚弱 */
	int state;
	/** ios相关设置 */
	int iosSystem;

	/** 城防舰队 */
	FleetGroup mainGroup=new FleetGroup();
	/** 已经读取过的系统邮件id,state */
	ObjectArray readSystemMessage=new ObjectArray();

	/** dynamic */
	/** 拥有玩家 */
	Player player;

	/** 查看指定系统邮件是否已经删除 */
	public boolean isStateMessage(Message message,int state)
	{
		if(message.getMessageType()!=Message.SYSTEM_TYPE) return true;
		Object[] array=readSystemMessage.getArray();
		for(int i=0;i<array.length;i++)
		{
			SystemMessageState system=(SystemMessageState)array[i];
			if(system.getMessageId()==message.getMessageId()
				&&system.getState()==state) return true;
		}
		return false;
	}

	/** 查看指定系统邮件的读取状态 */
	public int getStateMessage(Message message)
	{
		if(message.getMessageType()!=Message.SYSTEM_TYPE) return 0;
		Object[] array=readSystemMessage.getArray();
		for(int i=0;i<array.length;i++)
		{
			SystemMessageState system=(SystemMessageState)array[i];
			if(system.getMessageId()==message.getMessageId())
				return system.getState();
		}
		return 0;
	}

	/** 自动补充主力舰队 */
	public synchronized boolean autoAddMainGroup()
	{
		if(!SeaBackKit.isOpen(PublicConst.AUTO_ADD_MAINGROUP,player
			.getIsland().getIosSystem())) return false;
		IntList autoList=mainGroup.hurtList(FleetGroup.AUTO_ADD_SHIP);
		boolean bool=false;
		// sid,num
		for(int i=0;i<autoList.size();i+=3)
		{
			int shipSid=autoList.get(i);
			int canAddNum=autoList.get(i+1);
			int location=autoList.get(i+2);
			// 查看港口是否有船只
			canAddNum=reduceShipBySid(shipSid,canAddNum,troops);
			if(canAddNum>0)
			{
				bool=true;
				// 对主力舰队的某个location添加数量
				mainGroup.addShipByLocation(location,canAddNum);
			}
		}
		if(bool)
		{
			JBackKit.resetMainGroup(player);
			// 刷新前台
			JBackKit.sendResetTroops(player);
		}
		return bool;
	}
	/** 获得当前建筑等待队列个数 */
	public int getProduceNum()
	{
		return produceNum;
	}

	/** 获取某个资源的总计产量 */
	public float getProduceWithType(int buildType)
	{
		Object builds[]=getBuildArray();
		float produceNum=0.0f;
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild.getBuildType()==buildType
				&&playerBuild.getBuildLevel()>0)
			{
				Produce produce=playerBuild.getProduce();
				// 如果是资源建筑
				if(produce instanceof ResourcesProduce)
				{
					float num=((ResourcesProduce)produce).produceNum(
						playerBuild.getBuildLevel()-1,player,TimeKit
							.getSecondTime());
					produceNum+=num;
				}
			}
		}
		// 额外加指挥中心的产量
		PlayerBuild build=(PlayerBuild)getBuildByIndex(BuildInfo.INDEX_0,
			getBuilds());
		CommandProduce commandPorudce=(CommandProduce)build.getProduce();
		// 建筑类型的type对应的serverType
		int serverType=PublicConst.ADD_METAL_BUFF;
		//永久性BUFF
		int foreServerType=PublicConst.FORE_METAL_BUFF;
		//关卡buff
		int pointType=Chapter.METAL;
		if(buildType==Build.BUILD_OIL)
		{
			serverType=PublicConst.ADD_OIL_BUFF;
			foreServerType=PublicConst.FORE_OIL_BUFF;
			pointType=Chapter.OIL;
		}
		else if(buildType==Build.BUILD_SILION)
		{
			serverType=PublicConst.ADD_SILICON_BUFF;
			foreServerType=PublicConst.FORE_SILICON_BUFF;
			pointType=Chapter.SILICON;
		}
		else if(buildType==Build.BUILD_URANIUM)
		{
			serverType=PublicConst.ADD_URANIUM_BUFF;
			foreServerType=PublicConst.FORE_URANIUM_BUFF;
			pointType=Chapter.URANIUM;
		}
		else if(buildType==Build.BUILD_MONEY)
		{
			serverType=PublicConst.ADD_MONEY_BUFF;
			foreServerType=PublicConst.FORE_MONEY_BUFF;
			pointType=Chapter.MONEY;
		}
		produceNum+=commandPorudce.produceNum(build.getBuildLevel()-1,
			player,serverType,TimeKit.getSecondTime());
		produceNum+=commandPorudce.produceForeNum(build.getBuildLevel()-1,
			player,foreServerType,TimeKit.getSecondTime());
		produceNum+=commandPorudce.producePointNum(pointType,
			commandPorudce.noBuffProduceNum(build.getBuildLevel()-1,player),player);
		return produceNum;
	}
	/** 提升建筑位 */
	public void upBuildNum()
	{
		buildNum++;
		if(buildNum>BUILD_MAX) buildNum=BUILD_MAX;
	}

	/** 标记一个系统邮件的状态 */
	public void addStateSystemMessage(int state,int messageId)
	{
		Object[] readSystemMessages=this.readSystemMessage.getArray();
		for(int i=0;i<readSystemMessages.length;i++)
		{
			SystemMessageState message=(SystemMessageState)readSystemMessages[i];
			if(message.getMessageId()==messageId)
			{
				message.setState(state);
				return;
			}
		}
		SystemMessageState message=new SystemMessageState();
		message.setMessageId(messageId);
		message.setState(state);
		this.readSystemMessage.add(message);
	}

	/** 该岛屿是否拥有此index */
	public boolean isPlayerHaveIndex(int index)
	{
		return BuildInfo.isHaveIndex(index,player);
	}

	/** 对应位置是否可以建筑该类型建筑 */
	public boolean isBuildThisType(String buildType,int index)
	{
		return BuildInfo.isBuildThisType(buildType,index);
	}

	/** 获得当前拥有科技 */
	public Science[] getSciences()
	{
		PlayerBuild playerBuild=getBuildByType(Build.BUILD_RESEARCH,builds);
		if(playerBuild==null) return null;
		ScienceProduce produce=(ScienceProduce)playerBuild.getProduce();
		return produce.getAllScience();
	}

	/** 结算所有资源 上线的时候 */
	public void pushAll(int checkTime,CreatObjectFactory objectFactory)
	{
		// push伤兵
		// gotHurtTroops(checkTime);
		gotNowBuilding(checkTime,objectFactory);
		gotProsperityInfo(TimeKit.getSecondTime());
		gotProduce(checkTime,objectFactory);
		gotEnergy(checkTime);
		player.checkService(checkTime);
		// 初始化防御舰队的军官信息
		getMainGroup().getOfficerFleetAttr().initOfficers(player);
		JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.ONLINE_PUSH_ALL);
	}

	/** 邀请奖励 */
	public void pushInviet(CreatObjectFactory objectFactory)
	{
		// 包扩自己的
		int inveted[]=player.getInviter_id();
		for(int i=0;i<inveted.length;i+=2)
		{
			if(inveted[i+1]==0)
			{
				// 自己发放20宝石
				if(inveted[i]==player.getId())
				{
					int gems=20;
					Resources.addGemsNomal(20,player.getResources(),player);
					MessageKit.sendBeInvetedGems(player,objectFactory);
					if(player.getInveted()!=0)
					{
						gems+=10;
						Resources.addGemsNomal(10,player.getResources(),
							player);
						// 和平旗
						Prop prop=(Prop)Prop.factory.newSample(509);
						player.getBundle().incrProp(prop,true);
						JBackKit.sendResetBunld(player);
					}
					// 宝石日志记录
					objectFactory.createGemTrack(GemsTrack.INVITE,player
						.getId(),gems,0,
						Resources.getGems(player.getResources()));
				}
				// 发放一个金属资源包 奖励品sid为5
				else
				{
					Prop prop=(Prop)Prop.factory.newSample(4);
					player.getBundle().incrProp(prop,true);
					JBackKit.sendResetBunld(player);
					Resources.addGemsNomal(15,player.getResources(),player);
					// 发送邮件
					MessageKit.sendInvetedMoney(player,objectFactory,
						inveted[i]);
					// 宝石日志记录
					objectFactory.createGemTrack(GemsTrack.INVITE,player
						.getId(),15,inveted[i],
						Resources.getGems(player.getResources()));
				}
				inveted[i+1]=1;
			}
			player.setInviter_id(inveted);
		}
	}

	// /** 推算伤兵 */
	// public void gotHurtTroops(int checkTime)
	// {
	// Object object[]=hurtsTroops.getArray();
	// for(int i=0;i<object.length;i++)
	// {
	// HurtTroop troop=(HurtTroop)object[i];
	// int num=troop.nowHurtsNum(checkTime);
	// troop.setNum(num);
	// if(num<=0)
	// {
	// hurtsTroops.remove(troop);
	// }
	// }
	// }
	
	/**
	 * 获取繁荣度上限  根据建筑多少和等级计算
	 */
	public int getProsperityMax(){
		
		Object[] builds=this.builds.getArray();
		int max = 0;
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild = (PlayerBuild)builds[i];
			max +=playerBuild.getBuildLevel() * playerBuild.getGiveProsperity();
		}
		player.getProsperityInfo()[2]=max;
		
		return max;
	}
	
	/**获取刷新当前 繁荣度相关信息 */
	public int gotProsperityInfo(int checkTime){
		
		int[] prosperityInfo=player.getProsperityInfo();

		synchronized(prosperityInfo)
		{
			int max=getProsperityMax();// 当前繁荣度上线
			if(prosperityInfo[0]>=max||prosperityInfo[1]==0)
			{// 如果繁荣度已满 这里用大于条件判断 排除摧毁建筑的可能
				prosperityInfo[0]=max; 
				prosperityInfo[1]=checkTime;// 重置检查时间
				// 重置繁荣度等级
				int lv=0;
				for(int i=0;i<Player.PROSPERITY_lV_BUFF.length;i+=3)
				{
					if(max<Player.PROSPERITY_lV_BUFF[i]){
						lv=i/3-1;
						break;
					}
					if(lv==0&&i==Player.PROSPERITY_lV_BUFF.length-3){
						lv = (Player.PROSPERITY_lV_BUFF.length-3)/3;
					}
				}
				
				prosperityInfo[3]=lv;
				JBackKit.sendResetProsperity(player);
				return prosperityInfo[0];
			}
			int count=(checkTime-prosperityInfo[1])/Player.PROSPERITY_TIME;// 繁荣度点数
			if(count>0)
			{
				int prosperity=prosperityInfo[0]+count;
				if(prosperity>=max) prosperity=max;
				prosperityInfo[0]=prosperity;
				// 重置繁荣度等级
				int lv=0;
				for(int i=0;i<Player.PROSPERITY_lV_BUFF.length;i+=3)
				{
					if(prosperity<Player.PROSPERITY_lV_BUFF[i]){
						lv=i/3-1;
						break;
					}
					if(lv==0&&i==Player.PROSPERITY_lV_BUFF.length-3){
						lv = (Player.PROSPERITY_lV_BUFF.length-3)/3;
					}
				}
				prosperityInfo[3]=lv;
				// 重置检查时间
				prosperityInfo[1]+=count*Player.PROSPERITY_TIME;
				JBackKit.sendResetProsperity(player);
			}
			//打印看下
			/*System.out.println("繁荣度:"+prosperityInfo[0]);
			System.out.println("繁荣度checkTime:"+prosperityInfo[1]);
			System.out.println("繁荣度MAX:"+prosperityInfo[2]);
			System.out.println("繁荣度LV:"+prosperityInfo[3]);*/
			return prosperityInfo[0];
			
		}
	}
	
	/** 获取当前精力值 */
	public int gotEnergy(int checkTime)
	{
		if(player.getActives()[Player.ENERGY_INDEX]>=Player.MAX_ENERGY)
		{
			return player.getActives()[Player.ENERGY_INDEX];
		}
		int times=(checkTime-player.getActives()[Player.ENERGY_TIME_INDEX])
			/Player.ENERGY_TIME;
		if(times>0)
		{
			player.getActives()[Player.ENERGY_INDEX]+=times;
			int leftTime=(checkTime-player.getActives()[Player.ENERGY_TIME_INDEX])
				%Player.ENERGY_TIME;
			player.getActives()[Player.ENERGY_TIME_INDEX]=(checkTime-leftTime);
			if(player.getActives()[Player.ENERGY_INDEX]>Player.MAX_ENERGY)
				player.getActives()[Player.ENERGY_INDEX]=Player.MAX_ENERGY;
		}
		return player.getActives()[Player.ENERGY_INDEX];
	}

	/** 获取生产资源 5分钟一次 */
	public void gotProduce(int checkTime,CreatObjectFactory objectFactory)
	{
		Object[] builds=this.builds.getArray();
		for(int i=0;i<builds.length;i++)
		{
			/** 资源矿产 */
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild==null) continue;
			playerBuild.produce(player,checkTime,objectFactory);
		}
	}

	/** 查看正在建筑事件 是否完成 */
	public void gotNowBuilding(int checkTime,CreatObjectFactory fatory)
	{
		Object[] builds=this.builds.getArray();
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild==null) continue;
			if(playerBuild.getBuildCompleteTime()==0) continue;
			/** 建筑完成 有可能是升级 */
			if(playerBuild.checkBuildTime(checkTime))
			{
				/** 获得该index的建筑 */
				playerBuild.setBuildCompleteTime(0);
				if(playerBuild.getBuildLevel()==0)
				{
					playerBuild.levelUp();
					// 获取经验
					player.incrExp(playerBuild
						.getLevelExperience(playerBuild.getBuildLevel()-1),fatory);
					// 获取繁荣度
					int[] prosperityInfo =  player.getProsperityInfo();
					synchronized(prosperityInfo)
					{
						prosperityInfo[0]+=playerBuild.getGiveProsperity();
						prosperityInfo[2]+=playerBuild.getGiveProsperity();
					}
					// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.BUILD_FINISH_TASK_EVENT,playerBuild,
						player,null);
					JBackKit.sendResetProsperity(player);
				}
				else
				{
					playerBuild.levelUp();
					// 获取经验
					player.incrExp(playerBuild
						.getLevelExperience(playerBuild.getBuildLevel()-1),fatory);
					// 获取繁荣度
					int[] prosperityInfo =  player.getProsperityInfo();
					synchronized(prosperityInfo)
					{
						prosperityInfo[0]+=playerBuild.getGiveProsperity();
						prosperityInfo[2]+=playerBuild.getGiveProsperity();
					}
					// 发送change消息
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.BUILD_FINISH_TASK_EVENT,playerBuild,
						player,null);
					JBackKit.sendResetProsperity(player);
				}
				//成就信息采集
				AchieveCollect.buildLevel(playerBuild,this,player);
				//新兵福利
				RecruitKit.pushTaskBuild(playerBuild,player);
			}
		}
	}

	/** 当前index是否在升级 */
	public boolean checkNowBuildingByIndex(int index)
	{
		Object[] nowbuilding=this.builds.getArray();
		for(int i=0;i<nowbuilding.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)nowbuilding[i];
			if(playerBuild.getBuildCompleteTime()!=0
				&&playerBuild.getIndex()==index) return true;
		}
		return false;
	}

	/** 取消建筑事件 */
	public void cancelBuilding(int index)
	{
		/** 返回衰减后的资源 */
		Object[] builds=this.builds.getArray();
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild.getIndex()==index
				&&playerBuild.getBuildCompleteTime()!=0)
			{
				playerBuild.cancelBuilding(player);
				// 新建建筑
				if(playerBuild.getBuildLevel()==0)
				{
					this.builds.remove(playerBuild);
				}
				return;
			}
		}
	}

	/** 获得城防建筑相应sid的船只数量 */
	public int getShipsBySidForDefendGroup(int shipSid)
	{
		int num=0;
		Fleet fleet[]=mainGroup.getArray();
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null||fleet[i].getNum()<=0) continue;
			if(fleet[i].getShip().getSid()==shipSid)
			{
				num+=fleet[i].getNum();
			}
		}
		return num;
	}

	/** 获得当前sid的船只数量 */
	public int getShipsBySid(int shipSid,ObjectArray troopArray)
	{
		if(troopArray==null) troopArray=this.troops;
		Object[] troops=troopArray.getArray();
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				return troop.getNum();
			}
		}
		return 0;
	}

	/** 根据ship类型 */
	public int getShipsByType(int shipType,ObjectArray troopArray)
	{
		if(troopArray==null) troopArray=this.troops;
		Object[] troops=troopArray.getArray();
		int num=0;
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			Ship ship=(Ship)Ship.factory.getSample(troop.getShipSid());
			if(ship!=null||ship.getPlayerType()==shipType)
			{
				num+=troop.getNum();
			}
		}
		return num;
	}

	/** 减少某个sid的船 返回减少的船只数量* */
	public synchronized int reduceShipBySid(int shipSid,int num,
		ObjectArray troopArray)
	{
		if(troopArray==null) troopArray=troops;
		Object[] troops=troopArray.getArray();
		int reduceNum=0;
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid&&troop.getNum()>0)
			{
				reduceNum=troop.reduceNum(num);
				if(troop.getNum()<=0) troopArray.remove(troop);
				return reduceNum;
			}
		}
		return 0;
	}

	/** 获得所有船只数量 排除城防 */
	public int getAllShipsNum()
	{
		Object[] troops=this.troops.getArray();
		int num=0;
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			Ship ship=(Ship)Role.factory.getSample(troop.getShipSid());
			if(ship!=null&&ship.isMoveShips()) num+=troop.getNum();
		}
		return num;
	}

	/** 减少伤兵 */
	public synchronized boolean reduceHurtTroop(int shipSid,int nums)
	{
		if(shipSid==0||nums==0) return false;
		Object[] troops=hurtsTroops.getArray();
		for(int i=0;i<troops.length;i++)
		{
			HurtTroop troop=(HurtTroop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				troop.reduceNum(nums);
				if(troop.getNum()<=0)
				{
					hurtsTroops.remove(troop);
				}
				return true;
			}
		}
		return false;
	}

	/** 添加伤兵 */
	public synchronized void addHurtTroop(int shipSid,int nums,int time)
	{
		if(shipSid==0||nums==0) return;
		Object[] troops=hurtsTroops.getArray();
		for(int i=0;i<troops.length;i++)
		{
			HurtTroop troop=(HurtTroop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				// troop.nowHurtsNum(time);
				troop.addNums(nums);
				return;
			}
		}
		HurtTroop addTroop=new HurtTroop();
		addTroop.setNum(nums);
		addTroop.setShipSid(shipSid);
		addTroop.setTime(time);
		hurtsTroops.add(addTroop);
	}

	/** 减少兵力 */
	public synchronized void reduceTroop(int shipSid,int nums,
		ObjectArray troopArray)
	{
		if(shipSid==0||nums==0) return;
		if(troopArray==null) troopArray=troops;
		Object[] troops=troopArray.getArray();
		// 限制船的数量不要超过上限
		// int nowHave=getAllShipsNum();
		// int limiteNum=getShipNumLimite();
		// if((nowHave+nums)>limiteNum)
		// {
		// nums=limiteNum-nowHave;
		// }
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				troop.reduceNum(nums);
				return;
			}
		}
		Troop addTroop=new Troop();
		addTroop.setNum(nums);
		addTroop.setShipSid(shipSid);
		troopArray.add(addTroop);
	}

	/** 添加兵力 */
	public synchronized void addTroop(int shipSid,int nums,
		ObjectArray troopArray)
	{
		if(shipSid==0||nums==0) return;
		if(troopArray==null) troopArray=troops;
		Object[] troops=troopArray.getArray();
		// 限制船的数量不要超过上限
		// int nowHave=getAllShipsNum();
		// int limiteNum=getShipNumLimite();
		// if((nowHave+nums)>limiteNum)
		// {
		// nums=limiteNum-nowHave;
		// }
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				troop.addNums(nums);
				return;
			}
		}
		Troop addTroop=new Troop();
		addTroop.setNum(nums);
		addTroop.setShipSid(shipSid);
		troopArray.add(addTroop);
	}

	/** 添加一个建筑,返回是否成功 */
	public boolean addBuild(PlayerBuild build)
	{
		if(getBuildByIndex(build.getIndex(),builds)!=null) return false;
		int percent=0;
		// 科技建筑加速比列
		AdjustmentData data=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.BUILD_BUFF));
		//建筑加速活动
		int activypercent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.BUILD_ID);
		if(data!=null)
		{
			percent=data.percent;
		}
		int time=build.getBuildTime()[0]*100/(100+percent)*(100-activypercent)/100;
		build.setBuildCompleteTime(TimeKit.getSecondTime()+time);
		build.setBuildTotleTime(time);
		build.init(build.getBuildCompleteTime());
		builds.add(build);
		return true;
	}

	/** 直接添加一个成品建筑 */
	public boolean addBuildNow(PlayerBuild build,int index)
	{
		if(getBuildByIndex(index,builds)!=null) return false;
		if(!BuildInfo.isBuildThisType(build.getBuildType()+"",index))
			return false;
		build.init(TimeKit.getSecondTime());
		build.setIndex(index);
		builds.add(build);
		return true;
	}
	/**
	 * 移除一个建筑 撤除建筑
	 * 
	 * @param index 建筑index
	 * @return 返回是否成功
	 */
	public boolean deleteBuild(int index)
	{
		PlayerBuild build=getBuildByIndex(index,this.builds);
		if(build!=null)
		{
			this.builds.remove(build);
			return true;
		}
		return false;
	}

	/** 获得某一个建筑 可能会有多个 只用于判断是否存在 */
	public PlayerBuild getBuildBySid(int sid,ObjectArray buildss)
	{
		Object[] builds=buildss.getArray();
		for(int i=0;i<builds.length;i++)
		{
			if(((PlayerBuild)builds[i]).getSid()==sid)
			{
				return (PlayerBuild)builds[i];
			}
		}
		return null;
	}

	/** 如果有多个返回等级最高的 */
	public PlayerBuild getBuildByType(int buildType,ObjectArray buildss)
	{
		if(buildss==null) buildss=builds;
		Object[] builds=buildss.getArray();
		PlayerBuild playerBuild=null;
		for(int i=0;i<builds.length;i++)
		{
			if(((PlayerBuild)builds[i]).getBuildType()==buildType)
			{
				PlayerBuild checkBuild=(PlayerBuild)builds[i];
				if(playerBuild==null
					||playerBuild.getBuildLevel()<checkBuild.getBuildLevel())
					playerBuild=(PlayerBuild)builds[i];
			}
		}
		return playerBuild;
	}

	/** 获得某一个类型建筑数量 */
	public int getBuildNumByType(int buildType,ObjectArray buildss)
	{
		Object[] builds=buildss.getArray();
		int num=0;
		for(int i=0;i<builds.length;i++)
		{
			if(((PlayerBuild)builds[i]).getBuildType()==buildType)
			{
				num++;
			}
		}
		return num;
	}

	/** 根据index获得一个建筑 */
	public PlayerBuild getBuildByIndex(int index,ObjectArray buildss)
	{
		if(buildss==null) buildss=builds;
		Object[] builds=buildss.getArray();
		for(int i=0;i<builds.length;i++)
		{
			if(builds[i]==null) continue;
			if(((PlayerBuild)builds[i]).getIndex()==index)
			{
				return (PlayerBuild)builds[i];
			}
		}
		return null;
	}

	/** 获取自动升级建筑队列 */
	public PlayerBuild[] getAutoBuildArray()
	{
		Object[] buildings=builds.getArray();
		PlayerBuild[] array=new PlayerBuild[buildings.length];
		int onBuilding=0;
		for(int i=0;i<buildings.length;i++)
		{
			PlayerBuild b=(PlayerBuild)buildings[i];
			if(checkNowBuildingByIndex(b.getIndex()))
			{
				if(onBuilding!=i)
				{
					PlayerBuild temp=array[onBuilding];
					array[onBuilding]=b;
					array[i]=temp;
					onBuilding++;
					continue;
				}
			}
			array[i]=b;
		}
		//将正在升级的建筑放在最前端并按升级完成时间升序
		for(int i=0;i<onBuilding;i++)
		{
			for(int j=i+1;j<onBuilding;j++)
			{
				if(array[j].getBuildCompleteTime()<array[i].getBuildCompleteTime())
				{
					PlayerBuild temp=array[i];
					array[i]=array[j];
					array[j]=temp;
				}
			}
		}
		//非正在升级建筑按等级升序
		for(int i=onBuilding;i<array.length;i++)
		{
			for(int j=i+1;j<array.length;j++)
			{
				if(array[j].getBuildLevel()<array[i].getBuildLevel())
				{
					PlayerBuild temp=array[i];
					array[i]=array[j];
					array[j]=temp;
				}
			}
		}
		return array;
	}
	
	public void bytesReadSystemMessage(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			SystemMessageState systemState=new SystemMessageState();
			temp[i]=systemState.bytesRead(data);
		}
		readSystemMessage=new ObjectArray(temp);
		return;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteSystemMessage(ByteBuffer data)
	{
		if(readSystemMessage.size()>200) readSystemMessage.clear();
		if(readSystemMessage!=null&&readSystemMessage.size()>0)
		{
			Object[] array=readSystemMessage.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((SystemMessageState)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** 从字节缓存中反序列化得到一个对象 */
	public Object bytesRead(ByteBuffer data)
	{
		islandSid=data.readUnsignedShort();
		buildNum=data.readUnsignedByte();
		produceNum=data.readUnsignedByte();
		islandLevel=data.readUnsignedByte();
		state=data.readUnsignedByte();
		iosSystem=data.readInt();
		bytesReadBuilds(data);
		bytesReadTroop(data);
		mainGroup.bytesRead(data);
		bytesReadSystemMessage(data);
		bytesReadhurtsTroops(data);
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(islandSid);
		data.writeByte(buildNum);
		data.writeByte(produceNum);
		data.writeByte(islandLevel);
		data.writeByte(state);
		data.writeInt(iosSystem);
		bytesWriteBuilds(data);
		bytesWriteTroop(data);
		bytesWriteMainGroup(data);
		bytesWriteSystemMessage(data);
		bytesWritehurtsTroops(data);
	}
	public void showBytesWrite(ByteBuffer data,int current,
		CreatObjectFactory objectFactory)
	{
		data.writeShort(islandSid);// 写入前台需要的sid
		data.writeByte(buildNum);
		data.writeByte(produceNum);
		data.writeByte(islandLevel);
		data.writeByte(state);
		data.writeInt(iosSystem);
		if(builds!=null&&builds.size()>0)
		{
			Object[] array=builds.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				if(array[i]==null) continue;
				((PlayerBuild)array[i]).showBytesWrite(data,current);
			}
		}
		else
		{
			data.writeByte(0);
		}
		showBytesWriteTroops(data,current);
		showBytesWriteMainGroup(data);
		showBytesWritehurtsTroops(data,current);
		bytesWriteEvents(data,objectFactory);
	}

	public void showBytesWriteTroops(ByteBuffer data,int current)
	{
		if(troops!=null&&troops.size()>0)
		{
			Object[] array=troops.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Troop)array[i]).showBytesWrite(data,current);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	public void showBytesWritehurtsTroops(ByteBuffer data,int current)
	{
		if(hurtsTroops!=null&&hurtsTroops.size()>0)
		{
			Object[] array=hurtsTroops.getArray();
			data.writeShort(array.length);
			for(int i=0;i<array.length;i++)
			{
				((HurtTroop)array[i]).showBytesWrite(data,current);
			}
		}
		else
		{
			data.writeShort(0);
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public void bytesReadMainGroup(ByteBuffer data)
	{
		mainGroup.bytesRead(data);
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteMainGroup(ByteBuffer data)
	{
		mainGroup.bytesWrite(data);
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void showBytesWriteMainGroup(ByteBuffer data)
	{
		mainGroup.showBytesWrite(data);
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadhurtsTroops(ByteBuffer data)
	{
		int n=data.readUnsignedShort();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			HurtTroop troop=new HurtTroop();
			temp[i]=troop.bytesRead(data);
		}
		hurtsTroops=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWritehurtsTroops(ByteBuffer data)
	{
		if(hurtsTroops!=null&&hurtsTroops.size()>0)
		{
			Object[] array=hurtsTroops.getArray();
			data.writeShort(array.length);
			for(int i=0;i<array.length;i++)
			{
				((HurtTroop)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeShort(0);
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadTroop(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			Troop troop=new Troop();
			temp[i]=troop.bytesRead(data);
		}
		troops=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteTroop(ByteBuffer data)
	{
		if(troops!=null&&troops.size()>0)
		{
			Object[] array=troops.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Troop)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadBuilds(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			temp[i]=PlayerBuild.bytesReadBuild(data);
		}
		builds=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteBuilds(ByteBuffer data)
	{
		if(builds!=null&&builds.size()>0)
		{
			Object[] array=builds.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((PlayerBuild)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}
	/** 玩家事件序列化 */
	public void bytesWriteEvents(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		// 找到玩家岛屿
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null)
			data.writeShort(0);
		else
		{
			data.writeShort(fightEventList.size());
			for(int i=0;i<fightEventList.size();i++)
			{
				FightEvent event=(FightEvent)fightEventList.get(i);
				if(event.getDelete()==FightEvent.DELETE_TYPE) continue;
				SeaBackKit.showByteswrite(data,TimeKit.getSecondTime(),
					event,objectFactory);
			}
		}
	}
	/** 根据sid time num获取到伤兵条例 */
	public HurtTroop getHurtTroop(int sid)
	{
		Object object[]=hurtsTroops.getArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			HurtTroop troop=(HurtTroop)object[i];
			if(troop.getShipSid()==sid)
			{
				return troop;
			}
		}
		return null;
	}

	/** 移除所有伤兵 */
	public synchronized void removeAllTroop()
	{
		hurtsTroops.clear();
	}

	/** 恢复所有伤兵 */
	public synchronized void repairAllHurtTroops()
	{
		Object object[]=hurtsTroops.getArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			HurtTroop troop=(HurtTroop)object[i];
			// troop.nowHurtsNum(TimeKit.getSecondTime());
			addTroop(troop.getShipSid(),troop.getNum(),troops);
		}
	}

	/**
	 * @return builds
	 */
	public ObjectArray getBuilds()
	{
		return builds;
	}

	public Object[] getBuildArray()
	{
		return builds.getArray();
	}

	/**
	 * @param builds 要设置的 builds
	 */
	public void setBuilds(ObjectArray builds)
	{
		this.builds=builds;
	}

	/**
	 * @return player
	 */
	public Player getPlayer()
	{
		return player;
	}

	/**
	 * @param player 要设置的 player
	 */
	public void setPlayer(Player player)
	{
		this.player=player;
	}

	/**
	 * @return troops
	 */
	public ObjectArray getTroops()
	{
		return troops;
	}

	/**
	 * @param troops 要设置的 troops
	 */
	public void setTroops(ObjectArray troops)
	{
		this.troops=troops;
	}

	/**
	 * @return islandLevel
	 */
	public int getIslandLevel()
	{
		return islandLevel;
	}

	/**
	 * @param islandLevel 要设置的 islandLevel
	 */
	public void setIslandLevel(int islandLevel)
	{
		this.islandLevel=islandLevel;
		// 发送change消息
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.PLAYER_LEVEL_ISLAND_EVENT,this,player,null);
	}

	/**
	 * @return state
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * @param state 要设置的 state
	 */
	public void setState(int state)
	{
		this.state=state;
	}

	/**
	 * @return mainGroup
	 */
	public FleetGroup getMainGroup()
	{
		return mainGroup;
	}

	/**
	 * @param mainGroup 要设置的 mainGroup
	 */
	public void setMainGroup(FleetGroup mainGroup)
	{
		this.mainGroup=mainGroup;
	}

	/**
	 * @return buildNum
	 */
	public int getBuildNum()
	{
		return buildNum;
	}

	/**
	 * @param buildNum 要设置的 buildNum
	 */
	public void setBuildNum(int buildNum)
	{
		this.buildNum=buildNum;
	}

	/**
	 * @return iosSystem
	 */
	public int getIosSystem()
	{
		return iosSystem;
	}

	/**
	 * @param iosSystem 要设置的 iosSystem
	 */
	public void setIosSystem(int iosSystem)
	{
		this.iosSystem=iosSystem;
	}

	/**
	 * @param produceNum 要设置的 produceNum
	 */
	public void setProduceNum(int produceNum)
	{
		this.produceNum=produceNum;
	}

	/**
	 * @return hurtsTroops
	 */
	public ObjectArray getHurtsTroops()
	{
		return hurtsTroops;
	}

	/**
	 * @param hurtsTroops 要设置的 hurtsTroops
	 */
	public void setHurtsTroops(ObjectArray hurtsTroops)
	{
		this.hurtsTroops=hurtsTroops;
	}

}
