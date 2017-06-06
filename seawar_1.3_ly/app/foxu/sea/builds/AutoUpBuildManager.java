package foxu.sea.builds;

import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.builds.produce.ResourcesProduce;
import foxu.sea.kit.JBackKit;

/**
 * �����Զ�����������
 * 
 * @author Alan
 * 
 */
public class AutoUpBuildManager implements TimerListener
{

	/** ���������� */
	BuildManager manager;
	/** ������ʱ��(key)��ŵ���Ҷ�������(value),��ʱ���� */
	private IntKeyHashMap autoBuildMap;
	/** ��ǰ�Զ����������(key)�����ڵĶ��������뼤��ʱ��(value),��ѯ�� */
	private IntKeyHashMap autoPlayerMap;
	TimerEvent autoLevelUp;
	/** ��ʱ�����뼶���� */
	public static final long MILLTIME=1000;
	/** ǿ�Ƽ����� 1���� */
	public static final int FORCE_CHECK=60;
	/** ��ʱ����һ�μ����ʱ�� */
	int nextTime;
	/** �Ƿ��ʼ����� */
	boolean isInited=false;

	public void init(BuildManager manager)
	{
		this.manager=manager;
		autoBuildMap=new IntKeyHashMap();
		autoPlayerMap=new IntKeyHashMap();
		Object[] players=manager.objectFactory.getPlayerCache()
			.getCacheMap().valueArray();
		for(int i=0;i<players.length;i++)
		{
			Player player=((PlayerSave)players[i]).getData();
			//////////////
//			player.setAutoLevelUp(true);
//			Service autoService=new Service();
//			autoService.setServiceType(PublicConst.AUTO_BUILD_BUFF);
//			autoService.setServiceTime(300);
//			player.addService(autoService,TimeKit.getSecondTime());
			//////////////
			if(player.getAutoLevelUp()&&checkAutoBuild(player))
			{
				player.getIsland().pushAll(TimeKit.getSecondTime(),
					manager.objectFactory);
				// �����Դ���ӻᴥ���Զ��������Զ���������ִ��ʱ�Ὣ��ǰ����¼�������ʱ�������
				// ��������������й���Դ����
				if(autoPlayerMap.get(player.getId())==null)
					addAutoPlayer(player);
			}
		}
		autoLevelUp=new TimerEvent(this,"auto_level_up",0,true);
		setNextTime();
		TimerCenter.getSecondTimer().add(autoLevelUp);
		isInited=true;
	}
	/** ����Զ����������Ƿ���� */
	public static boolean checkAutoBuild(Player player)
	{
		Service autoService=(Service)player
			.getServiceByType(PublicConst.AUTO_BUILD_BUFF);
		if(autoService==null||autoService.isOver(TimeKit.getSecondTime()))
		{
			return false;
		}
		return true;
	}

	/** ��ӽ����Զ�������� */
	public void addAutoPlayer(Player player)
	{
		player.setAutoLevelUp(true);
		autoLevelUp(player);
		PlayerEntry entryAfter=(PlayerEntry)autoPlayerMap
			.get(player.getId());
		if(isInited&&entryAfter.nextTime<nextTime)
			setNextTime(entryAfter.nextTime);
	}

	/** �Ƴ������Զ�������� */
	public void removeAutoPlayer(Player player)
	{
		player.setAutoLevelUp(false);
		PlayerEntry entry=(PlayerEntry)autoPlayerMap.get(player.getId());
		if(entry!=null) entry.arrayBelong.remove(player);
	}

	/** ά���Զ��������� */
	private synchronized void autoLevelUpArray()
	{
//		System.out.println("--------------ά������---������"+autoBuildMap.size()
//			+"----"+TimeKit.getMillisTime());
		ObjectArray autoPlayers=(ObjectArray)autoBuildMap.remove(nextTime);
		if(autoPlayers!=null&&autoPlayers.size()>0)
		{
//			System.out.println("��ǰ����:"+autoPlayers.size());
			while(autoPlayers.size()>0)
			{
				Player player=(Player)autoPlayers.getArray()[0];
				if(checkAutoBuild(player))
				{
					player.getIsland().pushAll(TimeKit.getSecondTime(),
						manager.objectFactory);
					// �����Դ���ӻᴥ���Զ��������Զ���������ִ��ʱ�Ὣ��ǰ��Ҵӵ�ǰʱ��������Ƴ�
					// ��������������û�н��й���Դ����
					if(autoPlayers.size()>0
						&&((Player)autoPlayers.getArray()[0]).getId()==player
							.getId())
					{
						autoLevelUp(player);
					}
				}
				else
				{
					autoPlayers.remove(player);
					autoPlayerMap.remove(player.getId());
				}
			}
		}
		setNextTime();
//		System.out.println("--------------ά�����---�´μ��"
//			+autoLevelUp.getNextTime()+"----"+TimeKit.getMillisTime());
	}

	/** �����Զ����� */
	private synchronized void autoLevelUp(Player player)
	{
		PlayerBuild[] buildArray=player.getIsland().getAutoBuildArray();
		IntList autoBuilds=new IntList();
		for(int i=0;i<buildArray.length;i++)
		{
			if(manager.isBuildDequeFull(player))
			{
				break;
			}
			String str=manager.checkbuildUpLevel(player,
				buildArray[i].getIndex());
			if(str!=null)
			{
				continue;
			}
			int index=buildArray[i].getIndex();
			manager.buildUpLevel(player,index);
			autoBuilds.add(index);
		}
		int playerNextTime=getPlayerNextTime(player);
		// �Ӿ��������Ƴ�
		PlayerEntry entry=(PlayerEntry)autoPlayerMap.get(player.getId());
		if(entry!=null)
			entry.arrayBelong.remove(player);
		else
			entry=new PlayerEntry();
		// ����������
		ObjectArray oa=(ObjectArray)autoBuildMap.get(playerNextTime);
		if(oa==null)
		{
			oa=new ObjectArray();
			autoBuildMap.put(playerNextTime,oa);
		}
		oa.add(player);
		entry.nextTime=playerNextTime;
		entry.arrayBelong=oa;
		autoPlayerMap.put(player.getId(),entry);
		JBackKit.sendBuildServiceInfo(player,autoBuilds.getArray(),
			autoBuilds.size());
	}

	/** ��ȡ��ǰ����´μ����ʱ�� */
	private int getPlayerNextTime(Player player)
	{
		int playerNextTime=0;
		// �ٴ����������������������ȷ������Ϊ���������������в�����������ռλ
		PlayerBuild[] buildArray=player.getIsland().getAutoBuildArray();
		if(!manager.isBuildDequeFull(player))
		{
			// ����δ��
			playerNextTime=TimeKit.getSecondTime()
				+ResourcesProduce.ONE_MINUTE;
			if(player.getIsland().checkNowBuildingByIndex(
				buildArray[0].index)
				&&buildArray[0].getBuildCompleteTime()<playerNextTime)
			{
				playerNextTime=buildArray[0].getBuildCompleteTime();
				// ����������ɣ�С��1����
			}
		}
		else
			playerNextTime=buildArray[0].getBuildCompleteTime();
		return playerNextTime;
	}

	/** �����̵߳���ʱ��ȫ�ӱ���������һ�μ���ʱ�� */
	private void setNextTime()
	{
		int checkTime=getNextTime();
		setNextTime(checkTime);
	}

	/** �������������Զ�����ʱ��ǿ�����ü���ʱ�� */
	private void setNextTime(int checkTime)
	{
		if(checkTime<=0) checkTime=TimeKit.getSecondTime()+FORCE_CHECK;
		nextTime=checkTime;
		long nextChceckTime=checkTime*MILLTIME;
		autoLevelUp.setNextTime(nextChceckTime);
	}

	/** ��ȡ��һ�μ���ʱ�� */
	private int getNextTime()
	{
		int length=autoBuildMap.keyArray().length;
		int nextTime=0;
//		int playerSize=0;
		IntList zeroList=new IntList();
		for(int i=0;i<length;i++)
		{
			ObjectArray oa=(ObjectArray)autoBuildMap.get(autoBuildMap
				.keyArray()[i]);
			if(oa!=null&&oa.size()>0)
			{
//				playerSize+=oa.size();
				if(nextTime==0)
				{
					nextTime=autoBuildMap.keyArray()[i];
					continue;
				}
				else if(nextTime>autoBuildMap.keyArray()[i])
				{
					nextTime=autoBuildMap.keyArray()[i];
				}
			}
			else
			{
				zeroList.add(autoBuildMap.keyArray()[i]);
			}
		}
//		System.out.println("----------------playerSize:"+playerSize);\
		for(int i=0;i<zeroList.size();i++){
			autoBuildMap.remove(zeroList.get(i));
		}
		return nextTime;
	}

	/** �Զ��������а������������� */
	public void containedPlayer2Up(Player player)
	{
		PlayerEntry entryBefore=(PlayerEntry)autoPlayerMap.get(player
			.getId());
		if(entryBefore!=null&&checkAutoBuild(player)
			&&entryBefore.arrayBelong.contain(player))
		{
			autoLevelUp(player);
			PlayerEntry entryAfter=(PlayerEntry)autoPlayerMap.get(player
				.getId());
			if(isInited&&entryAfter.nextTime<nextTime)
				setNextTime(entryAfter.nextTime);
		}
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		autoLevelUpArray();
	}

	/** �洢�´μ���ʱ����������������Ԫ�� */
	class PlayerEntry
	{

		/** ����ʱ�� */
		int nextTime;
		/** �������� */
		ObjectArray arrayBelong=new ObjectArray();
	}
}
