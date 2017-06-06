package foxu.sea.gm.operators;

import java.util.ArrayList;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.log.LogFactory;
import mustang.log.Logger;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.ChatMessagePort;


/**
 * 清空世界频道
 * @author yw
 *
 */
public class ClearChatMessage extends GMOperator
{
	Logger log=LogFactory.getLogger(ClearChatMessage.class);
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		ChatMessagePort messport=(ChatMessagePort)BackKit.getContext().get("chatMessagePort");
		ArrayList<ChatMessage> mes=messport.getChatMessages();
		for(int i=0;i<mes.size();i++)
		{
			log.error("clear_message=============:"+mes.get(i).getText());
		}
		messport.getChatMessages().clear();
		return 0;
	}

}
