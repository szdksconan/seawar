package foxu.sea;

import java.text.SimpleDateFormat;
import java.util.Date;

import mustang.field.Fields;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.MathKit;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.NpcIslandGameDBAccess;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.dcaccess.datasave.NpcIsLandSave;
import foxu.sea.event.FightEvent;
import foxu.sea.kit.FightKit;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.port.RankPort.RankInfo;

/***
 * 宝石岛屿管理器
 * 
 * @author lhj
 *
 */
public class GemsNpcIslandManager implements TimerListener
{

	Logger log=LogFactory.getLogger(GemsNpcIslandManager.class);

	CreatObjectFactory factory;

	public static final int time=5000*3;
	/** 岛屿加载器 **/
	NpcIslandGameDBAccess islanddBAccess;
	/** 宝石岛屿内存设置 **/
	IntKeyHashMap cahceMap=new IntKeyHashMap();
	/** 宝石岛屿的最大长度 **/
	public static int ISLAND_SIZE=100;
	/** 宝石岛屿当前的长度 **/
	public static int CURRENTLENGTH;
	/** 当天的时间记录器 **/
	public static int CURRENTTIME;
	/** 宝石岛屿的等级 **/
	public static int LEVEL;
	/** rank 前100 **/
	public static int RANK_HUNRANDS=100;

	/** 初始化 **/
	public void init()
	{
		String sql="SELECT * from npc_islands where endTime<>0";
		NpcIsland island[]=(NpcIsland[])islanddBAccess.loadBySql(sql);
		if(island!=null)
		{
			for(int i=0,n=island.length;i<n;i++)
			{
				NpcIsLandSave isLandSave=new NpcIsLandSave();
				isLandSave.setData(island[i]);
				cahceMap.put(island[i].getIndex(),isLandSave);
			}
		}
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"gemsisland",time));
	}

	@Override
	public void onTimer(TimerEvent event)
	{
		check();
	}

	/** 检查是否需要更新 **/
	public void check()
	{
		/** 是否更新数据 **/
		int timeNow=TimeKit.getSecondTime();
		if(!SeaBackKit.isSameDay(CURRENTTIME,timeNow))
		{
			/* 根据昨天的在线玩家设置今天的岛屿个数 */
			Date as=new Date(new Date().getTime()-24*60*60*1000);
			SimpleDateFormat matter1=new SimpleDateFormat("yyyy-MM-dd");
			String[] time=TextKit.split(matter1.format(as),"-");
			int year=TextKit.parseInt(time[0]);
			int mouth=TextKit.parseInt(time[1]);
			int day=TextKit.parseInt(time[2]);
			factory.getGameDataCache().saveAndExit();
			int count=factory.getgDataByDidCache().getYesterdayMaxOnline(year,mouth,day);
			boolean flag=false;
			if(count==0) flag=factory.getgDataByDidCache().getCount();
			if(count==0 && flag) 
			{
				CURRENTLENGTH=PublicConst.ORIGINAL_NUM;
				LEVEL=0;
				CURRENTTIME=timeNow;
				return;
			}
			CURRENTLENGTH=(count/PublicConst.CONTROL_ONLINE)+PublicConst.ORIGINAL_NUM;	
			/** 设置岛屿类型 **/
			setLevelType();
			CURRENTTIME=timeNow;
		}
		checkGemIsland();
		addGemsIsland();
	}

	/** 检测岛屿 **/
	public void checkGemIsland()
	{
		if(cahceMap.size()>0)
		{
			Object[] objects=cahceMap.valueArray();
			for(int i=0;i<objects.length;i++)
			{
				if(objects[i]==null) continue;
				NpcIsLandSave save=(NpcIsLandSave)objects[i];
				if(save==null) continue;
				NpcIsland island=save.getData();
				if(island==null) continue;
				if(island.checkDismiss()) removeGemsIsland(island.getIndex());
			}
		}
	}

	/** 移除岛屿 **/
	public void removeGemsIsland(int index)
	{
		NpcIsland island=factory.getIslandByIndex(index+"");
		if(island==null)
		{
			cahceMap.remove(index);
			return ;
		}
		int sx=island.getIndex()%600+1;
		int sy=island.getIndex()/600+1;
		log.error("----------gemsisland-------remove----------sx="+sx+"----sy="+sy);
		if(island.getTempAttackEventId()!=0)
		{
			/** 回收岛屿事件 **/
			FightEvent event=(FightEvent)factory.getEventCache()
				.loadOnly(island.getTempAttackEventId()+"");
			if(event!=null)
			{
				Player player=factory.getPlayerCache().load(event.getPlayerId()+"");
				if(event.getAttackIslandIndex()==island.getIndex() && player!=null)
				{
					SeaBackKit.shipReturnBack(event,player);
					sendMessage(sx,sy,event.getResources()[Resources.GEMS]
						/PublicConst.LOWLIMIT_GEMS_TIMES,player,event);
				}
			}
			else
			{
				// 加入改变列表
				factory.getIslandByIndex(island.getIndex()+"");
				island.setTempAttackEventId(0);
			}
		}
		/** 设置成海水 **/
		island.updateSid(FightKit.WATER_ISLAND_SID);
		island.setEndTime(0);
		island.removeGemsBuff();
		// 刷新前台
		JBackKit.flushIsland(factory.getDsmanager(),island,factory);
		/** 当前内存中清楚 **/
		cahceMap.remove(island.getIndex());
		log.error("----------remove----------"+island.getIndex());
	}

	/** 设置岛屿类型 **/
	public void setLevelType()
	{
		ArrayList playerIdsFight=new ArrayList();
		String sql="SELECT player_name,level,fightScore FROM players where deleteTime=0 ORDER BY fightScore DESC LIMIT "
			+RANK_HUNRANDS;
		Fields[] fields=((PlayerGameDBAccess)factory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		if(fields==null) return;
		playerIdsFight.clear();
		for(int i=0;i<fields.length;i++)
		{
			RankInfo rank=new RankInfo(fields[i].getArray()[0].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[1]
				.getValue().toString()),Integer.parseInt(fields[i]
				.getArray()[2].getValue().toString()));
			playerIdsFight.add(rank);
		}
		
		int totleLevel=0;
		if(playerIdsFight!=null && playerIdsFight.size()!=0)
		{
			for(int j=0;j<playerIdsFight.size();j++)
			{
				if(playerIdsFight.get(j)==null) continue;
				RankInfo rank=(RankInfo)playerIdsFight.get(j);
				if(rank==null) continue;
				totleLevel+=rank.getPlayerLevel();
			}
		}
		System.out.println(totleLevel+"---totleLevel--------");
		totleLevel=totleLevel/100;
		/** 通过配置加载 **/
		for(int i=0;i<PublicConst.GEMISLAND_LEVEL_TYPE.length;i+=2)
		{
			int level=PublicConst.GEMISLAND_LEVEL_TYPE[i];
			if(level<totleLevel)
				LEVEL=PublicConst.GEMISLAND_LEVEL_TYPE[i+1];
		}
		if(LEVEL<=0) LEVEL=1;
	}

	/** 新增岛屿 **/
	public void addGemsIsland()
	{
		if(!PublicConst.GEMS_ISLAND_CLOSE) return ;
		if(CURRENTLENGTH==0||cahceMap.size()>=ISLAND_SIZE
			||cahceMap.size()>=CURRENTLENGTH) return;
		NpcIsland island=factory.getIslandCache().getRandomSpace();
		if(island==null)
		{
			log.error("show gems island fail,world is full");
			return;
		}
		int index=island.getIndex();
		int id=island.getId();
		island=(NpcIsland)NpcIsland.factory
			.newSample(getGemsIslandSid(LEVEL));
		island.setPlayerId(0);
		island.setTempAttackEventId(0);
		island.setId(id);
		island.setIndex(index);
		// 设置剩余时间
		island.setEndTime(MathKit.randomValue(
			PublicConst.GEMS_ISLAND_LIMIT_TIME[0],
			PublicConst.GEMS_ISLAND_LIMIT_TIME[1])
			*60*60+TimeKit.getSecondTime());
		island.createFleetGroup();
		NpcIsLandSave save=new NpcIsLandSave();
		save.setData(island);
		factory.getIslandCache().getCacheMap().put(index,save);
		factory.getIslandByIndex(index+"");
		cahceMap.put(island.getIndex(),save);
		
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"gems_island_show");
		message=TextKit.replace(message,"%",island.getIslandLevel()+"");
		int sx=index%600+1;
		int sy=index/600+1;
		message=TextKit.replace(message,"%",getGemsDirection(sx,sy));
		// 系统公告
		SeaBackKit.sendSystemMsg(factory.getDsmanager(),message);
		
		log.error("----------gemsisland-------created----------index="
			+island.getIndex());
		log.error("----------gemsisland-------created----------sx="+sx
			+"----sy="+sy);
	}

	/** 获取宝石岛屿的sid **/
	public int getGemsIslandSid(int level)
	{
		if(LEVEL<=1 || LEVEL<=2) return PublicConst.GEMISLAND_SIDS[0];
		return PublicConst.GEMISLAND_SIDS[(level/2)-1];
	}

	public void setIslanddBAccess(NpcIslandGameDBAccess islanddBAccess)
	{
		this.islanddBAccess=islanddBAccess;
	}

	public IntKeyHashMap getCahceMap()
	{
		return cahceMap;
	}

	public void setCahceMap(IntKeyHashMap cahceMap)
	{
		this.cahceMap=cahceMap;
	}

	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	/**船只返航有系统邮件**/
	public void sendMessage(int sx,int sy,int gems,Player player,FightEvent event)
	{
		if(event.getPlayerId()!=player.getId()) return ;
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"gems_returnback_title");
		String content=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"gems_returnback_content");
		content=TextKit.replace(content,"%",sx+"");
		content=TextKit.replace(content,"%",sy+"");
		content=TextKit.replace(content,"%",gems+"");
		Message message=factory.createMessage(0,player.getId(),
			content,sendName,player.getName(),Message.SYSTEM_ONE_TYPE,title,
			true,null);
		// 刷新前台
		JBackKit.sendRevicePlayerMessage(player,message,
			message.getRecive_state(),factory);
	}
	
	public String getGemsDirection(int sx,int sy)
	{
		String dir=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"boss_taunt_direction");
		String[] dirs=TextKit.split(dir,",");
		int[] dirs_int={5,0,6,2,4,3,7,1,8};
		int index=0;
		boolean get=false;
		for(int y=0;y<=2;y++)
		{
			if(sy<=(y+1)*200)
			{
				for(int x=0;x<=2;x++)
				{
					if(sx<=(x+1)*200)
					{
						index=y*3+x;
						get=true;
						break;
					}
				}
				if(get==true)break;
			}
		}
		return dirs[dirs_int[index]];
		
	}


}
