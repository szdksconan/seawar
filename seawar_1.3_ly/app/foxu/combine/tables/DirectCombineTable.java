package foxu.combine.tables;

import java.util.ArrayList;

import foxu.combine.DataTable;
import foxu.combine.Server;

/**
 * 直接合并的表
 * @author comeback
 *
 */
public class DirectCombineTable extends DataTable
{
	
	@Override
	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		
	}

	@Override
	public void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2,Server s3)
	{
		
	}

}
