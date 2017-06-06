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
	
	/** 玩家表，用于修改player上的user_id属性 */
	PlayerTable playerTable;
	
	/** 清理掉的玩家列表 */
	IntList clearList1=new IntList();
	
	IntList clearList2=new IntList();
	
	public void setPlayerTable(PlayerTable playerTable)
	{
		this.playerTable=playerTable;
	}
	
	/**
	 * 移除指定playerid的User,检查如果User存在多个player，要移除所有的player才移除user
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
	 * 检查相同的账号
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
				// 这里的比较要注意user的user type属性，例如91平台的账号都是User.GUEST，但不应该比较UDID
				// 而应该比较user_account，之前做91认证的时候弄错了，要注意，不是用UDID登录的情况，
				// 都应该是User.USER类型
				if(u1.getUserType()==User.GUEST&&u2.getUserType()==User.GUEST)
				{
					if(u1.getLoginUdid().equalsIgnoreCase(u2.getLoginUdid()))
					{
						log.info("合并user:u1="+u1.getUserAccount()+",u2="+u2.getUserAccount());
						combineUser(u1,u2);
						// 更新player的user_id
						playerTable.updateUserId2(u2.getId(),u1.getId());
						iter2.remove();
						break;
					}
				}
				else if(u1.getUserType()==User.USER&&u2.getUserType()==User.USER)
				{
					if(u1.getUserAccount().equalsIgnoreCase(u2.getUserAccount()))
					{
						log.info("合并user:u1="+u1.getUserAccount()+",u2="+u2.getUserAccount());
						combineUser(u1,u2);
						// 更新player的user_id
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
		// 如果1服上是空号，则直接转移pid
		if(u1.getPlayerId()==0&&u2.getPlayerId()!=0)
		{
			u1.setPlayerId(u2.getPlayerId());
		}
		// 如果两个服的角色都不为空，则拼接两个角色ID
		else if(u1.getPlayerId()!=0&&u2.getPlayerId()!=0)
		{
			String playerIds="".intern();
			int[] ids1=u1.getPlayerIds();
			int[] ids2=u2.getPlayerIds();
			// 第一个账号上的pid，如果pids有内容，使用pids，否则使用
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
			// 第二个账号上的pid，如果pids有内容,使用pids,否则使用
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
		// 如果1服上不是空，2服是空，则直接忽略
		else
		{
			
		}
	}

	@Override
	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		// 清理USER
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
				log.info(" 没有找到账号====name="+u2.getUserAccount());
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
	 * 清理已经移除player的User和原本就没有player的user
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
