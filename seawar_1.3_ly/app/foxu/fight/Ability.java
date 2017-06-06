package foxu.fight;

import java.util.List;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * 类说明：技能类.各种常量定义尽量采用2的N次方方式定义. </p>
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */
public class Ability extends Sample
{

	/* static fields */
	/**
	 * 技能移除类型常量:REMOVE_DEAD=1死亡时不移除,REMOVE_FIGHT=2战斗结束时不移除,
	 * REMOVE_OFF_LINE=4下线时不移除;
	 */
	public static final int REMOVE_DEAD=1,REMOVE_FIGHT=2,REMOVE_OFF_LINE=4;
	/** 技能类型,ATTACK=1攻击技能,STATE=2普通状态技能,PASSIVE=4被动技能,ETERNAL=8光环技能 */
	public static final int ATTACK=1,STATE=2,PASSIVE=4,ETERNAL=8;

	/* static methods */
	/** 从字节数组中反序列化获得对象的域 */
	public static Ability bytesReadAbility(ByteBuffer data,
		SampleFactory factory)
	{
		int sid=data.readUnsignedShort();
		Ability s=(Ability)factory.newSample(sid);
		if(s==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,
				Ability.class.getName()+" bytesRead, invalid sid:"+sid);
		s.bytesRead(data);
		return s;
	}

	/* fields */
	/** 名称 */
	String name;
	/** 样式 */
	String style;
	/** 描述 */
	String description;
	/** 施放对象 */
	Spread spreads;
	/** 效果数组 */
	Effect[] effects;
	/** 动作类型 */
	int actionType;
	/** 技能ID */
	int cid;
	/** 压制其他技能列表 */
	int[] suppress={};
	/** 被抵抗技能列表 */
	int[] resist={};
	/** 驱散其他技能列表 */
	int[] clean={};
	/** 技能类型 攻击1,状态2,被动4,光环8 */
	int type;
	/** 技能等级 */
	int level;
	/** 技能起作用的时间：0表示时间无限 -1表示一次攻击 连击算2次 */
	int usefulTime;
	/** 是否必中 */
	boolean mustHit;
	/** 是否必爆 */
	boolean mustErupt;
	/** 技能能否被打断(有起作用次数的技能也被当做可打断技能处理) */
	boolean isBreak;
	/** 技能作用的间隔时间 */
	int intervalTime;
	/** 技能消耗方式 */
	Consumer consumer;
	// /** 是否需要序列化Spread */
	// private boolean needSpreadSerialization;
	// /** 是否需要序列化Effect */
	// private boolean needEffectSerialization;
	// /** 是否需要序列化Consumer */
	// private boolean needConsumerSerialization;
	/** 技能移除限制 */
	int removeLimited;

	/* dynamic fields */
	/** 技能是否作用 */
	boolean enabled=true;
	/** 技能的起始回合 */
	int startTime;
	/** 技能上一次起作用的时间 */
	int lastTime=-1000;

	/* properties */
	/** 获得技能上一次起作用的时间 */
	public int getLastTime()
	{
		return lastTime;
	}
	/** 设置技能上一次起作用的时间 */
	public void setLastTime(int lastTime)
	{
		this.lastTime=lastTime;
	}
	/** 得到技能起作用的间隔时间 */
	public int getIntervalTime()
	{
		if(intervalTime<0) return 0;
		return intervalTime;
	}
	/** 设置是否可用 */
	public void setEnabled(boolean enable)
	{
		enabled=enable;
	}
	/** 获得技能的动作类型 */
	public int getActionType()
	{
		return actionType;
	}
	/** 获得技能效果能否被打断 */
	public boolean isBreak()
	{
		return isBreak;
	}
	/** 获得技能起作用的时间 */
	public int getUsefulTime()
	{
		return usefulTime;
	}
	/**
	 * 设置技能起作用的时间
	 * 
	 * @param time 回合,秒为单位时间,帧数
	 */
	public void setUsefulTime(int time)
	{
		usefulTime=time;
	}
	/** 得到起始回合 */
	public int getStartTime()
	{
		return startTime;
	}
	/** 设置起始回合 */
	public void setStartTime(int start)
	{
		startTime=start;
		lastTime=start;
	}
	/** 获得压制列表 */
	public int[] getSuppress()
	{
		return suppress;
	}
	/** 获得驱散列表 */
	public int[] getClean()
	{
		return clean;
	}
	/** 获得抵抗列表 */
	public int[] getResist()
	{
		return resist;
	}
	/** 获得技能ID */
	public int getCid()
	{
		return cid;
	}
	/** 技能是否可用 */
	public boolean isEnabled()
	{
		return enabled;
	}
	/** 获得名称 */
	public String getName()
	{
		return name;
	}
	/** 获得样式 */
	public String getStyle()
	{
		return style;
	}
	/** 获得描述 */
	public String getDescription()
	{
		return description;
	}
	/** 得到施放描述对象 */
	public Spread getSpread()
	{
		return spreads;
	}
	/** 获得效果 */
	public Effect[] getEffects()
	{
		return effects;
	}
	/** 获得技能类型 */
	public int getType()
	{
		return type;
	}
	/** 设置技能等级 */
	public void setLevel(int level)
	{
		this.level=level;
	}
	/** 获得技能等级 */
	public int getLevel()
	{
		return level;
	}
	/** 获得技能是否必中目标 */
	public boolean isMustHit()
	{
		return mustHit;
	}
	/** 设置技能必须命中 */
	public void setMustHit(boolean mustHit)
	{
		 this.mustHit=mustHit;
	}
	/** 获得技能是否必爆 */
	public boolean isMustErupt()
	{
		return mustErupt;
	}
	/** 设置技能是否必爆 */
	public void setMustErupt(boolean mustErupt)
	{
		this.mustErupt=mustErupt;
	}
	/** 设置消耗 */
	public void setConsumer(Consumer consumer)
	{
		this.consumer=consumer;
	}
	/** 获得消耗 */
	public Consumer getConsumer()
	{
		return consumer;
	}

	/* methods */
//	/**
//	 * 检查移除限制
//	 * 
//	 * @param remove 移除限制类型
//	 * @return 返回true标示限制当前移除方式(死亡时不能移除,战斗结束时不能移除,下线不能移除)
//	 */
//	public boolean checkRemove(int remove)
//	{
//		return (removeLimited&remove)!=0;
//	}
	/**
	 * 从工厂中获得一个effect 如果需要序列化Effect.子类覆盖此方.
	 * 
	 * @param sid effect编号
	 * @return 返回这个effect
	 */
	public Effect newEffect(int sid)
	{
		return null;
	}
	/**
	 * 从工厂中获得一个Spread 如果需要序列化Spread.子类覆盖此方.
	 * 
	 * @param sid Spread编号
	 * @return 返回这个Spread
	 */
	public Spread newSpread(int sid)
	{
		return null;
	}
	/**
	 * 从工厂中获得一个Consumer 如果需要序列化Consumer.子类覆盖此方.
	 * 
	 * @param sid Consumer编号
	 * @return 返回这个Consumer
	 */
	public Consumer newConsumer(int sid)
	{
		return null;
	}
	/**
	 * 使用技能后消耗
	 * 
	 * @param fighter 使用技能的fighter
	 */
	public void consume(Fighter fighter)
	{
		if(consumer!=null) consumer.consume(fighter,this);
	}
	/** 等级增长 */
	public int riseLevel()
	{
		return ++level;
	}
	/** 检查源角色能否对目标角色使用该技能 */
	public int checkUsed(Fighter source,Object target)
	{
		return getSpread().checkUsed(source,target,this);
	}
	/** 源角色对目标角色使用该技能 */
	public boolean used(Fighter source,Object target)
	{
		return getSpread().used(source,target,this);
	}
	/**
	 * 超时判定
	 * 
	 * @param time 当前的时间
	 * @return 返回true表示超时
	 */
	public boolean timeOut(int time)
	{
//		if(checkRemove(REMOVE_FIGHT)) return false;
		int usefulTime=getUsefulTime();
		if(usefulTime==0||time-getStartTime()<usefulTime)
		{
			Effect[] effect=getEffects();
			if(effect!=null&&effect.length>0)
			{
				for(int i=effect.length-1;i>=0;i--)
				{
					effect[i].timeOut(time);
				}
			}
			return false;
		}
		return true;
	}
	/**
	 * 驱散技能列表里的技能
	 * 
	 * @param abilitys 技能列表
	 */
	public void clean(Ability[] abilitys,List<Ability> needClean)
	{
		int[] clean=getClean();
		int length=0;
		for(int i=clean.length-1,j=0;i>=0;i--)
		{
			for(j=abilitys.length-1;j>=0;j--)
			{
				if(abilitys[j]!=null)
				{
					if(abilitys[j].getCid()==clean[i])
					{
						if(abilitys[j].getCid()==cid)
						{
							if(abilitys[j].getLevel()>level) continue;
						}
						else
						{	
							needClean.add(abilitys[j]);
						}
						length++;
						abilitys[j].disSuppress(abilitys);
						abilitys[j]=abilitys[abilitys.length-length];
						abilitys[abilitys.length-length]=null;
					}
				}
			}
		}
	}
	/**
	 * 压制技能列表里的技能
	 * 
	 * @param abilitys 技能列表
	 */
	public void suppress(Ability[] abilitys)
	{
		int[] suppress=getSuppress();
		for(int i=suppress.length-1,j=0;i>=0;i--)
		{
			for(j=abilitys.length-1;j>=0;j--)
			{
				if(abilitys[j]!=null&&abilitys[j].getCid()==suppress[i])// 如果技能在压制列表里
				{
					if(level>=abilitys[j].getLevel())
					{
						abilitys[j].setEnabled(false);
					}
				}
			}
		}
	}
	/**
	 * 解除技能压制
	 * 
	 * @param abilitys 技能列表
	 */
	public void disSuppress(Ability[] abilitys)
	{
		int[] suppress=getSuppress();
		for(int i=suppress.length-1,j=0;i>=0;i--)
		{
			for(j=abilitys.length-1;j>=0;j--)
			{
				if(abilitys[j]!=null&&abilitys[j].getCid()==suppress[i])
				{
					abilitys[j].enabled=true;
				}
			}
		}
	}

	/** 复制方法（主要复制深层次的域变量，如对象、数组等） */
	public Object copy(Object obj)
	{
		Ability s=(Ability)super.copy(obj);
		if(spreads!=null)
		{
			s.spreads=(Spread)spreads.clone();
		}
		if(effects!=null)
		{
			s.effects=new Effect[effects.length];
			for(int i=0;i<effects.length;i++)
			{
				s.effects[i]=(Effect)effects[i].clone();
				s.effects[i].setAbility(s);
			}
		}
		if(consumer!=null) s.consumer=(Consumer)consumer.clone();
		return s;
	}

	/** 序列化方法 */
	public void bytesWrite(ByteBuffer bb)
	{
		super.bytesWrite(bb);
		bb.writeByte(level);
	}

	public Object bytesRead(ByteBuffer data)
	{
		level=data.readUnsignedByte();
		return this;
	}

	/**
	 * 读出一个spread
	 * 
	 * @param data
	 * @return 返回一个Spread
	 */
	public Spread bytesReadSpread(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Spread spread=newSpread(sid);
		if(spread==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,
				Spread.class.getName()+" bytesRead, invalid sid:"+sid);
		return spread;
	}
	/**
	 * 读出一个effect
	 * 
	 * @param data
	 * @return 返回一个effect
	 */
	public Effect bytesReadEffect(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Effect e=newEffect(sid);
		if(e==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,
				Effect.class.getName()+" bytesRead, invalid sid:"+sid);
		e.bytesRead(data);
		return e;
	}
	/**
	 * 读出一个Consumer
	 * 
	 * @param data
	 * @return 返回一个Consumer
	 */
	public Consumer bytesReadConsumer(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Consumer c=newConsumer(sid);
		if(c==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,
				Consumer.class.getName()+" bytesRead, invalid sid:"+sid);
		c.bytesRead(data);
		return c;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[name="+name+", style="+style+"] ";
	}
}