package foxu.sea.proplist;


/**
 * 物品日志
 * @author Alan
 *
 */
public class PropTrack
{
	public static final int INCR_PROP=0,DECR_PROP=1;
	//[2014-12-08 09:55:13]INCR_PROP pid:167827162 sid:5      num:1 cnum:1
	int id;
	int createAt;
	int type;
	int playerId;
	int propSid;
	int invokeNum;
	int leftNum;
	
	public PropTrack()
	{
		super();
	}

	public PropTrack(int id,int createAt,int type,int playerId,int propSid,
		int invokeNum,int leftNum)
	{
		super();
		this.id=id;
		this.createAt=createAt;
		this.type=type;
		this.playerId=playerId;
		this.propSid=propSid;
		this.invokeNum=invokeNum;
		this.leftNum=leftNum;
	}

	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getCreateAt()
	{
		return createAt;
	}
	
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type=type;
	}
	
	public int getPlayerId()
	{
		return playerId;
	}
	
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}
	
	public int getPropSid()
	{
		return propSid;
	}
	
	public void setPropSid(int propSid)
	{
		this.propSid=propSid;
	}
	
	public int getInvokeNum()
	{
		return invokeNum;
	}
	
	public void setInvokeNum(int invokeNum)
	{
		this.invokeNum=invokeNum;
	}
	
	public int getLeftNum()
	{
		return leftNum;
	}
	
	public void setLeftNum(int leftNum)
	{
		this.leftNum=leftNum;
	}
	

}
