package foxu.sea;


/**
 * 亲密度相关
 * 
 * @author liuh
 *
 */
class FriendIntimacy
{
	Player player;
	/** 赠送给好友的时间 */
	int lastGiveTime;
	/** 好友赠送给我的亲密度的时间*/
	int lastfriendGiveTime;
	/** 领取还有亲密度时间 */
	int lastReceivedTime;
	
	
	

	
	public Player getPlayer()
	{
		return player;
	}

	
	public void setPlayer(Player player)
	{
		this.player=player;
	}

	public int getLastGiveTime()
	{
		return lastGiveTime;
	}
	
	public void setLastGiveTime(int lastGiveTime)
	{
		this.lastGiveTime=lastGiveTime;
	}
	
	public int getLastfriendGiveTime()
	{
		return lastfriendGiveTime;
	}
	
	public void setLastfriendGiveTime(int lastfriendGiveTime)
	{
		this.lastfriendGiveTime=lastfriendGiveTime;
	}
	
	public int getLastReceivedTime()
	{
		return lastReceivedTime;
	}
	
	public void setLastReceivedTime(int lastReceivedTime)
	{
		this.lastReceivedTime=lastReceivedTime;
	}

	public FriendIntimacy(int lastGiveTime,
		int lastfriendGiveTime,int lastReceivedTime)
	{
		this.lastGiveTime=lastGiveTime;
		this.lastfriendGiveTime=lastfriendGiveTime;
		this.lastReceivedTime=lastReceivedTime;
	}
	
	public FriendIntimacy(Player player,int lastGiveTime,
		int lastfriendGiveTime,int lastReceivedTime)
	{
		this.player = player;
		this.lastGiveTime=lastGiveTime;
		this.lastfriendGiveTime=lastfriendGiveTime;
		this.lastReceivedTime=lastReceivedTime;
	}
	
	
}