package foxu.sea.arena;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.ArenaSave;
import foxu.dcaccess.mem.ArenaMemCache;
import foxu.fight.FightScene;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.recruit.RecruitDayTask;
import foxu.sea.recruit.RecruitKit;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * �������������������������������ţ����ּ���
 * 
 * @author comeback
 * 
 */
public class ArenaManager implements TimerListener
{
	/** ����ʱ�� */
	public static int RESET_TIME=22*3600;
	/** ������Ҫ������ */
	public static int MESSAGE_RANKING=5;
	int nextAwardTime;

	/** ���������ݻ��� */
	ArenaMemCache memCache;

	CreatObjectFactory objectFactory;

	TimerEvent te;

	/**
	 * ��������һ��ӳ������㰴����ȡ����
	 */
	IntKeyHashMap sortedMap=new IntKeyHashMap();

	/** ���� */
	IntKeyHashMap reportsMap=new IntKeyHashMap();
	
	/** ���� */
	IntKeyHashMap reportIdMap=new IntKeyHashMap();
	
	/** ���θı�ͬ���� */
	Object rlock=new Object();
	
	Logger log=LogFactory.getLogger(ArenaManager.class);

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public void init(ArenaMemCache memCache)
	{
		this.memCache=memCache;

		Object[] objs=memCache.getCacheMap().valueArray();
		for(int i=0;i<objs.length;i++)
		{
			ArenaSave save=(ArenaSave)objs[i];
			SeawarGladiator gladiator=(SeawarGladiator)save.getData();
			sortedMap.put(gladiator.getRanking(),gladiator);
		}
		// ���´�����ÿ�ս���ʱ������Ϊ������ҹ
		nextAwardTime=updateDriveTime();
		te=new TimerEvent(this,"dailyAward",60*1000);
		TimerCenter.getMinuteTimer().add(te);
	}

	private SeawarGladiator createNewGladiator(int playerId)
	{
		SeawarGladiator sg=memCache.createObect(playerId);
		sortedMap.put(sg.getRanking(),sg);
		return sg;
	}
	public void collocate(int time)
	{
		// ��ȡ�������Ժ��ٽ��н��㣬�Ա������ʱ����ĳЩ���ڽ����е�ս��������Ϣ����ȷ
		synchronized(rlock)
		{
			synchronized(memCache)
			{
				StringBuffer record=new StringBuffer();
				Object[] objs=memCache.getCacheMap().valueArray();
				for(int i=0;i<objs.length;i++)
				{
					ArenaSave save=(ArenaSave)objs[i];
					SeawarGladiator gladiator=(SeawarGladiator)save
						.getData();
					gladiator.setLastDayRanking(gladiator.getRanking());
					gladiator.setAwardSid(getAwardSid(gladiator));
					if(gladiator.getLastDayRanking()<=ArenaHelper.DAILY_AWARD_MAX_RANKING)
					{
						record.append(gladiator.getLastDayRanking()+",");
						record.append(gladiator.getPlayerId()+":");
					}
					if(gladiator.getTodayBattleCount()>0)
					{
						memCache.getChangeListMap().put(
							gladiator.getPlayerId(),save);
					}
					// ������ս����
					gladiator.resetTodayBattleCount();
					// �����ϴ���սʱ��
					gladiator.setLastBattleTime(0);
					// ����ʱ���������Ϣ
					Player player=objectFactory.getPlayerById(gladiator
						.getPlayerId());
					if(player!=null)
						JBackKit.resetArenaInfo(player,getMainInfo(player));
				}
				log.error(record.toString());
				sendSystemMessage();
			}

		}
	}
	
	private void collocateReport(int playerId)
	{
		ArrayList list=(ArrayList)reportsMap.get(playerId);
		if(list==null)
		{
			return;
		}
		if(list.size()>ArenaHelper.MAX_REPORT_COUNT)
		{
			while(list.size()>ArenaHelper.MAX_REPORT_COUNT)
			{
				Message msg=(Message)list.remove(0);
				if(msg!=null)
					reportIdMap.remove(msg.getMessageId());
			}
		}
	}

	public void putReport(int playerId,Message message)
	{
		ArrayList list=(ArrayList)reportsMap.get(playerId);
		if(list==null)
		{
			list=new ArrayList();
			reportsMap.put(playerId,list);
		}
		reportIdMap.put(message.getMessageId(),message);
		list.add(message);
	}

	/**
	 * ��ȡָ����ҵ����б���
	 * 
	 * @param playerId
	 * @return
	 */
	public ByteBuffer getReports(Player player,int pageIndex,ByteBuffer data)
	{
		ArrayList list=(ArrayList)reportsMap.get(player.getId());
		if(list==null)
		{
			data.clear();
			data.writeByte(0);
			return data;
		}
		collocateReport(player.getId());
		Message[] msgs=new Message[list.size()];
		list.toArray(msgs);
		data.clear();
		int top=data.top();
		int count=0;
		data.writeByte(0);
		for(int i=pageIndex*10;i<(pageIndex+1)*10&&i<msgs.length;i++)
		{
			msgs[i].showBytesWrite(data,msgs[i].getRecive_state(),
				msgs[i].getContent(),player);
			count++;
		}
		if(count>0)
		{
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(count);
			data.setTop(newTop);
		}
		return data;
	}
	
	/**
	 * ��ȡָ��ID����ս����
	 * @param msgId
	 * @return
	 */
	public Message getReportObject(int msgId)
	{
		return (Message)reportIdMap.get(msgId);
	}

	/**
	 * ���ָ����ҵ�ս��
	 * 
	 * @param player
	 */
	public void clearReports(Player player)
	{
		ArrayList list=(ArrayList)reportsMap.get(player.getId());
		if(list==null)
		{
			return;
		}
		for(int i=list.size()-1;i>=0;i--)
		{
			Message msg=(Message)list.get(i);
			reportIdMap.remove(msg.getMessageId());
		}
		list.clear();
	}

	/**
	 * ɾ��ָ����ս��
	 * 
	 * @param player
	 * @param id
	 */
	public void removeReport(Player player,int id)
	{
		ArrayList list=(ArrayList)reportsMap.get(player.getId());
		if(list==null)
		{
			return;
		}
		Object[] objs=list.toArray();
		for(int i=0;i<objs.length;i++)
		{
			Message msg=(Message)objs[i];
			if(msg.getMessageId()==id)
			{
				list.remove(msg);
				reportIdMap.remove(msg.getMessageId());
				break;
			}
		}
	}

	/**
	 * ��ȡ�ʼ�����
	 * 
	 * @param player
	 * @return
	 */
	public ByteBuffer getReportCount(Player player,ByteBuffer data)
	{
		ArrayList list=(ArrayList)reportsMap.get(player.getId());
		data.clear();
		if(list==null||list.size()==0)
		{
			data.writeShort(0);
			data.writeShort(0);
			return data;
		}
		collocateReport(player.getId());
		Object[] objs=list.toArray();
		int readed=0;
		for(int i=0;i<objs.length;i++)
		{
			Message msg=(Message)objs[i];
			if(msg.getRecive_state()==Message.READ) readed++;
		}
		data.writeShort(objs.length);
		data.writeShort(readed);
		return data;
	}

	/**
	 * ��ȡ�ʼ�����
	 * 
	 * @param player
	 * @param msgId
	 * @param data
	 * @return
	 */
	public ByteBuffer getReportContent(Player player,int msgId,
		ByteBuffer data)
	{
		ArrayList list=(ArrayList)reportsMap.get(player.getId());
		if(list==null)
		{
			throw new DataAccessException(0,"message_not_exists");
		}
		Message msg=null;
		for(int i=list.size()-1;i>=0;i--)
		{
			Message m=(Message)list.get(i);
			if(m.getMessageId()==msgId)
			{
				msg=m;
				break;
			}
		}
		if(msg==null)
		{
			throw new DataAccessException(0,"message_not_exists");
		}
		data.clear();
		ByteBuffer temp=msg.getFightDataFore();
		data.write(temp.getArray(),temp.offset(),temp.length());
		temp=msg.getFightData();
		ByteBuffer tempMail=new ByteBuffer();
		// �ݲ������Ƿ����
		boolean isNullFightData=msg.getFightData()==null
			||msg.getFightData().length()<=0;
		tempMail.writeBoolean(!isNullFightData);
		// ��װս���汾
		tempMail.writeInt(msg.getFightVersion());
		if(!isNullFightData)
		{
			// ��װ�ݲ�����
			tempMail.write(msg.getFightData().getArray(),msg.getFightData()
				.offset(),msg.getFightData().length());
		}
		// ��װ������Ϣ
		tempMail.write(msg.getOfficerData().getArray(),msg.getOfficerData()
			.offset(),msg.getOfficerData().length());
		data.writeData(tempMail.getArray(),tempMail.offset(),
			tempMail.length());
		// �ʼ�����Ϊ�Ѷ�
		msg.setRecive_state(Message.READ);
		return data;
	}

	/**
	 * ��ȡָ����ҵ���Ҫ��Ϣ��������ǰ������ǰһ��������������Ϣ
	 * 
	 * @param playerId
	 * @return
	 */
	public ByteBuffer getMainInfo(Player player)
	{
		ByteBuffer data=new ByteBuffer();
		SeawarGladiator gladiator=getGladiator(player.getId());
		// �������ھ�������û�����ݣ����´���һ��
		if(gladiator==null)
		{
			gladiator=createNewGladiator(player.getId());
			// �±�����
			RecruitKit.pushTask(RecruitDayTask.EXERCISE_RANK,
				gladiator.getRanking(),player,false);
		}
		// д�뵱ǰ����
		data.writeInt(gladiator.getRanking());
		// д�������Ϣ
		int top=data.top();
		int count=0;
		data.writeByte(0);
		for(int i=1;i<=5;i++)
		{
			int ranking=ArenaHelper.calculateRival(gladiator.getRanking(),i);
			SeawarGladiator temp=(SeawarGladiator)sortedMap.get(ranking);
			if(temp==null) continue;
			Player tempPlayer=objectFactory
				.getPlayerById(temp.getPlayerId());
			data.writeInt(ranking);
			data.writeUTF(tempPlayer.getName());
			data.writeByte(tempPlayer.getLevel());
			count++;
		}
		if(count>0)
		{
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(count);
			data.setTop(newTop);
		}

		return data;
	}

	/**
	 * ��ȡ���а���Ϣ
	 * 
	 * @return
	 */
	public ByteBuffer getRankingInfo(int pageIndex)
	{
		ByteBuffer data=new ByteBuffer();
		int top=data.top();
		int length=0;
		data.writeByte(0);
//		synchronized(sortedMap)
//		{
			for(int i=pageIndex*ArenaHelper.RANK_COUNT_PER_PAGE+1;i<=(pageIndex+1)
				*ArenaHelper.RANK_COUNT_PER_PAGE
				&&i<=sortedMap.size();i++)
			{
				SeawarGladiator gladiator=(SeawarGladiator)sortedMap.get(i);
				if(gladiator==null) continue;
				ArenaHelper.writeRanking(gladiator,data,objectFactory);
				length++;
			}
//		}
		if(length>0)
		{
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(length);
			data.setTop(newTop);
		}
		return data;
	}

	/**
	 * ������ս
	 * 
	 * @param player
	 * @param attackName
	 * @return
	 */
	public ByteBuffer applyFight(Player player,String attackName)
	{
		synchronized(rlock)
		{
			Player beAttackPlayer=objectFactory.getPlayerByName(attackName,
				false);

			if(player==null||beAttackPlayer==null)
				throw new DataAccessException(0,"player is null");
			// ȡ��˫����ҵľ���������
			SeawarGladiator gladiator=getGladiator(player.getId());
			SeawarGladiator beAttackGladiator=getGladiator(beAttackPlayer
				.getId());
			if(gladiator==null||beAttackGladiator==null)
				throw new DataAccessException(0,"unknown_error");
			// �����ս�������Ƿ���ȷ
			if(!ArenaHelper.checkBattleRanking(gladiator.getRanking(),
				beAttackGladiator.getRanking()))
			{
				throw new DataAccessException(0,"ranking_error");
			}
			// �����ս����,����ϴ���ս���ڽ����賿֮ǰ�������ô���
			if(gladiator.getTodayBattleCount()>=ArenaHelper
				.getMaxBattleCount(player))
			{
				throw new DataAccessException(0,"no_arena_battle_count");
			}
			// ���ʱ��
			if(TimeKit.getSecondTime()-gladiator.getLastBattleTime()<ArenaHelper.BATTLE_INTERAL_TIME)
				throw new DataAccessException(0,
					"arena_battle_time_too_short");
			// ����˫������Ⱥ
			FleetGroup fleetGroup=ArenaHelper.createFleetGroup(gladiator,
				objectFactory);
			FleetGroup beAttackGroup=ArenaHelper.createFleetGroup(
				beAttackGladiator,objectFactory);
			// ���¼���˫����Ҽ���
			SeaBackKit.resetPlayerSkill(player,objectFactory);
			SeaBackKit.resetPlayerSkill(beAttackPlayer,objectFactory);
			// ����ս��
			FightScene scene=FightSceneFactory.factory.create(fleetGroup,
				beAttackGroup);
			// ��ʼս��
			FightShowEventRecord record=FightSceneFactory.factory.fight(
				scene,null);
			boolean needReset=false;
			// ���ӽ������ս����
			gladiator.incrTodayBattleCount();
			// �����ϴ���սʱ��
			gladiator.setLastBattleTime(TimeKit.getSecondTime());
			// ���������ʤ��
			boolean isSuccess=scene.getSuccessTeam()==0;
			int oldRanking1=gladiator.getRanking();
			int oldRanking2=beAttackGladiator.getRanking();
			if(isSuccess)
			{
				// ��¼˫����ʤ����ʧ�ܴ���
				gladiator.incrWin();
				beAttackGladiator.incrLose();
				// ��������,����������������ϵ��򲻽���
				if(beAttackGladiator.getRanking()<gladiator.getRanking())
				{
					int r=gladiator.getRanking();
					gladiator.setRanking(beAttackGladiator.getRanking());
					//�±�����
					RecruitKit.pushTask(RecruitDayTask.EXERCISE_RANK,
						beAttackGladiator.getRanking(),player,false);
					sortedMap.put(gladiator.getRanking(),gladiator);
					beAttackGladiator.setRanking(r);
					sortedMap.put(beAttackGladiator.getRanking(),
						beAttackGladiator);
					needReset=true;
					AchieveCollect.arenaRank(gladiator.getRanking(),player);
				}
			}
			// ���������ʧ��
			else
			{
				// ��¼˫����ʤ����ʧ�ܴ���
				gladiator.incrLose();
				beAttackGladiator.incrWin();
			}
			// ����ս������
			int awardSid=ArenaHelper.getAwardSid(gladiator.getRanking());
			Award award=(Award)Award.factory.newSample(awardSid);
			// award.awardLenth(awardData,player,objectFactory,null);

			// ����ս��
			ByteBuffer fightData=new ByteBuffer();
			SeaBackKit.conFightRecord(fightData,record.getRecord(),
				player.getName(),player.getLevel(),beAttackPlayer.getName(),
				beAttackPlayer.getLevel(),PublicConst.FIGHT_TYPE_11,player,
				beAttackPlayer,fleetGroup,beAttackGroup,false,null,null);

			// ����ս��
			ByteBuffer data=MessageKit.attackArenaPlayer(player,
				beAttackPlayer,fleetGroup,beAttackGroup,objectFactory,
				fightData,award,isSuccess,oldRanking1,oldRanking2);

			// ���ظ�ǰ̨������
			data.writeBoolean(needReset);
			if(needReset)
			{
				bytesWriteNowData(gladiator,data);
			}
			return data;
		}
	}

	public ByteBuffer viewAward(Player player){
		ByteBuffer data=new ByteBuffer();
		int awardSid=getAwardSid((SeawarGladiator)memCache.load(String
			.valueOf(player.getId())));
		if(awardSid<=0)
			throw new DataAccessException(0,"not_have_any_award");
		Award award=(Award)Award.factory.newSample(awardSid);
		award.viewAward(data,player);
		return data;
	}
	
	/**
	 * ��ȡ����
	 * 
	 * @param player
	 * @return ���û���쳣������null�����򷵻�δ����Ĵ�����Ϣ
	 */
	public ByteBuffer getAward(Player player)
	{
		SeawarGladiator gladiator=getGladiator(player.getId());
		ByteBuffer data=new ByteBuffer();
		if(gladiator==null)
		{
			throw new DataAccessException(0,"player is null");
		}
		int awardSid=gladiator.getAwardSid();
		if(awardSid<=0)
			throw new DataAccessException(0,"not_have_any_award");
		gladiator.setAwardSid(0);
		Award award=(Award)Award.factory.newSample(awardSid);
		award.awardLenth(data,player,objectFactory,null,
			new int[]{EquipmentTrack.FROM_ARENA});
		return data;
	}

	/**
	 * ���𽢶�
	 * 
	 * @param player
	 * @param list ����λ�ã���ֻSID����ֻ������һ�������б�
	 * @param length �б��еĽ�������
	 * @return
	 */
	public ByteBuffer deployFleet(Player player,IntList list,int length,
		ByteBuffer data)
	{
		// ��齢���Ƿ������
		FleetGroup mainGroup=player.getIsland().getMainGroup();
		String err=SeaBackKit.checkShipNumLimit(list,length,data,player,
			mainGroup,0);
		if(err!=null) throw new DataAccessException(0,err);
		SeawarGladiator gladiator=getGladiator(player.getId());
		if(gladiator==null)
		{
			gladiator=createNewGladiator(player.getId());
			// �±�����
			RecruitKit.pushTask(RecruitDayTask.EXERCISE_RANK,
				gladiator.getRanking(),player,false);
		}
		// �ж��Ƿ��ǵ�һ������
		boolean isFirst=gladiator.shipsIsEmpty();
		gladiator.clearShips();// ���ԭ���Ĵ�ֻ
		for(int i=0;i<length;i++)
		{
			int sid=list.get(i*3+1);
			int count=list.get(i*3+2);
			int location=list.get(i*3+3);
			gladiator.setShipSid(location,sid);
			gladiator.setShipCount(location,count);
		}
		data.clear();
		data.writeBoolean(isFirst);
		if(isFirst)
		{
			bytesWriteNowData(gladiator,data);
		}
		return data;
	}

	/**
	 * �����ϴ���սʱ��
	 * 
	 * @param player
	 * @return
	 */
	public String speedUp(Player player)
	{
		SeawarGladiator gladiator=getGladiator(player.getId());
		if(gladiator==null) throw new DataAccessException(0,"unknow error");
		int time=TimeKit.getSecondTime()-gladiator.getLastBattleTime();
		time=ArenaHelper.BATTLE_INTERAL_TIME-time;
		// ������ʱ���Ѿ��������Ƶļ��
		if(time<=0)
		{
			return null;
		}
		int gemsCount=SeaBackKit.getGemsForTime(time);
		if(!Resources.checkGems(gemsCount,player.getResources()))
			return "arena_gems_not_enough";
		if(Resources.reduceGems(gemsCount,player.getResources(),player))
		{
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.ARENA,player.getId(),
				gemsCount,0,Resources.getGems(player.getResources()));
			// �����ϴ���ս��ʱ��
			gladiator.setLastBattleTime(0);
		}

		return null;
	}

	/**
	 * ��ҵ�¼ʱ��Ҫ���л���ǰ̨����Ϣ
	 * 
	 * @param player
	 * @param data
	 */
	public void showBytesWritePlayer(Player player,ByteBuffer data)
	{
		SeawarGladiator gladiator=getGladiator(player.getId());
		if(gladiator!=null)
		{
			// д������
			data.writeInt(gladiator.getLastDayRanking());
			// д�뽱��SID�����û�н�����Ϊ0
			data.writeBoolean(gladiator.getAwardSid()<=0);
			int lastTime=gladiator.getLastBattleTime();
			int time=TimeKit.getSecondTime()-lastTime;
			// ��һ����ս��ʣ��ʱ��
			if(time<0||time>=ArenaHelper.BATTLE_INTERAL_TIME)
				data.writeShort(0);
			else
				data.writeShort(ArenaHelper.BATTLE_INTERAL_TIME-time);
			// д���������ս������������һ����սʱ���ڽ����0��֮ǰ��������ս������Ϊ0
			data.writeByte(gladiator.getTodayBattleCount());
			// д�뽢�ӣ���ʽΪ:��������,[λ��,��ֻsid,��ֻ����]...�������������Ϊ0����û����Ϣ
			gladiator.showBytesWriteFleet(data);
		}
		else
		{
			data.writeInt(0); // ��������
			data.writeBoolean(false);// ����ȡ�Ľ���SID,0��ʾû�н���������ȡ
			data.writeShort(0); // �����´���ս��ʣ��ʱ��
			data.writeByte(0); // ��������ս����
			data.writeByte(0);// ��������
		}
	}

	public void bytesWriteNowData(SeawarGladiator gladiator,ByteBuffer data)
	{
		data.writeInt(gladiator.getRanking());
		int top=data.top();
		data.writeByte(0);
		int count=0;
		for(int i=1;i<=5;i++)
		{
			int ranking=ArenaHelper.calculateRival(gladiator.getRanking(),i);
			SeawarGladiator temp=(SeawarGladiator)sortedMap.get(ranking);
			if(temp==null) continue;
			Player tempPlayer=objectFactory
				.getPlayerById(temp.getPlayerId());
			data.writeInt(ranking);
			data.writeUTF(tempPlayer.getName());
			data.writeByte(tempPlayer.getLevel());
			count++;
		}
		if(count>0)
		{
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(count);
			data.setTop(newTop);
		}
	}

	public void onTimer(TimerEvent e)
	{
		if("dailyAward".equals(e.getParameter()))
		{
			int currentTime=TimeKit.getSecondTime();
			if(currentTime>nextAwardTime)
			{
				// �´�������ʱ������Ϊ������ҹ
				nextAwardTime=updateDriveTime();
				collocate(currentTime);
			}
		}
	}

	public SeawarGladiator getGladiator(int playerId)
	{
		SeawarGladiator gladiator;
//		synchronized(memCache)
//		{
			gladiator=(SeawarGladiator)memCache.load(String
				.valueOf(playerId));
//		}
		return gladiator;
	}

	
	public IntKeyHashMap getSortedMap()
	{
		return sortedMap;
	}
	
	public int updateDriveTime()
	{
		int offset=SeaBackKit.getSomedayBegin(0)+RESET_TIME;
		if(offset<TimeKit.getSecondTime())
			offset+=(SeaBackKit.DAY_MILL_TIMES/1000);
		return offset;
	}
	
	public int getAwardSid(SeawarGladiator gladiator)
	{
		String[] arenaAwards=PublicConst.ARENA_DAILY_AWARDS;
		for(int j=0;j<arenaAwards.length;j++)
		{
			String[] awards_temp=TextKit.split(arenaAwards[j],":");
			if(TextKit.parseInt(awards_temp[0])>=gladiator
						 	.getRanking())
			{
				int[] awards=TextKit.parseIntArray(awards_temp);
				for(int k=1;k<awards.length;k+=2)
				{
					Player player=objectFactory.getPlayerById(gladiator.getPlayerId());
					if(player!=null&&awards[k]>=player.getLevel())
					{
					 	return awards[k+1];
					}
				}
			}
		}
		return 0;
	}
	
	/** ���͵�ϵͳ��ʾ */
	public void sendSystemMessage()
	{
		java.util.ArrayList<String> texts=new java.util.ArrayList<String>();
		for(int i=1;i<=MESSAGE_RANKING;i++)
		{
			SeawarGladiator temp=(SeawarGladiator)sortedMap.get(i);
			if(temp==null) continue;
			Player tempPlayer=objectFactory
				.getPlayerById(temp.getPlayerId());
			String rankingStr=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"arena_ranking_"+i);
//			texts.add(rankingStr+tempPlayer.getName());
			texts.add(rankingStr+ChatMessage.SEPARATORS+tempPlayer.getName()+ChatMessage.SEPARATORS);
		}
		if(texts.size()>0)
		{
			String rankingTitle=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"arena_ranking_title");
			texts.add(0,rankingTitle);
			int[] signs=new int[texts.size()];
			for(int i=0;i<signs.length-1;i++)
			{
				signs[i]=ChatMessage.FORMAT_NEW_LINE;
			}
			signs[signs.length-1]=0;
			SeaBackKit.sendFormatSystemMessage(
				texts.toArray(new String[]{}),signs,
				objectFactory.getDsmanager());
		}
	}
}
