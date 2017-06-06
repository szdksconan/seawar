package foxu.fight;

import java.util.List;

import mustang.event.ChangeListener;
import mustang.set.ObjectArray;
import foxu.fight.FightScene.FightEvent;
import foxu.sea.fight.RoundActiveSpread;

/**
 * ������ϵļ����б� </p> ����ദ����֮��Ĺ�ϵ
 * 
 * @author ZYT
 */

public class AbilityList
{

	/* fields */
	/** �����б� */
	ObjectArray list;
	/** ������Щ���ܵ�fighter */
	Fighter self;

	/* dynamic fields */
	/** ��������ability */
	private Ability[] cache;

	/* constructors */
	public AbilityList(Fighter self)
	{
		this.self=self;
		list=new ObjectArray();
	}

	/* methods */
	/** ���һ������ */
	public boolean add(Ability ability,int round,List<Ability> needClean)
	{
		Ability[] abilitys=getAllAbility();
		if(abilitys.length>0)
		{
			int[] resist=ability.getResist();
			for(int i=resist.length-1,j=0;i>=0;i--) // �������м��ܣ������Ƿ��еֿ����ܴ���
			{
				for(j=abilitys.length-1;j>=0;j--)
				{
					if(abilitys[j]!=null&&abilitys[j].getCid()==resist[i])
					{
						if(abilitys[j].getLevel()>=ability.getLevel())// �Ƚϵȼ�,���صļ��ܵȼ�>�¼��ܵĵȼ�,�¼��ܱ��ֿ�
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
			//����ÿ�����ܵ�ѹ���б�,���Ƿ��м���ѹ���¼���
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
			//����¼��ܴ��ڿ���״̬,���������¼���ѹ����������
			if(ability.isEnabled()) ability.suppress(abilitys);
			resetList(abilitys);
		}
		ability.setStartTime(round);
		list.add(ability);
		cache=null;
		return true;
	}
	/** �Ƴ�һ���ѹ��صļ��� */
	public void remove(Ability ability)
	{
		list.remove(ability);
		cache=null;
	}
	/**
	 * �Ƴ�ָ��Fighter�ҵ���Fighter���ϵļ���
	 * 
	 * @param fighter ԴFighter
	 * @return ���ر��Ƴ��ļ���
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
	 * �Ƴ�ָ��fighter�ͷŵ������ɫ���ϵĹ⻷����
	 * 
	 * @param fighter �ͷŹ⻷��fighter
	 * @return ���ر��Ƴ��ļ���
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
	/** �õ��Լ����Ϲ��ŵļ���,�м���п� */
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
	/** ս����ʼʱ�ͷŹ⻷�� */
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
					// ����ͷ�Ŀ���Ǽ���
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
	 * ˢ�����ϵ�ÿ�غϴ���һ�ε�Ч��
	 * 
	 * @param time ���ڵ�ʱ��
	 * @param code Ч��У����
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
				// ������ʱ��
				int interval=time-abilitys[j].getLastTime();
				// ����Ϊ
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
	 * �Ƴ���ʱ����
	 * 
	 * @param time ��ǰ��ʱ��
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
	 * �����б�λ
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
	/** ��ɫ������������ */
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
	/** ս������ */
	public void overClear()
	{
		resetList(null);
	}
	/**
	 * �ͷŴ���ʽ����
	 * 
	 * @param source Դ
	 * @param time ����ʱ��
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
					// �׳��¼�������������
					if(listener!=null)
					{
						listener.change(self,FightEvent.ACTIVE_STATE.ordinal(),abilityes[i]);
//test					listener.change(self,FightEvent.ADD_ABILITY.ordinal(),abilityes[i]);
					}
				}
			}
		}
	}
	
	/** ����Ƿ��Ȼ���� */
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
	 * �Ƴ�һ���Լ���
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
				//System.out.println("һ���Լ���ʧЧ��"+abilitys[i].getSid());
				abilitys[i]=null;
				cache=null;
			}
		}
		if(cache==null) resetList(abilitys);
	}
	
}