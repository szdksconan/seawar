package foxu.dcaccess.mem;

import java.util.HashMap;
import java.util.Map;
import jedis.JedisMemCacheAccess;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.MessageSave;
import foxu.sea.messgae.Message;

/**
 * message�ڴ���� ÿ1��Сʱ���޸ĵ����ݱ�������ݿ� ����redis��ͬid������ɾ�� ÿ10���� ͬ�����ݵ�redis
 * �����������ȴ����ݿ������������ Ȼ���redisȡ��ͬid�����ݸ��� ���ݿ������ author:icetiger
 */
public class MessageMemCache extends MemCache
{
	/** Ĭ�ϸı��б�����ݱ��С */
	public static final int ALL_MESSAGES=20000,CHANGE_MESSAG=2000;
	/** 1Сʱ�޸Ĺ������ݸ��µ����ݿ� */
	public static final int MESSAGE_DB_TIME=60*10,MESSAGE_REDIS_TIME=60*5;
	/** ���id��Ӧ��message���� */
	HashMap<Integer,MessageList> messageMap=new HashMap<Integer,MessageList>();

	/** ϵͳ�ʼ� */
	ArrayList systemMessageMap=new ArrayList();
	/**�����ʼ�    id ����id  ----�����ʼ�**/
	HashMap<Integer,MessageList> allianceMessageMap=new HashMap<Integer,MessageList>();

	PlayerMemCache playerMemCache;

	/** �ط��洢��ǰ���� */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.MESSAGE_REDIS);
		return changeListMap.size();
	}

	/** ��һ�Զ�Ĺ�ϵ������� */
	public void setMessageMap()
	{
		Object[] object=cacheMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			MessageSave save=(MessageSave)object[i];
			Message message=save.getData();
			if(message.getMessageType()==Message.SYSTEM_TYPE)
			{
				addSystemMessage(message);
			}
			else 
			{
				if(message.getMessageType()==Message.ALLIANCE_FIGHT_TYPE)
						//����ս��
						addAllianceMessage(message);
				else
				{
					addMessage(message);
					addSelfMessage(message);
				}
			}
		}
	}
	
	// /** ������ҵķ����� */
	// public void setSendMessageMap()
	// {
	// Object[] object=cacheMap.valueArray();
	// for(int i=0;i<object.length;i++)
	// {
	// MessageSave save=(MessageSave)object[i];
	// Message message=save.getData();
	// if(message.getMessageType()!=Message.COPY_FOR_SELF) continue;
	// if(message.getDelete()==Message.DELETE) continue;
	// addSendMessage(message);
	// }
	// }

	// /** �����ҵķ����ʼ� */
	// public void addSendMessage(Message message)
	// {
	// MessageList list=sendMessageMap.get(message.getSendId());
	// // ���˾�����ʼ�
	// if(list!=null)
	// {
	// list.addMessage(message);
	// return;
	// }
	// // ��û�о͸���ȥ
	// list=new MessageList();
	// list.addMessage(message);
	// sendMessageMap.put(message.getSendId(),list);
	// }

	/** ��ȡĳ������ʼ��б� */
	public ArrayList getMessageListById(int playerId)
	{
		if(messageMap.get(playerId)==null) return null;
		return messageMap.get(playerId).getMessageList();
	}

	/** ���ϵͳ�ʼ� */
	public void addSystemMessage(Message message)
	{
		systemMessageMap.add(message);
	}

	/** ��һ�Զ�map�������message */
	public void addMessage(Message message)
	{
		if(message.getDelete()==Message.DELETE) return;
		// �ռ���ɾ���˵� Ҳ���ü���
		if(message.checkReciveState(Message.ONE_DELETE)) return;
		MessageList list=messageMap.get(message.getReceiveId());
		// ���˾�����ʼ�
		if(list!=null)
		{
			list.addMessage(message);
			return;
		}
		// ��û�о͸���ȥ
		list=new MessageList();
		list.addMessage(message);
		messageMap.put(message.getReceiveId(),list);
	}

	/** �Լ�����ȥ���ʼ�Ҳ��Ҫ�� */
	public void addSelfMessage(Message message)
	{
		if(message.getDelete()==Message.DELETE) return;
		if(message.getSendId()==0) return;
		// ������ɾ���˵� Ҳ���ü���
		if(message.checkState(Message.ONE_DELETE)) return;
		message.addState(Message.READ);
		MessageList list=messageMap.get(message.getSendId());
		// ���˾�����ʼ�
		if(list!=null)
		{
			list.addMessage(message);
			return;
		}
		// ��û�о͸���ȥ
		list=new MessageList();
		list.addMessage(message);
		messageMap.put(message.getSendId(),list);
	}

	/**�������ս�ʼ�  �������ʼ��� id �����������˵�id**/
	public void addAllianceMessage(Message message)
	{
		if(message.getDelete()==Message.DELETE)
			return ;
		MessageList list=allianceMessageMap.get(message.getReceiveId());
		// ���˾�����ʼ�
		if(list!=null)
		{
			list.addMessage(message);
			return;
		}
		list=new MessageList();
		list.addMessage(message);
		allianceMessageMap.put(message.getReceiveId(),list);
	}
	
	// /** ��ȡ�ʼ�����ĸ��� */
	// public boolean gotMessageDatas(Player player,Message message)
	// {
	// MessageSave save=new MessageSave();
	// save.setData(message);
	// // �ı��б��������
	// changeListMap.put(message.getMessageId(),save);
	// return message.gotMessageDatas(player);
	// }
	
	/**���洢*/
	public synchronized Message createObectOnly()
	{
		Message message=new Message();
		message.setMessageId(uidkit.getPlusUid());
		return message;
	}
	
	/**
	 * ��������󷵻ؽ��мӹ��� Ҫ�ǵ�ͬ����һ�Զ��map����ȥ ��������ͬ��
	 */
	public synchronized Message createObect()
	{
		Message message=new Message();
		message.setMessageId(uidkit.getPlusUid());
		MessageSave save=new MessageSave();
		save.setData(message);
		save.setSaveTimeRedis(TimeKit.getSecondTime());
		save.setSaveTimeDB(TimeKit.getSecondTime());
		// �ڴ��м���
		cacheMap.put(message.getMessageId(),save);
		// �ı��б��������
		changeListMap.put(message.getMessageId(),save);
		return message;
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_MESSAGES);
		changeListMap=new IntKeyHashMap(CHANGE_MESSAG);
		int time=TimeKit.getSecondTime();
		int checkTimeDelete=time-60*60*24*7;
		/** ɾ������10����ʼ� ս�� */
		String deleteSql="DELETE FROM messages WHERE create_at<"
			+checkTimeDelete+" AND message_type="+Message.FIGHT_TYPE;
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		checkTimeDelete=time-60*60*24*30;
		/**ɾ������һ���µ�����ʼ�*/
		deleteSql="DELETE FROM messages WHERE create_at<"
			+checkTimeDelete;
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		/** ���������ʼ� */
		String sql="SELECT * FROM messages";
		// ���ݿ���������ʼ�����
		Message message[]=(Message[])dbaccess.loadBySql(sql);
		if(message!=null)
		{
			for(int i=0,n=message.length;i<n;i++)
			{
				MessageSave messageSave=new MessageSave();
				messageSave.setData(message[i]);
				// ���ñ����ʱ��
				messageSave.setSaveTimeDB(time);
				cacheMap.put(message[i].getMessageId(),messageSave);
			}
		}
		// redis��ȡ����������ݽ��и���
//		Message messageRedis[]=jedisCache.loadAllMessages();
//		if(messageRedis!=null)
//		{
//			for(int i=0,n=messageRedis.length;i<n;i++)
//			{
//				MessageSave save=(MessageSave)cacheMap.get(messageRedis[i]
//					.getMessageId());
//				// ���ݿ⻹��
//				if(save!=null)
//				{
//					save.setData(messageRedis[i]);
//					// ���ǵ����ݼ���ı��б� �´�һ��洢
//					changeListMap.put(messageRedis[i].getMessageId(),save);
//				}
//				// ���ݿ�û�� message����ʱ��ᱻɾ�� ֻ��player��order�������
//				else
//				{
//					// ����message��redis
//					jedisCache.delKey(JedisMemCacheAccess.MESSAGE_REDIS
//						+messageRedis[i].getMessageId());
//				}
//			}
//		}
		// ����һ�Զ��ϵ
		setMessageMap();
		// ������ʱ��
//		TimerCenter.getMinuteTimer().add(eventRedis);
		TimerCenter.getMinuteTimer().add(eventDB);
	}

	public synchronized Object load(String key)
	{
		MessageSave data=(MessageSave)cacheMap.get(Integer.parseInt(key));
		if(data!=null)
		{
			if(changeListMap.get(Integer.parseInt(key))==null)
			{
				changeListMap.put(Integer.parseInt(key),data);
			}
			return data.getData();
		}
		return null;
	}

	public Object[] loads(String[] keys)
	{
		// TODO �Զ����ɷ������
		return null;
	}

	public synchronized void save(String key,Object data)
	{
		// TODO �Զ����ɷ������
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		MessageSave save=new MessageSave();
		save.setData((Message)data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// �ڴ��м���
		cacheMap.put(Integer.parseInt(key),save);
		// �ı��б��������
		changeListMap.put(Integer.parseInt(key),save);
	}

	public void onTimer(TimerEvent e)
	{
		// TODO �Զ����ɷ������
		if(e.getParameter().equals("db"))
		{
			collateDB((int)(e.getCurrentTime()/1000),changeListMap,
				MESSAGE_DB_TIME,JedisMemCacheAccess.MESSAGE_REDIS);
		}
		else if(e.getParameter().equals("redis"))
		{
//			collateRedis((int)(e.getCurrentTime()/1000),changeListMap,
//				MESSAGE_REDIS_TIME,JedisMemCacheAccess.MESSAGE_REDIS);
		}
	}

	/** �ڲ��� ĳ����ҵ��ʼ��б� */
	private class MessageList
	{

		ArrayList messageList=new ArrayList();

		/** ����ʼ� */
		public void addMessage(Message message)
		{
			messageList.add(message);
		}

		/**
		 * @return messageList
		 */
		public ArrayList getMessageList()
		{
			return messageList;
		}

		/**
		 * @param messageList Ҫ���õ� messageList
		 */
		public void setMessageList(ArrayList messageList)
		{
			this.messageList=messageList;
		}
	}

	/**
	 * @return playerMemCache
	 */
	public PlayerMemCache getPlayerMemCache()
	{
		return playerMemCache;
	}

	/**
	 * @param playerMemCache Ҫ���õ� playerMemCache
	 */
	public void setPlayerMemCache(PlayerMemCache playerMemCache)
	{
		this.playerMemCache=playerMemCache;
	}

	/**
	 * @return systemMessageMap
	 */
	public ArrayList getSystemMessageMap()
	{
		return systemMessageMap;
	}

	/**
	 * @param systemMessageMap Ҫ���õ� systemMessageMap
	 */
	public void setSystemMessageMap(ArrayList systemMessageMap)
	{
		this.systemMessageMap=systemMessageMap;
	}

	@Override
	public void deleteCache(Object message)
	{
		// TODO �Զ����ɷ������
		if(message==null) return;
		Message ev=(Message)message;
		cacheMap.remove(ev.getMessageId());
		if(ev.getMessageType()==Message.SYSTEM_TYPE)
			systemMessageMap.remove(ev);
	}

	/**��ȡ����ս��ս������**/
	public ArrayList getAllianceFightMessage(int allianceId)
	{
		if(allianceMessageMap.get(allianceId)==null) return null;
		return allianceMessageMap.get(allianceId).getMessageList();
	}
	
	
	public HashMap<Integer,MessageList> getAllianceMessageMap()
	{
		return allianceMessageMap;
	}

	/**��������ʼ�**/
	public void clearAllianceMessage()
	{
		if(allianceMessageMap.size()==0) return;
		for(Map.Entry<Integer,MessageList> entry:allianceMessageMap
			.entrySet())
		{
			MessageList messageList=entry.getValue();
			if(messageList!=null&&messageList.messageList.size()!=0)
			{
				ArrayList list=messageList.getMessageList();
				for(int i=0;i<list.size();i++)
				{
					Message message=(Message)list.get(i);
					if(message==null) continue;
					message.setDelete(Message.DELETE);
					// ����ı��б�
					load(message.getMessageId()+"");
				}
			}
		}
		allianceMessageMap.clear();
	}
	// /**
	// * @return sendMessageMap
	// */
	// public HashMap<Integer,MessageList> getSendMessageMap()
	// {
	// return sendMessageMap;
	// }
	//
	// /**
	// * @param sendMessageMap Ҫ���õ� sendMessageMap
	// */
	// public void setSendMessageMap(HashMap<Integer,MessageList>
	// sendMessageMap)
	// {
	// this.sendMessageMap=sendMessageMap;
	// }
}
