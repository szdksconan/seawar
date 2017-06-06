package foxu.fight;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

import mustang.util.Sample;

/**
 * ��˵�������ü�Ч��
 * </p>
 * ����Ч���̳д��࣬����������������Ч������Ч�����͡���Ч��ǿ�Ⱥ�Ч�����õ�����
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class Effect extends Sample
{
	/* fields */
	int cid;
	/** ���� */
	int formulaType;
	/** ���� */
	int level;
	/** ���õ� */
	int spot;
	/** ��Ӫ */
	int camp;
	/** ���� */
	String description;
	/** ����ʱ��,0Ϊ���� */
	int usefulTime;
	/** ѹ������Ч���б� */
	int[][] suppress;
	/** ���ռ��ܶ��� */
	Ability ability;

	/* dynamic fields */
	/** Ч���Ƿ������� */
	boolean enable=true;

	/* properties */
	/** ���Ч������ */
	public String getDescription()
	{
		return description;
	}
	/** �õ����Ч���ļ��ܶ��� */
	public Ability getAbility()
	{
		return ability;
	}
	/** �������Ч���ļ��ܶ��� */
	public void setAbility(Ability ability)
	{
		this.ability=ability;
	}
	/** �Ƿ���� */
	public void setEnable(boolean enable)
	{
		this.enable=enable;
	}
	/** �Ƿ���� */
	public boolean isEnable()
	{
		return enable;
	}
	/** ���ѹ���б� */
	public int[] getSupperss()
	{
		return suppress[level];
	}
	/** ������� */
	public int getFormulaType()
	{
		return formulaType;
	}
	/** ��ü��� */
	public int getLevel()
	{
		return level;
	}
	/** ������õ� */
	public int getSpot()
	{
		return spot;
	}
	/** �����Ӫ */
	public int getCamp()
	{
		return camp;
	}
	/** �õ�Ч�������ûغ��� */
	public int getActiveRound()
	{
		return usefulTime;
	}
	/** ������Ӫ */
	public void setCamp(int c)
	{
		camp=c;
	}
	/** ���cid */
	public int getCid()
	{
		return cid;
	}

	/* methods */
	/**
	 * ��鳬ʱ
	 * 
	 * @param time ��ǰʱ��
	 * @return ����true��ʾ�ѳ�ʱ
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