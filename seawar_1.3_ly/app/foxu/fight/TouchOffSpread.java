package foxu.fight;

import java.util.List;

import foxu.sea.fight.LimitNumSkill;
import mustang.io.ByteBuffer;

/**
 * ����ʩ��:���ݴ���ʱ��ʹ���������ʩ�ż���.ͨ����ͬ����չTouchOffSpread����,
 * �жϴ������ܺ������Ч���ͼ��ܸı�Ķ���,�������ͬ��TouchOffSpread���ͣ����ݴ���ʱ���ж��Ƿ�ô�����
 * 
 * @author ZYT
 */
public class TouchOffSpread extends Spread
{

	/* static fields */
	/**
	 * BEFFOR_SPREAD=1�ͷ�ǰ,Ӱ�켼�ܵ�����, HURT=2������˺���,�������ٸı��˺�,
	 * BE_HURT=3������˺���,�������ٸı��˺�, DEAD=4������,HIT=5���������к�,
	 * BE_HIT=6�����������У�COMPUTE_ERUPT=7���㱩��,COMPUTE_SPEED=8�����ٶ�ʱ,
	 * COMPUTE_EXEMPT=9�������ʱ,HARM=10���㹥����ʱ,DEFENCE=11�������ʱ,AFTER_HURT=12�����˺��Ժ�
	 * ROUND_START=13  �غϿ�ʼʱ���� HURT_END �˺��������ʱ���� , BEFFOR_SPREAD_FOR_HURT = 15 ����ǰ�����Ķ������ˡ����С��ر��ȼ���, 
	 * AFTER_SPREAD_FOR_HURT = 16 ȷ��Ŀ��� ����ǰ ,SPREAD_ENDһ�γ��ֳ��׽����� (���纽ĸ����6����λ�󵥶�����)
	 */
	public final static int BEFFOR_SPREAD_FOR_HURT=15,BEFFOR_SPREAD=1,AFTER_SPREAD_FOR_HURT=16,HURT=2,BE_HURT=3,DEAD=4,HIT=5,
					BE_HIT=6,COMPUTE_ERUPT=7,COMPUTE_SPEED=8,
					COMPUTE_EXEMPT=9,HARM=10,DEFENCE=11,AFTER_HURT=12,ROUND_START=13,HURT_END=14,SPREAD_END=17;
	/** ������֮��ͨѶ����:FAILD_TIME_ERROR=600����ʱ������,FAILD_UNLUCK=601�������� */
	public final static int FAILD_TIME_ERROR=600,FAILD_UNLUCK=601;

	/* fields */
	/** �������� */
	int occurProbability;
	/** ����ʱ�� */
	int touchOffTime;
	/** �������� */
	int touchOffLimitNum;
	/* dynamic fields */
	/** ��ǰ״̬ */
	int currentTouchTime;
	/** ����Ѫ��*/
	int sourceHpPrecent;
	/** ����Ѫ��*/
	int targetHpPrecent;

	/* preporties */
	/** ���ô������� */
	public void setOccurProbability(int occurProbability)
	{
		this.occurProbability=occurProbability;
	}
	/** ���õ�ǰ״̬ */
	public void setCurrentTouchTime(int currentTouchTime)
	{
		this.currentTouchTime=currentTouchTime;
	}
	/** ��ô������� */
	public int getOccurProbability()
	{
		return occurProbability;
	}
	/** ��ô���ʱ�� */
	public int getTouchOffTime()
	{
		return touchOffTime;
	}
	/** ���ô���ʱ�� */	
	public void setTouchOffTime(int touchOffTime)
	{
		this.touchOffTime=touchOffTime;
	}
	/** ��õ�ǰ����ʱ�� */
	public int getCurrentTouchTime()
	{
		return currentTouchTime;
	}
	/** ��ȡ�������� */	
	public int getTouchOffLimitNum()
	{
		return touchOffLimitNum;
	}
	/** ���ô������� */	
	public void setTouchOffLimitNum(int touchOffLimitNum)
	{
		this.touchOffLimitNum=touchOffLimitNum;
	}
	/* methods */
	public int checkUsed(Fighter source,Object target,Ability ability)
	{
		if(touchOffTime!=currentTouchTime)
		{
			currentTouchTime=0;
			return FAILD_TIME_ERROR;
		}
		if(checkFighterSpreedNum(source,ability)){
			//System.out.println("�����˴������� �ͷ�ʧ��");
			return FAILD_TIME_ERROR;
		}
		if(checkHpCondition(source,target)){
			//System.out.println("û�дﵽѪ��Ҫ��");
			return FAILD_TIME_ERROR;
		}
		
		currentTouchTime=0;
		// ���һ������ Ϊ�˷���ʹ�����ڴ��� ���ܵ���ʱ�� ����Ϊ10000ʱ������������� ������ͨ���ܵ���
		if(occurProbability==10000){
			addFighterSpreedNum(source,ability);
			return 0;
		}
		int rd=source.getScene().getRandom().randomValue(
			FightScene.RANDOM_MINI,FightScene.RANDOM_MAX);
		rd=rd-1<0?0:rd-1;
		if(rd<occurProbability){
			addFighterSpreedNum(source,ability);
			return 0;
		}
		return FAILD_UNLUCK;
	}
	
	/**
	 * �жϼ����Ƿ���Ѫ��Ҫ��
	 * @param source �ͷ�Դ
	 * @param target Ŀ��
	 * @return �Ƿ�û�д������ false ��ʾ�����ͷ�
	 */
	public boolean checkHpCondition(Fighter source,Object target){
		boolean conditon = false;
		if(sourceHpPrecent!=0){//�ж��ͷ�Դ
				int sourceHp = source.getScene().getFighterContainer().getTeamHp(source.getTeam());
				int sourceHpMax = source.getScene().getFighterContainer().getTeamHpMax(source.getTeam());
				float hpPrecent = (float)sourceHp/sourceHpMax;
				//System.out.println("�ͷ�ԴѪ�ߣ�"+hpPrecent*100);
				if(hpPrecent*100>=sourceHpPrecent)conditon = true;//û�дﵽ�ͷ�����
		}
		if(targetHpPrecent!=0){//�ж�Ŀ��
			Fighter f = null;
			if(target instanceof Fighter&&(Fighter)target!=null) f = (Fighter)target;
			else if(target instanceof Fighter[]&&((Fighter[])target).length>0) f = ((Fighter[])target)[0];
			if(f!=null){
			int targetHp = source.getScene().getFighterContainer().getTeamHp(f.getTeam());
			int targetHpMax = source.getScene().getFighterContainer().getTeamHpMax(f.getTeam());
			float hpPrecent = (float)targetHp/targetHpMax;
			//System.out.println("Ŀ��Ѫ�ߣ�"+hpPrecent*100);
			if(hpPrecent*100>=targetHpPrecent)conditon = true;//û�дﵽ�ͷ�����
			}	
		}
		
		return conditon;
	}
	
	
	/**
	 * ��� ��λ �����Լ��� ���� �ж��Ƿ����ͷ�
	 * @return
	 */
	public boolean checkFighterSpreedNum(Fighter source,Ability ability){
		if(touchOffLimitNum==0) return false;// û�д�������
		Object o=source.getUseLimitNumSkill().get(ability.getSid());
		int useNum=o==null?0:(Integer)o;
		//System.out.println("�ͷ����Ƽ��ܴ�����"+useNum+" ��λ ��"+source.getLocation());
		if(useNum>=touchOffLimitNum) return true;//�����޶�����
		return false;
	}
	
	/**
	 * ��� ��λ �����Լ��� ���� ��¼�ͷŴ���
	 * @return
	 */
	public void addFighterSpreedNum(Fighter source,Ability ability){
		if(touchOffLimitNum==0) return ;// û�д�������
		Object o=source.getUseLimitNumSkill().get(ability.getSid());
		int useNum=o==null?0:(Integer)o;
		source.getUseLimitNumSkill().put(ability.getSid(),useNum+1);
	}
	
	/**
	 * �ж��Ƿ񳬹����ƴ��� �����ĳһ��  ����ĳ�����ڿ�λ ������
	 * @param source
	 * @param ability
	 * @return
	 */
	@Deprecated
	public boolean checkUseLimitNum(Fighter source,Ability ability){
		if(touchOffLimitNum==0)return false;//û�д�������
		List<LimitNumSkill> limitNumSkills = source.getScene().getLimitNumSkillList();//�Ѿ��ͷŹ��� ���Ƽ���
		if(limitNumSkills.size()==0){
			limitNumSkills.add(new LimitNumSkill(source.getTeam(),ability.getSid(),1));
			return false;
		}
		for(int i=0;i<limitNumSkills.size();i++){
			LimitNumSkill info = limitNumSkills.get(0);
			if(info.getTeam()==source.getTeam()&&info.getSid()==ability.getSid()){//�Ѿ��ͷŹ�
				if(info.getCount()>=touchOffLimitNum)//�����޶�����
					return true;
				else
					info.setCount(info.getCount()+1);
			}else{//��һ���ͷ�
				limitNumSkills.add(new LimitNumSkill(source.getTeam(),ability.getSid(),1));
			}
		}
		return false;
	}
	
	/** Դ��ɫ��Ŀ���ɫʹ�øü��� */
	public boolean used(Fighter source,Object target,Ability ability)
	{
		return false;
	}
	public void bytesWriteTouchOffInfo(ByteBuffer bb)
	{
		bb.writeByte(occurProbability);
	}

	public Object bytesReadTouchOffInfo(ByteBuffer data)
	{
		occurProbability=data.readUnsignedByte();
		return this;
	}
}