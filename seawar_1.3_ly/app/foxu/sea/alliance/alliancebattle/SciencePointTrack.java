package foxu.sea.alliance.alliancebattle;


/***
 * �Ƽ�������ʼ�¼����־
 * @author lhj
 *
 */
public class SciencePointTrack
{
	/** ��־���� */
	public static final int ADD=0,REDUCE=1;
	/**
	 * ��˿Ƽ� ���� FROM_ALLIANCE_FIGHT=1 ����սս�������Ժ��ȡ FROM_DAY_SNED=2 ����ս��ռ��ĵ���
	 * FROM_GIVE_VALUES=3 ���׻�ȡ * ���� USER_ALLIANCE_SKILL=4 ��������
	 */
	public static final int FROM_ALLIANCE_FIGHT=1,FROM_DAY_SNED=2,
					FROM_GIVE_VALUES=3,USE_ALLIANCE_SKILL=4;

	/**
	 * �������ӣ�FROM_GIVE_VALUE=1 ���˾��� REBACK_BY_COMBINED=2 ϵͳ��������(�Ϸ�)
	 * REBACK_BY_BEYOND=4 ����ʧ��(��������) BET_ISLAND=3 ��������ս����
	 **/
	public static final int FROM_GIVE_VALUE=1,REBACK_BY_COMBINED=2,
					REBACK_BY_BEYOND=4,BET_ISLAND=3;
	
	/**MATERIAL=1 ���� SCIENCE_POINT=2 �Ƽ��� **/
	public static final int MATERIAL=1,SCIENCE_POINT=2;
	/** ��¼ID */
	int id;
	/** ��־���� */
	int type;
	/** ���ID */
	int playerId;
	/**����id**/
	int allianceId;
	/** ���Ļ��ֵ����� */
	int num;
	/** ����ʱ�� */
	int createAt;
	/** �� */
	int year;
	/** �� */
	int month;
	/** �� */
	int day;
	/** ��ǰʣ�� */
	long nowLeft;
	/** ���ӻ��Ǽ��� **/
	int state;
	/**�����ǿƼ���**/
	int  style;
	/**������Ϣ**/
	String extra;
	
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

	
	public int getState()
	{
		return state;
	}
	
	public void setState(int state)
	{
		this.state=state;
	}

	
	public int getPlayerId()
	{
		return playerId;
	}

	
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	
	public long getNowLeft()
	{
		return nowLeft;
	}

	
	public void setNowLeft(long nowLeft)
	{
		this.nowLeft=nowLeft;
	}

	
	public int getStyle()
	{
		return style;
	}

	
	public void setStyle(int style)
	{
		this.style=style;
	}

	
	public String getExtra()
	{
		return extra;
	}

	
	public void setExtra(String extra)
	{
		this.extra=extra;
	}
	
	
	
}
