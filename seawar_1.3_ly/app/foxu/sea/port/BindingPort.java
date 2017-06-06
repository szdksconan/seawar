package foxu.sea.port;

import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.bind.TelBindingManager;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;

/**
 * °ó¶¨¶Ë¿Ú 1036
 * 
 * @author Alan
 * 
 */
public class BindingPort extends AccessPort
{

	public static final int DELETE_TEL=65,ADD_OR_UPDATE_TEL=66,ADD_REQUEST_RECORD=67;
	TelBindingManager telBindingManager;

	@Override
	public ByteBuffer access(Connect c,ByteBuffer data)
	{
		if(c.getSource()==null)
		{
			c.close();
			return null;
		}
		Session s=(Session)(c.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			c.close();
			return null;
		}
		int type=data.readUnsignedByte();
		if(type==ADD_OR_UPDATE_TEL)
		{
			String newPhone=data.readUTF();
			String newZone=data.readUTF();
			String code=data.readUTF();
			data.clear();
			String msg=telBindingManager.updatePlayerTel(player,newZone,newPhone,code);
			if(msg!=null) throw new DataAccessException(0,msg);
			data.writeUTF(newPhone);
			data.writeUTF(newZone);
		}
		else if(type==DELETE_TEL)
		{
			String code=data.readUTF();
			data.clear();
			String msg=telBindingManager.deletePlayerTel(player,code);
			if(msg!=null) throw new DataAccessException(0,msg);
		}
		else if(type==ADD_REQUEST_RECORD)
		{
			String phone=data.readUTF();
			String zone=data.readUTF();
			data.clear();
			telBindingManager.addRequestRecord(player,zone,phone);
		}
		return data;
	}

	public TelBindingManager getTelBindingManager()
	{
		return telBindingManager;
	}

	public void setTelBindingManager(TelBindingManager telBindingManager)
	{
		this.telBindingManager=telBindingManager;
	}

}
