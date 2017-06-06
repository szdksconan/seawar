package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;

/***
 * 军备航线
 * 
 * @author lhj
 */
public class ArmsCheckPoint extends CheckPoint
{

	/** 付费次数 **/
	int payCount;
	/** 挑战次数 **/
	int challengTime;
	/** 每次付费的宝石数量 **/
	int[] gems;

	/** 付费次数 **/
	public int getPayCount()
	{
		return payCount;
	}

	public void setPayCount(int payCount)
	{
		this.payCount=payCount;
	}
	/** 挑战次数 **/
	public int getChallengTime()
	{
		return challengTime;
	}

	public void setChallengTime(int challengTime)
	{
		this.challengTime=challengTime;
	}
	/** 每次付费的宝石数量 **/
	public int[] getGems()
	{
		return gems;
	}

	public void setGems(int[] gems)
	{
		this.gems=gems;
	}
	/* methods */
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
		ArmsRoutePoint point=player.getArmsroutePoint();
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
		award.awardLenth(data,player,objectFactory,null,new int[]{EquipmentTrack.FROM_ARMS});
		return star;
	}
}
