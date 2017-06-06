package foxu.cross.goalleague;

import java.io.IOException;
import java.util.HashMap;

import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cross.goalleague.persistent.CrossCopyPlayerDBAccess;
import foxu.cross.server.CrossServer;
import foxu.cross.war.CrossWarPTComparator;
import foxu.cross.war.CrossWarPlayer;
import foxu.sea.ContextVarManager;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.uid.UidKit;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.ArrayList;
import mustang.set.Comparator;
import mustang.set.IntKeyHashMap;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * 服务器端联赛管理器
 * 
 * @author Alan
 * 
 */
public class ServerLeagueManager implements TimerListener,Comparator
{

	private static final Logger log=LogFactory
		.getLogger(ServerLeagueManager.class);
	/**
	 * LEAGUE_STAUT_CLOSE=0 比赛关闭, LEAGUE_STAUT_OPEN=1, 比赛开启
	 * LEAGUE_STAUT_RANK=2 重置排行榜, LEAGUE_STAUT_COPY_IN=3 拷贝AI副本,
	 * LEAGUE_STAUT_COPY_OUT=4 下发AI副本, LEAGUE_STAUT_PLAYER_INFO=5 收集参与玩家信息,
	 * LEAGUE_INFO=6 获取联赛信息, LEAGUE_CENTER_INFO=7 下发联赛服务器信息,
	 * LEAGUE_STAUT_SETTLE=100 赛末结算
	 */
	public static final int LEAGUE_STAUT_CLOSE=0,LEAGUE_STAUT_OPEN=1,
					LEAGUE_STAUT_RANK=2,LEAGUE_STAUT_COPY_IN=3,
					LEAGUE_STAUT_COPY_OUT=4,LEAGUE_STAUT_PLAYER_INFO=5,
					LEAGUE_INFO=6,LEAGUE_CENTER_INFO=7,
					LEAGUE_SETTLE_AWARD=8,LEAGUE_STAUT_SETTLE=100;
	public static int RANK_UPDATE_TIME=60*60;
	/** 排行榜数量 */
	public static int RANK_COUNT=200;
	/**
	 * BATTLE_INTERAL_COUNT=每天挑战次数,BATTLE_FLUSH_COUNT=每天免费刷新次数,
	 * BATTLE_FLUSH_GEMS=列表刷新消耗宝石
	 */
	public static int BATTLE_INTERAL_COUNT=9,BATTLE_FLUSH_COUNT=5,
					BATTLE_FLUSH_GEMS=30;
	/** 付费战斗消耗宝石 */
	public static int[] BATTLE_FIGHT_GEMS;
	/** 挑战目标选取战力分段区间[起始百分比,终止百分比] */
	public static int[] SELECT_FIGHT_SCORE_RANGE={0,75,75,90,90,
		Integer.MAX_VALUE};
	/** 联赛商店物品[sid,coins,count,稀有等级] */
	public static int[] LEAGUE_SHOP;
	/** AI副本限制级数 */
	public static int COPY_LIMIT_LV=50;
	/** 副本区间信息(组数应与目标选取分段区间组数相同)[副本数量起始比例,副本数量截止比例,玩家保底数量,...] */
	public static int[] COPY_RANGE_INFO={0,50,300,50,75,300,75,100,300};
	/** 挑战区间使用超限以重置的比例 */
	public static int COPY_RANGE_RESET_PERCENT=75;
	/** 联赛开启、结算周中天数 */
	public static int OPEN_DAY_WEEK=1,COPY_CLIENT_PLAYER_DAY_WEEK=6,
					SETTLE_DAY_WEEK=5;
	/** 奖励发送延迟时间,小时 */
	public static int LEAGUE_AWARD_TIME_DELAY=12;
	/** 客户端http处理端口代号 */
	public static int CLIENT_HTTP_ACTION_PORT=12,CLIENT_HTTP_ACTION_TYPE=1,
					SERVER_HTTP_ACTION_TYPE=2;
	/** 战斗积分是否可用 */
	boolean isWarActive;
	/** 上一次重置榜单的时间 */
	int lastRankTime;
	/** 系统开启状态 */
	boolean isOpen;
	/** 服务器列表 */
	ArrayList serverlist=new ArrayList();
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
	/** 下次拷贝时间 */
	int nextCopyTime;
	/** 下次开启时间 */
	int nextOpenTime;
	/** 联赛停止积分时间 */
	int currentActiveEndTime;
	/** 本次联赛id */
	int leagueId;
	/** 本次联赛是否已发奖 */
	boolean isAward;
	/** UID提供器 */
	UidKit uidkit;
	/** 复制锁 */
	byte[] copyLock=new byte[0];
	/** 复制锁标记 */
	boolean isCopyLock;
	/** 排行锁标记 */
	boolean isRankLock;
	CrossWarPTComparator cptc=new CrossWarPTComparator();
	IntKeyHashMap failDatas=new IntKeyHashMap();
	CrossCopyPlayerDBAccess warDBAccess;

	TimerEvent timer=new TimerEvent(this,"main",1000);
	TimerEvent readyCopyTimer=new TimerEvent(this,"readyCopy",60*1000);
	TimerEvent resendTimer=new TimerEvent(this,"resend",5*1000);
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	public static boolean CROSS_LEAGUE_SERVER;
	
	public void init()
	{
		if(!CROSS_LEAGUE_SERVER) return;
		Sample[] sps=factory.getSamples();
		for(int i=0;i<sps.length;i++)
		{
			if(sps[i]==null) continue;
			CrossServer cs=(CrossServer)sps[i];
			serverlist.add(cs);
		}
		// 商店列表
		LeagueShopProp lsp=null;
		for(int i=0;i<LEAGUE_SHOP.length;i+=4)
		{
			lsp=new LeagueShopProp(LEAGUE_SHOP[i],LEAGUE_SHOP[i+1],
				LEAGUE_SHOP[i+2],LEAGUE_SHOP[i+3]);
			sellList.add(lsp);
		}
		isOpen=ContextVarManager.getInstance().getVarValue(
			ContextVarManager.CROSS_LEAGUE_SERVER_INFO)==1;
		initCurrentLeague();
		TimerCenter.getSecondTimer().add(timer);
		TimerCenter.getMinuteTimer().add(readyCopyTimer);
	}

	public void open(int time)
	{
		if(!isOpen) return;
		if(!isReady)
		{
//			log.error("copy not ready, invoke method later...");
			return;
		}
		currentActiveEndTime=SeaBackKit.getDayOfWeekTime(SETTLE_DAY_WEEK);
		nextCopyTime=SeaBackKit
			.getDayOfWeekTime(COPY_CLIENT_PLAYER_DAY_WEEK);
		nextOpenTime=SeaBackKit.getDayOfWeekTime(OPEN_DAY_WEEK)
			-(int)(SeaBackKit.DAY_MILL_TIMES/1000)
			+(int)(SeaBackKit.WEEK_MILL_TIMES/1000);
		currentCopyList=readyCopyList;
		// 如果当前时间已经过了结算时间,那么不再进行活动开启操作,等待下一次活动开启
		if(time>=currentActiveEndTime)
		{
			isReady=false;
			readyCopyList.clear();
			return;
		}
		leagueId=uidkit.getPlusUid();
		isReady=false;
		isWarActive=true;
		isAward=false;
		log.info("league server open, currentActiveEndTime:"
			+SeaBackKit.formatDataTime(currentActiveEndTime)
			+", nextOpenTime:"+SeaBackKit.formatDataTime(nextOpenTime)
			+", nextCopyTime:"+SeaBackKit.formatDataTime(nextCopyTime));
		// 开启初始参数
		ByteBuffer data=new ByteBuffer();
		bytesWriteLeagueOpen(data);
		sendLeaguaStaut(LEAGUE_STAUT_OPEN,data);
		saveCurrentLeague();
	}

	/** 发送联赛状态 */
	public void sendLeaguaStaut(int staut,ByteBuffer extraData)
	{
		CrossServer cs=null;
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<serverlist.size();i++)
		{
			data.clear();
			data.writeByte(staut);
			if(extraData!=null)
				data.write(extraData.getArray(),extraData.offset(),
					extraData.length());
			cs=(CrossServer)serverlist.get(i);
			log.info("send to server:"+cs.getIp()+","+cs.getPort());
			ByteBuffer temp=(ByteBuffer)data.clone();
			ByteBuffer result=sendHttpData(data,cs.getIp(),cs.getPort());
			// 补偿机制
			if(result==null)
			{
				log.error("send staut:"+staut+" to server fail:"+cs.getIp()
					+","+cs.getPort());
				failDatas.put(cs.getSid(),new FailSendEntry(cs,temp));
			}
		}
	}

	/** 发送预存挑战列表 */
	public void sendReadyCopyList()
	{
		ByteBuffer data=new ByteBuffer();
		bytesWriteTargetList(data,readyCopyList);
		sendLeaguaStaut(LEAGUE_STAUT_COPY_OUT,data);
	}

	/** 收集整合挑战列表 */
	public void collectCopyList()
	{
		if(isCopyLock) return;
		synchronized(copyLock)
		{
			try
			{
				isCopyLock=true;
				if(!isOpen) return;
				ArrayList list=null;
				CrossServer cs=null;
				CrossWarPlayer cp=null;
				ByteBuffer data=new ByteBuffer();
				readyCopyList=new ArrayList();
				for(int i=0;i<serverlist.size();i++)
				{
					data.clear();
					cs=(CrossServer)serverlist.get(i);
					data.writeByte(LEAGUE_STAUT_COPY_IN);
					data.writeShort(COPY_LIMIT_LV);
					data=sendHttpData(data,cs.getIp(),cs.getPort());
					list=bytesReadCrossWarPlayer(data,cs);
					for(int j=0;j<list.size();j++)
					{
						cp=(CrossWarPlayer)list.get(j);
						readyCopyList.add(cp);
					}
				}
				// 挑战列表排序
				Object[] objs=readyCopyList.toArray();
				SetKit.sort(objs,this);
				int ident=1;
				for(int i=0;i<objs.length;i++)
				{
					cp=(CrossWarPlayer)objs[i];
					cp.setCrossid(ident++);
				}
				readyCopyList=new ArrayList(objs);
				sendReadyCopyList();
				isReady=true;
			}
			finally
			{
				isCopyLock=false;
			}
		}
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		/*int time=(int)(e.getCurrentTime()/1000);
		if("readyCopy".equals(e.getParameter()))
		{
			if(time>=nextCopyTime&&!isReady) collectCopyList();
			return;
		}
		// 补偿发送
		else if("resend".equals(e.getParameter()))
		{
			synchronized(failDatas)
			{
				FailSendEntry fse=null;
				ByteBuffer data=null;
				int[] keys=failDatas.keyArray();
				for(int i=0;i<keys.length;i++)
				{
					fse=(FailSendEntry)failDatas.get(keys[i]);
					if(fse==null)
					{
						failDatas.remove(keys[i]);
						continue;
					}
					data=sendHttpData(fse.data,fse.cs.getIp(),
						fse.cs.getPort());
					// 如果成功返回,则移除
					if(fse.count<=0||data!=null)
					{
						failDatas.remove(keys[i]);
						continue;
					}
					fse.count--;
				}
			}
			return;
		}
		// 联赛状态整理
		collateLeagueInfo(time);
		// 重置排行榜
		if(lastRankTime+RANK_UPDATE_TIME<=time)
		{
			collectServerInfo(time,false);
			sendCenterInfo();
		}
		collateLeagueInfo(time,isResetRank);*/
	}

	/** 初始化积分赛信息 */
	public void initCurrentLeague()
	{
		byte[] arr=ContextVarManager.getInstance().getVarData(
			ContextVarManager.CROSS_LEAGUE_SERVER_INFO);
		if(arr==null) return;
		ByteBuffer data=new ByteBuffer(arr);
		// 基础信息
		if(isOpen)
		{
			leagueId=data.readInt();
			currentActiveEndTime=data.readInt();
			nextOpenTime=data.readInt();
			bytesReadRankList(data);
			// 当前列表是否可用
			isWarActive=data.readBoolean();
			isAward=data.readBoolean();
			if(isWarActive)
			{
				currentCopyList.clear();
				// 当前列表
				CrossWarPlayer[] cwps=warDBAccess.loadBySqlAll();
				if(cwps!=null&&cwps.length>0)
				{
					for(int i=0;i<cwps.length;i++)
					{
						currentCopyList.add(cwps[i]);
					}
				}
			}
		}
		log.info("league server init, currentActiveEndTime:"
			+SeaBackKit.formatDataTime(currentActiveEndTime)
			+", nextOpenTime:"+SeaBackKit.formatDataTime(nextOpenTime));
		// 下发同步数据
		data.clear();
		bytesWriteLeagueInfo(data);
		sendLeaguaStaut(LEAGUE_INFO,data);
	}

	/** 保存当前赛季信息 */
	public void saveCurrentLeague()
	{
		ByteBuffer data=new ByteBuffer();
		// 基础信息
		if(isOpen)
		{
			data.writeInt(leagueId);
			data.writeInt(currentActiveEndTime);
			data.writeInt(nextOpenTime);
			bytesWriteRankList(data);
			// 当前列表是否可用
			data.writeBoolean(isWarActive);
			// 当前联赛是否已结算
			data.writeBoolean(isAward);
			ContextVarManager.getInstance().setVarData(
				ContextVarManager.CROSS_LEAGUE_SERVER_INFO,data.toArray());
			warDBAccess.deleteAll();
			if(isWarActive)
			{
				// 当前列表
				CrossWarPlayer cp=null;
				for(int i=0;i<currentCopyList.size();i++)
				{
					cp=(CrossWarPlayer)currentCopyList.get(i);
					warDBAccess.save(cp);
				}
			}
		}
	}

	/** 整理联赛信息,结算、开关 */
	public synchronized void collateLeagueInfo(int time)
	{
		if(time>=currentActiveEndTime&&isWarActive)
		{
			isWarActive=false;
			collectServerInfo(time,true);
			saveCurrentLeague();
		}
		else if(!isWarActive&&!isAward
			&&time>=currentActiveEndTime+LEAGUE_AWARD_TIME_DELAY*3600)
		{
			settleDown();
		}
		else if(time>=nextOpenTime&&!isWarActive)
		{
			// 开始
			open(time);
		}
	}

	/** 联赛结算 */
	public void settleDown()
	{
		synchronized(rankList)
		{
			sendLeaguaStaut(LEAGUE_STAUT_SETTLE,null);
			LeaguePlayer cp=null;
			CrossServer cs=null;
			ByteBuffer data=new ByteBuffer();
			for(int i=0;i<rankList.size();i++)
			{
				data.clear();
				cp=(LeaguePlayer)rankList.get(i);
				int ranking=i+1;
				int awardSid=getAwardSid(cp,ranking);
				// 奖励错误
				if(awardSid<=0)
				{
					log.error("settle down award error, ranking:"+ranking
						+", area:"+cp.getAreaid()+", plat:"+cp.getPlatid()
						+", server:"+cp.getSeverid()+", svName:"
						+cp.getSeverName()+", player:"+cp.getId()+", name:"
						+cp.getName());
					continue;
				}
				for(int j=0;j<serverlist.size();j++)
				{
					cs=(CrossServer)serverlist.get(j);
					// 预留的服务器sid
					if(cs.getSid()==cp.getServerSid()) break;
					cs=null;
				}
				// 服务器错误
				if(cs==null)
				{
					log.error("settle down cross_server error, ranking:"
						+ranking+", area:"+cp.getAreaid()+", plat:"
						+cp.getPlatid()+", server:"+cp.getSeverid()
						+", svName:"+cp.getSeverName()+", player:"
						+cp.getId()+", name:"+cp.getName());
					continue;
				}
				log.info("player settle down, ranking:"+ranking+", area:"
					+cp.getAreaid()+", plat:"+cp.getPlatid()+", server:"
					+cp.getSeverid()+", svName:"+cp.getSeverName()
					+", player:"+cp.getId()+", name:"+cp.getName()
					+", award_sid:"+awardSid);
				data.writeByte(LEAGUE_SETTLE_AWARD);
				// 玩家id
				data.writeInt(cp.getId());
				// 玩家名字
				data.writeUTF(cp.getName());
				// 玩家排名
				data.writeShort(ranking);
				// 奖励sid
				data.writeShort(awardSid);
				sendHttpData(data,cs.getIp(),cs.getPort());
			}
			isAward=true;
			saveCurrentLeague();
		}
	}

	/** 下发联赛服务器信息 */
	public void sendCenterInfo()
	{
		String ip=System.getProperty("webAddress");
		String port=String
			.valueOf(shelby.httpserver.HttpServer.DEFAULT_PORT);
		CrossServer cs=null;
		ByteBuffer data=new ByteBuffer();
		for(int i=0;i<serverlist.size();i++)
		{
			data.clear();
			data.writeByte(LEAGUE_CENTER_INFO);
			cs=(CrossServer)serverlist.get(i);
			data.writeUTF(ip);
			data.writeUTF(port);
			data.writeUTF(cs.getSeverName());
			sendHttpData(data,cs.getIp(),cs.getPort());
		}
	}

	/** 收集整合各服务器玩家信息 */
	public void collectServerInfo(int time,boolean isSettleDown)
	{
		// 如果当前为结算整合数据,则不进行拦截
		if(!isOpen||(!isWarActive&&!isSettleDown))
		{
			lastRankTime=time;
			return;
		}
		// 优先进行镜像拷贝
		if(isRankLock||isCopyLock) return;
		synchronized(copyLock)
		{
			try
			{
				isRankLock=true;
				ArrayList lists=new ArrayList();
				ArrayList list=null;
				CrossServer cs=null;
				CrossWarPlayer cwp=null;
				ByteBuffer data=new ByteBuffer();
				// 临时id,用于排序
				int ident=1;
				for(int i=0;i<serverlist.size();i++)
				{
					data.clear();
					cs=(CrossServer)serverlist.get(i);
					data.writeByte(LEAGUE_STAUT_PLAYER_INFO);
					// 是否是结算时提取
					data.writeBoolean(isSettleDown);
					data=sendHttpData(data,cs.getIp(),cs.getPort());
					list=bytesReadLeaguePlayer(data,cs);
					for(int j=0;j<list.size();j++)
					{
						cwp=(CrossWarPlayer)list.get(j);
						cwp.setCrossid(ident++);
						lists.add(cwp);
					}
				}
				resetRankList(lists);
				log.info("rankList reset, size:"+rankList.size());
				// 发送排行榜
				data.clear();
				bytesWriteRankList(data);
				sendLeaguaStaut(LEAGUE_STAUT_RANK,data);
				lastRankTime=time;
			}
			finally
			{
				isRankLock=false;
			}
		}
	}

	/** 重置排行榜 */
	public void resetRankList(ArrayList playerList)
	{
		// 当前玩家
		CrossWarPlayer player=null;
		// 临时得分最高玩家
		CrossWarPlayer target=null;
		// 是否首次循环(解决循环体内变量首次赋值的一些问题)
		boolean isFirstTime=true;
		// 上一轮最高得分
		long lastScore=0;
		// 总记录数
		int recordSize=0;
		IntKeyHashMap record=new IntKeyHashMap();
		record.clear();
		while(playerList.size()>recordSize)
		{
			target=null;
			// 临时最高得分
			long rankScore=0;
			// 当前名次
			int ranking=record.size();
			// 获取上一轮名次列表(如果本轮最高分数相同则直接添加,否则新实例化一个列表)
			IntKeyHashMap list=(IntKeyHashMap)record.get(ranking);
			for(int j=0;j<playerList.size();j++)
			{
				player=(CrossWarPlayer)playerList.get(j);
				if(player==null) continue;
				long playerScore=player.getGoal();
				if(rankScore==0)
				{
					// 第一次直接赋值or上一轮得分大于等于当前玩家得分时
					if(isFirstTime||lastScore>=playerScore)
					{
						// 判断当前玩家是否已经记录过了
						if(!isFirstTime&&lastScore==playerScore&&list!=null
							&&list.get(player.getCrossid())!=null) continue;
						rankScore=playerScore;
						target=player;
					}
					continue;
				}
				if(playerScore<rankScore) continue;
				// 如果小于上一轮最高得分并且高于临时最高得分,临时变量更新
				// 或者,如果等于上一轮最高得分并且上一轮无记录,此轮无需实例化新列表,临时变量更新
				if((((isFirstTime||lastScore>playerScore)&&rankScore<playerScore))
					||(list!=null&&lastScore==playerScore&&list.get(player
						.getCrossid())==null))
				{
					rankScore=playerScore;
					target=player;
					// 如果此轮遍历的最高有效分与上一轮相同,则无需继续遍历
					if(lastScore==playerScore) break;
				}
			}
			// 找不到更多的目标玩家,即当前得分已是最低,不需要继续遍历
			if(target==null) break;
			// 如果此轮遍历的最高有效分与上一轮不同,则需新实例化一个列表
			// 也许新服中所有玩家的某个得分都为0,那么也应该要放进容器
			if(lastScore!=rankScore||(isFirstTime&&rankScore==0))
			{
				list=new IntKeyHashMap();
				ranking++;
			}
			lastScore=rankScore;
			list.put(target.getCrossid(),target);
			recordSize++;
			record.put(ranking,list);
			// 得分相同时,总数量会大于名次,先用名次作为循环退出的判定
			// 如果此时找到的记录数已经符合长度,则不需要继续遍历
			if(ranking>=RANK_COUNT) break;
			if(isFirstTime) isFirstTime=false;
		}
		rankList.clear();
		// 处理并列问题
		IntKeyHashMap ihm=null;
		Object[] ps=null;
		// 名次为key,从1开始
		for(int i=1;i<=record.size();i++)
		{
			ihm=(IntKeyHashMap)record.get(i);
			if(ihm==null) continue;
			ps=ihm.valueArray();
			SetKit.sort(ps,cptc);// 积分排序
			for(int j=0;j<ps.length;j++)
			{
				if(ps[j]==null) continue;
				rankList.add(ps[j]);
				if(rankList.size()>=RANK_COUNT) break;
			}
			if(rankList.size()>=RANK_COUNT) break;
		}
	}

	/** 关闭联赛 */
	public void closeSystem()
	{
		isOpen=false;
		sendLeaguaStaut(LEAGUE_STAUT_CLOSE,null);
	}

	/** 读取玩家副本信息 */
	public ArrayList bytesReadCrossWarPlayer(ByteBuffer data,CrossServer cs)
	{
		ArrayList list=new ArrayList();
		CrossWarPlayer cwp=null;
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			cwp=ClientLeagueManager.bytesRead2CrossWarPlayer(data);
			cwp.setSeverName(cs.getSeverName());
			list.add(cwp);
		}
		return list;
	}

	/** 读取参与玩家信息 */
	public ArrayList bytesReadLeaguePlayer(ByteBuffer data,CrossServer cs)
	{
		ArrayList list=new ArrayList();
		LeaguePlayer cp=null;
		int leagueId=data.readInt();
		if(this.leagueId!=leagueId)
		{
			log.error("server:"+cs.getSeverName()
				+", collect league player league_id error, "+this.leagueId
				+":"+leagueId);
			return list;
		}
		int len=data.readInt();
		log.info("server:"+cs.getSeverName()+", collect league player size:"
			+len);
		for(int i=0;i<len;i++)
		{
			cp=ClientLeagueManager.showBytesRead2LeaguePlayer(data);
			cp.setSeverName(cs.getSeverName());
			// 预留服务器sid
			cp.setServerSid(cs.getSid());
			list.add(cp);
		}
		return list;
	}

	/** 序列化联赛开始信息 */
	public void bytesWriteLeagueOpen(ByteBuffer data)
	{
		data.writeInt(leagueId);
		data.writeInt(currentActiveEndTime);
		data.writeInt(nextOpenTime);
		data.writeByte(sellList.size());
		LeagueShopProp lsp=null;
		for(int i=0;i<sellList.size();i++)
		{
			lsp=(LeagueShopProp)sellList.get(i);
			ClientLeagueManager.bytesWrite2Buffer(data,lsp);
		}
	}

	/** 序列化挑战列表 */
	public void bytesWriteTargetList(ByteBuffer data,ArrayList list)
	{
		data.writeInt(list.size());
		LeaguePlayer cwp;
		for(int i=0;i<list.size();i++)
		{
			cwp=(LeaguePlayer)list.get(i);
			ClientLeagueManager.bytesWrite2Buffer(data,cwp);
		}
	}

	/** 序列化排行榜列表 */
	public void bytesWriteRankList(ByteBuffer data)
	{
		data.writeInt(rankList.size());
		LeaguePlayer cp=null;
		for(int i=0;i<rankList.size();i++)
		{
			cp=(LeaguePlayer)rankList.get(i);
			ClientLeagueManager.showBytesWrite2Buffer(data,cp);
		}
	}

	/** 反序列化排行榜列表 */
	public void bytesReadRankList(ByteBuffer data)
	{
		int len=data.readInt();
		CrossWarPlayer cp=null;
		for(int i=0;i<len;i++)
		{
			cp=ClientLeagueManager.showBytesRead2LeaguePlayer(data);
			rankList.add(cp);
		}
	}

	/** 序列化联赛基本信息 */
	public void bytesWriteLeagueInfo(ByteBuffer data)
	{
		// 功能开启标识
		data.writeBoolean(isOpen);
		if(!isOpen) return;
		bytesWriteLeagueOpen(data);
		// 预存列表是否可用
		data.writeBoolean(isReady);
		if(isReady) bytesWriteTargetList(data,readyCopyList);
		// 当前列表是否可用
		data.writeBoolean(isWarActive);
		if(isWarActive) bytesWriteTargetList(data,currentCopyList);
		bytesWriteRankList(data);
	}

	public ByteBuffer sendHttpData(ByteBuffer data,String ip,String port)
	{
		HttpRequester request=new HttpRequester();
		ByteBuffer baseData=new ByteBuffer();
		baseData.writeShort(CLIENT_HTTP_ACTION_TYPE);
		ByteBuffer tdata=(ByteBuffer)data.clone();
		baseData.write(tdata.getArray(),tdata.offset(),tdata.length());
		String httpData=SeaBackKit.createBase64(baseData);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// 设置port
		map.put("port",String.valueOf(CLIENT_HTTP_ACTION_PORT));
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+ip+":"+port+"/","POST",map,null);
		}
		catch(IOException e)
		{
			log.error("send to server:"+ip+","+port+" failed ",e);
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}

	public boolean isLeagueSettled(int leagueId,int time)
	{
		// 15秒内的时间差不再进行同步
		if(this.leagueId==leagueId
			&&(!isWarActive||currentActiveEndTime<=time+15)) return true;
		return false;
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

	public int getAwardSid(CrossWarPlayer cp,int ranking)
	{
		String[] rankAwards=PublicConst.CROSS_LEAGUE_RANK_AWARD;
		for(int j=0;j<rankAwards.length;j++)
		{
			String[] awards_temp=TextKit.split(rankAwards[j],":");
			if(TextKit.parseInt(awards_temp[0])>=ranking)
			{
				int[] awards=TextKit.parseIntArray(awards_temp);
				for(int k=1;k<awards.length;k+=2)
				{
					if(cp!=null&&awards[k]>=cp.getLevel())
					{
						return awards[k+1];
					}
				}
			}
		}
		return 0;
	}

	public UidKit getUidkit()
	{
		return uidkit;
	}

	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}

	public CrossCopyPlayerDBAccess getWarDBAccess()
	{
		return warDBAccess;
	}

	public void setWarDBAccess(CrossCopyPlayerDBAccess warDBAccess)
	{
		this.warDBAccess=warDBAccess;
	}

	/** 失败的发送,补偿发送对象 */
	class FailSendEntry
	{

		CrossServer cs;
		ByteBuffer data;
		int count=10;

		/** 失败的发送,补偿发送对象 */
		public FailSendEntry(CrossServer cs,ByteBuffer data)
		{
			super();
			this.cs=cs;
			this.data=data;
		}

	}
}
