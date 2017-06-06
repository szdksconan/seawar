package foxu.fight;

import java.util.List;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * ��˵����������.���ֳ������御������2��N�η���ʽ����. </p>
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */
public class Ability extends Sample
{

	/* static fields */
	/**
	 * �����Ƴ����ͳ���:REMOVE_DEAD=1����ʱ���Ƴ�,REMOVE_FIGHT=2ս������ʱ���Ƴ�,
	 * REMOVE_OFF_LINE=4����ʱ���Ƴ�;
	 */
	public static final int REMOVE_DEAD=1,REMOVE_FIGHT=2,REMOVE_OFF_LINE=4;
	/** ��������,ATTACK=1��������,STATE=2��ͨ״̬����,PASSIVE=4��������,ETERNAL=8�⻷���� */
	public static final int ATTACK=1,STATE=2,PASSIVE=4,ETERNAL=8;

	/* static methods */
	/** ���ֽ������з����л���ö������ */
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
	/** ���� */
	String name;
	/** ��ʽ */
	String style;
	/** ���� */
	String description;
	/** ʩ�Ŷ��� */
	Spread spreads;
	/** Ч������ */
	Effect[] effects;
	/** �������� */
	int actionType;
	/** ����ID */
	int cid;
	/** ѹ�����������б� */
	int[] suppress={};
	/** ���ֿ������б� */
	int[] resist={};
	/** ��ɢ���������б� */
	int[] clean={};
	/** �������� ����1,״̬2,����4,�⻷8 */
	int type;
	/** ���ܵȼ� */
	int level;
	/** ���������õ�ʱ�䣺0��ʾʱ������ -1��ʾһ�ι��� ������2�� */
	int usefulTime;
	/** �Ƿ���� */
	boolean mustHit;
	/** �Ƿ�ر� */
	boolean mustErupt;
	/** �����ܷ񱻴��(�������ô����ļ���Ҳ�������ɴ�ϼ��ܴ���) */
	boolean isBreak;
	/** �������õļ��ʱ�� */
	int intervalTime;
	/** �������ķ�ʽ */
	Consumer consumer;
	// /** �Ƿ���Ҫ���л�Spread */
	// private boolean needSpreadSerialization;
	// /** �Ƿ���Ҫ���л�Effect */
	// private boolean needEffectSerialization;
	// /** �Ƿ���Ҫ���л�Consumer */
	// private boolean needConsumerSerialization;
	/** �����Ƴ����� */
	int removeLimited;

	/* dynamic fields */
	/** �����Ƿ����� */
	boolean enabled=true;
	/** ���ܵ���ʼ�غ� */
	int startTime;
	/** ������һ�������õ�ʱ�� */
	int lastTime=-1000;

	/* properties */
	/** ��ü�����һ�������õ�ʱ�� */
	public int getLastTime()
	{
		return lastTime;
	}
	/** ���ü�����һ�������õ�ʱ�� */
	public void setLastTime(int lastTime)
	{
		this.lastTime=lastTime;
	}
	/** �õ����������õļ��ʱ�� */
	public int getIntervalTime()
	{
		if(intervalTime<0) return 0;
		return intervalTime;
	}
	/** �����Ƿ���� */
	public void setEnabled(boolean enable)
	{
		enabled=enable;
	}
	/** ��ü��ܵĶ������� */
	public int getActionType()
	{
		return actionType;
	}
	/** ��ü���Ч���ܷ񱻴�� */
	public boolean isBreak()
	{
		return isBreak;
	}
	/** ��ü��������õ�ʱ�� */
	public int getUsefulTime()
	{
		return usefulTime;
	}
	/**
	 * ���ü��������õ�ʱ��
	 * 
	 * @param time �غ�,��Ϊ��λʱ��,֡��
	 */
	public void setUsefulTime(int time)
	{
		usefulTime=time;
	}
	/** �õ���ʼ�غ� */
	public int getStartTime()
	{
		return startTime;
	}
	/** ������ʼ�غ� */
	public void setStartTime(int start)
	{
		startTime=start;
		lastTime=start;
	}
	/** ���ѹ���б� */
	public int[] getSuppress()
	{
		return suppress;
	}
	/** �����ɢ�б� */
	public int[] getClean()
	{
		return clean;
	}
	/** ��õֿ��б� */
	public int[] getResist()
	{
		return resist;
	}
	/** ��ü���ID */
	public int getCid()
	{
		return cid;
	}
	/** �����Ƿ���� */
	public boolean isEnabled()
	{
		return enabled;
	}
	/** ������� */
	public String getName()
	{
		return name;
	}
	/** �����ʽ */
	public String getStyle()
	{
		return style;
	}
	/** ������� */
	public String getDescription()
	{
		return description;
	}
	/** �õ�ʩ���������� */
	public Spread getSpread()
	{
		return spreads;
	}
	/** ���Ч�� */
	public Effect[] getEffects()
	{
		return effects;
	}
	/** ��ü������� */
	public int getType()
	{
		return type;
	}
	/** ���ü��ܵȼ� */
	public void setLevel(int level)
	{
		this.level=level;
	}
	/** ��ü��ܵȼ� */
	public int getLevel()
	{
		return level;
	}
	/** ��ü����Ƿ����Ŀ�� */
	public boolean isMustHit()
	{
		return mustHit;
	}
	/** ���ü��ܱ������� */
	public void setMustHit(boolean mustHit)
	{
		 this.mustHit=mustHit;
	}
	/** ��ü����Ƿ�ر� */
	public boolean isMustErupt()
	{
		return mustErupt;
	}
	/** ���ü����Ƿ�ر� */
	public void setMustErupt(boolean mustErupt)
	{
		this.mustErupt=mustErupt;
	}
	/** �������� */
	public void setConsumer(Consumer consumer)
	{
		this.consumer=consumer;
	}
	/** ������� */
	public Consumer getConsumer()
	{
		return consumer;
	}

	/* methods */
//	/**
//	 * ����Ƴ�����
//	 * 
//	 * @param remove �Ƴ���������
//	 * @return ����true��ʾ���Ƶ�ǰ�Ƴ���ʽ(����ʱ�����Ƴ�,ս������ʱ�����Ƴ�,���߲����Ƴ�)
//	 */
//	public boolean checkRemove(int remove)
//	{
//		return (removeLimited&remove)!=0;
//	}
	/**
	 * �ӹ����л��һ��effect �����Ҫ���л�Effect.���า�Ǵ˷�.
	 * 
	 * @param sid effect���
	 * @return �������effect
	 */
	public Effect newEffect(int sid)
	{
		return null;
	}
	/**
	 * �ӹ����л��һ��Spread �����Ҫ���л�Spread.���า�Ǵ˷�.
	 * 
	 * @param sid Spread���
	 * @return �������Spread
	 */
	public Spread newSpread(int sid)
	{
		return null;
	}
	/**
	 * �ӹ����л��һ��Consumer �����Ҫ���л�Consumer.���า�Ǵ˷�.
	 * 
	 * @param sid Consumer���
	 * @return �������Consumer
	 */
	public Consumer newConsumer(int sid)
	{
		return null;
	}
	/**
	 * ʹ�ü��ܺ�����
	 * 
	 * @param fighter ʹ�ü��ܵ�fighter
	 */
	public void consume(Fighter fighter)
	{
		if(consumer!=null) consumer.consume(fighter,this);
	}
	/** �ȼ����� */
	public int riseLevel()
	{
		return ++level;
	}
	/** ���Դ��ɫ�ܷ��Ŀ���ɫʹ�øü��� */
	public int checkUsed(Fighter source,Object target)
	{
		return getSpread().checkUsed(source,target,this);
	}
	/** Դ��ɫ��Ŀ���ɫʹ�øü��� */
	public boolean used(Fighter source,Object target)
	{
		return getSpread().used(source,target,this);
	}
	/**
	 * ��ʱ�ж�
	 * 
	 * @param time ��ǰ��ʱ��
	 * @return ����true��ʾ��ʱ
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
	 * ��ɢ�����б���ļ���
	 * 
	 * @param abilitys �����б�
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
	 * ѹ�Ƽ����б���ļ���
	 * 
	 * @param abilitys �����б�
	 */
	public void suppress(Ability[] abilitys)
	{
		int[] suppress=getSuppress();
		for(int i=suppress.length-1,j=0;i>=0;i--)
		{
			for(j=abilitys.length-1;j>=0;j--)
			{
				if(abilitys[j]!=null&&abilitys[j].getCid()==suppress[i])// ���������ѹ���б���
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
	 * �������ѹ��
	 * 
	 * @param abilitys �����б�
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

	/** ���Ʒ�������Ҫ�������ε�����������������ȣ� */
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

	/** ���л����� */
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
	 * ����һ��spread
	 * 
	 * @param data
	 * @return ����һ��Spread
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
	 * ����һ��effect
	 * 
	 * @param data
	 * @return ����һ��effect
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
	 * ����һ��Consumer
	 * 
	 * @param data
	 * @return ����һ��Consumer
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