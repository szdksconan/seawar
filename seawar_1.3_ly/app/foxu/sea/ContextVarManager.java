package foxu.sea;

import java.util.HashMap;
import java.util.Map;

import foxu.dcaccess.ContextVarDBAccess;

/**
 * �����������ı���������
 * 
 * @author Alan
 */
public class ContextVarManager
{

	public static final String WORLD_CHAT_LEVEL="world_chat_level",// ��������ȼ�����
					PRIVATE_CHAT_LEVEL="private_chat_level",// ˽������ȼ�����
					EMAIL_LEVEL="email_level",// �ʼ��ȼ�����
					DATE_OFF_STATE="date_off_state",// �����̵꿪��״̬
					CREATE_ALLIANCE_LEVEL_LIMIT="create_alliance_level_limit",// �������˵ȼ�����
					JOIN_ALLIANCE_LEVEL_LIMIT="join_alliance_level_limit",// �������˵ȼ�����
					ALLIANCE_DONATE_LEVEL_LIMIT="alliance_donate_level_limit",// ���˾��׵ȼ�����
					ALLIANCE_SHIP_DONATE_LEVEL_LIMIT="alliance_ship_donate_level_limit",// ���˽������׵ȼ�����
					AWARD_LUCKY_SIDS="award_lucky_sids",// ����Ʒ��ʾsid
					MEAL_TIME_ENERGY="meal_time_energy",// ��ʱ������ֵ
					ALLIANCE_FIGHT_DATA="alliance_fight_data",//����ս�����ݱ����ʽ
					ALLIANCE_FIGHT_RECORD="alliance_fight_record",//����ս�ı�ʶ
					SAVE_ALLIANCEBATTLE_CTIME="save_alliancebattle_ctime",//����ս��־һ�ܵĴ���ʱ��
					SAVE_CREATE_FIGHT_NAME="save_create_fight_name",//����ս��־������
					SAVE_OFFCER_SHOP_TIME="save_offcer_shop_time",//�����̵��ˢ��ʱ��
					SAVE_OFFCER_SHOP_LIMIT="save_offcer_shop_limit",//������Ʒ���ֱ������ĳ��ɫ�ľ�����Ƭ
					GROWTH_PLAN_DATA="growth_plan_data",//�ɳ��ƻ����ݴ洢[��Ҫ]
					ACTIVITY_AWARD_DATA="activity_award_data",//��Ʒ��ʱ��������
					CROSS_LEAGUE_SERVER_INFO="cross_league_server_info",//�����������Ϣ
					CROSS_LEAGUE_CLIENT_INFO="cross_league_client_info";//�����������Ϣ
	private static ContextVarManager varManager=new ContextVarManager();
	Map<String,VarEntry> vars=new HashMap<String,VarEntry>();
	ContextVarDBAccess varDBAccess;

	public static ContextVarManager getInstance()
	{
		return varManager;
	}

	/** ��ȡ������ֵ�����û�������������Integer.MIN_VALUE */
	public int getVarValue(String key)
	{
		VarEntry var=vars.get(key);
		if(var!=null) return var.getVar();
		return Integer.MIN_VALUE;
	}

	/** ��ȡ���������� */
	public String getVarDest(String key)
	{
		VarEntry var=vars.get(key);
		if(var!=null) return var.getDest();
		return null;
	}
	
	/** ��ȡ���������� */
	public byte[] getVarData(String key)
	{
		VarEntry var=vars.get(key);
		if(var!=null) return var.getData();
		return null;
	}

	/** ���ñ�����ֵ */
	public void setVarValue(String key,int value)
	{

		VarEntry var=getVarEntry(key);
		var.setVar(value);
		varDBAccess.save(var);
	}

	/** ���ñ����ĸ�����Ϣ */
	public void setVarDest(String key,String dest)
	{
		VarEntry var=getVarEntry(key);
		var.setDest(dest);
		varDBAccess.save(var);
	}
	
	/** ���ñ��������� */
	public void setVarData(String key,byte[] data)
	{
		VarEntry var=getVarEntry(key);
		var.setData(data);
		varDBAccess.save(var);
	}

	/** ����һ���������� */
	public void putVar(String key,int value,String dest)
	{
		VarEntry var=getVarEntry(key);
		var.setVar(value);
		var.setDest(dest);
		varDBAccess.save(var);
	}

	/** ��ȡ��������,��������ھ����� */
	public VarEntry getVarEntry(String key)
	{
		VarEntry var=vars.get(key);
		if(var==null)
		{
			var=new VarEntry();
			vars.put(key,var);
			var.setKey(key);
		}
		return var;
	}

	public void init()
	{
		VarEntry[] vars_temp=varDBAccess.loadAll();
		if(vars_temp!=null) for(int i=0;i<vars_temp.length;i++)
		{
			vars.put(vars_temp[i].getKey(),vars_temp[i]);
		}
	}
	/** �����ö�ȡĬ������,���������������� */
	public void checkVars(String key,int value,String dest)
	{
		if(vars.get(key)==null) putVar(key,value,dest);
	}

	public ContextVarDBAccess getVarDBAccess()
	{
		return varDBAccess;
	}

	public void setVarDBAccess(ContextVarDBAccess varDBAccess)
	{
		this.varDBAccess=varDBAccess;
	}

	/** ���ֵ�븽����Ϣ��ʵ�� */
	public class VarEntry
	{

		/** ������ */
		String key;
		/** ����ֵ */
		int var=Integer.MIN_VALUE;
		/** ���� */
		String dest;
		/** ���� */
		byte[] data;

		public VarEntry()
		{
			super();
		}

		public VarEntry(String key,int var,String dest)
		{
			super();
			this.key=key;
			this.var=var;
			this.dest=dest;
		}
		
		public VarEntry(String key,int var,String dest,byte[] data)
		{
			super();
			this.key=key;
			this.var=var;
			this.dest=dest;
			this.data=data;
		}

		public String getKey()
		{
			return key;
		}

		public void setKey(String key)
		{
			this.key=key;
		}

		public int getVar()
		{
			return var;
		}

		public void setVar(int var)
		{
			this.var=var;
		}

		public String getDest()
		{
			return dest;
		}

		public void setDest(String dest)
		{
			this.dest=dest;
		}

		public byte[] getData()
		{
			return data;
		}
		
		public void setData(byte[] data)
		{
			this.data=data;
		}

	}
}
