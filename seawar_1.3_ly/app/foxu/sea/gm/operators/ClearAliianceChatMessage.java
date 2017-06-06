package foxu.sea.gm.operators;

import java.util.ArrayList;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.log.LogFactory;
import mustang.log.Logger;
import foxu.sea.alliance.Alliance;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.ChatMessagePort;


/**
 * Çå¿ÕÁªÃËÁÄÌì
 * @author yw
 *
 */
public class ClearAliianceChatMessage extends GMOperator
{
	Logger log=LogFactory.getLogger(ClearAliianceChatMessage.class);
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String aname=params.get("aname");
		Alliance al=info.getObjectFactory().getAllianceMemCache().loadByName(aname,true);
		if(al==null)return GMConstant.ERR_PARAMATER_ERROR;
		ChatMessagePort messport=(ChatMessagePort)BackKit.getContext().get("chatMessagePort");
		ArrayList<ChatMessage> achat=(ArrayList<ChatMessage>)messport.getAlliancesChat().get(al.getId());
		if(achat!=null)
		{
			for(int i=0;i<achat.size();i++)
			{
				log.error("clear_al_message=============:"+achat.get(i).getText());
			}
			achat.clear();
		}
		return 0;
	}

}
