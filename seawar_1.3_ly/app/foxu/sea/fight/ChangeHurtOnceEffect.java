/**
 * 
 */
package foxu.sea.fight;

import mustang.set.ArrayList;
import foxu.fight.ChangeHurtEffect;
import foxu.fight.Fighter;

/**
 * @author rockzyt 改变一次伤害,就移除该技能
 */
public class ChangeHurtOnceEffect extends ChangeHurtEffect
{

	/* fields */
	/** 目标缓存 当目标为多人的时候,需要将多个目标都计算完成后才移除这个技能.每计算一个目标缓存一个,判断缓存的数量和选择目标的数量是否相等 */
	ArrayList targetsCache;

	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		if(targetsCache==null) targetsCache=new ArrayList();
		float v=super.used(source,target,data,type);
		if(type==getHurtTime())
		{
			//如果目标为单人,计算一次后就移除该技能
			//如果source的目标为空,表示source正在被攻击,目前不存在多个角色同时攻击某一目标的情况.就算有多个人同时攻击一个人,也只计算一次伤害改变
			if(source.getTarget()==null||source.getTarget() instanceof Fighter)
			{
				source.removeAbility(getAbility());
			}
			else
			{
				targetsCache.add(target);
				if(targetsCache.size()>=((Fighter[])source.getTarget()).length)
				{
					targetsCache.clear();
					source.removeAbility(getAbility());
				}
			}
		}
		return v;
	}
}