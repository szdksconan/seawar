package jedis.test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;


public class doTest_1
{
	
	
	public static void main(String[] args)
	{
		MessageCollection collenction = new MessageCollection();//存放消息的容器
		SimpleDateFormat c = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		while(true){
		System.out.println("----欢迎登陆留言板系统----");
		System.out.println("-------主菜单-------");
		System.out.println("    1、查询留言   ");
		System.out.println("    2、添加留言");
		System.out.println("    3、退出系统");
		System.out.println("请选择");
		Scanner sc = new Scanner(System.in);
		String choice = sc.nextLine();
		
		if("1".equals(choice)){//查询
			for(int i=0;i<collenction.getSize();i++) {
				collenction.get(i).show();
			}
			continue;
		}else if("2".equals(choice)){//添加
			Message message = new Message();//一条消息
			
			message.setID(collenction.getSize()+"");//设置ID
			message.setTime(c.format(new Date()));//设置时间
			
			System.out.println("请输入留言人姓名:");
			message.setUserName(sc.nextLine());
			System.out.println("请输入标题:");
			message.setTitle(sc.nextLine());
			System.out.println("请输入留言内容：");
			message.setContent(sc.nextLine());
			
			collenction.add(message);
			continue;
		}else if("3".equals(choice)){
			System.out.println("谢谢使用，再见！");
			break;
		}else{
			System.out.println("错误的选项");
			continue;
		}
		}
	}
	
}



/**
 * 因为只能用数组 自己写个简单的集合类 只提供添加方法
 * @author liuh
 *
 */
class MessageCollection{
	
	private Message[] messages ;
	private int size;
	
	/**
	 * 带长度的构造器
	 */
	public MessageCollection(int initialCapacity) {
			this.messages = new Message[initialCapacity];
	}
	
	/**
	 * 默认构造器
	 */
	public MessageCollection() {
	this(10);
	}
	
	/**
	 * 检测容积
	 */
	public void ensureCapacity(int minCapacity) {
			int oldCapacity = messages.length;
			if (minCapacity > oldCapacity) {//如果超出长度
			    int newCapacity = (oldCapacity * 3)/2 + 1;//给出新数组的长度
		    	if (newCapacity < minCapacity) newCapacity = minCapacity;//如果还是不够长 则用需要的长度
		    	messages = Arrays.copyOf(messages, newCapacity);//生成新的更长的数组
			}
	}
	
	/**
	 * 添加新消息
	 */
	public boolean add(Message e) {
		ensureCapacity(size + 1);  // Increments modCount!!
		messages[size++] = e;
		return true;
	}

	public Message get(int index){
		if (index >= size)//超出长度抛出越界异常
		    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
		return messages[index];
	}
	
	
	public int getSize()
	{
		return size;
	}

	
	public void setSize(int size)
	{
		this.size=size;
	}
	
	
	
	
}





/**
 * 留言实体类
 * @author liuh
 */
class Message {
	
	
	String ID;
	String userName;
	String title;
	String content;
	String time;
	
	/**
	 * 显示信息
	 */
	public void show(){
			System.out.print("留言人："+ID);
			System.out.println("  留言时间："+time);
			System.out.println("标题："+title);
			System.out.println("留言内容："+time);
			System.out.println();
	}
	
	
	public String getID()
	{
		return ID;
	}
	
	public void setID(String iD)
	{
		ID=iD;
	}
	
	public String getUserName()
	{
		return userName;
	}
	
	public void setUserName(String userName)
	{
		this.userName=userName;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title=title;
	}
	
	public String getContent()
	{
		return content;
	}
	
	public void setContent(String content)
	{
		this.content=content;
	}
	
	public String getTime()
	{
		return time;
	}
	
	public void setTime(String time)
	{
		this.time=time;
	}
	
}