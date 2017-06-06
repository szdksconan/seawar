package jedis.test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;


public class doTest_1
{
	
	
	public static void main(String[] args)
	{
		MessageCollection collenction = new MessageCollection();//�����Ϣ������
		SimpleDateFormat c = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		while(true){
		System.out.println("----��ӭ��½���԰�ϵͳ----");
		System.out.println("-------���˵�-------");
		System.out.println("    1����ѯ����   ");
		System.out.println("    2���������");
		System.out.println("    3���˳�ϵͳ");
		System.out.println("��ѡ��");
		Scanner sc = new Scanner(System.in);
		String choice = sc.nextLine();
		
		if("1".equals(choice)){//��ѯ
			for(int i=0;i<collenction.getSize();i++) {
				collenction.get(i).show();
			}
			continue;
		}else if("2".equals(choice)){//���
			Message message = new Message();//һ����Ϣ
			
			message.setID(collenction.getSize()+"");//����ID
			message.setTime(c.format(new Date()));//����ʱ��
			
			System.out.println("����������������:");
			message.setUserName(sc.nextLine());
			System.out.println("���������:");
			message.setTitle(sc.nextLine());
			System.out.println("�������������ݣ�");
			message.setContent(sc.nextLine());
			
			collenction.add(message);
			continue;
		}else if("3".equals(choice)){
			System.out.println("ллʹ�ã��ټ���");
			break;
		}else{
			System.out.println("�����ѡ��");
			continue;
		}
		}
	}
	
}



/**
 * ��Ϊֻ�������� �Լ�д���򵥵ļ����� ֻ�ṩ��ӷ���
 * @author liuh
 *
 */
class MessageCollection{
	
	private Message[] messages ;
	private int size;
	
	/**
	 * �����ȵĹ�����
	 */
	public MessageCollection(int initialCapacity) {
			this.messages = new Message[initialCapacity];
	}
	
	/**
	 * Ĭ�Ϲ�����
	 */
	public MessageCollection() {
	this(10);
	}
	
	/**
	 * ����ݻ�
	 */
	public void ensureCapacity(int minCapacity) {
			int oldCapacity = messages.length;
			if (minCapacity > oldCapacity) {//�����������
			    int newCapacity = (oldCapacity * 3)/2 + 1;//����������ĳ���
		    	if (newCapacity < minCapacity) newCapacity = minCapacity;//������ǲ����� ������Ҫ�ĳ���
		    	messages = Arrays.copyOf(messages, newCapacity);//�����µĸ���������
			}
	}
	
	/**
	 * �������Ϣ
	 */
	public boolean add(Message e) {
		ensureCapacity(size + 1);  // Increments modCount!!
		messages[size++] = e;
		return true;
	}

	public Message get(int index){
		if (index >= size)//���������׳�Խ���쳣
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
 * ����ʵ����
 * @author liuh
 */
class Message {
	
	
	String ID;
	String userName;
	String title;
	String content;
	String time;
	
	/**
	 * ��ʾ��Ϣ
	 */
	public void show(){
			System.out.print("�����ˣ�"+ID);
			System.out.println("  ����ʱ�䣺"+time);
			System.out.println("���⣺"+title);
			System.out.println("�������ݣ�"+time);
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