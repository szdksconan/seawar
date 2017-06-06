package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.LevelAbility;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.task.TaskEventExecute;

/**
 * 星石（舰船等级）
 * 
 * @author yw
 * 
 */
public class UpShipPort extends AccessPort
{
	/** 星石sid */
	public final static int STAR_STONE_SID=2009;
	public final static int UP_SHIP=1,RESET_SHIP=2,GET_GEMS_RATE=3;
	
	CreatObjectFactory objectFactory;

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		// TODO 自动生成方法存根
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
		// 升级船只
		if(type==UP_SHIP)
		{
			// 指挥中心等级
			PlayerBuild build=player.getIsland().getBuildByIndex(
				BuildInfo.INDEX_0,null);
			if(build==null
				||build.getBuildLevel()<PublicConst.STAR_STONE_CENTER_LVL)
			{
				throw new DataAccessException(0,"center level limit");
			}
			int sid=data.readUnsignedShort();
			LevelAbility ability=(LevelAbility)LevelAbility.factory
				.getSample(sid);
			if(ability==null)
			{
				throw new DataAccessException(0,"ability is null");
			}
			String check=ability.canUpLevel(player);
			if(check!=null)
			{
				throw new DataAccessException(0,check);
			}
			// 扣星石
			player
				.getBundle().decrProp(STAR_STONE_SID,ability.getCost(player.getShipAbilityLevel(sid)));
			// 升级
			int lvl=player.upShipLevel(sid);
			JBackKit.sendResetBunld(player);
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.SHIP_LV_UP);
			// 日志？
			data.clear();
			data.writeByte(lvl);
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.STAR_STONE_LEVEL_EVENT,null,player,null);
		}
		// 重置船只
		else if(type==RESET_SHIP)
		{
			int sid=data.readUnsignedShort();
			data.clear();
			LevelAbility ability=(LevelAbility)LevelAbility.factory
				.getSample(sid);
			if(ability==null)
			{
				throw new DataAccessException(0,"ability is null");
			}
			int lv=player.getShipAbilityLevel(sid);
			// 如果玩家未持有当前技能
			if(lv<=0)
				throw new DataAccessException(0,"ability is null");
			// 检测是否可以进行重置
			IntList failSids=player.checkShipLevelReset(sid);
			if(failSids.size()>0)
			{
				data.writeBoolean(false);
				data.writeShort(failSids.size());
				for(int i=0;i<failSids.size();i++)
				{
					data.writeShort(failSids.get(i));
				}
				return data;
			}
			int needGems=(int)(lv*PublicConst.RESET_VOID_LAB);
			// 宝石是否足够
			if(!Resources.checkGems(needGems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			Resources.reduceGems(needGems,
				player.getResources(),player);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.RESET_VOID_LAB,
				player.getId(),needGems,sid,
				Resources.getGems(player.getResources()));
			// 重置
			player.removeShipLevel(sid);
			// 累计返回的星石
			int starStones=0;
			for(int i=0;i<lv;i++)
			{
				starStones+=ability.getCost(i);
			}
			// 加星石
			NormalProp starStone=(NormalProp)Prop.factory.newSample(STAR_STONE_SID);
			starStone.setCount(starStones);
			player
				.getBundle().incrProp(starStone,true);
			JBackKit.sendResetBunld(player);
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.CLEAR_SHIP_LV);
			data.writeBoolean(true);
			data.writeByte(0);
		}
		else if(type==GET_GEMS_RATE)
		{
			data.clear();
			data.writeFloat(PublicConst.RESET_VOID_LAB);
		}
		return data;
	}

	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

}
