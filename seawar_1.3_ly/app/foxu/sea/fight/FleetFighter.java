/**
 * 
 */
package foxu.sea.fight;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import foxu.fight.Ability;
import foxu.fight.ChangeAttributeEffect;
import foxu.fight.Effect;
import foxu.fight.FightScene;
import foxu.fight.Fighter;
import foxu.sea.PublicConst;
import foxu.sea.Ship;

/**
 * ս��fighter
 * 
 * @author rockzyt
 */
public class FleetFighter extends Fighter
{

	/* fields */
	/** ս�� */
	Fleet fleet;
	/** ս���еĳ����ٶ� */
	int speed;
	/** ��ǰ���ֻغ��� */
	int currentRound;
	/** ��ѡ�����б� */
	int[] abilitySelectList;
	
	
	/** ���Ѫ��  ����ս�����ܼ��� ÿ��ս��ǰ ����*/
	int fighterMaxHp;
	/** ��λ���� ����ս�������˺�  */
	int shield;

	/* properties */
	/**
	 * ���ܵ����˺�
	 * @param hurt ��������ǰ��ɵ��˺�
	 * @return ���պ���˺�
	 */
	public float reduceHurtByShiled(float hurt){
		if(shield<=0)return hurt;
		System.out.println("���������ܵ��˺�ֵ��"+hurt);
		System.out.println("Ŀ�����ϻ���ֵ��"+shield);
		float h = (int)hurt-shield;
		FightShowEventRecord OfficerChanger = (FightShowEventRecord)getChangeListener();
		if(h>0)
		{
			//�˺�����0  ������ʧ
			OfficerChanger.changeForOfficer(this,FightShowEventRecord.MISS_SHIELD_TIME_OUT_TYPE,null,null);
			shield=0;
			return h;
		}
		//���ܻ��� ��������Ч��
		OfficerChanger.changeForOfficer(this,FightShowEventRecord.MISS_SHIELD_TYPE,null,null);
		shield = shield-(int)hurt;
		return 0;
	}
	
	/** ���ó����ٶ� */
	public void setSpeed(int speed)
	{
		this.speed=speed;
	}
	
	public int getFighterMaxHp()
	{
		return fighterMaxHp;
	}
	
	public void setFighterMaxHp(int fighterMaxHp)
	{
		this.fighterMaxHp=fighterMaxHp;
	}
	
	public int getShield()
	{
		return shield;
	}
	
	public void setShield(int shield)
	{
		this.shield=shield;
	}

	public void setScene(FightScene s)
	{
		super.setScene(s);
		fleet.fightInit();
	}
	/** ���ý��� */
	public void setFleet(Fleet f)
	{
		fleet=f;
	}
	public Fleet getFleet()
	{
		return fleet;
	}
	/** ���ô��ü����б� */
	public void setSelectAbility(int[] list)
	{
		abilitySelectList=list;
	}
	/** ��ȡ���ü����б� */
	public int[] getSelectAbility()
	{
		return abilitySelectList;
	}
	/* methods */
	public Skill getNomalAttack()
	{
		return (Skill)FightScene.abilityFactory.newSample(fleet.getShip()
			.getNormalAbility());
	}
	public float getSpeed()
	{
		return speed;
	}
	/**
	 * ������������
	 * 
	 * @param attr ���Գ���
	 * @param v ��ǰֵ
	 * @return ���ؼӳɹ��������
	 */
	public float computAttrValue(int attr,int v)
	{
		Ability[] abilitys=getAbilityOnSelf();
		Effect[] effects=null;
		float value=0;
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(!abilitys[i].isEnabled()) continue;
			effects=abilitys[i].getEffects();
			for(int j=effects.length-1;j>=0;j--)
			{
				if(effects[j] instanceof ChangeAttributeEffect)
				{
					ChangeAttributeEffect effect=(ChangeAttributeEffect)effects[j];
					if(effect.checkType(attr,0))
					{
						if(effect.getValueType()==ChangeAttributeEffect.PRECENT)
						{
							value+=effect.getValue(this);
						}
						else
						{
							v+=effect.getValue(this);
						}
					}
				}
			}
		}
		return v+v*value/100;
	}
	public float getAttrValue(int attr)
	{
		if(attr==PublicConst.ATTACK)
		{
			return fleet.attack();
		}
		else if(attr==PublicConst.DEFENCE)
		{
			return fleet.defence(1);// ���ڷ��������㷨����,����ط��޷���ù���������,ֻ�з���һ�Ҵ��ķ������ⲿ�����������
		}
		else if(attr==PublicConst.ACCURATE)
		{
			return computAttrValue(PublicConst.ACCURATE,fleet.getAccurate());
		}
		else if(attr==PublicConst.AVOID)
		{
			return computAttrValue(PublicConst.AVOID,fleet.getAvoid());
		}
		else if(attr==PublicConst.FLEET_HP)
		{
			return fleet.getHp();
		}
		else if(attr==PublicConst.SHIP_NUM)
		{
			return fleet.getNumber();
		}
		else if(attr==PublicConst.CRITICAL_HIT)
		{
			return computAttrValue(PublicConst.CRITICAL_HIT,
				fleet.getCritical());
		}
		else if(attr==PublicConst.CRITICAL_HIT_RESIST)
		{
			return computAttrValue(PublicConst.CRITICAL_HIT_RESIST,
				fleet.getDecritical());
		}
		else if(attr>=PublicConst.AIR_RAID&&attr<=PublicConst.NUCLEAR)
		{
			return computAttrValue(attr,fleet.getResist(attr));
		}
		else if(attr>=PublicConst.RESIST_AIR_RAID&&attr<=PublicConst.RESIST_NUCLEAR)
		{
			return fleet.getEquipResist(attr-PublicConst.RESIST_AIR_RAID);
		}
		else if(attr>=PublicConst.ATTACH_BATTLE&&attr<=PublicConst.ATTACH_AIRCRAFT)
		{
			return fleet.getEquipAttach(attr-PublicConst.ATTACH_BASE);
		}
		return 0;
	}
	public int getLevel()
	{
		return fleet.getLevel();
	}
	public float setDynamicAttr(int type,float value)
	{
		if(type==PublicConst.FLEET_HP)
		{
			//����һ����Ѫ�� ����Ѫ�� ��ȥ �仯���Ѫ��
			if(value<0)value=0;//����һ�� ��� ��Щ�ط�û�м��
			this.getScene().getFighterContainer().reduceHp(this.getTeam(),fleet.getHp()-(int)value);
			fleet.setHp((int)value);
		}
		return value;
	}
	public int getFighterType()
	{
		return fleet.getType();
	}
	public boolean isDead()
	{
		return fleet.getNumber()==0;
	}
	public boolean isAttackFighter()
	{
		return fleet.isAttack();
	}
	/** ��ɫ������������صļ��� */
	public void deadClear()
	{
		super.deadClear();
		getScene().removeFighter(this);
	}
	/** ׼���ж�,ѡ����,ѡ��Ŀ�� */
	public void getReady()
	{
		Ship ship=fleet.getShip();
		if(ship.getAbilityListInFight()==null
			||ship.getAbilityListInFight().length<=0)
		{
			useNormalSkill();
		}
		else
		{
			Skill skill=findSkill(ship.getAbilityListInFight());
			if(skill==null)
			{
				useNormalSkill();
			}
			else
			{
				Object target=findTarget(skill);
				if(target==null)
				{
					useNormalSkill();
				}
				else
				{
					setAct(skill,target);
				}
			}
		}
	}
	/** ʹ����ͨ�����ж� */
	public void useNormalSkill()
	{
		Skill skill=getNomalAttack();
		Object target=findTarget(skill);
		setAct(skill,target);
	}
	/**
	 * ѡ��һ������
	 * 
	 * @return ����ѡ����sid,�������0��ʾû���ҵ�����
	 */
	public int selectAbility()
	{
		if(abilitySelectList==null||abilitySelectList.length<=0) return 0;
		int value=getScene().getRandom().randomValue(FightScene.RANDOM_MINI,
			FightScene.RANDOM_MAX);
		int[] temp=abilitySelectList;
		for(int i=0;i<temp.length;i+=2)
		{
			if(value<temp[i+1]) return temp[i];
		}
		return 0;
	}
	/** �ڼ����б����ҵ�һ������ */
	public Skill findSkill(Ability[] list)
	{
		int sid=selectAbility();
		if(sid!=0)
		{
			Skill skill=null;
			for(int i=list.length-1;i>=0;i--)
			{
				skill=(Skill)list[i];
				if(skill.getSid()==sid)
				{
					if(skill instanceof ContainerAbility)
					{
						ContainerAbility temp=(ContainerAbility)skill;
						sid=temp.getAbilitySidByShipSid(fleet.getShip()
							.getSid());
						skill=(Skill)FightScene.abilityFactory
							.newSample(sid);
						skill.setLevel(temp.getLevel());
						return skill;
					}
					else
					{
						return (Skill)skill.clone();
					}
				}
			}
		}
		sid=fleet.getShip().getNormalAbility();
		return (Skill)FightScene.abilityFactory.newSample(sid);
	}
	/** ʵ��ָ�����������ҵ���Ӧ��Ŀ�� */
	public Object findTarget(Skill skill)
	{
		TargetSelector s=skill.getSelector();
		IntKeyHashMap team=null;
		if(skill.getSpread().getSpreadTeam()==0)// 0��ʾĿ���ǶԷ�����
		{
			if(getTeam()==0)
			{
				team=getScene().getFighterContainer().getTeam(1);
			}
			else
			{
				team=getScene().getFighterContainer().getTeam(0);
			}
		}
		else
		{
			team=getScene().getFighterContainer().getTeam(getTeam());
		}
		Object target=TargetSelector.getTarget(this,s,team);
		return target;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		super.showBytesWrite(data);
		fleet.showFightBytesWirte(data);
	}

	public int getCurrentRound()
	{
		return getScene().getCurrentRound();
	}

	public void setCurrentRound(int currentRound)
	{
		this.currentRound=currentRound;
	}
}