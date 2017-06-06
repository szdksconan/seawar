package foxu.sea.proplist;

import mustang.net.Session;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */


/**
 * ��˵������Ʒ������
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public interface PropOperation
{

	/* methods */
	/** ���ָ��λ�õ���Ʒ�ĸ��� */
	public Prop check(Session session,int index);
	/** ���ָ��λ����Ʒ��ָ��������Ʒ�ĸ��� */
	public Prop check(Session session,int index,int count);
	/** ����ָ��λ�õ���Ʒ */
	public Prop remove(Session session,int index);
	/** ����ָ��λ����Ʒ��ָ������ */
	public Prop remove(Session session,int index,int count);
	/** ����Ƿ���������Ʒ��ָ���Ŀ�λ�ã������Ƿ�ɹ� */
	// !!! ����ʹ�÷���int
	public boolean checkAdd(Session session,Prop prop,int index,
		boolean autoCombine);
	/**���ǰ̨�������Ʒ�Ƿ�ͺ�̨��һ��*/
	public boolean checkPropId(Session session,int index,long propid);
	/** ������Ʒ��ָ���Ŀ�λ�ã������Ƿ�ɹ� */
	// !!! ����ʹ�÷���int
	public boolean add(Session session,Prop prop,int index,
		boolean autoCombine);
	/** ˢ�� */
	public void flush(Session session);
}