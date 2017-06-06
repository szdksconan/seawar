package foxu.sea.achieve;

import shelby.ds.DSManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.LoginLogMemCache;
import foxu.sea.Player;
import foxu.sea.User;
import foxu.sea.award.Award;
import foxu.sea.builds.Build;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import mustang.event.ChangeAdapter;
import mustang.io.ByteBuffer;
import mustang.net.Session;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.Sample;
import mustang.util.TimeKit;

/**
 * 成就管理器
 * 
 * @author yw
 * 
 */
public class AchieveManager extends ChangeAdapter implements TimerListener
{

	public static AchieveManager instance;
	/** 属性key-成就sid */
	IntKeyHashMap key_sid=new IntKeyHashMap();

	CreatObjectFactory objectFactory;
	/** 连续登陆刷新时间 */
	int flushTime;
	/** 成就总积分 */
	int maxScore;
	
	DSManager dsManager;
	

	public void init()
	{
		instance=this;
		Sample[] samples=Achievement.factory.getSamples();
		Achievement achieve=null;
		IntList sid_list=null;
		for(int i=samples.length-1;i>=0;i--)
		{
			if(samples[i]==null) continue;
			achieve=(Achievement)samples[i];
			sid_list=(IntList)key_sid.get(achieve.atrKey);
			if(sid_list==null)
			{
				sid_list=new IntList();
				sid_list.add(achieve.getSid());
				key_sid.put(achieve.atrKey,sid_list);
			}
			else
			{
				sid_list.add(achieve.getSid());
			}
			maxScore+=achieve.getMaxScore();

		}
		flushTime=TimeKit.getSecondTime();
		// 开启定时器
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"flushlogin",60*1000));
	}
	
	/** 推算过去已达成成就 */
	public void pushOldAchieve(Player player)
	{
		AchieveCollect.buildLevel(
			player.getIsland().getBuildByType(Build.BUILD_DIRECTOR,null),
			player.getIsland(),player);
		AchieveCollect.buildLevel(
			player.getIsland().getBuildByType(Build.BUILD_SHIP,null),
			player.getIsland(),player);
		AchieveCollect.buildLevel(
			player.getIsland().getBuildByType(Build.BUILD_RESEARCH,null),
			player.getIsland(),player);
		AchieveCollect.honorLevel(player.getHonor()[Player.HONOR_LEVEL_INDEX],player);
		AchieveCollect.resourceStock(player);
		AchieveCollect.chapterLevel(player);
		User user=objectFactory.getUserDBAccess().load(player.getUser_id()+"");
		if(user.getUserType()==User.USER)
		{
			AchieveCollect.bindUser(player);
		}
		AchieveCollect.playerLevel(player);
		AchieveCollect.commandLevel(player);
		//邀请玩家
//		int[] iviteId=player.getInviter_id();
//		int decr=SeaBackKit.isContainValue(iviteId,player.getId())?1:0;
//		player.getAchieveData().getKey_attr().put(Achievement.INVITE,new Long(iviteId.length/2-decr));
		
		
	}

	/**
	 * 推动成就属性值
	 * 
	 * @param key
	 * @param addValue
	 * @param player
	 */
	public void pushAchieveValue(int key,long addValue,Player player)
	{
		if(addValue<=0) return;
		boolean flush=player.changeAchieveValue(key,addValue);
		if(!flush) return;
		IntList sid_list=(IntList)key_sid.get(key);
		if(sid_list==null||sid_list.size()<=0) return;
		for(int i=0;i<sid_list.size();i++)
		{
//			System.out.println(key+"-----pushAchieveValue----i------:::"+i);
			int sid=sid_list.get(i);
			checkHead(player, sid);
			long cvalue=player.getAchieveValue(key);
			JBackKit.sendFlushAchieve(player,sid,cvalue,false);
		}

	}
	
	
	/**
	 * 验证成就是否完成，获得头像
	 * 
	 * @param player
	 * @param sid
	 */
	private void checkHead(Player player, int sid) {
		Achievement achievement = (Achievement) Achievement.factory.getSample(sid);
		if (achievement != null) {
			int progress = player.getAchieveProgress(sid);
			for (int i = 0, len = achievement.getNeedValue().length; i < len; i++) {
				int nvalue = achievement.getNeedValue()[i];
				long cvalue = player.getAchieveValue(achievement.getAtr_key());
				if (cvalue >= nvalue && i <= progress) {
					if (achievement.baseType == Achievement.OTHER) {// 特殊天赋需要自动进入下一阶段
						player.addAchieveProgress(sid, len);
						progress = player.getAchieveProgress(sid);
					}
					int headSid = achievement.getHeadSid(i);
					if (headSid > 0 && !player.isHeadEnabled(headSid)) {
						player.updateHeadInfo(headSid, true, false);
						// 发送更新消息
						JBackKit.sendEnabledHead(player, headSid);
					}
				}
			}
		}
	}
	
	
	

	/**
	 * 获取成就奖励
	 * 
	 * @param data
	 * @param player
	 */
	public String getAchieveAward(ByteBuffer data,Player player)
	{
		int sid=data.readUnsignedShort();
//		System.out.println("===========Award================sid:"+sid);
		data.clear();
		Achievement achieve=(Achievement)Achievement.factory.getSample(sid);
		if(achieve==null) return "no this achieve";
		int cprogress=player.getAchieveProgress(sid);
		if(cprogress>=achieve.getNeedValue().length) return "max progress";
		int nvalue=achieve.getNeedValue()[cprogress];
		long cvalue=player.getAchieveValue(achieve.atrKey);
		if(cvalue<nvalue) return "cvalue limit";
		int asid=achieve.getAwardSid(cprogress);
		((Award)Award.factory.getSample(asid)).awardLenth(data,player,
			objectFactory,null,null,new int[]{EquipmentTrack.FROM_ACHIEVE});
		player.addAchieveProgress(sid,achieve.getNeedValue().length);
		if(achieve.awardClear)
		{
			player.clearAchieveAtr(achieve.atrKey);
			cvalue=0;
		}
		checkHead(player, sid);
		JBackKit.sendFlushAchieve(player,sid,cvalue,true);
		return null;

	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		if(!SeaBackKit.isSameDay(TimeKit.getSecondTime(),flushTime))
		{
			flushTime=TimeKit.getSecondTime();
			Session[] sessions=dsManager.getSessionMap().getSessions();
			for(int i=sessions.length-1;i>=0;i--)
			{
				if(sessions[i]==null||sessions[i].getSource()==null
					||sessions[i].getConnect()==null
					||!sessions[i].getConnect().isActive()) continue;
				Player player=(Player)sessions[i].getSource();
				player.addLoginDays();
				LoginLogMemCache.loginLogMem.flushLoginLog(player,objectFactory);
			}
		}

	}
	
	public int getMaxScore()
	{
		return maxScore;
	}

	@Override
	public void change(Object source,int type)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int v1,int v2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int v1,int v2,int v3)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,Object value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,Object v1,Object v2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,Object v1,Object v2,Object v3)
	{
		// TODO Auto-generated method stub
		
	}

	
	public void setDsManager(DSManager dsManager)
	{
		this.dsManager=dsManager;
	}
	

}
