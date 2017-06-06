package jedis.test;


import mustang.orm.ConnectionManager;
import mustang.orm.SqlPersistence;
import mustang.util.TimeKit;
import foxu.dcaccess.PlayerGameDBAccess;


public class DBtest
{
    public static void main(String[] args)
	{
    	ConnectionManager  maanger = new ConnectionManager();
    	maanger.getProperties().put("user","root");
    	maanger.getProperties().put("password","123123");
    	maanger.setDriver("com.mysql.jdbc.Driver");
    	maanger.setURL("jdbc:mysql://localhost:3306/seawar");
    	maanger.setCharacterEncoding("utf8");
    	maanger.init();
    	
    	SqlPersistence sql = new SqlPersistence();
    	sql.setConnectionManager(maanger);
    	sql.setTable("players");
    	
    	PlayerGameDBAccess p = new PlayerGameDBAccess();
    	p.setGamePersistence(sql);
//    	long time = System.currentTimeMillis();
//    	for(int i=30;i<=31;i++)
//    	{
//    	  	foxu.wk.Player player = new foxu.wk.Player();
//    	  	player.setId(i);
//        	player.setIsland_name("12345678");
//        	player.setCreateAt(TimeKit.getSecondTime());
//        	player.setMuteTime(TimeKit.getSecondTime());
//        	player.setUpdateTime(TimeKit.getSecondTime());
//        	player.getResources().setGems(1000);
//        	player.getResources().setMoney(50000);
//        	player.setUser_id(i);
//        	player.setLevel(29);
//        	Quest quest = new Quest();
//        	quest.setTestb("任务插入正常2");
//        	Achievement ac = new Achievement();
//        	player.setQuest(quest);
//        	player.setAchievement(ac);
//        	Heros hero = new Heros();
//        	hero.setTestb("英雄插入正常");
//        	hero.setExperience(1000);
//        	BuildingAndTroops bd = new BuildingAndTroops();
//        	bd.setTestb("建筑插入正常");
//        	player.setBuildingAndTroops(bd);
//        	player.setEvent(new PlayerEvent());
//        	player.setHero(hero);
//        	player.setPlayer_name("ice"+i);
//        	p.save(player);
//    	}
//    	System.out.println("消耗时间-=-======================="+(System.currentTimeMillis()-time));
    	foxu.sea.Player player = p.load("31");
    	player.setCreateTime(TimeKit.getSecondTime());
    	p.save(player);
    	
    	/**jedis test================================**/
//    	ByteBuffer data = p.load("1");
//    	JedisMemCacheAccess jedis = new JedisMemCacheAccess();
//    	jedis.save("checkbase64",data);
//    	
//    	ByteBuffer dat1a = jedis.load("checkbase64");
//    	System.out.println("=========="+data.length());
//    	foxu.wk.Player player =  new foxu.wk.Player();
//    	player.bytesRead(dat1a);
//    	
//    	System.out.println("哈哈============"+player.getId());
//    	System.out.println("哈哈============"+player.getLevel());
//    	System.out.println("哈哈============"+player.getLocale());
//    	System.out.println("hah============"+player.getPlayer_name());
//    	System.out.println("hah============"+player.getIsland_name());
//    	System.out.println("哈哈============"+player.getQuest().getTesta());
//    	System.out.println("哈哈============"+player.getQuest().getTestb());
    	
	}
}
