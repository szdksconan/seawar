package foxu.sea;


/**
 * ���ܶ����
 * 
 * @author liuh
 *
 */
class FriendIntimacy
{
	Player player;
	/** ���͸����ѵ�ʱ�� */
	int lastGiveTime;
	/** �������͸��ҵ����ܶȵ�ʱ��*/
	int lastfriendGiveTime;
	/** ��ȡ�������ܶ�ʱ�� */
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