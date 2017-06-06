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
 * ���������� author:icetiger
 */
public class BuildManager
{

	CreatObjectFactory objectFactory;
	AutoUpBuildManager autoUpBuilding;
	public void init()
	{
		//��ʼ����ҽ����Զ���������
		autoUpBuilding.init(this);
	}

	/** �õ����Ƿ�ӵ�д�index */
	public boolean isPlayerHaveIndex(Player player,int index)
	{
		return player.isPlayerHaveIndex(index);
	}

	/** ָ�����ȼ��Ƿ��㹻 */
	public boolean isEnoughDirector(Player player,int level)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(BuildInfo.INDEX_0,
			builds.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.getBuildLevel()>level;
	}

	/** ��ǰ�����Ƿ��ڽ�����Ʒ�������� */
	public String isBusying(PlayerBuild checkBuild,Player player,int index)
	{
		/** ����Ƿ���Խ��� */
		if(!checkBuild.checkProduce(player)) return "produce deque is full";
		/** ����Ƿ������� */
		// PlayerBuild upBuild=player.getIsland().getBuildByIndex(index,
		// player.getIsland().getNowbuilding());
		// if(upBuild!=null) return "is uping";
		return null;
	}

	/** ��齨�������Ƿ��Ѿ��� */
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

	/** ��ǰλ���Ƿ��н��� */
	public boolean isHaveBuildOnIndex(Player player,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		return checkBuild!=null;
	}

	/** ��ǰλ���Ƿ���Գ��� �������Ƿ���Գ��� */
	public boolean isRemoveBuildOnIndex(Player player,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.isRemove();
	}

	/** ��ǰ���ڽ������Ƿ��иý�������������� */
	public boolean isBuildingOnIndex(Player player,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.getBuildCompleteTime()!=0;
	}

	/** ����Ƿ����½�һ������ */
	public String checkBuildOne(Player player,int index,int buildSid)
	{
		/** �鿴���������Ƿ����� */
		if(isBuildDequeFull(player)) return "deque is full";
		/** ���λ���Ƿ��н��� */
		if(isHaveBuildOnIndex(player,index))
		{
			PlayerBuild build=player.getIsland().getBuildByIndex(index,null);
			JBackKit.resetOneBuild(player,build);
			return "index is have build";
		}
		/** ���ڽ������Ƿ��� */
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
		/** ��λ���Ƿ������������� */
		if(!player.isBuildThisType(playerBuild.getBuildType()+"",index))
		{
			return "index can not build this type";
		}
		int propSidCost=playerBuild.getLevelPropSidCost(1);
		/** �����Ƿ��㹻 */
		if(propSidCost!=0&&!player.checkPropEnough(propSidCost,1))
			return "not enough prop";
		/** ��Դ�Ƿ��㹻 */
		if(!player.checkResource(playerBuild,0)) return "resource limit";
		return null;
	}
	/** �½�һ������ */
	public boolean buildOne(Player player,int index,int buildSid)
	{
		/** �۳���Դ */
		PlayerBuild playerBuild=(PlayerBuild)Build.factory
			.newSample(buildSid);
		playerBuild.setIndex(index);
		Island builds=player.getIsland();
		player.reduceResource(playerBuild,0);
		// trueΪ�ɹ� Ҳ�����ڽ����������
		boolean bool=builds.addBuild(playerBuild);
		if(bool)	autoUpBuilding.containedPlayer2Up(player);
		return bool;
	}

	/** �������һ������ */
	public String checkbuildUpLevel(Player player,int index)
	{
		Island builds=player.getIsland();
		/** �Ƿ��н��� */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		/** �鿴���������Ƿ����� */
		if(isBuildDequeFull(player)) return "deque is full";
		/** ���λ���Ƿ��н��� */
		if(checkBuild==null) return "index is null";
		/** �Ƿ�õ������������� */
		if(checkBuild.getBuildLevel()>=checkBuild.getMaxLevel())
			return "buildLevel can not levelUp";
		/** ָ�����ĵȼ��Ƿ��㹻 */
		if(!isEnoughDirector(player,checkBuild.getBuildLevel())
			&&index!=BuildInfo.INDEX_0) return "not enough director";
		/** �Ƿ��������� */
		if(builds.checkNowBuildingByIndex(index)) return "index is uping";
		int propSidCost=checkBuild.getLevelPropSidCost(checkBuild
			.getBuildLevel());
		/** �����Ƿ��㹻 */
		if(propSidCost!=0&&!player.checkPropEnough(propSidCost,1))
			return "not enough prop";
		/** ��Դ�Ƿ��㹻 */
		if(!player.checkResource(checkBuild,checkBuild.getBuildLevel()))
			return "resource limit";
		return null;
	}

	/** ����һ������ */
	public boolean buildUpLevel(Player player,int index)
	{
		Island builds=player.getIsland();
		/** �Ƿ��н��� */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		/** �۳���Դ */
		player.reduceResource(checkBuild,checkBuild.getBuildLevel());
		/** �۳����� */
		int propSidCost=checkBuild.getLevelPropSidCost(checkBuild
			.getBuildLevel());
		if(propSidCost!=0)
		{
			player.getBundle().decrProp(propSidCost);
		}
		int activypercent=0;
		// �Ƽ��������ٱ���
		AdjustmentData data=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.BUILD_BUFF));
		//�������ٻ
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
	 * ����Ƿ���ȡ��һ�����ڽ��е��¼� cancelTypeȡ�������� 1=ȡ������ 2=ȡ���춫��
	 */
	public String checkCancelBuilding(Player player,int index)
	{
		/** ���λ���Ƿ��н��� */
		if(!isHaveBuildOnIndex(player,index)) return "index is null";
		/** ��һ�� */
		pushAllBuilds(player,TimeKit.getSecondTime());
		/** �Ƿ����ڽ����� */
		if(!isBuildingOnIndex(player,index)) return "index is finish";
		return null;
	}

	/** ȡ���������춫���¼� */
	public void cancelBuilding(Player player,int index)
	{
		/** ���λ���Ƿ��н��� */
		Island builds=player.getIsland();
		builds.cancelBuilding(index);
		autoUpBuilding.containedPlayer2Up(player);
	}

	/** ���Ƽ��Ƿ��������0��1Ҳ�������� */
	public String checkProduceScience(Player player,int scienceSid,int index)
	{
		/** ����Ƿ��д��� */
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null||checkBuild.getBuildLevel()<=0)
			return "index is null";
		/** ����Ƿ���Խ������sid�ĿƼ� */
		if(!checkProducePropSid(checkBuild,scienceSid))
			return "can not build theSid";
		// �����ڿƼ�����
		if(checkBuild.getBuildType()!=Build.BUILD_RESEARCH)
			return "is not research";
		String busy=isBusying(checkBuild,player,index);
		if(busy!=null) return busy;
		/** ����Ƿ������������Ƽ� */
		if(!checkProducePropSid(checkBuild,scienceSid))
			return "can not build theSid";
		/** �����Դ�Ƿ��㹻 */
		if(!player.checkResourceForScience(scienceSid,checkBuild,player))
			return "resource limit";
		/** ���ȼ��Ƿ�õ����� */
		if(checkScienceIsLimit(scienceSid,player,checkBuild))
			return "science can not levelUp";
		if(((ScienceProduce)checkBuild.getProduce()).checkProduceSid(player,scienceSid))
		{
			return "science is levelUp";
		}
		return null;
	}

	/** �������� */
	public Product buildUpScience(Player player,int scienceSid,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild scienceBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(scienceBuild==null) return null;
		Science science=(Science)Science.factory.getSample(scienceSid);
		ScienceProduce produce=(ScienceProduce)scienceBuild.getProduce();
		Science have=produce.getScienceBySid(scienceSid);
		//scienceSid ��ͬ
		if(produce.checkProduceSid(player,scienceSid))
		{
			return null;
		}
		int level=0;
		if(have!=null) level=have.getLevel();
		/** �۳���Դ */
		Resources.reduceResources(player.getResources(),science
			.getLevelMetalCost(level),science.getLevelOilCost(level),science
			.getLevelSiliconCost(level),science.getLevelUraniumCost(level),
			science.getLevelMoneyCost(level),player);
		// �۳�����
		int propSidCost=science.getLevelPropSidCost(level);
		if(propSidCost!=0)
		{
			player.getBundle().decrProp(propSidCost,1);
		}
		//�Ƽ����ٻ
		int perscent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.SCIENCE_ID);
		/** ���� */
		Product product=new Product();
		product.setNum(1);
		product.setSid(scienceSid);
		float time=(float)(science.getLevelBuildTime(level)
			/(float)(0.95+scienceBuild.getBuildLevel()*0.05))*(100-perscent)/100f;
		product.setProduceTime((int)time);
		produce.addProduct(player,product);
		return product;
	}

	/** ���Ƽ��ȼ��Ƿ�õ����� */
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

	/** ����Ƿ���������ֻ �Ƿ���λ���ڽ����������Ķ��� */
	public String checkProduceShips(Player player,int shipSid,int num,
		int index,int type)
	{
		/** ����Ƿ��д��� */
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(num==0) return "num is 0";
		if(checkBuild==null||checkBuild.getBuildLevel()<=0)
			return "index is null";
		// �����ڽ����ڽ��춫���Ľ���
		if(checkBuild.getBuildType()!=Build.BUILD_AIR
			&&checkBuild.getBuildType()!=Build.BUILD_ARTILLERY
			&&checkBuild.getBuildType()!=Build.BUILD_MISSILE
			&&checkBuild.getBuildType()!=Build.BUILD_SHIP
			&&checkBuild.getBuildType()!=Build.BUILD_SHIP_UPDATE
			&&checkBuild.getBuildType()!=Build.BUILD_STRENTGH_SHIP)
			return "is not shipsBuild";
		String busy=isBusying(checkBuild,player,index);
		if(busy!=null) return busy;
		/** ����Ƿ���Խ������sid�Ĵ�ֻ */
		if((type==0||type==PublicConst.SHIP_STRENGTH)&&!checkProducePropSid(checkBuild,shipSid))
			return "can not build theSid";
		if(type==BuildPort.SHIP_UP_LEVEL)
		{
			checkBuild=player.getIsland().getBuildByType(Build.BUILD_SHIP,
				null);
			if(!checkProducePropSid(checkBuild,shipSid))
				return "can not build theSid";
		}
		// // �Ƿ���������������
		// if(checkBuild.getBuildType()!=Build.BUILD_SHIP)
		// {
		// int limiteNum=player.getShipNum();
		// // ��ǰĳ�ֳǷ��ı���
		// int nowNum=player.getIsland().getShipsBySid(shipSid,
		// player.getIsland().getTroops());
		// if((nowNum+num)>limiteNum) return "shipNum limit";
		// }
		/** �����Դ�Ƿ��㹻 */
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
				/** ����Ƿ���Ʒ�㹻 */
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
					/** ����Ƿ�ֻ�㹻 */
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

	/** ����Ƿ���Խ�����sid�Ĵ�ֻ����Ʒ */
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

	/** ������ֻ */
	public Product buildShips(Player player,int shipSid,int num,int index,
		int type)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return null;
		Ship ship=(Ship)Role.factory.getSample(shipSid);
		int upTime=ship.getBuildTime();
		/** �۳���Դ */
		if(type==0) player.reduceBuidResource(ship.getCostResources(),num);
		// �۳�����sid,num
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
			// �۳���ֻ
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
						// �۳��Ƿ������
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
			// ������ֻ��־
			objectFactory.addShipTrack(0,ShipCheckData.UP_SHIP_PRODUCE,player,
				list,null,false);
		}
		/** ���� */
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		Product product=new Product();
		product.setNum(num);
		product.setSid(shipSid);
		/** �Ƽ�˥�� */
		AdjustmentData adjust=(AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.ADD_ACCURATE_BUFF);
		//�������ٻ
		int percent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.ARM_ID);
		int decr=percent;
		int adpersent=0;
		if(adjust!=null) adpersent=adjust.percent;
		float time=(float)upTime
			/(float)(0.95+checkBuild.getBuildLevel()*0.05+adpersent/100f);
		/** ��ȡ�� */
		time=(int)Math.ceil(time);
		if(decr!=0) time=time*(100-decr)/100;
		/** ��ȡ�� */
		int timeUp=(int)Math.ceil(time);
		product.setProduceTime(timeUp*num);
		produce.addProduct(player,product);
		return product;
	}
	/** �۳��Ƿ�����ָ��sid��ֻ */
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

	/** ����Ƿ����������Ʒ */
	public String checkProducePorps(Player player,int propsSid,int num,
		int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return "index is null";
		// �����ڽ����ڽ��춫���Ľ���
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
		/** ����Ƿ���Խ������sid����Ʒ */
		if(!checkProducePropSid(checkBuild,propsSid))
			return "can not build theSid";
		/** �������ȼ� */
		if(!checkBuild.checkPlayerLevelPropSid(propsSid,player.getLevel()))
			return "can not build theSid";
		/** �����Դ�Ƿ��㹻 */
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
				/** ����Ƿ���Ʒ�㹻 */
				if(!player.checkPropEnough(costPropSid[i],costPropSid[i+1]
					*num)) return "not enough prop";
			}
		}
		return null;
	}

	/** ������Ʒ */
	public Product buildProps(Player player,int propSid,int num,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return null;
		Prop prop=(Prop)Prop.factory.getSample(propSid);
		/** �۳���Դ */
		player.reduceBuidResource(prop.getCostResources(),num);
		/** �۳���Ʒ */
		if(prop.getCostPropSid()!=null&&prop.getCostPropSid().length>0)
		{
			for(int i=0;i<prop.getCostPropSid().length;i+=2)
			{
				player.getBundle().decrProp(prop.getCostPropSid()[i],
					prop.getCostPropSid()[i+1]*num);
			}
			// ˢ����
			JBackKit.sendResetBunld(player);
		}
		/** ���� */
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		Product product=new Product();
		product.setNum(num);
		product.setSid(propSid);
		int upTime=prop.getBuildTime();
		if(checkBuild.getBuildType()==Build.BUILD_SHOP)
		{
			float time=(float)upTime
				/(float)(0.98+checkBuild.getBuildLevel()*0.02);
			/** ��ȡ�� */
			time=(int)Math.ceil(time);
			/** ��ȡ�� */
			upTime=(int)Math.ceil(time);
		}
		if(upTime==0) upTime=20;
		product.setProduceTime(upTime*num);
		produce.addProduct(player,product);
		// ���ɲ�Ʒid
		int id=SeaBackKit.generateProductId(player,objectFactory,index,produce,product);
		// �鿴��ǰ��Ʒ�Ƿ�������״̬
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

	/** ����ܷ�ȡ����ֻ������Ʒ���� ȡ��ĳ��λ�ý��������� Ŀǰ��ȡ����Ʒ�ʹ�ֻ���� */
	public String checkCancelProduceShipOrProps(Player player,
		int buildIndex,int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(buildIndex,builds
			.getBuilds());
		/** ��һ�� */
		pushAllBuilds(player,TimeKit.getSecondTime());
		/** ��λ���Ƿ��н��� */
		if(checkBuild==null) return "index is null";
		/** �Ƿ����ڴ������߳�������о�Ժ */
		if(checkBuild.getBuildType()!=Build.BUILD_SHIP
			&&checkBuild.getBuildType()!=Build.BUILD_RESEARCH
			&&checkBuild.getBuildType()!=Build.BUILD_SHOP
			&&checkBuild.getBuildType()!=Build.BUILD_AIR
			&&checkBuild.getBuildType()!=Build.BUILD_ARTILLERY
			&&checkBuild.getBuildType()!=Build.BUILD_MISSILE
			&&checkBuild.getBuildType()!=Build.BUILD_SHIP_UPDATE
			&&checkBuild.getBuildType()!=Build.BUILD_STRENTGH_SHIP)
			return "is not cancel";
		/** ����ܷ�ȡ�����index���������� */
		if(!checkBuild.checkCancelProduce(index))
			return "index is not exist";
		return null;
	}

	/**
	 * ȡ����ֻ������Ʒ���� buildIndex:����λ�� index�����������±�
	 */
	public void cancleProduceShipOrProps(Player player,int buildIndex,
		int index)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(buildIndex,builds
			.getBuilds());
		if(checkBuild==null) return;
		// ȡ������ ��־
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

	/** ��鳷������ ֻ�ܳ���3����Դ */
	public String checkRemoveBuild(Player player,int index)
	{
		/** ���λ���Ƿ��н��� */
		if(!isHaveBuildOnIndex(player,index)) return "index is null";
		/** �Ƿ���Գ��� */
		if(!isRemoveBuildOnIndex(player,index))
			return "can not delete this build";
		return null;
	}

	/** �Ƴ�һ������ */
	public void removeOneBuild(Player player,int index)
	{
		Island builds=player.getIsland();
		/** �Ƿ��н��� */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild!=null&&checkBuild.isRemove())
		{
			// �Ƴ�����
			player.getIsland().deleteBuild(index);
			// ˢ�·��ٶ�
			int prosperity=player.getProsperityInfo()[0];
			int prosperityNow=player.getIsland().gotProsperityInfo(
				TimeKit.getSecondTime());
			if(prosperity==prosperityNow)//������û��Ч��ǿ������һ��
				JBackKit.sendResetProsperity(player);
		}
	}

	/** ����Ƿ����ֱ�����һ���������� */
	public String checkImmediateUpBuild(int index,Player player)
	{
		Island builds=player.getIsland();
		/** �Ƿ��н��� */
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		/** ���λ���Ƿ��н��� */
		if(checkBuild==null) return "index is null";
		/** �Ƿ�õ������������� */
		if(checkBuild.getBuildLevel()>=checkBuild.getMaxLevel())
			return "buildLevel can not levelUp";
		/** ָ�����ĵȼ��Ƿ��㹻 */
		if(!isEnoughDirector(player,checkBuild.getBuildLevel())
			&&index!=BuildInfo.INDEX_0) return "not enough director";
		/** �Ƿ��������� */
		if(builds.checkNowBuildingByIndex(index)) return "index is uping";
		int needGems=checkBuild.getLevelGemsCost(checkBuild.getBuildLevel());
		if(!Resources.checkGems(needGems,player.getResources()))
			return "not enough gems";
		return null;
	}
	
	/** ֱ����������һ�� */
	public int immediateUpBuild(int index,Player player,CreatObjectFactory factroy)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return 0;
		// ��ʯ����
		int needGems=checkBuild.getLevelGemsCost(checkBuild.getBuildLevel());
		if(needGems<0||!Resources.checkGems(needGems,player.getResources()))
			return 0;
		// �۳���ʯ
		Resources.reduceGems(needGems,player.getResources(),player);
		// ��ʯ���Ѽ�¼
		objectFactory.createGemTrack(GemsTrack.BUILD_SPEED_UP,
			player.getId(),needGems,index,
			Resources.getGems(player.getResources()));
		checkBuild.setBuildCompleteTime(TimeKit.getSecondTime());
		player.getIsland().gotNowBuilding(TimeKit.getSecondTime(),factroy);
		autoUpBuilding.containedPlayer2Up(player);
		return needGems;
	}
	
	/** ����Ƿ���Լ���һ��������� */
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

	/** ���ٽ���һ�� */
	public int speedUpBuild(int index,Player player,CreatObjectFactory factory)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		if(checkBuild==null) return 0;
		// ��ʯ����
		int needGems=SeaBackKit.getGemsForTime(checkBuild
			.getBuildCompleteTime()
			-TimeKit.getSecondTime());
		if(needGems<0||!Resources.checkGems(needGems,player.getResources()))
			return 0;
		// �۳���ʯ
		Resources.reduceGems(needGems,player.getResources(),player);
		// ��ʯ���Ѽ�¼
		objectFactory.createGemTrack(GemsTrack.BUILD_SPEED_UP,
			player.getId(),needGems,index,
			Resources.getGems(player.getResources()));
		checkBuild.setBuildCompleteTime(TimeKit.getSecondTime());
		player.getIsland().gotNowBuilding(TimeKit.getSecondTime(),factory);
		autoUpBuilding.containedPlayer2Up(player);
		return needGems;
	}

	/** �����Ʒ�������� */
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

	/** ���ٽ��� */
	public int produceSpeedUp(int index,Player player)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
			.getBuilds());
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		Product product=produce.getNowProduce();
		int needGems=SeaBackKit.getGemsForTime(product.getFinishTime()
			-TimeKit.getSecondTime());
		// �۳���ʯ
		Resources.reduceGems(needGems,player.getResources(),player);
		// ��ʯ���Ѽ�¼
		objectFactory.createGemTrack(GemsTrack.PRODUCE_SPEED_UP,
			player.getId(),needGems,index,
			Resources.getGems(player.getResources()));
		product.setFinishTime(TimeKit.getSecondTime());
		player.getIsland().pushAll(TimeKit.getSecondTime(),objectFactory);
		return needGems;
	}

	/** ����������Դ �������� ��ֻ���� */
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
	 * @param objectFactory Ҫ���õ� objectFactory
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
