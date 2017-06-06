package foxu.sea;

import foxu.sea.Player;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UpShipPort;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * �����ȼ�����
 * 
 * @author yw
 * 
 */
public class LevelAbility extends Sample
{

	/** ǰ�ü���Sid */
	public int[] preSid;
	/** ���� */
	public int[] cost;
	/** �����ӳ� */
	public float[] addLife;
	/** �����ӳ� */
	public float[] addAttack;
	/** ��Ӱ�촬ֻ */
	public int[] shipSids;

	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/** �ж��ܷ����� */
	public String canUpLevel(Player player)
	{
		int lvl=player.getShipAbilityLevel(getSid());
		if(player.getLevel()<=lvl)
		{
			return "player lvl limit";
		}
		if(preSid!=null)
		{
			boolean limit=true;
			for(int i=0;i<preSid.length;i++)
			{
				if(preSid[i]<=0)
				{
					limit=false;
					break;
				}
				int prelvl=player.getShipAbilityLevel(preSid[i]);
				if(prelvl>lvl)
				{
					limit=false;
					break;
				}
			}
			if(limit)return "pre lvl limit";
		}
		int starCost=getCost(lvl);
		if(starCost>player.getBundle().getCountBySid(UpShipPort.STAR_STONE_SID))
		{
			return "star stone limit";
		}
		return null;
	}
	/** ��ȡ���� */
	public int getCost(int lvl)
	{
		if(lvl>=cost.length) return cost[cost.length-1];
		return cost[lvl];
	}
	/** ��ȡ�����ӳ� */
	public float getAddLife(int lvl)
	{
		if(lvl-1>=addLife.length) return addLife[addLife.length-1];
		return addLife[lvl-1];
	}
	/** ��ȡ�����ӳ� */
	public float getAddAttack(int lvl)
	{
		if(lvl-1>=addAttack.length) return addAttack[addAttack.length-1];
		return addAttack[lvl-1];
	}
	/** �ж�ĳ���Ƿ���Ӱ�� */
	public boolean isEffect(int shipSid)
	{
		return SeaBackKit.isContainValue(shipSids,shipSid);
	}
}
