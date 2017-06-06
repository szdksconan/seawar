package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.set.SetKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.ContextVarManager;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.comparator.MessageComparator;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;

/** �ʼ��˿� 1012 */
public class MessagePort extends AccessPort
{
	private static Logger log=LogFactory.getLogger(MessagePort.class);
	
	/** �ռ��䣬���棬������  ��С*/
	public static final int GET_SIZE=100,REPORT_SIZE=100,SEND_SIZE=50;
	/** ��ҳ��С */
	public static final int PAGE_SIZE=10;

	CreatObjectFactory factory;

	public static final int SEND_EMAIL=0,// �����ʼ�
					DELETE_EMAIL=1,// ɾ��ָ��id���ʼ�
					DELETE_SYSTEM_PLAYER_EMAIL=2,// ɾ������ϵͳ������ʼ�
					DELETE_BATTLE_EMAIL=3,// ɾ������ս��
					DELETE_SEND_EMAIL=4,// ɾ�������ѷ��ʼ�
					GET_SYSTEM_PLAYER_EMAIL=5,// ��ȡϵͳ������ʼ�
					GET_BATTLE_EMAIL=6,// ��ȡս��
					GET_SEND_EMAIL=7,// ��ȡ�����ʼ�;
					GET_MESSAGE_CONTENT=8,// ��ȡĳ���ʼ�����
					GET_READ_NUM=9,// ��ȡ�Ѿ��ʼ�����
					GET_FIGHT_DATA=10,// ��ȡս�������������
					VIEW_HAVA_MESSAGE=11,
					GET_OTHER_FIGHT_DATA=12,
					ALLIANCE_MESSAGES=13,// �Ƿ���δ���ʼ�
					GET_MES_AWARD=20;//��ȡ����

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		// TODO �Զ����ɷ������
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		// ����
		int type=data.readUnsignedByte();
		if(type==SEND_EMAIL)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.EMAIL_LEVEL,player,
				"email_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			String title=data.readUTF();
			String receiveName=data.readUTF();
			Player revice=factory.getPlayerByName(receiveName,false);
			if(receiveName.equals(player.getName()))
				throw new DataAccessException(0,"you can not sendSelf");
			if(revice==null)
				throw new DataAccessException(0,"player is not exist");
			boolean isInBlackList=SeaBackKit.isInBlackList(revice,player.getId());
			String content=data.readUTF();
			Message message=factory.createMessage(player.getId(),revice
				.getId(),content,player.getName(),receiveName,0,title,true,isInBlackList,null);
			message.addState(Message.READ);
			if(!isInBlackList)
				JBackKit.sendRevicePlayerMessage(revice,message,message
					.getRecive_state(),factory);
			collateSendMail(player);
			data.clear();
			data.writeInt(message.getMessageId());
		}
		// �����ʼ�
		else if(type==ALLIANCE_MESSAGES)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.EMAIL_LEVEL,player,
				"email_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			Alliance alliance=null;
			// �����������
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				alliance=(Alliance)factory.getAllianceMemCache().loadOnly(
					player.getAttributes(PublicConst.ALLIANCE_ID));
			}
			if(alliance==null)
				throw new DataAccessException(0,"you have no alliance");
			// Ȩ���ж�
			String str=checkAllianceMaster(player);
			if(str!=null) throw new DataAccessException(0,str);
			String title=data.readUTF();
			String content=data.readUTF();
			IntList list=alliance.getPlayerList();
			for(int i=0;i<list.size();i++)
			{
				Player bePlayer=factory.getPlayerById(list.get(i));
				if(bePlayer==null) continue;
				Message message=factory.createMessage(player.getId(),
					bePlayer.getId(),content,player.getName(),bePlayer
						.getName(),0,title,false);
				message.setMass(1);
				if(bePlayer.getName().equals(player.getName()))
				{
					message.addState(Message.READ);
					message.addReciveState(Message.READ);
				}
				JBackKit.sendRevicePlayerMessage(bePlayer,message,message
					.getRecive_state(),factory);
			}
		}
		else if(type==DELETE_EMAIL)
		{
			int len=data.readUnsignedByte();
			IntList rmlist=new IntList();
			for(int i=len;i>0;i--)
			{
				int messageId=data.readInt();
				Message message=(Message)factory.getMessageCache().load(
					messageId+"");
				if(message==null)
				{
					rmlist.add(messageId);
					JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
					throw new DataAccessException(0,"message is null");
				}
				if(message.getAward()!=null
					&&!player.getAnnex().contain(message.getMessageId()))
				{
					JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
					throw new DataAccessException(0,"have annex");
				}
				// �����ϵͳ�ʼ�
				if(message.getMessageType()==Message.SYSTEM_TYPE)
				{
					player.getIsland().addStateSystemMessage(Message.ONE_DELETE,
						message.getMessageId());
					rmlist.add(messageId);
				}
				// �����ս��
				else if(message.getMessageType()==Message.FIGHT_TYPE)
				{
					message.addReciveState(Message.ONE_DELETE);
					message.setDelete(Message.DELETE);
					rmlist.add(messageId);
				}
				// �����ʼ�
				else if(player.getId()==message.getSendId()
					&&player.getId()==message.getReceiveId())
				{
					message.addState(Message.ONE_DELETE);
					message.addReciveState(Message.ONE_DELETE);
					rmlist.add(messageId);
				}
				// ����Ƿ�����
				else if(player.getId()==message.getSendId())
				{
					message.addState(Message.ONE_DELETE);
					rmlist.add(messageId);
				}
				// ������ռ���
				else if(player.getId()==message.getReceiveId())
				{
					message.addReciveState(Message.ONE_DELETE);
					rmlist.add(messageId);
				}
				else
				{
					JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
					throw new DataAccessException(0,"mail_type_error");
				}
			}
			JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
		}
		// ɾ������ʼ���ϵͳ�ʼ�
		else if(type==DELETE_SYSTEM_PLAYER_EMAIL)
		{
			// ���
			Object[] msgs=getReceiveMsg(player);
			if(msgs!=null&&msgs.length>0)
			{
				boolean all_annex=false;
				for(int i=0;i<msgs.length;i++)
				{
					Message mes=(Message)msgs[i];
					if(mes.getAward()!=null
						&&!player.getAnnex().contain(mes.getMessageId()))
					{
						all_annex=true;
					}
					else
					{
						all_annex=false;
						break;
					}
				}
				if(all_annex)
				{
					throw new DataAccessException(0,"have annex");
				}
			}
			IntList rmlist0=clearMessages(0,player,false);
			IntList rmlist1=clearMessages(Message.SYSTEM_ONE_TYPE,player,
				false);
			// ���ϵͳ�ʼ�
			ArrayList list=factory.getMessageCache().getSystemMessageMap();
			IntList rmlist2=new IntList();
			for(int i=0;i<list.size();i++)
			{
				Message message=(Message)list.get(i);
				if(message.getAward()!=null
					&&message.getCreateAt()<player.getCreateTime())
					continue;
				if(message.getAward()!=null
					&&!player.getAnnex().contain(message.getMessageId()))
					continue;
				player.getIsland().addStateSystemMessage(Message.ONE_DELETE,
					message.getMessageId());
				rmlist2.add(message.getMessageId());
			}
			linkList(rmlist0,rmlist1);
			linkList(rmlist0,rmlist2);
			JBackKit.sendRMmsg(rmlist0,player,getMsgNum(player));
		}
		// ɾ��ս��
		else if(type==DELETE_BATTLE_EMAIL)
		{
			IntList rmlist=clearMessages(Message.FIGHT_TYPE,player,false);
			JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
		}
		// ��շ�����
		else if(type==DELETE_SEND_EMAIL)
		{
			IntList rmlist=clearMessages(0,player,true);
			JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
		}
		// ��ȡ����ʼ�+ϵͳ�ʼ�
		else if(type==GET_SYSTEM_PLAYER_EMAIL)
		{
			int pageIndex=data.readUnsignedByte();
			writeData(data,getReceiveMsg(player),pageIndex,player,true);
		}
		// ��ȡս��
		else if(type==GET_BATTLE_EMAIL)
		{
			int pageIndex=data.readUnsignedByte();
			Object messages[]=sort(getMessages(Message.FIGHT_TYPE,player,
				false));
			writeData(data,messages,pageIndex,player,true);
		}
		// ��ȡ������
		else if(type==GET_SEND_EMAIL)
		{
			int pageIndex=data.readUnsignedByte();
			Object messages[]=sort(getMessages(Message.PLAYER_TYPE,player,
				true));
			writeData(data,messages,pageIndex,player,false);
		}
		// ��ȡĳ���ż�������
		else if(type==GET_MESSAGE_CONTENT)
		{
			int messageId=data.readInt();
			Message message=(Message)factory.getMessageCache().load(
				messageId+"");
			if(message==null)
			{
				throw new DataAccessException(0,"mail not exist");
			}
			data.clear();
			if(message.getMessageType()==Message.SYSTEM_TYPE)
			{
				// ���ϵͳ�ʼ�Ϊ�Ѷ�
				player.getIsland().addStateSystemMessage(Message.READ,
					message.getMessageId());
				data.writeUTF(message.getContent());
			}
			// ����Ƿ�����
			else if(player.getId()==message.getSendId())
			{
				message.addState(Message.READ);
				data.writeUTF(message.getContent());
			}
			// ������ռ���
			else if(player.getId()==message.getReceiveId())
			{
				message.addReciveState(Message.READ);
				data.writeUTF(message.getContent());
			}
			else
			{
				log.info("=======player_id"+player.getId()+",player_name="+player.getName()+",recv_id="+message.getReceiveId()+",send_id="+message.getSendId()+
					",recv_name="+message.getReceiveName()+",send_name="+message.getSendName());
				throw new DataAccessException(0,"mail_type_error");
			}
		}
		else if(type==GET_READ_NUM)
		{
			// ��ͨ�ʼ�
			int[] num=getMsgNum(player);
			data.clear();
			for(int i=0;i<num.length;i++)
			{
				data.writeShort(num[i]);
			}
		}
		// ��ȡս�������������
		else if(type==GET_FIGHT_DATA)
		{
			int messageId=data.readInt();
			Message message=(Message)factory.getMessageCache().load(
				messageId+"");
			if(message==null) throw new DataAccessException(0,"fight_report_not_exists");
			message.addReciveState(Message.READ);
			data.clear();
			// �������
			if(message.getFightType()==Message.FIGHT_TYPE_ZHE_CHA
				||message.getFightType()==Message.RETURN_BACK)
			{
				if(message.getFightDataFore()==null)
					throw new DataAccessException(0,"data is null");
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
			}
			// ս������
			else
			{
				// ��дս������֮ǰ������
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
				ByteBuffer temp=new ByteBuffer();
				// �ݲ������Ƿ����
				boolean isNullFightData=message.getFightData()==null
					||message.getFightData().length()<=0;
				temp.writeBoolean(!isNullFightData);
				// ��װս���汾
				temp.writeInt(message.getFightVersion());
				if(!isNullFightData)
				{
					// ��װ�ݲ�����
					temp.write(message.getFightData().getArray(),message
						.getFightData().offset(),message.getFightData().length());
				}
				// ��װ������Ϣ
				temp.write(message.getOfficerData().getArray(),message
					.getOfficerData().offset(),message.getOfficerData()
					.length());
				data.writeData(temp.getArray(),temp.offset(),temp.length());
			}
			// ���ٹ�ѫֵ,���ݸ���ս��������ʾ��ѫֵ�����ݽṹ
			data.writeShort(message.getFeats());
		}
		// �Ƿ���δ���ʼ�
		else if(type==VIEW_HAVA_MESSAGE)
		{
			boolean noRead=false;
			data.clear();
			int num[]=getMessageNum(Message.PLAYER_TYPE,player,false);
			int num1[]=getMessageNum(Message.SYSTEM_ONE_TYPE,player,false);
			if(num[1]+num1[1]<num[0]+num1[0]) noRead=true;
			num=getMessageNum(Message.FIGHT_TYPE,player,false);
			if(num[1]<num[0]) noRead=true;
			data.writeBoolean(noRead);
		}
		// ����
		else if(type==GET_OTHER_FIGHT_DATA)
		{
			int messageId=data.readInt();
			Message message=(Message)factory.getMessageCache().load(
				messageId+"");
			if(message==null)
				message=(Message)factory.getArenaManager().getReportObject(messageId);
			if(message==null)
			{
				throw new DataAccessException(0,"message has been deleted");
			}
			data.clear();
			data.writeByte(message.getMessageType());
			data.writeInt(message.getCreateAt());
			if(message.getMessageType()==Message.ARENA)
				data.writeByte(message.getFightType());
			// �������
			if(message.getFightType()==Message.FIGHT_TYPE_ZHE_CHA
				||message.getFightType()==Message.RETURN_BACK)
			{
				if(message.getFightDataFore()==null)
					throw new DataAccessException(0,"data is null");
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
			}
			// ս������
			else
			{
				// ��дս������֮ǰ������
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
				ByteBuffer temp=new ByteBuffer();
				// �ݲ������Ƿ����
				boolean isNullFightData=message.getFightData()==null
					||message.getFightData().length()<=0;
				temp.writeBoolean(!isNullFightData);
				// ��װս���汾
				temp.writeInt(message.getFightVersion());
				if(!isNullFightData)
				{
					// ��װ�ݲ�����
					temp.write(message.getFightData().getArray(),message
						.getFightData().offset(),message.getFightData().length());
				}
				// ��װ������Ϣ
				temp.write(message.getOfficerData().getArray(),message
					.getOfficerData().offset(),message.getOfficerData()
					.length());
				data.writeData(temp.getArray(),temp.offset(),temp.length());
			}
			// ���ٹ�ѫֵ,���ݸ���ս��������ʾ��ѫֵ�����ݽṹ
			data.writeShort(message.getFeats());
		}
		//��ȡ�ʼ�����
		else if(type==GET_MES_AWARD)
		{
			int messageId=data.readInt();
			if(player.getAnnex().contain(messageId))
			{
				throw new DataAccessException(0,"got annex");// ����ȡ����
			}
			Message message=(Message)factory.getMessageCache().load(
				messageId+"");
			if(message==null)
			{
				throw new DataAccessException(0,"message is null");
			}
			if(message.getMessageType()!=Message.SYSTEM_TYPE
				&&message.getMessageType()!=Message.SYSTEM_ONE_TYPE)
			{
				throw new DataAccessException(0,"not system mail");// ��ϵͳ�ʼ�
			}
			if(message.getMessageType()==Message.SYSTEM_ONE_TYPE
				&&message.getReceiveId()!=player.getId())
			{
				throw new DataAccessException(0,"other player mail");// ���Լ����ʼ�
			}
			if(message.getAward()==null)
			{
				throw new DataAccessException(0,"no annex");// �޸���
			}
			message.getAward().awardLenth(data,player,factory,null,null);
			player.getAnnex().add(messageId);
		}
		return data;
	}
	/**
	 * @return factory
	 */
	public CreatObjectFactory getFactory()
	{
		return factory;
	}

	/**
	 * @param factory Ҫ���õ� factory
	 */
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	/** д���ݸ�ǰ̨ */
	public void writeData(ByteBuffer data,Object messages[],int pageIndex,
		Player player,boolean state)
	{
		if(messages==null)
		{
			data.clear();
			data.writeByte(0);
			return;
		}
		int num=messages.length-pageIndex*PAGE_SIZE;
		if(num<0) num=0;
		if(num>PAGE_SIZE) num=PAGE_SIZE;
		data.clear();
		data.writeByte(num);
		for(int i=pageIndex*PAGE_SIZE;i<(pageIndex+1)*PAGE_SIZE;i++)
		{
			if(i>=messages.length) break;
			Message message=(Message)messages[i];
			if(message.getMessageType()==Message.SYSTEM_TYPE)
			{
				message.showBytesWrite(data,player.getIsland()
					.getStateMessage(message),"",player);
			}
			else
			{
				if(state)
				{
					message.showBytesWrite(data,message.getRecive_state(),
						"",player);
				}
				else
				{
					message
						.showBytesWrite(data,message.getState(),"",player);
				}
			}
		}
	}

	/** ��ȡĳ�����͵����ʼ����Ѷ��ʼ� */
	public int[] getMessageNum(int messageType,Player player,boolean selfSend)
	{
		ArrayList messages=getMessages(messageType,player,selfSend);
		int num[]=new int[2];
		if(messages==null) return num;
		num[0]=messages.size();
		int read=0;
		// �����Ѷ��ʼ�
		for(int i=0;i<messages.size();i++)
		{
			Message message=(Message)messages.get(i);
			// �ռ���
			if(!selfSend)
			{
				if(message.getMessageType()==Message.SYSTEM_TYPE)
				{
					if(message.getAward()!=null
						&&message.getCreateAt()<player.getCreateTime())
					{
						num[0]--;
						continue;
					}
					if(player.getIsland().isStateMessage(message,
						Message.READ))
					{
						read++;
					}
				}
				if(message.checkReciveState(Message.READ)) read++;
			}
		}
		num[1]=read;
		return num;
	}
	/** ��ȡĳ�����͵��ʼ� selfSend�Ƿ��Ƿ����� */
	public ArrayList getMessages(int messageType,Player player,
		boolean selfSend)
	{
		// ����ռ���
		ArrayList messages=getNotServerMessages(messageType,player,selfSend);
		// ���ǻ�ȡ������ �Ҳ���ս��
		if(!selfSend&&messageType==Message.SYSTEM_ONE_TYPE)
		{
			// ���ϵͳ�ʼ�
			ArrayList systemList=factory.getMessageCache()
				.getSystemMessageMap();
			for(int i=0;i<systemList.size();i++)
			{
				Message message=(Message)systemList.get(i);
				if(message.getAward()!=null
					&&message.getCreateAt()<player.getCreateTime())
					continue;
				// ���Լ�����ɾ��
				if(!player.getIsland().isStateMessage(message,
					Message.ONE_DELETE))
				{
					messages.add(message);
				}
			}
		}
		return messages;
	}
	
	/** ��ȡ��ȫ���ʼ� */
	public ArrayList getNotServerMessages(int messageType,Player player,
		boolean selfSend)
	{
		ArrayList messageList=factory.getMessageCache().getMessageListById(
			player.getId());
		// ����ռ���
		ArrayList messages=new ArrayList();
		if(messageList!=null)
		{
			for(int i=0;i<messageList.size();i++)
			{
				Message message=(Message)messageList.get(i);			
				// ������
				if(selfSend)
				{
					if(message.getDelete()==Message.ONE_DELETE) continue;
//					if(!message.getSendName().equals(player.getName()))
//						continue;
					if(message.getSendId()!=player.getId())
						continue;
					if(message.checkState(Message.ONE_DELETE)) continue;
					messages.add(message);
				}
				else
				{
					if(message.getDelete()==Message.ONE_DELETE) continue;
					if(message.getReceiveId()!=player.getId())
						continue;
					if(message.getSendId()==message.getReceiveId())
						continue;
					if(message.getMessageType()!=messageType) continue;
					if(message.checkReciveState(Message.ONE_DELETE))
						continue;
					messages.add(message);
				}
			}
		}
		return messages;
	}

	public Object[] sort(ArrayList messages)
	{
		if(messages==null) return null;
		Object[] objects=messages.toArray();
		SetKit.sort(objects,MessageComparator.getInstance());
		return objects;
	}

	/** ���ĳ�����͵��ʼ� selfSend�Ƿ��Ƿ����� */
	public IntList clearMessages(int messageType,Player player,boolean selfSend)
	{
		ArrayList messageList=factory.getMessageCache().getMessageListById(
			player.getId());
		IntList rmlist=new IntList();
		if(messageList==null) return rmlist;
		ArrayList list=new ArrayList();
		for(int i=0;i<messageList.size();i++)
		{
			Message message=(Message)messageList.get(i);
			// �����ʼ�
			if(message.getSendId()==player.getId()&&player.getId()==message.getReceiveId())
			{
				message.addState(Message.ONE_DELETE);
				message.addReciveState(Message.ONE_DELETE);
				continue;
			}
			// ������
			if(selfSend)
			{
				if(message.getSendId()==player.getId())
				{
					// �Լ�Ϊ������
					message.addState(Message.ONE_DELETE);
					rmlist.add(message.getMessageId());
				}
			}
			else
			{
				if(!(message.getReceiveId()==player.getId())) continue;
				if(message.getMessageType()!=messageType) continue;
				if(message.getAward()!=null
					&&!player.getAnnex().contain(message.getMessageId()))
					continue;
				// �Լ�Ϊ�ռ���
				message.addReciveState(Message.ONE_DELETE);
				if(messageType==Message.FIGHT_TYPE)
				{
					message.setDelete(Message.DELETE);
				}
				rmlist.add(message.getMessageId());
			}
			// ����ı��б�
			factory.getMessageCache().load(message.getMessageId()+"");
		}
		for(int i=0;i<list.size();i++)
		{
			Message message=(Message)list.get(i);
			messageList.remove(message);
			rmlist.add(message.getMessageId());
		}
		return rmlist;
	}

	/** ����Ȩ���ж� */
	public String checkAllianceMaster(Player player)
	{
		// ���˲�����
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			player.getAttributes(PublicConst.ALLIANCE_ID));
		// �Ƿ��ǻ᳤�򸱻᳤
		if(!alliance.isMaster(player.getId()))
		{
			throw new DataAccessException(0,"you are not master");
		}
		return null;
	}
	
	/** �������� */
	public void collateSendMail(Player player)
	{
		Object messages[]=sort(getMessages(Message.PLAYER_TYPE,player,true));
		if(messages!=null&&messages.length>SEND_SIZE)
		{
			for(int i=messages.length-1;i>=MessagePort.SEND_SIZE;i--)
			{
				if(messages[i]==null) continue;
				Message mes=(Message)messages[i];
				mes.addState(Message.ONE_DELETE);
			}
		}
	}
	/** ��ȡ�ռ��� */
	public Object[] getReceiveMsg(Player player)
	{
		ArrayList list1=getMessages(Message.PLAYER_TYPE,player,false);
		ArrayList list2=getMessages(Message.SYSTEM_ONE_TYPE,player,false);
		for(int i=0;i<list2.size();i++)
		{
			list1.add(list2.get(i));
		}
		return sort(list1);
	}
	/** ���� intlist */
	public void linkList(IntList list0,IntList list1)
	{
		for(int i=0;i<list1.size();i++)
		{
			list0.add(list1.get(i));
		}
	}
	/** ��ȡ�����ʼ����� */
	public int[] getMsgNum(Player player)
	{
		int[] msx_num=new int[5];
		int num[]=getMessageNum(Message.PLAYER_TYPE,player,false);
		int num1[]=getMessageNum(Message.SYSTEM_ONE_TYPE,player,false);
		msx_num[0]=num[0]+num1[0];
		msx_num[1]=num[1]+num1[1];
		num=getMessageNum(Message.FIGHT_TYPE,player,false);
		msx_num[2]=num[0];
		msx_num[3]=num[1];
		num=getMessageNum(Message.PLAYER_TYPE,player,true);
		msx_num[4]=num[0];
		return msx_num;
	}
}
