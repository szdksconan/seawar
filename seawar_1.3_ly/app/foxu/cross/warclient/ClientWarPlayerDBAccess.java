package foxu.cross.warclient;

import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import shelby.dc.GameDBAccess;


/**
 * 跨服战玩家（客服端）
 * @author yw
 *
 */
public class ClientWarPlayerDBAccess extends GameDBAccess
{


	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(ClientWarPlayerDBAccess.class);
	
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
				"ClientWarPlayerDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	public ClientWarPlayer[] loadBySql(String sql)
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
				"ClientWarPlayerDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		ClientWarPlayer[] players=new ClientWarPlayer[array.length];
		for(int i=0;i<array.length;i++)
		{
			players[i]=mapping(array[i]);
		}
		return players;
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
				"CrossWarPlayerDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public ClientWarPlayer mapping(Fields fields)
	{
		ClientWarPlayer player=new ClientWarPlayer();
		int crossid=((IntField)fields.get("crossid")).value;
		player.setCrossid(crossid);
		int warid=((IntField)fields.get("warid")).value;
		player.setWarid(warid);
		int id=((IntField)fields.get("id")).value;
		player.setId(id);
		int sid=((IntField)fields.get("sid")).value;
		player.setSid(sid);//ADD
		int platid=((IntField)fields.get("platid")).value;
		player.setPlatid(platid);//ADD
		int areaid=((IntField)fields.get("areaid")).value;
		player.setAreaid(areaid);//ADD
		int serverid=((IntField)fields.get("serverid")).value;
		player.setServerid(serverid);//ADD
		String sname=((StringField)fields.get("sname")).value;
		player.setSname(sname);;//add
		String aname=((StringField)fields.get("aname")).value;
		player.setAname(aname);//add
		String national=((StringField)fields.get("national")).value;
		player.setNational(national);//add
		String name=((StringField)fields.get("name")).value;
		player.setName(name);
		int num=((IntField)fields.get("num")).value;
		player.setNum(num);
		int rank=((IntField)fields.get("rank")).value;
		player.setRank(rank);
		int fightscore=((IntField)fields.get("fightscore")).value;
		player.setFightscore(fightscore);
		int bet=((IntField)fields.get("bet")).value;
		player.setBet(bet);
		byte[] array=((ByteArrayField)fields.get("attacklist")).value;
		if(array!=null&&array.length>0)
			player.bytesReadAttacklist(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("defencelist")).value;
		if(array!=null&&array.length>0)
			player.bytesReadDefencelist(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("officer")).value;
		if(array!=null&&array.length>0)
			player.bytesReadOFS(new ByteBuffer(array));

		return player;
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object player)
	{
		if(player==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(
			FieldKit.create("crossid",((ClientWarPlayer)player).getCrossid()),
			mapping(player));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+player);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object player)
	{
		ClientWarPlayer p=(ClientWarPlayer)player;
		FieldObject[] array=new FieldObject[18];
		int i=0;
//		array[i++]=FieldKit.create("crossid",p.getCrossid());
		array[i++]=FieldKit.create("id",p.getId());
		array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("platid",p.getPlatid());
		array[i++]=FieldKit.create("areaid",p.getAreaid());
		array[i++]=FieldKit.create("serverid",p.getServerid());
		array[i++]=FieldKit.create("sname",p.getSname());
		array[i++]=FieldKit.create("aname",p.getAname());
		array[i++]=FieldKit.create("national",p.getNational());
		array[i++]=FieldKit.create("name",p.getName());
		array[i++]=FieldKit.create("warid",p.getWarid());
		array[i++]=FieldKit.create("rank",p.getRank());
		array[i++]=FieldKit.create("num",p.getNum());
		array[i++]=FieldKit.create("fightscore",p.getFightscore());
		array[i++]=FieldKit.create("bet",p.getBet());
		
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		p.bytesWriteAttacklist(bb);
		array[i++]=FieldKit.create("attacklist",bb.toArray());
		
		bb.clear();
		p.bytesWriteDefencelist(bb);
		array[i++]=FieldKit.create("defencelist",bb.toArray());
		
		bb.clear();
		p.bytesWriteOFS(bb);
		array[i++]=FieldKit.create("officer",bb.toArray());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}



}
