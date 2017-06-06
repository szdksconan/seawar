package foxu.sea.port;

import java.util.ArrayList;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.MessageSave;
import foxu.ds.SWDSManager;
import foxu.sea.ContextVarManager;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.alliance.Alliance;
import foxu.sea.equipment.Equipment;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.messgae.ReportPlayerManager;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerManager;

/**
 * �������˿� port:1002
 */
public class ChatMessagePort extends AccessPort
{
	/** ��������¼���� */
	public static final int MAX_NUM=50;
	public static final int MAX_MSG_LENGTH=200;
	// /** 20����֮�ڵ������¼ */
	// public static final int TIME=60*20;
	/** dsmanager */
	SWDSManager dsmanager;
	/** ����������¼ */
	ArrayList<ChatMessage> chatMessages=new ArrayList<ChatMessage>();
	/** ���������¼���� */
	IntKeyHashMap alliancesChat=new IntKeyHashMap();

	CreatObjectFactory objectFactory;

	ReportPlayerManager reportManager;
	// /** ʱ����� */
	// public void timeFiler()
	// {
	// Object object[]=chatMessages.toArray();
	// for(int i=0;i<object.length;i++)
	// {
	// ChatMessage message=(ChatMessage)object[i];
	// if((TimeKit.getSecondTime()-message.getTime())>=TIME)
	// chatMessages.remove(message);
	// }
	// }

	/** ������������¼ */
	public void addAllianceChat(int allianceId,ChatMessage message)
	{
		ArrayList<ChatMessage> messages=(ArrayList<ChatMessage>)alliancesChat.get(allianceId);
		if(messages!=null)
		{
			messages.add(message);
		}
		else
		{
			messages=new ArrayList<ChatMessage>();
			messages.add(message);
			alliancesChat.put(allianceId,messages);
		}
	}

	/** ���������������� */
	public void allianceChatFiler(int allianceId)
	{
		ArrayList<ChatMessage> messages=(ArrayList<ChatMessage>)alliancesChat.get(allianceId);
		if(messages!=null)
		{
			if(messages.size()>MAX_NUM)
			{
				messages.remove(0);
			}
		}
	}
	/** �������� */
	public void numFiler()
	{
		if(chatMessages.size()>MAX_NUM)
		{
			chatMessages.remove(0);
		}
	}

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session session=(Session)(connect.getSource());
		Player player=(Player)(session.getSource());
		if(player==null)
		{
			connect.close();
			return null;
		}
		// type
		int type=data.readUnsignedByte();
		// ���Ի�ȡ�����б�
		if(type!=ChatMessage.GET_FORE_MESSAGE&&player.getMuteTime()!=0)
		{
			int leftTime=player.getMuteTime()-TimeKit.getSecondTime();
			// ����ʣ��ʱ��
			if(leftTime>0)
			{
				// ȡ��ǰ̨��ʾ����
				String content=InterTransltor.getInstance().getTransByKey(
					player.getLocale(),"muteTime_left");
				content=TextKit.replace(content,"%",(leftTime/60)+"");
				throw new DataAccessException(0,content);
			}
		}
		// timeFiler();
		// ������Ϣ
		ChatMessage message=new ChatMessage();
		message.setType(type);
		message.setSrc(player.getName());
		message.setTime(TimeKit.getSecondTime());
		message.setPlayerType(player.getPlayerType());
		// ȡ������Ϣ byte����
		if(type==ChatMessage.GET_FORE_MESSAGE)
		{
			data.clear();
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			Alliance alliance=null;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					Integer.parseInt(allianceId)+"");
			}
			int size=chatMessages.size();
			boolean isUpdate=false;
			ArrayList<ChatMessage> messages=null;
			if(alliance!=null)
			{
				messages=(ArrayList<ChatMessage>)alliancesChat
					.get(Integer.parseInt(allianceId));
                if(messages!=null)
                {
                	size+=messages.size();
                }
			}
			int top=data.top();
			data.writeByte(size);
			if(PublicConst.CHAT_GROUP_LOCALE)
			{
				for(int i=0;i<chatMessages.size();i++)
				{
					ChatMessage messageSend=chatMessages.get(i);
					if(messageSend.getLocale()!=player.getLocale())
					{
						size--;
						isUpdate=true;
						continue;
					}
					messageSend.showBytesWrite(data,true);
				}
			}
			else
			{
				for(int i=0;i<chatMessages.size();i++)
				{
					ChatMessage messageSend=chatMessages.get(i);
					messageSend.showBytesWrite(data,true);
				}
			}
			if(messages!=null)
			{
				int joinTime=Integer.valueOf(player
					.getAttributes(PublicConst.ALLIANCE_JOIN_TIME));
				for(int i=0;i<messages.size();i++)
				{
					ChatMessage messageSend=(ChatMessage)messages.get(i);
					// �����������ʱ�������Ϣʱ�䣬�򲻽�����ʾ
					if(joinTime>messageSend.getTime())
					{
						size--;
						isUpdate=true;
						continue;
					}
					messageSend.showBytesWrite(data,true);
				}
			}
			if(isUpdate)
			{
				int newTop=data.top();
				data.setTop(top);
				data.writeByte(size);
				data.setTop(newTop);
			}
			return data;
		}
		//����Ƶ������ɾ�
		else if(type==ChatMessage.ACHIEVE_DATA)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.WORLD_CHAT_LEVEL,player,
				"world_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			int sid=data.readUnsignedShort();
			message.setSid(sid);
			message.setText("");
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
//			getAchieveData(data,player,sid,type);
			SessionMap smap=dsmanager.getSessionMap();
			smap.send(data);
			numFiler();
			chatMessages.add(message);
			data.clear();
			return data;
		}
		//����Ƶ������ɾ�
		else if(type==ChatMessage.ACHIEVE_ALLIANCE_DATA)
		{
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			Alliance alliance=null;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					Integer.parseInt(allianceId)+"");
			}
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			int sid=data.readUnsignedShort();
			message.setSid(sid);
			message.setText("");
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
//			getAchieveData(data,player,sid,type);
			sendAllAlliancePlayers(data,alliance);
			allianceChatFiler(Integer.parseInt(allianceId));
			// ��ӱ���
			addAllianceChat(Integer.parseInt(allianceId),message);
			data.clear();
			return data;
		}
		//˽�ķ���ɾ�
		else if(type==ChatMessage.ACHIEVE_SELF_DATA)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.PRIVATE_CHAT_LEVEL,player,
				"self_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			int sid=data.readUnsignedShort();
			String name=data.readUTF();
			Player bplayer=objectFactory.getPlayerByName(name,false);
			data.clear();
			if(bplayer==null)
			{
				throw new DataAccessException(0,"friend not online");
			}
			if(bplayer.getSource()==null)
			{
				throw new DataAccessException(0,"friend not online");
			}
			Session s=(Session)bplayer.getSource();
			if(s.getConnect()==null||!s.getConnect().isActive())
			{
				throw new DataAccessException(0,"friend not online");
			}
//			getAchieveData(data,player,sid,type);
			message.setSid(sid);
			message.setText("");
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
			data.writeUTF(bplayer.getName());
			s.getConnect().send(data);
			connect.send(data);
			data.clear();
			return data;
		}
		//����Ƶ���������
		else if(type==ChatMessage.EQUIPMENT_WORLD)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.WORLD_CHAT_LEVEL,player,
				"world_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			int eid=data.readInt();
			Equipment equip=player.getEquips().getEquip(eid);
			if(equip==null)
				throw new DataAccessException(0,"equip not exist");
			message.setSid(equip.getSid());
			message.setText("");
			message.setLevel(equip.getLevel());
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
//			getAchieveData(data,player,sid,type);
			SessionMap smap=dsmanager.getSessionMap();
			smap.send(data);
			numFiler();
			chatMessages.add(message);
			data.clear();
			return data;
		}
		//����Ƶ���������
		else if(type==ChatMessage.EQUIPMENT_ALLIANCE)
		{
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			Alliance alliance=null;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					Integer.parseInt(allianceId)+"");
			}
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			int eid=data.readInt();
			Equipment equip=player.getEquips().getEquip(eid);
			if(equip==null)
				throw new DataAccessException(0,"equip not exist");
			message.setSid(equip.getSid());
			message.setText("");
			message.setLevel(equip.getLevel());
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
//			getAchieveData(data,player,sid,type);
			sendAllAlliancePlayers(data,alliance);
			allianceChatFiler(Integer.parseInt(allianceId));
			// ��ӱ���
			addAllianceChat(Integer.parseInt(allianceId),message);
			data.clear();
			return data;
		}
		//˽�ķ������
		else if(type==ChatMessage.EQUIPMENT_FRIEND)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.PRIVATE_CHAT_LEVEL,player,
				"self_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			int eid=data.readInt();
			Equipment equip=player.getEquips().getEquip(eid);
			if(equip==null)
				throw new DataAccessException(0,"equip not exist");
			String name=data.readUTF();
			Player bplayer=objectFactory.getPlayerByName(name,false);
			data.clear();
			if(bplayer==null)
			{
				throw new DataAccessException(0,"friend not online");
			}
			if(bplayer.getSource()==null)
			{
				throw new DataAccessException(0,"friend not online");
			}
			Session s=(Session)bplayer.getSource();
			if(s.getConnect()==null||!s.getConnect().isActive())
			{
				throw new DataAccessException(0,"friend not online");
			}
//			getAchieveData(data,player,sid,type);
			message.setSid(equip.getSid());
			message.setText("");
			message.setLevel(equip.getLevel());
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
			data.writeUTF(bplayer.getName());
			s.getConnect().send(data);
			connect.send(data);
			data.clear();
			return data;
		}
		// ����Ƶ���������
		else if(type==ChatMessage.OFFICER_WORLD)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.WORLD_CHAT_LEVEL,player,
				"world_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			int sid=data.readUnsignedShort();
			int oid=data.readInt();
			Officer officer=OfficerManager.getInstance().getOfficer(player,
				oid,sid);
			if(officer==null)
				throw new DataAccessException(0,"officer not exist");
			message.setSid(officer.getSid());
			message.setText("");
			message.setLevel(officer.getLevel());
			message.setPlayerType(officer.getMilitaryRank());
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
			// getAchieveData(data,player,sid,type);
			SessionMap smap=dsmanager.getSessionMap();
			smap.send(data);
			numFiler();
			chatMessages.add(message);
			data.clear();
			return data;
		}
		// ����Ƶ���������
		else if(type==ChatMessage.OFFICER_ALLIANCE)
		{
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			Alliance alliance=null;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					Integer.parseInt(allianceId)+"");
			}
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			int sid=data.readUnsignedShort();
			int oid=data.readInt();
			Officer officer=OfficerManager.getInstance().getOfficer(player,
				oid,sid);
			if(officer==null)
				throw new DataAccessException(0,"officer not exist");
			message.setSid(officer.getSid());
			message.setText("");
			message.setLevel(officer.getLevel());
			message.setPlayerType(officer.getMilitaryRank());
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
			// getAchieveData(data,player,sid,type);
			sendAllAlliancePlayers(data,alliance);
			allianceChatFiler(Integer.parseInt(allianceId));
			// ��ӱ���
			addAllianceChat(Integer.parseInt(allianceId),message);
			data.clear();
			return data;
		}
		// ˽�ķ������
		else if(type==ChatMessage.OFFICER_FRIEND)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.PRIVATE_CHAT_LEVEL,player,
				"self_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			int sid=data.readUnsignedShort();
			int oid=data.readInt();
			Officer officer=OfficerManager.getInstance().getOfficer(player,
				oid,sid);
			if(officer==null)
				throw new DataAccessException(0,"officer not exist");
			String name=data.readUTF();
			Player bplayer=objectFactory.getPlayerByName(name,false);
			data.clear();
			if(bplayer==null)
			{
				throw new DataAccessException(0,"friend not online");
			}
			if(bplayer.getSource()==null)
			{
				throw new DataAccessException(0,"friend not online");
			}
			Session s=(Session)bplayer.getSource();
			if(s.getConnect()==null||!s.getConnect().isActive())
			{
				throw new DataAccessException(0,"friend not online");
			}
			// getAchieveData(data,player,sid,type);
			message.setSid(officer.getSid());
			message.setText("");
			message.setLevel(officer.getLevel());
			message.setPlayerType(officer.getMilitaryRank());
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
			data.writeUTF(bplayer.getName());
			s.getConnect().send(data);
			connect.send(data);
			data.clear();
			return data;
		}
		else if(type==ChatMessage.REPORT_MSG)
		{
			String reportPlayer=data.readUTF();
			String beReportedPlayer=data.readUTF();
			String beReportedContent=data.readUTF();
			reportManager.addReport(reportPlayer,beReportedPlayer,beReportedContent);
			data.clear();
			data.writeBoolean(true);
			return data;
		}
		String text=data.readUTF();
		if(text!=null&&text.length()>MAX_MSG_LENGTH)
			text=text.substring(0,MAX_MSG_LENGTH-1);
//		message.setType(type);
//		message.setSrc(player.getName());
		// �����ֹ���
		text=ChatMessage.filerText(text);
		message.setText(text);
		message.setLocale(player.getLocale());
		boolean bool=true;
		// ˽��ʧ��������
		int subType=0;
		// ����������Ϣ
		if(message.getType()==ChatMessage.WORLD_CHAT)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.WORLD_CHAT_LEVEL,player,
				"world_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			if("1".equals(player.getAttributes(PublicConst.FORBID_CHAT)))
			{
				SeaBackKit.sendMsgToOne(message,player);
				
			}else
			{
				if("1".equals(player.getAttributes(PublicConst.PLAYER_GM)))
				{
					message.setType(ChatMessage.GM_WORLD_CHAT);
				}
				numFiler();
				SeaBackKit.sendAllMsg(message,dsmanager,PublicConst.CHAT_GROUP_LOCALE);
				chatMessages.add(message);
			}
		}
		// ��������
		else if(message.getType()==ChatMessage.ALLIANCE_CHAT)
		{
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			Alliance alliance=null;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					Integer.parseInt(allianceId)+"");
			}
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			allianceChatFiler(Integer.parseInt(allianceId));
			// ��ӱ���
			addAllianceChat(Integer.parseInt(allianceId),message);
			SeaBackKit.sendMsgForAlliance(message,dsmanager,alliance); 
		}
		// ˽��
		else if(message.getType()==ChatMessage.CHAT_SELF)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.PRIVATE_CHAT_LEVEL,player,
				"self_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			String dest=data.readUTF();
			Player destPlayer=objectFactory.getPlayerByName(dest,false);
			if(destPlayer!=null)
			{
				checkString=SeaBackKit.checkChatOpen(
					ContextVarManager.PRIVATE_CHAT_LEVEL,destPlayer,
					"dest_self_chat_level_limit");
				if(checkString!=null)
					throw new DataAccessException(0,checkString);
			}
			message.setDest(dest);
			bool=SeaBackKit.sendOneToOneMsg(message,dsmanager,player);
			// ǰ̨�Լ�Ҳ��Ҫ
			if(bool)
			{
				ByteBuffer selfdata=new ByteBuffer();
				selfdata.writeShort(PublicConst.MESSAGE_PORT);
				selfdata.writeByte(message.getType());
				selfdata.writeInt(message.getTime());
				selfdata.writeUTF(message.getText());
				selfdata.writeUTF(message.getSrc());
				selfdata.writeUTF(message.getDest());
				connect.send(selfdata);
			}else
			{
				subType=SeaBackKit.checkPlayer(message.getDest(),player.getId(),
					objectFactory);
			}
		}
		// ս��
		else if(message.getType()==ChatMessage.FIGHT_DATA)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.WORLD_CHAT_LEVEL,player,
				"world_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			String txt=message.getText();
			//ս�����ݼ�� ��ֹǰ̨����
			String[] txts=TextKit.split(txt," ");
			if(txts.length<2||txts[0].equals(""))
			{
				message.setText("??"+message.getText());
			}
			int messageId=data.readInt();
			boolean deal=data.readBoolean();
			//����ս��
			if(deal)
				structureMessage(message,messageId);
			message.setMessageId(messageId);
			//�ɾ����ݲɼ�
			AchieveCollect.shareFightData(player);
			numFiler();
			SeaBackKit.sendAllMsg(message,dsmanager,PublicConst.CHAT_GROUP_LOCALE);
			chatMessages.add(message);
		}
		// ����ս������
		else if(message.getType()==ChatMessage.ALLIANCE_FIGHT_DATA)
		{
			int messageId=data.readInt();
			boolean deal=data.readBoolean();
			//����ս��
			if(deal)
				structureMessage(message,messageId);
			message.setMessageId(messageId);
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			Alliance alliance=null;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					Integer.parseInt(allianceId)+"");
			}
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			data.clear();
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
//			data.writeByte(message.getType());
//			data.writeUTF(message.getText());
//			data.writeUTF(message.getSrc());
//			if(message.getType()==ChatMessage.ALLIANCE_FIGHT_DATA)
//			{
//				data.writeInt(message.getMessageId());
//			}
			sendAllAlliancePlayers(data,alliance);
			
			allianceChatFiler(Integer.parseInt(allianceId));
			// ��ӱ���
			addAllianceChat(Integer.parseInt(allianceId),message);
			//�ɾ����ݲɼ�
			AchieveCollect.shareFightData(player);
		}
		//����ս������ 
		else if(message.getType()==ChatMessage.CHAT_SELF_FIGHT_DATA)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.PRIVATE_CHAT_LEVEL,player,
				"self_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			String dest=data.readUTF();
			int messageId=data.readInt();
			boolean deal=data.readBoolean();
			//����ս��
			if(deal)
				structureMessage(message,messageId);
			message.setDest(dest);
			message.setMessageId(messageId);
			bool=SeaBackKit.sendOneToOneFightId(message,dsmanager,player,messageId);
			// ǰ̨�Լ�Ҳ��Ҫ
			if(bool)
			{
				ByteBuffer selfdata=new ByteBuffer();
				selfdata.writeShort(PublicConst.MESSAGE_PORT);
				message.setMessageId(messageId);
				message.showBytesWrite(selfdata,true);
				selfdata.writeUTF(message.getDest());
				connect.send(selfdata);
			}
			//�ɾ����ݲɼ�
			AchieveCollect.shareFightData(player);
		}
		// ��������
		else if(message.getType()==ChatMessage.SHIP_INFO_WORLD)
		{
			data.clear();
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.WORLD_CHAT_LEVEL,player,
				"world_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			SeaBackKit.sendAllMsg(message,dsmanager,
				PublicConst.CHAT_GROUP_LOCALE);
			chatMessages.add(message);
			numFiler();
		}
		// ���˽�������
		else if(message.getType()==ChatMessage.SHIP_INFO_ALLIANCE)
		{
			data.clear();
			String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
			Alliance alliance=null;
			if(allianceId!=null&&!allianceId.equals(""))
			{
				// ���˲�����
				alliance=(Alliance)objectFactory.getAllianceMemCache().load(
					Integer.parseInt(allianceId)+"");
			}
			if(alliance==null)
				throw new DataAccessException(0,"alliance is null");
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
			sendAllAlliancePlayers(data,alliance);

			allianceChatFiler(Integer.parseInt(allianceId));
			// ��ӱ���
			addAllianceChat(Integer.parseInt(allianceId),message);
		}
		// ���˽�������
		else if(message.getType()==ChatMessage.SHIP_INFO_FRIEND)
		{
			String dest=data.readUTF();
			data.clear();
			message.setDest(dest);
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.PRIVATE_CHAT_LEVEL,player,
				"self_chat_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			Player destPlayer=objectFactory.getPlayerByName(dest,false);
			if(!SeaBackKit.isPlayerOnline(destPlayer))
				throw new DataAccessException(0,"friend not online");
			SeaBackKit.sendMsgToOne(message,destPlayer);
			data.clear();
			// ���ͷ���Ҫ
			data.writeShort(PublicConst.MESSAGE_PORT);
			message.showBytesWrite(data,true);
			data.writeUTF(destPlayer.getName());
			connect.send(data);
			data.clear();
		}
		data.clear();
		data.writeBoolean(bool);
		if(!bool&&type==ChatMessage.CHAT_SELF)
		{
			data.writeByte(subType);
		}
		return data;
	}

	/** ���˹㲥 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance)
	{
		SessionMap smap=dsmanager.getSessionMap();
		Session[] sessions=smap.getSessions();
		Player player=null;
		Connect con=null;
		IntList list=alliance.getPlayerList();
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					for(int j=0;j<list.size();j++)
					{
						if(player.getId()==list.get(j))
						{
							con.send(data);
						}
					}
				}
			}
		}
	}

	/**���һ������ս������**/
	public  void addAllianceFightData(ChatMessage message,Alliance alliance)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(PublicConst.MESSAGE_PORT);
		message.showBytesWrite(data,true);
		
		sendAllAlliancePlayers(data,alliance);
		
		allianceChatFiler(alliance.getId());
		// ��ӱ���
		addAllianceChat(alliance.getId(),message);
	}
	
	
	/**����ϵͳ�ʼ�������(����ս���ķ���)**/
	public void structureMessage(ChatMessage cmessage,int messageId)
	{
		MessageSave messageSave=(MessageSave)objectFactory.getMessageCache().getCacheMap().get(messageId);
		if(messageSave==null)
			throw new DataAccessException(0,"mail not exist");
		Message message=messageSave.getData();
		// ����ϵͳ��Ϣ
		String text=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"send_alliance_report");
		String[] title=message.getAllianceFightTitle();
		text=TextKit.replace(text,"%",title[0]+"("+title[1]+")");
		text=TextKit.replace(text,"%",title[2]+"("+title[3]+")");
		text+=" "+PublicConst.FIGHT_TYPE_18;
		cmessage.setText(text);
	}
	/**
	 * @return dsmanager
	 */
	public SWDSManager getDsmanager()
	{
		return dsmanager;
	}

	/**
	 * @param dsmanager Ҫ���õ� dsmanager
	 */
	public void setDsmanager(SWDSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	public ArrayList<ChatMessage> getChatMessages()
	{
		return chatMessages;
	}

	
	public void setChatMessages(ArrayList<ChatMessage> chatMessages)
	{
		this.chatMessages=chatMessages;
	}

	
	public ReportPlayerManager getReportManager()
	{
		return reportManager;
	}

	
	public void setReportManager(ReportPlayerManager reportManager)
	{
		this.reportManager=reportManager;
	}

	
	public IntKeyHashMap getAlliancesChat()
	{
		return alliancesChat;
	}
	
}
