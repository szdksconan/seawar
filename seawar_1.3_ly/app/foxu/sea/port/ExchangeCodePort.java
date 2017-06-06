package foxu.sea.port;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.Session;


/**
 * ¶Ò»»Âë¶Ë¿Ú1021
 * @author yw
 *
 */
public class ExchangeCodePort extends AccessPort
{

	CreatObjectFactory objectFactory;
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	@Override
	public ByteBuffer access(Connect c,ByteBuffer data)
	{
		Player player=(Player)((Session)c.getSource()).getSource();
		int awawdSid=8;
		Award award=(Award)Award.factory.newSample(awawdSid);
		data.clear();
		award.awardLenth(data,player,objectFactory,null,
			new int[]{EquipmentTrack.FROM_EXCHANGECODE});
		return data;
	}

}
