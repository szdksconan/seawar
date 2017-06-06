package foxu.sea.recruit;

import shelby.ds.DSManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * 新兵福利管理器
 * 
 * @author yw
 * 
 */
public class RecruitWelfareManager implements TimerListener
{

	/** 新兵福利列表 */
	ArrayList list=new ArrayList();

	CreatObjectFactory cfactory;

	DSManager dsmanager;

	// 临时变量
	int pushTime;

	public void init()
	{
		pushTime=TimeKit.getSecondTime();
		TimerCenter.getMinuteTimer()
			.add(new TimerEvent(this,"push",60*1000));
	}

	/** 获取第几天奖励 0普通 1vip 2任务 */
	public void getAward(int getType,Player player,ByteBuffer data)
	{
		int days=data.readUnsignedByte()+1;
		RecruitDayWelfare welfare=checkNormal(days,player);
		RecruitAward award=null;
		int taskid=0;
		if(getType==0)
		{
			award=welfare.getNormalAward();
		}
		else if(getType==1)
		{
			if(welfare.getViplv()>player.getUser_state())
				throw new DataAccessException(0,"vip limit");
			award=welfare.getVipAward();
		}
		else if(getType==2)
		{
			taskid=data.readUnsignedByte();
			RecruitDayTask task=welfare.getTasks(taskid);
			if(!checkTask(task,player))
				throw new DataAccessException(0,"condition limit");
			award=task.getAward();
		}
		if(!canGetAward(award,player))
			throw new DataAccessException(0,"have got");
		RecruitKit.recordAward(award,player);
		sendWelfare(award,player,data);
		data.clear();
		data.writeByte(days-1);
		if(getType==2) data.writeByte(taskid);
	}
	/** 抢购第几天的半价 */
	public void buyHalf(Player player,ByteBuffer data)
	{
		int days=data.readUnsignedByte()+1;
		RecruitDayWelfare welfare=checkNormal(days,player);
		RecruitAward award=welfare.getHalfAward();
		if(!canGetAward(award,player))
			throw new DataAccessException(0,"have buy");
		Prop prop=(Prop)Prop.factory.newSample(award.getSid());
		int needgems=prop.getNeedGems()%((float)2)==0?prop.getNeedGems()/2
			:prop.getNeedGems()/2+1;
		needgems=needgems<=0?1:needgems;
		if(!Resources.checkGems(needgems,player.getResources()))
			throw new DataAccessException(0,"gems limit");
		Resources.reduceGems(needgems,player.getResources(),player);
		cfactory.createGemTrack(GemsTrack.RECRUIT_HALF,player.getId(),
			needgems,prop.getSid(),Resources.getGems(player.getResources()));
		RecruitKit.recordAward(award,player);
		player.getBundle().incrProp(prop,true);
		data.clear();
		data.writeByte(days-1);
	}

	/** 发放福利 */
	public void sendWelfare(RecruitAward rec_award,Player player,
		ByteBuffer data)
	{
		Award award=(Award)Award.factory.newSample(rec_award.getSid());
		data.clear();
		award.awardLenth(data,player,cfactory,null,null,
			new int[]{EquipmentTrack.FROM_WELFARE});
	}
	/** 检测是否满足任务条件 */
	public boolean checkTask(RecruitDayTask task,Player player)
	{
		int type=task.getType();
		Integer val=(Integer)player.getRecruit().getTaskMarks().get(type);
		if(val==null) return false;
		return task.checkTask(val);
	}
	/** 检测是否可领该奖励 */
	public boolean canGetAward(RecruitAward award,Player player)
	{
		if(award==null) return false;
		int[] record=player.getRecruit().getAwardMark();
		int key=award.getKey();
		int value=award.getValue();
		if(key>=record.length) return true;
		return (record[key]&(1<<value))==0;
	}
	/** 基础条件检测 */
	public RecruitDayWelfare checkNormal(int days,Player player)
	{
		if(player.getRecruit().getTimeout()<TimeKit.getSecondTime())
			throw new DataAccessException(0,"welfare over");
		if(!checkDays(days,player))
			throw new DataAccessException(0,"days limit");
		RecruitDayWelfare welfare=getWelfare(days);
		if(welfare==null)
			throw new DataAccessException(0,"no this welfare");
		return welfare;
	}
	/** 检测是否满足天数条件 */
	public boolean checkDays(int days,Player player)
	{
		return days<=player.getRecruit().getDays();
	}

	/** 获取第几天的福利 */
	public RecruitDayWelfare getWelfare(int days)
	{
		for(int i=0;i<list.size();i++)
		{
			RecruitDayWelfare welfare=(RecruitDayWelfare)list.get(i);
			if(days==welfare.getDay()) return welfare;
		}
		return null;
	}

	/** 追加福利 */
	public void addWelfare(RecruitDayWelfare newWelfare)
	{
		boolean needAdd=true;
		for(int i=0;i<list.size();i++)
		{
			RecruitDayWelfare welfare=(RecruitDayWelfare)list.get(i);
			if(newWelfare.getDay()==welfare.getDay())
			{
				list.set(newWelfare,i);
				needAdd=false;
				break;
			}
		}
		if(needAdd)
		{
			list.add(newWelfare);
			int days=newWelfare.getDay()+RecruitKit.AWARD_DAYS;
			RecruitKit.DAYS=RecruitKit.DAYS<days?days:RecruitKit.DAYS;
		}
	}

	/** 推动天数（跨天时） */
	public void pushDays()
	{
		// System.out.println("----------pushDays----00--------");
		Session[] ss=dsmanager.getSessionMap().getSessions();
		if(ss==null) return;
		// System.out.println("----------pushDays----11--------");
		for(int i=0;i<ss.length;i++)
		{
			Player player=(Player)ss[i].getSource();
			if(player==null) continue;
			RecruitKit.setDays(player);
			// todo 刷新天数
			JBackKit.sendRecruitAll(this,player);
		}
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(!SeaBackKit.isSameDay(pushTime,TimeKit.getSecondTime()))
		{
			pushTime=TimeKit.getSecondTime();
			pushDays();
		}

	}
	
	/** 显示序列化 */
	public void showBytesWrite(Player player,ByteBuffer data)
	{
		RecruitRecord record=player.getRecruit();
		if(!record.showBytesWrite(data))
		{
			int len=list.size()>record.getDays()?record.getDays():list
				.size();
			data.writeByte(len);
			for(int i=0;i<len;i++)
			{
				RecruitDayWelfare day=(RecruitDayWelfare)list.get(i);
				day.showBytesWrite(data,record,player);
			}
		}
	}
	
	/** 推送任务序列化 */
	public void sendBytesWrite(ByteBuffer data,int taskType,Player player)
	{
		RecruitRecord record=player.getRecruit();
		RecruitDayWelfare day=null;
		int d=0;
		for(int i=0;i<list.size();i++)
		{
			day=(RecruitDayWelfare)list.get(i);
			d=i+1;
			if(day.checkTaskType(taskType))break;
		}
		data.writeByte(d);
		day.sendBytesWrite(data,record);
		
	}

	public ArrayList getList()
	{
		return list;
	}

	public void setList(ArrayList list)
	{
		this.list=list;
	}

	public CreatObjectFactory getCfactory()
	{
		return cfactory;
	}

	public void setCfactory(CreatObjectFactory cfactory)
	{
		this.cfactory=cfactory;
	}

	public DSManager getDsmanager()
	{
		return dsmanager;
	}

	public void setDsmanager(DSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}

}
