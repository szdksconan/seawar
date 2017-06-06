package foxu.sea.kit;


import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FleetGroup;
import foxu.sea.messgae.Message;

/** 发送战报内容拼接 */
public class MessageKit
{

	/** 发送自定义标题的系统邮件 */
	public static void sendSystemMessages(Player player,
		CreatObjectFactory objectFactory,String content,String title)
	{
		// 发送邮件
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),0,title,true);
		JBackKit.sendRevicePlayerMessage(player,message,
			message.getRecive_state(),objectFactory);
	}
	/** 发送邮件 */
	public static void sendSystemMessages(Player player,
		CreatObjectFactory objectFactory,String content)
	{
		String title=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"alliance_system_title");
		sendSystemMessages(player,objectFactory,content,title);
	}

	/** 发送邀请者奖励的邮件 */
	public static void sendBeInvetedGems(Player player,
		CreatObjectFactory objectFactory)
	{
		if(player.getInveted()==0) return;
		// 发送邮件
		String title=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"be_invite_new_one_title");
		String content=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"be_invite_new_one_content");

		Player beplayer=objectFactory.getPlayerById(player.getInveted());
		if(beplayer!=null)
			content=TextKit.replace(content,"%",beplayer.getName());
		String sendName=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"system_mail");
		objectFactory.createMessage(0,player.getId(),content,sendName,player
			.getName(),0,title,true);
	}

	/** 发送邀请者奖励的邮件 */
	public static void sendInvetedMoney(Player player,
		CreatObjectFactory objectFactory,int someOneId)
	{
		// 发送邮件
		String title=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"invite_new_one_title");
		String content=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"invite_new_one_content");
		//
		Player beplayer=objectFactory.getPlayerById(someOneId);
		if(beplayer!=null)
		{
			title=TextKit.replace(title,"%",beplayer.getName());
			content=TextKit.replace(content,"%",beplayer.getName());
		}
		String sendName=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"system_mail");
		objectFactory.createMessage(0,player.getId(),content,sendName,player
			.getName(),0,title,true);
	}

	/**
	 * 挑战
	 * 
	 * @param player
	 * @param objectFactory
	 * @param data
	 * @param isSuccess
	 */
	public static ByteBuffer attackArenaPlayer(Player player,
		Player defendPlayer,FleetGroup lostGroup,FleetGroup delostGroup,
		CreatObjectFactory objectFactory,ByteBuffer fight,Award award,
		boolean isSuccess,int ranking,int beRanking)
	{
		// 给挑战方发邮件
		Message message=objectFactory.createMessageOnly(0,player.getId(),
			ranking+":"+beRanking+":"+defendPlayer.getName(),"",player
				.getName(),Message.ARENA,"",true);
		IntList intList=lostGroup.hurtList(FleetGroup.HURT_TROOPS);
		IntList deintList=delostGroup.hurtList(FleetGroup.HURT_TROOPS);
		message.setFightType(PublicConst.FIGHT_TYPE_11);
		message.createArenaFightReport(player,defendPlayer.getName(),
			isSuccess,fight,award,intList,deintList,objectFactory,null,lostGroup,delostGroup);

		objectFactory.getArenaManager().putReport(player.getId(),message);

		// JBackKit.sendRenaRevicePlayerMessage(player,message,message.getState());
		ByteBuffer data=new ByteBuffer();
		message.setRecive_state(Message.READ);
		message.showBytesWrite(data,message.getRecive_state(),message
			.getContent(),null);
		ByteBuffer bb=message.getFightDataFore();
		data.write(bb.getArray(),bb.offset(),bb.length());
		ByteBuffer temp=new ByteBuffer();
		temp.writeBoolean(true);
		// 前置信息之前组装战报版本
		temp.writeInt(message.getFightVersion());
		// 组装演播数据
		temp.write(fight.getArray(),fight.offset(),fight.length());
		//组装军官信息
		temp.write(message.getOfficerData().getArray(),message
			.getOfficerData().offset(),message.getOfficerData()
			.length());
		data.writeData(temp.toArray());
		// 给被挑战方发邮件
		message=objectFactory.createMessageOnly(0,defendPlayer.getId(),
			beRanking+":"+ranking+":"+player.getName(),"",defendPlayer
				.getName(),Message.ARENA,"",true);
		intList=lostGroup.hurtList(FleetGroup.HURT_TROOPS);
		deintList=delostGroup.hurtList(FleetGroup.HURT_TROOPS);
		message.setFightType(PublicConst.FIGHT_TYPE_12);
		message.createArenaFightReport(defendPlayer,player.getName(),
			!isSuccess,fight,null,intList,deintList,objectFactory,null,lostGroup,delostGroup);

		objectFactory.getArenaManager().putReport(defendPlayer.getId(),
			message);

		JBackKit.sendRenaRevicePlayerMessage(defendPlayer,message,message
			.getState(),objectFactory);

		return data;
	}

	/** *攻打野地 bool进攻方是否胜利 data战斗报告数据 */
	public static void attackBossNpcIsLand(Player player,NpcIsland beIsland,
		CreatObjectFactory objectFactory,ByteBuffer data,FightEvent event,
		boolean bool,int sourceIndex,Award award,FleetGroup bossGroup)
	{
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		//
		IntList defendList=bossGroup.hurtList(FleetGroup.HURT_TROOPS);
		// 发送邮件
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_13,beIsland.getName(),
			beIsland.getIndex(),player.getName(),beIsland.getName(),bool,
			data,award,event,fightList,defendList,sourceIndex,beIsland
				.getSid(),0,0,"",bossGroup,0);
	}
	
	/** *攻打年兽 bool进攻方是否胜利 data战斗报告数据 */
	public static void attackNianNpcIsLand(Player player,NpcIsland beIsland,
		CreatObjectFactory objectFactory,ByteBuffer data,FightEvent event,
		boolean bool,int sourceIndex,Award award,FleetGroup bossGroup)
	{
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		//
		IntList defendList=bossGroup.hurtList(FleetGroup.HURT_TROOPS);
		// 发送邮件
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_17,beIsland.getName(),
			beIsland.getIndex(),player.getName(),beIsland.getName(),bool,
			data,award,event,fightList,defendList,sourceIndex,beIsland
				.getSid(),0,0,"",bossGroup,0);
	}

	/** *攻打野地 bool进攻方是否胜利 data战斗报告数据 */
	public static void attackNpcIsLand(Player player,NpcIsland beIsland,
		CreatObjectFactory objectFactory,ByteBuffer data,FightEvent event,
		boolean bool,int sourceIndex)
	{
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		IntList defendList=beIsland.getNowFightFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		Award award=null;
		if(bool)
		{
			award=(Award)Award.factory.newSample(beIsland.getSid());
			if(award!=null)
			{
				int exp=award.getExperienceAward(player.getLevel());
				if(exp>0)
				{
					// 如果有经验奖励，计算活动加成
					exp=ActivityContainer.getInstance().resetActivityExp(exp);
					award.setExperienceAward(exp);
				}
			}
		}
		// 发送邮件
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_1,beIsland.getName(),beIsland.getIndex(),
			player.getName(),beIsland.getName(),bool,data,award,event,
			fightList,defendList,sourceIndex,beIsland.getSid(),0,0,"",
			beIsland.createFleetGroup(),0);
	}

	/** 攻打玩家 */
	public static void attackPlayer(Player player,Player beAttacker,
		FightEvent event,CreatObjectFactory objectFactory,
		NpcIsland beIsland,ByteBuffer data,boolean bool,int exp,
		int honorScore,Player allianceDefender,FleetGroup fleetGroup,int feats,int reduceProsperity)
	{
		String allianceDefend="";
		if(allianceDefender!=null)
			allianceDefend=allianceDefender.getName();
		// 发送push 邮件
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);

		int sourceIndex=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		// 防守方伤兵
		IntList defendList=beAttacker.getIsland().getMainGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		if(allianceDefender!=null)
			defendList=fleetGroup.hurtList(FleetGroup.HURT_TROOPS);
		Award award=null;
		if(exp!=0&&bool)
		{
			award=new Award();
			award.setExperienceAward(exp);
		}
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_2,beAttacker.getName(),beIsland.getIndex(),
			player.getName(),beAttacker.getName(),bool,data,award,event,
			fightList,defendList,sourceIndex,0,0,honorScore,
			allianceDefend,allianceDefender!=null?fleetGroup:beAttacker
				.getIsland().getMainGroup(),feats,reduceProsperity);		
		Award beaward=null;
		if(!bool&&exp!=0)
		{
			beaward=new Award();
			beaward.setExperienceAward(exp);
		}
		if(allianceDefender!=null)
		{
			// 发送邮件对方玩家
			SeaBackKit.fight_send_every(objectFactory,allianceDefender,
				PublicConst.FIGHT_TYPE_9,beAttacker.getName(),beIsland
					.getIndex(),player.getName(),beAttacker.getName(),!bool,
				data,beaward,event,fightList,defendList,sourceIndex,0,0,
				0,allianceDefend,fleetGroup,0);

			// 发送邮件对方玩家
			SeaBackKit.fight_send_every(objectFactory,beAttacker,
				PublicConst.FIGHT_TYPE_8,beAttacker.getName(),beIsland
					.getIndex(),player.getName(),beAttacker.getName(),!bool,
				data,null,event,fightList,defendList,sourceIndex,0,0,honorScore,
				allianceDefend,fleetGroup,0,reduceProsperity);
		}
		else
		{
			// 发送邮件对方玩家
			SeaBackKit.fight_send_every(objectFactory,beAttacker,
				PublicConst.FIGHT_TYPE_3,beAttacker.getName(),beIsland
					.getIndex(),player.getName(),beAttacker.getName(),!bool,
				data,beaward,event,fightList,defendList,sourceIndex,0,0,
				honorScore,allianceDefend,beAttacker.getIsland().getMainGroup(),0,reduceProsperity);
		}
	}

	/** 攻打野地驻守的玩家 bool进攻方是否胜利 data战斗报告数据 */
	public static void attackTempPlayer(Player player,Player beAttacker,
		FightEvent event,FightEvent holdEvent,
		CreatObjectFactory objectFactory,NpcIsland beIsland,ByteBuffer data,
		boolean bool,int exp)
	{
		int sourceIndex=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		// 发送push 邮件
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		// 防守方伤兵
		IntList defendList=holdEvent.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		Award award=null;
		if(exp>0&&bool)
		{
			award=new Award();
			award.setExperienceAward(exp);
		}
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_5,beIsland.getName(),beIsland.getIndex(),
			player.getName(),beAttacker.getName(),bool,data,award,event,
			fightList,defendList,sourceIndex,beIsland.getSid(),0,0,"",
			holdEvent.getFleetGroup(),0);

		Award beaward=null;
		if(exp>0&&!bool)
		{
			beaward=new Award();
			beaward.setExperienceAward(exp);
		}

		// 发送邮件对方玩家
		SeaBackKit.fight_send_every(objectFactory,beAttacker,
			PublicConst.FIGHT_TYPE_4,beIsland.getName(),beIsland.getIndex(),
			player.getName(),beAttacker.getName(),!bool,data,beaward,event,
			fightList,defendList,sourceIndex,beIsland.getSid(),0,0,"",
			holdEvent.getFleetGroup(),0);
	}
	
	/** 联盟岛屿争夺战 */
	public static void allianceFight(Player player,Player beAttacker,
		CreatObjectFactory objectFactory,ByteBuffer data,
		FleetGroup fleetGroup1,FleetGroup fleetGroup2,boolean victory,
		Alliance attackAlliance,Alliance beAttackAlliance,ArrayList list1,
		ArrayList list2)
	{
		// 攻击方
		IntList intList=fleetGroup1.hurtList(FleetGroup.HURT_TROOPS);
		// 防御方
		IntList deintList=fleetGroup2.hurtList(FleetGroup.HURT_TROOPS);
		/** 挑战方 **/
		Message message1=SeaBackKit.fight_send_alliance(player,beAttacker,
			victory,data,intList,deintList,attackAlliance.getName(),
			beAttackAlliance.getName(),attackAlliance.getId(),
			PublicConst.FIGHT_TYPE_18,fleetGroup1,fleetGroup2);
		/** 被挑战方 **/
//		Message message2=SeaBackKit.fight_send_alliance(beAttacker,player,
//			!victory,data,intList,deintList,beAttackAlliance.getName(),
//			attackAlliance.getName(),beAttackAlliance.getId(),
//			PublicConst.FIGHT_TYPE_19,fleetGroup2,fleetGroup1);
		Message message2=SeaBackKit.fight_send_alliance(player,beAttacker,
			victory,data,intList,deintList,attackAlliance.getName(),
			beAttackAlliance.getName(),beAttackAlliance.getId(),
			PublicConst.FIGHT_TYPE_19,fleetGroup1,fleetGroup2);

		// 设置联盟战的title
		message1.setAllianceFightTile(player.getName(),
			attackAlliance.getName(),beAttacker.getName(),
			beAttackAlliance.getName(),victory);
		// 设置联盟战的title
		message2.setAllianceFightTile(player.getName(),
			attackAlliance.getName(),beAttacker.getName(),
			beAttackAlliance.getName(),victory);

		list1.add(message1);
		list2.add(message2);
	}

	
}
