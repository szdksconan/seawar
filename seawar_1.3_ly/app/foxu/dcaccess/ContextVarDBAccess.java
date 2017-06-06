package foxu.dcaccess;

import mustang.field.ByteArrayField;
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
import foxu.sea.ContextVarManager;

/**
 * �����������ı������ݿ����
 * 
 * @author Alan
 */
public class ContextVarDBAccess extends GameDBAccess
{

	/** ��־��¼ */
	private static final Logger log=LogFactory
		.getLogger(PasswordDBAccess.class);

	/** ���淽�� �����Ƿ�����ɹ� */
	public boolean save(ContextVarManager.VarEntry var)
	{
		// ��ӳ��������������ݿ���
		int t=gamePersistence.set(FieldKit.create("var_key",var.getKey()),
			mapping(var));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+var);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** ӳ�������� */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[4];
		int i=0;
		array[i++]=FieldKit.create("var_key",(String)null);
		array[i++]=FieldKit.create("dest",(String)null);
		array[i++]=FieldKit.create("var_value",Integer.MIN_VALUE);
		array[i++]=FieldKit.create("data",(byte[])null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** ӳ�������� */
	public Fields mapping(ContextVarManager.VarEntry var)
	{
		FieldObject[] array=new FieldObject[4];
		int i=0;
		array[i++]=FieldKit.create("var_key",var.getKey());
		array[i++]=FieldKit.create("dest",var.getDest());
		array[i++]=FieldKit.create("var_value",var.getVar());
		array[i++]=FieldKit.create("data",var.getData());
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
	/** ӳ�������� */
	public ContextVarManager.VarEntry mapping(Fields fields)
	{
		ContextVarManager.VarEntry var=ContextVarManager.getInstance().new VarEntry();
		var.setKey(((StringField)fields.get("var_key")).value);
		var.setDest(((StringField)fields.get("dest")).value);
		var.setVar(((IntField)fields.get("var_value")).value);
		var.setData(((ByteArrayField)fields.get("data")).value);
		return var;
	}

	public ContextVarManager.VarEntry load(String key)
	{
		// ����һ�����򣨰�����NpcIsland���������ԣ�
		Fields fields=mapping();
		// ʹ�ó־��� ��ָ��name�ҵ���Ӧ��ֵ��ֵ��fields
		int t=gamePersistence.get(FieldKit.create("var_key",key),fields);
		// �����������
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"ContextVarDBAccess db error");
		// ������������ݵ�������װ��һ����Ҷ���
		ContextVarManager.VarEntry var=mapping(fields);
		return var;
	}
	
	public ContextVarManager.VarEntry[] loadAll()
	{
		return loadBySql("select * from context_var");
	}
	
	public ContextVarManager.VarEntry[] loadBySql(String sql)
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
				"PlayerGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		ContextVarManager.VarEntry[] vars=new ContextVarManager.VarEntry[array.length];
		for(int i=0;i<array.length;i++)
		{
			vars[i]=mapping(array[i]);
		}
		return vars;
	}
}
