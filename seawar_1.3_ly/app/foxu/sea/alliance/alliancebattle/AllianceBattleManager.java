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
 * 联盟争夺战 管理器
 * 
 * @author lhj
 */
public class AllianceBattleManager
{

	public static AllianceBattleManager battleManager;

	public static final int FIGHT_LEVEL=20;
	/** 数据库存储的表的对象 **/
	CreatObjectFactory factory;
	/** 联盟争夺岛屿战 **/
	AllianceBattleFight allianceFight;

	public void init()
	{
		battleManager=this;
	}
	

	/** 捐赠物资 **/
	public String donatedMaterial(Player player,ByteBuffer data)
	{
		// 检测
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

	/** 捐献联盟新技能 **/
	public String addAllianceSkill(Player player,int sciencePoint,
		int skillSid,ByteBuffer data)
	{
		// 检测
		String result=checkAlliance(player);
		if(result!=null) return result;
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		// 该玩家没有权限
		if(!alliance.isMaster(player.getId())) return "not master";
		// 当前联盟的贡献点不足
		if(!alliance.enoughSciencepoint(sciencePoint))
			return "science point is not enough";
		if(!isHaveSkillSid(skillSid,player,
			alliance.getAllianceLevel()))
		{
			return "alliance level limit";
		}
		// 是否技能等级超过联盟等级
		if(alliance.getSkillExp(skillSid)[1]>=alliance.getAllianceLevel())
			return "alliance level limit";
		// 减去联盟的升级点
		alliance.reduceSciencepoint(sciencePoint);
		JBackKit.sendAllianceWarResource(factory,alliance);
		/**科技点记录**/
		factory.createSciencePointTrack(
			SciencePointTrack.USE_ALLIANCE_SKILL,alliance.getId(),
			sciencePoint,alliance.getSciencepoint(),
			SciencePointTrack.REDUCE,player.getId(),
			SciencePointTrack.SCIENCE_POINT,skillSid+"");
		// 增加经验
		int addLevel=alliance.incrSkillExp(sciencePoint,skillSid);
		boolean upLevel=false;
		// 如果联盟技能升级 添加联盟事件
		if(addLevel>0)
		{
			int level[]=alliance.getSkillExp(skillSid);
			alliance.flushAllianceSkill(factory);
			// 联盟事件
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

	/** 竞标一个岛屿 **/
	public String betBattleIsland(Player player,ByteBuffer data)
	{
		// 检测
		String result=checkAlliance(player);
		if(result!=null) return result;
		int battleIslandId=data.readInt();
		BattleIsland battleIsland=(BattleIsland)factory
			.getBattleIslandMemCache().load(battleIslandId,true);
		if(battleIsland==null) return "island is not exits";
		// 改变量
		int material=data.readInt();
		 // 竞标时间已过
		 if(allianceFight.getAllianceStage().getStage()!=Stage.STAGE_TWO)
		 return "island can not bet";
		int allianceId=TextKit.parseInt(player
			.getAttributes(PublicConst.ALLIANCE_ID));
		Alliance alliance=factory.getAlliance(allianceId,true);
		if(!alliance.isMaster(player.getId())) return "not master";
		// 只能竞标一个单位 这里 原来里那么的竞标的岛屿id 为0
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
		// 押注
		result=battleIsland.addBetIsland(material,alliance,factory,player);
		if(result!=null) return result;
		data.clear();
		data.writeShort(1);
		battleIsland.showBytesBattleInfo(data,factory);
		JBackKit.sendAllianceBetInfo(battleIsland,factory);
		return null;
	}

	/** 玩家报名 **/
	public String joinAllianceFight(Player player,ByteBuffer data)
	{
		// 检测
		String result=checkAlliance(player);
		if(result!=null) return result;
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		result=isAbleJoin(player,alliance);
		if(result!=null) return result;
		IntList list=playerRegister(data,player);
		// 清除统御书的使用量
		list.remove(0);
		FleetGroup group=new FleetGroup();
		group.getOfficerFleetAttr().initOfficers(player);
		PlayerAllianceFight pfight=new PlayerAllianceFight(player.getId(),
			list,0,alliance.getId(),group.getOfficerFleetAttr());
		int fightScore=getPlayerFightScore(factory,player,pfight.getAllianceFightShip(),pfight.getOfficerBattle());
		pfight.setFightScore(fightScore);
		// 如果有参与联盟战的玩家 记录玩家船只信息
		factory.addShipTrack(0,ShipCheckData.ALLIANCE_BATTLE_FIGHT,player,
			list,null,false);
		JBackKit.sendResetTroops(player);
		BattleIsland island=(BattleIsland)factory
			.getBattleIslandMemCache().load(alliance.getBetBattleIsland(),true);
		island.addAllianceFightPlayer(pfight);
		return null;
	}

	/** 联盟商店消费 **/
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
		// 消耗获取当前的积分
		player.setAttribute(PublicConst.PLAYER_POINT_VALUE,
			(playerScore-needScore)+"");
		// 添加次数
		player.getPropExchangeNum().put(propSid,++playerNum);
		//TODO
		if(prop instanceof NormalProp) ((NormalProp)prop).setCount(1);
		player.getBundle().incrProp(prop,true);
		JBackKit.sendResetBunld(player);
		JBackKit.sendPlayerIntegral(player);
		// 积分日志记录
		factory.createIntegrationTrack(IntegrationTrack.FROM_ALLIANCE_SHOP,
			player.getId(),propSid,playerScore,player.getIntegral(),
			IntegrationTrack.REDUCE);
		data.clear();
		data.writeShort(propSid);
		return null;
	}
	/** 联盟常规判断 */
	public String checkAlliance(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(allianceId==null||allianceId.equals(""))
		{
			return "you have no alliance";
		}
		// 联盟不存在
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

	/** 检测能否添加捐献 **/
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

	/** 增加捐献点 **/
	public String addBattleValue(Player player)
	{
		String allianceId=player.getAttributes(PublicConst.ALLIANCE_ID);
		Alliance alliance=(Alliance)factory.getAllianceMemCache().load(
			allianceId);
		int times=alliance.getPlayerGiveTimes(player.getId());
		// 根据次数获取物资和宝石的数量
		int num=PublicConst.ALLIANCE_VALUES[times*3+1];
		int costGem=PublicConst.ALLIANCE_VALUES[times*3];
		// 获取积分
		int point=PublicConst.ALLIANCE_VALUES[times*3+2];
		if(times==0) 
		{
			alliance.addSciencepoint(PublicConst.ADD_SCIENCEPOINT);
			/** 科技点 **/
			factory.createSciencePointTrack(
				SciencePointTrack.FROM_GIVE_VALUES,alliance.getId(),
				PublicConst.ADD_SCIENCEPOINT,alliance.getSciencepoint(),
				SciencePointTrack.ADD,player.getId(),
				SciencePointTrack.SCIENCE_POINT,"");
		}
		if(costGem>0)
		{
			// 当前宝石数量是否足够
			if(!Resources.checkGems(costGem,player.getResources()))
				return "gems limit";
			// 扣除宝石
			Resources.reduceGems(costGem,player.getResources(),player);
			// 日志记录
			factory.createGemTrack(GemsTrack.ALLIANCE_VALUE,player.getId(),
				costGem,0,Resources.getGems(player.getResources()));
			
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,null,player,costGem);
		}
		// 增加联盟物资
		alliance.addMaterial(num);
		// 增加联盟物资捐献记录
		boolean state=alliance.addPlayerMaterialValue(player.getId(),num);
		if(state)
		{
			// 联盟事件
			AllianceEvent event=new AllianceEvent(
				AllianceEvent.ALLIANCE_EVENT_PLAYER_MATERIAL,
				player.getName(),"",alliance.getPlayerGiveMaterial(player
					.getId())+"",TimeKit.getSecondTime());
			alliance.addEvent(event);
		}
		// 添加玩家积分
		player.addIntegral(point);
		JBackKit.sendPlayerIntegral(player);
		JBackKit.sendAllianceWarResource(factory,alliance);
		JBackKit.sendPlayerVoteTimes(player,alliance.getPlayerGiveTimes(player.getId()));
		int rank=alliance.getPosition(player.getId());
		JBackKit.sendPlayerMaterialRank(alliance.getPlayerList(),factory,player,num,rank);
		// 积分日志记录
		factory.createIntegrationTrack(IntegrationTrack.FROM_GIVE_VALUE,
				player.getId(),0,point,player.getIntegral(),
				IntegrationTrack.ADD);
		/**物资**/
		factory.createSciencePointTrack(
			SciencePointTrack.FROM_GIVE_VALUE,alliance.getId(),
			num,alliance.getMaterial(),
			SciencePointTrack.ADD,player.getId(),SciencePointTrack.MATERIAL,times+1+"");
	
		return null;
	}
	/** 玩家报名参加联盟战 **/
	public IntList playerRegister(ByteBuffer data,Player player)
	{
		IntList list=new IntList();
		// 舰队信息
		int length=data.readUnsignedByte();
		if(length<=0) throw new DataAccessException(0,"you have no ship");
		String str=SeaBackKit.checkShipNumLimit(list,length,data,player,
			player.getIsland().getMainGroup(),0);
		if(str!=null) throw new DataAccessException(0,str);
		// 如果船只都为0
		boolean shipsNums=true;
		// 因为list(0)为统御
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
		// 组建舰队
		reduceIslandGroup(list,player,true);
		return list;
	}

	/** 玩家在報名以后直接剔除玩家的船只 **/
	public boolean reduceIslandGroup(IntList list,Player player,
		boolean reduce)
	{
		boolean resetMainGroup=false;
		player.getBundle().decrProp(PublicConst.COMMANDER_LEVEL_UP_SID,
			list.get(0));// 扣除统御书
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
					// 扣除城防里面的
					reduceShips(player,shipSid,(num-reduceNum));
				}
			}
		}
		if(resetMainGroup) JBackKit.resetMainGroup(player);
		return true;
	}
	/** 扣除城防舰队指定sid船只 */
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
	/** 获取玩家的当前出兵的战斗力 **/
	public int getPlayerFightScore(CreatObjectFactory factory,Player player,
		IntList list,OfficerBattleHQ hq)
	{
		return SeaBackKit.getPlayerFightScroe(player,factory,list,hq,true);
	}

	/** 判断玩家是否可以报名 **/
	public String isAbleJoin(Player player,Alliance alliance)
	{
		 if(allianceFight.getAllianceStage().getStage()!=Stage.STAGE_THREE)
		 return "the alliance fight join not open";
		BattleIsland blsland=allianceFight.getBattleIslandById(alliance
			.getBetBattleIsland(),false);
		// 联盟没有竞标的情况下 不能报名
		if(blsland==null||blsland.getRankByAllianceId(alliance.getId())==0)
			return "you can not join alliance fight";
		if(blsland.isHavePlayer(player.getId()))
			return "you have already join alliance fight";
		return null;
	}

	/** 联盟战的序列化 **/
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

	/** 获取联盟战的战报内容 **/
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
		// 先写战报数据之前的内容
		data.write(message.getFightDataFore().getArray(),message
			.getFightDataFore().offset(),message.getFightDataFore().length());
		ByteBuffer temp=new ByteBuffer();
		// 演播数据是否存在
		boolean isNullFightData=message.getFightData()==null
			||message.getFightData().length()<=0;
		temp.writeBoolean(!isNullFightData);
		// 组装战报版本
		temp.writeInt(message.getFightVersion());
		if(!isNullFightData)
		{
			// 组装演播数据
			temp.write(message.getFightData().getArray(),message
				.getFightData().offset(),message.getFightData().length());
		}
		// 组装军官信息
		temp.write(message.getOfficerData().getArray(),message
			.getOfficerData().offset(),message.getOfficerData()
			.length());
		data.writeData(temp.getArray(),temp.offset(),temp.length());
		return null;
	}

	/** 获取当前的新联盟战的岛屿信息 **/
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
	
	/**获取联盟战的简要信息**/
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
	
	/**新联盟战的序列化**/
	public  void showByteWriteAllianceInfo(ByteBuffer data,Player p)
	{
		Alliance alliance=getAlliance(p);
		showBytesWriteBattleFight(data,p,factory,alliance);
		//联盟物资积分个个人信息等
		showBytesWriteAllianceWarResource(data,alliance,p);
	}
	
	/**序列化联盟资源**/
	public void showBytesWriteAllianceWarResource(ByteBuffer data,Alliance alliance,Player p)
	{
		if(alliance==null) return ;
		alliance.showBytesWriteWarResrouce(data,p.getIntegral(),p,alliance);
		//序列化联盟物资排名
		alliance.showByteWriteMaterialRank(MaterialValue.DAY_TYPE,factory,data);
		alliance.showByteWriteMaterialRank(MaterialValue.WEEK_TYPE,factory,data);
		alliance.showByteWriteMaterialRank(MaterialValue.MOUTH_TYPE,factory,data);
		alliance.showByteWriteMaterialRank(MaterialValue.TOTLE_TYPE,factory,data);
		showBytesWriteAllianceShop(data,p);
	}
	
	private void showBytesWriteAllianceShop(ByteBuffer data,Player player)
	{
		
		// 判断是否今日首次登陆 重置联盟商店兑换次数
		resetExchangeNum(player);
		//联盟商店
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
	
	/**判断玩家是否是第一天进入联盟**/
	public String checkVoteMaterial(Player player)
	{
		Alliance alliance=getAlliance(player);
		if(alliance.getMasterPlayerId()==player.getId())
			return null;
		// // 第一天不能捐献
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
	
	
	//重置联盟商店兑换次数
	private void resetExchangeNum(Player p)
		{
			int lastTime=p.getUpdateTime();
			if(SeaBackKit.isSameDay(lastTime,TimeKit.getSecondTime()))
				return;
//			p.setPropExchangeNum(new IntKeyHashMap());
			p.getPropExchangeNum().clear();
		}
	
	/** 是否能提升某个技能 */
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
