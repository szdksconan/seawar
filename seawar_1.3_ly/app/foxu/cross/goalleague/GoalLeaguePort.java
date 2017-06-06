package foxu.cross.goalleague;

import foxu.cross.war.CrossWarPlayer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.util.TimeKit;

/**
 * 跨服积分赛端口
 * 
 * @author Alan
 * 
 */
public class GoalLeaguePort extends AccessPort
{

	/**
	 * GET_CHALLENGE_LIST=1 获取挑战列表, GET_SHOP_LIST=2 获取商店列表, GET_RANK_LIST=3
	 * 获取排行榜
	 */
	public static final int RESET_CHALLENGE_LIST=1,BUY_SHOP_PROP=2,
					GET_RANK_LIST=3,SET_FLEET=4,FIGHT_TARGET=5,
					TARGET_INFO=6,INIT_LEAGUE_PLAYER=7,BUY_BATTLE_COUNT=8;
	CreatObjectFactory objectFactory;
	ClientLeagueManager clm;
	LeagueFightManager lfm;

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
		// 功能调用时进行跨天刷新检测
		int time=TimeKit.getSecondTime();
		clm.checkLeaguePlayerInNewDay(time,player,true);
		int type=data.readUnsignedByte();
		if(type==RESET_CHALLENGE_LIST)
		{
			int count=data.readUnsignedShort();
			String msg=clm.refreshCurrentChallengeList(player,count,time);
			if(msg!=null) throw new DataAccessException(0,msg);
			data.clear();
			clm.showBytesWriteChallengeList(data,player);
		}
		else if(type==BUY_SHOP_PROP)
		{
			int sid=data.readUnsignedShort();
			data.clear();
			String msg=clm.buyProp(sid,player,time);
			if(msg!=null) throw new DataAccessException(0,msg);
			data.writeShort(sid);
			data.writeInt(clm.getLeaguePlayer(player).getBet());
		}
		else if(type==GET_RANK_LIST)
		{
			int page=data.readUnsignedByte();
			data.clear();
			int size=20;
			ArrayList list=clm.getRankList();
			CrossWarPlayer cp=null;
			int num=list.size()-page*size;
			if(num<0) num=0;
			if(num>size) num=size;
			data.writeByte(num);
			for(int i=page*size;i<(page+1)*size;i++)
			{
				if(i>=list.size()) break;
				cp=(CrossWarPlayer)list.get(i);
				clm.showBytesWriteCrossWarPlayer(null,cp,data,false);
			}
		}
		else if(type==SET_FLEET)
		{
			String msg=lfm.deployFleet(player,clm,data,objectFactory);
			if(msg!=null) throw new DataAccessException(0,msg);
			data.clear();
		}
		else if(type==FIGHT_TARGET)
		{
			int targetId=data.readInt();
			data.clear();
			String msg=lfm.applyFight(player,targetId,time,clm,data,
				objectFactory);
			if(msg!=null) throw new DataAccessException(0,msg);
		}
		else if(type==TARGET_INFO)
		{
			int targetId=data.readInt();
			data.clear();
			IntList list=clm.getCurrentChallengeList(player);
			CrossWarPlayer cwp=null;
			for(int i=0;i<list.size();i+=2)
			{
				cwp=clm.getCrossWarPlayer(list.get(i));
				if(cwp.getCrossid()!=targetId) continue;
				boolean isSuccess=list.get(i+1)==LeaguePlayer.CHALLENGE_SUCCESS;
				clm.showBytesWriteCrossWarPlayerDetail(player,cwp,data,
					isSuccess);
				break;
			}
			if(data.length()<=0)
				throw new DataAccessException(0,
					"league target not exist in list");
		}
		else if(type==INIT_LEAGUE_PLAYER)
		{
			data.clear();
			clm.showBytesWriteImmed(objectFactory,player,data,time);
		}
		else if(type==BUY_BATTLE_COUNT)
		{
			int count=data.readUnsignedShort();
			data.clear();
			String msg=clm.buyBattleCount(count,player,time);
			if(msg!=null) throw new DataAccessException(0,msg);
		}
		return data;
	}
	public ClientLeagueManager getClm()
	{
		return clm;
	}

	public void setClm(ClientLeagueManager clm)
	{
		this.clm=clm;
	}

	public LeagueFightManager getLfm()
	{
		return lfm;
	}

	public void setLfm(LeagueFightManager lfm)
	{
		this.lfm=lfm;
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
