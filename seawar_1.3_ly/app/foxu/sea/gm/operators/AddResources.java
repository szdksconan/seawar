package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gems.GemsTrack;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;

/**
 * 增加资源
 * @author comeback
 *
 */
public class AddResources extends GMOperator
{
	public static final String COMMAND="addresource";
	
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playerName=params.get("player_name");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		
		player.getIsland().pushAll(TimeKit.getSecondTime(),objectFactory);
		// 金属
		String metal=params.get("metal");
		// 石油
		String oil=params.get("oil");
		// 硅
		String silicon=params.get("silicon");
		// 低铀
		String uranium=params.get("uranium");
		// 金钱
		String money=params.get("money");
		// 宝石
		String gems=params.get("gems");
		//首冲
		String reduce_type=params.get("reduce_type");
		String addMaxGems=params.get("max_gems");
		int honor=0;
		if(params.get("honor")!=null
			&&!params.get("honor").equals(""))
			honor=Integer.parseInt(params.get("honor"));

		int exp=0;
		if(params.get("exp")!=null
			&&!params.get("exp").equals(""))
			exp=Integer.parseInt(params.get("exp"));

		Resources.addResources(player.getResources(),Long
			.parseLong(metal),Long.parseLong(oil),Long
			.parseLong(silicon),Long.parseLong(uranium),Long
			.parseLong(money),player);
		int gem=Integer.parseInt(gems);
		if(addMaxGems.equals("true"))
		{
			Resources.addGems(gem,player
				.getResources(),player);
			//成就数据采集 
			AchieveCollect.gemsStock(gem,player);
			player.flushVIPlevel();
		}
		else if(addMaxGems.equals("reduce"))
		{
			Resources.reduceGems(gem,player
				.getResources(),player);
		}
		else if(addMaxGems.equals("reduceMax"))
		{
			int retype=TextKit.parseInt(reduce_type);
			int cgem=(int)Resources.getGems(player.getResources());
			int dgem=cgem>gem?gem:cgem;
			Resources.reduceGems(dgem,player.getResources(),player);
			if(retype==2) 
				reduceFirstAward(player);
			player.getResources()[Resources.MAXGEMS]-=gem;
			if(player.getResources()[Resources.MAXGEMS]<0)
				player.getResources()[Resources.MAXGEMS]=0;
			player.flushVIPlevel();
			JBackKit.sendResetResources(player);

		}
		else if(addMaxGems.equals("reduceVip"))
		{
			player.getResources()[Resources.MAXGEMS]-=gem;
			if(player.getResources()[Resources.MAXGEMS]<0)
				player.getResources()[Resources.MAXGEMS]=0;
			player.flushVIPlevel();
			JBackKit.sendResetResources(player);
		}
		else
		{
			Resources.addGemsNomal(Integer.parseInt(gems),player
				.getResources(),player);
		}
		if(gem!=0)
		{
			if(addMaxGems.equals("reduce")||addMaxGems.equals("reduceMax"))gem=-gem;
			if(!addMaxGems.equals("reduceVip"))
			// 宝石日志记录
			objectFactory.createGemTrack(GemsTrack.GM_SEND,player.getId(),
				gem,0,
				Resources.getGems(player.getResources()));
		}
		player.incrExp(exp,null);
		if(honor>0&&honor<10000000) player.incrHonorExp(honor);
		// 能量
		// 金属
		String energy=params.get("energy");
		player.addEnergy(Integer.parseInt(energy));
		return GMConstant.ERR_SUCCESS;
	}
	
	/**收回首冲奖励**/
	public boolean reduceFirstAward(Player player)
	{
		if(player.getAttributes(PublicConst.FP_AWARD)==null) return true;
		int fp=Integer.parseInt(player.getAttributes(PublicConst.FP_AWARD));
		if((fp&1)!=0)
		{
			int gems=fp>>>16;
			int cgem=(int)Resources.getGems(player.getResources());
			int dgem=cgem>gems?gems:cgem;
			Resources.reduceGems(dgem,player.getResources(),player);
		}
		player.setAttribute(PublicConst.FP_AWARD,null);
		JBackKit.sendFPaward(player);
		return true;
	}

}
