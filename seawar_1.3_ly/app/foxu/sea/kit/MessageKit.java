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

/** ����ս������ƴ�� */
public class MessageKit
{

	/** �����Զ�������ϵͳ�ʼ� */
	public static void sendSystemMessages(Player player,
		CreatObjectFactory objectFactory,String content,String title)
	{
		// �����ʼ�
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),0,title,true);
		JBackKit.sendRevicePlayerMessage(player,message,
			message.getRecive_state(),objectFactory);
	}
	/** �����ʼ� */
	public static void sendSystemMessages(Player player,
		CreatObjectFactory objectFactory,String content)
	{
		String title=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"alliance_system_title");
		sendSystemMessages(player,objectFactory,content,title);
	}

	/** ���������߽������ʼ� */
	public static void sendBeInvetedGems(Player player,
		CreatObjectFactory objectFactory)
	{
		if(player.getInveted()==0) return;
		// �����ʼ�
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

	/** ���������߽������ʼ� */
	public static void sendInvetedMoney(Player player,
		CreatObjectFactory objectFactory,int someOneId)
	{
		// �����ʼ�
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
	 * ��ս
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
		// ����ս�����ʼ�
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
		// ǰ����Ϣ֮ǰ��װս���汾
		temp.writeInt(message.getFightVersion());
		// ��װ�ݲ�����
		temp.write(fight.getArray(),fight.offset(),fight.length());
		//��װ������Ϣ
		temp.write(message.getOfficerData().getArray(),message
			.getOfficerData().offset(),message.getOfficerData()
			.length());
		data.writeData(temp.toArray());
		// ������ս�����ʼ�
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

	/** *����Ұ�� bool�������Ƿ�ʤ�� dataս���������� */
	public static void attackBossNpcIsLand(Player player,NpcIsland beIsland,
		CreatObjectFactory objectFactory,ByteBuffer data,FightEvent event,
		boolean bool,int sourceIndex,Award award,FleetGroup bossGroup)
	{
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		//
		IntList defendList=bossGroup.hurtList(FleetGroup.HURT_TROOPS);
		// �����ʼ�
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_13,beIsland.getName(),
			beIsland.getIndex(),player.getName(),beIsland.getName(),bool,
			data,award,event,fightList,defendList,sourceIndex,beIsland
				.getSid(),0,0,"",bossGroup,0);
	}
	
	/** *�������� bool�������Ƿ�ʤ�� dataս���������� */
	public static void attackNianNpcIsLand(Player player,NpcIsland beIsland,
		CreatObjectFactory objectFactory,ByteBuffer data,FightEvent event,
		boolean bool,int sourceIndex,Award award,FleetGroup bossGroup)
	{
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		//
		IntList defendList=bossGroup.hurtList(FleetGroup.HURT_TROOPS);
		// �����ʼ�
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_17,beIsland.getName(),
			beIsland.getIndex(),player.getName(),beIsland.getName(),bool,
			data,award,event,fightList,defendList,sourceIndex,beIsland
				.getSid(),0,0,"",bossGroup,0);
	}

	/** *����Ұ�� bool�������Ƿ�ʤ�� dataս���������� */
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
					// ����о��齱���������ӳ�
					exp=ActivityContainer.getInstance().resetActivityExp(exp);
					award.setExperienceAward(exp);
				}
			}
		}
		// �����ʼ�
		SeaBackKit.fight_send_every(objectFactory,player,
			PublicConst.FIGHT_TYPE_1,beIsland.getName(),beIsland.getIndex(),
			player.getName(),beIsland.getName(),bool,data,award,event,
			fightList,defendList,sourceIndex,beIsland.getSid(),0,0,"",
			beIsland.createFleetGroup(),0);
	}

	/** ������� */
	public static void attackPlayer(Player player,Player beAttacker,
		FightEvent event,CreatObjectFactory objectFactory,
		NpcIsland beIsland,ByteBuffer data,boolean bool,int exp,
		int honorScore,Player allianceDefender,FleetGroup fleetGroup,int feats,int reduceProsperity)
	{
		String allianceDefend="";
		if(allianceDefender!=null)
			allianceDefend=allianceDefender.getName();
		// ����push �ʼ�
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);

		int sourceIndex=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		// ���ط��˱�
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
			// �����ʼ��Է����
			SeaBackKit.fight_send_every(objectFactory,allianceDefender,
				PublicConst.FIGHT_TYPE_9,beAttacker.getName(),beIsland
					.getIndex(),player.getName(),beAttacker.getName(),!bool,
				data,beaward,event,fightList,defendList,sourceIndex,0,0,
				0,allianceDefend,fleetGroup,0);

			// �����ʼ��Է����
			SeaBackKit.fight_send_every(objectFactory,beAttacker,
				PublicConst.FIGHT_TYPE_8,beAttacker.getName(),beIsland
					.getIndex(),player.getName(),beAttacker.getName(),!bool,
				data,null,event,fightList,defendList,sourceIndex,0,0,honorScore,
				allianceDefend,fleetGroup,0,reduceProsperity);
		}
		else
		{
			// �����ʼ��Է����
			SeaBackKit.fight_send_every(objectFactory,beAttacker,
				PublicConst.FIGHT_TYPE_3,beAttacker.getName(),beIsland
					.getIndex(),player.getName(),beAttacker.getName(),!bool,
				data,beaward,event,fightList,defendList,sourceIndex,0,0,
				honorScore,allianceDefend,beAttacker.getIsland().getMainGroup(),0,reduceProsperity);
		}
	}

	/** ����Ұ��פ�ص���� bool�������Ƿ�ʤ�� dataս���������� */
	public static void attackTempPlayer(Player player,Player beAttacker,
		FightEvent event,FightEvent holdEvent,
		CreatObjectFactory objectFactory,NpcIsland beIsland,ByteBuffer data,
		boolean bool,int exp)
	{
		int sourceIndex=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		// ����push �ʼ�
		IntList fightList=event.getFleetGroup().hurtList(
			FleetGroup.HURT_TROOPS);
		// ���ط��˱�
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

		// �����ʼ��Է����
		SeaBackKit.fight_send_every(objectFactory,beAttacker,
			PublicConst.FIGHT_TYPE_4,beIsland.getName(),beIsland.getIndex(),
			player.getName(),beAttacker.getName(),!bool,data,beaward,event,
			fightList,defendList,sourceIndex,beIsland.getSid(),0,0,"",
			holdEvent.getFleetGroup(),0);
	}
	
	/** ���˵�������ս */
	public static void allianceFight(Player player,Player beAttacker,
		CreatObjectFactory objectFactory,ByteBuffer data,
		FleetGroup fleetGroup1,FleetGroup fleetGroup2,boolean victory,
		Alliance attackAlliance,Alliance beAttackAlliance,ArrayList list1,
		ArrayList list2)
	{
		// ������
		IntList intList=fleetGroup1.hurtList(FleetGroup.HURT_TROOPS);
		// ������
		IntList deintList=fleetGroup2.hurtList(FleetGroup.HURT_TROOPS);
		/** ��ս�� **/
		Message message1=SeaBackKit.fight_send_alliance(player,beAttacker,
			victory,data,intList,deintList,attackAlliance.getName(),
			beAttackAlliance.getName(),attackAlliance.getId(),
			PublicConst.FIGHT_TYPE_18,fleetGroup1,fleetGroup2);
		/** ����ս�� **/
//		Message message2=SeaBackKit.fight_send_alliance(beAttacker,player,
//			!victory,data,intList,deintList,beAttackAlliance.getName(),
//			attackAlliance.getName(),beAttackAlliance.getId(),
//			PublicConst.FIGHT_TYPE_19,fleetGroup2,fleetGroup1);
		Message message2=SeaBackKit.fight_send_alliance(player,beAttacker,
			victory,data,intList,deintList,attackAlliance.getName(),
			beAttackAlliance.getName(),beAttackAlliance.getId(),
			PublicConst.FIGHT_TYPE_19,fleetGroup1,fleetGroup2);

		// ��������ս��title
		message1.setAllianceFightTile(player.getName(),
			attackAlliance.getName(),beAttacker.getName(),
			beAttackAlliance.getName(),victory);
		// ��������ս��title
		message2.setAllianceFightTile(player.getName(),
			attackAlliance.getName(),beAttacker.getName(),
			beAttackAlliance.getName(),victory);

		list1.add(message1);
		list2.add(message2);
	}

	
}
