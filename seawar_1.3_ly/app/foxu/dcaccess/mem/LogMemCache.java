package foxu.dcaccess.mem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;

import foxu.sea.kit.SeaBackKit;
import shelby.dc.GameDBAccess;
import mustang.field.ByteArrayField;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.StringField;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;


/**
 * 分表缓存
 * @author yw
 *
 */
public class LogMemCache
{
	/** 表名 */
	String table;
	/** 保存表数  */
	int saveTables;
	/** 分表间隔（天） */
	int tableCD;
	/** 上次换表时间 */
	int tableTime;
	/** 创建表 */
	String createSql;//="(`id` int(11) NOT NULL AUTO_INCREMENT,`type` tinyint(4) DEFAULT NULL,`createAt` int(11) DEFAULT NULL,`playerId` int(11) NOT NULL,`attackPlayerName` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,`extra` varchar(150) COLLATE utf8_unicode_ci DEFAULT NULL,`leftList` blob,`eventList` blob,`hurtList` blob,`list` blob, PRIMARY KEY (`id`),KEY `playerId` (`playerId`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPACT";
	
	public String getTable()
	{
		return table;
	}
	
	public void setTable(String table)
	{
		this.table=table;
	}
	
	public int getSaveTables()
	{
		return saveTables;
	}
	
	public void setSaveTables(int saveTables)
	{
		this.saveTables=saveTables;
	}
	
	public int getTableCD()
	{
		return tableCD;
	}
	
	public void setTableCD(int tableCD)
	{
		this.tableCD=tableCD;
	}
	
	public int getTableTime()
	{
		return tableTime;
	}
	
	public void setTableTime(int tableTime)
	{
		this.tableTime=tableTime;
	}

	
	public String getCreateSql()
	{
		return createSql;
	}

	
	public void setCreateSql(String createSql)
	{
		this.createSql=createSql;
	}
	
	/** 保存数据 */
	public void saves(GameDBAccess dbaccess,IntKeyHashMap list)
	{
		SqlPersistence sp=(SqlPersistence)dbaccess.getGamePersistence();
		Connection conn=sp.getConnectionManager().getConnection();
		try
		{
			// conn.setAutoCommit(false);
			PreparedStatement pst=conn
				.prepareStatement("load data local infile '' into table "
					+sp.getTable()+" fields terminated by ','");
			StringBuilder sb=new StringBuilder();
			int[] keys=list.keyArray();
			for(int i=0;i<keys.length;i++)
			{
				ArrayList trackList=(ArrayList)list.get(keys[i]);
				if(trackList==null||trackList.size()<=0) continue;
				Object[] objs=trackList.getArray();
				for(int m=0;m<objs.length;m++)
				{
					if(objs[m]==null) continue;
					Fields fields=dbaccess.mapping(objs[m]);
					FieldObject[] fobjs=fields.getArray();
					for(int n=0;n<fobjs.length;n++)
					{
						String value="";
						if(fobjs[n].getValue()!=null)
							value=fobjs[n].getValue().toString();
						if(fobjs[n] instanceof ByteArrayField)
						{
							value=SeaBackKit
								.BytesToAsciiString((byte[])fobjs[n]
									.getValue());
						}
						sb.append(value);
						if(n<fobjs.length-1)
						{
							sb.append(",");
						}
						else
						{
							sb.append("\n");
						}
					}
				}
				trackList.clear();
			}
			InputStream is=new ByteArrayInputStream(sb.toString().getBytes());
			((com.mysql.jdbc.Statement)pst).setLocalInfileInputStream(is);
			pst.execute();
			conn.commit();
			sb.setLength(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			SeaBackKit.log.error("save "+sp.getTable()+" Exception:"
				+e.toString());
		}
		finally
		{
			SqlKit.close(conn);
		}
	}
	
	/** 查找日志信息(多表遍历), 全字段查询,传入需要转换的日志对象类型 */
	public <T>T[] loadTracks(Class<T> clazz,GameDBAccess dbaccess,
		String conditionSql)
	{
		String sql="show tables like '"+getTable()+"%'";
		Fields[] fields=SqlKit.querys(((SqlPersistence)dbaccess
			.getGamePersistence()).getConnectionManager(),sql);
		if(fields==null||fields.length<=0) return null;
		T[] datas=(T[])Array.newInstance(clazz,0);
		for(int i=0;i<fields.length;i++)
		{
			FieldObject[] objs=fields[i].getArray();
			String tableName=((StringField)objs[0]).value;
			sql="SELECT * FROM "+tableName+" "+conditionSql;
			Object[] data=dbaccess.loadBySql(sql);
			if(data!=null&&data.length>0)
			{
				T[] old=datas;
				datas=(T[])Array.newInstance(clazz,old.length+data.length);
				System.arraycopy(old,0,datas,0,old.length);
				System.arraycopy(data,0,datas,old.length,data.length);
			}
		}
		return datas;
	}
}
