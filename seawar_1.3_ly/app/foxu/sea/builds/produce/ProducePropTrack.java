package foxu.sea.builds.produce;


public class ProducePropTrack
{
	public static final int START=1,HOLD=2,COMPLETE=3,CANCEL=4;
	int id;
	int type;
	int playerId;
	int propSid;
	int num;
	int createAt;
	int needTime;
	int buildIndex;
	int buildSid;
	int buildLv;
	int productId;
	public ProducePropTrack(int id,int type,int playerId,int propSid,int num,
		int createAt,int needTime,int buildIndex,int buildSid,int buildLv,int productId)
	{
		super();
		this.id=id;
		this.type=type;
		this.playerId=playerId;
		this.propSid=propSid;
		this.num=num;
		this.createAt=createAt;
		this.needTime=needTime;
		this.buildIndex=buildIndex;
		this.buildSid=buildSid;
		this.buildLv=buildLv;
		this.productId=productId;
	}
	public ProducePropTrack()
	{
		super();
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type=type;
	}
	
	public int getPropSid()
	{
		return propSid;
	}
	
	public void setPropSid(int propSid)
	{
		this.propSid=propSid;
	}
	
	public int getNum()
	{
		return num;
	}
	
	public void setNum(int num)
	{
		this.num=num;
	}
	
	public int getCreateAt()
	{
		return createAt;
	}
	
	public void setCreateAt(int creatAt)
	{
		this.createAt=creatAt;
	}
	
	public int getNeedTime()
	{
		return needTime;
	}
	
	public void setNeedTime(int needTime)
	{
		this.needTime=needTime;
	}
	
	public int getBuildIndex()
	{
		return buildIndex;
	}
	
	public void setBuildIndex(int buildIndex)
	{
		this.buildIndex=buildIndex;
	}
	
	public int getBuildSid()
	{
		return buildSid;
	}
	
	public void setBuildSid(int buildSid)
	{
		this.buildSid=buildSid;
	}
	
	public int getBuildLv()
	{
		return buildLv;
	}
	
	public void setBuildLv(int buildLv)
	{
		this.buildLv=buildLv;
	}
	
	public int getPlayerId()
	{
		return playerId;
	}
	
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}
	
	public int getProductId()
	{
		return productId;
	}
	
	public void setProductId(int productId)
	{
		this.productId=productId;
	}
	
	public String toString()
	{
		return "id:"+id+",type:"+type+",playerId:"+playerId+",propSid:"
			+propSid+",num:"+num+",createAt:"+createAt+",needTime:"+needTime
			+",buildIndex:"+buildIndex+",buildSid:"+buildSid+",buildLv:"
			+buildLv+",productId:"+productId;
	}
}
