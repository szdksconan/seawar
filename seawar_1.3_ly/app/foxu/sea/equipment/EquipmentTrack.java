package foxu.sea.equipment;

/**
 * װ����־
 * @author Alan
 *
 */
public class EquipmentTrack
{
	/** ��־���� */
	public static final int ADD=0,REDUCE=1;
	/**
	 *  װ���䶯ԭ�� 
	 *  FROM_CHECK_POINT=0 �ؿ�����, FROM_EQUIP_BOX=1 װ������,
	 *	FROM_GM_ADD=2 GM���, FROM_NPCISLAND=3 npc�������, FROM_COMBINE=4 ���ײ��Ϻϳ�, 
	 *	FROM_QUALITY_UP=5 ����װ��, FROM_FOLLOW=6 ��������, FROM_BOSS=7 ����boss,
	 *	FROM_ARENA=8 ������, FROM_ALLIANCE=9 ���˾���, FROM_ANNOUNCEMENT=10 ���潱��,
	 *	FROM_EXCHANGECODE=11 �һ���, FROM_DAY_AWARD=12 ÿ�յ�½, FROM_ONLINE=13 ���߽���,
	 *	FROM_VIP_LIMIT=14 vip�������, FROM_TASK=15 ������, FROM_CLASSIC_LUCKY=16 ������͸,
	 *	FROM_CIRCLE_LUCKY=17 ��������, FROM_LOW_LOTTO=18 �ͼ�����, FROM_HIGH_LOTTO=19 �߼�����,
	 *	FROM_ACHIEVE=20 �ɾ�,FROM_VARIBLE=21 �콵����,FROM_ARMS=22 ��������,FROM_FIRST_PAY=23 �׳佱��,
	 *	FROM_TOTAL_BUY=24 �ۼƳ�ֵ,FROM_APP_GRADE=25 Ӧ������ FROM_APP_SHARE=26 Ӧ�÷���
	 *	FROM_RANK_AWARD=27 ��������� FROM_JIGSAW=28 ƴͼ� FROM_CROSSWAR���ս FROM_WELFARE�±�����,
	 *	ALLIANCE_CHEST_AWARD=31���˱���,ALLIANCE_LUCKY_POINT_AWARD=32�������˻��ֱ���, 
	 *	LOGIN_REWARD=33��½�н��,PAY_RELAY=34��ֵ����,QUESTIONNAIRE=35�����ʾ�
	 *	RROM_WAR_MANNIACս������ FROM_COMRADEս��ϵͳ FROM_SHIPPING_LUCKY=38ͨ�̺��˳齱
	 *  FROM_LUCKY_EXPLORED=39,����̽�ջ FROM_ROB_LUCKY=40 ȫ����"��"�,FROM_GROWTH_PLAN=41�ɳ��ƻ�
	 *	<p>
	 *	INTO_INCR_EXP=100 ���Ӿ���, INTO_QUALITY_UP=101 װ����������, INTO_SALE=102 ����, 
	 *	INTO_COMBINE=103 ���ײ��Ϻϳ����� ,
	 */
	public static final int 
					// ��ȡ����
					FROM_CHECK_POINT=0,FROM_EQUIP_BOX=1,
					FROM_GM_ADD=2,FROM_NPCISLAND=3,FROM_COMBINE=4,
					FROM_QUALITY_UP=5,FROM_FOLLOW=6,FROM_BOSS=7,
					FROM_ARENA=8,FROM_ALLIANCE=9,FROM_ANNOUNCEMENT=10,
					FROM_EXCHANGECODE=11,FROM_DAY_AWARD=12,FROM_ONLINE=13,
					FROM_VIP_LIMIT=14,FROM_TASK=15,FROM_CLASSIC_LUCKY=16,
					FROM_CIRCLE_LUCKY=17,FROM_LOW_LOTTO=18,FROM_HIGH_LOTTO=19,
					FROM_ACHIEVE=20,FROM_VARIBLE=21,FROM_ARMS=22,FROM_FIRST_PAY=23,
					FROM_TOTAL_BUY=24,FROM_APP_GRADE=25,FROM_APP_SHARE=26,
					FROM_RANK_AWARD=27,FROM_JIGSAW=28,FROM_CROSSWAR=29,FROM_WELFARE=30,
					ALLIANCE_CHEST_AWARD=31,ALLIANCE_LUCKY_POINT_AWARD=32,
					LOGIN_REWARD=33,PAY_RELAY=34,QUESTIONNAIRE=35,
					RROM_WAR_MANNIAC=36,FROM_COMRADE=37,FROM_SHIPPING_LUCKY=38,
					FROM_LUCKY_EXPLORED=39,FROM_ROB_LUCKY=40,FROM_GROWTH_PLAN=41,
					FROM_SELLING_PACK1=42,FROM_SELLING_PACK2=43,FROM_SELLING_PACK3=44,
					FROM_INTIMACY_LUCKY = 45,
					// ��������
					INTO_INCR_EXP=100,INTO_QUALITY_UP=101,INTO_SALE=102,
					INTO_COMBINE=103;

	/** ��¼ID */
	int id;
	/** ��־���� */
	int type;
	/** װ���䶯ԭ�� */
	int reason;
	/** ���ID */
	int playerId;
	/** װ��sid */
	int equipSid;
	/** װ������ */
	int num;
	/** ����ʱ�� */
	int createAt;
	/** ��ƷID */
	int item_id;
	/** �� */
	int year;
	/** �� */
	int month;
	/** �� */
	int day;
	/** ��ǰʣ�� */
	int nowLeft;
	
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
	
	public int getReason()
	{
		return reason;
	}
	
	public void setReason(int reason)
	{
		this.reason=reason;
	}
	
	public int getPlayerId()
	{
		return playerId;
	}
	
	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}
	
	public int getEquipSid()
	{
		return equipSid;
	}
	
	public void setEquipSid(int equipSid)
	{
		this.equipSid=equipSid;
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
	
	public int getItem_id()
	{
		return item_id;
	}
	
	public void setItem_id(int item_id)
	{
		this.item_id=item_id;
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
	

}
