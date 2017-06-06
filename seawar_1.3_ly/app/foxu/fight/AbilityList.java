package foxu.fight;

import java.util.List;

import mustang.event.ChangeListener;
import mustang.set.ObjectArray;
import foxu.fight.FightScene.FightEvent;
import foxu.sea.fight.RoundActiveSpread;

/**
 * 玩家身上的技能列表 </p> 这个类处理技能之间的关系
 * 
 * @author ZYT
 */

public class AbilityList
{

	/* fields */
	/** 技能列表 */
	ObjectArray list;
	/** 挂载这些技能的fighter */
	Fighter self;

	/* dynamic fields */
	/** 缓存所有ability */
	private Ability[] cache;

	/* constructors */
	public AbilityList(Fighter self)
	{
		this.self=self;
		list=new ObjectArray();
	}

	/* methods */
	/** 添加一个技能 */
	public boolean add(Ability ability,int round,List<Ability> needClean)
	{
		Ability[] abilitys=getAllAbility();
		if(abilitys.length>0)
		{
			int[] resist=ability.getResist();
			for(int i=resist.length-1,j=0;i>=0;i--) // 遍历所有技能，查找是否有抵抗技能存在
			{
				for(j=abilitys.length-1;j>=0;j--)
				{
					if(abilitys[j]!=null&&abilitys[j].getCid()==resist[i])
					{
						if(abilitys[j].getLevel()>=ability.getLevel())// 比较等级,挂载的技能等级>新技能的等级,新技能被抵抗
						{
							ChangeListener listener=self.getChangeListener();
							if(listener!=null)
								listener
									.change(this,
										FightEvent.RESIST.ordinal(),self,
										ability);
							return false;
						}
					}
				}
			}
			int[] suppress=null;
			//遍历每个技能的压制列表,看是否有技能压制新技能
			for(int i=abilitys.length-1,j=0;i>=0;i--)
			{
				suppress=abilitys[i].getSuppress();
				for(j=suppress.length-1;j>=0;j--)
				{
					if(suppress[j]==ability.getCid()
						&&abilitys[i].getLevel()>=ability.getLevel())
					{
						ability.setEnabled(false);
					}
				}
			}
			ability.clean(abilitys,needClean);
			//如果新技能处于可用状态,再判设置新技能压制其他技能
			if(ability.isEnabled()) ability.suppress(abilitys);
			resetList(abilitys);
		}
		ability.setStartTime(round);
		list.add(ability);
		cache=null;
		return true;
	}
	/** 移除一个已挂载的技能 */
	public void remove(Ability ability)
	{
		list.remove(ability);
		cache=null;
	}
	/**
	 * 移除指定Fighter挂到此Fighter身上的技能
	 * 
	 * @param fighter 源Fighter
	 * @return 返回被移除的技能
	 */
	public void removeSourceAbility(Fighter fighter)
	{
		Ability[] abilitys=getAllAbility();
		ChangeListener listener=self.getChangeListener();
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i]==null) continue;
			if(abilitys[i].getSpread().getSpreadSource()==fighter.getUid())
			{
				if(listener!=null)
					listener.change(this,FightEvent.CLEAN_ABILITY.ordinal(),
						self,abilitys[i]);
				abilitys[i].disSuppress(abilitys);
				abilitys[i]=null;
				cache=null;
			}
		}
		if(cache==null) resetList(abilitys);
	}
	/**
	 * 移除指定fighter释放到这个角色身上的光环技能
	 * 
	 * @param fighter 释放光环的fighter
	 * @return 返回被移除的技能
	 */
	public void removePerennityAbility(Fighter fighter)
	{
		Ability[] abilitys=getAllAbility();
		ChangeListener listener=fighter.getChangeListener();
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i]==null) continue;
			if(abilitys[i].getType()==Ability.ETERNAL
				&&abilitys[i].getSpread().getSpreadSource()==fighter
					.getUid())
			{
				if(listener!=null)
					listener.change(this,FightEvent.CLEAN_ABILITY.ordinal(),
						self,abilitys[i]);
				abilitys[i].disSuppress(abilitys);
				abilitys[i]=null;
				cache=null;
			}
		}
		if(cache==null) resetList(abilitys);
	}
	/** 得到自己身上挂着的技能,中间会有空 */
	public Ability[] getAllAbility()
	{
		Ability[] temp=cache;
		if(temp==null)
		{
			temp=new Ability[list.size()];
			list.toArray(temp);
			cache=temp;
		}
		return temp;
	}
	/** 战斗开始时释放光环技 */
	public void spreadHalo(Fighter[] fighters)
	{
		Ability[] abilitys=getAllAbility();
		for(int i=abilitys.length-1,m=0;i>=0;i--)
		{
			if(abilitys[i]!=null&&abilitys[i].getType()==Ability.ETERNAL
				&&abilitys[i].getSpread().getSpreadSource()==self.getUid()
				&&abilitys[i].getSpread().getRange()==0)
			{
				for(m=fighters.length-1;m>=0;m--)
				{
					if(fighters[m]==self) continue;
					// 如果释放目标是己方
					if(abilitys[i].getSpread().getSpreadTeam()==1
						&&fighters[m].getTeam()==self.getTeam())
					{
						fighters[m].addAbility((Ability)abilitys[i].clone(),
							0);
					}
					else if(abilitys[i].getSpread().getSpreadTeam()==0
						&&fighters[m].getTeam()!=self.getTeam())
					{
						fighters[m].addAbility((Ability)abilitys[i].clone(),
							0);
					}
				}
			}
		}
	}
	/**
	 * 刷新身上的每回合触发一次的效果
	 * 
	 * @param time 现在的时间
	 * @param code 效果校验码
	 */
	public void flushIntervalChangeEffect(int time)
	{
		Ability[] abilitys=null;
		Effect[] effects=null;
		abilitys=getAllAbility();
		// boolean temp=false;
		// ChangeListener listener=self.getChangeListener();
		for(int j=abilitys.length-1,n=0;j>=0;j--)
		{
			if(abilitys[j]!=null&&abilitys[j].isEnabled())
			{
				// 计算间隔时间
				int interval=time-abilitys[j].getLastTime();
				// 配置为
				int intervalTime=abilitys[j].getIntervalTime()+1;
				// interval%=intervalTime;
				if(interval<intervalTime) continue;
				abilitys[j].setLastTime(time-(interval%intervalTime));
				effects=abilitys[j].getEffects();
				if(effects!=null&&effects.length>0)
				{
					for(n=effects.length-1;n>=0;n--)
					{
						if(effects[n].isEnable()
							&&effects[n] instanceof IntervalChangeEffect)
						{
							IntervalChangeEffect effect=(IntervalChangeEffect)effects[n];
							effect.changeAttr(self);
						}
					}
				}
			}
		}
	}
	/**
	 * 移除超时技能
	 * 
	 * @param time 当前的时间
	 */
	public void timeOutAbility(int time)
	{
		Ability[] abilitys=getAllAbility();
		ChangeListener listener=self.getChangeListener();
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i]!=null&&abilitys[i].timeOut(time))
			{
				if(listener==null) continue;
				listener.change(this,FightEvent.ABILITY_TIME_OUT.ordinal(),
					self,abilitys[i]);
				abilitys[i]=null;
				cache=null;
			}
		}
		if(cache==null) resetList(abilitys);
	}
	/**
	 * 技能列表复位
	 * 
	 * @param abilitys
	 */
	private void resetList(Object[] abilitys)
	{
		list.clear();
		if(abilitys!=null&&abilitys.length>0)
		{
			for(int i=abilitys.length-1;i>=0;i--)
			{
				if(abilitys[i]!=null) list.add(abilitys[i]);
			}
		}
		cache=null;
	}
	/** 角色死亡后清理工作 */
	public void deadClear()
	{
		Ability[] abilitys=getAllAbility();
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i]==null) continue;
			abilitys[i].setEnabled(true);
			if(abilitys[i].getType()==Ability.PASSIVE) continue;
			if(abilitys[i].getType()==Ability.ETERNAL
				&&abilitys[i].getSpread().getSpreadSource()==self.getUid())
				continue;
			abilitys[i]=null;
		}
		resetList(abilitys);
	}
	/** 战后清理 */
	public void overClear()
	{
		resetList(null);
	}
	/**
	 * 释放触发式技能
	 * 
	 * @param source 源
	 * @param time 触发时机
	 */
	public void spreadTouchOffAbility(int time,int cround,ChangeListener listener)
	{
		Ability[] abilityes=getAllAbility();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			if(abilityes[i].isEnabled()
				&&abilityes[i].getSpread() instanceof RoundActiveSpread)
			{
				RoundActiveSpread spread=(RoundActiveSpread)abilityes[i]
					.getSpread();
				spread.setCurrentTouchTime(time);
				spread.setCurrentRound(cround);
				int value=abilityes[i].checkUsed(self,self);
				if(value==0)
				{
					abilityes[i].used(self,self);
					// 抛出事件！！！！！！
					if(listener!=null)
					{
						listener.change(self,FightEvent.ACTIVE_STATE.ordinal(),abilityes[i]);
//test					listener.change(self,FightEvent.ADD_ABILITY.ordinal(),abilityes[i]);
					}
				}
			}
		}
	}
	
	/** 检测是否必然命中 */
	public boolean isMustHit()
	{
		Ability[] abilityes=getAllAbility();
		for(int i=abilityes.length-1;i>=0;i--)
		{
			if(abilityes[i].isEnabled()
				&&abilityes[i].getSpread() instanceof RoundActiveSpread)
			{
				RoundActiveSpread spread=(RoundActiveSpread)abilityes[i]
					.getSpread();
				if(spread.isMustHit()) return true;
			}
		}
		return false;
	}
	
	/**
	 * 移除一次性技能
	 */
	public void removeOnceAbility(){
		Ability[] abilitys=getAllAbility();
		//ChangeListener listener=self.getChangeListener();
		for(int i=abilitys.length-1;i>=0;i--)
		{
			if(abilitys[i]!=null&&abilitys[i].getUsefulTime()==-1)
			{
				/*if(listener==null) continue;
				listener.change(this,FightEvent.ABILITY_TIME_OUT.ordinal(),
					self,abilitys[i]);*/
				//System.out.println("一次性技能失效："+abilitys[i].getSid());
				abilitys[i]=null;
				cache=null;
			}
		}
		if(cache==null) resetList(abilitys);
	}
	
}