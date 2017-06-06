package foxu.sea.port;

import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.event.FightEvent;
import foxu.sea.gems.GemsTrack;
import foxu.sea.island.SpaceIslandContainer;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.task.TaskEventExecute;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.util.TimeKit;

/** �����Ǩ����port 1015 */
public class MoveIslandPort extends AccessPort
{

	/** ��Ǩ���� */
	public final static int RANDOM_MOVE=1,HOLD_MOVE=2,HOLD_MOVE_2=3;
	/** ��Ʒsid */
	public final static int PORP_SID_1=1005,PORP_SID_2=1006;
	/** ���ݻ�ȡ�� */
	CreatObjectFactory objectFactory;
	/** �յ������� */
	SpaceIslandContainer islandContainer;

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayerOnly(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		objectFactory.pushAll(player,TimeKit.getSecondTime());
		int type=data.readUnsignedByte();
		NpcIsland foreIsland=null;
		NpcIsland island=null;
		// �����Ǩ
		if(type==RANDOM_MOVE)
		{
			// �Ƿ�����Ʒ
			Prop prop=player.getBundle().getPropById(PORP_SID_1);
			if(prop==null||((NormalProp)prop).getCount()<=0)
			{
				throw new DataAccessException(0,"prop is null");
			}
			// �Ƿ��н��ӳ�����
			if(isFightShipOut(player))
			{
				throw new DataAccessException(0,"ship is out side");
			}
			// ���ѡȡ����
			island=islandContainer.randomIsLand();
			if(island==null)
				throw new DataAccessException(0,"the world is full");
			// ��ԭ�ȵĵ���playerid����Ϊ��
			foreIsland=objectFactory.getIslandCache()
				.getPlayerIsLandAndChange(player.getId());
			foreIsland.setPlayerId(0);
			// ���ĵ���
			player.getBundle().decrProp(PORP_SID_1);
			// ��ȡ�µ���
			island.setPlayerId(player.getId());
			// �µ������ı��б�
			objectFactory.getIslandCache().load(island.getIndex()+"");
			objectFactory.getIslandCache().removePlayerIslandMap(
				player.getId());
			objectFactory.getIslandCache().addPlayerIsLandMap(island);
			//���������ӿյ����б����Ƴ�
			objectFactory.getIslandCache().removeSpaceIsland(island);
			// ����������ĵ���index
			data.clear();
			data.writeInt(island.getIndex());
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),island,
				objectFactory);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),foreIsland,
				objectFactory);
		}
		// ָ����Ǩ
		else if(type==HOLD_MOVE)
		{
			// �Ƿ�����Ʒ
			Prop prop=player.getBundle().getPropById(PORP_SID_2);
			if(prop==null||((NormalProp)prop).getCount()<=0)
			{
				throw new DataAccessException(0,"prop is null");
			}
			// ���н���������
			if(isFightShipOut(player))
			{
				throw new DataAccessException(0,"ship is out side");
			}
			// Ҫ��Ǩ���õ���
			int index=data.readInt();
			if(index<0)
			{
				throw new DataAccessException(0,"index is wrong");
			}
			island=objectFactory.getIslandByIndex(index+"");
			if(island==null)
			{
				throw new DataAccessException(0,"island is null");
			}
			else if(island.getPlayerId()!=0)
			{
				throw new DataAccessException(0,"have player already");
			}
			else if(island.getTempAttackEventId()!=0)
			{
				throw new DataAccessException(0,"have player hold event");
			}
			else if(island.getIslandType()!=NpcIsland.ISLAND_WARTER)
			{
				throw new DataAccessException(0,"needs_of_the_water");
			}
			else if(isIslandBeAttacked(objectFactory,island.getIndex()))
			{
				throw new DataAccessException(0,"island_is_being_attacked");
			}
			// ��ԭ�ȵĵ���playerid����Ϊ��
			foreIsland=objectFactory.getIslandCache()
				.getPlayerIsLandAndChange(player.getId());
			foreIsland.setPlayerId(0);
			island.setPlayerId(player.getId());
			// ���ĵ���
			player.getBundle().decrProp(PORP_SID_2);
			// �µ������ı��б�
			objectFactory.getIslandCache().load(island.getIndex()+"");
			objectFactory.getIslandCache().removePlayerIslandMap(
				player.getId());
			objectFactory.getIslandCache().addPlayerIsLandMap(island);
			//���������ӿյ����б����Ƴ�
			objectFactory.getIslandCache().removeSpaceIsland(island);
			// ����������ĵ���index
			data.clear();
			data.writeInt(island.getIndex());
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),island,
				objectFactory);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),foreIsland,
				objectFactory);
		}
		// ָ����Ǩ�����ı�ʯ
		else if(type==HOLD_MOVE_2)
		{
			// �Ƿ����㹻�ı�ʯ
			Prop prop=(Prop)Prop.factory.newSample(PORP_SID_2);
			int needGems=prop.getNeedGems();
			if(!Resources.checkGems(needGems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			// ���н���������
			if(isFightShipOut(player))
			{
				throw new DataAccessException(0,"ship is out side");
			}
			// Ҫ��Ǩ���õ���
			int index=data.readInt();
			if(index<0)
			{
				throw new DataAccessException(0,"index is wrong");
			}
			island=objectFactory.getIslandByIndex(String.valueOf(index));
			if(island==null)
			{
				throw new DataAccessException(0,"island is null");
			}
			else if(island.getPlayerId()!=0)
			{
				throw new DataAccessException(0,"have player already");
			}
			else if(island.getTempAttackEventId()!=0)
			{
				throw new DataAccessException(0,"have player hold event");
			}
			else if(island.getIslandType()!=NpcIsland.ISLAND_WARTER)
			{
				throw new DataAccessException(0,"needs_of_the_water");
			}
			else if(isIslandBeAttacked(objectFactory,island.getIndex()))
			{
				throw new DataAccessException(0,"island_is_being_attacked");
			}
			if(!Resources.reduceGems(needGems,player.getResources(),player))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			objectFactory.createGemTrack(GemsTrack.MOVE_ISLAND,player
				.getId(),needGems,0,Resources.getGems(player.getResources()));
			// ��ԭ�ȵĵ���playerid����Ϊ��
			foreIsland=objectFactory.getIslandCache()
				.getPlayerIsLandAndChange(player.getId());
			foreIsland.setPlayerId(0);
			island.setPlayerId(player.getId());
			// �µ������ı��б�
			objectFactory.getIslandCache().load(island.getIndex()+"");
			objectFactory.getIslandCache().removePlayerIslandMap(
				player.getId());
			objectFactory.getIslandCache().addPlayerIsLandMap(island);
			//���������ӿյ����б����Ƴ�
			objectFactory.getIslandCache().removeSpaceIsland(island);
			// ����������ĵ���index
			data.clear();
			data.writeInt(island.getIndex());
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),island,
				objectFactory);
			// ˢ��ǰ̨
			JBackKit.flushIsland(objectFactory.getDsmanager(),foreIsland,
				objectFactory);
		}
		// ˢ����ұ������¼�
		removeAttack(foreIsland,island,player);

		return data;
	}

	/**
	 * �ж�һ�������Ƿ񱻹���
	 * 
	 * @param objectFactory
	 * @param islandId
	 * @return
	 */
	private boolean isIslandBeAttacked(CreatObjectFactory objectFactory,
		int islandId)
	{
		Object object[]=FightKit.pushFightEvent(islandId,objectFactory);
		if(object!=null) FightKit.checkFightEvent(object,objectFactory);
		ArrayList eventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		return eventList!=null&&eventList.size()>0;
	}

	/** �Ƿ��н��ӳ��� */
	public boolean isFightShipOut(Player player)
	{
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null||fightEventList.size()<=0) return false;
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event.getAttackIslandIndex()==islandId
				&&event.getEventState()==FightEvent.HOLD_ON) return true;
			if(event.getPlayerId()==player.getId()) return true;
		}
		return false;
	}
	/** ˢ�� �Ƴ��ϵ��������¼� ����µ��������¼� */
	public void removeAttack(NpcIsland foreIsland,NpcIsland nowIsland,
		Player player)
	{
		ArrayList list=objectFactory.getEventCache().getFightEventListById(
			foreIsland.getIndex());
		FightEvent fevent=null;
		if(list!=null)
		{
			for(int i=list.size()-1;i>=0;i--)
			{
				fevent=(FightEvent)list.get(i);
				if(fevent==null) continue;
				if(fevent.getType()==FightEvent.ATTACK_BACK)
				{
					int delete=fevent.getDelete();
					fevent.setDynamicDelete(FightEvent.DELETE_TYPE);
					JBackKit.sendFightEvent(player,fevent,objectFactory);
					fevent.setDynamicDelete(delete);
				}
			}
		}

		list=objectFactory.getEventCache().getFightEventListById(
			nowIsland.getIndex());
		if(list!=null)
		{
			for(int i=list.size()-1;i>=0;i--)
			{
				fevent=(FightEvent)list.get(i);
				if(fevent==null) continue;
				if(fevent.getType()==FightEvent.ATTACK_BACK)
				{
					JBackKit.sendFightEvent(player,fevent,objectFactory);
				}
			}
		}

	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public SpaceIslandContainer getIslandContainer()
	{
		return islandContainer;
	}

	public void setIslandContainer(SpaceIslandContainer islandContainer)
	{
		this.islandContainer=islandContainer;
	}

}
