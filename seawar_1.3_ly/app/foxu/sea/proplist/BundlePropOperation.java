package foxu.sea.proplist;

import mustang.net.Session;
import foxu.sea.Player;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */


/**
 * 类说明：包裹物品操作类
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public class BundlePropOperation implements PropOperation
{
	/* methods */
	/** 检测指定位置的物品 返回该物品的副本 */
	public Prop check(Session session,int index)
	{
		Player p=(Player)session.getSource();
		if(p==null)
			throw new IllegalArgumentException(getClass().getName()
				+" get, null session source");
		return p.getBundle().getIndex(index);
	}

	/** 获得指定位置物品的指定数量 */
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

	/** 拿起指定位置的物品 */
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

	/** 拿起指定位置物品的指定数量 */
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

	/** 检查是否能增加物品到指定的位置，返回是否成功 */
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

	/** 增加物品到指定的空位置，返回是否成功 */
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
	/**检查前后拿起的物品是否和后台的一致*/
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

	/** 刷新 TODO*/
	public void flush(Session session)
	{
//		JBackKit.resetPlayerBundle(session,new ByteBuffer());
	}

	public String toString()
	{
		return "BundlePropOperation";
	}
}