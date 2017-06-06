package foxu.sea.alliance.alliancebattle;

public class AllianceFightRecordTrack
{

	/**
	 * SAVE_WEEK_TIME=1 һ�ܵĿ�ʼʱ������ SAVE_STAGE_INFO =2
	 * ��ǰ���а���Ϣ,SAVE_PLAYER_INFO=3��ǰ��ұ�����Ϣ SAVE_BATTLE_INFO=4 ����ս��������Ϣ
	 * **/
	public static final int SAVE_WEEK_TIME=1,SAVE_BET_INFO=2,
					SAVE_PLAYER_INFO=3,SAVE_BATTLE_INFO=4;

	/** ��¼ID */
	int id;
	/** ����id **/
	int allianceId;
	/** ���а� **/
	String rankvalue;
	/** ����������Ϣ **/
	String players;
	/** ÿ�ܵ�ʱ�� **/
	int type;
	/** ����id **/
	int battleIsland;
	/** �������ʵ����� */
	int num;
	/** ����ʱ�� */
	int createAt;
	/** �� */
	int year;
	/** �� */
	int month;
	/** �� */
	int day;
	/** �׶� **/
	int stage;
	/** �ܱ�ʶ **/
	int state;
	/** ��ʼʱ�� **/
	int stime;
	/** ����ʱ�� **/
	int etime;

	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getAllianceId()
	{
		return allianceId;
	}

	
	public void setAllianceId(int allianceId)
	{
		this.allianceId=allianceId;
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
	
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}
	
	public int getYear()
	{
		return year;
	}
	
	public void setYear(int year)
	{
		this.year=year;
	}
	
	public int getMonth()
	{
		return month;
	}
	
	public void setMonth(int month)
	{
		this.month=month;
	}
	
	public int getDay()
	{
		return day;
	}
	
	public void setDay(int day)
	{
		this.day=day;
	}


	public int getStage()
	{
		return stage;
	}

	
	public void setStage(int stage)
	{
		this.stage=stage;
	}

	
	public int getBattleIsland()
	{
		return battleIsland;
	}

	
	public void setBattleIsland(int battleIsland)
	{
		this.battleIsland=battleIsland;
	}

	
	public int getState()
	{
		return state;
	}

	
	public void setState(int state)
	{
		this.state=state;
	}

	
	public String getRankvalue()
	{
		return rankvalue;
	}

	
	public void setRankvalue(String rankvalue)
	{
		this.rankvalue=rankvalue;
	}

	
	public String getPlayers()
	{
		return players;
	}

	
	public void setPlayers(String players)
	{
		this.players=players;
	}

	
	public int getType()
	{
		return type;
	}

	
	public void setType(int type)
	{
		this.type=type;
	}

	
	public int getStime()
	{
		return stime;
	}

	
	public void setStime(int stime)
	{
		this.stime=stime;
	}

	
	public int getEtime()
	{
		return etime;
	}

	
	public void setEtime(int etime)
	{
		this.etime=etime;
	}
	
	
}
