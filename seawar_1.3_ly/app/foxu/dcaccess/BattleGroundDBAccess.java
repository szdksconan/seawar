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
import foxu.sea.alliance.alliancefight.BattleGround;
import shelby.dc.GameDBAccess;


public class BattleGroundDBAccess extends GameDBAccess
{
	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(BattleGroundDBAccess.class);
	
	
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object ground)
	{
		if(ground==null) return false;
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("sid",((BattleGround)ground)
			.getSid()),mapping(ground));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+ground);
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
	public Fields mapping(Object ground)
	{
		BattleGround bg=(BattleGround)ground;
		FieldObject[] array=new FieldObject[5];
		int i=0;
		array[i++]=FieldKit.create("sid",bg.getSid());
		array[i++]=FieldKit.create("id",bg.getId());
		array[i++]=FieldKit.create("captureTime",bg.getCaptureTime());
		array[i++]=FieldKit.create("lastTime",bg.getLastTime());
		
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		bg.bytesWriteFleet(bb);
		array[i++]=FieldKit.create("fleet",bb.toArray());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public BattleGround mapping(Fields fields)
	{
		int value=((IntField)fields.get("sid")).value;
		
		BattleGround bg=(BattleGround)BattleGround.factory.newSample(value);
		value=((IntField)fields.get("id")).value;
		bg.setId(value);
		value=((IntField)fields.get("captureTime")).value;
		bg.setCaptureTime(value);
		value=((IntField)fields.get("lastTime")).value;
		bg.setLastTime(value);
		
		byte[] array=((ByteArrayField)fields.get("fleet")).value;
		if(array!=null&&array.length>0)
			bg.bytesReadFleet(new ByteBuffer(array));

		return bg;
	}
	
	/** 加载所有据点 */
	public BattleGround[] loadBySql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			array=SqlKit.querys(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
//			System.out.println("-----------BattleGround----------loadBySql-----DataAccessException-------");
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"BattleGroundDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		BattleGround[] ground=new BattleGround[array.length];
		for(int i=0;i<array.length;i++)
		{
			ground[i]=mapping(array[i]);
		}
		return ground;
	}


}
