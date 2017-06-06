package foxu.sea;

/***
 * 
 * 所有buff 计算完 以后的加成
 * @author lhj
 *
 */
public class BuffService extends Service
{

	/**战前全属性加成(对应有哪些加成效果)**/
	int fullAttribute[];
	
	
	
	public int[] getFullAttribute()
	{
		return fullAttribute;
	}
	
	public void setFullAttribute(int[] fullAttribute)
	{
		this.fullAttribute=fullAttribute;
	}
}
