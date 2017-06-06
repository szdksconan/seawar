package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.back.BackKit;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.util.TimeKit;
import shelby.serverOnMina.server.MinaConnectServer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.port.UserToCenterPort;


public class CloseServer extends GMOperator
{
	public static int PRIVILEGE=50;
	
	private static Logger log=LogFactory.getLogger(CloseServer.class);
	
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String close=params.get("close");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		int num=0;
		int left[]=new int[14];
		if(close.equals("true"))
		{
			// 关闭服务器 先断服务器连接
			num=objectFactory.getDsmanager().close();
			// 解除端口绑定
			MinaConnectServer server=(MinaConnectServer)BackKit
				.getContext().get("server");
			server.close();
			// 存储数据
			left=objectFactory.saveAndExit(true);
			// 清理僵尸用户
			clearIsland(objectFactory);
			PublicConst.READY=true;
		}
		String str="close_server:----"+close+"   online size========"+num
			+"  left data num="+left[0]+","+left[1]+","+left[2]+","+left[3]
			+","+left[4]+","+left[5]+","+left[6]+","+left[7]+","+left[8]+","
			+left[9]+","+left[10]+","+left[11]+","+left[12]+","+left[13]+","
			+left[14]+","+left[15]+","+left[16]+","+left[17]+","+left[18]
			+","+left[19]+","+left[20]+","+left[21]+","+left[22]+","
			+left[23]+","+left[24]+","+left[25]+","+left[26]+","+left[27]
			+","+left[28]+","+left[29]+","+left[30]+","+left[31]+","+left[32];
		log.info(str);
		try
		{
			JSONObject jo=new JSONObject();
			jo.put(GMConstant.SERVER_ID,UserToCenterPort.SERVER_ID);
			jo.put(GMConstant.CLOSE_PORT,close);
			jo.put(GMConstant.ONLINE,num);
			jo.put(GMConstant.UNSAVED_PLAYER,left[0]);
			jo.put(GMConstant.UNSAVED_EVENT,left[1]);
			jo.put(GMConstant.UNSAVED_ISLAND,left[2]);
			jo.put(GMConstant.UNSAVED_MESSAGE,left[3]);
			jo.put(GMConstant.UNSAVED_ALLIANCE,left[4]);
			jo.put(GMConstant.UNSAVED_GEMSTRACK,left[5]);
			jo.put(GMConstant.UNSAVED_GAMEDATA,left[6]);
			jo.put(GMConstant.UNSAVED_SHIPLOG,left[7]);
			jo.put(GMConstant.UNSAVED_ARENA,left[8]);
			jo.put(GMConstant.UNSAVED_WORLDBOSS,left[9]);
			jo.put(GMConstant.UNSAVED_AFIGHT,left[10]);
			jo.put(GMConstant.UNSAVED_AFIGHTEVENT,left[11]);
			jo.put(GMConstant.UNSAVED_BATTLEGROUND,left[12]);
			jo.put(GMConstant.UNSAVED_ACTIVITYLOG,left[13]);
			jsonArray.put(jo);
		}
		catch(JSONException e)
		{
		}
		
		return GMConstant.ERR_SUCCESS;
	}
	
	/** 清理等级为1-3的玩家岛屿坐标 且2天没有登录过 */
	private void clearIsland(CreatObjectFactory objectFactory)
	{
		Object object[]=objectFactory.getPlayerCache().getCacheMap()
			.valueArray();
		for(int i=0;i<object.length;i++)
		{
			PlayerSave data=(PlayerSave)object[i];
			Player player=data.getData();
			int createTime=player.getCreateTime();
			createTime=TimeKit.getSecondTime()-createTime;
			int updateTime=player.getUpdateTime();
			updateTime=TimeKit.getSecondTime()-updateTime;
			if((player.getLevel()<=5&&(updateTime>60*60*24*2)))
			{
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				if(island==null) continue;
				island.setPlayerId(0);
				objectFactory.getIslandCache().getDbaccess().save(island);
			}
		}
	}
	
	@Override
	/**
	 * 检查操作所需要的权限等级
	 * @return
	 */
	public boolean checkPrivilegeLevel(int privilege)
	{
		boolean b=super.checkPrivilegeLevel(privilege);
		
		return b || privilege==PRIVILEGE;
	}
}
