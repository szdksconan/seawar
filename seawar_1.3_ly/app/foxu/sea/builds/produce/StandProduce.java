package foxu.sea.builds.produce;

import mustang.io.ByteBuffer;
import mustang.set.ObjectArray;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.Produce;
import foxu.sea.builds.Produceable;
import foxu.sea.builds.Product;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.task.TaskEventExecute;

/**
 * 标准建造
 * 
 * @author icetiger
 */
public class StandProduce extends Produce
{

	/** 误差时间5秒 */
	public static final int SEC_5=0;
	/* Serialization fileds */
	/** 生产队列.第一个元素为当前元素,后边都为备用队列 */
	ObjectArray productes;

	/* fields */
	SampleFactory factory;

	public void init(int checkTime)
	{

	}

	private void next()
	{
		if(productes.size()>0)
		{
			Product p=(Product)productes.getArray()[0];
			productes.remove(p);// 移除第一个元素
		}
	}

	/** 查看队列里面是否有sid相同的产品 */
	public boolean checkProduceSid(Player player,int sid)
	{
		if(productes==null) return false;
		Object[] object=productes.getArray();
		for(int i=0;i<object.length;i++)
		{
			Product pp=(Product)object[i];
			if(pp.getSid()==sid) return true;
		}
		return false;
	}

	/**
	 * 添加一个待产品
	 * 
	 * @param p 待产品
	 * @param finish 完成时间点
	 */
	public void addProduct(Player player,Product p)
	{
		if(p==null) return;
		if(productes==null) productes=new ObjectArray();
		if(productes.size()<=0)
			p.setFinishTime(TimeKit.getSecondTime()+p.getProduceTime());
		productes.add(p);
	}
	// /** 获取第一个队列的完成时间 */
	// public int getFristFinishTime()
	// {
	// if(productes==null||productes.size()<=0)
	// return TimeKit.getSecondTime();
	// Product p=(Product)productes.getArray()[0];
	// return p.getFinlishTime();
	// }

	/** produceTime 生产完成时间点 */
	public int produce(Player player,int buildLevel,int checkTime,
		CreatObjectFactory objectFactory)
	{
		/** 时间未到 */
		if(productes==null||productes.size()<=0) return Integer.MAX_VALUE;
		Product p=(Product)productes.getArray()[0];
		if(p.getFinishTime()==0)
		{
			p.setFinishTime(TimeKit.getSecondTime()+p.getProduceTime());
			return Integer.MAX_VALUE;
		}
		if((checkTime+SEC_5)<p.getFinishTime())
			return p.getFinishTime()-checkTime;
		Produceable pa=(Produceable)factory.newSample(p.sid);
		try
		{
			pa.finish(player,p,objectFactory);

			// 是否需要记录日志
			if(SeaBackKit.isContainValue(PublicConst.PRODUCE_LOG_SIDS,
				getBuildSid()))
			{
				// 生产日志-完成
				int id=SeaBackKit.getProductId(player,getBuildIndex(),0);
				objectFactory.createProducePropTrack(
					ProducePropTrack.COMPLETE,player.getId(),p,
					getBuildIndex(),getBuildSid(),buildLevel,id);
				SeaBackKit.removeProductId(player,getBuildIndex(),0);
			}
			// 累计事件通知任务
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.PRODUCE_COUNT_TASK_EVENT,p,player,getBuildSid());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			next();
		}
		if(productes.size()>0)
		{
			// 设置下一个的完成时间
			Product next=(Product)productes.getArray()[0];
			next.setFinishTime(p.getFinishTime()+next.getProduceTime());
			produce(player,buildLevel,checkTime,objectFactory);
			// 是否需要记录日志
			if(SeaBackKit.isContainValue(PublicConst.PRODUCE_LOG_SIDS,
				getBuildSid()))
			{
				// 等待事件开始日志
				int id=SeaBackKit.getProductId(player,getBuildIndex(),0);
				objectFactory.createProducePropTrack(ProducePropTrack.START,
					player.getId(),next,getBuildIndex(),getBuildSid(),
					buildLevel,id);
			}
		}
		return 0;
	}

	/** 获取当前的生产队列 */
	public Product getNowProduce()
	{
		if(productes==null||productes.size()<=0) return null;
		return (Product)productes.getArray()[0];
	}

	/** 检查是否能取消这个index的生产队列 */
	public boolean checkCancelProduce(int index)
	{
		if(productes==null||productes.size()<=0) return false;
		if(productes.size()<=index) return false;
		return true;
	}

	/**
	 * index=取消的队列数组下标
	 */
	public void cancelProduce(Player player,int index,
		CreatObjectFactory objectFactory)
	{
		if(productes==null||productes.size()<=0) return;
		if(productes.size()<=index) return;
		Product p=(Product)productes.getArray()[index];
		Produceable pa=(Produceable)factory.newSample(p.sid);
		try
		{
			pa.cancel(player,p);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			productes.remove(p);
		}
		// 重新计算完成时间
		if(productes.size()>0)
		{
			Object left[]=productes.getArray();
			// index=0的情况
			if(index==0)
			{
				p=(Product)left[0];
				p.setFinishTime(TimeKit.getSecondTime()+p.getProduceTime());
			}
		}
	}

	/** 检查是否可以生产 */
	public boolean checkProduce(Player player)
	{
		if(productes==null||productes.size()<=0) return true;
		// VIP对应相应的等待建筑队列
		int vipDeque=PublicConst.VIP_LEVEL_FOR_DEQUE[player.getUser_state()];
		return vipDeque>productes.size();
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadProductes(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		Product p;
		for(int i=0;i<n;i++)
		{
			p=new Product();
			p.bytesRead(data);
			temp[i]=p;
		}
		productes=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteProductes(ByteBuffer data)
	{
		if(productes!=null&&productes.size()>0)
		{
			Object[] array=productes.getArray();
			data.writeByte(array.length);
			Product p;
			for(int i=0;i<array.length;i++)
			{
				p=(Product)array[i];
				p.bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}
	public Object bytesRead(ByteBuffer data)
	{
		bytesReadProductes(data);
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		bytesWriteProductes(data);
	}
	/** 将对象的域序列化到字节缓存中 */
	public void showBytesWrite(ByteBuffer data,int current)
	{
		if(productes!=null&&productes.size()>0)
		{
			Object[] array=productes.getArray();
			data.writeByte(array.length);
			Product p;
			for(int i=0;i<array.length;i++)
			{
				p=(Product)array[i];
				p.showBytesWrite(data,current);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/**
	 * @return productes
	 */
	public ObjectArray getProductes()
	{
		return productes;
	}

	/**
	 * @param productes 要设置的 productes
	 */
	public void setProductes(ObjectArray productes)
	{
		this.productes=productes;
	}

	public Object copy(Object obj)
	{
		StandProduce p=(StandProduce)super.copy(obj);
		if(productes!=null)
		{
			Object[] products=productes.toArray();
			for(int i=products.length-1;i>=0;i--)
			{
				Product product=(Product)products[i];
				products[i]=product.clone();
			}
			p.productes=new ObjectArray(products);
		}
		return p;
	}
}
