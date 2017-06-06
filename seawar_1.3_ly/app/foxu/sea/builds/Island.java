package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.Science;
import foxu.sea.Ship;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.builds.produce.CommandProduce;
import foxu.sea.builds.produce.ResourcesProduce;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.checkpoint.Chapter;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.messgae.SystemMessageState;
import foxu.sea.proplist.Prop;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.task.TaskEventExecute;

/**
 * ������ �������ӵ�еĽ����ͱ��� author:icetiger
 */
public class Island
{

	public static final int ISLAND_DEFAULT_LEVEL=1;
	/** *��ʼ����λ�� �����λ�� */
	public static final int BUILD_NUM=2,BUILD_MAX=7;
	/** Ĭ�Ͻ������� */
	public static final int PRODUCE_NUM=1,PRODUCE_MAX=7;
	/** ǰ̨��Ҫ�ĵ���sid */
	int islandSid=1;
	/** �������� ��ʼ��2 */
	int buildNum=BUILD_NUM;
	/** �����ȴ����� Ĭ��Ϊ1�� ��ǰ�����Ķ��� */
	int produceNum=PRODUCE_NUM;
	/** Serialization fileds */
	/** ӵ�еĽ��� */
	ObjectArray builds=new ObjectArray();
	/** ӵ�еı����ͳǷ����� */
	ObjectArray troops=new ObjectArray();
	/** �˱� �ȴ��ظ����˱� */
	ObjectArray hurtsTroops=new ObjectArray();
	/** ����ȼ� */
	int islandLevel=ISLAND_DEFAULT_LEVEL;
	/** ����״̬���������������� */
	int state;
	/** ios������� */
	int iosSystem;

	/** �Ƿ����� */
	FleetGroup mainGroup=new FleetGroup();
	/** �Ѿ���ȡ����ϵͳ�ʼ�id,state */
	ObjectArray readSystemMessage=new ObjectArray();

	/** dynamic */
	/** ӵ����� */
	Player player;

	/** �鿴ָ��ϵͳ�ʼ��Ƿ��Ѿ�ɾ�� */
	public boolean isStateMessage(Message message,int state)
	{
		if(message.getMessageType()!=Message.SYSTEM_TYPE) return true;
		Object[] array=readSystemMessage.getArray();
		for(int i=0;i<array.length;i++)
		{
			SystemMessageState system=(SystemMessageState)array[i];
			if(system.getMessageId()==message.getMessageId()
				&&system.getState()==state) return true;
		}
		return false;
	}

	/** �鿴ָ��ϵͳ�ʼ��Ķ�ȡ״̬ */
	public int getStateMessage(Message message)
	{
		if(message.getMessageType()!=Message.SYSTEM_TYPE) return 0;
		Object[] array=readSystemMessage.getArray();
		for(int i=0;i<array.length;i++)
		{
			SystemMessageState system=(SystemMessageState)array[i];
			if(system.getMessageId()==message.getMessageId())
				return system.getState();
		}
		return 0;
	}

	/** �Զ������������� */
	public synchronized boolean autoAddMainGroup()
	{
		if(!SeaBackKit.isOpen(PublicConst.AUTO_ADD_MAINGROUP,player
			.getIsland().getIosSystem())) return false;
		IntList autoList=mainGroup.hurtList(FleetGroup.AUTO_ADD_SHIP);
		boolean bool=false;
		// sid,num
		for(int i=0;i<autoList.size();i+=3)
		{
			int shipSid=autoList.get(i);
			int canAddNum=autoList.get(i+1);
			int location=autoList.get(i+2);
			// �鿴�ۿ��Ƿ��д�ֻ
			canAddNum=reduceShipBySid(shipSid,canAddNum,troops);
			if(canAddNum>0)
			{
				bool=true;
				// ���������ӵ�ĳ��location�������
				mainGroup.addShipByLocation(location,canAddNum);
			}
		}
		if(bool)
		{
			JBackKit.resetMainGroup(player);
			// ˢ��ǰ̨
			JBackKit.sendResetTroops(player);
		}
		return bool;
	}
	/** ��õ�ǰ�����ȴ����и��� */
	public int getProduceNum()
	{
		return produceNum;
	}

	/** ��ȡĳ����Դ���ܼƲ��� */
	public float getProduceWithType(int buildType)
	{
		Object builds[]=getBuildArray();
		float produceNum=0.0f;
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild.getBuildType()==buildType
				&&playerBuild.getBuildLevel()>0)
			{
				Produce produce=playerBuild.getProduce();
				// �������Դ����
				if(produce instanceof ResourcesProduce)
				{
					float num=((ResourcesProduce)produce).produceNum(
						playerBuild.getBuildLevel()-1,player,TimeKit
							.getSecondTime());
					produceNum+=num;
				}
			}
		}
		// �����ָ�����ĵĲ���
		PlayerBuild build=(PlayerBuild)getBuildByIndex(BuildInfo.INDEX_0,
			getBuilds());
		CommandProduce commandPorudce=(CommandProduce)build.getProduce();
		// �������͵�type��Ӧ��serverType
		int serverType=PublicConst.ADD_METAL_BUFF;
		//������BUFF
		int foreServerType=PublicConst.FORE_METAL_BUFF;
		//�ؿ�buff
		int pointType=Chapter.METAL;
		if(buildType==Build.BUILD_OIL)
		{
			serverType=PublicConst.ADD_OIL_BUFF;
			foreServerType=PublicConst.FORE_OIL_BUFF;
			pointType=Chapter.OIL;
		}
		else if(buildType==Build.BUILD_SILION)
		{
			serverType=PublicConst.ADD_SILICON_BUFF;
			foreServerType=PublicConst.FORE_SILICON_BUFF;
			pointType=Chapter.SILICON;
		}
		else if(buildType==Build.BUILD_URANIUM)
		{
			serverType=PublicConst.ADD_URANIUM_BUFF;
			foreServerType=PublicConst.FORE_URANIUM_BUFF;
			pointType=Chapter.URANIUM;
		}
		else if(buildType==Build.BUILD_MONEY)
		{
			serverType=PublicConst.ADD_MONEY_BUFF;
			foreServerType=PublicConst.FORE_MONEY_BUFF;
			pointType=Chapter.MONEY;
		}
		produceNum+=commandPorudce.produceNum(build.getBuildLevel()-1,
			player,serverType,TimeKit.getSecondTime());
		produceNum+=commandPorudce.produceForeNum(build.getBuildLevel()-1,
			player,foreServerType,TimeKit.getSecondTime());
		produceNum+=commandPorudce.producePointNum(pointType,
			commandPorudce.noBuffProduceNum(build.getBuildLevel()-1,player),player);
		return produceNum;
	}
	/** ��������λ */
	public void upBuildNum()
	{
		buildNum++;
		if(buildNum>BUILD_MAX) buildNum=BUILD_MAX;
	}

	/** ���һ��ϵͳ�ʼ���״̬ */
	public void addStateSystemMessage(int state,int messageId)
	{
		Object[] readSystemMessages=this.readSystemMessage.getArray();
		for(int i=0;i<readSystemMessages.length;i++)
		{
			SystemMessageState message=(SystemMessageState)readSystemMessages[i];
			if(message.getMessageId()==messageId)
			{
				message.setState(state);
				return;
			}
		}
		SystemMessageState message=new SystemMessageState();
		message.setMessageId(messageId);
		message.setState(state);
		this.readSystemMessage.add(message);
	}

	/** �õ����Ƿ�ӵ�д�index */
	public boolean isPlayerHaveIndex(int index)
	{
		return BuildInfo.isHaveIndex(index,player);
	}

	/** ��Ӧλ���Ƿ���Խ��������ͽ��� */
	public boolean isBuildThisType(String buildType,int index)
	{
		return BuildInfo.isBuildThisType(buildType,index);
	}

	/** ��õ�ǰӵ�пƼ� */
	public Science[] getSciences()
	{
		PlayerBuild playerBuild=getBuildByType(Build.BUILD_RESEARCH,builds);
		if(playerBuild==null) return null;
		ScienceProduce produce=(ScienceProduce)playerBuild.getProduce();
		return produce.getAllScience();
	}

	/** ����������Դ ���ߵ�ʱ�� */
	public void pushAll(int checkTime,CreatObjectFactory objectFactory)
	{
		// push�˱�
		// gotHurtTroops(checkTime);
		gotNowBuilding(checkTime,objectFactory);
		gotProsperityInfo(TimeKit.getSecondTime());
		gotProduce(checkTime,objectFactory);
		gotEnergy(checkTime);
		player.checkService(checkTime);
		// ��ʼ���������ӵľ�����Ϣ
		getMainGroup().getOfficerFleetAttr().initOfficers(player);
		JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.ONLINE_PUSH_ALL);
	}

	/** ���뽱�� */
	public void pushInviet(CreatObjectFactory objectFactory)
	{
		// �����Լ���
		int inveted[]=player.getInviter_id();
		for(int i=0;i<inveted.length;i+=2)
		{
			if(inveted[i+1]==0)
			{
				// �Լ�����20��ʯ
				if(inveted[i]==player.getId())
				{
					int gems=20;
					Resources.addGemsNomal(20,player.getResources(),player);
					MessageKit.sendBeInvetedGems(player,objectFactory);
					if(player.getInveted()!=0)
					{
						gems+=10;
						Resources.addGemsNomal(10,player.getResources(),
							player);
						// ��ƽ��
						Prop prop=(Prop)Prop.factory.newSample(509);
						player.getBundle().incrProp(prop,true);
						JBackKit.sendResetBunld(player);
					}
					// ��ʯ��־��¼
					objectFactory.createGemTrack(GemsTrack.INVITE,player
						.getId(),gems,0,
						Resources.getGems(player.getResources()));
				}
				// ����һ��������Դ�� ����ƷsidΪ5
				else
				{
					Prop prop=(Prop)Prop.factory.newSample(4);
					player.getBundle().incrProp(prop,true);
					JBackKit.sendResetBunld(player);
					Resources.addGemsNomal(15,player.getResources(),player);
					// �����ʼ�
					MessageKit.sendInvetedMoney(player,objectFactory,
						inveted[i]);
					// ��ʯ��־��¼
					objectFactory.createGemTrack(GemsTrack.INVITE,player
						.getId(),15,inveted[i],
						Resources.getGems(player.getResources()));
				}
				inveted[i+1]=1;
			}
			player.setInviter_id(inveted);
		}
	}

	// /** �����˱� */
	// public void gotHurtTroops(int checkTime)
	// {
	// Object object[]=hurtsTroops.getArray();
	// for(int i=0;i<object.length;i++)
	// {
	// HurtTroop troop=(HurtTroop)object[i];
	// int num=troop.nowHurtsNum(checkTime);
	// troop.setNum(num);
	// if(num<=0)
	// {
	// hurtsTroops.remove(troop);
	// }
	// }
	// }
	
	/**
	 * ��ȡ���ٶ�����  ���ݽ������ٺ͵ȼ�����
	 */
	public int getProsperityMax(){
		
		Object[] builds=this.builds.getArray();
		int max = 0;
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild = (PlayerBuild)builds[i];
			max +=playerBuild.getBuildLevel() * playerBuild.getGiveProsperity();
		}
		player.getProsperityInfo()[2]=max;
		
		return max;
	}
	
	/**��ȡˢ�µ�ǰ ���ٶ������Ϣ */
	public int gotProsperityInfo(int checkTime){
		
		int[] prosperityInfo=player.getProsperityInfo();

		synchronized(prosperityInfo)
		{
			int max=getProsperityMax();// ��ǰ���ٶ�����
			if(prosperityInfo[0]>=max||prosperityInfo[1]==0)
			{// ������ٶ����� �����ô��������ж� �ų��ݻٽ����Ŀ���
				prosperityInfo[0]=max; 
				prosperityInfo[1]=checkTime;// ���ü��ʱ��
				// ���÷��ٶȵȼ�
				int lv=0;
				for(int i=0;i<Player.PROSPERITY_lV_BUFF.length;i+=3)
				{
					if(max<Player.PROSPERITY_lV_BUFF[i]){
						lv=i/3-1;
						break;
					}
					if(lv==0&&i==Player.PROSPERITY_lV_BUFF.length-3){
						lv = (Player.PROSPERITY_lV_BUFF.length-3)/3;
					}
				}
				
				prosperityInfo[3]=lv;
				JBackKit.sendResetProsperity(player);
				return prosperityInfo[0];
			}
			int count=(checkTime-prosperityInfo[1])/Player.PROSPERITY_TIME;// ���ٶȵ���
			if(count>0)
			{
				int prosperity=prosperityInfo[0]+count;
				if(prosperity>=max) prosperity=max;
				prosperityInfo[0]=prosperity;
				// ���÷��ٶȵȼ�
				int lv=0;
				for(int i=0;i<Player.PROSPERITY_lV_BUFF.length;i+=3)
				{
					if(prosperity<Player.PROSPERITY_lV_BUFF[i]){
						lv=i/3-1;
						break;
					}
					if(lv==0&&i==Player.PROSPERITY_lV_BUFF.length-3){
						lv = (Player.PROSPERITY_lV_BUFF.length-3)/3;
					}
				}
				prosperityInfo[3]=lv;
				// ���ü��ʱ��
				prosperityInfo[1]+=count*Player.PROSPERITY_TIME;
				JBackKit.sendResetProsperity(player);
			}
			//��ӡ����
			/*System.out.println("���ٶ�:"+prosperityInfo[0]);
			System.out.println("���ٶ�checkTime:"+prosperityInfo[1]);
			System.out.println("���ٶ�MAX:"+prosperityInfo[2]);
			System.out.println("���ٶ�LV:"+prosperityInfo[3]);*/
			return prosperityInfo[0];
			
		}
	}
	
	/** ��ȡ��ǰ����ֵ */
	public int gotEnergy(int checkTime)
	{
		if(player.getActives()[Player.ENERGY_INDEX]>=Player.MAX_ENERGY)
		{
			return player.getActives()[Player.ENERGY_INDEX];
		}
		int times=(checkTime-player.getActives()[Player.ENERGY_TIME_INDEX])
			/Player.ENERGY_TIME;
		if(times>0)
		{
			player.getActives()[Player.ENERGY_INDEX]+=times;
			int leftTime=(checkTime-player.getActives()[Player.ENERGY_TIME_INDEX])
				%Player.ENERGY_TIME;
			player.getActives()[Player.ENERGY_TIME_INDEX]=(checkTime-leftTime);
			if(player.getActives()[Player.ENERGY_INDEX]>Player.MAX_ENERGY)
				player.getActives()[Player.ENERGY_INDEX]=Player.MAX_ENERGY;
		}
		return player.getActives()[Player.ENERGY_INDEX];
	}

	/** ��ȡ������Դ 5����һ�� */
	public void gotProduce(int checkTime,CreatObjectFactory objectFactory)
	{
		Object[] builds=this.builds.getArray();
		for(int i=0;i<builds.length;i++)
		{
			/** ��Դ��� */
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild==null) continue;
			playerBuild.produce(player,checkTime,objectFactory);
		}
	}

	/** �鿴���ڽ����¼� �Ƿ���� */
	public void gotNowBuilding(int checkTime,CreatObjectFactory fatory)
	{
		Object[] builds=this.builds.getArray();
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild==null) continue;
			if(playerBuild.getBuildCompleteTime()==0) continue;
			/** ������� �п��������� */
			if(playerBuild.checkBuildTime(checkTime))
			{
				/** ��ø�index�Ľ��� */
				playerBuild.setBuildCompleteTime(0);
				if(playerBuild.getBuildLevel()==0)
				{
					playerBuild.levelUp();
					// ��ȡ����
					player.incrExp(playerBuild
						.getLevelExperience(playerBuild.getBuildLevel()-1),fatory);
					// ��ȡ���ٶ�
					int[] prosperityInfo =  player.getProsperityInfo();
					synchronized(prosperityInfo)
					{
						prosperityInfo[0]+=playerBuild.getGiveProsperity();
						prosperityInfo[2]+=playerBuild.getGiveProsperity();
					}
					// ����change��Ϣ
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.BUILD_FINISH_TASK_EVENT,playerBuild,
						player,null);
					JBackKit.sendResetProsperity(player);
				}
				else
				{
					playerBuild.levelUp();
					// ��ȡ����
					player.incrExp(playerBuild
						.getLevelExperience(playerBuild.getBuildLevel()-1),fatory);
					// ��ȡ���ٶ�
					int[] prosperityInfo =  player.getProsperityInfo();
					synchronized(prosperityInfo)
					{
						prosperityInfo[0]+=playerBuild.getGiveProsperity();
						prosperityInfo[2]+=playerBuild.getGiveProsperity();
					}
					// ����change��Ϣ
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.BUILD_FINISH_TASK_EVENT,playerBuild,
						player,null);
					JBackKit.sendResetProsperity(player);
				}
				//�ɾ���Ϣ�ɼ�
				AchieveCollect.buildLevel(playerBuild,this,player);
				//�±�����
				RecruitKit.pushTaskBuild(playerBuild,player);
			}
		}
	}

	/** ��ǰindex�Ƿ������� */
	public boolean checkNowBuildingByIndex(int index)
	{
		Object[] nowbuilding=this.builds.getArray();
		for(int i=0;i<nowbuilding.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)nowbuilding[i];
			if(playerBuild.getBuildCompleteTime()!=0
				&&playerBuild.getIndex()==index) return true;
		}
		return false;
	}

	/** ȡ�������¼� */
	public void cancelBuilding(int index)
	{
		/** ����˥�������Դ */
		Object[] builds=this.builds.getArray();
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild playerBuild=(PlayerBuild)builds[i];
			if(playerBuild.getIndex()==index
				&&playerBuild.getBuildCompleteTime()!=0)
			{
				playerBuild.cancelBuilding(player);
				// �½�����
				if(playerBuild.getBuildLevel()==0)
				{
					this.builds.remove(playerBuild);
				}
				return;
			}
		}
	}

	/** ��óǷ�������Ӧsid�Ĵ�ֻ���� */
	public int getShipsBySidForDefendGroup(int shipSid)
	{
		int num=0;
		Fleet fleet[]=mainGroup.getArray();
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null||fleet[i].getNum()<=0) continue;
			if(fleet[i].getShip().getSid()==shipSid)
			{
				num+=fleet[i].getNum();
			}
		}
		return num;
	}

	/** ��õ�ǰsid�Ĵ�ֻ���� */
	public int getShipsBySid(int shipSid,ObjectArray troopArray)
	{
		if(troopArray==null) troopArray=this.troops;
		Object[] troops=troopArray.getArray();
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				return troop.getNum();
			}
		}
		return 0;
	}

	/** ����ship���� */
	public int getShipsByType(int shipType,ObjectArray troopArray)
	{
		if(troopArray==null) troopArray=this.troops;
		Object[] troops=troopArray.getArray();
		int num=0;
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			Ship ship=(Ship)Ship.factory.getSample(troop.getShipSid());
			if(ship!=null||ship.getPlayerType()==shipType)
			{
				num+=troop.getNum();
			}
		}
		return num;
	}

	/** ����ĳ��sid�Ĵ� ���ؼ��ٵĴ�ֻ����* */
	public synchronized int reduceShipBySid(int shipSid,int num,
		ObjectArray troopArray)
	{
		if(troopArray==null) troopArray=troops;
		Object[] troops=troopArray.getArray();
		int reduceNum=0;
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid&&troop.getNum()>0)
			{
				reduceNum=troop.reduceNum(num);
				if(troop.getNum()<=0) troopArray.remove(troop);
				return reduceNum;
			}
		}
		return 0;
	}

	/** ������д�ֻ���� �ų��Ƿ� */
	public int getAllShipsNum()
	{
		Object[] troops=this.troops.getArray();
		int num=0;
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			Ship ship=(Ship)Role.factory.getSample(troop.getShipSid());
			if(ship!=null&&ship.isMoveShips()) num+=troop.getNum();
		}
		return num;
	}

	/** �����˱� */
	public synchronized boolean reduceHurtTroop(int shipSid,int nums)
	{
		if(shipSid==0||nums==0) return false;
		Object[] troops=hurtsTroops.getArray();
		for(int i=0;i<troops.length;i++)
		{
			HurtTroop troop=(HurtTroop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				troop.reduceNum(nums);
				if(troop.getNum()<=0)
				{
					hurtsTroops.remove(troop);
				}
				return true;
			}
		}
		return false;
	}

	/** ����˱� */
	public synchronized void addHurtTroop(int shipSid,int nums,int time)
	{
		if(shipSid==0||nums==0) return;
		Object[] troops=hurtsTroops.getArray();
		for(int i=0;i<troops.length;i++)
		{
			HurtTroop troop=(HurtTroop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				// troop.nowHurtsNum(time);
				troop.addNums(nums);
				return;
			}
		}
		HurtTroop addTroop=new HurtTroop();
		addTroop.setNum(nums);
		addTroop.setShipSid(shipSid);
		addTroop.setTime(time);
		hurtsTroops.add(addTroop);
	}

	/** ���ٱ��� */
	public synchronized void reduceTroop(int shipSid,int nums,
		ObjectArray troopArray)
	{
		if(shipSid==0||nums==0) return;
		if(troopArray==null) troopArray=troops;
		Object[] troops=troopArray.getArray();
		// ���ƴ���������Ҫ��������
		// int nowHave=getAllShipsNum();
		// int limiteNum=getShipNumLimite();
		// if((nowHave+nums)>limiteNum)
		// {
		// nums=limiteNum-nowHave;
		// }
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				troop.reduceNum(nums);
				return;
			}
		}
		Troop addTroop=new Troop();
		addTroop.setNum(nums);
		addTroop.setShipSid(shipSid);
		troopArray.add(addTroop);
	}

	/** ��ӱ��� */
	public synchronized void addTroop(int shipSid,int nums,
		ObjectArray troopArray)
	{
		if(shipSid==0||nums==0) return;
		if(troopArray==null) troopArray=troops;
		Object[] troops=troopArray.getArray();
		// ���ƴ���������Ҫ��������
		// int nowHave=getAllShipsNum();
		// int limiteNum=getShipNumLimite();
		// if((nowHave+nums)>limiteNum)
		// {
		// nums=limiteNum-nowHave;
		// }
		for(int i=0;i<troops.length;i++)
		{
			Troop troop=(Troop)troops[i];
			if(troop.getShipSid()==shipSid)
			{
				troop.addNums(nums);
				return;
			}
		}
		Troop addTroop=new Troop();
		addTroop.setNum(nums);
		addTroop.setShipSid(shipSid);
		troopArray.add(addTroop);
	}

	/** ���һ������,�����Ƿ�ɹ� */
	public boolean addBuild(PlayerBuild build)
	{
		if(getBuildByIndex(build.getIndex(),builds)!=null) return false;
		int percent=0;
		// �Ƽ��������ٱ���
		AdjustmentData data=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.BUILD_BUFF));
		//�������ٻ
		int activypercent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.BUILD_ID);
		if(data!=null)
		{
			percent=data.percent;
		}
		int time=build.getBuildTime()[0]*100/(100+percent)*(100-activypercent)/100;
		build.setBuildCompleteTime(TimeKit.getSecondTime()+time);
		build.setBuildTotleTime(time);
		build.init(build.getBuildCompleteTime());
		builds.add(build);
		return true;
	}

	/** ֱ�����һ����Ʒ���� */
	public boolean addBuildNow(PlayerBuild build,int index)
	{
		if(getBuildByIndex(index,builds)!=null) return false;
		if(!BuildInfo.isBuildThisType(build.getBuildType()+"",index))
			return false;
		build.init(TimeKit.getSecondTime());
		build.setIndex(index);
		builds.add(build);
		return true;
	}
	/**
	 * �Ƴ�һ������ ��������
	 * 
	 * @param index ����index
	 * @return �����Ƿ�ɹ�
	 */
	public boolean deleteBuild(int index)
	{
		PlayerBuild build=getBuildByIndex(index,this.builds);
		if(build!=null)
		{
			this.builds.remove(build);
			return true;
		}
		return false;
	}

	/** ���ĳһ������ ���ܻ��ж�� ֻ�����ж��Ƿ���� */
	public PlayerBuild getBuildBySid(int sid,ObjectArray buildss)
	{
		Object[] builds=buildss.getArray();
		for(int i=0;i<builds.length;i++)
		{
			if(((PlayerBuild)builds[i]).getSid()==sid)
			{
				return (PlayerBuild)builds[i];
			}
		}
		return null;
	}

	/** ����ж�����صȼ���ߵ� */
	public PlayerBuild getBuildByType(int buildType,ObjectArray buildss)
	{
		if(buildss==null) buildss=builds;
		Object[] builds=buildss.getArray();
		PlayerBuild playerBuild=null;
		for(int i=0;i<builds.length;i++)
		{
			if(((PlayerBuild)builds[i]).getBuildType()==buildType)
			{
				PlayerBuild checkBuild=(PlayerBuild)builds[i];
				if(playerBuild==null
					||playerBuild.getBuildLevel()<checkBuild.getBuildLevel())
					playerBuild=(PlayerBuild)builds[i];
			}
		}
		return playerBuild;
	}

	/** ���ĳһ�����ͽ������� */
	public int getBuildNumByType(int buildType,ObjectArray buildss)
	{
		Object[] builds=buildss.getArray();
		int num=0;
		for(int i=0;i<builds.length;i++)
		{
			if(((PlayerBuild)builds[i]).getBuildType()==buildType)
			{
				num++;
			}
		}
		return num;
	}

	/** ����index���һ������ */
	public PlayerBuild getBuildByIndex(int index,ObjectArray buildss)
	{
		if(buildss==null) buildss=builds;
		Object[] builds=buildss.getArray();
		for(int i=0;i<builds.length;i++)
		{
			if(builds[i]==null) continue;
			if(((PlayerBuild)builds[i]).getIndex()==index)
			{
				return (PlayerBuild)builds[i];
			}
		}
		return null;
	}

	/** ��ȡ�Զ������������� */
	public PlayerBuild[] getAutoBuildArray()
	{
		Object[] buildings=builds.getArray();
		PlayerBuild[] array=new PlayerBuild[buildings.length];
		int onBuilding=0;
		for(int i=0;i<buildings.length;i++)
		{
			PlayerBuild b=(PlayerBuild)buildings[i];
			if(checkNowBuildingByIndex(b.getIndex()))
			{
				if(onBuilding!=i)
				{
					PlayerBuild temp=array[onBuilding];
					array[onBuilding]=b;
					array[i]=temp;
					onBuilding++;
					continue;
				}
			}
			array[i]=b;
		}
		//�����������Ľ���������ǰ�˲����������ʱ������
		for(int i=0;i<onBuilding;i++)
		{
			for(int j=i+1;j<onBuilding;j++)
			{
				if(array[j].getBuildCompleteTime()<array[i].getBuildCompleteTime())
				{
					PlayerBuild temp=array[i];
					array[i]=array[j];
					array[j]=temp;
				}
			}
		}
		//�����������������ȼ�����
		for(int i=onBuilding;i<array.length;i++)
		{
			for(int j=i+1;j<array.length;j++)
			{
				if(array[j].getBuildLevel()<array[i].getBuildLevel())
				{
					PlayerBuild temp=array[i];
					array[i]=array[j];
					array[j]=temp;
				}
			}
		}
		return array;
	}
	
	public void bytesReadSystemMessage(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			SystemMessageState systemState=new SystemMessageState();
			temp[i]=systemState.bytesRead(data);
		}
		readSystemMessage=new ObjectArray(temp);
		return;
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteSystemMessage(ByteBuffer data)
	{
		if(readSystemMessage.size()>200) readSystemMessage.clear();
		if(readSystemMessage!=null&&readSystemMessage.size()>0)
		{
			Object[] array=readSystemMessage.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((SystemMessageState)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** ���ֽڻ����з����л��õ�һ������ */
	public Object bytesRead(ByteBuffer data)
	{
		islandSid=data.readUnsignedShort();
		buildNum=data.readUnsignedByte();
		produceNum=data.readUnsignedByte();
		islandLevel=data.readUnsignedByte();
		state=data.readUnsignedByte();
		iosSystem=data.readInt();
		bytesReadBuilds(data);
		bytesReadTroop(data);
		mainGroup.bytesRead(data);
		bytesReadSystemMessage(data);
		bytesReadhurtsTroops(data);
		return this;
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(islandSid);
		data.writeByte(buildNum);
		data.writeByte(produceNum);
		data.writeByte(islandLevel);
		data.writeByte(state);
		data.writeInt(iosSystem);
		bytesWriteBuilds(data);
		bytesWriteTroop(data);
		bytesWriteMainGroup(data);
		bytesWriteSystemMessage(data);
		bytesWritehurtsTroops(data);
	}
	public void showBytesWrite(ByteBuffer data,int current,
		CreatObjectFactory objectFactory)
	{
		data.writeShort(islandSid);// д��ǰ̨��Ҫ��sid
		data.writeByte(buildNum);
		data.writeByte(produceNum);
		data.writeByte(islandLevel);
		data.writeByte(state);
		data.writeInt(iosSystem);
		if(builds!=null&&builds.size()>0)
		{
			Object[] array=builds.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				if(array[i]==null) continue;
				((PlayerBuild)array[i]).showBytesWrite(data,current);
			}
		}
		else
		{
			data.writeByte(0);
		}
		showBytesWriteTroops(data,current);
		showBytesWriteMainGroup(data);
		showBytesWritehurtsTroops(data,current);
		bytesWriteEvents(data,objectFactory);
	}

	public void showBytesWriteTroops(ByteBuffer data,int current)
	{
		if(troops!=null&&troops.size()>0)
		{
			Object[] array=troops.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Troop)array[i]).showBytesWrite(data,current);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	public void showBytesWritehurtsTroops(ByteBuffer data,int current)
	{
		if(hurtsTroops!=null&&hurtsTroops.size()>0)
		{
			Object[] array=hurtsTroops.getArray();
			data.writeShort(array.length);
			for(int i=0;i<array.length;i++)
			{
				((HurtTroop)array[i]).showBytesWrite(data,current);
			}
		}
		else
		{
			data.writeShort(0);
		}
	}

	/** ���ֽ������з����л���ö������ */
	public void bytesReadMainGroup(ByteBuffer data)
	{
		mainGroup.bytesRead(data);
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteMainGroup(ByteBuffer data)
	{
		mainGroup.bytesWrite(data);
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void showBytesWriteMainGroup(ByteBuffer data)
	{
		mainGroup.showBytesWrite(data);
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesReadhurtsTroops(ByteBuffer data)
	{
		int n=data.readUnsignedShort();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			HurtTroop troop=new HurtTroop();
			temp[i]=troop.bytesRead(data);
		}
		hurtsTroops=new ObjectArray(temp);
		return this;
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWritehurtsTroops(ByteBuffer data)
	{
		if(hurtsTroops!=null&&hurtsTroops.size()>0)
		{
			Object[] array=hurtsTroops.getArray();
			data.writeShort(array.length);
			for(int i=0;i<array.length;i++)
			{
				((HurtTroop)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeShort(0);
		}
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesReadTroop(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			Troop troop=new Troop();
			temp[i]=troop.bytesRead(data);
		}
		troops=new ObjectArray(temp);
		return this;
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteTroop(ByteBuffer data)
	{
		if(troops!=null&&troops.size()>0)
		{
			Object[] array=troops.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Troop)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesReadBuilds(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			temp[i]=PlayerBuild.bytesReadBuild(data);
		}
		builds=new ObjectArray(temp);
		return this;
	}

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteBuilds(ByteBuffer data)
	{
		if(builds!=null&&builds.size()>0)
		{
			Object[] array=builds.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((PlayerBuild)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}
	/** ����¼����л� */
	public void bytesWriteEvents(ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		// �ҵ���ҵ���
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null)
			data.writeShort(0);
		else
		{
			data.writeShort(fightEventList.size());
			for(int i=0;i<fightEventList.size();i++)
			{
				FightEvent event=(FightEvent)fightEventList.get(i);
				if(event.getDelete()==FightEvent.DELETE_TYPE) continue;
				SeaBackKit.showByteswrite(data,TimeKit.getSecondTime(),
					event,objectFactory);
			}
		}
	}
	/** ����sid time num��ȡ���˱����� */
	public HurtTroop getHurtTroop(int sid)
	{
		Object object[]=hurtsTroops.getArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			HurtTroop troop=(HurtTroop)object[i];
			if(troop.getShipSid()==sid)
			{
				return troop;
			}
		}
		return null;
	}

	/** �Ƴ������˱� */
	public synchronized void removeAllTroop()
	{
		hurtsTroops.clear();
	}

	/** �ָ������˱� */
	public synchronized void repairAllHurtTroops()
	{
		Object object[]=hurtsTroops.getArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			HurtTroop troop=(HurtTroop)object[i];
			// troop.nowHurtsNum(TimeKit.getSecondTime());
			addTroop(troop.getShipSid(),troop.getNum(),troops);
		}
	}

	/**
	 * @return builds
	 */
	public ObjectArray getBuilds()
	{
		return builds;
	}

	public Object[] getBuildArray()
	{
		return builds.getArray();
	}

	/**
	 * @param builds Ҫ���õ� builds
	 */
	public void setBuilds(ObjectArray builds)
	{
		this.builds=builds;
	}

	/**
	 * @return player
	 */
	public Player getPlayer()
	{
		return player;
	}

	/**
	 * @param player Ҫ���õ� player
	 */
	public void setPlayer(Player player)
	{
		this.player=player;
	}

	/**
	 * @return troops
	 */
	public ObjectArray getTroops()
	{
		return troops;
	}

	/**
	 * @param troops Ҫ���õ� troops
	 */
	public void setTroops(ObjectArray troops)
	{
		this.troops=troops;
	}

	/**
	 * @return islandLevel
	 */
	public int getIslandLevel()
	{
		return islandLevel;
	}

	/**
	 * @param islandLevel Ҫ���õ� islandLevel
	 */
	public void setIslandLevel(int islandLevel)
	{
		this.islandLevel=islandLevel;
		// ����change��Ϣ
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.PLAYER_LEVEL_ISLAND_EVENT,this,player,null);
	}

	/**
	 * @return state
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * @param state Ҫ���õ� state
	 */
	public void setState(int state)
	{
		this.state=state;
	}

	/**
	 * @return mainGroup
	 */
	public FleetGroup getMainGroup()
	{
		return mainGroup;
	}

	/**
	 * @param mainGroup Ҫ���õ� mainGroup
	 */
	public void setMainGroup(FleetGroup mainGroup)
	{
		this.mainGroup=mainGroup;
	}

	/**
	 * @return buildNum
	 */
	public int getBuildNum()
	{
		return buildNum;
	}

	/**
	 * @param buildNum Ҫ���õ� buildNum
	 */
	public void setBuildNum(int buildNum)
	{
		this.buildNum=buildNum;
	}

	/**
	 * @return iosSystem
	 */
	public int getIosSystem()
	{
		return iosSystem;
	}

	/**
	 * @param iosSystem Ҫ���õ� iosSystem
	 */
	public void setIosSystem(int iosSystem)
	{
		this.iosSystem=iosSystem;
	}

	/**
	 * @param produceNum Ҫ���õ� produceNum
	 */
	public void setProduceNum(int produceNum)
	{
		this.produceNum=produceNum;
	}

	/**
	 * @return hurtsTroops
	 */
	public ObjectArray getHurtsTroops()
	{
		return hurtsTroops;
	}

	/**
	 * @param hurtsTroops Ҫ���õ� hurtsTroops
	 */
	public void setHurtsTroops(ObjectArray hurtsTroops)
	{
		this.hurtsTroops=hurtsTroops;
	}

}
