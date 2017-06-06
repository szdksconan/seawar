package foxu.sea.alliance;

import java.util.ArrayList;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.set.AttributeList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.FightScene;
import foxu.sea.AttrAdjustment;
import foxu.sea.InterTransltor;
import foxu.sea.IslandLocationSave;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.alliance.alliancebattle.MaterialValue;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.AllianceSkill;
import foxu.sea.fight.Skill;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.port.AlliancePort;

/**
 * ���� author:icetiger
 */
public class Alliance
{

	/** �᳤�Զ��ƽ�ʱ��Ϊ7�� */
	public static final int AUTO_TIME=60*60*24*7;
	/** �����¼��洢�ݶ�200�� */
	public static final int EVENT_SIZE=200;
	/** �������� */
	public static final int APPLICATION_MAX_NUM=30;

	public static final int MILITARY_RANK1=0,// ��ͨ��Ա
					MILITARY_RANK2=1,// ���˸��᳤
					MILITARY_RANK3=2;// ���˻᳤
	
	/** �������ʵ���Դ�ĳ��� **/
	public static final int MATERIAL_LENGHT=3;
	/** �������˵жԵĳ��� **/
	public static final int HOSTILE_LENGTH=5;
	/** ��Ҫ�������ļ���sid */
	public static int[] skills2check;
	/** ����id ���� */
	int id;
	/** ���˵�ǰ���� */
	int allianceExp;
	/** �᳤���id */
	int masterPlayerId;
	/** ������ս�� */
	int allFightScore;
	/** ���˵ȼ� Ĭ��1�� */
	int allianceLevel=1;
	/** ���ᴴ��ʱ�� */
	int create_at;
	/** �������� */
	String name="";
	/** ���˽��� */
	String description="";
	/** ���˹��� */
	String announcement="";
	/** ���᳤�б� ���id */
	IntList vicePlayers=new IntList();
	/** ��Ա�б� ���id */
	IntList playerList=new IntList();
	/** ��������б� */
	IntList applicationList=new IntList();
	/** �¼��б� */
	ArrayList<AllianceEvent> eventList=new ArrayList<AllianceEvent>();
	/** ���˼��� */
	ObjectArray allianSkills=new ObjectArray();
	/** ���Ա� */
	AttributeList attributes=new AttributeList();

	/** ����bossս */
	AllianceBoss boss=new AllianceBoss();

	/** ��ǰ�������� */
	int rankNum;
	/** �Լ����������ĸ���ʱ�� */
	int rankTime;
	/** ���˵ж� **/
	String hostile;
	/** �������� **/
	long material;
	/** �Ƽ��� **/
	long sciencepoint;
	/** ����sid **/
	int betBattleIsland;
	/** �������ʾ��׼�¼ **/
	IntKeyHashMap  materialValue=new IntKeyHashMap();
	/**������Դ�������а�**/
	IntKeyHashMap  giveValue=new IntKeyHashMap();
	/** �ղص����� */
	ArrayList<IslandLocationSave> locationSaveList=new ArrayList<IslandLocationSave>();

	/** ���˱������˻��� */
	int luckyPoints;

	/** �Ƿ����Զ����� 0�� 1�� */
	int autoJoin=0;
	/** �������� �ȼ� */
	int joinPlayerLevel=0;
	/** �������� ս�� */
	int joinFightScore=0;
	/**���˳齱��ʱ��**/
	int luckyCreateAt;
	/**��������**/
	AllianceFlag flag=new AllianceFlag();
	public ArrayList<IslandLocationSave> getLocationSaveList()
	{
		return locationSaveList;
	}

	public void bytesWriteBoss(ByteBuffer data)
	{
		boss.bytesWrite(data);
	}

	public void bytesReadBoss(ByteBuffer data)
	{
		boss.bytesRead(data);
	}

	public void showBytesWriteBoss(ByteBuffer data,int playerId)
	{
		boss.showBytesWrite(data,playerId);
	}

	/** �鿴�����Ƿ�������� */
	public boolean isHavePlayer(int playerId)
	{
		for(int i=0;i<playerList.size();i++)
		{
			if(playerId==playerList.get(i)) return true;
		}
		return false;
	}

	/** ����¼�����ҳ�� */
	public int getEventPageSize()
	{
		if(eventList.size()%AlliancePort.MAX_EVENT_SIZE==0)
		{
			return eventList.size()/AlliancePort.MAX_EVENT_SIZE;
		}
		else
		{
			return eventList.size()/AlliancePort.MAX_EVENT_SIZE+1;
		}
	}

	/** ��ȡĳ�����˼��ܵľ���͵ȼ� */
	public int[] getSkillExp(int skillSid)
	{
		Object object[]=allianSkills.getArray();
		int exp[]=new int[2];
		for(int i=0;i<object.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)object[i];
			if(skill==null) continue;
			if(skill.getSid()==skillSid)
			{
				exp[0]=skill.getNowExp();
				exp[1]=skill.getLevel();
				break;
			}
		}
		return exp;
	}
	/** ���Ӽ��ܾ���ֵ */
	public int incrSkillExp(int n,int skillSid)
	{
		Object object[]=allianSkills.getArray();
		int addLevel=0;
		for(int i=0;i<object.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)object[i];
			if(skill==null) continue;
			if(skill.getSid()==skillSid)
			{
				addLevel=skill.incrExp(n,allianceLevel);
				return addLevel;
			}
		}
		AllianceSkill skill=(AllianceSkill)FightScene.abilityFactory
			.newSample(skillSid);
		if(skill!=null)
		{
			addLevel=skill.incrExp(n,allianceLevel);
			allianSkills.add(skill);
		}
		return addLevel;
	}

	/** ���Ӿ���ֵ */
	public int incrExp(int n)
	{
		if(n<=0||allianceExp+n<0) return 0;// Խ���ж�
		if(getAllianceLevel()>=PublicConst.MAX_ALLIANCE_LEVEL) return 0;
		allianceExp+=n;
		int addLevel=flashLevel();
		return addLevel;
	}
	/** �����ȡһ���ȼ�δ���ļ��� */
	public int getRandomSkill()
	{
		IntList sids=new IntList();
		for(int i=0;i<PublicConst.ALLIANCE_LEVEL_OPEN_SKILL.length;i++)
		{
			String[] strs=PublicConst.ALLIANCE_LEVEL_OPEN_SKILL[i].split(":");
			if(allianceLevel>=Integer.parseInt(strs[0])
				&&!SeaBackKit.isContainValue(
					PublicConst.ALLIANCE_COMBO_SKILL,
					Integer.parseInt(strs[1])))
			{
				sids.add(Integer.parseInt(strs[1]));
			}
		}
		int index=MathKit.randomValue(0,sids.size());
		AllianceSkill skill=null;
		for(int i=index;i<sids.size();i++)
		{
			skill=(AllianceSkill)FightScene.abilityFactory.getSample(sids.get(i));
			int[] explevel=getSkillExp(sids.get(i));
			if(explevel[1]>=skill.getExperience().length)continue;
			if(explevel[1]==allianceLevel&&explevel[0]==skill.getExperience()[explevel[1]]-1)continue;
			return sids.get(i);
		}
		for(int i=0;i<index;i++)
		{
			skill=(AllianceSkill)FightScene.abilityFactory.getSample(sids.get(i));
			int[] explevel=getSkillExp(sids.get(i));
			if(explevel[1]>=skill.getExperience().length)continue;
			if(explevel[1]==allianceLevel&&explevel[0]==skill.getExperience()[explevel[1]]-1)continue;
			return sids.get(i);
		}
		return 0;
	}
	/** ˢ�µȼ� */
	public int flashLevel()
	{
		if(getAllianceLevel()<1) setAllianceLevel(1);
		if(getAllianceLevel()>PublicConst.MAX_ALLIANCE_LEVEL)
			setAllianceLevel(PublicConst.MAX_ALLIANCE_LEVEL);
		int addLevel=0;
		int length=PublicConst.MAX_ALLIANCE_LEVEL<PublicConst.ALLIANCE_LEVEL_EXP.length
			?PublicConst.MAX_ALLIANCE_LEVEL:PublicConst.ALLIANCE_LEVEL_EXP.length;
		for(int i=getAllianceLevel()-1;i<length;i++)
		{
			if(allianceExp>=PublicConst.ALLIANCE_LEVEL_EXP[i])
			{
				addLevel++;
				allianceExp-=PublicConst.ALLIANCE_LEVEL_EXP[i];
				continue;
			}
			break;
		}
		if(allianceExp>PublicConst.ALLIANCE_LEVEL_EXP[length-1])
			allianceExp=PublicConst.ALLIANCE_LEVEL_EXP[length-1];
		setAllianceLevel(getAllianceLevel()+addLevel);
		return addLevel;
	}

	/** �����Զ��ƽ��᳤ */
	public void autoChangeMaster(CreatObjectFactory objectFactory)
	{
		// if(masterPlayerId==0) return;
		// Player master=objectFactory.getPlayerById(masterPlayerId);
		// // �ҵ�user�ĵ�¼ʱ��
		// User user=objectFactory.getUserDBAccess().load(
		// master.getUser_id()+"");
		// if(user!=null)
		// {
		// int loginTime=user.getLoginTime();
		// if((TimeKit.getSecondTime()-loginTime)>=AUTO_TIME)
		// {
		// // �����᳤
		// }
		// }
	}

	/** �ҵ����׶���ߵ���7��֮�ڵ�¼�������id 3���¼ */
	public int findPlayerContribution()
	{
		return 0;
	}

	/** ���һ�����᳤ */
	public void addVicePlayer(int playerId)
	{
		if(vicePlayers.size()>=100) return;
		for(int i=0;i<vicePlayers.size();i++)
		{
			if(vicePlayers.get(i)==playerId) return;
		}
		vicePlayers.add(playerId);
	}

	/** �Ƴ�һ�����᳤ */
	public void removeVicePlayer(int playerId)
	{
		vicePlayers.remove(playerId);
	}

	/** Ϊĳ�����������˼��� */
	public void addAllianceSkills(Player player)
	{
		// ������˼���
		player.resetAdjustment();
		// �����������
		player.getAllianceList().clear();
		Object[] array=getAllianSkills().getArray();
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isDefenceSkill()) continue;
			skill.setChangeValue(player.getAdjstment());
		}
		// ������������
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isAllianceSkill())
			{
				player.getAllianceList().add(skill.getSid());
				player.getAllianceList().add(skill.getLevel());
			}
		}
	}

	/** ĳ����Ƿ��������б��� */
	public boolean isApplication(int playerId)
	{
		for(int i=0;i<applicationList.size();i++)
		{
			if(playerId==applicationList.get(i)) return true;
		}
		return false;
	}

	/** �Ƿ��ǹ��� */
	public boolean isMaster(int playerId)
	{
		if(playerId==masterPlayerId) return true;
		for(int i=0;i<vicePlayers.size();i++)
		{
			if(playerId==vicePlayers.get(i))
			{
				return true;
			}
		}
		return false;
	}

	/** ���˼������� ���¹�����Ҽ��� */
	public void flushAllianceSkill(CreatObjectFactory objectFactory)
	{
		Player player=null;
		// ������������˵ļ��ܼӳ�
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player!=null)
			{
				addAllianceSkills(player);
			}
		}
	}

	/** ���˽�ɢ */
	public void dismiss(CreatObjectFactory objectFactory)
	{
		Player player=null;
		// ������������˵ļ��ܼӳ�
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player!=null)
			{
				clearAllianceDefence(player,objectFactory);
				player.setAttribute(PublicConst.ALLIANCE_ID,null);
				player.getAllianceList().clear();
				player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER,
					null);
				player.setAttribute(PublicConst.ALLIANCE_APPLICATION,null);
				player.resetAdjustment();
				// �����ʼ�
				String title=InterTransltor.getInstance().getTransByKey(
					player.getLocale(),"dismiss_alliance_title");
				String content=InterTransltor.getInstance().getTransByKey(
					player.getLocale(),"dismiss_alliance_content");
				content=TextKit.replace(content,"%",
					SeaBackKit.formatDataTime(TimeKit.getSecondTime()));
				MessageKit.sendSystemMessages(player,objectFactory,content,
					title);
			}
		}
		//�����һ�׶ε������  ��֤�Ƿ���ռ�쵺��
		if(getBetBattleIsland()!=0)
		{
			BattleIsland island=(BattleIsland)objectFactory
				.getBattleIslandMemCache().load(betBattleIsland,true);
			if(island!=null) island.setChangeRankValue(id);
		}
		allFightScore=0;
		masterPlayerId=0;
		vicePlayers.clear();
		playerList.clear();
		applicationList.clear();
		eventList.clear();
		allianSkills.clear();
		materialValue.clear();
		giveValue.clear();
		ActivityContainer.getInstance().clearActivityInfo(getId());
		objectFactory.getAllianceMemCache().deleteCache(this);
		objectFactory.getAllianceMemCache().getDbaccess().delete(this);
	}

	/** ����һ�������� */
	public void addApllication(int playerId)
	{
		applicationList.add(playerId);
	}

	/** �Ƴ�һ�������� */
	public void removeApllication(int playerId)
	{
		applicationList.remove(playerId);
	}

	/** �Ƴ�һ����Ա */
	public void removePlayerId(int playerId)
	{
		playerList.remove(playerId);
		vicePlayers.remove(playerId);
	}

	/** ���������г�Ա���һ��buff */
	public void addPlayerServices(CreatObjectFactory objectFactory,
		int serviceSid,int serviceTime)
	{
		Player player=null;
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player==null) continue;
			Service servicePlayer=(Service)Service.factory.newSample(serviceSid);
			servicePlayer.setServiceTime(serviceTime);
			player.addService(servicePlayer,TimeKit.getSecondTime());
			JBackKit.sendResetService(player);
		}
	}

	/** ��ս��push */
	public void pushAllFightScore(CreatObjectFactory objectFactory)
	{
		Player player=null;
		allFightScore=0;
		for(int i=0;i<playerList.size();i++)
		{
			player=objectFactory.getPlayerById(playerList.get(i));
			if(player==null) continue;
			allFightScore+=player.getFightScore();
		}
	}

	/** ��Ա���� */
	public int playersNum()
	{
		return playerList.size();
	}

	/** ���һ����Ա */
	public void addPlayer(int playerId)
	{
		if(playerList.contain(playerId))return;
		playerList.add(playerId);
	}

	/** �Ƿ������˳�Ա���� */
	public boolean inAlliance(int playerId)
	{
		return playerList.contain(playerId);
	}

	/** ��ȡĳ������ */
	public String getAttributes(String key)
	{
		return attributes.get(key);
	}

	/** ��ȡȫ������ */
	public String[] getAttributes()
	{
		return attributes!=null?attributes.getArray():AttributeList.NULL;
	}

	/** ���һ���¼� */
	public void addEvent(AllianceEvent event)
	{
		if(eventList.size()>=EVENT_SIZE)
		{
			for(int i=eventList.size()-1;i>=0;i--)
			{
				if(eventList.get(i)==null) continue;
				eventList.remove(i);
				break;
			}
		}
		eventList.add(0,event);
	}

	/** ����ָ�����Ե�ֵ */
	public void setAttribute(String key,String value)
	{
		attributes.set(key,value);
	}

	/** ��û᳤������ */
	public String getMasterName(CreatObjectFactory factoy)
	{
		Player master=factoy.getPlayerById(masterPlayerId);
		if(master==null) return "";
		return master.getName();
	}
	/** ��ȡ��������ֵ */
	public AttrAdjustment getAdjustment()
	{
		Object[] array=getAllianSkills().getArray();
		AttrAdjustment adjustment=new AttrAdjustment();
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isDefenceSkill()) continue;
			skill.setChangeValue(adjustment);
		}
		return adjustment;
	}
	
	/** ��ȡ�������� */
	public IntList getSkillList()
	{
		Object[] array=getAllianSkills().getArray();
		IntList list=new IntList();
		// ������������
		for(int i=0;i<array.length;i++)
		{
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.isAllianceSkill())
			{
				list.add(skill.getSid());
				list.add(skill.getLevel());
			}
		}
		return list;
	}
	/** ��ȡ��ǰ������ */
	public int getShipMax()
	{
		return PublicConst.AFIGHT_SHIP[allianceLevel-1];
	}
	/** �������Э��(Э�����ӷ���) */
	public void clearAllianceDefence(Player player,CreatObjectFactory objectFactory)
	{
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		mustang.set.ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null||fightEventList.size()<=0) return;
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event.getType()==FightEvent.ATTACK_HOLD
				&&event.getEventState()!=FightEvent.RETRUN_BACK)
			{
				NpcIsland beIsland=objectFactory.getIslandCache().loadOnly(
					event.getAttackIslandIndex()+"");
				if(beIsland.getPlayerId()!=0)// Э��
				{
					// todo
					int now=TimeKit.getSecondTime();
					int needTime=FightKit.needTime(
						event.getAttackIslandIndex(),
						event.getSourceIslandIndex());
					// �����е�Э�����ӷ����������������
					if(event.getEventState()==FightEvent.ATTACK)
					{
						int leftTime=event.getCreatAt()+event.getNeedTime()
							-now;
						// �����û��Ŀ�ĵأ����ڵ�ǰ�ص㷵�أ���ʱ��������򻨷�ʱ����ͬ
						if(leftTime>0) needTime=event.getNeedTime()-leftTime;
						event.setNeedTimeDB(needTime);
					}
					else
					{
						Player aPlayer=objectFactory.getPlayerById(event.getPlayerId());
						event.setNeedTime(needTime,aPlayer,now);
					}
					event.setCreatAt(now);
					event.setEventState(FightEvent.RETRUN_BACK);
					JBackKit.sendFightEvent(player,event,objectFactory);
					JBackKit.deleteFightEvent(player,event);
					JBackKit.sendFightEvent(
						objectFactory.getPlayerById(event.getPlayerId()),
						event,objectFactory);
				}
			}
		}
	}
	
	public boolean isLevelAndSkillsMax()
	{
		boolean isMax=true;
		if(getAllianceLevel()<PublicConst.MAX_ALLIANCE_LEVEL)
			return false;
		// ��⼼�ܵȼ�
		Object[] skills=getAllianSkills().getArray();
		if(skills==null||skills.length<=0)
			isMax=false;
		else
		{
			IntKeyHashMap typeMap=new IntKeyHashMap();
			for(int i=0;i<skills.length;i++)
			{
				Skill skill=(Skill)skills[i];
				if(typeMap.get(skill.getSid())!=null) continue;
				if(skill.getLevel()<PublicConst.MAX_SKILL_LEVEL)
				{
					isMax=false;
					break;
				}
				typeMap.put(skill.getSid(),"");
			}
			// ������ּ��ܶ��ﵽ���������ж��Ƿ���������п������ļ���
			if(isMax)
			{
				for(int i=0;i<skills2check.length;i++)
				{
					if(typeMap.get(skills2check[i])==null)
					{
						isMax=false;
						break;
					}
				}
			}
		}
		return true;
	}
	
	/**���һ�����˵ж�**/
	public void  addHostile(int id)
	{
		if(hostile==null || hostile.length()==0)
		{
			hostile=id+"";
			return ;
		}
		hostile+=","+id;
	}
	/**�Ƴ�һ���ж�**/
	public void  removeHostile(int id)
	{
		if(hostile==null || hostile.length()==0)
			return;
		String info[]=hostile.split(",");
		if(info.length==0)
		{	
			hostile="";
			return;
		}
		String newHost="";
		for(int i=0;i<info.length;i++)
		{
			if(TextKit.parseInt(info[i])!=id)
			{
				if(newHost==null ||newHost.length()==0)
					newHost=info[i];
				else
					newHost+=","+info[i];
			}
		}
		hostile=newHost;
	}
	
	/** ǰ̨���л� */
	public void showBytesWrite(ByteBuffer data,Player player,
		CreatObjectFactory factoy)
	{
		//��ʼ����ҵľ���
		initGiveValue(factoy);
		data.writeInt(id);
		Player master=factoy.getPlayerById(masterPlayerId);
		data.writeUTF(master.getName());
		data.writeInt(allFightScore);
		data.writeByte(allianceLevel);
		data.writeInt(create_at);
		data.writeUTF(name);
		data.writeUTF(description);
		data.writeUTF(announcement);
		data.writeInt(allianceExp);
		showBytesWritePlayerList(data,factoy);
		// showBytesWriteEventList(data);
		showbytesWriteSkills(data);
		// ĳ�������ҵ���ľ��״���
		int[] give=new int[6];
		DonateRank rank=(DonateRank)giveValue.get(player.getId());
		if(rank!=null)
			give=rank.getDonaterecord();
		for(int i=0;i<give.length;i++)
		{
			data.writeByte(give[i]);
		}
		data.writeByte(applicationList.size());
		Player appPlayer=null;
		for(int i=0;i<applicationList.size();i++)
		{
			appPlayer=factoy.getPlayerById(applicationList.get(i));
			if(appPlayer!=null)
			{
				data.writeInt(appPlayer.getId());
				data.writeUTF(appPlayer.getName());
				data.writeByte(appPlayer.getLevel());
				data.writeInt(appPlayer.getFightScore());
			}
		}
		showBytesWriteBoss(data,player.getId());
		//���л����˵ж�
		showBytesWriteHostile(data,factoy);
		// ���л���������
		data.writeByte(autoJoin);
		data.writeShort(joinPlayerLevel);
		data.writeInt(joinFightScore);
		boolean isJoinPlayerLevel=Boolean.parseBoolean(attributes
			.get(PublicConst.ALLIANCE_JOIN_LEVEL_BOOL));
		boolean isJoinPlayerScore=Boolean.parseBoolean(attributes
			.get(PublicConst.ALLIANCE_JOIN_SCORE_BOOL));
		data.writeBoolean(isJoinPlayerLevel);
		data.writeBoolean(isJoinPlayerScore);
		flag.showBytesWriteFlagsInfo(data);
		
		bytesWriteLocationSaveList(data);

	}

	/** ���л���Ա */
	public void showBytesWritePlayerList(ByteBuffer data,
		CreatObjectFactory factoy)
	{
		data.writeByte(playerList.size());
		Player player=null;
		int rank=MILITARY_RANK1;
		int nowTime=TimeKit.getSecondTime();
		for(int i=0;i<playerList.size();i++)
		{
			int playerId=playerList.get(i);
			player=factoy.getPlayerById(playerId);
			data.writeInt(playerId);
			data.writeUTF(player.getName());
			data.writeByte(player.getLevel());
			rank=getPosition(playerId);
			data.writeByte(rank);
			data.writeInt(player.getFightScore());
			data.writeShort((nowTime-player.getUpdateTime())
				/PublicConst.DAY_SEC);
			DonateRank drank=(DonateRank)giveValue.get(playerId);
			if(drank!=null)
			{
				drank.flushValue();
				data.writeShort(drank.getDayValue());
				data.writeShort(drank.getWeekValue());
				data.writeShort(drank.getMouthValue());
				data.writeInt((int)drank.getTotleValue());
				continue;
			}
			data.writeShort(0);
			data.writeShort(0);
			data.writeShort(0);
			data.writeInt(0);
		}
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(masterPlayerId);
		data.writeInt(allFightScore);
		data.writeByte(allianceLevel);
		data.writeInt(create_at);
		data.writeUTF(name);
		data.writeUTF(description);
		data.writeUTF(announcement);
		data.writeInt(allianceExp);
		bytesWriteVicePlayersList(data);
		bytesWritePlayerList(data);
		bytesWriteEventList(data);
		bytesWriteSkills(data);
		bytesWriteAttributes(data);
		bytesWriteApplicationList(data);
		bytesWriteBoss(data);
		bytesWriteLocationSaveList(data);
		data.writeInt(luckyPoints);
		data.writeInt(autoJoin);
		data.writeInt(joinPlayerLevel);
		data.writeInt(joinFightScore);
		data.writeInt(luckyCreateAt);

	}

	/** ������������л����ֽڻ����� */
	public Object bytesRead(ByteBuffer data)
	{
		id=data.readInt();
		masterPlayerId=data.readInt();
		allFightScore=data.readInt();
		allianceLevel=data.readUnsignedByte();
		create_at=data.readInt();
		name=data.readUTF();
		description=data.readUTF();
		announcement=data.readUTF();
		allianceExp=data.readInt();
		bytesReadVicePlayersList(data);
		bytesReadPlayerList(data);
		bytesReadEventList(data);
		bytesReadSkills(data);
		bytesReadAttributes(data);
		bytesReadApplicationList(data);
		bytesReadBoss(data);
		bytesReadLocationSaveList(data);
		luckyPoints=data.readInt();
		autoJoin=data.readInt();
		joinPlayerLevel=data.readInt();
		joinFightScore=data.readInt();
		luckyCreateAt=data.readInt();
		return this;
	}

	public void bytesWriteDes(ByteBuffer data)
	{
		data.writeUTF(description);
	}

	public void bytesWriteAnn(ByteBuffer data)
	{
		data.writeUTF(announcement);
	}

	public void bytesWriteApplicationList(ByteBuffer data)
	{
		if(applicationList==null||applicationList.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(applicationList.size());
			for(int i=0;i<applicationList.size();i++)
			{
				data.writeInt(applicationList.get(i));
			}
		}
	}

	public void bytesReadDes(ByteBuffer data)
	{
		description=data.readUTF();
	}

	public void bytesReadAnn(ByteBuffer data)
	{
		announcement=data.readUTF();
	}

	public void bytesReadApplicationList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		applicationList.clear();
		for(int i=0;i<length;i++)
		{
			applicationList.add(data.readInt());
		}
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesReadAttributes(ByteBuffer data)
	{
		String[] array=new String[data.readUnsignedShort()*2];
		for(int i=0;i<array.length;i++)
		{
			array[i]=data.readUTF();
		}
		attributes=new AttributeList(array);
		return this;
	}

	/** �����϶����������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteAttributes(ByteBuffer data)
	{
		if(attributes!=null)
		{
			String[] array=attributes.getArray();
			data.writeShort(array.length/2);
			for(int i=0;i<array.length;i++)
				data.writeUTF(array[i]);
		}
		else
		{
			data.writeShort(0);
		}
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesReadSkills(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			temp[i]=Skill.bytesReadAbility(data,FightScene.abilityFactory);
		}
		allianSkills=new ObjectArray(temp);
		return this;
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteSkills(ByteBuffer data)
	{
		if(allianSkills!=null&&allianSkills.size()>0)
		{
			int len=0;
			int top=data.top();
			Object[] array=allianSkills.getArray();
			data.writeByte(len);
			for(int i=0;i<array.length;i++)
			{
				((AllianceSkill)array[i]).bytesWrite(data);
				len++;
			}
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(len);
			data.setTop(newTop);
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void showbytesWriteSkills(ByteBuffer data)
	{
		if(allianSkills!=null&&allianSkills.size()>0)
		{
			int len=0;
			int top=data.top();
			Object[] array=allianSkills.getArray();
			data.writeByte(len);
			for(int i=0;i<array.length;i++)
			{
				if(((AllianceSkill)array[i]).isEffectEnemySkill()) continue;
				((AllianceSkill)array[i]).bytesWrite(data);
				len++;
			}
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(len);
			data.setTop(newTop);
		}
		else
		{
			data.writeByte(0);
		}
	}
	
	public void bytesWriteVicePlayersList(ByteBuffer data)
	{
		if(vicePlayers==null||vicePlayers.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(vicePlayers.size());
			for(int i=0;i<vicePlayers.size();i++)
			{
				data.writeInt(vicePlayers.get(i));
			}
		}
	}

	public void bytesReadVicePlayersList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		vicePlayers.clear();
		for(int i=0;i<length;i++)
		{
			vicePlayers.add(data.readInt());
		}
	}
	
	public void bytesWriteLocationSaveList(ByteBuffer data)
	{
		if(locationSaveList==null||locationSaveList.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(locationSaveList.size());
			IslandLocationSave location=null;
			for(int i=0;i<locationSaveList.size();i++)
			{
				location=locationSaveList.get(i);
				location.bytesWrite(data);
			}
		}
	}
	
	public void bytesReadLocationSaveList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		locationSaveList.clear();
		IslandLocationSave location=null;
		for(int i=0;i<length;i++)
		{
			location=new IslandLocationSave();
			location.bytesRead(data);
			locationSaveList.add(location);
		}
	}

	/** Ĭ����෢20�� */
	public void showBytesWriteEventList(ByteBuffer data)
	{
		if(eventList==null||eventList.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			int length=PublicConst.DEFAOULT_ALLIANCE_SIZE;
			if(eventList.size()>=PublicConst.DEFAOULT_ALLIANCE_SIZE)
			{
				data.writeByte(PublicConst.DEFAOULT_ALLIANCE_SIZE);
			}
			else
			{
				length=eventList.size();
				data.writeByte(eventList.size());
			}
			for(int i=0;i<length;i++)
			{
				AllianceEvent event=eventList.get(i);
				event.showBytesWrite(data);
			}
		}

	}

	public void bytesWriteEventList(ByteBuffer data)
	{
		if(eventList==null||eventList.size()<=0)
		{
			data.writeShort(eventList.size());
		}
		else
		{
			data.writeShort(eventList.size());
			for(int i=0;i<eventList.size();i++)
			{
				AllianceEvent event=eventList.get(i);
				event.bytesWrite(data);
			}
		}
	}

	public void bytesReadEventList(ByteBuffer data)
	{
		int length=data.readUnsignedShort();
		eventList.clear();
		for(int i=0;i<length;i++)
		{
			AllianceEvent event=new AllianceEvent();
			event.bytesRead(data);
			eventList.add(event);
		}
	}

	public void bytesWritePlayerList(ByteBuffer data)
	{
		if(playerList==null||playerList.size()<=0)
		{
			data.writeShort(0);
		}
		else
		{
			data.writeShort(playerList.size());
			for(int i=0;i<playerList.size();i++)
			{
				data.writeInt(playerList.get(i));
			}
		}
	}

	public void bytesReadPlayerList(ByteBuffer data)
	{
		int length=data.readUnsignedShort();
		playerList.clear();
		for(int i=0;i<length;i++)
		{
			playerList.add(data.readInt());
		}
	}

	/**���л��ж�����**/
	public void showBytesWriteHostile(ByteBuffer data,CreatObjectFactory factory)
	{
		if(hostile==null || hostile.length()==0)
		{
			data.writeByte(0);
			return;
		}
		String[]  hostiles=hostile.split(",");
		int top=data.top();
		data.writeByte(hostiles.length);
		//��ǰ�ж����˵ĳ���
		int end=hostiles.length;
		for(int i=0;i<hostiles.length;i++)
		{
			int aid=TextKit.parseInt(hostiles[i]);
			Alliance alliance=factory.getAlliance(aid,false);
			if(alliance==null)
					end-=1;
			else
			{
				data.writeInt(aid);
				data.writeUTF(alliance.getName());
				data.writeByte(alliance.playersNum());
				data.writeInt(alliance.getAllFightScore());
				alliance.getFlag().showBytesWriteAllianceFlag(data);
			}
		}
		if(end!=hostiles.length)
		{
			int now_top=data.top();
			data.setTop(top);
			data.writeByte(end);
			data.setTop(now_top);
		}

	}

	/** �������˵��� */
	public synchronized void addLuckyPoints()
	{
		this.luckyPoints++;
	}
	public int getId()
	{
		return id;
	}

	/**��������**/
	public void addMaterial(int num)
	{
		if(num<0) return ;
		material+=num;
		if(material<0) material=Long.MAX_VALUE;
	}
	
	/**�۳���������**/
	public void reduceMaterial(int num)
	{
		if(num<0) return ;
		material-=num;
		if(material<0) material=0;
	}
	
	/**�������˿Ƽ���**/
	public void addSciencepoint(int num)
	{
		if(num<0) return ;
		sciencepoint+=num;
		if(sciencepoint<0) sciencepoint=Long.MAX_VALUE;
	}
	
	/**�۳����˿Ƽ���**/
	public void reduceSciencepoint(int num)
	{
		if(num<0) return ;
		sciencepoint-=num;
		if(sciencepoint<0) sciencepoint=0;
	}
	
	/** �ж��µ�ǰ�������Ƿ��㹻 **/
	public boolean enoughMaterial(int num)
	{
			if(num<0) return false;
			if(material<num) return false;
			return true;
	}
	
	/** �ж��µ�ǰ�Ƽ����Ƿ��㹻 **/
	public boolean enoughSciencepoint(int num)
	{
			if(num<0) return false;
			if(sciencepoint<num) return false;
			return true;
	}
	
	/** �������ս��Ϣ **/
	public void clear()
	{
		setBetBattleIsland(0);
	}

	public void setId(int allianceId)
	{
		this.id=allianceId;
	}

	public int getAllianceLevel()
	{
		return allianceLevel;
	}

	public void setAllianceLevel(int allianceLevel)
	{
		this.allianceLevel=allianceLevel;
	}

	public String getAnnouncement()
	{
		return announcement;
	}

	public void setAnnouncement(String announcement)
	{
		announcement=ChatMessage.filerText(announcement);
		this.announcement=announcement;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		description=ChatMessage.filerText(description);
		this.description=description;
	}

	public ArrayList<AllianceEvent> getEventList()
	{
		return eventList;
	}

	public void setEventList(ArrayList<AllianceEvent> eventList)
	{
		this.eventList=eventList;
	}

	public int getMasterPlayerId()
	{
		return masterPlayerId;
	}

	public void setMasterPlayerId(int masterPlayerId)
	{
		this.masterPlayerId=masterPlayerId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

	public IntList getPlayerList()
	{
		return playerList;
	}

	public void setPlayerList(IntList playerList)
	{
		this.playerList=playerList;
	}

	public int getCreate_at()
	{
		return create_at;
	}

	public void setCreate_at(int create_at)
	{
		this.create_at=create_at;
	}

	public int getAllFightScore()
	{
		return allFightScore;
	}

	public void setAllFightScore(int allFightScore)
	{
		this.allFightScore=allFightScore;
	}

	public IntList getVicePlayers()
	{
		return vicePlayers;
	}

	public void setVicePlayers(IntList vicePlayers)
	{
		this.vicePlayers=vicePlayers;
	}

	public void setAttributes(AttributeList attributes)
	{
		this.attributes=attributes;
	}

	public int getAllianceExp()
	{
		return allianceExp;
	}

	public void setAllianceExp(int allianceExp)
	{
		this.allianceExp=allianceExp;
	}

	public int getRankTime()
	{
		return rankTime;
	}

	public void setRankTime(int rankTime)
	{
		this.rankTime=rankTime;
	}

	public int getRankNum()
	{
		return rankNum;
	}

	public void setRankNum(int rankNum)
	{
		this.rankNum=rankNum;
	}

	public IntList getApplicationList()
	{
		return applicationList;
	}

	public void setApplicationList(IntList applicationList)
	{
		this.applicationList=applicationList;
	}

	public ObjectArray getAllianSkills()
	{
		return allianSkills;
	}

	public void setAllianSkills(ObjectArray allianSkills)
	{
		this.allianSkills=allianSkills;
	}

	public AllianceBoss getBoss()
	{
		return boss;
	}

	public void setBoss(AllianceBoss boss)
	{
		this.boss=boss;
	}

	public String getHostile()
	{
		return hostile;
	}

	public void setHostile(String hostile)
	{
		this.hostile=hostile;
	}

	public long getMaterial()
	{
		return material;
	}

	public void setMaterial(long material)
	{
		this.material=material;
	}

	public long getSciencepoint()
	{
		return sciencepoint;
	}

	public void setSciencepoint(long sciencepoint)
	{
		this.sciencepoint=sciencepoint;
	}

	public int getBetBattleIsland()
	{
		return betBattleIsland;
	}

	
	public void setBetBattleIsland(int betBattleIsland)
	{
		this.betBattleIsland=betBattleIsland;
	}

	/**���л�**/
	public void showBytesWriteWarResrouce(ByteBuffer data,int integral,Player p,Alliance alliance)
	{
		data.writeLong(material);
		data.writeLong(sciencepoint);
		data.writeInt(integral);
		MaterialValue mvlaue=(MaterialValue)alliance.getMaterialValue().get(p.getId());
		if(mvlaue==null) 
			data.writeByte(0);
		else
			data.writeByte(mvlaue.getTimes());
	}

	/**���л���������**/
	public void showByteWriteMaterialRank(int type,CreatObjectFactory factory,ByteBuffer data)
	{
		if(materialValue==null||materialValue.size()==0)
		{
			data.writeShort(0);
			return;
		}
		Object[] objects=materialValue.valueArray();
		data.writeShort(materialValue.size());
		for(int i=0;i<objects.length;i++)
		{
			MaterialValue mValue=(MaterialValue)objects[i];
			int rank=getPosition(mValue.getPlayerId());
			mValue.showByteWrite(data,factory,type,rank);
		}
	}
	
	public IntKeyHashMap getMaterialValue()
	{
		return materialValue;
	}

	
	public void setMaterialValue(IntKeyHashMap materialValue)
	{
		this.materialValue=materialValue;
	}
	
	/** �������л� */
	public void bytesWriteMaterialValue(ByteBuffer data)
	{
		if(materialValue==null||materialValue.size()==0)
		{
			data.writeInt(0);
			return;
		}
		Object[] objects=materialValue.valueArray();
		data.writeInt(materialValue.size());
		for(int i=0;i<objects.length;i++)
		{
			MaterialValue mValue=(MaterialValue)objects[i];
			mValue.bytesWrite(data);
		}
	}

	/** �������л� */
	public void bytesWriteGiveValue(ByteBuffer data)
	{
		if(giveValue==null||giveValue.size()==0)
		{
			data.writeInt(0);
			return;
		}
		Object[] objects=giveValue.valueArray();
		data.writeInt(giveValue.size());
		for(int i=0;i<objects.length;i++)
		{
			DonateRank rank=(DonateRank)objects[i];
			rank.bytesWrite(data);
		}
	}
	
	/**������**/
	public void bytesReadMaterialValue(ByteBuffer data)
	{
		int le=data.readInt();
		for(int i=0;i<le;i++)
		{
			MaterialValue mValue=new MaterialValue();
			mValue.bytesRead(data);
			materialValue.put(mValue.getPlayerId(),mValue);
		}
	}
	/**������**/
	public void bytesReadGiveValue(ByteBuffer data)
	{
		int le=data.readInt();
		for(int i=0;i<le;i++)
		{
			DonateRank rank=new DonateRank();
			rank.bytesRead(data);
			giveValue.put(rank.getPlayerId(),rank);
		}
	}
	
	/** ��������������ʵļ�¼ **/
	public boolean addPlayerMaterialValue(int playerId,int num)
	{
		if(num<=0) return false;
		MaterialValue mValue=null;
		if(materialValue.get(playerId)!=null)
		{
			mValue=(MaterialValue)materialValue.get(playerId);
			return mValue.addMaterialValue(num);
		}
		mValue=new MaterialValue();
		mValue.setPlayerId(playerId);
		mValue.addMaterialValue(num);
		materialValue.put(playerId,mValue);
		return false;
	}

	/** ��ȡ������ʾ��״��� **/
	public int getPlayerGiveTimes(int playerId)
	{
		if(materialValue.get(playerId)!=null)
		{
			MaterialValue mValue=(MaterialValue)materialValue.get(playerId);
			return mValue.getTimes();
		}
		return 0;
	}

	/** ��ȡ������ʾ������� **/
	public long getPlayerGiveMaterial(int playerId)
	{
		if(materialValue.get(playerId)!=null)
		{
			MaterialValue mValue=(MaterialValue)materialValue.get(playerId);
			return mValue.getTotleValue();
		}
		return 0;
	}

	
	public IntKeyHashMap getGiveValue()
	{
		return giveValue;
	}

	
	public void setGiveValue(IntKeyHashMap giveValue)
	{
		this.giveValue=giveValue;
	}
	
	/**�������þ��׼�¼**/
	public  void setPlayerGiveValue(Player player,CreatObjectFactory factory)
	{
		String str=player.getAttributes(PublicConst.ALLIANCE_GIVE_VALUES);
		if(str==null || str.length()==0) return;
		String[] values=str.split(",");
		int votetime=TextKit.parseInt(values[1]);
		int timeNow=TimeKit.getSecondTime();
		// ����
		MaterialValue value=new MaterialValue();
		value.setPlayerId(player.getId());
		if(votetime!=0&&SeaBackKit.isSameDay(votetime,timeNow))
		{
			value.setTimes(TextKit.parseInt(values[0]));
			value.setValueTime(votetime);
		}
		materialValue.put(player.getId(),value);
		int rank1=0;
		// ְ��
		if(player.getId()==masterPlayerId)
		{
			rank1=MILITARY_RANK3;
		}
		for(int j=0;j<vicePlayers.size();j++)
		{
			if(player.getId()==vicePlayers.get(j))
			{
				rank1=MILITARY_RANK2;
				break;
			}
		}
		JBackKit.sendPlayerMaterialRank(playerList,factory,player,0,rank1);
		votetime=TextKit.parseInt(values[8]);
		// ���˾���
		DonateRank rank=new DonateRank();
		rank.setPlayerId(player.getId());
		if(votetime!=0&&SeaBackKit.isSameDay(votetime,timeNow))
		{
			int[] giveValues=new int[6];
			for(int i=2;i<values.length-1;i++)
			{
				giveValues[i-2]=TextKit.parseInt(values[i]);
			}
			rank.setDonaterecord(giveValues);
			rank.setValueTime(votetime);
		}
		giveValue.put(player.getId(),rank);
	}
	
	public int getLuckyPoints()
	{
		return luckyPoints;
	}

	public void setLuckyPoints(int luckyPoints)
	{
		this.luckyPoints=luckyPoints;
	}

	public int getAutoJoin()
	{
		return autoJoin;
	}

	public void setAutoJoin(int autoJoin)
	{
		this.autoJoin=autoJoin;
	}

	public int getJoinPlayerLevel()
	{
		return joinPlayerLevel;
	}

	public void setJoinPlayerLevel(int joinPlayerLevel)
	{
		this.joinPlayerLevel=joinPlayerLevel;
	}

	public int getJoinFightScore()
	{
		return joinFightScore;
	}

	public void setJoinFightScore(int joinFightScore)
	{
		this.joinFightScore=joinFightScore;
	}

	
	public int getLuckyCreateAt()
	{
		return luckyCreateAt;
	}

	
	public void setLuckyCreateAt(int luckyCreateAt)
	{
		this.luckyCreateAt=luckyCreateAt;
	}

	/** ��Ӿ������а� **/
	public  void initGiveValue(CreatObjectFactory factory)
	{
		if(giveValue.size()!=0)
			return;
		for(int i=0;i<playerList.size();i++)
		{
			Player player=factory.getPlayerById(playerList.get(i));
			if(player==null) continue;
			DonateRank rank=new DonateRank();
			String str=player.getAttributes(PublicConst.ALLIANCE_GIVE_TIMES);
			//���ý������˵ľ���ֵ
			if(str!=null)
			{
				rank.setValueTime(0);
				rank.setDonaterecord(player.todayAlliance());
			}
			String  gives=player.getAttributes(PublicConst.ALLIANCE_GIVE_VALUE_PLAYER);
			if(gives!=null)
			rank.setTotleValue(TextKit.parseInt(gives));
			rank.setPlayerId(playerList.get(i));
			rank.flushValue();
			giveValue.put(playerList.get(i),rank);
			MaterialValue value=new MaterialValue();
			value.setPlayerId(playerList.get(i));
			materialValue.put(playerList.get(i),value);
		}
	}
	/**�Ƴ���������**/
	public void removeGiveRank(int playerId)
	{
		giveValue.remove(playerId);
	}
	
	/**�Ƴ���������**/
	public void removeMaterialRank(int playerId)
	{
		materialValue.remove(playerId);
	}
	
	/**��ȡְλ**/
	public int getPosition(int playerId)
	{
		int rank1=0;
		// ְ��
		if(playerId==masterPlayerId)
		{
			rank1=MILITARY_RANK3;
		}
		for(int j=0;j<vicePlayers.size();j++)
		{
			if(playerId==vicePlayers.get(j))
			{
				rank1=MILITARY_RANK2;
				return rank1;
			}
		}
		return rank1;
	}

	
	public AllianceFlag getFlag()
	{
		return flag;
	}

	
	public void setFlag(AllianceFlag flag)
	{
		this.flag=flag;
	}
}
