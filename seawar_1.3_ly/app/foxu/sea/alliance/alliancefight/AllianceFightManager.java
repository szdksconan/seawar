package foxu.sea.alliance.alliancefight;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AFightEventSave;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.datasave.BattleGroundSave;
import foxu.fight.Ability;
import foxu.fight.ChangeHurtEffect;
import foxu.fight.FightScene;
import foxu.sea.AttrAdjustment;
import foxu.sea.OrderList;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.alliance.Alliance;
import foxu.sea.fight.AllianceFleet;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.Sample;

/**
 * ����ս������
 * 
 * @author yw
 * 
 */
public class AllianceFightManager implements TimerListener
{
	public static AllianceFightManager amanager;
	/** ռ���¼���event ȫ��,�������ݿ���ʼ���� */
	OrderList allEvent=new OrderList();

	/** ���ݿ��-�ڴ棨���õ� player Alliance Alliancefight battleground AFightEvent�� */
	CreatObjectFactory objectFactory;

	/** ��ʼ�� */
	public void init()
	{
		amanager=this;
		checkBattleGround();
		checkAllianceFight();
		addAllEvent();
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"award",60*1000));
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"horn",60*15*1000));
	}
	/** ��ⴴ���ݵ� */
	public void checkBattleGround()
	{
		Sample[] sample=BattleGround.factory.getSamples();
		for(int i=1;i<sample.length;i++)
		{
			if(sample[i]==null) break;
			int sid=sample[i].getSid();
			if(getBGround(sid)==null)
			{
				saveBGround((BattleGround)BattleGround.factory
					.newSample(sid));
			}
		}
	}

	/** ���������ս�¼� */
	public void addAllEvent()
	{
		Object[] objs=objectFactory.getaFightEventMemCache().getCacheMap()
			.valueArray();
		AllianceFightEvent aEvent=null;
		for(int i=0;i<objs.length;i++)
		{
			aEvent=(AllianceFightEvent)((AFightEventSave)objs[i]).getData();
			if(aEvent.getType()==AllianceFightEvent.WIN)
			{
				boolean add=false;
				for(int k=0;k<allEvent.size();k++)
				{
					if(aEvent.getCreateTime()>((AllianceFightEvent)allEvent
						.get(k)).getCreateTime())
					{
						allEvent.addAt(aEvent,k);
						add=true;
						break;
					}
				}
				if(!add)
				{
					allEvent.add(aEvent);
				}
				if(allEvent.size()>AllianceFight.EVENT_MAX)
				{
					allEvent.remove();
				}

			}
		}
	}
	/** ��� Ϊû����ս���� ������ս ��ʼ���������� */
	public void checkAllianceFight()
	{
		Object[] objs=objectFactory.getAllianceMemCache().getCacheMap()
			.valueArray();
		AllianceFight afight=null;
		int allianceID=0;
		for(int i=0;i<objs.length;i++)
		{
			allianceID=((AllianceSave)objs[i]).getId();
			afight=getAfight(allianceID);
			if(afight==null)
			{
				afight=createAllianceFight(allianceID,objectFactory);
			}
			afight.initRankData();
			BattleGround ground=getHoldBGround(allianceID);
			afight.checkProduceFinish(afight.getUpship(),ground);
			afight.checkProduceFinish(afight.getUpship1(),ground);
		}
	}
	/** ������ս */
	public AllianceFight createAllianceFight(int allianceID,
		CreatObjectFactory objectFactory)
	{
		AllianceFight allianceFight=new AllianceFight(allianceID);
		saveAFight(allianceFight);
		return allianceFight;
	}
	/** �贬 */
	public String dinateShip(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		String result=(String)objs[0];
		if(result==null)
		{
			AllianceFight afight=(AllianceFight)objs[1];
			result=afight.dinateShip(player,data,getHoldBGround(afight.getAllianceID()),objectFactory);
			if(result==null)
			{
				recruit(afight,false);
				JBackKit.sendResetTroops(player);
				JBackKit.sendAfightFleet(objectFactory,
					getAlliance(afight.getAllianceID()),afight);
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.ALLIANCE_DINATE_SHIP);
			}
		}
		return result;

	}
	/** ��ȡ���оݵ� */
	public Object[] getBGrounds()
	{
		return objectFactory.getBattleGroundMemCache().getCacheMap()
			.valueArray();
	}
	/** ��ȡ��ռ��ľݵ� */
	public BattleGround getHoldBGround(int id)
	{
		Object[] objs=getBGrounds();
		BattleGround ground=null;
		for(int i=0;i<objs.length;i++)
		{
			ground=(BattleGround)((BattleGroundSave)objs[i]).getData();
			if(id==ground.getId()) return ground;
		}
		return null;
	}
	/** �����Զ����� */
	public String setRecruit(Player player,ByteBuffer data)
	{
		String a_id=player.getAttributes(PublicConst.ALLIANCE_ID);
		int allianceID=Integer.parseInt(a_id);
		Alliance attackAlliance=getAlliance(allianceID);
		AllianceFight attackAfight=getAfight(allianceID);
		if(attackAfight==null) return "no afight";
		if(!attackAlliance.isMaster(player.getId())) return "not master";
		int onoff=data.readUnsignedByte();
		attackAfight.setReinforce(onoff);
		BattleGround ground=recruit(attackAfight,false);
		if(ground==null)return "no hold ground";
		// ˢ�´�ֻ
		JBackKit.sendAfightFleet(objectFactory,attackAlliance,attackAfight);
		data.clear();
		data.writeByte(attackAfight.getHorn().getCPercent());
		ground.showBytesWriteFleet(data);
		data.writeBoolean(attackAfight.isReinforce());
		return null;
	}
	/** ���� */
	public String doRecruit(Player player,ByteBuffer data)
	{
		String a_id=player.getAttributes(PublicConst.ALLIANCE_ID);
		int allianceID=Integer.parseInt(a_id);
		Alliance attackAlliance=getAlliance(allianceID);
		if(!attackAlliance.isMaster(player.getId())) return "not master";
		Object[] objs=checkAFight(player);
		if(objs[0]==null)
		{
			BattleGround ground=recruit((AllianceFight)objs[1],true);
			if(ground==null)
			{
				JBackKit.flushGrounds(getBGrounds(),player,this);
				return "no hold ground";
			}
			// ˢ�´�ֻ
			JBackKit.sendAfightFleet(objectFactory,getAlliance(Integer
				.parseInt(player.getAttributes(PublicConst.ALLIANCE_ID))),
				(AllianceFight)objs[1]);
			data.clear();
			data.writeByte(((AllianceFight)objs[1]).getHorn().getCPercent());// �Ž�ֵ
			ground.showBytesWriteFleet(data);
			data.writeBoolean(((AllianceFight)objs[1]).reinforce==1);
		}
		return (String)objs[0];
	}
	/** �Զ����� */
	public BattleGround recruit(AllianceFight afight,boolean force)
	{
		BattleGround ground=getHoldBGround(afight.getAllianceID());
		if(ground!=null)
		{
			ground.recruit(objectFactory,force);
			saveBGround(ground);
		}
		saveAFight(afight);
		return ground;

	}
	/** �ж��Ƿ���ռ��ݵ� */
	public boolean isHold(int allianceId)
	{
		BattleGroundSave[] save=(BattleGroundSave[])objectFactory
			.getBattleGroundMemCache().getCacheMap().valueArray();
		for(int i=0;i<save.length;i++)
		{
			if(((BattleGround)save[i].getData()).getId()==allianceId)
			{
				return true;
			}
		}
		return false;
	}

	/** ����ĳ���ݵ� */
	public String occupyBattleGround(Player player,
		ByteBuffer data)
	{
		String a_id=player.getAttributes(PublicConst.ALLIANCE_ID);
		int allianceID=Integer.parseInt(a_id);
		Alliance attackAlliance=getAlliance(allianceID);
		AllianceFight attackAfight=getAfight(allianceID);
		if(attackAfight==null) return "no afight";
		if(!attackAlliance.isMaster(player.getId())) return "not master";
		synchronized(attackAfight)
		{
			int battleSid=data.readUnsignedShort();
			battleSid=getBttaleSid(battleSid);
			BattleGround bground=getBGround(battleSid);
			if(bground==null) return "no battle ground";
			if(getHoldBGround(allianceID)!=null) return "hold ground";
//			if(attackAfight.getReadDayCount()>=AllianceFight.FIGHT_MAX)
//				return "fight max";
			if(attackAfight.getHorn().getCPercent()<0)return "limit horn";

			int defAllianceId=bground.id;
			int eType=AllianceFightEvent.WIN;
			AllianceFight defAfight=null;
			Alliance defAlliance=null;
			ByteBuffer fightData=null;
			IntList decrlist=new IntList();
			String result=attackAfight
				.decrShip(data,decrlist,attackAlliance);
			if(result!=null) return result;
			IntList attackLoss=new IntList();
			IntList defLoss=new IntList();
			int dcrhorn=0;
			synchronized(bground)
			{

				FleetGroup attackGroup=null;
				FleetGroup defGroup=null;
				if(defAllianceId==0)
				{
					bground.updateHoldData(allianceID,decrlist);// ֱ��ռ��
					attackAfight.getHorn().takeBake(-1,false);// �ŽǷ�ת
					bground.awardBuff(attackAlliance,objectFactory);// ����BUFF
				}
				else
				{
					defAlliance=getAlliance(defAllianceId);
					defAfight=getAfight(defAllianceId);

					attackGroup=creatFleetGroup(attackAlliance,
						decrlist);
					defGroup=creatFleetGroup(defAlliance,
						bground.getFleet());

					FightScene scene=FightSceneFactory.factory.create(attackGroup,getHornAblity(attackAfight.horn),
						defGroup,getHornAblity(defAfight.horn));
					scene.setDefend(true);
					fightData=FightSceneFactory.factory.fight(scene,null)
						.getRecord();
					// ������ʤ��
					if(scene.getSuccessTeam()==0)
					{
						bground.updateHoldData(allianceID,attackGroup
							.getResidualShip(AllianceFight.LOSE_PERC,
								attackLoss));
						attackAfight.getHorn().takeBake(-1,false);// �ŽǷ�ת
						defAfight.getHorn().takeBake(1,false);// �ŽǷ�ת
						bground.awardBuff(attackAlliance,objectFactory);// ����������BUFF
						bground.cancelBuff(defAlliance,objectFactory);// ȡ��ʧ�ܷ�BUFF
						defAfight.addResidualShip(defGroup.getResidualShip(
							AllianceFight.LOSE_PERC,defLoss));// �黹ʧ�ܷ�����
					}
					else
					{
						eType=AllianceFightEvent.LOSE;
						bground.setFleet(defGroup.getResidualShip(
							AllianceFight.LOSE_PERC,defLoss));// ����פ������
						attackAfight
							.addResidualShip(attackGroup.getResidualShip(
								AllianceFight.LOSE_PERC,attackLoss));// �黹ʧ�ܷ�����
						if(scene.getCurrentRound()<=1)
						{
							attackAfight.getHorn().forceDecr();
							dcrhorn=AllianceFight.LOSE_PERC;
							JBackKit.sendAfightHorn(objectFactory,attackAlliance,attackAfight);
						}
					}

				}
				// ���ս��
				ByteBuffer fore=null;
				if(fightData!=null)
				{
					fore=new ByteBuffer();
					String winName=attackAlliance.getName();
					if(eType==AllianceFightEvent.LOSE)
					{
						winName=defAlliance.getName();
					}
					SeaBackKit.conAllianceFightRecord(winName,
						attackAlliance.getName(),defAlliance.getName(),
						attackLoss,dcrhorn,defLoss,fore,fightData,
						attackAlliance.getAllianceLevel(),
						defAlliance.getAllianceLevel(),
						PublicConst.FIGHT_TYPE_15,null,null,attackGroup,defGroup);
				}
				// ����ս��
				createAllianceFightEvent(eType,bground.getShowSid(),
					allianceID,defAllianceId,fore,0,0,player.getName(),dcrhorn);
				// �Զ�����
				bground.recruit(objectFactory,false);
				// ����ݵ�
				saveBGround(bground);
				// ���ӳ�ս����
				attackAfight.incrDayCount();
				// ������ս
				saveAFight(attackAfight);
				saveAFight(defAfight);

				// ˢ�¾ݵ�
				if(eType==AllianceFightEvent.WIN)
					JBackKit.sendGround(objectFactory,bground,this);
				// ˢ�´�ֻ
				JBackKit.sendAfightFleet(objectFactory,attackAlliance,
					attackAfight);
				JBackKit
					.sendAfightFleet(objectFactory,defAlliance,defAfight);
				
				if(fightData!=null)
				{
					//��־
					IntList lose=SeaBackKit.collateLostShip(attackLoss);
					if(lose.size()>0)
					{
						IntList left=attackAfight.getAllFleets();
						IntList bleft=bground.getId()==attackAfight.getAllianceID()?bground.getAllFleets():null;
						AfihgtShipData.instance().generateShipTrack(ShipRecord.ATTACK,allianceID,defAlliance.getName(),left,bleft,lose);
					}
					lose=SeaBackKit.collateLostShip(defLoss);
					if(lose.size()>0)
					{
						IntList left=defAfight.getAllFleets();
						IntList bleft=bground.getId()==defAfight.getAllianceID()?bground.getAllFleets():null;
						AfihgtShipData.instance().generateShipTrack(ShipRecord.DEFENSE,defAllianceId,attackAlliance.getName(),left,bleft,lose);
					}
					
				}
				// ��Ӧ
				data.clear();
				if(fore==null)
				{
					data.writeBoolean(false);
				}
				else
				{
					data.writeBoolean(true);

					for(int k=fore.offset();k<fore.length();k++)
					{
						data.writeByte(fore.toArray()[k]);
					}
				}
			}
		}
		return null;
	}

	/** ����ĳ���ݵ� */
	public String retreatGround(Player player,ByteBuffer data)
	{
		String a_id=player.getAttributes(PublicConst.ALLIANCE_ID);
		int allianceID=Integer.parseInt(a_id);
		Alliance attackAlliance=getAlliance(allianceID);
		AllianceFight attackAfight=getAfight(allianceID);
		if(attackAfight==null) return "no afight";
		if(!attackAlliance.isMaster(player.getId())) return "not master";
		BattleGround bground=getHoldBGround(allianceID);
		if(bground==null) return "no hold ground";
		synchronized(bground)
		{
			attackAfight.addResidualShip(bground.getFleet());// �黹����
			bground.reset();// ���þݵ�
			attackAfight.getHorn().takeBake(1,false);// �ŽǷ�ת
			bground.cancelBuff(attackAlliance,objectFactory);
		}
		// ����ս��
		createAllianceFightEvent(AllianceFightEvent.WIN,
			bground.getShowSid(),0,allianceID,null,0,0,player.getName(),0);
		// ����ݵ�
		saveBGround(bground);
		// ������ս
		saveAFight(attackAfight);

		// ˢ�¾ݵ�
		JBackKit.sendGround(objectFactory,bground,this);
		// ˢ�´�ֻ
		JBackKit.sendAfightFleet(objectFactory,attackAlliance,attackAfight);
		//ˢ�ºŽ�
		JBackKit.sendAfightHorn(objectFactory,attackAlliance,attackAfight);
		// ��Ӧ
		data.clear();
		
		return null;
	}
	/** �齨���� */
	public FleetGroup creatFleetGroup(Alliance alliance,IntList list)
	{
		AttrAdjustment adjustment=alliance.getAdjustment();
		IntList skillList=alliance.getSkillList();
		FleetGroup group=new FleetGroup();
		for(int i=0;i<list.size();i+=3)
		{
			int shipSid=list.get(i+1);
			int num=list.get(i+2);
			if(num<=0) continue;
			int location=list.get(i);

			AllianceFleet fleet=new AllianceFleet();
			fleet.initNum(num);
			fleet.setLocation(location);
			fleet.setShip((Ship)Ship.factory.newSample(shipSid));
			fleet.setAdjustment(adjustment);
			fleet.setSkillList(skillList);
			group.setFleet(location,fleet);
		}
		return group;
	}
	/** ��ȡս���ŽǼӳ� ������һ��ability�� */
	public Ability[] getHornAblity(WarHorn horn)
	{
		Ability abliAbility=(Ability)FightScene.abilityFactory
			.newSample(1000);
		((ChangeHurtEffect)abliAbility.getEffects()[0]).setHurtPrecent(horn
			.getCPercent());
		Ability[] ability=new Ability[1];
		ability[0]=abliAbility;
		return ability;
	}

	/** ������ս�¼� */
	public void createAllianceFightEvent(int type,int battleId,int attackId,
		int defId,ByteBuffer fightData,int addValue,int skillsid,String aName,int dcrHorm)
	{
		AllianceFightEvent event=(AllianceFightEvent)objectFactory
			.getaFightEventMemCache().createObect();
		event.init(type,battleId,attackId,defId,fightData,addValue,skillsid,aName,dcrHorm);
		saveFightEvent(event);
		// ���¼�������б�
		if(attackId!=0)
			getAfight(attackId).addEvent(event.getUid(),objectFactory);
		if(defId!=0)
			getAfight(defId).addEvent(event.getUid(),objectFactory);
		if(type==AllianceFightEvent.WIN) addEvent(event);

	}
	/** ������ս�¼���ȫ���� */
	public void addEvent(AllianceFightEvent event)
	{
		synchronized(allEvent)
		{
			allEvent.addAt(event,0);
			clearEvent();
		}
	}
	/** ����ս�� */
	public void clearEvent()
	{
		if(allEvent.size()>AllianceFight.EVENT_MAX)
		{
			AllianceFightEvent event=(AllianceFightEvent)allEvent.remove();
			if(event!=null)
			{
				event.decrCount();
				saveFightEvent(event);
			}
		}
		if(allEvent.size()>AllianceFight.EVENT_MAX) clearEvent();
	}

	/** ��ȡ�� */
	public Alliance getAlliance(int id)
	{
		return (Alliance)objectFactory.getAllianceMemCache().load(id+"");
	}
	/** ��ȡ��ս */
	public AllianceFight getAfight(int id)
	{
		return (AllianceFight)objectFactory.getAllianceFightMemCache().load(
			id+"");
	}
	/** ������ս */
	public void saveAFight(AllianceFight afight)
	{
		if(afight==null) return;
		objectFactory.getAllianceFightMemCache().save(
			afight.getAllianceID()+"",afight);
	}
	/** ��ȡ��սս�� */
	public AllianceFightEvent getFightEvent(int id)
	{
		return (AllianceFightEvent)objectFactory.getaFightEventMemCache()
			.load(id+"");
	}
	/** ��������ս�� */
	public void saveFightEvent(AllianceFightEvent event)
	{
		if(event==null) return;
		objectFactory.getaFightEventMemCache().save(event.getUid()+"",event);
	}
	/** ����SID ��ȡ�ݵ� */
	public BattleGround getBGround(int sid)
	{
		return (BattleGround)objectFactory.getBattleGroundMemCache().load(
			sid+"");
	}
	/** ����ݵ� */
	public void saveBGround(BattleGround bground)
	{
		if(bground==null) return;
		objectFactory.getBattleGroundMemCache().save(bground.getSid()+"",
			bground);
	}

	/** ��ȡĳҳ�������� */
	public String getRanksByPage(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		if(objs[0]==null)
		{
			((AllianceFight)objs[1]).getRanksByPage(objectFactory,data);
		}
		return (String)objs[0];
	}
	/** ��ȡĳ�˾贬��ϸ */
	public String getDiantionByName(Player player,ByteBuffer data)
	{
		String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
		String vname=data.readUTF();
		Player vplayer=objectFactory.getPlayerByName(vname,false);
		if(vplayer==null) return "no vplayer";
		if(!aid.equals(vplayer.getAttributes(PublicConst.ALLIANCE_ID)))
			return "you are not same alliance";
		AllianceFight afight=getAfight(Integer.parseInt(aid));
		if(afight==null) return "no afight";

		afight.getDiantionByName(vplayer,data);
		return null;
	}
	/** ��ȡĳҳ���׼�¼ */
	public String getRecordByPage(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		if(objs[0]==null)
		{
			((AllianceFight)objs[1]).getRecordByPage(objectFactory,data);
		}
		return (String)objs[0];

	}
	/** ��ȡս�µ�ĳҳ */
	public String getFightEvent(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		if(objs[0]==null)
		{
			((AllianceFight)objs[1]).getFightEvent(this,data);
		}
		return (String)objs[0];
	}
	/** ��ȡս�� */
	public String getFightRecord(ByteBuffer data)
	{
		int id=data.readInt();
		AllianceFightEvent event=getFightEvent(id);
		if(event==null) return "no afightevent";
		if(event.getFightData()==null) return "no fightdata";
		data.clear();
		data.writeData(event.getFightData().toArray(),event.getFightData()
			.offset(),event.getFightData().length());
		return null;

	}

	/** ��ȡȫ��ս�µ�ĳҳ */
	public String getAllEvent(ByteBuffer data)
	{
		int cpage=data.readUnsignedByte();
		int index=(cpage-1)*AllianceFight.PAGEMAX20;
		data.clear();
		synchronized(allEvent)
		{
			int allPage=allEvent.size()%AllianceFight.PAGEMAX20==0?allEvent
				.size()/AllianceFight.PAGEMAX20:allEvent.size()
				/AllianceFight.PAGEMAX20+1;
			data.writeByte(allPage==0?1:allPage);
			data.writeByte(cpage);

			if(index<0||index>=allEvent.size())
				data.writeByte(0);
			else
			{
				int len=allEvent.size()-index;
				if(len>AllianceFight.PAGEMAX20) len=AllianceFight.PAGEMAX20;
				data.writeByte(len);
				for(int i=index;i<index+len;i++)
				{
					AllianceFightEvent event=(AllianceFightEvent)allEvent
						.get(i);
					event.showBytesWrite(this,data);
				}
			}
		}
		return null;

	}
	/** ��ȡ��ս��ǰ̨�� */
	public String getAllianceFight(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		if(objs[0]==null)
		{
			data.clear();
			((AllianceFight)objs[1]).showBytesWrite(data);
		}
		return (String)objs[0];
	}
	/** ��ȡ��潢��+�ݵ���Ϣ */
	public String getStockFleet(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		if(objs[0]==null)
		{
			data.clear();
			Object[] grounds=getBGrounds();
			data.writeByte(grounds.length);
			for(int i=0;i<grounds.length;i++)
			{
				((BattleGround)((BattleGroundSave)grounds[i]).getData())
					.showBytesWrite(this,data);
			}
			AllianceFight afight=(AllianceFight)objs[1];
			data.writeByte(afight.getHorn().getCPercent());
			data.writeBoolean(afight.isReinforce());
			afight.getStockFleet(data,getHoldBGround(afight.getAllianceID()));
			afight.flushUpShip(data);
		}
		return (String)objs[0];

	}
	public void showBytesWrite(int aid,ByteBuffer data)
	{
		Object[] grounds=getBGrounds();
		data.writeByte(grounds.length);
		for(int i=0;i<grounds.length;i++)
		{
			((BattleGround)((BattleGroundSave)grounds[i]).getData())
				.showBytesWrite(this,data);
		}
		AllianceFight afight=getAfight(aid);
		data.writeByte(afight.getHorn().getCPercent());
		data.writeBoolean(afight.isReinforce());
		
		afight.getStockFleet(data,getHoldBGround(aid));
		afight.flushUpShip(data);
	}
	/** �����ս */
	public Object[] checkAFight(Player player)
	{
		Object[] objs=new Object[2];
		String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
		AllianceFight afight=getAfight(Integer.parseInt(aid));
		if(afight==null)
		{
			objs[0]="no afight";
			return objs;
		}
		objs[1]=afight;
		return objs;
	}

	/** ������ֻ */
	public String upShip(Player player,ByteBuffer data)
	{
		String a_id=player.getAttributes(PublicConst.ALLIANCE_ID);
		int allianceID=Integer.parseInt(a_id);
		Alliance attackAlliance=getAlliance(allianceID);
		AllianceFight attackAfight=getAfight(allianceID);
		if(attackAfight==null) return "no afight";

		String result=attackAfight.upShip(player,
			attackAlliance.getAllianceLevel(),data,getHoldBGround(allianceID));// ��Ҫ���㹱��ʲô��ô
		if(result==null)
		{
			saveAFight(attackAfight);
			// ˢ�´�ֻ
			JBackKit.sendAfightFleet(objectFactory,attackAlliance,
				attackAfight);
			// ˢ����������
			JBackKit.sendUpShip(objectFactory,attackAlliance,attackAfight);
		}
		return result;

	}
	/** ��������ֻCD */
	public String clearCD(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		int allianceID=Integer.parseInt(player.getAttributes(PublicConst.ALLIANCE_ID));
		if(objs[0]==null)
		{
			String result=((AllianceFight)objs[1]).clearCD(player,
				objectFactory,data.readUnsignedByte(),getHoldBGround(allianceID));
			data.clear();
			if(result==null)
			{
				saveAFight((AllianceFight)objs[1]);
				Alliance alliance=getAlliance(allianceID);
				// ˢ�´�ֻ
				JBackKit.sendAfightFleet(objectFactory,alliance,
					(AllianceFight)objs[1]);
				// ˢ����������
				JBackKit.sendUpShip(objectFactory,alliance,
					(AllianceFight)objs[1]);
			}
			return result;
		}
		return (String)objs[0];

	}
	/** ��ȡս�� */
	public String getFightData(Player player,ByteBuffer data)
	{
		int id=data.readInt();
		AllianceFightEvent event=getFightEvent(id);
		if(event==null||event.getFightData()==null)
		{
			return "no fightdata";
		}
		else
		{
			data.clear();
			byte[] bytes=event.getFightData().toArray();

			for(int k=event.getFightData().offset();k<event.getFightData()
				.length();k++)
			{
				data.writeByte(bytes[k]);
			}
			return null;
		}
	}
	/** ��ȡ�ݵ㲿�� */
	public String getGroundFleet(Player player,ByteBuffer data)
	{
		Object[] result=checkAFight(player);
		if(result[0]==null)
		{
			Object[] objs=getBGrounds();
			BattleGround ground=null;
			int showSid=data.readUnsignedShort();
			for(int i=0;i<objs.length;i++)
			{
				ground=(BattleGround)((BattleGroundSave)objs[i]).getData();
				if(showSid==ground.getShowSid()) break;
			}
			if(ground==null) return "no battle ground";
			data.clear();
			if(ground.getId()==0)
			{
				data.writeByte(0);
			}
			else
			{
				data.writeByte(getAfight(ground.getId()).getHorn()
					.getCPercent());// �Ž�ֵ
			}
			ground.showBytesWriteFleet(data);
			data.writeBoolean(((AllianceFight)result[1]).isReinforce());
		}
		return (String)result[0];
	}
	/** ͨ���ݵ����Sid ��ȡʵ��Sid */
	public int getBttaleSid(int showSid)
	{
		Object[] objs=getBGrounds();
		BattleGround ground=null;
		for(int i=0;i<objs.length;i++)
		{
			ground=(BattleGround)((BattleGroundSave)objs[i]).getData();
			if(showSid==ground.getShowSid()) return ground.getSid();
		}
		return 0;
	}
	/** ��ȡ�������� */
	public String getUpShip(Player player,ByteBuffer data)
	{
		Object[] objs=checkAFight(player);
		if(objs[0]==null)
		{
			AllianceFight afight=(AllianceFight)objs[1];
			if(afight.getUpship()[1]==0) return "no upship";
			int type=data.readUnsignedByte();
			data.clear();
			afight.getUpShipData(data,type);
		}
		return (String)objs[0];
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	// ==========================�������================================================================
	public void creatAlliance(int id)
	{
		createAllianceFight(id,objectFactory);
	}
	public void jionAlliance(Player player,int id)
	{
		BattleGround ground=getHoldBGround(id);
		if(ground!=null)
		{
			ground.awardPersonBuff(player);
		}
	}
	public void exitAlliance(Player player,int id)
	{
		BattleGround ground=getHoldBGround(id);
		if(ground!=null)
		{
			ground.cancelPersonBuff(player);
		}
		AllianceFight afight=getAfight(id);
		afight.exitRmDinate(player.getId());
		saveAFight(afight);
	}
	public void dismissAlliance(Alliance alliance)
	{
		BattleGround ground=getHoldBGround(alliance.getId());
		if(ground==null) return;
		synchronized(ground)
		{
			ground.reset();// ���þݵ�
			ground.cancelBuff(alliance,objectFactory);
		}
		createAllianceFightEvent(AllianceFightEvent.WIN,ground.getShowSid(),
			0,alliance.getId(),null,0,0,objectFactory.getPlayerById(alliance.getMasterPlayerId()).getName(),0);
		// ����ݵ�
		saveBGround(ground);
		// ������ս
		AllianceFight afight=getAfight(alliance.getId());
		afight.dismiss(objectFactory);
		afight.decrCount();
		saveAFight(afight);

	}
	// =========================��ʱ����=================================
	// ��ʱ����
	@Override
	public void onTimer(TimerEvent e)
	{
		if("award".equals(e.getParameter()))
		{
			Object[] objs=getBGrounds();
			int[] sids=null;
			for(int i=0;i<objs.length;i++)
			{
				BattleGround ground=(BattleGround)((BattleGroundSave)objs[i])
					.getData();
				sids=ground.awardUpPoint(this,objectFactory);
				if(sids!=null)
				{
					for(int k=0;k<sids.length;k++)
					{
						if(sids[k]!=0)
						{
							createAllianceFightEvent(
								AllianceFightEvent.UPPOINT,
								ground.getShowSid(),ground.getId(),0,null,
								ground.getUpPoint(),sids[k]==-1?0:sids[k],
								"",0);
						}
						else
						{
							break;
						}
					}
				}
			}
		}
		else if("horn".equals(e.getParameter()))
		{
			Object[] objs=objectFactory.getAllianceMemCache().getCacheMap()
				.valueArray();
			Alliance alliance=null;
			AllianceFight afight=null;
			for(int i=objs.length-1;i>=0;i--)
			{
				if(objs[i]!=null)
				{
					alliance=((AllianceSave)objs[i]).getData();
					afight=getAfight(alliance.getId());
					JBackKit.sendAfightHorn(objectFactory,alliance,afight);
				}
			}
		}

	}

}
