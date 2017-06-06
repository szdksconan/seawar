package foxu.sea.checkpoint;

import foxu.sea.AttrAdjustment;
import foxu.sea.Ship;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/** 
 * �½���Ϣ 
 * yw
 */
public class Chapter extends Sample
{
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	
	/** buff���ͣ���ʾ�±꣩*/
	public static final int METAL=0,OIL=1,SILICON=2,URANIUM=3,MONEY=4,ATTACK=6,HP=5;
	/** ����-��ʾsid��ϵ  */
	public static final int[] SHOW_SIDS={401,402,403,404,405,406,407};
	/**USE_STATE=1 ʹ��״̬ NO_USER_STATE=0 δʹ��״̬**/
	public static int USE_STATE=1,NO_USER_STATE=0;
	/** �ӳ����� */
	int type;
	
	/** ����-�ȼ� */
	int[] starlevel;
	
	/** �ӳ� */
	int[] addValue;
	
	/** �ؿ������������� */
	int[] awardsStar;

	/** �ؿ����佱��sid */
	int[] awards;

//	/** ��������ȡ�ȼ� */
//	public int getLevel(int star)
//	{
//		for(int i=starlevel.length-1;i>=0;i--)
//		{
//			if(star<starlevel[i])continue;
//			return i+1;
//		}
//		return 0;
//	}
	
	/** ���ȼ���ȡ�ӳɣ�����100�� */
	public int getAddValue(int level)
	{
		if(level>=addValue.length)return addValue[addValue.length-1];
		return addValue[level-1];
	}
	
	/** ���øı�ֵ */
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
