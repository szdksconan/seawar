package foxu.sea.arena;

/**
 * 竞技场的角斗士
 * 
 * @author comeback
 * 
 */
public class Gladiator
{
	/** 玩家id */
	int playerId;
	
	/** 排名 */
	int ranking;
	
	/** 前一天的排名 */
	int lastDayRanking;
	
	/** 胜利次数 */
	int win;
	
	/** 失败次数 */
	int lose;
	
	/** 今天已挑战次数 */
	int todayBattleCount;
	
	/**
	 * 设置玩家ID
	 * @param playerId
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}
	
	/** 获取玩家ID */
	public int getPlayerId()
	{
		return playerId;
	}
	
	/**
	 * 设置排名
	 * @param ranking
	 */
	public void setRanking(int ranking)
	{
		this.ranking=ranking;
	}
	
	/**
	 * 获取排名
	 * @return
	 */
	public int getRanking()
	{
		return this.ranking;
	}
	
	/**
	 * 设置前一天的排名
	 * @param ranking
	 */
	public void setLastDayRanking(int ranking)
	{
		this.lastDayRanking=ranking;
	}
	
	/**
	 * 获取前一天的排名
	 * @return
	 */
	public int getLastDayRanking()
	{
		return this.lastDayRanking;
	}
	
	public void setWin(int n)
	{
		this.win=n;
	}
	
	public int getWin()
	{
		return this.win;
	}
	public void incrWin()
	{
		if(this.win<Integer.MAX_VALUE)
			this.win++;
	}
	
	public void setLose(int lose)
	{
		this.lose=lose;
	}
	
	public int getLose()
	{
		return this.lose;
	}
	
	public void incrLose()
	{
		if(this.lose<Integer.MAX_VALUE)
			this.lose++;
	}
	
	public void incrTodayBattleCount()
	{
		this.todayBattleCount++;
	}
	
	public void resetTodayBattleCount()
	{
		this.todayBattleCount=0;
	}
	
	public void setTodayBattleCount(int todayBattleCount)
	{
		this.todayBattleCount=todayBattleCount;
	}
	
	public int getTodayBattleCount()
	{
		return this.todayBattleCount;
	}
}