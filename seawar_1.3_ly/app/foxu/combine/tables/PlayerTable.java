package foxu.combine.tables;

import java.util.ArrayList;
import java.util.Iterator;

import shelby.dc.GameDBAccess;
import mustang.field.Fields;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.combine.CombineManager;
import foxu.combine.DataTable;
import foxu.combine.Server;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.builds.BuildInfo;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.proplist.UniqueProp;

/**
 * players��<br />
 * �������ݣ����뵽PlayerMemCache��changeCache���ٴ������ݿ�
 * 
 * @author comeback
 */
public class PlayerTable extends DataTable
{

	private static final Logger log=LogFactory.getLogger(PlayerTable.class);

	/** �Ƴ���ұ���ʱ�䣬����������ʱ��Ĳ��Ƴ�(�ݶ�7��) */
	public static final int PROTECTED_TIME=7*24*3600;

	/** �Ƴ����ָ�����ı����ȼ������벻��������ȼ��ſ��Ա��Ƴ� */
	public static final int PROTECTED_LEVEL=30;

	/** �û��� */
	UserTable userTable;
	
	/** �����б� */
	AllianceTable allianceTable;

	/** �Ƴ�ָ��ʱ��ǰδ��¼������ */
	int removeTime=30*24*3600;

	/** �Ƴ�ָ���ȼ����µ���ң������趨�ȼ� */
	int removeLevel=5;

	/** ���������������� */
	ArrayList<Player> playerList3=new ArrayList<Player>();

	public void setUserTable(UserTable userTable)
	{
		this.userTable=userTable;
	}
	
	public void setAllianceTable(AllianceTable allianceTable)
	{
		this.allianceTable=allianceTable;
	}

	public void setRemoveTime(int time)
	{
		this.removeTime=time;
	}
	
	public void setRemoveLevel(int level)
	{
		this.removeLevel=level;
	}
	
	private void addProps(Server s,Player player)
	{
		int[] addProps=s.getAddProps();
		if(addProps==null||addProps.length<2) return;
		for(int i=0;i<addProps.length;i+=2)
		{
			int sid=addProps[i];
			int count=addProps[i+1];
			Prop prop=(Prop)Prop.factory.newSample(sid);
			if(prop instanceof NormalProp)
			{
				NormalProp np=(NormalProp)prop;
				int maxCount=np.getMaxCount();
				while(count>=maxCount)
				{
					NormalProp np1=(NormalProp)Prop.factory.newSample(sid);
					np1.setCount(maxCount);
					player.getBundle().incrProp(np1,true);
					count-=maxCount;
				}
				if(count>0)
				{
					np.setCount(count);
					player.getBundle().incrProp(prop,true);
				}
			}
			else if(prop instanceof UniqueProp)
			{
				do
				{
					player.getBundle().incrProp(prop,true);
					count--;
					if(count<=0) break;
					prop=(Prop)Prop.factory.newSample(sid);
				}
				while(true);
			}
		}
	}

	private void addGems(Server s,Player player)
	{
		int addGems=s.getAddGems();
		if(addGems<=0) return;
		Resources.addGemsNomal(addGems,player.getResources(),player);
	}

	public void updateUserId1(int oldId,int newId)
	{
		Iterator<Object> iter=getList1().iterator();
		while(iter.hasNext())
		{
			Player p=(Player)iter.next();
			if(p.getUser_id()==oldId)
			{
				p.setUser_id(newId);
				playerList3.add(p);
				//break;
			}
		}
	}

	public void updateUserId2(int oldId,int newId)
	{
		Iterator<Object> iter=getList2().iterator();
		while(iter.hasNext())
		{
			Player p=(Player)iter.next();
			if(p.getUser_id()==oldId)
			{
				p.setUser_id(newId);
				playerList3.add(p);
				//break;
			}
		}
	}

	public void savePlayerList3(Server s3)
	{
		GameDBAccess dbAccess=getDBAccess();
		Persistence p=dbAccess.getGamePersistence();
		dbAccess.setGamePersistence(s3.getPersistence(getTableName()));
		Iterator<Player> iter=playerList3.iterator();
		while(iter.hasNext())
		{
			Player player=iter.next();
			dbAccess.save(player);
		}
		dbAccess.setGamePersistence(p);
	}

	public ArrayList<Player> savePlayers(ArrayList<Player> list,Server server)
	{
		GameDBAccess dbAccess=getDBAccess();
		ArrayList<Player> unsaved=new ArrayList<Player>();
		Persistence p=dbAccess.getGamePersistence();
		dbAccess.setGamePersistence(server.getPersistence(getTableName()));
		Iterator<Player> iter=playerList3.iterator();
		while(iter.hasNext())
		{
			Player player=iter.next();
			if(!dbAccess.save(player))
			{
				unsaved.add(player);
			}
		}
		dbAccess.setGamePersistence(p);
		return unsaved;
	}

	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		// ��������
		int nowTime=TimeKit.getSecondTime();
		Iterator<Object> iter=list1.iterator();
		while(iter.hasNext())
		{
			Player p=(Player)iter.next();
			String removeStr=p.getAttributes(CombineManager.CANOT_REMOVE_PLAYER);
			p.setAttribute(CombineManager.CANOT_REMOVE_PLAYER,null);
			boolean canotRemove="true".equals(removeStr);
			if(removeTime>=PROTECTED_TIME&&removeLevel<PROTECTED_LEVEL&&!canotRemove)
			{
				// �Ƴ�����¼ʱ�����ָ��ʱ�䣬vip�ȼ�Ϊ0,�ȼ�������ָ���ȼ�
				if(nowTime-p.getUpdateTime()>=removeTime
					&&p.getUser_state()==0
					&&p.getIsland().getBuildByIndex(BuildInfo.INDEX_0,null)
						.getBuildLevel()<=removeLevel
					&&p.getResources()[Resources.MAXGEMS]<=0)
				{
					// �Ƴ������б� �Ƴ����������б�
					allianceTable.removePlayerFromAlliance(p.getId(),1);
					// �Ƴ�����
					removeFriend(list1,p);
					// ����Ҫ��player id�����ܴ�user id����Ϊһ��user���ܻ�������player ���
					userTable.removeUserByPlayerId(p.getId(),1);
					iter.remove();
					continue;
				}
			}
			// ��Ӳ���
			addProps(s1,p);
			addGems(s1,p);
		}
		iter=list2.iterator();
		while(iter.hasNext())
		{
			Player p=(Player)iter.next();
			String removeStr=p.getAttributes(CombineManager.CANOT_REMOVE_PLAYER);
			p.setAttribute(CombineManager.CANOT_REMOVE_PLAYER,null);
			boolean canotRemove="true".equals(removeStr);
			if(removeTime>=PROTECTED_TIME&&removeLevel<PROTECTED_LEVEL&&!canotRemove)
			{
				// �Ƴ�����¼ʱ�����ָ��ʱ�䣬vip�ȼ�Ϊ0,�ȼ�������ָ���ȼ�
				if(nowTime-p.getUpdateTime()>=removeTime
					&&p.getUser_state()==0
					&&p.getIsland().getBuildByIndex(BuildInfo.INDEX_0,null)
						.getBuildLevel()<=removeLevel
					&&p.getResources()[Resources.MAXGEMS]<=0)
				{
					// �Ƴ������б� �Ƴ����������б�
					allianceTable.removePlayerFromAlliance(p.getId(),2);
					// �Ƴ�����
					removeFriend(list2,p);
					// ����Ҫ��player id�����ܴ�user id����Ϊһ��user���ܻ�������player ���
					userTable.removeUserByPlayerId(p.getId(),2);
					iter.remove();
					continue;
				}
			}
			addProps(s2,p);
			addGems(s2,p);
		}
		
		// ����ͬ��
		checkSameNames(list1,list2,s1.getNameSuffix(),s2.getNameSuffix());
	}

	public void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2,Server s3)
	{
		log.info("����ʧ��:="+list1.size()+",="+list2.size());
		Iterator<Object> iter=list2.iterator();
		while(iter.hasNext())
		{
			Player p=(Player)iter.next();
			String sql="select * from "+getTableName()
				+" where player_name='"+p.getName()+"'";
			Fields fields=SqlKit.query(s1.getConnectionManager(),sql);
			if(fields==null)
			{
				log.info(" û���ҵ����====name="+p.getName());
				continue;
			}
			// ����û�и��º����б�
			p.setName(p.getName()+s2.getNameSuffix());
			p=(Player)getDBAccess().mapping(fields);
			p.setName(p.getName()+s1.getNameSuffix());
			list1.add(p);
		}
		log.info("list1 size="+list1.size()+",list2 size="+list2.size());
	}
	
	/**
	 * ��list��
	 * @param list
	 * @param p
	 */
	private void removeFriend(ArrayList<Object> list,Player p)
	{
		Iterator<Object> iter=list.iterator();
		StringBuilder sb=new StringBuilder();
		while(iter.hasNext())
		{
			Player player=(Player)iter.next();
			String friends=player.getAttributes(PublicConst.FRIENDS_LIST);
			if(friends!=null&&!friends.isEmpty())
			{
				if(friends.indexOf(p.getName())>=0)
				{
					sb.setLength(0);
					String[] names=TextKit.split(friends,",");
					for(int i=0;i<names.length;i++)
					{
						if(!names[i].equalsIgnoreCase(p.getName()))
						{
							if(sb.length()>0)
								sb.append(',');
							sb.append(names[i]);
						}
					}
					player.setAttribute(PublicConst.FRIENDS_LIST,sb.toString());
				}
			}
			friends=player.getAttributes(PublicConst.BLACK_LIST);
			if(friends!=null&&!friends.isEmpty())
			{
				if(friends.indexOf(p.getName())>=0)
				{
					sb.setLength(0);
					String[] names=TextKit.split(friends,",");
					for(int i=0;i<names.length;i++)
					{
						if(!names[i].equalsIgnoreCase(p.getName()))
						{
							if(sb.length()>0)
								sb.append(',');
							sb.append(names[i]);
						}
					}
					player.setAttribute(PublicConst.BLACK_LIST,sb.toString());
				}
			}
		}
	}

	/**
	 * ���ͬ�����
	 * 
	 * @param list1
	 * @param list2
	 * @param nameSuffix1
	 * @param nameSuffix2
	 */
	private void checkSameNames(ArrayList<Object> list1,
		ArrayList<Object> list2,String nameSuffix1,String nameSuffix2)
	{
		if(nameSuffix1==null||nameSuffix1.length()==0||nameSuffix2==null
			||nameSuffix2.length()==0) return;

		// Iterator<Object> iter1=list1.iterator();
		// Iterator<Object> iter2=list2.iterator();
		// while(iter1.hasNext())
		// {
		// Player player=(Player)iter1.next();
		// String newName=player.getName()+nameSuffix1;
		// updatePlayerFriends(list1,player.getName(),newName);
		// player.setName(newName);
		// }
		// while(iter2.hasNext())
		// {
		// Player player=(Player)iter2.next();
		// String newName=player.getName()+nameSuffix2;
		// updatePlayerFriends(list2,player.getName(),newName);
		// player.setName(newName);
		// }

		Iterator<Object> iter1=list1.iterator();
		Iterator<Object> iter2;
		while(iter1.hasNext())
		{
			Player p1=(Player)iter1.next();
			iter2=list2.iterator();
			while(iter2.hasNext())
			{
				Player p2=(Player)iter2.next();
				// ���������ҵ�������ͬ������и���
				if(p1.getName().equalsIgnoreCase(p2.getName()))
				{
					// �������Ӻ��������׺
					String newName1=p1.getName()+nameSuffix1;
					String newName2=p2.getName()+nameSuffix2;
					long t1=System.currentTimeMillis();
					log.info("[change name start,name1="+p1.getName()
						+",name2="+p2.getName()+",newName1="+newName1
						+",newName2="+newName2+",startTime="+t1);
					updatePlayerFriends(list1,p1.getName(),newName1);
					updatePlayerFriends(list2,p2.getName(),newName2);
					long t2=System.currentTimeMillis();
					log.info("[change name end,name1="+p1.getName()
						+",name2="+p2.getName()+",newName1="+newName1
						+",newName2="+newName2+",endTime="+t2
						+",elapsedTime="+(t2-t1));
					p1.setName(newName1);
					p2.setName(newName2);
					// �ж���ѭ������Ϊͬ����������ͬ��
					break;
				}
			}
		}
	}

	/**
	 * ������ҵĺ����б�
	 * 
	 * @param list
	 * @param oldName
	 * @param newName
	 */
	private void updatePlayerFriends(ArrayList<Object> list,String oldName,
		String newName)
	{
		Iterator<Object> iter=list.iterator();
		while(iter.hasNext())
		{
			Player p=(Player)iter.next();
			String friendList=p.getAttributes(PublicConst.FRIENDS_LIST);
			String newFriendList=updateFriendList(friendList,oldName,newName);
			if(newFriendList!=null)
				p.setAttribute(PublicConst.FRIENDS_LIST,newFriendList);
			friendList=p.getAttributes(PublicConst.BLACK_LIST);
			newFriendList=updateFriendList(friendList,oldName,newName);
			if(newFriendList!=null)
				p.setAttribute(PublicConst.BLACK_LIST,newFriendList);
		}
	}

	/**
	 * �滻�����б��е�������֣�����滻�ɹ��������µ��б�ʧ���򷵻�null
	 * 
	 * @param friendList ԭ���ĺ����б�
	 * @param oldName Ҫ�滻������
	 * @param newName �滻�ɵ�������
	 * @return �滻����б����û�и����滻�򷵻�null
	 */
	private String updateFriendList(String friendList,String oldName,
		String newName)
	{
		if(friendList==null||friendList.length()<oldName.length())
			return null;
		if(friendList.indexOf(oldName)<0) 
			return null;
		if(friendList.equalsIgnoreCase(oldName))
			return newName;
		// ����������ʱ����
		if(friendList.startsWith(oldName+","))
		{
			String s=TextKit.replace(friendList,oldName+",",newName+",");
			return s;
		}
		if(friendList.endsWith(","+oldName))
		{
			String s=TextKit.replace(friendList,","+oldName,","+newName);
			return s;
		}
		if(friendList.indexOf(","+oldName+",")>0)
		{
			String s=TextKit.replace(friendList,","+oldName+",",","+newName
				+",");
			return s;
		}
		return null;
	}

}
