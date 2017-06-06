package foxu.sea.gm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import mustang.back.BackKit;
import mustang.codec.MD5;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.MathKit;
import mustang.net.Session;
import mustang.orm.SqlKit;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.ds.DSManager;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import shelby.httpserver.HttpRequestMessage;
import shelby.serverOnMina.server.MinaConnectServer;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.OrderGameDBAccess;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.dcaccess.mem.GameDataMemCache;
import foxu.ds.PlayerKit;
import foxu.email.EmailManager;
import foxu.sea.GameData;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PlayerAdvice;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.Science;
import foxu.sea.Ship;
import foxu.sea.User;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.builds.Build;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.Island;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.Troop;
import foxu.sea.builds.produce.ScienceProduce;
import foxu.sea.charge.VerifyHeroChargeInfoManager;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.fight.Skill;
import foxu.sea.gems.GemsTrack;
import foxu.sea.gm.operators.CheckOld;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;
import foxu.sea.order.Order;
import foxu.sea.port.UserToCenterPort;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.shipdata.ShipCheckData;
import foxu.sea.shipdata.ShipDataEvent;

/** GM 工具管理器 */
public class GMSetManager implements HttpHandlerInterface
{

	/** 所有联盟技能sid */
	public final int ALLIANCE_SKILLS_SID[]={501,502,503,504,505,506,507,508,
		509,510};

	/** 科技对应的名字 */
	public final String SCIENC_FOR_NAME[]={"sample_tech_name_01","加强型舰炮",
		"sample_tech_name_02","战列舰维护","sample_tech_name_03","破甲鱼雷",
		"sample_tech_name_04","潜艇维护","sample_tech_name_05","高爆弹头",
		"sample_tech_name_06","巡洋舰维护","sample_tech_name_07","制导炸弹",
		"sample_tech_name_08","航母维护","sample_tech_name_15","铁矿冶炼",
		"sample_tech_name_16","石油分馏","sample_tech_name_17","硅矿冶炼",
		"sample_tech_name_18","铀矿提纯","sample_tech_name_19","铸币术",
		"sample_tech_name_20","战斗经验","sample_tech_name_21","战斗掠夺",
		"sample_tech_name_22","高速航行","sample_tech_name_23","建筑学",
		"sample_tech_name_24","载重","sample_tech_name_25","储存技术"};

	/** 船只事件类型 */
	public final String FIGHT_EVENT_STYLE[]={"造船完成","取消造船","世界战斗","修理船只",
		"攻打关卡","攻打NPC岛屿","攻打玩家岛屿","船坞回港","升级船只","基地被攻打","驻守野地被攻打","GM添加",
		"联盟协防","随机奖励"};
	/***/
	public final String SHIP_FOR_NAME[]={"10001","轻型战列舰","10002","中型战列舰",
		"10003","重型战列舰","10004","野熊战列舰","10005","壁垒战列舰","10006","泰坦战列舰",
		"10007","无畏壁垒","10011","轻型潜艇","10012","中型潜艇","10013","重型潜艇","10014",
		"幽灵潜艇","10015","死神潜艇","10016","冥王潜艇","10017","厄运死神","10021","轻型巡洋舰",
		"10022","中型巡洋舰","10023","重型巡洋舰","10024","猛禽巡洋舰","10025","神盾巡洋舰",
		"10026","宙斯巡洋舰","10027","雷霆神盾","10031","轻型航母","10032","中型航母",
		"10033","重型航母","10034","龙崖航母","10035","帝国之刃","10036","战神之刃","10037",
		"炙炎之刃"};

	/** 建筑名对应的名字 */
	public final String BUILD_FOR_NAME[]={"sample_build_name_01","金属矿",
		"sample_build_name_02","石油矿","sample_build_name_03","硅矿",
		"sample_build_name_04","铀矿","sample_build_name_05","金币",
		"sample_build_name_06","船厂","sample_build_name_07","指挥中心",
		"sample_build_name_08","研究院","sample_build_name_09","制造车间",
		"sample_build_name_10","","sample_build_name_14","联盟建筑"};

	/** 宝石消费type */
	public final String GEMS_TRACK_TYPE[]={"建筑加速","生产加速","购买能量","购买物品",
		"购买建筑队列","修复船只","刷新和直接完成每日任务","充值宝石","系统产出宝石","GM工具添加宝石","战斗事件加速",
		"邀请奖励","每日领取宝石","联盟捐献","抽奖","岛屿搬迁","环球军演加速","重置虚空","准备充值","取消充值",
		"全服补偿"};

	public final String GEMS_BUILD_NUM[]={"第三个建筑位","第四个建筑位","第五个建筑位"};

	public final String INDEX_FOR_BUILD_NAME[]={"指挥中心","金币","研究院","仓库","仓库",
		"制造车间","联盟建筑","空军基地","导弹基地","火炮阵地","船厂","船厂","船厂","资源地","资源地"};

	/** html头信息 */
	public static final String HTML_HEAD="<html xmlns='http://www.w3.org/1999/xhtml'><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8' /><title>GM工具</title></head><body>";
	/** 日志记录 */
	private static final Logger log=LogFactory.getLogger(GMSetManager.class);
	/** 返回常量 成功 失败 */
	public final static String OK="ok",FAILS="fails";
	/**
	 * 常量USER=1账号 PLAYER=2岛屿 FIGHT_EVENT=3战斗事件 MESSAGES=4邮件 SYSTEM=5系统信息
	 * VIEM_GEMS=6宝石查看 player_advice=7玩家建议,VIEW_PLAYER_ADVICE=8查看还未处理的玩家建议
	 * VIEW_GAME_DATA=查看运营数据 关闭服务器CLOSE_SERVER=14
	 * SET_NEW_TASK_MARK=16设置新手任务的步数CHANGGE_PASSWORD修改密码
	 */
	public final static int USERS=1,PLAYERS=2,FIGHT_EVENTS=3,MESSAGES=4,
					SYSTEM=5,VIEM_GEMS=6,PLAYER_ADVICE=7,
					VIEW_PLAYER_ADVICE=8,DELETE_PLAYER_REDIS=9,
					VIEW_GAME_DATA=10,VIEW_GEMS_DATA=11,
					VIEW_GEMS_PLAYER_DATA=12,SEND_ALL_SYSTEM_MESSAGE=13,
					CLOSE_SERVER=14,VIEW_PLAYER_INFO=15,
					SET_NEW_TASK_MARK=16,BANED_AND_MUTIME=17,
					SYSTEM_CHAT_MESSAGE=18,UPDATE_SCEIEN_LEVEL=19,
					PLAYER_GM=20,VIEW_ONLINE_PLAYER=21,VIEW_FIGHT_EVENT=22,
					BUY_GEMS_INFO=23,BUY_GEMS_MORE_INFO=24,
					VIEW_PLAYER_SHIP_DATA=25,ALLIANCE_CHANGE=26,
					CHANGGE_PASSWORD=27,VIEW_PLAYER_GEMS=28,
					CHECK_PLAYER_ORDER=29,OPEN_GEMS_BUY=30,
					ALL_PLAYER_GIVE_GEMS=31,TAKE_OVER_PLAYER=32,
					VIEW_SYSTEM_MAIL=33,REMOVE_SYSTEM_MAIL=34,
					SET_SIMPLE_ACTIVITY=35,SET_ACTIVITY=36,BANNED_DEVICE=37,
					RECALL_EMAIL=38,SHOW_GM_OP=39;
	/**
	 * PLAYER_ADD_RESOUCRE=1增加资源 PLAYER_ADD_SHIPS=2增加船只
	 * PLAYER_ADD_BUILD=3增加一个建筑 PLAYER_UP_BUIL=4建筑升级 增加经验=5
	 */
	public final static int PLAYER_ADD_RESOUCRE=1097,PLAYER_ADD_SHIPS=2,
					PLAYER_ADD_BUILD=3,PLAYER_UP_BUILD=4,
					PLAYER_ADD_EXPERIENCE=5,ISLAND_LEVEL=6,ADD_PROP=7;
	/** 船舰类型和等级对应的sid */
	public final static int SHIPS_SIDS[][]={
		{10001,10002,10003,10004,10005,10006,10007},
		{10011,10012,10013,10014,10015,10016,10017},
		{10021,10022,10023,10024,10025,10026,10027},
		{10031,10032,10033,10034,10035,10036,10037}};
	
	/** 网页表单标签多值分隔符 */
	public final static char MULTI_VALUE_SEPARATOR='&';
	/** 创建对象管理器 */
	CreatObjectFactory objectFactory;

	DSManager manager;

	GMManager gmManager;

	public void setGMManager(GMManager gmManager)
	{
		this.gmManager=gmManager;
	}

	/** 获取多值标签的值集 */
	public static String[] getMultiValues(String key,Map<String,String> parameters)
	{
		String value=parameters.get(key);
		return TextKit.split(value,MULTI_VALUE_SEPARATOR);
	}
	
	private Map<String,String> parseHeaders(HttpRequestMessage request)
	{
		Map<String,String> parameters=new HashMap<String,String>();

		Iterator<Entry<String,String[]>> iter=request.getHeaders()
			.entrySet().iterator();

		while(iter.hasNext())
		{
			Entry<String,String[]> entry=iter.next();
			String key=entry.getKey();
			// 连接参数是以'@'开头的
			if(key==null||key.length()<=0||key.charAt(0)!='@') continue;
			key=key.substring(1);
			String[] value=entry.getValue();
			if(value!=null&&value.length>0)
			{
				try
				{
					StringBuffer sb=new StringBuffer(value[0]);
					// 如果有多个值用分隔符进行组合以兼容多选框
					for(int i=1;i<value.length;i++)
						sb.append(MULTI_VALUE_SEPARATOR+value[i]);
					parameters.put(key,URLDecoder.decode(sb.toString(),"UTF-8"));
				}
				catch(UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
			else
				parameters.put(key,"");
		}
		return parameters;
	}

	public String callGMManager(HttpRequestMessage request)
	{
		Map<String,String> params=parseHeaders(request);
		String userName=params.get("gmusername");
		String password=params.get("gmpassword");
		String command=params.get("command");
		params.remove("gmusername");
		params.remove("gmpassword");
		MD5 md5=new MD5();
		password=md5.encode(password);
		return gmManager.process(userName,password,command,params);
	}

	/** 执行GM命令 */
	public String excuteString(HttpRequestMessage request,String ip)
	{
		String ret=callGMManager(request);
		if(ret==null)
			return "{\"success\":"+GMConstant.ERR_PASSWORD_ERROR
				+",\"msg\":\"账号或者密码错误。\"}";
		else if(ret.length()>0) return ret;
		// 操作那张表
		String type=request.getParameter("table_type");
		if(!CheckOld.IS_OPEN&&!type.equals("11"))
		{
			return "{'success':-2}";
		}
		String str="null";
		try
		{
			if(Integer.parseInt(type)==PLAYERS)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory.getPlayerByName(playerName,true);
				if(player==null) return "player is null";
				str=excutePlayerGm(request,player);
			}
			else if(Integer.parseInt(type)==SYSTEM_CHAT_MESSAGE)
			{
				// 玩家名字
				String text=URLDecoder.decode(request
					.getParameter("chatText"),"utf-8");
				str+="message is ="+text;
				// 聊天消息
				ChatMessage message=new ChatMessage();
				// type
				message.setType(ChatMessage.SYSTEM_CHAT);
				message.setSrc("");
				message.setText(text);
				SeaBackKit.sendAllMsg(message,manager,false);
			}
			else if(Integer.parseInt(type)==SYSTEM)
			{
				int kb=1024;
				// 可使用内存
				long totalMemory=Runtime.getRuntime().totalMemory()/kb/kb;
				// 剩余内存
				long freeMemory=Runtime.getRuntime().freeMemory()/kb/kb;
				// 最大可使用内存
				long maxMemory=Runtime.getRuntime().maxMemory()/kb/kb;
				str="totalMemory:"+totalMemory+"mb,freeMemory:"+freeMemory
					+"mb,maxMemory:"+maxMemory+"mb";
			}
			// 玩家
			else if(Integer.parseInt(type)==USERS)
			{

			}
			// 事件
			else if(Integer.parseInt(type)==FIGHT_EVENTS)
			{

			}
			// 邮件
			else if(Integer.parseInt(type)==MESSAGES)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory.getPlayerByName(playerName,true);
				if(player==null) return "player is null";
				String sendName=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"system_mail");
				int sendId=0;
				int reciveId=player.getId();
				/** 奖励品sid */
				String awardSid=request.getParameter("awardSid");
				str="system message,player_name:"+player.getName()
					+",player_award_add:awardSid="+awardSid;
				Award award=(Award)Award.factory.getSample(Integer
					.parseInt(awardSid));
				if(award!=null)
					award.awardSelf(player,TimeKit.getSecondTime(),null,
						null,"",new int[]{EquipmentTrack.FROM_GM_ADD});
				// MessageData messageData=new MessageData();
				// // 金属
				// String metal=request.getParameter("metal");
				// // 石油
				// String oil=request.getParameter("oil");
				// // 硅
				// String silicon=request.getParameter("silicon");
				// // 低铀
				// String uranium=request.getParameter("uranium");
				// // 金钱
				// String money=request.getParameter("money");
				// // 宝石
				// String gems=request.getParameter("gems");
				//
				// String honor=request.getParameter("honor");
				// int resource[]={Integer.parseInt(metal),
				// Integer.parseInt(oil),Integer.parseInt(silicon),
				// Integer.parseInt(uranium),Integer.parseInt(money),
				// Integer.parseInt(gems),0};
				// str="system message,player_name:"+player.getName()
				// +",player_resource_add:metal="+metal+",oil="+oil
				// +",silicon="+silicon+",uranium="+uranium+",money="+money
				// +",gems="+gems+",honor="+honor;
				// // 物品sid
				// String propSid=request.getParameter("propSid");
				// if(propSid!=null&&!propSid.equals(""))
				// {
				// String props[]=TextKit.split(propSid,"-");
				// int messageProps[]=new int[propSid.length()];
				// str+=",add_props:";
				// for(int i=0;i<props.length;i++)
				// {
				// messageProps[i]=Integer.parseInt(props[i]);
				// str+=messageProps[i]+",";
				// }
				// messageData.setPropsid(messageProps);
				// }
				//
				// // 船sid
				// String shipSid=request.getParameter("shipSid");
				// if(shipSid!=null&&!shipSid.equals(""))
				// {
				// String ships[]=TextKit.split(shipSid,"-");
				// int messageShips[]=new int[shipSid.length()];
				// str+="add_ships:";
				// for(int i=0;i<ships.length;i++)
				// {
				// messageShips[i]=Integer.parseInt(ships[i]);
				// str+=messageShips[i]+",";
				// }
				// messageData.setShipSids(messageShips);
				// }
				String content=URLDecoder.decode(request
					.getParameter("content"),"utf-8");
				// messageData.setResources(resource);
				String reciviceName=player.getName();
				String title=URLDecoder.decode(
					request.getParameter("title"),"utf-8");
				Message message=objectFactory.createMessage(0,reciveId,
					content,sendName,reciviceName,0,title,true);
				// 刷新前台
				JBackKit.sendRevicePlayerMessage(player,message,message
					.getRecive_state(),objectFactory);
			}
			// 查看总共充值宝石
			else if(Integer.parseInt(type)==VIEM_GEMS)
			{
				int maxGems=objectFactory.getGems();
				str="the allGems is="+maxGems;
			}
			// 玩家建议
			else if(Integer.parseInt(type)==PLAYER_ADVICE)
			{
				// 建议ID
				String id=request.getParameter("adviceId");
				PlayerAdvice advice=objectFactory.getPlayerAdvice(id);
				if(advice==null) return "fail";
				if(advice.getState()!=0)
					return "this advice has been responed";
				// GM回复
				String gmResponse=URLDecoder.decode(request
					.getParameter("gmResponse"),"utf-8");
				// String gems=request.getParameter("gmGems");
				// GM增送宝石
				// str="gm advice,player_name:"+advice.getPlayerName()
				// +"send gems:"+gems+",response:"+gmResponse;
				// MessageData messageData=new MessageData();
				// int gem=0;
				// if(gems!=null&&!gems.equals(""))
				// {
				// gem=Integer.parseInt(gems);
				// int resources[]={0,0,0,0,0,gem,0};
				// messageData.setResources(resources);
				// }
				// String playeradvice="您的提问:"+advice.getContent()+"\n\n"
				// +"客服回复："+gmResponse;
				// // 存储玩家建议
				// objectFactory.gmResponse(id,gmResponse);
				// // 给玩家发送消息
				// objectFactory.createMessage(0,advice.getPlayerId(),
				// playeradvice,"帝国舰队运营团队",advice.getPlayerName(),0,
				// "非常感谢您的来信",objectFactory);
			}
			else if(Integer.parseInt(type)==VIEW_PLAYER_ADVICE)
			{
				PlayerAdvice advice[]=objectFactory.getPlayerAdvice();
				str=HTML_HEAD
					+"<table width='1224' border='5'><tr><td>建议ID</td><td>玩家名字</td><td>玩家ID</td><td>标题</td><td>内容</td><td>创建时间</td></tr>";
				SimpleDateFormat format=new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
				for(int i=0;i<advice.length;i++)
				{
					int creatTime=advice[i].getCreatTime();
					long time=creatTime*1000l;
					str+="<tr>";
					str+="<td>"+advice[i].getId()+"</td>";
					str+="<td>"+advice[i].getPlayerName()+"</td>";
					str+="<td>"+advice[i].getPlayerId()+"</td>";
					str+="<td>"+advice[i].getTitile()+"</td>";
					str+="<td>"+advice[i].getContent()+"</td>";
					str+="<td>"+format.format(new Date(time))+"</td>";
					str+="</tr>";
				}
				str+="</table></body></html>";
				return str;
			}
			else if(Integer.parseInt(type)==VIEW_GAME_DATA)
			{
				long time=TimeKit.getMillisTime();
				GameDataMemCache save=objectFactory.getGameDataCache();
				int year=Integer.parseInt(request.getParameter("year"));
				int month=Integer.parseInt(request.getParameter("month"));
				if(save.getSave().getData().getThe_year()==year
					&&save.getSave().getData().getThe_month()==month)
					save.updata(true,GameDataMemCache.FORCE_DB_TIME);
				long checkTime=(TimeKit.getMillisTime()-time);
				ArrayList list=save.getMonthData(year,month,save.getSave().getData().getPlat());
				Object listArray[]=list.toArray();
				Object temp;
				// 排序 日期排序
				for(int i=0;i<listArray.length;i++)
				{
					for(int j=i;j<listArray.length;j++)
					{
						GameData dataA=(GameData)listArray[i];
						GameData dataB=(GameData)listArray[j];
						if(dataA.getThe_day()>dataB.getThe_day())
						{
							temp=listArray[i];
							listArray[i]=listArray[j];
							listArray[j]=temp;
						}
					}
				}
				str=HTML_HEAD
					+"数据库查询时间:"
					+checkTime
					+"毫秒<hr/>"
					+"<table width='1924' border='5'><tr><td>日期</td><td>新增用户</td><td>新增udid</td><td>DAU</td><td>充值额度</td><td>充值人数</td><td>MAU</td><td>最高同时在线</td><td>隔日留存率</td><td>周留存率</td><td>双周留存率</td><td>月留存率</td><td>ARPU</td><td>ARPPU</td><td>用户总数</td><td>充值用户总数</td><td>付费率</td><td>累计充值额度</td><td>全用户arpu</td><td>ARPPU</td><td>流失率(日)</td><td>流失率(周)</td><td>当前在线</td></tr>";
				for(int i=0;i<listArray.length;i++)
				{
					GameData data=(GameData)listArray[i];
					save.Calculation(data);
					// 第二天流失率
					float last_day_rate=data.getLast_day_rate()/100f;
					last_day_rate=(float)(Math.round(last_day_rate*100))/100;

					float week_rate=data.getWeek_rate()/100f;
					week_rate=(float)(Math.round(week_rate*100))/100;

					float doublu_week_rate=data.getDoublu_week_rate()/100f;
					doublu_week_rate=(float)(Math
						.round(doublu_week_rate*100))/100;

					float month_rate=data.getMonth_rate()/100f;
					month_rate=(float)(Math.round(month_rate*100))/100;

					float charge_rate=data.getCharge_rate()/100f;
					charge_rate=(float)(Math.round(charge_rate*100))/100;

//					float loss_rate_week=data.getLoss_rate_week()/100f;
//					loss_rate_week=(float)(Math.round(loss_rate_week*100))/100;

					// 第二天流失率
					float arpu=data.getArpu()/10000f;
					arpu=(float)(Math.round(arpu*10000))/10000;

					// 第二天流失率
					float arppu=data.getArppu()/10000f;
					arppu=(float)(Math.round(arppu*10000))/10000;

					str+="<tr>";
					str+="<td>"+data.getThe_year()+"-"+data.getThe_month()
						+"-"+data.getThe_day()+"</td>";
					str+="<td>"+data.getNew_user()+"</td>";
					str+="<td>"+data.getNew_udid()+"</td>";
					str+="<td>"+data.getDau()+"</td>";
					str+="<td>"+data.getCharge_amount()+"</td>";
					str+="<td>"+data.getCharge_people()+"</td>";
					str+="<td>"+data.getMau()+"</td>";
					str+="<td>"+data.getMaxOnline()+"</td>";
					str+="<td>"+last_day_rate+"%"+"</td>";
					str+="<td>"+week_rate+"%"+"</td>";
					str+="<td>"+doublu_week_rate+"%"+"</td>";
					str+="<td>"+month_rate+"%"+"</td>";
					str+="<td>"+arpu+"</td>";
					str+="<td>"+arppu+"</td>";
					str+="<td>"+data.getTotal_user()+"</td>";
					str+="<td>"+data.getCharge_total_user()+"</td>";
					str+="<td>"+charge_rate+"%"+"</td>";
					str+="<td>"+data.getTotal_charge()+"</td>";
					str+="<td>"+data.getTotal_arpu()+"</td>";
					str+="<td>"+data.getTotal_arppu()+"</td>";
//					str+="<td>"+data.getLoss_rate_day()+"%"+"</td>";
//					str+="<td>"+loss_rate_week+"%"+"</td>";
					str+="<td>"+manager.getSessionMap().size()+"</td>";
					str+="</tr>";
				}
				str+="</table></body></html>";
				return str;
			}
			// 宝石消费数据
			else if(Integer.parseInt(type)==VIEW_GEMS_DATA)
			{
				long time=TimeKit.getMillisTime();
				int tracks[]=new int[20];
				int year=Integer.parseInt(request.getParameter("year"));
				int month=Integer.parseInt(request.getParameter("month"));
				int startDay=Integer.parseInt(request
					.getParameter("startDay"));
				int lastDay=Integer
					.parseInt(request.getParameter("lastDay"));
				for(int i=0;i<tracks.length;i++)
				{
					String sql="SELECT SUM(gems) FROM gem_tracks WHERE type="
						+i
						+" AND year="
						+year
						+" AND month="
						+month
						+" AND day>="+startDay+" AND day<="+lastDay;
					tracks[i]=SeaBackKit.loadBySqlOneData(sql,objectFactory
						.getGemsTrackMemCache().getDbaccess());
				}
				long checkTime=(TimeKit.getMillisTime()-time);
				str=HTML_HEAD
					+"数据库查询时间:"
					+checkTime
					+"毫秒<hr/>"
					+"<table width='524' border='5'><tr><td>类型</td><td>总共宝石数量</td></tr>";
				String name=null;
				for(int i=0;i<tracks.length;i++)
				{
					for(int j=0;j<GEMS_TRACK_TYPE.length;j++)
					{
						if(i==j)
						{
							name=GEMS_TRACK_TYPE[i];
							break;
						}
					}
					str+="<tr>";
					str+="<td>"+name+"</td>";
					str+="<td>"+tracks[i]+"</td>";
					str+="</tr>";
					if(i==2)
					{
						for(int j=0;j<9;j++)
						{
							str+="<tr>";
							String sql="SELECT count(*) FROM gem_tracks WHERE type="
								+i
								+" AND year="
								+year
								+" AND month="
								+month
								+" AND day>="
								+startDay
								+" AND day<="
								+lastDay
								+" AND item_id="+j;
							int track=SeaBackKit.loadBySqlOneData(sql,
								objectFactory.getGemsTrackMemCache()
									.getDbaccess());
							str+="<td>**第"+(j+1)+"次购买能量"+"</td>";
							str+="<td>"+track+"次</td>";
							str+="</tr>";
						}
					}
					// 刷新和直接完成每日任务或者重置每日任务
					if(i==6)
					{
						str+="<tr>";
						String sql="SELECT count(*) FROM gem_tracks WHERE type="
							+i
							+" AND year="
							+year
							+" AND month="
							+month
							+" AND day>="
							+startDay
							+" AND day<="
							+lastDay
							+" AND item_id="+1000;
						int track=SeaBackKit.loadBySqlOneData(sql,
							objectFactory.getGemsTrackMemCache()
								.getDbaccess());
						str+="<td>**刷新每日任务次数"+"</td>";
						str+="<td>"+track+"次</td>";
						str+="</tr>";

						str+="<tr>";
						sql="SELECT count(*) FROM gem_tracks WHERE type="+i
							+" AND year="+year+" AND month="+month
							+" AND day>="+startDay+" AND day<="+lastDay
							+" AND item_id!="+1000;
						int track1=SeaBackKit.loadBySqlOneData(sql,
							objectFactory.getGemsTrackMemCache()
								.getDbaccess());
						str+="<td>**直接完成每日任务次数"+"</td>";
						str+="<td>"+(track1)+"次</td>";
						str+="</tr>";

						str+="<tr>";
						sql="SELECT count(*) FROM gem_tracks WHERE type="+i
							+" AND year="+year+" AND month="+month
							+" AND day>="+startDay+" AND day<="+lastDay
							+" AND gems="+28;
						int track2=SeaBackKit.loadBySqlOneData(sql,
							objectFactory.getGemsTrackMemCache()
								.getDbaccess());
						str+="<td>**重置每日任务次数"+"</td>";
						str+="<td>"+track2+"次</td>";
						str+="</tr>";
					}
					// 购买建筑队列
					if(i==4)
					{
						for(int j=0;j<3;j++)
						{
							str+="<tr>";
							String sql="SELECT count(*) FROM gem_tracks WHERE type="
								+i
								+" AND year="
								+year
								+" AND month="
								+month
								+" AND day>="
								+startDay
								+" AND day<="
								+lastDay
								+" AND item_id="+(j+3);
							int track=SeaBackKit.loadBySqlOneData(sql,
								objectFactory.getGemsTrackMemCache()
									.getDbaccess());
							str+="<td>**第"+(j+1)+"次购买建筑位"+"</td>";
							str+="<td>"+track+"次</td>";
							str+="</tr>";
						}
					}
					// 购买物品
					if(i==3)
					{
//						// 物品sid和对应的名字
//						String propSidAndName[]={"1","小块铁锭","2","小桶石油","3",
//							"小块硅矿","4","小块铀矿","5","小堆金币","6","大堆铁锭","7",
//							"大桶石油","8","大块硅矿","9","大块铀矿","10","大堆金块","11",
//							"荣誉勋章包","12","新手礼包","13","大型资源箱","14","超大资源箱",
//							"15","特大资源箱","16","急速生产","501","铁矿开采","502",
//							"石油开采","503","硅矿开采","504","铀矿开采","505","金币铸造",
//							"506","离子盾","507","高爆炸药","508","顺风行军","509",
//							"和平旗","1001","能量试剂","1002","能量药水","1003","经验书",
//							"1004","声望书","1005","迁岛令","1006","高级迁岛令","2021",
//							"荣誉勋章","2022","统御书","2001","脉冲炸弹","2002","群体10%",
//							"2020","自动建造道具"};
						String propSidAndName[]={"1","小块铁锭","2","小桶石油","3",
							"小块硅矿","4","小块铀矿","5","小堆金币","6","大堆铁锭","7",
							"大桶石油","8","大块硅矿","9","大块铀矿","10","大堆金块","11",
							"荣誉勋章宝箱","13","大型资源箱","14","超大资源箱","15","特大资源箱",
							"16","急速生产","412","燃气轮机货单","413","聚变核心货单","416",
							"一堆星石","419","大堆星石","501","铁矿开采","502","石油开采",
							"503","硅矿开采","504","铀矿开采","505","金币铸造","506",
							"离子盾","507","高爆炸药","508","顺风行军","509","和平旗",
							"510","激光制导","511","烟雾屏障","512","贫铀弹","513",
							"反应装甲","1005","迁岛令","1006","高级迁岛令","2001",
							"空中支援","2002","脉冲炸弹","2007","C98燃气轮机","2008",
							"聚变核心","2009","星石","2020","自动建设","2021","荣誉勋章",
							"2022","统御书","4009","大箱铁锭","4010","大箱石油","4011",
							"大箱硅矿","4012","大箱铀矿","4013","大箱金块"};
						for(int j=0;j<propSidAndName.length;j+=2)
						{
							int sid=Integer.parseInt(propSidAndName[j]);
							str+="<tr>";
							String sql="SELECT count(*) FROM gem_tracks WHERE type="
								+i
								+" AND year="
								+year
								+" AND month="
								+month
								+" AND day>="
								+startDay
								+" AND day<="
								+lastDay
								+" AND item_id="+sid;
							int track=SeaBackKit.loadBySqlOneData(sql,
								objectFactory.getGemsTrackMemCache()
									.getDbaccess());
							str+="<td>**物品("+propSidAndName[j+1]+")</td>";
							str+="<td>"+track+"次</td>";
							str+="</tr>";
						}
					}
					// 建筑加速
					if(i==0)
					{
						for(int j=0;j<INDEX_FOR_BUILD_NAME.length;j++)
						{
							str+="<tr>";
							String sql="SELECT count(*) FROM gem_tracks WHERE type="
								+i
								+" AND year="
								+year
								+" AND month="
								+month
								+" AND day>="
								+startDay
								+" AND day<="
								+lastDay
								+" AND item_id="+j;
							int track=SeaBackKit.loadBySqlOneData(sql,
								objectFactory.getGemsTrackMemCache()
									.getDbaccess());
							str+="<td>**建筑（"+INDEX_FOR_BUILD_NAME[j]
								+"）</td>";
							str+="<td>"+track+"次</td>";
							sql="SELECT sum(gems) FROM gem_tracks WHERE type="
								+i
								+" AND year="
								+year
								+" AND month="
								+month
								+" AND day>="
								+startDay
								+" AND day<="
								+lastDay
								+" AND item_id="+j;
							track=SeaBackKit.loadBySqlOneData(sql,
								objectFactory.getGemsTrackMemCache()
									.getDbaccess());
							str+="<td>该项花费宝石"+track+"</td>";
							str+="</tr>";
						}
					}
					// 抽奖宝石
					if(i==GemsTrack.LOTTO)
					{
						String[] lottoType={
							String.valueOf(PublicConst.LOTTO_ADVANCE),
							"**高级",String.valueOf(PublicConst.LOTTO_LUXURY),
							"**豪华"};
						int[] lottoGems={PublicConst.LOTTO_NEED_GEM_2,
							PublicConst.LOTTO_NEED_GEM_3};
						for(int j=0;j<lottoType.length;j+=2)
						{
							int sid=Integer.parseInt(lottoType[j]);
							str+="<tr>";
							String sql="SELECT count(*) FROM gem_tracks WHERE type="
								+i
								+" AND year="
								+year
								+" AND month="
								+month
								+" AND day>="
								+startDay
								+" AND day<="
								+lastDay
								+" AND item_id="+sid;
							int track=SeaBackKit.loadBySqlOneData(sql,
								objectFactory.getGemsTrackMemCache()
									.getDbaccess());
							str+="<td>"+lottoType[j+1]+"</td>";
							str+="<td>"+track+"次</td>";
							str+="<td>该项花费宝石"+track*lottoGems[j/2]+"</td>";
							str+="</tr>";
						}
					}
				}
				str+="</table></body></html>";
				return str;
			}
			// 获得某个玩家宝石消费数据
			else if(Integer.parseInt(type)==VIEW_GEMS_PLAYER_DATA)
			{
				long time=TimeKit.getMillisTime();
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				int year=Integer.parseInt(request.getParameter("year"));
				int month=Integer.parseInt(request.getParameter("month"));
				int day=Integer.parseInt(request.getParameter("day"));
				// 某个玩家的宝石消费日志 -1 表示全部
				GemsTrack tracks[]=objectFactory.getGemsTrackMemCache()
					.loadTracks(player.getId(),year,month,day,PublicConst.LOAD_ALL_GEM_COST);
				long checkTime=(TimeKit.getMillisTime()-time);
				str=HTML_HEAD
					+"数据库查询时间:"
					+checkTime
					+"毫秒<hr/>"
					+"<table width='1024' border='5'><tr><td>id</td><td>类型</td><td>playerId</td><td>宝石数量</td><td>当前宝石数量</td><td>物品id</td><td>创建时间</td></tr>";
				String name="";
				if(tracks==null||tracks.length<=0) return str;
				for(int i=0;i<tracks.length;i++)
				{
					for(int j=0;j<GEMS_TRACK_TYPE.length;j++)
					{
						if(tracks[i].getType()==j)
						{
							name=GEMS_TRACK_TYPE[j];
							break;
						}
					}
					str+="<tr>";
					str+="<td>"+tracks[i].getId()+"</td>";
					str+="<td>"+name+"</td>";
					str+="<td>"+tracks[i].getPlayerId()+"</td>";
					str+="<td>"+tracks[i].getGems()+"</td>";
					str+="<td>"+tracks[i].getNowGems()+"</td>";
					str+="<td>"+tracks[i].getItem_id()+"</td>";
					str+="<td>"
						+SeaBackKit.formatDataTime(tracks[i].getCreateAt())
						+"</td>";
					str+="</tr>";
				}
				// 目前还未存入数据库的
				Object[] object=objectFactory.getGemsTrackMemCache()
					.getList().valueArray();
				for(int i=object.length-1;i>=0;i--)
				{
					GemsTrack track=(GemsTrack)object[i];
					if(track.getPlayerId()!=player.getId()) continue;
					for(int j=0;j<GEMS_TRACK_TYPE.length;j++)
					{
						if(track.getType()==j)
						{
							name=GEMS_TRACK_TYPE[j];
							break;
						}
					}
					str+="<tr>";
					str+="<td>"+track.getId()+"</td>";
					str+="<td>"+name+"</td>";
					str+="<td>"+track.getPlayerId()+"</td>";
					str+="<td>"+track.getGems()+"</td>";
					str+="<td>"+track.getNowGems()+"</td>";
					str+="<td>"+track.getItem_id()+"</td>";
					str+="<td>"
						+SeaBackKit.formatDataTime(track.getCreateAt())
						+"</td>";
					str+="</tr>";
				}
				str+="</table></body></html>";
				return str;
			}
			else if(Integer.parseInt(type)==SEND_ALL_SYSTEM_MESSAGE)
			{
				// 内容
				String content=URLDecoder.decode(request
					.getParameter("content"),"utf-8");
				// messageData.setResources(resource);
				// 标题
				String title=URLDecoder.decode(
					request.getParameter("title"),"utf-8");
				// S
				String mail_type=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"system_mail");
				objectFactory.createSystemMessage(Message.SYSTEM_TYPE,content,title,mail_type,null);
				str="system_message_send_sucess"+content;
			}
			else if(Integer.parseInt(type)==CLOSE_SERVER)
			{
				String close=request.getParameter("close");
				int num=0;
				int left[]=new int[10];
				if(close.equals("true"))
				{
					// 关闭服务器 先断服务器连接
					num=objectFactory.getDsmanager().close();
					// 解除端口绑定
					MinaConnectServer server=(MinaConnectServer)BackKit
						.getContext().get("server");
					server.close();
					// 存储数据
					left=objectFactory.saveAndExit(true);
					// 清理僵尸用户
					clearIsland();
				}
				str="close_server:----"+close+"   online size========"+num
					+"  left data num="+left[0]+","+left[1]+","+left[2]+","
					+left[3]+","+left[4]+","+left[5]+","+left[6]+","+left[7]
					+","+left[8]+","+left[9]+","+left[10]+","+left[11]+","
					+left[12];
			}
			else if(Integer.parseInt(type)==VIEW_PLAYER_INFO)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					String udid=request.getParameter("udid");
					String account=request.getParameter("account");
					if(account!=null&&account.length()>0)
						account=URLDecoder.decode(account,"utf-8");
					if(udid!=null&&!udid.equals(""))
					{
						String sql="select * from users where login_udid='"
							+udid+"'";
						User users[]=objectFactory.getUserDBAccess()
							.loadBySql(sql);
						String userudid="find:";
						if(users==null) return userudid;
						for(int i=0;i<users.length;i++)
						{
							userudid+=users[i].getUserAccount();
							userudid+=":";
							Player beplayer=objectFactory
								.getPlayerById(users[i].getPlayerId());
							String beName="null";
							if(beplayer!=null) beName=beplayer.getName();
							userudid+=beName;
							userudid+=",";
						}
						return userudid;
					}
					else if(account!=null&&account.length()>0)
					{
						String sql="select * from users where user_account='"
							+account+"'";
						User users[]=objectFactory.getUserDBAccess()
							.loadBySql(sql);
						String userudid="find:";
						if(users==null) return userudid;
						for(int i=0;i<users.length;i++)
						{
							userudid+=users[i].getUserAccount();
							userudid+=":";
							Player beplayer=objectFactory
								.getPlayerById(users[i].getPlayerId());
							String beName="null";
							if(beplayer!=null) beName=beplayer.getName();
							userudid+=beName;
							userudid+=",";
						}
						return userudid;
					}
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				str=HTML_HEAD
					+"<table width='2024' border='5'><tr><td>playerId</td><td>玩家名字</td><td>玩家等级</td><td>创建时间</td><td>数据更新时间</td><td>玩家资源</td><td>玩家建筑</td><td>玩家船只</td><td>玩家城防舰队</td><td>玩家账号信息</td><td>udid对应的账号</td><td>技能</td><td>科技</td></tr>";
				str+="<tr>";
				str+="<td>"+player.getId()+"</td>";
				str+="<td>"+player.getName()+"VIP等级="+player.getUser_state()
					+"</td>";
				str+="<td>"+player.getLevel()+"</td>";
				str+="<td>"
					+SeaBackKit.formatDataTime(player.getCreateTime())
					+"</td>";
				str+="<td>"
					+SeaBackKit.formatDataTime(player.getUpdateTime())
					+"</td>";
				String name="null";
				Alliance alliance=(Alliance)objectFactory
					.getAllianceMemCache().loadOnly(
						player.getAttributes(PublicConst.ALLIANCE_ID));
				if(alliance!=null) name=alliance.getName();
				str+="<td>"+"金属:"+player.getResources()[0]+" 石油:"
					+player.getResources()[1]+" 硅："+player.getResources()[2]
					+" 铀："+player.getResources()[3]+" 金币:"
					+player.getResources()[4]+" 宝石："
					+player.getResources()[5]+"充值宝石:"
					+player.getResources()[6]+",军衔等级："
					+player.getPlayerType()+",统御等级:"
					+player.getCommanderLevel()+",声望等级:"
					+player.getHonor()[Player.HONOR_LEVEL_INDEX]+",建筑队列:"
					+player.getIsland().getBuildNum()+",联盟名字:"+name+",战力值:"
					+player.getFightScore()+"</td>";
				// 玩家建筑
				Object builds[]=player.getIsland().getBuildArray();
				String build="";
				for(int i=0;i<builds.length;i++)
				{
					PlayerBuild b=(PlayerBuild)builds[i];
					build+="  ";
					// 名字翻译
					String strName=b.getBuildName();
					for(int j=0;j<BUILD_FOR_NAME.length;j+=2)
					{
						if(strName.equals(BUILD_FOR_NAME[j]))
						{
							strName=BUILD_FOR_NAME[j+1];
						}
					}
					build+=strName;
					build+="=";
					build+=b.getBuildLevel();
					build+=",";
				}
				str+="<td>"+build+"</td>";
				// 玩家船只
				Object troops[]=player.getIsland().getTroops().getArray();
				String s="";
				for(int i=0;i<troops.length;i++)
				{
					Troop troop=(Troop)troops[i];
					Ship ship=(Ship)Ship.factory.getSample(troop
						.getShipSid());
					s+="   ";
					String strName=ship.getName();
					for(int j=0;j<SHIP_FOR_NAME.length;j+=2)
					{
						if(strName.endsWith(SHIP_FOR_NAME[j]))
						{
							strName=SHIP_FOR_NAME[j+1];
						}
					}
					// 船只翻译
					s+=strName;
					s+=""+troop.getNum();
					s+=",";
				}
				str+="<td>"+s+"</td>";
				s="";
				// 玩家城防舰队
				FleetGroup main=player.getIsland().getMainGroup();
				Fleet fleet[]=main.getArray();
				for(int i=0;i<fleet.length;i++)
				{
					if(fleet[i]==null) continue;
					Ship ship=(Ship)Ship.factory.getSample(fleet[i]
						.getShip().getSid());
					s+="   ";
					String strName=ship.getName();
					for(int j=0;j<SHIP_FOR_NAME.length;j+=2)
					{
						if(strName.endsWith(SHIP_FOR_NAME[j]))
						{
							strName=SHIP_FOR_NAME[j+1];
						}
					}
					s+=strName;
					s+="="+fleet[i].getNum();
					s+=",";
				}
				str+="<td>"+s+"</td>";
				User user=objectFactory.getUserDBAccess().load(
					player.getUser_id()+"");
				String account=user.getUserAccount();
				str+="<td>"+"  "+account+"  "+","+user.getCreateUdid()
					+"</td>";
				String udidaccount="";
				String udid=request.getParameter("udid");
				if(udid!=null&&!udid.equals(""))
				{
					String sql="select * from users where login_udid='"+udid
						+"'";
					User users[]=objectFactory.getUserDBAccess().loadBySql(
						sql);
					String userudid="";
					if(users!=null)
					{
						for(int i=0;i<users.length;i++)
						{
							userudid+="  ";
							userudid+=users[i].getUserAccount();
							userudid+=":";
							Player beplayer=objectFactory
								.getPlayerById(users[i].getPlayerId());
							String beName="null";
							if(beplayer!=null) beName=beplayer.getName();
							userudid+=beName;
							userudid+=",";
						}
					}
					str+="<td>"+userudid+"</td>";
				}
				else
				{
					str+="<td>"+"</td>";
				}
				// 玩家技能
				Object object[]=player.getSkills().toArray();
				String skills="";
				for(int i=0;i<object.length;i++)
				{
					skills+=" ";
					if(object[i]==null) continue;
					skills+=((Skill)object[i]).getName();
					skills+="=";
					skills+=((Skill)object[i]).getLevel();
					skills+=",";
				}
				str+="<td>"+skills+"</td>";
				// 玩家科技
				PlayerBuild buildscice=player.getIsland().getBuildByIndex(
					BuildInfo.INDEX_2,player.getIsland().getBuilds());
				String sciecnce="";
				if(buildscice!=null)
				{
					ScienceProduce sp=(ScienceProduce)buildscice
						.getProduce();
					Science science[]=sp.getAllScience();
					for(int i=0;i<science.length;i++)
					{
						sciecnce+=" ";
						// 科技名字替换
						String re=science[i].getName();
						for(int j=0;j<SCIENC_FOR_NAME.length;j+=2)
						{
							if(science[i].getName().equals(
								SCIENC_FOR_NAME[j]))
							{
								re=SCIENC_FOR_NAME[j+1];
							}
						}
						sciecnce+=re;
						sciecnce+="=";
						sciecnce+=science[i].getLevel();
						sciecnce+=",";
					}
					str+="<td>"+sciecnce+"</td>";
				}
				else
				{
					str+="<td>"+"</td>";
				}
				str+="</tr>";
				str+="</table></body></html>";
				String changeName=URLDecoder.decode(request
					.getParameter("changeName"),"utf-8");
				if(changeName!=null&&!changeName.equals(""))
				{
					String change[]=TextKit.split(changeName,":");
					if(change[0].equals("true"))
					{
						player.setName(change[1]);
					}
				}
				return str;
			}
			else if(Integer.parseInt(type)==SET_NEW_TASK_MARK)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				int mark=Integer.parseInt(request.getParameter("mark"));
				player.setTaskMark(mark);
				str="gm set taskMark:="+player.getName()+"    mark="+mark;
			}
			else if(Integer.parseInt(type)==BANED_AND_MUTIME)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				String banned=request.getParameter("banned");
				User user=null;
				if(banned.equals("true"))
				{
					user=objectFactory.getUserDBAccess().load(
						player.getUser_id()+"");
					if(user!=null)
					{
						user.setBanned(1);
						objectFactory.getUserDBAccess().save(user);
						Session session=(Session)player.getSource();
						if(session!=null&&session.getConnect()!=null)
							session.getConnect().close();
					}
				}
				else
				{
					user=objectFactory.getUserDBAccess().load(
						player.getUser_id()+"");
					if(user!=null)
					{
						user.setBanned(0);
						objectFactory.getUserDBAccess().save(user);
					}
				}
				int mutime=Integer.parseInt(request.getParameter("mutime"));
				if(mutime>=0)
				{
					player.setMuteTime(TimeKit.getSecondTime()+mutime*60*60);
					// 发送系统公告
				}
				str="gm set banned:="+banned+",mutime="+mutime+"hours";
			}
			else if(Integer.parseInt(type)==BANNED_DEVICE)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				String banned=request.getParameter("banned");
				String op=request.getParameter("op");
				if("show".equals(op))
				{
					Map<String,String> params=new HashMap<String,String>();
					params.put("login_udid",playerName);
					HttpRespons re=sendHttpDataToCenter(6,params);
					if(re!=null)
						return re.getContent();
					else
						return "{\"success\":false}";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				User user=objectFactory.getUserDBAccess().load(
					player.getUser_id()+"");
				String bannedDevice="";
				if(user!=null)
				{
					bannedDevice="true".equals(banned)?user.getLoginUdid()
						:"";
					user.setBannedDevice(bannedDevice);
					objectFactory.getUserDBAccess().save(user);
					if(bannedDevice!=null&&bannedDevice.length()>0)
					{
						Session session=(Session)player.getSource();
						if(session!=null&&session.getConnect()!=null)
							session.getConnect().close();
					}
					// 通知平台记录封号
					Map<String,String> params=new HashMap<String,String>();
					params.put("user_account",user!=null?user
						.getUserAccount():"");
					params.put("login_udid",user!=null?user.getLoginUdid()
						:"");
					params.put("server_id",String
						.valueOf(UserToCenterPort.SERVER_ID));
					params.put("banned_device",String.valueOf(banned
						.equals("true")));
					sendHttpDataToCenter(5,params);
					str="{\"success\":true,\"banned device\":"+banned+"}";
				}
				else
					str="{\"success\":false,\"msg\":\"user is not exists.\"}";
			}
			// 接管玩家账号
			else if(Integer.parseInt(type)==TAKE_OVER_PLAYER)
			{
				String strType=request.getParameter("dataType");
				String strData=request.getParameter("data");
				String strTime=request.getParameter("time");

				Player targetPlayer=null;
				if("1".equals(strType))
				{
					String playerName=URLDecoder.decode(strData,"utf-8");
					targetPlayer=objectFactory.getPlayerByName(playerName,
						false);
				}
				else if("2".equals(strType))
				{
					targetPlayer=objectFactory.getPlayerById(Integer
						.parseInt(strData));
				}
				else if("3".equals(strType))
				{
					return "can not support user account";
				}
				if(targetPlayer==null)
				{
					return "player not exists";
				}
				try
				{
					int time=Integer.parseInt(strTime);
					if(time<=0)
					{
						targetPlayer
							.setAttribute(PublicConst.TAKE_OVER,null);
						return "cancel success.";
					}
					// 生成长度为6的由字母和数字组成的随机密码，排队Il1这三个不易识别的
					// String
					// chars="ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyx023456789";
					String chars="0123456789";
					String passwd="";
					for(int i=0;i<6;i++)
					{
						passwd+=chars.charAt(MathKit.randomValue(0,chars
							.length()));
					}
					String md5passwd=(new MD5()).encode(passwd);
					time*=60; // 传的时间为分钟，转换为秒
					time+=TimeKit.getSecondTime();
					if(time<0) time=Integer.MAX_VALUE;
					targetPlayer.setAttribute(PublicConst.TAKE_OVER,
						md5passwd+"|"+time);
					Session session=(Session)targetPlayer.getSource();
					if(session!=null&&session.getConnect()!=null)
						session.getConnect().close();
					return "success. account:"+GameDBCCAccess.SHARP
						+targetPlayer.getId()+",password:"+passwd;
				}
				catch(Exception e)
				{
					return "fail";
				}
			}
			else if(Integer.parseInt(type)==UPDATE_SCEIEN_LEVEL)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				int sid=Integer.parseInt(request.getParameter("sid"));
				int level=Integer.parseInt(request.getParameter("level"));
				PlayerBuild build=(PlayerBuild)player.getIsland()
					.getBuildByType(Build.BUILD_RESEARCH,null);
				ScienceProduce produce=(ScienceProduce)build.getProduce();
				Science science[]=produce.getAllScience();
				Science setScinece=null;
				for(int i=0;i<science.length;i++)
				{
					// science[i].setLevel(level);
					if(science[i].getSid()==sid)
					{
						setScinece=science[i];
						setScinece.setLevel(level);
					}
				}
				if(setScinece!=null)
					str="set ["+setScinece.getName()+"] level:"+level;
				else
					str="fail.";
			}
			// PLAYER_GM
			else if(Integer.parseInt(type)==PLAYER_GM)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				String bool=request.getParameter("gm");
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				if(bool.equals("true"))
					player.setAttribute(PublicConst.PLAYER_GM,"1");
				else
					player.setAttribute(PublicConst.PLAYER_GM,null);
				str="player gm suceess="+playerName+",========"
					+player.getAttributes(PublicConst.PLAYER_GM);
			}
			else if(Integer.parseInt(type)==VIEW_ONLINE_PLAYER)
			{
				String pepoe="";
				Session sessions[]=manager.getSessionMap().getSessions();
				for(int j=0;j<sessions.length;j++)
				{
					Player player=PlayerKit.getInstance().getPlayerOnly(
						sessions[j]);
					if(player==null) continue;
					if(j>=100) break;
					pepoe+=player.getName()+",";
				}
				str=HTML_HEAD
					+"<table width='524' border='5'><tr><td>在线的人</td></tr>";
				str+="<td>"+pepoe+"</td>";
				str+="</tr>";
				str+="</table></body></html>";
				return str;
			}
			// 查看玩家今天的战斗事件
			else if(Integer.parseInt(type)==VIEW_FIGHT_EVENT)
			{

			}
			else if(Integer.parseInt(type)==VIEW_PLAYER_GEMS)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				// 查看某个玩家充值订单状态
				// 成功的条数
				String sql="SELECT COUNT(*) FROM orders where order_state=1 AND user_id="
					+player.getId();
				Fields field=objectFactory.getOrderCache().getDbaccess()
					.loadSql(sql);
				int num1=Integer.parseInt(field.getArray()[0].getValue()
					.toString());
				sql="SELECT COUNT(*) FROM orders where order_state=0 AND user_id="
					+player.getId();
				field=objectFactory.getOrderCache().getDbaccess().loadSql(
					sql);
				int num2=Integer.parseInt(field.getArray()[0].getValue()
					.toString());
				sql="SELECT COUNT(*) FROM orders where order_state=2 AND user_id="
					+player.getId();
				field=objectFactory.getOrderCache().getDbaccess().loadSql(
					sql);
				int num3=Integer.parseInt(field.getArray()[0].getValue()
					.toString());

				sql="SELECT COUNT(*) FROM orders where order_state=3 AND user_id="
					+player.getId();
				field=objectFactory.getOrderCache().getDbaccess().loadSql(
					sql);
				int num4=Integer.parseInt(field.getArray()[0].getValue()
					.toString());
				str=HTML_HEAD
					+"<table width='524' border='5'><tr><td>购买成功条数</td><td>验证超时条数</td><td>验证失败条数</td><td>订单记录相同条数</td></tr>";
				str+="<td>"+num1+"</td>";
				str+="<td>"+num2+"</td>";
				str+="<td>"+num3+"</td>";
				str+="<td>"+num4+"</td>";
				str+="</tr>";
				str+="</table>";
				str+="</body></html>";
			}
			// 重新验证某个玩家的超时订单
			else if(Integer.parseInt(type)==CHECK_PLAYER_ORDER)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				String sql="SELECT * FROM orders where order_state=0 AND user_id="
					+player.getId();
				Object orders[]=objectFactory.getOrderCache().getDbaccess()
					.loadBySql(sql);
				str="exception=";
				int num=0;
				int hand=0;
				if(orders!=null)
				{
					for(int i=0;i<orders.length;i++)
					{
						if(orders[i]==null) continue;
						try
						{
							reviewOrder((Order)orders[i],player);
							hand++;
						}
						catch(Exception e)
						{
							num++;
						}
					}
				}
				str+=num;
				str+="  hand====="+hand;
				return str;
			}
			// 查看消费记录
			else if(Integer.parseInt(type)==BUY_GEMS_INFO)
			{
				// 成功的条数
				String sql="SELECT COUNT(*) FROM orders where order_state=1";
				Fields field=objectFactory.getOrderCache().getDbaccess()
					.loadSql(sql);
				int num1=Integer.parseInt(field.getArray()[0].getValue()
					.toString());
				sql="SELECT COUNT(*) FROM orders where order_state=0";
				field=objectFactory.getOrderCache().getDbaccess().loadSql(
					sql);
				int num2=Integer.parseInt(field.getArray()[0].getValue()
					.toString());
				sql="SELECT COUNT(*) FROM orders where order_state=2";
				field=objectFactory.getOrderCache().getDbaccess().loadSql(
					sql);
				int num3=Integer.parseInt(field.getArray()[0].getValue()
					.toString());

				sql="SELECT COUNT(*) FROM orders where order_state=3";
				field=objectFactory.getOrderCache().getDbaccess().loadSql(
					sql);
				int num4=Integer.parseInt(field.getArray()[0].getValue()
					.toString());
				str=HTML_HEAD
					+"<table width='524' border='5'><tr><td>购买成功条数</td><td>验证超时条数</td><td>验证失败条数</td><td>订单记录相同条数</td></tr>";
				str+="<td>"+num1+"</td>";
				str+="<td>"+num2+"</td>";
				str+="<td>"+num3+"</td>";
				str+="<td>"+num4+"</td>";
				str+="</tr>";
				str+="</table>";
				// String check_all_order=request
				// .getParameter("check_all_order");
				// if(check_all_order.equals("true"))
				// {
				// sql = "select * from orders where order_state=0";
				// Object order[]=objectFactory.getOrderCache()
				// .getDbaccess().loadBySql(sql);
				// str+="处理后：";
				// str="<table width='524'
				// border='5'><tr><td>购买成功条数</td><td>验证超时条数</td><td>验证失败条数</td></tr>";
				// }
				str+="</body></html>";
			}
			else if(Integer.parseInt(type)==BUY_GEMS_MORE_INFO)
			{
				String sql="select user_name,sum(gems),sum(money) as money from orders group by user_name order by money desc limit 50";
				Fields fields[]=((OrderGameDBAccess)objectFactory
					.getOrderCache().getDbaccess()).loadSqls(sql);
				str=HTML_HEAD
					+"<table width='524' border='5'><tr><td>名次</td><td>玩家名字</td><td>总共充值宝石</td><td>总共充值RMB</td></tr>";
				for(int i=0;i<fields.length;i++)
				{
					str+="<td>"+(i+1)+"</td>";
					str+="<td>"
						+fields[i].getArray()[0].getValue().toString()
						+"</td>";
					str+="<td>"
						+fields[i].getArray()[1].getValue().toString()
						+"</td>";
					str+="<td>"
						+fields[i].getArray()[2].getValue().toString()
						+"</td>";
					str+="</tr>";
				}
				str+="</table></body></html>";
				return str;
			}
			// 修改密码
			else if(Integer.parseInt(type)==CHANGGE_PASSWORD)
			{
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				String pwd=request.getParameter("password");
				if(pwd==null||pwd.equals("")) return "pwd is null";
				MD5 md5=new MD5();
				String md5pwd=md5.encode(pwd);
				User user=objectFactory.getUserDBAccess().load(
					player.getUser_id()+"");
				if(user==null||user.getUserType()==User.GUEST)
				{
					return "user is not exist";
				}
				ByteBuffer data=new ByteBuffer();
				data.clear();
				data.writeByte(UserToCenterPort.CHANGE_PASSWORD);
				data.writeUTF(user.getUserAccount());
				data.writeUTF(md5pwd);
				data.writeUTF(user.getPassword());
				data.writeBoolean(true);
				data=sendHttpData(data);
				int typeReturn=data.readUnsignedByte();
				if(typeReturn==0)
				{
					user.setPassword(md5pwd);
					objectFactory.getUserDBAccess().save(user);
				}
				else
				{
					return "center user is not exist";
				}
				str+="success";
			}
			else if(Integer.parseInt(type)==ALLIANCE_CHANGE)
			{
				// 联盟名字
				String allianceName=URLDecoder.decode(request
					.getParameter("allianceName"),"utf-8");
				if(allianceName==null||allianceName.equals(""))
				{
					return "allianceName is null";
				}
				Alliance alliance=objectFactory.getAllianceMemCache()
					.loadByName(allianceName,true);
				if(alliance==null) return "alliance is null";
				// 等级提升
				String allianceLevel=request.getParameter("allianceLevel");
				if(allianceLevel.equals("true"))
				{
					alliance.incrExp(10000);
					// 所有联盟技能升级为满级
					for(int i=0;i<ALLIANCE_SKILLS_SID.length;i++)
					{
						alliance.incrSkillExp(10000,ALLIANCE_SKILLS_SID[i]);
					}
				}
			}
			// 全服玩家加宝石
			else if(Integer.parseInt(type)==ALL_PLAYER_GIVE_GEMS)
			{
				String addGems=request.getParameter("addGems");
				String code=request.getParameter("code");
				String gemsStr=request.getParameter("gems");
				String names=request.getParameter("names");
				if(names!=null&&names.length()>0)
					names=URLDecoder.decode(names,"utf-8");
				int gems=Integer.parseInt(gemsStr);
				if(addGems.equals("true"))
				{
					if(code.equals("3125098"))
					{
						Object object[]=objectFactory.getPlayerCache()
							.getCacheMap().valueArray();

						for(int i=0;i<object.length;i++)
						{
							PlayerSave data=(PlayerSave)object[i];
							Player allplayer=data.getData();
							Resources.addGemsNomal(gems,allplayer
								.getResources(),allplayer);
							objectFactory.createGemTrack(
								GemsTrack.SERVER_AWARD,allplayer.getId(),
								gems,0,
								Resources.getGems(allplayer.getResources()));
						}
						return "success=gems="+gems;
					}
					else if(code.equals("63023474"))
					{
						StringBuilder sb=new StringBuilder();
						sb.append("failed players:");
						String[] players=TextKit.split(names,",");
						for(int i=0;i<players.length;i++)
						{
							Player allplayer=objectFactory.getPlayerByName(
								players[i],true);
							if(allplayer!=null)
							{
								Resources.addGemsNomal(gems,
									allplayer.getResources(),allplayer);
								objectFactory.createGemTrack(
									GemsTrack.SERVER_AWARD,
									allplayer.getId(),gems,0,Resources
										.getGems(allplayer.getResources()));
							}
							else
							{
								// 记录未补偿成功的
								sb.append(players[i]).append(",");
							}
						}
						return sb.toString();
					}
				}
				return "fail=gems="+gems;
			}
			else if(Integer.parseInt(type)==VIEW_PLAYER_SHIP_DATA)
			{
				long time=TimeKit.getMillisTime();
				// 玩家名字
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				// 一天
				int viewTime=60*60*24;
				if(request.getParameter("checkTime")!=null
					&&!request.getParameter("checkTime").equals(""))
				{
					viewTime=viewTime
						*Integer.parseInt(request.getParameter("checkTime"));
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				// 某个玩家的宝石消费日志
				ShipCheckData data[]=objectFactory.getShipCache()
					.loadTracks(player.getId(),viewTime,null);
				long checkTime=(TimeKit.getMillisTime()-time);
				str=HTML_HEAD
					+"数据库查询时间:"
					+checkTime
					+"毫秒<hr/>"
					+"<table width='1324' border='5'><tr><td>id</td><td>类型</td><td>playerId</td><td>当前港口兵力</td><td>当前伤兵</td><td>当前事件兵力</td><td>此记录影响兵力</td><td>额外信息</td><td>创建时间</td></tr>";
				ShipCheckData temp;
				// if(data!=null)
				// {
				// // 排序 日期排序
				// for(int i=0;i<data.length;i++)
				// {
				// for(int j=i;j<data.length;j++)
				// {
				// ShipCheckData dataA=(ShipCheckData)data[i];
				// ShipCheckData dataB=(ShipCheckData)data[j];
				// if(dataA.getCreateAt()>dataB.getCreateAt())
				// {
				// temp=data[i];
				// data[i]=data[j];
				// data[j]=temp;
				// }
				// }
				// }
				//
				// for(int i=0;i<data.length;i++)
				// {
				// ShipCheckData cdata=data[i];
				// if(cdata.getPlayerId()!=player.getId()) continue;
				// str+="<td>"+cdata.getId()+"</td>";
				// str+="<td>"+FIGHT_EVENT_STYLE[cdata.getType()]
				// +"</td>";
				// str+="<td>"+cdata.getPlayerId()+"</td>";
				// // 港口兵力
				// str+="<td>"+createShipData(cdata.getLeftList())
				// +"</td>";
				// // 伤兵
				// str+="<td>"+createShipData(cdata.getHurtList())
				// +"</td>";
				// // 事件兵力
				// str+="<td>"
				// +createEventShipData(cdata.getEventList())
				// +"</td>";
				// // 本次操作的
				// str+="<td>"+createShipData(cdata.getList())+"</td>";
				// // 额外信息
				// str+="<td>"+cdata.getExtra()+"</td>";
				// str+="<td>"
				// +SeaBackKit.formatDataTime(cdata.getCreateAt())
				// +"</td>";
				// str+="</tr>";
				// }
				// }
				// // 加上内存中的
				// Object shipdata[]=objectFactory.getShipCache().getList()
				// .valueArray();
				// if(shipdata!=null)
				// {
				// // 排序 日期排序
				// for(int i=0;i<shipdata.length;i++)
				// {
				// for(int j=i;j<shipdata.length;j++)
				// {
				// ShipCheckData dataA=(ShipCheckData)shipdata[i];
				// ShipCheckData dataB=(ShipCheckData)shipdata[j];
				// if(dataA.getCreateAt()>dataB.getCreateAt())
				// {
				// temp=(ShipCheckData)shipdata[i];
				// shipdata[i]=(ShipCheckData)shipdata[j];
				// shipdata[j]=temp;
				// }
				// }
				// }
				// }
//				ShipCheckData[] shipdata=objectFactory.getShipCache()
//					.getPlayerDatas(player.getId());
//				if(shipdata!=null)
//				{
//					for(int i=0;i<shipdata.length;i++)
//					{
//						ShipCheckData cdata=(ShipCheckData)shipdata[i];
//						if(cdata.getPlayerId()!=player.getId()) continue;
//						str+="<td>"+"内存中的"+cdata.getId()+"</td>";
//						str+="<td>"+FIGHT_EVENT_STYLE[cdata.getType()]
//							+"</td>";
//						str+="<td>"+cdata.getPlayerId()+"</td>";
//						// 港口兵力
//						str+="<td>"+createShipData(cdata.getLeftList())
//							+"</td>";
//						// 伤兵
//						str+="<td>"+createShipData(cdata.getHurtList())
//							+"</td>";
//						// 事件兵力
//						str+="<td>"
//							+createEventShipData(cdata.getEventList())
//							+"</td>";
//						// 本次操作的
//						str+="<td>"+createShipData(cdata.getList())+"</td>";
//						// 额外信息
//						str+="<td>"+cdata.getExtra()+"</td>";
//						str+="<td>"
//							+SeaBackKit.formatDataTime(cdata.getCreateAt())
//							+"</td>";
//						str+="</tr>";
//					}
//				}
//				str+="</table></body></html>";
				return str;
			}
			// 查看系统邮件
			else if(Integer.parseInt(type)==VIEW_SYSTEM_MAIL)
			{
				return getSystemMail();
			}
			// 删除系统邮件
			else if(Integer.parseInt(type)==REMOVE_SYSTEM_MAIL)
			{
				String strId=request.getParameter("mailid");
				return removeSystemMail(Integer.parseInt(strId));
			}
			// // 设置简单活动
			// else if(Integer.parseInt(type)==SET_SIMPLE_ACTIVITY)
			// {
			// return setSimpleActivity(request);
			// }
			// 设置限时购买，打折。。。。
			else if(Integer.parseInt(type)==SET_ACTIVITY
				||Integer.parseInt(type)==SET_SIMPLE_ACTIVITY)
			{
				return setActivity(request);
			}
			else if(Integer.parseInt(type)==RECALL_EMAIL)
			{
				int days=Integer.parseInt(request.getParameter("days"));
				String push=URLDecoder.decode(request.getParameter("push"),
					"utf-8");
				String title=URLDecoder.decode(
					request.getParameter("title"),"utf-8");
				String content=URLDecoder.decode(request
					.getParameter("content"),"utf-8");
				String user=URLDecoder.decode(request.getParameter("user"),
					"utf-8");
				String pwd=URLDecoder.decode(request.getParameter("pwd"),
					"utf-8");
				String server=URLDecoder.decode(request
					.getParameter("emailserver"),"utf-8");
				if(user==null||user.equals("")||pwd==null||pwd.equals("")
					||server==null||server.equals(""))
				{
					return "must write user pwd and emailserver";
				}
				EmailManager.user=user;
				EmailManager.pwd=pwd;
				EmailManager.emailServer=server;
				return EmailManager.getInstance().sendMailAndPush(
					objectFactory,days,push,title,content);
			}
			else if(Integer.parseInt(type)==SHOW_GM_OP)
			{
				String strAccount=request.getParameter("user_account");
				if(strAccount==null||strAccount.length()==0)
					return "account is null";
				String gmAccount=URLDecoder.decode(strAccount,"utf-8");
				String year=request.getParameter("year");
				String month=request.getParameter("month");
				return selectGMOperation(gmAccount,Integer.parseInt(year),
					month);
			}
			log.warn("gm set port:ip==="+ip+" info==="+str);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "fail";
		}
		return str;
	}
	public String createEventShipData(ObjectArray ships)
	{
		Object[] troops=ships.getArray();
		String temp="";
		for(int i=0;i<troops.length;i++)
		{
			ShipDataEvent event=(ShipDataEvent)troops[i];
			temp+="事件id:"+event.getEventId()+"    ";
			for(int x=0;x<event.getShips().size();x++)
			{
				for(int j=0;j<SHIP_FOR_NAME.length;j+=2)
				{
					if(event.getShips().get(x)==Integer
						.parseInt(SHIP_FOR_NAME[j]))
					{
						temp+=SHIP_FOR_NAME[j+1]+":"
							+event.getShips().get(x+1)+"    ";
						break;
					}
				}
			}
			temp+="事件状态:"+event.getState()+"     ";
			temp+="目标坐标:"+SeaBackKit.getIslandLocation(event.getIndex())
				+"  ";
			temp+="</br>";
		}
		return temp;
	}
	/** createshipdata */
	public String createShipData(IntList list)
	{
		String temp="";
		for(int i=0;i<list.size();i+=2)
		{
			for(int j=0;j<SHIP_FOR_NAME.length;j+=2)
			{
				if(list.get(i)==Integer.parseInt(SHIP_FOR_NAME[j]))
				{
					temp+=SHIP_FOR_NAME[j+1]+"("+list.get(i+1)+")   ";
					break;
				}
			}
		}
		return temp;
	}
	// /** 验证超时账单 */
	// public void chargeOrder(Order order)
	// {
	// if(order.getVerifyInfo()==null||order.getVerifyInfo().equals(""))
	// return;
	// String str=VerifyHeroChargeInfoManager.getInstance()
	// .verifyHeroChargeInfo(order.getVerifyInfo(),order.getUserId(),
	// order.getUserName());
	// if(!str.startsWith("com.seawar"))
	// {
	// // 验证失败
	// order.setOrderState(Order.ORDER_FAIL);
	// }
	// else
	// {
	// int orderSid=1;
	// String splitStr[]=TextKit.split(str,":");
	// for(int i=0;i<BuyGemsPort.APP_ABOUT.length;i++)
	// {
	// if(splitStr[0].equals(BuyGemsPort.APP_ABOUT[i]))
	// {
	// orderSid=i+1;
	// break;
	// }
	// }
	// long transaction_id=Long.valueOf(splitStr[1]);
	// // 判断这个交易记录是否已经存在
	// String sql="SELECT * FROM orders WHERE transaction_id="
	// +transaction_id;
	// if(objectFactory.getOrderCache().getDbaccess().loadSql(sql)!=null)
	// {
	// //交易记录已存在 删除这个order
	// objectFactory.getOrderCache().getDbaccess().delete(order);
	// return;
	// }
	//			
	// }
	// objectFactory.getOrderCache().getDbaccess().save(order);
	// }

	/** 对玩家操作 player */
	public String excutePlayerGm(HttpRequestMessage request,Player player)
	{
		player.getIsland().pushAll(TimeKit.getSecondTime(),objectFactory);
		String type=request.getParameter("type");
		// 增加资源
		if(Integer.parseInt(type)==PLAYER_ADD_RESOUCRE)
		{
			// 金属
			String metal=request.getParameter("metal");
			// 石油
			String oil=request.getParameter("oil");
			// 硅
			String silicon=request.getParameter("silicon");
			// 低铀
			String uranium=request.getParameter("uranium");
			// 金钱
			String money=request.getParameter("money");
			// 宝石
			String gems=request.getParameter("gems");

			String addMaxGems=request.getParameter("maxGems");
			int honor=0;
			if(request.getParameter("honor")!=null
				&&!request.getParameter("honor").equals(""))
				honor=Integer.parseInt(request.getParameter("honor"));

			int exp=0;
			if(request.getParameter("exp")!=null
				&&!request.getParameter("exp").equals(""))
				exp=Integer.parseInt(request.getParameter("exp"));

			Resources.addResources(player.getResources(),Integer
				.parseInt(metal),Integer.parseInt(oil),Integer
				.parseInt(silicon),Integer.parseInt(uranium),Integer
				.parseInt(money),player);
			if(addMaxGems.equals("true"))
			{
				Resources.addGems(Integer.parseInt(gems),player
					.getResources(),player);
				int vipState=0;
				// 检查vip等级
				player.flushVIPlevel();
			}
			else if(addMaxGems.equals("reduce"))
			{
				Resources.reduceGems(Integer.parseInt(gems),player
					.getResources(),player);
			}
			else
			{
				Resources.addGemsNomal(Integer.parseInt(gems),player
					.getResources(),player);
			}
			if(Integer.parseInt(gems)!=0)
			{
				// 宝石日志记录
				objectFactory.createGemTrack(GemsTrack.GM_SEND,player
					.getId(),Integer.parseInt(gems),0,
					Resources.getGems(player.getResources()));
			}
			player.incrExp(exp,null);
			if(honor>0&&honor<10000000) player.incrHonorExp(honor);
			// 能量
			// 金属
			String energy=request.getParameter("energy");
			player.addEnergy(Integer.parseInt(energy));
			return "player_resource_add,player_name:"+player.getName()
				+",metal="+metal+",oil="+oil+",silicon="+silicon+",uranium="
				+uranium+",money="+money+",gems="+gems+",honor="+honor
				+",addMaxGems="+addMaxGems+",energy="+energy;
		}
		// 增加船只或者城防
		else if(Integer.parseInt(type)==PLAYER_ADD_SHIPS)
		{
			String shipType=request.getParameter("shipType");
			if(shipType==null||shipType.equals(""))
				return "shipType is null";
			String shipLevel=request.getParameter("shipLevel");
			if(shipLevel==null||shipLevel.equals(""))
				return "shipLevel is null";
			int sid=SHIPS_SIDS[Integer.parseInt(shipType)-1][Integer
				.parseInt(shipLevel)-1];
			Ship ship=(Ship)Role.factory.newSample(sid);
			if(ship==null) return "ship is null";
			String num=request.getParameter("shipNum");
			int nowNum=Integer.parseInt(num);
			if(nowNum>0)
				player.getIsland().addTroop(sid,nowNum,
					player.getIsland().getTroops());
			else
			{
				nowNum=Math.abs(nowNum);
				player.getIsland().reduceTroop(sid,nowNum,
					player.getIsland().getTroops());
			}
			JBackKit.sendResetTroops(player);
			// 船只日志
			IntList fightlist=new IntList();
			fightlist.add(sid);
			fightlist.add(Integer.parseInt(num));
			objectFactory.addShipTrack(0,ShipCheckData.GM_SEND,player,
				fightlist,null,false);
			return "player_ships_add:player_name:"+player.getName()+",sid="
				+sid+",num="+num;
		}
		// 增加建筑 PLAYER_ADD_BUILD
		else if(Integer.parseInt(type)==PLAYER_ADD_BUILD)
		{
			int index=Integer.parseInt(request.getParameter("index"));
			boolean bool=BuildInfo.isHaveIndex(index,player);
			if(!bool) return "you not have the index";
			Island builds=player.getIsland();
			PlayerBuild checkBuild=builds.getBuildByIndex(index,builds
				.getBuilds());
			if(checkBuild!=null) return "the index is have build";
			String buildSid=request.getParameter("buildSid");
			PlayerBuild build=(PlayerBuild)Build.factory.newSample(Integer
				.parseInt(buildSid));
			if(build==null) return "buildSid is wrong";
			bool=BuildInfo.isBuildThisType(build.getBuildType()+"",index);
			if(!bool) return "this build can not build on the index";
			build.setBuildLevel(1);
			player.getIsland().addBuildNow(build,index);
			return "player_new_build:player_name:"+player.getName()+"sid="
				+buildSid+",:index="+index+",buildName="
				+build.getBuildName();
		}
		// 建筑升级 PLAYER_UP_BUILD
		else if(Integer.parseInt(type)==PLAYER_UP_BUILD)
		{
			String index=request.getParameter("index");
			Island builds=player.getIsland();
			PlayerBuild checkBuild=builds.getBuildByIndex(Integer
				.parseInt(index),builds.getBuilds());
			if(checkBuild==null) return "the index is null";
			/** 是否得到自身升级条件 */
			if(checkBuild.getBuildLevel()>=checkBuild.getMaxLevel())
				return "buildLevel can not levelUp";
			int buildlevel=Integer.parseInt(request
				.getParameter("buildlevel"));
			if(buildlevel==0) return "buildLevel can not be 0";
			PlayerBuild directorBuild=builds.getBuildByIndex(
				BuildInfo.INDEX_0,builds.getBuilds());
			/** 指挥中心等级是否足够 */
			if(!isEnoughDirector(player,buildlevel)
				&&Integer.parseInt(index)!=BuildInfo.INDEX_0)
				return "not enough director,director level is==="
					+directorBuild.getBuildLevel();
			/** 是否正在升级 */
			if(builds.checkNowBuildingByIndex(Integer.parseInt(index)))
				return "index is uping";
			checkBuild.setBuildLevel(buildlevel);
			player.getTaskManager().checkTastEvent(null);
			return "player_build_level_up:player_name:"+player.getName()
				+",index="+index+",level="+buildlevel+",buildName="
				+checkBuild.getBuildName();
		}
		// 修改岛屿等级
		else if(Integer.parseInt(type)==ISLAND_LEVEL)
		{
			int islandLevel=Integer.parseInt(request
				.getParameter("islandLevel"));
			if(islandLevel<0||islandLevel>5) return "island maxLevel is 5";
			player.getIsland().setIslandLevel(islandLevel);
			return "player_island_level_set:player_name:"+player.getName()
				+",islandLevel="+islandLevel;
		}
		// 增加物品
		else if(Integer.parseInt(type)==ADD_PROP)
		{
			int propSid=Integer.parseInt(request.getParameter("propSid"));
			int num=Integer.parseInt(request.getParameter("propNum"));
			Prop prop=(Prop)Prop.factory.newSample(propSid);
			if(prop==null) return "prop is null";
			if(prop instanceof NormalProp) ((NormalProp)prop).setCount(num);
			player.getBundle().incrProp(prop,true);
			return "player_add_props,player_name:"+player.getName()
				+",propSid="+propSid+",num="+num;
		}

		return "fail";
	}
	/** 对事件操作 fight_event */
	public String excuteFightEventGm(HttpRequestMessage request)
	{
		return null;
	}

	/** 对邮件操作 messages */
	public String excuteMessagesGm(HttpRequestMessage request)
	{
		return null;
	}

	/** 对账号操作 users */
	public String excuteUsersGm(HttpRequestMessage request)
	{
		return null;
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	/** 指挥所等级是否足够 */
	public boolean isEnoughDirector(Player player,int level)
	{
		Island builds=player.getIsland();
		PlayerBuild checkBuild=builds.getBuildByIndex(BuildInfo.INDEX_0,
			builds.getBuilds());
		if(checkBuild==null) return false;
		return checkBuild.getBuildLevel()>=level;
	}

	/**
	 * @return manager
	 */
	public DSManager getManager()
	{
		return manager;
	}

	/**
	 * @param manager 要设置的 manager
	 */
	public void setManager(DSManager manager)
	{
		this.manager=manager;
	}

	/** 重新验证订单数据 */
	public void reviewOrder(Order order,Player player)
	{
		String str=VerifyHeroChargeInfoManager.getInstance()
			.verifyHeroChargeInfo(order.getVerifyInfo(),player.getId(),
				player.getName());
		if(!str.startsWith("com.seawar"))
		{
			// 验证失败
			order.setOrderState(Order.ORDER_FAIL);
			objectFactory.getOrderCache().getDbaccess().save(order);
			return;
		}
		int orderSid=1;
		String splitStr[]=TextKit.split(str,":");
//		for(int i=0;i<BuyGemsPort.APP_ABOUT.length;i++)
//		{
//			if(splitStr[0].equals(BuyGemsPort.APP_ABOUT[i]))
//			{
//				orderSid=i+1;
//				break;
//			}
//		}
		long transaction_id=Long.valueOf(splitStr[1]);
		// 判断这个交易记录是否已经存在
		String sql="SELECT * FROM orders WHERE transaction_id="
			+transaction_id;
		if(objectFactory.getOrderCache().getDbaccess().loadSql(sql)!=null)
		{
//			order.setTransaction_id(transaction_id);
			order.setOrderState(Order.TRANS_SAME);
			objectFactory.getOrderCache().getDbaccess().save(order);
			return;
		}
		Order trueOrder=(Order)Order.factory.newSample(orderSid);
		trueOrder.setId(objectFactory.getOrderCache().getUidkit()
			.getPlusUid());
		// 添加宝石
		Resources.addGems(trueOrder.getGems(),player.getResources(),player);
		int vipState=0;
		// 检查vip等级
		player.flushVIPlevel();
		// 宝石日志记录
		objectFactory.createGemTrack(GemsTrack.GEMS_PAY,player.getId(),
			trueOrder.getGems(),trueOrder.getSid(),
			Resources.getGems(player.getResources()));
		// 临时
		trueOrder.setIosType(1);
		trueOrder.setCreateAt(TimeKit.getSecondTime());
		trueOrder.setVerifyInfo(order.getVerifyInfo());
		// 临时
		trueOrder.setOrderState(Order.ORDER_SUCCESS);
//		trueOrder.setTransaction_id(transaction_id);
		trueOrder.setPlayerLevel(player.getLevel());
		trueOrder.setUserId(player.getId());
		trueOrder.setUserName(player.getName());
		// 存入order入数据库
		objectFactory.getOrderCache().getDbaccess().save(trueOrder);
		// 删除之前保存的记录订单
		objectFactory.getOrderCache().getDbaccess().delete(order);
	}

	private String getSystemMail()
	{
		StringBuilder sb=new StringBuilder();
		sb.append(
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n<title>系统邮件</title>\n")
			.append("<script language=\"javascript\">\n")
			.append("<!--\n")
			.append("function deleteSystemMail(id){\n")
			// .append("\talert(id);\n")
			.append("\tdocument.getElementById(\"mailid\").value=id;\n")
			.append("\tdocument.form1.submit();\n")
			.append("}\n")
			.append("-->\n")
			.append("</script>\n")
			.append("</head>\n<body>\n")
			.append(
				"<form action=\"/\" method=\"post\" id=\"form1\" name=\"form1\">\n")
			.append(
				"\t<input name=\"table_type\" type=\"hidden\" value=\"34\" />\n")
			.append(
				"\t<input name=\"port\" type=\"hidden\" value=\"1\" />\n")
			.append(
				"\t<input id=\"mailid\" name=\"mailid\" type=\"hidden\" value=\"1\" />\n")
			.append("</form>\n");

		sb
			.append("<table width=\"770\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"67\">序号</td><td width=\"83\">标题</td><td width=\"410\">内容</td><td width=\"182\">操作</td></tr>\n");
		String line="<tr><td>%d</td><td>%s</td><td>%s</td><td><a href=\"javascript:deleteSystemMail(%d);\" >删除</a></td></tr>\n";
		ArrayList list=objectFactory.getMessageCache().getSystemMessageMap();
		for(int i=0;i<list.size();i++)
		{
			Message msg=(Message)list.get(i);
			sb.append(String.format(line,i+1,msg.getTitle(),
				msg.getContent(),msg.getMessageId()));
		}
		sb.append("</table>\n");
		sb.append("</body>\n</html>");
		return sb.toString();
	}

	private String removeSystemMail(int mailId)
	{
		ArrayList list=objectFactory.getMessageCache().getSystemMessageMap();
		for(int i=0;i<list.size();i++)
		{
			Message mail=(Message)list.get(i);
			if(mail.getMessageId()==mailId)
			{
				objectFactory.getMessageCache().deleteCache(mail);
				return getSystemMail();
			}
		}
		return "mail not exixts.";
	}

	/** 清理等级为1-3的玩家岛屿坐标 且2天没有登录过 */
	public void clearIsland()
	{
		Object object[]=objectFactory.getPlayerCache().getCacheMap()
			.valueArray();
		for(int i=0;i<object.length;i++)
		{
			PlayerSave data=(PlayerSave)object[i];
			Player player=data.getData();
			int createTime=player.getCreateTime();
			createTime=TimeKit.getSecondTime()-createTime;
			int updateTime=player.getUpdateTime();
			updateTime=TimeKit.getSecondTime()-updateTime;
			if((player.getLevel()<=5&&(createTime>60*60*24*2)))
			{
				NpcIsland island=objectFactory.getIslandCache()
					.getPlayerIsland(player.getId());
				if(island==null) continue;
				island.setPlayerId(0);
				objectFactory.getIslandCache().getDbaccess().save(island);
			}
		}
	}
	/** 设置增加20%经验活动，BOSS加速刷新活动，充值返利，限时购买，打折活动，..... */
	public String setActivity(HttpRequestMessage request)
	{
		int id=Integer.parseInt(request.getParameter("id"));
//		if(!ActivityContainer.getInstance().checkId(id))
//		{
//			return "{\"no activityID=\""+id+"\"}";
//		}
		int type=Integer.parseInt(request.getParameter("op_type"));
		try
		{
			String stime=URLDecoder.decode(request.getParameter("stime"),
				"utf-8");
			String etime=URLDecoder.decode(request.getParameter("etime"),
				"utf-8");
			String initData=URLDecoder.decode(request
				.getParameter("initData"),"utf-8");
			if(id==ActivityContainer.DISCOUNT_ID
				||id==ActivityContainer.BOSS_ID
				||id==ActivityContainer.EXP_ID)
			{
				initData=request.getParameter("percent")+","+initData;
			}
			if(type==1)// 开启活动
			{
				return ActivityContainer.getInstance().startActivity(id,
					stime,etime,initData);
			}
			else if(type==2)// 重设活动
			{
//				return ActivityContainer.getInstance().resetActivity(id,stime,etime,initData);
			}
		}
		catch(Exception e)
		{

		}

//		return ActivityContainer.getInstance().getActivityState(id);
		return "";
	}

	public ByteBuffer sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// 设置port
		map.put("port","1");
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

	private String selectGMOperation(String gmAccount,int year,String month)
	{
		if(gmAccount==null||gmAccount.length()==0)
			return "Account is null.";
		if(gmAccount.indexOf('\'')>=0||gmAccount.indexOf('\"')>=0
		// || gmAccount.indexOf(" or ")>=0
		// || gmAccount.indexOf(" and ")>=0
		)
		{
			return "Account error.";
		}
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuilder sb=new StringBuilder();

		Date d;
		int start,end;
		try
		{
			String date=year+"-"+month+"-00 00:00:00";
			d=format.parse(date);
			start=(int)(d.getTime()/1000L);
			date=year+"-"+(month+1)+"-00 00:00:00";
			d=format.parse(date);
			end=(int)(d.getTime()/1000L);
		}
		catch(ParseException e)
		{
			return "date error.";
		}
		String sql="select * from gm_tracks where user_account='"+gmAccount
			+"' and created_at>"+start+" and created_at<="+end;
		if(month.equals("all"))
		{
			sql="select * from gm_tracks where user_account='"+gmAccount;
		}
		Fields[] fields=SqlKit.querys(gmManager.getConnectionManager(),sql);
		if(fields!=null&&fields.length>0)
		{
			sb.append("<table width=\"1024\" border=\"5\">");
			String fs="<tr><td>%s</td><td>%s</td><td width=\"500\">%s</td><td>%s</td><td>%s</td></tr>";
			String line=String.format(fs,"账号","玩家","参数","结果","时间");
			sb.append(line);

			for(int i=0;i<fields.length;i++)
			{
				String account=((StringField)fields[i].get("user_account")).value;
				String player=((StringField)fields[i].get("player_name")).value;
				String parameters=((StringField)fields[i].get("parameters")).value;
				parameters=URLDecoder.decode(parameters);
				int result=((IntField)fields[i].get("result")).value;
				long time=((IntField)fields[i].get("created_at")).value;
				line=String.format(fs,account,player,parameters,String
					.valueOf(result),format.format(new Date(time*1000L)));
				sb.append(line).append("\n\t");
			}
			sb.append("</table>");
		}
		else
			sb.append("no record");
		return sb.toString();
	}

	private HttpRespons sendHttpDataToCenter(int type,
		Map<String,String> params)
	{
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		// 设置port
		params.put("port","3");
		params.put("table_type",String.valueOf(type));
		HttpRespons re=null;
		try
		{
			re=request
				.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
					+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",params,
					null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return re;
	}

	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
		// TODO Auto-generated method stub
		return null;
	}
}