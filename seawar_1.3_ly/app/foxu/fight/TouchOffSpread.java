package foxu.fight;

import java.util.List;

import foxu.sea.fight.LimitNumSkill;
import mustang.io.ByteBuffer;

/**
 * 触发施放:根据触发时间和触发几率来施放技能.通过不同的扩展TouchOffSpread类型,
 * 判断触发技能后产生的效果和技能改变的对象,如果是相同的TouchOffSpread类型，根据触发时机判断是否该触发。
 * 
 * @author ZYT
 */
public class TouchOffSpread extends Spread
{

	/* static fields */
	/**
	 * BEFFOR_SPREAD=1释放前,影响技能的数据, HURT=2计算出伤害后,主动方再改变伤害,
	 * BE_HURT=3计算出伤害后,被动方再改变伤害, DEAD=4死亡后,HIT=5主动方命中后,
	 * BE_HIT=6被动方被命中，COMPUTE_ERUPT=7计算暴击,COMPUTE_SPEED=8计算速度时,
	 * COMPUTE_EXEMPT=9计算豁免时,HARM=10计算攻击力时,DEFENCE=11计算防御时,AFTER_HURT=12命中伤害以后
	 * ROUND_START=13  回合开始时触发 HURT_END 伤害结算完毕时触发 , BEFFOR_SPREAD_FOR_HURT = 15 出手前触发的独立增伤、必中、必爆等技能, 
	 * AFTER_SPREAD_FOR_HURT = 16 确定目标后 击中前 ,SPREAD_END一次出手彻底结束后 (比如航母打完6个坑位后单独触发)
	 */
	public final static int BEFFOR_SPREAD_FOR_HURT=15,BEFFOR_SPREAD=1,AFTER_SPREAD_FOR_HURT=16,HURT=2,BE_HURT=3,DEAD=4,HIT=5,
					BE_HIT=6,COMPUTE_ERUPT=7,COMPUTE_SPEED=8,
					COMPUTE_EXEMPT=9,HARM=10,DEFENCE=11,AFTER_HURT=12,ROUND_START=13,HURT_END=14,SPREAD_END=17;
	/** 类与类之间通讯常量:FAILD_TIME_ERROR=600触发时机错误,FAILD_UNLUCK=601运气不好 */
	public final static int FAILD_TIME_ERROR=600,FAILD_UNLUCK=601;

	/* fields */
	/** 触发几率 */
	int occurProbability;
	/** 触发时间 */
	int touchOffTime;
	/** 触发次数 */
	int touchOffLimitNum;
	/* dynamic fields */
	/** 当前状态 */
	int currentTouchTime;
	/** 触发血线*/
	int sourceHpPrecent;
	/** 触发血线*/
	int targetHpPrecent;

	/* preporties */
	/** 设置触发几率 */
	public void setOccurProbability(int occurProbability)
	{
		this.occurProbability=occurProbability;
	}
	/** 设置当前状态 */
	public void setCurrentTouchTime(int currentTouchTime)
	{
		this.currentTouchTime=currentTouchTime;
	}
	/** 获得触发几率 */
	public int getOccurProbability()
	{
		return occurProbability;
	}
	/** 获得触发时间 */
	public int getTouchOffTime()
	{
		return touchOffTime;
	}
	/** 设置触发时机 */	
	public void setTouchOffTime(int touchOffTime)
	{
		this.touchOffTime=touchOffTime;
	}
	/** 获得当前触发时机 */
	public int getCurrentTouchTime()
	{
		return currentTouchTime;
	}
	/** 获取触发次数 */	
	public int getTouchOffLimitNum()
	{
		return touchOffLimitNum;
	}
	/** 设置触发次数 */	
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
			//System.out.println("超过了触发次数 释放失败");
			return FAILD_TIME_ERROR;
		}
		if(checkHpCondition(source,target)){
			//System.out.println("没有达到血线要求");
			return FAILD_TIME_ERROR;
		}
		
		currentTouchTime=0;
		// 添加一个机制 为了方便使用现在代码 技能调用时机 概率为10000时候不做随机数运算 当做普通技能调用
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
	 * 判断技能是否有血线要求
	 * @param source 释放源
	 * @param target 目标
	 * @return 是否没有达成条件 false 表示可以释放
	 */
	public boolean checkHpCondition(Fighter source,Object target){
		boolean conditon = false;
		if(sourceHpPrecent!=0){//判断释放源
				int sourceHp = source.getScene().getFighterContainer().getTeamHp(source.getTeam());
				int sourceHpMax = source.getScene().getFighterContainer().getTeamHpMax(source.getTeam());
				float hpPrecent = (float)sourceHp/sourceHpMax;
				//System.out.println("释放源血线："+hpPrecent*100);
				if(hpPrecent*100>=sourceHpPrecent)conditon = true;//没有达到释放条件
		}
		if(targetHpPrecent!=0){//判断目标
			Fighter f = null;
			if(target instanceof Fighter&&(Fighter)target!=null) f = (Fighter)target;
			else if(target instanceof Fighter[]&&((Fighter[])target).length>0) f = ((Fighter[])target)[0];
			if(f!=null){
			int targetHp = source.getScene().getFighterContainer().getTeamHp(f.getTeam());
			int targetHpMax = source.getScene().getFighterContainer().getTeamHpMax(f.getTeam());
			float hpPrecent = (float)targetHp/targetHpMax;
			//System.out.println("目标血线："+hpPrecent*100);
			if(hpPrecent*100>=targetHpPrecent)conditon = true;//没有达到释放条件
			}	
		}
		
		return conditon;
	}
	
	
	/**
	 * 检测 坑位 限制性技能 次数 判断是否能释放
	 * @return
	 */
	public boolean checkFighterSpreedNum(Fighter source,Ability ability){
		if(touchOffLimitNum==0) return false;// 没有次数限制
		Object o=source.getUseLimitNumSkill().get(ability.getSid());
		int useNum=o==null?0:(Integer)o;
		//System.out.println("释放限制技能次数："+useNum+" 坑位 ："+source.getLocation());
		if(useNum>=touchOffLimitNum) return true;//超过限定次数
		return false;
	}
	
	/**
	 * 检测 坑位 限制性技能 次数 记录释放次数
	 * @return
	 */
	public void addFighterSpreedNum(Fighter source,Ability ability){
		if(touchOffLimitNum==0) return ;// 没有次数限制
		Object o=source.getUseLimitNumSkill().get(ability.getSid());
		int useNum=o==null?0:(Integer)o;
		source.getUseLimitNumSkill().put(ability.getSid(),useNum+1);
	}
	
	/**
	 * 判断是否超过限制次数 针对于某一方  需求改成针对于坑位 现作废
	 * @param source
	 * @param ability
	 * @return
	 */
	@Deprecated
	public boolean checkUseLimitNum(Fighter source,Ability ability){
		if(touchOffLimitNum==0)return false;//没有次数限制
		List<LimitNumSkill> limitNumSkills = source.getScene().getLimitNumSkillList();//已经释放过的 限制技能
		if(limitNumSkills.size()==0){
			limitNumSkills.add(new LimitNumSkill(source.getTeam(),ability.getSid(),1));
			return false;
		}
		for(int i=0;i<limitNumSkills.size();i++){
			LimitNumSkill info = limitNumSkills.get(0);
			if(info.getTeam()==source.getTeam()&&info.getSid()==ability.getSid()){//已经释放过
				if(info.getCount()>=touchOffLimitNum)//超过限定次数
					return true;
				else
					info.setCount(info.getCount()+1);
			}else{//第一次释放
				limitNumSkills.add(new LimitNumSkill(source.getTeam(),ability.getSid(),1));
			}
		}
		return false;
	}
	
	/** 源角色对目标角色使用该技能 */
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