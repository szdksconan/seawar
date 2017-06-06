/**
 * 
 */
package foxu.sea.event;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.port.FightPort;
import mustang.io.ByteBuffer;

/**
 * 攻击事件显示序列化对象
 * 
 * @author rockzyt
 */
public class AttackEventWriter implements FightEventShowByteswriteable
{

	/* fields */

	/* methdos */
	public void showBytesWrite(FightEvent e,ByteBuffer data,int current,
		CreatObjectFactory objectFactory)
	{
		data.writeByte(e.getEventState());
		data.writeInt(e.id);
		data.writeInt(e.needTime+e.creatAt-current);
		data.writeInt(e.needTime);
		data.writeInt(e.attackIslandIndex);
		NpcIsland island=objectFactory
			.getIslandByIndexOnly(e.attackIslandIndex+"");
		// 新手引导特殊操作
		Player player=objectFactory.getPlayerById(e.getPlayerId());
		if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
		{
			island=(NpcIsland)NpcIsland.factory
							.getSample(FightPort.NEW_PLAYER_ATT_ISLAND);
		}
		data.writeInt(e.sourceIslandIndex);
		String beAttackName=" ";
		if(island.getPlayerId()==0)
		{
			data.writeShort(island.getSid());
		}
		else
		{
			data.writeShort(0);
			Player p=objectFactory.getPlayerById(island.getPlayerId());
			beAttackName=p.getName();
		}
		data.writeByte(e.getType());
		Player source = objectFactory.getPlayerById(e.getPlayerId());
		data.writeUTF(source.getName());
		data.writeUTF(beAttackName);
		e.getFleetGroup().showBytesWrite(data);
	}
}