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

/** 邮件端口 1012 */
public class MessagePort extends AccessPort
{
	private static Logger log=LogFactory.getLogger(MessagePort.class);
	
	/** 收件箱，报告，发件箱  大小*/
	public static final int GET_SIZE=100,REPORT_SIZE=100,SEND_SIZE=50;
	/** 分页大小 */
	public static final int PAGE_SIZE=10;

	CreatObjectFactory factory;

	public static final int SEND_EMAIL=0,// 发送邮件
					DELETE_EMAIL=1,// 删除指定id的邮件
					DELETE_SYSTEM_PLAYER_EMAIL=2,// 删除所有系统和玩家邮件
					DELETE_BATTLE_EMAIL=3,// 删除所有战报
					DELETE_SEND_EMAIL=4,// 删除所有已发邮件
					GET_SYSTEM_PLAYER_EMAIL=5,// 获取系统和玩家邮件
					GET_BATTLE_EMAIL=6,// 获取战报
					GET_SEND_EMAIL=7,// 获取发送邮件;
					GET_MESSAGE_CONTENT=8,// 获取某封邮件内容
					GET_READ_NUM=9,// 获取已经邮件数量
					GET_FIGHT_DATA=10,// 获取战报或者侦查内容
					VIEW_HAVA_MESSAGE=11,
					GET_OTHER_FIGHT_DATA=12,
					ALLIANCE_MESSAGES=13,// 是否有未读邮件
					GET_MES_AWARD=20;//领取附件

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		// TODO 自动生成方法存根
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
		// 类型
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
		// 联盟邮件
		else if(type==ALLIANCE_MESSAGES)
		{
			String checkString=SeaBackKit.checkChatOpen(
				ContextVarManager.EMAIL_LEVEL,player,
				"email_level_limit");
			if(checkString!=null)
				throw new DataAccessException(0,checkString);
			Alliance alliance=null;
			// 检查有无联盟
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				alliance=(Alliance)factory.getAllianceMemCache().loadOnly(
					player.getAttributes(PublicConst.ALLIANCE_ID));
			}
			if(alliance==null)
				throw new DataAccessException(0,"you have no alliance");
			// 权限判定
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
				// 如果是系统邮件
				if(message.getMessageType()==Message.SYSTEM_TYPE)
				{
					player.getIsland().addStateSystemMessage(Message.ONE_DELETE,
						message.getMessageId());
					rmlist.add(messageId);
				}
				// 如果是战报
				else if(message.getMessageType()==Message.FIGHT_TYPE)
				{
					message.addReciveState(Message.ONE_DELETE);
					message.setDelete(Message.DELETE);
					rmlist.add(messageId);
				}
				// 联盟邮件
				else if(player.getId()==message.getSendId()
					&&player.getId()==message.getReceiveId())
				{
					message.addState(Message.ONE_DELETE);
					message.addReciveState(Message.ONE_DELETE);
					rmlist.add(messageId);
				}
				// 玩家是发件人
				else if(player.getId()==message.getSendId())
				{
					message.addState(Message.ONE_DELETE);
					rmlist.add(messageId);
				}
				// 玩家是收件人
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
		// 删除玩家邮件和系统邮件
		else if(type==DELETE_SYSTEM_PLAYER_EMAIL)
		{
			// 检测
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
			// 清空系统邮件
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
		// 删除战报
		else if(type==DELETE_BATTLE_EMAIL)
		{
			IntList rmlist=clearMessages(Message.FIGHT_TYPE,player,false);
			JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
		}
		// 清空发件箱
		else if(type==DELETE_SEND_EMAIL)
		{
			IntList rmlist=clearMessages(0,player,true);
			JBackKit.sendRMmsg(rmlist,player,getMsgNum(player));
		}
		// 获取玩家邮件+系统邮件
		else if(type==GET_SYSTEM_PLAYER_EMAIL)
		{
			int pageIndex=data.readUnsignedByte();
			writeData(data,getReceiveMsg(player),pageIndex,player,true);
		}
		// 获取战报
		else if(type==GET_BATTLE_EMAIL)
		{
			int pageIndex=data.readUnsignedByte();
			Object messages[]=sort(getMessages(Message.FIGHT_TYPE,player,
				false));
			writeData(data,messages,pageIndex,player,true);
		}
		// 获取发件箱
		else if(type==GET_SEND_EMAIL)
		{
			int pageIndex=data.readUnsignedByte();
			Object messages[]=sort(getMessages(Message.PLAYER_TYPE,player,
				true));
			writeData(data,messages,pageIndex,player,false);
		}
		// 获取某个信件的内容
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
				// 添加系统邮件为已读
				player.getIsland().addStateSystemMessage(Message.READ,
					message.getMessageId());
				data.writeUTF(message.getContent());
			}
			// 玩家是发件人
			else if(player.getId()==message.getSendId())
			{
				message.addState(Message.READ);
				data.writeUTF(message.getContent());
			}
			// 玩家是收件人
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
			// 普通邮件
			int[] num=getMsgNum(player);
			data.clear();
			for(int i=0;i<num.length;i++)
			{
				data.writeShort(num[i]);
			}
		}
		// 获取战报或者侦查内容
		else if(type==GET_FIGHT_DATA)
		{
			int messageId=data.readInt();
			Message message=(Message)factory.getMessageCache().load(
				messageId+"");
			if(message==null) throw new DataAccessException(0,"fight_report_not_exists");
			message.addReciveState(Message.READ);
			data.clear();
			// 侦查内容
			if(message.getFightType()==Message.FIGHT_TYPE_ZHE_CHA
				||message.getFightType()==Message.RETURN_BACK)
			{
				if(message.getFightDataFore()==null)
					throw new DataAccessException(0,"data is null");
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
			}
			// 战报内容
			else
			{
				// 先写战报数据之前的内容
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
				ByteBuffer temp=new ByteBuffer();
				// 演播数据是否存在
				boolean isNullFightData=message.getFightData()==null
					||message.getFightData().length()<=0;
				temp.writeBoolean(!isNullFightData);
				// 组装战报版本
				temp.writeInt(message.getFightVersion());
				if(!isNullFightData)
				{
					// 组装演播数据
					temp.write(message.getFightData().getArray(),message
						.getFightData().offset(),message.getFightData().length());
				}
				// 组装军官信息
				temp.write(message.getOfficerData().getArray(),message
					.getOfficerData().offset(),message.getOfficerData()
					.length());
				data.writeData(temp.getArray(),temp.offset(),temp.length());
			}
			// 军官功勋值,兼容个人战报可能显示功勋值的数据结构
			data.writeShort(message.getFeats());
		}
		// 是否有未读邮件
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
		// 共享
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
			// 侦查内容
			if(message.getFightType()==Message.FIGHT_TYPE_ZHE_CHA
				||message.getFightType()==Message.RETURN_BACK)
			{
				if(message.getFightDataFore()==null)
					throw new DataAccessException(0,"data is null");
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
			}
			// 战报内容
			else
			{
				// 先写战报数据之前的内容
				data.write(message.getFightDataFore().getArray(),message
					.getFightDataFore().offset(),message.getFightDataFore()
					.length());
				ByteBuffer temp=new ByteBuffer();
				// 演播数据是否存在
				boolean isNullFightData=message.getFightData()==null
					||message.getFightData().length()<=0;
				temp.writeBoolean(!isNullFightData);
				// 组装战报版本
				temp.writeInt(message.getFightVersion());
				if(!isNullFightData)
				{
					// 组装演播数据
					temp.write(message.getFightData().getArray(),message
						.getFightData().offset(),message.getFightData().length());
				}
				// 组装军官信息
				temp.write(message.getOfficerData().getArray(),message
					.getOfficerData().offset(),message.getOfficerData()
					.length());
				data.writeData(temp.getArray(),temp.offset(),temp.length());
			}
			// 军官功勋值,兼容个人战报可能显示功勋值的数据结构
			data.writeShort(message.getFeats());
		}
		//领取邮件附件
		else if(type==GET_MES_AWARD)
		{
			int messageId=data.readInt();
			if(player.getAnnex().contain(messageId))
			{
				throw new DataAccessException(0,"got annex");// 已领取附件
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
				throw new DataAccessException(0,"not system mail");// 非系统邮件
			}
			if(message.getMessageType()==Message.SYSTEM_ONE_TYPE
				&&message.getReceiveId()!=player.getId())
			{
				throw new DataAccessException(0,"other player mail");// 非自己的邮件
			}
			if(message.getAward()==null)
			{
				throw new DataAccessException(0,"no annex");// 无附件
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
	 * @param factory 要设置的 factory
	 */
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	/** 写数据给前台 */
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

	/** 获取某个类型的总邮件和已读邮件 */
	public int[] getMessageNum(int messageType,Player player,boolean selfSend)
	{
		ArrayList messages=getMessages(messageType,player,selfSend);
		int num[]=new int[2];
		if(messages==null) return num;
		num[0]=messages.size();
		int read=0;
		// 计算已读邮件
		for(int i=0;i<messages.size();i++)
		{
			Message message=(Message)messages.get(i);
			// 收件箱
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
	/** 获取某个类型的邮件 selfSend是否是发件箱 */
	public ArrayList getMessages(int messageType,Player player,
		boolean selfSend)
	{
		// 玩家收件箱
		ArrayList messages=getNotServerMessages(messageType,player,selfSend);
		// 不是获取发件箱 且不是战报
		if(!selfSend&&messageType==Message.SYSTEM_ONE_TYPE)
		{
			// 添加系统邮件
			ArrayList systemList=factory.getMessageCache()
				.getSystemMessageMap();
			for(int i=0;i<systemList.size();i++)
			{
				Message message=(Message)systemList.get(i);
				if(message.getAward()!=null
					&&message.getCreateAt()<player.getCreateTime())
					continue;
				// 看自己有无删除
				if(!player.getIsland().isStateMessage(message,
					Message.ONE_DELETE))
				{
					messages.add(message);
				}
			}
		}
		return messages;
	}
	
	/** 获取非全服邮件 */
	public ArrayList getNotServerMessages(int messageType,Player player,
		boolean selfSend)
	{
		ArrayList messageList=factory.getMessageCache().getMessageListById(
			player.getId());
		// 玩家收件箱
		ArrayList messages=new ArrayList();
		if(messageList!=null)
		{
			for(int i=0;i<messageList.size();i++)
			{
				Message message=(Message)messageList.get(i);			
				// 发件箱
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

	/** 清空某个类型的邮件 selfSend是否是发件箱 */
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
			// 联盟邮件
			if(message.getSendId()==player.getId()&&player.getId()==message.getReceiveId())
			{
				message.addState(Message.ONE_DELETE);
				message.addReciveState(Message.ONE_DELETE);
				continue;
			}
			// 发件箱
			if(selfSend)
			{
				if(message.getSendId()==player.getId())
				{
					// 自己为发件人
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
				// 自己为收件人
				message.addReciveState(Message.ONE_DELETE);
				if(messageType==Message.FIGHT_TYPE)
				{
					message.setDelete(Message.DELETE);
				}
				rmlist.add(message.getMessageId());
			}
			// 加入改变列表
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

	/** 联盟权限判断 */
	public String checkAllianceMaster(Player player)
	{
		// 联盟不存在
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			player.getAttributes(PublicConst.ALLIANCE_ID));
		// 是否是会长或副会长
		if(!alliance.isMaster(player.getId()))
		{
			throw new DataAccessException(0,"you are not master");
		}
		return null;
	}
	
	/** 整理发件箱 */
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
	/** 获取收件箱 */
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
	/** 连接 intlist */
	public void linkList(IntList list0,IntList list1)
	{
		for(int i=0;i<list1.size();i++)
		{
			list0.add(list1.get(i));
		}
	}
	/** 获取现有邮件数量 */
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
