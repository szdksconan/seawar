package foxu.sea.uid;

import mustang.util.UidFile;

/**uid������*/
public class UidKit
{
	/* static fields */
	/** �������� */
	public static final int COUNT=1000;

	/* fields */
	/** ÿ�λ�ȡΨһ��ŵ����� */
	int count=COUNT;
	/***/
	UidFile uidFile;

	/** ����Ψһ��� */
	private int plusUid1;
	/** ����Ψһ��ŵ���Сֵ */
	private int plusUid2;
	/** ����Ψһ��� */
	private int minusUid1;
	/** ����Ψһ��ŵ���Сֵ */
	private int minusUid2;
	/** ����Ψһ���ͬ������ */
	private Object plusLock=new Object();
	/** ����Ψһ���ͬ������ */
	private Object minusLock=new Object();

	/* properties */
	/** ��������ṩ�� */
	public int getCount()
	{
		return count;
	}
	/** ����ÿ�λ�ȡΨһ��ŵ����� */
	public void setCount(int count)
	{
		this.count=count;
	}
	/* methods */
	/** ���Ψһ��������� */
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
	/** ���Ψһ�ĸ������ */
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
	 * @param uidFile Ҫ���õ� uidFile
	 */
	public void setUidFile(UidFile uidFile)
	{
		this.uidFile=uidFile;
	}
}
