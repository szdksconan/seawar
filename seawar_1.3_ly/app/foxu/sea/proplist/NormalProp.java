package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.builds.Product;
import foxu.sea.task.TaskEventExecute;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

/**
 * ��˵������ͨ��Ʒ�࣬����������
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class NormalProp extends Prop
{
	/* static fields */
	/** ��Ʒ����������������������ͽ��ֳ�2����Ʒ */
	public static final int MAX_COUNT=65535;

	/** ��Ʒ�����õ�������� */
	public static final int LIMIT_MAX=65535;

	/* static methods */
	/** ������� */
	public static NormalProp newNormalProp(int sid,int number)
	{
		NormalProp p=(NormalProp)factory.newSample(sid);
		number=number>LIMIT_MAX?LIMIT_MAX:number;
		p.count=number;
		return p;
	}
	/** ������Ʒ������number����Ҫ�ֳ��������������ԭʼ�����٣��������µ���Ʒ */
	public static NormalProp splitNormalProp(NormalProp np,int number)
	{
		if(number<=0) return null;
		if(np.count<=number) return null;
		np.count-=number;
		NormalProp p=(NormalProp)factory.newSample(np.getSid());
		p.count=number;
		p.count=p.count>LIMIT_MAX?LIMIT_MAX:p.count;
		return p;
	}

	/* fields */
	/** ��ǰ���� */
	int count=1;
	/** ��Ʒ������������������ã� */
	int maxCount=MAX_COUNT;

	/** ���������Ĺ��췽�� */
	protected NormalProp()
	{
	}
	/* properties */
	/** ���õ�ǰ���� */
	public void setCount(int count)
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		this.count=count;
	}
	/** ��õ�ǰ���� */
	public int getCount()
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		return count;
	}
	/** ���������� */
	public int getMaxCount()
	{
		maxCount=maxCount>LIMIT_MAX?LIMIT_MAX:maxCount;
		return maxCount;
	}
	/** ����������� */
	public void setMaxCount(int maxCount)
	{
		maxCount=maxCount>LIMIT_MAX?LIMIT_MAX:maxCount;
		this.maxCount=maxCount;
	}
	/** ��ü۸� */
	public int getPrice()
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		return price*count;
	}
	/** ��ü򻯰汾����Ʒ */
	public Prop toShowProp()
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		NormalProp np=newNormalProp(getSid(),this.count);
		return np;
	}
	/* methods */
	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		count=data.readUnsignedShort();
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		super.bytesWrite(data);
		data.writeShort(count);
	}
	/* common methods */
	public String toString()
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		return super.toString()+",count="+count;
	}

	public void finish(Player player,Product product,CreatObjectFactory objectFactory)
	{
		setCount(product.getNum());
		// ������Ʒ
		player.getBundle().incrProp(this,true);
		// �¼�֪ͨ����
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.SHIPS_OR_PROP_PRODUCE_FINISH,this,player,null);
	}

	public void cancel(Player player,Product product)
	{
		float scroe=(product.getFinishTime()-TimeKit.getSecondTime())*1.0f
			/(getBuildTime()*product.getNum());
		if(product.getFinishTime()==0) scroe=1;
		// �ȴ�����
		if(scroe<0) return;
		if(scroe>1) scroe=1;
		// ������Դ
		int[] cost=getCostResources();
		/** ������Դ �������ø��� �۸񸺺� ������Դ */
		Resources.addResources(player.getResources(),
			(int)(cost[Resources.METAL]*scroe*product.getNum()),
			(int)(cost[Resources.OIL]*scroe*product.getNum()),
			(int)(cost[Resources.SILICON]*scroe*product.getNum()),
			(int)(cost[Resources.URANIUM]*scroe*product.getNum()),
			(int)(cost[Resources.MONEY]*scroe*product.getNum()),player);
		//���ص���
		int costPropSid[] = getCostPropSid();
		if(costPropSid!=null&&costPropSid.length>0)
		{
			for(int i=0;i<costPropSid.length;i+=2)
			{
				NormalProp prop=(NormalProp)Prop.factory.newSample(costPropSid[i]);
				prop.setCount(costPropSid[i+1]*product.getNum());
				player.getBundle().incrProp(prop,true);
			}
		}
	}

}