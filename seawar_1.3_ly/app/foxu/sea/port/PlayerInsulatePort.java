package foxu.sea.port;

import java.io.IOException;
import java.util.HashMap;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.User;
import foxu.sea.kit.SeaBackKit;

/****
 * 玩家隔离端口
 * 
 * @author lhj
 * 
 */
public class PlayerInsulatePort extends AccessPort
{

	/** 玩家隔离端口 **/
	public static int PLAYER_INSULATE=1;
	/** 玩家取消隔离 **/
	public static int SET_USER_STATE=12;

	CreatObjectFactory objectFactory;

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		int type=data.readUnsignedByte();
		// 玩家取消删除状态
		if(type==PLAYER_INSULATE)
		{
			String account=data.readUTF();
			String sql="select *from users where user_account='"+account+"'";
			User user=objectFactory.getUserDBAccess().loadUserBySql(sql);
			if(user!=null)
			{
				data.clear();
				data.writeByte(SET_USER_STATE);
				data.writeUTF(user.getUserAccount());
				data.writeInt(0);
				data.writeInt(UserToCenterPort.AREA_ID);
				try
				{
					data=sendHttpData(data);
				}
				catch(Exception e)
				{
					throw new DataAccessException(0,"center is close");
				}
				if(data==null)
					throw new DataAccessException(0,"center is close");
				if(!data.readBoolean())
					throw new DataAccessException(0,"user is null");
				user.setDeleteTime(0);
				objectFactory.getUserDBAccess().save(user);
			}
		}
		return null;
	}

	/** 向中心发送消息 **/
	public ByteBuffer sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// 设置port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,null);
		}
		catch(IOException e)
		{
			// e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}
