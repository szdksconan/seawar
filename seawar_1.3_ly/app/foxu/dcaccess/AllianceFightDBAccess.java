package foxu.dcaccess;

import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import foxu.sea.alliance.alliancefight.AllianceFight;
import shelby.dc.GameDBAccess;


public class AllianceFightDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(AllianceFightDBAccess.class);
	
	@Override
	public void delete(Object message)
	{
		if(message==null)return;
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		int id=((AllianceFight)message).getAllianceID();
		String sql="delete from alliancefight where allianceId="+id;
		try
		{
			SqlKit.execute(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"AllianceFightDBAccess delete valid, db error");
		}
	}
	
	@Override
	public boolean isDelete(Object message)
	{
		if(message==null)return false;
		return ((AllianceFight)message).getDeleteCount()<=0;
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object allianceFight)
	{
		if(allianceFight==null) return false;
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("allianceId",((AllianceFight)allianceFight)
			.getAllianceID()),mapping(allianceFight));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+allianceFight);
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
	public Fields mapping(Object allianceFight)
	{
		AllianceFight a=(AllianceFight)allianceFight;
		FieldObject[] array=new FieldObject[10];
		int i=0;
		array[i++]=FieldKit.create("allianceId",a.getAllianceID());
		array[i++]=FieldKit.create("deleteCount",a.getDeleteCount());
		array[i++]=FieldKit.create("reinforce",a.getReinforce());
		array[i++]=FieldKit.create("dayCount",a.getDayCount());
		
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		a.bytesWriteHorn(bb);
		array[i++]=FieldKit.create("horn",bb.toArray());
		
		bb.clear();
		a.bytesWriteFleets(bb);
		array[i++]=FieldKit.create("fleets",bb.toArray());

		bb.clear();
		a.bytesWriteRecords(bb);
		array[i++]=FieldKit.create("records",bb.toArray());

		bb.clear();
		a.bytesWriteDinationMap(bb);
		array[i++]=FieldKit.create("dinationMap",bb.toArray());
		
		bb.clear();
		a.bytesWriteFightEvent(bb);
		array[i++]=FieldKit.create("fightEvent",bb.toArray());
		
		bb.clear();
		a.bytesWriteUpShip(bb);
		array[i++]=FieldKit.create("upship",bb.toArray());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public AllianceFight mapping(Fields fields)
	{
		AllianceFight allianceFight=new AllianceFight();
		int value=((IntField)fields.get("allianceId")).value;
		allianceFight.setAllianceID(value);
		value=((IntField)fields.get("deleteCount")).value;
		allianceFight.setDeleteCount(value);
		value=((IntField)fields.get("reinforce")).value;
		allianceFight.setReinforce(value);
		value=((IntField)fields.get("dayCount")).value;
		allianceFight.setDayCount(value);
		
		byte[] array=((ByteArrayField)fields.get("horn")).value;
		if(array!=null&&array.length>0)
			allianceFight.bytesReadHorn(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("fleets")).value;
		if(array!=null&&array.length>0)
			allianceFight.bytesReadFleets(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("records")).value;
		if(array!=null&&array.length>0)
			allianceFight.bytesReadRecords(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("dinationMap")).value;
		if(array!=null&&array.length>0)
			allianceFight.bytesReadDinationMap(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("fightEvent")).value;
		if(array!=null&&array.length>0)
			allianceFight.bytesReadFightEvent(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("upship")).value;
		if(array!=null&&array.length>0)
			allianceFight.bytesReadUpShip(new ByteBuffer(array));

		return allianceFight;
	}
	
	/** 加载所有盟战 */
	public AllianceFight[] loadBySql(String sql)
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
				"AllianceFightDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		AllianceFight[] allianceFights=new AllianceFight[array.length];
		for(int i=0;i<array.length;i++)
		{
			allianceFights[i]=mapping(array[i]);
		}
		return allianceFights;
	}


}
