package foxu.combine.tables;

import java.util.ArrayList;

import foxu.combine.DataTable;
import foxu.combine.Server;
import foxu.dcaccess.NpcIslandGameDBAccess;

/**
 * npc_islands��<br />
 * �������е��죬�������¼�
 * @author comeback
 *
 */
public class IslandTable extends DataTable
{

	NpcIslandGameDBAccess dbAccess;

	@Override
	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2,Server s3)
	{
		// TODO Auto-generated method stub
		
	}
	
//	@Override
//	public void processImpl(Server s1,Server s2,Server s3)
//	{
//		// ������������
//		String sql="select * from npc_islands";
//		Fields[] fields=SqlKit.querys(s1.getConnectionManager(),sql);
//		for(int i=0;i<fields.length;i++)
//		{
//			
//		}
//	}

	
}
