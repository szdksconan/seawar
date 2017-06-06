//package jedis.test;
//
//import mustang.io.ByteBuffer;
//import mustang.orm.ConnectionManager;
//import mustang.orm.SqlPersistence;
//import foxu.dcaccess.MessageGameDBAccess;
//import foxu.sea.event.FightEvent;
//import foxu.sea.messgae.Message;
//import foxu.sea.messgae.MessageData;
//
//
//public class MessageTest
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
//    	sql.setTable("messages");
//    	
//    	MessageGameDBAccess p = new MessageGameDBAccess();
//    	p.setGamePersistence(sql);
//    	
//    	Message message = new Message();
//    	message.setMessageId(3);
//    	message.setContent("≤‚ ‘ƒ⁄»›221∞°∞°∞°∞°");
//    	message.setSendId(30);
//    	message.setReceiveId(31);
//    	message.setSendName("icetiger");
//    	message.setReceiveName("icetiger");
//    	message.setState(10);
//    	MessageData data = new MessageData();
//    	data.getResources().addGems(5000);
//    	message.setMessageData(data);
//    	p.save(message);
//    	
//    	Message event1 = p.load("3");
//    	
//    	System.out.println("============="+event1.getContent());
//    	System.out.println("============="+event1.getMessageData().getResources().getGems());
//	}
//} 
