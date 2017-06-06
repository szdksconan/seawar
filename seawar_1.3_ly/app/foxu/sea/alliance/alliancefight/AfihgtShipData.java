package foxu.sea.alliance.alliancefight;

import foxu.sea.OrderList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;


/**
 * ��ս��ֻ��־����
 * @author yw
 *
 */
public class AfihgtShipData
{
	private static AfihgtShipData ashipData;
	/** ÿ������־���� */
	public static final int MAX=100;
	
	/** ��ս��־��¼ key-orderlist*/
	IntKeyHashMap shipdata=new IntKeyHashMap();
	
	
	private AfihgtShipData()
	{
		ashipData=this;
	}
	
	public static AfihgtShipData instance()
	{
		if(ashipData==null)ashipData=new AfihgtShipData();
		return ashipData;
	}
	
	/** ����һ����¼ */
	public void generateShipTrack(int type,int allianceId,String target,IntList left,IntList bleft,IntList list)
	{
		if(list==null||list.size()<=0)return;
		OrderList alist=(OrderList)shipdata.get(allianceId);
		if(alist==null)
		{
			alist=new OrderList();
			shipdata.put(allianceId,alist);
		}
		synchronized(alist)
		{
			alist.add(new ShipRecord(type,target,left,bleft,list));
			if(alist.size()>MAX)alist.removeAt(0);
		}
		
	}
	
	/** ��ȡĳ���˴�ֻ�ļ�¼ */
	public OrderList getShipRecords(int allianceId)
	{
		return (OrderList)shipdata.get(allianceId);
	}
	

}
