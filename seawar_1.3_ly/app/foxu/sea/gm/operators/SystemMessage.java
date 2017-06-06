package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.util.TimeKit;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.ChatMessagePort;

/**
 * ��ϵͳ��Ϣ
 * @author comeback
 *
 */
public class SystemMessage extends GMOperator
{
	
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String systemMessage=params.get("system_message");
		// ������Ϣ
		ChatMessage message=new ChatMessage();
		// type
		message.setType(ChatMessage.SYSTEM_CHAT);
		message.setTime(TimeKit.getSecondTime());
		message.setSrc("");
		message.setText(systemMessage);
		SeaBackKit.sendAllMsg(message,info.getDSManager(),false);
		//����Ϣ
		ChatMessagePort chatPort=(ChatMessagePort)BackKit.getContext().get(
			"chatMessagePort");
		chatPort.numFiler();
		chatPort.getChatMessages().add(message);
		JBackKit.sendScrollMessage(info.getDSManager(),systemMessage);
		return GMConstant.ERR_SUCCESS;
	}

}
