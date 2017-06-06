package foxu.combine;

import java.util.ArrayList;
import java.util.Iterator;

import mustang.field.Fields;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;

import shelby.dc.GameDBAccess;

/**
 * 数据表，
 * 
 * @author comeback
 * 
 */
public abstract class DataTable
{
	private static Logger log=LogFactory.getLogger(DataTable.class);
	
	/** 依赖项目 */
	ArrayList<DataTable> dependList=new ArrayList<DataTable>();
	
	/** 是否已处理完 */
	private boolean isFinished=false;
	
	/** 表名 */
	private String tableName;
	
	private GameDBAccess dbAccess;
	
	
	ArrayList<Object> dataList1=new ArrayList<Object>();
	
	ArrayList<Object> dataList2=new ArrayList<Object>();
	
	
	public void setDBAccess(GameDBAccess dbAccess)
	{
		this.dbAccess=dbAccess;
	}
	
	public GameDBAccess getDBAccess()
	{
		return this.dbAccess;
	}
	
	public void setTableName(String name)
	{
		this.tableName=name;
	}
	
	public String getTableName()
	{
		return this.tableName;
	}
	
	public void addDependency(DataTable table)
	{
		if(table==null)
			return;
		dependList.add(table);
	}
	
	public ArrayList<Object> getList1()
	{
		return this.dataList1;
	}
	
	public ArrayList<Object> getList2()
	{
		return this.dataList2;
	}
	
	public boolean isFinished()
	{
		return isFinished;
	}
	
	private void processDependency(DataTable t,Server s1,Server s2,Server s3)
	{
		int size=dependList.size();
		for(int i=0;i<size;i++)
		{
			DataTable table=dependList.get(i);
			// 如果已经处理过了，就跳过
			if(table.isFinished())
				continue;
			// 检查依赖有没有循环，实际只能检查到直接的相互依赖，对于A-B-C-A这种情况检查不到
			if(table.checkDependency(this))
				throw new RuntimeException("dependency error.");
			table.process(s1,s2,s3);
		}
	}
	
	public boolean checkDependency(DataTable table)
	{
		return dependList.contains(table);
	}
	
	public final void process(Server s1,Server s2,Server s3)
	{
		if(isFinished())
			return;
		// 先把依赖的表处理了
		this.processDependency(this,s1,s2,s3);
		// 处理具体的逻辑
		this.processImpl(s1,s2,s3);
		// 设置状态为已完成
		isFinished=true;
	}
	
	public final void processImpl(Server s1,Server s2,Server s3)
	{
		String sql="select * from "+tableName;
		dataList1.clear();
		dataList2.clear();
		
		// 查询数据并映射成对象
		Fields[] fields=SqlKit.querys(s1.getConnectionManager(),sql);
		mapping(fields,dataList1);
		fields=SqlKit.querys(s2.getConnectionManager(),sql);
		mapping(fields,dataList2);
		
		// 预处理数据
		beforeSave(dataList1,dataList2,s1,s2);
		// 保存数据
		ArrayList<Object> unsaved1=saveDataList(dataList1,s3);
		ArrayList<Object> unsaved2=saveDataList(dataList2,s3);
		// 处理保存失败的列表
		afterSave(unsaved1,unsaved2,s1,s2,s3);
		// 最后再保存一次
		unsaved1=saveDataList(unsaved1,s3);
		unsaved2=saveDataList(unsaved2,s3);
		// 打印最终还是没有保存成功的数量
		if(log.isInfoEnabled())
			log.info("unsaved size1="+unsaved1.size()+",unsaved size2="+unsaved2.size());
		
	}
	
	/**
	 * 将域映射到对象(反序列化操作)
	 * @param fields
	 * @param list
	 */
	private void mapping(Fields[] fields,ArrayList<Object> list)
	{
		if(fields==null||fields.length==0)
			return;
		for(int i=0;i<fields.length;i++)
		{
			list.add(dbAccess.mapping(fields[i]));
		}
	}
	
	/**
	 * 将列表中的数据保存到指针数据库的表中 
	 * @param list
	 * @param server
	 * @return
	 */
	public ArrayList<Object> saveDataList(ArrayList<Object> list,Server server)
	{
		ArrayList<Object> unsaved=new ArrayList<Object>();
		// 线程同步？
		Persistence p=dbAccess.getGamePersistence();
		dbAccess.setGamePersistence(server.getPersistence(tableName));
		Iterator<Object> iter=list.iterator();
		while(iter.hasNext())
		{
			Object o=iter.next();
			if(!dbAccess.save(o))
			{
				unsaved.add(o);
			}
		}
		dbAccess.setGamePersistence(p);
		return unsaved;
	}
	
	/**
	 * 保存前调用，预处理数据
	 * @param list1
	 * @param list2
	 * @param s1
	 * @param s2
	 */
	public abstract void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,Server s1, Server s2);
	
	/**
	 * 保存后调用，处理保存失败的数据
	 * @param list1
	 * @param list2
	 * @param s1
	 * @param s2
	 */
	public abstract void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,Server s1, Server s2,Server s3);

}
