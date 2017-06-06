package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * 一般活动
 * @author yw
 *
 */
public abstract class Activity extends Sample
{
	public static SampleFactory factory=new SampleFactory();

	/** 活动ID */
	int id;
	/** 开始时间 */
	int startTime;
	/** 结束时间 */
	int endTime;
//	/** 激活时间 */
//	int activeTime;
	/** 是否需要显示在活动列表 */
	boolean isListShow=true;
	/** 是否是新型活动 */
	boolean isnew;
	
	/** 开启活动 */
	public abstract String startActivity(String stime,String etime,String initData,CreatObjectFactory factoty);
	/** 重设活动 */
	public abstract String resetActivity(String stime,String etime,String initData,CreatObjectFactory factoty);
	/** 获取活动状态 */
	public abstract String getActivityState();
	/** 设置初始化活动数据 */
	public abstract void initData(ByteBuffer data,CreatObjectFactory factoty,boolean active);
	/** 获取初始化活动数据 */
	public abstract ByteBuffer getInitData();
	/** 显示序列化 */
	public abstract void showByteWrite(ByteBuffer data);
	/** 刷新前台 */
	public abstract void sendFlush(SessionMap smap);
	/** 新型序列化 */
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
	/** 判断活动是否在进行中 */
	public boolean isOpen(int nowTime)
	{
		return endTime>nowTime&&nowTime>=startTime;
	}
	
	/**
	 * 关闭活动
	 * 
	 * @return 关闭活动后是否保存活动数据
	 */
	public boolean closeActivity()
	{
		return true;
	}
	
	/** 是否需要显示在活动列表 */
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
