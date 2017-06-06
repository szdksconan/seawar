package foxu.sea.proplist;

import mustang.net.Session;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */


/**
 * 类说明：物品操作类
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */

public interface PropOperation
{

	/* methods */
	/** 获得指定位置的物品的副本 */
	public Prop check(Session session,int index);
	/** 获得指定位置物品的指定数量物品的副本 */
	public Prop check(Session session,int index,int count);
	/** 拿起指定位置的物品 */
	public Prop remove(Session session,int index);
	/** 拿起指定位置物品的指定数量 */
	public Prop remove(Session session,int index,int count);
	/** 检查是否能增加物品到指定的空位置，返回是否成功 */
	// !!! 考虑使用返回int
	public boolean checkAdd(Session session,Prop prop,int index,
		boolean autoCombine);
	/**检查前台拿起的物品是否和后台的一致*/
	public boolean checkPropId(Session session,int index,long propid);
	/** 增加物品到指定的空位置，返回是否成功 */
	// !!! 考虑使用返回int
	public boolean add(Session session,Prop prop,int index,
		boolean autoCombine);
	/** 刷新 */
	public void flush(Session session);
}