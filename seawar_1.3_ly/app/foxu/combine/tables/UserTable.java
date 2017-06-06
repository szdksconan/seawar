package foxu.combine.tables;

import java.util.ArrayList;
import java.util.Iterator;

import mustang.field.Fields;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.orm.SqlKit;
import mustang.set.IntList;

import foxu.combine.DataTable;
import foxu.combine.Server;
import foxu.sea.User;


public class UserTable extends DataTable
{
	public static final Logger log=LogFactory.getLogger(UserTable.class);
	
	/** ��ұ������޸�player�ϵ�user_id���� */
	PlayerTable playerTable;
	
	/** �����������б� */
	IntList clearList1=new IntList();
	
	IntList clearList2=new IntList();
	
	public void setPlayerTable(PlayerTable playerTable)
	{
		this.playerTable=playerTable;
	}
	
	/**
	 * �Ƴ�ָ��playerid��User,������User���ڶ��player��Ҫ�Ƴ����е�player���Ƴ�user
	 * @param id
	 */
	public void removeUserByPlayerId(int id,int serverIndex)
	{
		if(serverIndex==1)
			clearList1.add(id);
		else if(serverIndex==2)
			clearList2.add(id);
		else
			throw new RuntimeException("error parameter,serverIndex="+serverIndex);
	}

	/** 
	 * �����ͬ���˺�
	 * @param list1
	 * @param list2
	 * @param nameSuffix1
	 * @param nameSuffix2
	 */
	private void checkSameNames(ArrayList<Object> list1, ArrayList<Object> list2, String nameSuffix1,String nameSuffix2)
	{
		Iterator<Object> iter1=list1.iterator();
		Iterator<Object> iter2;
		
		while(iter1.hasNext())
		{
			User u1=(User)iter1.next();
			iter2=list2.iterator();
			while(iter2.hasNext())
			{
				User u2=(User)iter2.next();
				// ����ıȽ�Ҫע��user��user type���ԣ�����91ƽ̨���˺Ŷ���User.GUEST������Ӧ�ñȽ�UDID
				// ��Ӧ�ñȽ�user_account��֮ǰ��91��֤��ʱ��Ū���ˣ�Ҫע�⣬������UDID��¼�������
				// ��Ӧ����User.USER����
				if(u1.getUserType()==User.GUEST&&u2.getUserType()==User.GUEST)
				{
					if(u1.getLoginUdid().equalsIgnoreCase(u2.getLoginUdid()))
					{
						log.info("�ϲ�user:u1="+u1.getUserAccount()+",u2="+u2.getUserAccount());
						combineUser(u1,u2);
						// ����player��user_id
						playerTable.updateUserId2(u2.getId(),u1.getId());
						iter2.remove();
						break;
					}
				}
				else if(u1.getUserType()==User.USER&&u2.getUserType()==User.USER)
				{
					if(u1.getUserAccount().equalsIgnoreCase(u2.getUserAccount()))
					{
						log.info("�ϲ�user:u1="+u1.getUserAccount()+",u2="+u2.getUserAccount());
						combineUser(u1,u2);
						// ����player��user_id
						playerTable.updateUserId2(u2.getId(),u1.getId());
						iter2.remove();
						break;
					}
				}
			}
		}
	}
	
	private void combineUser(User u1,User u2)
	{
		// ���1�����ǿպţ���ֱ��ת��pid
		if(u1.getPlayerId()==0&&u2.getPlayerId()!=0)
		{
			u1.setPlayerId(u2.getPlayerId());
		}
		// ����������Ľ�ɫ����Ϊ�գ���ƴ��������ɫID
		else if(u1.getPlayerId()!=0&&u2.getPlayerId()!=0)
		{
			String playerIds="".intern();
			int[] ids1=u1.getPlayerIds();
			int[] ids2=u2.getPlayerIds();
			// ��һ���˺��ϵ�pid�����pids�����ݣ�ʹ��pids������ʹ��
			if(ids1!=null&&ids1.length>0&&ids1[0]>0)
			{
				for(int i=0;i<ids1.length;i++)
				{
					if(i>0)playerIds+=":";
					playerIds+=String.valueOf(ids1[i]);
				}
			}
			else
			{
				playerIds=String.valueOf(u1.getPlayerId());
			}
			// �ڶ����˺��ϵ�pid�����pids������,ʹ��pids,����ʹ��
			if(ids2!=null&&ids2.length>0&&ids2[0]>0)
			{
				for(int i=0;i<ids2.length;i++)
				{
					playerIds+=":"+ids2[i];
				}
			}
			else
			{
				playerIds+=":"+u2.getPlayerId();
			}

			u1.setPlayerIdsString(playerIds);
		}
		// ���1���ϲ��ǿգ�2���ǿգ���ֱ�Ӻ���
		else
		{
			
		}
	}

	@Override
	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		// ����USER
		clearUser(list1,clearList1);
		clearUser(list2,clearList2);
		
		checkSameNames(list1,list2,s1.getNameSuffix(),s2.getNameSuffix());
	}

	@Override
	public void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2,Server s3)
	{
		log.info("after save user list1 size="+list1.size()+",after save user list2 size="+list2.size());
		Iterator<Object> iter=list2.iterator();
		while(iter.hasNext())
		{
			User u2=(User)iter.next();
			String sql="select * from "+getTableName()+" where user_account='"+u2.getUserAccount()+"'";
			Fields fields=SqlKit.query(s1.getConnectionManager(),sql);
			if(fields==null)
			{
				log.info(" û���ҵ��˺�====name="+u2.getUserAccount());
				continue;
			}
			User u1=(User)getDBAccess().mapping(fields);
			playerTable.updateUserId2(u2.getId(),u1.getId());
			combineUser(u1,u2);
			list1.add(u1);
			iter.remove();
		}
		playerTable.savePlayerList3(s3);
	}
	
	/**
	 * �����Ѿ��Ƴ�player��User��ԭ����û��player��user
	 * @param alliances
	 * @param players
	 */
	private void clearUser(ArrayList<Object> users,IntList players)
	{
		Iterator<Object> iter=users.iterator();
		while(iter.hasNext())
		{
			User user=(User)iter.next();
			if(user.getPlayerId()==0)
			{
				iter.remove();
				continue;
			}
			for(int i=0;i<players.size();i++)
			{
				int playerId=players.get(i);
				if(user.getPlayerId()==playerId)
				{
					iter.remove();
					break;
				}
			}
		}
	}

}
