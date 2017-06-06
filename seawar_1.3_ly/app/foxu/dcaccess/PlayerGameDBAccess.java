package foxu.dcaccess;

import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.LongField;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;
import mustang.orm.Persistence;
import mustang.orm.SqlKit;
import mustang.orm.SqlPersistence;
import mustang.set.AttributeList;
import mustang.text.CharBuffer;
import mustang.text.CharBufferThreadLocal;
import mustang.text.TextKit;
import shelby.dc.GameDBAccess;
import foxu.sea.FriendInfo;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Role;

/**
 * 岛屿加载器 author:icetiger
 */
public class PlayerGameDBAccess extends GameDBAccess
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(PlayerGameDBAccess.class);

	/** 判断用户名是否存在 */
	public boolean isExist(Object key,int id)
	{
		Fields fields=mapping();
		int t=Persistence.EXCEPTION;
		t=getGamePersistence().get(
			FieldKit.create("player_name",(String)key),fields);
		if(t==Persistence.RESULTLESS) return false;
		if(t==Persistence.EXCEPTION) return true;
		return true;
	}

	/** 判断用户id是否存在 */
	public boolean isExistByID(int id)
	{ 
		Fields fields=mapping();
		int t=Persistence.EXCEPTION;
		t=getGamePersistence().get(FieldKit.create("id",id),fields);
		if(t==Persistence.RESULTLESS) return false;
		if(t==Persistence.EXCEPTION) return true;
		return true;
	}

	public Fields[] loadSqls(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			array=SqlKit.querys(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"PlayerGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		return array;
	}

	public Player load(String id)
	{
		// 构造一个空域（包括了player表所有属性）
		Fields fields=mapping();
		// 使用持久器 对指定name找到相应的值赋值给fields
		int t=gamePersistence.get(FieldKit.create("id",id),fields);
		// 出错情况处理
		if(t==Persistence.EXCEPTION||t==Persistence.RESULTLESS)
		{
			log.warn("playerGameDBAccess load fail======id===="+id);
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,"PlayerGameDBAccess db error");
		}
		// 将存有玩家数据的域对象封装成一个玩家对象
		Player player=mapping(fields);
		// ByteBuffer bb=ByteBufferThreadLocal.getByteBuffer();
		// bb.clear();
		// player.bytesWrite(bb);
		return player;
	}
	public Player[] loadBySql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields[] array=null;
		try
		{
			array=SqlKit.querys(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"PlayerGameDBAccess loadBysql valid, db error");
		}
		if(array==null) return null;
		Player[] players=new Player[array.length];
		for(int i=0;i<array.length;i++)
		{
			players[i]=mapping(array[i]);
		}
		return players;
	}

	public Fields loadSql(String sql)
	{
		SqlPersistence sp=(SqlPersistence)getGamePersistence();
		Fields array=null;
		try
		{
			array=SqlKit.query(sp.getConnectionManager(),sql);
		}
		catch(Exception e)
		{
			throw new DataAccessException(
				DataAccessException.SERVER_INTERNAL_ERROR,
				"PlayerGameDBAccess loadBysql valid, db error");
		}
		return array;
	}

	/** 映射成域对象 */
	public Fields mapping()
	{
		FieldObject[] array=new FieldObject[80];
		int i=0;
		// array[i++]=FieldKit.create("immure",0);
		// array[i++]=FieldKit.create("cause",(String)null);
		array[i++]=FieldKit.create("sid",0);
		array[i++]=FieldKit.create("id",0);
		array[i++]=FieldKit.create("player_type",0);
		array[i++]=FieldKit.create("commander_level",0);
		array[i++]=FieldKit.create("level",0);
		array[i++]=FieldKit.create("user_state",0);
		array[i++]=FieldKit.create("bundleSize",0);
		array[i++]=FieldKit.create("user_id",0);
		array[i++]=FieldKit.create("reward",0);
		array[i++]=FieldKit.create("experience",0);
		array[i++]=FieldKit.create("inveted",0);
		array[i++]=FieldKit.create("plunderResource",0l);
		array[i++]=FieldKit.create("fightScore",0);
		array[i++]=FieldKit.create("achieveScore",0);

		array[i++]=FieldKit.create("locale",0);
		array[i++]=FieldKit.create("player_name",(String)null);
		array[i++]=FieldKit.create("udid",(String)null);

		array[i++]=FieldKit.create("create_at",0);
		array[i++]=FieldKit.create("mute_time",0);
		array[i++]=FieldKit.create("update_at",0);
		array[i++]=FieldKit.create("exit_time",0);
		array[i++]=FieldKit.create("save_time",0);
		array[i++]=FieldKit.create("online_time",0);
		array[i++]=FieldKit.create("taskMark",0);
		array[i++]=FieldKit.create("style",0);
		array[i++]=FieldKit.create("honorScore",0);
		array[i++]=FieldKit.create("playerTaskMark",0);
		array[i++]=FieldKit.create("platid",0);
		array[i++]=FieldKit.create("deleteTime",0);
		array[i++]=FieldKit.create("escapeDevice",0);

		array[i++]=FieldKit.create("area",(String)null);
		array[i++]=FieldKit.create("prosperityInfo",(String)null);
		array[i++]=FieldKit.create("actives",(String)null);
		array[i++]=FieldKit.create("inviter_id",(String)null);
		array[i++]=FieldKit.create("resources",(String)null);
		array[i++]=FieldKit.create("honor",(String)null);
		array[i++]=FieldKit.create("attributes",(String)null);
		array[i++]=FieldKit.create("deviceToken",(String)null);

		array[i++]=FieldKit.create("quest",(byte[])null);
		array[i++]=FieldKit.create("achievement",(byte[])null);
		array[i++]=FieldKit.create("build_and_troops",(byte[])null);
		array[i++]=FieldKit.create("bundle",(byte[])null);
		array[i++]=FieldKit.create("service",(byte[])null);
		array[i++]=FieldKit.create("selfCheckPoint",(byte[])null);
		array[i++]=FieldKit.create("tearCheckPoint",(byte[])null);
		array[i++]=FieldKit.create("skills",(byte[])null);
		array[i++]=FieldKit.create("locationSaveList",(byte[])null);
		array[i++]=FieldKit.create("shipLevel",(byte[])null);//加
		array[i++]=FieldKit.create("equips",(byte[])null);
		array[i++]=FieldKit.create("gemsActivity",(byte[])null);
		
		array[i++]=FieldKit.create("heritagePoint",(byte[])null);
		array[i++]=FieldKit.create("createIp",(String)null);
		array[i++]=FieldKit.create("bindIp",(String)null);
		array[i++]=FieldKit.create("loginIp",(String)null);
		
		array[i++]=FieldKit.create("platName",(String)null);
		array[i++]=FieldKit.create("armsCheckPoint",(byte[])null);
		array[i++]=FieldKit.create("produceQueue",(byte[])null);
		array[i++]=FieldKit.create("bundle_id",(String)null);
		array[i++]=FieldKit.create("bakFormation",(byte[])null);
		array[i++]=FieldKit.create("betmap",(byte[])null);
		array[i++]=FieldKit.create("recruit",(byte[])null);
		array[i++]=FieldKit.create("officers",(byte[])null);
		array[i++]=FieldKit.create("comrade",(byte[])null);
		array[i++]=FieldKit.create("annex",(byte[])null);
		array[i++]=FieldKit.create("allianceChest",(byte[])null);
		array[i++]=FieldKit.create("propExchangeNum",(byte[])null);
		array[i++]=FieldKit.create("elite",(byte[])null);
		array[i++]=FieldKit.create("officershop",(byte[])null);
		array[i++]=FieldKit.create("growthplan",(byte[])null);
		array[i++]=FieldKit.create("pointBuff",(byte[])null);
		array[i++]=FieldKit.create("friendInfo",(byte[])null);
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

	/** 映射成域对象 */
	public Player mapping(Fields fields)
	{
		int sampleId=((IntField)fields.get("sid")).value;
		Player p=(Player)Role.factory.newSample(sampleId);
		// p.init(name);
		int id=((IntField)fields.get("id")).value;
		p.bindUid(id);
		p.setId(id);
		int level=((IntField)fields.get("level")).value;
		p.setLevel(level);
		int playerType=((IntField)fields.get("player_type")).value;
		p.setPlayerType(playerType);
		int commanderLevel=((IntField)fields.get("commander_level")).value;
		p.setCommanderLevel(commanderLevel);
		int user_state=((IntField)fields.get("user_state")).value;
		p.setUser_state(user_state);

		int bundleSize=((IntField)fields.get("bundleSize")).value;
		p.setBundleSize(bundleSize);

		int user_id=((IntField)fields.get("user_id")).value;
		p.setUser_id(user_id);
		int reward=((IntField)fields.get("reward")).value;
		p.setReward(reward);
		long experience=((LongField)fields.get("experience")).value;
		p.setExperience(experience);
		int inveted=((IntField)fields.get("inveted")).value;
		p.setInveted(inveted);
		long plunderResource=((LongField)fields.get("plunderResource")).value;
		p.setPlunderResource(plunderResource);
		int fightScore=((IntField)fields.get("fightScore")).value;
		p.setFightScore(fightScore);
		int achieveScore=((IntField)fields.get("achieveScore")).value;
		p.setAchieveScore(achieveScore);
		

		int locale=((IntField)fields.get("locale")).value;
		p.setLocale(locale);
		String player_name=((StringField)fields.get("player_name")).value;
		p.setName(player_name);
		String udid=((StringField)fields.get("udid")).value;
		p.setUdid(udid);

		String deviceToken=((StringField)fields.get("deviceToken")).value;
		p.setDeviceToken(deviceToken);

		int createdAt=((IntField)fields.get("create_at")).value;
		p.setCreateTime(createdAt);
		int muteTime=((IntField)fields.get("mute_time")).value;
		p.setMuteTime(muteTime);
		int updateAt=((IntField)fields.get("update_at")).value;
		p.setUpdateTime(updateAt);
		int exitTime=((IntField)fields.get("exit_time")).value;
		p.setExitTime(exitTime);
		int saveTime=((IntField)fields.get("save_time")).value;
		p.setSaveTime(saveTime);
		int onlineTime=((IntField)fields.get("online_time")).value;
		p.setOnlineTime(onlineTime);

		int taskMark=((IntField)fields.get("taskMark")).value;
		p.setTaskMark(taskMark);

		int style=((IntField)fields.get("style")).value;
		p.setStyle(style);

		int honorScore=((IntField)fields.get("honorScore")).value;
		p.setHonorScore(honorScore);
		
		int playerTaskMark=((IntField)fields.get("playerTaskMark")).value;
		p.setPlayerTaskMark(playerTaskMark);
		
		int platid=((IntField)fields.get("platid")).value;
		p.setPlatid(platid);

		int deleteTime=((IntField)fields.get("deleteTime")).value;
		p.setDeleteTime(deleteTime);
		
		int escapeDevice=((IntField)fields.get("escapeDevice")).value;
		p.setEscapeDevice(escapeDevice);
		
		String str=((StringField)fields.get("area")).value;
		if(str!=null)
		{
			p.setArea(str);
		}
		
		
		str=((StringField)fields.get("actives")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,":");
			int[] actives=new int[strs.length];
			for(int i=0;i<strs.length;i++)
				actives[i]=Integer.parseInt(strs[i]);
			p.setActives(actives);
		}

		str=((StringField)fields.get("resources")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,":");
			long[] resources=new long[9];
			for(int i=0;i<strs.length;i++)
				resources[i]=TextKit.parseLong(strs[i]);
			p.setResources(resources);
		}

		str=((StringField)fields.get("honor")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,":");
			int[] honor=new int[strs.length];
			for(int i=0;i<strs.length;i++)
				honor[i]=Integer.parseInt(strs[i]);
			p.setHonor(honor);
		}

		str=((StringField)fields.get("inviter_id")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,":");
			int[] inviter_id=new int[strs.length];
			for(int i=0;i<strs.length;i++)
			{
				inviter_id[i]=Integer.parseInt(strs[i]);
			}
			p.setInviter_id(inviter_id);
		}
		
		//*************************************
		//*************attr中含有序列化改变的标记，需要提前
		CharBuffer cb=CharBufferThreadLocal.getCharBuffer();
		cb.clear();
		
		str=((StringField)fields.get("attributes")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,':');
			for(int i=0;i<strs.length;i++)
			{
				cb.clear();
				if(SHARP2.decode(strs[i],cb)) strs[i]=cb.getString();
			}
			p.setAttributes(new AttributeList(strs));
		}
		
		byte[] array=((ByteArrayField)fields.get("quest")).value;
		if(array!=null&&array.length>0)
			p.bytesReadQuest(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("achievement")).value;
		if(array!=null&&array.length>0)
			p.bytesReadAchieve(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("build_and_troops")).value;
		if(array!=null&&array.length>0)
			p.bytesReadBulids(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("bundle")).value;
		if(array!=null&&array.length>0)
			p.bytesReadBundle(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("service")).value;
		if(array!=null&&array.length>0)
			p.bytesReadServices(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("selfCheckPoint")).value;
		if(array!=null&&array.length>0)
			p.bytesReadSelfCheckPoint(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("tearCheckPoint")).value;
		if(array!=null&&array.length>0)
			p.bytesReadTearCheckPoint(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("skills")).value;
		if(array!=null&&array.length>0)
			p.bytesReadSkills(new ByteBuffer(array));

		array=((ByteArrayField)fields.get("locationSaveList")).value;
		if(array!=null&&array.length>0)
			p.bytesReadLocaitonSave(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("shipLevel")).value;
		if(array!=null&&array.length>0)
			p.bytesReadShipLevel(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("heritagePoint")).value;
		if(array!=null&&array.length>0)
			p.bytesReadHeritageCityPoint(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("equips")).value;
		if(array!=null&&array.length>0)
			p.bytesReadEquips(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("gemsActivity")).value;
		if(array!=null&&array.length>0)
			p.bytesReadConsumeGemsData(new ByteBuffer(array));
		
		str=((StringField)fields.get("createIp")).value;
		p.setCreateIp(str);
		str=((StringField)fields.get("bindIp")).value;
		p.setBindIp(str);
		str=((StringField)fields.get("loginIp")).value;
		p.setLoginIp(str);
		str=((StringField)fields.get("platName")).value;
		p.setPlat(str);
		
		array=((ByteArrayField)fields.get("armsCheckPoint")).value;
		if(array!=null&&array.length>0)
			p.bytesReadArmsRoutePoint(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("produceQueue")).value;
		if(array!=null&&array.length>0)
			p.bytesReadProduceQueue(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("bakFormation")).value;
		if(array!=null&&array.length>0)
			p.bytesReadFormations(new ByteBuffer(array));
		str=((StringField)fields.get("bundle_id")).value;
		p.setBundleId(str);
		array=((ByteArrayField)fields.get("betmap")).value;
		if(array!=null&&array.length>0)
			p.bytesReadBetmap(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("recruit")).value;
		if(array!=null&&array.length>0)
			p.bytesReadRecruit(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("officers")).value;
		if(array!=null&&array.length>0)
			p.bytesReadOfficers(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("comrade")).value;
		if(array!=null&&array.length>0)
			p.bytesReadComrade(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("annex")).value;
		if(array!=null&&array.length>0)
			p.bytesReadAnnex(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("allianceChest")).value;
		if(array!=null&&array.length>0)
			p.bytesReadAllianceChest(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("propExchangeNum")).value;
		if(array!=null&&array.length>0)
			p.bytesReadPropExchangeNum(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("elite")).value;
		if(array!=null&&array.length>0)
			p.bytesReadElitePoint(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("officershop")).value;
		if(array!=null&&array.length>0)
			p.bytesReadOffcerShop(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("growthplan")).value;
		if(array!=null&&array.length>0)
			p.bytesReadGrowthPlan(new ByteBuffer(array));
		
		//繁荣度信息
		str=((StringField)fields.get("prosperityInfo")).value;
		if(str!=null&&str.length()>0)
		{
			String[] strs=TextKit.split(str,":");
			int[] prosperityInfo = new int[strs.length];
			for(int i=0;i<strs.length;i++)
				prosperityInfo[i]=Integer.parseInt(strs[i]);
			p.setProsperityInfo(prosperityInfo);
		}else{
			int[] prosperityInfo = new int[4];// 繁荣度相关信息   指数、checkTime、繁荣度max值、繁荣度等级
			p.setProsperityInfo(prosperityInfo);
		}
		
		array=((ByteArrayField)fields.get("pointBuff")).value;
		if(array!=null&&array.length>0)
			p.bytesReadPointBuff(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("friendInfo")).value;
		if(array!=null&&array.length>0)
			p.byteReadFriendInfo(new ByteBuffer(array));
		
		return p;
	}
	/** 保存方法 使用包含玩家数据的字节数组初始化指定姓名的玩家并保存 */
	public boolean save(int id,Object player)
	{
		if(log.isInfoEnabled())
			log.info("save, id="+id+", data="+player.toString());
		try
		{
			// int offset=data.offset();
			// Player player=(Player)Role.bytesReadRole(data);
			// data.setOffset(offset);
			return save(player);
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("save error, id="+id,e);
			return false;
		}
	}
	/** 保存方法 返回是否操作成功 */
	public boolean save(Object player)
	{
		if(player==null) return false;// 偶尔会出现
		// 将玩家数据映射成域对象存入数据库中
		int t=gamePersistence.set(FieldKit.create("id",((Player)player)
			.getId()),mapping(player));
		if(log.isInfoEnabled()) log.info("save, "+t+" "+player);
		return t==Persistence.OK||t==Persistence.ADD;
	}

	/** 映射成域对象 */
	public Fields mapping(Object player)
	{
		Player p=(Player)player;
		FieldObject[] array=new FieldObject[80];
		int i=0;
		array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("id",p.getId());
		array[i++]=FieldKit.create("level",p.getLevel());
		array[i++]=FieldKit.create("player_type",p.getPlayerType());
		array[i++]=FieldKit.create("commander_level",p.getCommanderLevel());
		array[i++]=FieldKit.create("user_state",p.getUser_state());
		array[i++]=FieldKit.create("bundleSize",p.getBundleSize());
		array[i++]=FieldKit.create("user_id",p.getUser_id());
		array[i++]=FieldKit.create("reward",p.getReward());
		array[i++]=FieldKit.create("experience",p.getExperience());
		array[i++]=FieldKit.create("inveted",p.getInveted());
		array[i++]=FieldKit.create("plunderResource",p.getPlunderResource());
		array[i++]=FieldKit.create("fightScore",p.getFightScore());
		array[i++]=FieldKit.create("achieveScore",p.getAchieveScore());
		array[i++]=FieldKit.create("locale",p.getLocale());
		array[i++]=FieldKit.create("honorScore",p.getHonorScore());
		array[i++]=FieldKit.create("playerTaskMark",p.getPlayerTaskMark());
		array[i++]=FieldKit.create("platid",p.getPlatid());
		array[i++]=FieldKit.create("deleteTime",p.getDeleteTime());
		array[i++]=FieldKit.create("escapeDevice",p.getEscapeDevice());
		CharBuffer cb=CharBufferThreadLocal.getCharBuffer();
		cb.clear();

		String str=p.getArea();
		if(str==null)
			array[i++]=FieldKit.create("area",(String)null);
		else
			array[i++]=FieldKit.create("area",str);
		str=p.getName();
		if(str==null)
			array[i++]=FieldKit.create("player_name",(String)null);
		else
			array[i++]=FieldKit.create("player_name",str);
		str=p.getUdid();
		if(str==null)
			array[i++]=FieldKit.create("udid",(String)null);
		else
			array[i++]=FieldKit.create("udid",str);

		str=p.getDeviceToken();
		if(str==null)
			array[i++]=FieldKit.create("deviceToken",(String)null);
		else
			array[i++]=FieldKit.create("deviceToken",str);

		array[i++]=FieldKit.create("create_at",p.getCreateTime());
		array[i++]=FieldKit.create("mute_time",p.getMuteTime());
		array[i++]=FieldKit.create("update_at",p.getUpdateTime());
		array[i++]=FieldKit.create("exit_time",p.getExitTime());
		array[i++]=FieldKit.create("save_time",p.getSaveTime());
		array[i++]=FieldKit.create("online_time",p.getOnlineTime());
		array[i++]=FieldKit.create("taskMark",p.getTaskMark());
		array[i++]=FieldKit.create("style",p.getStyle());
		
		//繁荣度信息
		int[] temp=p.getProsperityInfo();
		for(int j=0;j<temp.length;j++)
			cb.append(temp[j]).append(':');
		if(cb.length()>0) cb.setTop(cb.length()-1);
		array[i++]=FieldKit.create("prosperityInfo",cb.getString());
		cb.clear();
		
		
		temp=p.getActives();
		for(int j=0;j<temp.length;j++)
			cb.append(temp[j]).append(':');
		if(cb.length()>0) cb.setTop(cb.length()-1);
		array[i++]=FieldKit.create("actives",cb.getString());
		cb.clear();

		long[] temp1=p.getResources();
		for(int j=0;j<temp1.length;j++)
			cb.append(temp1[j]).append(':');
		if(cb.length()>0) cb.setTop(cb.length()-1);
		array[i++]=FieldKit.create("resources",cb.getString());
		cb.clear();

		temp=p.getHonor();
		for(int j=0;j<temp.length;j++)
			cb.append(temp[j]).append(':');
		if(cb.length()>0) cb.setTop(cb.length()-1);
		array[i++]=FieldKit.create("honor",cb.getString());
		cb.clear();

		temp=p.getInviter_id();
		for(int j=0;j<temp.length;j++)
			cb.append(temp[j]).append(':');
		if(cb.length()>0) cb.setTop(cb.length()-1);
		array[i++]=FieldKit.create("inviter_id",cb.getString());
		cb.clear();

		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		p.bytesWriteQuest(bb);
		array[i++]=FieldKit.create("quest",bb.toArray());

		bb.clear();
		p.bytesWriteAchieve(bb);
		array[i++]=FieldKit.create("achievement",bb.toArray());

		bb.clear();
		p.bytesWriteBulids(bb);
		array[i++]=FieldKit.create("build_and_troops",bb.toArray());

		bb.clear();
		p.bytesWriteBundle(bb);
		array[i++]=FieldKit.create("bundle",bb.toArray());

		bb.clear();
		p.bytesWriteServices(bb);
		array[i++]=FieldKit.create("service",bb.toArray());

		bb.clear();
		p.bytesWriteSelfCheckPoint(bb);
		array[i++]=FieldKit.create("selfCheckPoint",bb.toArray());
		
		bb.clear();
		p.bytesWriteTearCheckPoint(bb);
		array[i++]=FieldKit.create("tearCheckPoint",bb.toArray());


		bb.clear();
		p.bytesWriteSkills(bb);
		array[i++]=FieldKit.create("skills",bb.toArray());

		bb.clear();
		p.bytesWriteLocaitonSave(bb);
		array[i++]=FieldKit.create("locationSaveList",bb.toArray());
		
		bb.clear();
		p.bytesWriteShipLevel(bb);
		array[i++]=FieldKit.create("shipLevel",bb.toArray());
		
		bb.clear();
		p.bytesWriteEquips(bb);
		array[i++]=FieldKit.create("equips",bb.toArray());
		
		bb.clear();
		p.bytesWriteHeritageCityPoint(bb);
		array[i++]=FieldKit.create("heritagePoint",bb.toArray());
		
		bb.clear();
		p.bytesWriteConsumeGemsData(bb);
		array[i++]=FieldKit.create("gemsActivity",bb.toArray());
		
		array[i++]=FieldKit.create("createIp",p.getCreateIp());
		array[i++]=FieldKit.create("bindIp",p.getBindIp());
		array[i++]=FieldKit.create("loginIp",p.getLoginIp());
		
		array[i++]=FieldKit.create("platName",p.getPlat());

		bb.clear();
		p.bytesWriteArmsRoutePoint(bb);
		array[i++]=FieldKit.create("armsCheckPoint",bb.toArray());
		
		bb.clear();
		p.bytesWriteElitePoint(bb);
		array[i++]=FieldKit.create("elite",bb.toArray());
		
		bb.clear();
		p.bytesWriteProduceQueue(bb);
		array[i++]=FieldKit.create("produceQueue",bb.toArray());
		
		bb.clear();
		p.bytesWriteFormations(bb);
		array[i++]=FieldKit.create("bakFormation",bb.toArray());
		
		bb.clear();
		p.bytesWriteBetmap(bb);
		array[i++]=FieldKit.create("betmap",bb.toArray());
				
		bb.clear();
		p.bytesWriteRecruit(bb);
		array[i++]=FieldKit.create("recruit",bb.toArray());
		
		bb.clear();
		p.bytesWriteOfficers(bb);
		array[i++]=FieldKit.create("officers",bb.toArray());
		
		bb.clear();
		p.bytesWriteComrade(bb);
		array[i++]=FieldKit.create("comrade",bb.toArray());
		
		bb.clear();
		p.bytesWriteAnnex(bb);
		array[i++]=FieldKit.create("annex",bb.toArray());
		bb.clear();
		p.bytesWriteAllianceChest(bb);
		array[i++]=FieldKit.create("allianceChest",bb.toArray());
		
		bb.clear();
		p.bytesWritePropExchangeNum(bb);
		array[i++]=FieldKit.create("propExchangeNum",bb.toArray());
		
		array[i++]=FieldKit.create("bundle_id",p.getBundleId());
		
		bb.clear();
		p.bytesWriteOffcerShop(bb);
		array[i++]=FieldKit.create("officershop",bb.toArray());
		
		bb.clear();
		p.bytesWriteGrowthPlan(bb);
		array[i++]=FieldKit.create("growthplan",bb.toArray());
		
		bb.clear();
		p.bytesWritePointBuff(bb);
		array[i++]=FieldKit.create("pointBuff",bb.toArray());
		
		bb.clear();
		p.byteWriteFriendInfo(bb);
		array[i++]=FieldKit.create("friendInfo",bb.toArray());
		
		//**********************************
		//***********写属性 放最后 （含有读写标记,放前面可能会造成数据错误）
		if(!PublicConst.HEAD_SIGN.equals(p.getAttributes(PublicConst.HEAD_TO_ACHIEVEMENT)))
			p.setAttribute(PublicConst.HEAD_TO_ACHIEVEMENT, PublicConst.HEAD_SIGN);
		String[] strs=p.getAttributes();
		if(strs.length>0) SHARP2.encode(strs[0],cb);
		for(int j=1;j<strs.length;j++)
		{
			cb.append(':');
			SHARP2.encode(strs[j],cb);
		}
		array[i++]=FieldKit.create("attributes",cb.getString());
		cb.clear();
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}

}
