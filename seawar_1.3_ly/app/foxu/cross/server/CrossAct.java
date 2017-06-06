package foxu.cross.server;

import foxu.sea.kit.SeaBackKit;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import mustang.text.TextKit;
import mustang.util.TimeKit;


/**
 * ������Ϣ
 * @author yw
 *
 */
public class CrossAct implements CrossActRW
{
	/*static fields */
	
	/** ����sid */
	public static int WAR_SID=1;
	/** ���п��� */
	public static int[] crossacts={WAR_SID};
	
	/* fields */
	
	/** ����Ψһid */
	int id;
	/** ����sid */
	int sid;
	/** ������ʼʱ�� */
	int stime;
	/** ��������ʱ�� */
	int etime;
	/** ǿ�ƽ��� */
	boolean forceover;
	
	//��ʱ����
	/**�Ƿ���Ҫ����*/
	boolean needsave;
	
	
	/** �жϻ�Ƿ���� */
	public boolean isover()
	{
		return TimeKit.getSecondTime()>getEtime()||isForceover();
	}
	
	/** �޸Ľ��� */
	public boolean setAward(String award)
	{
		return true;
	}
	/** ���ʱ��Ϸ��� */
	public boolean checkDate(String date)
	{
		return true;
	}
	
	public static int[] getCrossacts()
	{
		return crossacts;
	}
	
	public static void setCrossacts(int[] crossacts)
	{
		CrossAct.crossacts=crossacts;
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getSid()
	{
		return sid;
	}
	
	public void setSid(int sid)
	{
		this.sid=sid;
	}
	
	public int getStime()
	{
		return stime;
	}
	
	public void setStime(int stime)
	{
		this.stime=stime;
	}

	
	public int getEtime()
	{
		return etime;
	}

	
	public void setEtime(int etime)
	{
		this.etime=etime;
	}
	
	
	public boolean isForceover()
	{
		return forceover;
	}

	
	public void setForceover(boolean forceover)
	{
		this.forceover=forceover;
	}
	
	
	public boolean isNeedsave()
	{
		return needsave;
	}

	
	public void setNeedsave(boolean needsave)
	{
		this.needsave=needsave;
	}

	public void showBytesWrite(ByteBuffer data)
	{
			data.writeInt(id);
			data.writeShort(sid);
			data.writeInt(stime);
			data.writeInt(etime);
			data.writeBoolean(forceover);
	}
	
	public void showBytesRead(ByteBuffer data)
	{
		id=data.readInt();
		sid=data.readUnsignedShort();
		stime=data.readInt();
		etime=data.readInt();
		forceover=data.readBoolean();
	}

	@Override
	public void writeData(ByteBuffer data)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readData(ByteBuffer data)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb=new StringBuffer();
		sb.append("sid:"+sid+" ");
		sb.append("id:"+id+" ");
		sb.append("stime:"+stime+" ");
		sb.append("etime:"+etime+" ");
		return sb.toString();
	}
	public JSONObject toJson()
	{
		JSONObject jsn=new JSONObject();
		try
		{
			jsn.put("id",id);
			jsn.put("sid",sid);
			jsn.put("stime",SeaBackKit.formatDataTime(stime));
			jsn.put("etime",SeaBackKit.formatDataTime(etime));
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jsn;
	}
	
	
}
