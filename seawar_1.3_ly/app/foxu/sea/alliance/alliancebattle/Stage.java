package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.text.TextKit;
import foxu.sea.ContextVarManager;

/***
 * ����ս��״̬����
 * 
 * @author lhj
 */
public class Stage
{

	/** ��ʼʱ�� **/
	int stime;
	/** ����ʱ�� **/
	int etime;
	/** ����ս�Ľ׶� Ĭ��Ϊ0 **/
	int stage;
	/** �׶��Ƿ���� **/
	boolean over=true;
	/** ϵͳ���ŵĴ��� **/
	int resourceTimes;
	/** ս������ **/
	int times;

	/** ϵͳ���ŵ������� **/
	public static final int RESOURCE_MAX=5;

	/**
	 * �׶� STAGE_ONE=1 ϵͳ������������ STAGE_TWO=2 ���˲��뾺������ STAGE_THREE=3 ��ҿ�ʼ����
	 * STAGE_FOUR=4 ����ս����
	 */
	public static final int STAGE_ONE=1,STAGE_TWO=2,STAGE_THREE=3,
					STAGE_FOUR=4;

	/** �׶α任 **/
	public void addStage()
	{
		stage++;
	}

	/** ���ý���ʱ�� **/
	public void resetEtime(int time)
	{
		etime+=time;
	}
	/** ����ϵͳ����Դ�Ĵ��� **/
	public void addResourceTimes()
	{
		resourceTimes++;
	}
	/** ���ÿ�ʼʱ�� **/
	public void resetStime(int time)
	{
		stime+=time;
		if(stime>etime) stime=etime;
	}
	/****/
	public void addTimes()
	{
		times++;
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

	public int getStage()
	{
		return stage;
	}

	public void setStage(int stage)
	{
		this.stage=stage;
	}

	public boolean isOver()
	{
		return over;
	}

	public void setOver(boolean over)
	{
		this.over=over;
	}

	public int getResourceTimes()
	{
		return resourceTimes;
	}

	public void setResourceTimes(int resourceTimes)
	{
		this.resourceTimes=resourceTimes;
	}

	public int getTimes()
	{
		return times;
	}

	public void setTimes(int times)
	{
		this.times=times;
	}

	/** ���ݱ��� **/
	public int saveAndExit()
	{
		ContextVarManager varManager=ContextVarManager.getInstance();
		StringBuffer buffer=new StringBuffer();
		buffer.append(stime);
		buffer.append(","+etime);
		buffer.append(","+stage);
		if(over)
			buffer.append(","+1);
		else
			buffer.append(","+0);
		buffer.append(","+resourceTimes);
		buffer.append(","+times);
		varManager.setVarDest(ContextVarManager.ALLIANCE_FIGHT_DATA,
			buffer.toString());
		return 0;
	}

	/** ��ʼ�� **/
	public Stage init()
	{
		ContextVarManager varManager=ContextVarManager.getInstance();
		String data=varManager
			.getVarDest(ContextVarManager.ALLIANCE_FIGHT_DATA);
		if(data!=null)
		{
			String[] datas=TextKit.split(data,",");
			stime=TextKit.parseInt(datas[0]);
			etime=TextKit.parseInt(datas[1]);
			stage=TextKit.parseInt(datas[2]);
			int flag=TextKit.parseInt(datas[3]);
			if(flag==1)
				over=true;
			else
				over=false;
			resourceTimes=TextKit.parseInt(datas[4]);
			times=TextKit.parseInt(datas[5]);
		}
		else
		{
			over=true;
			//��ʼ�����˱�ʶ
			int record=ContextVarManager.getInstance().getVarValue(
				ContextVarManager.ALLIANCE_FIGHT_RECORD);
			if(record<=0)
			{
				ContextVarManager.getInstance().setVarValue(
					ContextVarManager.ALLIANCE_FIGHT_RECORD,1);
			}
		}
		return this;
	}

	/** ����׶���Ϣ **/
	public void showBytesWriteStage(ByteBuffer data)
	{
		data.writeByte(stage);
		data.writeInt(stime);
		data.writeInt(etime);

	}

	/**��ս�׶ε�ʱ��ʹ���**/
	public void showByteWrite(ByteBuffer data,boolean flag)
	{
		if(!flag)
		{
			data.writeInt(0);
			data.writeShort(0);
			return;
		}
		data.writeInt(stime);
		data.writeShort(times);
	}

	public void clear()
	{
		resourceTimes=0;
		times=0;
	}
}
