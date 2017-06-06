package foxu.sea.achieve;

import foxu.sea.Player;
import mustang.util.Sample;
import mustang.util.SampleFactory;


/**
 * �ɾ�
 * @author yw
 *
 */
public class Achievement extends Sample
{
	
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	
	/** ����KEY <=500��ǰ����  501-999�����������    >=1000�ۼ�����*/
	
	/** ���� 1-100 501-600 1000-1100 
	DIRECTOR_BUILD ָ������,SHIP_BUILD ����,TECH_BUILD ��������,
	HONOR_LEVEL ����,MONEY ���,METAL ����,OIL ʯ��,SILICA ���,URANIUM �˿�
	*/
	public static final int DIRECTOR_BUILD=1,SHIP_BUILD=2,TECH_BUILD=3,
					HONOR_LEVEL=4,MONEY=5,METAL=6,OIL=7,SILICA=8,URANIUM=9;
	/** ���� 101-200 601-700 1101-1200 
 	CHAPTER �½�,WILD Ұ��,ATTACK_PLAYR �������,BATTLE_SHIP ������ҵ�ս�н�,
	SUBMARINE ������ҵ�սǱͧ,CRUISER ������ҵ�Ѳ��,CARRIER ������ҵĺ�ĸ
	 */
	public static final int CHAPTER=601,WILD=1101,ATTACK_PLAYR=1102,BATTLE_SHIP=1103,
					SUBMARINE=1104,CRUISER=1105,CARRIER=1106;
	/** ���� 201-300 701-800 1201-1300 
	RECHARGE ��ֵ,BIND ���˺�,SERIERS_LOGIN ������½,
	SHARE_GAME ������Ϸ,SHARE_FIGHT ����ս��,PLAYER_LEVEL ��ҵȼ�,
	COMMAND_LEVEL ͳ���ȼ�,INVITE �������,ALLIANCE_OFFER �˹��׵�,
	ATTACK_BOSS ����Boss,KILL_BOSS ��ɱBoss,ALLIANCE_OFFER_ONE_DAY ÿ���˹��׵�,
	ARENA_RANK=603��������,MOUTH_CARD=604�¿���HONOR_SCORE=605����ֵ��FIGHT_SCORE=606ս��
	 */
	public static final int RECHARGE=1201,BIND=201,SERIERS_LOGIN=202,
					SHARE_GAME=1202,SHARE_FIGHT=1203,PLAYER_LEVEL=203,
					COMMAND_LEVEL=204,INVITE=1204,ALLIANCE_OFFER=1205,
					ATTACK_BOSS=1206,KILL_BOSS=1207,ALLIANCE_OFFER_ONE_DAY=602,
					ARENA_RANK=603,MOUTH_CARD=604,HONOR_SCORE=605,FIGHT_SCORE=606;
	/** ���Էֽ�ֵ */
	public static final int ATR_TYPE1=500,ATR_TYPE2=1000;
	/** �������ೣ�� */
	public static final int BASE=1,ARMY=2,HONOR=3,OTHER=4;
	
	/** ����keyֵ */
	int atrKey;
	/** ��Ҫ���ֵ */
	int[] needValue;
	 /** ��Ӧ����Sid */
	int[] awardSids;
	/** ���׶��ܻ��� */
	int[] score={1,2,3,4,5};
	/** ��Ӧ�Ľ���ͷ�� */
	int[] headSids;
	/** �������� */
	int baseType;
	/** ����ֵ */
	int sort;
	/** �콱���� */
	boolean awardClear;
	/** ��ֵ�ɼ� */
	boolean fullCollect=true;
	
	public boolean canAddValue(Player player)
	{
		int progress=player.getAchieveProgress(getSid());
		long cvalue=player.getAchieveValue(atrKey);
		if(!fullCollect)
		{
			if(progress>=needValue.length||cvalue>=needValue[progress])
				return false;
		}
		return true;
	}
	public int getNeedValue(int progress)
	{
		if(progress>=needValue.length) return needValue[needValue.length-1];
		return needValue[progress];
	}
	public int getAwardSid(int progress)
	{
		if(progress>=awardSids.length) return awardSids[awardSids.length-1];
		return awardSids[progress];
	}
	public int computeScore(int pg,long cv)
	{
		if(pg>=needValue.length) return score[score.length-1];
		if(cv<needValue[pg]) pg--;
		if(pg<0) return 0;
		return score[pg];
	}
	public int getMaxScore()
	{
		return score[score.length-1];
	}
	/** ��ȡ�ɾ��������� */
	public int getAddScore(int pg)
	{
		if(pg==0)return score[pg];
		return score[pg]-score[pg-1];
	}
	
	public int getAtr_key()
	{
		return atrKey;
	}
	
	public void setAtr_key(int atr_key)
	{
		this.atrKey=atr_key;
	}
	
	public int[] getNeedValue()
	{
		return needValue;
	}
	
	public void setNeedValue(int[] needValue)
	{
		this.needValue=needValue;
	}
	
	public int[] getAwardSid()
	{
		return awardSids;
	}
	
	public void setAwardSid(int[] awardSid)
	{
		this.awardSids=awardSid;
	}
	
	public int[] getHeadSids() {
		return headSids;
	}

	public void setHeadSids(int[] headSids) {
		this.headSids = headSids;
	}

	public int getHeadSid(int progress) {
		if (headSids == null) {
			return -1;
		}
		if (progress >= headSids.length) {
			return headSids[headSids.length - 1];
		}
		return headSids[progress];
	}

	public int getAchieveType() {
		return baseType;
	}
	
}
