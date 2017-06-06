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
 * ��׼����
 * 
 * @author icetiger
 */
public class StandProduce extends Produce
{

	/** ���ʱ��5�� */
	public static final int SEC_5=0;
	/* Serialization fileds */
	/** ��������.��һ��Ԫ��Ϊ��ǰԪ��,��߶�Ϊ���ö��� */
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
			productes.remove(p);// �Ƴ���һ��Ԫ��
		}
	}

	/** �鿴���������Ƿ���sid��ͬ�Ĳ�Ʒ */
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
	 * ���һ������Ʒ
	 * 
	 * @param p ����Ʒ
	 * @param finish ���ʱ���
	 */
	public void addProduct(Player player,Product p)
	{
		if(p==null) return;
		if(productes==null) productes=new ObjectArray();
		if(productes.size()<=0)
			p.setFinishTime(TimeKit.getSecondTime()+p.getProduceTime());
		productes.add(p);
	}
	// /** ��ȡ��һ�����е����ʱ�� */
	// public int getFristFinishTime()
	// {
	// if(productes==null||productes.size()<=0)
	// return TimeKit.getSecondTime();
	// Product p=(Product)productes.getArray()[0];
	// return p.getFinlishTime();
	// }

	/** produceTime �������ʱ��� */
	public int produce(Player player,int buildLevel,int checkTime,
		CreatObjectFactory objectFactory)
	{
		/** ʱ��δ�� */
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

			// �Ƿ���Ҫ��¼��־
			if(SeaBackKit.isContainValue(PublicConst.PRODUCE_LOG_SIDS,
				getBuildSid()))
			{
				// ������־-���
				int id=SeaBackKit.getProductId(player,getBuildIndex(),0);
				objectFactory.createProducePropTrack(
					ProducePropTrack.COMPLETE,player.getId(),p,
					getBuildIndex(),getBuildSid(),buildLevel,id);
				SeaBackKit.removeProductId(player,getBuildIndex(),0);
			}
			// �ۼ��¼�֪ͨ����
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
			// ������һ�������ʱ��
			Product next=(Product)productes.getArray()[0];
			next.setFinishTime(p.getFinishTime()+next.getProduceTime());
			produce(player,buildLevel,checkTime,objectFactory);
			// �Ƿ���Ҫ��¼��־
			if(SeaBackKit.isContainValue(PublicConst.PRODUCE_LOG_SIDS,
				getBuildSid()))
			{
				// �ȴ��¼���ʼ��־
				int id=SeaBackKit.getProductId(player,getBuildIndex(),0);
				objectFactory.createProducePropTrack(ProducePropTrack.START,
					player.getId(),next,getBuildIndex(),getBuildSid(),
					buildLevel,id);
			}
		}
		return 0;
	}

	/** ��ȡ��ǰ���������� */
	public Product getNowProduce()
	{
		if(productes==null||productes.size()<=0) return null;
		return (Product)productes.getArray()[0];
	}

	/** ����Ƿ���ȡ�����index���������� */
	public boolean checkCancelProduce(int index)
	{
		if(productes==null||productes.size()<=0) return false;
		if(productes.size()<=index) return false;
		return true;
	}

	/**
	 * index=ȡ���Ķ��������±�
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
		// ���¼������ʱ��
		if(productes.size()>0)
		{
			Object left[]=productes.getArray();
			// index=0�����
			if(index==0)
			{
				p=(Product)left[0];
				p.setFinishTime(TimeKit.getSecondTime()+p.getProduceTime());
			}
		}
	}

	/** ����Ƿ�������� */
	public boolean checkProduce(Player player)
	{
		if(productes==null||productes.size()<=0) return true;
		// VIP��Ӧ��Ӧ�ĵȴ���������
		int vipDeque=PublicConst.VIP_LEVEL_FOR_DEQUE[player.getUser_state()];
		return vipDeque>productes.size();
	}

	/** ���ֽ������з����л���ö������ */
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

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		bytesWriteProductes(data);
	}
	/** ������������л����ֽڻ����� */
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
	 * @param productes Ҫ���õ� productes
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
