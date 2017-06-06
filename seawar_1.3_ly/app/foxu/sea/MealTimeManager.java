package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;

/**
 * 饭点活动管理器
 * 
 * @author Alan
 */
public class MealTimeManager implements TimerListener
{

	private static MealTimeManager manager=new MealTimeManager();
	/** 配置时段 */
	public static String[] ENERGY_SEND_TIME;
	/** 后台是否进行消息推送 */
	public static boolean IS_PUSH;
	/** 初始化后转换的时间 */
	int[] energyTime;
	TimerEvent event;
	CreatObjectFactory objectFactory;
	/** 当前时段 */
	int[] sendEnergyTime=new int[2];

	public static MealTimeManager getInstance()
	{
		return manager;
	}

	public void init()
	{
		energyTime=new int[ENERGY_SEND_TIME.length];
		for(int i=0;i<ENERGY_SEND_TIME.length;i++)
		{
			String[] hourMinu=TextKit.split(ENERGY_SEND_TIME[i],":");
			energyTime[i]=TextKit.parseInt(hourMinu[0])*3600+TextKit.parseInt(hourMinu[1])*60;
		}
		event=new TimerEvent(this,"",60*1000);
		TimerCenter.getMinuteTimer().add(event);
	}

	@Override
	public void onTimer(TimerEvent e)
	{
//		System.out.println("---------------前时段："
//						+SeaBackKit.formatDataTime(sendEnergyTime[0])+"~"
//						+SeaBackKit.formatDataTime(sendEnergyTime[1]));
		if(setNextEnergyTime())
		{
			pushEnergyInfo();
//			if(IS_PUSH)
//			{
//				String content=InterTransltor.getInstance().getTransByKey(
//					PublicConst.SERVER_LOCALE,"meal_time_energy_push");
//				SeaBackKit.appPush(objectFactory,content,
//					PublicConst.MEAL_TIME_ENERGY_PUSH);
//			}
		}
//		System.out.println("---------------后时段："
//						+SeaBackKit.formatDataTime(sendEnergyTime[0])+"~"
//						+SeaBackKit.formatDataTime(sendEnergyTime[1]));
	}

	/** 在线玩家赠送体力推送 */
	public void pushEnergyInfo()
	{
		Session[] sessions=objectFactory.getDsmanager().getSessionMap().getSessions();
		if(sessions==null)return;
		for(int i=sessions.length-1;i>=0;i--)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=PlayerKit.getInstance().getPlayer(sessions[i]);
			if(player==null) continue;
			JBackKit.sendMealTimeInfo(player);
//			System.out.println("---------------饭时活动体力："+player.getName());
		}
	}
	
	/** 为玩家发放体力,返回发放体力数 */
	public int sendEnergy(Player player)
	{
		int energy=getMealTimeEnergy();
		player.setAttribute(PublicConst.MEAL_TIME_PUSH,TimeKit.getSecondTime()+"");
		player.addEnergy(energy);
		JBackKit.sendResetActives((Session)player.getSource(),player);
//		sendMail(player,energy);
		return energy;
	}
	
	/** 可获取能量 */
	public int getCanEnergy(Player player)
	{
		if(!checkEnergerState(player))return 0;
		return getMealTimeEnergy();
	}
	
	/**获取饭点能量值 */
	public int getMealTimeEnergy()
	{
		int energy=ContextVarManager.getInstance().getVarValue(
			ContextVarManager.MEAL_TIME_ENERGY);
		if(energy<0)
			energy=0;
		if(energy>20)energy=20;
		return energy;
	}
	
	/** 设置下一次赠送体力的时间 ,返回是否新开时段*/
	public boolean setNextEnergyTime()
	{
		int checkTime=TimeKit.getSecondTime();
		if(checkTime<sendEnergyTime[1])
			return false;
		// 本时段结束时,刷新玩家状态
		pushEnergyInfo();
		int dayStart=SeaBackKit.getSomedayBegin(0);
		// 按开始结束时间倒序判定
		for(int i=energyTime.length-1;i>0;i-=2)
		{
			// 本时段结束
			if(checkTime>=dayStart+energyTime[i])
			{
				return false;
			}
			// 本时段开始
			else if(checkTime>=dayStart+energyTime[i-1])
			{
//				System.out.println("----------checkTime:"+checkTime+":"+SeaBackKit.formatDataTime(checkTime));
				sendEnergyTime[0]=dayStart+energyTime[i-1];
				sendEnergyTime[1]=dayStart+energyTime[i];
				return true;
			}
		}
		return false;
	}
	
	/** 检测当前时段能量可领取状态 */
	public boolean checkEnergerState(Player player)
	{
		int now=TimeKit.getSecondTime();
		if(now>=sendEnergyTime[1])
			return false;
		String state=player.getAttributes(PublicConst.MEAL_TIME_PUSH);
		if(state!=null)
		{
			int getTime=TextKit.parseInt(state);
			if(getTime>=sendEnergyTime[0]&&getTime<sendEnergyTime[1])
				return false;
		}
		return true;
	}
	
	/** 玩家序列化检测赠送体力情况,返回数大于0则赠送成功(前台显示) */
	public int checkPlayerMealEnergy(Player player)
	{
		if(checkEnergerState(player))
			return sendEnergy(player);
		return 0;
	}
	
	public void sendMail(Player player,int energy)
	{
		String content=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"meal_time_send_energy");
		content=TextKit.replace(content,"%",energy+"");
		MessageKit.sendSystemMessages(player,objectFactory,content);
	}
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	public void showBytesWrite(Player player,ByteBuffer data)
	{
		data.writeByte(getMealTimeEnergy());
		data.writeBoolean(checkEnergerState(player));
	}
}
