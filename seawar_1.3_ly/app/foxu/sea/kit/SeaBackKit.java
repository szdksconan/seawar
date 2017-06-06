package foxu.sea.kit;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mustang.back.BackKit;
import mustang.back.SessionMap;
import mustang.codec.Base64;
import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.MathKit;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.LongList;
import mustang.set.Selector;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.dc.GameDBAccess;
import shelby.ds.DSManager;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.cross.war.CrossWarRoundSave;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AFightEventSave;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.dcaccess.mem.LogMemCache;
import foxu.push.AndroidPush;
import foxu.push.PushManager;
import foxu.sea.AttrAdjustment;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.ContextVarManager;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Service;
import foxu.sea.Ship;
import foxu.sea.TransportShip;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.Flag;
import foxu.sea.alliance.alliancebattle.AllianceBattleFight;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.DonateRank;
import foxu.sea.alliance.alliancebattle.MaterialValue;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightEvent;
import foxu.sea.award.Award;
import foxu.sea.builds.Build;
import foxu.sea.builds.Product;
import foxu.sea.builds.produce.StandProduce;
import foxu.sea.checkpoint.Chapter;
import foxu.sea.comrade.ComradeHandler;
import foxu.sea.comrade.ComradeTask;
import foxu.sea.equipment.Equipment;
import foxu.sea.event.AttackEventWriter;
import foxu.sea.event.FightEvent;
import foxu.sea.event.FightEventShowByteswriteable;
import foxu.sea.event.StayEventShowWriter;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.officer.OfficerBattleHQ;
import foxu.sea.officer.OfficerManager;
import foxu.sea.port.ChatMessagePort;
import foxu.sea.port.FightPort;
import foxu.sea.proplist.Prop;
import foxu.sea.recruit.RecruitDayTask;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.service.ServiceAbility;

/** 常用方法 */
public class SeaBackKit
{
	/** 1天的毫秒数 */
	public static final long DAY_MILL_TIMES=3600*24*1000;
	/** 一周的毫秒数 */
	public static final long WEEK_MILL_TIMES=3600*24*7*1000;
	/** 两周的毫秒数 */
	public static final long DOUBLE_WEEK_MILL_TIMES=3600*24*14*1000;
	/** 1个月的毫秒数 */
	public static final long MONTH_MILL_TIMES=3600*24*30*1000;
	/**1个月的秒数**/
	public static final long MONTH_MILL_TIME=3600*24*30;
	/** 默认的Base64编解码算法 */
	public static final Base64 BASE64=new Base64();

	/** 序列化操作 */
	public final static IntKeyHashMap writerMap=new IntKeyHashMap();
	
	public static Logger log=LogFactory.getLogger(SeaBackKit.class);
	
	public static CreatObjectFactory cbFactory;
	
	/** 为某个玩家重新设置技能 */
	public static void resetPlayerSkill(Player player,CreatObjectFactory objectFactory)
	{
		if(objectFactory==null) objectFactory=cbFactory;
		int alliance_id=0;
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			alliance_id=Integer.parseInt(player
				.getAttributes(PublicConst.ALLIANCE_ID));
			Alliance alliance=(Alliance)objectFactory.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance!=null)
			{
				// 添加联盟技能
				alliance.addAllianceSkills(player);
			}
			else
			{
				player.resetAdjustment();
			}
		}
		else
		{
			player.resetAdjustment();
		}
	}
	/* static methods */
	/**
	 * 设置事件序列化方法
	 * 
	 * @param key 事件type
	 * @param writer 序列化方法
	 */
	public static void setEventWriter(int key,
		FightEventShowByteswriteable writer)
	{
		if(writer==null) return;
		writerMap.put(key,writer);
	}

	public static void showByteswrite(ByteBuffer data,int current,
		FightEvent event,CreatObjectFactory objectFactory)
	{
		FightEventShowByteswriteable writer=(FightEventShowByteswriteable)writerMap
			.get(event.getEventState());
		if(writer==null)
		{
			FightEventShowByteswriteable w=null;
			if(event.getEventState()==FightEvent.HOLD_ON)
			{
				w=new StayEventShowWriter();
				writerMap.put(event.getEventState(),w);
			}
			else
			{
				w=new AttackEventWriter();
				writerMap.put(event.getEventState(),w);
			}
			writer=w;
		}
		writer.showBytesWrite(event,data,current,objectFactory);
	}

	/**
	 * Base64编解码算法 二进制数据转化为字符串
	 */
	public static String createBase64(ByteBuffer data)
	{
		byte[] array=data.toArray();
		data.clear();
		BASE64.encode(array,0,array.length,data);
		return new String(data.getArray(),0,data.top());
	}

	/** 根据时间算出宝石数量 */
	public static int getGemsForTime(int time)
	{
		int count=0;
		if(time<=0) return 0;
		// 如果整数分钟应该直接除，如果不是整数分钟，除完取整之后会舍去不足一分钟的部分，需要+1
		// 不足1分钟按1分钟算
		if(time%PublicConst.GEMS_SPEED==0)
			count=time/PublicConst.GEMS_SPEED;
		else
			count=time/PublicConst.GEMS_SPEED+1;
		count=count*PublicConst.GEMS_PER_UNIT_SPEED;
		return count;
	}

	// /** 发送奖励品 */
	// public static String sendAwardSelf(int awardSid,Player player,
	// int checkTime)
	// {
	// Award award=(Award)Award.factory.getSample(awardSid);
	// return award.awardSelf(player,checkTime,null);
	// }
	/** 获取某个玩家联盟名字 */
	public static String getAllianceByPlayer(Player player,
		CreatObjectFactory objectFactory)
	{
		Alliance allianceSource=(Alliance)objectFactory
			.getAllianceMemCache().load(
				player.getAttributes(PublicConst.ALLIANCE_ID));
		if(allianceSource!=null) return "("+allianceSource.getName()+")";
		return "";
	}
	/** 获取玩家联盟的名字 */
	public static String getAllianceName(Player player,
		CreatObjectFactory objectFactory)
	{
		if(player==null)return "";
		Alliance allianceSource=(Alliance)objectFactory
			.getAllianceMemCache().load(
				player.getAttributes(PublicConst.ALLIANCE_ID));
		if(allianceSource!=null) return allianceSource.getName();
		return "";
	}

	/** 计算某个舰队的资源携带量 */
	public static int groupCarryResource(FleetGroup gourp)
	{
		Fleet fleet[]=gourp.getArray();
		int carry=0;
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null||fleet[i].getShip()==null) continue;
			carry+=fleet[i].getCarryResource();
		}
		// 随军额外载重
		float extra=gourp.getOfficerFleetAttr().getCommonAttr(
			OfficerBattleHQ.ARMY,Ship.ALL_SHIP,PublicConst.EXTRA_CARRY,
			false,0);
		carry=(int)((100+extra)*carry/100);
		return carry;
	}

	/** 计算未被仓库保护的某种资源量的20% 就是战斗掠夺资源 */
	public static long fightResource(Player beAttacker,int resourceType)
	{
		/** 仓库容量 */
		long storeCap=Resources.buildCapacity(Build.BUILD_STORE,beAttacker);
		// 算上科技加成 容量的加成
		AdjustmentData buff=((AdjustmentData)beAttacker.getAdjstment()
			.getAdjustmentValue(PublicConst.STORE_ADD_BUFF));
		if(buff!=null) storeCap=storeCap/100*(100+buff.percent);
		long canCarry=(beAttacker.getResources()[resourceType]-storeCap)/10;
		if(canCarry<=0) return 0;
		return canCarry;
	}

	/** 对数组里面的值相加 */
	public static int resourceTotal(int resource[])
	{
		int num=0;
		for(int i=0;i<resource.length;i++)
		{
			num+=resource[i];
		}
		return num;
	}
	
	/** 对数组里面的值相加 */
	public static long resourceTotalP(long resource[])
	{
		long num=0;
		for(int i=0;i<resource.length;i++)
		{
			num+=resource[i];
		}
		return num;
	}

	/** 获得玩家sid兵力的数量 包括事件里面的 */
	public static IntList getPlayerAllTroops(Player player,
		CreatObjectFactory factory,boolean bool,int alliance_id)
	{
		IntList list=new IntList();
		int num=0;
		//联盟战
		IntList fightList=getAlliancePlayerShips(player,bool,alliance_id);
		for(int i=0;i<PublicConst.SHIP_FOR_SID.length;i++)
		{
			num=player.getIsland().getShipsBySid(
				PublicConst.SHIP_FOR_SID[i],null);
			num+=player.getIsland().getShipsBySidForDefendGroup(
				PublicConst.SHIP_FOR_SID[i]);
			// 派出去的船 找到派出去的事件
			ArrayList eventList=getFightEventSelf(player,factory);
			if(eventList!=null)
			{
				for(int j=0;j<eventList.size();j++)
				{
					FightEvent event=(FightEvent)eventList.get(j);
					if(event.getPlayerId()==player.getId())
					{
						FleetGroup group=event.getFleetGroup();
						Fleet fleet[]=group.getArray();
						for(int x=0;x<fleet.length;x++)
						{
							if(fleet[x]!=null)
							{
								if(fleet[x].getShip().getSid()==PublicConst.SHIP_FOR_SID[i])
								{
									num+=fleet[x].getNum();
									// boss战不产生战力损失
									if(factory.getIslandCache().load(event.getAttackIslandIndex()+"")
													.getIslandType()==NpcIsland.WORLD_BOSS)
									{
										num+=fleet[x].lostNum();
									}
								}
							}
						}

					}
				}
			}
			if(fightList!=null && fightList.size()!=0)
			{
				for(int j=0;j<fightList.size();j+=3)
				{
					if(fightList.get(j)==PublicConst.SHIP_FOR_SID[i])
					{
						num+=fightList.get(j+1);
					}
				}
			}
			if(num!=0)
			{
				list.add(PublicConst.SHIP_FOR_SID[i]);
				list.add(num);
			}
		}
		return list;
	}

	public static void getSomeShipSidScore(Player player,
		LongList fightScore,int shipSid,int num,int times,int dataInt,
		int dataInt1,int dataInt2,int dataInt3,OfficerBattleHQ hq,Fleet fleet,int attr)
	{
		if(times>7)return;
		int limitNum=num;
		int nowNum=0;
		int leadShipNum=player.getShipNum()+attr;
		double score=0;
		if(num>leadShipNum)
		{
			limitNum=leadShipNum;
			nowNum=num-leadShipNum;
		}
		Ship ship=(Ship)Ship.factory.newSample(shipSid);
		if(ship!=null)
		{
			double attack=0;
			double defence=0;
			double life=0;
			// 军官加成(不享受星石加成)
			if(hq!=null&&times<6)
			{
				if(fleet==null)
				{
					fleet=new Fleet();
					fleet.setShip((Ship)Ship.factory.newSample(shipSid));
				}
				fleet.clearFleetAdjust();
				fleet.setLocation(times);
				hq.initAttrs(fleet,OfficerBattleHQ.ARMY);
				attack=getOfficerAttribute(PublicConst.ATTACK,fleet);
				defence=getOfficerAttribute(PublicConst.DEFENCE,fleet);
				life=getOfficerAttribute(PublicConst.SHIP_HP,fleet);
				// 通用加成
				dataInt+=fleet.getFleetAttrAdjustment(PublicConst.ACCURATE,
					false);
				dataInt1+=fleet.getFleetAttrAdjustment(PublicConst.AVOID,
					false);
				dataInt2+=fleet.getFleetAttrAdjustment(
					PublicConst.CRITICAL_HIT,false);
				dataInt3+=fleet.getFleetAttrAdjustment(
					PublicConst.CRITICAL_HIT_RESIST,false);
			}
			// 增加 舰船等级技能影响
			float[] add=player.getLevelAbilityValue(ship.getSid());
			if(add!=null)
			{
				ship.addLife(add[0]);
				ship.addAttack(add[1]);
			}
			// 攻击*生命/10*战力加成/100*（100+命中+闪避+暴击+韧性）*数量
			double EQUIP_ACCURATE=getEquipAttribute(PublicConst.ACCURATE,
				false,shipSid,player);
			double EQUIP_AVOID=getEquipAttribute(PublicConst.AVOID,false,
				shipSid,player);
			double EQUIP_CRITICAL=getEquipAttribute(
				PublicConst.CRITICAL_HIT,false,shipSid,player);
			double EQUIP_CRITICAL_RESIST=getEquipAttribute(
				PublicConst.CRITICAL_HIT_RESIST,false,shipSid,player);
			score=((100f+PublicConst.EQUIP_RATIO
				*(EQUIP_ACCURATE+EQUIP_AVOID+EQUIP_CRITICAL+EQUIP_CRITICAL_RESIST))
				*(attack+ship.getAttack(1,player)+defence+ship.getDefence(1,
					player))
				*(life+ship.getShipLife(player))
				*(double)ship.getAttackFactor()*(double)limitNum)/10f/100f;
			// 计算加成以后的数值
			score=score/100l*(100l+dataInt+dataInt1+dataInt2+dataInt3);
		}
		fightScore.add(shipSid);
		fightScore.add((long)score);
		fightScore.add(limitNum);
		if(nowNum>0)
		{ 
			times++;
			getSomeShipSidScore(player,fightScore,shipSid,nowNum,times,
				dataInt,dataInt1,dataInt2,dataInt3,hq,fleet,attr);
		}
	
	}
	
	/**dataInt,dataInt1,dataInt2,dataInt3 为通用加成(带入每坑位战力计算以兼容军官加成)
	 * 因带入军官加成,需要将通用加成带入方法体,避免重复加成。hq为空则不进行加成,fightScore
	 * 3位1组{sid,score,num},times会作为军官填充的位置参数使用
	 * */
	public static void getSomeShipSidScore(Player player,
		LongList fightScore,int shipSid,int num,int times,int dataInt,
		int dataInt1,int dataInt2,int dataInt3,OfficerBattleHQ hq,int attr)
	{
		getSomeShipSidScore(player,fightScore,shipSid,num,times,dataInt,
			dataInt1,dataInt2,dataInt3,hq,null,attr);
	}
	
	/**计算当前玩家出兵的战斗力**/
	public static int getPlayerFightScroe(Player player,
		CreatObjectFactory factory,IntList list,OfficerBattleHQ hq,boolean flag)
	{
		boolean bool=false;
		int alliance_id=0;
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			alliance_id=Integer.parseInt(player
				.getAttributes(PublicConst.ALLIANCE_ID));
			Alliance alliance=(Alliance)factory.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance!=null) bool=true;
		}
		return calculateFightScore(player,factory,list,alliance_id,bool,hq,flag);
	}
	
	/** 计算玩家的战力并设置最高战力 */
	public static int setPlayerFightScroe(Player player,
		CreatObjectFactory factory)
	{
		boolean bool=false;
		int alliance_id=0;
		if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
			&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
		{
			alliance_id=Integer.parseInt(player
				.getAttributes(PublicConst.ALLIANCE_ID));
			Alliance alliance=(Alliance)factory.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance!=null) bool=true;
		}
		IntList list=getPlayerAllTroops(player,factory,bool,alliance_id);
		long finalScroe=calculateFightScore(player,factory,list,alliance_id,
			bool,null,false);
		player.setFightScore((int)finalScroe);
		AchieveCollect.FightScore(finalScroe,player);
		// 新兵福利
		RecruitKit.pushTask(RecruitDayTask.FIGHT_SCORE,
			player.getFightScore(),player,true);
		// 战友系统
		ComradeHandler.getInstance().finishTask(player,
			ComradeTask.FIGHT_SCORE);
		return (int)finalScroe;
	}
	
	/**根据当前船只数量计算出最大战斗力**/
	public static  int calculateFightScore(Player player,
		CreatObjectFactory factory,IntList list,int alliance_id,boolean bool,OfficerBattleHQ hq,boolean flag)
	{
		// 联盟
		boolean isNeedFlush=true;
		if(bool)
		{
			Alliance alliance=(Alliance)factory.getAllianceMemCache()
				.loadOnly(alliance_id+"");
			if(alliance!=null)
			{
				// 添加联盟技能
				alliance.addAllianceSkills(player);
				isNeedFlush=false;
			}
		}
		// 初始化军官信息
		if(!flag)
		{
			hq=new OfficerBattleHQ();
			hq.initOfficers(player);
		}
		if(hq==null)
			hq=new OfficerBattleHQ();
		int attr=0;
		//联盟站需要额外计算带兵量
		if(flag)
			attr=(int)hq.getCommonAttr(OfficerBattleHQ.ARMY,Ship.ALL_SHIP,
				PublicConst.EXTRA_SHIP,true,0);
		LongList fightScore=new LongList();
		if(isNeedFlush) player.resetAdjustment();
		// 通用属性加成
		AttrAdjustment.AdjustmentData data=player.getAdjstment()
			.getAdjustmentValue(1,PublicConst.ACCURATE);
		AttrAdjustment.AdjustmentData data1=player.getAdjstment()
			.getAdjustmentValue(1,PublicConst.AVOID);
		AttrAdjustment.AdjustmentData data2=player.getAdjstment()
			.getAdjustmentValue(1,PublicConst.CRITICAL_HIT);
		AttrAdjustment.AdjustmentData data3=player.getAdjstment()
			.getAdjustmentValue(1,PublicConst.CRITICAL_HIT_RESIST);
		int dataInt=0;
		int dataInt1=0;
		int dataInt2=0;
		int dataInt3=0;
		if(data!=null)
		{
			dataInt=data.percent;
		}
		if(data1!=null)
		{
			dataInt1=data1.percent;
		}
		if(data2!=null)
		{
			dataInt2=data2.percent;
		}
		if(data3!=null)
		{
			dataInt3=data3.percent;
		}
		// 计算所有的战斗力
		for(int i=0;i<list.size();i+=2)
		{
			getSomeShipSidScore(player,fightScore,list.get(i),list.get(i+1),
				0,dataInt,dataInt1,dataInt2,dataInt3,null,attr);
		}
		// 排序
		long value[]=fightScore.toArray();
		long temp=0;
		for(int i=0;i<value.length;i+=3)
		{
			for(int j=i;j<value.length;j+=3)
			{
				long dataA=value[i+1];
				long dataB=value[j+1];
				if(dataA<dataB)
				{
					// 交换sid
					temp=value[i];
					value[i]=value[j];
					value[j]=temp;
					// 交换战力
					temp=value[i+1];
					value[i+1]=value[j+1];
					value[j+1]=temp;
					// 交换数量
					temp=value[i+2];
					value[i+2]=value[j+2];
					value[j+2]=temp;
				}
			}
		}
		// 取前6位战力
		long finalScroe=0;
		LongList finalScoreList=new LongList();
		for(int i=0;i<value.length;i+=3)
		{
			finalScoreList.clear();
			if(i==6*3) break;
			getSomeShipSidScore(player,finalScoreList,(int)value[i],
				(int)value[i+2],i/3,dataInt,dataInt1,dataInt2,dataInt3,hq,attr);
			finalScroe+=finalScoreList.get(1);
		}
		finalScroe=(long)MathKit.sqrt(finalScroe);
		return (int)finalScroe;
	}
	
//	/** 总可掠夺资源 */
//	public static int[] canResource(Player beAttack)
//	{
//		int resource[]=new int[5];
//		// 金属
//		resource[Resources.METAL]=fightResource(beAttack,Resources.METAL);
//		// 石油
//		resource[Resources.OIL]=fightResource(beAttack,Resources.OIL);
//		// 硅
//		resource[Resources.SILICON]=fightResource(beAttack,Resources.SILICON);
//		// 铀
//		resource[Resources.URANIUM]=fightResource(beAttack,Resources.URANIUM);
//		// 金币
//		resource[Resources.MONEY]=fightResource(beAttack,Resources.MONEY);
//		return resource;
//	}
	
	/** 总可掠夺资源 */
	public static long[] canResourceP(Player beAttack)
	{
		long resource[]=new long[5];
		// 金属
		resource[Resources.METAL]=fightResource(beAttack,Resources.METAL);
		// 石油
		resource[Resources.OIL]=fightResource(beAttack,Resources.OIL);
		// 硅
		resource[Resources.SILICON]=fightResource(beAttack,Resources.SILICON);
		// 铀
		resource[Resources.URANIUM]=fightResource(beAttack,Resources.URANIUM);
		// 金币
		resource[Resources.MONEY]=fightResource(beAttack,Resources.MONEY);
		return resource;
	}

	/** 计算舰队可掠夺资源 */
	public static int[] attackResource(FleetGroup group,int resource[])
	{
		// int resource[]=canResource(beAttack);
		// 掠夺资源量
		long canCarry=groupCarryResource(group);
		int resourceTotal=resourceTotal(resource);
		// 超过舰队携带量
		if(canCarry<resourceTotal)
		{
			double temp=resource[Resources.METAL]*canCarry;
			resource[Resources.METAL]=(int)(temp/resourceTotal);
			temp=resource[Resources.OIL]*canCarry;
			resource[Resources.OIL]=(int)(temp/resourceTotal);
			temp=resource[Resources.SILICON]*canCarry;
			resource[Resources.SILICON]=(int)(temp/resourceTotal);
			temp=resource[Resources.URANIUM]*canCarry;
			resource[Resources.URANIUM]=(int)(temp/resourceTotal);
			temp=resource[Resources.MONEY]*canCarry;
			resource[Resources.MONEY]=(int)(temp/resourceTotal);
			temp=resource[Resources.GEMS]*canCarry;
			resource[Resources.GEMS]=(int)(temp/resourceTotal);
		}
		return resource;
	}
	
	/** 计算舰队可掠夺资源 */
	public static int[] attackResourceP(FleetGroup group,long resource[])
	{
		// 掠夺资源量
		long canCarry=groupCarryResource(group);
		long resourceTotal=resourceTotalP(resource);
		int[] res=new int[resource.length];
		// 超过舰队携带量
		if(canCarry<resourceTotal)
		{
			double temp=resource[Resources.METAL]/((double)resourceTotal);
			res[Resources.METAL]=(int)(temp*canCarry);
			temp=resource[Resources.OIL]/((double)resourceTotal);
			res[Resources.OIL]=(int)(temp*canCarry);
			temp=resource[Resources.SILICON]/((double)resourceTotal);
			res[Resources.SILICON]=(int)(temp*canCarry);
			temp=resource[Resources.URANIUM]/((double)resourceTotal);
			res[Resources.URANIUM]=(int)(temp*canCarry);
			temp=resource[Resources.MONEY]/((double)resourceTotal);
			res[Resources.MONEY]=(int)(temp*canCarry);
		}
		else
		{
			res[Resources.METAL]=(int)resource[Resources.METAL];
			res[Resources.OIL]=(int)resource[Resources.OIL];
			res[Resources.SILICON]=(int)resource[Resources.SILICON];
			res[Resources.URANIUM]=(int)resource[Resources.URANIUM];
			res[Resources.MONEY]=(int)resource[Resources.MONEY];

		}
		return res;
	}
	/**
	 * 根据key加载数据 单一key
	 * 
	 * @param key id
	 * @return 返回的ByteBuffer
	 */
	public static ByteBuffer load(String data)
	{
		if(data==null||data.equals("null")) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		BASE64.decode(data,0,data.length(),bb);
		return bb;
	}

	/** 设置舰队的附加属性 */
	public static void addAdjustment(Player player,FleetGroup fleetGroup)
	{
		if(player==null||fleetGroup==null) return;
		Fleet[] fleets=fleetGroup.getArray();
		for(int j=0;j<fleets.length;j++)
		{
			Fleet fleet=fleets[j];
			if(fleet!=null) fleet.setPlayter(player);
		}
	}

	/** 将当前毫秒数转化为时间格式 */
	public static String formatDataTime(int creatTime)
	{
		long time=creatTime*1000l;
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(time));
	}

	/** 从一个int数组里面取出不重复的n个数 */
	public static int[] getRandomNums(int nums[],int n)
	{
		IntList list=new IntList();

		for(int i=0;i<nums.length;i++)
		{
			list.add(nums[i]);
		}

		IntList list1=new IntList();
		while(list1.size()<n)
		{
			int random=list.get(MathKit.randomValue(0,list.size()));
			list.remove(random);
			list1.add(random);
		}
		return list1.toArray();
	}

	/** sql统计数量 */
	public static int loadBySqlOneData(String sql,GameDBAccess dbaccess)
	{
		Fields field=dbaccess.loadSql(sql);
		if(field==null) return 0;
		int value=0;
		if(field.getArray()!=null&&field.getArray().length>0)
		{
			value=Integer
				.parseInt(field.getArray()[0].getValue().toString());
		}
		return value;
	}

	/** 获得某天的起始秒数00:00:00 传0是今天 */
	public static int getSomedayBegin(long offsetTime)
	{
		Calendar today=Calendar.getInstance();
		long time=System.currentTimeMillis()-offsetTime;
		today.setTimeInMillis(time);
		today.set(Calendar.HOUR_OF_DAY,0);
		today.set(Calendar.MINUTE,0);
		today.set(Calendar.SECOND,0);
		return (int)(today.getTimeInMillis()/1000l);
	}

	/** 获得某天的结束时间 23:59:59 传0是今天 */
	public static int getSomedayEnd(long offsetTime)
	{
		Calendar today=Calendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis()-offsetTime);
		today.set(Calendar.HOUR_OF_DAY,23);
		today.set(Calendar.MINUTE,59);
		today.set(Calendar.SECOND,59);
		return (int)(today.getTimeInMillis()/1000l);
	}

	/** 获得某天的结束时间 time传毫秒 */
	public static int getSomeDayEndTime(long time)
	{
		Calendar today=Calendar.getInstance();
		today.setTimeInMillis(time);
		today.set(Calendar.HOUR_OF_DAY,23);
		today.set(Calendar.MINUTE,59);
		today.set(Calendar.SECOND,59);
		return (int)(today.getTimeInMillis()/1000l);
	}
	
	/** 获得某天的结束时间 time传毫秒 */
	public static int getWeekEndTime()
	{
		Calendar today=Calendar.getInstance();
		today.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
		today.set(Calendar.HOUR_OF_DAY,23);
		today.set(Calendar.MINUTE,59);
		today.set(Calendar.SECOND,59);
		int now=TimeKit.getSecondTime();
		int end=(int)(today.getTimeInMillis()/1000l);
		if(end>now)return end;
		return (int)(end+WEEK_MILL_TIMES/1000);
	}

	/** 获得某周中的1天的结束时间 time传毫秒 (周日)*/
	public static int getWeekEndSunTime()
	{
		Calendar today=Calendar.getInstance();
		today.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
		today.set(Calendar.HOUR_OF_DAY,23);
		today.set(Calendar.MINUTE,59);
		today.set(Calendar.SECOND,59);
		int now=TimeKit.getSecondTime();
		int end=(int)(today.getTimeInMillis()/1000l);
		if(end>now)return end;
		return (int)(end+WEEK_MILL_TIMES/1000);
	}
	
	

	/** *获得这是这一年的第几天 */
	public static int getDayOfYear()
	{
		Calendar now=Calendar.getInstance();
		return now.get(Calendar.DAY_OF_YEAR);
	}

	/** 获得这是第几年 */
	public static int getTheYear()
	{
		Calendar now=Calendar.getInstance();
		return now.get(Calendar.YEAR);
	}

	/** 获得这是今年第几个月 */
	public static int getTheMonth()
	{
		Calendar now=Calendar.getInstance();
		return now.get(Calendar.MONTH)+1;
	}

	/** 获得这是这个月第几天 */
	public static int getDayOfMonth()
	{
		Calendar now=Calendar.getInstance();
		return now.get(Calendar.DAY_OF_MONTH);
	}

	/** 获得这是这天的第几个小时 */
	public static int getHourOfTheDay()
	{
		Calendar now=Calendar.getInstance();
		return now.get(Calendar.HOUR_OF_DAY);
	}

	/** 将int值的前16位和后16位做为2个short值取出 */
	public static int[] get2ShortInInt(int value)
	{
		int l=value;
		int r=value;
		l>>=16;
		r<<=16;
		r>>=16;
		return new int[]{l,r};
	}

	/** 将2个short值放入int的前16位和后16位 */
	public static int put2ShortInInt(int left,int right)
	{
		int v=left;
		v<<=16;
		return v|right;
	}

	/**
	 * 是否在黑名单
	 * @param player
	 * @param name
	 * @return
	 */
	@Deprecated
	public static boolean isInBlackList(Player player,String name)
	{
		String friends=player.getAttributes(PublicConst.BLACK_LIST);
		if(friends==null||friends.length()==0)
			return false;
		String[] names=TextKit.split(friends,",");
		for(int i=0;i<names.length;i++)
		{
			if(names[i].equals(name))
				return true;
		}
		return false;
	}
	
	/**
	 * 是否在黑名单 
	 * @return
	 */
	public static boolean isInBlackList(Player player,int checkPlayerId){
		return player.getFriendInfo().getBlackList().get(checkPlayerId)==null?false:true;
	}
	
	
	/**
	 * 是否在好友列表
	 * @param player
	 * @param name
	 * @return
	 */
	public static boolean isInFriendList(Player player,String name)
	{
		String friends=player.getAttributes(PublicConst.FRIENDS_LIST);
		if(friends==null||friends.length()==0)
			return false;
		String[] names=TextKit.split(friends,",");
		for(int i=0;i<names.length;i++)
		{
			if(names[i].equals(name))
				return true;
		}
		return false;
	}
	
	/**
	 * 本线点对点消息
	 * 
	 * @param sessions:发送目标会话组
	 * @param msg:消息
	 */
	public static boolean sendOneToOneMsg(ChatMessage msg,DSManager manager,Player srcPlayer)
	{
		if(manager==null||msg==null||msg.getDest()==null
			||msg.getDest().length()==0) return false;
		// 获得本服务器的会话表
		SessionMap smap=manager.getSessionMap();
		Session[] sessions=smap.getSessions();
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(PublicConst.MESSAGE_PORT);
		data.writeByte(msg.getType());
		data.writeInt(msg.getTime());
		data.writeUTF(msg.getText());
		data.writeUTF(msg.getSrc());
		data.writeUTF(msg.getDest());
		Player player=null;
		Connect con=null;
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					if(player.getName().equalsIgnoreCase(msg.getDest()))
					{
						// 如果发送源不是在黑名单，才发邮件
						if(!isInBlackList(player,srcPlayer.getId()))
						{
							con.send(data);
							return true;
						}
						break;
					}
				}
			}
		}
		return false;
	}

	/** 系统消息 */
	public static void sendSystemMsg(Session session,String text)
	{
		if(session==null||text==null||text.length()==0) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(PublicConst.MESSAGE_PORT);
		data.writeByte(ChatMessage.SYSTEM_CHAT);
		data.writeInt(TimeKit.getSecondTime());
		data.writeUTF(text);
		data.writeUTF("");
		Connect con=session.getConnect();
		if(con!=null&&con.isActive())
		{
			con.send(data);
		}
	}

	/** 发送全服系统聊天消息 */
	public static void sendSystemMsg(DSManager manager,String text)
	{
		if(manager==null||text==null||text.length()==0) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(PublicConst.MESSAGE_PORT);
		data.writeByte(ChatMessage.SYSTEM_CHAT);
		data.writeInt(TimeKit.getSecondTime());
		data.writeUTF(text);
		data.writeUTF("");
		// 获得本服务器的会话表
		SessionMap smap=manager.getSessionMap();
		smap.send(data);
		//存信息
		ChatMessage mes=new ChatMessage();
		mes.setType(ChatMessage.SYSTEM_CHAT);
		mes.setTime(TimeKit.getSecondTime());
		mes.setSrc("");
		mes.setText(text);
		ChatMessagePort chatPort=(ChatMessagePort)BackKit.getContext().get(
			"chatMessagePort");
		if(chatPort!=null)
		{
			chatPort.numFiler();
			chatPort.getChatMessages().add(mes);
		}
	}
	

	/** 联盟消息广播 */
	public static void sendMsgForAlliance(ChatMessage msg,DSManager manager,
		Alliance alliance)
	{
		if(manager==null||msg==null) return;
		if(alliance==null) return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(PublicConst.MESSAGE_PORT);
		data.writeByte(msg.getType());
		data.writeInt(msg.getTime());
		data.writeUTF(msg.getText());
		data.writeUTF(msg.getSrc());
		Session sessions[]=manager.getSessionMap().getSessions();
		Player player=null;
		Connect con=null;
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					for(int j=0;j<alliance.getPlayerList().size();j++)
					{
						if(player.getId()==alliance.getPlayerList().get(j))
						{
							con.send(data);
							break;
						}
					}
				}
			}
		}

	}

	/**
	 * 本线广播消息
	 * 
	 * @param msg:消息
	 */
	public static void sendAllMsg(ChatMessage msg,DSManager manager,boolean sendByLocale)
	{
		if(manager==null||msg==null) return;
		// 获得本服务器的会话表
		SessionMap smap=manager.getSessionMap();
		final ByteBuffer data=new ByteBuffer();
		data.writeShort(PublicConst.MESSAGE_PORT);
		data.writeByte(msg.getType());
		data.writeInt(msg.getTime());
		data.writeUTF(msg.getText());
		data.writeUTF(msg.getSrc());
		if(msg.getType()==ChatMessage.FIGHT_DATA)
		{
			data.writeInt(msg.getMessageId());
			
		}else if(msg.getType()==ChatMessage.ACHIEVE_DATA)
		{
			data.writeShort(msg.getSid());
		}
		data.writeInt(msg.getPlayerType());
		if(sendByLocale)
		{
			final int locale=msg.getLocale();
			smap.select(new Selector(){
				public int select(Object obj)
				{
					Session session=(Session)obj;
					Player p=(Player)session.getSource();
					if(p!=null&&p.getLocale()==locale)
						session.getConnect().send(data);
					return Selector.FALSE;
				}
			});
		}
		else
		{
			smap.send(data);
		}
	}
	
	public static void sendMsgToOne(ChatMessage msg,Player player)
	{
		if(msg==null) return;
		Session session=(Session)player.getSource();
		if(session==null)return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive())return;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(PublicConst.MESSAGE_PORT);
		data.writeByte(msg.getType());
		data.writeInt(msg.getTime());
		data.writeUTF(msg.getText());
		data.writeUTF(msg.getSrc());
		if(msg.getType()==ChatMessage.FIGHT_DATA)
		{
			data.writeInt(msg.getMessageId());
		}else if(msg.getType()==ChatMessage.ACHIEVE_DATA)
		{
			data.writeShort(msg.getSid());
		}
		c.send(data);
	}

	/** 序列化战报技能 */
	public static void playerSkills(Player player,ByteBuffer fore,IntList list)
	{
		if(player==null)
		{
			fore.writeByte(0);
			return;
		}
		// buff技能
		Object[] object=player.getService().toArray();
		int num=0;
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			if(object[i] instanceof ServiceAbility)
			{
				num++;
			}
		}
		if(player.getPointBuffLv(Chapter.ATTACK)>0)
		{
			num++;
		}
		if(player.getPointBuffLv(Chapter.HP)>0)
		{
			num++;
		}
		int prosperityBuffSid = player.getProsperityInfoBuff();
		if(prosperityBuffSid>0){
			num++;
		}
		if(list !=null && list.size()!=0) num+=list.size();
		fore.writeByte(num);
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null) continue;
			if(object[i] instanceof ServiceAbility)
			{
				ServiceAbility sa=(ServiceAbility)object[i];
				fore.writeShort(sa.getSid());
			}
		}
		if(player.getPointBuffLv(Chapter.ATTACK)>0)
		{
			fore.writeShort(PublicConst.SHOW_SIDS[Chapter.ATTACK]);
		}
		if(player.getPointBuffLv(Chapter.HP)>0)
		{
			fore.writeShort(PublicConst.SHOW_SIDS[Chapter.HP]);
		}
		if(prosperityBuffSid>0){
			fore.writeShort(prosperityBuffSid);
		}
		/**新增buff序列化**/
		if(list!=null && list.size()!=0)
		{
			for(int i=0;i<list.size();i++)
			{
				fore.writeShort(list.get(i));
			}
		}
	}

	/** 序列化战报技能 */
	public static void playerNobuffSkills(Player player,ByteBuffer fore)
	{
		if(player==null)
		{
			fore.writeByte(0);
			return;
		}
		int num=0;
		if(player.getPointBuffLv(Chapter.ATTACK)>0)
		{
			num++;
		}
		if(player.getPointBuffLv(Chapter.HP)>0)
		{
			num++;
		}
		int prosperityBuffSid = player.getProsperityInfoBuff();
		if(prosperityBuffSid>0){
			num++;
		}
		fore.writeByte(num);
		if(player.getPointBuffLv(Chapter.ATTACK)>0)
		{
			fore.writeShort(PublicConst.SHOW_SIDS[Chapter.ATTACK]);
		}
		if(player.getPointBuffLv(Chapter.HP)>0)
		{
			fore.writeShort(PublicConst.SHOW_SIDS[Chapter.HP]);
		}
		if(prosperityBuffSid>0){
			fore.writeShort(prosperityBuffSid);
		}
	}
	
	/** 跨服战组合战报 */
	public static ByteBuffer crossConFightRecord(ByteBuffer data,
		CrossWarRoundSave save,int pid,boolean isattack,boolean isfinal)
	{
		data.writeInt(save.getId());
		data.writeInt(save.getCreatetime());
		if(save.getDefencepid()!=pid||isfinal)
		{
			if(isattack)
			{
				data.writeUTF(save.getAttackname());
				data.writeUTF(save.getAservername());
				data.writeUTF(save.getAnational());
				data.writeShort(save.getAttacklv());
				data.writeUTF(save.getDefencename());
				data.writeUTF(save.getDservername());
				data.writeUTF(save.getDnational());
				data.writeShort(save.getDefencelv());
				// System.out.println(save.getAttackname()
				// +":------Attack-----0------:"+save.getS1().isAttackWin());
				data.writeBoolean(save.getS1().isAttackWin());
				data.writeFloat(save.getS1().getLosePercent(true));
				data.writeFloat(save.getS1().getLosePercent(false));
				data.writeByte(save.getS1().getAlist().size()/2);
				for(int i=0;i<save.getS1().getAlist().size();i+=2)
				{
					data.writeShort(save.getS1().getAlist().get(i));
					data.writeShort(save.getS1().getAlist().get(i+1));
				}
				data.writeByte(save.getS1().getDlist().size()/2);
				for(int i=0;i<save.getS1().getDlist().size();i+=2)
				{
					data.writeShort(save.getS1().getDlist().get(i));
					data.writeShort(save.getS1().getDlist().get(i+1));
				}
				if(save.getS1().getRecord()==null)
				{
					data.writeBoolean(false);
				}
				else
				{
					data.writeBoolean(true);
					data.writeData(save.getS1().getRecord().toArray());
				}
				// 军官
				save.getS1().showBytesWriteOfficers(data,
					save.getS1().getAttackOfficers());
				save.getS1().showBytesWriteOfficers(data,
					save.getS1().getDefendOfficers());
			}
			else
			{
				data.writeUTF(save.getDefencename());
				data.writeUTF(save.getDservername());
				data.writeUTF(save.getDnational());
				data.writeShort(save.getDefencelv());
				data.writeUTF(save.getAttackname());
				data.writeUTF(save.getAservername());
				data.writeUTF(save.getAnational());
				data.writeShort(save.getAttacklv());
				data.writeBoolean(save.getS2().isAttackWin());
				data.writeFloat(save.getS2().getLosePercent(true));
				data.writeFloat(save.getS2().getLosePercent(false));
				data.writeByte(save.getS2().getAlist().size()/2);
				for(int i=0;i<save.getS2().getAlist().size();i+=2)
				{
					data.writeShort(save.getS2().getAlist().get(i));
					data.writeShort(save.getS2().getAlist().get(i+1));
				}
				data.writeByte(save.getS2().getDlist().size()/2);
				for(int i=0;i<save.getS2().getDlist().size();i+=2)
				{
					data.writeShort(save.getS2().getDlist().get(i));
					data.writeShort(save.getS2().getDlist().get(i+1));
				}
				if(save.getS2().getRecord()==null)
				{
					data.writeBoolean(false);
				}
				else
				{
					data.writeBoolean(true);
					data.writeData(save.getS2().getRecord().toArray());
				}
				// 军官
				save.getS2().showBytesWriteOfficers(data,
					save.getS2().getAttackOfficers());
				save.getS2().showBytesWriteOfficers(data,
					save.getS2().getDefendOfficers());
			}
		}
		else
		{
			if(isattack)
			{
				data.writeUTF(save.getDefencename());
				data.writeUTF(save.getDservername());
				data.writeUTF(save.getDnational());
				data.writeShort(save.getDefencelv());
				data.writeUTF(save.getAttackname());
				data.writeUTF(save.getAservername());
				data.writeUTF(save.getAnational());
				data.writeShort(save.getAttacklv());
				data.writeBoolean(save.getS2().isAttackWin());
				data.writeFloat(save.getS2().getLosePercent(true));
				data.writeFloat(save.getS2().getLosePercent(false));
				data.writeByte(save.getS2().getAlist().size()/2);
				for(int i=0;i<save.getS2().getAlist().size();i+=2)
				{
					data.writeShort(save.getS2().getAlist().get(i));
					data.writeShort(save.getS2().getAlist().get(i+1));
				}
				data.writeByte(save.getS2().getDlist().size()/2);
				for(int i=0;i<save.getS2().getDlist().size();i+=2)
				{
					data.writeShort(save.getS2().getDlist().get(i));
					data.writeShort(save.getS2().getDlist().get(i+1));
				}
				if(save.getS2().getRecord()==null)
				{
					data.writeBoolean(false);
				}
				else
				{
					data.writeBoolean(true);
					data.writeData(save.getS2().getRecord().toArray());
				}
				// 军官
				save.getS2().showBytesWriteOfficers(data,
					save.getS2().getAttackOfficers());
				save.getS2().showBytesWriteOfficers(data,
					save.getS2().getDefendOfficers());

			}
			else
			{
				data.writeUTF(save.getAttackname());
				data.writeUTF(save.getAservername());
				data.writeUTF(save.getAnational());
				data.writeShort(save.getAttacklv());
				data.writeUTF(save.getDefencename());
				data.writeUTF(save.getDservername());
				data.writeUTF(save.getDnational());
				data.writeShort(save.getDefencelv());
				data.writeBoolean(save.getS1().isAttackWin());
				data.writeFloat(save.getS1().getLosePercent(true));
				data.writeFloat(save.getS1().getLosePercent(false));
				data.writeByte(save.getS1().getAlist().size()/2);
				for(int i=0;i<save.getS1().getAlist().size();i+=2)
				{
					data.writeShort(save.getS1().getAlist().get(i));
					data.writeShort(save.getS1().getAlist().get(i+1));
				}
				data.writeByte(save.getS1().getDlist().size()/2);
				for(int i=0;i<save.getS1().getDlist().size();i+=2)
				{
					data.writeShort(save.getS1().getDlist().get(i));
					data.writeShort(save.getS1().getDlist().get(i+1));
				}
				if(save.getS1().getRecord()==null)
				{
					data.writeBoolean(false);
				}
				else
				{
					data.writeBoolean(true);
					data.writeData(save.getS1().getRecord().toArray());
				}
				// 军官
				save.getS1().showBytesWriteOfficers(data,
					save.getS1().getAttackOfficers());
				save.getS1().showBytesWriteOfficers(data,
					save.getS1().getDefendOfficers());
			}
		}

		return data;
	}
	
	/** 组合战报和前提信息 (attackGroup,defendGroup为组合军官信息传入)
	 * @param isExtraInfo 直接写入战报额外信息(版本号和军官信息,适用于不需要邮件的战报) */
	public static ByteBuffer conFightRecord(ByteBuffer fore,
		ByteBuffer fight,String attackName,int attackLevel,String defent,
		int defentLevel,int fightType,Player attacker,Player defend,
		FleetGroup attackGroup,FleetGroup defendGroup,boolean isExtraInfo,
		IntList attackBuff,IntList beAttackBuff)
	{
		
		if(isExtraInfo)
		{
			fore.writeBoolean(isExtraInfo);
			/** 战报版本(兼容战报解析有改动时旧战报显示) */
			fore.writeInt(PublicConst.FIGHT_RECORD_VERSION);
		}
		if(attackName==null) attackName="";
		if(defent==null) defent="";
		/** 战报其他信息 */
		fore.writeByte(fightType);
		/** 进攻方 */
		fore.writeUTF(attackName);
		fore.writeByte(attackLevel);
		playerSkills(attacker,fore,attackBuff);
		/** 防守方 */
		fore.writeUTF(defent);
		fore.writeByte(defentLevel);
		playerSkills(defend,fore,beAttackBuff);
		/** 战报 */
		fore.write(fight.getArray(),fight.offset(),fight.length());
		if(isExtraInfo)
		{
			// 组合随军军官信息
			attackGroup.getOfficerFleetAttr().showBytesWriteOfficers(fore);
			defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(fore);
		}
		return fore;
	}
	
	/** 组合战报和前提信息 (attackGroup,defendGroup为组合军官信息传入) */
	public static ByteBuffer combindeFightRecord(int groups,int npclength,
		ByteBuffer fore,ByteBuffer[] fight,String attackName,
		int attackLevel,String defent,int defentLevel,int fightType,
		Player attacker,Player defend,FleetGroup attackGroup,
		FleetGroup defendGroup)
	{
		fore.writeBoolean(true);
		/** 战报版本(兼容战报解析有改动时旧战报显示) */
		fore.writeInt(PublicConst.FIGHT_RECORD_VERSION);
		if(attackName==null) attackName="";
		if(defent==null) defent="";
		/** 战报其他信息 */
		fore.writeByte(fightType);
		/** 进攻方 */
		fore.writeUTF(attackName);
		fore.writeByte(attackLevel);
		playerSkills(attacker,fore,null);
		/** 防守方 */
		fore.writeUTF(defent);
		fore.writeByte(defentLevel);
		playerSkills(defend,fore,null);
		/** 战报 */
		for(int i=0;i<fight.length;i++)
		{
			fore.write(fight[i].getArray(),fight[i].offset(),fight[i].length());
		}
		// 组合随军军官信息
		attackGroup.getOfficerFleetAttr().showBytesWriteOfficers(fore);
		defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(fore);
		/**玩家有的队伍个数*/
		fore.writeByte(groups);
		/**NPC有的队伍个数*/
		fore.writeByte(npclength);
		return fore;
	}
	/** 组合盟战战报 (attackGroup,defendGroup为组合军官信息传入) */
	public static ByteBuffer conAllianceFightRecord(String win,
		String attack,String def,IntList attackList,int dcrhorn,
		IntList defList,ByteBuffer fore,ByteBuffer fight,int attackLevel,
		int defentLevel,int fightType,Player attacker,Player defend,
		FleetGroup attackGroup,FleetGroup defendGroup)
	{
		fore.writeUTF(attack);
		IntList lost=collateLostShip(attackList);
		fore.writeByte(lost.size()/2);
		for(int i=0;i<lost.size();i+=2)
		{
			fore.writeInt(lost.get(i+1));
			fore.writeShort(lost.get(i));
		}
		fore.writeByte(dcrhorn);
		
		fore.writeUTF(def);
		lost=collateLostShip(defList);
		fore.writeByte(lost.size()/2);
		for(int i=0;i<lost.size();i+=2)
		{
			fore.writeInt(lost.get(i+1));
			fore.writeShort(lost.get(i));
		}
		
		fore.writeUTF(win);
		// 后台无法从此处加入兼容代码(一个buffer),若前台不进行特殊处理,此处先按照原格式来解析
		conFightRecord(fore,fight,attack,attackLevel,def,defentLevel,
			fightType,attacker,defend,attackGroup,defendGroup,false,null,null);
		return fore;
	}
	
	/** 组合战报和前提信息 (attackGroup,defendGroup为组合军官信息传入)
	 * @param isExtraInfo 直接写入战报额外信息(版本号和军官信息,适用于不需要邮件的战报) */
	public static ByteBuffer conAllianceBattleFightRecord(ByteBuffer fore,
		ByteBuffer fight,String attackName,int attackLevel,String defent,
		int defentLevel,int fightType,Player attacker,Player defend,
		FleetGroup attackGroup,FleetGroup defendGroup,boolean isExtraInfo)
	{
		if(isExtraInfo)
			/** 战报版本(兼容战报解析有改动时旧战报显示) */
			fore.writeInt(PublicConst.FIGHT_RECORD_VERSION);
		if(attackName==null) attackName="";
		if(defent==null) defent="";
		/** 战报其他信息 */
		fore.writeByte(fightType);
		/** 进攻方 */
		fore.writeUTF(attackName);
		fore.writeByte(attackLevel);
		playerNobuffSkills(attacker,fore);
		/** 防守方 */
		fore.writeUTF(defent);
		fore.writeByte(defentLevel);
		playerNobuffSkills(defend,fore);
		/** 战报 */
		fore.write(fight.getArray(),fight.offset(),fight.length());
		if(isExtraInfo)
		{
			// 组合随军军官信息
			attackGroup.getOfficerFleetAttr().showBytesWriteOfficers(fore);
			defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(fore);
		}
		return fore;
	}
	
	/** 整理舰队损失 */
	public static IntList collateLostShip(IntList lost)
	{
		if(lost.size()<=2)return lost;
		IntList decr=new IntList();
		for(int i=0;i<lost.size();i+=2)
		{
			int sid=lost.get(i);
			int num=lost.get(i+1);
			boolean add=false;
			for(int k=0;k<decr.size();k+=2)
			{
				if(sid==decr.get(k))
				{
					decr.set(decr.get(k+1)+num,k+1);
					add=true;
				}
			}
			if(!add)
			{
				decr.add(sid);
				decr.add(num);
			}
			
		}
		return decr;
	}

	/** 查看某个系统设置是否打开 */
	public static boolean isOpen(int index,int iosSystem)
	{
		int tag=1;
		tag=tag<<index;
		if((iosSystem&tag)!=0) return true;
		return false;
	}

	/** 全服离线玩家push消息通知,switchIndex:推送类型 */
	public static void appPush(CreatObjectFactory objectFactory,String push,
		int switchIndex)
	{
		if(push==null) return;
		Object[] playerSaves=objectFactory.getPlayerCache().getCacheMap()
			.valueArray();
		Object temp=new Object();
		// 一个设备可能创建多个玩家,只需要推送一次
		Map<String,Object> pushedPlayers=new HashMap<String,Object>();
		for(int i=0;i<playerSaves.length;i++)
		{
			Player player=((PlayerSave)playerSaves[i]).getData();
			if(player!=null
				&&isOpen(switchIndex,player.getIsland().getIosSystem())
				&&pushedPlayers.get(player.getDeviceToken())==null)
			{
				Session session=objectFactory.getDsmanager().getSessionMap()
					.get(player.getName());
				if(session!=null&&session.getConnect().isActive()) continue;
				if(player.isIOS())
				{
					SeaBackKit.appPush(player,push);
				}
				else
				{
					AndroidPush.androidPush.addPush(
						player,
						push,
						InterTransltor.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,"game_name"));
				}
				pushedPlayers.put(player.getDeviceToken(),temp);
			}
		}
	}
	
	/** push消息通知 */
	public static void appPush(Player player,String content)
	{
		if(player.getDeviceToken()==null
			||player.getDeviceToken().length()%2!=0) return;
//		ApplePush.getInstance().sendPush(player.getDeviceToken(),content,
//			player.getName());
		PushManager.getInstance().push(player.getBundleId(),player.getDeviceToken(),content);
	}
	
	/**
	 * push 消息通知，群发
	 * @param players
	 * @param content
	 */
	public static void appPush(Player[] players,String content)
	{
		if(players==null||players.length==0||content==null||content.isEmpty())
			return;
		Map<String,ArrayList> bundleMap=new HashMap<String,ArrayList>();
		for(int i=0;i<players.length;i++)
		{
			Player player=players[i];
			String token=player.getDeviceToken();
			// 如果玩家设置不接收推送，那么token为空
			if(token==null||token.isEmpty())
				continue;
			String bundleId=player.getBundleId();
			if(bundleId==null||bundleId.isEmpty())
				continue;
			ArrayList list=bundleMap.get(bundleId);
			if(list==null)
			{
				list=new ArrayList();
				bundleMap.put(bundleId,list);
			}
			list.add(token);
		}
		if(bundleMap.size()==0)
			return;
		Iterator<Entry<String,ArrayList>> iter=bundleMap.entrySet().iterator();
		while(iter.hasNext())
		{
			Entry<String,ArrayList> entry=iter.next();
			String bundleId=entry.getKey();
			ArrayList list=entry.getValue();
			String[] tokens=new String[list.size()];
			list.toArray(tokens);
			PushManager.getInstance().push(bundleId,tokens,content);
		}
	}

	/** 战斗push */
	public static void sendFightPush(String push,Player player)
	{
		if(push==null) return;
		if(player.isIOS())
		{
			// 下线玩家已通知的次数
			int times=0;
			if(player.getAttributes(PublicConst.FIGHT_PUSH_TIME)!=null
				&&!player.getAttributes(PublicConst.FIGHT_PUSH_TIME).equals(
					""))
			{
				times=Integer.parseInt(player
					.getAttributes(PublicConst.FIGHT_PUSH_TIME));
				if(times>=3) return;
			}
			times++;
			player.setAttribute(PublicConst.FIGHT_PUSH_TIME,times+"");
			// push通知
			if(push!=null
				&&SeaBackKit.isOpen(PublicConst.ISLAND_BE_ATTACK,player
					.getIsland().getIosSystem()))
			{
				SeaBackKit.log.info("---ios----push--");
				SeaBackKit.appPush(player,push);
			}
		}
		else
		{
			if(push!=null
				&&SeaBackKit.isOpen(PublicConst.ISLAND_BE_ATTACK,player
					.getIsland().getIosSystem()))
			{
				SeaBackKit.log.info("-----and----push-----:");
				AndroidPush.androidPush.addPush(player,push,null);
			}
		}

	}
	
	/**
	 * 重载战斗邮件 含有减少的繁荣度
	 */
	public static void fight_send_every(CreatObjectFactory objectFactory,
		Player player,int fightType,String name,int index,String attackName,
		String defendName,boolean success,ByteBuffer fight,Award award,
		FightEvent event,IntList lostGourp,IntList delostGourp,
		int sourceIndex,int beIsLandSid,int mainFightType,int honorScore,
		String allianceDefend,FleetGroup defendGroup,int feats,int reduceProsperity)
	{
		// 发送邮件
		Message message=objectFactory.createMessage(0,player.getId(),"",
			"fight_report",player.getName(),Message.FIGHT_TYPE,"",true);
		message.setFightType(mainFightType);
		// 系统消息
		String messageString=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"attack_island_prop");
		NpcIsland island=(NpcIsland)NpcIsland.factory.getSample(beIsLandSid);
		if(island!=null)
		{
			messageString=setSystemContent(messageString,"%",ChatMessage.SEPARATORS,player.getName());
			messageString=TextKit.replace(messageString,"%",InterTransltor
				.getInstance().getTransByKey(PublicConst.SERVER_LOCALE,
					island.getName()));
			messageString=TextKit.replace(messageString,"%",island
				.getIslandLevel()
				+"");
		}
		message.createFightReports(player,fightType,name,index,attackName,
			defendName,success,fight,award,event,lostGourp,delostGourp,
			sourceIndex,beIsLandSid,objectFactory,messageString,honorScore,
			allianceDefend,defendGroup,feats,reduceProsperity,island);
		// 刷新前台
		JBackKit.sendRevicePlayerMessage(player,message,message
			.getRecive_state(),objectFactory);
		
	}
	

	/** 战斗发送邮件 刷新前台邮件 和push通知 */
	public static void fight_send_every(CreatObjectFactory objectFactory,
		Player player,int fightType,String name,int index,String attackName,
		String defendName,boolean success,ByteBuffer fight,Award award,
		FightEvent event,IntList lostGourp,IntList delostGourp,
		int sourceIndex,int beIsLandSid,int mainFightType,int honorScore,
		String allianceDefend,FleetGroup defendGroup,int feats)
	{
		fight_send_every(objectFactory,player,fightType,name,index,
			attackName,defendName,success,fight,award,event,lostGourp,
			delostGourp,sourceIndex,beIsLandSid,mainFightType,honorScore,allianceDefend,defendGroup,feats,0);
	}

	/** 根据岛屿index 计算出岛屿的坐标字符串 */
	public static String getIslandLocation(int index)
	{
		// 算出的坐标+1 与前台保持一致
		int x=index%NpcIsland.WORLD_WIDTH,y=index/NpcIsland.WORLD_WIDTH;
		return (x+1)+","+(y+1);
	}

	/** 将玩家的城防舰队设置进主力舰队 参战 */
	public static void addDefendBuild(Player beAttackPlayer,
		FleetGroup beAttackGroup)
	{
		Fleet fleet;
		int num=beAttackPlayer.getIsland().getShipsBySid(
			PublicConst.POSITION_FIRE_SID,null);
		if(num>0)
		{
			fleet=new Fleet((Ship)Ship.factory
				.newSample(PublicConst.POSITION_FIRE_SID),num);
			beAttackGroup.setFleet(PublicConst.POSITION_FIRE_INDEX,fleet);
		}
		num=beAttackPlayer.getIsland().getShipsBySid(
			PublicConst.POSITION_AIR_SID,null);
		if(num>0)
		{
			fleet=new Fleet((Ship)Ship.factory
				.newSample(PublicConst.POSITION_AIR_SID),num);
			beAttackGroup.setFleet(PublicConst.POSITION_AIR_INDEX,fleet);
		}
		num=beAttackPlayer.getIsland().getShipsBySid(
			PublicConst.POSITION_MISSILE_SID,null);
		if(num>0)
		{
			fleet=new Fleet((Ship)Ship.factory
				.newSample(PublicConst.POSITION_MISSILE_SID),num);
			beAttackGroup.setFleet(PublicConst.POSITION_MISSILE_INDEX,fleet);
		}
	}

	/** 找到一个玩家的派兵事件 */
	public static ArrayList getFightEventSelf(Player player,
		CreatObjectFactory objectFactory)
	{
		// 找到玩家岛屿
		int islandId=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList fightEventList=objectFactory.getEventCache()
			.getFightEventListById(islandId);
		if(fightEventList==null) return null;
		ArrayList list=new ArrayList();
		for(int i=0;i<fightEventList.size();i++)
		{
			if(fightEventList.get(i)==null) continue;
			FightEvent event=(FightEvent)fightEventList.get(i);
			if(event.getPlayerId()==player.getId()) list.add(event);
		}
		return list;
	}
	
	/**
	 * 获取玩家派出去执行任务的舰队中指定sid船只的数量
	 * @param sid
	 * @param player
	 * @param objectFactory
	 * @return
	 */
	public static int getEventShipCount(int sid,Player player,CreatObjectFactory objectFactory)
	{
		int num=0;
		// 派出去的船 找到派出去的事件
		ArrayList eventList=SeaBackKit.getFightEventSelf(player,objectFactory);
		if(eventList!=null)
		{
			for(int j=0;j<eventList.size();j++)
			{
				FightEvent event=(FightEvent)eventList.get(j);
				if(event.getPlayerId()==player.getId())
				{
					FleetGroup group=event.getFleetGroup();
					Fleet fleet[]=group.getArray();
					for(int x=0;x<fleet.length;x++)
					{
						if(fleet[x]!=null)
						{
							if(fleet[x].getShip().getSid()==sid)
							{
								num+=fleet[x].getNum();
							}
						}
					}

				}
			}
		}
		return num;
	}
	/**
	 * 检测玩家的存在和在线状态
	 * 
	 * @param playerName
	 * @param objectFactory
	 * @return 0存在且在线 1不存在 2存在不在线
	 */
	@Deprecated
	public static int checkPlayer(String playerName,String srcName,CreatObjectFactory objectFactory)
	{
		Player dest=objectFactory.getPlayerByName(playerName,false);
		if(dest==null) return 1;
		if(SeaBackKit.isInBlackList(dest,srcName))return 2;
		Session session=objectFactory.getDsmanager().getSessionMap()
			.get(playerName);
		if(session==null||!session.getConnect().isActive()) return 2;
		return 0;
	}
	
	public static int checkPlayer(String playerName,int srcId,CreatObjectFactory objectFactory)
	{
		Player dest=objectFactory.getPlayerByName(playerName,false);
		if(dest==null) return 1;
		if(SeaBackKit.isInBlackList(dest,srcId))return 2;
		Session session=objectFactory.getDsmanager().getSessionMap()
			.get(playerName);
		if(session==null||!session.getConnect().isActive()) return 2;
		return 0;
	}
	
	
	/** 判断某数组中是否包含某个值 */
	public static boolean isContainValue(int[] a,int value)
	{
		if(a==null) return false;
		for(int i=a.length-1;i>=0;i--)
		{
			if(a[i]==value) return true;
		}
		return false;
	}
	/** 将 yyyy-MM-dd HH:mm:ss 转成 秒 */
	public static int parseFormatTime(String formatTime)
	{
		if(formatTime==null||formatTime=="") return 0;
		int seconds=0;
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			seconds=(int)(format.parse(formatTime).getTime()/1000);
		}
		catch(Exception e)
		{
		}
		return seconds;
	}
	/**返回时间是星期几  0星期天   time 毫秒*/
	public static int getDayOfWeek(long time)
	{
//		Locale.setDefault(Locale.CHINA);
		Calendar c=Calendar.getInstance();
        c.setTimeInMillis(time);
        int day=c.get(Calendar.DAY_OF_WEEK);//获取时间  
        return day-1;
	}
	/** 获取本周某天 24时时间戳 0周日 */
	public static int getDayOfWeekTime(int day)
	{
		Calendar cal = Calendar.getInstance(); 
		cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 24, 0,0); 
		cal.set(Calendar.DAY_OF_WEEK, day+1); 
		return (int) (cal.getTimeInMillis()/1000); 
	}
	/**判断两个时间是否是同一周*/
	public static boolean isSameWeek(int time1,int time2)
	{
		 if(time2-time1>WEEK_MILL_TIMES/1000)  return false;
		 Calendar calendar = Calendar.getInstance(); 
		 calendar.setTimeInMillis(time1*1000l);
		 int week1=calendar.get(Calendar.WEEK_OF_YEAR); 
		 calendar.setTimeInMillis(time2*1000l);
		 int week2=calendar.get(Calendar.WEEK_OF_YEAR); 
		 return week1==week2;
	
	}
	
	/**判断两个时间是否是同一月*/
	public static boolean isSameMouth(int time1,int time2)
	{
		 if((time2-time1)>(MONTH_MILL_TIME))  return false;
		 Calendar calendar = Calendar.getInstance(); 
		 calendar.setTimeInMillis(time1*1000l);
		 int week1=calendar.get(Calendar.MONTH); 
		 calendar.setTimeInMillis(time2*1000l);
		 int week2=calendar.get(Calendar.MONTH); 
		 return week1==week2;
	
	}
	/** 判断两个时间是否是同一天 */
	public static boolean isSameDay(int time1,int time2)
	{
		if(time1-time2>PublicConst.DAY_SEC||time2-time1>PublicConst.DAY_SEC)
		{
			return false;
		}
		else
		{
			Calendar now=Calendar.getInstance();
			now.setTimeInMillis(time1*1000l);
			int day1=now.get(Calendar.DAY_OF_YEAR);
			now.setTimeInMillis(time2*1000l);
			int day2=now.get(Calendar.DAY_OF_YEAR);
			if(day1!=day2)
			{
				return false;
			}
		}
		return true;
	}
	/**
	 * 本线点对点消息
	 * 
	 * @param sessions:发送目标会话组
	 * @param msg:消息
	 */
	public static boolean sendOneToOneFightId(ChatMessage msg,
		DSManager manager,Player srcPlayer,int fightID)
	{
		if(manager==null||msg==null||msg.getDest()==null
			||msg.getDest().length()==0) return false;
		// 获得本服务器的会话表
		SessionMap smap=manager.getSessionMap();
		Session[] sessions=smap.getSessions();
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(PublicConst.MESSAGE_PORT);
		data.writeByte(msg.getType());
		data.writeInt(msg.getTime());
		data.writeUTF(msg.getText());
		data.writeUTF(msg.getSrc());
		data.writeInt(fightID);
		data.writeUTF(msg.getDest());
		Player player=null;
		Connect con=null;
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					if(player.getName().equalsIgnoreCase(msg.getDest()))
					{
						// 如果发送源不是在黑名单，才发邮件
						if(!isInBlackList(player,srcPlayer.getId())) con.send(data);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/** 检查是否舰队数量足够,isCheckCmdBook=是否检测统御书拥有数量(兼容快速阵型) */
	public static String checkShipNumLimit(IntList list,int length,ByteBuffer data,
		Player player,FleetGroup mainGroup,int commanderNum,boolean isCheckCmdBook)
	{
		IntKeyHashMap haveNums=new IntKeyHashMap();
		int index=player.getPlayerType()-PublicConst.COMMANDER_OPEN_LEVEL;
		if(commanderNum>0)
		{
			if(index<0)
			{
				return "playerType limit";//判断军衔
			}
			if(PublicConst.CLEVEL_CNUM[index]<commanderNum)
			{
				return "commander book max";//使用数量过多
			}
			if(isCheckCmdBook&&!player.checkPropEnough(PublicConst.COMMANDER_LEVEL_UP_SID,commanderNum))			
			{
				return "commander book limit";//判断统御书是否够
			}
		}
		list.add(commanderNum);
		int comShipNum=commanderNum*PublicConst.COMMANDER_NUM;//统御书额外带兵量
		// 军官提供的额外带兵量
		comShipNum+=(int)mainGroup.getOfficerFleetAttr()
			.getCommonAttr(OfficerBattleHQ.ARMY,Ship.ALL_SHIP,
				PublicConst.EXTRA_SHIP,true,0);
		// 判断舰队数量
		for(int i=0;i<length;i++)
		{
			int location=data.readUnsignedByte();
			int shipSid=data.readUnsignedShort();
			int num=data.readUnsignedShort();
			// 不能带城防兵力
			Ship ship=(Ship)Ship.factory.getSample(shipSid);
			if(ship==null
				||(ship.getPlayerType()==Ship.POSITION_AIR
					||ship.getPlayerType()==Ship.POSITION_MISSILE||ship
					.getPlayerType()==Ship.POSITION_FIRE))
				return "sid error";
			if(location<0||location>=7) return "unknown error";
			// 判断舰队是否足够
			Integer haveNum=(Integer)haveNums.get(shipSid);
			if(haveNum==null)
			{
				haveNum=player.getIsland().getShipsBySid(shipSid,
					player.getIsland().getTroops());
				if(mainGroup!=null)
				{
					haveNum+=mainGroup.getShipBySid(shipSid);
				}
				haveNums.put(shipSid,haveNum);
			}

			if(haveNum<num) return "ship num is limit";
			// 从总数里减去
			haveNums.put(shipSid,haveNum-num);
			int locationExtra=(int)mainGroup.getOfficerFleetAttr()
				.getCommonAttr(OfficerBattleHQ.ARMY,
					OfficerBattleHQ.CURRENT_LOCATION,PublicConst.EXTRA_SHIP,
					true,location);
			/** 带兵数量的限制 */
			if(num>player.getShipNum()+comShipNum+locationExtra)
				return "you can not take more";
			list.add(shipSid);
			list.add(num);
			list.add(location);
		}
		return null;
	}
	
	/** 检查是否舰队数量足够 */
	public static String checkShipNumLimit(IntList list,int length,ByteBuffer data,
		Player player,FleetGroup mainGroup,int commanderNum)
	{
		return checkShipNumLimit(list,length,data,player,mainGroup,
			commanderNum,true);
	}
	
	public static void resetPlayerName(Player player,final String newName,CreatObjectFactory objectFactory)
	{
		// 如果名字已经存在，就返回
		if(objectFactory.getPlayerByName(newName,false)!=null)
			return;
		// 找到加了好友的其他玩家,修改好友列表
		final String oldName=player.getName();
		player.setName(newName);
		final CreatObjectFactory factory=objectFactory;
		Object object[]=objectFactory.getPlayerCache().getCacheMap().valueArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null)continue;
			boolean change=false;
			PlayerSave save=(PlayerSave)object[i];
			Player p=(Player)save.getData();
			String friendList=p.getAttributes(PublicConst.FRIENDS_LIST);
			if(friendList!=null&&friendList.indexOf(oldName)>=0)change=true;
			friendList=replaceFriendList(friendList,oldName,newName);
			if(friendList!=null)
				p.setAttribute(PublicConst.FRIENDS_LIST,friendList);
			friendList=p.getAttributes(PublicConst.BLACK_LIST);
			if(friendList!=null&&friendList.indexOf(oldName)>=0)change=true;
			friendList=replaceFriendList(friendList,oldName,newName);
			if(friendList!=null)
				p.setAttribute(PublicConst.BLACK_LIST,friendList);
			if(change)factory.getPlayerCache().getChangeListMap().put(p.getId(),save);
		}
		
		object=objectFactory.getaFightEventMemCache().getCacheMap().valueArray();
		for(int i=0;i<object.length;i++)
		{
			if(object[i]==null)continue;
			AFightEventSave save=(AFightEventSave)object[i];
			AllianceFightEvent e=(AllianceFightEvent)save.getData();
			if(oldName.equals(e.getaName()))
			{
				e.setaName(newName);
				factory.getaFightEventMemCache().getChangeListMap()
					.put(e.getUid(),save);
			}
		}
	}
	
	private static String replaceFriendList(String friendList,String oldName,String newName)
	{
		if(friendList==null||friendList.length()<oldName.length())
			return null;
		if(friendList.indexOf(oldName)<0)
			return null;
		if(friendList.equalsIgnoreCase(oldName))
			return newName;
		// 产生大量临时对象
		if(friendList.startsWith(oldName+","))
		{
			String s=TextKit.replace(friendList,oldName+",",newName+",");
			return s;
		}
		if(friendList.endsWith(","+oldName))
		{
			String s=TextKit.replace(friendList,","+oldName,","+newName);
			return s;
		}
		if(friendList.indexOf(","+oldName+",")>0)
		{
			String s=TextKit.replace(friendList,","+oldName+",",","+newName+",");
			return s;
		}
		return null;
	}
	/***
	 * 获取当天凌晨的结束时间
	 * @return
	 */
	public static int getTimesnight(){ 
		Calendar cal = Calendar.getInstance(); 
		cal.set(Calendar.HOUR_OF_DAY, 24); 
		cal.set(Calendar.SECOND, 0); 
		cal.set(Calendar.MINUTE, 0); 
		cal.set(Calendar.MILLISECOND, 0); 
		return (int) (cal.getTimeInMillis()/1000); 
		} 
	
	public static void resetAllianceName(Alliance al,final String newName,CreatObjectFactory objectFactory)
	{
		final String oldName=al.getName();
		al.setName(newName);
		final CreatObjectFactory factory=objectFactory;
		Object[] objs=objectFactory.getPlayerCache().getCacheMap().valueArray();
		for(int i=0;i<objs.length;i++)
		{
			if(objs[i]==null)continue;
			PlayerSave save=(PlayerSave)objs[i];
			Player p=(Player)save.getData();
			if(oldName.equals(p.getAttributes(PublicConst.ALLIANCE_ID)))
			{
				p.setAttribute(PublicConst.ALLIANCE_ID,newName);
				factory.getPlayerCache().getChangeListMap()
					.put(p.getId(),save);
			}
		}
	}
	
	public static void checkAllianceDescr(CreatObjectFactory objectFactory,String[] emoji)
	{
		final CreatObjectFactory factory=objectFactory;
		final String[] emojis=emoji;
		Object[] objs=objectFactory.getAllianceMemCache().getCacheMap().valueArray();
		for(int k=0;k<objs.length;k++)
		{
			if(objs[k]==null)continue;
			AllianceSave save=(AllianceSave)objs[k];
			Alliance a=(Alliance)save.getData();
			a.getEventList().clear();
			String des=a.getDescription();
			if(des!=null)
			for(int i=0;i<emojis.length;i++)
			{
				if(des.indexOf(emojis[i])>=0)
				{
					log.error(a.getName()+":---clear---des------");
					a.setDescription("");
					
					break;
				}
			}
			String an=a.getAnnouncement();
			if(an!=null)
				for(int i=0;i<emojis.length;i++)
				{
					if(an.indexOf(emojis[i])>=0)
					{
						log.error(a.getName()+":---clear---an------");
						a.setAnnouncement("");
						break;
					}
				}
			factory.getAllianceMemCache().getChangeListMap().put(a.getId(),save);
		}
	}
	
	public static String[] getDeleteName(String oldName,int count,int type,CreatObjectFactory creatObjectFactory,String[] emoji,String spname)
	{
		String[] result=new String[2];
		String newName=oldName;
		if(newName.indexOf(spname)>=0&&newName.length()==2)newName="";
		for(int i=0;i<emoji.length;i++)
		{
			newName=TextKit.replaceAll(newName,emoji[i],"");
		}
		if(type==1)
		{
			if(newName.equals(""))
			{
				newName="batlship"+getCount(count);
			}
			while(creatObjectFactory.getPlayerByName(newName,false)!=null)
			{
				newName="batlship"+getCount(count);
				count++;
			}
			result[0]=newName;
			result[1]=""+count;
		}else if(type==2)
		{
			if(newName.equals(""))newName="al"+getCount(count);
			while(creatObjectFactory.getAllianceMemCache().loadByName(newName,false)!=null||creatObjectFactory.getAllianceMemCache().getDbaccess().isExist(newName,0))
			{
				newName="al"+getCount(count);
				count++;
			}
			result[0]=newName;
			result[1]=""+count;
			
		}
		return result;
	}
	public static String getCount(int count)
	{
		if(count<10)
		{
			return "00"+count;
		}
		if(count<100)
		{
			return "0"+count;
		}
		return ""+count;
	}
	
	/**
	 * 繁荣度伤害加成
	 * @return 加成百分比
	 */
	public static float getProsperityAttBuff(Player player){
		float value = 0;
		if(player.getProsperityInfo()!=null){
			
			int lv = player.getProsperityInfo()[3];//繁荣度等级
			float buff = Player.PROSPERITY_lV_BUFF[lv*3+2];//加成基数
			value = buff/100;
		}
		return value;
	}
	

	/** 获取装备对舰船属性的加成,装备不享受星石加成,isFix是否获取为具体值而非百分比 */
	public static float getEquipAttribute(int attrType,boolean isFix,int sid,Player player)
	{
		Ship sp=(Ship)Ship.factory.getSample(sid);
		float value=0;
		int equipAttrType=-1;
		//属性本身是百分比
		boolean isBasePercentage=true;
		switch(attrType)
		{
			case PublicConst.ATTACK:
				value=sp.getAttack();
				equipAttrType=PublicConst.EQUIP_ATTACK;
				isBasePercentage=false;
				break;
			case PublicConst.DEFENCE:
				value=sp.getDefence();
				equipAttrType=PublicConst.EQUIP_DEFENCE;
				isBasePercentage=false;
				break;
			case PublicConst.ACCURATE:
				equipAttrType=PublicConst.EQUIP_ACCURATE;
				break;
			case PublicConst.AVOID:
				equipAttrType=PublicConst.EQUIP_AVOID;
				break;
			case PublicConst.SHIP_HP:
				value=sp.getLife();
				equipAttrType=PublicConst.EQUIP_HP;
				isBasePercentage=false;
				break;
			case PublicConst.CRITICAL_HIT:
				equipAttrType=PublicConst.EQUIP_CRITICAL;
				break;
			case PublicConst.CRITICAL_HIT_RESIST:
				equipAttrType=PublicConst.EQUIP_CRITICAL_RESIST;
				break;
		}
		if(equipAttrType==-1)	return 0;
		AttrAdjustment.AdjustmentData data=player.getAdjstment().getAdjustmentValue(sp.getPlayerType(),
			equipAttrType);
		if(data!=null)
		{
			if(isFix)
				return data.fix;
			else if(isBasePercentage)
				return data.percent;
			else
				return value*data.percent/100;
		}
		return 0;
	}
	
	/** 装备日志(检测配件与材料剩余数量) */
	public static void createEquipTrackByAutoLeft(int equipSid,int num,
		int reason,int type,int item_id,Player player,
		CreatObjectFactory objectFactory)
	{
		int left=player.getEquips().getSameSidCount(equipSid);
		if(Equipment.factory.getSample(equipSid)==null)
		{
			left=player.getEquips().getQualityStuffCount(equipSid);
		}
		objectFactory.createEquipTrack(type,reason,player.getId(),equipSid,
			num,item_id,left);
	}
	
	/** 检测一个玩家是否在线 */
	public static boolean isPlayerOnline(Player player)
	{
		if(player==null) return false;
		Session session=(Session)player.getSource();
		if(session!=null&&session.getConnect()!=null
			&&session.getConnect().isActive()) return true;
		return false;
	}
	/** 判定字符串是否是数字 */
	public static boolean isDigit(String digit)
	{
		if(digit==null||digit.length()<=0) return false;
		for(int i=0;i<digit.length();i++)
		{
			if(!Character.isDigit(digit.charAt(i))) return false;
		}
		return true;
	}
	
	/** 获取当前建筑对应建造队列位置的产品id */
	public static int getProductId(Player player,int buildIndex,int productIndex)
	{
		int produceId=0;
		IntList record=(IntList)player.getProductRecord().get(
			buildIndex);
		if(record!=null&&record.size()>productIndex)
		{
			produceId=record.get(productIndex);
		}
		return produceId;
	}
	
	/** 移除当前建筑对应建造队列位置的产品id */
	public static void removeProductId(Player player,int buildIndex,int productIndex)
	{
		IntList record=(IntList)player.getProductRecord().get(
			buildIndex);
		if(record!=null&&record.size()>productIndex)
		{
			record.removeIndex(productIndex);
		}
	}
	
	/** 当前产品id生成,并于player记录 */
	public static int generateProductId(Player player,
		CreatObjectFactory objectFactory,int buildIndex,
		StandProduce produce,Product product)
	{
		int produceId=objectFactory.getProduceTrackMemCache().getUidkit()
			.getPlusUid();
		IntList record=(IntList)player.getProductRecord().get(
			buildIndex);
		if(record==null)
		{
			record=new IntList();
			player.getProductRecord().put(buildIndex,record);
		}
		int productIndex=-1;
		Object[] products=produce.getProductes().getArray();
		for(int i=0;i<products.length;i++)
		{
			if(products[i]==product)
			{
				productIndex=i;
				break;
			}
		}
		if(productIndex<0) return 0;
		record.add(produceId,productIndex);
		return produceId;
	}
	
	/** 向前台发送格式化的系统消息 */
	public static void sendFormatSystemMessage(String[] texts,int[] signs,DSManager manager)
	{
		ChatMessage message=new ChatMessage();
		// type
		message.setType(ChatMessage.FORMAT_SYSTEM_MESSAGE);
		message.setTime(TimeKit.getSecondTime());
		message.setSrc("");
		message.setText("");
		message.setFormatText(texts);
		message.setFormatSign(signs);
		if(manager==null) return;
		// 获得本服务器的会话表
		SessionMap smap=manager.getSessionMap();
		ByteBuffer data=new ByteBuffer();
		data.writeShort(PublicConst.MESSAGE_PORT);
		message.showBytesWrite(data,true);
		//存信息
		ChatMessagePort chatPort=(ChatMessagePort)BackKit.getContext().get(
			"chatMessagePort");
		chatPort.numFiler();
		chatPort.getChatMessages().add(message);
		smap.send(data);
	}
	
	/** 向中心http 请求*/
	public static ByteBuffer sendHttpData(ByteBuffer data,String token,int port,int type)
	{
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		if(data!=null)
		{
			String httpData=SeaBackKit.createBase64(data);
			map.put("data",httpData);
		}
		map.put("type",type+"");
		map.put("token",token);
		// 设置port
		map.put("port",port+"");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,null);
		}
		catch(IOException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
	
	/** 当前玩家是否可用对应功能,返回翻译异常信息 */
	public static String checkChatOpen(String chatTypeKey,Player player,String i18nKey)
	{
		//如果玩家为VIP则无需限制
		if(player.getUser_state() > 0)
			return null;
		int limitLv=ContextVarManager.getInstance().getVarValue(chatTypeKey);
		if(limitLv>player.getLevel())
		{
			String content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,i18nKey);
			return TextKit.replace(content,"%",limitLv+"");
		}
		return null;
	}
	
	/** 判断联盟是否是敌对 a1 自己联盟 a2 别的联盟 **/
	public static boolean isHostile(Alliance a1,Alliance a2)
	{
		if(a1==null||a2==null) return false;
		if(a1.getHostile()==null||a1.getHostile().length()==0) return false;
		String[] str=a1.getHostile().split(",");
		for(int i=0;i<str.length;i++)
		{
			if(TextKit.parseInt(str[i])==a2.getId()) return true;
		}
		return false;
	}
	
	/** 判断联盟是否是同一联盟 a1 自己联盟 a2 别的联盟 **/
	public static boolean isSameAlliance(Alliance a1,Alliance a2)
	{
		if(a1==null||a2==null) return false;
		if(a1.getId()!=a2.getId()) return false;
		return true;
	}
	
	/**判断联盟是否是敌对 **/
	public static boolean  isHostile(CreatObjectFactory objectFactory,Player player,String allianceName)
	{
		String paid=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(paid!=null && paid.length()!=0)
		{
			Alliance palliance=objectFactory.getAlliance(TextKit.parseInt(paid),false);
			Alliance alliance=objectFactory.getAllianceMemCache()
							.loadByName(allianceName,false);
			if(palliance!=null)
			{
				if(isHostile(palliance,alliance)) 
					return true;
			}
		}
		return false;
	}
	
	/**从配置的奖励品数组设置奖励信息(sid,num) **/
	public static void resetAward(Award award,int[] awardSid)
	{
		if(awardSid==null) return;
		award
		.setOfficerSids(SeaBackKit.getItems(awardSid,Prop.OFFICER));
		award
			.setEquipSids(SeaBackKit.getItems(awardSid,Prop.EQUIP));
		award.setShipSids(SeaBackKit.getItems(awardSid,Prop.SHIP));
		award.setPropSid(SeaBackKit.getItems(awardSid,Prop.PROP));
	}
	
	/**获取奖励数组信息(sid,num--适用于Award对象) **/
	public static int[] getItems(int[] awardSid,int propType)
	{
		IntList list=new IntList();
		for(int i=0;i<awardSid.length;i+=2)
		{
			if(getSidType(awardSid[i])==propType)
			{
				list.add(awardSid[i]);
				list.add(awardSid[i+1]);
			}
		}
		return list.toArray();
	}
	
	/** 检测玩家新手引导标志是否正常，否则修复标志状态 */
	public static void checkNewPlayerMark(Player player)
	{
		int max=PublicConst.NEW_PLAYER_MARK_MAX;
		if(player.getTaskMark()>=max || player.getPlayerTaskMark()>=max)
		{
			if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
				player.setAttribute(PublicConst.NEW_FOLLOW_PLAYER,null);
			String newPlayerAward=player.getAttributes(PublicConst.NEW_PLAYER_AWARD);
				if(newPlayerAward==null||"".equals(newPlayerAward)){
					/** 送1个2级潜艇,2级巡洋舰 */
					Award award=(Award)Award.factory.getSample(PublicConst.NEW_PLAYER_AWARD_SID);	
					award.awardLenth(new ByteBuffer(),player,null,null,null);
					JBackKit.sendResetTroops(player);
					if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER_HOLD)!=null)
					{
						player.setAttribute(PublicConst.NEW_FOLLOW_PLAYER_HOLD,null);
						NpcIsland island=(NpcIsland)NpcIsland.factory
										.getSample(FightPort.NEW_PLAYER_ATT_ISLAND);
						int[] res=new int[Player.RESOURCES_SIZE];
						island.forceSetResource(res,FightPort.NEW_PLAYER_HOLD_RESOURCE);
						// 回归资源
						Resources.addResources(player.getResources(),res,player);
					}
					player.setAttribute(PublicConst.NEW_PLAYER_AWARD,"Y");
				}
			player.setTaskMark(max);
			player.setPlayerTaskMark(max);
		}
	}
	
	/**
	 * 创建，切换与删除表
	 * @param cache 日志内存管理器
	 * @param sp 数据库代理
	 */
	public static void collateLogTable(LogMemCache cache,SqlPersistence sp)
	{
		int nowSec=TimeKit.getSecondTime();
		if(cache.getTableTime()>0
			&&!SeaBackKit.isSameDay(cache.getTableTime()+cache.getTableCD()
				*PublicConst.DAY_SEC,nowSec)) return;
		Calendar c=Calendar.getInstance();
		int year=c.get(Calendar.YEAR);
		int mon=c.get(Calendar.MONTH)+1;
		int day=c.get(Calendar.DAY_OF_MONTH);
		String m=mon>=10?mon+"":"0"+mon;
		String d=day>=10?day+"":"0"+day;
		String nowName=cache.getTable()+"_"+year+"_"+m+"_"+d;
		long nowTime=c.getTimeInMillis();
		String sql="show TABLES like ";
		Fields fields=null;
		String tabelName=null;
		// 判断是否已有目的表
		boolean have=false;
		for(long n=0;n<cache.getTableCD();n++)
		{
			c.setTimeInMillis(nowTime-n*PublicConst.DAY_SEC*1000);
			year=c.get(Calendar.YEAR);
			mon=c.get(Calendar.MONTH)+1;
			day=c.get(Calendar.DAY_OF_MONTH);
			m=mon>=10?mon+"":"0"+mon;
			d=day>=10?day+"":"0"+day;
			tabelName=cache.getTable()+"_"+year+"_"+m+"_"+d;
			fields=SqlKit.query(sp.getConnectionManager(),sql+"'"+tabelName
				+"'");
			if(fields!=null&&fields.size()>0)
			{
				cache.setTableTime((int)(c.getTimeInMillis()/1000));
				have=true;
				break;
			}
		}
		// 创建
		if(!have)
		{
			sql="CREATE TABLE `"+nowName+"` "+cache.getCreateSql();
			SqlKit.execute(sp.getConnectionManager(),sql);
			cache.setTableTime(TimeKit.getSecondTime());
			tabelName=nowName;
		}
		// 设置
		sp.setTable(tabelName);
		// 清理
		sql="DROP TABLE IF EXISTS ";
		long delTime=cache.getTableTime()*1000l-PublicConst.DAY_SEC*1000l
			*cache.getSaveTables()*cache.getTableCD();
		for(long i=0;i<cache.getSaveTables();i++)
		{
			c.setTimeInMillis(delTime-i*cache.getTableCD()
				*PublicConst.DAY_SEC*1000);
			year=c.get(Calendar.YEAR);
			mon=c.get(Calendar.MONTH)+1;
			day=c.get(Calendar.DAY_OF_MONTH);
			m=mon>=10?mon+"":"0"+mon;
			d=day>=10?day+"":"0"+day;
			tabelName=cache.getTable()+"_"+year+"_"+m+"_"+d;
			SqlKit.execute(sp.getConnectionManager(),sql+"`"+tabelName+"`");
		}

	}
	/**
	 * byte数组 转 ascii字符串
	 * @param b
	 * @return
	 */
	public static String BytesToAsciiString(byte[] b)
	{
		StringBuffer sub=new StringBuffer();
		for(int i=0;i<b.length;i++)
		{
			sub.append((char)b[i]);
		}
		return sub.toString();
	}
	
	/** 兼容手动设置抽奖提示(使用LUCKY_SIDS的地方请使用该方法获取) */
	public static int[] getLuckySids()
	{
		ContextVarManager varManager=ContextVarManager.getInstance();
		String key=ContextVarManager.AWARD_LUCKY_SIDS;
		if(varManager.getVarValue(key)>0)
		{
			if(PublicConst.manualLuckySids==null)
			{
				PublicConst.manualLuckySids=TextKit.parseIntArray(TextKit.split(
					varManager.getVarDest(key),","));
			}
			return PublicConst.manualLuckySids;
		}
		return PublicConst.LUCKY_SIDS;
	}
	
	/** 检查使用的阵型是否数量足够,获取阵型时不需要带入统御书计算 */
	public static String checkFormationShipNum(Player player,
		FleetGroup mainGroup,IntList group)
	{
		IntKeyHashMap haveNums=new IntKeyHashMap();
		// 判断舰队数量
		for(int i=0;i<group.size();i+=3)
		{
			int location=group.get(i+2);
			int shipSid=group.get(i);
			int num=group.get(i+1);
			// 不能带城防兵力
			Ship ship=(Ship)Ship.factory.getSample(shipSid);
			if(ship==null
				||(ship.getPlayerType()==Ship.POSITION_AIR
					||ship.getPlayerType()==Ship.POSITION_MISSILE||ship
					.getPlayerType()==Ship.POSITION_FIRE))
				return "sid error";
			if(location<0||location>=7) return "unknown error";
			// 判断舰队是否足够
			Integer haveNum=(Integer)haveNums.get(shipSid);
			if(haveNum==null)
			{
				haveNum=player.getIsland().getShipsBySid(shipSid,
					player.getIsland().getTroops());
				if(mainGroup!=null)
				{
					haveNum+=mainGroup.getShipBySid(shipSid);
				}
				haveNums.put(shipSid,haveNum);
			}

			if(haveNum<num) return "ship num is limit";
			// 从总数里减去
			haveNums.put(shipSid,haveNum-num);
		}
		return null;
	}
	
	/**检测字符串中是否有空格**/
	public static boolean checkBlank(String str)
	{
		if(str==null || str.length()==0) return false; 
		Pattern pattern=Pattern.compile("[\\s]+");
		Matcher matcher=pattern.matcher(str);
		while(matcher.find())
		{
			return true;
		}
		return false;
	}
	
	/** 判定sid属于某种类型 */
	public static int getSidType(int sid)
	{
		Object obj=Prop.factory.getSample(sid);
		if(obj!=null) return Prop.PROP;
		obj=Ship.factory.getSample(sid);
		if(obj!=null) return Prop.SHIP;
		obj=Equipment.factory.getSample(sid);
		if(obj!=null) return Prop.EQUIP;
		for(int i=0;i<Equipment.QUALITY_STUFFS.length;i+=3)
		{
			if(sid==Equipment.QUALITY_STUFFS[i]) return Prop.EQUIP;
		}
		obj=OfficerManager.factory.getSample(sid);
		if(obj!=null) return Prop.OFFICER;
		return Prop.VALID;
	}
	
	/**玩家是否通过了新手改名流程**/
	public static boolean isNewPlayerChangeName(int playerId,
		CreatObjectFactory objectFactory)
	{
		Player player=objectFactory.getPlayerById(playerId);
		if(player==null)
			return false;
		if(player.getName()==null)
			return false;
		if(!player.getName().startsWith("player"))
			return true;
		if(player.getAttributes(PublicConst.CREAT_NAME)!=null)
			return false;
		return true;
	}

	/** 判断字符创数组中是否包含某个值 */
	public static boolean isContainValue(String aa,String value)
	{
		if(aa==null) return false;
		String [] a=aa.split(",");
		for(int i=a.length-1;i>=0;i--)
		{
			if(a[i].equals(value)) return true;
		}
		return false;
	}

	/** 战斗发送邮件 刷新前台邮件 和push通知 */
	public static Message fight_send_alliance(Player player,Player defend,
		boolean success,ByteBuffer fight,IntList lostGourp,IntList delostGourp,String attackAllianceName,String beAttackAllianceName,int allianceId,int fightType,FleetGroup attackGroup,FleetGroup defendGroup
		)
	{
		// 发送邮件
		Message message=cbFactory.createMessage(0,allianceId,"",
			"","",Message.ALLIANCE_FIGHT_TYPE,"",true);
		//设置战斗类型
		message.setFightType(fightType);
		message.createAllianceFightReports(player,attackAllianceName,defend,beAttackAllianceName,success, fight, lostGourp,delostGourp, attackGroup, defendGroup);
		return message;
		
	}
	
	/** 群发竞标邮件 **/
	public static void sendAllianceMessage(String title,String content,
		IntList list,int playerId,IntKeyHashMap map,int awardSid)
	{
		if(list==null) return ;
		Award award=null;
		String excontent="";
		if(awardSid!=0)
		{
			award=(Award)Award.factory.newSample(awardSid);
			excontent=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"alliance_fight_award");
		}
		// 邮件主题
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		Message message;
		for(int i=0;i<list.size();i++)
		{
			Player player=cbFactory.getPlayerById(list.get(i));
			if(player==null) continue;
			if(award!=null && map.get(player.getId())!=null)
			{
				String content1=TextKit.replace(content,"%",excontent);
				message=cbFactory.createMessage(0,player.getId(),content1,
					sendName,player.getName(),Message.SYSTEM_ONE_TYPE,title,
					true,award);
			}
			else
			{
				String content1=TextKit.replace(content,"%","");
				message=cbFactory.createMessage(0,player.getId(),content1,
					sendName,player.getName(),Message.SYSTEM_ONE_TYPE,title,
					true,null);
			}
			// 刷新前台
			JBackKit.sendRevicePlayerMessage(player,message,
				message.getRecive_state(),cbFactory);
		}
		if(playerId!=0)
		{
			Player player=cbFactory.getPlayerById(playerId);
			if(player==null) return;
			message=cbFactory.createMessage(0,player.getId(),content,
				sendName,player.getName(),Message.SYSTEM_ONE_TYPE,title,
				true,null);
			// 刷新前台
			JBackKit.sendRevicePlayerMessage(player,message,
				message.getRecive_state(),cbFactory);
		}
	}
	/** 计算两个时刻 相差多少天 */
	public static int getDdays(int t1,int t2)
	{
		if(t2<t1)
		{
			int t=t1;
			t1=t2;
			t2=t;
		}
		Calendar now=Calendar.getInstance();
		now.setTimeInMillis(t1*1000l);
		int day1=now.get(Calendar.DAY_OF_YEAR);
		int y1=now.get(Calendar.YEAR);
		now.setTimeInMillis(t2*1000l);
		int day2=now.get(Calendar.DAY_OF_YEAR);
		int y2=now.get(Calendar.YEAR);
		if(y1==y2) return day2-day1;
		int ydays=0;
		for(int y=y1+1;y<y2;y++)
		{
			ydays+=365;
			if((y%4==0&&y%100!=0)||y%400==0)
			{
				ydays++;
			}
		}
		day1=365-day1;
		if((y1%4==0&&y1%100!=0)||y1%400==0)
		{
			day1++;
		}
		return day1+day2+ydays;
	}
	
	/**给联盟管理者发邮件**/
	public void sendMessageToAllianceManage(Alliance alliance,String title,String content)
	{
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		IntList list=alliance.getVicePlayers();
		for(int i=0;i<list.size();i++)
		{
			Player player=cbFactory.getPlayerById(list.get(i));
			if(player==null) continue;
			Message message=cbFactory.createMessage(0,player.getId(),
				content,sendName,player.getName(),0,title,true);
			// 刷新前台
			JBackKit.sendRevicePlayerMessage(player,message,message
				.getRecive_state(),cbFactory);
		}
	}
	
	/**离开联盟需要对物资捐献和联盟捐献做记录**/
	public static void leaveAllianceRecord(Player player,Alliance alliance)
	{
		if(player==null) return ;
		if(alliance==null) return;
		StringBuffer buffer=new StringBuffer();
		MaterialValue value=(MaterialValue)alliance.getMaterialValue().get(player.getId());
		if(value!=null)
			buffer.append(value.getReocrd());
		else 
			buffer.append("0,0");
		DonateRank rank=(DonateRank)alliance.getGiveValue().get(player.getId());
		if(rank!=null)
			buffer.append(rank.getGiveRecord());
		else 
			buffer.append(",0,0,0,0,0,0,0");
		player.setAttribute(PublicConst.ALLIANCE_GIVE_VALUES,buffer.toString());
		
	}
	/** 获取军官对舰船属性的加成,不享受星石加成,isFix是否获取为具体值而非百分比 */
	public static double getOfficerAttribute(int attrType,Fleet fleet)
	{
		Ship sp=(Ship)Ship.factory.getSample(fleet.getShip().getSid());
		float value=0;
		// 属性本身是百分比
		boolean isBasePercentage=true;
		switch(attrType)
		{
			case PublicConst.ATTACK:
				value=sp.getAttack();
				isBasePercentage=false;
				break;
			case PublicConst.DEFENCE:
				value=sp.getDefence();
				isBasePercentage=false;
				break;
			case PublicConst.SHIP_HP:
				value=sp.getLife();
				isBasePercentage=false;
				break;
		}
		double fix=fleet.getFleetAttrAdjustment(attrType,true);
		double percent=fleet.getFleetAttrAdjustment(attrType,false);
		if(isBasePercentage)
			return percent;
		else
			return value*percent/100+fix;

	}
	
	/** 获取军需乐透免费抽奖的倒计时 */
	public static int getFreeLottoTime(Player player,int time)
	{
		String lastLotto=player.getAttributes(PublicConst.LAST_FREE_LOTTO);
		int lastTime=lastLotto==null?0:TextKit.parseInt(lastLotto);
		int nextTime=lastTime+PublicConst.LOTTO_FREE_CIRCLE;
		return nextTime-time>0?nextTime-time:0;
	}
	
	/**移除当前玩家在联盟物资和捐献排行榜**/
	public static void removeAllianceRank(Player player,Alliance alliance)
	{
		alliance.removeMaterialRank(player.getId());
		alliance.removeGiveRank(player.getId());
		int rank1=0;
		// 职务
		if(player.getId()==alliance.getMasterPlayerId())
		{
			rank1=Alliance.MILITARY_RANK3;
		}
		for(int j=0;j<alliance.getVicePlayers().size();j++)
		{
			if(player.getId()==alliance.getVicePlayers().get(j))
			{
				rank1=Alliance.MILITARY_RANK2;
				break;
			}
		}
		JBackKit.sendPlayerMaterialRank(alliance.getPlayerList(),cbFactory,player,MaterialValue.DELETE,rank1);
	}
	
	/** 获取玩家当前的充值设备是否可用 */
	public static boolean isPaymentDeviceAvailable(Player player,
		String device,int max)
	{
		String[] infos=getPlayerPaymentDevices(player);
		if(infos==null)
			return true;
		else
		{
			int i=0;
			for(;i<infos.length;i++)
			{
				if(infos[i].equals(device)) return true;
			}
			if(i>=infos.length&&infos.length<max) return true;
		}
		return false;
	}
	
	/** 获取玩家在用的充值设备信息 */
	public static String[] getPlayerPaymentDevices(Player player)
	{
		String info=player.getAttributes(PublicConst.PAYMENT_DEVICES);
		if(info==null||"".equals(info)) return null;
		return TextKit.split(info,",");
	}
	
	/** 设置玩家的充值设备信息,此处不做最大数量检测 */
	public static void setPlayerPaymentDevices(Player player,String device)
	{
		String[] infos=getPlayerPaymentDevices(player);
		if(infos==null)
			player.setAttribute(PublicConst.PAYMENT_DEVICES,device);
		else
		{
			int i=0;
			for(;i<infos.length;i++)
			{
				if(infos[i].equals(device)) break;
			}
			if(i>=infos.length)
				player.setAttribute(PublicConst.PAYMENT_DEVICES,device+","
					+player.getAttributes(PublicConst.PAYMENT_DEVICES));
		}
	}
	
	/**获取玩家报名联盟战的船只数据**/
	public static  IntList  getAlliancePlayerShips(Player player,boolean bool,int allianceId)
	{
		if(!bool) return null;
		Alliance alliance =cbFactory.getAlliance(allianceId,false);
		if(alliance==null) return null;
		if(alliance.getBetBattleIsland()==0) return null;
		AllianceBattleFight fight=cbFactory.getBattleFight();
		if(fight.getAllianceStage().getStage()<=Stage.STAGE_TWO)
			return null;
		BattleIsland island=fight.getBattleIslandById(alliance.getBetBattleIsland(),false);
		if(island==null) return null;
		if(!island.isHavePlayer(player.getId()))
			return null;
		//获取当前玩家船只信息
		return island.getPlayerFight(player.getId()).getList();
	}
	
	/**模糊相应位置的字符**/
	public static String blurString(String info,int start,int end,char value)
	{
		char[] chars=info.toCharArray();
		for(int i=0;i<chars.length;i++)
		{
			if(i>=start&&i<=end)
				chars[i]=value;
		}
		return new String(chars);
	}
	/**舰队返航**/
	public static String shipReturnBack(FightEvent back,Player player)
	{
		if(back==null) return "event is null";
		if(back.getPlayerId()!=player.getId())
		{
			// 找到玩家岛屿
			int islandId=cbFactory.getIslandCache()
				.getPlayerIsLandId(player.getId());
			ArrayList fightEventList=cbFactory.getEventCache()
				.getFightEventListById(islandId);
			if(fightEventList!=null)
			{
				fightEventList.remove(back);
				// 刷新被驻守方的事件
				JBackKit.deleteFightEvent(player,back);
			}
			return "event is not yours";
		}
		// 新手引导部分特殊操作
		if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
			back.setEventState(FightEvent.HOLD_ON);
		if(back.getEventState()!=FightEvent.HOLD_ON)
			return "eventState is wrong";
		// 设置状态
		back.setEventState(FightEvent.RETRUN_BACK);
		NpcIsland beIsland=cbFactory.getIslandCache().load(
			back.getAttackIslandIndex()+"");
		// 新手引导部分特殊操作
		if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
			beIsland=(NpcIsland)NpcIsland.factory
				.getSample(FightPort.NEW_PLAYER_ATT_ISLAND);
		// 计算时间
		NpcIsland island=cbFactory.getIslandCache().getPlayerIsland(
			player.getId());
		int needTime=FightKit.needTime(island.getIndex(),
			back.getAttackIslandIndex());
		int nowTime=TimeKit.getSecondTime();
		if(beIsland.getPlayerId()==0)
		{
			// 结算资源
			if(beIsland.getIslandType()==NpcIsland.ISLAND_GEMS && beIsland.getEndTime()<nowTime)
				beIsland.setResource(player,back.getResources(),
					(beIsland.getEndTime()-back.getCreatAt())/60,back.getFleetGroup(),0);
			else
				beIsland.setResource(player,back.getResources(),
					(nowTime-back.getCreatAt())/60,back.getFleetGroup(),0);
		}
		else
		{
			Player beholdPlayer=cbFactory.getPlayerById(beIsland
				.getPlayerId());
			int beeventId=0;
			if(beholdPlayer
				.getAttributes(PublicConst.ALLIANCE_DEFND_ATT)!=null
				&&!beholdPlayer.getAttributes(
					PublicConst.ALLIANCE_DEFND_ATT).equals(""))
			{
				beeventId=Integer.parseInt(beholdPlayer
					.getAttributes(PublicConst.ALLIANCE_DEFND_ATT));
			}
			if(beeventId==back.getId())
			{
				beholdPlayer.setAttribute(
					PublicConst.ALLIANCE_DEFND_ATT,null);
			}
			// 刷新被驻守方的事件
			JBackKit.deleteFightEvent(beholdPlayer,back);
		}
		// 清楚临时事件
		beIsland.setTempAttackEventId(0);
		// 设置时间
		back.setCreatAt(nowTime);
		// 设置需要时间
		back.setNeedTime(needTime,player,nowTime);
		if(player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER)!=null)
			back.setNeedTimeDB(FightPort.NEW_PLAYER_TRAVEL_TIME);
		JBackKit.sendFightEvent(player,back,cbFactory);
		// 检查自己的事件
		cbFactory.getEventCache().removeHoldOnEvent(
			island.getIndex(),beIsland.getIndex(),player);
		//
		JBackKit.sendMarchLine(cbFactory,back);
		return null;
	}
	
	/** 将arr2合并至arr1的末尾 */
	public static int[] assembleIntArrays(int[] arr1,int[] arr2)
	{
		if(arr2==null||arr2.length<=0) return arr1;
		if(arr1==null||arr1.length<=0) return arr2;
		int[] arr=new int[arr1.length+arr2.length];
		System.arraycopy(arr1,0,arr,0,arr1.length);
		System.arraycopy(arr2,0,arr,arr1.length,arr2.length);
		return arr;
	}
	
	/** 获取舰队群采集速度加成部分（减去基础值） */
	public static int groupResourceExtraAddition(Player player,FleetGroup gourp,int type)
	{
		return groupResourceAddition(player,gourp,type)-PublicConst.AWARD_TOTAL_LENGTH;
	}
	
	/** 获取舰队群采集速度加成 */
	public static int groupResourceAddition(Player player,FleetGroup gourp,int type)
	{
		if(type==NpcIsland.ISLAND_GEMS) return PublicConst.AWARD_TOTAL_LENGTH;
		Fleet fleet[]=gourp.getArray();
		IntKeyHashMap ships=new IntKeyHashMap();
		IntKeyHashMap addtions=new IntKeyHashMap();
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null
				||!(fleet[i].getShip() instanceof TransportShip)) continue;
			// 船只数量累计
			int temp=0;
			Integer single=(Integer)ships.get(fleet[i].getShip().getSid());
			if(single!=null)
				temp=single;
			temp+=fleet[i].getNum();
			ships.put(fleet[i].getShip().getSid(),temp);
			// 加成计算
			TransportShip ts=(TransportShip)fleet[i].getShip();
			temp=temp/ts.getCircleThreshold()
				*ts.getBaseAddition();
			addtions.put(fleet[i].getShip().getSid(),temp);
		}
		int add=PublicConst.AWARD_TOTAL_LENGTH;
		int[] keys=addtions.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			int sa=PublicConst.AWARD_TOTAL_LENGTH;
			if(addtions.get(keys[i])!=null&&(Integer)addtions.get(keys[i])<sa)
				sa=(Integer)addtions.get(keys[i]);
			add+=sa;
		}
		return add;
	}
	
	/**增加和平镇时间**/
	public static void addPeaceCover(int time,Service service)
	{
		IntKeyHashMap map=cbFactory.getPlayerCache().getCacheMap();
		IntKeyHashMap changMap=cbFactory.getPlayerCache().getChangeListMap();
		if(map==null||map.size()==0) return;
		service.setServiceTime(time);
		Object[] objects=map.valueArray();
		for(int i=0;i<objects.length;i++)
		{
			if(objects[i]==null) continue;
			PlayerSave save=(PlayerSave)objects[i];
			if(save==null) continue;
			Player player=save.getData();
			if(player==null) continue;
			player.addService(service,TimeKit.getSecondTime());
			JBackKit.sendResetService(player);
			changMap.put(save.getId(),save);
		}
	}
	
	/**减少和平镇时间**/
	public static void reduceService(int time,Service service)
	{
		if(time<=0) return ;
		IntKeyHashMap map=cbFactory.getPlayerCache().getCacheMap();
		IntKeyHashMap changMap=cbFactory.getPlayerCache().getChangeListMap();
		if(map==null||map.size()==0) return;
		Object[] objects=map.valueArray();
		for(int i=0;i<objects.length;i++)
		{
			if(objects[i]==null) continue;
			PlayerSave save=(PlayerSave)objects[i];
			if(save==null) continue;
			Player player=save.getData();
			if(player==null) continue;
			player.reduceService(service.getServiceType(),time);
			JBackKit.sendResetService(player);
			changMap.put(save.getId(),save);
		}
	}
	
	/**获取buff**/
	public  static void getBuff(IntList list,ByteBuffer data)
	{
		if(list!=null&&list.size()!=0)
		{
			data.writeByte(list.size());
			for(int i=0;i<list.size();i++)
			{
				data.writeShort(list.get(i));
			}
		}
		else
			data.writeByte(0);
	}

	/** 检测 **/
	public static String checkFlagSid(int image_sid,int clour_sid,int modle_sid)
	{
		Flag flagig=(Flag)Flag.factory.getSample(image_sid);
		Flag flagcl=(Flag)Flag.factory.getSample(clour_sid);
		Flag flagmo=(Flag)Flag.factory.getSample(modle_sid);
		if(flagig==null || !SeaBackKit.isContainValue(PublicConst.IMAGE,image_sid)) return "image error";
		if(flagcl==null || !SeaBackKit.isContainValue(PublicConst.COLOUR,clour_sid)) return "colour error";
		if(flagmo==null || !SeaBackKit.isContainValue(PublicConst.MODEL,modle_sid)) return "model error";
		return null;
	}
	
	/** 设置系统分隔符 **/
	public static String setSystemContent(String message,String replace,
		String separators,String playerName)
	{
		message=TextKit.replace(message,replace,separators+playerName+separators);
		return message;
	}
	
	
	/**获取当获得抽奖的记录**/
	public  static String getActivityAwardInfo(ByteBuffer data,int times)
	{
		int offset=data.offset();
		/**活动的sid**/
		data.readUnsignedShort();
		/**奖励品的长度**/
		data.readUnsignedShort();
		/**类型**/
		data.readUnsignedByte();
		int num=data.readInt();
		int sid=data.readUnsignedShort();
		if(times==0)
		{
			data.setOffset(offset);
			return sid+"x"+num;
		}
		else if(times==1)
		{
			data.readUnsignedByte();
			num=data.readInt();
			sid=data.readUnsignedShort();
			data.setOffset(offset);
			return sid+"x"+num;
		}
		else if(times==2)
		{
			data.readUnsignedByte();
			num=data.readInt();
			sid=data.readUnsignedShort();
			data.setOffset(offset);
			return sid+"x"+num;
		}
		return null;
	}
}
