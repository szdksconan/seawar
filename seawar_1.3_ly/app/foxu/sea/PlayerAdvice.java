package foxu.sea;

import mustang.util.TimeKit;

/** ��ҽ��� */
public class PlayerAdvice
{
    /***/
	public static final int RESPONSE=1;
	/** ���ݿ����� */
	int id;
	/** ���� */
	String titile;
	/** ���� */
	String content;
	/** ������� */
	String playerName;
	/** ���ID */
	int playerId;
	/** ����ʱ�� */
	int creatTime;
	/** ״̬ 0δ���� 1�Ѵ��� */
	int state;
	/**GM�ظ�*/
	String gmResponse;

	public void creatIt(String title,String content,String playerName,
		int playerId)
	{
		this.titile=title;
		this.content=content;
		this.playerName=playerName;
		this.playerId=playerId;
		this.creatTime=TimeKit.getSecondTime();
	}

	
	/**
	 * @return content
	 */
	public String getContent()
	{
		return content;
	}

	
	/**
	 * @param content Ҫ���õ� content
	 */
	public void setContent(String content)
	{
		this.content=content;
	}

	
	/**
	 * @return creatTime
	 */
	public int getCreatTime()
	{
		return creatTime;
	}

	
	/**
	 * @param creatTime Ҫ���õ� creatTime
	 */
	public void setCreatTime(int creatTime)
	{
		this.creatTime=creatTime;
	}

	
	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}

	
	/**
	 * @param id Ҫ���õ� id
	 */
	public void setId(int id)
	{
		this.id=id;
	}

	
	/**
	 * @return playerId
	 */
	public int getPlayerId()
	{
		return playerId;
	}

	
	/**
	 * @param playerId Ҫ���õ� playerId
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	
	/**
	 * @return playerName
	 */
	public String getPlayerName()
	{
		return playerName;
	}

	
	/**
	 * @param playerName Ҫ���õ� playerName
	 */
	public void setPlayerName(String playerName)
	{
		this.playerName=playerName;
	}

	
	/**
	 * @return state
	 */
	public int getState()
	{
		return state;
	}

	
	/**
	 * @param state Ҫ���õ� state
	 */
	public void setState(int state)
	{
		this.state=state;
	}

	
	/**
	 * @return titile
	 */
	public String getTitile()
	{
		return titile;
	}

	
	/**
	 * @param titile Ҫ���õ� titile
	 */
	public void setTitile(String titile)
	{
		this.titile=titile;
	}


	
	/**
	 * @return gmResponse
	 */
	public String getGmResponse()
	{
		return gmResponse;
	}


	
	/**
	 * @param gmResponse Ҫ���õ� gmResponse
	 */
	public void setGmResponse(String gmResponse)
	{
		this.gmResponse=gmResponse;
	}
}
