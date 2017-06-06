package foxu.sea;

import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.ArrayList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.ServerRanklistActivity;
import foxu.sea.kit.SeaBackKit;

public class RankManager implements TimerListener
{

	/** 分页大小 */
	public static final int PAGE_SIZE=20;
	public static final int MAX_SIZE=100;
	public static final int FLUSH_TIME=60*60*2,MEM_FLUSH_TIME=5*60;
	private static RankManager manager=new RankManager();
	/** 数据获取类 */
	CreatObjectFactory objectFactory;

	/** 上一次刷新的时间 */
	int lastTime;
	/** 掠夺资源的排行 */
	ArrayList playerIdsPlunder=new ArrayList();
	/** 最高战力的排行 */
	ArrayList playerIdsFight=new ArrayList();
	/** 荣誉积分的排行 */
	ArrayList honorScoreIds=new ArrayList();
	/** 成就积分排名 */
	ArrayList achieveScore=new ArrayList();

	Logger log=LogFactory.getLogger(RankManager.class);
	/** 下次 排行日志输出时间 */
	int nextTime;

	public static RankManager getInstance()
	{
		return manager;
	}
	private void getInfoFromList(Player player,int pageIndex,ArrayList list,
		ByteBuffer data,boolean isScoreLong)
	{
		flushFileds();
		flushSelfRank(player);
		int num=list.size()-pageIndex*PAGE_SIZE;
		if(num<0) num=0;
		if(num>PAGE_SIZE) num=PAGE_SIZE;
		data.writeByte(num);
		for(int i=pageIndex*PAGE_SIZE;i<(pageIndex+1)*PAGE_SIZE;i++)
		{
			if(i>=list.size()) break;
			RankInfo playerRank=(RankInfo)list.get(i);
			data.writeUTF(playerRank.getPlayerName());
			data.writeByte(playerRank.getPlayerLevel());
			if(isScoreLong)
				data.writeLong(playerRank.getRankInfo());
			else
				data.writeInt((int)playerRank.getRankInfo());
		}
	}

	public void getFightScoreRank(Player player,int pageIndex,ByteBuffer data)
	{
		getInfoFromList(player,pageIndex,playerIdsFight,data,false);
		// 自己的战斗力排名
		data.writeInt(player.getFightScoreRank());
		data.writeInt(player.getFightScore());
	}

	public void getCheckPointRank(Player player,int pageIndex,ByteBuffer data)
	{
		getInfoFromList(player,pageIndex,playerIdsPlunder,data,true);
		// 自己的掠夺资源排名
		data.writeInt(player.getPlunderRank());
		data.writeLong(player.getPlunderResource());
	}

	public void getHonorRank(Player player,int pageIndex,ByteBuffer data)
	{
		getInfoFromList(player,pageIndex,honorScoreIds,data,false);
		// 自己的荣誉排名
		data.writeInt(player.getHonorScoreRank());
		data.writeInt(player.getHonorScore());
	}

	public void getAchieveRank(Player player,int pageIndex,ByteBuffer data)
	{
		getInfoFromList(player,pageIndex,achieveScore,data,false);
		// 自己成就积分排名
		data.writeInt(player.getAchieveScoreRank());
		data.writeInt(player.getAchieveScore());
	}

	private void resetRankFromDB(String sql,ArrayList list)
	{
		Fields fields[]=((PlayerGameDBAccess)objectFactory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		list.clear();
		for(int i=0;i<fields.length;i++)
		{
			int playerId=TextKit.parseInt(fields[i].getArray()[0].getValue()
							.toString());
			// 如果当前玩家还没完成新手改名流程则不进入列表
			if(!SeaBackKit.isNewPlayerChangeName(playerId,objectFactory))
				continue;
			RankInfo rank=new RankInfo(fields[i].getArray()[1].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[2]
				.getValue().toString()),Integer.parseInt(fields[i]
				.getArray()[3].getValue().toString()));
			list.add(rank);
		}
	}

	public boolean isDBRankFlush(int last,int now)
	{
		int lastOffset=(last-SeaBackKit.getSomedayBegin(0))/FLUSH_TIME;
		int offset=(now-SeaBackKit.getSomedayBegin(0))/FLUSH_TIME;
		if(lastOffset<offset)
			return true;
		return false;
	}
	
	/** 根据sql语言返回玩家名字的集合 */
	public void flushFileds()
	{
		int nowTime=TimeKit.getSecondTime();
		String sql=null;
		ServerRanklistActivity act=null;
		boolean isFromDB=false;
		// 关卡排行
		// 活动期间使用内存数据
		act=(ServerRanklistActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NEW_SERV_POINT,0);
		if(act!=null&&act.isOpen(nowTime)
			&&act.isUpdateRankFromActivity(nowTime,FLUSH_TIME))
		{
			act.resetRankList(playerIdsPlunder,nowTime,objectFactory,
				MAX_SIZE);
		}
		else if(isDBRankFlush(lastTime,nowTime))
		{
			// 有玩家删除就不加入排行榜
			sql="SELECT id,player_name,level,plunderResource FROM players where deleteTime=0 ORDER BY plunderResource DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,playerIdsPlunder);
			isFromDB=true;
		}
		// 战力排行
		// 活动期间使用内存数据
		act=(ServerRanklistActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NEW_SERV_FIGHT,0);
		if(act!=null&&act.isOpen(nowTime)
			&&act.isUpdateRankFromActivity(nowTime,FLUSH_TIME))
		{
			act.resetRankList(playerIdsFight,nowTime,objectFactory,MAX_SIZE);
		}
		else if(isDBRankFlush(lastTime,nowTime))
		{
			// 有玩家删除就不加入排行榜
			sql="SELECT id,player_name,level,fightScore FROM players where deleteTime=0 ORDER BY fightScore DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,playerIdsFight);
			isFromDB=true;
		}
		// 荣誉排行
		// 活动期间使用内存数据
		act=(ServerRanklistActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NEW_SERV_HONOR,0);
		if(act!=null&&act.isOpen(nowTime)
			&&act.isUpdateRankFromActivity(nowTime,FLUSH_TIME))
		{
			act.resetRankList(honorScoreIds,nowTime,objectFactory,MAX_SIZE);
		}
		else if(isDBRankFlush(lastTime,nowTime))
		{
			// 荣誉排行 有玩家删除就不加入排行榜
			sql="SELECT id,player_name,level,honorScore FROM players where deleteTime=0  ORDER BY honorScore DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,honorScoreIds);
			isFromDB=true;
		}
		// 成就排行
		if(isDBRankFlush(lastTime,nowTime))
		{
			// 成就排行 有玩家删除就不加入排行榜
			sql="SELECT id,player_name,level,achieveScore FROM players where deleteTime=0 ORDER BY achieveScore DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,achieveScore);
			isFromDB=true;
		}
		if(isFromDB) lastTime=nowTime;
	}

	/** 刷新自己的排名 */
	public void flushSelfRank(Player player)
	{
		int nowTime=TimeKit.getSecondTime();
		if(!isDBRankFlush(player.getLastRankTime(),nowTime)) return;
		String sql=null;
		Fields field=null;
		// 战力
		SeaBackKit.setPlayerFightScroe(player,objectFactory);
		// 活动期间使用内存数据(前100名)
		boolean isFound=false;
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.NEW_SERV_FIGHT))
		{
			int i=0;
			for(;i<playerIdsFight.size();i++)
			{
				// 模拟数据库排序
				if(player.getFightScore()<((RankInfo)playerIdsFight.get(i)).rankInfo
					&&playerIdsFight.size()-1<i
					&&player.getFightScore()>=((RankInfo)playerIdsFight
						.get(i+1)).rankInfo) break;
			}
			// 如果处于前100名,取排行榜排名
			if(i<playerIdsFight.size())
			{
				player.setFightScoreRank(i+1);
				isFound=true;
			}
		}
		if(!isFound)
		{
			sql="SELECT COUNT(*) FROM players WHERE fightScore>"
				+player.getFightScore();
			field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
			int fightScoreRank=Integer.parseInt(field.getArray()[0]
				.getValue().toString());
			player.setFightScoreRank(fightScoreRank+1);
		}
		// 关卡
		// 活动期间使用内存数据(前100名)
		isFound=false;
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.NEW_SERV_POINT))
		{
			int i=0;
			for(;i<playerIdsPlunder.size();i++)
			{
				//模拟数据库排序
				if(player.getPlunderResource()<((RankInfo)playerIdsPlunder
					.get(i)).rankInfo
					&&playerIdsPlunder.size()-1<i
					&&player.getPlunderResource()>=((RankInfo)playerIdsPlunder
						.get(i+1)).rankInfo) break;
			}
			// 如果处于前100名,取排行榜排名
			if(i<playerIdsPlunder.size())
			{
				player.setPlunderRank(i+1);
				isFound=true;
			}
		}
		if(!isFound)
		{
			sql="SELECT COUNT(*) FROM players WHERE plunderResource>"
				+player.getPlunderResource();
			field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
			int plunderResourceRank=Integer.parseInt(field.getArray()[0]
				.getValue().toString());
			player.setPlunderRank(plunderResourceRank+1);
		}
		// 荣誉
		// 活动期间使用内存数据(前100名)
		isFound=false;
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.NEW_SERV_HONOR))
		{
			int i=0;
			for(;i<honorScoreIds.size();i++)
			{
				//模拟数据库排序
				if(player.getHonorScore()<((RankInfo)honorScoreIds.get(i)).rankInfo
					&&honorScoreIds.size()-1<i
					&&player.getHonorScore()>=((RankInfo)honorScoreIds
						.get(i+1)).rankInfo) break;
			}
			// 如果处于前100名,取排行榜排名
			if(i<honorScoreIds.size())
			{
				player.setHonorScoreRank(i+1);
				isFound=true;
			}
		}
		if(!isFound)
		{// 荣誉排行
			sql="SELECT COUNT(*) FROM players WHERE honorScore>"
				+player.getHonorScore();
			field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
			int honorScoreRank=Integer.parseInt(field.getArray()[0]
				.getValue().toString());
			player.setHonorScoreRank(honorScoreRank+1);
		}
		// 成就排行
		sql="SELECT COUNT(*) FROM players WHERE achieveScore>"
			+player.getAchieveScore();
		field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
		int achieveScoreRank=Integer.parseInt(field.getArray()[0].getValue()
			.toString());
		player.setAchieveScoreRank(achieveScoreRank+1);

		player.setLastRankTime(nowTime);
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public static class RankInfo
	{

		/** 玩家id */
		int playerId;
		/** 玩家名字 */
		String playerName;
		/** 玩家等级 */
		int playerLevel;
		/** 排行数据 */
		long rankInfo;

		public RankInfo(String playerName,int playerLevel,long rankInfo)
		{
			this.playerName=playerName;
			this.playerLevel=playerLevel;
			this.rankInfo=rankInfo;
		}

		public int getPlayerId()
		{
			return playerId;
		}

		public void setPlayerId(int playerId)
		{
			this.playerId=playerId;
		}

		/**
		 * @return playerLevel
		 */
		public int getPlayerLevel()
		{
			return playerLevel;
		}

		/**
		 * @param playerLevel 要设置的 playerLevel
		 */
		public void setPlayerLevel(int playerLevel)
		{
			this.playerLevel=playerLevel;
		}

		/**
		 * @return playerName
		 */
		public String getPlayerName()
		{
			return playerName;
		}

		/**
		 * @param playerName 要设置的 playerName
		 */
		public void setPlayerName(String playerName)
		{
			this.playerName=playerName;
		}

		/**
		 * @return rankInfo
		 */
		public long getRankInfo()
		{
			return rankInfo;
		}

		/**
		 * @param rankInfo 要设置的 rankInfo
		 */
		public void setRankInfo(long rankInfo)
		{
			this.rankInfo=rankInfo;
		}
	}

	/** 设置时钟 */
	public void init()
	{
		nextTime=SeaBackKit.getSomedayEnd(0);
		TimerCenter.getMinuteTimer()
			.add(new TimerEvent(this,"rank",60*1000));
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(TimeKit.getSecondTime()>nextTime)
		{
			rankLog();
			nextTime+=PublicConst.DAY_SEC;
		}

	}

	/** 刷新输出各排行榜 */
	public void rankLog()
	{
		lastTime=0;
		flushFileds();
		log(playerIdsPlunder,"checkpoit");
		log(playerIdsFight,"fightscore");
		log(honorScoreIds,"honor");
		log(achieveScore,"achieve");
	}

	/** 输出某排行日志 */
	public void log(ArrayList list,String type)
	{
		StringBuffer strb=new StringBuffer();
		strb.append("=======rank type:");
		strb.append(type);
		log.error(strb.toString());
		strb.setLength(0);
		String rank="rank:";
		String name="  name:";
		String value="   value:";
		String lev="   lev:";
		for(int i=0;i<list.size();i++)
		{
			RankInfo info=(RankInfo)list.get(i);
			if(rank==null) continue;
			strb.append(rank);
			strb.append(i+1);
			strb.append(name);
			strb.append(info.getPlayerName());
			strb.append(lev);
			strb.append(info.getPlayerLevel());
			strb.append(value);
			strb.append(info.getRankInfo());
			log.error(strb.toString());
			strb.setLength(0);
		}
	}

	public ArrayList getPlayerIdsPlunder()
	{
		return playerIdsPlunder;
	}

	public ArrayList getPlayerIdsFight()
	{
		return playerIdsFight;
	}

	public ArrayList getHonorScoreIds()
	{
		return honorScoreIds;
	}

	public ArrayList getAchieveScore()
	{
		return achieveScore;
	}

}
