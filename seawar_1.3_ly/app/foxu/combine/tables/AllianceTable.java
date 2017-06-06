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
import foxu.sea.alliance.Alliance;

public class AllianceTable extends DataTable
{

	public static final Logger log=LogFactory.getLogger(AllianceTable.class);

	/** 清理掉的玩家列表 */
	IntList clearList1=new IntList();
	
	IntList clearList2=new IntList();
	
	/**
	 * 从所有联盟列表和申请列表中移除一个玩家
	 * 
	 * @param playerId
	 * @param serverIndex
	 */
	public void removePlayerFromAlliance(int playerId,int serverIndex)
	{
		if(serverIndex==1)
			clearList1.add(playerId);
		else if(serverIndex==2)
			clearList2.add(playerId);
		else
			throw new RuntimeException("error parameter.serverIndex="
				+serverIndex);
	}

	/**
	 * 处理同名的联盟
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
		//
		// while(iter1.hasNext())
		// {
		// Alliance a=(Alliance)iter1.next();
		// String newName=a.getName()+nameSuffix1;
		// a.setName(newName);
		// }
		//
		// while(iter2.hasNext())
		// {
		// Alliance a=(Alliance)iter2.next();
		// String newName=a.getName()+nameSuffix2;
		// a.setName(newName);
		// }

		Iterator<Object> iter1=list1.iterator();
		Iterator<Object> iter2;

		while(iter1.hasNext())
		{
			Alliance a1=(Alliance)iter1.next();
			iter2=list2.iterator();
			while(iter2.hasNext())
			{
				Alliance a2=(Alliance)iter2.next();
				// 如果两个玩家的名字相同，则进行更名
				if(a1.getName().equalsIgnoreCase(a2.getName()))
				{
					// 更名，加后服务器后缀
					String newName1=a1.getName()+nameSuffix1;
					String newName2=a2.getName()+nameSuffix2;
					long t1=System.currentTimeMillis();
					log.info("[change name start,alliance1="+a1.getName()
						+",alliance2="+a2.getName()+",newName1="+newName1
						+",newName2="+newName2+",startTime="+t1);
					long t2=System.currentTimeMillis();
					log.info("[change name end,alliance1="+a1.getName()
						+",alliance2="+a2.getName()+",newName1="+newName1
						+",newName2="+newName2+",endTime="+t2
						+",elapsedTime="+(t2-t1));
					a1.setName(newName1);
					a2.setName(newName2);
					// 中断内循环，因为同服不会有相同的
					break;
				}
			}
		}
	}

	@Override
	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		// 清理玩家
		clearPlayer(list1,clearList1);
		clearPlayer(list2,clearList2);
		
		// 处理同名
		checkSameNames(list1,list2,s1.getNameSuffix(),s2.getNameSuffix());
	}

	@Override
	public void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2,Server s3)
	{
		log.info("保存失败:="+list1.size()+",="+list2.size());
		Iterator<Object> iter=list2.iterator();
		while(iter.hasNext())
		{
			Alliance a=(Alliance)iter.next();
			String sql="select * from "+getTableName()
				+" where name='"+a.getName()+"'";
			Fields fields=SqlKit.query(s1.getConnectionManager(),sql);
			if(fields==null)
			{
				log.info(" 没有找到联盟====name="+a.getName());
				continue;
			}
			a.setName(a.getName()+s2.getNameSuffix());
			a=(Alliance)getDBAccess().mapping(fields);
			a.setName(a.getName()+s1.getNameSuffix());
			list1.add(a);
		}
		log.info("list1 size="+list1.size()+",list2 size="+list2.size());

	}
	
	private void clearPlayer(ArrayList<Object> alliances,IntList players)
	{
		Iterator<Object> iter=alliances.iterator();
		while(iter.hasNext())
		{
			Alliance alliance=(Alliance)iter.next();
			for(int i=0;i<players.size();i++)
			{
				int playerId=players.get(i);
				alliance.removeApllication(playerId);
				alliance.removePlayerId(playerId);
			}
		}
	}

}
