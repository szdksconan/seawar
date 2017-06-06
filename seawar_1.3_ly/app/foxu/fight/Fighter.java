package foxu.fight;

import java.util.ArrayList;
import java.util.List;

import mustang.event.ChangeListener;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.Sample;
import foxu.fight.FightScene.FightEvent;
import foxu.sea.fight.FightShowEventRecord;

/**
 * ��ս��
 * 
 * @author ZYT
 */
public abstract class Fighter extends Sample
{

	/* static fieldes */
	/** ����ͨѶ�� */
	private static int PLUSID;
	/** ����ͨѶ�� */
	private static int MINUSID;
	/** ����ͨѶ��ͬ������ */
	private static Object PLUSLOCK=new Object();
	/** ����ͨѶ��ͬ������ */
	private static Object MINUSLOCK=new Object();
	/** ��ɫ���ͳ�����TYPE_PLAYER=1���,TYPE_MONSTER=2���� */
	public static final int TYPE_PLAYER=1,TYPE_NPC=2;

	// /* static methods */
	// public static Sample bytesReadFighter(ByteBuffer data,
	// SampleFactory factory)
	// {
	// int sid=data.readUnsignedShort();
	// if(sid==0) return null;
	// Fighter f=(Fighter)factory.newSample(sid);
	// if(f==null)
	// throw new DataAccessException(
	// DataAccessException.CLIENT_SDATA_ERROR,
	// Fighter.class.getName()+" bytesRead, invalid sid:"+sid);
	// return f.bytesRead(data);
	// }
	/** ��ȡ�µ�����ͨѶ�� */
	public static int newPlusId()
	{
		synchronized(PLUSLOCK)
		{
			PLUSID++;
			if(PLUSID<=0) PLUSID=1;
			return PLUSID;
		}
	}
	/** ��ȡ�µĸ���ͨѶ�� */
	public static int newMinusId()
	{
		synchronized(MINUSLOCK)
		{
			MINUSID--;
			if(MINUSID>=0) MINUSID=-1;
			return MINUSID;
		}
	}

	/* fields */
	/** ���ұ�ʾ.��;:���Ա��������,��Ӫ���,����� ���ڸ�������µ�ս����ʾ���ܵ�����Ŀ�� */
	int team;
	/** ����λ�� */
	int location;
	/** ���ڳ��� */
	FightScene scene;
	/** ʹ�õļ��� */
	Ability ability;
	/** Ŀ����� */
	Object target;
	/** �������ϵ�Ч���б� */
	AbilityList abilityList=new AbilityList(this);
	/** ʹ�ù����д������Ƶļ��ܵ��б�*/
	IntKeyHashMap useLimitNumSkill = new IntKeyHashMap();
	/** ���εȼ� */
	int officerLv;
	/** �������ǰ�˼���Ч�� ǰ�˼��ؼ����м����ӳ�  ���ڳ����ȼ��ؼ��� �غϿ�ʼ���Ƴ���ǰ��Ҫ����ļ���**/
	List needClean = new ArrayList();
	
	/** ��ȡ���Fighter��ʱ���Ƿ�ѡ�� */
	private boolean isSelect;
	/** fighter��ΨһID */
	private int uid;

	/* constructor */
	protected Fighter()
	{
	}

	/* properties */
	/** ���Ŀ�� */
	public Object getTarget()
	{
		return target;
	}
	/** ��ù��ؼ����б� */
	public AbilityList getAbilityList()
	{
		return abilityList;
	}
	/** ���fighter��ΨһID */
	public int getUid()
	{
		return uid;
	}
	/** �����Ƿ�ѡ�� */
	public void setSelect(boolean select)
	{
		isSelect=select;
	}
	/** �ж��Ƿ�ѡ�� */
	public boolean isSelect()
	{
		return isSelect;
	}
	/** �����ڶ����е��±� */
	public void setLocation(int index)
	{
		this.location=index;
	}
	/** ���ö���� */
	public void setTeam(int team)
	{
		this.team=team;
	}
	/** ��ö���� */
	public int getTeam()
	{
		return team;
	}
	/** ��ó��� */
	public FightScene getScene()
	{
		return scene;
	}
	/** ���ó��� */
	public void setScene(FightScene scene)
	{
		this.scene=scene;
	}
	/** ��ü����� */
	public ChangeListener getChangeListener()
	{
		if(scene==null) return null;
		return scene.getChangeListener();
	}

	/* methods */
	/**
	 * ��ʼ��Fighter
	 * 
	 * @param type
	 */
	public void init(int type)
	{
		uid=(type>0)?(-10000-newMinusId()):(10000+newPlusId());
	}
	/**
	 * �Ƴ�ָ��Fighter�ҵ���Fighter���ϵļ���
	 * 
	 * @param fighter ԴFighter
	 * @return �����Ƴ��ļ���
	 */
	public void removeSourceAbility(Fighter fighter)
	{
		if(fighter==null) return;
		abilityList.removeSourceAbility(fighter);
	}
	/**
	 * �Ƴ�ָ��fighter�ͷŵ������ɫ���ϵ�perenity����
	 * 
	 * @param fighter �ͷŹ⻷��fighter
	 * @return ���ر��Ƴ��ļ���
	 */
	public void removePerennityAbility(Fighter fighter)
	{
		if(fighter==null) return;
		if(fighter==this) return;
		abilityList.removePerennityAbility(fighter);
	}
	/**
	 * �鿴�Ƿ���ָ�����Ϳ��õĿ���Ч��
	 * 
	 * @param ability ��ǰʹ�õļ���
	 * @return ����true��ʾ������
	 */
	public boolean checkControl(Ability ability)
	{
		Ability[] abilitys=getAbilityOnSelf();
		Effect[] effects=null;
		for(int i=abilitys.length-1,j=0;i>=0;i--)
		{
			if(abilitys[i].isEnabled())
			{
				effects=abilitys[i].getEffects();
				for(j=effects.length-1;j>=0;j--)
				{
					if(effects[j].enable
						&&effects[j] instanceof ControlEffect)
					{
						if(((ControlEffect)effects[j]).checkControl(this,
							ability)) return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * �Ƴ�һ������
	 * 
	 * @param ability ��Ҫ�Ƴ��ļ���
	 * @return �����Ƴ��Ƿ�ɹ�
	 */
	public boolean removeAbility(Ability ability)
	{
		if(ability==null) return false;
		abilityList.remove(ability);
		ChangeListener listener=getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.CLEAN_ABILITY.ordinal(),this,
				ability);
		return true;
	}
	/**
	 * ����ɫ��һ������
	 * 
	 * @param ability ��Ҫ�ҵļ���
	 * @return �����Ƿ���ӳɹ�
	 */
	public boolean addAbility(Ability ability,int round)
	{
		if(ability==null) return false;
		boolean addSuccess=abilityList.add(ability,round,needClean);
		if(addSuccess)
		{
			Effect[] effects=ability.getEffects();
			for(int i=effects.length-1;i>=0;i--)
			{
				if(effects[i] instanceof IntervalChangeEffect
					&&!(effects[i] instanceof TheCurrentNoEffect))
				{
					IntervalChangeEffect effect=(IntervalChangeEffect)effects[i];
					effect.changeAttr(this);
				}
			}
			ChangeListener listener=getChangeListener();
			if(listener!=null)
			{	
				listener.change(this,FightEvent.ADD_ABILITY.ordinal(),
					ability);
			}
			
		}
		return addSuccess;
	}
	
	/**
	 * ����ɫ��һ������ (���ټ���)
	 * @param ability
	 * @param round
	 * @return
	 */
	public boolean addAbilityForSource(Ability ability,int round)
	{
		if(ability==null) return false;
		boolean addSuccess=abilityList.add(ability,round,needClean);
		if(addSuccess)
		{
			FightShowEventRecord listener=(FightShowEventRecord)getChangeListener();
			if(listener!=null)
			{	
				listener.change(this,FightEvent.ADD_ABILITY.ordinal(),
					ability,ability);
			}
		}
		return addSuccess;
	}
	
	/**
	 * ���͸�ǰ�� (��������Ѿ��Ƴ��ļ���Ч��)
	 * ��Ϊ���ڼ��ؼ��ܺ��Ƴ�����ǰ�˲���һ��ʱ�����ϣ�
	 * �����Ƴ�Ȼ������µļ����м���м��ʱ�䣬
	 * �����ȼ��ؼ��� �������ں��ʵ�ʱ�䣨ÿ�غϿ�ʼ���㼼��ʧЧ��ʱ�����Ƴ���ǰ��Ч��
	 */
	public void clearClientAbility(){
		ChangeListener listener=getChangeListener();
		for(int i=0;i<needClean.size();i++){
			if(listener!=null){
				listener.change(abilityList,FightEvent.ABILITY_TIME_OUT.ordinal(),this,needClean.get(i));
			}
		}
		needClean.clear();
	}
	
	
	/** �õ��Լ����Ϲ��ŵļ��� */
	public Ability[] getAbilityOnSelf()
	{
		return abilityList.getAllAbility();
	}
	/**
	 * ���ü���
	 * 
	 * @param ability �ͷŵļ��ܶ���
	 */
	public void setAct(Ability ability,Object target)
	{
		this.ability=ability;
		if(getAbilityList().isMustHit())this.ability.setMustHit(true);
		this.target=target;
		if(scene==null) return;
		ChangeListener listener=getChangeListener();
		if(listener!=null) listener.change(this,FightEvent.READY.ordinal());
	}
	/** �����һ�غ�ʹ�õļ��ܣ���ѡ���Ŀ�� */
	public void clearTarget()
	{
		ability=null;
		target=null;
	}
	/** �����ͨ�������� */
	abstract public Ability getNomalAttack();
	/** �����ж�ֵ�������ж�ֵȷ���ж�˳�� */
	abstract public float getSpeed();
	/** �õ�fighter��һ������ */
	abstract public float getAttrValue(int attr);
	/** ��ý�ɫ�ȼ� */
	abstract public int getLevel();
	/** ��ȡ��ǰ���ֻغ��� */
	abstract public int getCurrentRound();
	/** ���õ�ǰ���ֻغ��� */
	abstract public void setCurrentRound(int currentRound);
	/**
	 * ���ö�̬����
	 * 
	 * @param type ��������
	 * @param value ֵ
	 */
	abstract public float setDynamicAttr(int type,float value);
	/** ���ս�������� */
	abstract public int getFighterType();
	/** �õ�ʩ�ŵļ��� */
	public Ability getAbility()
	{
		return ability;
	}
	/** �õ��±� */
	public int getLocation()
	{
		return location;
	}
	/** �жϽ�ɫ�Ƿ��Ѿ��ж� */
	public boolean isActed()
	{
		return ability!=null;
	}
	/** �жϽ�ɫ�Ƿ����� */
	abstract public boolean isDead();
	/** �жϽ�ɫ�Ƿ������������� */
	abstract public boolean isAttackFighter();
	/**
	 * ˢ�¹⻷��
	 * 
	 * @param fighter ����fighter
	 */
	public void flushHalo(Fighter[] fighter)
	{
		abilityList.spreadHalo(fighter);
	}
	/**
	 * ˢ�¼��� </p> ˢ�����ϵ�ÿ�غϴ���һ�ε�Ч��,�Ƴ���ʱ����
	 * 
	 * @param time ��ǰ��ʱ��
	 * @param code ˢ�µ�(����:�غϿ�ʼ,�ж���ʼ,�ͷż���...)
	 */
	public void flushAbility(int time,ChangeListener listener)
	{
		clearClientAbility();
		//�غϿ�ʼʱ����ĳЩ����
		abilityList.spreadTouchOffAbility(TouchOffSpread.ROUND_START,time,listener);
		abilityList.flushIntervalChangeEffect(time);
		abilityList.timeOutAbility(time);
	}
	/** ��ɫ������������صļ��� */
	public void deadClear()
	{
		ChangeListener listener=getChangeListener();
		if(listener!=null)
			listener.change(this,FightEvent.DEAD.ordinal(),this);
		abilityList.deadClear();
		// clearTarget();
	}

	public void clear()
	{
		abilityList.overClear();
		clearTarget();
		scene=null;
	}
	/** ���л����� */
	public void showBytesWrite(ByteBuffer bb)
	{
		// System.out.println("............Fighter");
		// super.bytesWrite(bb);
		// System.out.println("bytesWrite1:"
		// +CodecKit.getCrc32(bb.getArray(),bb.offset(),bb.top()));
		bb.writeInt(uid);
		bb.writeByte(team);
		bb.writeByte(location);
		// System.out.println("bytesWrite2:"
		// +CodecKit.getCrc32(bb.getArray(),bb.offset(),bb.top()));
		// Ability[] abilitys=abilityList.getAllAbility();
		// bb.writeByte(abilitys.length);
		// for(int i=0;i<abilitys.length;i++)
		// {
		// abilitys[i].bytesWrite(bb);
		// }
		// System.out.println("bytesWrite3:"
		// +CodecKit.getCrc32(bb.getArray(),bb.offset(),bb.top()));
	}
	// /** �����л����� */
	// public Fighter bytesRead(ByteBuffer bb)
	// {
	// super.bytesRead(bb);
	// uid=bb.readInt();
	// team=bb.readInt();
	// location=bb.readInt();
	// int length=bb.readUnsignedByte();
	// Ability[] abilitys=new Ability[length];
	// for(int i=0;i<length;i++)
	// {
	// abilitys[i]=Ability.bytesReadAbility(bb,
	// FightScene.abilityFactory);
	// }
	// abilityList=new AbilityList(this);
	// abilityList.list.add(abilitys);
	// return this;
	// }
	/** ��㸴�� */
	public Object copy(Object obj)
	{
		Fighter f=(Fighter)super.copy(obj);
		f.ability=null;
		f.abilityList=new AbilityList(f);
		f.target=null;
		return obj;
	}
	
	public IntKeyHashMap getUseLimitNumSkill()
	{
		return useLimitNumSkill;
	}
	
	public void setUseLimitNumSkill(IntKeyHashMap useLimitNumSkill)
	{
		this.useLimitNumSkill=useLimitNumSkill;
	}
	
	public int getOfficerLv()
	{
		return officerLv;
	}
	
	public void setOfficerLv(int officerLv)
	{
		this.officerLv=officerLv;
	}
	
	
}