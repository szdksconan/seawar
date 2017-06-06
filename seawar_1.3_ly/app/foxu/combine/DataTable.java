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
 * ���ݱ�
 * 
 * @author comeback
 * 
 */
public abstract class DataTable
{
	private static Logger log=LogFactory.getLogger(DataTable.class);
	
	/** ������Ŀ */
	ArrayList<DataTable> dependList=new ArrayList<DataTable>();
	
	/** �Ƿ��Ѵ����� */
	private boolean isFinished=false;
	
	/** ���� */
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
			// ����Ѿ�������ˣ�������
			if(table.isFinished())
				continue;
			// ���������û��ѭ����ʵ��ֻ�ܼ�鵽ֱ�ӵ��໥����������A-B-C-A���������鲻��
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
		// �Ȱ������ı�����
		this.processDependency(this,s1,s2,s3);
		// ���������߼�
		this.processImpl(s1,s2,s3);
		// ����״̬Ϊ�����
		isFinished=true;
	}
	
	public final void processImpl(Server s1,Server s2,Server s3)
	{
		String sql="select * from "+tableName;
		dataList1.clear();
		dataList2.clear();
		
		// ��ѯ���ݲ�ӳ��ɶ���
		Fields[] fields=SqlKit.querys(s1.getConnectionManager(),sql);
		mapping(fields,dataList1);
		fields=SqlKit.querys(s2.getConnectionManager(),sql);
		mapping(fields,dataList2);
		
		// Ԥ��������
		beforeSave(dataList1,dataList2,s1,s2);
		// ��������
		ArrayList<Object> unsaved1=saveDataList(dataList1,s3);
		ArrayList<Object> unsaved2=saveDataList(dataList2,s3);
		// ������ʧ�ܵ��б�
		afterSave(unsaved1,unsaved2,s1,s2,s3);
		// ����ٱ���һ��
		unsaved1=saveDataList(unsaved1,s3);
		unsaved2=saveDataList(unsaved2,s3);
		// ��ӡ���ջ���û�б���ɹ�������
		if(log.isInfoEnabled())
			log.info("unsaved size1="+unsaved1.size()+",unsaved size2="+unsaved2.size());
		
	}
	
	/**
	 * ����ӳ�䵽����(�����л�����)
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
	 * ���б��е����ݱ��浽ָ�����ݿ�ı��� 
	 * @param list
	 * @param server
	 * @return
	 */
	public ArrayList<Object> saveDataList(ArrayList<Object> list,Server server)
	{
		ArrayList<Object> unsaved=new ArrayList<Object>();
		// �߳�ͬ����
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
	 * ����ǰ���ã�Ԥ��������
	 * @param list1
	 * @param list2
	 * @param s1
	 * @param s2
	 */
	public abstract void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,Server s1, Server s2);
	
	/**
	 * �������ã�������ʧ�ܵ�����
	 * @param list1
	 * @param list2
	 * @param s1
	 * @param s2
	 */
	public abstract void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,Server s1, Server s2,Server s3);

}
