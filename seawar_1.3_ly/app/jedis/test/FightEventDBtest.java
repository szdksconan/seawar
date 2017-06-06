//package jedis.test;
//
//import mustang.io.ByteBuffer;
//import mustang.orm.ConnectionManager;
//import mustang.orm.SqlPersistence;
//import foxu.dcaccess.FightEventGameDBAccess;
//import foxu.sea.Resources;
//import foxu.sea.Role;
//import foxu.sea.Ship;
//import foxu.sea.event.FightEvent;
//import foxu.sea.event.FightShip;
//
//
//public class FightEventDBtest
//{
//    public static void main(String[] args)
//	{
//    	ConnectionManager  maanger = new ConnectionManager();
//    	maanger.getProperties().put("user","root");
//    	maanger.getProperties().put("password","123123");
//    	maanger.setDriver("com.mysql.jdbc.Driver");
//    	maanger.setURL("jdbc:mysql://localhost:3306/seawar");
//    	maanger.setCharacterEncoding("utf8");
//    	maanger.init();
//    	
//    	SqlPersistence sql = new SqlPersistence();
//    	sql.setConnectionManager(maanger);
//    	sql.setTable("fight_events");
//    	
//    	FightEventGameDBAccess p = new FightEventGameDBAccess();
//    	p.setGamePersistence(sql);
//    	
//    	FightEvent fightEvent = new FightEvent();
//    	fightEvent.setType(1);
//    	fightEvent.setAttackId(1001);
//    	Resources re = new Resources();
//    	re.addGems(10000);
//    	re.setMoney(99900);
//    	fightEvent.setResources(re);
//    	FightShip event = new FightShip();
//    	event.setIndex(1);
//    	event.setNum(10);
//    	Ship ship = new Ship();
//    	ship.setExperience(5000);
//    	ship.setLife(1000);
//    	ship.setPlayerType(111);
//    	event.setShip(ship);
//    	ship = (Ship)Role.factory.newSample(202);
//    	ship.setExperience(6000);
//    	ship.setLife(5000);
//    	ship.setPlayerType(123654);
//    	
//    	fightEvent.addShip(event);
//    	event = new FightShip();
//    	event.setShip(ship);
//    	fightEvent.addShip(event);
//    	p.save(fightEvent);
//    	
//    	FightEvent event1 = p.load("12");
//    	
//    	System.out.println("============"+event1.getType());
//    	System.out.println("============"+event1.getResources().getGems());
//    	System.out.println("============"+event1.getResources().getMetal());
//    	System.out.println("============"+event1.getResources().getMoney());
//    	System.out.println("============"+event1.getResources().getMoney());
//    	System.out.println("============"+((FightShip)event1.getAllFighters().toArray()[0]).getNum());
//    	System.out.println("============"+((FightShip)event1.getAllFighters().toArray()[1]).getShip().getLife());
//    	System.out.println("============"+((FightShip)event1.getAllFighters().toArray()[1]).getShip().getExperience());
//	}
//}
