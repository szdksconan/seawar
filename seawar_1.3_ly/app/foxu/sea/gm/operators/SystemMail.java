package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.award.Award;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.proplist.Prop;
import foxu.sea.uid.UidKit;

/**
 * 发送系统邮件
 * 
 * @author comeback
 * 
 */
public class SystemMail extends GMOperator implements TimerListener
{

	Logger log=LogFactory.getLogger(SystemMail.class);
	
	static final int time=5000;
	/** 系统邮件 map **/
	static ArrayList mesMap=new ArrayList();

	CreatObjectFactory objectFactory;
	/** UID提供器 */
	UidKit uidkit;

	// 发件名字
	String sendName;

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		objectFactory=info.getObjectFactory();
		try
		{
			// 玩家名
			String names=params.get("names");
			String[] namess=null;
			if(!names.equals(""))
			{
				namess=TextKit.split(names,",");
				for(int i=0;i<namess.length;i++)
				{
					if(objectFactory.getPlayerByName(namess[i],false)==null)
						return GMConstant.ERR_PARAMATER_ERROR;
				}
			}
			// 标题
			String title=params.get("title");
			// 内容
			String content=params.get("content");
			if(title.equals("")||content.equals(""))
			{
				return GMConstant.ERR_PARAMATER_ERROR;
			}
			// 是否有附件
			String annex=params.get("annex");
			// 是否是定时发送
			String timing=params.get("timing");
			Award award=null;
			if(TextKit.parseInt(annex)!=1)
			{
				String gems=params.get("gems");
				String sids=params.get("sids");
				int gem=TextKit.parseInt(gems);
				if(gem<0) return GMConstant.ERR_PARAMATER_ERROR;
				IntList proplist=new IntList();
				IntList shiplist=new IntList();
				IntList euiplist=new IntList();
				IntList officerlist=new IntList();
				if(!sids.equals(""))
				{
					int[] sidss=TextKit.parseIntArray(TextKit
						.split(sids,","));
					if(gem>0&&sidss.length>6)
						return GMConstant.ERR_PARAMATER_ERROR;
					for(int i=0;i<sidss.length;i+=2)
					{
						int ptype=SeaBackKit.getSidType(sidss[i]);
						if(ptype==Prop.VALID)
							return GMConstant.ERR_PARAMATER_ERROR;
						if(sidss[i+1]>PublicConst.ADD_PROP_LIMIT
							||sidss[i+1]<1)
							return GMConstant.ERR_PARAMATER_ERROR;
						if(ptype==Prop.PROP)
						{
							proplist.add(sidss[i]);
							proplist.add(sidss[i+1]);
						}
						else if(ptype==Prop.SHIP)
						{
							shiplist.add(sidss[i]);
							shiplist.add(sidss[i+1]);
						}
						else if(ptype==Prop.EQUIP)
						{
							euiplist.add(sidss[i]);
							euiplist.add(sidss[i+1]);
						}
						else if(ptype==Prop.OFFICER)
						{
							officerlist.add(sidss[i]);
							officerlist.add(sidss[i+1]);
						}
					}

				}
				award=(Award)Award.factory.newSample(Message.EMPTY_SID);
				award.setGemsAward(gem);
				if(proplist.size()>0) award.setPropSid(proplist.toArray());
				if(shiplist.size()>0) award.setShipSids(shiplist.toArray());
				if(euiplist.size()>0)
					award.setEquipSids(euiplist.toArray());
				if(officerlist.size()>0)
					award.setOfficerSids(officerlist.toArray());
			}
			int stime=TimeKit.getSecondTime();
			if(TextKit.parseInt(timing)!=1)
			{
				stime=SeaBackKit.parseFormatTime(params.get("stime"));
			}
			if(namess!=null)
			{
				for(int i=0;i<namess.length;i++)
				{
					addMes(Message.SYSTEM_ONE_TYPE,title,content,award,stime,namess[i]);
				}
			}
			else
			{
				addMes(Message.SYSTEM_TYPE,title,content,award,stime,null);
			}

		}
		catch(Exception e)
		{
			return GMConstant.ERR_PARAMATER_ERROR;
		}
		return GMConstant.ERR_SUCCESS;
	}
	/** 添加一封邮件 */
	public void addMes(int mesType,String title,String content,Award award,
		int stime,String receiveName)
	{
		Message mes=new Message();
		mes.setMessageId(uidkit.getPlusUid());
		mes.setMessageType(mesType);
		mes.setTitle(title);
		mes.setContent(content);
		mes.setAward(award);
		mes.setStartTime(stime);
		if(receiveName!=null)mes.setReceiveName(receiveName);
		mesMap.add(mes);
	}

	/** 启动定时器 */
	public void startTimer()
	{
		sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		TimerCenter.getSecondTimer().add(new TimerEvent(this,"flush",time));
	}
	@Override
	public void onTimer(TimerEvent arg0)
	{
		sendMail();
	}

	public void sendMail()
	{
		for(int i=0;i<mesMap.size();i++)
		{
			Message msg=(Message)mesMap.get(i);
			if(msg.getStartTime()<=TimeKit.getSecondTime())
			{
				if(msg.getMessageType()==Message.SYSTEM_TYPE)
				{
					objectFactory.createSystemMessage(msg.getMessageType(),
						msg.getContent(),msg.getTitle(),sendName,
						msg.getAward());
				}
				else if(msg.getMessageType()==Message.SYSTEM_ONE_TYPE)
				{
					Player player=objectFactory.getPlayerByName(
						msg.getReceiveName(),true);
					if(player!=null)
					{
						Message message=objectFactory.createMessage(0,
							player.getId(),msg.getContent(),sendName,
							player.getName(),Message.SYSTEM_ONE_TYPE,
							msg.getTitle(),true,msg.getAward());
						// 刷新前台
						JBackKit.sendRevicePlayerMessage(player,message,
							message.getRecive_state(),objectFactory);
					}
					else
					{
						log.error("----sytemMail---error---pname-----:"
							+msg.getReceiveName());
					}
				}
				mesMap.remove(msg);
				i--;
			}
		}
	}
	
	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}
	
	

}
