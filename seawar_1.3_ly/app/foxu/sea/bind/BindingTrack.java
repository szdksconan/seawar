package foxu.sea.bind;


/**
 * ����־
 * @author Alan
 *
 */
public class BindingTrack
{
	/** ��������*/
	public static final int ALL=0;
	/** ������<p>TELPHONE=1 �绰, */
	public static final int TELPHONE=1;
	
	/** ��־����<p>ADD_RECORD=1 ������¼,UPDATE_RECORD=2 �޸ļ�¼,DELETE_RECORD=3 ɾ����¼,REQUEST_CODE=4 ��ȡ��֤��*/
	public static final int ADD_RECORD=1,UPDATE_RECORD=2,DELETE_RECORD=3,REQUEST_CODE=4;
	/** ��������<p>GM=1 GM,PLAYER=2 ���,CENTER=3 ��������	*/
	public static final int GM=1,PLAYER=2,CENTER=3;
	
	int id;
	/** ������*/
	int bindType;
	/** ��־����*/
	int trackType;
	/** ��������*/
	int actionType;
	/** �˺�id*/
	int uid;
	/** ��ɫid*/
	int pid;
	int time;
	/** ���β�����Ϣ*/
	String operateInfo;
	/** ֮ǰ�ļ�¼��Ϣ*/
	String lastRecord;
	/** ���ڵļ�¼��Ϣ*/
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
