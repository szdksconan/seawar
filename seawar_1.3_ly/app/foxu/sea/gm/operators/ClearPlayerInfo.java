package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.event.FightEvent;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/***
 * �߼�ɾ�������Ϣ
 * 
 * @author lhj
 * 
 */
public class ClearPlayerInfo extends GMOperator
{

	/** ��ս */
	AllianceFightManager afightManager;
	/** DELETE_PLAYER=1 ɾ����� RECOVER_PLAYER=2 �޸���� */
	public final int DELETE_PLAYER=1,RECOVER_PLAYER=2;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String playername=params.get("player_name");
		String types=params.get("type");
		if(playername==null||playername.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		//ɾ�����
		if(Integer.parseInt(types)==DELETE_PLAYER)
		{
			Player player=objectFactory.getPlayerByName(playername,true);
			if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
			if(TextKit.parseInt(player
				.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE)
				return GMConstant.ERR_DELETE_ERRO;
			// �������
			if(LeaveAlliance(player,objectFactory)>0)
				return LeaveAlliance(player,objectFactory);
			/** ɾ�����첢������¼� */
			NpcIsland island=objectFactory.getIslandCache().getPlayerIsland(
				player.getId());
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
			Session[] sessions=objectFactory.getDsmanager().getSessionMap()
				.getSessions();
			reMoveFriendorBlackList(sessions,player);
			User users=objectFactory.getUserDBAccess().load(
				player.getUser_id()+"");
			users.delete(player);
			objectFactory.getUserDBAccess().save(users);
			player.setAttribute(PublicConst.PLAYER_DELETE_FLAG,String.valueOf(PublicConst.DELETE_STATE));
			Session session=(Session)player.getSource();
			if(session!=null)
			{
				Connect c=session.getConnect();
				if(c!=null&&c.isActive()) session.getConnect().close();
			}
		}
		//�޸��������
		else if(Integer.parseInt(types)==RECOVER_PLAYER)
		{
			Player player=objectFactory.getPlayerByName(playername,true);
			if(player==null) return GMConstant.ERR_PLAYER_NOT_EXISTS;
			if(TextKit.parseInt(player
				.getAttributes(PublicConst.PLAYER_DELETE_FLAG))!=PublicConst.DELETE_STATE)
				return GMConstant.ERR_COVER_PLAYERNAME;
			User users=objectFactory.getUserDBAccess().load(
				player.getUser_id()+"");
			users.recover(player);
			objectFactory.getUserDBAccess().save(users);
			player.setAttribute(PublicConst.PLAYER_DELETE_FLAG,String.valueOf(PublicConst.RECOVER_PLAYER_STATE));
		}
		else
			return GMConstant.ERR_UNKNOWN;
		return GMConstant.ERR_SUCCESS;
	}

	/** �Ƴ����ӵ�и���ҵĺ��ѻ����Ǻ����� */
	public void reMoveFriendorBlackList(Session[] sessions,Player player)
	{
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null||sessions[i].getSource()==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player bePlayer=(Player)sessions[i].getSource();
			// �Ƴ�������
			RemoveBlackFriednList(bePlayer,player.getName(),
				PublicConst.BLACK_LIST);
			// �Ƴ�����
			RemoveBlackFriednList(bePlayer,player.getName(),
				PublicConst.FRIENDS_LIST);

		}

	}
	/***
	 * �Ƴ����ѻ����Ǻ��������������Ƶ�
	 * 
	 * @param player
	 * @param name
	 * @param key
	 */
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

	/**�˳�����*/
	public int LeaveAlliance(Player player,CreatObjectFactory objectFactory)
	{
		Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
			.loadOnly(player.getAttributes(PublicConst.ALLIANCE_ID));
		if(alliance==null)
		{
			player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
			return GMConstant.ERR_SUCCESS;
		}
		if(alliance.getMasterPlayerId()==player.getId())
			return GMConstant.ERR_PLAYER_IS_MASTER;
		if(!alliance.isHavePlayer(player.getId()))
			return GMConstant.ERR_SUCCESS;
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
		player.resetAdjustment();
		//��¼��ǰ��ҵ�����ֵ
		SeaBackKit.leaveAllianceRecord(player,alliance);
		//�Ƴ���ҵ�����������Ϣ
		SeaBackKit.removeAllianceRank(player,alliance);
		return GMConstant.ERR_SUCCESS;
	}

	public void setAfightManager(AllianceFightManager afightManager)
	{
		this.afightManager=afightManager;
	}

}
