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
 * ��������
 * 
 * @author lhj
 */
public class ArmsCheckPoint extends CheckPoint
{

	/** ���Ѵ��� **/
	int payCount;
	/** ��ս���� **/
	int challengTime;
	/** ÿ�θ��ѵı�ʯ���� **/
	int[] gems;

	/** ���Ѵ��� **/
	public int getPayCount()
	{
		return payCount;
	}

	public void setPayCount(int payCount)
	{
		this.payCount=payCount;
	}
	/** ��ս���� **/
	public int getChallengTime()
	{
		return challengTime;
	}

	public void setChallengTime(int challengTime)
	{
		this.challengTime=challengTime;
	}
	/** ÿ�θ��ѵı�ʯ���� **/
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
	 * ��ʼ�ؿ�ս��
	 * 
	 * @param attacker ������
	 */
	public Object[] fight(FleetGroup attacker)
	{
		if(island==null) getIsland();
		island.getFleetGroup();
		return attacker!=null?island.fight(attacker):null;
	}

	/** �ؿ�ʤ������ */
	public int fightSuccess(Player player,FleetGroup before,ByteBuffer data,
		CreatObjectFactory objectFactory,int sid,boolean sweep)
	{
		// �洢�ؿ�
		ArmsRoutePoint point=player.getArmsroutePoint();
		// �����Ǽ�
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
		// ���ͽ���
		if(sweep) data.writeByte(star);
		Award award=getAward();
		int exp=award.getExperienceAward(player.getLevel());
		if(exp>0)
		{
			// ����ֵ��ӳ�
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			award.setExperienceAward(exp);
		}
		award.awardLenth(data,player,objectFactory,null,new int[]{EquipmentTrack.FROM_ARMS});
		return star;
	}
}
