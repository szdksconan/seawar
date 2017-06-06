package foxu.sea.checkpoint;

import foxu.sea.AttrAdjustment;
import foxu.sea.Ship;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/** 
 * 章节信息 
 * yw
 */
public class Chapter extends Sample
{
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	
	/** buff类型（暗示下标）*/
	public static final int METAL=0,OIL=1,SILICON=2,URANIUM=3,MONEY=4,ATTACK=6,HP=5;
	/** 类型-显示sid关系  */
	public static final int[] SHOW_SIDS={401,402,403,404,405,406,407};
	/**USE_STATE=1 使用状态 NO_USER_STATE=0 未使用状态**/
	public static int USE_STATE=1,NO_USER_STATE=0;
	/** 加成类型 */
	int type;
	
	/** 星数-等级 */
	int[] starlevel;
	
	/** 加成 */
	int[] addValue;
	
	/** 关卡宝箱需求星数 */
	int[] awardsStar;

	/** 关卡宝箱奖励sid */
	int[] awards;

//	/** 按星数获取等级 */
//	public int getLevel(int star)
//	{
//		for(int i=starlevel.length-1;i>=0;i--)
//		{
//			if(star<starlevel[i])continue;
//			return i+1;
//		}
//		return 0;
//	}
	
	/** 按等级获取加成（基数100） */
	public int getAddValue(int level)
	{
		if(level>=addValue.length)return addValue[addValue.length-1];
		return addValue[level-1];
	}
	
	/** 设置改变值 */
	public void setChangeValue(AttrAdjustment adjustment,int k2,int level)
	{
		adjustment.add(Ship.ALL_SHIP,k2,getAddValue(level),false);
	}

	
	public int getType()
	{
		return type;
	}

	
	public void setType(int type)
	{
		this.type=type;
	}

	
	public int[] getStarlevel()
	{
		return starlevel;
	}

	
	public void setStarlevel(int[] starlevel)
	{
		this.starlevel=starlevel;
	}

	
	public int[] getAddValue()
	{
		return addValue;
	}

	
	public void setAddValue(int[] addValue)
	{
		this.addValue=addValue;
	}

	public int[] getAwardsStar()
	{
		return awardsStar;
	}

	public void setAwardsStar(int[] awardsStar)
	{
		this.awardsStar=awardsStar;
	}

	public int[] getAwards()
	{
		return awards;
	}

	public void setAwards(int[] awards)
	{
		this.awards=awards;
	}

}
