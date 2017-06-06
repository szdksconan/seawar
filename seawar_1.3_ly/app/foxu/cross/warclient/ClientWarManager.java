package foxu.cross.warclient;

import java.io.IOException;
import java.util.HashMap;

import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import mustang.back.BackKit;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.cross.server.CrossAct;
import foxu.cross.war.CrossWar;
import foxu.cross.war.CrossWarRoundSave;
import foxu.cross.war.CrossWarSaveDBAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.ds.SWDSManager;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.ConsumeGemsActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.port.ChatMessagePort;
import foxu.sea.port.UserToCenterPort;

/**
 * 跨服战客服端管理器
 * 
 * @author yw
 * 
 */
public class ClientWarManager implements TimerListener
{

	private static Logger log=LogFactory.getLogger(ClientWarManager.class);

	/**
	 * 主动推跨服战 FORE_PORT前端接收端口 SEND_ACT发送活动进度 SEND_REP发送预赛战报
	 * SEND_SN发送64强,SEND_FINALREP发送决赛战报
	 * ,OPEN_ACT开启活动,SEND_WARD_STATE刷新领奖状态,FLUSH_AWARD刷新奖励
	 */
	public static int FORE_PORT=2006,SEND_ACT=0,SEND_REP=1,SEND_SN=2,
					SEND_FINALREP=3,OPEN_ACT=4,SEND_WARD_STATE=5,
					FLUSH_AWARD=6;
	/** 未做处理 */
	public static int ERR_TYPE=127;
	/** 报名错误返回 JION_SUCCC参加成功 JION_END报名截止 JION_SERVER未对该服开放 JION_HAD已参加 */
	public static int JION_SUCC=0,JION_END=1,JION_SERVER=2,JION_HAD=3;
	/** 设置错误返回 SET_SUCC设置成功 SET_PLAEYR没有该玩家 SET_CANOT非设置时间段 SET_SERVER为开放该活动能 */
	public static int SET_SUCC=0,SET_NOPLAEYR=1,SET_CANOT=2,SET_SERVER=3;
	/** 获取错误返回 GETSHIP_SUCC设置成功 GETSHIP_NOPLAEYR没有该玩家 */
	public static int GETSHIP_SUCC=0,GETSHIP_NOPLAEYR=1;
	/** 领取奖励 错误返回 GET_SUCC领取成功 GET_HAD 已领取 GET_NOPLAYER无该人员 GET_CANNOT非领奖时段 */
	public static int GET_SUCC=0,GET_HAD=1,GET_NOPLAYER=2,GET_CANNOT=3;
	/** 获取跨服战进度 错误返回 */
	public static int GETACT_SUCC=0,GETACT_NOACT=1;
	/** 押注 错误返回 BET_CANNOT当前不能下注,BET_NOPLAYER无改玩家,BET_NOACT无跨服战活动 */
	public static int BET_SUCC=0,BET_CANNOT=1,BET_NOPLAYER=2,BET_NOACT=3;

	/**
	 * 接收数据常量 JION 参加,SUBMIT 批量提交玩家数据,GET_AWARD领取奖励
	 * ,GET_WAR获取所有跨服活动,BET押注,GET_SERVER获取服务器信息
	 */
	public static int JION=0,SUBMIT=1,GET_AWARD=2,GET_WAR=3,BET=4,
					GET_SERVER=5;

	/** 跨服中心ip */
	String cross_ip="192.168.10.66";
	/** 跨服中心port */
	String cross_port="7163";
	/** 跨服中心 处理端口 */
	String cross_actionPort="10";
	/** 所有跨服战 */
	IntKeyHashMap wars=new IntKeyHashMap();
	/** 最后开启的跨服战 */
	CrossWar warsave;
	/** 跨服玩家表 id-clientWarPlayer */
	IntKeyHashMap map=new IntKeyHashMap();
	/** 64强玩家列表 */
	ArrayList sn=new ArrayList();
	/** 64强玩家列表 */
	ArrayList betsn=new ArrayList();
	/** 预赛战报 */
	ArrayList prerep=new ArrayList();
	/** 决赛战报 */
	ArrayList finalrep=new ArrayList();
	/** n强播报 */
	ArrayList showSn=new ArrayList();
	/** 内存总管理器 */
	CreatObjectFactory cfactory;
	/** 当前提取下标 */
	int subindex;
	/** 每次提交最大数量 */
	int submax=100;
	/** 每次存库数量 */
	int savemax=100;
	/** 每次推送战报最大条数 */
	int premax=500;
	SWDSManager dsmanager;

	/** 本服名称 */
	String server_name;
	/** 本服国籍 */
	String national;

	// /** 冠军跨服id */
	// int cmid;
	/** 本服ip */
	String address;
	/** 预赛下标 */
	int preIndex;
	/** 决赛下标 */
	int finIndex;
	/** 预赛存储下标 */
	int presaveIn;
//	/** 决赛存储下标 */
//	int finsaveIn;

	/** 跨服玩家 数据库操作器 */
	ClientWarPlayerDBAccess cwpDBAccess;
	/** 战报数据库 操作器 */
	CrossWarSaveDBAccess cwsDBAccess;

	ClientWarPNComparator cpnc=new ClientWarPNComparator();
	ClientWarRIdComparator crid=new ClientWarRIdComparator();
	ClientWarPBComparator cpbc=new ClientWarPBComparator();
	int gettry;
	int getmax=10;

	// test
	int testCount;

	public CrossWarSaveDBAccess getCwsDBAccess()
	{
		return cwsDBAccess;
	}

	public void setCwsDBAccess(CrossWarSaveDBAccess cwsDBAccess)
	{
		this.cwsDBAccess=cwsDBAccess;
	}

	public ClientWarPlayerDBAccess getCwpDBAccess()
	{
		return cwpDBAccess;
	}

	public void setCwpDBAccess(ClientWarPlayerDBAccess cwpDBAccess)
	{
		this.cwpDBAccess=cwpDBAccess;
	}

	public SWDSManager getDsmanager()
	{
		return dsmanager;
	}

	public void setDsmanager(SWDSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}

	public CrossWar getWarsave()
	{
		return warsave;
	}

	public void setWarsave(CrossWar warsave)
	{
		this.warsave=warsave;
	}

	public CreatObjectFactory getCfactory()
	{
		return cfactory;
	}

	public void setCfactory(CreatObjectFactory cfactory)
	{
		this.cfactory=cfactory;
	}
	
	
	public String getCross_ip()
	{
		return cross_ip;
	}

	
	public void setCross_ip(String cross_ip)
	{
		this.cross_ip=cross_ip;
	}

	
	public String getCross_port()
	{
		return cross_port;
	}

	
	public void setCross_port(String cross_port)
	{
		this.cross_port=cross_port;
	}
	
	public String getCross_actionPort()
	{
		return cross_actionPort;
	}

	
	public void setCross_actionPort(String cross_actionPort)
	{
		this.cross_actionPort=cross_actionPort;
	}

	/** 初始化 */
	public void init()
	{
		address=System.getProperty("webAddress");
		// TimerCenter.getSecondTimer().add(
		// new TimerEvent(this,"getact",60*1000,10,60*1000));
		// test
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"getact",10*1000,10,10*1000));

		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"getserver",30*1000));

		// 加载战报
		preIndex=prerep.size();
		finIndex=finalrep.size();
	}
	/** 获取舰队 */
	public void getFleet(Player player,ByteBuffer data)
	{
		ClientWarPlayer cplayer=(ClientWarPlayer)map.get(player.getId());
		data.clear();
		if(cplayer==null)
		{
			data.writeByte(GETSHIP_NOPLAEYR);
		}
		else
		{
			data.writeByte(GETSHIP_SUCC);
			cplayer.bytesWriteDefencelist(data);
			cplayer.bytesWriteAttacklist(data);
		}
	}
	/** 获取人气榜 */
	public void getRank(Player player,ByteBuffer data)
	{
		data.clear();
		if(warsave==null||warsave.isover())
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(betsn.size());
			int rank=CrossWar.S64+1;
			for(int i=0;i<betsn.size();i++)
			{
				ClientWarPlayer cp=(ClientWarPlayer)betsn.get(i);
				data.writeByte(i+1);
				data.writeInt(cp.getCrossid());
				data.writeUTF(cp.getName());
				data.writeByte(getSnType(cp));// 未晋级0，止步1，晋级2，冠军3
				data.writeShort(cp.getRank());
				data.writeInt(cp.getBet());
				if(cp.isLocal()&&player.getId()==cp.getId())
				{
					rank=i+1;
				}
			}
			ClientWarPlayer mycp=getCplayer(player.getId());
			data.writeBoolean(mycp!=null);
			if(mycp!=null)
			{
				data.writeByte(rank);
				data.writeInt(mycp.getCrossid());
				data.writeByte(getSnType(mycp));
				data.writeShort(mycp.getRank());
				data.writeInt(mycp.getBet());
			}
		}
	}
	/** 计算晋级类型 */
	public int getSnType(ClientWarPlayer cp)
	{
		int match=warsave.getMatch();
		int type=0;// 未晋级决赛
		if(cp.getRank()<=CrossWar.S0)
		{
			type=0;
		}
		else if(cp.getRank()==CrossWar.S1)
		{
			type=3;// 冠军
		}
		else if(cp.getRank()>match)
		{
			type=1;// 止步
		}
		else
		{
			type=2;// 晋级
			if(cp.getRank()==CrossWar.S2)type=5;
			if(warsave.getCmid()>0)type=4;
		}
		return type;
	}
	/** 获取64强信息 */
	public void getSn64(Player player,ByteBuffer data)
	{
		int cid=data.readInt();
		ClientWarPlayer cp=getSnPlayer(cid);
		if(cp==null)
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"no this cplayer"));
		data.clear();
		data.writeShort(cp.getSid());
		data.writeUTF(cp.getName());
		data.writeInt(cp.getBet());
		data.writeInt(cp.getFightscore());
		data.writeUTF(cp.getSname());
		data.writeUTF(cp.getNational());// 国旗
		data.writeUTF(cp.getAname());
		data.writeByte(getSnType(cp));
		data.writeShort(cp.getRank());
	}
	/** 领取奖励 */
	public void getAward(Player player,ByteBuffer data,int type)
	{
		if(warsave==null||!warsave.isAward())
		{
			data.clear();
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"get cannot"));
		}
		if(type==0)// 押注
		{
			player.setCrossAwardState(3,2);
			player.getBetmap().remove(0);
			int gem=(int)(player.getBet(warsave.getCmid())*warsave.getOdds());
			if(gem>0)
			{
				Resources.addGemsNomal(gem,player.getResources(),player);
				cfactory.createGemTrack(GemsTrack.WIN_BET,player.getId(),
					gem,0,Resources.getGems(player.getResources()));
			}
		}
		else
		// 排名
		{
			player.setCrossAwardState(4,2);
			Integer warid=(Integer)player.getBetmap().remove(1);
			warid=warid==null?0:warid;
			Integer rank=(Integer)player.getBetmap().get(2);
			rank=rank==null?0:rank;
			if(warid==warsave.getId())
			{
				Award[] awards=warsave.getAwards();
				Award ad=getRankAward(rank,awards);
				ad.awardLenth(data,player,cfactory,null,null,
					new int[]{EquipmentTrack.FROM_CROSSWAR});
			}

		}
	}

	/** 获取排名奖励 */
	public Award getRankAward(int rank,Award[] awards)
	{
		if(awards==null)
		{
			return (Award)Award.factory.getSample(CrossWar.EMPTY_SID);
		}
		int index=awards.length-1;
		if(rank==CrossWar.S1)
		{
			index=0;
		}
		else if(rank==CrossWar.S2)
		{
			index=1;
		}
		else if(rank==CrossWar.S4)
		{
			index=2;
		}
		else if(rank==CrossWar.S8)
		{
			index=3;
		}
		else if(rank==CrossWar.S16)
		{
			index=4;
		}
		else if(rank==CrossWar.S32)
		{
			index=5;
		}
		else if(rank==CrossWar.S64)
		{
			index=6;
		}
		if(index>=awards.length) index=awards.length-1;
		return awards[index];
	}
	/** 获取跨服战信息 */
	public boolean getWars()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(CrossAct.WAR_SID);
		data.writeByte(GET_WAR);
		data=sendHttpData(data,cross_ip,cross_port);
		if(data==null) return false;
		// 序列化活动
		boolean ishave=bytesReadWars(data);
		// 初始化数据
		initWarData(ishave);
		// 序列化64强
		sn.clear();
		betsn.clear();
		updateSnPlayer(data);
		// 序列化决赛战报
		bytesReadFinalRep(data);
		return true;
	}
	/** 获取本服 私有信息（服务器名，国籍） */
	public void getLocalServer()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(CrossAct.WAR_SID);
		data.writeByte(GET_SERVER);
		data.writeUTF(address);
		data=sendHttpData(data,cross_ip,cross_port);
		if(data==null) return;
		server_name=data.readUTF();
		national=data.readUTF();
	}
	/**
	 * 序列化所有跨服战
	 * @param data
	 * @return 是否已有目标跨服战
	 */
	public boolean bytesReadWars(ByteBuffer data)
	{
		wars.clear();
		int len=data.readUnsignedShort();
		CrossWar cw=null;
		for(int i=0;i<len;i++)
		{
			cw=new CrossWar();
			cw.showBytesRead(data);
			wars.put(cw.getId(),cw);
		}
		if(cw!=null&&warsave!=null&&warsave.getId()==cw.getId())
		{
			wars.put(warsave.getId(),warsave);
			return true;
		}
		if(cw!=null&&!cw.isover())
		{
			warsave=cw;
			warsave.setOpen(true);
		}
		return false;
	}

	/** 序列化决赛战报 */
	public void bytesReadFinalRep(ByteBuffer data)
	{
		finalrep.clear();
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			CrossWarRoundSave crs=new CrossWarRoundSave();
			crs.showBytesRead(data);
			finalrep.add(crs);
		}
		SetKit.sort(finalrep.getArray(),crid);
	}

	/** 初始化相关活动数据 */
	public void initWarData(boolean ishave)
	{
		if(warsave==null||warsave.isover()) return;
		map.clear();
		String sql="select * from clientwar_player where warid="
			+warsave.getId();
		ClientWarPlayer[] cps=cwpDBAccess.loadBySql(sql);
		if(cps!=null&&cps.length>0)
		{
			for(int i=0;i<cps.length;i++)
			{
				map.put(cps[i].getId(),cps[i]);
			}
		}
		if(ishave)
		{
			saveRep(true);
		}
		prerep.clear();
		sql="select * from croundsave_client where warid="+warsave.getId();
		CrossWarRoundSave[] crs=cwsDBAccess.loadBySql(sql);
		if(crs!=null&&crs.length>0)
		{
			for(int i=0;i<crs.length;i++)
			{
				prerep.add(crs[i]);
			}
			presaveIn=prerep.size();
			preIndex=prerep.size();
		}
	}

	/** 创建一个跨服玩家 */
	public void createCrossPlayer(ClientWarPlayer cp,int id,int crossid,
		String sname,String national,int warid)
	{
		cp.setWarid(warid);
		cp.setId(id);
		cp.setCrossid(crossid);
		cp.setSname(sname);
		cp.setNational(national);
		map.put(id,cp);
	}

	/** 玩家跨服战序列化 */
	public void crossWarBytesWrite(boolean join,ClientWarPlayer cp,
		Player player,ByteBuffer data)
	{
		if(join)
		{
			data.writeShort(CrossAct.WAR_SID);
			data.writeByte(JION);
			data.writeInt(player.getId());
			data.writeShort(UserToCenterPort.SERVER_ID);// 服务器id
			data.writeShort(UserToCenterPort.AREA_ID);// 大区id
			data.writeShort(UserToCenterPort.PLAT_ID);// 平台id
			data.writeShort(player.getSid());
			data.writeUTF(player.getName());

			String allid=player.getAttributes(PublicConst.ALLIANCE_ID);
			int alid=allid==null?0:TextKit.parseInt(allid);
			Alliance alliance=cfactory.getAlliance(alid,false);
			String aname=alliance==null?" ":alliance.getName();

			data.writeUTF(aname);
			
			cp.setSid(player.getSid());
			cp.setPlatid(UserToCenterPort.PLAT_ID);
			cp.setAreaid(UserToCenterPort.AREA_ID);
			cp.setAreaid(UserToCenterPort.SERVER_ID);
			cp.setAname(aname);
		}
		else
		{
			data.writeInt(cp.getCrossid());
			data.writeUTF(player.getName());
		}
		
		data.writeShort(player.getLevel());
		data.writeInt(player.getFightScore());
		player.crossWriteAdjustment(data);
		player.bytesWriteShipLevel(data);
		
		cp.bytesWriteAttacklist(data);
		cp.bytesWriteDefencelist(data);
		cp.bytesWriteOFS(data);//军官
		cp.setName(player.getName());
		cp.setFightscore(player.getFightScore());
	}
	/** 报名 */
	public void jion(Player player,ByteBuffer data)
	{
		if(warsave==null)
		{
			data.clear();
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"not open cross war"));
		}
		else if(warsave.joinEnd())
		{
			data.clear();
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"jion end"));
		}
		else if(player.getLevel()<warsave.getLvlimit())
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"lv limit"));
		}
		else
		{
			ClientWarPlayer cp=new ClientWarPlayer();
			boolean defence_set=cp.bytesReadDefencelist(data);
			boolean attck_set=cp.bytesReadAttacklist(data);
			if(!defence_set&&!attck_set)
			{
				throw new DataAccessException(0,
					InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"ship must > 0"));
			}
			if(!(defence_set&&attck_set))
				cp.correctList(attck_set,defence_set);
			cp.setOfs(player.getOfficers().getUsingOfficers());//军官
			data.clear();
			// todo 序列化玩家
			crossWarBytesWrite(true,cp,player,data);
			ByteBuffer httpdata=sendHttpData(data,cross_ip,cross_port);
			backJion(cp,player,data,httpdata);
		}

	}
	/** 设置舰队 */
	public void setFleet(ClientWarPlayer cplayer,Player player,
		ByteBuffer data)
	{
		if(warsave==null)
		{
			data.clear();
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"not open cross war"));
		}
		else if(!warsave.setCan()||cplayer.getRank()!=warsave.getMatch())
		{
			data.clear();
			if(!warsave.setCan())
			{
				throw new DataAccessException(0,InterTransltor.getInstance()
					.getTransByKey(PublicConst.SERVER_LOCALE,"set cannot"));
			}
			else
			{
				throw new DataAccessException(0,InterTransltor.getInstance()
					.getTransByKey(PublicConst.SERVER_LOCALE,"cannot embattle"));
			}
		}
		else
		{
			boolean defence_set=cplayer.bytesReadDefencelist(data);
			boolean attck_set=cplayer.bytesReadAttacklist(data);
			if(!defence_set&&!attck_set)
			{
				throw new DataAccessException(0,
					InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"ship must > 0"));
			}
			if(!(defence_set&&attck_set))
				cplayer.correctList(attck_set,defence_set);
			cplayer.setOfs(player.getOfficers().getUsingOfficers());//军官
			cwpDBAccess.save(cplayer);
			data.clear();
			data.writeInt(cplayer.getCrossid());
			// throw new DataAccessException(0,"set succ");
		}

	}
	/** 判断设置舰队||报名 */
	public void jionSet(Player player,ByteBuffer data)
	{
		ClientWarPlayer cplayer=(ClientWarPlayer)map.get(player.getId());
		if(cplayer==null)
		{
			jion(player,data);
		}
		else
		{
			setFleet(cplayer,player,data);
		}
	}
	/** 报名处理跨服回应 */
	public void backJion(ClientWarPlayer cp,Player player,ByteBuffer data,
		ByteBuffer httpdata)
	{
		if(httpdata==null)
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"cannot connect cross_server"));
		int type=httpdata.readUnsignedByte();
		if(type==ERR_TYPE)
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"cannot handle the message"));
		}
		else if(type==JION_END)
		{
			data.clear();
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"jion end"));
		}
		else if(type==JION_SERVER)
		{
			data.clear();
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"not open cross war"));
		}
		else if(type==JION_HAD)
		{
			data.clear();
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"jion had"));
		}
		else
		{
			int crossid=httpdata.readInt();
			String sname=httpdata.readUTF();
			String national=httpdata.readUTF();
			createCrossPlayer(cp,player.getId(),crossid,
				sname,national,warsave.getId());
			player.jionCrossWar(warsave.getId());
			cwpDBAccess.save(cp);
			data.clear();
			data.writeInt(crossid);
			// throw new DataAccessException(0,"jion succ");
		}
	}
	/** 激活提取玩家数据 */
	public void activeSubmit()
	{
		TimerCenter.getMinuteTimer().add(
			new TimerEvent(this,"submit",10*1000));
	}
	/** 提交玩家数据 */
	public boolean submitPlayer()
	{
		Object[] objs=map.valueArray();
		int sub=0;
		ByteBuffer data=new ByteBuffer();
		data.writeShort(CrossAct.WAR_SID);
		data.writeByte(SUBMIT);
		int top=data.top();
		data.writeShort(sub);
		for(;subindex<objs.length&&sub<submax;subindex++)
		{
			if(objs[subindex]==null) continue;
			ClientWarPlayer cp=(ClientWarPlayer)objs[subindex];
			if(cp.getRank()!=warsave.getMatch()) continue;
			Player player=cfactory.getPlayerById(cp.getId());
			if(player==null) continue;
			crossWarBytesWrite(false,cp,player,data);
			sub++;
		}
		int nowtop=data.top();
		data.setTop(top);
		data.writeShort(sub);
		data.setTop(nowtop);
		sendHttpData(data,cross_ip,cross_port);
		if(subindex>=objs.length)
		{
			subindex=0;
			return true;
		}
		return false;

	}
	/** 同步进度 */
	public void updateStep(ByteBuffer data)
	{
		int id=warsave==null?0:warsave.getId();
		if(warsave==null) warsave=new CrossWar();
		warsave.showBytesRead(data);
		if(id!=warsave.getId())
		{
			warsave.setOpen(true);
			wars.put(warsave.getId(),warsave);
			map.clear();
			sn.clear();
			betsn.clear();
			prerep.clear();
			finalrep.clear();
			showSn.clear();
			subindex=0;
			preIndex=0;
			finIndex=0;
			presaveIn=0;
			savemax=100;
		}
	}
	/** 接收战报 */
	public void receiveRep(ByteBuffer data,boolean isfinal)
	{
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			CrossWarRoundSave save=new CrossWarRoundSave();
			save.showBytesRead(data);
			if(!isfinal)
			{
				prerep.add(save);
			}
			else
			{
				finalrep.add(save);
			}
		}
		if(isfinal) SetKit.sort(finalrep.getArray(),crid);
	}
	/** n强播报 */
	public void receiveSn(ByteBuffer data)
	{
		synchronized(showSn)
		{
			String sname=data.readUTF();
			String name=data.readUTF();
			int n=data.readUnsignedShort();
			showSn.add(new ShowSn(sname,name,n));
		}
	}

	/** 同步n强人员押注量 */
	public void updateSnBet(ByteBuffer data)
	{
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			int crossid=data.readInt();
			int bet=data.readInt();
			ClientWarPlayer cp=getSnPlayer(crossid);
			if(cp==null) continue;
			cp.setBet(bet);
			if(cp.isLocal())
			{
				setLocalSn(cp);
			}
		}
		SetKit.sort(betsn.getArray(),cpbc);// 押注量排序

	}

	/** 同步n强玩家 */
	public void updateSnPlayer(ByteBuffer data)
	{
		int len=data.readUnsignedShort();
		boolean flush_bet=false;
		for(int i=0;i<len;i++)
		{
			int crossid=data.readInt();
			ClientWarPlayer cp=getSnPlayer(crossid);
			if(cp==null)
			{
				cp=new ClientWarPlayer();
				cp.setCrossid(crossid);
				sn.add(cp);
				betsn.add(cp);
				flush_bet=true;
			}
			cp.showBytesRead(data);
			cp.setSend(false);
			if(cp.isLocal())// 同步本地数据
			{
				setLocalSn(cp);
			}
		}
		SetKit.sort(sn.getArray(),cpnc);// 编号排序
		if(flush_bet)SetKit.sort(betsn.getArray(),cpbc);// 押注量排序
		if(len==CrossWar.S1)
		{
			warsave.setCmid(getChampion().getCrossid());
		}
	}
	/** 修改n强本地数据 */
	public void setLocalSn(ClientWarPlayer cp)
	{
		// 有假数据时 报空异常
		try
		{
			ClientWarPlayer lcp=(ClientWarPlayer)map.get(cp.getId());
			lcp.setRank(cp.getRank());
			lcp.setBet(cp.getBet());
			cfactory.getPlayerCache().load(cp.getId()+"")
				.setCrossSn(warsave.getId(),cp.getRank());
			cwpDBAccess.save(lcp);
		}
		catch(Exception e)
		{

		}

	}
	/** 获取冠军 */
	public ClientWarPlayer getChampion()
	{
		for(int i=0;i<sn.size();i++)
		{
			ClientWarPlayer cp=(ClientWarPlayer)sn.get(i);
			if(cp.getRank()==CrossWar.S1) return cp;
		}
		return null;
	}
	/** 根据跨服唯一id 获取n强玩家 */
	public ClientWarPlayer getSnPlayer(int crossid)
	{
		for(int i=0;i<sn.size();i++)
		{
			ClientWarPlayer cp=(ClientWarPlayer)sn.get(i);
			if(cp.getCrossid()==crossid)
			{
				return cp;
			}
		}
		return null;
	}
	/** 根据id 获取跨服玩家 */
	public ClientWarPlayer getCplayer(int id)
	{
		return (ClientWarPlayer)map.get(id);
	}

	/** 开启活动 */
	public void openAct(ByteBuffer data)
	{
		if(warsave==null||warsave.isSend()||!warsave.isOpen()) return;
		data.clear();
		Session[] ss=dsmanager.getSessionMap().getSessions();
		for(int i=0;i<ss.length;i++)
		{
			if(ss[i]==null) continue;
			Player p=(Player)ss[i].getSource();
			if(p==null) continue;
			Connect c=ss[i].getConnect();
			if(c==null||!c.isActive()) continue;
			data.writeShort(FORE_PORT);
			data.writeByte(OPEN_ACT);
			warsave.showClientBytesWrite(data);// 活动信息
			showBytesWriteFleet(p,data);// 出征舰队
			showBytesWriteRep(p,false,data);// 预赛战报
			showBytesWriteFnPlayer(data);// 决赛圈玩家
			showBytesWriteRep(p,true,data);// 决赛战报
			showBytesWriteBet(p,data);// 押注数据
			writeSelfInfo(p,data);// 个人跨服信息
			writeAwardSate(p,data);// 领奖信息
			writeAward(p,data);// 奖励
			data.writeUTF(server_name==null?" ":server_name);// 玩家自己的服务器名
			data.writeUTF(national==null?" ":national);// 自己的国家名
			data.writeBoolean(p.getPreState(warsave.getId()));// 预赛显示标记
			c.send(data);
		}

	}

	/** 广播 活动进度 */
	public void sendAct(ByteBuffer data)
	{
		if(warsave==null||warsave.isSend()||warsave.isOpen()) return;
		data.clear();
		data.writeShort(FORE_PORT);
		data.writeByte(SEND_ACT);
		warsave.showClientBytesWrite(data);
		dsmanager.getSessionMap().send(data);
	}
	/** 刷新奖励内容 */
	public void sendFluhAward(ByteBuffer data)
	{
		if(warsave==null||!warsave.isShowAward()) return;
		data.clear();
		data.writeShort(FORE_PORT);
		data.writeByte(FLUSH_AWARD);
		int top=data.top();
		Session[] ss=dsmanager.getSessionMap().getSessions();
		for(int i=0;i<ss.length;i++)
		{
			Object obj=ss[i].getSource();
			if(obj==null) continue;
			Connect c=ss[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player p=(Player)obj;
			writeAward(p,data);
			c.send(data);
			data.setTop(top);
		}
		warsave.setShowAward(false);
	}
	/** 广播领奖状态 */
	public void sendAwardSate(ByteBuffer data)
	{
		if(warsave==null||!warsave.isAwardSate()) return;
		data.clear();
		data.writeShort(FORE_PORT);
		data.writeByte(SEND_WARD_STATE);
		int top=data.top();
		Session[] ss=dsmanager.getSessionMap().getSessions();
		for(int i=0;i<ss.length;i++)
		{
			Object obj=ss[i].getSource();
			if(obj==null) continue;
			Connect c=ss[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player p=(Player)obj;
			data.writeByte(p.getCrossAwardState(3));
			data.writeByte(p.getCrossAwardState(4));
			c.send(data);
			data.setTop(top);
		}
		warsave.setAwardSate(false);
	}

	/** 发送战报给前台 */
	public void sendRep(Player player,CrossWarRoundSave cs,ByteBuffer data)
	{
		Session session=(Session)player.getSource();
		if(session==null) return;
		Connect c=session.getConnect();
		if(c==null||!c.isActive()) return;
		data.clear();
		data.writeShort(FORE_PORT);
		data.writeByte(SEND_REP);
		data.writeShort(1);
		cs.clientBytesWrite(player.getId(),data,false);
		c.send(data);
	}
	/** 广播 预赛战报 */
	public void sendPreRep(ByteBuffer data)
	{
		if(prerep.size()>preIndex)
		{
			int max=preIndex+premax;
			for(;preIndex<prerep.size()&&preIndex<max;preIndex++)
			{
				CrossWarRoundSave cs=(CrossWarRoundSave)prerep.get(preIndex);
				if(address.equals(cs.getAttackip()))
				{
					Player player=cfactory.getPlayerById(cs.getAttackpid());
					if(player!=null)
					{
						sendRep(player,cs,data);
					}
				}

				if(address.equals(cs.getDefenceip()))
				{
					Player player=cfactory.getPlayerById(cs.getDefencepid());
					if(player!=null)
					{
						sendRep(player,cs,data);
					}
				}
			}
		}

	}

	/** 广播 决赛战报 */
	public void sendFinRep(ByteBuffer data)
	{
		if(finalrep.size()>finIndex)
		{
			CrossWarRoundSave cs=(CrossWarRoundSave)finalrep.get(finIndex);
			int totalNum=getTotalByMatch(cs.getType());
			int repNum=totalNum/2;
			if(totalNum==CrossWar.S8)
			{
				repNum=CrossWar.S8-1;
			}
			data.clear();
			data.writeShort(FORE_PORT);
			data.writeByte(SEND_FINALREP);
			data.writeShort(1);
			data.writeShort(cs.getType());
			data.writeInt(totalNum);
			int[] ids=getSnId(totalNum);
			for(int k=0;k<totalNum;k++)
			{
				data.writeInt(ids[k]);
			}
			data.writeInt(repNum);
			for(int k=0;k<repNum;k++)
			{
				CrossWarRoundSave save=getIndexNRep(k,cs.getType());
				if(save!=null)
				{
					save.clientBytesWrite(0,data,true);
				}
				else
				{
					CrossWarRoundSave.defClientRepWrite(data);
				}
			}
			dsmanager.getSessionMap().send(data);
			finIndex=finalrep.size();
		}
	}

	/** 广播 当前参赛人员 */
	public void sendFinNowCP(ByteBuffer data)
	{
		if(warsave.isSend()) return;
		warsave.setSend(true);
		warsave.setOpen(false);
		if(warsave.getStep()<=CrossWar.T1) return;
		CrossWarRoundSave cs=null;
		if(finalrep.size()>0)
			cs=(CrossWarRoundSave)finalrep.get(finalrep.size()-1);
		int type=cs!=null?cs.getType()+1:CrossWarRoundSave.FIN64;
		int totalNum=getTotalByMatch(type);
		if(totalNum<CrossWar.S8) return;
		int repNum=totalNum/2;
		if(totalNum==CrossWar.S8)
		{
			repNum=CrossWar.S8-1;
		}
		data.clear();
		data.writeShort(FORE_PORT);
		data.writeByte(SEND_FINALREP);
		data.writeShort(1);
		data.writeShort(type);
		data.writeInt(totalNum);
		int[] ids=getSnId(totalNum);
		for(int k=0;k<totalNum;k++)
		{
			data.writeInt(ids[k]);
		}
		data.writeInt(repNum);
		for(int k=0;k<repNum;k++)
		{
			CrossWarRoundSave save=getIndexNRep(k,type);
			if(save!=null)
			{
				save.clientBytesWrite(0,data,true);
			}
			else
			{
				CrossWarRoundSave.defClientRepWrite(data);
			}
		}
		dsmanager.getSessionMap().send(data);
	}
	/** 刷新奖励 */
	public void flushAward()
	{
		if(warsave==null||!warsave.isFlushAward()) return;
		warsave.updateAward();
	}
	/** 设置领奖状态 */
	public void setAwardState()
	{
		if(warsave==null||!warsave.isAwardSate()) return;
		Object[] objs=cfactory.getPlayerCache().getCacheMap().valueArray();
		if(objs==null) return;
		for(int i=0;i<objs.length;i++)
		{
			if(objs[i]==null) continue;
			PlayerSave save=(PlayerSave)objs[i];
			Player p=save.getData();
			if(p==null) continue;
			if(p.getBet(warsave.getCmid())>0)// 设置押注奖励状态
			{
				p.setCrossAwardState(3,1);
				cfactory.getPlayerCache().getChangeListMap()
					.put(p.getId(),save);
			}
			else
			{
				p.setCrossAwardState(3,0);
			}
		}
		int[] pids=map.keyArray();
		for(int i=0;i<pids.length;i++)
		{
			Player player=cfactory.getPlayerCache().load(pids[i]+"");
			if(player==null) continue;
			player.setCrossAwardState(4,1);// 设置参与奖励状态
		}
		warsave.setShowAward(true);
	}

	/** 设置预赛标记显示状态 */
	public void setPreState()
	{
		if(warsave==null||!warsave.isPreSate()) return;
		int[] pids=map.keyArray();
		for(int i=0;i<pids.length;i++)
		{
			Player player=cfactory.getPlayerCache().load(pids[i]+"");
			if(player==null) continue;
			player.setCrossAwardState(5,1);
			player.setCrossAwardState(6,warsave.getId());
		}
		warsave.setPreSate(false);
	}

	/** 主动发送奖励 */
	public void sendAward()
	{
		// //
		// System.out.println((warsave==null)+"-----sendAward---00--"+(warsave.getCmid()<=0)+"-----"+warsave.isSendAward());
		// if(warsave==null||warsave.getCmid()<=0||warsave.isSendAward())
		// return;
		// System.out.println("-----sendAward---11--");
		// Session[] ss=dsmanager.getSessionMap().getSessions();
		// for(int i=0;i<ss.length;i++)
		// {
		// System.out.println(i+"----i-0---");
		// if(ss[i]==null) continue;
		// Connect c=ss[i].getConnect();
		// System.out.println(i+"----i-1--");
		// if(c==null||!c.isActive()) continue;
		// Player p=(Player)ss[i].getSource();
		// System.out.println(i+"----i-2--");
		// if(p==null) continue;
		// System.out.println(i+"----i-3--");
		// checkSendAward(p);
		// }
		// warsave.setAward(true);
	}
	/** 参赛奖励邮件 */
	public void messageAward(Player p,Award ad,int gems,int rank)
	{
		String text0=InterTransltor.getInstance().getTransByKey(
			p.getLocale(),"join in award");
		String text="";
		if(ad!=null)
		{
			String rankstr=null;
			if(rank==CrossWar.S1)
			{
				rankstr=InterTransltor.getInstance().getTransByKey(
					p.getLocale(),"promoted cross battle champion");
				rankstr=TextKit.replace(rankstr,"%",p.getName());
			}
			else if(rank==CrossWar.S2)
			{
				rankstr=InterTransltor.getInstance().getTransByKey(
					p.getLocale(),"promoted cross final");
				rankstr=TextKit.replace(rankstr,"%",p.getName());
			}
			else if(rank>CrossWar.S2&&rank<=CrossWar.S64)
			{
				rankstr=InterTransltor.getInstance().getTransByKey(
					p.getLocale(),"promoted cross battle");
				rankstr=TextKit.replace(rankstr,"%",p.getName());
				rankstr=TextKit.replace(rankstr,"%",rank+"");
			}
			else
			{
				text0=InterTransltor.getInstance().getTransByKey(
					p.getLocale(),"anyone join in award");
			}
			if(rank>=CrossWar.S1&&rank<=CrossWar.S64)
			{
				text0=TextKit.replace(text0,"%",rankstr);
			}
			text+=text0+"\n";
			
			StringBuffer pstr=new StringBuffer();
			int[] sids=ad.getPropSid();
			if(sids!=null)
			{
				for(int i=0;i<sids.length;i+=2)
				{
					pstr.append(InterTransltor.getInstance().getTransByKey(
						p.getLocale(),"prop_sid_"+sids[i]));
					pstr.append("*"+sids[i+1]+",");
				}
			}
			sids=ad.getEquipSids();
			if(sids!=null)
			{
				for(int i=0;i<sids.length;i+=2)
				{
					pstr.append(InterTransltor.getInstance().getTransByKey(
						p.getLocale(),"prop_sid_"+sids[i]));
					pstr.append("*"+sids[i+1]+",");
				}
			}
			sids=ad.getShipSids();
			if(sids!=null)
			{
				for(int i=0;i<sids.length;i+=2)
				{
					pstr.append(InterTransltor.getInstance().getTransByKey(
						p.getLocale(),"prop_sid_"+sids[i]));
					pstr.append("*"+sids[i+1]+",");
				}
			}
			sids=ad.getOfficerSids();
			if(sids!=null)
			{
				for(int i=0;i<sids.length;i+=2)
				{
					pstr.append(InterTransltor.getInstance().getTransByKey(
						p.getLocale(),"prop_sid_"+sids[i]));
					pstr.append("*"+sids[i+1]+",");
				}
			}
			if(ad.getGemsAward(1)>0)text=TextKit.replace(text,"%",ad.getGemsAward(1)+"");
			text=TextKit.replace(text,"%",pstr.toString());
		}
		if(gems>=0)
		{
			text+=InterTransltor.getInstance().getTransByKey(
				p.getLocale(),"win bet award")+"\n";
			text=TextKit.replace(text,"%",gems+"");
		}
		if(text.length()>0)
		{
			String sendName=InterTransltor.getInstance().getTransByKey(
				p.getLocale(),"system_mail");
			String title=InterTransltor.getInstance().getTransByKey(
				p.getLocale(),"cross battle award");
			Message message=cfactory.createMessage(0,p.getId(),text,
				sendName,p.getName(),0,title,true);
			JBackKit.sendRevicePlayerMessage(p,message,
				message.getRecive_state(),cfactory);
		}

	}
//	/** 根据玩家是N强 返回奖励sid */
//	public int getSnAwardSid(CrossWar cw,int sn)
//	{
//		// todo
//		return 1;
//	}

	/** 检测发放奖励 */
	public void checkSendAward(Player p)
	{
		IntKeyHashMap betmap=p.getBetmap();
		Integer betwarid=(Integer)betmap.get(0);
		if(betwarid==null) betwarid=0;
		Integer joinwarid=(Integer)betmap.get(1);
		if(joinwarid==null) joinwarid=0;
		if(betwarid==0&&joinwarid==0) return;
		int[] keys=wars.keyArray();
		int awardgem=-1;
		Award ad=null;
		Integer rank=null;
		for(int i=0;i<keys.length;i++)
		{
			CrossWar cw=(CrossWar)wars.get(keys[i]);
			if(warsave!=null&&!warsave.isover()&&cw.getId()==warsave.getId())
				continue;
			if(betwarid==cw.getId()&&cw.isAward()
				&&p.getCrossAwardState(3)==1)
			{
				betmap.remove(0);
				p.setCrossAwardState(3,2);
				Integer gems=(Integer)betmap.get(cw.getCmid());
				gems=gems==null?0:gems;
				awardgem=(int)(gems*cw.getOdds());
				cfactory.createGemTrack(GemsTrack.WIN_BET,p.getId(),
					awardgem,0,Resources.getGems(p.getResources()));
				Resources.addGemsNomal(awardgem,p.getResources(),p);
				cfactory.getPlayerCache().load(p.getId()+"");
			}
			if(joinwarid==cw.getId()&&p.getCrossAwardState(4)==1)
			{
				betmap.remove(1);
				p.setCrossAwardState(4,2);
				rank=(Integer)betmap.get(2);
				rank=rank==null?0:rank;
				Award[] awards=cw.getAwards();
				ad=getRankAward(rank,awards);
				ad.awardLenth(new ByteBuffer(),p,cfactory,null,null,
					new int[]{EquipmentTrack.FROM_CROSSWAR});
				cfactory.getPlayerCache().load(p.getId()+"");
			}

		}
		if(ad!=null||awardgem>=0) messageAward(p,ad,awardgem,rank);
	}
	/** 根据决赛类型反推参赛人数 */
	public int getTotalByMatch(int mtype)
	{
		return CrossWar.S64>>(mtype-1);
	}
	/** 广播 晋级人员 */
	public void sendSn(ByteBuffer data)
	{
		int count=0;
		data.clear();
		data.writeShort(FORE_PORT);
		data.writeByte(SEND_SN);
		int top=data.top();
		data.writeShort(count);
		for(int i=0;i<sn.size();i++)
		{
			ClientWarPlayer cp=(ClientWarPlayer)sn.get(i);
			if(!cp.isSend())
			{
				cp.clientBytesWrite(data);
				cp.setSend(true);
				count++;
			}
		}
		if(count>0)
		{
			int nowtop=data.top();
			data.setTop(top);
			data.writeShort(count);
			data.setTop(nowtop);
			dsmanager.getSessionMap().send(data);
		}
	}
	/** 广播 晋级提示 */
	public void sendShowSn()
	{
		if(showSn.size()>0)
		{
			synchronized(showSn)
			{
				for(int i=0;i<showSn.size();i++)
				{
					// 系统聊天消息
					ShowSn sn=(ShowSn)showSn.get(i);
					String text=null;
					if(sn.getRank()==CrossWar.S1)
					{
						text=InterTransltor.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,
							"win cross battle champion");
						text=TextKit.replace(text,"%",sn.getServername());
//						text=TextKit.replace(text,"%",sn.getName());
						text=SeaBackKit.setSystemContent(text,"%",ChatMessage.SEPARATORS,sn.getName());
					}
					else if(sn.getRank()==CrossWar.S2)
					{
						text=InterTransltor.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,
							"promoted cross final");
//						text=TextKit.replace(text,"%",sn.getName());
						text=SeaBackKit.setSystemContent(text,"%",ChatMessage.SEPARATORS,sn.getName());
					}
					else if(sn.getRank()==CrossWar.S64)
					{
						text=InterTransltor.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,
							"promoted cross battle finals");
//						text=TextKit.replace(text,"%",sn.getName());
						text=SeaBackKit.setSystemContent(text,"%",ChatMessage.SEPARATORS,sn.getName());
					}
					else
					{
						text=InterTransltor.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,"promoted cross battle");
//						text=TextKit.replace(text,"%",sn.getName());
						text=SeaBackKit.setSystemContent(text,"%",ChatMessage.SEPARATORS,sn.getName());
						text=TextKit.replace(text,"%",sn.rank+"");
					}
					ChatMessage mes=new ChatMessage();
					mes.setType(ChatMessage.SYSTEM_CHAT);
					mes.setSrc("");
					mes.setText(text);
					SeaBackKit.sendAllMsg(mes,dsmanager,false);
					// JBackKit.sendScrollMessage(dsmanager,text);

					// 存信息
					ChatMessagePort chatPort=(ChatMessagePort)BackKit
						.getContext().get("chatMessagePort");
					ChatMessage message=new ChatMessage();
					message.setType(mes.getType());
					message.setTime(TimeKit.getSecondTime());
					message.setText(mes.getText());
					chatPort.numFiler();
					chatPort.getChatMessages().add(message);
				}
				showSn.clear();
			}
		}
	}

	/** 广播 活动进度，战报，晋级人员 */
	public void broadCast(ByteBuffer data)
	{
		if(warsave==null)return;
		// 刷新奖励内容
		flushAward();
		// 设置领奖状态
		setAwardState();
		//设置预赛标记
		setPreState();
		// //
		sendSn(data);
		sendFinRep(data);
		sendShowSn();
		openAct(data);
		sendAct(data);
		sendAwardSate(data);
		sendFluhAward(data);
		sendFinNowCP(data);
		forceAward();
		sendPreRep(data);
		saveRep(false);
	}

	/** 获取n强位置id(奇葩位) */
	public int[] getSnId(int totalNum)
	{
		int[] locs=new int[totalNum];
		for(int k=0;k<totalNum;k++)
		{
			ClientWarPlayer cp=getIndexNSn(k,totalNum);
			if(cp!=null)
			{
				locs[k]=cp.getCrossid();
			}
			else
			{
				locs[k]=0;
			}
		}

		for(int i=0;i<locs.length;i+=4)
		{
			if(i+3>=locs.length) break;
			int loc1=locs[i+1];
			int loc2=locs[i+2];
			locs[i+1]=loc2;
			locs[i+2]=loc1;
		}

		return locs;
	}

	/** 第n个n强玩家 */
	public ClientWarPlayer getIndexNSn(int n,int snn)
	{
		for(int i=0,j=0;i<sn.size();i++)
		{
			ClientWarPlayer cp=(ClientWarPlayer)sn.get(i);
			if(cp.getRank()<=snn&&j==n)
			{
				return cp;
			}
			if(cp.getRank()<=snn) j++;
		}
		return null;
	}
	/** 获取第n个n强战报 */
	public CrossWarRoundSave getIndexNRep(int n,int matchType)
	{
		for(int i=0,j=0;i<finalrep.size();i++)
		{
			CrossWarRoundSave save=(CrossWarRoundSave)finalrep.get(i);
			if(save.getType()==matchType&&j==n)
			{
				return save;
			}
			if(save.getType()==matchType)
			{
				j++;
			}
		}
		return null;
	}
	/** 合并战报 */
	public void combinRep(ByteBuffer data,CrossWarRoundSave save,int pid,
		boolean isattack,boolean isfinal)
	{
		data.clear();
		SeaBackKit.crossConFightRecord(data,save,pid,isattack,isfinal);
	}
	/** 获取战报 */
	public void getRep(Player player,int id,boolean isattack,ByteBuffer data)
	{
		Object[] objs=getRepById(id);
		CrossWarRoundSave save=(CrossWarRoundSave)objs[0];
		if(save==null)
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"no the rep"));
		combinRep(data,save,player.getId(),isattack,(Boolean)objs[1]);
	}

	/** 根据唯一id 获取战报 */
	public Object[] getRepById(int id)
	{
		Object[] objs=new Object[2];
		objs[1]=false;
		for(int i=0;i<finalrep.size();i++)
		{
			CrossWarRoundSave save=(CrossWarRoundSave)finalrep.get(i);
			if(save.getId()==id)
			{
				objs[0]=save;
				objs[1]=true;
			}
		}
		if(objs[0]==null)
		{
			for(int i=0;i<prerep.size();i++)
			{
				CrossWarRoundSave save=(CrossWarRoundSave)prerep.get(i);
				if(save.getId()==id)
				{
					objs[0]=save;
				}
			}
		}
		return objs;
	}
	/** 检测押注量 */
	public boolean checkBetGems(int vip,int gem)
	{
		int[] vpbet=warsave.getVipbet();
		for(int i=0;i<vpbet.length;i+=2)
		{
			if(gem==vpbet[i]&&vip>=vpbet[i+1]) return true;
		}
		return false;
	}

	/** 押注 */
	public void bet(Player player,ByteBuffer data)
	{
		if(warsave==null)
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"not open cross war"));
		}
		if(!warsave.betCan())
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"bet cannot"));
		}
		int warid=warsave.getId();
		if(!player.canBet(warid))
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"had bet"));
		}
		int cid=data.readInt();
		int gems=data.readInt();
		if(!checkBetGems(player.getUser_state(),gems))
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"vip gems error"));
		}
		ClientWarPlayer cp=getSnPlayer(cid);
		if(cp==null)
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"no this cplayer"));
		}
		if(!Resources.checkGems(gems,player.getResources()))
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"gems limit"));
		}
		if(!Resources.reduceGemsOnly(gems,player.getResources()))
		{
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,"gems limit"));
		}
		log.error(player.getId()+":---id----bet--to---cid-----:"+cid
			+":-----gems---:"+gems);
		data.clear();
		data.writeShort(CrossAct.WAR_SID);
		data.writeByte(BET);
		data.writeShort(UserToCenterPort.SERVER_ID);
		data.writeShort(UserToCenterPort.AREA_ID);
		data.writeShort(UserToCenterPort.PLAT_ID);
		data.writeInt(cid);
		data.writeInt(gems);
		ByteBuffer bb=sendHttpData(data,cross_ip,cross_port);
		if(bb!=null)
		{
			int type=bb.readByte();
			if(type==BET_SUCC)
			{
				log.error(player.getId()+":-------BET_SUCC----------");
				JBackKit.sendGemChange(player,false,gems);
				JBackKit.sendResetResources(player);
				ConsumeGemsActivity act=(ConsumeGemsActivity)ActivityContainer
					.getInstance().getActivity(
						ActivityContainer.CONSUME_GEMS_ID,0);
				if(act!=null&&act.isOpen(TimeKit.getSecondTime()))
					act.addRecord(player,gems);
				cfactory.createGemTrack(GemsTrack.CROSSWAR_BET,
					player.getId(),gems,0,
					Resources.getGems(player.getResources()));
				player.bet(warid,cid,gems);// 押注
				cp.addBet(gems);
				data.clear();
				data.writeShort(1);
				data.writeInt(cid);
				data.writeInt(player.getBet(cid));
			}
			else
			{
				log.error(player.getId()
					+":-------BET_FAIL----return-----gems-:"+gems
					+":--failtype---:"+type);
				Resources.addGemsNomalOnly(gems,player.getResources());
				if(type==BET_NOACT)
				{
					throw new DataAccessException(0,InterTransltor
						.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,"not open cross war"));
				}
				else if(type==BET_CANNOT)
				{
					throw new DataAccessException(0,InterTransltor
						.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,"bet cannot"));
				}
				else
				{
					throw new DataAccessException(0,InterTransltor
						.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,"no this cplayer"));
				}

			}
		}
		else
		{
			Resources.addGemsNomalOnly(gems,player.getResources());
			log.error(player.getId()
				+":-------BET_Exception----return-----gems-:"+gems);
			throw new DataAccessException(0,InterTransltor.getInstance()
				.getTransByKey(PublicConst.SERVER_LOCALE,
					"cannot connect cross_server"));
		}

	}
	/** 序列化玩家舰队 */
	public void showBytesWriteFleet(Player p,ByteBuffer data)
	{
		ClientWarPlayer cp=getCplayer(p.getId());
		if(cp==null)
		{
			data.writeByte(0);
			data.writeByte(0);
		}
		else
		{
			cp.bytesWriteDefencelist(data);
			cp.bytesWriteAttacklist(data);
		}
	}
	/** 序列化战报数据 */
	public void showBytesWriteRep(Player player,boolean isfinal,
		ByteBuffer data)
	{
		if(isfinal)
		{
			if(sn.size()<=0)
			{
				int matchLen=4;// 4种比赛类型
				data.writeShort(matchLen);
				for(int i=0;i<matchLen;i++)
				{
					int matchType=i+1;
					int totalNum=64>>i;
					int repNum=totalNum/2;

					data.writeShort(matchType);
					data.writeInt(totalNum);
					for(int k=0;k<totalNum;k++)
					{
						data.writeInt(0);
					}
					data.writeInt(repNum);
					for(int k=0;k<repNum;k++)
					{
						CrossWarRoundSave.defClientRepWrite(data);
					}
				}
			}
			else
			{
				int matchLen=4;// 4种比赛类型
				data.writeShort(matchLen);
				for(int i=0;i<matchLen;i++)
				{
					int matchType=i+1;
					int totalNum=CrossWar.S64>>i;
					int repNum=totalNum/2;
					if(totalNum==CrossWar.S8)
					{
						repNum=CrossWar.S8-1;
					}
					data.writeShort(matchType);
					data.writeInt(totalNum);
					// test
					// for(int m=0;m<sn.size();m++)
					// {
					// ClientWarPlayer cp=(ClientWarPlayer)sn.get(m);
					// //System.out.println(m+":-----:"+cp.getName()+":------cp---00---:"+cp.getNum()+":---rank---:"+cp.getRank()+":--cid---:"+cp.getCrossid());
					// }
					int[] ids=getSnId(totalNum);
					for(int k=0;k<totalNum;k++)
					{
						data.writeInt(ids[k]);
					}
					data.writeInt(repNum);
					for(int k=0;k<repNum;k++)
					{
						CrossWarRoundSave save=getIndexNRep(k,matchType);
						if(save!=null)
						{
							save.clientBytesWrite(player.getId(),data,true);
						}
						else
						{
							CrossWarRoundSave.defClientRepWrite(data);
						}
					}
				}
			}
		}
		else
		{
			int top=data.top();
			int count=0;
			data.writeShort(count);
			int id=player.getId();
			for(int i=0;i<prerep.size();i++)
			{
				CrossWarRoundSave save=((CrossWarRoundSave)prerep.get(i));
				if(!save.belong(id)) continue;
				save.clientBytesWrite(id,data,false);
				count++;
			}
			if(count>0)
			{
				int nowtop=data.top();
				data.setTop(top);
				data.writeShort(count);
				data.setTop(nowtop);
			}

		}

	}
	/** 序列化决赛圈玩家 */
	public void showBytesWriteFnPlayer(ByteBuffer data)
	{
		if(sn.size()>0)
		{
			data.writeShort(sn.size());
			for(int i=0;i<sn.size();i++)
			{
				((ClientWarPlayer)sn.get(i)).clientBytesWrite(data);
			}
		}
		else
		{ // 默认填坑数据（前台需要）
			data.writeShort(CrossWar.S64);
			ClientWarPlayer cp=new ClientWarPlayer();
			for(int i=0;i<CrossWar.S64;i++)
			{
				cp.clientBytesWrite(data);
			}

		}
	}
	/** 序列化押注量 */
	public void showBytesWriteBet(Player p,ByteBuffer data)
	{
		IntKeyHashMap betmap=p.getBetmap();
		Integer warid=(Integer)betmap.get(0);
		if(warsave==null||warid==null||warsave.getId()!=warid)
		{
			data.writeShort(0);
		}
		else
		{
			int[] keys=betmap.keyArray();
			int len=keys.length;
			int top=data.top();
			data.writeShort(len);
			for(int i=0;i<keys.length;i++)
			{
				if(keys[i]==0||keys[i]==1||keys[i]==2||keys[i]==3
					||keys[i]==4||keys[i]==5||keys[i]==6)
				{
					len--;
					continue;
				}
				data.writeInt(keys[i]);// id
				data.writeInt((Integer)betmap.get(keys[i]));// 押注量
			}
			int nowtop=data.top();
			data.setTop(top);
			data.writeShort(len);
			data.setTop(nowtop);
		}
	}
	/** 个人信息 */
	public void writeSelfInfo(Player player,ByteBuffer data)
	{
		ClientWarPlayer cp=getCplayer(player.getId());
		data.writeInt(cp==null?0:cp.getCrossid());
	}
	/** 领奖信息 */
	public void writeAwardSate(Player player,ByteBuffer data)
	{
		if(warsave==null||!warsave.isAward())
		{
			data.writeByte(0);// 压住 0，1，2 不可能领，可领，已领
			data.writeByte(0);
		}
		else
		{
			data.writeByte(player.getCrossAwardState(3));// 压住 0，1，2
															// 不可能领，可领，已领
			data.writeByte(player.getCrossAwardState(4));// 排名 0，1，2
															// 不可能领，可领，已领
		}
	}
	/** 奖励信息 */
	public void writeAward(Player player,ByteBuffer data)
	{
		// enum CROSS_SERVER_REWARD_TYPE //奖励详细类型
		// {
		// REWARD_TYPE_NONE = -1,// 无
		// REWARD_TYPE_BET =0,//押注
		// REWARD_TYPE_RANK_1 = 1,// 冠军
		// REWARD_TYPE_RANK_2 = 2,// 亚军
		// REWARD_TYPE_RANK_4 = 3,// 4强
		// REWARD_TYPE_RANK_8 = 4,// 8强
		// REWARD_TYPE_RANK_16= 5,//16强
		// REWARD_TYPE_RANK_32= 6,//32强
		// REWARD_TYPE_RANK_64= 7,//64强
		// REWARD_TYPE_JOIN =8 //参与奖
		// };
		if(warsave==null)
		{
			int len=9;
			data.writeShort(len);// 奖励种类数量
			for(int i=0;i<len;i++)
			{
				data.writeShort(i);// 奖励类型
				Award ad=(Award)Award.factory.getSample(CrossWar.EMPTY_SID);
				ad.viewAward(data,player);
			}

		}
		else
		{
			Award[] ads=warsave.getAwards();
			int len=ads==null?0:ads.length+1;
			data.writeShort(len);// 奖励种类数量
			int gem=player.getBet(warsave.getCmid());
			Award ad=(Award)Award.factory.newSample(CrossWar.EMPTY_SID);
			ad.setGemsAward((int)(gem*warsave.getOdds()));
			data.writeShort(0);
			ad.viewAward(data,player);
			if(ads==null)
			{
				for(int i=1;i<=8;i++)
				{
					data.writeShort(i);// 奖励类型
					ad=(Award)Award.factory.getSample(CrossWar.EMPTY_SID);
					ad.viewAward(data,player);
				}
			}
			else
			{
				for(int i=1;i<=ads.length;i++)
				{
					data.writeShort(i);// 奖励类型
					ads[i-1].viewAward(data,player);
				}
			}
		}
	}

	/** 序列化跨服活动相关数据 */
	public void showBytesWriteWar(Player p,ByteBuffer data)
	{
		checkSendAward(p);// 检测发放奖励
		if(warsave==null||warsave.isover())
		{
			data.writeBoolean(true);
			data.writeBoolean(false);
			// data.writeBoolean(false);
			return;
		}
		data.writeBoolean(true);
		warsave.showClientBytesWrite(data);// 活动信息
		showBytesWriteFleet(p,data);// 出征舰队
		showBytesWriteRep(p,false,data);// 预赛战报
		showBytesWriteFnPlayer(data);// 决赛圈玩家
		showBytesWriteRep(p,true,data);// 决赛战报
		showBytesWriteBet(p,data);// 押注数据
		writeSelfInfo(p,data);// 个人跨服信息
		writeAwardSate(p,data);// 领奖信息
		writeAward(p,data);// 奖励
		data.writeUTF(server_name==null?" ":server_name);// 玩家自己的服务器名
		data.writeUTF(national==null?" ":national);// 自己的国家名
		data.writeBoolean(p.getPreState(warsave.getId()));// 是否需要显示预选赛标签上的提示叹号

	}
	public ByteBuffer sendHttpData(ByteBuffer data,String ip,String port)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// 设置port
		map.put("port",cross_actionPort);
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+ip+":"+port+"/","POST",map,null);
		}
		catch(IOException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}

	@Override
	public void onTimer(TimerEvent e)
	{
	/*	if(e.getParameter().equals("getact"))
		{
			boolean get=false;
			try
			{
				if(gettry>1)
				{
					get=getWars();
				}
				gettry++;
			}
			catch(Exception e2)
			{
				e2.printStackTrace();
			}
			if(get||gettry>getmax)
			{
				TimerCenter.getSecondTimer().remove(e);
			}
		}
		else if(e.getParameter().equals("submit"))
		{
			if(submitPlayer())
			{
				TimerCenter.getMinuteTimer().remove(e);
			}
		}
		else if(e.getParameter().equals("getserver"))
		{
			getLocalServer();
		}*/
	}

	/** 存储战报 */
	public int saveRep(boolean force)
	{
		if(force)
		{
			savemax=prerep.size();
		}
		int fail=0;
		for(int num=0;presaveIn<prerep.size()&&num<savemax;presaveIn++,num++)
		{
			fail+=cwsDBAccess.save(prerep.get(presaveIn))?0:1;
		}
		return fail;
	}
	/** 服战结束强制奖励 */
	public void forceAward()
	{
		if(warsave==null||warsave.isForceAward()) return;
		int now=TimeKit.getSecondTime();
		if(warsave.getCheckTime()<warsave.getEtime()
			&&now>=warsave.getEtime())
		{
			warsave.setForceAward(true);
			Object[] objs=cfactory.getPlayerCache().getCacheMap()
				.valueArray();
			if(objs==null) return;
			for(int i=0;i<objs.length;i++)
			{
				if(objs[i]==null) continue;
				PlayerSave save=(PlayerSave)objs[i];
				Player p=save.getData();
				if(p==null) continue;
				checkSendAward(p);
			}
		}
		warsave.setCheckTime(now);
	}
}
