/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

package foxu.fight;

/**
 * ��˵����ֱ�ӹ�����Ч��
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class AttackEffect extends Effect
{

	/* fields */
	/** �˺�����:��Ϯ,�ڻ�,����,����,���� */
	int attackType;
	/** �˺�ֵ */
	float damage;

	/* properties */
	/** ����˺����ͣ��������� */
	public int getAttackType()
	{
		return attackType;
	}

	/* methods */
	/**
	 * ����˺�ֵ,���Ը���Ŀ��ĸ��������������˺�ֵ�ı仯
	 * 
	 * @param target �˺�Ŀ��
	 * @return ���Ը���Ŀ��
	 */
	public float getValue(Fighter target)
	{
		return damage;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[attackTtype="+attackType+", damage="
			+damage+"] ";
	}
}