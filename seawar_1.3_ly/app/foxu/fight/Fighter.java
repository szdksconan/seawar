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
 * 参战者
 * 
 * @author ZYT
 */
public abstract class Fighter extends Sample
{

	/* static fieldes */
	/** 正数通讯号 */
	private static int PLUSID;
	/** 负数通讯号 */
	private static int MINUSID;
	/** 正数通讯号同步对象 */
	private static Object PLUSLOCK=new Object();
	/** 负数通讯号同步对象 */
	private static Object MINUSLOCK=new Object();
	/** 角色类型常量：TYPE_PLAYER=1玩家,TYPE_MONSTER=2怪物 */
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
	/** 获取新的正数通讯号 */
	public static int newPlusId()
	{
		synchronized(PLUSLOCK)
		{
			PLUSID++;
			if(PLUSID<=0) PLUSID=1;
			return PLUSID;
		}
	}
	/** 获取新的负数通讯号 */
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
	/** 敌我标示.用途:可以保存队伍编号,阵营编号,帮会编号 用于各种情况下的战斗标示技能的作用目标 */
	int team;
	/** 所在位置 */
	int location;
	/** 所在场景 */
	FightScene scene;
	/** 使用的技能 */
	Ability ability;
	/** 目标对象 */
	Object target;
	/** 挂在身上的效果列表 */
	AbilityList abilityList=new AbilityList(this);
	/** 使用过的有次数限制的技能的列表*/
	IntKeyHashMap useLimitNumSkill = new IntKeyHashMap();
	/** 军衔等级 */
	int officerLv;
	/** 用于清除前端技能效果 前端加载技能中间有延迟  现在尝试先加载技能 回合开始再移除以前需要清除的技能**/
	List needClean = new ArrayList();
	
	/** 在取随机Fighter的时候是否被选中 */
	private boolean isSelect;
	/** fighter的唯一ID */
	private int uid;

	/* constructor */
	protected Fighter()
	{
	}

	/* properties */
	/** 获得目标 */
	public Object getTarget()
	{
		return target;
	}
	/** 获得挂载技能列表 */
	public AbilityList getAbilityList()
	{
		return abilityList;
	}
	/** 获得fighter的唯一ID */
	public int getUid()
	{
		return uid;
	}
	/** 设置是否被选中 */
	public void setSelect(boolean select)
	{
		isSelect=select;
	}
	/** 判断是否被选中 */
	public boolean isSelect()
	{
		return isSelect;
	}
	/** 设置在队伍中的下标 */
	public void setLocation(int index)
	{
		this.location=index;
	}
	/** 设置队伍号 */
	public void setTeam(int team)
	{
		this.team=team;
	}
	/** 获得队伍号 */
	public int getTeam()
	{
		return team;
	}
	/** 获得场景 */
	public FightScene getScene()
	{
		return scene;
	}
	/** 设置场景 */
	public void setScene(FightScene scene)
	{
		this.scene=scene;
	}
	/** 获得监听器 */
	public ChangeListener getChangeListener()
	{
		if(scene==null) return null;
		return scene.getChangeListener();
	}

	/* methods */
	/**
	 * 初始化Fighter
	 * 
	 * @param type
	 */
	public void init(int type)
	{
		uid=(type>0)?(-10000-newMinusId()):(10000+newPlusId());
	}
	/**
	 * 移除指定Fighter挂到此Fighter身上的技能
	 * 
	 * @param fighter 源Fighter
	 * @return 返回移除的技能
	 */
	public void removeSourceAbility(Fighter fighter)
	{
		if(fighter==null) return;
		abilityList.removeSourceAbility(fighter);
	}
	/**
	 * 移除指定fighter释放到这个角色身上的perenity技能
	 * 
	 * @param fighter 释放光环的fighter
	 * @return 返回被移除的技能
	 */
	public void removePerennityAbility(Fighter fighter)
	{
		if(fighter==null) return;
		if(fighter==this) return;
		abilityList.removePerennityAbility(fighter);
	}
	/**
	 * 查看是否有指定类型可用的控制效果
	 * 
	 * @param ability 当前使用的技能
	 * @return 返回true表示被控制
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
	 * 移除一个技能
	 * 
	 * @param ability 需要移除的技能
	 * @return 返回移除是否成功
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
	 * 给角色挂一个技能
	 * 
	 * @param ability 需要挂的技能
	 * @return 返回是否添加成功
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
	 * 给角色挂一个技能 (军官技能)
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
	 * 推送给前端 (清除身上已经移除的技能效果)
	 * 因为现在加载技能和移除技能前端不在一个时间线上，
	 * 技能移除然后加载新的技能中间会有间隔时间，
	 * 现在先加载技能 后续再在合适的时间（每回合开始计算技能失效的时候）再移除以前的效果
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
	
	
	/** 得到自己身上挂着的技能 */
	public Ability[] getAbilityOnSelf()
	{
		return abilityList.getAllAbility();
	}
	/**
	 * 设置技能
	 * 
	 * @param ability 释放的技能对象
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
	/** 清除上一回合使用的技能，和选择的目标 */
	public void clearTarget()
	{
		ability=null;
		target=null;
	}
	/** 获得普通攻击技能 */
	abstract public Ability getNomalAttack();
	/** 计算行动值，根据行动值确定行动顺序 */
	abstract public float getSpeed();
	/** 得到fighter的一个属性 */
	abstract public float getAttrValue(int attr);
	/** 获得角色等级 */
	abstract public int getLevel();
	/** 获取当前出手回合数 */
	abstract public int getCurrentRound();
	/** 设置当前出手回合数 */
	abstract public void setCurrentRound(int currentRound);
	/**
	 * 设置动态属性
	 * 
	 * @param type 属性类型
	 * @param value 值
	 */
	abstract public float setDynamicAttr(int type,float value);
	/** 获得战斗者类型 */
	abstract public int getFighterType();
	/** 得到施放的技能 */
	public Ability getAbility()
	{
		return ability;
	}
	/** 得到下标 */
	public int getLocation()
	{
		return location;
	}
	/** 判断角色是否已经行动 */
	public boolean isActed()
	{
		return ability!=null;
	}
	/** 判断角色是否死亡 */
	abstract public boolean isDead();
	/** 判断角色是否是主动攻击型 */
	abstract public boolean isAttackFighter();
	/**
	 * 刷新光环技
	 * 
	 * @param fighter 所有fighter
	 */
	public void flushHalo(Fighter[] fighter)
	{
		abilityList.spreadHalo(fighter);
	}
	/**
	 * 刷新技能 </p> 刷新身上的每回合触发一次的效果,移除超时技能
	 * 
	 * @param time 当前的时间
	 * @param code 刷新点(例如:回合开始,行动开始,释放技能...)
	 */
	public void flushAbility(int time,ChangeListener listener)
	{
		clearClientAbility();
		//回合开始时触发某些技能
		abilityList.spreadTouchOffAbility(TouchOffSpread.ROUND_START,time,listener);
		abilityList.flushIntervalChangeEffect(time);
		abilityList.timeOutAbility(time);
	}
	/** 角色死亡，清理挂载的技能 */
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
	/** 序列化方法 */
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
	// /** 反序列化方法 */
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
	/** 深层复制 */
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