package foxu.sea.fight;

import mustang.event.ChangeAdapter;
import mustang.io.ByteBuffer;
import foxu.fight.Ability;
import foxu.fight.AbilityList;
import foxu.fight.AttackBuff;
import foxu.fight.FightScene;
import foxu.fight.FightScene.FightEvent;
import foxu.fight.Fighter;
import foxu.fight.IntervalChangeEffect;
import foxu.fight.Spread;
import foxu.sea.PublicConst;

/**
 * 战斗事件收集器
 * 
 * @author rockzyt
 */
public class FightShowEventRecord extends ChangeAdapter
{

	
	
	/** 军官技能 效果 SID*/
	/**常规护盾**/
	public static final int NORMAl_SHIELD = 21001;
	/**护盾出现**/
	public static final int OPEN_SHIELD = 21002 ;
	/**反伤护盾*/
	public static final int RESET_SHIELD = 21003;
	/**吸收护盾*/
	public static final int MISS_SHIELD = 21004;
	/**反伤护盾的常规护盾 **/
	public static final int RETURN_NORMAl_SHIELD = 21005;
	
	/** 军官技能 效果 Type*/
	/**军官技能出手 */
	public static final int OFFICER_SKILL_START_TYPE = 36;
	/**反伤伤害*/
	public static final int RETURN_HURT_TYPE = 37;
	/** 延迟事件 */
	public static final int LAG_TYPE = 101;
	/** 护盾展开  102*/
	public static final int OPEN_SHIELD_TYPE = 102;
	/** 护盾吸收 103*/
	public static final int MISS_SHIELD_TYPE = 103;
	/** 普通护盾消失 104*/
	public static final int MISS_SHIELD_TIME_OUT_TYPE = 104;
	/** 护盾反伤 */
	public static final int RESET_SHIEL_TYPE = 105;
	
	
	
	
	// GET_SCENE 0
	// SCENE_ADD_FIGHTER 1
	// FIGHT_START 2
	// FIGHT_OVER 3
	// ROUND_START 4
	// SKII_SPRING 5
	// READY 6
	// ROUND_OVER 7
	// ACTION_START 8
	// SPREAD_START 9
	// ADD_ABILITY 10
	// EXEMPT 11
	// HURT 12
	// IMMUNITY 13
	// RESIST 14
	// DEAD 15
	// SPREAD_OVER 16
	// REMOVE_FIGHTER 17
	// CLEAN_ABILITY 18
	// TOUCH_SPREAD_START 19
	// TOUCH_SPREAD_OVER 20
	// ACTION_OVER 21
	// ERUPT 22
	// NEXT_TARGET 23
	// ATTACK_ONCE 24
	// ABILITY_TIME_OUT 25
	// INTERVAL_EFFECT 26
	// NEXT_FIGHTER 27
	// EXECUTE_AILITY 28
	// LIMIT_HURT 29
	// FLUSH_BUFF 30
	// OTHER_HURT 31
	// ADD_TIME 32
	// ATTACK_CONTINUE 33

	/* fields */
	ByteBuffer data=new ByteBuffer();

	/* properties */
	/** 获得战报 */
	public ByteBuffer getRecord()
	{
		return data;
	}

	/* methods */
	public void change(Object source,int type)
	{
		
		if(type==FightEvent.FIGHT_START.ordinal()
			&&source instanceof FightScene)
		{//战斗开始前双发部队信息的序列化
			// System.out
			// .println("战斗开始 type:"+FightEvent.FIGHT_START.ordinal());
			data.clear();
			FightScene scene=(FightScene)source;
			Fighter[] fighters=scene.getFighterContainer().getAllFighter();
			data.writeByte(type);
			data.writeByte(fighters.length);
			for(int i=0;i<fighters.length;i++)
			{
				fighters[i].showBytesWrite(data);
			}
		}
		else if(type==FightEvent.ROUND_START.ordinal()
			&&source instanceof FightScene)
		{//计算回合数
			// System.out.println("回合开始 type:"+FightEvent.ROUND_START.ordinal()
			// +" currentRound:"+((FightScene)source).getCurrentRound());
			data.writeByte(type);
			data.writeByte(((FightScene)source).getCurrentRound());
		}
		// else if(type==FightEvent.SPREAD_OVER.ordinal()//
		// 脉冲技能处理完成的一个单独的释放完毕消息
		// &&source instanceof AbilityList)
		// {
		// data.writeByte(type);
		// }
		// else if(type==FightEvent.SPREAD_OVER.ordinal()//
		// 脉冲技能处理完成的一个单独的释放完毕消息
		// &&source instanceof Fighter)
		// {
		// data.writeByte(type);
		// }
		else if(type==FightEvent.SPREAD_OVER.ordinal()// 脉冲技能处理完成的一个单独的释放完毕消息
			&&source instanceof FightScene)
		{
			// System.out.println("spread_over type:"
			// +FightEvent.SPREAD_OVER.ordinal());
			data.writeByte(type);
		}
	}
	public void change(Object source,int type,int v1)
	{
		if(type==FightEvent.ROUND_OVER.ordinal()
			&&source instanceof FightScene)
		{
			//System.out.println("回合结束 type:"+FightEvent.ROUND_OVER.ordinal());
			data.writeByte(type);
		}
		// listener.change(this,FightEvent.FIGHT_OVER.ordinal(),team);
		else if(type==FightEvent.FIGHT_OVER.ordinal()
			&&source instanceof FightScene)
		{
			//System.out.println("战斗结束,胜利队伍 type:"
			//+FightEvent.FIGHT_OVER.ordinal()+" 队伍号:"+v1);
			data.writeByte(type);
			data.writeByte(v1);
		}
		else if(type==FightEvent.ATTACK_CONTINUE.ordinal())
		{
			data.writeByte(type);
			data.writeShort(v1);
		}
	}
	public void change(Object source,int type,Object v1)
	{
		if(type==FightEvent.ADD_ABILITY.ordinal()&&source instanceof Fighter)
		{
			//System.out.println("Figher type:"
			//+FightEvent.ADD_ABILITY.ordinal()+" fighterUid:"
			//+((Fighter)source).getUid()+",挂载技能="+((Ability)v1).getSid());
			
			if(((Ability)v1).getSid() == RETURN_NORMAl_SHIELD){//特殊效果挂载技能 后端拼接
				data.writeByte(FightEvent.ADD_ABILITY.ordinal());
				data.writeShort(OPEN_SHIELD);
				data.writeInt(((Fighter)source).getUid());
				data.writeByte(FightEvent.ABILITY_TIME_OUT.ordinal());
				data.writeShort(OPEN_SHIELD);
				data.writeInt(((Fighter)source).getUid());
				data.writeByte(FightEvent.ADD_ABILITY.ordinal());
				data.writeShort(RETURN_NORMAl_SHIELD);
				data.writeInt(((Fighter)source).getUid());
			}else{
				data.writeByte(type);
				data.writeShort(((Ability)v1).getSid());
				data.writeInt(((Fighter)source).getUid());
			}
		}
		else if(type==FightEvent.NEXT_FIGHTER.ordinal()
			&&source instanceof FightScene)
		{
			Fighter f=(Fighter)v1;
			if(f.isDead()) return;
			//System.out.println("下一个Fighter type:"
			//+FightEvent.NEXT_FIGHTER.ordinal()+" fighterUid:"
			//+((Fighter)v1).getTeam()+" "+((Fighter)v1).getUid());
			data.writeByte(type);
			data.writeInt(f.getUid());
		}
		// change(this,FightScene.FightEvent.IMMUNITY.ordinal(),fighter.getLocation())
		else if(type==FightEvent.IMMUNITY.ordinal()
			&&source instanceof BaseAttackFormula)
		{
			//System.out.println("Fighter免疫:"+((Fighter)v1).getUid());
			data.writeByte(type);
			data.writeInt(((Fighter)v1).getUid());
		}
		else if(type==FightEvent.DEAD.ordinal()
			&&((source instanceof BaseAttackFormula)||(source instanceof Fighter)))
		{
			//System.out.println("Fighter死亡 type:"+type==FightEvent.DEAD
			//.ordinal()+" fighterUid:"+((Fighter)v1).getUid());
			data.writeByte(type);
			data.writeInt(((Fighter)v1).getUid());
		}
		else if(type==FightEvent.ACTIVE_STATE.ordinal()&&source instanceof Fighter)
		{
			//System.out.println("Figher type:"
			//+FightEvent.ACTIVE_STATE.ordinal()+" fighterUid:"
			//+((Fighter)source).getUid()+",激活技能="+((Ability)v1).getSid());
			data.writeByte(type);
			data.writeShort(((Ability)v1).getSid());
			data.writeInt(((Fighter)source).getUid());
		}
	}
	
	/**
	 * 战报序列化for 军官技能 组装战报  前端表示自己没法自己组装
	 * @param source 效果源
	 * @param type
	 * @param v1
	 */
	public void changeForOfficer(Object source,int type,Object v1,Object v2){
		if(type==OFFICER_SKILL_START_TYPE&&source instanceof Fighter){ //军官释放技能
			//System.out.println("出手前触发了军官技能："+((Ability)v1).getSid());
			data.writeByte(type);
			data.writeShort(((Ability)v1).getSid());
			data.writeInt(((Fighter)source).getTeam());
			data.writeByte(((Fighter)source).getOfficerLv());//军官等级
		}else if(type==FightEvent.ADD_ABILITY.ordinal()&&source instanceof Fighter){ //普通军官挂载效果
			//System.out.println("触发了军官技能攻击效果");
			data.writeByte(type);
			data.writeShort(((AttackBuff)v1).getSid());
			data.writeInt(((Fighter)source).getTeam());
		}else if(type==LAG_TYPE){//延迟事件
			data.writeByte(type);
			data.writeInt(700);
		}else if(type==OPEN_SHIELD_TYPE){//护盾展开   由展开-》 正常组合而成
			//System.out.println("展开了吸收护盾");
			data.writeByte(FightEvent.ADD_ABILITY.ordinal());
			data.writeShort(OPEN_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ABILITY_TIME_OUT.ordinal());
			data.writeShort(OPEN_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ADD_ABILITY.ordinal());
			data.writeShort(NORMAl_SHIELD);
			data.writeInt(((Fighter)source).getUid());
		}else if(type==MISS_SHIELD_TIME_OUT_TYPE){//护盾消失 移除技能
			//System.out.println("吸收护盾被打破");
			data.writeByte(FightEvent.ABILITY_TIME_OUT.ordinal());
			data.writeShort(NORMAl_SHIELD);
			data.writeInt(((Fighter)source).getUid());
		}else if(type==MISS_SHIELD_TYPE){//护盾吸收  移除普通护盾-》加载吸收效果-》移除吸收效果-》加载普通护盾
			data.writeByte(FightEvent.ABILITY_TIME_OUT.ordinal());
			data.writeShort(NORMAl_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ADD_ABILITY.ordinal());
			data.writeShort(MISS_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ABILITY_TIME_OUT.ordinal());
			data.writeShort(MISS_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ADD_ABILITY.ordinal());
			data.writeShort(NORMAl_SHIELD);
			data.writeInt(((Fighter)source).getUid());
		}else if(type==RESET_SHIEL_TYPE){//护盾反伤 移除普通护盾-》加载反伤效果-》移除反伤效果-》加载普通护盾
			data.writeByte(FightEvent.ABILITY_TIME_OUT.ordinal());
			data.writeShort(RETURN_NORMAl_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ADD_ABILITY.ordinal());
			data.writeShort(RESET_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ABILITY_TIME_OUT.ordinal());
			data.writeShort(RESET_SHIELD);
			data.writeInt(((Fighter)source).getUid());
			data.writeByte(FightEvent.ADD_ABILITY.ordinal());
			data.writeShort(RETURN_NORMAl_SHIELD);
			data.writeInt(((Fighter)source).getUid());
		}else if(type==RETURN_HURT_TYPE){//反伤效果 前端特殊处理
			data.writeByte(type);
			data.writeInt((Integer)v1);
			data.writeInt(((Fighter)source).getUid());
			int num=((Integer)v2).intValue();
			data.writeShort(num-(int)((Fighter)source).getAttrValue(PublicConst.SHIP_NUM));
		}
	}
	
	
	public void change(Object source,int type,Object v1,Object v2)
	{
		if(type==FightEvent.SPREAD_OVER.ordinal()&&source instanceof Spread)
		{
			data.writeByte(type);
		}
		// else if(type==FightEvent.HURT.ordinal()
		// &&source instanceof BaseAttackFormula)
		// {
		// //
		// System.out.println("伤害 type:"+FightEvent.HURT.ordinal()+" hurt:"
		// // +(Integer)v1+" fighterUid:"+((Fighter)v2).getUid()
		// // +" shipNum:"
		// // +(int)((Fighter)v2).getAttrValue(PublicConst.SHIP_NUM));
		// data.writeByte(type);
		// data.writeInt((Integer)v1);
		// Fighter target=(Fighter)v2;
		// data.writeInt(target.getUid());
		// data.writeShort((int)target.getAttrValue(PublicConst.SHIP_NUM));
		// // (int)target.getAttrValue(PublicConst.SHIP_NUM)
		// }
		// else if(type==FightEvent.ERUPT.ordinal()
		// &&source instanceof BaseAttackFormula)
		// {
		// // System.out.println("暴击 type:"+FightEvent.ERUPT.ordinal()
		// // +" hurt:"+(Integer)v1+" fighterUid:"+((Fighter)v2).getUid()
		// // +" shipNum:"
		// // +(int)((Fighter)v2).getAttrValue(PublicConst.SHIP_NUM));
		// data.writeByte(type);
		// data.writeInt((Integer)v1);
		// Fighter target=(Fighter)v2;
		// data.writeInt(target.getUid());
		// data.writeShort((int)target.getAttrValue(PublicConst.SHIP_NUM));
		// }
		else if(type==FightEvent.SPREAD_START.ordinal()
			&&source instanceof Spread)
		{
			//System.out.println("释放技能 type:"
			//+FightEvent.SPREAD_START.ordinal()+" 技能sid:"
			//+((Ability)v2).getSid());
			data.writeByte(type);
			data.writeShort(((Ability)v2).getSid());
			// Fighter f=(Fighter)v1;
			// if(f.getTarget() instanceof Fighter)
			// {
			// // data.writeByte(1);
			// //
			// System.out.println("目标:"+((Fighter)f.getTarget()).getLocation());
			// // data.writeInt(((Fighter)f.getTarget()).getUid());
			// }
			// else
			// {
			// Fighter[] targets=(Fighter[])f.getTarget();
			// // int top=data.top();
			// // int length=0;
			// // data.writeByte(0);
			// for(int i=0;i<targets.length;i++)
			// {
			// if(targets[i]!=null)
			// {
			// // System.out.println("目标:"+targets[i].getLocation());
			// // data.writeInt(targets[i].getUid());
			// // length++;
			// }
			// }
			// // if(length>0)
			// // {
			// // int current=data.top();
			// // data.setTop(top);
			// // data.writeByte(length);
			// // data.setTop(current);
			// // }
			// }
		}
		// listener.change(this,FightScene.FightEvent.EXEMPT.ordinal(),target,ability);
		else if(type==FightEvent.EXEMPT.ordinal()
			&&source instanceof BaseAttackFormula)
		{
			//System.out.println("闪避:"+((Ability)v2).getSid());
			data.writeByte(type);
			data.writeInt(((Fighter)v1).getUid());
		}
		// else if(type==FightEvent.SKII_SPRING.ordinal()
		// &&source instanceof IntervalChangeEffect)
		// {
		// IntervalChangeEffect effect=(IntervalChangeEffect)source;
		// Fighter fighter=(Fighter)v1;
		// Integer value=(Integer)v2;
		// // System.out.println("脉冲技能 sid:"+effect.getAbility().getSid()
		// // +" fighterUid:"+fighter.getUid()+" 脉冲数值:"
		// // +Math.abs(value.intValue())+" shipNum:"
		// // +(int)fighter.getAttrValue(PublicConst.SHIP_NUM));
		// data.writeByte(type);
		// data.writeShort(effect.getAbility().getSid());
		// data.writeInt(fighter.getUid());
		// data.writeInt(Math.abs(value.intValue()));
		// data.writeShort((int)fighter.getAttrValue(PublicConst.SHIP_NUM));
		// }
		// else if(type==FightEvent.INTERVAL_EFFECT.ordinal()
		// &&source instanceof AbilityList)
		// {
		// // System.out.println("INTERVAL_EFFECT");
		// data.writeByte(type);
		// data.writeInt(((Fighter)v1).getUid());
		// data.writeShort(((Ability)v2).getSid());
		// }
		// //
		// listener.change(this,FightEvent.ABILITY_TIME_OUT.ordinal(),self,abilitys[i]);
		else if(type==FightEvent.ABILITY_TIME_OUT.ordinal()
			&&source instanceof AbilityList)
		{
			//System.out.println("ABILITY_TIME_OUT:"+((Ability)v2).getSid()
			//+" fighterUid:"+((Fighter)v1).getUid());
			data.writeByte(type);
			data.writeShort(((Ability)v2).getSid());
			data.writeInt(((Fighter)v1).getUid());
		}
		else if(type==FightEvent.CLEAN_ABILITY.ordinal()
			&&source instanceof Fighter)
		{
			// 技能被手动移除
			data.writeByte(type);
			data.writeShort(((Ability)v2).getSid());
			data.writeInt(((Fighter)v1).getUid());
		}
		// else if(type==FightEvent.CLEAN_ABILITY.ordinal()
		// &&source instanceof AbilityList)
		// {
		// // System.out.println("CLEAN_ABILITY");
		// data.writeByte(type);
		// data.writeInt(((Fighter)v1).getUid());
		// data.writeShort(((Ability)v2).getSid());
		// }
	}
	public void change(Object source,int type,Object v1,Object v2,Object v3)
	{
		if(type==FightEvent.HURT.ordinal()
			&&source instanceof BaseAttackFormula)
		{
//			System.out.println("伤害 type:"+FightEvent.HURT.ordinal()+" hurt:"
//				+(Integer)v1+" fighterUid:"+((Fighter)v2).getUid()
//				+" shipNum:"
//				+(int)((Fighter)v2).getAttrValue(PublicConst.SHIP_NUM));
			data.writeByte(type);
			data.writeInt((Integer)v1);
			Fighter target=(Fighter)v2;
			data.writeInt(target.getUid());
			int num=((Integer)v3).intValue();
			data.writeShort(num
				-(int)target.getAttrValue(PublicConst.SHIP_NUM));
		}
		else if(type==FightEvent.ERUPT.ordinal()
			&&source instanceof BaseAttackFormula)
		{
//			System.out.println("暴击 type:"+FightEvent.ERUPT.ordinal()
//				+" hurt:"+(Integer)v1+" fighterUid:"+((Fighter)v2).getUid()
//				+" shipNum:"
//				+(int)((Fighter)v2).getAttrValue(PublicConst.SHIP_NUM));
			data.writeByte(type);
			data.writeInt((Integer)v1);
			Fighter target=(Fighter)v2;
			data.writeInt(target.getUid());
			int num=((Integer)v3).intValue();
			data.writeShort(num
				-(int)target.getAttrValue(PublicConst.SHIP_NUM));
		}
		else if(type==FightEvent.SKII_SPRING.ordinal()
			&&source instanceof IntervalChangeEffect)
		{
			IntervalChangeEffect effect=(IntervalChangeEffect)source;
			FleetFighter fighter=(FleetFighter)v1;
			int currentValue=((Integer)v3).intValue();
			//System.out.println("脉冲技能 sid:"+effect.getAbility().getSid()
			// +" fighterUid:"+fighter.getUid()+" 脉冲数值:"
			// +Math.abs(value.intValue())+" shipNum:"
			// +(int)fighter.getAttrValue(PublicConst.SHIP_NUM));
			data.writeByte(type);
			data.writeShort(effect.getAbility().getSid());
			data.writeInt(fighter.getUid());
			data.writeInt(Math.abs(((Integer)v2).intValue()));
			Fleet f=fighter.getFleet();
			int num=Fleet.compteShipNum(f,currentValue);
			//System.out.println("脉冲技能 sid:"+effect.getAbility().getSid()
			//	+" fighterUid:"+fighter.getUid()+" 脉冲数值:"
			//	+Math.abs(((Integer)v2).intValue())+" shipNum:"
			//	+(int)fighter.getAttrValue(PublicConst.SHIP_NUM)+" "
			//	+currentValue+" "
			//	+Math.ceil(currentValue*1.0/f.getShipLife())+" "
			//	+currentValue*1.0/f.getShipLife());
			data.writeShort(num
				-(int)fighter.getAttrValue(PublicConst.SHIP_NUM));

		}
	}
	
}