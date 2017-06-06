package jedis.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.thread.ThreadPoolExecutor;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;
import shelby.dc.GameDBAccess;
import shelby.dc.GameDataHandleImp;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.dcaccess.datasave.NpcIsLandSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.dcaccess.mem.MessageMemCache;
import foxu.dcaccess.mem.PlayerMemCache;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.builds.BuildManager;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.Fleet;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.BuildPort;

public class PlayerDbTEST implements TimerListener
{

	// String
	// str="11001,115,11002,110,11003,105,11004,100,11005,95,11006,90,11007,85,11008,80,11009,75,11010,70,11011,50,11012,48,11013,46,11014,44,11015,42,11016,40,11017,38,11018,36,11019,34,11020,32,11021,30,11022,29,11023,28,11024,27,11025,26,11026,25,11027,24,11028,23,11029,22,11030,21,11031,20,11032,19,11033,18,11034,17,11035,16,11036,15,11037,14,11038,13,11039,12,11040,11,11041,10,11042,9,11043,8,11044,7,11045,6,11046,5,11047,4,11048,3,11049,2,11050,1,11101,115,11102,110,11103,105,11104,100,11105,95,11106,90,11107,85,11108,80,11109,75,11110,70,11111,50,11112,48,11113,46,11114,44,11115,42,11116,40,11117,38,11118,36,11119,34,11120,32,11121,30,11122,29,11123,28,11124,27,11125,26,11126,25,11127,24,11128,23,11129,22,11130,21,11131,20,11132,19,11133,18,11134,17,11135,16,11136,15,11137,14,11138,13,11139,12,11140,11,11141,10,11142,9,11143,8,11144,7,11145,6,11146,5,11147,4,11148,3,11149,2,11150,1,11201,115,11202,110,11203,105,11204,100,11205,95,11206,90,11207,85,11208,80,11209,75,11210,70,11211,50,11212,48,11213,46,11214,44,11215,42,11216,40,11217,38,11218,36,11219,34,11220,32,11221,30,11222,29,11223,28,11224,27,11225,26,11226,25,11227,24,11228,23,11229,22,11230,21,11231,20,11232,19,11233,18,11234,17,11235,16,11236,15,11237,14,11238,13,11239,12,11240,11,11241,10,11242,9,11243,8,11244,7,11245,6,11246,5,11247,4,11248,3,11249,2,11250,1,11301,115,11302,110,11303,105,11304,100,11305,95,11306,90,11307,85,11308,80,11309,75,11310,70,11311,50,11312,48,11313,46,11314,44,11315,42,11316,40,11317,38,11318,36,11319,34,11320,32,11321,30,11322,29,11323,28,11324,27,11325,26,11326,25,11327,24,11328,23,11329,22,11330,21,11331,20,11332,19,11333,18,11334,17,11335,16,11336,15,11337,14,11338,13,11339,12,11340,11,11341,10,11342,9,11343,8,11344,7,11345,6,11346,5,11347,4,11348,3,11349,2,11350,1,11401,115,11402,110,11403,105,11404,100,11405,95,11406,90,11407,85,11408,80,11409,75,11410,70,11411,50,11412,48,11413,46,11414,44,11415,42,11416,40,11417,38,11418,36,11419,34,11420,32,11421,30,11422,29,11423,28,11424,27,11425,26,11426,25,11427,24,11428,23,11429,22,11430,21,11431,20,11432,19,11433,18,11434,17,11435,16,11436,15,11437,14,11438,13,11439,12,11440,11,11441,10,11442,9,11443,8,11444,7,11445,6,11446,5,11447,4,11448,3,11449,2,11450,1,11502,800,11503,150,11504,40,11505,10";

	String str="11002,50,11004,48,11006,46,11008,44,11010,42,11012,40,11014,38,11016,36,11018,34,11020,32,11022,30,11024,28,11026,26,11028,24,11030,22,11032,20,11034,18,11036,16,11038,14,11040,12,11042,10,11044,8,11046,6,11048,4,11050,2,11102,50,11104,48,11106,46,11108,44,11110,42,11112,40,11114,38,11116,36,11118,34,11120,32,11122,30,11124,28,11126,26,11128,24,11130,22,11132,20,11134,18,11136,16,11138,14,11140,12,11142,10,11144,8,11146,6,11148,4,11150,2,11202,50,11204,48,11206,46,11208,44,11210,42,11212,40,11214,38,11216,36,11218,34,11220,32,11222,30,11224,28,11226,26,11228,24,11230,22,11232,20,11234,18,11236,16,11238,14,11240,12,11242,10,11244,8,11246,6,11248,4,11250,2,11302,50,11304,48,11306,46,11308,44,11310,42,11312,40,11314,38,11316,36,11318,34,11320,32,11322,30,11324,28,11326,26,11328,24,11330,22,11332,20,11334,18,11336,16,11338,14,11340,12,11342,10,11344,8,11346,6,11348,4,11350,2,11402,50,11404,48,11406,46,11408,44,11410,42,11412,40,11414,38,11416,36,11418,34,11420,32,11422,30,11424,28,11426,26,11428,24,11430,22,11432,20,11434,18,11436,16,11438,14,11440,12,11442,10,11444,8,11446,6,11448,4,11450,2,11501,6750";
	GameDataHandleImp gameDataHandleImp;

	GameDBAccess ac;

	BuildManager buildmanager=new BuildManager();

	Player player;

	int a=0;

	BuildPort buildPort=new BuildPort();

	PlayerMemCache cache;

	MessageMemCache messageCache;

	CreatObjectFactory objectFactory;

	/***/
	ThreadPoolExecutor excutor;

	public void gameCenter()
	{
		excutor=new ThreadPoolExecutor();
		excutor.setLimit(5000);
		excutor.init();

		AppPushRunabel app=new AppPushRunabel();
		for(int i=0;i<10000;i++)
			excutor.execute(app);
	}

	public class AppPushRunabel implements Runnable
	{

		public void run()
		{
			// TODO 自动生成方法存根
			ByteBuffer data=new ByteBuffer();
			data.writeByte(1);
			HttpRequester request=new HttpRequester();
			request.setDefaultContentEncoding("UTF-8");
			HashMap<String,String> map=new HashMap<String,String>();
			map.put("type","2");
			// 设置port
			map.put("port","2");
			HttpRespons re=null;
			try
			{
				re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP
					+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,null);
			}
			catch(IOException e)
			{
				// TODO 自动生成 catch 块
				e.printStackTrace();
			}
		}

	}

	public void creatIsland()
	{
		IntKeyHashMap map=new IntKeyHashMap();
		String temps[]=TextKit.split(str,",");
		int num=0;
		IntList list=new IntList();
		for(int i=0;i<temps.length;i+=2)
		{
			int temp=Integer.parseInt(temps[i+1]);
			num+=temp;
			list.add(Integer.parseInt(temps[i]));
			list.add(num);
		}
		for(int j=0;j<360000;j++)
		{
			int random=MathKit.randomValue(0,10001);
			if(random==0) random=1;
			int npcIslandSid=11002;
			for(int i=0;i<list.size();i+=2)
			{
				int nextValue=0;
				int nowValue=list.get(i+1);
				if(i!=0)
				{
					nextValue=list.get(i-1);
				}
				if(random>nextValue&&random<=nowValue)
				{
					npcIslandSid=list.get(i);
					break;
				}
			}
			int value=1;
			if(map.get(npcIslandSid)!=null)
				value+=Integer.parseInt(map.get(npcIslandSid).toString());
			map.put(npcIslandSid,value);
			NpcIsland island=(NpcIsland)NpcIsland.factory
				.newSample(npcIslandSid);
			island.setIndex(j);
			objectFactory.getIslandCache().getDbaccess().save(island);
		}
		System.out.println("create over=======");
		int keys[]=map.keyArray();
		int allnum=0;
		for(int i=0;i<keys.length;i++)
		{
			System.out.println("npcIsland sid ======"+keys[i]);
			allnum+=Integer.parseInt(map.get(keys[i]).toString());
			System.out.println("npcIsland num ======"+map.get(keys[i]));
		}
		System.out.println("allnum============"+allnum);
		// long time=System.currentTimeMillis();
	}

	/** 打印npcisland数据 */
	public void islandData()
	{
		IntKeyHashMap map=objectFactory.getIslandCache().getCacheMap();
		Object[] object=map.valueArray();
		IntKeyHashMap data=new IntKeyHashMap();
		for(int i=0;i<object.length;i++)
		{
			NpcIsLandSave islandsava=(NpcIsLandSave)object[i];
			NpcIsland island=islandsava.getData();
			ArrayList oneIsland=new ArrayList();
			oneIsland.add(island.getSid());
			if(data.get(island.getSid())!=null)
			{
				int num=Integer.parseInt(((ArrayList)data.get(island
					.getSid())).get(1).toString());
				num++;
				oneIsland.add(num);
				data.put(island.getSid(),oneIsland);
			}
			else
			{
				oneIsland.add(1);
				data.put(island.getSid(),oneIsland);
			}
		}
		Object[] dataisland=data.valueArray();
		int allnum=0;
		for(int i=0;i<dataisland.length;i++)
		{
			ArrayList list=(ArrayList)dataisland[i];
			allnum+=Integer.parseInt(list.get(1).toString());
		}
		System.out.println("allnum========"+allnum);
	}

	/** 根据sql语言返回玩家名字的集合 */
	public ArrayList getSqlFileds(String sql)
	{
		Fields fields[]=((PlayerGameDBAccess)objectFactory.getPlayerCache()
			.getDbaccess()).loadSqls(sql);
		ArrayList names=new ArrayList();
		for(int i=0;i<fields.length;i++)
		{
			names.add(fields[i].getArray()[0].getValue().toString());
		}
		return names;
	}

	// 打印岛屿分布
	public void worldIsland()
	{
		String str="\r\n";
		System.out.println("f=========");
		for(int i=0;i<70000;i++)
		{
			NpcIsland island=objectFactory.getIslandCache().getRandomSpace();
			int index=island.getIndex();
			// int
			// x=index%NpcIsland.WORLD_WIDTH,y=index/NpcIsland.WORLD_WIDTH;
			str+=index+":";
			objectFactory.getIslandCache().removeSpaceIsland(island);
		}
		File f=new File("file-zheng.txt");
		System.out.println("f========"+f);
		try
		{
			f.createNewFile();
			FileOutputStream fos=new FileOutputStream(f);
			ObjectOutputStream oos=new ObjectOutputStream(fos);

			oos.writeObject(str);
			oos.close();
		}
		catch(IOException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}

	/** 计算星级和战斗力 */
	public void aaa()
	{
		Object object[]=cache.getCacheMap().valueArray();
		for(int i=0;i<object.length;i++)
		{
			PlayerSave data=(PlayerSave)object[i];
			Player player=data.getData();
			// 计算星星
			player.setPlunderResource(player.getSelfCheckPoint()
				.getAllstars());
			// 计算战斗力
			SeaBackKit.setPlayerFightScroe(player,objectFactory);
			cache.getDbaccess().save(player);
		}
	}
	
	/**清理等级为1-3的玩家岛屿坐标 且2天没有登录过*/
	public void clearIsland()
	{
		Object object[]=cache.getCacheMap().valueArray();
		for(int i=0;i<object.length;i++)
		{
			PlayerSave data=(PlayerSave)object[i];
			Player player=data.getData();
			int createTime = player.getCreateTime();
			createTime = TimeKit.getSecondTime()-createTime;
			if(player.getLevel()<=5&&(createTime>60*60*24))
			{
				NpcIsland island = objectFactory.getIslandCache().getPlayerIsland(player.getId());
				if(island==null)continue;
				island.setPlayerId(0);
				objectFactory.getIslandCache().getDbaccess().save(island);
			}
		}
	}

	public void change()
	{
//		aaa();
//		clearIsland();
//		worldIsland();
	}

	public void creatEnent()
	{
		NpcIsland island=(NpcIsland)NpcIsland.factory.newSample(10002);
		FightEvent event=objectFactory.createFightEvent(player.getId(),30,
			10,island.createFleetGroup());
		// 计算时间
		int needTime=needTime(30,10);
		event.setCreatAt(TimeKit.getSecondTime());
		// event.setNeedTime(needTime);
		event.setType(FightEvent.ATTACK_BACK);
	}

	/** 计算攻击时间 */
	public int needTime(int attackIndex,int beIndex)
	{
		int x=attackIndex%NpcIsland.WORLD_WIDTH,y=attackIndex
			/NpcIsland.WORLD_WIDTH;
		int beX=beIndex%NpcIsland.WORLD_WIDTH,beY=beIndex
			/NpcIsland.WORLD_WIDTH;
		int needTime=(int)Math.sqrt(((x-beX)*(x-beX)+(y-beY)*(y-beY)));
		return needTime*10+60;
	}

	public void fleet(Fleet fleet[])
	{
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null) continue;
			System.out.println("i============="+i);
			System.out.println("index============"+fleet[i].getLocation());
			System.out.println("fleet====ship====="
				+fleet[i].getShip().getSid());
			System.out.println("fleet====ship====="
				+fleet[i].getShip().hashCode());
			System.out.println("fleet====num====="+fleet[i].getNum());
		}
	}

	/**
	 * @return gameDataHandleImp
	 */
	public GameDataHandleImp getGameDataHandleImp()
	{
		return gameDataHandleImp;
	}

	/**
	 * @param gameDataHandleImp 要设置的 gameDataHandleImp
	 */
	public void setGameDataHandleImp(GameDataHandleImp gameDataHandleImp)
	{
		this.gameDataHandleImp=gameDataHandleImp;
	}

	/**
	 * @return ac
	 */
	public GameDBAccess getAc()
	{
		return ac;
	}

	/**
	 * @param ac 要设置的 ac
	 */
	public void setAc(GameDBAccess ac)
	{
		this.ac=ac;
	}

	public void onTimer(TimerEvent e)
	{
//		ApplePush
//			.getInstance()
//			.sendPush(
//				"5a0308c27491d5dee0c8cdba6f175b849ef4d5f00a04f160cea43cd736459cc1",
//				"中国wwwww22222区,"+TimeKit.getMillisTime(),"icetiger");
		// buildmanager.pushAllBuilds(player,TimeKit.getSecondTime());
		// TaskManager task=player.getTaskManager();
		// task.pushNextTask();
		// int taskMark[]=task.getTasksMark();
		// System.out.println("=====资源==="+player.getResources()[0]);
		// System.out.println("=====资源==="+player.getResources()[1]);
		// System.out.println("=====资源==="+player.getResources()[2]);
		// System.out.println("=====资源==="+player.getResources()[3]);
		// System.out.println("=====资源==="+player.getResources()[4]);
		// System.out.println("=====资源==="+player.getResources()[5]);
		// System.out.println("=====资源==="+player.getResources()[6]);
		// for(int i=0;i<taskMark.length;i++)
		// {
		// System.out.println("任务标记====="+taskMark[i]+" i=="+i);
		// }
		//
		// int taskMark1[][]=task.getDaytasksMark();
		// for(int i=0;i<taskMark1.length;i++)
		// {
		// for(int j=0;j<taskMark1[i].length;j++)
		// {
		// System.out.println("每日任务标记========"+taskMark1[i][j]
		// +"i======="+i+"j======"+j);
		// }
		// }
		// Object builds[]=player.getIsland().getBuildArray();
		// for(int i=0;i<builds.length;i++)
		// {
		// PlayerBuild build=(PlayerBuild)builds[i];
		// System.out.println("playerBuild====="+build.getBuildName());
		// }
		//
		// Object object[]=task.getTasks().toArray();
		// for(int i=0;i<object.length;i++)
		// {
		// Task tasks=(Task)object[i];
		// if(tasks.getTaskType()==Task.TASK_DAY)
		// {
		// int dayTime=task.getDayTaskDateTime(tasks.getKey(),tasks
		// .getValue());
		// System.out.println("===="+tasks.getKey());
		// System.out.println("===="+tasks.getValue());
		// System.out.println("========今日次数======="+dayTime);
		// }
		// if(tasks.isFinish()) task.reportTask(tasks.getSid());
		// }
		// String str=buildmanager.checkBuildOne(player,14,1);
		// if(str==null) buildmanager.buildOne(player,14,1);
		//
		// Object ships[]=player.getIsland().getTroops().toArray();
		// for(int i=0;i<ships.length;i++)
		// {
		// Troop trop=(Troop)ships[i];
		// System.out.println("玩家兵力============"+trop.getShipSid());
		// System.out.println("玩家兵力============"+trop.getNum());
		// }
		//
		// str=buildmanager.checkBuildOne(player,15,2);
		// if(str==null) buildmanager.buildOne(player,15,2);
		// str=buildmanager.checkBuildOne(player,9,6);
		// if(str==null) buildmanager.buildOne(player,9,6);
		//
		// str=buildmanager.checkProduceShips(player,201,1,9);
		// System.out.println("造兵==============="+str);
		// if(str==null) buildmanager.buildShips(player,201,1,9);
		//
		// Island island=player.getIsland();
		// PlayerBuild
		// checkBuild=island.getBuildByIndex(9,island.getBuilds());
		// if(checkBuild!=null)
		// {
		// StandProduce produce=(StandProduce)checkBuild.getProduce();
		// ObjectArray productes=produce.getProductes();
		// if(productes!=null&&productes.size()>0)
		// {
		// Object objects[]=productes.toArray();
		// for(int i=0;i<objects.length;i++)
		// {
		// System.out.println("舰船建筑队列======"+i);
		// }
		// }
		// }
		//
		// str=buildmanager.checkBuildOne(player,6,9);
		// if(str==null) buildmanager.buildOne(player,6,9);
		//
		// str=buildmanager.checkProducePorps(player,2,1,6);
		// System.out.println("str============"+str);
		// if(str==null) buildmanager.buildProps(player,2,1,6);
		//
		// // str=buildmanager.checkbuildUpLevel(player,6);
		// // if(str==null) buildmanager.buildUpLevel(player,6);
		//
		// PropList bunld=player.getBundle();
		// Prop[] prop=bunld.getProps();
		// for(int i=0;i<prop.length;i++)
		// {
		// System.out.println("玩家物品=========="+i);
		// System.out.println("玩家物品=========="+prop[i].getName());
		// System.out.println("==数量=="+((NormalProp)prop[i]).getCount());
		// }
	}

	/**
	 * @return cache
	 */
	public PlayerMemCache getCache()
	{
		return cache;
	}

	/**
	 * @param cache 要设置的 cache
	 */
	public void setCache(PlayerMemCache cache)
	{
		this.cache=cache;
	}

	/**
	 * @return messageCache
	 */
	public MessageMemCache getMessageCache()
	{
		return messageCache;
	}

	/**
	 * @param messageCache 要设置的 messageCache
	 */
	public void setMessageCache(MessageMemCache messageCache)
	{
		this.messageCache=messageCache;
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFafctory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
}
