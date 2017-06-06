package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.io.BytesReader;
import mustang.io.BytesWritable;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**
 * 建筑功能类 author：icetiger
 */
public abstract class Produce implements BytesWritable,BytesReader,Cloneable
{

	/**
	 * 一定时间自动生产资源或是生产船 produceTime时间 checkTime传入检查的时间 计算战斗事件的时候需要
	 */
	/**
	 * @param player
	 * @param buildLevel
	 * @param checkTime
	 * @return 如果出错,返回剩余时间
	 */
	public abstract int produce(Player player,int buildLevel,int checkTime,
		CreatObjectFactory objectFactory);
	/** 初始化数据 */
	public abstract void init(int checkTime);
	/** 查看是否能添加一个生产队列 */
	public abstract boolean checkProduce(Player player);
	public abstract void addProduct(Player player,Product p);
	public abstract void showBytesWrite(ByteBuffer data,int current);
	/** 所属建筑类型 */
	int buildSid;
	/** 所属建筑位置,临时变量 */
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
	
	/** 所属建筑位置,临时变量 */
	public int getBuildIndex()
	{
		return buildIndex;
	}
	
	/** 所属建筑位置,临时变量 */
	public void setBuildIndex(int buildIndex)
	{
		this.buildIndex=buildIndex;
	}
}
