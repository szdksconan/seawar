package foxu.sea.port;

import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.alliance.Alliance;
import foxu.sea.fight.Fleet;
import foxu.sea.worldboss.BossHurt;
import foxu.sea.worldboss.WorldBoss;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.util.TimeKit;

public class WorldBossPort extends AccessPort
{

	/** 展示联盟数 */
	public static final int MAX_ALLIANCE_NUM=10;
	/** 获取指定boss数据type=1 查看存活BOSS信息type=2*/
	public static final int VIEW_BOSS=1,GET_BOSS_INFO=2;

	CreatObjectFactory objectFactory;

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

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
		if(type==VIEW_BOSS)
		{
			int index=data.readInt();
			NpcIsland island=(NpcIsland)objectFactory
				.getIslandByIndexOnly(index+"");
			if(island==null||island.getIslandType()!=NpcIsland.WORLD_BOSS)
			{
				throw new DataAccessException(0,"player is null");
			}
			WorldBoss boss=objectFactory.getWorldBossBySid(island.getSid());
			if(boss==null)
			{
				throw new DataAccessException(0,"boss is null");
			}
			data.clear();
			int time=0;
			// 判断玩家时间
			if(player.getAttributes(PublicConst.ATTACK_BOSS_TIME)!=null
				&&!player.getAttributes(PublicConst.ATTACK_BOSS_TIME)
					.equals(""))
			{
				time=Integer.parseInt(player
					.getAttributes(PublicConst.ATTACK_BOSS_TIME));
				time=time-TimeKit.getSecondTime();
				if(time<0) time=0;
			}
			data.writeInt(time);
			// 写给前台，如果boss还有船，最少为1%
			int fleetNum=boss.getFleetNowNum()*100/boss.getFleetMaxNum();
			if(boss.getFleetNowNum()>0&&fleetNum==0)
				fleetNum=1;
			data.writeByte(fleetNum);
			int num=0;
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(
						player.getAttributes(PublicConst.ALLIANCE_ID)+"");
				if(alliance!=null)
				{
					num=boss.allianceAttackNum(alliance.getId());
				}
			}
			data.writeInt(num);
			float floatNum=0.00f;
			floatNum=(float)num*100/(float)boss.getFleetMaxNum();
			floatNum=(float)(Math.round(floatNum*100))/100;
			data.writeFloat(floatNum);
			Object objectAlliances[]=boss.getHurtMostAllianceId(objectFactory);
			int length=objectAlliances.length;
			if(length>=MAX_ALLIANCE_NUM) length=10;
			data.writeByte(length);
			if(objectAlliances!=null&&objectAlliances.length>0)
			{
				for(int i=0;i<objectAlliances.length;i++)
				{
					if(i>=10) break;
					BossHurt bosshurt=(BossHurt)objectAlliances[i];
					data.writeByte(boss.sortNum(bosshurt.getId()));
					String allianceName="";
					num=0;
					Alliance alliance=(Alliance)objectFactory
						.getAllianceMemCache().loadOnly(
							bosshurt.getId()+"");
					if(alliance!=null)
					{
						allianceName=alliance.getName();
						num=boss.allianceAttackNum(alliance.getId());
					}
					data.writeUTF(allianceName);
					data.writeInt(num);
					floatNum=(float)num*100/(float)boss.getFleetMaxNum();
					floatNum=(float)(Math.round(floatNum*100))/100;
					data.writeFloat(floatNum);
				}

			}
			// buff类型
			Service service=(Service)Service.factory.newSample(boss
				.getServiceSid());
			data.writeByte(service.getServiceType());
			data.writeInt(service.getServiceTime()/60);
			Fleet fleet[]=boss.getFleetGroup().getArray();
			for(int i=0;i<fleet.length;i++)
			{
				num=0;
				if(fleet[i]!=null&&fleet[i].getNum()>0)
				{
					num=fleet[i].getNum();
				}
				data.writeShort(num);
			}
		}
		else if(type==GET_BOSS_INFO)
		{
			objectFactory.getWorldBossCache().getBossInfo(data);
		}
		return data;
	}
}
