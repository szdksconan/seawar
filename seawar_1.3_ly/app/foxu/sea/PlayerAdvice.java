package foxu.sea;

import mustang.util.TimeKit;

/** 玩家建议 */
public class PlayerAdvice
{
    /***/
	public static final int RESPONSE=1;
	/** 数据库自增 */
	int id;
	/** 标题 */
	String titile;
	/** 内容 */
	String content;
	/** 玩家名字 */
	String playerName;
	/** 玩家ID */
	int playerId;
	/** 创建时间 */
	int creatTime;
	/** 状态 0未处理 1已处理 */
	int state;
	/**GM回复*/
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
	 * @param content 要设置的 content
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
	 * @param creatTime 要设置的 creatTime
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
	 * @param id 要设置的 id
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
	 * @param playerId 要设置的 playerId
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
	 * @param playerName 要设置的 playerName
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
	 * @param state 要设置的 state
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
	 * @param titile 要设置的 titile
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
	 * @param gmResponse 要设置的 gmResponse
	 */
	public void setGmResponse(String gmResponse)
	{
		this.gmResponse=gmResponse;
	}
}
