package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.io.BytesReader;
import mustang.io.BytesWritable;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**
 * ���������� author��icetiger
 */
public abstract class Produce implements BytesWritable,BytesReader,Cloneable
{

	/**
	 * һ��ʱ���Զ�������Դ���������� produceTimeʱ�� checkTime�������ʱ�� ����ս���¼���ʱ����Ҫ
	 */
	/**
	 * @param player
	 * @param buildLevel
	 * @param checkTime
	 * @return �������,����ʣ��ʱ��
	 */
	public abstract int produce(Player player,int buildLevel,int checkTime,
		CreatObjectFactory objectFactory);
	/** ��ʼ������ */
	public abstract void init(int checkTime);
	/** �鿴�Ƿ������һ���������� */
	public abstract boolean checkProduce(Player player);
	public abstract void addProduct(Player player,Product p);
	public abstract void showBytesWrite(ByteBuffer data,int current);
	/** ������������ */
	int buildSid;
	/** ��������λ��,��ʱ���� */
	int buildIndex;
	public Object clone()
	{
		try
		{
			return copy(super.clone());
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException(getClass().getName()+" clone, "+e);
		}
	}

	public Object copy(Object obj)
	{
		return obj;
	}
	
	public int getBuildSid()
	{
		return buildSid;
	}
	
	/** ��������λ��,��ʱ���� */
	public int getBuildIndex()
	{
		return buildIndex;
	}
	
	/** ��������λ��,��ʱ���� */
	public void setBuildIndex(int buildIndex)
	{
		this.buildIndex=buildIndex;
	}
}
