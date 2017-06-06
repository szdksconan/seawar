package foxu.sea.port;

import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.LuckyExploredActivity;
import foxu.sea.activity.WarManicActivity;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.util.TimeKit;


/**
 * 战争狂人端口
 * @author yw
 *
 */
public class WarManicPort extends AccessPort
{
	/** RANK排行榜  RANK_AWARD排行奖励 STAGE_AWARD阶段奖励 */
	public static int RANK=0,RANK_AWARD=1,STAGE_AWARD=2,LUCKY_EXPLORED_SINGLE=3,LUCKY_EXPLORED_TEN=4;
	
	CreatObjectFactory objfactory;
	
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
		if(type==RANK)
		{
			WarManicActivity act=(WarManicActivity)ActivityContainer
							.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity over");
			}
			int stype=data.readUnsignedByte();
			data.clear();
			data.writeShort(1);
			if(stype==0)
			{
				act.showBytesWriteRankP(data,player,objfactory);
			}
			else
			{
				act.showBytesWriteRankA(data,player,objfactory);
			}
		}
		else if(type==RANK_AWARD)
		{
			WarManicActivity act=(WarManicActivity)ActivityContainer
							.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity over");
			}
			int stype=data.readUnsignedByte()+WarManicActivity.RANKP;
			int index=data.readUnsignedShort()-1;
			act.getAward(data,stype,index,player);
			
		}
		else if(type==STAGE_AWARD)
		{
			WarManicActivity act=(WarManicActivity)ActivityContainer
							.getInstance().getActivity(ActivityContainer.WAR_MANIC_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity over");
			}
			int stype=data.readUnsignedByte();
			int index=data.readUnsignedByte();
			act.getAward(data,stype,index,player);
		}
		// 幸运探险活动(大富翁) 掷1次
		else if(type==LUCKY_EXPLORED_SINGLE)
		{
			LuckyExploredActivity luckyAct=(LuckyExploredActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.LUCKY_EXPLORED_ID,0);
			if(luckyAct==null||!luckyAct.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(!luckyAct.checkTimes(player.getId(),1))
				throw new DataAccessException(0,"draw times over");
			data.clear();
			luckyAct.move(player,0,data,objfactory);
		}
		// 幸运探险活动(大富翁) 掷10次
		else if(type==LUCKY_EXPLORED_TEN)
		{
			LuckyExploredActivity luckyAct=(LuckyExploredActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.LUCKY_EXPLORED_ID,0);
			if(luckyAct==null||!luckyAct.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(!luckyAct.checkTimes(player.getId(),10))
				throw new DataAccessException(0,"draw times over");
			data.clear();
			luckyAct.move(player,1,data,objfactory);
		}
		return data;
	}

	
	public CreatObjectFactory getObjfactory()
	{
		return objfactory;
	}

	
	public void setObjfactory(CreatObjectFactory objfactory)
	{
		this.objfactory=objfactory;
	}
	

}
