package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.kit.SeaBackKit;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;

/**
 * 充值活动
 * @author yw
 *
 */
public class BuyGemsActivity extends Activity
{

	/** 开启活动*/
	@Override
	public String startActivity(String stime,String etime,String initData,CreatObjectFactory factory)
	{
		return resetActivity(stime,etime,initData,factory);
	}
	/** 重写活动*/
	@Override
	public String resetActivity(String stime,String etime,String initData,CreatObjectFactory factory)
	{
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
		return getActivityState();
	}

	/** 获取活动状态 */
	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"").append("20").append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	/** 序列化活动给前台 */
	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(20);
		data.writeInt(endTime-TimeKit.getSecondTime());

	}
	
	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{

	}

	@Override
	public ByteBuffer getInitData()
	{
		return null;
	}
	@Override
	public void sendFlush(SessionMap smap)
	{
		// TODO Auto-generated method stub
		
	}

}
