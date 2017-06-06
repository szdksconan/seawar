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
 * �����˿� author:icetiger port:1000
 */
public class BuildPort extends AccessPort
{

	/** �������� */
	public static final int SHIP_UP_LEVEL=1;
	/** ���������� */
	BuildManager mananger;
	/** ���ݻ�ȡ�� */
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
		// ����
		int type=data.readUnsignedByte();
		String str=null;
		// ����index
		int index=data.readUnsignedByte();
		// �½�����
		if(type==PublicConst.BUILD_ADD_TYPE)
		{
			// ����sid
			int buildSid=data.readUnsignedShort();
			str=mananger.checkBuildOne(player,index,buildSid);
			data.clear();
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.buildOne(player,index,buildSid);
		}
		// ��������
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
		// ȡ����ǰ������������
		else if(type==PublicConst.CANCLE_BUILD_OR_LEVELUP)
		{
			// ȡ������ ȡ����������ȡ����ǰ������
			str=mananger.checkCancelBuilding(player,index);
			data.clear();
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.cancelBuilding(player,index);
			//����Զ������ֽ������������֪ͨǰ̨������ˢ��
			data.writeBoolean(!player.getIsland().checkNowBuildingByIndex(index));
		}
		// ��ֻ���߳Ƿ���������
		else if(type==PublicConst.SHIPS_OR_PROP_PRODUCE)
		{
			// ��sid
			int shipSid=data.readUnsignedShort();
			// ��������
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
			// �����ֻ���ĵ��ߣ�ˢ�°���
			Ship ship=(Ship)Role.factory.getSample(shipSid);
			if(ship.getCostPropSid()!=null&&ship.getCostPropSid().length>0)
			{
				JBackKit.sendResetBunld(player);
			}
		}
		// ȡ����ֻ����Ʒ������Ƽ�����
		else if(type==PublicConst.CANLE_SHIP_OR_PROP_PRODUCE)
		{
			// �������е��±�
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
		// ��������
		else if(type==PublicConst.DELETE_BUILD_TYPE)
		{
			str=mananger.checkRemoveBuild(player,index);
			if(str!=null)
			{
				throw new DataAccessException(0,str);
			}
			mananger.removeOneBuild(player,index);
		}
		// �����Ƽ�
		else if(type==PublicConst.SCIENCE)
		{
			// ��sid
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
		// ����αװ
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
		// �Ƿ�ӭս
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
		// ��ʯ����
		else if(type==PublicConst.GEMS_SPEED_TYPE)
		{
			// ��ʯ��������
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
			// �о��¼�����
			else if(typeIn==PublicConst.FIGHT_MOVE_UP)
			{
				int fightEventId=data.readInt();
				str=FightKit.checkUpFightEvent(player,fightEventId,
					objectFactory);
				if(str!=null) throw new DataAccessException(0,str);
				costGems=FightKit.fightEventUp(player,fightEventId,
					objectFactory);
			}
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,costGems);
		}
		// ��Ʒ����
		else if(type==PublicConst.PRODUCE_PROPS)
		{
			// ��Ʒsid
			int propSid=data.readUnsignedShort();
			// ��Ʒ����
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
		// ������ֻ
		else if(type==PublicConst.SHIP_UPGRADE_LEVEL)
		{
			// ��sid
			int shipSid=data.readUnsignedShort();
			// ��������
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
			// �����ֻ���ĵ��ߣ�ˢ�°���
			Ship ship=(Ship)Role.factory.getSample(shipSid);
			if(ship.getCostPropSid()!=null&&ship.getCostPropSid().length>0)
			{
				JBackKit.sendResetBunld(player);
			}
		}
		//���콢��
		else if(type==PublicConst.SHIP_STRENGTH)
		{
			// ��sid
			int shipSid=data.readUnsignedShort();
			// ��������
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
//		���ý����Զ���������
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
		// ����ֱ������
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
			// ����change��Ϣ
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
	 * @param mananger Ҫ���õ� mananger
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
	 * @param objectFactory Ҫ���õ� objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

}
