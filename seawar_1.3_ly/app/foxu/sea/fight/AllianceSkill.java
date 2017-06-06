package foxu.sea.fight;

import foxu.sea.AttrAdjustment;
import foxu.sea.PublicConst;
import mustang.io.ByteBuffer;
/** 联盟技能 */
public class AllianceSkill extends Skill
{
	/** 当前经验 */
	int nowExp;
	/** 经验配置 */
	int experience[];
	/** 是否联盟主动技能 */
	boolean allianceSkill;
	/** 防御生效 */
	boolean defenceSkill;
	/** 技能对应的舰船类型 */
	int shipType;
	/** 是否是影响敌人技能的技能 */
	boolean isEffectEnemySkill=false;
	
	/* properties */
	/**
	 * 获得技能对应的舰船类型
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
		//修改的战斗属性,不再添加修改值集合
		if(!allianceSkill)
		{
			super.setChangeValue(adjustment);
		}
	}
	
	/** 序列化方法 */
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
	
	/** 增加经验值 */
	public int incrExp(int n,int level)
	{
		if(n<=0||nowExp+n<0) return 0;// 越界判定
		if(getLevel()>level) return 0;
		nowExp+=n;
		int addLevel = flashLevel(level);
		return addLevel;
	}
	
	/** 刷新等级 */
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

	/** 是否是影响敌人技能的技能 */
	public boolean isEffectEnemySkill()
	{
		return isEffectEnemySkill;
	}

	
	public void setEffectEnemySkill(boolean isEffectEnemySkill)
	{
		this.isEffectEnemySkill=isEffectEnemySkill;
	}
}
