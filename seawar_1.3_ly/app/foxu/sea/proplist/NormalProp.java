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
 * 类说明：普通物品类，有数量属性
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class NormalProp extends Prop
{
	/* static fields */
	/** 物品的最大数量，超过此数量就将分成2个物品 */
	public static final int MAX_COUNT=65535;

	/** 物品可配置的最大数量 */
	public static final int LIMIT_MAX=65535;

	/* static methods */
	/** 获得数量 */
	public static NormalProp newNormalProp(int sid,int number)
	{
		NormalProp p=(NormalProp)factory.newSample(sid);
		number=number>LIMIT_MAX?LIMIT_MAX:number;
		p.count=number;
		return p;
	}
	/** 分离物品，参数number代表要分出的数量（必须比原始数量少），返回新的物品 */
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
	/** 当前数量 */
	int count=1;
	/** 物品的最大数量（允许配置） */
	int maxCount=MAX_COUNT;

	/** 保护样本的构造方法 */
	protected NormalProp()
	{
	}
	/* properties */
	/** 设置当前数量 */
	public void setCount(int count)
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		this.count=count;
	}
	/** 获得当前数量 */
	public int getCount()
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		return count;
	}
	/** 获得最大数量 */
	public int getMaxCount()
	{
		maxCount=maxCount>LIMIT_MAX?LIMIT_MAX:maxCount;
		return maxCount;
	}
	/** 设置最大数量 */
	public void setMaxCount(int maxCount)
	{
		maxCount=maxCount>LIMIT_MAX?LIMIT_MAX:maxCount;
		this.maxCount=maxCount;
	}
	/** 获得价格 */
	public int getPrice()
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		return price*count;
	}
	/** 获得简化版本的物品 */
	public Prop toShowProp()
	{
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		NormalProp np=newNormalProp(getSid(),this.count);
		return np;
	}
	/* methods */
	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		count=data.readUnsignedShort();
		count=count>LIMIT_MAX?LIMIT_MAX:count;
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
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
		// 增加物品
		player.getBundle().incrProp(this,true);
		// 事件通知任务
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.SHIPS_OR_PROP_PRODUCE_FINISH,this,player,null);
	}

	public void cancel(Player player,Product product)
	{
		float scroe=(product.getFinishTime()-TimeKit.getSecondTime())*1.0f
			/(getBuildTime()*product.getNum());
		if(product.getFinishTime()==0) scroe=1;
		// 等待建造
		if(scroe<0) return;
		if(scroe>1) scroe=1;
		// 返回资源
		int[] cost=getCostResources();
		/** 返回资源 消耗配置负数 价格负号 返回资源 */
		Resources.addResources(player.getResources(),
			(int)(cost[Resources.METAL]*scroe*product.getNum()),
			(int)(cost[Resources.OIL]*scroe*product.getNum()),
			(int)(cost[Resources.SILICON]*scroe*product.getNum()),
			(int)(cost[Resources.URANIUM]*scroe*product.getNum()),
			(int)(cost[Resources.MONEY]*scroe*product.getNum()),player);
		//返回道具
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