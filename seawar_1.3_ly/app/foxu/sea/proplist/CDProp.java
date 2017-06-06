package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.builds.AutoUpBuildManager;
import foxu.sea.builds.Build;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.Island;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.Product;
import foxu.sea.builds.produce.StandProduce;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.kit.JBackKit;

/**
 * 减少建筑建造时间道具
 * @author Alan
 *	
 */
public class CDProp extends AwardProp
{
	/**BULID_TYPE=1 建筑加速 SCIENCE_TYPE=2 科技加速 SHIP_TYPE=3 船只加速**/
	public static int BULID_TYPE=1,SCIENCE_TYPE=2,SHIP_TYPE=3;
	/** seconds */
	int cdTime;
	/**类型**/
	int type;
	
	
	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type=type;
	}


	/** ByteBuffer为兼容新的物品使用返回信息 */
	public void use(Player player,CreatObjectFactory objectFactory,AutoUpBuildManager autoUpBuilding,ByteBuffer data)
	{
		super.use(player,objectFactory,data);
		int time=TimeKit.getSecondTime();
		Object[] buildings=player.getIsland().getBuildArray();
		IntList CDBuilds=new IntList();
		IntList completeBuilds=new IntList();
		for(int i=0;i<buildings.length;i++){
			PlayerBuild pb=(PlayerBuild)buildings[i];
			if(pb!=null&&player.getIsland().checkNowBuildingByIndex(pb.getIndex())){
				int completeTime=pb.getBuildCompleteTime()-cdTime;
				pb.setBuildCompleteTime(completeTime);
				if(completeTime<=time)
					completeBuilds.add(pb.getIndex());
				CDBuilds.add(pb.getIndex());
			}
		}
		player.getIsland().gotNowBuilding(time,objectFactory);
		autoUpBuilding.containedPlayer2Up(player);
		JBackKit.sendResetBunld(player);
		for(int i=0;i<completeBuilds.size();i++){
			if(player.getIsland().checkNowBuildingByIndex(completeBuilds.get(i))){
				CDBuilds.remove(completeBuilds.get(i));
			}
		}
		JBackKit.sendBuildServiceInfo(player,CDBuilds.getArray(),CDBuilds.size());
	}
	
	
	/**科技加速**/
	public  void scienceSpeed(Player player,CreatObjectFactory factory,ByteBuffer data)
	{
		super.use(player,factory,data);
		PlayerBuild checkBuild=(PlayerBuild)player.getIsland()
			.getBuildByType(Build.BUILD_RESEARCH,null);
		/** 检测下列表中的时间 **/
		data.writeByte(1);
		data.writeByte(Build.BUILD_RESEARCH);
		data.writeShort(proSpeed(player,checkBuild,factory,cdTime));
		JBackKit.sendResetScience(player,checkBuild);
		JBackKit.sendFightScore(player,factory,true,
			FightScoreConst.ONLINE_PUSH_ALL);
	}
	
	/**船只加速**/
	public void shipSpeed(Player player,CreatObjectFactory factory,ByteBuffer data)
	{
		super.use(player,factory,data);
		Island builds=player.getIsland();
		/** 需要判断下当下的那些建筑有效 根据有效建筑加速 **/
		PlayerBuild checkBuild=builds.getBuildByIndex(BuildInfo.INDEX_10,
			builds.getBuilds());
		PlayerBuild checkBuild1=builds.getBuildByIndex(BuildInfo.INDEX_11,
			builds.getBuilds());
		PlayerBuild checkBuild2=builds.getBuildByIndex(BuildInfo.INDEX_12,
			builds.getBuilds());
		PlayerBuild checkBuild3=builds.getBuildByIndex(BuildInfo.INDEX_13,
			builds.getBuilds());
		data.writeByte(4);
		data.writeByte(BuildInfo.INDEX_10);
		data.writeShort(proSpeed(player,checkBuild,factory,cdTime));
		data.writeByte(BuildInfo.INDEX_11);
		data.writeShort(proSpeed(player,checkBuild1,factory,cdTime));
		data.writeByte(BuildInfo.INDEX_12);
		data.writeShort(proSpeed(player,checkBuild2,factory,cdTime));
		data.writeByte(BuildInfo.INDEX_13);
		data.writeShort(proSpeed(player,checkBuild3,factory,cdTime));
		JBackKit.sendResetScience(player,checkBuild);
		JBackKit.sendResetScience(player,checkBuild1);
		JBackKit.sendResetScience(player,checkBuild2);
		JBackKit.sendResetScience(player,checkBuild3);
		JBackKit.sendResetTroops(player);
		JBackKit.sendFightScore(player,factory,true,FightScoreConst.ONLINE_PUSH_ALL);
		
	}
	
	
	/**物品加速**/
	public int proSpeed(Player player,PlayerBuild checkBuild,CreatObjectFactory factory,int endTime)
	{
		if(checkBuild==null) return 0;
		int time=TimeKit.getSecondTime();
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		/** 检测下列表中的时间 **/
		checkBuild.produce(player,time,factory);
		if(produce.getProductes()==null) return 0;
		Object[] productes=produce.getProductes().getArray();
		if(productes==null||productes.length==0)
		{
			return 0;
		}
		Product p=(Product)productes[0];
		if(p==null) return 0;
		int ptime=p.getFinishTime()-time;
		if(ptime<=0) return 0;
		if(ptime>endTime)
		{
			p.setFinishTime(p.getFinishTime()-endTime);
			return 0;
		}
		else
		{
			p.setFinishTime(time);
			checkBuild.produce(player,time,factory);
			return p.getSid();
		}
	}
	
	
	/** ByteBuffer为兼容新的物品使用返回信息 */
	public void use(Player player,CreatObjectFactory objectFactory,AutoUpBuildManager autoUpBuilding,ByteBuffer data,int num)
	{
		for(int i=0;i<num;i++)
		{
			if(type==BULID_TYPE)
				use(player,objectFactory,autoUpBuilding,data);
			else if(type==SCIENCE_TYPE)
				scienceSpeed(player,objectFactory,data);
			else if(type==SHIP_TYPE) shipSpeed(player,objectFactory,data);
		}
	}
}
