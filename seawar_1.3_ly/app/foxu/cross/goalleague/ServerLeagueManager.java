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
 * ������������������
 * 
 * @author Alan
 * 
 */
public class ServerLeagueManager implements TimerListener,Comparator
{

	private static final Logger log=LogFactory
		.getLogger(ServerLeagueManager.class);
	/**
	 * LEAGUE_STAUT_CLOSE=0 �����ر�, LEAGUE_STAUT_OPEN=1, ��������
	 * LEAGUE_STAUT_RANK=2 �������а�, LEAGUE_STAUT_COPY_IN=3 ����AI����,
	 * LEAGUE_STAUT_COPY_OUT=4 �·�AI����, LEAGUE_STAUT_PLAYER_INFO=5 �ռ����������Ϣ,
	 * LEAGUE_INFO=6 ��ȡ������Ϣ, LEAGUE_CENTER_INFO=7 �·�������������Ϣ,
	 * LEAGUE_STAUT_SETTLE=100 ��ĩ����
	 */
	public static final int LEAGUE_STAUT_CLOSE=0,LEAGUE_STAUT_OPEN=1,
					LEAGUE_STAUT_RANK=2,LEAGUE_STAUT_COPY_IN=3,
					LEAGUE_STAUT_COPY_OUT=4,LEAGUE_STAUT_PLAYER_INFO=5,
					LEAGUE_INFO=6,LEAGUE_CENTER_INFO=7,
					LEAGUE_SETTLE_AWARD=8,LEAGUE_STAUT_SETTLE=100;
	public static int RANK_UPDATE_TIME=60*60;
	/** ���а����� */
	public static int RANK_COUNT=200;
	/**
	 * BATTLE_INTERAL_COUNT=ÿ����ս����,BATTLE_FLUSH_COUNT=ÿ�����ˢ�´���,
	 * BATTLE_FLUSH_GEMS=�б�ˢ�����ı�ʯ
	 */
	public static int BATTLE_INTERAL_COUNT=9,BATTLE_FLUSH_COUNT=5,
					BATTLE_FLUSH_GEMS=30;
	/** ����ս�����ı�ʯ */
	public static int[] BATTLE_FIGHT_GEMS;
	/** ��սĿ��ѡȡս���ֶ�����[��ʼ�ٷֱ�,��ֹ�ٷֱ�] */
	public static int[] SELECT_FIGHT_SCORE_RANGE={0,75,75,90,90,
		Integer.MAX_VALUE};
	/** �����̵���Ʒ[sid,coins,count,ϡ�еȼ�] */
	public static int[] LEAGUE_SHOP;
	/** AI�������Ƽ��� */
	public static int COPY_LIMIT_LV=50;
	/** ����������Ϣ(����Ӧ��Ŀ��ѡȡ�ֶ�����������ͬ)[����������ʼ����,����������ֹ����,��ұ�������,...] */
	public static int[] COPY_RANGE_INFO={0,50,300,50,75,300,75,100,300};
	/** ��ս����ʹ�ó��������õı��� */
	public static int COPY_RANGE_RESET_PERCENT=75;
	/** ���������������������� */
	public static int OPEN_DAY_WEEK=1,COPY_CLIENT_PLAYER_DAY_WEEK=6,
					SETTLE_DAY_WEEK=5;
	/** ���������ӳ�ʱ��,Сʱ */
	public static int LEAGUE_AWARD_TIME_DELAY=12;
	/** �ͻ���http����˿ڴ��� */
	public static int CLIENT_HTTP_ACTION_PORT=12,CLIENT_HTTP_ACTION_TYPE=1,
					SERVER_HTTP_ACTION_TYPE=2;
	/** ս�������Ƿ���� */
	boolean isWarActive;
	/** ��һ�����ð񵥵�ʱ�� */
	int lastRankTime;
	/** ϵͳ����״̬ */
	boolean isOpen;
	/** �������б� */
	ArrayList serverlist=new ArrayList();
	/** ���ְ� */
	ArrayList rankList=new ArrayList();
	/** ��ǰ��ս�б� */
	ArrayList currentCopyList=new ArrayList();
	/** Ԥ����ս�б� */
	ArrayList readyCopyList=new ArrayList();
	/** �̵���Ʒ�б� */
	ArrayList sellList=new ArrayList();
	/** ϵͳ����׼��״̬ */
	boolean isReady;
	/** �´ο���ʱ�� */
	int nextCopyTime;
	/** �´ο���ʱ�� */
	int nextOpenTime;
	/** ����ֹͣ����ʱ�� */
	int currentActiveEndTime;
	/** ��������id */
	int leagueId;
	/** ���������Ƿ��ѷ��� */
	boolean isAward;
	/** UID�ṩ�� */
	UidKit uidkit;
	/** ������ */
	byte[] copyLock=new byte[0];
	/** ��������� */
	boolean isCopyLock;
	/** ��������� */
	boolean isRankLock;
	CrossWarPTComparator cptc=new CrossWarPTComparator();
	IntKeyHashMap failDatas=new IntKeyHashMap();
	CrossCopyPlayerDBAccess warDBAccess;

	TimerEvent timer=new TimerEvent(this,"main",1000);
	TimerEvent readyCopyTimer=new TimerEvent(this,"readyCopy",60*1000);
	TimerEvent resendTimer=new TimerEvent(this,"resend",5*1000);
	/** �������� */
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
		// �̵��б�
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
		// �����ǰʱ���Ѿ����˽���ʱ��,��ô���ٽ��л��������,�ȴ���һ�λ����
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
		// ������ʼ����
		ByteBuffer data=new ByteBuffer();
		bytesWriteLeagueOpen(data);
		sendLeaguaStaut(LEAGUE_STAUT_OPEN,data);
		saveCurrentLeague();
	}

	/** ��������״̬ */
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
			// ��������
			if(result==null)
			{
				log.error("send staut:"+staut+" to server fail:"+cs.getIp()
					+","+cs.getPort());
				failDatas.put(cs.getSid(),new FailSendEntry(cs,temp));
			}
		}
	}

	/** ����Ԥ����ս�б� */
	public void sendReadyCopyList()
	{
		ByteBuffer data=new ByteBuffer();
		bytesWriteTargetList(data,readyCopyList);
		sendLeaguaStaut(LEAGUE_STAUT_COPY_OUT,data);
	}

	/** �ռ�������ս�б� */
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
				// ��ս�б�����
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
		// ��������
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
					// ����ɹ�����,���Ƴ�
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
		// ����״̬����
		collateLeagueInfo(time);
		// �������а�
		if(lastRankTime+RANK_UPDATE_TIME<=time)
		{
			collectServerInfo(time,false);
			sendCenterInfo();
		}
		collateLeagueInfo(time,isResetRank);*/
	}

	/** ��ʼ����������Ϣ */
	public void initCurrentLeague()
	{
		byte[] arr=ContextVarManager.getInstance().getVarData(
			ContextVarManager.CROSS_LEAGUE_SERVER_INFO);
		if(arr==null) return;
		ByteBuffer data=new ByteBuffer(arr);
		// ������Ϣ
		if(isOpen)
		{
			leagueId=data.readInt();
			currentActiveEndTime=data.readInt();
			nextOpenTime=data.readInt();
			bytesReadRankList(data);
			// ��ǰ�б��Ƿ����
			isWarActive=data.readBoolean();
			isAward=data.readBoolean();
			if(isWarActive)
			{
				currentCopyList.clear();
				// ��ǰ�б�
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
		// �·�ͬ������
		data.clear();
		bytesWriteLeagueInfo(data);
		sendLeaguaStaut(LEAGUE_INFO,data);
	}

	/** ���浱ǰ������Ϣ */
	public void saveCurrentLeague()
	{
		ByteBuffer data=new ByteBuffer();
		// ������Ϣ
		if(isOpen)
		{
			data.writeInt(leagueId);
			data.writeInt(currentActiveEndTime);
			data.writeInt(nextOpenTime);
			bytesWriteRankList(data);
			// ��ǰ�б��Ƿ����
			data.writeBoolean(isWarActive);
			// ��ǰ�����Ƿ��ѽ���
			data.writeBoolean(isAward);
			ContextVarManager.getInstance().setVarData(
				ContextVarManager.CROSS_LEAGUE_SERVER_INFO,data.toArray());
			warDBAccess.deleteAll();
			if(isWarActive)
			{
				// ��ǰ�б�
				CrossWarPlayer cp=null;
				for(int i=0;i<currentCopyList.size();i++)
				{
					cp=(CrossWarPlayer)currentCopyList.get(i);
					warDBAccess.save(cp);
				}
			}
		}
	}

	/** ����������Ϣ,���㡢���� */
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
			// ��ʼ
			open(time);
		}
	}

	/** �������� */
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
				// ��������
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
					// Ԥ���ķ�����sid
					if(cs.getSid()==cp.getServerSid()) break;
					cs=null;
				}
				// ����������
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
				// ���id
				data.writeInt(cp.getId());
				// �������
				data.writeUTF(cp.getName());
				// �������
				data.writeShort(ranking);
				// ����sid
				data.writeShort(awardSid);
				sendHttpData(data,cs.getIp(),cs.getPort());
			}
			isAward=true;
			saveCurrentLeague();
		}
	}

	/** �·�������������Ϣ */
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

	/** �ռ����ϸ������������Ϣ */
	public void collectServerInfo(int time,boolean isSettleDown)
	{
		// �����ǰΪ������������,�򲻽�������
		if(!isOpen||(!isWarActive&&!isSettleDown))
		{
			lastRankTime=time;
			return;
		}
		// ���Ƚ��о��񿽱�
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
				// ��ʱid,��������
				int ident=1;
				for(int i=0;i<serverlist.size();i++)
				{
					data.clear();
					cs=(CrossServer)serverlist.get(i);
					data.writeByte(LEAGUE_STAUT_PLAYER_INFO);
					// �Ƿ��ǽ���ʱ��ȡ
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
				// �������а�
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

	/** �������а� */
	public void resetRankList(ArrayList playerList)
	{
		// ��ǰ���
		CrossWarPlayer player=null;
		// ��ʱ�÷�������
		CrossWarPlayer target=null;
		// �Ƿ��״�ѭ��(���ѭ�����ڱ����״θ�ֵ��һЩ����)
		boolean isFirstTime=true;
		// ��һ����ߵ÷�
		long lastScore=0;
		// �ܼ�¼��
		int recordSize=0;
		IntKeyHashMap record=new IntKeyHashMap();
		record.clear();
		while(playerList.size()>recordSize)
		{
			target=null;
			// ��ʱ��ߵ÷�
			long rankScore=0;
			// ��ǰ����
			int ranking=record.size();
			// ��ȡ��һ�������б�(���������߷�����ͬ��ֱ�����,������ʵ����һ���б�)
			IntKeyHashMap list=(IntKeyHashMap)record.get(ranking);
			for(int j=0;j<playerList.size();j++)
			{
				player=(CrossWarPlayer)playerList.get(j);
				if(player==null) continue;
				long playerScore=player.getGoal();
				if(rankScore==0)
				{
					// ��һ��ֱ�Ӹ�ֵor��һ�ֵ÷ִ��ڵ��ڵ�ǰ��ҵ÷�ʱ
					if(isFirstTime||lastScore>=playerScore)
					{
						// �жϵ�ǰ����Ƿ��Ѿ���¼����
						if(!isFirstTime&&lastScore==playerScore&&list!=null
							&&list.get(player.getCrossid())!=null) continue;
						rankScore=playerScore;
						target=player;
					}
					continue;
				}
				if(playerScore<rankScore) continue;
				// ���С����һ����ߵ÷ֲ��Ҹ�����ʱ��ߵ÷�,��ʱ��������
				// ����,���������һ����ߵ÷ֲ�����һ���޼�¼,��������ʵ�������б�,��ʱ��������
				if((((isFirstTime||lastScore>playerScore)&&rankScore<playerScore))
					||(list!=null&&lastScore==playerScore&&list.get(player
						.getCrossid())==null))
				{
					rankScore=playerScore;
					target=player;
					// ������ֱ����������Ч������һ����ͬ,�������������
					if(lastScore==playerScore) break;
				}
			}
			// �Ҳ��������Ŀ�����,����ǰ�÷��������,����Ҫ��������
			if(target==null) break;
			// ������ֱ����������Ч������һ�ֲ�ͬ,������ʵ����һ���б�
			// Ҳ���·���������ҵ�ĳ���÷ֶ�Ϊ0,��ôҲӦ��Ҫ�Ž�����
			if(lastScore!=rankScore||(isFirstTime&&rankScore==0))
			{
				list=new IntKeyHashMap();
				ranking++;
			}
			lastScore=rankScore;
			list.put(target.getCrossid(),target);
			recordSize++;
			record.put(ranking,list);
			// �÷���ͬʱ,���������������,����������Ϊѭ���˳����ж�
			// �����ʱ�ҵ��ļ�¼���Ѿ����ϳ���,����Ҫ��������
			if(ranking>=RANK_COUNT) break;
			if(isFirstTime) isFirstTime=false;
		}
		rankList.clear();
		// ����������
		IntKeyHashMap ihm=null;
		Object[] ps=null;
		// ����Ϊkey,��1��ʼ
		for(int i=1;i<=record.size();i++)
		{
			ihm=(IntKeyHashMap)record.get(i);
			if(ihm==null) continue;
			ps=ihm.valueArray();
			SetKit.sort(ps,cptc);// ��������
			for(int j=0;j<ps.length;j++)
			{
				if(ps[j]==null) continue;
				rankList.add(ps[j]);
				if(rankList.size()>=RANK_COUNT) break;
			}
			if(rankList.size()>=RANK_COUNT) break;
		}
	}

	/** �ر����� */
	public void closeSystem()
	{
		isOpen=false;
		sendLeaguaStaut(LEAGUE_STAUT_CLOSE,null);
	}

	/** ��ȡ��Ҹ�����Ϣ */
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

	/** ��ȡ���������Ϣ */
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
			// Ԥ��������sid
			cp.setServerSid(cs.getSid());
			list.add(cp);
		}
		return list;
	}

	/** ���л�������ʼ��Ϣ */
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

	/** ���л���ս�б� */
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

	/** ���л����а��б� */
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

	/** �����л����а��б� */
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

	/** ���л�����������Ϣ */
	public void bytesWriteLeagueInfo(ByteBuffer data)
	{
		// ���ܿ�����ʶ
		data.writeBoolean(isOpen);
		if(!isOpen) return;
		bytesWriteLeagueOpen(data);
		// Ԥ���б��Ƿ����
		data.writeBoolean(isReady);
		if(isReady) bytesWriteTargetList(data,readyCopyList);
		// ��ǰ�б��Ƿ����
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
		// ����port
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
		// 15���ڵ�ʱ���ٽ���ͬ��
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

	/** ʧ�ܵķ���,�������Ͷ��� */
	class FailSendEntry
	{

		CrossServer cs;
		ByteBuffer data;
		int count=10;

		/** ʧ�ܵķ���,�������Ͷ��� */
		public FailSendEntry(CrossServer cs,ByteBuffer data)
		{
			super();
			this.cs=cs;
			this.data=data;
		}

	}
}
