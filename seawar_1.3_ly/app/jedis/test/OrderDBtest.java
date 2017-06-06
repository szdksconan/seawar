package jedis.test;

import mustang.orm.ConnectionManager;
import mustang.orm.SqlPersistence;
import foxu.dcaccess.OrderGameDBAccess;
import foxu.sea.order.Order;

public class OrderDBtest
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
    	sql.setTable("orders");
    	
    	OrderGameDBAccess p = new OrderGameDBAccess();
    	p.setGamePersistence(sql);
    	
//    	Order order = new Order();
//    	order.setCreateAt(TimeKit.getSecondTime());
//    	order.setId(Integer.MAX_VALUE);
//    	order.setMoney(20000);
//    	order.setUserName("icetiger");
//    	p.save(order);
    	Order order = p.load("2147483647");
    	order.setUserId(778899);
    	p.save(order);
	}
}
