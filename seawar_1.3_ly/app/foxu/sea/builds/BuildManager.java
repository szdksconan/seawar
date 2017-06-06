package foxu.sea.builds;

import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.Science;
import foxu.sea.Ship;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.builds.produce.ProducePropTrack;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.builds.produce.StandProduce;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.BuildPort;
import foxu.sea.proplist.Prop;
import foxu.sea.shipdata.ShipCheckData;

/**
 * 建筑管理器 author:icetiger
 */
public class BuildManager
{

	CreatObjectFactory objectFactory;
	AutoUpBuildManager autoUpBuilding;
	public void init()
	{
		//初始化玩家建筑自动升级队列
		autoUpBuilding.init(this);
	}

	/** 该岛屿是否拥有此index */
	public boolean isPlayerHaveIndex(Player player,int index)
	{
		return player.isPlayerHaveIndex(index);
	}

	/** 指挥所等级是否足够 */
	public boolean isEnoughDirector(Player player,int level)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(BuildInfo.INDEX_0,
			builds.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.getBuildLevel()>level;
	}

	/** 当前建筑是否在建造物品或者升级 */
	public String isBusying(PlayerBuild checkBuild,Player player,int index)
	{
		/** 检查是否可以建造 */
		if(!checkBuild.checkProduce(player)) return "produce deque is full";
		/** 检查是否在升级 */
		// PlayerBuild upBuild=player.getIsland().getBuildByIndex(index,
		// player.getIsland().getNowbuilding());
		// if(upBuild!=null) return "is uping";
		return null;
	}

	/** 检查建筑队列是否已经满 */
	public boolean isBuildDequeFull(Player player)
	{
		Object[] builds=player.getIsland().getBuildArray();
		int buildingNum=0;
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild.getBuildCompleteTime()!=0) buildingNum++;
		}
		return buildingNum>=player.getIsland().getBuildNum();
	}

	/** 当前位置是否有建筑 */
	public boolean isHaveBuildOnIndex(Player player,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		return checkBuild!=null;
	}

	/** 当前位置是否可以撤除 配置里是否可以撤除 */
	public boolean isRemoveBuildOnIndex(Player player,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.isRemove();
	}

	/** 当前正在建筑中是否有该建筑建造或者升级 */
	public boolean isBuildingOnIndex(Player player,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.getBuildCompleteTime()!=0;
	}

	/** 检查是否能新建一个建筑 */
	public String checkBuildOne(Player player,int index,int buildSid)
	{
		/** 查看建筑队列是否满了 */
		if(isBuildDequeFull(player)) return "deque is full";
		/** 这个位置是否有建筑 */
		if(isHaveBuildOnIndex(player,index))
		{
			PlayerBuild build=player.getIsland().getBuildByIndex(index,null);
			JBackKit.resetOneBuild(player,build);
			return "index is have build";
		}
		/** 正在建筑中是否有 */
		if(isBuildingOnIndex(player,index))
			return "index nowBuilding is have";
		if(!isPlayerHaveIndex(player,index))
		{
			return "index not open";
		}
		PlayerBuild playerBuild=(PlayerBuild)Build.factory
			.getSample(buildSid);
		if(playerBuild==null) return "no buildSid";
		if(!isHaveBuildOnIndex(player,BuildInfo.INDEX_0)
			&&index!=BuildInfo.INDEX_0) return "not enough director";
		/** 该位置是否可以造这个建筑 */
		if(!player.isBuildThisType(playerBuild.getBuildType()+"",index))
		{
			return "index can not build this type";
		}
		int propSidCost=playerBuild.getLevelPropSidCost(1);
		/** 道具是否足够 */
		if(propSidCost!=0&&!player.checkPropEnough(propSidCost,1))
			return "not enough prop";
		/** 资源是否足够 */
		if(!player.checkResource(playerBuild,0)) return "resource limit";
		return null;
	}
	/** 新建一个建筑 */
	public boolean buildOne(Player player,int index,int buildSid)
	{
		/** 扣除资源 */
		PlayerBuild playerBuild=(PlayerBuild)Build.factory
			.newSample(buildSid);
		playerBuild.setIndex(index);
		Island builds=player.getIsland();
		player.reduceResource(playerBuild,0);
		// true为成功 也马上在建筑里面加上
		boolean bool=builds.addBuild(playerBuild);
		if(bool)	autoUpBuilding.containedPlayer2Up(player);
		return bool;
	}

	/** 检查升级一个建筑 */
	public String checkbuildUpLevel(Player player,int index)
	{
		Island builds=player.getIsland();
		/** 是否有建筑 */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		/** 查看建筑队列是否满了 */
		if(isBuildDequeFull(player)) return "deque is full";
		/** 这个位置是否有建筑 */
		if(checkBuild==null) return "index is null";
		/** 是否得到自身升级条件 */
		if(checkBuild.getBuildLevel()>=checkBuild.getMaxLevel())
			return "buildLevel can not levelUp";
		/** 指挥中心等级是否足够 */
		if(!isEnoughDirector(player,checkBuild.getBuildLevel())
			&&index!=BuildInfo.INDEX_0) return "not enough director";
		/** 是否正在升级 */
		if(builds.checkNowBuildingByIndex(index)) return "index is uping";
		int propSidCost=checkBuild.getLevelPropSidCost(checkBuild
			.getBuildLevel());
		/** 道具是否足够 */
		if(propSidCost!=0&&!player.checkPropEnough(propSidCost,1))
			return "not enough prop";
		/** 资源是否足够 */
		if(!player.checkResource(checkBuild,checkBuild.getBuildLevel()))
			return "resource limit";
		return null;
	}

	/** 升级一个建筑 */
	public boolean buildUpLevel(Player player,int index)
	{
		Island builds=player.getIsland();
		/** 是否有建筑 */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		/** 扣除资源 */
		player.reduceResource(checkBuild,checkBuild.getBuildLevel());
		/** 扣除道具 */
		int propSidCost=checkBuild.getLevelPropSidCost(checkBuild
			.getBuildLevel());
		if(propSidCost!=0)
		{
			player.getBundle().decrProp(propSidCost);
		}
		int activypercent=0;
		// 科技建筑加速比列
		AdjustmentData data=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.BUILD_BUFF));
		//建筑加速活动
		int percent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.BUILD_ID);
		if(data!=null)
		{
			activypercent=data.percent;
		}
		int time=checkBuild.getLevelBuildTime(checkBuild.getBuildLevel())
			*100/(100+activypercent)*(100-percent)/100;
		if(time<1) time=1;
		checkBuild.setBuildCompleteTime(TimeKit.getSecondTime()+time);
		checkBuild.setBuildTotleTime(time);
		return true;
	}
	/**
	 * 检查是否能取消一个正在进行的事件 cancelType取消的类型 1=取消建筑 2=取消造东西
	 */
	public String checkCancelBuilding(Player player,int index)
	{
		/** 这个位置是否有建筑 */
		if(!isHaveBuildOnIndex(player,index)) return "index is null";
		/** 算一次 */
		pushAllBuilds(player,TimeKit.getSecondTime());
		/** 是否正在建筑中 */
		if(!isBuildingOnIndex(player,index)) return "index is finish";
		return null;
	}

	/** 取消建筑或造东西事件 */
	public void cancelBuilding(Player player,int index)
	{
		/** 这个位置是否有建筑 */
		Island builds=player.getIsland();
		builds.cancelBuilding(index);
		autoUpBuilding.containedPlayer2Up(player);
	}

	/** 检查科技是否可以升级0到1也属于升级 */
	public String checkProduceScience(Player player,int scienceSid,int index)
	{
		/** 检查是否有船厂 */
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null||checkBuild.getBuildLevel()<=0)
			return "index is null";
		/** 检查是否可以建筑这个sid的科技 */
		if(!checkProducePropSid(checkBuild,scienceSid))
			return "can not build theSid";
		// 不属于科技建筑
		if(checkBuild.getBuildType()!=Build.BUILD_RESEARCH)
			return "is not research";
		String busy=isBusying(checkBuild,player,index);
		if(busy!=null) return busy;
		/** 检查是否可以升级这个科技 */
		if(!checkProducePropSid(checkBuild,scienceSid))
			return "can not build theSid";
		/** 检查资源是否足够 */
		if(!player.checkResourceForScience(scienceSid,checkBuild,player))
			return "resource limit";
		/** 检查等级是否得到限制 */
		if(checkScienceIsLimit(scienceSid,player,checkBuild))
			return "science can not levelUp";
		if(((ScienceProduce)checkBuild.getProduce()).checkProduceSid(player,scienceSid))
		{
			return "science is levelUp";
		}
		return null;
	}

	/** 建筑加速 */
	public Product buildUpScience(Player player,int scienceSid,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild scienceBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(scienceBuild==null) return null;
		Science science=(Science)Science.factory.getSample(scienceSid);
		ScienceProduce produce=(ScienceProduce)scienceBuild.getProduce();
		Science have=produce.getScienceBySid(scienceSid);
		//scienceSid 相同
		if(produce.checkProduceSid(player,scienceSid))
		{
			return null;
		}
		int level=0;
		if(have!=null) level=have.getLevel();
		/** 扣除资源 */
		Resources.reduceResources(player.getResources(),science
			.getLevelMetalCost(level),science.getLevelOilCost(level),science
			.getLevelSiliconCost(level),science.getLevelUraniumCost(level),
			science.getLevelMoneyCost(level),player);
		// 扣除道具
		int propSidCost=science.getLevelPropSidCost(level);
		if(propSidCost!=0)
		{
			player.getBundle().decrProp(propSidCost,1);
		}
		//科技加速活动
		int perscent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.SCIENCE_ID);
		/** 生产 */
		Product product=new Product();
		product.setNum(1);
		product.setSid(scienceSid);
		float time=(float)(science.getLevelBuildTime(level)
			/(float)(0.95+scienceBuild.getBuildLevel()*0.05))*(100-perscent)/100f;
		product.setProduceTime((int)time);
		produce.addProduct(player,product);
		return product;
	}

	/** 检查科技等级是否得到限制 */
	public boolean checkScienceIsLimit(int scienceSid,Player player,
		PlayerBuild scienceBuild)
	{
		if(!(scienceBuild.getProduce() instanceof ScienceProduce))
			return true;
		ScienceProduce produce=(ScienceProduce)scienceBuild.getProduce();
		Science have=produce.getScienceBySid(scienceSid);
		int level=0;
		if(have!=null) level=have.getLevel();
		if(level>=scienceBuild.getBuildLevel()) return true;
		if(level>=player.getHonor()[Player.HONOR_LEVEL_INDEX]) return true;
		return false;
	}

	/** 检查是否能生产船只 城防单位等在建筑内生产的东西 */
	public String checkProduceShips(Player player,int shipSid,int num,
		int index,int type)
	{
		/** 检查是否有船厂 */
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(num==0) return "num is 0";
		if(checkBuild==null||checkBuild.getBuildLevel()<=0)
			return "index is null";
		// 不属于建筑内建造东西的建筑
		if(checkBuild.getBuildType()!=Build.BUILD_AIR
			&&checkBuild.getBuildType()!=Build.BUILD_ARTILLERY
			&&checkBuild.getBuildType()!=Build.BUILD_MISSILE
			&&checkBuild.getBuildType()!=Build.BUILD_SHIP
			&&checkBuild.getBuildType()!=Build.BUILD_SHIP_UPDATE
			&&checkBuild.getBuildType()!=Build.BUILD_STRENTGH_SHIP)
			return "is not shipsBuild";
		String busy=isBusying(checkBuild,player,index);
		if(busy!=null) return busy;
		/** 检查是否可以建筑这个sid的船只 */
		if((type==0||type==PublicConst.SHIP_STRENGTH)&&!checkProducePropSid(checkBuild,shipSid))
			return "can not build theSid";
		if(type==BuildPort.SHIP_UP_LEVEL)
		{
			checkBuild=player.getIsland().getBuildByType(Build.BUILD_SHIP,
				null);
			if(!checkProducePropSid(checkBuild,shipSid))
				return "can not build theSid";
		}
		// // 城防建筑有数量限制
		// if(checkBuild.getBuildType()!=Build.BUILD_SHIP)
		// {
		// int limiteNum=player.getShipNum();
		// // 当前某种城防的兵力
		// int nowNum=player.getIsland().getShipsBySid(shipSid,
		// player.getIsland().getTroops());
		// if((nowNum+num)>limiteNum) return "shipNum limit";
		// }
		/** 检查资源是否足够 */
		if(type==0&&!player.checkResourceForShips(shipSid,num))
		{
			JBackKit.sendResetResources(player);
			return "resource limit";
		}
		if((type==BuildPort.SHIP_UP_LEVEL||type==PublicConst.SHIP_STRENGTH)
			&&!player.checkResourceForUpShips(shipSid,num))
		{
			JBackKit.sendResetResources(player);
			return "resource limit";
		}
		Ship ship=(Ship)Role.factory.getSample(shipSid);
		if(ship==null) return "ship is null";
		int costPropSid[]=ship.getCostPropSid();
		if(costPropSid!=null&&costPropSid.length>0)
		{
			for(int i=0;i<costPropSid.length;i+=2)
			{
				/** 检查是否物品足够 */
				if(!player.checkPropEnough(costPropSid[i],costPropSid[i+1]
					*num)) return "not enough prop";
			}
		}
		if(type==BuildPort.SHIP_UP_LEVEL||type==PublicConst.SHIP_STRENGTH)
		{
			int costShip[]=ship.getUpgradeShipConsume();
			if(costShip!=null&&costShip.length>0)
			{
				for(int i=0;i<costShip.length;i+=2)
				{
					/** 检查是否船只足够 */
					int haveNum=player.getIsland().getShipsBySid(
						costShip[i],null);
					haveNum+=player.getIsland().getShipsBySidForDefendGroup(
						costShip[i]);
					if(costShip[i+1]*num>haveNum) return "not enough ship";
				}
			}
		}
		return null;
	}

	/** 检查是否可以建筑该sid的船只或物品 */
	public boolean checkProducePropSid(PlayerBuild playerBuild,int propSid)
	{
		if(playerBuild.getBuildLevel()<=0) return false;
		if(playerBuild.getBuildType()!=Build.BUILD_AIR
			&&playerBuild.getBuildType()!=Build.BUILD_ARTILLERY
			&&playerBuild.getBuildType()!=Build.BUILD_MISSILE
			&&playerBuild.getBuildType()!=Build.BUILD_SHIP
			&&playerBuild.getBuildType()!=Build.BUILD_SHOP
			&&playerBuild.getBuildType()!=Build.BUILD_RESEARCH
			&&playerBuild.getBuildType()!=Build.BUILD_STRENTGH_SHIP)
			return false;
		return playerBuild.checkLevelPropSid(propSid);
	}

	/** 生产船只 */
	public Product buildShips(Player player,int shipSid,int num,int index,
		int type)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return null;
		Ship ship=(Ship)Role.factory.getSample(shipSid);
		int upTime=ship.getBuildTime();
		/** 扣除资源 */
		if(type==0) player.reduceBuidResource(ship.getCostResources(),num);
		// 扣除道具sid,num
		if(ship.getCostPropSid()!=null&&ship.getCostPropSid().length>0)
		{
			for(int i=0;i<ship.getCostPropSid().length;i+=2)
			{
				player.getBundle().decrProp(ship.getCostPropSid()[i],
					ship.getCostPropSid()[i+1]*num);
			}
		}
		if(type==BuildPort.SHIP_UP_LEVEL||type==PublicConst.SHIP_STRENGTH)
		{
			upTime=ship.getUpgradeTime();
			player.reduceBuidResource(ship.getUpgradeResources(),num);
			IntList list=new IntList();
			// 扣除船只
			if(ship.getUpgradeShipConsume()!=null
				&&ship.getUpgradeShipConsume().length>0)
			{
				boolean bool = false;
				for(int i=0;i<ship.getUpgradeShipConsume().length;i+=2)
				{
					int reduceNum=player.getIsland().reduceShipBySid(
						ship.getUpgradeShipConsume()[i],
						ship.getUpgradeShipConsume()[i+1]*num,null);
					if(reduceNum<ship.getUpgradeShipConsume()[i+1]*num)
					{
						bool=true;
						// 扣除城防里面的
						reduceShips(
							player,
							ship.getUpgradeShipConsume()[i],
							(ship.getUpgradeShipConsume()[i+1]*num-reduceNum));
					}
					list.add(ship.getUpgradeShipConsume()[i]);
					list.add(ship.getUpgradeShipConsume()[i+1]*num);
				}
				if(bool)JBackKit.resetMainGroup(player);
				JBackKit.sendResetTroops(player);
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SHIP_UP_OR_STRENGTH);
			}
			// 升级船只日志
			objectFactory.addShipTrack(0,ShipCheckData.UP_SHIP_PRODUCE,player,
				list,null,false);
		}
		/** 生产 */
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		Product product=new Product();
		product.setNum(num);
		product.setSid(shipSid);
		/** 科技衰减 */
		AdjustmentData adjust=(AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.ADD_ACCURATE_BUFF);
		//军备加速活动
		int percent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.ARM_ID);
		int decr=percent;
		int adpersent=0;
		if(adjust!=null) adpersent=adjust.percent;
		float time=(float)upTime
			/(float)(0.95+checkBuild.getBuildLevel()*0.05+adpersent/100f);
		/** 先取整 */
		time=(int)Math.ceil(time);
		if(decr!=0) time=time*(100-decr)/100;
		/** 先取整 */
		int timeUp=(int)Math.ceil(time);
		product.setProduceTime(timeUp*num);
		produce.addProduct(player,product);
		return product;
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

	/** 检查是否可以制造物品 */
	public String checkProducePorps(Player player,int propsSid,int num,
		int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return "index is null";
		// 不属于建筑内建造东西的建筑
		if(checkBuild.getBuildType()!=Build.BUILD_SHOP)
			return "is not shopBuild";
		String busy=isBusying(checkBuild,player,index);
		if(busy!=null) return busy;
		if(!PublicConst.SWITCH_STATE[2])
		{
			if(SeaBackKit.isContainValue(PublicConst.WORKSHOP_PROSID,propsSid))
				return "can not build theSid";
		}
		Prop prop=(Prop)Prop.factory.getSample(propsSid);
		if(prop==null) return "prop not exist";
		/** 检查是否可以建筑这个sid的物品 */
		if(!checkProducePropSid(checkBuild,propsSid))
			return "can not build theSid";
		/** 检查人物等级 */
		if(!checkBuild.checkPlayerLevelPropSid(propsSid,player.getLevel()))
			return "can not build theSid";
		/** 检查资源是否足够 */
		if(!player.checkResourceForPorps(propsSid,num))
		{
			JBackKit.sendResetResources(player);
			return "resource limit";
		}
		int costPropSid[]=prop.getCostPropSid();
		if(costPropSid!=null&&costPropSid.length>0)
		{
			for(int i=0;i<costPropSid.length;i+=2)
			{
				/** 检查是否物品足够 */
				if(!player.checkPropEnough(costPropSid[i],costPropSid[i+1]
					*num)) return "not enough prop";
			}
		}
		return null;
	}

	/** 建筑物品 */
	public Product buildProps(Player player,int propSid,int num,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return null;
		Prop prop=(Prop)Prop.factory.getSample(propSid);
		/** 扣除资源 */
		player.reduceBuidResource(prop.getCostResources(),num);
		/** 扣除物品 */
		if(prop.getCostPropSid()!=null&&prop.getCostPropSid().length>0)
		{
			for(int i=0;i<prop.getCostPropSid().length;i+=2)
			{
				player.getBundle().decrProp(prop.getCostPropSid()[i],
					prop.getCostPropSid()[i+1]*num);
			}
			// 刷包裹
			JBackKit.sendResetBunld(player);
		}
		/** 生产 */
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		Product product=new Product();
		product.setNum(num);
		product.setSid(propSid);
		int upTime=prop.getBuildTime();
		if(checkBuild.getBuildType()==Build.BUILD_SHOP)
		{
			float time=(float)upTime
				/(float)(0.98+checkBuild.getBuildLevel()*0.02);
			/** 先取整 */
			time=(int)Math.ceil(time);
			/** 先取整 */
			upTime=(int)Math.ceil(time);
		}
		if(upTime==0) upTime=20;
		product.setProduceTime(upTime*num);
		produce.addProduct(player,product);
		// 生成产品id
		int id=SeaBackKit.generateProductId(player,objectFactory,index,produce,product);
		// 查看当前产品是否处于生产状态
		int type=ProducePropTrack.START;
		if(produce.getNowProduce()!=product)
		{
			type=ProducePropTrack.HOLD;
		}
		objectFactory.createProducePropTrack(type,player.getId(),product,
			checkBuild.getIndex(),checkBuild.getSid(),
			checkBuild.getBuildLevel(),id);
		return product;
	}

	/** 检查能否取消船只或者物品生产 取消某个位置建筑的生产 目前可取消物品和船只建造 */
	public String checkCancelProduceShipOrProps(Player player,
		int buildIndex,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(buildIndex,builds
			.getBuilds());
		/** 算一次 */
		pushAllBuilds(player,TimeKit.getSecondTime());
		/** 该位置是否有建筑 */
		if(checkBuild==null) return "index is null";
		/** 是否属于船厂或者车间或者研究院 */
		if(checkBuild.getBuildType()!=Build.BUILD_SHIP
			&&checkBuild.getBuildType()!=Build.BUILD_RESEARCH
			&&checkBuild.getBuildType()!=Build.BUILD_SHOP
			&&checkBuild.getBuildType()!=Build.BUILD_AIR
			&&checkBuild.getBuildType()!=Build.BUILD_ARTILLERY
			&&checkBuild.getBuildType()!=Build.BUILD_MISSILE
			&&checkBuild.getBuildType()!=Build.BUILD_SHIP_UPDATE
			&&checkBuild.getBuildType()!=Build.BUILD_STRENTGH_SHIP)
			return "is not cancel";
		/** 检查能否取消这个index的生产队列 */
		if(!checkBuild.checkCancelProduce(index))
			return "index is not exist";
		return null;
	}

	/**
	 * 取消船只或者物品生产 buildIndex:建筑位置 index：建筑队列下标
	 */
	public void cancleProduceShipOrProps(Player player,int buildIndex,
		int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(buildIndex,builds
			.getBuilds());
		if(checkBuild==null) return;
		// 取消生产 日志
		Produce produce=checkBuild.getProduce();
		if(produce!=null&&produce instanceof StandProduce)
		{
			StandProduce sProduce=(StandProduce)produce;
			Object[] products=sProduce.getProductes().getArray();
			if(products.length>index)
			{
				Product product=(Product)products[index];
				objectFactory.createProducePropTrack(
					ProducePropTrack.CANCEL,player.getId(),product,
					checkBuild.getIndex(),checkBuild.getSid(),
					checkBuild.getBuildLevel(),
					SeaBackKit.getProductId(player,buildIndex,index));
				SeaBackKit.removeProductId(player,buildIndex,index);
			}
		}
		checkBuild.cancelProduce(player,index,objectFactory);
		JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.CANLE_SHIP_UP_STRENGTH);
	}

	/** 检查撤除建筑 只能撤除3中资源 */
	public String checkRemoveBuild(Player player,int index)
	{
		/** 这个位置是否有建筑 */
		if(!isHaveBuildOnIndex(player,index)) return "index is null";
		/** 是否可以撤除 */
		if(!isRemoveBuildOnIndex(player,index))
			return "can not delete this build";
		return null;
	}

	/** 移除一个建筑 */
	public void removeOneBuild(Player player,int index)
	{
		Island builds=player.getIsland();
		/** 是否有建筑 */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild!=null&&checkBuild.isRemove())
		{
			// 移除建筑
			player.getIsland().deleteBuild(index);
			// 刷新繁荣度
			int prosperity=player.getProsperityInfo()[0];
			int prosperityNow=player.getIsland().gotProsperityInfo(
				TimeKit.getSecondTime());
			if(prosperity==prosperityNow)//检测机制没生效则强制推送一次
				JBackKit.sendResetProsperity(player);
		}
	}

	/** 检查是否可以直接完成一个建筑升级 */
	public String checkImmediateUpBuild(int index,Player player)
	{
		Island builds=player.getIsland();
		/** 是否有建筑 */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		/** 这个位置是否有建筑 */
		if(checkBuild==null) return "index is null";
		/** 是否得到自身升级条件 */
		if(checkBuild.getBuildLevel()>=checkBuild.getMaxLevel())
			return "buildLevel can not levelUp";
		/** 指挥中心等级是否足够 */
		if(!isEnoughDirector(player,checkBuild.getBuildLevel())
			&&index!=BuildInfo.INDEX_0) return "not enough director";
		/** 是否正在升级 */
		if(builds.checkNowBuildingByIndex(index)) return "index is uping";
		int needGems=checkBuild.getLevelGemsCost(checkBuild.getBuildLevel());
		if(!Resources.checkGems(needGems,player.getResources()))
			return "not enough gems";
		return null;
	}
	
	/** 直接升级建筑一个 */
	public int immediateUpBuild(int index,Player player,CreatObjectFactory factroy)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return 0;
		// 宝石消耗
		int needGems=checkBuild.getLevelGemsCost(checkBuild.getBuildLevel());
		if(needGems<0||!Resources.checkGems(needGems,player.getResources()))
			return 0;
		// 扣除宝石
		Resources.reduceGems(needGems,player.getResources(),player);
		// 宝石消费记录
		objectFactory.createGemTrack(GemsTrack.BUILD_SPEED_UP,
			player.getId(),needGems,index,
			Resources.getGems(player.getResources()));
		checkBuild.setBuildCompleteTime(TimeKit.getSecondTime());
		player.getIsland().gotNowBuilding(TimeKit.getSecondTime(),factroy);
		autoUpBuilding.containedPlayer2Up(player);
		return needGems;
	}
	
	/** 检查是否可以加速一个建筑完成 */
	public String checkSpeedUpBuild(int index,Player player)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return "build is not exist";
		if(checkBuild.getBuildCompleteTime()==0) return "build is finished";
		int needGems=SeaBackKit.getGemsForTime(checkBuild
			.getBuildCompleteTime()
			-TimeKit.getSecondTime());
		if(needGems==0) return "build is finished";
		if(!Resources.checkGems(needGems,player.getResources()))
			return "not enough gems";
		return null;
	}

	/** 加速建筑一个 */
	public int speedUpBuild(int index,Player player,CreatObjectFactory factory)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return 0;
		// 宝石消耗
		int needGems=SeaBackKit.getGemsForTime(checkBuild
			.getBuildCompleteTime()
			-TimeKit.getSecondTime());
		if(needGems<0||!Resources.checkGems(needGems,player.getResources()))
			return 0;
		// 扣除宝石
		Resources.reduceGems(needGems,player.getResources(),player);
		// 宝石消费记录
		objectFactory.createGemTrack(GemsTrack.BUILD_SPEED_UP,
			player.getId(),needGems,index,
			Resources.getGems(player.getResources()));
		checkBuild.setBuildCompleteTime(TimeKit.getSecondTime());
		player.getIsland().gotNowBuilding(TimeKit.getSecondTime(),factory);
		autoUpBuilding.containedPlayer2Up(player);
		return needGems;
	}

	/** 检查物品生产加速 */
	public String checkProduceUp(int index,Player player)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return "build is not exist";
		if(!(checkBuild.getProduce() instanceof StandProduce))
			return "is not StandProduce";
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		Product product=produce.getNowProduce();
		if(product==null) return "produce is finished";
		int needGems=SeaBackKit.getGemsForTime(product.getFinishTime()
			-TimeKit.getSecondTime());
		if(needGems==0) return "produce is finished";
		if(!Resources.checkGems(needGems,player.getResources()))
			return "not enough gems";
		return null;
	}

	/** 加速建筑 */
	public int produceSpeedUp(int index,Player player)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		Product product=produce.getNowProduce();
		int needGems=SeaBackKit.getGemsForTime(product.getFinishTime()
			-TimeKit.getSecondTime());
		// 扣除宝石
		Resources.reduceGems(needGems,player.getResources(),player);
		// 宝石消费记录
		objectFactory.createGemTrack(GemsTrack.PRODUCE_SPEED_UP,
			player.getId(),needGems,index,
			Resources.getGems(player.getResources()));
		product.setFinishTime(TimeKit.getSecondTime());
		player.getIsland().pushAll(TimeKit.getSecondTime(),objectFactory);
		return needGems;
	}

	/** 结算生产资源 建筑建造 船只建造 */
	public void pushAllBuilds(Player player,int checkTime)
	{
		objectFactory.pushAll(player,checkTime);
	}
	
	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	
	public AutoUpBuildManager getAutoUpBuilding()
	{
		return autoUpBuilding;
	}
	
	public void setAutoUpBuilding(AutoUpBuildManager autoUpBuilding)
	{
		this.autoUpBuilding=autoUpBuilding;
	}
}
