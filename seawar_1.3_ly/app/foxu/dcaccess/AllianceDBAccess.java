package foxu.dcaccess;

import foxu.sea.alliance.Alliance;
import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.AttributeList;
import mustang.text.CharBuffer;
import mustang.text.CharBufferThreadLocal;
import mustang.text.TextKit;
import shelby.dc.GameDBAccess;

public class AllianceDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(AllianceDBAccess.class);

	/** 判断用户名是否存在 */
	public boolean isExist(Object key,int id)
	{
		Fields fields=mapping();
		int t=Persistence.EXCEPTION;
		t=getGamePersistence().get(FieldKit.create("name",(String)key),
			fields);
		if(t==Persistence.RESULTLESS) return false;
		if(t==Persistence.EXCEPTION) return true;
		return true;
	}

	public boolean isDelete(Object object,int id)
	{
		if(object==null) return false;
		Alliance ev=(Alliance)object;
		return ev.getMasterPlayerId()==0&&ev.getPlayerList().size()==0;
	}

	/** 删除方法 */
	public void delete(Object alliance)
	{
		if(alliance==null) return;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.delete(FieldKit.create("id",
			((Alliance)alliance).getId()));
		if(log.isInfoEnabled()) log.info("delete, "+t+" "+alliance);
	}

	public Alliance[] loadBySql(String sql)
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
				"AllianceDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		Alliance[] alliances=new Alliance[array.length];
		for(int i=0;i<array.length;i++)
		{
			alliances[i]=mapping(array[i]);
		}
		return alliances;
	}

	/** 保存方法 返回是否操作成功 */
	public boolean save(Object alliance)
	{
		if(alliance==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",((Alliance)alliance)
			.getId()),mapping(alliance));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+alliance);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[29];
		int i=0;
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("masterPlayerId",0);
		array[i++]=FieldKit.create("allFightScore",0);
		array[i++]=FieldKit.create("allianceLevel",0);
		array[i++]=FieldKit.create("create_at",0);
		array[i++]=FieldKit.create("allianceExp",0);
		array[i++]=FieldKit.create("material",0);
		array[i++]=FieldKit.create("sciencepoint",0);
		array[i++]=FieldKit.create("betislandsid",0);
		array[i++]=FieldKit.create("name",(String)null);
		array[i++]=FieldKit.create("description",(byte[])null);
		array[i++]=FieldKit.create("announcement",(byte[])null);
		array[i++]=FieldKit.create("attributes",(String)null);
		array[i++]=FieldKit.create("playerList",(byte[])null);
		array[i++]=FieldKit.create("eventList",(byte[])null);
		array[i++]=FieldKit.create("vicePlayers",(byte[])null);
		array[i++]=FieldKit.create("allianSkills",(byte[])null);
		array[i++]=FieldKit.create("applicationList",(byte[])null);
		array[i++]=FieldKit.create("boss",(byte[])null);
		array[i++]=FieldKit.create("locationList",(byte[])null);
		array[i++]=FieldKit.create("hostile",(byte[])null);
		array[i++]=FieldKit.create("materialrank",(byte[])null);
		array[i++]=FieldKit.create("givevalue",(byte[])null);
		array[i++]=FieldKit.create("luckyPoints",0);
		array[i++]=FieldKit.create("autoJoin",0);
		array[i++]=FieldKit.create("joinPlayerLevel",0);
		array[i++]=FieldKit.create("joinFightScore",0);
		array[i++]=FieldKit.create("luckyCreateAt",0);
		array[i++]=FieldKit.create("flag",(byte[])null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public Fields mapping(Object alliance)
	{
		Alliance a=(Alliance)alliance;
		FieldObject[] array=new FieldObject[29];
		int i=0;
		array[i++]=FieldKit.create("id",a.getId());
		array[i++]=FieldKit.create("masterPlayerId",a.getMasterPlayerId());
		array[i++]=FieldKit.create("allFightScore",a.getAllFightScore());
		array[i++]=FieldKit.create("allianceLevel",a.getAllianceLevel());
		array[i++]=FieldKit.create("allianceExp",a.getAllianceExp());
		array[i++]=FieldKit.create("create_at",a.getCreate_at());
		array[i++]=FieldKit.create("material",a.getMaterial());
		array[i++]=FieldKit.create("sciencepoint",a.getSciencepoint());
		array[i++]=FieldKit.create("betislandsid",a.getBetBattleIsland());
		array[i++]=FieldKit.create("luckyCreateAt",a.getLuckyCreateAt());
		String str=a.getName();
		if(str==null)
			array[i++]=FieldKit.create("name",(String)null);
		else
			array[i++]=FieldKit.create("name",str);

		CharBuffer cb=CharBufferThreadLocal.getCharBuffer();
		cb.clear();

		String[] strs=a.getAttributes();
		if(strs.length>0) SHARP2.encode(strs[0],cb);
		for(int j=1;j<strs.length;j++)
		{
			cb.append(':');
			SHARP2.encode(strs[j],cb);
		}
		array[i++]=FieldKit.create("attributes",cb.getString());
		cb.clear();

		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		a.bytesWritePlayerList(bb);
		array[i++]=FieldKit.create("playerList",bb.toArray());

		bb.clear();
		a.bytesWriteEventList(bb);
		array[i++]=FieldKit.create("eventList",bb.toArray());

		bb.clear();
		a.bytesWriteVicePlayersList(bb);
		array[i++]=FieldKit.create("vicePlayers",bb.toArray());

		bb.clear();
		a.bytesWriteSkills(bb);
		array[i++]=FieldKit.create("allianSkills",bb.toArray());

		bb.clear();
		a.bytesWriteApplicationList(bb);
		array[i++]=FieldKit.create("applicationList",bb.toArray());

		bb.clear();
		a.bytesWriteDes(bb);
		array[i++]=FieldKit.create("description",bb.toArray());
		
		bb.clear();
		a.bytesWriteAnn(bb);
		array[i++]=FieldKit.create("announcement",bb.toArray());
		
		bb.clear();
		a.bytesWriteBoss(bb);
		array[i++]=FieldKit.create("boss",bb.toArray());
		
		bb.clear();
		a.bytesWriteLocationSaveList(bb);
		array[i++]=FieldKit.create("locationList",bb.toArray());
		
		array[i++]=FieldKit.create("hostile",a.getHostile());
		
		bb.clear();
		a.bytesWriteMaterialValue(bb);
		array[i++]=FieldKit.create("materialrank",bb.toArray());
		bb.clear();
		a.bytesWriteGiveValue(bb);
		array[i++]=FieldKit.create("givevalue",bb.toArray());
		
		bb.clear();
		a.getFlag().bytesWrite(bb);
		array[i++]=FieldKit.create("flag",bb.toArray());
		
		array[i++]=FieldKit.create("luckyPoints",a.getLuckyPoints());
		array[i++]=FieldKit.create("autoJoin",a.getAutoJoin());
		array[i++]=FieldKit.create("joinPlayerLevel",a.getJoinPlayerLevel());
		array[i++]=FieldKit.create("joinFightScore",a.getJoinFightScore());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public Alliance mapping(Fields fields)
	{
		Alliance alliance=new Alliance();
		int allianceId=((IntField)fields.get("id")).value;
		alliance.setId(allianceId);
		int masterPlayerId=((IntField)fields.get("masterPlayerId")).value;
		alliance.setMasterPlayerId(masterPlayerId);
		int allFightScore=((IntField)fields.get("allFightScore")).value;
		alliance.setAllFightScore(allFightScore);
		int allianceLevel=((IntField)fields.get("allianceLevel")).value;
		alliance.setAllianceLevel(allianceLevel);
		int create_at=((IntField)fields.get("create_at")).value;
		alliance.setCreate_at(create_at);
		int allianceExp=((IntField)fields.get("allianceExp")).value;
		alliance.setAllianceExp(allianceExp);
		long material=((LongField)fields.get("material")).value;
		alliance.setMaterial(material);
		long sciencepoint=((LongField)fields.get("sciencepoint")).value;
		alliance.setSciencepoint(sciencepoint);
		int betislandsid=((IntField)fields.get("betislandsid")).value;
		alliance.setBetBattleIsland(betislandsid);
		CharBuffer cb=CharBufferThreadLocal.getCharBuffer();
		cb.clear();
		String str=((StringField)fields.get("attributes")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,':');
			for(int i=0;i<strs.length;i++)
			{
				cb.clear();
				if(SHARP2.decode(strs[i],cb)) strs[i]=cb.getString();
			}
			alliance.setAttributes(new AttributeList(strs));
		}

		str=((StringField)fields.get("name")).value;
		alliance.setName(str);

		byte[] array=((ByteArrayField)fields.get("playerList")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadPlayerList(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("eventList")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadEventList(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("vicePlayers")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadVicePlayersList(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("allianSkills")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadSkills(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("applicationList")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadApplicationList(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("description")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadDes(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("announcement")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadAnn(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("boss")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadBoss(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("locationList")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadLocationSaveList(new ByteBuffer(array));

		String hostile=((StringField)fields.get("hostile")).value;
		alliance.setHostile(hostile);
		
		array=((ByteArrayField)fields.get("materialrank")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadMaterialValue(new ByteBuffer(array));

		
		array=((ByteArrayField)fields.get("givevalue")).value;
		if(array!=null&&array.length>0)
			alliance.bytesReadGiveValue(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("flag")).value;
		if(array!=null&&array.length>0)
			alliance.getFlag().bytesRead(new ByteBuffer(array));
		
		int luckyPoints=((IntField)fields.get("luckyPoints")).value;
		alliance.setLuckyPoints(luckyPoints);
		int autoJoin=((IntField)fields.get("autoJoin")).value;
		alliance.setAutoJoin(autoJoin);
		int joinPlayerLevel=((IntField)fields.get("joinPlayerLevel")).value;
		alliance.setJoinPlayerLevel(joinPlayerLevel);
		int joinFightScore=((IntField)fields.get("joinFightScore")).value;
		alliance.setJoinFightScore(joinFightScore);
		
		int luckyCreateAt=((IntField)fields.get("luckyCreateAt")).value;
		alliance.setLuckyCreateAt(luckyCreateAt);
		
		return alliance;
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
				"AllianceDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
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
				"AllianceDBAccess loadBysql valid, db error");
		}
		return array;
	}
}
