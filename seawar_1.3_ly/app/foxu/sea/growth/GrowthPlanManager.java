package foxu.sea.growth;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.ContextVarManager;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;

/**
 * 成长计划管理器
 * 
 * @author Alan
 * 
 */
public class GrowthPlanManager
{

	CreatObjectFactory objectFactory;
	ContextVarManager varManager;
	public static final int LIMIT_LEVEL=40;
	public static final int CLOSED=0,OPENED=1;
	IntKeyHashMap costPlayers=new IntKeyHashMap();

	public void init()
	{
		initData();
	}

	/** 检测当前个人奖励是否可用 */
	public String checkPrivateAwardAvailable(Player player,int level)
	{
		if(player.getLevel()<level) return "level_not_enough";
		IntList record=((Player)costPlayers.get(player.getId()))
			.getGrowthPlan().getPrivateList();
		if(!player.getGrowthPlan().isBuyPlan()) return "do not join growth plan";
		if(record.contain(level)) return "already get this award";
		return null;
	}

	/** 检测当前全服奖励是否可用 */
	public String checkServerAwardAvailable(Player player,int num)
	{
		if(costPlayers.size()<num) return "cost player not enough";
		IntList typeRecord=player.getGrowthPlan().getServerList();
		if(typeRecord!=null&&typeRecord.contain(num))
			return "already get this award";
		return null;
	}

	/** 领取个人奖励 */
	public String openPrivateAward(Player player,int level,
		CreatObjectFactory objectFactory)
	{
		if(!isSystemOpen()) return "system is closed";
		String msg=checkPrivateAwardAvailable(player,level);
		if(msg!=null) return msg;
		int sid=-1;
		for(int i=0;i<PublicConst.GROWTH_PLAN_PRIVATE.length;i+=2)
		{
			if(PublicConst.GROWTH_PLAN_PRIVATE[i]==level)
				sid=PublicConst.GROWTH_PLAN_PRIVATE[i+1];
		}
		Award award=(Award)Award.factory.newSample(sid);
		award.awardLenth(new ByteBuffer(),player,objectFactory,null,
			new int[]{EquipmentTrack.FROM_GROWTH_PLAN});
		addPrivateAwardRecord(player.getId(),level);
		return null;
	}

	/** 领取全服奖励 */
	public String openServerAward(Player player,int num,
		CreatObjectFactory objectFactory)
	{
		if(!isSystemOpen()) return "system is closed";
		String msg=checkServerAwardAvailable(player,num);
		if(msg!=null) return msg;
		int sid=-1;
		for(int i=0;i<PublicConst.GROWTH_PLAN_SERVER.length;i+=2)
		{
			if(PublicConst.GROWTH_PLAN_SERVER[i]==num)
				sid=PublicConst.GROWTH_PLAN_SERVER[i+1];
		}
		if(sid<0) return "param error";
		player.getGrowthPlan().addServerRecord(num);
		Award award=(Award)Award.factory.newSample(sid);
		award.awardLenth(new ByteBuffer(),player,objectFactory,null,
			new int[]{EquipmentTrack.FROM_GROWTH_PLAN});
		return null;
	}

	/** 添加一个个人奖励记录 */
	public void addPrivateAwardRecord(int pid,int level)
	{
		addAwardRecord(pid,level,costPlayers);
	}

	/** 添加一个奖励记录 */
	public void addAwardRecord(int pid,int value,IntKeyHashMap record)
	{
		IntList typeRecord=((Player)record.get(pid))
						.getGrowthPlan().getPrivateList();
		if(typeRecord==null)
		{
			typeRecord=new IntList();
			record.put(pid,typeRecord);
		}
		typeRecord.add(value);
	}

	/** 添加一个玩家成长计划记录 */
	public void addPlayerGrowthPlan(Player player,CreatObjectFactory factory)
	{
		player.getGrowthPlan().setBuyPlan(true);
		costPlayers.put(player.getId(),player);
		JBackKit.sendGrowthPlan(factory,this);
	}

	public String buyPrivatePlan(Player player,CreatObjectFactory factory)
	{
		if(!isSystemOpen()) return "system is closed";
		if(player.getLevel()>LIMIT_LEVEL) return "player level limit";
		if(player.getGrowthPlan().isBuyPlan()) return "growth plan exist";
		if(!Resources.checkGems(PublicConst.GROWTH_PLAN_COST,
			player.getResources())) return "not enough gems";
		Resources.reduceGems(PublicConst.GROWTH_PLAN_COST,
			player.getResources(),player);
		factory.createGemTrack(GemsTrack.BUY_GROWTH_PLAN,player.getId(),
			PublicConst.GROWTH_PLAN_COST,0,
			Resources.getGems(player.getResources()));
		addPlayerGrowthPlan(player,factory);
		return null;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		data.writeInt(costPlayers.size());
	}

	public void showBytesWrite(ByteBuffer data,Player player)
	{
		// 个人奖励信息
		IntList record=player.getGrowthPlan().getPrivateList();
		boolean isJoin=player.getGrowthPlan().isBuyPlan();
		data.writeBoolean(isJoin);
		data.writeShort(record.size());
		for(int i=0;i<record.size();i++)
		{
			data.writeByte(record.get(i));
			data.writeBoolean(true);
		}
		// 全服奖励
		data.writeInt(costPlayers.size());
		record=player.getGrowthPlan().getServerList();
		data.writeShort(record.size());
		for(int i=0;i<record.size();i++)
		{
			data.writeInt(record.get(i));
			data.writeBoolean(true);
		}
	}

	public void initData()
	{
		int[] keys=objectFactory.getPlayerCache().getCacheMap().keyArray();
		Player player;
		for(int k=0;k<keys.length;k++)
		{
			player=objectFactory.getPlayerById(keys[k]);
			if(player!=null&&player.getGrowthPlan().isBuyPlan())
				costPlayers.put(player.getId(),player);
		}
	}

	public boolean isSystemOpen()
	{
		return varManager.getVarValue(ContextVarManager.GROWTH_PLAN_DATA)==OPENED;
	}

	public ContextVarManager getVarManager()
	{
		return varManager;
	}

	public void setVarManager(ContextVarManager varManager)
	{
		this.varManager=varManager;
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}
