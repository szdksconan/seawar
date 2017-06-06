package foxu.sea.uid;

import mustang.util.UidFile;

/**uid管理器*/
public class UidKit
{
	/* static fields */
	/** 数量常量 */
	public static final int COUNT=1000;

	/* fields */
	/** 每次获取唯一编号的数量 */
	int count=COUNT;
	/***/
	UidFile uidFile;

	/** 正数唯一编号 */
	private int plusUid1;
	/** 正数唯一编号的最小值 */
	private int plusUid2;
	/** 负数唯一编号 */
	private int minusUid1;
	/** 负数唯一编号的最小值 */
	private int minusUid2;
	/** 正数唯一编号同步对象 */
	private Object plusLock=new Object();
	/** 负数唯一编号同步对象 */
	private Object minusLock=new Object();

	/* properties */
	/** 获得连接提供器 */
	public int getCount()
	{
		return count;
	}
	/** 设置每次获取唯一编号的数量 */
	public void setCount(int count)
	{
		this.count=count;
	}
	/* methods */
	/** 获得唯一的正数编号 */
	public int getPlusUid()
	{
		synchronized(plusLock)
		{
			if(plusUid1>=plusUid2)
			{
				plusUid2=uidFile.getPlusUid(count);
				plusUid1=plusUid2-count;
			}
			return ++plusUid1;
		}
	}
	/** 获得唯一的负数编号 */
	public int getMinusUid()
	{
		synchronized(minusLock)
		{
			if(minusUid1<=minusUid2)
			{
				minusUid2=uidFile.getMinusUid(count);
				minusUid1=minusUid2+count;
			}
			return --minusUid1;
		}
	}
	
	/**
	 * @return uidFile
	 */
	public UidFile getUidFile()
	{
		return uidFile;
	}
	
	/**
	 * @param uidFile 要设置的 uidFile
	 */
	public void setUidFile(UidFile uidFile)
	{
		this.uidFile=uidFile;
	}
}
