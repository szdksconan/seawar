package foxu.sea.alliance.alliancefight;

import shelby.ds.DSManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.ForeverService;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Service;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.AllianceEvent;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.AlliancePort;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;

/**
 * 据点
 * 
 * @author yw
 * 
 */
public class BattleGround extends Sample
{

	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	/** 联盟id */
	int id;
	/** 驻防舰队 位置 sid num */
	IntList fleet=new IntList();
	/** 占领时刻 */
	int captureTime;
	/** 上次奖励时间 */
	int lastTime;

	/** 显示SID */
	int showSid;
	/** 升级点奖励（每小时） */
	int upPoint;
	/** 基础产量增益（service 的SID ） */
	int[] sids;

	/** 清理占领数据 */
	public synchronized void reset()
	{
		id=0;
		captureTime=TimeKit.getSecondTime();
		lastTime=captureTime;
		fleet.clear();
	}
	/** 更新占领数据 */
	public synchronized void updateHoldData(int allianceId,IntList fleet)
	{
		id=allianceId;
		this.fleet=fleet;
		captureTime=TimeKit.getSecondTime();
		lastTime=captureTime;
	}
	public int getShipCountBySid(int sid)
	{
		for(int i=0;i<fleet.size();i+=3)
		{
			if(fleet.get(i+1)==sid)return fleet.get(i+2);
		}
		return 0;
	}
	/** 取消BUFF */
	public void cancelBuff(Alliance alliance,CreatObjectFactory objFactory)
	{
		if(sids==null||sids.length<=0) return;
		IntList plist=alliance.getPlayerList();
		Player player=null;
		for(int i=0;i<plist.size();i++)
		{
			player=objFactory.getPlayerById(plist.get(i));
			for(int k=0;k<sids.length;k++)
			{
				Service service=player.getServiceBySid(sids[k]);
				if(service!=null)
					service.setEndTime(TimeKit.getSecondTime());
				else
				{
					System.out
						.println("-------service------errot-----!!!!!----");
				}
			}
			JBackKit.sendResetService(player);
		}
	}
	/** 奖励BUFF */
	public void awardBuff(Alliance alliance,CreatObjectFactory objFactory)
	{
		if(sids==null||sids.length<=0) return;
		IntList plist=alliance.getPlayerList();
		Player player=null;
		ForeverService service=null;
		for(int i=0;i<plist.size();i++)
		{
			player=objFactory.getPlayerById(plist.get(i));
			for(int k=0;k<sids.length;k++)
			{
				service=(ForeverService)Service.factory.newSample(sids[k]);
				service.setEndTime(-1);
				player.addForeverService(service,TimeKit.getSecondTime());
			}
			objFactory.getPlayerCache().save(player.getId()+"",player);
			// 是否要刷给前台？
			JBackKit.sendResetService(player);
		}

	}
	/** 取消个人BUFF */
	public void cancelPersonBuff(Player player)
	{
		if(sids==null||sids.length<=0) return;
		for(int k=0;k<sids.length;k++)
		{
			Service service=player.getServiceBySid(sids[k]);
			if(service!=null)
				service.setEndTime(TimeKit.getSecondTime());
			else
			{
				System.out
					.println("----Person---service------errot-----!!!!!----");
			}
		}
		JBackKit.sendResetService(player);

	}
	/** 奖励个人BUFF */
	public void awardPersonBuff(Player player)
	{
		if(sids==null||sids.length<=0) return;
		ForeverService service=null;
		for(int k=0;k<sids.length;k++)
		{
			service=(ForeverService)Service.factory.newSample(sids[k]);
			service.setEndTime(-1);
			player.addForeverService(service,TimeKit.getSecondTime());
		}
		JBackKit.sendResetService(player);
	}
	/** 奖励升级点 */
	public int[] awardUpPoint(AllianceFightManager amanager,CreatObjectFactory objectFactory)
	{
		if(id==0) return null;
		if(upPoint<=0) return null;
		int nowTime=TimeKit.getSecondTime();
		int[] sids=null;
		if(nowTime-lastTime>=AllianceFight.AWARD_INERVAL)
		{
			ByteBuffer data=new ByteBuffer();
			Alliance alliance=amanager.getAlliance(id);
			int len=(nowTime-lastTime)/AllianceFight.AWARD_INERVAL;
			lastTime=lastTime+len*AllianceFight.AWARD_INERVAL;
			sids=new int[len];
			int skillsid=0;
			int i=0;
			for(;i<len;i++)
			{
				skillsid=alliance.getRandomSkill();
				if(skillsid!=0)
				{
					int addLevel=alliance.incrSkillExp(upPoint,skillsid);
					if(addLevel>0)
					{
						int level[]=alliance.getSkillExp(skillsid);
						alliance.flushAllianceSkill(objectFactory);
						// 联盟事件
						AllianceEvent event=new AllianceEvent(
							AllianceEvent.ALLIANCE_EVENT_SKILL_LEVEL,
							skillsid+"",skillsid+"",level[1]+"",TimeKit
								.getSecondTime());
						alliance.addEvent(event);
						// 成员战力计算
						IntList plist=alliance.getPlayerList();
						if(plist!=null)
						{
							Player player;
							for(int l=0;l<plist.size();l++)
							{
								player=objectFactory.getPlayerById(plist
									.get(l));
								JBackKit.sendFightScore(player,
									objectFactory,true,FightScoreConst.BATTLE_GROUP_SKILL_POINT);
							}
						}
					}
					// 广播
					data.clear();
					data.writeShort(AlliancePort.ALLIANCE_PORT);
					data.writeByte(AlliancePort.SINGLE_SKILL_CHANGE);
					data.writeShort(skillsid);
					int exp[]=alliance.getSkillExp(skillsid);
					data.writeInt(exp[0]);
					data.writeByte(exp[1]);
					sendAllAlliancePlayers(data,alliance,objectFactory.getDsmanager());
					
					sids[i]=skillsid;
				}
				else
				{
					break;
				}
			}
			for(;i<len;i++)
			{
				if(alliance.getAllianceLevel()<PublicConst.MAX_ALLIANCE_LEVEL)
				{
					int addLevel=alliance.incrExp(upPoint);
					if(addLevel>0)
					{
						// 联盟事件
						AllianceEvent event=new AllianceEvent(
							AllianceEvent.ALLIANCE_EVENT_LEVEL,"","",alliance
								.getAllianceLevel()
								+"",TimeKit.getSecondTime());
						alliance.addEvent(event);
						// 提示
						String message=InterTransltor.getInstance()
							.getTransByKey(PublicConst.SERVER_LOCALE,
								"alliance_level_up");
						message=TextKit.replace(message,"%",alliance.getName());
						message=TextKit.replace(message,"%",alliance
							.getAllianceLevel()
							+"");
						SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
							message);
					}
					// 广播
					data.clear();
					data.writeShort(AlliancePort.ALLIANCE_PORT);
					data.writeByte(AlliancePort.EXP_LEVEL_CHANGE);
					data.writeInt(alliance.getAllianceExp());
					data.writeByte(alliance.getAllianceLevel());
					sendAllAlliancePlayers(data,alliance,objectFactory.getDsmanager());
					
					sids[i]=-1;
				}
			}
			
		}
		return sids;
	}
	
	/** 联盟广播 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance,DSManager dsmanager)
	{
		SessionMap smap=dsmanager.getSessionMap();
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
	
	/** 补充舰船 */
	public synchronized void recruit(CreatObjectFactory objFactory,
		boolean force)
	{
		AllianceFight afight=(AllianceFight)objFactory
			.getAllianceFightMemCache().load(id+"");
		if(!force&&!afight.isReinforce()) return;
		Alliance alliance=(Alliance)objFactory.getAllianceMemCache().load(
			id+"");
		int max=alliance.getShipMax();
		IntList list=new IntList();// 可补充量-实际补充量
		for(int i=0;i<fleet.size();i+=3)
		{
			list.add(fleet.get(i+1));
			list.add(max-fleet.get(i+2)<0?0:max-fleet.get(i+2));
		}
		afight.recruitShip(list,this);
		for(int i=0,k=1;i<fleet.size();i+=3,k+=2)
		{
			fleet.set(list.get(k)+fleet.get(i+2),i+2);
		}
	}
	
	/** 获取驻防舰队数量 */
	public IntList getAllFleets()
	{
		if(fleet.size()<3) return null;
		IntList decr=new IntList();
		for(int i=0;i<fleet.size();i+=3)
		{
			int num=fleet.get(i+2);
			if(num<=0) continue;
			int sid=fleet.get(i+1);
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
		return decr.size()>0?decr:null;
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		id=data.readInt();
		captureTime=data.readInt();
		lastTime=data.readInt();
		bytesReadFleet(data);
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(captureTime);
		data.writeInt(lastTime);
		bytesWriteFleet(data);
	}
	public void bytesReadFleet(ByteBuffer data)
	{
		fleet.clear();
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			fleet.add(data.readUnsignedByte());
			fleet.add(data.readUnsignedShort());
			fleet.add(data.readUnsignedShort());
		}
	}

	public void bytesWriteFleet(ByteBuffer data)
	{
		int len=fleet.size()/3;
		data.writeByte(len);
		for(int i=0;i<len;i++)
		{
			data.writeByte(fleet.get(3*i));
			data.writeShort(fleet.get(3*i+1));
			data.writeShort(fleet.get(3*i+2));
		}
	}
	public void showBytesWrite(AllianceFightManager manager,ByteBuffer data)
	{
		data.writeShort(showSid);
		if(id==0)
		{
			data.writeUTF("");
		}
		else
		{
			data.writeUTF(manager.getAlliance(id).getName());
		}
	}
	public void showBytesWriteFleet(ByteBuffer data)
	{
		int len=fleet.size()/3;
		data.writeByte(len);
		for(int i=0;i<len;i++)
		{
			data.writeShort(fleet.get(3*i+1));
			data.writeShort(fleet.get(3*i+2));
			data.writeByte(fleet.get(3*i));
		}
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}

	public int getUpPoint()
	{
		return upPoint;
	}

	public void setUpPoint(int upLevelPoint)
	{
		this.upPoint=upLevelPoint;
	}

	public int[] getSids()
	{
		return sids;
	}

	public void setSids(int[] sids)
	{
		this.sids=sids;
	}

	public IntList getFleet()
	{
		return fleet;
	}

	public void setFleet(IntList fleet)
	{
		this.fleet=fleet;
	}

	public int getCaptureTime()
	{
		return captureTime;
	}

	public void setCaptureTime(int captureTime)
	{
		this.captureTime=captureTime;
	}

	public int getShowSid()
	{
		return showSid;
	}

	public void setShowSid(int showSid)
	{
		this.showSid=showSid;
	}

	public int getLastTime()
	{
		return lastTime;
	}

	public void setLastTime(int lastTime)
	{
		this.lastTime=lastTime;
	}

}
