/**
 * 
 */
package foxu.sea.fight;

import mustang.set.IntKeyHashMap;
import foxu.fight.Fighter;

/**
 * 目标选择规则
 * 
 * @author rockzyt
 */
public class TargetSelector
{

	/* static fields */
	public static int ROW_MAX=3;

	/* static methods */
	public static Object getTarget(Fighter src,TargetSelector s,
		IntKeyHashMap fighters)
	{
		if(s.projectA==null||s.projectA.length<=0)
		{
			Object[] objs=fighters.valueArray();
			Fighter[] f=new Fighter[objs.length];
			for(int i=f.length-1;i>=0;i--)
			{
				f[i]=((Fighter)objs[i]);
				if(!checkLimit(src,f[i],0,s)) f[i]=null;
			}
			return f;
		}
		else
		{
			if(s.projectA.length==1)
			{
				Fighter f=getFighter(src,src.getLocation(),s.projectA[0],
					s.projectB,fighters,s);
				if(s.otherProjectA==null||s.otherProjectA.length<=0)
				{
					return f;
				}
				else
				{
					if(f==null) return null;
					Fighter[] targets=new Fighter[s.otherProjectA.length+1];
					targets[0]=f;
					for(int i=1;i<targets.length;i++)
					{
						targets[i]=(Fighter)fighters.get(f.getLocation()+s.otherProjectA[i-1]);
					}
					return targets;
				}
			}
			else
			{
				Fighter[] targets=new Fighter[s.projectA.length];
				return getFighter(src,targets,0,s.projectA,s.projectB,
					fighters,s);
			}
		}
	}
	/**
	 * 获得一个指定的Fighter,备选目标为一组绝对坐标,3位一组,根据首选目标的所在列确定在第几组坐标取fighter
	 * 
	 * @param src 攻击者
	 * @param loc 攻击者的位置,如果不需要这个信息传0
	 * @param a 目标的查找规则,在有loc的情况下,a==0表示和src相同位置的人,+1,-1上一个,下一个
	 * @param b 备选目标查找规则,a规则无法找到目标话,就按照b规则继续查找
	 * @param fighters 用于目标查找的fighter集合
	 * @param s 描述规则
	 * @return 返回找到的Fighter
	 */
	public static Fighter getFighter(Fighter src,int loc,int a,int[] b,
		IntKeyHashMap fighters,TargetSelector s)
	{
		Fighter f=(Fighter)fighters.get(loc+a);
		if(f==null)
		{
			if(b==null||b.length<=0) return null;
			int col=loc%3;
			for(int i=ROW_MAX*col;i<b.length;i++)
			{
				f=(Fighter)fighters.get(b[i]);
				if(checkLimit(src,f,0,s)) return f;
			}
		}
		else
		{
			if(checkLimit(src,f,0,s)) return f;
		}
		return null;
	}
	/**
	 * 获得一组指定的Fighter,需要外部传入Fighter容器,备选目标都是首选目标的相对坐标
	 * 
	 * @param src 攻击者
	 * @param targets 目标容器
	 * @param index 容器起始坐标
	 * @param a 目标的查找规则,在有loc的情况下,a==0表示和src相同位置的人,+1,-1上一个,下一个
	 * @param b 备选目标查找规则,a规则无法找到目标话,就按照b规则继续查找
	 * @param fighters 用于目标查找的fighter集合
	 * @param s 描述规则
	 * @return 返回找到的Fighter
	 */
	public static Fighter[] getFighter(Fighter src,Fighter[] targets,int index,
		int[] a,int[] b,IntKeyHashMap fighters,TargetSelector s)
	{
		Fighter[] fs=new Fighter[a.length];
		Fighter f=null;
		for(int i=0,j=0;i<a.length;i++)
		{
			f=(Fighter)fighters.get(a[i]);
			if(checkLimit(src,f,0,s))
			{
				fs[i]=f;
			}
			else if(b!=null&&b.length>0)
			{
				for(j=0;j<b.length;j++)
				{
					f=(Fighter)fighters.get(a[i]+b[j]);
					if(checkLimit(src,f,0,s))
					{
						fs[i]=f;
						break;
					}
				}
			}
		}
		return fs;
	}
	/**
	 * 判断fighter是否符合限制条件
	 * 
	 * @param src 技能释放者
	 * @param target 找到的一个目标
	 * @param targetIndex 目标下标
	 * @param s 目标选择器
	 * @return 返回true标示目标符合条件
	 */
	public static boolean checkLimit(Fighter src,Fighter target,
		int targetIndex,TargetSelector s)
	{
		if(target==null) return false;
		if(target.isDead()) return false;
		if(s.typeLimit==null||s.typeLimit.length<=0) return true;
		if((target.getFighterType()&s.typeLimit[targetIndex])==0)
			return false;
		return true;
	}

	/* fields */
	// 按照文档规则一列3个Fighter
	/**
	 * 首选目标规则,配置偏移位置.举例:{0}标示和自己一样的位置,{3}列+1,{-3}列-1.如果有长度,默认每一个元素
	 * 都是目标的绝对位置,为null|| length==0,表示全体
	 */
	int[] projectA;
	/**
	 * 备选目标规则,当首选目标唯一时,备选目标为3位一组的多个绝对坐标.当首选目标为多个,备选目标都是每个首选目标的相对坐标
	 */
	int[] projectB;
	/**
	 * 额外目标的首选规则(首选目标的相对位置),长度表示有几个额外目标
	 */
	int[] otherProjectA;
	/** 额外目标备选规则,每个备选目标都会使用这个规则来选择Fighter */
	int[] otherProjectB;
	/**
	 * 目标类型限制.数组长度=1+额外目标个数.第一位是首选目标的类型限制,第二位是第一个额外目标的类型限制,以此类推.舰船类型常量见Ship类.
	 * 全类型限制0xffffffff
	 * ,具体类型就配置具体的常量值,某几种类型限定以2+4+8方式配置.如果前边的配置为全屏攻击状态,那么typeLimit长度只能为1
	 * ,所有目标都要满足这个条件
	 */
	int[] typeLimit;
}