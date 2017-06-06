package foxu.dcaccess;

import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;
import foxu.sea.order.Order;

/**
 * 邮件加载器 author:icetiger
 */
public class OrderGameDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(OrderGameDBAccess.class);

	public Order load(String id)
	{
		// 构造一个空域（包括了player表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"OrderGameDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		Order order=mapping(fields);
		return order;
	}

	/** 删除方法 */
	public void delete(Object order)
	{
		if(order==null) return;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.delete(FieldKit.create("id",((Order)order)
			.getId()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+order);
	}

	/** 获取查找的记录长度 */
	public int loadByLength(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			array=SqlKit.querys(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"OrderGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return 0;
		return array.length;
	}

	public Fields loadSql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields array=null;
		try
		{
			array=SqlKit.query(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"OrderGameDBAccess loadBysql valid, db error");
		}
		return array;
	}

	public Fields[] loadSqls(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			array=SqlKit.querys(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"OrderGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	public Order[] loadBySql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			array=SqlKit.querys(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"OrderGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		Order[] order=new Order[array.length];
		for(int i=0;i<array.length;i++)
		{
			order[i]=mapping(array[i]);
		}
		return order;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[20];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("money",0);
		array[i++]=FieldKit.create("gems",0);
		array[i++]=FieldKit.create("user_id",0);
		array[i++]=FieldKit.create("order_state",0);
		array[i++]=FieldKit.create("create_at",0);
		array[i++]=FieldKit.create("player_level",0);
		array[i++]=FieldKit.create("ios_type",0);
		array[i++]=FieldKit.create("transaction_id",(String)null);

		array[i++]=FieldKit.create("user_name",(String)null);
		array[i++]=FieldKit.create("verifyInfo",(String)null);
		array[i++]=FieldKit.create("device",(String)null);
		array[i++]=FieldKit.create("dataSignature",(String)null);
		array[i++]=FieldKit.create("udid",(String)null);
		array[i++]=FieldKit.create("idfa",(String)null);
		array[i++]=FieldKit.create("ip",(String)null);
		array[i++]=FieldKit.create("purchaseInfo",(String)null);
		array[i++]=FieldKit.create("plat_id",(String)null);
		array[i++]=FieldKit.create("pdid",(String)null);
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public Order mapping(Fields fields)
	{
		int sampleId=((IntField)fields.get("sid")).value;
		Order order=(Order)Order.factory.newSample(sampleId);
		int id=((IntField)fields.get("id")).value;
		order.setId(id);
		int money=((IntField)fields.get("money")).value;
		order.setMoney(money);
		int gems=((IntField)fields.get("gems")).value;
		order.setGems(gems);
		int userId=((IntField)fields.get("user_id")).value;
		order.setUserId(userId);
		String userName=((StringField)fields.get("user_name")).value;
		order.setUserName(userName);
		String verifyInfo=((StringField)fields.get("verifyInfo")).value;
		order.setVerifyInfo(verifyInfo);
		int orderState=((IntField)fields.get("order_state")).value;
		order.setOrderState(orderState);
		int createAt=((IntField)fields.get("create_at")).value;
		order.setCreateAt(createAt);
		int playerLevel=((IntField)fields.get("player_level")).value;
		order.setPlayerLevel(playerLevel);
		String transaction_id=((StringField)fields.get("transaction_id")).value;
		order.setTransaction_id(transaction_id);
		int iosType=((IntField)fields.get("ios_type")).value;
		order.setIosType(iosType);
		String device=((StringField)fields.get("device")).value;
		order.setDevice(device);
		String dataSignature=((StringField)fields.get("dataSignature")).value;
		order.setDataSignature(dataSignature);
		String udid=((StringField)fields.get("udid")).value;
		order.setUdid(udid);
		String idfa=((StringField)fields.get("idfa")).value;
		order.setIdfa(idfa);
		String ip=((StringField)fields.get("ip")).value;
		order.setIp(ip);
		String purchaseInfo=((StringField)fields.get("purchaseInfo")).value;
		order.setPurchaseInfo(purchaseInfo);
		String plat_id=((StringField)fields.get("plat_id")).value;
		order.setPlat_id(plat_id);
		String pdid=((StringField)fields.get("pdid")).value;
		order.setPdid(pdid);
		
		return order;
	}

	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object order)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+order.toString());
		try
		{
			return save(order);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object order)
	{
		if(order==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",((Order)order)
			.getId()),mapping(order));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+order);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object o)
	{
		Order order=(Order)o;
		FieldObject[] array=new FieldObject[20];
		int i=0;
		array[i++]=FieldKit.create("id",order.getId());
		array[i++]=FieldKit.create("money",order.getMoney());
		array[i++]=FieldKit.create("sid",order.getSid());
		array[i++]=FieldKit.create("gems",order.getGems());
		array[i++]=FieldKit.create("user_id",order.getUserId());
		array[i++]=FieldKit.create("user_name",order.getUserName());
		array[i++]=FieldKit.create("order_state",order.getOrderState());
		array[i++]=FieldKit.create("create_at",order.getCreateAt());
		array[i++]=FieldKit.create("player_level",order.getPlayerLevel());
		array[i++]=FieldKit.create("ios_type",order.getIosType());
		array[i++]=FieldKit.create("transaction_id",order
			.getTransaction_id());
		array[i++]=FieldKit.create("verifyInfo",order.getVerifyInfo());
		array[i++]=FieldKit.create("device",order.getDevice());
		array[i++]=FieldKit.create("dataSignature",order.getDataSignature());
		array[i++]=FieldKit.create("udid",order.getUdid());
		array[i++]=FieldKit.create("idfa",order.getIdfa());
		array[i++]=FieldKit.create("ip",order.getIp());
		array[i++]=FieldKit.create("purchaseInfo",order.getPurchaseInfo());
		array[i++]=FieldKit.create("plat_id",order.getPlat_id());
		array[i++]=FieldKit.create("pdid",order.getPdid());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
