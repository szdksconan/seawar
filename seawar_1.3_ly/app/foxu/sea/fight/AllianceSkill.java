package foxu.sea.fight;

import foxu.sea.AttrAdjustment;
import foxu.sea.PublicConst;
import mustang.io.ByteBuffer;
/** ���˼��� */
public class AllianceSkill extends Skill
{
	/** ��ǰ���� */
	int nowExp;
	/** �������� */
	int experience[];
	/** �Ƿ������������� */
	boolean allianceSkill;
	/** ������Ч */
	boolean defenceSkill;
	/** ���ܶ�Ӧ�Ľ������� */
	int shipType;
	/** �Ƿ���Ӱ����˼��ܵļ��� */
	boolean isEffectEnemySkill=false;
	
	/* properties */
	/**
	 * ��ü��ܶ�Ӧ�Ľ�������
	 * 
	 * @return
	 */
	public int getShipType()
	{
		return shipType;
	}
	
	/* methods */
	public void setChangeValue(AttrAdjustment adjustment)
	{
		//�޸ĵ�ս������,��������޸�ֵ����
		if(!allianceSkill)
		{
			super.setChangeValue(adjustment);
		}
	}
	
	/** ���л����� */
	public void bytesWrite(ByteBuffer bb)
	{
		super.bytesWrite(bb);
		bb.writeInt(nowExp);
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		nowExp=data.readInt();
		return this;
	}
	
	/** ���Ӿ���ֵ */
	public int incrExp(int n,int level)
	{
		if(n<=0||nowExp+n<0) return 0;// Խ���ж�
		if(getLevel()>level) return 0;
		nowExp+=n;
		int addLevel = flashLevel(level);
		return addLevel;
	}
	
	/** ˢ�µȼ� */
	public int flashLevel(int level)
	{
		int oldLevel=getLevel();
		if(oldLevel<0) setLevel(0);
		if(oldLevel>level) setLevel(level);
		int addLevel=0;
		int length=PublicConst.MAX_PLAYER_LEVEL<experience.length
			?PublicConst.MAX_PLAYER_LEVEL:experience.length;
		for(int i=getLevel();i<length;i++)
		{
			if(i<experience.length&&nowExp>=experience[i])
			{
				addLevel++;
				nowExp-=experience[i];
				continue;
			}
			break;
		}
		if(nowExp>experience[length-1])
			nowExp=experience[length-1];
		setLevel(oldLevel+addLevel);
		if(getLevel()>level)
		{
			setLevel(level);
			if(level>=experience.length)
			{
				setNowExp(0);
			}else
			{
				setNowExp(experience[level]-1);
			}
			return level-oldLevel;
		}
		return addLevel;
	}

	
	public int[] getExperience()
	{
		return experience;
	}

	
	public void setExperience(int[] experience)
	{
		this.experience=experience;
	}

	
	public int getNowExp()
	{
		return nowExp;
	}

	
	public void setNowExp(int nowExp)
	{
		this.nowExp=nowExp;
	}

	
	public boolean isAllianceSkill()
	{
		return allianceSkill;
	}

	
	public void setAllianceSkill(boolean allianceSkill)
	{
		this.allianceSkill=allianceSkill;
	}

	
	public boolean isDefenceSkill()
	{
		return defenceSkill;
	}

	
	public void setDefenceSkill(boolean defenceSkill)
	{
		this.defenceSkill=defenceSkill;
	}

	/** �Ƿ���Ӱ����˼��ܵļ��� */
	public boolean isEffectEnemySkill()
	{
		return isEffectEnemySkill;
	}

	
	public void setEffectEnemySkill(boolean isEffectEnemySkill)
	{
		this.isEffectEnemySkill=isEffectEnemySkill;
	}
}
