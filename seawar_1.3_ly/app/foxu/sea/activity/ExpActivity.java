package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.kit.SeaBackKit;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


/**
 * 经验加成活动
 * @author yw
 *
 */
public class ExpActivity extends Activity
{

	/** 经验加成 */
	int percent=20;
	
	public int getPercent()
	{
		return percent;
	}

	@Override
	public String startActivity(String stime,String etime,String initData,CreatObjectFactory factory)
	{
		return resetActivity(stime,etime,initData,factory);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,CreatObjectFactory factory)
	{
//		System.out.println("---------resetActivity------00--------："+initData);
		String[] data=initData.split(",");
		try
		{
			int perc=Integer.parseInt(data[0]);
			if(perc>=0&&perc<=100)
			{
				this.percent=perc;
			}
		}
		catch(Exception e)
		{
		}
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
//		System.out.println("---------resetActivity------11--------");
		return getActivityState();
	}

	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"").append("["+percent+"]")
			.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		percent=data.readUnsignedShort();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(percent);
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(percent);
		data.writeInt(endTime-TimeKit.getSecondTime());
		
	}
	@Override
	public void sendFlush(SessionMap smap)
	{
		// TODO Auto-generated method stub
		
	}

}
