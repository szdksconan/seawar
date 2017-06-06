package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Role;
import foxu.sea.Ship;
import foxu.sea.builds.AutoUpBuildManager;
import foxu.sea.builds.Build;
import foxu.sea.builds.BuildManager;
import foxu.sea.builds.Island;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.Product;
import foxu.sea.builds.produce.CommandProduce;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.task.TaskEventExecute;

/**
 * 建筑端口 author:icetiger port:1000
 */
public class BuildPort extends AccessPort
{

	/** 舰船升级 */
	public static final int SHIP_UP_LEVEL=1;
	/** 建筑管理器 */
	BuildManager mananger;
	/** 数据获取类 */
	CreatObjectFactory objectFactory;

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
		mananger.pushAllBuilds(player,TimeKit.getSecondTime());
		// 类型
		int type=data.readUnsignedByte();
		String str=null;
		// 建筑index
		int index=data.readUnsignedByte();
		// 新建建筑
		if(type==PublicConst.BUILD_ADD_TYPE)
		{
			// 建筑sid
			int buildSid=data.readUnsignedShort();
			str=mananger.checkBuildOne(player,index,buildSid);
			data.clear();
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.buildOne(player,index,buildSid);
		}
		// 建筑升级
		else if(type==PublicConst.BUILD_LEVEL_UP)
		{
			str=mananger.checkbuildUpLevel(player,index);
			data.clear();
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.buildUpLevel(player,index);
		}
		// 取消当前建筑或者升级
		else if(type==PublicConst.CANCLE_BUILD_OR_LEVELUP)
		{
			// 取消类型 取消建筑或者取消当前的生产
			str=mananger.checkCancelBuilding(player,index);
			data.clear();
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.cancelBuilding(player,index);
			//如果自动升级又将其进行升级，通知前台不再做刷新
			data.writeBoolean(!player.getIsland().checkNowBuildingByIndex(index));
		}
		// 船只或者城防建筑生产
		else if(type==PublicConst.SHIPS_OR_PROP_PRODUCE)
		{
			// 船sid
			int shipSid=data.readUnsignedShort();
			// 船的数量
			int num=data.readUnsignedShort();
			if(num>100)
			{
				SeaBackKit.log.info("produce:"+player.getName()+"  err build shipsid:"+shipSid+"  num:"+num);
				throw new DataAccessException(0,"system error");
			}
			str=mananger.checkProduceShips(player,shipSid,num,index,0);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			Product product=mananger.buildShips(player,shipSid,num,index,0);
			data.clear();
			product.showBytesWrite(data,TimeKit.getSecondTime());
			// 如果船只消耗道具，刷新包裹
			Ship ship=(Ship)Role.factory.getSample(shipSid);
			if(ship.getCostPropSid()!=null&&ship.getCostPropSid().length>0)
			{
				JBackKit.sendResetBunld(player);
			}
		}
		// 取消船只或物品生产或科技升级
		else if(type==PublicConst.CANLE_SHIP_OR_PROP_PRODUCE)
		{
			// 生产队列的下标
			int produceIndex=data.readUnsignedByte();
			str=mananger.checkCancelProduceShipOrProps(player,index,
				produceIndex);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.cancleProduceShipOrProps(player,index,produceIndex);
			JBackKit.sendResetBunld(player);
			data.clear();
			data.writeByte(produceIndex);
		}
		// 撤除建筑
		else if(type==PublicConst.DELETE_BUILD_TYPE)
		{
			str=mananger.checkRemoveBuild(player,index);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.removeOneBuild(player,index);
		}
		// 升级科技
		else if(type==PublicConst.SCIENCE)
		{
			// 船sid
			int scienceSid=data.readUnsignedShort();
			str=mananger.checkProduceScience(player,scienceSid,index);
			if(str!=null)
			{
				Island builds=player.getIsland();
				PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
					.getBuilds());
				JBackKit.resetOneBuild(player,checkBuild);
				throw new DataAccessException(0,str);
			}
			Product product=mananger.buildUpScience(player,scienceSid,index);
			data.clear();
			if(product==null)
			{
				throw new DataAccessException(0,"net is wrong");
			}
			product.showBytesWrite(data,TimeKit.getSecondTime());
		}
		// 隐藏伪装
		else if(type==PublicConst.COMMAND_PRETEND)
		{
			Island builds=player.getIsland();
			PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
				.getBuilds());
			if(checkBuild==null)
				throw new DataAccessException(0,"build is null");
			int dataType=data.readUnsignedByte();
			if(checkBuild.getBuildType()!=Build.BUILD_DIRECTOR)
				throw new DataAccessException(0,"build is not director");
			if(dataType!=CommandProduce.FLAGE
				&&dataType!=CommandProduce.CAMPAIN)
				throw new DataAccessException(0,"dataType is wrong");
			((CommandProduce)(checkBuild.getProduce()))
				.setPretendType(dataType);
		}
		// 是否迎战
		else if(type==PublicConst.COMMAND_FIGHT)
		{
			Island builds=player.getIsland();
			PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
				.getBuilds());
			if(checkBuild==null)
				throw new DataAccessException(0,"build is null");
			if(checkBuild.getBuildType()!=Build.BUILD_DIRECTOR)
				throw new DataAccessException(0,"build is not director");
			boolean isFight=data.readBoolean();
			((CommandProduce)(checkBuild.getProduce())).setFight(isFight);
		}
		// 宝石加速
		else if(type==PublicConst.GEMS_SPEED_TYPE)
		{
			// 宝石加速类型
			int typeIn=data.readUnsignedByte();
			int costGems=0;
			if(typeIn==PublicConst.BUILD_SPEED_UP)
			{
				str=mananger.checkSpeedUpBuild(index,player);
				if(str!=null) throw new DataAccessException(0,str);
				costGems=mananger.speedUpBuild(index,player,objectFactory);
				data.clear();
				player.getIsland().getBuildByIndex(index,null)
				.showBytesWrite(data,TimeKit.getSecondTime());
			}
			else if(typeIn==PublicConst.PRODUCE_SPEED_UP)
			{
				str=mananger.checkProduceUp(index,player);
				if(str!=null)
				{
					JBackKit.sendResetTroops(player);
					throw new DataAccessException(0,str);
				}
				costGems=mananger.produceSpeedUp(index,player);
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SCIENCE_SHIP_COMPLETE);
			}
			// 行军事件加速
			else if(typeIn==PublicConst.FIGHT_MOVE_UP)
			{
				int fightEventId=data.readInt();
				str=FightKit.checkUpFightEvent(player,fightEventId,
					objectFactory);
				if(str!=null) throw new DataAccessException(0,str);
				costGems=FightKit.fightEventUp(player,fightEventId,
					objectFactory);
			}
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,costGems);
		}
		// 物品生产
		else if(type==PublicConst.PRODUCE_PROPS)
		{
			// 物品sid
			int propSid=data.readUnsignedShort();
			// 物品数量
			int num=data.readUnsignedShort();
			str=mananger.checkProducePorps(player,propSid,num,index);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			Product product=mananger.buildProps(player,propSid,num,index);
			data.clear();
			product.showBytesWrite(data,TimeKit.getSecondTime());
		}
		// 升级船只
		else if(type==PublicConst.SHIP_UPGRADE_LEVEL)
		{
			// 船sid
			int shipSid=data.readUnsignedShort();
			// 船的数量
			int num=data.readUnsignedShort();
			str=mananger.checkProduceShips(player,shipSid,num,index,
				SHIP_UP_LEVEL);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			if(num>100)
			{
				SeaBackKit.log.info("up:"+player.getName()+"  err build shipsid:"+shipSid+"  num:"+num);
				throw new DataAccessException(0,"system error");
			}
			Product product=mananger.buildShips(player,shipSid,num,index,
				SHIP_UP_LEVEL);
			data.clear();
			product.showBytesWrite(data,TimeKit.getSecondTime());
			// 如果船只消耗道具，刷新包裹
			Ship ship=(Ship)Role.factory.getSample(shipSid);
			if(ship.getCostPropSid()!=null&&ship.getCostPropSid().length>0)
			{
				JBackKit.sendResetBunld(player);
			}
		}
		//改造舰船
		else if(type==PublicConst.SHIP_STRENGTH)
		{
			// 船sid
			int shipSid=data.readUnsignedShort();
			// 船的数量
			int num=data.readUnsignedShort();
			str=mananger.checkProduceShips(player,shipSid,num,index,
				PublicConst.SHIP_STRENGTH);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			Product product=mananger.buildShips(player,shipSid,num,index,
				PublicConst.SHIP_STRENGTH);
			JBackKit.sendResetBunld(player);
			data.clear();
			product.showBytesWrite(data,TimeKit.getSecondTime());
			
		}
//		设置建筑自动升级属性
		else if(type==PublicConst.AUTO_LEVEL_UP)
		{
			boolean auto=data.readBoolean();
			data.clear();
			if(auto)
			{
				if(AutoUpBuildManager.checkAutoBuild(player))
				{
					mananger.getAutoUpBuilding().addAutoPlayer(player);
				}
				else
				{
					throw new DataAccessException(0,"build auto up disable");
				}
			}
			else
				mananger.getAutoUpBuilding().removeAutoPlayer(player);
		}
		// 建筑直接升级
		else if(type==PublicConst.BUILD_UP_IMMED)
		{
			str=mananger.checkImmediateUpBuild(index,player);
			data.clear();
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			int costGems=mananger.immediateUpBuild(index,player,objectFactory);
			data.clear();
			player.getIsland().getBuildByIndex(index,null)
				.showBytesWrite(data,TimeKit.getSecondTime());
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,costGems);
		}
		return data;
	}

	/**
	 * @return mananger
	 */
	public BuildManager getMananger()
	{
		return mananger;
	}

	/**
	 * @param mananger 要设置的 mananger
	 */
	public void setMananger(BuildManager mananger)
	{
		this.mananger=mananger;
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

}
