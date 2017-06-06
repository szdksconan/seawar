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
 * message内存管理 每1个小时对修改的数据保存进数据库 并把redis相同id的数据删除 每10分钟 同步数据到redis
 * 服务器启动先从数据库加载所有数据 然后从redis取相同id的数据覆盖 数据库的数据 author:icetiger
 */
public class MessageMemCache extends MemCache
{
	/** 默认改变列表和数据表大小 */
	public static final int ALL_MESSAGES=20000,CHANGE_MESSAG=2000;
	/** 1小时修改过的数据更新到数据库 */
	public static final int MESSAGE_DB_TIME=60*10,MESSAGE_REDIS_TIME=60*5;
	/** 玩家id对应的message集合 */
	HashMap<Integer,MessageList> messageMap=new HashMap<Integer,MessageList>();

	/** 系统邮件 */
	ArrayList systemMessageMap=new ArrayList();
	/**联盟邮件    id 联盟id  ----联盟邮件**/
	HashMap<Integer,MessageList> allianceMessageMap=new HashMap<Integer,MessageList>();

	PlayerMemCache playerMemCache;

	/** 关服存储当前数据 */
	public int saveAndExit()
	{
		collateDB(TimeKit.getSecondTime(),changeListMap,0,
			JedisMemCacheAccess.MESSAGE_REDIS);
		return changeListMap.size();
	}

	/** 将一对多的关系组合起来 */
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
						//联盟战报
						addAllianceMessage(message);
				else
				{
					addMessage(message);
					addSelfMessage(message);
				}
			}
		}
	}
	
	// /** 设置玩家的发件箱 */
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

	// /** 添加玩家的发送邮件 */
	// public void addSendMessage(Message message)
	// {
	// MessageList list=sendMessageMap.get(message.getSendId());
	// // 有了就添加邮件
	// if(list!=null)
	// {
	// list.addMessage(message);
	// return;
	// }
	// // 还没有就个进去
	// list=new MessageList();
	// list.addMessage(message);
	// sendMessageMap.put(message.getSendId(),list);
	// }

	/** 获取某个玩家邮件列表 */
	public ArrayList getMessageListById(int playerId)
	{
		if(messageMap.get(playerId)==null) return null;
		return messageMap.get(playerId).getMessageList();
	}

	/** 添加系统邮件 */
	public void addSystemMessage(Message message)
	{
		systemMessageMap.add(message);
	}

	/** 往一对多map里面添加message */
	public void addMessage(Message message)
	{
		if(message.getDelete()==Message.DELETE) return;
		// 收件方删除了的 也不用加了
		if(message.checkReciveState(Message.ONE_DELETE)) return;
		MessageList list=messageMap.get(message.getReceiveId());
		// 有了就添加邮件
		if(list!=null)
		{
			list.addMessage(message);
			return;
		}
		// 还没有就个进去
		list=new MessageList();
		list.addMessage(message);
		messageMap.put(message.getReceiveId(),list);
	}

	/** 自己发出去的邮件也需要加 */
	public void addSelfMessage(Message message)
	{
		if(message.getDelete()==Message.DELETE) return;
		if(message.getSendId()==0) return;
		// 发件方删除了的 也不用加了
		if(message.checkState(Message.ONE_DELETE)) return;
		message.addState(Message.READ);
		MessageList list=messageMap.get(message.getSendId());
		// 有了就添加邮件
		if(list!=null)
		{
			list.addMessage(message);
			return;
		}
		// 还没有就个进去
		list=new MessageList();
		list.addMessage(message);
		messageMap.put(message.getSendId(),list);
	}

	/**添加联盟战邮件  在联盟邮件中 id 接收者是联盟的id**/
	public void addAllianceMessage(Message message)
	{
		if(message.getDelete()==Message.DELETE)
			return ;
		MessageList list=allianceMessageMap.get(message.getReceiveId());
		// 有了就添加邮件
		if(list!=null)
		{
			list.addMessage(message);
			return;
		}
		list=new MessageList();
		list.addMessage(message);
		allianceMessageMap.put(message.getReceiveId(),list);
	}
	
	// /** 获取邮件里面的附件 */
	// public boolean gotMessageDatas(Player player,Message message)
	// {
	// MessageSave save=new MessageSave();
	// save.setData(message);
	// // 改变列表里面加入
	// changeListMap.put(message.getMessageId(),save);
	// return message.gotMessageDatas(player);
	// }
	
	/**不存储*/
	public synchronized Message createObectOnly()
	{
		Message message=new Message();
		message.setMessageId(uidkit.getPlusUid());
		return message;
	}
	
	/**
	 * 创建完对象返回进行加工后 要记得同步到一对多的map里面去 保持数据同步
	 */
	public synchronized Message createObect()
	{
		Message message=new Message();
		message.setMessageId(uidkit.getPlusUid());
		MessageSave save=new MessageSave();
		save.setData(message);
		save.setSaveTimeRedis(TimeKit.getSecondTime());
		save.setSaveTimeDB(TimeKit.getSecondTime());
		// 内存中加入
		cacheMap.put(message.getMessageId(),save);
		// 改变列表里面加入
		changeListMap.put(message.getMessageId(),save);
		return message;
	}

	public void init()
	{
		cacheMap=new IntKeyHashMap(ALL_MESSAGES);
		changeListMap=new IntKeyHashMap(CHANGE_MESSAG);
		int time=TimeKit.getSecondTime();
		int checkTimeDelete=time-60*60*24*7;
		/** 删除超过10天的邮件 战报 */
		String deleteSql="DELETE FROM messages WHERE create_at<"
			+checkTimeDelete+" AND message_type="+Message.FIGHT_TYPE;
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		checkTimeDelete=time-60*60*24*30;
		/**删除超过一个月的玩家邮件*/
		deleteSql="DELETE FROM messages WHERE create_at<"
			+checkTimeDelete;
		SqlKit.execute(((SqlPersistence)dbaccess.getGamePersistence())
			.getConnectionManager(),deleteSql);
		/** 加载所有邮件 */
		String sql="SELECT * FROM messages";
		// 数据库加载所有邮件数据
		Message message[]=(Message[])dbaccess.loadBySql(sql);
		if(message!=null)
		{
			for(int i=0,n=message.length;i<n;i++)
			{
				MessageSave messageSave=new MessageSave();
				messageSave.setData(message[i]);
				// 设置保存的时间
				messageSave.setSaveTimeDB(time);
				cacheMap.put(message[i].getMessageId(),messageSave);
			}
		}
		// redis获取最新玩家数据进行覆盖
//		Message messageRedis[]=jedisCache.loadAllMessages();
//		if(messageRedis!=null)
//		{
//			for(int i=0,n=messageRedis.length;i<n;i++)
//			{
//				MessageSave save=(MessageSave)cacheMap.get(messageRedis[i]
//					.getMessageId());
//				// 数据库还有
//				if(save!=null)
//				{
//					save.setData(messageRedis[i]);
//					// 覆盖的数据加入改变列表 下次一起存储
//					changeListMap.put(messageRedis[i].getMessageId(),save);
//				}
//				// 数据库没有 message超过时间会被删除 只有player和order不用清除
//				else
//				{
//					// 清理message的redis
//					jedisCache.delKey(JedisMemCacheAccess.MESSAGE_REDIS
//						+messageRedis[i].getMessageId());
//				}
//			}
//		}
		// 设置一对多关系
		setMessageMap();
		// 启动定时器
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
		// TODO 自动生成方法存根
		return null;
	}

	public synchronized void save(String key,Object data)
	{
		// TODO 自动生成方法存根
		if(data==null) return;
		int time=TimeKit.getSecondTime();
		MessageSave save=new MessageSave();
		save.setData((Message)data);
		save.setSaveTimeDB(time);
		save.setSaveTimeRedis(time);
		// 内存中加入
		cacheMap.put(Integer.parseInt(key),save);
		// 改变列表里面加入
		changeListMap.put(Integer.parseInt(key),save);
	}

	public void onTimer(TimerEvent e)
	{
		// TODO 自动生成方法存根
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

	/** 内部类 某个玩家的邮件列表 */
	private class MessageList
	{

		ArrayList messageList=new ArrayList();

		/** 添加邮件 */
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
		 * @param messageList 要设置的 messageList
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
	 * @param playerMemCache 要设置的 playerMemCache
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
	 * @param systemMessageMap 要设置的 systemMessageMap
	 */
	public void setSystemMessageMap(ArrayList systemMessageMap)
	{
		this.systemMessageMap=systemMessageMap;
	}

	@Override
	public void deleteCache(Object message)
	{
		// TODO 自动生成方法存根
		if(message==null) return;
		Message ev=(Message)message;
		cacheMap.remove(ev.getMessageId());
		if(ev.getMessageType()==Message.SYSTEM_TYPE)
			systemMessageMap.remove(ev);
	}

	/**获取联盟战的战报内容**/
	public ArrayList getAllianceFightMessage(int allianceId)
	{
		if(allianceMessageMap.get(allianceId)==null) return null;
		return allianceMessageMap.get(allianceId).getMessageList();
	}
	
	
	public HashMap<Integer,MessageList> getAllianceMessageMap()
	{
		return allianceMessageMap;
	}

	/**清除联盟邮件**/
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
					// 加入改变列表
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
	// * @param sendMessageMap 要设置的 sendMessageMap
	// */
	// public void setSendMessageMap(HashMap<Integer,MessageList>
	// sendMessageMap)
	// {
	// this.sendMessageMap=sendMessageMap;
	// }
}
