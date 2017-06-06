/**
 * 
 */
package foxu.sea.event;

import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.FightPort;

/**
 * @author rockzyt
 */
public class StayEventShowWriter implements FightEventShowByteswriteable
{

	/* fields */

	/* methods */
	public void showBytesWrite(FightEvent e,ByteBuffer data,int current,
		CreatObjectFactory objectFactory)
	{
		data.writeByte(e.getEventState());
		data.writeInt(e.id);
		Player player=objectFactory.getPlayerById(e.getPlayerId());
		NpcIsland island=objectFactory
			.getIslandByIndexOnly(e.attackIslandIndex+"");
		// 根据当前资源计算驻守时间
		int time=island.pushTime(e.getResources());
		// 新手引导特殊操作
		int islandSid=island.getSid();
		int carryResource=SeaBackKit.groupCarryResource(e.getFleetGroup());
		if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
		{
			islandSid=FightPort.NEW_PLAYER_ATT_ISLAND;
			island=(NpcIsland)NpcIsland.factory.getSample(islandSid);
			carryResource=FightPort.NEW_PLAYER_HOLD_RESOURCE;
		}
		data.writeInt(current-e.creatAt+time);
		data.writeInt(island.getPluderResource());
		data.writeInt(e.attackIslandIndex);
		int etime=0;
		if(island.getPlayerId()==0)
		{
			data.writeShort(islandSid);
			if(island.getIslandType()==NpcIsland.ISLAND_GEMS)
			{
				island.checkBuff(TimeKit.getSecondTime()-e.getCreatAt(),objectFactory);
				int carryResource1=(island.getEndTime()-e.getCreatAt())/(PublicConst.LOWLIMIT_GEMS_TIMES*60)+(e.getResources()[Resources.GEMS]/PublicConst.LOWLIMIT_GEMS_TIMES);
				carryResource=carryResource/PublicConst.LOWLIMIT_GEMS_TIMES;
				if(carryResource1<carryResource)  carryResource=carryResource1;
				carryResource=carryResource>0?carryResource:0;
				etime=island.getEndTime();
			}
		}
		else
		{
			data.writeShort(0);
			data.writeUTF(player.getName());
			Player beplayer=objectFactory
				.getPlayerById(island.getPlayerId());
			data.writeUTF(beplayer.getName());
			if(island.getIslandType()==NpcIsland.ISLAND_GEMS)
			{
				island.checkBuff(TimeKit.getSecondTime()-e.getCreatAt(),objectFactory);
				int carryResource1=(island.getEndTime()-e.getCreatAt())/(PublicConst.LOWLIMIT_GEMS_TIMES*60)+(e.getResources()[Resources.GEMS]/PublicConst.LOWLIMIT_GEMS_TIMES);
				carryResource=carryResource/PublicConst.LOWLIMIT_GEMS_TIMES;
				if(carryResource1<carryResource)  carryResource=carryResource1;
				carryResource=carryResource>0?carryResource:0;
				etime=island.getEndTime();
			}
		}
		data.writeInt(carryResource);
		data.writeInt(etime);
		data.writeInt(e.getCreatAt());
		SeaBackKit.getBuff(island.getServices(),data);
		// 军队序列化
		e.getFleetGroup().showBytesWrite(data);
		// 采集加成比例
		data.writeShort(SeaBackKit.groupResourceExtraAddition(player,e.getFleetGroup(),island.getIslandType()));
	}
}