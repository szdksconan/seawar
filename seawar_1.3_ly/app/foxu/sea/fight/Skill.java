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

	/** 数据 */
	int[] value;
	/** value类型,true为固定值,false为百分比 */
	boolean fix=false;

	int attributeType;

	int skillBaseType[];

	/* fields */
	/** 目标选择器 */
	int selectorId;
	/** 释放概率,默认100% */
	int[] probability=DEFAULT_PROBABILITY;

	/** 每级提升所需道具数量 */
	int needPropNum[];

	/* properties */
	/** 获得释放概率 */
	public int getProbability()
	{
		if(probability==null||probability.length<=0) return DEFAULT_PROBABILITY[0];
		int level=getLevel();
		if(level>=probability.length) return probability[probability.length-1];
		if(level<=0) return probability[0];
		return probability[level];
	}
	/** 获得技能配置的目标选择器 */
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

	/** 获得当前升级需要的物品数量 */
	public int getNeedLevelPropNum()
	{
		if(needPropNum==null) return 1;
		if(getLevel()>=needPropNum.length)
			return needPropNum[needPropNum.length-1];
		return needPropNum[getLevel()];
	}

	/**
	 * @param needPropNum 要设置的 needPropNum
	 */
	public void setNeedPropNum(int[] needPropNum)
	{
		this.needPropNum=needPropNum;
	}

	/** 获得数据 */
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
