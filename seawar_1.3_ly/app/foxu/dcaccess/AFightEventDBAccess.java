package foxu.dcaccess;

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
import foxu.sea.alliance.alliancefight.AllianceFightEvent;
import shelby.dc.GameDBAccess;


public class AFightEventDBAccess extends GameDBAccess
{
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(AFightEventDBAccess.class);
	
	@Override
	public void delete(Object message)
	{
		if(message==null)return;
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		int id=((AllianceFightEvent)message).getUid();
		String sql="delete from afightevent where uid="+id;
		try
		{
			SqlKit.execute(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"AFightEventDBAccess delete valid, db error");
		}
	}
	@Override
	public boolean isDelete(Object message)
	{
		if(message==null)return false;
		return ((AllianceFightEvent)message).getDeleteCount()<=0;
	}
	
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object event)
	{
		if(event==null) return false;
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("uid",((AllianceFightEvent)event)
			.getUid()),mapping(event));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+event);
		return t==Persistence.OK||t==Persistence.ADD;
	}

//	/** 映射成域对象 */
//	public Fields mapping()
//	{
//		FieldObject[] array=new FieldObject[7];
//		int i=0;
//		array[i++]=FieldKit.create("allianceId",0);
//		array[i++]=FieldKit.create("reinforce",0);
//		array[i++]=FieldKit.create("horn",(byte[])null);
//		array[i++]=FieldKit.create("fleets",(byte[])null);
//		array[i++]=FieldKit.create("records",(byte[])null);
//		array[i++]=FieldKit.create("dinationMap",(byte[])null);
//		array[i++]=FieldKit.create("fightEvent",(byte[])null);
//		Fields fs=new Fields();
//		fs.add(array,0,i);
//		return fs;
//	}

	/** 映射成域对象 */
	public Fields mapping(Object event)
	{
		AllianceFightEvent ev=(AllianceFightEvent)event;
		FieldObject[] array=new FieldObject[12];
		int i=0;
		array[i++]=FieldKit.create("uid",ev.getUid());
		array[i++]=FieldKit.create("deleteCount",ev.getDeleteCount());
		array[i++]=FieldKit.create("type",ev.getType());
		array[i++]=FieldKit.create("battleId",ev.getBattleId());
		array[i++]=FieldKit.create("createTime",ev.getCreateTime());
		array[i++]=FieldKit.create("attackId",ev.getAttackId());
		array[i++]=FieldKit.create("defId",ev.getDefId());
		array[i++]=FieldKit.create("addValue",ev.getAddValue());
		array[i++]=FieldKit.create("skillsid",ev.getSkillsid());
		array[i++]=FieldKit.create("aName",ev.getaName());
		array[i++]=FieldKit.create("dcrHorn",ev.getDcrHorn());
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		ev.bytesWriteFightdata(bb);
		array[i++]=FieldKit.create("fightData",bb.toArray());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public AllianceFightEvent mapping(Fields fields)
	{
		AllianceFightEvent event=new AllianceFightEvent();
		int value=((IntField)fields.get("uid")).value;
		event.setUid(value);
		value=((IntField)fields.get("deleteCount")).value;
		event.setDeleteCount(value);
		value=((IntField)fields.get("type")).value;
		event.setType(value);
		value=((IntField)fields.get("battleId")).value;
		event.setBattleId(value);
		value=((IntField)fields.get("createTime")).value;
		event.setCreateTime(value);
		value=((IntField)fields.get("attackId")).value;
		event.setAttackId(value);
		value=((IntField)fields.get("defId")).value;
		event.setDefId(value);
		value=((IntField)fields.get("addValue")).value;
		event.setAddValue(value);
		value=((IntField)fields.get("skillsid")).value;
		event.setSkillsid(value);
		value=((IntField)fields.get("dcrHorn")).value;
		event.setDcrHorn(value);
		
		String aname=((StringField)fields.get("aName")).value;
		event.setaName(aname);
		
		byte[] array=((ByteArrayField)fields.get("fightData")).value;
		if(array!=null&&array.length>0)
			event.bytesReadFightdata(new ByteBuffer(array));


		return event;
	}
	
	/** 加载所有盟战 */
	public AllianceFightEvent[] loadBySql(String sql)
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
				"AFightEventDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		AllianceFightEvent[] event=new AllianceFightEvent[array.length];
		for(int i=0;i<array.length;i++)
		{
			event[i]=mapping(array[i]);
		}
		return event;
	}

}
