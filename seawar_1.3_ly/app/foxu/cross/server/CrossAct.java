package foxu.cross.server;

import foxu.sea.kit.SeaBackKit;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import mustang.text.TextKit;
import mustang.util.TimeKit;


/**
 * 跨服活动信息
 * @author yw
 *
 */
public class CrossAct implements CrossActRW
{
	/*static fields */
	
	/** 跨服活动sid */
	public static int WAR_SID=1;
	/** 现有跨服活动 */
	public static int[] crossacts={WAR_SID};
	
	/* fields */
	
	/** 跨服活动唯一id */
	int id;
	/** 跨服活动sid */
	int sid;
	/** 跨服活动开始时间 */
	int stime;
	/** 跨服活动所需时间 */
	int etime;
	/** 强制结束 */
	boolean forceover;
	
	//临时变量
	/**是否需要保存*/
	boolean needsave;
	
	
	/** 判断活动是否结束 */
	public boolean isover()
	{
		return TimeKit.getSecondTime()>getEtime()||isForceover();
	}
	
	/** 修改奖励 */
	public boolean setAward(String award)
	{
		return true;
	}
	/** 检测时间合法性 */
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
