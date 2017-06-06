package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.set.ArrayList;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.ChatMessagePort;

/**
 * 滚动系统消息
 * @author yw
 *
 */
public class RollSystemMessage extends GMOperator implements TimerListener
{

	ArrayList messages=new ArrayList();
	int index;
	int endtime;
	int interval;
	TimerEvent e;
	ServerInfo info;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		this.info=info;
		int optype=Integer.parseInt(params.get("op_type"));
		if(optype==1)
		{
			String systemMessage=params.get("system_message");
			endtime=SeaBackKit.parseFormatTime(params.get("etime"));
			int intervalTime=Integer.parseInt(params.get("interval"));
			// 聊天消息
			ChatMessage message=new ChatMessage();
			// type
			message.setType(ChatMessage.SYSTEM_CHAT);
			message.setSrc("");
			message.setText(systemMessage);
			messages.add(message);
			if(interval!=intervalTime)
			{
				interval=intervalTime;
				TimerCenter.getSecondTimer().remove(e);
				TimerCenter.getSecondTimer().remove(this);
				TimerCenter.getSecondTimer().add(new TimerEvent(this,"msg",interval*1000));
			}
		}else
		{
			clear();
		}
		return GMConstant.ERR_SUCCESS;
	}
	
	@Override
	public void onTimer(TimerEvent arg0)
	{
		if(TimeKit.getSecondTime()<=endtime)
		{
			ChatMessage mes=(ChatMessage)messages.get(index);
			SeaBackKit.sendAllMsg(mes,info.getDSManager(),false);
			JBackKit.sendScrollMessage(info.getDSManager(),mes.getText());
			index++;
			if(index>=messages.size()) index=0;
			// 存信息
			ChatMessagePort chatPort=(ChatMessagePort)BackKit.getContext()
				.get("chatMessagePort");
			ChatMessage message=new ChatMessage();
			message.setType(mes.getType());
			message.setTime(TimeKit.getSecondTime());
			message.setText(mes.getText());
			chatPort.numFiler();
			chatPort.getChatMessages().add(message);
		}else
		{
			clear();
		}
	}
	
	public void clear()
	{
		index=0;
		endtime=0;
		interval=0;
		TimerCenter.getSecondTimer().remove(e);
		TimerCenter.getSecondTimer().remove(this);
		e=null;
		messages.clear();
	}


}
