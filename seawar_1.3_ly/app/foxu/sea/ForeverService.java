package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


/**
 * �ݵ��������BUFF��ֱ���˲���ռ�ݸþݵ㣩
 * @author yw
 *
 */
public class ForeverService extends Service
{
	
	/** �����Ƿ��Ѿ����� */
	public boolean isOver(int second)
	{
		if(endTime<0)return false;
		return second>=endTime;
	}
	
	/** ��ȡ�������ʱ�� */
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
