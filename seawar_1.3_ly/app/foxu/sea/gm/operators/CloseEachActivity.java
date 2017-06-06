package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.util.TimeKit;
import shelby.ds.DSManager;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.PeaceActivity;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;

/***
 * 关闭某种活动
 * 
 * @author lhj
 *
 */
public class CloseEachActivity extends GMOperator
{

	DSManager dsManager;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String aSid=params.get("sid");
		int sid=0;
		try
		{
			sid=Integer.parseInt(aSid);
			Object[] objects=ActivityContainer.getInstance()
				.getActivityBySid(sid);
			if(objects==null) return GMConstant.ERR_SUCCESS;
			int timeNow=TimeKit.getSecondTime();
			for(int i=0;i<objects.length;i++)
			{
				Activity activity=(Activity)objects[i];
				if(activity==null) continue;
				if(activity instanceof PeaceActivity)
				{
					if(activity.isOpen(timeNow))
					{
						String stime=SeaBackKit.formatDataTime(timeNow-1);
						String etime=SeaBackKit.formatDataTime(timeNow);
						activity.resetActivity(stime,etime,null,
							info.getObjectFactory());
					}
					else
					{
						activity.setStartTime(timeNow-1);
						activity.setEndTime(timeNow);
					}
						continue;
				}
				if(activity.isOpen(timeNow))
				{
					activity.setEndTime(timeNow);
					activity.sendFlush(dsManager.getSessionMap());
					continue;
				}
				if(activity.getEndTime()>=timeNow)
				{
					activity.setStartTime(timeNow-1);
					activity.setEndTime(timeNow);
				}

			}

		}
		catch(Exception e)
		{
			return GMConstant.ERR_SUCCESS;
		}
		return GMConstant.ERR_SUCCESS;
	}

	public void setDsManager(DSManager dsManager)
	{
		this.dsManager=dsManager;
	}

}
