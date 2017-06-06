package foxu.sea.gm.operators;

import java.net.URLEncoder;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.set.ArrayList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;

/**
 * 获取系统邮件
 * @author comeback
 *
 */
public class GetSystemMail extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		CreatObjectFactory objectFactory=info.getObjectFactory();
		
		String type=params.get("type");
		if(type.equals("0"))
		{
			ArrayList list=objectFactory.getMessageCache().getSystemMessageMap();
			for(int i=0;i<list.size();i++)
			{
				Message msg=(Message)list.get(i);
				
				try
				{
					JSONObject jo=new JSONObject();
					jo.put(GMConstant.ID,msg.getMessageId());
					jo.put(GMConstant.TITLE,URLEncoder.encode(msg.getTitle(),"utf-8"));
					jo.put(GMConstant.CONTENT,URLEncoder.encode(msg.getContent(),"utf-8"));
					jo.put(GMConstant.START_TIME,"0");
					jsonArray.put(jo);
				}
				catch(Exception e)
				{
				}
			}
		}
		else
		{
			ArrayList list=SystemMail.mesMap;
			for(int i=0;i<list.size();i++)
			{
				Message msg=(Message)list.get(i);
				
				try
				{
					JSONObject jo=new JSONObject();
					jo.put(GMConstant.ID,msg.getMessageId());
					jo.put(GMConstant.START_TIME,SeaBackKit.formatDataTime(msg.getStartTime()));
					jo.put(GMConstant.TITLE,URLEncoder.encode(msg.getTitle(),"utf-8"));
					jo.put(GMConstant.CONTENT,URLEncoder.encode(msg.getContent(),"utf-8"));
					jsonArray.put(jo);
				}
				catch(Exception e)
				{
				}
			}
		}
		return GMConstant.ERR_SUCCESS;
	}

}
