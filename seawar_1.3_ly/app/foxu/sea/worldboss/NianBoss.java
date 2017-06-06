package foxu.sea.worldboss;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.util.Sample;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.comparator.AllianceBossHurtComparator;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;


/**
 * 年兽
 * @author yw
 *
 */
public class NianBoss extends Sample
{
	/** 配置数据长度 */
	public static int dataLength=4;
	/** 空奖励 */
	public static int EMPTY_SID=65051;
	/** 狂暴技能 */
	public static int CRZY_SID=1001;
	/** 是否需要保存 */
	boolean needsave;
	
	int max_beattack=10;
	int xy_boss=30*60;
	/** 联盟排名公告范围 */
	int allRange=3;
	/** 被攻击cd */
	int beAttackCD=5;
	
	/** boss等级 */
	int bossLevel;
	/** {shipSid,num,index,range}4位一组(range暂时不用，可用于随机数量) */
	int[] fleetGroupCfg;
	/** 出生时间   */
	int createTime;
	/** 被攻击次数 */
	int beAttack;
	/** 当前随机的岛屿index */
	int index;
	
	/** 攻击奖励  sid:psid,num... */
	String attackAwardSid;
	/** 击杀奖励品sid sid:psid,num... */
	String killAwardSid;
	/** 盟排名奖励  rank-rank:sid:psid,num...| */
	String awards_a;
	/** 玩家排名奖励 rank-rank:sid:psid,num...| */
	String awards_p;
	
	/** boss船只 */
	FleetGroup fleetGroup=new FleetGroup();
	
	/** 联盟伤害排名 联盟id，击杀船只数量 */
	IntKeyHashMap hurtList_a=new IntKeyHashMap();
	/** 个人伤害排名 玩家id，击杀船只数量 */
	IntKeyHashMap hurtList_p=new IntKeyHashMap();
	
	/** 攻击奖励*/
	Award attack_award;
	
	/** boss初始化 */
	public void initBoss(String initdata)
	{
		String[] initdatas=TextKit.split(initdata,"&");
		beAttackCD=TextKit.parseInt(initdatas[0]);
		attackAwardSid=initdatas[1];
		killAwardSid=initdatas[2];
		awards_a=initdatas[3];
		awards_p=initdatas[4];
		attack_award=parseAward(attackAwardSid);
	}
	
	/** 活动到期  boss逃离 */
	public void away(CreatObjectFactory factory)
	{
		// 移除boss岛屿
		NpcIsland beIsland=factory.getIslandByIndex(index+"");
		beIsland.updateSid(FightKit.WATER_ISLAND_SID);
		// 刷新前台
		JBackKit.flushIsland(factory.getDsmanager(),beIsland,factory);
		// boss逃离
		bekilled();
		
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"nian away");
		message=TextKit.replace(message,"%",getBossLevel()+"");
		// 系统公告
		SeaBackKit.sendSystemMsg(factory.getDsmanager(),message);
	}
	
	/** boss自爆坐标 */
	public boolean showXY(CreatObjectFactory factory)
	{
		if(index>0
			&&(beAttack>=max_beattack||TimeKit.getSecondTime()-createTime>xy_boss))
		{
			String message=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"nian showxy");
			int x=index%600+1;
			int y=index/600+1;
			message=TextKit.replace(message,"%",x+"");
			message=TextKit.replace(message,"%",y+"");
			// 系统公告
			SeaBackKit.sendSystemMsg(factory.getDsmanager(),message);
			return true;
		}
		return false;
	}
	
	/** 增加被攻击次数 */
	public void incrBeAttack()
	{
		beAttack++;
		setNeedsave(true);
	}
	
	/** 发放击杀奖励 */
	public void sendKillAward(Player player,CreatObjectFactory factory)
	{
		Award award=parseAward(killAwardSid);
		if(award!=null)
		{
			award.awardSelf(player,0,null,factory,null,null);
			
			String sendName=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"system_mail");
			String title=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"kill nian title");
			String content=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"kill nian content");
			int[] propSid=award.getPropSid();
			StringBuffer propinfo=new StringBuffer();
			for(int i=0;i<propSid.length;i+=2)
			{
				String pname=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"prop_sid_"+propSid[i]);
				propinfo.append(" "+pname+"*"+propSid[i+1]);
			}
			content=TextKit.replace(content,"%",propinfo.toString());
			Message mess=factory.createMessage(0,player.getId(),
				content,sendName,player.getName(),0,title,true);
			// 刷新前台
			JBackKit.sendRevicePlayerMessage(player,mess,
				mess.getRecive_state(),factory);
		}

	}
	/** 发放联盟排名奖励 */
	public void sendAllRankAward(CreatObjectFactory factory)
	{
		Object[] rank_awards=pargeRankAward(awards_a);
		Object objs[]=getHurtRank(factory,hurtList_a);
		
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"kill nian rank_a");
		String content=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"kill nian rank_a content");
		content=TextKit.replace(content,"%",getBossLevel()+"");
		for(int i=0;i<rank_awards.length;i+=3)
		{
			Award award=(Award)rank_awards[i+2];
			
			int[] propSid=award.getPropSid();
			StringBuffer propinfo=new StringBuffer();
			for(int n=0;n<propSid.length;n+=2)
			{
				String pname=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"prop_sid_"+propSid[n]);
				propinfo.append(" "+pname+"*"+propSid[n+1]);
			}
			for(int k=(Integer)rank_awards[i]-1;k<=(Integer)rank_awards[i+1]-1;k++)
			{
				if(k>=objs.length) break;
				BossHurt bosshurt=(BossHurt)objs[k];
				Alliance alliance=(Alliance)factory.getAllianceMemCache()
					.loadOnly(bosshurt.getId()+"");
				if(alliance==null) continue;
				String sendContent=TextKit.replace(content,"%",(k+1)+"");
				sendContent=TextKit.replace(sendContent,"%",propinfo.toString());
				IntList list=alliance.getPlayerList();
				for(int m=0;m<list.size();m++)
				{
					Player player=factory.getPlayerById(list.get(m));
					if(player==null) continue;
					award.awardSelf(player,0,null,factory,null,null);
					
					Message mess=factory.createMessage(0,player.getId(),
						sendContent,sendName,player.getName(),0,
						title,true);
					// 刷新前台
					JBackKit.sendRevicePlayerMessage(player,mess,
						mess.getRecive_state(),factory);
				}
			}

		}

	}
	/** 发放个人排名奖励 */
	public void sendPlayerRankAward(CreatObjectFactory factory)
	{
		Object[] rank_awards=pargeRankAward(awards_p);
		Object objs[]=getHurtRank(factory,hurtList_p);
		
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"kill nian rank_p");
		String content=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"kill nian rank_p content");
		content=TextKit.replace(content,"%",getBossLevel()+"");
		for(int i=0;i<rank_awards.length;i+=3)
		{
			Award award=(Award)rank_awards[i+2];
			
			int[] propSid=award.getPropSid();
			StringBuffer propinfo=new StringBuffer();
			for(int n=0;n<propSid.length;n+=2)
			{
				String pname=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"prop_sid_"+propSid[n]);
				propinfo.append(" "+pname+"*"+propSid[n+1]);
			}
			for(int k=(Integer)rank_awards[i]-1;k<=(Integer)rank_awards[i+1]-1;k++)
			{
				if(k>=objs.length) break;
				BossHurt bosshurt=(BossHurt)objs[k];
				Player player=factory.getPlayerById(bosshurt.getId());
				if(player==null) continue;
				String sendContent=TextKit.replace(content,"%",(k+1)+"");
				sendContent=TextKit.replace(sendContent,"%",propinfo.toString());
				award.awardSelf(player,0,null,factory,null,null);
				Message mess=factory.createMessage(0,player.getId(),
					sendContent,sendName,player.getName(),0,title,
					true);
				// 刷新前台
				JBackKit.sendRevicePlayerMessage(player,mess,
					mess.getRecive_state(),factory);

			}

		}
		
		
	}
	
	/** 解析排名奖励 */
	public Object[] pargeRankAward(String award_txt)
	{
		//rank-rank:psid,num...|
		String[] str_awards=TextKit.split(award_txt,"|");
		Object[] awards=new Object[str_awards.length*3];
		for(int i=0;i<str_awards.length;i++)
		{
			String[] str_award=TextKit.split(str_awards[i],":");
			String[] rank=TextKit.split(str_award[0],"-");
			awards[i*3]=TextKit.parseInt(rank[0]);
			awards[i*3+1]=TextKit.parseInt(rank[1]);
			awards[i*3+2]=parseAward(str_awards[i].substring(str_award[0]
				.length()+1));
		}
		return awards;
	}
	
	/** 解析单个奖励 */
	public Award parseAward(String award_txt)
	{
		String[] sids=TextKit.split(award_txt,",");
		Award award=(Award)Award.factory
			.newSample(EMPTY_SID);
		int[] propsid=new int[sids.length];
		for(int i=0;i<sids.length;i++)
		{
			propsid[i]=TextKit.parseInt(sids[i]);
		}
		SeaBackKit.resetAward(award,propsid);
		return award;

	}
	
	/** 获得初始最大船只数量 */
	public int getFleetMaxNum()
	{
		return fleetGroup.getMaxNum();
	}

	/** 获得当前的船只数量 */
	public int getFleetNowNum()
	{
		return fleetGroup.nowTotalNum();
	}

	/** 重置当前击毁数量 */
	public void resetLostNum()
	{
		fleetGroup.resetLastNum();
	}

	/** 本次攻击销毁船只数量 */
	public int lostNum()
	{
		return fleetGroup.hurtListNum();
	}

	/** 获得某个联盟的击毁船只数量 */
	public int getAttackNum(int id,IntKeyHashMap hurtlist)
	{
		int attackNum=0;
		BossHurt bosshurt=(BossHurt)hurtlist.get(id);
		if(bosshurt!=null)
		{
			attackNum=bosshurt.getHurtNum();
		}
		return attackNum;
	}
	
	/** 为某个id添加伤害船只数量 */
	public void addLostNum(int id,int num,boolean isplayer)
	{
		if(isplayer)
		{
			addLostNum(hurtList_p,id,num);
		}
		else
		{
			addLostNum(hurtList_a,id,num);
		}
		setNeedsave(true);
	}

	/** 为某个id添加伤害船只数量 */
	public void addLostNum(IntKeyHashMap hurtList,int id,int num)
	{
		if(hurtList.get(id)!=null&&!hurtList.get(id).equals(""))
		{
			BossHurt bosshurt=(BossHurt)hurtList.get(id);
			int attackNum=bosshurt.getHurtNum();
			bosshurt.setHurtNum(attackNum+num);
			return;
		}
		BossHurt bosshurt=new BossHurt();
		bosshurt.setId(id);
		bosshurt.setHurtNum(num);
		hurtList.put(id,bosshurt);
	}

	/** 获取伤害排名  */
	public Object[] getHurtRank(CreatObjectFactory objectFactory,IntKeyHashMap hurtList)
	{
		Object objects[]=hurtList.valueArray();
		SetKit.sort(objects,AllianceBossHurtComparator.getInstance());
		return objects;
	}

	/** 找到某个联盟的排名 */
	public int sortNum(int id,IntKeyHashMap hurtlist)
	{
		Object objects[]=hurtlist.valueArray();
		SetKit.sort(objects,AllianceBossHurtComparator.getInstance());
		for(int i=0;i<objects.length;i++)
		{
			BossHurt bosshurt=(BossHurt)objects[i];
			if(bosshurt.getId()==id)
			{
				return i+1;
			}
		}
		return 0;
	}

	public FleetGroup getFleetGroup()
	{
		if(fleetGroup==null) return createFleetGroup();
		return fleetGroup;
	}

	public FleetGroup createFleetGroup()
	{
		fleetGroup=new FleetGroup();
		for(int i=0;i<fleetGroupCfg.length;i+=dataLength)
		{
			Fleet fleet=new Fleet((Ship)Ship.factory
				.newSample(fleetGroupCfg[i]),fleetGroupCfg[i+1]);
			fleetGroup.setFleet(fleetGroupCfg[i+2],fleet);
		}
		return fleetGroup;
	}

	/**
	 * 开始关卡战斗
	 * 
	 * @param attacker 进攻方
	 */
	public Object[] fight(FleetGroup attacker)
	{
		Ability[] ab=new Ability[1];
		ab[0]=(Ability)FightScene.abilityFactory.newSample(CRZY_SID);
		FightScene scene=FightSceneFactory.factory.create(attacker,null,
			getFleetGroup(),ab);
		FightShowEventRecord r=FightSceneFactory.factory.fight(scene,null);
		Object[] object=new Object[2];
		object[0]=scene;
		object[1]=r;
//		beAttack++;
		return object;
	}

	public void bytesWirteFleetGroup(ByteBuffer data)
	{
		fleetGroup.bytesWrite(data);
	}

	public void bytesReadFleetGroup(ByteBuffer data)
	{
		fleetGroup.bytesRead(data);
	}

	public void bytesWriteHurtList(ByteBuffer data,IntKeyHashMap hurtList)
	{
		Object object[]=hurtList.valueArray();
		if(object.length<=0)
		{
			data.writeShort(0);
		}
		else
		{
			data.writeShort(object.length);
			for(int i=0;i<object.length;i++)
			{
				BossHurt hurtValue=(BossHurt)object[i];
				data.writeInt(hurtValue.getId());
				data.writeShort(hurtValue.getHurtNum());
			}
		}
	}

	public void bytesReadHurtList(ByteBuffer data,IntKeyHashMap hurtList)
	{
		int size=data.readUnsignedShort();
		for(int i=0;i<size;i++)
		{
			int allianceId=data.readInt();
			int hurtNum=data.readUnsignedShort();
			BossHurt hurtValue=new BossHurt();
			hurtValue.setId(allianceId);
			hurtValue.setHurtNum(hurtNum);
			hurtList.put(allianceId,hurtValue);
		}
	}

	/** 被击杀 */
	public void bekilled()
	{
		index=0;
		getHurtList_a().clear();
		getHurtList_p().clear();
		setNeedsave(true);
	}
	
	/** 获取BOSS信息 */
	public void getBossInfo(ByteBuffer data)
	{
		data.clear();
		data.writeByte(1);
		data.writeShort(getSid());
		if(beAttack>=max_beattack||TimeKit.getSecondTime()-createTime>xy_boss)
		{
			data.writeByte(2);
		}
		else
		{
			data.writeByte(0);
		}
		data.writeShort(getIndex()%600);
		data.writeShort(getIndex()/600);
	}

	public Object copy(Object obj)
	{
		NianBoss boss=(NianBoss)super.copy(obj);
		boss.fleetGroup=new FleetGroup();
		boss.hurtList_a=new IntKeyHashMap();
		boss.hurtList_p=new IntKeyHashMap();
		return boss;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(beAttackCD);
		data.writeInt(createTime);
		data.writeInt(beAttack);
		data.writeInt(index);
		data.writeUTF(attackAwardSid);
		data.writeUTF(killAwardSid);
		data.writeUTF(awards_a);
		data.writeUTF(awards_p);
		bytesWirteFleetGroup(data);
		bytesWriteHurtList(data,hurtList_a);
		bytesWriteHurtList(data,hurtList_p);
	}

	public Object bytesRead(ByteBuffer data)
	{
		beAttackCD=data.readInt();
		createTime=data.readInt();
		beAttack=data.readInt();
		index=data.readInt();
		attackAwardSid=data.readUTF();
		killAwardSid=data.readUTF();
		awards_a=data.readUTF();
		awards_p=data.readUTF();
		bytesReadFleetGroup(data);
		bytesReadHurtList(data,hurtList_a);
		bytesReadHurtList(data,hurtList_p);
		
		attack_award=parseAward(attackAwardSid);
		return this;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index=index;
	}

	public int getBeAttack()
	{
		return beAttack;
	}

	public void setBeAttack(int beAttack)
	{
		this.beAttack=beAttack;
	}

	
	public String getAttackAwardSid()
	{
		return attackAwardSid;
	}

	
	public void setAttackAwardSid(String attackAwardSid)
	{
		this.attackAwardSid=attackAwardSid;
	}

	
	public String getKillAwardSid()
	{
		return killAwardSid;
	}
	
	public void setKillAwardSid(String killAwardSid)
	{
		this.killAwardSid=killAwardSid;
	}

	
	public String getAwards_a()
	{
		return awards_a;
	}

	
	public void setAwards_a(String awards_a)
	{
		this.awards_a=awards_a;
	}

	
	public String getAwards_p()
	{
		return awards_p;
	}

	
	public void setAwards_p(String awards_p)
	{
		this.awards_p=awards_p;
	}

	
	public IntKeyHashMap getHurtList_a()
	{
		return hurtList_a;
	}

	
	public IntKeyHashMap getHurtList_p()
	{
		return hurtList_p;
	}

	
	public Award getAttack_award()
	{
		return attack_award;
	}

	
	public int getBossLevel()
	{
		return bossLevel;
	}

	
	public void setBossLevel(int bossLevel)
	{
		this.bossLevel=bossLevel;
	}

	
	public int getBeAttackCD()
	{
		return beAttackCD;
	}

	
	public void setBeAttackCD(int beAttackCD)
	{
		this.beAttackCD=beAttackCD;
	}

	
	public int getCreateTime()
	{
		return createTime;
	}

	
	public void setCreateTime(int createTime)
	{
		this.createTime=createTime;
	}

	
	public int getAllRange()
	{
		return allRange;
	}

	
	public void setAllRange(int allRange)
	{
		this.allRange=allRange;
	}
	
	
	public int getMax_beattack()
	{
		return max_beattack;
	}

	
	public void setMax_beattack(int max_beattack)
	{
		this.max_beattack=max_beattack;
	}

	
	public int getXy_boss()
	{
		return xy_boss;
	}

	
	public void setXy_boss(int xy_boss)
	{
		this.xy_boss=xy_boss;
	}
	
	
	public boolean isNeedsave()
	{
		return needsave;
	}
	
	public void setNeedsave(boolean needsave)
	{
		this.needsave=needsave;
	}

	public String toString()
	{
		StringBuffer sub=new StringBuffer();
		sub.append("boss_sid:"+getSid()+"&");
		sub.append("beAttackCD:"+beAttackCD+"&");
		sub.append("attackAwardSid:"+attackAwardSid+"&");
		sub.append("killAwardSid:"+killAwardSid+"&");
		sub.append("awards_a:"+awards_a+"&");
		sub.append("awards_p:"+awards_p);
		int sx=index%600+1;
		int sy=index/600+1;
		sub.append("index:"+(sx+","+sy));
		return sub.toString();
	}

}
