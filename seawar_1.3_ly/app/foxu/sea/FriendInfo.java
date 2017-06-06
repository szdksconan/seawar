package foxu.sea;


import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.util.TimeKit;

/**
 * ������Ϣ
 * @author liuh
 */
public class FriendInfo
{
	
	int eventSize = 0;//�����¼����ȼ��� ��Ϊ���ڲ����  ֱ��д�ڶ�������
	
	/**�������1  ��Ӻ���2 ɾ������3*/
	public static int EVENT_APPLY_FRIEND = 1, EVENT_ADD_FRIEND = 2, EVENT_REMOVE_FRIEND = 3,EVENT_HELP_FRIEND = 4;
	public static int MAX_RECEIVED_NUM = 20,RECEIVED_INTIMACY = 1,MAX_GIVE_NUM = 20;
	/** ���ܶȳ齱 */
	public static int INTIMACY_LUCKY_AWARD_ID = 51005;
	/** �齱�������ܶ� */
	public static int INTIMACY_LUCKY_NEED_NUM = 10;
	/** ����*/
	private IntKeyHashMap friends = new IntKeyHashMap();
	/** ������ */
	private IntKeyHashMap blackList = new IntKeyHashMap();
	/** �����¼� */
	private IntKeyHashMap eventMap = new IntKeyHashMap();
	/** ���ܶ�*/
	int intimacy;
	/** ���һ�θ���ʱ��*/
	int lastGiveTime;
	/** ���һ�θ���ʱ�Ĵ��� �͸���ʱ��һ����Ϊ����  ������һ�θ���ʱ�䲻��ͬһ������0 */
	int lastGiveTimes;
	/** ���һ����ȡʱ�� */
	int lastReceivedTime;
	/** ���һ����ȡ�Ĵ��� ����ȡʱ��һ����Ϊ����  ������һ����ȡʱ�䲻��ͬһ������0 */
	int lastReceivedTimes;
	
	
	/**
	 * ������� �����������¼�
	 */
	public void applyFriend(Player my,Player friend,CreatObjectFactory obf){
		
		synchronized(friend.getFriendInfo().getEventMap())
		{
			//���ж��Ƿ��Ѿ�����ͬ�¼�  �������
			IntKeyHashMap events =  friend.getFriendInfo().getEventMap();
			int[] keys  = events.keyArray();
			for(int i=0;i<keys.length;i++){
				FriendEvent e = (FriendEvent)events.get(keys[i]);
				if(e.getSendPlayerId()==my.getId()&&e.getEventType()==EVENT_APPLY_FRIEND)
					return;
			}
			//��������¼�
			addEvent(my,friend,EVENT_APPLY_FRIEND,obf);
		}
	}
	
	/**
	 * ȷ�Ϻ������ ˫�����
	 */
	public void addFriend(Player my,Player friend,int eventId,boolean agree,CreatObjectFactory obf){
		if(agree){
			my.getFriendInfo().getFriends().put(friend.getId(),new FriendIntimacy(friend,0,0,0));
			friend.getFriendInfo().getFriends().put(my.getId(),new FriendIntimacy(my,0,0,0));
			addEvent(my,friend,EVENT_ADD_FRIEND,obf);
			addEvent(friend,my,EVENT_ADD_FRIEND,obf);
			JBackKit.sendPlayerFriendInfo(friend,obf);//ˢ�º�����Ϣ
			JBackKit.sendPlayerFriendInfo(my,obf);//ˢ�º�����Ϣ
		}
		removeEvent(my,eventId,obf);
	}
	
	/**
	 * ɾ������
	 */
	public void reMoveFriend(Player my,Player friend,CreatObjectFactory obf){
		IntKeyHashMap Map = my.getFriendInfo().getFriends();
		Map.remove(friend.getId());
		addEvent(my,friend,EVENT_REMOVE_FRIEND,obf);
		
		IntKeyHashMap friendMap = friend.getFriendInfo().getFriends();
		friendMap.remove(my.getId());
		
		JBackKit.sendPlayerFriendInfo(friend,obf);//ˢ�º�����Ϣ
		JBackKit.sendPlayerFriendInfo(my,obf);//ˢ�º�����Ϣ
	}
	
	/**
	 * ����ǰ̨ˢ�µ����л�
	 */
	public void goodFriendWrite(ByteBuffer data,CreatObjectFactory obf){
		//��������͡���ȡʱ��
		int now =  TimeKit.getSecondTime();
		if(lastGiveTimes!=0&&lastGiveTime!=0&&!SeaBackKit.isSameDay(now,lastGiveTime)){
			lastGiveTimes = 0;
		}
		if(lastReceivedTimes!=0&&lastReceivedTime!=0&&!SeaBackKit.isSameDay(now,lastReceivedTime)){
			lastReceivedTimes = 0;
		}
		data.writeInt(intimacy);//���ܶ�
		
		data.writeByte(lastGiveTimes);//���ʹ���
		data.writeByte(MAX_GIVE_NUM);//��ȡ��������
		
		data.writeByte(lastReceivedTimes);//��ȡ����
		data.writeByte(MAX_RECEIVED_NUM);//��ȡ��������
		
		//�����б�
		int[] keys = friends.keyArray();
		data.writeByte(keys.length);
		for(int i=0;i<keys.length;i++){
			int playerId = keys[i];
			FriendIntimacy fi = (FriendIntimacy)friends.get(playerId);
			if(fi.getPlayer()==null)
				fi.setPlayer(obf.getPlayerById(playerId));
			data.writeInt(fi.getPlayer().getId());//����ID
			data.writeUTF(fi.getPlayer().getName());//��������
			data.writeInt(fi.getPlayer().getAttrHead());//����ͷ��
			data.writeInt(fi.getPlayer().getAttrHeadBorder());
			data.writeBoolean(fi.getLastGiveTime()==0?true:!SeaBackKit.isSameDay(now,fi.getLastGiveTime()));//�ܷ�����
			data.writeBoolean(fi.getLastfriendGiveTime()!=0&&
							SeaBackKit.isSameDay(now,fi.getLastfriendGiveTime())&&
							!SeaBackKit.isSameDay(now,fi.getLastReceivedTime()));//�ܷ���ȡ  ������������ʱ�� ���ǽ���   ��ȡʱ�䲻�ǽ���
			
		}
	}
	
	
	/**
	 * ����ǰ̨ˢ�µ����л�
	 */
	public void eventWrite(ByteBuffer data,CreatObjectFactory obf){
		int[] eventKeys = eventMap.keyArray();
		int size = eventKeys.length;
		data.writeShort(size);
		for(int i=0;i<size;i++){
			FriendEvent fe = (FriendEvent)eventMap.get(eventKeys[i]);
			Player pInfo = obf.getPlayerById(fe.getSendPlayerId());
			data.writeInt(fe.getEventId());//�¼�ID
			data.writeByte(fe.getEventType());//�¼�����
			data.writeUTF(pInfo.getName());//�����¼��˵�����
			data.writeInt(pInfo.getAttrHead());//����ͷ��
			data.writeInt(pInfo.getAttrHeadBorder());//�߿�
		}
	}
	
	
	/**
	 * ����¼�
	 * @param send ������
	 * @param to Ŀ����
	 * @param type �¼�����
	 */
	public void addEvent(Player send,Player to,int type,CreatObjectFactory obf){
		int eventId = getNewEventId();
		to.getFriendInfo().getEventMap().put(eventId,new FriendEvent(eventId,type,TimeKit.getSecondTime(),send.getId()));
		//����
		JBackKit.sendPlayerFriendEvents(to,obf);
	}
	
	public void removeEvent(Player my,int eventId,CreatObjectFactory obf){
		eventMap.remove(eventId);
		JBackKit.sendPlayerFriendEvents(my,obf);
	}
	
	/**
	 * ��½���ͻ��˺�����Ϣ
	 */
	public void infoWrite(ByteBuffer data,CreatObjectFactory obf){
		
		//��������͡���ȡʱ��
		int now =  TimeKit.getSecondTime();
		if(lastGiveTimes!=0&&lastGiveTime!=0&&!SeaBackKit.isSameDay(now,lastGiveTime)){
			lastGiveTimes = 0;
		}
		if(lastReceivedTimes!=0&&lastReceivedTime!=0&&!SeaBackKit.isSameDay(now,lastReceivedTime)){
			lastReceivedTimes = 0;
		}
		data.writeInt(intimacy);//���ܶ�
		
		data.writeByte(lastGiveTimes);//���ʹ���
		data.writeByte(MAX_GIVE_NUM);//��ȡ��������
		
		data.writeByte(lastReceivedTimes);//��ȡ����
		data.writeByte(MAX_RECEIVED_NUM);//��ȡ��������
		
		//�����б�
		int[] keys = friends.keyArray();
		data.writeByte(keys.length);
		for(int i=0;i<keys.length;i++){
			int playerId = keys[i];
			FriendIntimacy fi = (FriendIntimacy)friends.get(playerId);
			if(fi.getPlayer()==null)
				fi.setPlayer(obf.getPlayerById(playerId));
			data.writeInt(fi.getPlayer().getId());//����ID
			data.writeUTF(fi.getPlayer().getName());//��������
			data.writeInt(fi.getPlayer().getAttrHead());//����ͷ��
			data.writeInt(fi.getPlayer().getAttrHeadBorder());
			data.writeBoolean(fi.getLastGiveTime()==0?true:!SeaBackKit.isSameDay(now,fi.getLastGiveTime()));//�ܷ�����
			data.writeBoolean(fi.getLastfriendGiveTime()!=0&&
							SeaBackKit.isSameDay(now,fi.getLastfriendGiveTime())&&
							!SeaBackKit.isSameDay(now,fi.getLastReceivedTime()));//�ܷ���ȡ  ������������ʱ�� ���ǽ���   ��ȡʱ�䲻�ǽ���

		}
		
		//������ 
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
			data.writeInt(fe.getEventId());//�¼�ID
			data.writeByte(fe.getEventType());//�¼�����
			data.writeUTF(pInfo.getName());//�����¼��˵�����
			data.writeInt(pInfo.getAttrHead());//����ͷ��
			data.writeInt(pInfo.getAttrHeadBorder());//�߿�
		}
		
	}
	
	/**
	 * ��ȡ���ܶ�
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
				intimacy+=RECEIVED_INTIMACY;//�������ܶ�
				lastReceivedTimes++;//������ȡ����
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
				if(lastReceivedTimes>=MAX_RECEIVED_NUM)break;//����������
				FriendIntimacy fi=(FriendIntimacy)friends.get(keys[i]);
				if(fi.getLastfriendGiveTime()!=0
								&&SeaBackKit.isSameDay(now,fi.getLastfriendGiveTime())
								&&!SeaBackKit.isSameDay(now,fi.getLastReceivedTime()))
							{
								fi.setLastReceivedTime(now);
								intimacy+=RECEIVED_INTIMACY;//�������ܶ�
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
	 * �������ܶ�
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
			FriendIntimacy fi_1 = (FriendIntimacy)friend.getFriendInfo().getFriends().get(my.getId());//���º��ѵ���Ϣ
			if(fi_1!=null){
				fi_1.setLastfriendGiveTime(now);
			}
			
			lastGiveTimes++;//�������ʹ���
			intimacy+=RECEIVED_INTIMACY;//�Լ�Ҳ����
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
	 * ���л���Ϣ  ׼���־û�����
	 * @param data
	 */
	public synchronized void bytesWrite(ByteBuffer data)
	{
		data.writeInt(intimacy);//���ܶ�
		data.writeInt(lastGiveTime);// ���һ�θ���ʱ��
		data.writeInt(lastGiveTimes);// ���һ�θ���ʱ�Ĵ��� 
		data.writeInt(lastReceivedTime);//���һ����ȡʱ��
		data.writeInt(lastReceivedTimes);//���һ����ȡ�Ĵ��� ����ȡʱ��һ����Ϊ����
		//������Ϣ
		data.writeInt(friends.keyArray().length);
		for(int i=0;i<friends.keyArray().length;i++){
			data.writeInt(friends.keyArray()[i]);
			FriendIntimacy fi = (FriendIntimacy)friends.get(friends.keyArray()[i]);
			data.writeInt(fi.getLastGiveTime());//���͸����ѵ�ʱ�� 
			data.writeInt(fi.getLastfriendGiveTime());//�������͸��ҵ����ܶȵ�ʱ��
			data.writeInt(fi.getLastReceivedTime());//��ȡ�������ܶ�ʱ��
		}
		//������
		data.writeInt(blackList.keyArray().length);
		for(int i=0;i<blackList.keyArray().length;i++){
			data.writeInt(blackList.keyArray()[i]);
		}
		
	}
	
	/**
	 * �����л�����
	 * @param data
	 */
	public synchronized void bytesRead(ByteBuffer data){
		this.intimacy = data.readInt();//���ܶ�
		this.lastGiveTime = data.readInt();// ���һ�θ���ʱ��
		this.lastGiveTimes = data.readInt();// ���һ�θ���ʱ�Ĵ��� 
		this.lastReceivedTime = data.readInt();//���һ����ȡʱ��
		this.lastReceivedTimes = data.readInt();//���һ����ȡ�Ĵ��� ����ȡʱ��һ����Ϊ����
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
	 * �۳����ܶ�
	 * @param num
	 */
	public void reduceIntimacy(int num){ //��ȡ�ͼ�ȥ �ֿ� ������
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
	 * ����һ���¼�ID
	 * @return
	 */
	public synchronized int getNewEventId(){
		return ++eventSize;
	}



}
