package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


/**
 * 据点给的永久BUFF（直到盟不再占据该据点）
 * @author yw
 *
 */
public class ForeverService extends Service
{
	
	/** 服务是否已经结束 */
	public boolean isOver(int second)
	{
		if(endTime<0)return false;
		return second>=endTime;
	}
	
	/** 获取服务结束时间 */
	public int getEndTime()
	{
		if(endTime<0)return TimeKit.getSecondTime();
		return endTime;
	}
	public void showBytesWrite(ByteBuffer data,int time)
	{
		data.writeShort(getSid());
		data.writeInt(-1);
	}

}
