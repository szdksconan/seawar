/**
 * 
 */
package foxu.ds;

import java.util.Calendar;
import java.util.TimeZone;

import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.io.BytesReader;
import mustang.io.BytesWriter;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.httpserver.HttpServer;
import foxu.cross.goalleague.ClientLeagueManager;
import foxu.cross.warclient.ClientWarManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.achieve.AchieveManager;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.LoginRewardActivity;
import foxu.sea.alliance.alliancebattle.AllianceBattleManager;
import foxu.sea.comrade.ComradeHandler;
import foxu.sea.growth.GrowthPlanManager;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.recruit.RecruitKit;
import foxu.sea.recruit.RecruitWelfareManager;
import foxu.sea.weather.WorldWeatherManager;

/**
 * dsʹ�õ����л������л�����
 * 
 * @author rockzyt
 */
public class PlayerKit implements BytesReader,BytesWriter
{

	/* fields */
	CreatObjectFactory factory;
	private static final PlayerKit playerKit=new PlayerKit();

	ClientWarManager cWarManager;
	
	RecruitWelfareManager recruitManager;
	
	GrowthPlanManager growthPlanManager;
	
	ClientLeagueManager cLeagueManager;

	
	public RecruitWelfareManager getRecruitManager()
	{
		return recruitManager;
	}

	
	public void setRecruitManager(RecruitWelfareManager recruitManager)
	{
		this.recruitManager=recruitManager;
	}

	public ClientWarManager getcWarManager()
	{
		return cWarManager;
	}

	public void setcWarManager(ClientWarManager cWarManager)
	{
		this.cWarManager=cWarManager;
	}

	
	public GrowthPlanManager getGrowthPlanManager()
	{
		return growthPlanManager;
	}


	
	public void setGrowthPlanManager(GrowthPlanManager growthPlanManager)
	{
		this.growthPlanManager=growthPlanManager;
	}


	private PlayerKit()
	{

	}

	public static PlayerKit getInstance()
	{
		return playerKit;
	}

	public Object bytesRead(ByteBuffer data)
	{
		return null;
	}
	public void bytesWrite(Object obj,ByteBuffer data)
	{
		Player p=(Player)obj;
		int loginday=p.pushLoginDays();
		// �ɾ����ݲɼ�
		AchieveCollect.seriesLogin(loginday,p);
		//�±�����
		RecruitKit.setDays(p);
		// ����ɾͻ���
		p.computeAchieveScore();
		// ˢ�³ɾ�����
		flushAchieveRank(p);
		// ���push����
		p.setAttribute(PublicConst.FIGHT_PUSH_TIME,null);
		p.getTaskManager().pushNextTask();
		int time=TimeKit.getSecondTime();
		int islandIndex=-1;
		NpcIsland island=factory.getIslandCache().getPlayerIsland(p.getId());
		if(island!=null)
		{
			islandIndex=island.getIndex();
		}
		// ��ҵ��챻���� ���·���
		if(islandIndex==-1)
		{
			NpcIsland newIsland=factory.getIslandCache().getRandomSpace();
			if(newIsland==null)
				throw new DataAccessException(0,"The world is full");
			newIsland.setPlayerId(p.getId());
			factory.getIslandCache().getDbaccess().save(newIsland);
			factory.getIslandCache().addPlayerIsLandMap(newIsland);
			factory.getIslandCache().removeSpaceIsland(newIsland);
			islandIndex=newIsland.getIndex();
		}
		factory.pushAll(p,TimeKit.getSecondTime());
		// ����ɾ�
		AchieveManager.instance.pushOldAchieve(p);
		// ���л�֮ǰ����ս�����Խ�����ܳ��ֲ鿴�����Ϣ�͵��첻ͬ��
		SeaBackKit.setPlayerFightScroe(p,factory);
		SeaBackKit.checkNewPlayerMark(p);
		data.writeInt(time);
		p.showBytesWrite(data,time,factory);
		// ���ս���л�
		cWarManager.showBytesWriteWar(p,data);
		// ������������л�
		cLeagueManager.showBytesWrite(factory,p,data,time);
		p.showBytesWrite2(data,time,factory);
		// ���ͻ���л�
		ActivityContainer.getInstance().showBytesWriteNew(p,data);
		//�±����� 
		recruitManager.showBytesWrite(p,data);
		AllianceBattleManager.battleManager.showByteWriteAllianceInfo(data,p);
		ComradeHandler.getInstance().getComrades(data,p,TimeKit.getSecondTime());
		ComradeHandler.getInstance().showBytesWriteCombradeTasks(p,data);
		//�ɳ��ƻ�
		growthPlanManager.showBytesWrite(data,p);
		data.writeByte(WorldWeatherManager.getInstance().getPlayerWeather(p));
		
		p.getFriendInfo().infoWrite(data,factory);
		
		data.writeInt(islandIndex);
		data.writeInt(time
			+(TimeZone.getDefault().getRawOffset()+Calendar.getInstance()
				.get(Calendar.DST_OFFSET))/1000);
		// ��½���� ��½����
		loginRewardActivityCount(p);
		// ��״̬������
		showBytesWriteBingding(data,p);
		// ��תҳ������
		showBytesWriteJump(data,p);
		// �������л�
		showBytesWriteSwitch(data);
		// �������˺ŵ�״̬
		showBytesWriteBindingType(data,p);
		data.writeShort(HttpServer.DEFAULT_PORT);// http�˿�
		// ����ҳ��
		// data.writeByte(0);
		p.setUpdateTime(TimeKit.getSecondTime());

	}

	/**
	 * ÿ�յ�½��û���½��������
	 * 
	 * @param p
	 */
	private void loginRewardActivityCount(Player p)
	{
		LoginRewardActivity act=(LoginRewardActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.LOGIN_REWARD,0);
		if(act==null
			||!act.isOpen(TimeKit.getSecondTime())
			//��ʽ
			||SeaBackKit.getDayOfYear()==act.getPlayerRecordDay(p.getId())
//			||SeaBackKit
//				.isSameDay(p.getUpdateTime(),TimeKit.getSecondTime())
//			||SeaBackKit
//			.isSameDay(p.getExitTime(),TimeKit.getSecondTime())
			//������
//			||(p.getUpdateTime()+120)>=TimeKit.getSecondTime()
			)
			return;
		act.setPlayerLoginDays(p.getId(),1,SeaBackKit.getDayOfYear());
		act.setChange(true);
	}

	/**
	 * ���л����ѣ����������б�  2.6+���� ����ϵͳ��Ϊ˫��
	 * 
	 * @param player
	 * @param data
	 * @param listKey
	 */
	@Deprecated
	public void bytesWriteFriendList(Player player,ByteBuffer data,
		String listKey)
	{
		String friends=player.getAttributes(listKey);
		if(friends==null||friends.length()==0)
		{
			data.writeByte(0);
		}
		else
		{
			String[] names=TextKit.split(friends,",");
			data.writeByte(names.length);
			for(int i=0;i<names.length;i++)
			{
				Player p = factory.getPlayerByName(names[i],false);
				data.writeUTF(names[i]);
				data.writeInt(p.getAttrHead());
				data.writeInt(p.getAttrHeadBorder());
			}
		}
	}

	/** ����session���player */
	public Player getPlayer(Session s)
	{
		if(s==null||s.getAttribute(Player.KEY_ID)==null) return null;
		int id=(Integer)s.getAttribute(Player.KEY_ID);
		return factory.getPlayerCache().load(id+"");
	}

	/** ����session���player ������ı��б� */
	public Player getPlayerOnly(Session s)
	{
		if(s==null||s.getAttribute(Player.KEY_ID)==null) return null;
		int id=(Integer)s.getAttribute(Player.KEY_ID);
		return factory.getPlayerCache().loadPlayerOnly(id+"");
	}

	/**
	 * @return factory
	 */
	public CreatObjectFactory getFactory()
	{
		return factory;
	}

	
	public ClientLeagueManager getcLeagueManager()
	{
		return cLeagueManager;
	}


	
	public void setcLeagueManager(ClientLeagueManager cLeagueManager)
	{
		this.cLeagueManager=cLeagueManager;
	}


	/**
	 * @param factory Ҫ���õ� factory
	 */
	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	/** ˢ�³ɾ����� */
	public void flushAchieveRank(Player player)
	{
		// �ɾ�����
		String sql="SELECT COUNT(*) FROM players WHERE achieveScore>"
			+player.getAchieveScore();
		Fields field=factory.getPlayerCache().getDbaccess().loadSql(sql);
		int achieveScoreRank=Integer.parseInt(field.getArray()[0].getValue()
			.toString());
		player.setAchieveScoreRank(achieveScoreRank+1);
	}
	/** ǰ̨���ص����л� **/
	public void showBytesWriteSwitch(ByteBuffer data)
	{
		for(int i=0;i<PublicConst.SWITCH_STATE.length;i++)
		{
			data.writeBoolean(PublicConst.SWITCH_STATE[i]);
		}
	}
	/** �õ���ҵİ�״̬ **/
	public void showBytesWriteBingding(ByteBuffer data,Player p)
	{
		int timeNow=TimeKit.getSecondTime();
		User user=factory.getUserDBAccess().load(p.getUser_id()+"");
		// û���ο��˺�ֱ�ӷ��͹ر�
		if(user.getUserType()==User.USER
			||!SeaBackKit.isContainValue(PublicConst.BINDING_PLATID,p.getPlat()))
		{
			data.writeByte(PublicConst.COMMON_BINDING);
			data.writeUTF(PublicConst.BINDING_REASON);
			return;
		}
		int state=PublicConst.GAME_BINDING;
		// �����0,2��ֱ�ӷ��� 1�Ļ�Ҫ����֤
		if(state==PublicConst.ADVISE_BINDING)
		{
			String btime=p.getAttributes(PublicConst.PLAYER_BTIME);
			boolean bindState=false;
			if(btime!=null&&btime.length()!=0)
				bindState=SeaBackKit.isSameDay(TextKit.parseInt(btime),
					timeNow);
			if(!bindState)
			{
				data.writeByte(PublicConst.ADVISE_BINDING);
				p.setAttribute(PublicConst.PLAYER_BTIME,timeNow+"");
			}
			else
				data.writeByte(PublicConst.COMMON_BINDING);
		}
		else
			data.writeByte(PublicConst.GAME_BINDING);
		data.writeUTF(PublicConst.BINDING_REASON);
	}

	/** �õ���ҵİ�״̬ **/
	public void showBytesWriteJump(ByteBuffer data,Player p)
	{
		int timeNow=TimeKit.getSecondTime();
		int state=PublicConst.JUMP_ADDRESS;
		// �����ַΪ�շ��عر�״̬
		if(getUrlAddress(p.getPlat())==null
			||"".equals(getUrlAddress(p.getPlat())))
		{
			data.writeByte(PublicConst.COMMON_BINDING);
			data.writeUTF(PublicConst.JUMP_REASON);
			data.writeUTF(getUrlAddress(p.getPlat()));
			return;
		}
		// ��֤�ȼ�����
		if(p.getLevel()<PublicConst.JUMP_LEVEL)
		{
			data.writeByte(PublicConst.COMMON_BINDING);
			data.writeUTF(PublicConst.JUMP_REASON);
			data.writeUTF(getUrlAddress(p.getPlat()));
			return;
		}
		// ��֤��ǰ״̬�Ƿ��ǽ���
		if(state!=PublicConst.ADVISE_BINDING)
		{
			data.writeByte(PublicConst.JUMP_ADDRESS);
			data.writeUTF(PublicConst.JUMP_REASON);
			data.writeUTF(getUrlAddress(p.getPlat()));
			return;
		}
		boolean flag=false;
		String btime=p.getAttributes(PublicConst.PLAYER_URL_TIME);
		if(btime!=null&&btime.length()!=0)
			flag=SeaBackKit.isSameDay(TextKit.parseInt(btime),timeNow);
		if(!flag)
		{
			data.writeByte(PublicConst.ADVISE_BINDING);
			p.setAttribute(PublicConst.PLAYER_URL_TIME,timeNow+"");
		}
		else
			data.writeByte(PublicConst.COMMON_BINDING);
		data.writeUTF(PublicConst.JUMP_REASON);
		data.writeUTF(getUrlAddress(p.getPlat()));
	}

	/** ǰ̨�����İ�״̬ **/
	public void showBytesWriteBindingType(ByteBuffer data,Player p)
	{
		String str=p.getAttributes(PublicConst.ACCOUNT_INFO);
		if(str==null||str.length()==0)
		{
			data.writeBoolean(false);
			return;
		}
		String info[]=str.split(",");
		if(TextKit.parseInt(info[0])==1)
		{
			data.writeBoolean(true);
		}
		else
			data.writeBoolean(false);
	}

	/** ��ȡurl��ַ **/
	public String getUrlAddress(String platid)
	{
		if(PublicConst.URL_ADDRESS==null||PublicConst.URL_ADDRESS.length<=0)
			return "";
		for(int i=0;i<PublicConst.URL_ADDRESS.length;i+=2)
		{
			if(PublicConst.URL_ADDRESS[i].equals(platid))
			{
				return PublicConst.URL_ADDRESS[i+1];
			}
		}
		return "";
	}
}
