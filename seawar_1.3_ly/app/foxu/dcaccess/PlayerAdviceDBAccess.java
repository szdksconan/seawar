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
import foxu.sea.PlayerAdvice;

/** 玩家建议 BUG提交 */
public class PlayerAdviceDBAccess extends GameDBAccess
{
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(MessageGameDBAccess.class);

	public PlayerAdvice load(String id)
	{
		// 构造一个空域（包括了player表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"PlayerAdviceDBAccess db error");
		// 将存有玩家数据的域对象封装成一个玩家对象
		PlayerAdvice advice=mapping(fields);
		return advice;
	}

	public PlayerAdvice[] loadBySql(String sql)
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
				"PlayerAdviceDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		PlayerAdvice[] advice=new PlayerAdvice[array.length];
		for(int i=0;i<array.length;i++)
		{
			advice[i]=mapping(array[i]);
		}
		return advice;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[8];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("playerId",0);
		array[i++]=FieldKit.create("creatTime",0);
		array[i++]=FieldKit.create("state",0);

		array[i++]=FieldKit.create("titile",(String)null);
		array[i++]=FieldKit.create("content",(String)null);
		array[i++]=FieldKit.create("playerName",(String)null);
		array[i++]=FieldKit.create("gm_response",(String)null);

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public PlayerAdvice mapping(Fields fields)
	{
		PlayerAdvice advice=new PlayerAdvice();
		int id=((IntField)fields.get("id")).value;
		advice.setId(id);
		int playerId=((IntField)fields.get("playerId")).value;
		advice.setPlayerId(playerId);
		int creatTime=((IntField)fields.get("creatTime")).value;
		advice.setCreatTime(creatTime);
		int state=((IntField)fields.get("state")).value;
		advice.setState(state);

		String titile=((StringField)fields.get("titile")).value;
		advice.setTitile(titile);
		String content=((StringField)fields.get("content")).value;
		advice.setContent(content);
		String playerName=((StringField)fields.get("playerName")).value;
		advice.setPlayerName(playerName);
		String gm_response=((StringField)fields.get("gm_response")).value;
		advice.setGmResponse(gm_response);
		return advice;
	}

	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object advice)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+advice.toString());
		try
		{
			return save(advice);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object advice)
	{
		if(advice==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",
			((PlayerAdvice)advice).getId()),mapping(advice));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+advice);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	public boolean isDelete(Object advice)
	{
		if(advice==null) return false;
		PlayerAdvice ad=(PlayerAdvice)advice;
		return ad.getState()==1;
	}

	/** 删除方法 */
	public void delete(Object advice)
	{
		if(advice==null) return;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.delete(FieldKit.create("id",
			((PlayerAdvice)advice).getId()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+advice);
	}

	/** 映射成域对象 */
	public Fields mapping(Object a)
	{
		PlayerAdvice advice=(PlayerAdvice)a;
		FieldObject[] array=new FieldObject[8];
		int i=0;
		// array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",advice.getId());
		array[i++]=FieldKit.create("playerId",advice.getPlayerId());
		array[i++]=FieldKit.create("creatTime",advice.getCreatTime());
		array[i++]=FieldKit.create("state",advice.getState());
		array[i++]=FieldKit.create("titile",advice.getTitile());
		array[i++]=FieldKit.create("content",advice.getContent());
		array[i++]=FieldKit.create("playerName",advice.getPlayerName());
		array[i++]=FieldKit.create("gm_response",advice.getGmResponse());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
}
