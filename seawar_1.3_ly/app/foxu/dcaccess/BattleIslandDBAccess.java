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
import mustang.text.TextKit;
import shelby.dc.GameDBAccess;
import foxu.sea.alliance.alliancebattle.BattleIsland;

public class BattleIslandDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(BattleIslandDBAccess.class);

	/** 保存方法 返回是否操作成功 */
	public boolean save(Object bIsland)
	{
		if(bIsland==null) return false;
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(
			FieldKit.create("sid",((BattleIsland)bIsland).getSid()),
			mapping(bIsland));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+bIsland);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object bIsland)
	{
		BattleIsland bland=(BattleIsland)bIsland;
		FieldObject[] array=new FieldObject[9];
		int i=0;
		array[i++]=FieldKit.create("sid",bland.getSid());
		if(bland.isState())
			array[i++]=FieldKit.create("state",1);
		else
			array[i++]=FieldKit.create("state",0);
		if(bland.isAttack())
			array[i++]=FieldKit.create("shot",1);
		else
			array[i++]=FieldKit.create("shot",0);

		array[i++]=FieldKit.create("win_id",bland.getWin_Id());
		array[i++]=FieldKit.create("lose_id",bland.getlose_Id());
		array[i++]=FieldKit.create("bets",bland.getRankValues());
		ByteBuffer bb=new ByteBuffer();

		bb.clear();
		bland.bytesWriteAlliancePlayerInfo(bb);
		array[i++]=FieldKit.create("fights",bb.toArray());

		bb.clear();
		bland.bytesWriteFirstList(bb);
		array[i++]=FieldKit.create("firstlist",bb.toArray());

		bb.clear();
		bland.bytesWriteLastList(bb);
		array[i++]=FieldKit.create("lastlist",bb.toArray());

		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public BattleIsland mapping(Fields fields)
	{
		int value=((IntField)fields.get("sid")).value;
		BattleIsland bIisand=(BattleIsland)BattleIsland.factory
			.newSample(value);

		int win_id=((IntField)fields.get("win_id")).value;
		bIisand.setWin_Id(win_id);

		int lose_id=((IntField)fields.get("lose_id")).value;
		bIisand.setlose_Id(lose_id);

		value=((IntField)fields.get("state")).value;
		if(value==0)
			bIisand.setState(false);
		else
			bIisand.setState(true);

		value=((IntField)fields.get("shot")).value;
		if(value==0)
			bIisand.setAttack(false);
		else
			bIisand.setAttack(true);

		String str=((StringField)fields.get("bets")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,":");
			int[] rankValue=new int[4];
			for(int i=0;i<strs.length;i++)
				rankValue[i]=TextKit.parseInt(strs[i]);
			bIisand.setRankValue(rankValue);
		}
		byte[] array=((ByteArrayField)fields.get("fights")).value;
		if(array!=null&&array.length>0)
			bIisand.bytesReadAlliancePlayerInfo(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("firstlist")).value;
		if(array!=null&&array.length>0)
			bIisand.bytesReadFirstList(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("lastlist")).value;
		if(array!=null&&array.length>0)
			bIisand.bytesReadLastList(new ByteBuffer(array));

		return bIisand;
	}

	/** 加载联盟战岛屿 */
	public BattleIsland[] loadBySql(String sql)
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
				"BattleIslandDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		BattleIsland[] bIslands=new BattleIsland[array.length];
		for(int i=0;i<array.length;i++)
		{
			bIslands[i]=mapping(array[i]);
		}
		return bIslands;
	}
}
