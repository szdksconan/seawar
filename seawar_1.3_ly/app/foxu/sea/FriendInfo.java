package foxu.sea;


import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.util.TimeKit;

/**
 * 好友信息
 * @author liuh
 */
public class FriendInfo
{
	
	int eventSize = 0;//用于事件长度计数 因为现在不入库  直接写在对象里面
	
	/**申请好友1  添加好友2 删除好友3*/
	public static int EVENT_APPLY_FRIEND = 1, EVENT_ADD_FRIEND = 2, EVENT_REMOVE_FRIEND = 3,EVENT_HELP_FRIEND = 4;
	public static int MAX_RECEIVED_NUM = 20,RECEIVED_INTIMACY = 1,MAX_GIVE_NUM = 20;
	/** 亲密度抽奖 */
	public static int INTIMACY_LUCKY_AWARD_ID = 51005;
	/** 抽奖所需亲密度 */
	public static int INTIMACY_LUCKY_NEED_NUM = 10;
	/** 好友*/
	private IntKeyHashMap friends = new IntKeyHashMap();
	/** 黑名单 */
	private IntKeyHashMap blackList = new IntKeyHashMap();
	/** 好友事件 */
	private IntKeyHashMap eventMap = new IntKeyHashMap();
	/** 亲密度*/
	int intimacy;
	/** 最后一次给予时间*/
	int lastGiveTime;
	/** 最后一次给予时的次数 和给予时间一起作为条件  如果最后一次给予时间不在同一天则清0 */
	int lastGiveTimes;
	/** 最后一次领取时间 */
	int lastReceivedTime;
	/** 最后一次领取的次数 和领取时间一起作为条件  如果最后一次领取时间不在同一天则清0 */
	int lastReceivedTimes;
	
	
	/**
	 * 申请好友 添加申请好友事件
	 */
	public void applyFriend(Player my,Player friend,CreatObjectFactory obf){
		
		synchronized(friend.getFriendInfo().getEventMap())
		{
			//先判断是否已经有相同事件  有则不添加
			IntKeyHashMap events =  friend.getFriendInfo().getEventMap();
			int[] keys  = events.keyArray();
			for(int i=0;i<keys.length;i++){
				FriendEvent e = (FriendEvent)events.get(keys[i]);
				if(e.getSendPlayerId()==my.getId()&&e.getEventType()==EVENT_APPLY_FRIEND)
					return;
			}
			//添加申请事件
			addEvent(my,friend,EVENT_APPLY_FRIEND,obf);
		}
	}
	
	/**
	 * 确认好友添加 双向添加
	 */
	public void addFriend(Player my,Player friend,int eventId,boolean agree,CreatObjectFactory obf){
		if(agree){
			my.getFriendInfo().getFriends().put(friend.getId(),new FriendIntimacy(friend,0,0,0));
			friend.getFriendInfo().getFriends().put(my.getId(),new FriendIntimacy(my,0,0,0));
			addEvent(my,friend,EVENT_ADD_FRIEND,obf);
			addEvent(friend,my,EVENT_ADD_FRIEND,obf);
			JBackKit.sendPlayerFriendInfo(friend,obf);//刷新好友信息
			JBackKit.sendPlayerFriendInfo(my,obf);//刷新好友信息
		}
		removeEvent(my,eventId,obf);
	}
	
	/**
	 * 删除好友
	 */
	public void reMoveFriend(Player my,Player friend,CreatObjectFactory obf){
		IntKeyHashMap Map = my.getFriendInfo().getFriends();
		Map.remove(friend.getId());
		addEvent(my,friend,EVENT_REMOVE_FRIEND,obf);
		
		IntKeyHashMap friendMap = friend.getFriendInfo().getFriends();
		friendMap.remove(my.getId());
		
		JBackKit.sendPlayerFriendInfo(friend,obf);//刷新好友信息
		JBackKit.sendPlayerFriendInfo(my,obf);//刷新好友信息
	}
	
	/**
	 * 用于前台刷新的序列化
	 */
	public void goodFriendWrite(ByteBuffer data,CreatObjectFactory obf){
		//计算可赠送、领取时间
		int now =  TimeKit.getSecondTime();
		if(lastGiveTimes!=0&&lastGiveTime!=0&&!SeaBackKit.isSameDay(now,lastGiveTime)){
			lastGiveTimes = 0;
		}
		if(lastReceivedTimes!=0&&lastReceivedTime!=0&&!SeaBackKit.isSameDay(now,lastReceivedTime)){
			lastReceivedTimes = 0;
		}
		data.writeInt(intimacy);//亲密度
		
		data.writeByte(lastGiveTimes);//赠送次数
		data.writeByte(MAX_GIVE_NUM);//获取次数上限
		
		data.writeByte(lastReceivedTimes);//领取次数
		data.writeByte(MAX_RECEIVED_NUM);//领取次数上限
		
		//好友列表
		int[] keys = friends.keyArray();
		data.writeByte(keys.length);
		for(int i=0;i<keys.length;i++){
			int playerId = keys[i];
			FriendIntimacy fi = (FriendIntimacy)friends.get(playerId);
			if(fi.getPlayer()==null)
				fi.setPlayer(obf.getPlayerById(playerId));
			data.writeInt(fi.getPlayer().getId());//好友ID
			data.writeUTF(fi.getPlayer().getName());//好友名字
			data.writeInt(fi.getPlayer().getAttrHead());//好友头像
			data.writeInt(fi.getPlayer().getAttrHeadBorder());
			data.writeBoolean(fi.getLastGiveTime()==0?true:!SeaBackKit.isSameDay(now,fi.getLastGiveTime()));//能否赠送
			data.writeBoolean(fi.getLastfriendGiveTime()!=0&&
							SeaBackKit.isSameDay(now,fi.getLastfriendGiveTime())&&
							!SeaBackKit.isSameDay(now,fi.getLastReceivedTime()));//能否领取  条件：有赠送时间 且是今天   领取时间不是今天
			
		}
	}
	
	
	/**
	 * 用于前台刷新的序列化
	 */
	public void eventWrite(ByteBuffer data,CreatObjectFactory obf){
		int[] eventKeys = eventMap.keyArray();
		int size = eventKeys.length;
		data.writeShort(size);
		for(int i=0;i<size;i++){
			FriendEvent fe = (FriendEvent)eventMap.get(eventKeys[i]);
			Player pInfo = obf.getPlayerById(fe.getSendPlayerId());
			data.writeInt(fe.getEventId());//事件ID
			data.writeByte(fe.getEventType());//事件类型
			data.writeUTF(pInfo.getName());//发动事件人的名字
			data.writeInt(pInfo.getAttrHead());//好友头像
			data.writeInt(pInfo.getAttrHeadBorder());//边框
		}
	}
	
	
	/**
	 * 添加事件
	 * @param send 发起人
	 * @param to 目标人
	 * @param type 事件类型
	 */
	public void addEvent(Player send,Player to,int type,CreatObjectFactory obf){
		int eventId = getNewEventId();
		to.getFriendInfo().getEventMap().put(eventId,new FriendEvent(eventId,type,TimeKit.getSecondTime(),send.getId()));
		//推送
		JBackKit.sendPlayerFriendEvents(to,obf);
	}
	
	public void removeEvent(Player my,int eventId,CreatObjectFactory obf){
		eventMap.remove(eventId);
		JBackKit.sendPlayerFriendEvents(my,obf);
	}
	
	/**
	 * 登陆给客户端好友信息
	 */
	public void infoWrite(ByteBuffer data,CreatObjectFactory obf){
		
		//计算可赠送、领取时间
		int now =  TimeKit.getSecondTime();
		if(lastGiveTimes!=0&&lastGiveTime!=0&&!SeaBackKit.isSameDay(now,lastGiveTime)){
			lastGiveTimes = 0;
		}
		if(lastReceivedTimes!=0&&lastReceivedTime!=0&&!SeaBackKit.isSameDay(now,lastReceivedTime)){
			lastReceivedTimes = 0;
		}
		data.writeInt(intimacy);//亲密度
		
		data.writeByte(lastGiveTimes);//赠送次数
		data.writeByte(MAX_GIVE_NUM);//获取次数上限
		
		data.writeByte(lastReceivedTimes);//领取次数
		data.writeByte(MAX_RECEIVED_NUM);//领取次数上限
		
		//好友列表
		int[] keys = friends.keyArray();
		data.writeByte(keys.length);
		for(int i=0;i<keys.length;i++){
			int playerId = keys[i];
			FriendIntimacy fi = (FriendIntimacy)friends.get(playerId);
			if(fi.getPlayer()==null)
				fi.setPlayer(obf.getPlayerById(playerId));
			data.writeInt(fi.getPlayer().getId());//好友ID
			data.writeUTF(fi.getPlayer().getName());//好友名字
			data.writeInt(fi.getPlayer().getAttrHead());//好友头像
			data.writeInt(fi.getPlayer().getAttrHeadBorder());
			data.writeBoolean(fi.getLastGiveTime()==0?true:!SeaBackKit.isSameDay(now,fi.getLastGiveTime()));//能否赠送
			data.writeBoolean(fi.getLastfriendGiveTime()!=0&&
							SeaBackKit.isSameDay(now,fi.getLastfriendGiveTime())&&
							!SeaBackKit.isSameDay(now,fi.getLastReceivedTime()));//能否领取  条件：有赠送时间 且是今天   领取时间不是今天

		}
		
		//黑名单 
		int backKeys[] = blackList.keyArray();
		data.writeByte(backKeys.length);
		for(int i=0;i<backKeys.length;i++){
			int playerId = backKeys[i];
			Player p = (Player)blackList.get(playerId);
			if(p==null){
				p = obf.getPlayerCache().loadPlayerOnly(playerId+"");
				blackList.put(playerId,p);
			}
			data.writeUTF(p.getName());
			data.writeInt(p.getAttrHead());
			data.writeInt(p.getAttrHeadBorder());
		
		}
		
		int[] eventKeys = eventMap.keyArray();
		int size = eventKeys.length;
		data.writeShort(size);
		for(int i=0;i<size;i++){
			FriendEvent fe = (FriendEvent)eventMap.get(eventKeys[i]);
			Player pInfo = obf.getPlayerById(fe.getSendPlayerId());
			data.writeInt(fe.getEventId());//事件ID
			data.writeByte(fe.getEventType());//事件类型
			data.writeUTF(pInfo.getName());//发动事件人的名字
			data.writeInt(pInfo.getAttrHead());//好友头像
			data.writeInt(pInfo.getAttrHeadBorder());//边框
		}
		
	}
	
	/**
	 * 领取亲密度
	 */
	public String receivedIntimacy(Player player,int friendId,CreatObjectFactory cof)
	{
		int now=TimeKit.getSecondTime();
		if(lastReceivedTimes!=0&&lastReceivedTime!=0&&!SeaBackKit.isSameDay(now,lastReceivedTime)){
			lastReceivedTimes = 0;
		}
		if(lastReceivedTimes>=MAX_RECEIVED_NUM) return "you cant received today";
		IntList list= new IntList();
		if(friendId!=0)
		{
			FriendIntimacy fi=(FriendIntimacy)friends.get(friendId);
			if(fi==null) return "friend is null";
			if(fi.getLastfriendGiveTime()!=0
				&&SeaBackKit.isSameDay(now,fi.getLastfriendGiveTime())
				&&!SeaBackKit.isSameDay(now,fi.getLastReceivedTime()))
			{
				fi.setLastReceivedTime(now);
				intimacy+=RECEIVED_INTIMACY;//增加亲密度
				lastReceivedTimes++;//增加领取次数
				list.add(friendId);
			}
			else
			{
				return "you cant received";
			}
		}
		else
		{
			int[] keys = friends.keyArray();
			for(int i =0;i<keys.length;i++)
			{
				if(lastReceivedTimes>=MAX_RECEIVED_NUM)break;//超过则跳出
				FriendIntimacy fi=(FriendIntimacy)friends.get(keys[i]);
				if(fi.getLastfriendGiveTime()!=0
								&&SeaBackKit.isSameDay(now,fi.getLastfriendGiveTime())
								&&!SeaBackKit.isSameDay(now,fi.getLastReceivedTime()))
							{
								fi.setLastReceivedTime(now);
								intimacy+=RECEIVED_INTIMACY;//增加亲密度
								lastReceivedTimes++;
								list.add(keys[i]);
							}
			}
		}
		if(list.size()>0){
			lastReceivedTime = now;
			JBackKit.sendPlayerFriendInfo(player,cof);
		}
		return null;
	}
	
	/**
	 * 赠送亲密度
	 * @param my
	 * @param FriendId
	 * @return
	 */
	public String giveIntimacy(Player my,Player friend,CreatObjectFactory cof){
		int now =  TimeKit.getSecondTime();
		if(lastGiveTimes!=0&&lastGiveTime!=0&&!SeaBackKit.isSameDay(now,lastGiveTime)){
			lastGiveTimes = 0;
		}
		if(lastGiveTimes>=MAX_GIVE_NUM) return "you cant give today";
		FriendIntimacy fi=(FriendIntimacy)friends.get(friend.getId());
		if(fi==null) return "friend is null";
		if(fi.getLastGiveTime()==0?true:!SeaBackKit.isSameDay(now,fi.getLastGiveTime()))
		{
			fi.setLastGiveTime(now);
			FriendIntimacy fi_1 = (FriendIntimacy)friend.getFriendInfo().getFriends().get(my.getId());//更新好友的信息
			if(fi_1!=null){
				fi_1.setLastfriendGiveTime(now);
			}
			
			lastGiveTimes++;//增加赠送次数
			intimacy+=RECEIVED_INTIMACY;//自己也增加
			lastGiveTime = now;
			JBackKit.sendPlayerFriendInfo(my,cof);
			JBackKit.sendPlayerFriendInfo(friend,cof);
		}
		else
		{
			return "you have given";
		}
		
		return null;
	}
	
	
	
	/**
	 * 序列化信息  准备持久化数据
	 * @param data
	 */
	public synchronized void bytesWrite(ByteBuffer data)
	{
		data.writeInt(intimacy);//亲密度
		data.writeInt(lastGiveTime);// 最后一次给予时间
		data.writeInt(lastGiveTimes);// 最后一次给予时的次数 
		data.writeInt(lastReceivedTime);//最后一次领取时间
		data.writeInt(lastReceivedTimes);//最后一次领取的次数 和领取时间一起作为条件
		//好友信息
		data.writeInt(friends.keyArray().length);
		for(int i=0;i<friends.keyArray().length;i++){
			data.writeInt(friends.keyArray()[i]);
			FriendIntimacy fi = (FriendIntimacy)friends.get(friends.keyArray()[i]);
			data.writeInt(fi.getLastGiveTime());//赠送给好友的时间 
			data.writeInt(fi.getLastfriendGiveTime());//好友赠送给我的亲密度的时间
			data.writeInt(fi.getLastReceivedTime());//领取还有亲密度时间
		}
		//黑名单
		data.writeInt(blackList.keyArray().length);
		for(int i=0;i<blackList.keyArray().length;i++){
			data.writeInt(blackList.keyArray()[i]);
		}
		
	}
	
	/**
	 * 反序列化好友
	 * @param data
	 */
	public synchronized void bytesRead(ByteBuffer data){
		this.intimacy = data.readInt();//亲密度
		this.lastGiveTime = data.readInt();// 最后一次给予时间
		this.lastGiveTimes = data.readInt();// 最后一次给予时的次数 
		this.lastReceivedTime = data.readInt();//最后一次领取时间
		this.lastReceivedTimes = data.readInt();//最后一次领取的次数 和领取时间一起作为条件
		int len = data.readInt();
		for(int i=0;i<len;i++){
			int playerId = data.readInt();
			friends.put(playerId,new FriendIntimacy(data.readInt(),data.readInt(),data.readInt()));
		}
		int blackLen = data.readInt();
		for(int i=0;i<blackLen;i++){
			blackList.put(data.readInt(),null);
		}
		
	}
	
	
	
	
	/**
	 * 扣除亲密度
	 * @param num
	 */
	public void reduceIntimacy(int num){ //领取和减去 分开 不加锁
		intimacy-=num;
	}
	
	

	
	public IntKeyHashMap getEventMap()
	{
		return eventMap;
	}

	
	public void setEventMap(IntKeyHashMap eventMap)
	{
		this.eventMap=eventMap;
	}

	public IntKeyHashMap getFriends()
	{
		return friends;
	}

	
	public void setFriends(IntKeyHashMap friends)
	{
		this.friends=friends;
	}

	
	public IntKeyHashMap getBlackList()
	{
		return blackList;
	}

	
	public void setBlackList(IntKeyHashMap blackList)
	{
		this.blackList=blackList;
	}

	
	public int getIntimacy()
	{
		return intimacy;
	}

	
	public void setIntimacy(int intimacy)
	{
		this.intimacy=intimacy;
	}

	
	public int getLastGiveTime()
	{
		return lastGiveTime;
	}

	
	public void setLastGiveTime(int lastGiveTime)
	{
		this.lastGiveTime=lastGiveTime;
	}

	
	public int getLastGiveTimes()
	{
		return lastGiveTimes;
	}

	
	public void setLastGiveTimes(int lastGiveTimes)
	{
		this.lastGiveTimes=lastGiveTimes;
	}

	
	public int getLastReceivedTime()
	{
		return lastReceivedTime;
	}

	
	public void setLastReceivedTime(int lastReceivedTime)
	{
		this.lastReceivedTime=lastReceivedTime;
	}

	
	public int getLastReceivedTimes()
	{
		return lastReceivedTimes;
	}

	
	public void setLastReceivedTimes(int lastReceivedTimes)
	{
		this.lastReceivedTimes=lastReceivedTimes;
	}
	
	/**
	 * 返回一个事件ID
	 * @return
	 */
	public synchronized int getNewEventId(){
		return ++eventSize;
	}



}
