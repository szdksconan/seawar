package foxu.sea.alliance;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.AlliancePort;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * 联盟会长自动移交管理器
 * 
 * @author Alan
 */
public class AllianceAutoTransfer implements TimerListener
{

	/** 检测周期时间 */
	public static final int CHECK_CIRCLE_TIME=6*3600;
	/** 移交周期时间 */
	public static final int TRANSFER_CIRCLE_TIME=7*24*3600;
	CreatObjectFactory objectFactory;
	/** 联盟id */
	private IntList autoAlliances;
	TimerEvent autoTransfer;
	Object lock=new Object();

	public void init(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
		autoAlliances=new IntList();

		Object[] alliances=objectFactory.getAllianceMemCache().getCacheMap()
			.valueArray();
		for(int i=0;i<alliances.length;i++)
		{
			Alliance alliance=((AllianceSave)alliances[i]).getData();
			autoAlliances.add(alliance.getId());
		}
		autoTransfer=new TimerEvent(this,"auto_transfer",CHECK_CIRCLE_TIME);
		TimerCenter.getSecondTimer().add(autoTransfer);
	}

	/** 添加或更新联盟 */
	public void updateAutoAlliance(Alliance alliance)
	{
		// 不存在于列表则为新建联盟
		if(!autoAlliances.contain(alliance.getId()))
			autoAlliances.add(alliance.getId());
		// 存在则为人为转移会长
		else
		{
			Player newMaster=objectFactory.getPlayerCache().load(
				alliance.getMasterPlayerId()+"");
			// 记录新会长的上任基准时间
			newMaster.setAttribute(PublicConst.ALLIANCE_TRANSFER_TIME,
				TimeKit.getSecondTime()+"");
		}
	}

	/** 维护自动升级队列 */
	private void autoTransferArray(int time)
	{
		synchronized(lock)
		{
			IntList emptyAlliances=new IntList();
			for(int i=0;i<autoAlliances.size();i++)
			{
				Alliance alliance=objectFactory.getAlliance(
					autoAlliances.get(i),false);
				if(alliance!=null)
				{
					if(checkAutoTransfer(alliance,time))
						masterTransfer(alliance,time);
				}
				else
					emptyAlliances.add(autoAlliances.get(i));
			}
			for(int i=0;i<emptyAlliances.size();i++)
			{
				autoAlliances.remove(emptyAlliances.get(i));
			}
		}
	}

	/** 会长自动移交 */
	public void masterTransfer(Alliance alliance,int time)
	{
		String name=alliance.getMasterName(objectFactory);
		Player oldMaster=objectFactory.getPlayerByName(name,false);
		// 联盟成员多于1人时进行运算
		if(oldMaster!=null && alliance.getPlayerList().size()>1)
		{
			// 先进行副会长判定
			Player newMaster=getMasterFromArray(alliance.getVicePlayers(),
				alliance.getMasterPlayerId(),time);
			// 如果没有副会长，则进行普通成员判定
			if(newMaster==null)
			{
				newMaster=getMasterFromArray(alliance.getPlayerList(),
					alliance.getMasterPlayerId(),time);
			}
			else
			{
				// 移除副会长
				alliance.removeVicePlayer(newMaster.getId());
			}
			// 保证除会长外其他玩家不为空
			if(newMaster!=null)
			{
				alliance.setMasterPlayerId(newMaster.getId());
				if(oldMaster!=null) alliance.addVicePlayer(oldMaster.getId());
				// 记录新会长的上任基准时间
				newMaster.setAttribute(PublicConst.ALLIANCE_TRANSFER_TIME,
					TimeKit.getSecondTime()+"");
				// 保证改变的玩家正常保存
				objectFactory.getPlayerCache().load(newMaster.getId()+"");
				// 移交完成，通知成员

				// todo 发邮件
				allianceEventAndMail(alliance,name);
				// 保证改变的联盟正常保存
				objectFactory.getAlliance(alliance.getId(),true);
			}
		}
	}

	/** 从列表获取符合条件的玩家,排除当前会长 */
	private Player getMasterFromArray(IntList playerList,int masterId,int time)
	{
		Player newMaster=null;
		for(int i=0;i<playerList.size();i++)
		{
			Player player=objectFactory.getPlayerById(playerList.get(i));
			if(player!=null)
			{
				if(player.getId()==masterId) continue;
				if(!SeaBackKit.isPlayerOnline(player)
					&&player.getUpdateTime()<time-TRANSFER_CIRCLE_TIME)
					continue;
				if(newMaster==null)
				{
					newMaster=player;
					continue;
				}
				if(player.getFightScore()>newMaster.getFightScore())
				{
					newMaster=player;
					continue;
				}
				if(player.getFightScore()==newMaster.getFightScore()
					&&player.getUpdateTime()>newMaster.getUpdateTime())
				{
					newMaster=player;
					continue;
				}
			}
		}
		return newMaster;
	}

	/** 向联盟添加事件及其成员发送消息 */
	private void allianceEventAndMail(Alliance alliance,String oldMaster)
	{
		String newMaster=alliance.getMasterName(objectFactory);
		// 联盟事件
		AllianceEvent event=new AllianceEvent(
			AllianceEvent.ALLIANCE_EVENT_AUTOTRANFER,oldMaster,newMaster,
			Alliance.MILITARY_RANK3+"",TimeKit.getSecondTime());
		alliance.addEvent(event);
		// 会长改变信息
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(AlliancePort.ALLIANCE_PORT);
		data.writeByte(AlliancePort.RIGHT_CHANGE);
		data.writeUTF(newMaster);
		data.writeByte(Alliance.MILITARY_RANK3);
		// 副会长改变信息
		ByteBuffer data1=new ByteBuffer();
		data1.clear();
		data1.writeShort(AlliancePort.ALLIANCE_PORT);
		data1.writeByte(AlliancePort.RIGHT_CHANGE);
		data1.writeUTF(oldMaster);
		data1.writeByte(Alliance.MILITARY_RANK2);
		sendAllAlliancePlayers(data,alliance);
		sendAllAlliancePlayers(data1,alliance);
		String title=null;
		String content=null;
		// 旧会长邮件
		Player player=objectFactory.getPlayerByName(oldMaster,false);
		if(player!=null)
		{
			title=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_last_title");
			content=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_last_content");
			content=TextKit.replace(content,"%",newMaster);
			// 发送邮件
			MessageKit.sendSystemMessages(player,objectFactory,content,title);
		}
		// 新会长邮件
		player=objectFactory.getPlayerByName(newMaster,false);
		if(player!=null)
		{
			title=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_next_title");
			content=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_next_content");
			content=TextKit.replace(content,"%",oldMaster);
			// 发送邮件
			MessageKit.sendSystemMessages(player,objectFactory,content,title);
		}
	}

	/** 联盟广播 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance)
	{
		SessionMap smap=objectFactory.getDsmanager().getSessionMap();
		Session[] sessions=smap.getSessions();
		Player player=null;
		Connect con=null;
		IntList list=alliance.getPlayerList();
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					for(int j=0;j<list.size();j++)
					{
						if(player.getId()==list.get(j))
						{
							con.send(data);
						}
					}
				}
			}
		}
	}
	
	/** 检测联盟会长是否需要自动移交 */
	private boolean checkAutoTransfer(Alliance alliance,int time)
	{
		if(alliance.getPlayerList().size()<=1) return false;
		Player master=objectFactory.getPlayerById(alliance
			.getMasterPlayerId());
		int transferTime=time;
		// 特殊处理，避免会长不存在的情况
		if(master==null)
		{
			// 先进行副会长判定
			master=getMasterFromArray(alliance.getVicePlayers(),
				alliance.getMasterPlayerId(),time);
			// 如果没有副会长，则进行普通成员判定
			if(master==null)
			{
				master=getMasterFromArray(alliance.getPlayerList(),
					alliance.getMasterPlayerId(),time);
			}
			else
			{
				// 移除副会长
				alliance.removeVicePlayer(master.getId());
			}
			if(master!=null)
			{
				alliance.addVicePlayer(alliance.getMasterPlayerId());
				alliance.setMasterPlayerId(master.getId());
			}
		}
		if(master!=null)
		{
			if(SeaBackKit.isPlayerOnline(master))	return false;
			// 首次执行时属性不存在或者移交基准时间后有登录记录，则取会长上次登录时间(之前没有进行移交操作)
			transferTime=master.getUpdateTime();
			String transferTimeStr=master
				.getAttributes(PublicConst.ALLIANCE_TRANSFER_TIME);
			// 自动移交基准时间属性存在,如果移交基准后没有登录记录，则取属性值(之前进行了移交操作)
			if(transferTimeStr!=null&&!transferTimeStr.equals("")
				&&transferTime<=Integer.valueOf(transferTimeStr))
				transferTime=Integer.valueOf(transferTimeStr);
			int allianceNextTime=transferTime+TRANSFER_CIRCLE_TIME;
			if(allianceNextTime<=TimeKit.getSecondTime()) return true;
		}
		return false;
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		autoTransferArray((int)(arg0.getCurrentTime()/1000));
	}
}
