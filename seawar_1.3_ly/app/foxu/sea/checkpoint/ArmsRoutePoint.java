package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;

/***
 * ��������
 * 
 * @author lhj
 */
public class ArmsRoutePoint
{

	/** �������ߵ����� */
	IntKeyHashMap shipFleets=new IntKeyHashMap();
	/** ��¼ **/
	IntKeyHashMap routeRecord=new IntKeyHashMap();
	/** ��ǰ����Ľ��� **/
	int checkSid;

	public ArmsRoutePoint()
	{
		checkSid=PublicConst.ARMS_CHECK_SID;
	}

	/** ������������ **/
	public void addPoint(int star,int sid,int nextSid)
	{
		if(nextSid>checkSid) checkSid=nextSid;
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) record=new ArmsRecord();
		int times=record.getChallengTimes()+1;
		record.setSid(sid);
		record.setChallengTimes(times);
		record.setAttackTime(TimeKit.getSecondTime());
		if(record.getStarNum()<star) record.setStarNum(star);
		routeRecord.put(sid,record);
	}
	/** �Ƿ����ɨ�� **/
	public boolean canattact(int sid)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return false;
		return record.getStarNum()>=SelfCheckPoint.THREE_STAR;
	}
	/** �ж��Ƿ�ɸ���ˢ�� */
	public boolean canPayReset(int sid,int count)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return false;
		return record.getPayCount()>=count;
	}
	/** ��ȡ����ˢ�´��� */
	public int getPayCount(int sid)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return 0;
		return record.getPayCount();
	}
	/** ����ˢ�´��� */
	public boolean addPayCount(int sid)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return false;
		int count=record.getPayCount()+1;
		record.setPayCount(count);
		return true;
	}

	/** ��ȡ��ս���� */
	public int getChallengTimes(int sid)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return 0;
		return record.getChallengTimes();
	}
	/** �ж��Ƿ����ս */
	public String canChalleng(int sid,int times,int challengTime)
	{
		if(sid>checkSid) return "you can not fight the sid";
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return null;
		int timeNow=TimeKit.getSecondTime();
		// ������ͬһ����ˢ��
		if(!SeaBackKit.isSameDay(timeNow,record.getAttackTime()))
			clear(sid);
		if(record.getChallengTimes()+challengTime>times) return "times is out";
		return null;
	}
	/** ������Ѽ�¼����ս���� **/
	public void clear(int sid)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return;
		record.setChallengTimes(0);
		record.setPayCount(0);
	}
	/** �����ս��¼ **/
	public void clearChalleng(int sid)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return;
		record.setChallengTimes(0);
		record.setPayCount(record.getPayCount()+1);
	}
	/** ��ȡ�������� **/
	public int getPointStar(int sid)
	{
		ArmsRecord record=(ArmsRecord)routeRecord.get(sid);
		if(record==null) return 0;
		return record.getStarNum();
	}
	/** ���л���ǰ̨ **/
	public void showBytesWrite(ByteBuffer data)
	{
		// data.writeShort(checkSid);
		Object[] recordValue=routeRecord.valueArray();
		if(recordValue==null)
		{
			data.writeByte(0);
		}
		else
		{
			int len=0;
			int top=data.top();
			data.writeByte(routeRecord.size());
			int timenow=TimeKit.getSecondTime();
			for(int j=0;j<recordValue.length;j++)
			{
				if(recordValue[j]==null) continue;
				len++;
				ArmsRecord record=(ArmsRecord)recordValue[j];
				if(!SeaBackKit.isSameDay(timenow,record.getAttackTime()))
				{
					clear(record.getSid());
				}
				record.showBytesWrite(data);
			}
			if(len!=routeRecord.size())
			{
				int nowTop=data.top();
				data.setTop(top);
				data.writeByte(len);
				data.setTop(nowTop);
			}
		}
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(checkSid);
		Object[] recordValue=routeRecord.valueArray();
		if(recordValue==null)
		{
			data.writeInt(0);
		}
		else
		{
			int len=0;
			int top=data.top();
			data.writeInt(routeRecord.size());
			for(int i=0;i<recordValue.length;i++)
			{
				if(recordValue[i]==null) continue;
				len++;
				ArmsRecord record=(ArmsRecord)recordValue[i];
				record.bytesWrite(data);
			}
			if(len!=routeRecord.size())
			{
				int nowTop=data.top();
				data.setTop(top);
				data.writeInt(len);
				data.setTop(nowTop);
			}
		}
	}

	/** �����л� */
	public Object bytesRead(ByteBuffer data)
	{
		checkSid=data.readUnsignedShort();
		int length=data.readInt();
		if(length!=0)
		{
			for(int j=0;j<length;j++)
			{
				ArmsRecord record=new ArmsRecord();
				record.bytesRead(data,routeRecord);
			}
		}
		return this;
	}
	/** ��ǰ����Ľ��� **/
	public int getCheckSid()
	{
		return checkSid;
	}

	public void setCheckSid(int checkSid)
	{
		this.checkSid=checkSid;
	}

}
