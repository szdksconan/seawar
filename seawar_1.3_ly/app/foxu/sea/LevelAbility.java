package foxu.sea;

import foxu.sea.Player;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UpShipPort;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * 舰船等级技能
 * 
 * @author yw
 * 
 */
public class LevelAbility extends Sample
{

	/** 前置技能Sid */
	public int[] preSid;
	/** 消耗 */
	public int[] cost;
	/** 生命加成 */
	public float[] addLife;
	/** 攻击加成 */
	public float[] addAttack;
	/** 受影响船只 */
	public int[] shipSids;

	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	/** 判断能否升级 */
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
	/** 获取消耗 */
	public int getCost(int lvl)
	{
		if(lvl>=cost.length) return cost[cost.length-1];
		return cost[lvl];
	}
	/** 获取生命加成 */
	public float getAddLife(int lvl)
	{
		if(lvl-1>=addLife.length) return addLife[addLife.length-1];
		return addLife[lvl-1];
	}
	/** 获取攻击加成 */
	public float getAddAttack(int lvl)
	{
		if(lvl-1>=addAttack.length) return addAttack[addAttack.length-1];
		return addAttack[lvl-1];
	}
	/** 判断某船是否受影响 */
	public boolean isEffect(int shipSid)
	{
		return SeaBackKit.isContainValue(shipSids,shipSid);
	}
}
