package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * һ��
 * @author yw
 *
 */
public abstract class Activity extends Sample
{
	public static SampleFactory factory=new SampleFactory();

	/** �ID */
	int id;
	/** ��ʼʱ�� */
	int startTime;
	/** ����ʱ�� */
	int endTime;
//	/** ����ʱ�� */
//	int activeTime;
	/** �Ƿ���Ҫ��ʾ�ڻ�б� */
	boolean isListShow=true;
	/** �Ƿ������ͻ */
	boolean isnew;
	
	/** ����� */
	public abstract String startActivity(String stime,String etime,String initData,CreatObjectFactory factoty);
	/** ���� */
	public abstract String resetActivity(String stime,String etime,String initData,CreatObjectFactory factoty);
	/** ��ȡ�״̬ */
	public abstract String getActivityState();
	/** ���ó�ʼ������� */
	public abstract void initData(ByteBuffer data,CreatObjectFactory factoty,boolean active);
	/** ��ȡ��ʼ������� */
	public abstract ByteBuffer getInitData();
	/** ��ʾ���л� */
	public abstract void showByteWrite(ByteBuffer data);
	/** ˢ��ǰ̨ */
	public abstract void sendFlush(SessionMap smap);
	/** �������л� */
	public void showByteWriteNew(ByteBuffer data,Player player,CreatObjectFactory objfactory)
	{
		
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getStartTime()
	{
		return startTime;
	}
	
	public int getEndTime()
	{
		return endTime;
	}
	
//	public int getActiveTime()
//	{
//		return activeTime;
//	}
	
	public void setStartTime(int startTime)
	{
		this.startTime=startTime;
	}
	
	public void setEndTime(int endTime)
	{
		this.endTime=endTime;
	}
	
//	public void setActiveTime(int activeTime)
//	{
//		this.activeTime=activeTime;
//	}
	/** �жϻ�Ƿ��ڽ����� */
	public boolean isOpen(int nowTime)
	{
		return endTime>nowTime&&nowTime>=startTime;
	}
	
	/**
	 * �رջ
	 * 
	 * @return �رջ���Ƿ񱣴�����
	 */
	public boolean closeActivity()
	{
		return true;
	}
	
	/** �Ƿ���Ҫ��ʾ�ڻ�б� */
	public boolean isListShow()
	{
		return isListShow;
	}
	
	public boolean isIsnew()
	{
		return isnew;
	}
	
	public void setIsnew(boolean isnew)
	{
		this.isnew=isnew;
	}
	
	
}
