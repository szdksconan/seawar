package foxu.sea.arena;

import mustang.back.BackKit;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceAutoTransfer;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.comrade.Comrade;
import foxu.sea.event.FightEvent;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.AlliancePort;

/***
 * ɾ����ҹ�����
 * 
 * @author lhj
 * 
 */
public class ClearPlayerInfoManager
{

	CreatObjectFactory objectFactory;

	/** ���˻᳤�Զ��ƽ������� **/
	AllianceAutoTransfer allianceTransfer;

	/** ��ս������ **/
	AllianceFightManager afightManager;

	/** ɾ�������Ϣ **/
	public boolean deletePlayerInfo(int[] playerIds)
	{
		if(playerIds==null||playerIds.length==0) return true;
		for(int i=0;i<playerIds.length;i++)
		{
			Player player=objectFactory.getPlayerCache().load(playerIds[i]+"");
			if(player==null) continue;
			// �������
			LeaveAlliance(player);
			// �����ļ��Ϣ
			clearComradeInfo(player);
			/** ɾ�����첢������¼� */
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
				player.getId());
			// �������
			if(island!=null)
			{
				ArrayList list=objectFactory.getEventCache()
					.getFightEventListById(island.getIndex());
				clearEvent(list,player,objectFactory);
				objectFactory.getIslandCache().removePlayerIslandMap(
					player.getId());
				island.setPlayerId(0);
				objectFactory.getIslandCache().getDbaccess().save(island);
				objectFactory.getIslandCache().putSpaceIsland(island);
			}
			/** ������Ѻͺ����� */
			reMoveFriendorBlackList(player);
		}
		return true;
	}
	/** �Ƴ����ӵ�и���ҵĺ��ѻ����Ǻ����� */
	public void reMoveFriendorBlackList(Player player)
	{
		IntKeyHashMap map=objectFactory.getPlayerCache().getCacheMap();
		Object object[]=map.valueArray();
		if(object==null||object.length==0) return;
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			PlayerSave playerSave=(PlayerSave)object[i];
			// �Ƴ�������
			RemoveBlackFriednList(playerSave.getData(),player.getName(),
				PublicConst.BLACK_LIST);
			// �Ƴ�����
			RemoveBlackFriednList(playerSave.getData(),player.getName(),
				PublicConst.FRIENDS_LIST);
		}
	}
	/** �Ƴ����ѻ����Ǻ��������������Ƶ� **/
	public void RemoveBlackFriednList(Player player,String name,String key)
	{
		String black=player.getAttributes(key);
		if(black!=null&&black.length()!=0)
		{
			String[] blacks=black.split(",");
			boolean isHave=false;
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<blacks.length;i++)
			{
				if(blacks[i].equals(name))
					isHave=true;
				else
				{
					if(sb.length()==0)
						sb.append(blacks[i]);
					else
						sb.append(",").append(blacks[i]);
				}
			}
			if(isHave) player.setAttribute(key,sb.toString());
		}
	}

	/** ���ս���¼� **/
	public void clearEvent(ArrayList list,Player player,
		CreatObjectFactory factory)
	{
		if(list==null) return;
		for(int i=0;i<list.size();i++)
		{
			FightEvent event=(FightEvent)list.get(i);
			if(event==null) continue;
			if(event.getPlayerId()==player.getId())
			{
				int bindex=event.getAttackIslandIndex();
				NpcIsland beisland=factory.getIslandByIndex(bindex+"");
				ArrayList belist=factory.getEventCache()
					.getFightEventListById(bindex);
				Player beplayer=factory
					.getPlayerById(beisland.getPlayerId());
				if(belist!=null&&beplayer!=null)
				{
					belist.remove(event);
					JBackKit.deleteFightEvent(beplayer,event);
				}
				if(event.getFleetGroup()!=null)
				{
					event.getFleetGroup().cancel(player,false);
				}
				event.setDelete(FightEvent.DELETE_TYPE);
				// ����ı��б� �ȴ�����
				factory.getEventCache().load(event.getId()+"");
				JBackKit.deleteFightEvent(player,event);
			}
		}
		list.clear();
	}

	/** �˳����� */
	public void LeaveAlliance(Player player)
	{
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.loadOnly(player.getAttributes(PublicConst.ALLIANCE_ID));
		if(alliance==null)
		{
			player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
			return;
		}
		if(alliance.getPlayerList().size()==1
			&&alliance.isHavePlayer(player.getId()))
		{
			dismissAlliance(player);
			return;
		}
		// ����ǻ᳤ ������
		if(alliance.getMasterPlayerId()==player.getId())
			allianceTransfer.masterTransfer(alliance,TimeKit.getSecondTime());
		if(!alliance.isHavePlayer(player.getId())) return;
		// �����¼�
		AllianceEvent event=new AllianceEvent(
			AllianceEvent.ALLIANCE_EVENT_PLAYER_LEFT,player.getName(),
			player.getName(),"",TimeKit.getSecondTime());
		alliance.addEvent(event);
		// ��ս���
		afightManager.exitAlliance(player,alliance.getId());
		alliance.removePlayerId(player.getId());
		player.setAttribute(PublicConst.ALLIANCE_ID,null);
		player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,null);
		player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
		player.getAllianceList().clear();
		//��¼��ǰ��ҵ�����ֵ
		SeaBackKit.leaveAllianceRecord(player,alliance);
		//�Ƴ���ҵ�����������Ϣ
		SeaBackKit.removeAllianceRank(player,alliance);
		player.resetAdjustment();
	}

	/** ��ɢ���� **/
	public void dismissAlliance(Player player)
	{
		String str=checkAlliance(player);
		if(str!=null) return;
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.load(player.getAttributes(PublicConst.ALLIANCE_ID));
		// ��ս���
		alliance.dismiss(objectFactory);
		AlliancePort alliancePort=(AlliancePort)BackKit.getContext().get(
			"alliancePort");
		alliancePort.flushFileds(true);
		afightManager.dismissAlliance(alliance);
	}
	/** ���˳����ж� */
	public String checkAlliance(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(allianceId==null||allianceId.equals(""))
		{
			return "you have no alliance";
		}
		// ���˲�����
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.load(allianceId);
		if(alliance==null)
		{
			return "alliance has been dismiss";
		}
		return null;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	/** ���˻᳤�Զ��ƽ������� **/
	public void setAllianceTransfer(AllianceAutoTransfer allianceTransfer)
	{
		this.allianceTransfer=allianceTransfer;
	}

	/** ��ս������ **/
	public void setAfightManager(AllianceFightManager afightManager)
	{
		this.afightManager=afightManager;
	}

	/**�����ļ��Ϣ**/
	public void clearComradeInfo(Player player)
	{
		Comrade comrade=player.getComrade();
		if(comrade==null) return;
		int veterand=comrade.getVeteranId();
		if(veterand!=0)
		{
			Player vplayer=objectFactory.getPlayerCache().load(veterand+"");
			if(vplayer!=null)
			{
				vplayer.getComrade().getRecruitIds().remove(player.getId());
				comrade.setVeteranId(0);
			}
		}
		IntList list=comrade.getRecruitIds();
		if(list!=null&&list.size()!=0)
		{
			for(int i=0;i<list.size();i++)
			{
				Player tplayer=objectFactory.getPlayerCache().load(
					veterand+"");
				if(tplayer!=null)
				{
					tplayer.getComrade().setVeteranId(0);
				}
			}
			comrade.setRecruitIds(new IntList());
		}
	}
}
