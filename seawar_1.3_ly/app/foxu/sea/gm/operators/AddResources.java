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
 * ������Դ
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
		// ����
		String metal=params.get("metal");
		// ʯ��
		String oil=params.get("oil");
		// ��
		String silicon=params.get("silicon");
		// ����
		String uranium=params.get("uranium");
		// ��Ǯ
		String money=params.get("money");
		// ��ʯ
		String gems=params.get("gems");
		//�׳�
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
			//�ɾ����ݲɼ� 
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
			// ��ʯ��־��¼
			objectFactory.createGemTrack(GemsTrack.GM_SEND,player.getId(),
				gem,0,
				Resources.getGems(player.getResources()));
		}
		player.incrExp(exp,null);
		if(honor>0&&honor<10000000) player.incrHonorExp(honor);
		// ����
		// ����
		String energy=params.get("energy");
		player.addEnergy(Integer.parseInt(energy));
		return GMConstant.ERR_SUCCESS;
	}
	
	/**�ջ��׳影��**/
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
