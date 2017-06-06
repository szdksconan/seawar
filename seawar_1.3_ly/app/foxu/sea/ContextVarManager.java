package foxu.sea;

import java.util.HashMap;
import java.util.Map;

import foxu.dcaccess.ContextVarDBAccess;

/**
 * 服务器上下文变量管理器
 * 
 * @author Alan
 */
public class ContextVarManager
{

	public static final String WORLD_CHAT_LEVEL="world_chat_level",// 世界聊天等级限制
					PRIVATE_CHAT_LEVEL="private_chat_level",// 私密聊天等级限制
					EMAIL_LEVEL="email_level",// 邮件等级限制
					DATE_OFF_STATE="date_off_state",// 神秘商店开关状态
					CREATE_ALLIANCE_LEVEL_LIMIT="create_alliance_level_limit",// 创建联盟等级限制
					JOIN_ALLIANCE_LEVEL_LIMIT="join_alliance_level_limit",// 加入联盟等级限制
					ALLIANCE_DONATE_LEVEL_LIMIT="alliance_donate_level_limit",// 联盟捐献等级限制
					ALLIANCE_SHIP_DONATE_LEVEL_LIMIT="alliance_ship_donate_level_limit",// 联盟舰船捐献等级限制
					AWARD_LUCKY_SIDS="award_lucky_sids",// 奖励品提示sid
					MEAL_TIME_ENERGY="meal_time_energy",// 饭时送体力值
					ALLIANCE_FIGHT_DATA="alliance_fight_data",//联盟战的数据保存格式
					ALLIANCE_FIGHT_RECORD="alliance_fight_record",//联盟战的标识
					SAVE_ALLIANCEBATTLE_CTIME="save_alliancebattle_ctime",//联盟战日志一周的创建时间
					SAVE_CREATE_FIGHT_NAME="save_create_fight_name",//联盟战日志的名称
					SAVE_OFFCER_SHOP_TIME="save_offcer_shop_time",//军官商店的刷新时间
					SAVE_OFFCER_SHOP_LIMIT="save_offcer_shop_limit",//军官商品出现必须出现某种色的军官碎片
					GROWTH_PLAN_DATA="growth_plan_data",//成长计划数据存储[重要]
					ACTIVITY_AWARD_DATA="activity_award_data",//物品限时掉落活动数据
					CROSS_LEAGUE_SERVER_INFO="cross_league_server_info",//跨服积分赛信息
					CROSS_LEAGUE_CLIENT_INFO="cross_league_client_info";//跨服积分赛信息
	private static ContextVarManager varManager=new ContextVarManager();
	Map<String,VarEntry> vars=new HashMap<String,VarEntry>();
	ContextVarDBAccess varDBAccess;

	public static ContextVarManager getInstance()
	{
		return varManager;
	}

	/** 获取变量的值，如果没有这个变量返回Integer.MIN_VALUE */
	public int getVarValue(String key)
	{
		VarEntry var=vars.get(key);
		if(var!=null) return var.getVar();
		return Integer.MIN_VALUE;
	}

	/** 获取变量的描述 */
	public String getVarDest(String key)
	{
		VarEntry var=vars.get(key);
		if(var!=null) return var.getDest();
		return null;
	}
	
	/** 获取变量的数据 */
	public byte[] getVarData(String key)
	{
		VarEntry var=vars.get(key);
		if(var!=null) return var.getData();
		return null;
	}

	/** 设置变量的值 */
	public void setVarValue(String key,int value)
	{

		VarEntry var=getVarEntry(key);
		var.setVar(value);
		varDBAccess.save(var);
	}

	/** 设置变量的附加信息 */
	public void setVarDest(String key,String dest)
	{
		VarEntry var=getVarEntry(key);
		var.setDest(dest);
		varDBAccess.save(var);
	}
	
	/** 设置变量的数据 */
	public void setVarData(String key,byte[] data)
	{
		VarEntry var=getVarEntry(key);
		var.setData(data);
		varDBAccess.save(var);
	}

	/** 设置一个变量对象 */
	public void putVar(String key,int value,String dest)
	{
		VarEntry var=getVarEntry(key);
		var.setVar(value);
		var.setDest(dest);
		varDBAccess.save(var);
	}

	/** 获取变量对象,如果不存在就新增 */
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
	/** 从配置读取默认设置,如果有新增的则加入 */
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

	/** 存放值与附带信息的实体 */
	public class VarEntry
	{

		/** 变量名 */
		String key;
		/** 变量值 */
		int var=Integer.MIN_VALUE;
		/** 描述 */
		String dest;
		/** 数据 */
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
