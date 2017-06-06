package foxu.sea.bind;


/**
 * 绑定日志
 * @author Alan
 *
 */
public class BindingTrack
{
	/** 所有类型*/
	public static final int ALL=0;
	/** 绑定类型<p>TELPHONE=1 电话, */
	public static final int TELPHONE=1;
	
	/** 日志类型<p>ADD_RECORD=1 新增记录,UPDATE_RECORD=2 修改记录,DELETE_RECORD=3 删除记录,REQUEST_CODE=4 获取验证码*/
	public static final int ADD_RECORD=1,UPDATE_RECORD=2,DELETE_RECORD=3,REQUEST_CODE=4;
	/** 操作类型<p>GM=1 GM,PLAYER=2 玩家,CENTER=3 中心推送	*/
	public static final int GM=1,PLAYER=2,CENTER=3;
	
	int id;
	/** 绑定类型*/
	int bindType;
	/** 日志类型*/
	int trackType;
	/** 操作类型*/
	int actionType;
	/** 账号id*/
	int uid;
	/** 角色id*/
	int pid;
	int time;
	/** 本次操作信息*/
	String operateInfo;
	/** 之前的记录信息*/
	String lastRecord;
	/** 现在的记录信息*/
	String currentRecord;
	
	public BindingTrack()
	{
		super();
	}
	
	public BindingTrack(int bindType,int trackType,int actionType,int uid,
		int pid,int time,String operateInfo,String lastRecord,
		String currentRecord)
	{
		super();
		this.bindType=bindType;
		this.trackType=trackType;
		this.actionType=actionType;
		this.uid=uid;
		this.pid=pid;
		this.time=time;
		this.operateInfo=operateInfo;
		this.lastRecord=lastRecord;
		this.currentRecord=currentRecord;
	}

	public int getBindType()
	{
		return bindType;
	}
	
	public void setBindType(int bindType)
	{
		this.bindType=bindType;
	}
	
	public int getTrackType()
	{
		return trackType;
	}
	
	public void setTrackType(int trackType)
	{
		this.trackType=trackType;
	}
	
	public int getActionType()
	{
		return actionType;
	}
	
	public void setActionType(int actionType)
	{
		this.actionType=actionType;
	}
	
	public int getTime()
	{
		return time;
	}
	
	public void setTime(int time)
	{
		this.time=time;
	}
	
	public String getOperateInfo()
	{
		return operateInfo;
	}
	
	public void setOperateInfo(String operateInfo)
	{
		this.operateInfo=operateInfo;
	}
	
	public String getLastRecord()
	{
		return lastRecord;
	}
	
	public void setLastRecord(String lastRecord)
	{
		this.lastRecord=lastRecord;
	}
	
	public String getCurrentRecord()
	{
		return currentRecord;
	}
	
	public void setCurrentRecord(String currentRecord)
	{
		this.currentRecord=currentRecord;
	}

	
	public int getId()
	{
		return id;
	}

	
	public void setId(int id)
	{
		this.id=id;
	}

	
	public int getUid()
	{
		return uid;
	}

	
	public void setUid(int uid)
	{
		this.uid=uid;
	}

	
	public int getPid()
	{
		return pid;
	}

	
	public void setPid(int pid)
	{
		this.pid=pid;
	}
	
}
