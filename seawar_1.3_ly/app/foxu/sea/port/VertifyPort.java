package foxu.sea.port;

import mustang.codec.MD5;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.vertify.Vertify;
import foxu.sea.vertify.VertifyManager;

/**
 * 验证操作通知 端口 port:1030
 */
public class VertifyPort extends AccessPort
{

	VertifyManager vertifyManager;
	static final String vertifyKey="nkyntCuBgsNIEGujwH2fHruYUx3YrnXqk0HS1GQbJJqltMlBMggWHymfiurt6kud";

	/**
	 * 类型type SPY_FIELD=1 侦查野外岛屿
	 */
	private final int SPY_FIELD = 1;

	public void setVertifyManager(VertifyManager vertifyManager)
	{
		this.vertifyManager=vertifyManager;
	}

	@Override
	public ByteBuffer access(Connect c,ByteBuffer data)
	{
		if(c.getSource()==null)
		{
			c.close();
			return null;
		}
		Session s=(Session)c.getSource();
		Player player=(Player)s.getSource();
		if(player==null) 
		{
			c.close();
			return null;
		}
		// 验证成功结果
		int type=data.readUnsignedByte();
		boolean result=data.readBoolean();
		String key=data.readUTF();
		StringBuffer sb=new StringBuffer();
		sb.append(player.getId());	//用户id
		if(result)
			sb.append(1);
		else
			sb.append(0);
		sb.append(vertifyKey);		//key
		MD5 md5=new MD5();
		String md5Str=md5.encode(sb.toString());
		if(!key.toLowerCase().equals(md5Str.toLowerCase()))
			throw new DataAccessException(0,"vertify key exception");
		Vertify vertify=(Vertify)vertifyManager.player_vertify.get(player
			.getId());
		if(vertify==null) return null;
		int wrongCount=vertify.getVertifyWrongCount();
		if(type== SPY_FIELD)// 侦查扫矿 1
		{
			if(result)
			{
				vertify.setVertifyWrongCount(1);
				vertify.setOperateCount(0);
				vertify.setOperateTime(TimeKit.getSecondTime());
			}
			else
			{
				if(++wrongCount >= 5)
					wrongCount = 5;
				vertify.setVertifyWrongCount(wrongCount);
			}
		}
		vertifyManager.player_vertify.put(player.getId(),vertify);
		data.clear();
		data.writeInt(vertify.getVertifyWrongCount());
		data.writeInt(PublicConst.VERTIFY_TIME);
		data.writeInt(PublicConst.VERTIFY_MAX_COUNT);
		return data;
	}

}
