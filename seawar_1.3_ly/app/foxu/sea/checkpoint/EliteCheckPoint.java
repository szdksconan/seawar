package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.award.Award;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.officer.OfficerTrack;


/***
 * 精英战场
 * 
 * @author lhj
 * 
 */
public class EliteCheckPoint extends CheckPoint
{
	/**
	 * 开始关卡战斗
	 * 
	 * @param attacker 进攻方
	 */
	public Object[] fight(FleetGroup attacker)
	{
		if(island==null) getIsland();
		island.getFleetGroup();
		return attacker!=null?island.fight(attacker):null;
	}

	/** 关卡胜利奖励 */
	public int fightSuccess(Player player,FleetGroup before,ByteBuffer data,
		CreatObjectFactory objectFactory,int sid,boolean sweep)
	{
		// 存储关卡
		ElitePoint point=player.getElitePoint();
		// 计算星级
		int star=SelfCheckPoint.THREE_STAR;
		if(sweep)
		{
			Fleet fleet[]=before.getArray();
			int destoryNum=0;
			for(int i=0;i<fleet.length;i++)
			{
				if(fleet[i]!=null)
				{
					if(fleet[i].getNum()==0)
					{
						destoryNum++;
						star=SelfCheckPoint.TOW_STAR;
					}
					if(destoryNum>=2)
					{
						star=SelfCheckPoint.ONE_STAR;
					}
				}
			}
		}
		point.addPoint(star,sid,nextSid);
		// 发送奖励
		if(sweep) data.writeByte(star);
		Award award=getAward();
		int exp=award.getExperienceAward(player.getLevel());
		if(exp>0)
		{
			// 经验值活动加成
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			award.setExperienceAward(exp);
		}
		award.awardLenth(data,player,objectFactory,null,
			new int[]{OfficerTrack.FROM_ELITE});
		return star;
	}
}
