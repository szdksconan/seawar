package foxu.sea.webgm.opertrator;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;


public class PlayerGemsData extends GMOperator
{

	CreatObjectFactory objectFactory;
	/** 宝石消费type */
	public final String GEMS_TRACK_TYPE[]={"建筑加速","生产加速","购买能量","购买物品",
		"购买建筑队列","修复船只","刷新和直接完成每日任务","充值宝石","系统产出宝石","GM工具添加宝石","战斗事件加速",
		"邀请奖励","每日领取宝石","联盟捐献","抽奖","岛屿搬迁","环球军演加速","重置虚空","准备充值","取消充值","全服补偿"};
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		
		long time=TimeKit.getMillisTime();
		int tracks[]=new int[20];
		int year=Integer.parseInt(params.get("year"));
		int month=Integer.parseInt(params.get("month"));
		int startDay=Integer.parseInt(params.get("startDay"));
		int lastDay=Integer.parseInt(params.get("lastDay"));
		for(int i=0;i<tracks.length;i++)
		{
			String sql="SELECT SUM(gems) FROM gem_tracks WHERE type="
				+i
				+" AND year="
				+year
				+" AND month="
				+month
				+" AND day>="+startDay+" AND day<="+lastDay;
			tracks[i]=SeaBackKit.loadBySqlOneData(sql,objectFactory
				.getGemsTrackMemCache().getDbaccess());
		}
		String name=null;
		long checkTime=(TimeKit.getMillisTime()-time);
		for(int i=0;i<tracks.length;i++)
		{
			for(int j=0;j<GEMS_TRACK_TYPE.length;j++)
			{
				if(i==j)
				{
					name=GEMS_TRACK_TYPE[i];
					break;
				}
			}
		}
		return 0;
	}

}
