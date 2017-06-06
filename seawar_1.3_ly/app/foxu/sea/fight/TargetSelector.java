/**
 * 
 */
package foxu.sea.fight;

import mustang.set.IntKeyHashMap;
import foxu.fight.Fighter;

/**
 * Ŀ��ѡ�����
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
	 * ���һ��ָ����Fighter,��ѡĿ��Ϊһ���������,3λһ��,������ѡĿ���������ȷ���ڵڼ�������ȡfighter
	 * 
	 * @param src ������
	 * @param loc �����ߵ�λ��,�������Ҫ�����Ϣ��0
	 * @param a Ŀ��Ĳ��ҹ���,����loc�������,a==0��ʾ��src��ͬλ�õ���,+1,-1��һ��,��һ��
	 * @param b ��ѡĿ����ҹ���,a�����޷��ҵ�Ŀ�껰,�Ͱ���b�����������
	 * @param fighters ����Ŀ����ҵ�fighter����
	 * @param s ��������
	 * @return �����ҵ���Fighter
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
	 * ���һ��ָ����Fighter,��Ҫ�ⲿ����Fighter����,��ѡĿ�궼����ѡĿ����������
	 * 
	 * @param src ������
	 * @param targets Ŀ������
	 * @param index ������ʼ����
	 * @param a Ŀ��Ĳ��ҹ���,����loc�������,a==0��ʾ��src��ͬλ�õ���,+1,-1��һ��,��һ��
	 * @param b ��ѡĿ����ҹ���,a�����޷��ҵ�Ŀ�껰,�Ͱ���b�����������
	 * @param fighters ����Ŀ����ҵ�fighter����
	 * @param s ��������
	 * @return �����ҵ���Fighter
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
	 * �ж�fighter�Ƿ������������
	 * 
	 * @param src �����ͷ���
	 * @param target �ҵ���һ��Ŀ��
	 * @param targetIndex Ŀ���±�
	 * @param s Ŀ��ѡ����
	 * @return ����true��ʾĿ���������
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
	// �����ĵ�����һ��3��Fighter
	/**
	 * ��ѡĿ�����,����ƫ��λ��.����:{0}��ʾ���Լ�һ����λ��,{3}��+1,{-3}��-1.����г���,Ĭ��ÿһ��Ԫ��
	 * ����Ŀ��ľ���λ��,Ϊnull|| length==0,��ʾȫ��
	 */
	int[] projectA;
	/**
	 * ��ѡĿ�����,����ѡĿ��Ψһʱ,��ѡĿ��Ϊ3λһ��Ķ����������.����ѡĿ��Ϊ���,��ѡĿ�궼��ÿ����ѡĿ����������
	 */
	int[] projectB;
	/**
	 * ����Ŀ�����ѡ����(��ѡĿ������λ��),���ȱ�ʾ�м�������Ŀ��
	 */
	int[] otherProjectA;
	/** ����Ŀ�걸ѡ����,ÿ����ѡĿ�궼��ʹ�����������ѡ��Fighter */
	int[] otherProjectB;
	/**
	 * Ŀ����������.���鳤��=1+����Ŀ�����.��һλ����ѡĿ�����������,�ڶ�λ�ǵ�һ������Ŀ�����������,�Դ�����.�������ͳ�����Ship��.
	 * ȫ��������0xffffffff
	 * ,�������;����þ���ĳ���ֵ,ĳ���������޶���2+4+8��ʽ����.���ǰ�ߵ�����Ϊȫ������״̬,��ôtypeLimit����ֻ��Ϊ1
	 * ,����Ŀ�궼Ҫ�����������
	 */
	int[] typeLimit;
}