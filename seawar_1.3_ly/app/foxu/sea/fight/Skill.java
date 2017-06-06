/**
 * 
 */
package foxu.sea.fight;

import foxu.fight.Ability;
import foxu.sea.AdjustmentsetUseable;
import foxu.sea.AttrAdjustment;

/**
 * @author rockzyt
 */
public class Skill extends Ability implements AdjustmentsetUseable
{

	/***/
//	public static int MAX_LEVEL=70;
	/* static fields */
	static TargetSelector[] selector;
	static final int[] DEFAULT_PROBABILITY={10000}; 

	/** ���� */
	int[] value;
	/** value����,trueΪ�̶�ֵ,falseΪ�ٷֱ� */
	boolean fix=false;

	int attributeType;

	int skillBaseType[];

	/* fields */
	/** Ŀ��ѡ���� */
	int selectorId;
	/** �ͷŸ���,Ĭ��100% */
	int[] probability=DEFAULT_PROBABILITY;

	/** ÿ����������������� */
	int needPropNum[];

	/* properties */
	/** ����ͷŸ��� */
	public int getProbability()
	{
		if(probability==null||probability.length<=0) return DEFAULT_PROBABILITY[0];
		int level=getLevel();
		if(level>=probability.length) return probability[probability.length-1];
		if(level<=0) return probability[0];
		return probability[level];
	}
	/** ��ü������õ�Ŀ��ѡ���� */
	public TargetSelector getSelector()
	{
		return selector[selectorId];
	}

	/**
	 * @return needPropNum
	 */
	public int[] getNeedPropNum()
	{
		return needPropNum;
	}

	/** ��õ�ǰ������Ҫ����Ʒ���� */
	public int getNeedLevelPropNum()
	{
		if(needPropNum==null) return 1;
		if(getLevel()>=needPropNum.length)
			return needPropNum[needPropNum.length-1];
		return needPropNum[getLevel()];
	}

	/**
	 * @param needPropNum Ҫ���õ� needPropNum
	 */
	public void setNeedPropNum(int[] needPropNum)
	{
		this.needPropNum=needPropNum;
	}

	/** ������� */
	public int getValue()
	{
		return value[getLevel()-1];
	}

	public void setChangeValue(AttrAdjustment adjustment)
	{
		if(getLevel()<=0)return;
		if(attributeType==0)
		{
			for(int i=0;i<skillBaseType.length;i++)
			{
				adjustment.add(skillBaseType[i],getValue(),fix);
			}
		}
		else
		{
			for(int i=0;i<skillBaseType.length;i++)
			{
				adjustment.add(skillBaseType[i],attributeType,
					getValue(),fix);
			}
		}
	}

	public int[] getSkillBaseType()
	{
		return skillBaseType;
	}

	public void setSkillBaseType(int[] skillBaseType)
	{
		this.skillBaseType=skillBaseType;
	}
}
