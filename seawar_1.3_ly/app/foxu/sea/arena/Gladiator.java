package foxu.sea.arena;

/**
 * �������ĽǶ�ʿ
 * 
 * @author comeback
 * 
 */
public class Gladiator
{
	/** ���id */
	int playerId;
	
	/** ���� */
	int ranking;
	
	/** ǰһ������� */
	int lastDayRanking;
	
	/** ʤ������ */
	int win;
	
	/** ʧ�ܴ��� */
	int lose;
	
	/** ��������ս���� */
	int todayBattleCount;
	
	/**
	 * �������ID
	 * @param playerId
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}
	
	/** ��ȡ���ID */
	public int getPlayerId()
	{
		return playerId;
	}
	
	/**
	 * ��������
	 * @param ranking
	 */
	public void setRanking(int ranking)
	{
		this.ranking=ranking;
	}
	
	/**
	 * ��ȡ����
	 * @return
	 */
	public int getRanking()
	{
		return this.ranking;
	}
	
	/**
	 * ����ǰһ�������
	 * @param ranking
	 */
	public void setLastDayRanking(int ranking)
	{
		this.lastDayRanking=ranking;
	}
	
	/**
	 * ��ȡǰһ�������
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