package foxu.cross.goalleague;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cross.goalleague.persistent.CrossLeaguePlayerDBAccess;
import foxu.cross.war.CrossWarPlayer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.ContextVarManager;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.alliance.Alliance;
import foxu.sea.arena.ArenaManager;
import foxu.sea.arena.SeawarGladiator;
import foxu.sea.award.Award;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.port.UserToCenterPort;
import foxu.sea.proplist.Prop;
import foxu.sea.task.TaskEventExecute;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.MathKit;
import mustang.set.ArrayList;
import mustang.set.Comparator;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * 客户端联赛管理器
 * 
 * @author Alan
 * 
 */
public class ClientLeagueManager implements TimerListener,Comparator
{

	public static final int LEAGUE_OPEN=1,LEAGUE_SETTLE_READY=2,
					LEAGUE_SETTLE=3,LEAGUE_CLOSE=4,LEAGUE_CHALLENGE_LIST=5,
					LEAGUE_SHOP_LIST=6,LEAGUE_RANK_LIST=7;
	private static final Logger log=LogFactory
		.getLogger(ClientLeagueManager.class);
	/** 系统开启状态 */
	boolean isOpen;
	/** 参与玩家列表 */
	IntKeyHashMap playerlist=new IntKeyHashMap();
	/** 积分榜 */
	ArrayList rankList=new ArrayList();
	/** 当前挑战列表 */
	ArrayList currentCopyList=new ArrayList();
	/** 预存挑战列表 */
	ArrayList readyCopyList=new ArrayList();
	/** 商店物品列表 */
	ArrayList sellList=new ArrayList();
	/** 系统数据准备状态 */
	boolean isReady;
	/** 战斗积分系统是否可用 */
	boolean isWarActive;
	/** 初始化标识 */
	boolean isInited;
	/** 赛季结算验证 */
	boolean isSettleChecked;
	/** 上一次参与玩家刷新时间 */
	int lastFlushTime;
	/** 下次开启时间 */
	int nextOpenTime;
	/** 本次结算时间 */
	int currentSettleTime;
	/** 联赛id */
	int leagueId;
	/** 每次存库的最大条数 */
	int max=1000;
	String centerIP;
	String centerPort;
	String localServerName;
	TimerEvent te=new TimerEvent(this,"day_flush",1000);
	TimerEvent saveTimer=new TimerEvent(this,"player_save",60*15*1000);
	CrossLeaguePlayerDBAccess cpDBAccess;

	ArenaManager arenaManager;
	CreatObjectFactory objectFactory;

	public void init()
	{
		String centerInfo=ContextVarManager.getInstance().getVarDest(
			ContextVarManager.CROSS_LEAGUE_CLIENT_INFO);
		if(centerInfo!=null&&!"".equals(centerInfo))
		{
			String[] infos=TextKit.split(centerInfo,",");
			centerIP=infos[0];
			centerPort=infos[1];
			localServerName=infos[2];
		}
		// 加载参与玩家数据
		LeaguePlayer[] players=cpDBAccess.loadBySqlAll();
		if(players!=null) for(int i=0;i<players.length;i++)
		{
			playerlist.put(players[i].getId(),players[i]);
		}
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"get_league",10*1000,10));
		TimerCenter.getSecondTimer().add(te);
		TimerCenter.getMinuteTimer().add(saveTimer);
	}

	/** 与中心同步数据 */
	public synchronized void synchronizedLeague()
	{
		// 与中心同步数据
		if(centerIP!=null&&centerPort!=null)
		{
			ByteBuffer data=new ByteBuffer();
			data.writeByte(ServerLeagueManager.LEAGUE_INFO);
			data=sendHttpData(data,centerIP,centerPort);
			initLeague(data);
		}
	}

	/** 初始化联赛数据 */
	public synchronized void initLeague(ByteBuffer data)
	{
		isOpen=data.readBoolean();
		if(!isOpen) return;
		bytesReadLeagueOpen(data);
		isReady=data.readBoolean();
		if(isReady) readyCopyList=bytesReadTargetList(data);
		isWarActive=data.readBoolean();
		if(isWarActive) currentCopyList=bytesReadTargetList(data);
		rankList=bytesReadRankList(data);
		isInited=true;
		log.info("init league complete, isOpen:"+isOpen+", isReady:"+isReady
			+", isWarActive:"+isWarActive+", currentCopyList's size:"
			+currentCopyList.size());
	}

	public void bytesReadLeagueOpen(ByteBuffer data)
	{
		leagueId=data.readInt();
		currentSettleTime=data.readInt();
		nextOpenTime=data.readInt();
		int len=data.readUnsignedByte();
		sellList.clear();
		LeagueShopProp lsp=null;
		for(int i=0;i<len;i++)
		{
			lsp=ClientLeagueManager.bytesRead2LeagueShopProp(data);
			sellList.add(lsp);
		}
	}

	public ArrayList bytesReadTargetList(ByteBuffer data)
	{
		int len=data.readInt();
		ArrayList list=new ArrayList();
		CrossWarPlayer cwp=null;
		for(int i=0;i<len;i++)
		{
			cwp=ClientLeagueManager.bytesRead2CrossWarPlayer(data);
			list.add(cwp);
		}
		return list;
	}

	public ArrayList bytesReadRankList(ByteBuffer data)
	{
		int len=data.readInt();
		ArrayList list=new ArrayList();
		CrossWarPlayer cp=null;
		for(int i=0;i<len;i++)
		{
			cp=ClientLeagueManager.showBytesRead2LeaguePlayer(data);
			cp.setRank(i+1);
			list.add(cp);
		}
		return list;
	}

	public boolean open(int leagueId,int currentSettleTime,int nextOpenTime,
		ArrayList shopList)
	{
		clearPlayerInfo();
		if(!isReady)
		{
			isInited=false;
			// 如果准备数据没有成功,重新进行同步
			TimerCenter.getSecondTimer().add(
				new TimerEvent(this,"get_league",10*1000,10));
			log.error("copy not ready when open, invoke method later...");
			return false;
		}
		this.leagueId=leagueId;
		this.currentSettleTime=currentSettleTime;
		this.nextOpenTime=nextOpenTime;
		rankList.clear();
		currentCopyList.clear();
		currentCopyList=readyCopyList;
		sellList.clear();
		sellList=shopList;
		isReady=false;
		isWarActive=true;
		isSettleChecked=false;
		isOpen=true;
		sendLeaguaStaut(LEAGUE_OPEN);
		log.info("league open, currentCopyList size:"+currentCopyList.size()
			+"...");
		return true;
	}

	/** 发送联赛状态 */
	public void sendLeaguaStaut(int staut)
	{
		int time=TimeKit.getSecondTime();
		log.info("show info to players, staut:"+staut);
		Player player=null;
		int[] keys=playerlist.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			player=objectFactory.getPlayerById(keys[i]);
			switch(staut)
			{
				case LEAGUE_RANK_LIST:
					JBackKit.sendCrossLeaguePlayerInfo(objectFactory,player,
						this,time);
					break;
				case LEAGUE_SETTLE_READY:
					JBackKit.sendCrossLeaguePlayerInfo(objectFactory,player,
						this,time);
					break;
				case LEAGUE_CHALLENGE_LIST:
					JBackKit.sendCrossLeagueChallengeList(player,this);
					break;
				case LEAGUE_SHOP_LIST:
					JBackKit.sendCrossLeagueShopInfo(player,this,time);
					break;
				default:
					JBackKit.sendCrossLeagueWholeInfo(objectFactory,player,
						this,time);
					break;
			}
		}
	}

	/** 重置预存挑战列表 */
	public void resetReadyCopyList(ArrayList list)
	{
		readyCopyList=list;
		log.info("readyCopyList reset, size:"+list.size());
		isReady=true;
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		int now=(int)(e.getCurrentTime()/1000);
		if("get_league".equals(e.getParameter()))
		{
			try
			{
				if(!isInited) synchronizedLeague();
				TimerCenter.getSecondTimer().remove(e);
				isInited=true;
			}
			catch(Exception e2)
			{
				log.error("cross league synchronize fail ",e2);
			}
			return;
		}
		if("player_save".equals(e.getParameter()))
		{
			log.info("start to save league player, size:"+playerlist.size());
			savePlayer(false);
			log.info("save league player complete");
			return;
		}
		if(now>=currentSettleTime&&!isSettleChecked)
		{
			checkSettleStep();
		}
		if(SeaBackKit.isSameDay(now,lastFlushTime)) return;
		// 如果跨天进行重置
		synchronized(playerlist)
		{
			int[] keys=playerlist.keyArray();
			Player lp=null;
			for(int i=0;i<keys.length;i++)
			{
				lp=objectFactory.getPlayerById(keys[i]);
				checkLeaguePlayerInNewDay(now,lp);
				JBackKit
					.sendCrossLeaguePlayerInfo(objectFactory,lp,this,now);
				JBackKit.sendCrossLeagueShopInfo(lp,this,now);
			}
		}
		lastFlushTime=now;
	}

	/** 结算时向中心请求校验 */
	public void checkSettleStep()
	{
		if(centerIP==null||centerPort==null) return;
		ByteBuffer data=new ByteBuffer();
		data.writeByte(ServerLeagueManager.LEAGUE_STAUT_SETTLE);
		data.writeInt(leagueId);
		data=sendHttpData(data,centerIP,centerPort);
		// 先将战斗积分功能停用
		isWarActive=false;
		if(data==null)
			log.error("connect to league center:"+centerIP+","+centerPort
				+" fail when settled");
		// 如果中心校验不通过,同步数据
		if(!data.readBoolean()) initLeague(data);
		if(!isWarActive) isSettleChecked=true;
	}

	/** 收集整合挑战目标信息 */
	public ArrayList collectCopyInfo(int lv)
	{
		ArrayList list=new ArrayList();
		IntKeyHashMap map=arenaManager.getSortedMap();
		int[] keys=map.keyArray();
		SeawarGladiator gladiator=null;
		IntList shipList=null;
		Player player=null;
		for(int i=0;i<keys.length;i++)
		{
			gladiator=(SeawarGladiator)map.get(keys[i]);
			if(gladiator==null) continue;
			player=objectFactory.getPlayerById(gladiator.getPlayerId());
			if(player==null||player.getLevel()<lv) continue;
			shipList=new IntList();
			for(int j=0;j<SeawarGladiator.FLEET_MAX_COUNT;j++)
			{
				int sid=gladiator.getShipSidByIndex(j);
				int count=gladiator.getShipCountByIndex(j);
				if(sid>0&&count>0)
				{
					shipList.add(sid);
					shipList.add(count);
					shipList.add(j);
				}

			}
			if(shipList.size()<=0) continue;
			list.add(createLeaguePlayer(player,shipList));
		}
		return list;
	}

	/** 跨服竞技场副本序列化 */
	public LeaguePlayer createLeaguePlayer(Player player,IntList shipList)
	{
		LeaguePlayer cp=new LeaguePlayer();
		cp.setId(player.getId());
		cp.setAttacklist(shipList);
		cp.setOfs(player.getOfficers().getUsingOfficers());
		String allid=player.getAttributes(PublicConst.ALLIANCE_ID);
		int alid=allid==null?0:TextKit.parseInt(allid);
		Alliance alliance=objectFactory.getAlliance(alid,false);
		String aname=alliance==null?" ":alliance.getName();
		// 头像
		cp.setHeadPic(player.getAttrHead());
		cp.setHeadFrame(player.getAttrHeadBorder());
		cp.setSid(player.getSid());
		cp.setPlatid(UserToCenterPort.PLAT_ID);
		cp.setAreaid(UserToCenterPort.AREA_ID);
		cp.setSeverid(UserToCenterPort.SERVER_ID);
		cp.setAname(aname);
		cp.setName(player.getName());
		cp.setLevel(player.getLevel());
		ByteBuffer data=new ByteBuffer();
		player.crossWriteAdjustment(data);
		cp.bytesReadAdjustment(data);
		cp.setShipLevel(player.getShipLevel());
		// 战力计算需要将船只列表变为2位1组,不要坑位信息
		IntList tempList=new IntList();
		for(int i=0;i<cp.getAttacklist().size();i+=3)
		{
			tempList.add(cp.getAttacklist().get(i));
			tempList.add(cp.getAttacklist().get(i+1));
		}
		cp.setFightscore(SeaBackKit.calculateFightScore(player,
			objectFactory,tempList,0,false,null,false));
		return cp;
	}

	/** 刷新当前挑战列表 */
	public String refreshCurrentChallengeList(Player challenger,int count,
		int time)
	{
		if(!isWarActive) return "league not active";
		LeaguePlayer lp=getLeaguePlayer(challenger);
		if(lp.getLeagueId()!=leagueId) return "current league not match";
		if(lp.getCurrentFlushCount()!=count)
			return "league flush count error";
		if(lp.getCurrentFlushCount()>=ServerLeagueManager.BATTLE_FLUSH_COUNT)
		{
			if(!Resources.checkGems(ServerLeagueManager.BATTLE_FLUSH_GEMS,
				challenger.getResources()))
				return "not enough gems";
			else
			{
				Resources.reduceGems(ServerLeagueManager.BATTLE_FLUSH_GEMS,
					challenger.getResources(),challenger);
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,challenger,
					ServerLeagueManager.BATTLE_FLUSH_GEMS);
				objectFactory.createGemTrack(GemsTrack.LEAGUE_FLUSH_COUNT,
					challenger.getId(),
					ServerLeagueManager.BATTLE_FLUSH_GEMS,
					lp.getCurrentBattleCount(),
					Resources.getGems(challenger.getResources()));
			}
		}
		lp.incrTodayFlushCount();
		if(resetCurrentChallengeList(challenger))
			resetCurrentChallengeList(challenger);
		lp.setLastActiveTime(time);
		return null;
	}

	/** 购买可战斗次数 */
	public String buyBattleCount(int count,Player challenger,int time)
	{
		LeaguePlayer lp=getLeaguePlayer(challenger);
		if(lp.getLeagueId()!=leagueId) return "current league not match";
		if(lp.getCurrentBattleLimit()!=count)
			return "league battle limit count error";
		int limit=lp.getCurrentBattleLimit()+1
			-ServerLeagueManager.BATTLE_INTERAL_COUNT;
		int[] array=ServerLeagueManager.BATTLE_FIGHT_GEMS;
		int gems=array[array.length-1];
		for(int i=array.length-1;i>=0;i-=2)
		{
			int ct=array[i-1];
			if(limit>=ct)
			{
				gems=array[i];
				break;
			}
		}
		if(!Resources.checkGems(gems,challenger.getResources()))
			return "not enough gems";
		Resources.reduceGems(gems,challenger.getResources(),challenger);
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.GEMS_ADD_SOMETHING,this,challenger,gems);
		objectFactory.createGemTrack(GemsTrack.LEAGUE_BATTLE_COUNT,
			challenger.getId(),gems,lp.getCurrentBattleCount(),
			Resources.getGems(challenger.getResources()));
		lp.incrTodayBattleCountLimit();
		lp.setLastActiveTime(time);
		return null;
	}

	/** 获取玩家当前挑战列表(目标id,挑战状态...) */
	public IntList getCurrentChallengeList(Player challenger)
	{
		LeaguePlayer lp=(LeaguePlayer)playerlist.get(challenger.getId());
		if(lp==null||!isWarActive) return null;
		if(isNeedResetChallengeList(lp))
		{
			if(resetCurrentChallengeList(challenger))
				resetCurrentChallengeList(challenger);
		}
		return lp.getCurrentChallengeList();
	}

	/** 是否需要重置玩家当前挑战列表 */
	public boolean isNeedResetChallengeList(LeaguePlayer lp)
	{
		if(!isWarActive) return false;
		if(lp==null) return false;
		if(lp.getLeagueId()!=leagueId) return true;
		for(int i=0;i<lp.getCurrentChallengeList().size();i+=2)
		{
			if(lp.getCurrentChallengeList().get(i+1)==LeaguePlayer.CHALLENGE_FAIL)
				return false;
		}
		return true;
	}

	/** 当前跨服镜像是否是自己 */
	public boolean isCrossWarPlayerSelf(Player challenger,CrossWarPlayer cp)
	{
		if(cp.getAreaid()==UserToCenterPort.AREA_ID
			&&cp.getPlatid()==UserToCenterPort.PLAT_ID
			&&cp.getSeverid()==UserToCenterPort.SERVER_ID
			&&cp.getId()==challenger.getId()) return true;
		return false;
	}

	/** 重置玩家当前挑战列表,返回是否需要重新进行挑战列表重置(某一区间数量用尽) */
	public boolean resetCurrentChallengeList(Player challenger)
	{
		CrossWarPlayer cwp=null;
		LeaguePlayer lp=(LeaguePlayer)playerlist.get(challenger.getId());
		int[] range=ServerLeagueManager.SELECT_FIGHT_SCORE_RANGE;
		int circle=2;
		int[] ids=new int[range.length/circle];
		int fs=challenger.getFightScore();
		// 当前范围的镜像
		ArrayList rangeList=new ArrayList();
		int[] info=ServerLeagueManager.COPY_RANGE_INFO;
		int size=currentCopyList.size();
		// 填充玩家挑战目标信息
		for(int i=0;i<range.length;i+=circle)
		{
			float minRange=(float)range[i]*fs/100;
			float maxRange=(float)range[i+1]*fs/100;
			for(int j=0;j<currentCopyList.size();j++)
			{
				cwp=(CrossWarPlayer)currentCopyList.get(j);
				if(cwp==null) continue;
				if(cwp.getFightscore()>maxRange) break;
				if(cwp.getFightscore()<minRange) continue;
				if(!rangeList.contain(cwp)
					&&!isCrossWarPlayerSelf(challenger,cwp))
					rangeList.add(cwp);
			}
			// 起始索引
			int baseStart=info[i/circle*3]*size/100;
			// 截止索引
			int baseEnd=info[i/circle*3+1]*size/100;
			// 保底数量
			int baseCount=info[i/circle*3+2];
			for(int k=baseStart;k<baseEnd;k++)
			{
				cwp=(CrossWarPlayer)currentCopyList.get(k);
				if(cwp==null||rangeList.contain(cwp)
					||isCrossWarPlayerSelf(challenger,cwp)) continue;
				rangeList.add(cwp);
				if(rangeList.size()>=baseCount) break;
			}
		}
		Object[] objs=rangeList.toArray();
		SetKit.sort(objs,this);
		rangeList=new ArrayList(objs);
		size=rangeList.size();
		IntList currentTemp=new IntList();
		// 玩家可挑战总列表分段
		for(int j=0;j<info.length;j+=3)
		{
			// 起始索引
			int baseStart=info[j]*size/100;
			// 截止索引
			int baseEnd=info[j+1]*size/100;
			int index=-1;
			// 当前区间满足条件的镜像数量
			int count=0;
			// 找到第一个满足的镜像
			for(int k=baseStart;k<baseEnd;k++)
			{
				cwp=(CrossWarPlayer)rangeList.get(k);
				if(lp.getChallengeList().contain(cwp.getCrossid()))
					continue;
				if(index<0) index=k;
				count++;
			}
			// 如果已使用数量超过限制
			if(count*100<(100-ServerLeagueManager.COPY_RANGE_RESET_PERCENT)
				*(baseEnd-baseStart))
			{
				log.info("player:"+challenger.getName()
					+" reset challenge record, currentCopyList's size:"
					+currentCopyList.size());
				lp.resetChallengeRecord();
				return true;
			}
			// 抽取单个镜像
			int random=MathKit.randomValue(index,baseEnd);
			cwp=(CrossWarPlayer)rangeList.get(random);
			if(lp.getChallengeList().contain(cwp.getCrossid()))
			{
				for(int k=random;k<baseEnd;k++)
				{
					cwp=(CrossWarPlayer)rangeList.get(k);
					if(!lp.getChallengeList().contain(cwp.getCrossid()))
						break;
					cwp=null;
				}
				if(cwp==null) cwp=(CrossWarPlayer)rangeList.get(random);
			}
			ids[j/3]=cwp.getCrossid();
			currentTemp.add(ids[j/3]);
		}
		// 可能遇到区间极限问题造成玩家挑战列表战力顺序不正确，重新排序
		for(int k=0;k<ids.length;k++)
		{
			for(int k2=k+1;k2<ids.length;k2++)
			{
				if(ids[k]>ids[k2])
				{
					int temp=ids[k];
					ids[k]=ids[k2];
					ids[k2]=temp;
				}
			}
		}
		lp.resetCurrentChallengeList(ids,challenger);
		log.info("player:"+challenger.getName()+" challenge list:"
			+Arrays.toString(lp.getCurrentChallengeList().toArray()));
		return false;
	}

	/** 获取挑战目标 */
	public CrossWarPlayer getCrossWarPlayer(int id)
	{
		CrossWarPlayer cwp=null;
		for(int i=0;i<currentCopyList.size();i++)
		{
			cwp=(CrossWarPlayer)currentCopyList.get(i);
			if(cwp!=null&&cwp.getCrossid()==id) return cwp;
		}
		return null;
	}

	/** 获取挑战挑战者信息 */
	public LeaguePlayer getLeaguePlayer(Player player)
	{
		return (LeaguePlayer)playerlist.get(player.getId());
	}

	/** 获取挑战挑战目标档位(表示战力递增,从0开始) */
	public int getChallegeTargetIndex(int targetId,Player player)
	{
		LeaguePlayer lp=getLeaguePlayer(player);
		return lp.getTargetIndex(targetId);
	}

	public ArrayList getRankList()
	{
		return rankList;
	}

	public void setRankList(ArrayList rankList)
	{
		this.rankList=rankList;
	}

	public ArrayList getSellList()
	{
		return sellList;
	}

	public void setSellList(ArrayList sellList)
	{
		this.sellList=sellList;
	}

	/** 搜集挑战玩家信息 */
	public ArrayList collectCrossWarPlayer()
	{
		LeaguePlayer lp=null;
		Player player=null;
		ArrayList list=new ArrayList();
		int[] keys=playerlist.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			lp=(LeaguePlayer)playerlist.get(keys[i]);
			if(lp==null||lp.getGoal()<=0||lp.getLeagueId()!=leagueId)
				continue;
			player=objectFactory.getPlayerById(keys[i]);
			if(player==null) continue;
			lp.setAreaid(UserToCenterPort.AREA_ID);
			lp.setPlatid(UserToCenterPort.PLAT_ID);
			lp.setSeverid(UserToCenterPort.SERVER_ID);
			lp.setHeadPic(player.getAttrHead());
			lp.setHeadFrame(player.getAttrHeadBorder());
			list.add(lp);
		}
		log.info("collect local league player, size:"+list.size());
		return list;
	}

	/** 重置排行榜 */
	public void resetRankList(ArrayList list)
	{
		rankList.clear();
		rankList=list;
		sendLeaguaStaut(LEAGUE_RANK_LIST);
	}

	/** 清除参与玩家信息,代币不清空 */
	public void clearPlayerInfo()
	{
		LeaguePlayer lp=null;
		int[] keys=playerlist.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			lp=(LeaguePlayer)playerlist.get(keys[i]);
			// 清除积分
			lp.setGoal(0);
			lp.getCurrentList().clear();
			lp.getChallengeList().clear();
			lp.getPropList().clear();
			lp.setLastActiveTime(0);
			lp.setCurrentBattleCount(0);
			lp.setCurrentBattleLimit(ServerLeagueManager.BATTLE_INTERAL_COUNT);
			lp.setCurrentFlushCount(0);
		}
	}

	/** 设置联赛为结算中 */
	public void setLeagueSettle()
	{
		isWarActive=false;
		sendLeaguaStaut(LEAGUE_SETTLE_READY);
	}

	/** 结算比赛 */
	public void leagueSettleDown()
	{
		isWarActive=false;
		currentCopyList.clear();
		sendLeaguaStaut(LEAGUE_SETTLE);
	}

	/** 关闭联赛 */
	public boolean closeSystem()
	{
		isOpen=false;
		isReady=false;
		isWarActive=false;
		clearPlayerInfo();
		currentCopyList.clear();
		rankList.clear();
		readyCopyList.clear();
		sellList.clear();
		sendLeaguaStaut(LEAGUE_CLOSE);
		return true;
	}

	/** 检测目标是否可挑战 */
	public String checkTargetCanFight(int targetId,LeaguePlayer lp)
	{
		if(!isWarActive) return "league not active";
		;
		if(lp.getLeagueId()!=leagueId) return "current league not match";
		if(lp.getCurrentBattleCount()>=lp.getCurrentBattleLimit())
			return "league battle limit count not enough";
		if(!lp.isTargetCanFight(targetId)) return "target has been fighted";
		return null;
	}

	/** 增加积分并刷新激活时间 */
	public void addLeagueGoal(Player player,int goal,int time)
	{
		if(!isWarActive) return;
		LeaguePlayer lp=(LeaguePlayer)playerlist.get(player.getId());
		lp.setGoal(lp.getGoal()+goal);
		if(lp.getGoal()<0)
			lp.setGoal(0);
		lp.setJiontime(time);
	}

	/** 增加代币 */
	public void addLeagueCoin(Player player,int coin)
	{
		if(!isWarActive) return;
		LeaguePlayer lp=(LeaguePlayer)playerlist.get(player.getId());
		lp.addBet(coin);
		if(lp.getBet()<0)
			lp.setBet(0);
	}

	/** 获取玩家排名,如果未入榜则返回0 */
	public int getPlayerRank(Player player)
	{
		CrossWarPlayer temp=null;
		for(int i=0;i<rankList.size();i++)
		{
			temp=(CrossWarPlayer)rankList.get(i);
			if(isCrossWarPlayerSelf(player,temp))
			{
				log.info("player:"+player.getName()+" league rank:"+(i+1));
				return i+1;
			}
		}
		return 0;
	}

	/** 检测、刷新玩家信息,如果不存在可选创建 */
	public void checkLeaguePlayerInNewDay(int time,Player player,
		boolean isCreated)
	{
		LeaguePlayer lp=getLeaguePlayer(player);
		if(lp==null)
		{
			if(!isCreated) return;
			lp=new LeaguePlayer();
			playerlist.put(player.getId(),lp);
		}
		checkLeaguePlayerInNewDay(time,player);
	}

	/** 检测、刷新玩家信息 */
	public void checkLeaguePlayerInNewDay(int time,Player player)
	{
		LeaguePlayer lp=getLeaguePlayer(player);
		if(lp==null) return;
		// 如果玩家信息与当前联赛不匹配,进行重置
		if(lp.getLeagueId()!=leagueId) lp.setLastActiveTime(0);
		if(SeaBackKit.isSameDay(time,lp.getLastActiveTime())) return;
		lp.setLeagueId(leagueId);
		lp.setId(player.getId());
		lp.setName(player.getName());
		lp.setLastActiveTime(time);
		lp.setCurrentBattleCount(0);
		lp.setCurrentBattleLimit(ServerLeagueManager.BATTLE_INTERAL_COUNT);
		lp.setCurrentFlushCount(0);
		lp.getPropList().clear();
	}

	/** 兑换物品 */
	public String buyProp(int sid,Player challenger,int time)
	{
		LeaguePlayer lp=getLeaguePlayer(challenger);
		LeagueShopProp lsp=null;
		for(int i=0;i<sellList.size();i++)
		{
			lsp=(LeagueShopProp)sellList.get(i);
			if(lsp.getPropSid()==sid)
			{
				if(lp.getBet()<lsp.getCostCoin())
					return "league coin not enough";
				lp.incrPropRecord(sid,1);
				lp.setLastActiveTime(time);
				lp.setBet(lp.getBet()-lsp.getCostCoin());
				Prop prop=(Prop)Prop.factory.newSample(sid);
				challenger.getBundle().incrProp(prop,true);
				JBackKit.sendResetBunld(challenger);
				return null;
			}
		}
		return null;
	}

	public void updateLeagueCenterInfo(String ip,String port,
		String serverName)
	{
		centerIP=ip;
		centerPort=port;
		localServerName=serverName;
		ContextVarManager.getInstance().setVarDest(
			ContextVarManager.CROSS_LEAGUE_CLIENT_INFO,
			centerIP+","+centerPort+","+localServerName);
		log.info("update center info, ip:"+centerIP+","+centerPort+","
			+localServerName);
	}

	/** 发送联赛奖励 */
	public void sendRankingAward(int pid,String name,int ranking,int awardSid)
	{
		Player player=objectFactory.getPlayerById(pid);
		if(player==null)
		{
			log.error("send ranking award, player is null, id:"+pid
				+", name:"+name+", ranking:"+ranking+", awardSid:"+awardSid);
		}
		Award award=(Award)Award.factory.getSample(awardSid);
		if(award==null)
		{
			log.error("send ranking award, award is null, id:"+pid+", name:"
				+name+", ranking:"+ranking+", awardSid:"+awardSid);
		}
		log.info("send ranking award, id:"+pid+", name:"+player.getName()
			+", ranking:"+ranking+", awardSid:"+awardSid);
		String content=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"cross_goal_league_rank_award");
		content=TextKit.replace(content,"%",String.valueOf(ranking));
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"cross_goal_league_rank_title");
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),Message.SYSTEM_ONE_TYPE,title,
			true,award);
		// 刷新前台
		JBackKit.sendRevicePlayerMessage(player,message,
			message.getRecive_state(),objectFactory);
	}

	public ArenaManager getArenaManager()
	{
		return arenaManager;
	}

	public void setArenaManager(ArenaManager arenaManager)
	{
		this.arenaManager=arenaManager;
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public CrossLeaguePlayerDBAccess getCpDBAccess()
	{
		return cpDBAccess;
	}

	public void setCpDBAccess(CrossLeaguePlayerDBAccess cpDBAccess)
	{
		this.cpDBAccess=cpDBAccess;
	}

	/** 序列化LeaguePlayer(挑战列表) */
	public static void bytesWrite2Buffer(ByteBuffer data,LeaguePlayer cwp)
	{
		data.writeInt(cwp.getCrossid());
		data.writeShort(cwp.getAreaid());
		data.writeShort(cwp.getPlatid());
		data.writeShort(cwp.getSeverid());
		// 头像
		data.writeShort(cwp.getHeadPic());
		data.writeShort(cwp.getHeadFrame());
		data.writeInt(cwp.getId());
		data.writeShort(cwp.getSid());
		data.writeUTF(cwp.getSeverName());
		data.writeUTF(cwp.getName());
		data.writeInt(cwp.getFightscore());
		data.writeByte(cwp.getLevel());
		cwp.bytesWriteAdjustment(data);
		cwp.bytesWriteShiplevel(data);
		cwp.bytesWriteOFS(data);
		cwp.bytesWriteAttacklist(data);
	}

	/** 反序列化LeaguePlayer(挑战列表) */
	public static LeaguePlayer bytesRead2CrossWarPlayer(ByteBuffer data)
	{
		int crossId=data.readInt();
		int aid=data.readUnsignedShort();
		int pid=data.readUnsignedShort();
		int svid=data.readUnsignedShort();
		int headPic=data.readUnsignedShort();
		int headFrame=data.readUnsignedShort();
		int id=data.readInt();
		int sid=data.readUnsignedShort();
		String sname=data.readUTF();
		String pname=data.readUTF();
		int sf=data.readInt();
		int lv=data.readUnsignedByte();
		LeaguePlayer cwp=new LeaguePlayer();
		cwp.bytesReadAdjustment(data);
		cwp.bytesReadShiplevel(data);
		cwp.bytesReadOFS(data);
		cwp.bytesReadAttacklist(data);
		cwp.setCrossid(crossId);
		cwp.setAreaid(aid);
		cwp.setPlatid(pid);
		cwp.setSeverid(svid);
		cwp.setId(id);
		cwp.setSid(sid);
		cwp.setSeverName(sname);
		cwp.setName(pname);
		cwp.setFightscore(sf);
		cwp.setLevel(lv);
		cwp.setHeadPic(headPic);
		cwp.setHeadFrame(headFrame);
		return cwp;
	}

	/** 序列化LeaguePlayer(排行榜) */
	public static void showBytesWrite2Buffer(ByteBuffer data,LeaguePlayer cp)
	{
		data.writeInt(cp.getId());
		data.writeShort(cp.getAreaid());
		data.writeShort(cp.getPlatid());
		data.writeShort(cp.getSeverid());
		data.writeUTF(cp.getName());
		data.writeUTF(cp.getSeverName());
		data.writeInt(cp.getGoal());
		data.writeInt(cp.getJiontime());
		data.writeShort(cp.getLevel());
		// 头像
		data.writeInt(cp.getHeadPic());
		data.writeInt(cp.getHeadFrame());
	}

	/** 反序列化LeaguePlayer(排行榜) */
	public static LeaguePlayer showBytesRead2LeaguePlayer(ByteBuffer data)
	{
		int id=data.readInt();
		int aid=data.readUnsignedShort();
		int pid=data.readUnsignedShort();
		int sid=data.readUnsignedShort();
		String name=data.readUTF();
		String sName=data.readUTF();
		int goal=data.readInt();
		int joinTime=data.readInt();
		int lv=data.readUnsignedShort();
		int headPic=data.readInt();
		int headFrame=data.readInt();
		LeaguePlayer cp=new LeaguePlayer();
		cp.setId(id);
		cp.setAreaid(aid);
		cp.setPlatid(pid);
		cp.setSeverid(sid);
		cp.setName(name);
		cp.setSeverName(sName);
		cp.setGoal(goal);
		cp.setJiontime(joinTime);
		cp.setLevel(lv);
		cp.setHeadPic(headPic);
		cp.setHeadFrame(headFrame);
		return cp;
	}

	/** 序列化LeagueShopProp */
	public static void bytesWrite2Buffer(ByteBuffer data,LeagueShopProp lsp)
	{
		data.writeShort(lsp.getPropSid());
		data.writeInt(lsp.getCostCoin());
		data.writeShort(lsp.getLimit());
		data.writeByte(lsp.getLevel());
	}

	/** 反序列化LeagueShopProp */
	public static LeagueShopProp bytesRead2LeagueShopProp(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		int coin=data.readInt();
		int limit=data.readUnsignedShort();
		int level=data.readUnsignedByte();
		LeagueShopProp lsp=new LeagueShopProp(sid,coin,limit,level);
		return lsp;
	}

	/** 前端展示序列化-条目详细 */
	public void showBytesWriteCrossWarPlayerDetail(Player player,
		CrossWarPlayer cwp,ByteBuffer data,boolean isChallengeSuccess)
	{
		showBytesWriteCrossWarPlayer(player,cwp,data,isChallengeSuccess);
		cwp.bytesWriteAttacklist(data);
	}

	/** 前端展示序列化-条目基础信息 */
	public void showBytesWriteCrossWarPlayer(Player player,
		CrossWarPlayer cwp,ByteBuffer data,boolean isChallengeSuccess)
	{
		data.writeInt(cwp.getCrossid());
		data.writeUTF(cwp.getName());
		data.writeUTF(cwp.getSeverName());
		int goal=cwp.getGoal();
		if(player!=null)
			goal=LeagueFightManager.getFightLeagueGoal(
				getChallegeTargetIndex(cwp.getCrossid(),player),
				getLeaguePlayer(player).getFightscore(),cwp.getFightscore(),
				true);
		data.writeInt(goal);
		data.writeInt(cwp.getFightscore());
		data.writeByte(cwp.getLevel());
		data.writeInt(cwp.getRank());
		int headPic=0;
		int headFrame=0;
		if(cwp instanceof LeaguePlayer)
		{
			headPic=((LeaguePlayer)cwp).getHeadPic();
			headFrame=((LeaguePlayer)cwp).getHeadFrame();
		}
		data.writeShort(headPic);
		data.writeShort(headFrame);
		data.writeBoolean(isChallengeSuccess);
	}

	/** 序列化挑战列表 */
	public void showBytesWriteChallengeList(ByteBuffer data,Player player)
	{
		IntList list=getCurrentChallengeList(player);
		int size=0;
		if(list!=null) size=list.size();
		CrossWarPlayer cwp=null;
		data.writeByte(size/2);
		for(int i=0;i<size;i+=2)
		{
			cwp=getCrossWarPlayer(list.get(i));
			boolean isSuccess=list.get(i+1)==LeaguePlayer.CHALLENGE_SUCCESS;
			showBytesWriteCrossWarPlayer(player,cwp,data,isSuccess);
		}
	}

	/** 序列化部署舰队 */
	public void showBytesWriteAttackList(ByteBuffer data,Player player)
	{
		int size=0;
		IntList list=null;
		LeaguePlayer lp=getLeaguePlayer(player);
		if(lp!=null)
		{
			list=lp.getAttacklist();
			size=list.size();
		}
		data.writeByte(size/3);
		for(int i=0;i<size;i+=3)
		{
			data.writeShort(list.get(i));
			data.writeShort(list.get(i+1));
			data.writeByte(list.get(i+2));
		}
	}

	/** 序列化商店信息 */
	public void showBytesWriteShop(ByteBuffer data,Player player,int time)
	{
		LeagueShopProp lsp=null;
		IntList recordList=null;
		LeaguePlayer lp=getLeaguePlayer(player);
		data.writeByte(sellList.size());
		for(int i=0;i<sellList.size();i++)
		{
			lsp=(LeagueShopProp)sellList.get(i);
			data.writeShort(lsp.getPropSid());
			data.writeInt(lsp.getCostCoin());
			data.writeShort(lsp.getLimit());
			int count=0;
			// 第一个值记录上次兑换的时间
			if(lp!=null&&SeaBackKit.isSameDay(lp.getLastActiveTime(),time))
			{
				recordList=lp.getPropList();
				for(int j=0;j<recordList.size();j+=2)
				{
					if(recordList.get(j)!=lsp.getPropSid()) continue;
					count=recordList.get(j+1);
					break;
				}
			}
			data.writeShort(count);
		}
		data.writeInt(SeaBackKit.getSomedayEnd(0)-time);
	}

	/** 登录序列化 */
	public void showBytesWrite(CreatObjectFactory factory,Player player,
		ByteBuffer data,int time)
	{
		// 功能是否开启
		data.writeBoolean(isOpen);
		if(!isOpen) return;
		LeaguePlayer lp=getLeaguePlayer(player);
		boolean isExist=!(lp==null);
		// 是否有玩家数据
		data.writeBoolean(isExist);
		if(!isExist) return;
		showBytesWriteImmed(factory,player,data,time);
	}

	/** 直接序列化,不做功能与玩家参与标识的判定 */
	public void showBytesWriteImmed(CreatObjectFactory factory,
		Player player,ByteBuffer data,int time)
	{
		showBytesWriteBase(factory,player,data,time);
		// 挑战列表
		showBytesWriteChallengeList(data,player);
		// 部署舰队
		showBytesWriteAttackList(data,player);
		// 商店列表
		showBytesWriteShop(data,player,time);
	}

	/** 基础信息序列化 */
	public void showBytesWriteBase(CreatObjectFactory factory,Player player,
		ByteBuffer data,int time)
	{
		LeaguePlayer lp=getLeaguePlayer(player);
		checkLeaguePlayerInNewDay(time,player,false);
		// 基本信息
		// 积分
		data.writeInt(lp.getGoal());
		// 代币
		data.writeInt(lp.getBet());
		// 排名
		data.writeInt(getPlayerRank(player));
		// 当前挑战次数上限
		data.writeShort(lp.getCurrentBattleLimit());
		// 当前挑战次数
		data.writeShort(lp.getCurrentBattleCount());
		// 当前刷新次数
		data.writeShort(lp.getCurrentFlushCount());
		// 战斗功能是否可用
		data.writeBoolean(isWarActive);
	}

	public ByteBuffer sendHttpData(ByteBuffer data,String ip,String port)
	{
		HttpRequester request=new HttpRequester();
		ByteBuffer baseData=new ByteBuffer();
		baseData.writeShort(ServerLeagueManager.SERVER_HTTP_ACTION_TYPE);
		ByteBuffer tdata=(ByteBuffer)data.clone();
		baseData.write(tdata.getArray(),tdata.offset(),tdata.length());
		String httpData=SeaBackKit.createBase64(baseData);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// 设置port
		map.put("port",
			String.valueOf(ServerLeagueManager.CLIENT_HTTP_ACTION_PORT));
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+ip+":"+port+"/","POST",map,null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}

	/** 定时保存玩家 */
	public int savePlayer(boolean force)
	{
		int failSave=0;
		Object[] objs=playerlist.valueArray();
		if(objs==null||objs.length<=0) return failSave;
		int nmax=max;
		if(force) nmax=objs.length;
		for(int i=0,j=0;i<objs.length&&j<nmax;i++)
		{
			if(objs[i]==null) continue;
			CrossWarPlayer p=(CrossWarPlayer)objs[i];
			if(p.isSave())
			{
				boolean succ=cpDBAccess.save(p);
				failSave+=succ?0:1;
				p.setSave(!succ);
				j++;
			}
		}
		return failSave;
	}

	public int getLeagueId()
	{
		return leagueId;
	}

	@Override
	public int compare(Object o1,Object o2)
	{
		if(o1==null) return Comparator.COMP_LESS;
		if(o2==null) return Comparator.COMP_GRTR;
		CrossWarPlayer cp1=(CrossWarPlayer)o1;
		CrossWarPlayer cp2=(CrossWarPlayer)o2;
		if(cp1.getFightscore()<cp2.getFightscore())
			return Comparator.COMP_LESS;
		if(cp1.getFightscore()>cp2.getFightscore())
			return Comparator.COMP_GRTR;
		if(cp1.getJiontime()<cp2.getJiontime()) return Comparator.COMP_GRTR;
		return Comparator.COMP_LESS;
	}
}
