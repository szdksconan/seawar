package foxu.sea.comrade;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;

/**
 * ս��ϵͳ
 * 
 * @author yw
 * 
 */
public class Comrade
{

	public static final int COMRADE_LEVEL=30;
	/**����**/
	public static final  boolean  ON_LINE=true;
	/**����**/
	public static final boolean OFF_LINE=false;
	/**����״̬**/
	boolean online=false;
	/** R_MAX�±����� */
	public static int R_MAX=200;
	/** �ϱ� */
	int veteranId;
	/** �±� */
	IntList recruitIds=new IntList();

	/** �Լ����콱��¼ ����sid-AwardMark */
	IntKeyHashMap awardMap=new IntKeyHashMap();
	/** �Լ������ϱ���ɵ������¼(key,value) */
	int[] taskMark={};

	/** ���л�д */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(veteranId);

		data.writeShort(recruitIds.size());
		for(int i=0;i<recruitIds.size();i++)
		{
			data.writeInt(recruitIds.get(i));
		}

		data.writeShort(awardMap.size());
		int[] keys=awardMap.keyArray();
		for(int i=0;i<awardMap.size();i++)
		{
			AwardMark am=(AwardMark)awardMap.get(keys[i]);
			am.bytesWrite(data);
		}

		data.writeShort(taskMark.length);
		for(int i=0;i<taskMark.length;i++)
		{
			data.writeInt(taskMark[i]);
		}
	}

	/** ���л��� */
	public void bytesRead(ByteBuffer data)
	{
		veteranId=data.readInt();
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			recruitIds.add(data.readInt());
		}

		len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			AwardMark am=new AwardMark();
			am.bytesRead(data);
			awardMap.put(am.getSid(),am);
		}

		len=data.readUnsignedShort();
		taskMark=new int[len];
		for(int i=0;i<len;i++)
		{
			taskMark[i]=data.readInt();
		}
	}

	public int getVeteranId()
	{
		return veteranId;
	}

	public void setVeteranId(int veteranId)
	{
		this.veteranId=veteranId;
	}

	public IntList getRecruitIds()
	{
		return recruitIds;
	}

	public void setRecruitIds(IntList recruitIds)
	{
		this.recruitIds=recruitIds;
	}

	public IntKeyHashMap getAwardMap()
	{
		return awardMap;
	}

	public void setAwardMap(IntKeyHashMap awardMap)
	{
		this.awardMap=awardMap;
	}

	public int[] getTaskMark()
	{
		return taskMark;
	}

	public void setTaskMark(int[] taskMark)
	{
		this.taskMark=taskMark;
	}

	
	public boolean isOnline()
	{
		return online;
	}

	
	public void setOnline(boolean online)
	{
		this.online=online;
	}

}
