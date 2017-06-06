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
 * ����������
 * 
 * @author Alan
 */
public class MealTimeManager implements TimerListener
{

	private static MealTimeManager manager=new MealTimeManager();
	/** ����ʱ�� */
	public static String[] ENERGY_SEND_TIME;
	/** ��̨�Ƿ������Ϣ���� */
	public static boolean IS_PUSH;
	/** ��ʼ����ת����ʱ�� */
	int[] energyTime;
	TimerEvent event;
	CreatObjectFactory objectFactory;
	/** ��ǰʱ�� */
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
//		System.out.println("---------------ǰʱ�Σ�"
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
//		System.out.println("---------------��ʱ�Σ�"
//						+SeaBackKit.formatDataTime(sendEnergyTime[0])+"~"
//						+SeaBackKit.formatDataTime(sendEnergyTime[1]));
	}

	/** ������������������� */
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
//			System.out.println("---------------��ʱ�������"+player.getName());
		}
	}
	
	/** Ϊ��ҷ�������,���ط��������� */
	public int sendEnergy(Player player)
	{
		int energy=getMealTimeEnergy();
		player.setAttribute(PublicConst.MEAL_TIME_PUSH,TimeKit.getSecondTime()+"");
		player.addEnergy(energy);
		JBackKit.sendResetActives((Session)player.getSource(),player);
//		sendMail(player,energy);
		return energy;
	}
	
	/** �ɻ�ȡ���� */
	public int getCanEnergy(Player player)
	{
		if(!checkEnergerState(player))return 0;
		return getMealTimeEnergy();
	}
	
	/**��ȡ��������ֵ */
	public int getMealTimeEnergy()
	{
		int energy=ContextVarManager.getInstance().getVarValue(
			ContextVarManager.MEAL_TIME_ENERGY);
		if(energy<0)
			energy=0;
		if(energy>20)energy=20;
		return energy;
	}
	
	/** ������һ������������ʱ�� ,�����Ƿ��¿�ʱ��*/
	public boolean setNextEnergyTime()
	{
		int checkTime=TimeKit.getSecondTime();
		if(checkTime<sendEnergyTime[1])
			return false;
		// ��ʱ�ν���ʱ,ˢ�����״̬
		pushEnergyInfo();
		int dayStart=SeaBackKit.getSomedayBegin(0);
		// ����ʼ����ʱ�䵹���ж�
		for(int i=energyTime.length-1;i>0;i-=2)
		{
			// ��ʱ�ν���
			if(checkTime>=dayStart+energyTime[i])
			{
				return false;
			}
			// ��ʱ�ο�ʼ
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
	
	/** ��⵱ǰʱ����������ȡ״̬ */
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
	
	/** ������л���������������,����������0�����ͳɹ�(ǰ̨��ʾ) */
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
