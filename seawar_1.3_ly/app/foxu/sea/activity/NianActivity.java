package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.PublicConst;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.worldboss.NianBoss;
import foxu.sea.worldboss.WorldBoss;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;


/**
 * 年兽活动
 * @author yw
 *
 */
public class NianActivity extends Activity implements TimerListener,ActivitySave
{
	Logger log=LogFactory.getLogger(NianActivity.class);
	/** 时钟事件 */
	TimerEvent te;
	/** 年兽  */
	NianBoss boss;
	
	CreatObjectFactory factory;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		this.factory=factory;
		String[] initDatas=TextKit.split(initData,"#");
		boss=(NianBoss)WorldBoss.factory.newSample(TextKit.parseInt(initDatas[0]));
		resetActivity(stime,etime,initDatas[1],factory);
		return getActivityState();
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
		boss.initBoss(initData);
		initTimer();
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
			.append(",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append(",\"others\":\"").append(boss.toString()).append("\"")
			.append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		this.factory=factory;
		boss=(NianBoss)WorldBoss.factory.newSample(data.readUnsignedShort());
		boss.bytesRead(data);
		if(active)
		{
			showBoss();
			initTimer();
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(boss.getSid());
		boss.bytesWrite(data);
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeInt(endTime-TimeKit.getSecondTime());
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		if(isOpen(TimeKit.getSecondTime()))
		{
			//活动开始刷出boss
			showBoss();
		}
		else
		{
			// 活动到期关闭
			TimerCenter.getSecondTimer().remove(te);
			TimerCenter.getSecondTimer().remove(this);
			if(boss!=null&&boss.getIndex()>0)
			{
				boss.away(factory);
			}
		}
		
	}
	/** 显示boss */
	public void showBoss()
	{
		if(boss.getIndex()>0)return;
		if(!isOpen(TimeKit.getSecondTime()))return;
		NpcIsland island=factory.getIslandCache().getRandomSpace();
		if(island==null)
		{
			log.error("show nian fail,world is full");
			return;
		}
		island.updateSid(boss.getSid());
		factory.getIslandCache().load(island.getIndex()+"");
		boss.setIndex(island.getIndex());
		boss.setCreateTime(TimeKit.getSecondTime());
		boss.createFleetGroup();
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"nian born");
		message=TextKit.replace(message,"%",boss.getBossLevel()+"");
		int sx=boss.getIndex()%600+1;
		int sy=boss.getIndex()/600+1;
		message=TextKit.replace(message,"%",getBossDirection(sx,sy));
		// 系统公告
		SeaBackKit.sendSystemMsg(factory.getDsmanager(),message);
		// 刷新前台
		JBackKit.flushIsland(factory.getDsmanager(),island,factory);
	}
	
	public String getBossDirection(int sx,int sy)
	{
		String dir=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"boss_taunt_direction");
		String[] dirs=TextKit.split(dir,",");
		int[] dirs_int={5,0,6,2,4,3,7,1,8};
		int index=0;
		boolean get=false;
		for(int y=0;y<=2;y++)
		{
			if(sy<=(y+1)*200)
			{
				for(int x=0;x<=2;x++)
				{
					if(sx<=(x+1)*200)
					{
						index=y*3+x;
						get=true;
						break;
					}
				}
				if(get==true)break;
			}
		}
		return dirs[dirs_int[index]];
		
	}

	
	public NianBoss getBoss()
	{
		return boss;
	}

	
	public void setBoss(NianBoss boss)
	{
		this.boss=boss;
	}
	
	public TimerEvent getTe()
	{
		return te;
	}

	
	public void setTe(TimerEvent te)
	{
		this.te=te;
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(boss!=null&&boss.showXY(factory))
		{
			TimerCenter.getSecondTimer().remove(e);
			TimerCenter.getSecondTimer().remove(this);
		}
	}

	/** 初始化定时器 */
	public void initTimer()
	{
		if(!TimerCenter.getSecondTimer().contain(te))
		{
			TimerCenter.getSecondTimer().add(te);
		}
	}

	@Override
	public boolean isSave()
	{
		return boss.isNeedsave();
	}

	@Override
	public void setSave()
	{
		boss.setNeedsave(false);
	}
	
	@Override
	public Object copy(Object obj)
	{
		NianActivity act=(NianActivity)super.copy(obj);
		act.setTe(new TimerEvent(act,"xy",30*1000));
		return act;
	}
}
