package foxu.sea.proplist;

import mustang.net.Session;
import foxu.sea.Player;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */


/**
 * ��˵����������Ʒ������
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class BundlePropOperation implements PropOperation
{
	/* methods */
	/** ���ָ��λ�õ���Ʒ ���ظ���Ʒ�ĸ��� */
	public Prop check(Session session,int index)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" get, null session source");
		return p.getBundle().getIndex(index);
	}

	/** ���ָ��λ����Ʒ��ָ������ */
	public Prop check(Session session,int index,int count)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" get, null session source");
		Prop prop = p.getBundle().getIndex(index);
		if(prop instanceof NormalProp)
		{
			NormalProp np=null;
			np=NormalProp.newNormalProp(prop.getSid(),count);
			return np;
		}
		return prop;
	}

	/** ����ָ��λ�õ���Ʒ */
	public Prop remove(Session session,int index)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" remove, null session source");
//		if(p.getSprite().isBusiness())
//			throw new IllegalArgumentException(getClass().getName()
//				+"player in business can't remove");
		return p.getBundle().removeProp(index);
	}

	/** ����ָ��λ����Ʒ��ָ������ */
	public Prop remove(Session session,int index,int count)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" remove, null session source");
//		if(p.getSprite().isBusiness())
//			throw new IllegalArgumentException(getClass().getName()
//				+"player in business can't remove");
		return p.getBundle().removeProp(index,count);
	}

	/** ����Ƿ���������Ʒ��ָ����λ�ã������Ƿ�ɹ� */
	public boolean checkAdd(Session session,Prop prop,int index,
		boolean autoCombine)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" checkAdd, null session source");
//		if(p.getSprite().isBusiness())
//			return false;
		if(index>p.getBundle().getLength()-1)
			return false;
		if(index>=0)
		{
			return p.getBundle().checkAddProp(prop,index)>=0;
		}
		return p.getBundle().checkIncrProp(prop,autoCombine);
	}

	/** ������Ʒ��ָ���Ŀ�λ�ã������Ƿ�ɹ� */
	public boolean add(Session session,Prop prop,int index,
		boolean autoCombine)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" add, null session source");
//		if(p.getSprite().isBusiness())
//			return false;
		if(index>p.getBundle().getLength()-1)
			return false;
		if(index>=0) return p.getBundle().addProp(prop,index)==0;
		return p.getBundle().incrProp(prop,autoCombine);
	}
	/**���ǰ���������Ʒ�Ƿ�ͺ�̨��һ��*/
	public boolean checkPropId(Session session,int index,long propid)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" add, null session source");
		Prop prop = p.getBundle().getIndex(index);
		if(prop==null&&propid!=0)return false;
		if(prop!=null)
		return prop.getId()==propid;
		return true;
	}

	/** ˢ�� TODO*/
	public void flush(Session session)
	{
//		JBackKit.resetPlayerBundle(session,new ByteBuffer());
	}

	public String toString()
	{
		return "BundlePropOperation";
	}
}