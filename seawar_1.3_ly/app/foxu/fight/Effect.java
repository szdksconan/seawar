package foxu.fight;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

import mustang.util.Sample;

/**
 * 类说明：作用及效果
 * </p>
 * 所有效果继承此类，在子类中描述出该效果的子效果类型、子效果强度和效果作用的属性
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class Effect extends Sample
{
	/* fields */
	int cid;
	/** 类型 */
	int formulaType;
	/** 级别 */
	int level;
	/** 作用点 */
	int spot;
	/** 阵营 */
	int camp;
	/** 描述 */
	String description;
	/** 作用时间,0为永久 */
	int usefulTime;
	/** 压制其他效果列表 */
	int[][] suppress;
	/** 反握技能对象 */
	Ability ability;

	/* dynamic fields */
	/** 效果是否起作用 */
	boolean enable=true;

	/* properties */
	/** 获得效果描述 */
	public String getDescription()
	{
		return description;
	}
	/** 得到这个效果的技能对象 */
	public Ability getAbility()
	{
		return ability;
	}
	/** 设置这个效果的技能对象 */
	public void setAbility(Ability ability)
	{
		this.ability=ability;
	}
	/** 是否可用 */
	public void setEnable(boolean enable)
	{
		this.enable=enable;
	}
	/** 是否可用 */
	public boolean isEnable()
	{
		return enable;
	}
	/** 获得压制列表 */
	public int[] getSupperss()
	{
		return suppress[level];
	}
	/** 获得类型 */
	public int getFormulaType()
	{
		return formulaType;
	}
	/** 获得级别 */
	public int getLevel()
	{
		return level;
	}
	/** 获得作用点 */
	public int getSpot()
	{
		return spot;
	}
	/** 获得阵营 */
	public int getCamp()
	{
		return camp;
	}
	/** 得到效果起作用回合数 */
	public int getActiveRound()
	{
		return usefulTime;
	}
	/** 设置阵营 */
	public void setCamp(int c)
	{
		camp=c;
	}
	/** 获得cid */
	public int getCid()
	{
		return cid;
	}

	/* methods */
	/**
	 * 检查超时
	 * 
	 * @param time 当前时间
	 * @return 返回true表示已超时
	 */
	public boolean timeOut(int time)
	{
		if(usefulTime==0) return false;
		if(time-ability.getStartTime()>=usefulTime)
		{
			enable=false;
			return true;
		}
		return false;
	}
}