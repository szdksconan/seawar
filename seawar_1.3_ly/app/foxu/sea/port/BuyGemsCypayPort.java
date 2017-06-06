package foxu.sea.port;

import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.ValidCurrencyExpection;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.SeaBackKit;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;


/**
 * @author yw
 * cypay 充值端口
 */
public class BuyGemsCypayPort extends AccessPort
{
	public static final int SYSTEM_STATE=1,BUG_GEMS=2,CANCEL_ORDER=3;
	
	CreatObjectFactory objectFactory;
	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		if(type==SYSTEM_STATE)
		{
			int gems=data.readUnsignedShort();
			String cur=data.readUTF();
			if(isValid(cur))
			{
				throw new ValidCurrencyExpection(cur);
			}
			boolean need_order=data.readBoolean();
			int orderid=-1;
			if(need_order)
			{
				orderid=SeaBackKit.sendHttpData(null,null,2,12).readInt();
			}
			objectFactory.createGemTrack(GemsTrack.SUBMIT_ORDER,
				player.getId(),gems,0,Resources.getGems(player.getResources()));
			data.clear();
			data.writeInt(orderid);
			data.writeUTF(UserToCenterPort.AREA_ID+"_"+UserToCenterPort.SERVER_ID);
			return data;
		}
		else if(type==CANCEL_ORDER)
		{
			objectFactory.createGemTrack(GemsTrack.CANCEL_ORDER,
				player.getId(),0,0,Resources.getGems(player.getResources()));
			data.clear();
			return data;
		}
		return null;
	}
	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	/** 是否为禁用货币 */
	public boolean isValid(String cur)
	{
		if(PublicConst.VALID_CURRENCY==null) return false;
		for(int i=0;i<PublicConst.VALID_CURRENCY.length;i++)
		{
			if(cur.equals(PublicConst.VALID_CURRENCY[i])) return true;
		}
		return false;
	}
	
	

}
