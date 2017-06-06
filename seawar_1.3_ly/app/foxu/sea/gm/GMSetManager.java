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

/** GM ���߹����� */
public class GMSetManager implements HttpHandlerInterface
{

	/** �������˼���sid */
	public final int ALLIANCE_SKILLS_SID[]={501,502,503,504,505,506,507,508,
		509,510};

	/** �Ƽ���Ӧ������ */
	public final String SCIENC_FOR_NAME[]={"sample_tech_name_01","��ǿ�ͽ���",
		"sample_tech_name_02","ս�н�ά��","sample_tech_name_03","�Ƽ�����",
		"sample_tech_name_04","Ǳͧά��","sample_tech_name_05","�߱���ͷ",
		"sample_tech_name_06","Ѳ��ά��","sample_tech_name_07","�Ƶ�ը��",
		"sample_tech_name_08","��ĸά��","sample_tech_name_15","����ұ��",
		"sample_tech_name_16","ʯ�ͷ���","sample_tech_name_17","���ұ��",
		"sample_tech_name_18","�˿��ᴿ","sample_tech_name_19","������",
		"sample_tech_name_20","ս������","sample_tech_name_21","ս���Ӷ�",
		"sample_tech_name_22","���ٺ���","sample_tech_name_23","����ѧ",
		"sample_tech_name_24","����","sample_tech_name_25","���漼��"};

	/** ��ֻ�¼����� */
	public final String FIGHT_EVENT_STYLE[]={"�촬���","ȡ���촬","����ս��","����ֻ",
		"����ؿ�","����NPC����","������ҵ���","����ظ�","������ֻ","���ر�����","פ��Ұ�ر�����","GM���",
		"����Э��","�������"};
	/***/
	public final String SHIP_FOR_NAME[]={"10001","����ս�н�","10002","����ս�н�",
		"10003","����ս�н�","10004","Ұ��ս�н�","10005","����ս�н�","10006","̩̹ս�н�",
		"10007","��η����","10011","����Ǳͧ","10012","����Ǳͧ","10013","����Ǳͧ","10014",
		"����Ǳͧ","10015","����Ǳͧ","10016","ڤ��Ǳͧ","10017","��������","10021","����Ѳ��",
		"10022","����Ѳ��","10023","����Ѳ��","10024","����Ѳ��","10025","���Ѳ��",
		"10026","��˹Ѳ��","10027","�������","10031","���ͺ�ĸ","10032","���ͺ�ĸ",
		"10033","���ͺ�ĸ","10034","���º�ĸ","10035","�۹�֮��","10036","ս��֮��","10037",
		"����֮��"};

	/** ��������Ӧ������ */
	public final String BUILD_FOR_NAME[]={"sample_build_name_01","������",
		"sample_build_name_02","ʯ�Ϳ�","sample_build_name_03","���",
		"sample_build_name_04","�˿�","sample_build_name_05","���",
		"sample_build_name_06","����","sample_build_name_07","ָ������",
		"sample_build_name_08","�о�Ժ","sample_build_name_09","���쳵��",
		"sample_build_name_10","","sample_build_name_14","���˽���"};

	/** ��ʯ����type */
	public final String GEMS_TRACK_TYPE[]={"��������","��������","��������","������Ʒ",
		"����������","�޸���ֻ","ˢ�º�ֱ�����ÿ������","��ֵ��ʯ","ϵͳ������ʯ","GM������ӱ�ʯ","ս���¼�����",
		"���뽱��","ÿ����ȡ��ʯ","���˾���","�齱","�����Ǩ","������ݼ���","�������","׼����ֵ","ȡ����ֵ",
		"ȫ������"};

	public final String GEMS_BUILD_NUM[]={"����������λ","���ĸ�����λ","���������λ"};

	public final String INDEX_FOR_BUILD_NAME[]={"ָ������","���","�о�Ժ","�ֿ�","�ֿ�",
		"���쳵��","���˽���","�վ�����","��������","�������","����","����","����","��Դ��","��Դ��"};

	/** htmlͷ��Ϣ */
	public static final String HTML_HEAD="<html xmlns='http://www.w3.org/1999/xhtml'><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8' /><title>GM����</title></head><body>";
	/** ��־��¼ */
	private static final Logger log=LogFactory.getLogger(GMSetManager.class);
	/** ���س��� �ɹ� ʧ�� */
	public final static String OK="ok",FAILS="fails";
	/**
	 * ����USER=1�˺� PLAYER=2���� FIGHT_EVENT=3ս���¼� MESSAGES=4�ʼ� SYSTEM=5ϵͳ��Ϣ
	 * VIEM_GEMS=6��ʯ�鿴 player_advice=7��ҽ���,VIEW_PLAYER_ADVICE=8�鿴��δ�������ҽ���
	 * VIEW_GAME_DATA=�鿴��Ӫ���� �رշ�����CLOSE_SERVER=14
	 * SET_NEW_TASK_MARK=16������������Ĳ���CHANGGE_PASSWORD�޸�����
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
	 * PLAYER_ADD_RESOUCRE=1������Դ PLAYER_ADD_SHIPS=2���Ӵ�ֻ
	 * PLAYER_ADD_BUILD=3����һ������ PLAYER_UP_BUIL=4�������� ���Ӿ���=5
	 */
	public final static int PLAYER_ADD_RESOUCRE=1097,PLAYER_ADD_SHIPS=2,
					PLAYER_ADD_BUILD=3,PLAYER_UP_BUILD=4,
					PLAYER_ADD_EXPERIENCE=5,ISLAND_LEVEL=6,ADD_PROP=7;
	/** �������ͺ͵ȼ���Ӧ��sid */
	public final static int SHIPS_SIDS[][]={
		{10001,10002,10003,10004,10005,10006,10007},
		{10011,10012,10013,10014,10015,10016,10017},
		{10021,10022,10023,10024,10025,10026,10027},
		{10031,10032,10033,10034,10035,10036,10037}};
	
	/** ��ҳ����ǩ��ֵ�ָ��� */
	public final static char MULTI_VALUE_SEPARATOR='&';
	/** ������������� */
	CreatObjectFactory objectFactory;

	DSManager manager;

	GMManager gmManager;

	public void setGMManager(GMManager gmManager)
	{
		this.gmManager=gmManager;
	}

	/** ��ȡ��ֵ��ǩ��ֵ�� */
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
			// ���Ӳ�������'@'��ͷ��
			if(key==null||key.length()<=0||key.charAt(0)!='@') continue;
			key=key.substring(1);
			String[] value=entry.getValue();
			if(value!=null&&value.length>0)
			{
				try
				{
					StringBuffer sb=new StringBuffer(value[0]);
					// ����ж��ֵ�÷ָ�����������Լ��ݶ�ѡ��
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

	/** ִ��GM���� */
	public String excuteString(HttpRequestMessage request,String ip)
	{
		String ret=callGMManager(request);
		if(ret==null)
			return "{\"success\":"+GMConstant.ERR_PASSWORD_ERROR
				+",\"msg\":\"�˺Ż����������\"}";
		else if(ret.length()>0) return ret;
		// �������ű�
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
				// �������
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
				// �������
				String text=URLDecoder.decode(request
					.getParameter("chatText"),"utf-8");
				str+="message is ="+text;
				// ������Ϣ
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
				// ��ʹ���ڴ�
				long totalMemory=Runtime.getRuntime().totalMemory()/kb/kb;
				// ʣ���ڴ�
				long freeMemory=Runtime.getRuntime().freeMemory()/kb/kb;
				// ����ʹ���ڴ�
				long maxMemory=Runtime.getRuntime().maxMemory()/kb/kb;
				str="totalMemory:"+totalMemory+"mb,freeMemory:"+freeMemory
					+"mb,maxMemory:"+maxMemory+"mb";
			}
			// ���
			else if(Integer.parseInt(type)==USERS)
			{

			}
			// �¼�
			else if(Integer.parseInt(type)==FIGHT_EVENTS)
			{

			}
			// �ʼ�
			else if(Integer.parseInt(type)==MESSAGES)
			{
				// �������
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
				/** ����Ʒsid */
				String awardSid=request.getParameter("awardSid");
				str="system message,player_name:"+player.getName()
					+",player_award_add:awardSid="+awardSid;
				Award award=(Award)Award.factory.getSample(Integer
					.parseInt(awardSid));
				if(award!=null)
					award.awardSelf(player,TimeKit.getSecondTime(),null,
						null,"",new int[]{EquipmentTrack.FROM_GM_ADD});
				// MessageData messageData=new MessageData();
				// // ����
				// String metal=request.getParameter("metal");
				// // ʯ��
				// String oil=request.getParameter("oil");
				// // ��
				// String silicon=request.getParameter("silicon");
				// // ����
				// String uranium=request.getParameter("uranium");
				// // ��Ǯ
				// String money=request.getParameter("money");
				// // ��ʯ
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
				// // ��Ʒsid
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
				// // ��sid
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
				// ˢ��ǰ̨
				JBackKit.sendRevicePlayerMessage(player,message,message
					.getRecive_state(),objectFactory);
			}
			// �鿴�ܹ���ֵ��ʯ
			else if(Integer.parseInt(type)==VIEM_GEMS)
			{
				int maxGems=objectFactory.getGems();
				str="the allGems is="+maxGems;
			}
			// ��ҽ���
			else if(Integer.parseInt(type)==PLAYER_ADVICE)
			{
				// ����ID
				String id=request.getParameter("adviceId");
				PlayerAdvice advice=objectFactory.getPlayerAdvice(id);
				if(advice==null) return "fail";
				if(advice.getState()!=0)
					return "this advice has been responed";
				// GM�ظ�
				String gmResponse=URLDecoder.decode(request
					.getParameter("gmResponse"),"utf-8");
				// String gems=request.getParameter("gmGems");
				// GM���ͱ�ʯ
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
				// String playeradvice="��������:"+advice.getContent()+"\n\n"
				// +"�ͷ��ظ���"+gmResponse;
				// // �洢��ҽ���
				// objectFactory.gmResponse(id,gmResponse);
				// // ����ҷ�����Ϣ
				// objectFactory.createMessage(0,advice.getPlayerId(),
				// playeradvice,"�۹�������Ӫ�Ŷ�",advice.getPlayerName(),0,
				// "�ǳ���л��������",objectFactory);
			}
			else if(Integer.parseInt(type)==VIEW_PLAYER_ADVICE)
			{
				PlayerAdvice advice[]=objectFactory.getPlayerAdvice();
				str=HTML_HEAD
					+"<table width='1224' border='5'><tr><td>����ID</td><td>�������</td><td>���ID</td><td>����</td><td>����</td><td>����ʱ��</td></tr>";
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
				// ���� ��������
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
					+"���ݿ��ѯʱ��:"
					+checkTime
					+"����<hr/>"
					+"<table width='1924' border='5'><tr><td>����</td><td>�����û�</td><td>����udid</td><td>DAU</td><td>��ֵ���</td><td>��ֵ����</td><td>MAU</td><td>���ͬʱ����</td><td>����������</td><td>��������</td><td>˫��������</td><td>��������</td><td>ARPU</td><td>ARPPU</td><td>�û�����</td><td>��ֵ�û�����</td><td>������</td><td>�ۼƳ�ֵ���</td><td>ȫ�û�arpu</td><td>ARPPU</td><td>��ʧ��(��)</td><td>��ʧ��(��)</td><td>��ǰ����</td></tr>";
				for(int i=0;i<listArray.length;i++)
				{
					GameData data=(GameData)listArray[i];
					save.Calculation(data);
					// �ڶ�����ʧ��
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

					// �ڶ�����ʧ��
					float arpu=data.getArpu()/10000f;
					arpu=(float)(Math.round(arpu*10000))/10000;

					// �ڶ�����ʧ��
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
			// ��ʯ��������
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
					+"���ݿ��ѯʱ��:"
					+checkTime
					+"����<hr/>"
					+"<table width='524' border='5'><tr><td>����</td><td>�ܹ���ʯ����</td></tr>";
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
							str+="<td>**��"+(j+1)+"�ι�������"+"</td>";
							str+="<td>"+track+"��</td>";
							str+="</tr>";
						}
					}
					// ˢ�º�ֱ�����ÿ�������������ÿ������
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
						str+="<td>**ˢ��ÿ���������"+"</td>";
						str+="<td>"+track+"��</td>";
						str+="</tr>";

						str+="<tr>";
						sql="SELECT count(*) FROM gem_tracks WHERE type="+i
							+" AND year="+year+" AND month="+month
							+" AND day>="+startDay+" AND day<="+lastDay
							+" AND item_id!="+1000;
						int track1=SeaBackKit.loadBySqlOneData(sql,
							objectFactory.getGemsTrackMemCache()
								.getDbaccess());
						str+="<td>**ֱ�����ÿ���������"+"</td>";
						str+="<td>"+(track1)+"��</td>";
						str+="</tr>";

						str+="<tr>";
						sql="SELECT count(*) FROM gem_tracks WHERE type="+i
							+" AND year="+year+" AND month="+month
							+" AND day>="+startDay+" AND day<="+lastDay
							+" AND gems="+28;
						int track2=SeaBackKit.loadBySqlOneData(sql,
							objectFactory.getGemsTrackMemCache()
								.getDbaccess());
						str+="<td>**����ÿ���������"+"</td>";
						str+="<td>"+track2+"��</td>";
						str+="</tr>";
					}
					// ����������
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
							str+="<td>**��"+(j+1)+"�ι�����λ"+"</td>";
							str+="<td>"+track+"��</td>";
							str+="</tr>";
						}
					}
					// ������Ʒ
					if(i==3)
					{
//						// ��Ʒsid�Ͷ�Ӧ������
//						String propSidAndName[]={"1","С������","2","СͰʯ��","3",
//							"С����","4","С���˿�","5","С�ѽ��","6","�������","7",
//							"��Ͱʯ��","8","�����","9","����˿�","10","��ѽ��","11",
//							"����ѫ�°�","12","�������","13","������Դ��","14","������Դ��",
//							"15","�ش���Դ��","16","��������","501","���󿪲�","502",
//							"ʯ�Ϳ���","503","��󿪲�","504","�˿󿪲�","505","�������",
//							"506","���Ӷ�","507","�߱�ըҩ","508","˳���о�","509",
//							"��ƽ��","1001","�����Լ�","1002","����ҩˮ","1003","������",
//							"1004","������","1005","Ǩ����","1006","�߼�Ǩ����","2021",
//							"����ѫ��","2022","ͳ����","2001","����ը��","2002","Ⱥ��10%",
//							"2020","�Զ��������"};
						String propSidAndName[]={"1","С������","2","СͰʯ��","3",
							"С����","4","С���˿�","5","С�ѽ��","6","�������","7",
							"��Ͱʯ��","8","�����","9","����˿�","10","��ѽ��","11",
							"����ѫ�±���","13","������Դ��","14","������Դ��","15","�ش���Դ��",
							"16","��������","412","ȼ���ֻ�����","413","�۱���Ļ���","416",
							"һ����ʯ","419","�����ʯ","501","���󿪲�","502","ʯ�Ϳ���",
							"503","��󿪲�","504","�˿󿪲�","505","�������","506",
							"���Ӷ�","507","�߱�ըҩ","508","˳���о�","509","��ƽ��",
							"510","�����Ƶ�","511","��������","512","ƶ�˵�","513",
							"��Ӧװ��","1005","Ǩ����","1006","�߼�Ǩ����","2001",
							"����֧Ԯ","2002","����ը��","2007","C98ȼ���ֻ�","2008",
							"�۱����","2009","��ʯ","2020","�Զ�����","2021","����ѫ��",
							"2022","ͳ����","4009","��������","4010","����ʯ��","4011",
							"������","4012","�����˿�","4013","������"};
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
							str+="<td>**��Ʒ("+propSidAndName[j+1]+")</td>";
							str+="<td>"+track+"��</td>";
							str+="</tr>";
						}
					}
					// ��������
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
							str+="<td>**������"+INDEX_FOR_BUILD_NAME[j]
								+"��</td>";
							str+="<td>"+track+"��</td>";
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
							str+="<td>����ѱ�ʯ"+track+"</td>";
							str+="</tr>";
						}
					}
					// �齱��ʯ
					if(i==GemsTrack.LOTTO)
					{
						String[] lottoType={
							String.valueOf(PublicConst.LOTTO_ADVANCE),
							"**�߼�",String.valueOf(PublicConst.LOTTO_LUXURY),
							"**����"};
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
							str+="<td>"+track+"��</td>";
							str+="<td>����ѱ�ʯ"+track*lottoGems[j/2]+"</td>";
							str+="</tr>";
						}
					}
				}
				str+="</table></body></html>";
				return str;
			}
			// ���ĳ����ұ�ʯ��������
			else if(Integer.parseInt(type)==VIEW_GEMS_PLAYER_DATA)
			{
				long time=TimeKit.getMillisTime();
				// �������
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
				// ĳ����ҵı�ʯ������־ -1 ��ʾȫ��
				GemsTrack tracks[]=objectFactory.getGemsTrackMemCache()
					.loadTracks(player.getId(),year,month,day,PublicConst.LOAD_ALL_GEM_COST);
				long checkTime=(TimeKit.getMillisTime()-time);
				str=HTML_HEAD
					+"���ݿ��ѯʱ��:"
					+checkTime
					+"����<hr/>"
					+"<table width='1024' border='5'><tr><td>id</td><td>����</td><td>playerId</td><td>��ʯ����</td><td>��ǰ��ʯ����</td><td>��Ʒid</td><td>����ʱ��</td></tr>";
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
				// Ŀǰ��δ�������ݿ��
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
				// ����
				String content=URLDecoder.decode(request
					.getParameter("content"),"utf-8");
				// messageData.setResources(resource);
				// ����
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
					// �رշ����� �ȶϷ���������
					num=objectFactory.getDsmanager().close();
					// ����˿ڰ�
					MinaConnectServer server=(MinaConnectServer)BackKit
						.getContext().get("server");
					server.close();
					// �洢����
					left=objectFactory.saveAndExit(true);
					// ����ʬ�û�
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
				// �������
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
					+"<table width='2024' border='5'><tr><td>playerId</td><td>�������</td><td>��ҵȼ�</td><td>����ʱ��</td><td>���ݸ���ʱ��</td><td>�����Դ</td><td>��ҽ���</td><td>��Ҵ�ֻ</td><td>��ҳǷ�����</td><td>����˺���Ϣ</td><td>udid��Ӧ���˺�</td><td>����</td><td>�Ƽ�</td></tr>";
				str+="<tr>";
				str+="<td>"+player.getId()+"</td>";
				str+="<td>"+player.getName()+"VIP�ȼ�="+player.getUser_state()
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
				str+="<td>"+"����:"+player.getResources()[0]+" ʯ��:"
					+player.getResources()[1]+" �裺"+player.getResources()[2]
					+" �ˣ�"+player.getResources()[3]+" ���:"
					+player.getResources()[4]+" ��ʯ��"
					+player.getResources()[5]+"��ֵ��ʯ:"
					+player.getResources()[6]+",���εȼ���"
					+player.getPlayerType()+",ͳ���ȼ�:"
					+player.getCommanderLevel()+",�����ȼ�:"
					+player.getHonor()[Player.HONOR_LEVEL_INDEX]+",��������:"
					+player.getIsland().getBuildNum()+",��������:"+name+",ս��ֵ:"
					+player.getFightScore()+"</td>";
				// ��ҽ���
				Object builds[]=player.getIsland().getBuildArray();
				String build="";
				for(int i=0;i<builds.length;i++)
				{
					PlayerBuild b=(PlayerBuild)builds[i];
					build+="  ";
					// ���ַ���
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
				// ��Ҵ�ֻ
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
					// ��ֻ����
					s+=strName;
					s+=""+troop.getNum();
					s+=",";
				}
				str+="<td>"+s+"</td>";
				s="";
				// ��ҳǷ�����
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
				// ��Ҽ���
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
				// ��ҿƼ�
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
						// �Ƽ������滻
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
				// �������
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
				// �������
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
					// ����ϵͳ����
				}
				str="gm set banned:="+banned+",mutime="+mutime+"hours";
			}
			else if(Integer.parseInt(type)==BANNED_DEVICE)
			{
				// �������
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
					// ֪ͨƽ̨��¼���
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
			// �ӹ�����˺�
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
					// ���ɳ���Ϊ6������ĸ��������ɵ�������룬�Ŷ�Il1����������ʶ���
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
					time*=60; // ����ʱ��Ϊ���ӣ�ת��Ϊ��
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
				// �������
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
				// �������
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
					+"<table width='524' border='5'><tr><td>���ߵ���</td></tr>";
				str+="<td>"+pepoe+"</td>";
				str+="</tr>";
				str+="</table></body></html>";
				return str;
			}
			// �鿴��ҽ����ս���¼�
			else if(Integer.parseInt(type)==VIEW_FIGHT_EVENT)
			{

			}
			else if(Integer.parseInt(type)==VIEW_PLAYER_GEMS)
			{
				// �������
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				Player player=objectFactory
					.getPlayerByName(playerName,false);
				if(player==null) return "player is null";
				// �鿴ĳ����ҳ�ֵ����״̬
				// �ɹ�������
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
					+"<table width='524' border='5'><tr><td>����ɹ�����</td><td>��֤��ʱ����</td><td>��֤ʧ������</td><td>������¼��ͬ����</td></tr>";
				str+="<td>"+num1+"</td>";
				str+="<td>"+num2+"</td>";
				str+="<td>"+num3+"</td>";
				str+="<td>"+num4+"</td>";
				str+="</tr>";
				str+="</table>";
				str+="</body></html>";
			}
			// ������֤ĳ����ҵĳ�ʱ����
			else if(Integer.parseInt(type)==CHECK_PLAYER_ORDER)
			{
				// �������
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
			// �鿴���Ѽ�¼
			else if(Integer.parseInt(type)==BUY_GEMS_INFO)
			{
				// �ɹ�������
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
					+"<table width='524' border='5'><tr><td>����ɹ�����</td><td>��֤��ʱ����</td><td>��֤ʧ������</td><td>������¼��ͬ����</td></tr>";
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
				// str+="�����";
				// str="<table width='524'
				// border='5'><tr><td>����ɹ�����</td><td>��֤��ʱ����</td><td>��֤ʧ������</td></tr>";
				// }
				str+="</body></html>";
			}
			else if(Integer.parseInt(type)==BUY_GEMS_MORE_INFO)
			{
				String sql="select user_name,sum(gems),sum(money) as money from orders group by user_name order by money desc limit 50";
				Fields fields[]=((OrderGameDBAccess)objectFactory
					.getOrderCache().getDbaccess()).loadSqls(sql);
				str=HTML_HEAD
					+"<table width='524' border='5'><tr><td>����</td><td>�������</td><td>�ܹ���ֵ��ʯ</td><td>�ܹ���ֵRMB</td></tr>";
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
			// �޸�����
			else if(Integer.parseInt(type)==CHANGGE_PASSWORD)
			{
				// �������
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
				// ��������
				String allianceName=URLDecoder.decode(request
					.getParameter("allianceName"),"utf-8");
				if(allianceName==null||allianceName.equals(""))
				{
					return "allianceName is null";
				}
				Alliance alliance=objectFactory.getAllianceMemCache()
					.loadByName(allianceName,true);
				if(alliance==null) return "alliance is null";
				// �ȼ�����
				String allianceLevel=request.getParameter("allianceLevel");
				if(allianceLevel.equals("true"))
				{
					alliance.incrExp(10000);
					// �������˼�������Ϊ����
					for(int i=0;i<ALLIANCE_SKILLS_SID.length;i++)
					{
						alliance.incrSkillExp(10000,ALLIANCE_SKILLS_SID[i]);
					}
				}
			}
			// ȫ����Ҽӱ�ʯ
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
								// ��¼δ�����ɹ���
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
				// �������
				String playerName=URLDecoder.decode(request
					.getParameter("playerName"),"utf-8");
				if(playerName==null||playerName.equals(""))
				{
					return "playerName is null";
				}
				// һ��
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
				// ĳ����ҵı�ʯ������־
				ShipCheckData data[]=objectFactory.getShipCache()
					.loadTracks(player.getId(),viewTime,null);
				long checkTime=(TimeKit.getMillisTime()-time);
				str=HTML_HEAD
					+"���ݿ��ѯʱ��:"
					+checkTime
					+"����<hr/>"
					+"<table width='1324' border='5'><tr><td>id</td><td>����</td><td>playerId</td><td>��ǰ�ۿڱ���</td><td>��ǰ�˱�</td><td>��ǰ�¼�����</td><td>�˼�¼Ӱ�����</td><td>������Ϣ</td><td>����ʱ��</td></tr>";
				ShipCheckData temp;
				// if(data!=null)
				// {
				// // ���� ��������
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
				// // �ۿڱ���
				// str+="<td>"+createShipData(cdata.getLeftList())
				// +"</td>";
				// // �˱�
				// str+="<td>"+createShipData(cdata.getHurtList())
				// +"</td>";
				// // �¼�����
				// str+="<td>"
				// +createEventShipData(cdata.getEventList())
				// +"</td>";
				// // ���β�����
				// str+="<td>"+createShipData(cdata.getList())+"</td>";
				// // ������Ϣ
				// str+="<td>"+cdata.getExtra()+"</td>";
				// str+="<td>"
				// +SeaBackKit.formatDataTime(cdata.getCreateAt())
				// +"</td>";
				// str+="</tr>";
				// }
				// }
				// // �����ڴ��е�
				// Object shipdata[]=objectFactory.getShipCache().getList()
				// .valueArray();
				// if(shipdata!=null)
				// {
				// // ���� ��������
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
//						str+="<td>"+"�ڴ��е�"+cdata.getId()+"</td>";
//						str+="<td>"+FIGHT_EVENT_STYLE[cdata.getType()]
//							+"</td>";
//						str+="<td>"+cdata.getPlayerId()+"</td>";
//						// �ۿڱ���
//						str+="<td>"+createShipData(cdata.getLeftList())
//							+"</td>";
//						// �˱�
//						str+="<td>"+createShipData(cdata.getHurtList())
//							+"</td>";
//						// �¼�����
//						str+="<td>"
//							+createEventShipData(cdata.getEventList())
//							+"</td>";
//						// ���β�����
//						str+="<td>"+createShipData(cdata.getList())+"</td>";
//						// ������Ϣ
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
			// �鿴ϵͳ�ʼ�
			else if(Integer.parseInt(type)==VIEW_SYSTEM_MAIL)
			{
				return getSystemMail();
			}
			// ɾ��ϵͳ�ʼ�
			else if(Integer.parseInt(type)==REMOVE_SYSTEM_MAIL)
			{
				String strId=request.getParameter("mailid");
				return removeSystemMail(Integer.parseInt(strId));
			}
			// // ���ü򵥻
			// else if(Integer.parseInt(type)==SET_SIMPLE_ACTIVITY)
			// {
			// return setSimpleActivity(request);
			// }
			// ������ʱ���򣬴��ۡ�������
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
			temp+="�¼�id:"+event.getEventId()+"    ";
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
			temp+="�¼�״̬:"+event.getState()+"     ";
			temp+="Ŀ������:"+SeaBackKit.getIslandLocation(event.getIndex())
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
	// /** ��֤��ʱ�˵� */
	// public void chargeOrder(Order order)
	// {
	// if(order.getVerifyInfo()==null||order.getVerifyInfo().equals(""))
	// return;
	// String str=VerifyHeroChargeInfoManager.getInstance()
	// .verifyHeroChargeInfo(order.getVerifyInfo(),order.getUserId(),
	// order.getUserName());
	// if(!str.startsWith("com.seawar"))
	// {
	// // ��֤ʧ��
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
	// // �ж�������׼�¼�Ƿ��Ѿ�����
	// String sql="SELECT * FROM orders WHERE transaction_id="
	// +transaction_id;
	// if(objectFactory.getOrderCache().getDbaccess().loadSql(sql)!=null)
	// {
	// //���׼�¼�Ѵ��� ɾ�����order
	// objectFactory.getOrderCache().getDbaccess().delete(order);
	// return;
	// }
	//			
	// }
	// objectFactory.getOrderCache().getDbaccess().save(order);
	// }

	/** ����Ҳ��� player */
	public String excutePlayerGm(HttpRequestMessage request,Player player)
	{
		player.getIsland().pushAll(TimeKit.getSecondTime(),objectFactory);
		String type=request.getParameter("type");
		// ������Դ
		if(Integer.parseInt(type)==PLAYER_ADD_RESOUCRE)
		{
			// ����
			String metal=request.getParameter("metal");
			// ʯ��
			String oil=request.getParameter("oil");
			// ��
			String silicon=request.getParameter("silicon");
			// ����
			String uranium=request.getParameter("uranium");
			// ��Ǯ
			String money=request.getParameter("money");
			// ��ʯ
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
				// ���vip�ȼ�
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
				// ��ʯ��־��¼
				objectFactory.createGemTrack(GemsTrack.GM_SEND,player
					.getId(),Integer.parseInt(gems),0,
					Resources.getGems(player.getResources()));
			}
			player.incrExp(exp,null);
			if(honor>0&&honor<10000000) player.incrHonorExp(honor);
			// ����
			// ����
			String energy=request.getParameter("energy");
			player.addEnergy(Integer.parseInt(energy));
			return "player_resource_add,player_name:"+player.getName()
				+",metal="+metal+",oil="+oil+",silicon="+silicon+",uranium="
				+uranium+",money="+money+",gems="+gems+",honor="+honor
				+",addMaxGems="+addMaxGems+",energy="+energy;
		}
		// ���Ӵ�ֻ���߳Ƿ�
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
			// ��ֻ��־
			IntList fightlist=new IntList();
			fightlist.add(sid);
			fightlist.add(Integer.parseInt(num));
			objectFactory.addShipTrack(0,ShipCheckData.GM_SEND,player,
				fightlist,null,false);
			return "player_ships_add:player_name:"+player.getName()+",sid="
				+sid+",num="+num;
		}
		// ���ӽ��� PLAYER_ADD_BUILD
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
		// �������� PLAYER_UP_BUILD
		else if(Integer.parseInt(type)==PLAYER_UP_BUILD)
		{
			String index=request.getParameter("index");
			Island builds=player.getIsland();
			PlayerBuild checkBuild=builds.getBuildByIndex(Integer
				.parseInt(index),builds.getBuilds());
			if(checkBuild==null) return "the index is null";
			/** �Ƿ�õ������������� */
			if(checkBuild.getBuildLevel()>=checkBuild.getMaxLevel())
				return "buildLevel can not levelUp";
			int buildlevel=Integer.parseInt(request
				.getParameter("buildlevel"));
			if(buildlevel==0) return "buildLevel can not be 0";
			PlayerBuild directorBuild=builds.getBuildByIndex(
				BuildInfo.INDEX_0,builds.getBuilds());
			/** ָ�����ĵȼ��Ƿ��㹻 */
			if(!isEnoughDirector(player,buildlevel)
				&&Integer.parseInt(index)!=BuildInfo.INDEX_0)
				return "not enough director,director level is==="
					+directorBuild.getBuildLevel();
			/** �Ƿ��������� */
			if(builds.checkNowBuildingByIndex(Integer.parseInt(index)))
				return "index is uping";
			checkBuild.setBuildLevel(buildlevel);
			player.getTaskManager().checkTastEvent(null);
			return "player_build_level_up:player_name:"+player.getName()
				+",index="+index+",level="+buildlevel+",buildName="
				+checkBuild.getBuildName();
		}
		// �޸ĵ���ȼ�
		else if(Integer.parseInt(type)==ISLAND_LEVEL)
		{
			int islandLevel=Integer.parseInt(request
				.getParameter("islandLevel"));
			if(islandLevel<0||islandLevel>5) return "island maxLevel is 5";
			player.getIsland().setIslandLevel(islandLevel);
			return "player_island_level_set:player_name:"+player.getName()
				+",islandLevel="+islandLevel;
		}
		// ������Ʒ
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
	/** ���¼����� fight_event */
	public String excuteFightEventGm(HttpRequestMessage request)
	{
		return null;
	}

	/** ���ʼ����� messages */
	public String excuteMessagesGm(HttpRequestMessage request)
	{
		return null;
	}

	/** ���˺Ų��� users */
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
	 * @param objectFactory Ҫ���õ� objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	/** ָ�����ȼ��Ƿ��㹻 */
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
	 * @param manager Ҫ���õ� manager
	 */
	public void setManager(DSManager manager)
	{
		this.manager=manager;
	}

	/** ������֤�������� */
	public void reviewOrder(Order order,Player player)
	{
		String str=VerifyHeroChargeInfoManager.getInstance()
			.verifyHeroChargeInfo(order.getVerifyInfo(),player.getId(),
				player.getName());
		if(!str.startsWith("com.seawar"))
		{
			// ��֤ʧ��
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
		// �ж�������׼�¼�Ƿ��Ѿ�����
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
		// ��ӱ�ʯ
		Resources.addGems(trueOrder.getGems(),player.getResources(),player);
		int vipState=0;
		// ���vip�ȼ�
		player.flushVIPlevel();
		// ��ʯ��־��¼
		objectFactory.createGemTrack(GemsTrack.GEMS_PAY,player.getId(),
			trueOrder.getGems(),trueOrder.getSid(),
			Resources.getGems(player.getResources()));
		// ��ʱ
		trueOrder.setIosType(1);
		trueOrder.setCreateAt(TimeKit.getSecondTime());
		trueOrder.setVerifyInfo(order.getVerifyInfo());
		// ��ʱ
		trueOrder.setOrderState(Order.ORDER_SUCCESS);
//		trueOrder.setTransaction_id(transaction_id);
		trueOrder.setPlayerLevel(player.getLevel());
		trueOrder.setUserId(player.getId());
		trueOrder.setUserName(player.getName());
		// ����order�����ݿ�
		objectFactory.getOrderCache().getDbaccess().save(trueOrder);
		// ɾ��֮ǰ����ļ�¼����
		objectFactory.getOrderCache().getDbaccess().delete(order);
	}

	private String getSystemMail()
	{
		StringBuilder sb=new StringBuilder();
		sb.append(
				"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n<title>ϵͳ�ʼ�</title>\n")
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
			.append("<table width=\"770\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"67\">���</td><td width=\"83\">����</td><td width=\"410\">����</td><td width=\"182\">����</td></tr>\n");
		String line="<tr><td>%d</td><td>%s</td><td>%s</td><td><a href=\"javascript:deleteSystemMail(%d);\" >ɾ��</a></td></tr>\n";
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

	/** ����ȼ�Ϊ1-3����ҵ������� ��2��û�е�¼�� */
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
	/** ��������20%������BOSS����ˢ�»����ֵ��������ʱ���򣬴��ۻ��..... */
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
			if(type==1)// �����
			{
				return ActivityContainer.getInstance().startActivity(id,
					stime,etime,initData);
			}
			else if(type==2)// ����
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
		// ����port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"
				+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,null);
		}
		catch(IOException e)
		{
			// TODO �Զ����� catch ��
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
			String line=String.format(fs,"�˺�","���","����","���","ʱ��");
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
		// ����port
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