package foxu.sea.officer;

/**
 * ������־
 * @author Alan
 *
 */
public class OfficerTrack
{
	/** ��־���� */
	public static final int ADD=0,REDUCE=1;
	/**
	 *  ���ٱ䶯ԭ�� 
	 *  FROM_CHECK_POINT=0 �ؿ�����, FROM_EQUIP_BOX=1 װ������,
	 *	FROM_GM_ADD=2 GM���, FROM_NPCISLAND=3 npc�������, FROM_COMBINE=4 ���ײ��Ϻϳ�, 
	 *	FROM_QUALITY_UP=5 ����װ��, FROM_FOLLOW=6 ��������, FROM_BOSS=7 ����boss,
	 *	FROM_ARENA=8 ������, FROM_ALLIANCE=9 ���˾���, FROM_ANNOUNCEMENT=10 ���潱��,
	 *	FROM_EXCHANGECODE=11 �һ���, FROM_DAY_AWARD=12 ÿ�յ�½, FROM_ONLINE=13 ���߽���,
	 *	FROM_VIP_LIMIT=14 vip�������, FROM_TASK=15 ������, FROM_CLASSIC_LUCKY=16 ������͸,
	 *	FROM_CIRCLE_LUCKY=17 ��������, FROM_LOW_LOTTO=18 �ͼ�����, FROM_HIGH_LOTTO=19 �߼�����,
	 *	FROM_ACHIEVE=20 �ɾ�,FROM_VARIBLE=21 �콵����,FROM_ARMS=22 ��������,FROM_FIRST_PAY=23 �׳佱��,
	 *	FROM_TOTAL_BUY=24 �ۼƳ�ֵ,FROM_APP_GRADE=25 Ӧ������ FROM_APP_SHARE=26 Ӧ�÷���
	 *	FROM_RANK_AWARD=27 ��������� FROM_JIGSAW=28 ƴͼ� FROM_CROSSWAR���ս FROM_WELFARE�±�����
	 *	RROM_WAR_MANNIACս������ FROM_COMRADEս��ϵͳ FROM_FRAG_COMBINE=1000 ��Ƭ�ϳ�,FROM_FRAG_DRAW=1001 ��Ƭ�齱,
	 *	FROM_GM_ADD=1002 GM���    FROM_ELITE=1003 ��Ӣս�� FROM_OFFICER_SHOP=1004�����̵�
	 *	<p>
	 *	INTO_UP_RANK=2000 ����ͻ��,INTO_OFFICER_COMBINE=2001 ���ٺϳ�,INTO_OFFICER_RESET=2002 �����鼮
	 *	INTO_OFFICER_DISBAND=2003 ǲɢ����
	 */
	public static final int 
					// ��ȡ����(��ȡ��������Ʒ����ʱ,����װ����־ԭ��)
					FROM_FRAG_COMBINE=1000,FROM_FRAG_DRAW=1001,FROM_GM_ADD=1002,FROM_ELITE=1003,
					FROM_OFFICER_SHOP=1004,
					// ��������
					INTO_UP_RANK=2000,INTO_OFFICER_COMBINE=2001,INTO_OFFICER_RESET=2002,
					INTO_OFFICER_DISBAND=2003;

	/** ��¼ID */
	int id;
	/** ��־���� */
	int type;
	/** װ���䶯ԭ�� */
	int reason;
	/** ���ID */
	int playerId;
	/** װ��sid */
	int officerSid;
	/** װ������ */
	int num;
	/** ����ʱ�� */
	int createAt;
	/** ��ƷID */
	int item_id;
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
	
	public int getOfficerSid()
	{
		return officerSid;
	}
	
	public void setOfficerSid(int officerSid)
	{
		this.officerSid=officerSid;
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
	
	public int getNowLeft()
	{
		return nowLeft;
	}
	
	public void setNowLeft(int nowLeft)
	{
		this.nowLeft=nowLeft;
	}
	

}
