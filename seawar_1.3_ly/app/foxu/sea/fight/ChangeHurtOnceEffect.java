/**
 * 
 */
package foxu.sea.fight;

import mustang.set.ArrayList;
import foxu.fight.ChangeHurtEffect;
import foxu.fight.Fighter;

/**
 * @author rockzyt �ı�һ���˺�,���Ƴ��ü���
 */
public class ChangeHurtOnceEffect extends ChangeHurtEffect
{

	/* fields */
	/** Ŀ�껺�� ��Ŀ��Ϊ���˵�ʱ��,��Ҫ�����Ŀ�궼������ɺ���Ƴ��������.ÿ����һ��Ŀ�껺��һ��,�жϻ����������ѡ��Ŀ��������Ƿ���� */
	ArrayList targetsCache;

	/* methods */
	public float used(Fighter source,Object target,float data,int type)
	{
		if(targetsCache==null) targetsCache=new ArrayList();
		float v=super.used(source,target,data,type);
		if(type==getHurtTime())
		{
			//���Ŀ��Ϊ����,����һ�κ���Ƴ��ü���
			//���source��Ŀ��Ϊ��,��ʾsource���ڱ�����,Ŀǰ�����ڶ����ɫͬʱ����ĳһĿ������.�����ж����ͬʱ����һ����,Ҳֻ����һ���˺��ı�
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