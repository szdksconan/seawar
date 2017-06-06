package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.Service;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/***
 * 和平活动
 * 
 * @author lhj
 *
 */
public class PeaceActivity extends Activity implements ActivitySave
{

	public static final int SAVE_CIRCLE=15*60;

	int lastSaveTime;
	/**确保只进行一次**/
	boolean add=true;
	
	@Override
	public boolean isSave()
	{
		if(TimeKit.getSecondTime()>=lastSaveTime+SAVE_CIRCLE) return true;
		return false;
	}
	@Override
	public void setSave()
	{
		lastSaveTime=TimeKit.getSecondTime();
	}

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		return resetActivity(stime,etime,initData,factoty);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		boolean bool=isOpen(TimeKit.getSecondTime());
		int change=SeaBackKit.parseFormatTime(etime);
		if(bool)
		{
			if(startTime!=0&&startTime<TimeKit.getSecondTime())
				startTime=TimeKit.getSecondTime();
			if(endTime!=0)
			{
				Service service=(Service)Service.factory.newSample(9);
				if(change>endTime)
					SeaBackKit.addPeaceCover(change-endTime,service);
				else
				{
					if(TimeKit.getSecondTime()<change)
						SeaBackKit.reduceService(endTime-change,service);
					else
						SeaBackKit.reduceService(
							endTime-TimeKit.getSecondTime(),service);
				}
			}
		}
		startTime=SeaBackKit.parseFormatTime(stime);
		endTime=SeaBackKit.parseFormatTime(etime);
		if(!bool&&isOpen(TimeKit.getSecondTime()))
		{
			if(endTime!=0)
			{
				Service service=(Service)Service.factory.newSample(9);
				if(change>TimeKit.getSecondTime())
					SeaBackKit.addPeaceCover(change-TimeKit.getSecondTime(),service);
			}
		}
		JBackKit.sendPeaceActivity(factoty.getDsmanager().getSessionMap(),
			this,factoty);
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
			.append(",\"others\":\"");
		sb.append("\",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		add=data.readBoolean();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeBoolean(add);
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeInt(startTime);
		data.writeInt(endTime);
	}

	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		data.writeShort(getSid());
		data.writeInt(startTime);
		data.writeInt(endTime);
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		if(endTime>TimeKit.getSecondTime() && add)
		{
			Service service=(Service)Service.factory.newSample(9);
			SeaBackKit.addPeaceCover(endTime-TimeKit.getSecondTime(),service);
			add=false;
		}
	}

}
