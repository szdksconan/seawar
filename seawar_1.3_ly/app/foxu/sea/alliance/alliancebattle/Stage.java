package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.text.TextKit;
import foxu.sea.ContextVarManager;

/***
 * 联盟战的状态对象
 * 
 * @author lhj
 */
public class Stage
{

	/** 开始时间 **/
	int stime;
	/** 结束时间 **/
	int etime;
	/** 联盟战的阶段 默认为0 **/
	int stage;
	/** 阶段是否结束 **/
	boolean over=true;
	/** 系统发放的次数 **/
	int resourceTimes;
	/** 战斗计数 **/
	int times;

	/** 系统发放的最大次数 **/
	public static final int RESOURCE_MAX=5;

	/**
	 * 阶段 STAGE_ONE=1 系统主动发放物资 STAGE_TWO=2 联盟参与竞争岛屿 STAGE_THREE=3 玩家开始报名
	 * STAGE_FOUR=4 联盟战开启
	 */
	public static final int STAGE_ONE=1,STAGE_TWO=2,STAGE_THREE=3,
					STAGE_FOUR=4;

	/** 阶段变换 **/
	public void addStage()
	{
		stage++;
	}

	/** 重置结束时间 **/
	public void resetEtime(int time)
	{
		etime+=time;
	}
	/** 增加系统发资源的次数 **/
	public void addResourceTimes()
	{
		resourceTimes++;
	}
	/** 重置开始时间 **/
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

	/** 数据保存 **/
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

	/** 初始化 **/
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
			//初始化联盟标识
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

	/** 岛屿阶段信息 **/
	public void showBytesWriteStage(ByteBuffer data)
	{
		data.writeByte(stage);
		data.writeInt(stime);
		data.writeInt(etime);

	}

	/**对战阶段的时间和次数**/
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
