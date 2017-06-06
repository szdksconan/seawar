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
 * ��Ӣս��
 * 
 * @author lhj
 * 
 */
public class EliteCheckPoint extends CheckPoint
{
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
		ElitePoint point=player.getElitePoint();
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
		award.awardLenth(data,player,objectFactory,null,
			new int[]{OfficerTrack.FROM_ELITE});
		return star;
	}
}
