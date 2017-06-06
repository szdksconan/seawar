package foxu.sea;


/**
 * 好友事件    添加 移除 帮助等
 * @author liuh
 *
 */
class FriendEvent
{
	int eventId;
	int eventType;
	int createTime;
	int sendPlayerId;
	
	
	
	public FriendEvent(int eventId,int eventType,int createTime,int sendPlayerId)
	{
		super();
		this.eventId = eventId;
		this.eventType=eventType;
		this.createTime=createTime;
		this.sendPlayerId=sendPlayerId;
	}

	public int getEventType()
	{
		return eventType;
	}
	
	public void setEventType(int eventType)
	{
		this.eventType=eventType;
	}
	
	public int getCreateTime()
	{
		return createTime;
	}
	
	public void setCreateTime(int createTime)
	{
		this.createTime=createTime;
	}
	
	public int getSendPlayerId()
	{
		return sendPlayerId;
	}
	
	public void setSendPlayerId(int sendPlayerId)
	{
		this.sendPlayerId=sendPlayerId;
	}

	
	public int getEventId()
	{
		return eventId;
	}

	
	public void setEventId(int eventId)
	{
		this.eventId=eventId;
	}
	
}

