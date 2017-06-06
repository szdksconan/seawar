package foxu.ds;

import mustang.event.ChangeAdapter;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.text.TextKit;
import shelby.ds.DSManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.GameDataMemCache;
import foxu.push.AndroidPush;
import foxu.sea.Player;
import foxu.sea.PublicConst;

/**
 * ������ ����ds���ͳ�������Ϣ
 * 
 * @author rockzyt
 */
public class DSListener extends ChangeAdapter
{

	GameDataMemCache memCache;

	CreatObjectFactory objectFactory;

	public void change(Object source,int type,Object v1)
	{
		// ��¼��Ϣ
		if(type==DSManager.LOAD_CHANGED)
		{
			DSManager manager=(DSManager)source;
			int online=manager.getSessionMap().size();
			// �����������
			if(online>memCache.getSave().getMaxOnLine())
				memCache.getSave().setMaxOnLine(online);
			//���ӵ�½����
			memCache.incrLoginCount();
			Session session=((Session)v1);
			int id=Integer.parseInt(session.getId());
			Player player=objectFactory.getPlayerById(id);
			if(TextKit.parseInt(player
				.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE)
				session.getConnect().close();
			if(player.getPlatid()==Player.PLAT_ANDROID)
				AndroidPush.androidPush.clear(player);
		}
		else if(type==DSManager.PRE_LOAD_CHANGED)
		{
			Session session=((Session)v1);
			int id=Integer.parseInt(session.getId());
			Player player=objectFactory.getPlayerById(id);
			if(player!=null)
			{
				Object object=player.getSource();
				player.setSource(null);
				// ������������
//				 player.getTaskManager().checkTastEvent(null);
//				 objectFactory.pushAll(player,TimeKit.getSecondTime());
				player.setSource(object);
				if(TextKit.parseInt(player
					.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE)
					session.getConnect().close();
				if(player.getPlatid()==Player.PLAT_ANDROID)
					AndroidPush.androidPush.clear(player);
			}

		}
	}

	public void change(Object source,int type,Object v1,Object v2)
	{
		// ��������Ƿ������ܾ��쳣�������¼���Ự���Ա������û�����
		if(type==DSManager.EXIT_CHANGED)
		{
			if(v2!=null)
			{
				DataAccessException e=(DataAccessException)v2;
				if(e.getType()!=DataAccessException.SERVER_ACCESS_REFUSED)
					return;
			}
			Session s=(Session)v1;
			Player p=(Player)s.getSource();
			if(p==null) return;
			if(TextKit.parseInt(p
				.getAttributes(PublicConst.PLAYER_DELETE_FLAG))==PublicConst.DELETE_STATE) return;
			p.setSource(null);
			memCache.decrOnine(p.getPlat());
			p.incrOnlineTime();//��������ʱ��
			objectFactory.getPlayerByName(p.getName(),true);//��������ı��б�
		}
	}

	/**
	 * @param save Ҫ���õ� save
	 */
	public void setSave(GameDataMemCache memCache)
	{
		this.memCache=memCache;
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory Ҫ���õ� objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}