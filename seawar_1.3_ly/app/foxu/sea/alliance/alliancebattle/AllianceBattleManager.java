package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.fight.AllianceSkill;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.officer.OfficerBattleHQ;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.task.TaskEventExecute;

/****
 * 
 * ��������ս ������
 * 
 * @author lhj
 */
public class AllianceBattleManager
{

	public static AllianceBattleManager battleManager;

	public static final int FIGHT_LEVEL=20;
	/** ���ݿ�洢�ı�Ķ��� **/
	CreatObjectFactory factory;
	/** �������ᵺ��ս **/
	AllianceBattleFight allianceFight;

	public void init()
	{
		battleManager=this;
	}
	

	/** �������� **/
	public String donatedMaterial(Player player,ByteBuffer data)
	{
		// ���
		String result=checkAlliance(player);
		if(result!=null) return result;
		result=checkVoteMaterial(player);
		if(result!=null) return result;
		result=checkMaterialTimes(player);
		if(result!=null) return result;
		result=addBattleValue(player);
		if(result!=null) return result;
		return null;
	}

	/** ���������¼��� **/
	public String addAllianceSkill(Player player,int sciencePoint,
		int skillSid,ByteBuffer data)
	{
		// ���
		String result=checkAlliance(player);
		if(result!=null) return result;
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		// �����û��Ȩ��
		if(!alliance.isMaster(player.getId())) return "not master";
		// ��ǰ���˵Ĺ��׵㲻��
		if(!alliance.enoughSciencepoint(sciencePoint))
			return "science point is not enough";
		if(!isHaveSkillSid(skillSid,player,
			alliance.getAllianceLevel()))
		{
			return "alliance level limit";
		}
		// �Ƿ��ܵȼ��������˵ȼ�
		if(alliance.getSkillExp(skillSid)[1]>=alliance.getAllianceLevel())
			return "alliance level limit";
		// ��ȥ���˵�������
		alliance.reduceSciencepoint(sciencePoint);
		JBackKit.sendAllianceWarResource(factory,alliance);
		/**�Ƽ����¼**/
		factory.createSciencePointTrack(
			SciencePointTrack.USE_ALLIANCE_SKILL,alliance.getId(),
			sciencePoint,alliance.getSciencepoint(),
			SciencePointTrack.REDUCE,player.getId(),
			SciencePointTrack.SCIENCE_POINT,skillSid+"");
		// ���Ӿ���
		int addLevel=alliance.incrSkillExp(sciencePoint,skillSid);
		boolean upLevel=false;
		// ������˼������� ��������¼�
		if(addLevel>0)
		{
			int level[]=alliance.getSkillExp(skillSid);
			alliance.flushAllianceSkill(factory);
			// �����¼�
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_SKILL_LEVEL,skillSid+"",
				skillSid+"",level[1]+"",TimeKit.getSecondTime());
			alliance.addEvent(event);
			JBackKit.sendFightScore(player,factory,true,FightScoreConst.ALLIANCE_SKILL_UP);
			upLevel=true;
		}
		data.clear();
		data.writeByte(1);
		data.writeShort(skillSid);
		int exp[]=alliance.getSkillExp(skillSid);
		data.writeByte(exp[1]);
		data.writeInt(exp[0]);
		data.writeBoolean(upLevel);
		return null;
	}

	/** ����һ������ **/
	public String betBattleIsland(Player player,ByteBuffer data)
	{
		// ���
		String result=checkAlliance(player);
		if(result!=null) return result;
		int battleIslandId=data.readInt();
		BattleIsland battleIsland=(BattleIsland)factory
			.getBattleIslandMemCache().load(battleIslandId,true);
		if(battleIsland==null) return "island is not exits";
		// �ı���
		int material=data.readInt();
		 // ����ʱ���ѹ�
		 if(allianceFight.getAllianceStage().getStage()!=Stage.STAGE_TWO)
		 return "island can not bet";
		int allianceId=TextKit.parseInt(player
			.getAttributes(PublicConst.ALLIANCE_ID));
		Alliance alliance=factory.getAlliance(allianceId,true);
		if(!alliance.isMaster(player.getId())) return "not master";
		// ֻ�ܾ���һ����λ ���� ԭ������ô�ľ���ĵ���id Ϊ0
		if(alliance.getBetBattleIsland()!=0&&alliance.getBetBattleIsland()!=battleIslandId)
		{
			BattleIsland allianceBattle=(BattleIsland)factory
				.getBattleIslandMemCache().load(alliance.getBetBattleIsland(),false);
			if(allianceBattle==null) alliance.setBetBattleIsland(0);
			else
			{
				int bet=allianceBattle.getRankByAllianceId(alliance.getId());
				if(bet==0)
					alliance.setBetBattleIsland(0);
				else
					return "can only bid for a unit";
			}
		}
		// Ѻע
		result=battleIsland.addBetIsland(material,alliance,factory,player);
		if(result!=null) return result;
		data.clear();
		data.writeShort(1);
		battleIsland.showBytesBattleInfo(data,factory);
		JBackKit.sendAllianceBetInfo(battleIsland,factory);
		return null;
	}

	/** ��ұ��� **/
	public String joinAllianceFight(Player player,ByteBuffer data)
	{
		// ���
		String result=checkAlliance(player);
		if(result!=null) return result;
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		result=isAbleJoin(player,alliance);
		if(result!=null) return result;
		IntList list=playerRegister(data,player);
		// ���ͳ�����ʹ����
		list.remove(0);
		FleetGroup group=new FleetGroup();
		group.getOfficerFleetAttr().initOfficers(player);
		PlayerAllianceFight pfight=new PlayerAllianceFight(player.getId(),
			list,0,alliance.getId(),group.getOfficerFleetAttr());
		int fightScore=getPlayerFightScore(factory,player,pfight.getAllianceFightShip(),pfight.getOfficerBattle());
		pfight.setFightScore(fightScore);
		// ����в�������ս����� ��¼��Ҵ�ֻ��Ϣ
		factory.addShipTrack(0,ShipCheckData.ALLIANCE_BATTLE_FIGHT,player,
			list,null,false);
		JBackKit.sendResetTroops(player);
		BattleIsland island=(BattleIsland)factory
			.getBattleIslandMemCache().load(alliance.getBetBattleIsland(),true);
		island.addAllianceFightPlayer(pfight);
		return null;
	}

	/** �����̵����� **/
	public String allianceStoreConsume(Player player,ByteBuffer data)
	{
		int propSid=data.readUnsignedShort();
		Prop prop=(Prop)Prop.factory.newSample(propSid);
		if(prop==null)
		{
			return "prop is null";
		}
		int needScore=prop.getNeedAllianceScore();
		int maxNum=prop.getMaxExchangeNum();
		int playerScore=player.getIntegral();
		int playerNum=0;
		if(player.getPropExchangeNum().get(propSid)!=null)
			playerNum=(Integer)player.getPropExchangeNum().get(propSid);
		if(playerScore<needScore) return "you need more score";
		if(playerNum>=maxNum) return "number of times beyond the limit";
		// ���Ļ�ȡ��ǰ�Ļ���
		player.setAttribute(PublicConst.PLAYER_POINT_VALUE,
			(playerScore-needScore)+"");
		// ��Ӵ���
		player.getPropExchangeNum().put(propSid,++playerNum);
		//TODO
		if(prop instanceof NormalProp) ((NormalProp)prop).setCount(1);
		player.getBundle().incrProp(prop,true);
		JBackKit.sendResetBunld(player);
		JBackKit.sendPlayerIntegral(player);
		// ������־��¼
		factory.createIntegrationTrack(IntegrationTrack.FROM_ALLIANCE_SHOP,
			player.getId(),propSid,playerScore,player.getIntegral(),
			IntegrationTrack.REDUCE);
		data.clear();
		data.writeShort(propSid);
		return null;
	}
	/** ���˳����ж� */
	public String checkAlliance(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(allianceId==null||allianceId.equals(""))
		{
			return "you have no alliance";
		}
		// ���˲�����
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		if(alliance==null)
		{
			return "alliance has been dismiss";
		}
		if(alliance.getAllianceLevel()<FIGHT_LEVEL)
			return "alliancelevel limit";
		return null;
	}

	/** ����ܷ���Ӿ��� **/
	public String checkMaterialTimes(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		int times=alliance.getPlayerGiveTimes(player.getId());
		if(times>=PublicConst.ALLIANCE_VALUES.length/3)
			return "today typeGive is max";
		return null;
	}

	/** ���Ӿ��׵� **/
	public String addBattleValue(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		int times=alliance.getPlayerGiveTimes(player.getId());
		// ���ݴ�����ȡ���ʺͱ�ʯ������
		int num=PublicConst.ALLIANCE_VALUES[times*3+1];
		int costGem=PublicConst.ALLIANCE_VALUES[times*3];
		// ��ȡ����
		int point=PublicConst.ALLIANCE_VALUES[times*3+2];
		if(times==0) 
		{
			alliance.addSciencepoint(PublicConst.ADD_SCIENCEPOINT);
			/** �Ƽ��� **/
			factory.createSciencePointTrack(
				SciencePointTrack.FROM_GIVE_VALUES,alliance.getId(),
				PublicConst.ADD_SCIENCEPOINT,alliance.getSciencepoint(),
				SciencePointTrack.ADD,player.getId(),
				SciencePointTrack.SCIENCE_POINT,"");
		}
		if(costGem>0)
		{
			// ��ǰ��ʯ�����Ƿ��㹻
			if(!Resources.checkGems(costGem,player.getResources()))
				return "gems limit";
			// �۳���ʯ
			Resources.reduceGems(costGem,player.getResources(),player);
			// ��־��¼
			factory.createGemTrack(GemsTrack.ALLIANCE_VALUE,player.getId(),
				costGem,0,Resources.getGems(player.getResources()));
			
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,null,player,costGem);
		}
		// ������������
		alliance.addMaterial(num);
		// �����������ʾ��׼�¼
		boolean state=alliance.addPlayerMaterialValue(player.getId(),num);
		if(state)
		{
			// �����¼�
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_PLAYER_MATERIAL,
				player.getName(),"",alliance.getPlayerGiveMaterial(player
					.getId())+"",TimeKit.getSecondTime());
			alliance.addEvent(event);
		}
		// �����һ���
		player.addIntegral(point);
		JBackKit.sendPlayerIntegral(player);
		JBackKit.sendAllianceWarResource(factory,alliance);
		JBackKit.sendPlayerVoteTimes(player,alliance.getPlayerGiveTimes(player.getId()));
		int rank=alliance.getPosition(player.getId());
		JBackKit.sendPlayerMaterialRank(alliance.getPlayerList(),factory,player,num,rank);
		// ������־��¼
		factory.createIntegrationTrack(IntegrationTrack.FROM_GIVE_VALUE,
				player.getId(),0,point,player.getIntegral(),
				IntegrationTrack.ADD);
		/**����**/
		factory.createSciencePointTrack(
			SciencePointTrack.FROM_GIVE_VALUE,alliance.getId(),
			num,alliance.getMaterial(),
			SciencePointTrack.ADD,player.getId(),SciencePointTrack.MATERIAL,times+1+"");
	
		return null;
	}
	/** ��ұ����μ�����ս **/
	public IntList playerRegister(ByteBuffer data,Player player)
	{
		IntList list=new IntList();
		// ������Ϣ
		int length=data.readUnsignedByte();
		if(length<=0) throw new DataAccessException(0,"you have no ship");
		String str=SeaBackKit.checkShipNumLimit(list,length,data,player,
			player.getIsland().getMainGroup(),0);
		if(str!=null) throw new DataAccessException(0,str);
		// �����ֻ��Ϊ0
		boolean shipsNums=true;
		// ��Ϊlist(0)Ϊͳ��
		for(int i=1;i<list.size();i+=3)
		{
			int num=list.get(i+1);
			if(num>0)
			{
				shipsNums=false;
				break;
			}
		}
		if(shipsNums) throw new DataAccessException(0,"no ships");
		// �齨����
		reduceIslandGroup(list,player,true);
		return list;
	}

	/** ����ڈ����Ժ�ֱ���޳���ҵĴ�ֻ **/
	public boolean reduceIslandGroup(IntList list,Player player,
		boolean reduce)
	{
		boolean resetMainGroup=false;
		player.getBundle().decrProp(PublicConst.COMMANDER_LEVEL_UP_SID,
			list.get(0));// �۳�ͳ����
		for(int i=1;i<list.size();i+=3)
		{
			int shipSid=list.get(i);
			int num=list.get(i+1);
			if(num<=0) continue;
			if(reduce)
			{
				int reduceNum=player.getIsland().reduceShipBySid(shipSid,
					num,player.getIsland().getTroops());
				if(reduceNum<num)
				{
					resetMainGroup=true;
					// �۳��Ƿ������
					reduceShips(player,shipSid,(num-reduceNum));
				}
			}
		}
		if(resetMainGroup) JBackKit.resetMainGroup(player);
		return true;
	}
	/** �۳��Ƿ�����ָ��sid��ֻ */
	public void reduceShips(Player player,int shipSid,int num)
	{
		FleetGroup group=player.getIsland().getMainGroup();
		Fleet fleet[]=group.getArray();
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null) continue;
			if(fleet[i].getShip().getSid()==shipSid&&fleet[i].getNum()>0)
			{
				int reduceNum=group.reduceShipByLocation(
					fleet[i].getLocation(),num);
				if(reduceNum>=num) return;
				reduceShips(player,shipSid,(num-reduceNum));
				break;
			}
		}
	}
	/** ��ȡ��ҵĵ�ǰ������ս���� **/
	public int getPlayerFightScore(CreatObjectFactory factory,Player player,
		IntList list,OfficerBattleHQ hq)
	{
		return SeaBackKit.getPlayerFightScroe(player,factory,list,hq,true);
	}

	/** �ж�����Ƿ���Ա��� **/
	public String isAbleJoin(Player player,Alliance alliance)
	{
		 if(allianceFight.getAllianceStage().getStage()!=Stage.STAGE_THREE)
		 return "the alliance fight join not open";
		BattleIsland blsland=allianceFight.getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		// ����û�о��������� ���ܱ���
		if(blsland==null||blsland.getRankByAllianceId(alliance.getId())==0)
			return "you can not join alliance fight";
		if(blsland.isHavePlayer(player.getId()))
			return "you have already join alliance fight";
		return null;
	}

	/** ����ս�����л� **/
	public void showBytesWriteBattleFight(ByteBuffer data,Player player,
		CreatObjectFactory factory,Alliance alliance)
	{
		if(alliance==null)
		{
			data.writeBoolean(false);
			return;
		}
		data.writeBoolean(true);
		allianceFight.showByteWrite(data,alliance,player);
	}

	/** ��ȡ����ս��ս������ **/
	public String getAllianceReport(ByteBuffer data)
	{
		int messageId=data.readInt();
		Message message=(Message)factory.getMessageCache()
			.load(messageId+"");
		if(message==null
			||message.getMessageType()!=Message.ALLIANCE_FIGHT_TYPE)
			return "fight_report_not_exists";
		message.addReciveState(Message.READ);
		data.clear();
		// ��дս������֮ǰ������
		data.write(message.getFightDataFore().getArray(),message
			.getFightDataFore().offset(),message.getFightDataFore().length());
		ByteBuffer temp=new ByteBuffer();
		// �ݲ������Ƿ����
		boolean isNullFightData=message.getFightData()==null
			||message.getFightData().length()<=0;
		temp.writeBoolean(!isNullFightData);
		// ��װս���汾
		temp.writeInt(message.getFightVersion());
		if(!isNullFightData)
		{
			// ��װ�ݲ�����
			temp.write(message.getFightData().getArray(),message
				.getFightData().offset(),message.getFightData().length());
		}
		// ��װ������Ϣ
		temp.write(message.getOfficerData().getArray(),message
			.getOfficerData().offset(),message.getOfficerData()
			.length());
		data.writeData(temp.getArray(),temp.offset(),temp.length());
		return null;
	}

	/** ��ȡ��ǰ��������ս�ĵ�����Ϣ **/
	public String getAllianceSkillInfo(ByteBuffer data,Player player)
	{
		String result=checkAlliance(player);
		if(result!=null) return result;
		data.clear();
		Alliance alliance=factory.getAlliance(
			TextKit.parseInt(player.getAttributes(PublicConst.ALLIANCE_ID)),
			false);
		Object[] array=alliance.getAllianSkills().getArray();
		int top=data.top();
		data.writeByte(0);
		int count=0;
		for(int i=0;i<array.length;i++)
		{
			if(!((AllianceSkill)array[i]).isEffectEnemySkill()) continue;
			((AllianceSkill)array[i]).bytesWrite(data);
			count++;
		}
		if(count!=0)
		{
			int nowTop=data.top();
			data.setTop(top);
			data.writeByte(count);
			data.setTop(nowTop);
		}
		return null;
	}
	
	/**��ȡ����ս�ļ�Ҫ��Ϣ**/
	public  String  getSimpleFightInfo(ByteBuffer data,Player player)
	{
		data.clear();
		String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(aid==null) 
			return "alliance has been dismiss";
		Alliance alliance=factory.getAlliance(
			TextKit.parseInt(aid),false);
		if(alliance==null) return "alliance has been dismiss";
		Stage stage=allianceFight.getAllianceStage();
		Object object=factory.getBattleIslandMemCache().load(alliance.getBetBattleIsland()+"");
		if(object==null || stage.getStage()!=Stage.STAGE_THREE)
		{
			stage.showBytesWriteStage(data);
			data.writeBoolean(false);
			data.writeBoolean(false);
			stage.showByteWrite(data,false);
			return null;
		}
		BattleIsland battleIsland=(BattleIsland)object;
		stage.showBytesWriteStage(data);
		data.writeBoolean(true);
		data.writeBoolean(battleIsland.isHavePlayer(player
			.getId()));
		stage.showByteWrite(data,false);
		return null;
	}
	
	/**������ս�����л�**/
	public  void showByteWriteAllianceInfo(ByteBuffer data,Player p)
	{
		Alliance alliance=getAlliance(p);
		showBytesWriteBattleFight(data,p,factory,alliance);
		//�������ʻ��ָ�������Ϣ��
		showBytesWriteAllianceWarResource(data,alliance,p);
	}
	
	/**���л�������Դ**/
	public void showBytesWriteAllianceWarResource(ByteBuffer data,Alliance alliance,Player p)
	{
		if(alliance==null) return ;
		alliance.showBytesWriteWarResrouce(data,p.getIntegral(),p,alliance);
		//���л�������������
		alliance.showByteWriteMaterialRank(MaterialValue.DAY_TYPE,factory,data);
		alliance.showByteWriteMaterialRank(MaterialValue.WEEK_TYPE,factory,data);
		alliance.showByteWriteMaterialRank(MaterialValue.MOUTH_TYPE,factory,data);
		alliance.showByteWriteMaterialRank(MaterialValue.TOTLE_TYPE,factory,data);
		showBytesWriteAllianceShop(data,p);
	}
	
	private void showBytesWriteAllianceShop(ByteBuffer data,Player player)
	{
		
		// �ж��Ƿ�����״ε�½ ���������̵�һ�����
		resetExchangeNum(player);
		//�����̵�
		data.writeShort(PublicConst.ALLIANCE_SHOP_SELL_SIDS.length);
		for(int i=0;i<PublicConst.ALLIANCE_SHOP_SELL_SIDS.length;i++)
		{
			int sid=PublicConst.ALLIANCE_SHOP_SELL_SIDS[i];
			data.writeShort(sid);
			Prop p = (Prop)Prop.factory.getSample(sid);
			data.writeInt(p.getNeedAllianceScore());
			data.writeShort(p.getMaxExchangeNum());
			int num=0;
			if(player.getPropExchangeNum().get(sid)!=null)
				num=(Integer)player.getPropExchangeNum().get(sid);
			data.writeShort(num);
		}
	}
	
	public Alliance getAlliance(Player player)
	{
		int alliance_id=0;
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			alliance_id=Integer.parseInt(player
				.getAttributes(PublicConst.ALLIANCE_ID));
			Alliance alliance=(Alliance)factory.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance!=null
				&&alliance.getPlayerList().contain(player.getId()))
			{
				return alliance;
			}
		}
		return null;
	}
	
	/**�ж�����Ƿ��ǵ�һ���������**/
	public String checkVoteMaterial(Player player)
	{
		Alliance alliance=getAlliance(player);
		if(alliance.getMasterPlayerId()==player.getId())
			return null;
		// // ��һ�첻�ܾ���
		if(player.getAttributes(PublicConst.ALLIANCE_JOIN_TIME)!=null)
		{
			int time=Integer.parseInt(player
				.getAttributes(PublicConst.ALLIANCE_JOIN_TIME));
			int someDayEnd=SeaBackKit.getSomeDayEndTime(time*1000l);
			if(someDayEnd>TimeKit.getSecondTime())
			{
				return "today not allow";
			}
		}
		return null;
	}
	
	
	//���������̵�һ�����
	private void resetExchangeNum(Player p)
		{
			int lastTime=p.getUpdateTime();
			if(SeaBackKit.isSameDay(lastTime,TimeKit.getSecondTime()))
				return;
//			p.setPropExchangeNum(new IntKeyHashMap());
			p.getPropExchangeNum().clear();
		}
	
	/** �Ƿ�������ĳ������ */
	public static boolean isHaveSkillSid(int skillSid,Player player,
		int allianceLevel)
	{
		for(int i=0;i<PublicConst.ALLIANCE_LEVEL_OPEN_SKILL.length;i++)
		{
			String skillSids[]=TextKit.split(
				PublicConst.ALLIANCE_LEVEL_OPEN_SKILL[i],":");
			for(int j=1;j<skillSids.length;j++)
			{
				if(skillSid==Integer.parseInt(skillSids[j]))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	public void setAllianceFight(AllianceBattleFight allianceFight)
	{
		this.allianceFight=allianceFight;
	}

	
	public AllianceBattleFight getAllianceFight()
	{
		return allianceFight;
	}
	
}
