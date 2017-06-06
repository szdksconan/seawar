package foxu.sea.recruit;

import java.util.Calendar;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.Build;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/**
 * �±���������
 * 
 * @author yw
 * 
 */
public class RecruitKit
{

	/** �콱ʱ�� */
	public static int AWARD_DAYS=2;
	/** ��ʱ�� */
	public static int DAYS;
	
	public static RecruitWelfareManager rmanager;

	/** �趨������ʱʱ��(����ʱ) */
	public static void setWelfareOut(Player player)
	{
		RecruitRecord record=player.getRecruit();
		Calendar now=Calendar.getInstance();
		now.setTimeInMillis(player.getCreateTime()*1000l);
		int decr_sec=now.get(Calendar.HOUR_OF_DAY)*PublicConst.HOUR_SEC
			+now.get(Calendar.MINUTE)*PublicConst.MIN_SEC
			+now.get(Calendar.SECOND);
		record.setTimeout(player.getCreateTime()-decr_sec+DAYS
			*PublicConst.DAY_SEC);
		record.setDays(1);
	}
	/** ������������½ʱ�� */
	public static void setDays(Player player)
	{
		RecruitRecord record=player.getRecruit();
		if(record.getTimeout()<TimeKit.getSecondTime()) return;
		record.setDays(SeaBackKit.getDdays(player.getCreateTime(),
			TimeKit.getSecondTime())+1);
		//����
//		record.setDays((TimeKit.getSecondTime()-player.getCreateTime())/(5*60)+1);
	}

	/** �ƶ�����(������ȱ仯ʱ) */
	public static void pushTask(int taskType,int value,Player player,
		boolean max)
	{
		if(value<=0) return;
		RecruitRecord record=player.getRecruit();
		if(record.getTimeout()-RecruitKit.AWARD_DAYS*PublicConst.DAY_SEC<TimeKit
			.getSecondTime()) return;
		IntKeyHashMap mark=record.getTaskMarks();
		Integer val=(Integer)mark.get(taskType);
		if(val==null)
		{
			mark.put(taskType,value);
			JBackKit.sendRecruit(taskType,player);
		}
		else
		{
			if((max&&val<value)||(!max&&val>value))
			{
				mark.put(taskType,value);
				JBackKit.sendRecruit(taskType,player);
			}
		}
	}
	
	// ˢ���������
	public static void sendTask(ByteBuffer data,int taskType,Player player)
	{
		rmanager.sendBytesWrite(data,taskType,player);
	}

	/** �ƶ�����(������ȱ仯ʱ) */
	public static void pushTaskBuild(Build build,Player player)
	{
		int btype=build.getBuildType();
		if(btype==Build.BUILD_SHIP)
		{
			pushTask(RecruitDayTask.DOCK_LV,build.getBuildLevel(),player,
				true);
			JBackKit.sendRecruit(RecruitDayTask.DOCK_LV,player);
		}
		else if(btype==Build.BUILD_METAL||btype==Build.BUILD_OIL
			||btype==Build.BUILD_SILION||btype==Build.BUILD_URANIUM
			||btype==Build.BUILD_MONEY)
		{
			pushTask(RecruitDayTask.RES_BUILD_LV,build.getBuildLevel(),
				player,true);
			JBackKit.sendRecruit(RecruitDayTask.RES_BUILD_LV,player);
		}
	}

	/** ��¼���� */
	public static void recordAward(RecruitAward raward,Player player)
	{
		RecruitRecord record=player.getRecruit();
		int[] awardR=record.getAwardMark();
		int key=raward.getKey();
		int value=raward.getValue();
		if(key>=awardR.length)
		{
			int[] temp=new int[key+1];
			System.arraycopy(awardR,0,temp,0,awardR.length);
			awardR=temp;
		}
		awardR[key]=awardR[key]|(1<<value);
		record.setAwardMark(awardR);
	}
}
