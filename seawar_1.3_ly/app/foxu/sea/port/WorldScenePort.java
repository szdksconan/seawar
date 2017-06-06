package foxu.sea.port;

import java.awt.Point;

import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.event.FightEvent;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.kit.TwoDKit;
import foxu.sea.worldboss.WorldBoss;
import mustang.codec.MD5;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.text.TextKit;
import mustang.util.TimeKit;

/** 世界场景 WorldScenePort 1014 */
public class WorldScenePort extends AccessPort
{

	/** 日志记录 */
	private static final Logger log=LogFactory
		.getLogger(WorldScenePort.class);
	MD5 md5=new MD5();
	/** 行军线最大数量 */
	int max=50;
	/** 行军线开关 */
	public static boolean march=true;
	/** 篡改验证 私钥 */
	String distortKey="nkyntCuBgsNIEGujwH2fHruYUx3YrnXqk0HS1GQbJJqltMlBMggWHymfiurt6kud";


	CreatObjectFactory objectFactory;

	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		/** 起始index */
		int index=data.readInt();
		if(index<0||index>360000)
		{
			throw new DataAccessException(0,"index is wrong");
		}
		/** 向右要的长度 */
		int leftSize=data.readUnsignedByte();
		/** 向下要的长度 */
		int downSize=data.readUnsignedByte();
		if(leftSize>14||downSize>14)
		{
			throw new DataAccessException(0,"size is wrong");
		}
//		try
//		{
			int num=data.readInt();
			String md=data.readUTF();
			StringBuffer sub=new StringBuffer();
			sub.append(index);
			sub.append(leftSize);
			sub.append(downSize);
			sub.append(num);
			sub.append(distortKey);
			String sign_md5=md5.encode(sub.toString());
			if(!md.equalsIgnoreCase(sign_md5))
			{
				throw new DataAccessException(0,"error");
			}
//		}
//		catch(Exception e)
//		{
//			
//		}
		
				
		data.clear();
		int loadIndex=index;
		int length=0;
		/** 先算长度 */
		for(int i=0;i<downSize;i++)
		{
			for(int j=0;j<leftSize;j++)
			{
				loadIndex=600*i+index+j;
				NpcIsland land=objectFactory.getIslandCache().loadOnly(
					loadIndex+"");
				if(land==null) continue;
				length++;
			}
		}
		if(length>196)
			throw new DataAccessException(0,"length is wrong:"+length);
		data.writeByte(length);
		for(int i=0;i<downSize;i++)
		{
			for(int j=0;j<leftSize;j++)
			{
				loadIndex=600*i+index+j;
				NpcIsland land=objectFactory.getIslandCache().loadOnly(
					loadIndex+"");
				String playerName=null;
				int level=0;
				if(land==null) continue;
				int state=0;
				int playerSid=1;
				boolean bool=false;
				boolean hostile=false;
				int [] flag=null;
				if(land.getPlayerId()!=0)
				{
					Player checkPlayer=objectFactory.getPlayerCache()
						.loadPlayerOnly(land.getPlayerId()+"");
					if(checkPlayer==null)
					{
						log.error("error:playerid=0 worldscenePort======"
							+land.getPlayerId()+"   ,index="+land.getIndex()
							+",type="+land.getIslandType()+",level="
							+land.getIslandLevel()+",sid="+land.getSid());
						log.error("error:playerid=0 worldscenePort======"
							+land.getPlayerId()+"   ,index="+land.getIndex()
							+",type="+land.getIslandType()+",level="
							+land.getIslandLevel()+",sid="+land.getSid());
						land.setPlayerId(0);
						objectFactory.getIslandCache().load(loadIndex+"");
					}
					// 如果当前玩家还没完成新手改名流程则不显示岛屿
//					else if(!SeaBackKit.isNewPlayerChangeName(
//						checkPlayer.getName(),objectFactory))
//					{
//						land=(NpcIsland)NpcIsland.factory
//							.getSample(NpcIsland.ISLAND_WARTER);
//					}
					else
					{
						playerName=checkPlayer.getName();
						level=checkPlayer.getLevel();
						playerSid=checkPlayer.getId();
						if(checkPlayer.checkService(PublicConst.NOT_FIGHT_BUFF,TimeKit.getSecondTime())!=null)
							state=PublicConst.NOT_FIGHT_STATE;
						// 是否是同盟
						if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
							&&checkPlayer.getAttributes(PublicConst.ALLIANCE_ID)!=null)
						{
							String paid=player.getAttributes(PublicConst.ALLIANCE_ID);
							String checkaid=checkPlayer.getAttributes(PublicConst.ALLIANCE_ID);
							if(paid.length()>0 && checkaid.length()>0 && checkaid.equals(paid))
								bool=true;
							Alliance palliance=objectFactory.getAlliance(
								TextKit.parseInt(paid),false);
							Alliance calliance=objectFactory.getAlliance(
								TextKit.parseInt(checkaid),false);
							if(SeaBackKit.isHostile(palliance,calliance))
								hostile=true;
						}
						if(checkPlayer!=null)
						{
							String checkaid=checkPlayer.getAttributes(PublicConst.ALLIANCE_ID);
							if(checkaid!=null)
							{
								Alliance calliance=objectFactory.getAlliance(
									TextKit.parseInt(checkaid),false);
								if(calliance!=null)
									flag=calliance.getFlag().getAllianceFlag();
							}
						}
					}
				}
				// 查看玩家是否有保护
				// boss是否处于保护
				boolean bossBool=false;
				if(land.getIslandType()==NpcIsland.WORLD_BOSS)
				{
					WorldBoss boss=objectFactory.getWorldBossBySid(land
						.getSid());
					if(boss!=null
						&&boss.getProtectTime()>TimeKit.getSecondTime())
					{
						bossBool=true;
					}
				}
				land.showBytesWrite(data,playerName,level,state,playerSid,
					bool,bossBool,hostile,flag);
			}
		}
		//行军线
		marchLine(player,data,index,leftSize,downSize);
		return data;
	}
	/** 写行军线 */
	public int writeMarchLine(Player player,ByteBuffer data,ArrayList list,int count)
	{
		if(list!=null)
		{
			Alliance al=null;
			String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(aid!=null&&!aid.equals(""))
			{
				al=objectFactory.getAlliance(TextKit.parseInt(aid),false);
			}
			for(int i=0;i<list.size();i++)
			{
				FightEvent event=(FightEvent)list.get(i);
				if(event==null||event.getEventState()==FightEvent.HOLD_ON)
					continue;
				if(event.getNeedTime()<=(TimeKit.getSecondTime()-event
					.getCreatAt())) continue;
				// if(!checkEvent(tindex,left,down,event)) continue;
				data.writeInt(event.getId());
				if(event.getEventState()!=FightEvent.RETRUN_BACK)
				{
					data.writeInt(event.getSourceIslandIndex());
					data.writeInt(event.getAttackIslandIndex());
				}
				else
				{
					data.writeInt(event.getAttackIslandIndex());
					data.writeInt(event.getSourceIslandIndex());
				}
				data.writeInt(event.getNeedTime()+event.getCreatAt()
					-TimeKit.getSecondTime());
				data.writeInt(event.getNeedTime());
				int stype=2;
				if(player.getId()==event.getPlayerId()
					||(al!=null
						&&al.getPlayerList().contain(event.getPlayerId())&&event
						.getType()==FightEvent.ATTACK_HOLD))
				{
					stype=1;
				}
				data.writeByte(stype);// 1 己方 2敌方
				count++;
			}
		}
		return count;
	}
	/** 行军线 */
	public void marchLine(Player player,ByteBuffer data,int tindex,int left,int down)
	{
		int index=objectFactory.getIslandCache().getPlayerIsLandId(
			player.getId());
		ArrayList list=objectFactory.getEventCache().getFightEventListById(
			index);
		int count=0;
		int top=data.top();
		data.writeInt(count);
		if(!march) return;
		if(list==null)return;
		count=writeMarchLine(player,data,list,count);
		for(int i=0;i<list.size();i++)
		{
			FightEvent event=(FightEvent)list.get(i);
			if(event==null||event.getEventState()!=FightEvent.HOLD_ON)
				continue;
			NpcIsland island=objectFactory.getIslandByIndex(event
				.getAttackIslandIndex()+"");
			if(island.getPlayerId()!=0) continue;
			ArrayList list0=objectFactory.getEventCache()
				.getFightEventListById(event.getAttackIslandIndex());
			count=writeMarchLine(player,data,list0,count);
		}
		// if(count<max)//暂时不开放
		// {
		// Object[] objs=objectFactory.getEventCache().getCacheMap()
		// .valueArray();
		// for(int i=0;i<objs.length;i++)
		// {
		// if(objs[i]==null) continue;
		// FightEventSave save=(FightEventSave)objs[i];
		// FightEvent ev=save.getData();
		// if(ev==null||ev.getEventState()==FightEvent.HOLD_ON)
		// continue;
		// if(ev.getNeedTime()<=(TimeKit.getSecondTime()-ev
		// .getCreatAt())) continue;
		// if(ev.getSourceIslandIndex()==index
		// ||ev.getAttackIslandIndex()==index) continue;
		// if(!checkEvent(tindex,left,down,ev)) continue;
		// if(ev.getEventState()!=FightEvent.RETRUN_BACK)
		// {
		// data.writeInt(ev.getSourceIslandIndex());
		// data.writeInt(ev.getAttackIslandIndex());
		// }
		// else
		// {
		// data.writeInt(ev.getAttackIslandIndex());
		// data.writeInt(ev.getSourceIslandIndex());
		// }
		// data.writeInt(ev.getNeedTime());
		// data.writeInt(TimeKit.getSecondTime()-ev.getCreatAt());
		// data.writeByte(3);// 1 己方 2敌方 3中立
		// count++;
		// if(count>=max) break;
		// }
		//
		// }
		if(count>0)
		{
			int nowtop=data.top();
			data.setTop(top);
			data.writeInt(count);
			data.setTop(nowtop);
		}

		// System.out.println("--------1111--------:"+count);
	}
	//碰撞检测
	public boolean checkEvent(int index,int left,int down,FightEvent ev)
	{
		// 矩形线段
		int x1=index%600;
		int y1=index/600;
		int x2=x1+left;
		int y2=y1;
		int x3=x2;
		int y3=y1+down;
		int x4=x1;
		int y4=y1+down;

		// 事件线段
		int ex1=ev.getSourceIslandIndex()%600;
		int ey1=ev.getSourceIslandIndex()/600;
		int ex2=ev.getAttackIslandIndex()%600;
		int ey2=ev.getAttackIslandIndex()/600;

		//
		Point p1=new Point(x1,y1);
		Point p2=new Point(x2,y2);
		Point p3=new Point(x3,y3);
		Point p4=new Point(x4,y4);

		Point ep1=new Point(ex1,ey1);
		Point ep2=new Point(ex2,ey2);

		return TwoDKit.intersect(p1,p2,ep1,ep2)
			||TwoDKit.intersect(p2,p3,ep1,ep2)
			||TwoDKit.intersect(p3,p4,ep1,ep2)
			||TwoDKit.intersect(p4,p1,ep1,ep2);
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
	
	public int getMax()
	{
		return max;
	}
	
	public void setMax(int max)
	{
		this.max=max;
	}
	
}
