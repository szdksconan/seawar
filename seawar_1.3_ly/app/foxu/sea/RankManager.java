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

	/** ��ҳ��С */
	public static final int PAGE_SIZE=20;
	public static final int MAX_SIZE=100;
	public static final int FLUSH_TIME=60*60*2,MEM_FLUSH_TIME=5*60;
	private static RankManager manager=new RankManager();
	/** ���ݻ�ȡ�� */
	CreatObjectFactory objectFactory;

	/** ��һ��ˢ�µ�ʱ�� */
	int lastTime;
	/** �Ӷ���Դ������ */
	ArrayList playerIdsPlunder=new ArrayList();
	/** ���ս�������� */
	ArrayList playerIdsFight=new ArrayList();
	/** �������ֵ����� */
	ArrayList honorScoreIds=new ArrayList();
	/** �ɾͻ������� */
	ArrayList achieveScore=new ArrayList();

	Logger log=LogFactory.getLogger(RankManager.class);
	/** �´� ������־���ʱ�� */
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
		// �Լ���ս��������
		data.writeInt(player.getFightScoreRank());
		data.writeInt(player.getFightScore());
	}

	public void getCheckPointRank(Player player,int pageIndex,ByteBuffer data)
	{
		getInfoFromList(player,pageIndex,playerIdsPlunder,data,true);
		// �Լ����Ӷ���Դ����
		data.writeInt(player.getPlunderRank());
		data.writeLong(player.getPlunderResource());
	}

	public void getHonorRank(Player player,int pageIndex,ByteBuffer data)
	{
		getInfoFromList(player,pageIndex,honorScoreIds,data,false);
		// �Լ�����������
		data.writeInt(player.getHonorScoreRank());
		data.writeInt(player.getHonorScore());
	}

	public void getAchieveRank(Player player,int pageIndex,ByteBuffer data)
	{
		getInfoFromList(player,pageIndex,achieveScore,data,false);
		// �Լ��ɾͻ�������
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
			// �����ǰ��һ�û������ָ��������򲻽����б�
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
	
	/** ����sql���Է���������ֵļ��� */
	public void flushFileds()
	{
		int nowTime=TimeKit.getSecondTime();
		String sql=null;
		ServerRanklistActivity act=null;
		boolean isFromDB=false;
		// �ؿ�����
		// ��ڼ�ʹ���ڴ�����
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
			// �����ɾ���Ͳ��������а�
			sql="SELECT id,player_name,level,plunderResource FROM players where deleteTime=0 ORDER BY plunderResource DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,playerIdsPlunder);
			isFromDB=true;
		}
		// ս������
		// ��ڼ�ʹ���ڴ�����
		act=(ServerRanklistActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NEW_SERV_FIGHT,0);
		if(act!=null&&act.isOpen(nowTime)
			&&act.isUpdateRankFromActivity(nowTime,FLUSH_TIME))
		{
			act.resetRankList(playerIdsFight,nowTime,objectFactory,MAX_SIZE);
		}
		else if(isDBRankFlush(lastTime,nowTime))
		{
			// �����ɾ���Ͳ��������а�
			sql="SELECT id,player_name,level,fightScore FROM players where deleteTime=0 ORDER BY fightScore DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,playerIdsFight);
			isFromDB=true;
		}
		// ��������
		// ��ڼ�ʹ���ڴ�����
		act=(ServerRanklistActivity)ActivityContainer.getInstance()
			.getActivity(ActivityContainer.NEW_SERV_HONOR,0);
		if(act!=null&&act.isOpen(nowTime)
			&&act.isUpdateRankFromActivity(nowTime,FLUSH_TIME))
		{
			act.resetRankList(honorScoreIds,nowTime,objectFactory,MAX_SIZE);
		}
		else if(isDBRankFlush(lastTime,nowTime))
		{
			// �������� �����ɾ���Ͳ��������а�
			sql="SELECT id,player_name,level,honorScore FROM players where deleteTime=0  ORDER BY honorScore DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,honorScoreIds);
			isFromDB=true;
		}
		// �ɾ�����
		if(isDBRankFlush(lastTime,nowTime))
		{
			// �ɾ����� �����ɾ���Ͳ��������а�
			sql="SELECT id,player_name,level,achieveScore FROM players where deleteTime=0 ORDER BY achieveScore DESC,id ASC LIMIT "
				+MAX_SIZE;
			resetRankFromDB(sql,achieveScore);
			isFromDB=true;
		}
		if(isFromDB) lastTime=nowTime;
	}

	/** ˢ���Լ������� */
	public void flushSelfRank(Player player)
	{
		int nowTime=TimeKit.getSecondTime();
		if(!isDBRankFlush(player.getLastRankTime(),nowTime)) return;
		String sql=null;
		Fields field=null;
		// ս��
		SeaBackKit.setPlayerFightScroe(player,objectFactory);
		// ��ڼ�ʹ���ڴ�����(ǰ100��)
		boolean isFound=false;
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.NEW_SERV_FIGHT))
		{
			int i=0;
			for(;i<playerIdsFight.size();i++)
			{
				// ģ�����ݿ�����
				if(player.getFightScore()<((RankInfo)playerIdsFight.get(i)).rankInfo
					&&playerIdsFight.size()-1<i
					&&player.getFightScore()>=((RankInfo)playerIdsFight
						.get(i+1)).rankInfo) break;
			}
			// �������ǰ100��,ȡ���а�����
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
		// �ؿ�
		// ��ڼ�ʹ���ڴ�����(ǰ100��)
		isFound=false;
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.NEW_SERV_POINT))
		{
			int i=0;
			for(;i<playerIdsPlunder.size();i++)
			{
				//ģ�����ݿ�����
				if(player.getPlunderResource()<((RankInfo)playerIdsPlunder
					.get(i)).rankInfo
					&&playerIdsPlunder.size()-1<i
					&&player.getPlunderResource()>=((RankInfo)playerIdsPlunder
						.get(i+1)).rankInfo) break;
			}
			// �������ǰ100��,ȡ���а�����
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
		// ����
		// ��ڼ�ʹ���ڴ�����(ǰ100��)
		isFound=false;
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.NEW_SERV_HONOR))
		{
			int i=0;
			for(;i<honorScoreIds.size();i++)
			{
				//ģ�����ݿ�����
				if(player.getHonorScore()<((RankInfo)honorScoreIds.get(i)).rankInfo
					&&honorScoreIds.size()-1<i
					&&player.getHonorScore()>=((RankInfo)honorScoreIds
						.get(i+1)).rankInfo) break;
			}
			// �������ǰ100��,ȡ���а�����
			if(i<honorScoreIds.size())
			{
				player.setHonorScoreRank(i+1);
				isFound=true;
			}
		}
		if(!isFound)
		{// ��������
			sql="SELECT COUNT(*) FROM players WHERE honorScore>"
				+player.getHonorScore();
			field=objectFactory.getPlayerCache().getDbaccess().loadSql(sql);
			int honorScoreRank=Integer.parseInt(field.getArray()[0]
				.getValue().toString());
			player.setHonorScoreRank(honorScoreRank+1);
		}
		// �ɾ�����
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
	 * @param objectFactory Ҫ���õ� objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public static class RankInfo
	{

		/** ���id */
		int playerId;
		/** ������� */
		String playerName;
		/** ��ҵȼ� */
		int playerLevel;
		/** �������� */
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
		 * @param playerLevel Ҫ���õ� playerLevel
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
		 * @param playerName Ҫ���õ� playerName
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
		 * @param rankInfo Ҫ���õ� rankInfo
		 */
		public void setRankInfo(long rankInfo)
		{
			this.rankInfo=rankInfo;
		}
	}

	/** ����ʱ�� */
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

	/** ˢ����������а� */
	public void rankLog()
	{
		lastTime=0;
		flushFileds();
		log(playerIdsPlunder,"checkpoit");
		log(playerIdsFight,"fightscore");
		log(honorScoreIds,"honor");
		log(achieveScore,"achieve");
	}

	/** ���ĳ������־ */
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
