package foxu.sea.fight;

/**
 * �˺����� BUFF����
 * @author Administrator
 *
 */
public class FightAttBuffCollection
{
	/**��������*/
	private float attAlonePercent;
	/**����*/
	private boolean isMustHit;
	/**�ر�*/
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
	
	/** �ڲ��Լӷ��ӳ� */
	public void incAttAlonePercent(float percent){
		this.attAlonePercent +=percent;
	}
	
}
