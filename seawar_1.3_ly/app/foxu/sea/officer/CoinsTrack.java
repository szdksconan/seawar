package foxu.sea.officer;

/***
 * 
 * @author �����¼
 *
 */
public class CoinsTrack
{

	public static int ADD=0,DESC=1;

	/** ��¼ID */
	int id;
	/** ��־���� */
	int type;
	/** ���ID */
	int playerId;
	/** ������Ʒ��sid **/
	int propSid;
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
	int nowLeft;
	/** ���ӻ��Ǽ��� **/
	int state;

	/** �䶯ԭ�� **/
	/***
	 * BUY_OFFICER=1 �����̵����� FORM_DIS_OFFICER=2,ǲɢ���� ADD_BY_GM=3 gm���
	 */
	public static int BUY_OFFICER=1,FORM_DIS_OFFICER=2,ADD_BY_GM=3;

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

	public int getNowLeft()
	{
		return nowLeft;
	}

	public void setNowLeft(int nowLeft)
	{
		this.nowLeft=nowLeft;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state=state;
	}

}
