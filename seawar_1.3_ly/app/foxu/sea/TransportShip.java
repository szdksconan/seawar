package foxu.sea;


/**
 * 运输船
 * @author Alan
 *
 */
public class TransportShip extends Ship
{
	/** 采集加成单位数量 */
	int circleThreshold;
	/** 采集加成值 10000/10000 */
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
