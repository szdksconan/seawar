package foxu.sea;


/**
 * ���䴬
 * @author Alan
 *
 */
public class TransportShip extends Ship
{
	/** �ɼ��ӳɵ�λ���� */
	int circleThreshold;
	/** �ɼ��ӳ�ֵ 10000/10000 */
	int baseAddition;
	
	public int getCircleThreshold()
	{
		return circleThreshold;
	}
	
	public void setCircleThreshold(int circleThreshold)
	{
		this.circleThreshold=circleThreshold;
	}
	
	public int getBaseAddition()
	{
		return baseAddition;
	}
	
	public void setBaseAddition(int baseAddition)
	{
		this.baseAddition=baseAddition;
	}
	
}
