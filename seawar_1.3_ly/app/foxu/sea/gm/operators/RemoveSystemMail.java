package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.set.ArrayList;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.MessageSave;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.messgae.Message;

/**
 * 删除系统邮件
 * @author comeback
 *
 */
public class RemoveSystemMail extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String idStr=params.get("id");
		if(idStr==null||idStr.length()==0||TextKit.valid(idStr,TextKit.NUMBER)!=0)
			return GMConstant.ERR_PARAMATER_ERROR;
		int id=Integer.parseInt(idStr);
		CreatObjectFactory objectFactory=info.getObjectFactory();
		ArrayList list=objectFactory.getMessageCache().getSystemMessageMap();
		for(int i=0;i<list.size();i++)
		{
			Message mail=(Message)list.get(i);
			if(mail.getMessageId()==id)
			{
				// 从内存中移除
				objectFactory.getMessageCache().deleteCache(mail);
				// 标记为移除并放到改变列表中，下次存储时从数据库中删除
				mail.setDelete(1);
				MessageSave save=new MessageSave();
				save.setData(mail);
				objectFactory.getMessageCache().getChangeListMap().put(id,save);
				return GMConstant.ERR_SUCCESS;
			}
		}
		for(int i=0;i<SystemMail.mesMap.size();i++)
		{
			Message meg=(Message)SystemMail.mesMap.get(i);
			if(meg.getMessageId()==id)
			{
				SystemMail.mesMap.remove(meg);
				return GMConstant.ERR_SUCCESS;
			}
		}
		return GMConstant.ERR_PARAMATER_ERROR;
	}

}
