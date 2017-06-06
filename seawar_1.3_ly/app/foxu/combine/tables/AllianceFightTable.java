package foxu.combine.tables;

import java.util.ArrayList;
import java.util.Iterator;

import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.util.TimeKit;

import foxu.combine.DataTable;
import foxu.combine.Server;
import foxu.sea.alliance.alliancefight.AllianceFight;

/**
 * 联盟战表
 * @author comeback
 *
 */
public class AllianceFightTable extends DataTable
{
	private static Logger log=LogFactory.getLogger(AllianceFightTable.class);
	
	@Override
	public void beforeSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2)
	{
		// 重置士气
		Iterator<Object> iter;
		
		iter=list1.iterator();
		while(iter.hasNext())
		{
			AllianceFight af=(AllianceFight)iter.next();
			af.getHorn().setAddOrDecr(1);
			af.getHorn().setPercent(0);
			af.getHorn().setTurnTime(TimeKit.getSecondTime());
		}
		iter=list2.iterator();
		while(iter.hasNext())
		{
			AllianceFight af=(AllianceFight)iter.next();
			af.getHorn().setAddOrDecr(1);
			af.getHorn().setPercent(0);
			af.getHorn().setTurnTime(TimeKit.getSecondTime());
		}
	}

	@Override
	public void afterSave(ArrayList<Object> list1,ArrayList<Object> list2,
		Server s1,Server s2,Server s3)
	{
		log.info("未保存  list1 size="+list1.size()+",list2 size="+list2.size());
	}

}
