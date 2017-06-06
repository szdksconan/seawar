package foxu.sea.port;

import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.RankManager;
import foxu.sea.kit.SeaBackKit;

/** 排行榜端口 1008 每一个小时更新一次 */
public class RankPort extends AccessPort implements TimerListener
{
	/** 分页大小 */
	public static final int PAGE_SIZE=20;
	/** VIEW_MAX_FIGHT=1最高战斗力 常量VIEW_PLUNDER=2掠夺资源排行  VIES_ACHIEVE成就积分排名    */
	public static final int VIEW_MAX_FIGHT=1,VIEW_PLUNDER=2,VIEW_HONOR=3,VIEW_ACHIEVE=4;
	public static final int FLUSH_TIME=60*60*2;
	/** 数据获取类 */
	CreatObjectFactory objectFactory;

	/** 上一次刷新的时间 */
	int lastTime;
	/** 掠夺资源的排行 */
	ArrayList playerIdsPlunder=new ArrayList();
	/** 最高战力的排行 */
	ArrayList playerIdsFight=new ArrayList();
	/**荣誉积分的排行*/
	ArrayList honorScoreIds = new ArrayList();
	/** 成就积分排名 */
	ArrayList achieveScore = new  ArrayList();
	
	Logger log=LogFactory.getLogger(RankPort.class);
	/** 下次 排行日志输出时间 */
	int nextTime;

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		// 分页 每页10个
		int pageIndex=data.readUnsignedByte();
		// 支持前100名的排行
		if(pageIndex>4||pageIndex<0) return null;
		data.clear();
//		flushFileds();
//		flushSelfRank(player);
		// 掠夺资源排行 改为星级
		if(type==VIEW_PLUNDER)
		{
//			int num=playerIdsPlunder.size()-pageIndex*PAGE_SIZE;
//			if(num<0) num=0;
//			if(num>PAGE_SIZE) num=PAGE_SIZE;
//			data.writeByte(num);
//			for(int i=pageIndex*PAGE_SIZE;i<(pageIndex+1)*PAGE_SIZE;i++)
//			{
//				if(i>=playerIdsPlunder.size()) break;
//				RankInfo playerRank=(RankInfo)playerIdsPlunder.get(i);
//				data.writeUTF(playerRank.getPlayerName());
//				data.writeByte(playerRank.getPlayerLevel());
//				data.writeLong(playerRank.getRankInfo());
//			}
//			// 自己的掠夺资源排名
//			data.writeInt(player.getPlunderRank());
//			data.writeLong(player.getPlunderResource());
			RankManager.getInstance().getCheckPointRank(player,pageIndex,data);
		}
		// 最高战斗力排行
		else if(type==VIEW_MAX_FIGHT)
		{
//			int num=playerIdsFight.size()-pageIndex*PAGE_SIZE;
//			if(num<0) num=0;
//			if(num>PAGE_SIZE) num=PAGE_SIZE;
//			data.writeByte(num);
//			for(int i=pageIndex*PAGE_SIZE;i<(pageIndex+1)*PAGE_SIZE;i++)
//			{
//				if(i>=playerIdsFight.size()) break;
//				RankInfo playerRank=(RankInfo)playerIdsFight.get(i);
//				data.writeUTF(playerRank.getPlayerName());
//				data.writeByte(playerRank.getPlayerLevel());
//				data.writeInt((int)playerRank.getRankInfo());
//			}
//			// 自己的战斗力排名
//			data.writeInt(player.getFightScoreRank());
//			data.writeInt(player.getFightScore());
			RankManager.getInstance().getFightScoreRank(player,pageIndex,data);
		}
		//荣誉排行
		else if(type==VIEW_HONOR)
		{
//			int num=honorScoreIds.size()-pageIndex*PAGE_SIZE;
//			if(num<0) num=0;
//			if(num>PAGE_SIZE) num=PAGE_SIZE;
//			data.writeByte(num);
//			for(int i=pageIndex*PAGE_SIZE;i<(pageIndex+1)*PAGE_SIZE;i++)
//			{
//				if(i>=honorScoreIds.size()) break;
//				RankInfo playerRank=(RankInfo)honorScoreIds.get(i);
//				data.writeUTF(playerRank.getPlayerName());
//				data.writeByte(playerRank.getPlayerLevel());
//				data.writeInt((int)playerRank.getRankInfo());
//			}
//			// 自己的战斗力排名
//			data.writeInt(player.getHonorScoreRank());
//			data.writeInt(player.getHonorScore());
			RankManager.getInstance().getHonorRank(player,pageIndex,data);
		}
		else if(type==VIEW_ACHIEVE)
		{
//			int num=achieveScore.size()-pageIndex*PAGE_SIZE;
//			if(num<0) num=0;
//			if(num>PAGE_SIZE) num=PAGE_SIZE;
//			data.writeByte(num);
//			for(int i=pageIndex*PAGE_SIZE;i<(pageIndex+1)*PAGE_SIZE;i++)
//			{
//				if(i>=achieveScore.size()) break;
//				RankInfo playerRank=(RankInfo)achieveScore.get(i);
//				data.writeUTF(playerRank.getPlayerName());
//				data.writeByte(playerRank.getPlayerLevel());
//				data.writeInt((int)playerRank.getRankInfo());
//			}
//			// 自己成就积分排名
//			data.writeInt(player.getAchieveScoreRank());
//			data.writeInt(player.getAchieveScore());
			RankManager.getInstance().getAchieveRank(player,pageIndex,data);
			
		}
		return data;
	}

	/** 根据sql语言返回玩家名字的集合 */
	public void flushFileds()
	{
		int nowTime=TimeKit.getSecondTime();
		if(lastTime>nowTime) return;
		lastTime=nowTime+FLUSH_TIME;
		//有玩家删除就不加入排行榜
		String sql="SELECT player_name,level,plunderResource FROM players where deleteTime=0 ORDER BY plunderResource DESC LIMIT 100";
		Fields fields[]=((PlayerGameDBAccess)objectFactory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		playerIdsPlunder.clear();
		for(int i=0;i<fields.length;i++)
		{
			RankInfo rank=new RankInfo(fields[i].getArray()[0].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[1]
				.getValue().toString()),Integer.parseInt(fields[i]
				.getArray()[2].getValue().toString()));
			playerIdsPlunder.add(rank);
		}
		//有玩家删除就不加入排行榜
		sql="SELECT player_name,level,fightScore FROM players where deleteTime=0 ORDER BY fightScore DESC LIMIT 100";
		fields=((PlayerGameDBAccess)objectFactory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		playerIdsFight.clear();
		for(int i=0;i<fields.length;i++)
		{
			RankInfo rank=new RankInfo(fields[i].getArray()[0].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[1]
				.getValue().toString()),Integer.parseInt(fields[i]
				.getArray()[2].getValue().toString()));
			playerIdsFight.add(rank);
		}
		//荣誉排行 有玩家删除就不加入排行榜
		sql="SELECT player_name,level,honorScore FROM players where deleteTime=0  ORDER BY honorScore DESC LIMIT 100";
		fields=((PlayerGameDBAccess)objectFactory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		honorScoreIds.clear();
		for(int i=0;i<fields.length;i++)
		{
			RankInfo rank=new RankInfo(fields[i].getArray()[0].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[1]
				.getValue().toString()),Integer.parseInt(fields[i]
				.getArray()[2].getValue().toString()));
			honorScoreIds.add(rank);
		}
		
		//成就排行 有玩家删除就不加入排行榜
		sql="SELECT player_name,level,achieveScore FROM players where deleteTime=0 ORDER BY achieveScore DESC LIMIT 100";
		fields=((PlayerGameDBAccess)objectFactory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		achieveScore.clear();
		for(int i=0;i<fields.length;i++)
		{
			RankInfo rank=new RankInfo(fields[i].getArray()[0].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[1]
				.getValue().toString()),Integer.parseInt(fields[i]
				.getArray()[2].getValue().toString()));
			achieveScore.add(rank);
		}
	}
	
	/** 刷新自己的排名 */
	public void flushSelfRank(Player player)
	{
		int nowTime=TimeKit.getSecondTime();
		if(player.getLastRankTime()>nowTime) return;
		SeaBackKit.setPlayerFightScroe(player,objectFactory);
		player.setLastRankTime(nowTime+FLUSH_TIME);
		String sql="SELECT COUNT(*) FROM players WHERE fightScore>"
			+player.getFightScore();
		Fields field=objectFactory.getPlayerCache().getDbaccess().loadSql(
			sql);
		int fightScoreRank=Integer.parseInt(field.getArray()[0].getValue()
			.toString());
		player.setFightScoreRank(fightScoreRank+1);
		sql="SELECT COUNT(*) FROM players WHERE plunderResource>"
			+player.getPlunderResource();
		field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
		int plunderResourceRank=Integer.parseInt(field.getArray()[0]
			.getValue().toString());
		player.setPlunderRank(plunderResourceRank+1);
		//荣誉排行
		sql="SELECT COUNT(*) FROM players WHERE honorScore>"
			+player.getHonorScore();
		field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
		int honorScoreRank=Integer.parseInt(field.getArray()[0]
			.getValue().toString());
		player.setHonorScoreRank(honorScoreRank+1);
		
		//成就排行
		sql="SELECT COUNT(*) FROM players WHERE achieveScore>"
			+player.getAchieveScore();
		field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
		int achieveScoreRank=Integer.parseInt(field.getArray()[0]
			.getValue().toString());
		player.setAchieveScoreRank(achieveScoreRank+1);
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
		int pid;
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
		
		public int getPid()
		{
			return pid;
		}
		
		public void setPid(int pid)
		{
			this.pid=pid;
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
		String id="  playerId:";
		String name="  name:";
		String value="   value:";
		String lev="   lev:";
		for(int i=0;i<list.size();i++)
		{
			RankInfo info=(RankInfo)list.get(i);
			if(rank==null)continue;
			strb.append(rank);
			strb.append(i+1);
			strb.append(id);
			strb.append(info.getPid());
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

}
