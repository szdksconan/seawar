package foxu.sea.fight;

/**
 * 伤害计算 BUFF集合
 * @author Administrator
 *
 */
public class FightAttBuffCollection
{
	/**独立增伤*/
	private float attAlonePercent;
	/**必中*/
	private boolean isMustHit;
	/**必爆*/
	private boolean isMustErupt;
	
	public float getAttAlonePercent()
	{
		return attAlonePercent;
	}
	
	public void setAttAlonePercent(float attAlonePercent)
	{
		this.attAlonePercent=attAlonePercent;
	}
	
	public boolean isMustHit()
	{
		return isMustHit;
	}
	
	public void setMustHit(boolean isMustHit)
	{
		this.isMustHit=isMustHit;
	}
	
	public boolean isMustErupt()
	{
		return isMustErupt;
	}
	
	public void setMustErupt(boolean isMustErupt)
	{
		this.isMustErupt=isMustErupt;
	}
	
	/** 内部以加法加成 */
	public void incAttAlonePercent(float percent){
		this.attAlonePercent +=percent;
	}
	
}
