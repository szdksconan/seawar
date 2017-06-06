package foxu.combine.tables;

import java.util.ArrayList;
import java.util.Iterator;

import foxu.combine.DataTable;
import foxu.combine.Server;
import foxu.sea.messgae.Message;


public class MessageTable extends DataTable
{

	@Override
	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		Iterator<Object> iter=list1.iterator();
		while(iter.hasNext())
		{
			Message msg=(Message)iter.next();
			if(msg.getMessageType()==Message.SYSTEM_TYPE)
				iter.remove();
		}
		iter=list2.iterator();
		while(iter.hasNext())
		{
			Message msg=(Message)iter.next();
			if(msg.getMessageType()==Message.SYSTEM_TYPE)
				iter.remove();
		}
	}

	@Override
	public void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2,Server s3)
	{
		// TODO Auto-generated method stub

	}

}
