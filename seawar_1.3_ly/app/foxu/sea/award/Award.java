package foxu.sea.award;

import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Service;
import foxu.sea.Ship;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.OfficerManager;
import foxu.sea.officer.OfficerTrack;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.shipdata.ShipCheckData;

/**
 * ����Ʒ�� author:icetiger
 */
public class Award extends Sample
{

	/** �������ͳ��� */
	public static final int METAL_AWARD_TYPE=1,OIL_AWARD_TYPE=2,
					SILIECON_AWARD_TYPE=3,URANIUM_AWARD_TYPE=4,
					MONEY_AWARD_TYPE=5,EXP_AWARD_TYPE=6,GEMS_AWARD_TYPE=7,
					PROP_SID_TYPE=8,SHIP_TYPE=9,HONOR_AWARD_TYPE=10,
					SERVICE_SID_TYPE=11,RANDOM_PROPS_TYPE=12,ENERGY=13,VP_POINT=14,
					VITALITY_TYPE=15,EQUIP_TYPE=16,OFFICER_TYPE=17,OFFICER_FEATS_TYPE=18,
					INTEGRAL_TYPE=19,LEAGUE_GOAL_TYPE=20,LEAGUE_COIN_TYPE=21;

	/** ���ʷ�Χ */
	public static final int PROB_ABILITY=10000;
	/** ��sid�� */
	public static final int SHIP_START_SID=10001;
	/** װ��sid�� */
	public static final int EQUIP_START_SID=40000;
	/** װ�����ײ���sid�� */
	public static final int EQUIP_STUFF_START_SID=41200;
	/** ����(��Ƭ)sid�� */
	public static final int OFFICER_START_SID=60000;
	/** ���� */
	int metalAward;
	/** ʯ�� */
	int oilAward;
	/** �� */
	int siliconAward;
	/** ���� */
	int uraniumAward;
	/** ��Ǯ */
	int moneyAward;
	/** ��ʯ ���� */
	int gemsAward;
	/** ��Ʒsid */
	int propSid[];
	/** ����sid */
	int shipSids[];
	/** װ��sid */
	int equipSids[];
	/** ����(��Ƭ)sid */
	int officerSids[];
	/** ��������򱶷�ʽ3λһ�� sid,num,Probability=���� */
	int randomProps[];
	/** ��������򱶷�ʽ3λһ�� sid,num,Probabilities=����  �������� */
	int randomProps2[];
	/** ���齱�� */
	int experienceAward;
	/** �������� */
	int honorAward;
	/** ����sid */
	int serviceSid;
	/** vip�ɳ� **/
	int vppoint;
	/** ���� */
	int energy;
	/** ��Ծֵ */
	int vitality;
	/**����**/
	int  integral;
	/** �������� */
	int officerFeats;
	/** ������� */
	int leagueGoal;
	/** ������������� */
	int leagueCoin;

	/** ���� */
	int[] metalAwards;
	/** ʯ�� */
	int[] oilAwards;
	/** �� */
	int[] siliconAwards;
	/** ���� */
	int[] uraniumAwards;
	/** ��Ǯ */
	int[] moneyAwards;
	/** ��ʯ ���� */
	int[] gemsAwards;
	/** ���齱�� */
	int[] experienceAwards;
	/** �������� */
	int[] honorAwards;
	/** ����sid */
	int[] serviceSids;
	/** vip�ɳ� **/
	int[] vppoints;
	/** ���� */
	int[] energys;
	/** �������� */
	int[] awardTypes;


	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/** ���ֽ������з����л���ö������ */
	public static Award bytesReadAwrad(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Award r=(Award)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Award.class.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}
	
	/** ����Ʒ�ĳ���
	 *	trackReasonsΪ��־����(ԭ��)ר��{װ����־,ս���仯,...,...} 
	 *  blockFlush �������� ��������ʹ���ҷ�����ˢ�µ� һ���Ը���
	 */
	public int awardLenth(ByteBuffer data,Player player,
		CreatObjectFactory objectFactory,String message,int[] trackReasons,boolean blockFlush)
	{
		return awardLenth(data,player,objectFactory,message,new int[]{2022},trackReasons,blockFlush);
	}
	
	/** ����Ʒ�ĳ���
	 *	trackReasonsΪ��־����(ԭ��)ר��{װ����־,ս���仯,...,...} 
	 */
	public int awardLenth(ByteBuffer data,Player player,
		CreatObjectFactory objectFactory,String message,int[] trackReasons)
	{
		return awardLenth(data,player,objectFactory,message,new int[]{2022},trackReasons);
	}

	/** �鿴��Ӧ���� */
	public int viewAward(ByteBuffer data,Player player)
	{
		// �ж����ս���
		Award award=assembleActivityAward(player);
		if(!award.equals(this))
			return award.viewAward(data,player);
		int i=0;
		int level=1;
		if(player!=null) level=player.getLevel();
		if(getMetalAward(level)>0) i++;
		if(getOilAward(level)>0) i++;
		if(getSiliconAward(level)>0) i++;
		if(getUraniumAward(level)>0) i++;
		if(getMoneyAward(level)>0) i++;
		if(getExperienceAward(level)>0) i++;
		if(getGemsAward(level)>0) i++;
		if(getHonorAward(level)>0) i++;
		if(getServiceSid(level)!=0) i++;
		if(getVppoint(level)>0) i++;
		if(getEnergy(level)>0)i++;
		if(integral>0) i++;
		if(leagueGoal>0) i++;
		if(leagueCoin>0) i++;
		if(vitality>0)i++;
		if(officerFeats>0)i++;
//		if(randomProps!=null) i++;
//		if(randomProps2!=null) i++;
		if(propSid!=null&&propSid.length>0)
		{
			i+=propSid.length/2;
		}
		if(equipSids!=null&&equipSids.length>0)
		{
			i+=equipSids.length/2;
		}
		if(officerSids!=null&&officerSids.length>0)
		{
			i+=officerSids.length/2;
		}
		if(shipSids!=null&&shipSids.length>0)
		{
			i+=shipSids.length/2;
		}
		// ��Ʒ���ﲻд
		data.writeByte(i);
		if(getMetalAward(level)>0)
		{
			data.writeByte(METAL_AWARD_TYPE);
			data.writeInt(getMetalAward(level));
		}
		if(getOilAward(level)>0)
		{
			data.writeByte(OIL_AWARD_TYPE);
			data.writeInt(getOilAward(level));
		}
		if(getSiliconAward(level)>0)
		{
			data.writeByte(SILIECON_AWARD_TYPE);
			data.writeInt(getSiliconAward(level));
		}
		if(getUraniumAward(level)>0)
		{
			data.writeByte(URANIUM_AWARD_TYPE);
			data.writeInt(getUraniumAward(level));
		}
		if(getMoneyAward(level)>0)
		{
			data.writeByte(MONEY_AWARD_TYPE);
			int moneyAward=getMoneyAward(level);
			if(player!=null)
			{
				// ��Ҽӳ�BUFF
				AdjustmentData buff=((AdjustmentData)player.getAdjstment()
					.getAdjustmentValue(PublicConst.MONEY_BUFF));
				if(buff!=null)
				{
					moneyAward=moneyAward*(100+buff.percent)/100;
				}
			}
			data.writeInt(moneyAward);
		}
		if(getExperienceAward(level)>0)
		{
			data.writeByte(EXP_AWARD_TYPE);
			int exp=getExperienceAward(level);
			if(player!=null)
			{
				// ����ӳ�BUFF
				AdjustmentData buff=((AdjustmentData)player.getAdjstment()
					.getAdjustmentValue(PublicConst.EXP_ADD));
				if(buff!=null)
				{
					exp=exp*(100+buff.percent)/100;
				}
			}
			data.writeInt(exp);
		}
		if(getGemsAward(level)>0)
		{
			data.writeByte(GEMS_AWARD_TYPE);
			data.writeInt(getGemsAward(level));
		}
		if(getHonorAward(level)>0)
		{
			data.writeByte(HONOR_AWARD_TYPE);
			data.writeInt(getHonorAward(level));
		}
		if(getServiceSid(level)>0)
		{
			data.writeByte(SERVICE_SID_TYPE);
			data.writeInt(getServiceSid(level));
		}
		if(getVppoint(level)>0)
		{
			data.writeByte(VP_POINT);
			data.writeInt(getVppoint(level));
		}
		if(getEnergy(level)>0)
		{
			data.writeByte(ENERGY);
			data.writeInt(getEnergy(level));
		}
		if(vitality>0)
		{
			data.writeByte(VITALITY_TYPE);
			data.writeInt(vitality);
		}
		//��һ���
		if(integral>0)
		{
			data.writeByte(	INTEGRAL_TYPE);
			data.writeInt(integral);
		}
		// ���������
		if(leagueGoal>0)
		{
			data.writeByte(LEAGUE_GOAL_TYPE);
			data.writeInt(leagueGoal);
		}
		// �������������
		if(leagueCoin>0)
		{
			data.writeByte(LEAGUE_COIN_TYPE);
			data.writeInt(leagueCoin);
		}
		if(officerFeats>0)
		{
			data.writeByte(OFFICER_FEATS_TYPE);
			data.writeInt(officerFeats);
		}
		// ������Ʒ
		if(propSid!=null&&propSid.length>0)
		{
			for(int j=0;j<propSid.length;j+=2)
			{
				Prop prop=(Prop)Prop.factory.newSample(propSid[j]);
				data.writeByte(PROP_SID_TYPE);
				data.writeInt(propSid[j+1]);
				data.writeShort(prop.getSid());
			}
		}
		// ���Ӵ���
		if(shipSids!=null&&shipSids.length>0)
		{
			for(int j=0;j<shipSids.length;j+=2)
			{
				data.writeByte(SHIP_TYPE);
				data.writeInt(shipSids[j+1]);
				data.writeShort(shipSids[j]);
			}
		}
		// ����װ��
		if(equipSids!=null&&equipSids.length>0)
		{
			for(int k=0;k<equipSids.length;k+=2)
			{
				data.writeByte(EQUIP_TYPE);
				data.writeInt(equipSids[k+1]);
				data.writeShort(equipSids[k]);
			}
		}
		// ���Ӿ���
		if(officerSids!=null&&officerSids.length>0)
		{
			for(int k=0;k<officerSids.length;k+=2)
			{
				data.writeByte(OFFICER_TYPE);
				data.writeInt(officerSids[k+1]);
				data.writeShort(officerSids[k]);
			}
		}
//		// �������    //�����ε�������  ��Ҫ��д
//		if(randomProps!=null&&randomProps.length>0)
//		{
//			data.writeByte(RANDOM_PROPS_TYPE);
//			data.writeInt(0);
//			data.writeShort(0);
//		}
		return i;
	}	
	
	public int awardLenth(ByteBuffer data,Player player,
		CreatObjectFactory objectFactory,String message,int[] noticeSids,
		int[] trackReasons,boolean blockFlush)
	{
		// �ж����ս���
		Award award=assembleActivityAward(player);
		if(!award.equals(this))
			return award.awardLenth(data,player,objectFactory,message,
				noticeSids,trackReasons,blockFlush);
		int i=0;
		int level=1;
		if(player!=null) level=player.getLevel();
		if(getMetalAward(level)>0) i++;
		if(getOilAward(level)>0) i++;
		if(getSiliconAward(level)>0) i++;
		if(getUraniumAward(level)>0) i++;
		if(getMoneyAward(level)>0) i++;
		if(getExperienceAward(level)>0) i++;
		if(getGemsAward(level)>0) i++;
		if(getHonorAward(level)>0) i++;
		if(getServiceSid(level)!=0) i++;
		if(getVppoint(level)>0) i++;
		if(getEnergy(level)>0) i++;
		if(vitality>0) i++;
		if(leagueGoal>0) i++;
		if(leagueCoin>0) i++;
		if(officerFeats>0) i++;
		if(randomProps!=null) i++;
		if(randomProps2!=null) i++;
		if(propSid!=null&&propSid.length>0)
		{
			i+=propSid.length/2;
		}
		if(shipSids!=null&&shipSids.length>0)
		{
			i+=shipSids.length/2;
		}
		if(equipSids!=null&&equipSids.length>0)
		{
			i+=equipSids.length/2;
		}
		if(officerSids!=null&&officerSids.length>0)
		{
			i+=officerSids.length/2;
		}
		// ��Ʒ���ﲻд
		data.writeByte(i);
		// System.out.println("------iiii---------::"+i);
		if(getMetalAward(level)>0)
		{
			data.writeByte(METAL_AWARD_TYPE);
			data.writeInt(getMetalAward(level));
		}
		if(getOilAward(level)>0)
		{
			data.writeByte(OIL_AWARD_TYPE);
			data.writeInt(getOilAward(level));
		}
		if(getSiliconAward(level)>0)
		{
			data.writeByte(SILIECON_AWARD_TYPE);
			data.writeInt(getSiliconAward(level));
		}
		if(getUraniumAward(level)>0)
		{
			data.writeByte(URANIUM_AWARD_TYPE);
			data.writeInt(getUraniumAward(level));
		}
		if(getMoneyAward(level)>0)
		{
			data.writeByte(MONEY_AWARD_TYPE);
			// ��Ҽӳ�BUFF
			AdjustmentData buff=((AdjustmentData)player.getAdjstment()
				.getAdjustmentValue(PublicConst.MONEY_BUFF));
			int moneyAward=getMoneyAward(level);
			if(buff!=null)
			{
				moneyAward=moneyAward*(100+buff.percent)/100;
			}
			data.writeInt(moneyAward);
		}
		if(getExperienceAward(level)>0)
		{
			data.writeByte(EXP_AWARD_TYPE);
			// ����ӳ�BUFF
			AdjustmentData buff=((AdjustmentData)player.getAdjstment()
				.getAdjustmentValue(PublicConst.EXP_ADD));
			int exp=getExperienceAward(level);
			if(buff!=null)
			{
				exp=exp*(100+buff.percent)/100;
			}
			data.writeInt(exp);
		}
		if(getGemsAward(level)>0)
		{
			data.writeByte(GEMS_AWARD_TYPE);
			data.writeInt(getGemsAward(level));
		}
		if(getHonorAward(level)>0)
		{
			data.writeByte(HONOR_AWARD_TYPE);
			data.writeInt(getHonorAward(level));
		}
		if(getServiceSid(level)>0)
		{
			data.writeByte(SERVICE_SID_TYPE);
			data.writeInt(getServiceSid(level));
		}
		if(getVppoint(level)>0)
		{
			data.writeByte(VP_POINT);
			data.writeInt(getVppoint(level));
		}
		if(getEnergy(level)>0)
		{
			data.writeByte(ENERGY);
			data.writeInt(getEnergy(level));
		}
		if(vitality>0)
		{
			data.writeByte(VITALITY_TYPE);
			data.writeInt(vitality);
		}
		// ��һ���
		if(integral>0)
		{
			data.writeByte(INTEGRAL_TYPE);
			data.writeInt(integral);
		}
		// ���������
		if(leagueGoal>0)
		{
			data.writeByte(LEAGUE_GOAL_TYPE);
			data.writeInt(leagueGoal);
		}
		// �������������
		if(leagueCoin>0)
		{
			data.writeByte(LEAGUE_COIN_TYPE);
			data.writeInt(leagueCoin);
		}
		if(officerFeats>0)
		{
			data.writeByte(OFFICER_FEATS_TYPE);
			data.writeInt(officerFeats);
		}
		awardSelf(player,TimeKit.getSecondTime(),data,objectFactory,message,
			noticeSids,trackReasons,blockFlush);
		return i;
	}
	

	/** ����Ʒ�ĳ���
	 *	trackReasonsΪ��־����(ԭ��)ר��{װ����־,ս���仯,...,...}
	 */
	public int awardLenth(ByteBuffer data,Player player,
		CreatObjectFactory objectFactory,String message,int[] noticeSids,int[] trackReasons)
	{
		return awardLenth(data,player,objectFactory,message,noticeSids,trackReasons,false);
	}
	/**
	 * ���ͽ��� ���ػ�õ������Ʒ ��Ϊû�� ǰ̨Ҫ���ȶ����� �ٶ���Ʒsid ���ؽ���Ʒ��Ϣ
	 * trackReasonsΪ��־����(ԭ��)ר��{װ����־,ս���仯,...,...}
	 */
	public String awardSelf(Player player,int checkTime,ByteBuffer data,
		CreatObjectFactory objectFactory,String message,int[] trackReasons)
	{
		// �ж����ս���
		Award award=assembleActivityAward(player);
		if(!award.equals(this))
			return award.awardSelf(player,checkTime,data,objectFactory,
				message,new int[]{2022},trackReasons);
		// Ĭ�ϻ��ͳ������ʾ
		return awardSelf(player,checkTime,data,objectFactory,message,
			new int[]{2022},trackReasons);
	}
	
	public String awardSelf(Player player,int checkTime,ByteBuffer data,
		CreatObjectFactory objectFactory,String message,int[] noticeSids,int[] trackReasons,boolean blockFlush)
	{
		if(data==null) data=new ByteBuffer();
		int level=1;
		if(player!=null) level=player.getLevel();
		// ������Դ
		if(getMetalAward(level)>0||getOilAward(level)>0
			||getSiliconAward(level)>0||getUraniumAward(level)>0
			||getMoneyAward(level)>0)
		{
			// ��Ҽӳ�BUFF
			AdjustmentData buff=((AdjustmentData)player.getAdjstment()
				.getAdjustmentValue(PublicConst.MONEY_BUFF));
			int moneyAward=getMoneyAward(level);
			if(buff!=null)
			{
				moneyAward=moneyAward*(100+buff.percent)/100;
			}
			Resources.addResources(player.getResources(),
				getMetalAward(level),getOilAward(level),
				getSiliconAward(level),getUraniumAward(level),moneyAward,
				player);
		}
		if(getExperienceAward(level)>0)
		{
			// ����ӳ�BUFF
			AdjustmentData buff=((AdjustmentData)player.getAdjstment()
				.getAdjustmentValue(PublicConst.EXP_ADD));
			int exp=getExperienceAward(level);
			if(buff!=null)
			{
				// ��������ľ��鲻���ܼӳ�
				if(trackReasons==null||trackReasons[0]!=EquipmentTrack.FROM_TASK)
					exp=exp*(100+buff.percent)/100;
			}
			player.incrExp(exp,objectFactory);
			JBackKit.sendExp(player);
		}
		// ���ӱ�ʯ
		if(getGemsAward(level)!=0)
		{
			Resources.addGemsNomal(getGemsAward(level),
				player.getResources(),player);
			if(objectFactory!=null)
			{
				try
				{
					// ��ʯ���Ѽ�¼
					objectFactory.createGemTrack(GemsTrack.GEMS_AWARD,
						player.getId(),getGemsAward(level),getSid(),
						Resources.getGems(player.getResources()));
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
		}
		boolean resetBundle=false;
		// ������Ʒ
		if(propSid!=null&&propSid.length>0)
		{
			for(int i=0;i<propSid.length;i+=2)
			{
				Prop prop=(Prop)Prop.factory.newSample(propSid[i]);
				if(prop==null)continue;
				data.writeByte(PROP_SID_TYPE);
				data.writeInt(propSid[i+1]);
				data.writeShort(prop.getSid());
				if(prop!=null&&prop instanceof NormalProp)
				{
					((NormalProp)prop).setCount(propSid[i+1]);
					player.getBundle().incrProp(prop,true);
					resetBundle=true;
//					JBackKit.sendResetBunld(player);
				}
			}
		}
		boolean resetShip=false;
		// ���Ӵ���
		if(shipSids!=null&&shipSids.length>0)
		{
			// ��ֻ��־
			IntList fightlist=new IntList();
			for(int i=0;i<shipSids.length;i+=2)
			{
				data.writeByte(SHIP_TYPE);
				Ship ship=(Ship)Ship.factory.getSample(shipSids[i]);
				data.writeInt(shipSids[i+1]);
				data.writeShort(shipSids[i]);
				if(ship!=null)
				{
					player.getIsland().addTroop(shipSids[i],shipSids[i+1],
						player.getIsland().getTroops());
					fightlist.add(shipSids[i]);
					fightlist.add(shipSids[i+1]);
					resetShip=true;
				}
			}
			if(objectFactory!=null&&fightlist.size()>0)
			{
				objectFactory.addShipTrack(
					0,ShipCheckData.RANDOM_AWARD,player,
					fightlist,null,false);
				int reason=FightScoreConst.SOMETHING_ELSE;
				if(trackReasons!=null&&trackReasons.length>1)
					reason=trackReasons[1];
				JBackKit.sendFightScore(player,objectFactory,true,reason);
			}
//			if(!player.getIsland().autoAddMainGroup())
//				JBackKit.sendResetTroops(player);
		}
		// ����װ��
		if(equipSids!=null&&equipSids.length>0)
		{
			for(int i=0;i<equipSids.length;i+=2)
			{
				data.writeByte(EQUIP_TYPE);
				data.writeInt(equipSids[i+1]);
				data.writeShort(equipSids[i]);
				player.getEquips().addEquipOrStuff(equipSids[i],equipSids[i+1]);
				// װ����־
				if(objectFactory!=null&&trackReasons!=null
					&&trackReasons.length>0)
				{
					SeaBackKit.createEquipTrackByAutoLeft(equipSids[i],
						equipSids[i+1],trackReasons[0],EquipmentTrack.ADD,
						getSid(),player,objectFactory);
				}
			}
			JBackKit.sendEquipInfo(player);
		}
		// ���Ӿ���
		if(officerSids!=null&&officerSids.length>0)
		{
			for(int i=0;i<officerSids.length;i+=2)
			{
				data.writeByte(OFFICER_TYPE);
				data.writeInt(officerSids[i+1]);
				data.writeShort(officerSids[i]);
				OfficerManager.getInstance().addOfficerOrFragment(player,
					officerSids[i],officerSids[i+1]);
				// ������־
				if(objectFactory!=null&&trackReasons!=null
					&&trackReasons.length>0)
				{
					// ����Ʒ����Ϊͨ��,��ȡ��������Ʒ����ʱ,����װ����־ԭ��
					objectFactory.createOfficerTrack(OfficerTrack.ADD,
						trackReasons[0],player.getId(),officerSids[i],
						officerSids[i+1],getSid(),OfficerManager.getInstance().getOfficerOrFragmentCount(player,officerSids[i]));
				}
			}
			JBackKit.sendOfficerInfo(player);
		}
		if(getHonorAward(level)>0)
		{
			player.incrHonorExp(getHonorAward(level));
			JBackKit.resetHonor(player);
		}
		if(getServiceSid(level)>0)
		{
			Service service=(Service)Service.factory
				.newSample(getServiceSid(level));
			if(service!=null)
			{
				player.addService(service,checkTime);
				JBackKit.sendResetService(player);
			}
		}
		// vip�ɳ�
		if(getVppoint(level)>0)
		{
			player.addGrowthPoint(getVppoint(level));
			JBackKit.sendResetResources(player);
		}
		//����
		if(getEnergy(level)>0)
		{
			player.addEnergy(getEnergy(level));
			JBackKit.sendResetActives((Session)player.getSource(),player);
		}
		if(vitality>0){
			player.setVitality(vitality);
		}
		//��һ���
		if(integral>0)
		{
			player.addIntegral(integral);
			//ˢ��ǰ̨
			JBackKit.sendPlayerIntegral(player);
		}
		// ���������
		if(leagueGoal>0)
		{
			if(objectFactory!=null)
				objectFactory.getClientLeagueManager().addLeagueGoal(player,
					leagueGoal,checkTime);
		}
		// �������������
		if(leagueCoin>0)
		{
			if(objectFactory!=null)
				objectFactory.getClientLeagueManager().addLeagueCoin(player,
					leagueCoin);
		}
		if(officerFeats>0){
			player.getOfficers().incrFeats(officerFeats);
			if(!blockFlush)JBackKit.sendOfficerInfo(player);
		}
		// �������
		if(randomProps!=null&&randomProps.length>0)
		{
				boolean[] flshShipEqu=randomCreateAward(data,player,
				objectFactory,true,message,noticeSids,randomProps,trackReasons);
				if(flshShipEqu[0]) resetShip=true;
				if(flshShipEqu[1])resetBundle=true;
		}
		if(randomProps2!=null&&randomProps2.length>0)
		{
			boolean[] flshShipEqu=randomCreateAward(data,player,
				objectFactory,false,message,noticeSids,randomProps2,trackReasons);
			if(flshShipEqu[0]) resetShip=true;
			if(flshShipEqu[1])resetBundle=true;
		}
		// System.out.println("---------award-----data-------::"+data.length());
		if(resetBundle)JBackKit.sendResetBunld(player);
		if(resetShip)
		{
			player.autoAddMainGroup();
			JBackKit.sendResetTroops(player);
		} 	
		return "";
	}
	
	
	/**
	 * ���ͽ��� ���ػ�õ������Ʒ ��Ϊû�� ǰ̨Ҫ���ȶ����� �ٶ���Ʒsid ���ؽ���Ʒ��Ϣ
	 * trackReasonsΪ��־����(ԭ��)ר��{װ����־,ս���仯,...,...}
	 */
	public String awardSelf(Player player,int checkTime,ByteBuffer data,
		CreatObjectFactory objectFactory,String message,int[] noticeSids,
		int[] trackReasons)
	{
		return awardSelf(player,checkTime,data,objectFactory,message,
			noticeSids,trackReasons,false);
	}
	
	/**
	 * �����Ʒ���� type ���� ������Ϣ Probability ����
	 **/
	public boolean[] randomCreateAward(ByteBuffer data,Player player,
		CreatObjectFactory objectFactory,boolean type,String message,
		int[] noticeSids,int[] randomProps,int[] trackReasons)
	{
		boolean[] flshShipEqu=new boolean[]{false,false};
		// �����
		int random=MathKit.randomValue(0,PROB_ABILITY);
		data.writeByte(RANDOM_PROPS_TYPE);
		// ��������������ʵ���Ʒ
		if(random>randomProps[randomProps.length-1])
		{
			data.writeInt(0);
		}
		else
		{
			// ��ʼ�Ļ���
			int startProbability=0;
			for(int i=0;i<randomProps.length;i+=3)
			{
				// ���������������Χ��
				if(random>=startProbability&&random<=randomProps[i+2])
				{
					data.writeInt(randomProps[i+1]);
					// ����Ǿ���ϵͳ���
					if(randomProps[i]>=OFFICER_START_SID)
					{
						data.writeShort(randomProps[i]);
						OfficerManager.getInstance().addOfficerOrFragment(
							player,randomProps[i],randomProps[i+1]);
						JBackKit.sendOfficerInfo(player);
						if(type)
							sendSystemAwardMessage(randomProps[i],
								randomProps[i+1],objectFactory,message,
								noticeSids);
						// װ����־
						if(objectFactory!=null&&trackReasons!=null
							&&trackReasons.length>0)
						{
							// ����Ʒ����Ϊͨ��,��ȡ��������Ʒ����ʱ,����װ����־ԭ��
							objectFactory.createOfficerTrack(OfficerTrack.ADD,
								trackReasons[0],player.getId(),randomProps[i],
								randomProps[i+1],getSid(),OfficerManager.getInstance().getOfficerOrFragmentCount(player,randomProps[i]));
						}
						break;
					}
					// �����װ�����߽��ײ���
					if(randomProps[i]>=EQUIP_START_SID)
					{
						data.writeShort(randomProps[i]);
						player.getEquips().addEquipOrStuff(randomProps[i],
							randomProps[i+1]);
						JBackKit.sendEquipInfo(player);
						if(type)
							sendSystemAwardMessage(randomProps[i],
								randomProps[i+1],objectFactory,message,
								noticeSids);
						// װ����־
						if(objectFactory!=null&&trackReasons!=null
							&&trackReasons.length>0)
						{
							SeaBackKit.createEquipTrackByAutoLeft(randomProps[i],
								randomProps[i+1],trackReasons[0],EquipmentTrack.ADD,
								getSid(),player,objectFactory);
						}
						break;
					}
					// �����ж�����Ǵ�,SID�жϡ�����
					if(randomProps[i]>=SHIP_START_SID)
					{
						Ship ship=(Ship)Ship.factory
							.getSample(randomProps[i]);
						data.writeShort(randomProps[i]);
						if(ship!=null)
						{
							flshShipEqu[0]=true;
							player.getIsland().addTroop(randomProps[i],
								randomProps[i+1],
								player.getIsland().getTroops());
							if(objectFactory!=null)
							{
								// ��ֻ��־
								IntList fightlist=new IntList();
								fightlist.add(randomProps[i]);
								fightlist.add(randomProps[i+1]);
								objectFactory.addShipTrack(
									0,ShipCheckData.RANDOM_AWARD,player,
									fightlist,null,false);
								int reason=FightScoreConst.SOMETHING_ELSE;
								if(trackReasons!=null&&trackReasons.length>1)
									reason=trackReasons[1];
								JBackKit.sendFightScore(player,objectFactory,true,reason);
							}
						}
						if(type)
							sendSystemAwardMessage(randomProps[i],
								randomProps[i+1],objectFactory,message,
								noticeSids);
						break;
					}
					NormalProp prop=(NormalProp)Prop.factory
						.newSample(randomProps[i]);
					data.writeShort(prop.getSid());
					prop.setCount(randomProps[i+1]);
					player.getBundle().incrProp(prop,true);
					flshShipEqu[1]=true;
					if(type)
						sendSystemAwardMessage(randomProps[i],
							randomProps[i+1],objectFactory,message,
							noticeSids);
					break;
				}
				startProbability=randomProps[i+2];
			}
		}
		return flshShipEqu;
	}
	
	private void sendSystemAwardMessage(int sid,int count,
		CreatObjectFactory objectFactory,String message,int[] noticeSids)
	{
		if(sid<=0||objectFactory==null||noticeSids==null||message==null
			||message.length()==0) return;
		for(int i=0;i<noticeSids.length;i++)
		{
			if(sid==noticeSids[i])
			{
				if(TextKit.split(message,"%").length>2)
				{
					message=TextKit.replace(message,"%",count+"");
				}
				message=TextKit
					.replace(
						message,
						"%",
						InterTransltor.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,
							"prop_sid_"+noticeSids[i]));
				SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
					message);
			}
		}
	}

	public void setExperienceAward(int experienceAward)
	{
		this.experienceAward=experienceAward;
	}

	public void setGemsAward(int gemsAward)
	{
		this.gemsAward=gemsAward;
	}

	public int[] getRandomProps()
	{
		return randomProps;
	}

	public void setRandomProps(int[] randomProps)
	{
		this.randomProps=randomProps;
	}
	
	public int[] getrandomProps2()
	{
		return randomProps2;
	}

	
	public void setrandomProps2(int[] randomProps2)
	{
		this.randomProps2=randomProps2;
	}

	public int[] getPropSid()
	{
		return propSid;
	}

	public void setPropSid(int[] propSid)
	{
		this.propSid=propSid;
	}

	public int getMetalAward(int level)
	{
		if(metalAward>0) return metalAward;
		if(metalAwards==null) return 0;
		return (level-1)>=metalAwards.length
			?metalAwards[metalAwards.length-1]:metalAwards[level-1];
	}

	public int getOilAward(int level)
	{
		if(oilAward>0) return oilAward;
		if(oilAwards==null) return 0;
		return (level-1)>=oilAwards.length?oilAwards[oilAwards.length-1]
			:oilAwards[level-1];
	}

	public int getSiliconAward(int level)
	{
		if(siliconAward>0) return siliconAward;
		if(siliconAwards==null) return 0;
		return (level-1)>=siliconAwards.length
			?siliconAwards[siliconAwards.length-1]:siliconAwards[level-1];
	}

	public int getUraniumAward(int level)
	{
		if(uraniumAward>0) return uraniumAward;
		if(uraniumAwards==null) return 0;
		return (level-1)>=uraniumAwards.length
			?uraniumAwards[uraniumAwards.length-1]:uraniumAwards[level-1];
	}

	public int getMoneyAward(int level)
	{
		if(moneyAward>0) return moneyAward;
		if(moneyAwards==null) return 0;
		return (level-1)>=moneyAwards.length
			?moneyAwards[moneyAwards.length-1]:moneyAwards[level-1];
	}

	public int getServiceSid(int level)
	{
		if(serviceSid>0) return serviceSid;
		if(serviceSids==null) return 0;
		return (level-1)>=serviceSids.length
			?serviceSids[serviceSids.length-1]:serviceSids[level-1];
	}

	public int getVppoint(int level)
	{
		if(vppoint>0) return vppoint;
		if(vppoints==null) return 0;
		return (level-1)>=vppoints.length?vppoints[vppoints.length-1]
			:vppoints[level-1];
	}

	public int getGemsAward(int level)
	{
		if(gemsAward>0) return gemsAward;
		if(gemsAwards==null) return 0;
		return (level-1)>=gemsAwards.length?gemsAwards[gemsAwards.length-1]
			:gemsAwards[level-1];
	}

	public int getExperienceAward(int level)
	{
		if(experienceAward>0) return experienceAward;
		if(experienceAwards==null) return 0;
		return (level-1)>=experienceAwards.length
			?experienceAwards[experienceAwards.length-1]
			:experienceAwards[level-1];
	}

	public int getHonorAward(int level)
	{
		if(honorAward>0) return honorAward;
		if(honorAwards==null) return 0;
		return (level-1)>=honorAwards.length
			?honorAwards[honorAwards.length-1]:honorAwards[level-1];
	}
	
	public int getEnergy(int level)
	{
		if(energy>0) return energy;
		if(energys==null) return 0;
		return (level-1)>=energys.length
			?energys[energys.length-1]:energys[level-1];
	}
	
	/** �����л�(��������Ϣ) */
	public Award bytesRead(ByteBuffer data)
	{
		gemsAward=data.readInt();
		metalAward=data.readInt();
		oilAward=data.readInt();
		siliconAward=data.readInt();
		uraniumAward=data.readInt();
		moneyAward=data.readInt();
		experienceAward=data.readInt();
		honorAward=data.readInt();
		serviceSid=data.readInt();
		vppoint=data.readInt();
		energy=data.readInt();
		vitality=data.readInt();
		officerFeats=data.readInt();
		propSid=readInts(data);
		shipSids=readInts(data);
		equipSids=readInts(data);
		officerSids=readInts(data);
		randomProps=readInts(data);
		try
		{
			integral=data.readInt();
		}
		catch(Exception e)
		{
		}
		return this;
	}
	
	/** ���л� (��������Ϣ)*/
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(gemsAward);
		data.writeInt(metalAward);
		data.writeInt(oilAward);
		data.writeInt(siliconAward);
		data.writeInt(uraniumAward);
		data.writeInt(moneyAward);
		data.writeInt(experienceAward);
		data.writeInt(honorAward);
		data.writeInt(serviceSid);
		data.writeInt(vppoint);
		data.writeInt(energy);
		data.writeInt(vitality);
		data.writeInt(officerFeats);
		writeInts(data,propSid);
		writeInts(data,shipSids);
		writeInts(data,equipSids);
		writeInts(data,officerSids);
		writeInts(data,randomProps);
		//���ӻ���
		data.writeInt(integral);
	}
	
	public void writeInts(ByteBuffer data,int[] ints)
	{
		if(ints==null)
		{
			data.writeInt(0);
		}
		else
		{
			data.writeInt(ints.length);
			for(int i=0;i<ints.length;i++)
			{
				data.writeInt(ints[i]);
			}
		}
	}
	
	public int[] readInts(ByteBuffer data)
	{
		int len=data.readInt();
		int[] ints=new int[len];
		for(int i=0;i<len;i++)
		{
			ints[i]=data.readInt();
		}
		return ints;
	}

	public int[] getShipSids()
	{
		return shipSids;
	}

	
	public int[] getEquipSids()
	{
		return equipSids;
	}

	
	public void setEquipSids(int[] equipSids)
	{
		this.equipSids=equipSids;
	}

	
	public void setShipSids(int[] shipSids)
	{
		this.shipSids=shipSids;
	}

	
	public int[] getOfficerSids()
	{
		return officerSids;
	}

	
	public void setOfficerSids(int[] officerSids)
	{
		this.officerSids=officerSids;
	}

	
	public int getVitality()
	{
		return vitality;
	}

	
	public void setVitality(int vitality)
	{
		this.vitality=vitality;
	}

	
	public int getIntegral()
	{
		return integral;
	}

	
	public void setIntegral(int integral)
	{
		this.integral=integral;
	}

	
	public int getOfficerFeats()
	{
		return officerFeats;
	}

	
	public void setOfficerFeats(int officerFeats)
	{
		this.officerFeats=officerFeats;
	}

	
	public int[] getAwardTypes()
	{
		return awardTypes;
	}

	
	public void setAwardTypes(int[] awardTypes)
	{
		this.awardTypes=awardTypes;
	}

	
	public int getLeagueGoal()
	{
		return leagueGoal;
	}

	
	public void setLeagueGoal(int leagueGoal)
	{
		this.leagueGoal=leagueGoal;
	}

	
	public int getLeagueCoin()
	{
		return leagueCoin;
	}

	
	public void setLeagueCoin(int leagueCoin)
	{
		this.leagueCoin=leagueCoin;
	}

	/** ����һ�����ϲ�����ǰ����(buff���ܱ��ϲ�,������Ҫ�뵥��ִ��) */
	public void assembleAwardFromAnother(Award target,Player player)
	{
		int level=1;
		if(player!=null) level=player.getLevel();
		metalAward=getMetalAward(level)+target.getMetalAward(level);
		oilAward=getOilAward(level)+target.getOilAward(level);
		siliconAward=getSiliconAward(level)+target.getSiliconAward(level);
		uraniumAward=getUraniumAward(level)+target.getUraniumAward(level);
		moneyAward=getMoneyAward(level)+target.getMoneyAward(level);
		experienceAward=getExperienceAward(level)+target.getExperienceAward(level);
		gemsAward=getGemsAward(level)+target.getGemsAward(level);
		honorAward=getHonorAward(level)+target.getHonorAward(level);
//		serviceSid=getServiceSid(level)+target.getServiceSid(level);
		vppoint=getVppoint(level)+target.getVppoint(level);
		energy=getEnergy(level)+target.getEnergy(level);
		integral+=target.getIntegral();
		vitality+=target.getVitality();
		officerFeats+=target.getOfficerFeats();
		propSid=SeaBackKit.assembleIntArrays(propSid,target.getPropSid());
		equipSids=SeaBackKit.assembleIntArrays(equipSids,target.getEquipSids());
		officerSids=SeaBackKit.assembleIntArrays(officerSids,target.getOfficerSids());
		shipSids=SeaBackKit.assembleIntArrays(shipSids,target.getShipSids());
		int[] randomArr=target.getRandomProps();
		if(randomArr!=null&&randomProps!=null)
		{
			randomArr=target.getRandomProps().clone();
			for(int i=0;i<randomArr.length;i+=3)
				randomArr[i+2]+=randomProps[randomProps.length-1];
		}
		randomProps=SeaBackKit.assembleIntArrays(randomProps,randomArr);
		// randomProps2
	}
	
	/** ���ϻ����
	 *  <br>�����������ϵĻ���������������һ���µĽ���ʵ��,���򷵻ر�ʵ��
	 */
	public Award assembleActivityAward(Player player)
	{
		// Ĭ�ϴ��뱾ʵ��
		Award award=this;
		if(awardTypes!=null&&awardTypes.length>0)
		{
			for(int i=0;i<awardTypes.length;i++)
			{
				award=ActivityAwardManager.getInstance().assembleWholeAward(
					player,award,awardTypes[i],TimeKit.getSecondTime());
			}
			// ������Ǳ�ʵ��,��Ϊ��ʱ����ʵ��,���ý�������Ϊ��,���ٽ��ж��⽱���ж�
			if(!award.equals(this))
			{
				award.setAwardTypes(null);
			}
		}
		return award;
	}
	
}
