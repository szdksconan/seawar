package foxu.email;

import java.util.HashSet;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.push.AndroidPush;
import foxu.sea.Player;
import foxu.sea.User;
import foxu.sea.kit.SeaBackKit;
import mustang.thread.ThreadPoolExecutor;
import mustang.util.TimeKit;

/**
 * 邮件发送管理器
 * 
 * @author yw
 * 
 */
public class EmailManager
{

	/** 线程池 */
	ThreadPoolExecutor executor;

	/** 发信协议 */
	public static String emailProtocol="smtp";
	/** 发信端口 */
	public static String smtpPort;
	/** email服务器 */
	public static String emailServer;
	/** 邮箱用户名 */
	public static String user;
	/** 密码 */
	public static String pwd;

	InternetAddress from;

	private static EmailManager emanager;

	private EmailManager()
	{
	}
	public static EmailManager getInstance()
	{
		if(emanager==null)
		{
			emanager=new EmailManager();
			try
			{
				emanager.init();
			}
			catch(Exception e)
			{
			}
		}
		return emanager;
	}
	public void init() throws AddressException
	{
		executor=new ThreadPoolExecutor();
		executor.setLimit(5);
		executor.init();
		from=new InternetAddress(user);
	}
	public void sendMail(String emailTo,String title,String content)
	{
		executor.execute(new SendEmailTask(from,emailTo,title,content));
	}

	public String sendMailAndPush(CreatObjectFactory objectFactory,int days,
		String push,String title,String content)
	{
		if(days<0) return "erro days:"+days;
		if((push==null||push.equals(""))&&(title==null||title.equals(""))
			&&(content==null||content.equals("")))
			return "must write push or mail";
		Player player=null;
		User user=null;
		int daysTime=days*24*3600;
		int nowTime=TimeKit.getSecondTime();
		HashSet<String> pushed=new HashSet<String>();
		Object[] objs=objectFactory.getPlayerCache().getCacheMap().valueArray();
		for(int i=objs.length-1;i>=0;i--)
		{
			if(objs[i]==null)continue;
			player=((PlayerSave)objs[i]).getData();
			if(player==null) continue;
			if(nowTime-player.getUpdateTime()>=daysTime)
			{
				if(push!=null&&!push.equals("")
					&&canPush(objectFactory,player,pushed))
				{
					if(player.isIOS())
					{
						SeaBackKit.appPush(player,push);
					}
					else
					{
						AndroidPush.androidPush.addPush(player,push,null);
					}
					addPushed(objectFactory,player,pushed);
				}
				if((title!=null&&!title.equals(""))
					||(content!=null&&!content.equals("")))
				{
					title=title==null?"":title;
					content=content==null?"":content;
					user=objectFactory.getUserDBAccess().load(
						player.getUser_id()+"");
					if(user!=null&&user.getUserType()==User.USER)
					{
						sendMail(user.getUserAccount(),title,content);
					}
				}
			}

		}

		return null;
	}
	
	public void addPushed(CreatObjectFactory objectFactory,Player player,
		HashSet<String> pushed)
	{
		if(player.isIOS()&&player.getDeviceToken()!=null)
		{
			pushed.add(player.getDeviceToken());
		}
		else
		{
			String loginudid=player.getLoginUid();
			if(loginudid==null)
				loginudid=objectFactory.getUserDBAccess()
					.load(player.getUser_id()+"").getLoginUdid();
			pushed.add(loginudid);
		}
	}
	
	public boolean canPush(CreatObjectFactory objectFactory,Player player,
		HashSet<String> pushed)
	{
		if(player.isIOS()&&player.getDeviceToken()!=null)
		{
			return !pushed.contains(player.getDeviceToken());
		}
		if(!player.isIOS())
		{
			String loginudid=player.getLoginUid();
			if(loginudid==null)
				loginudid=objectFactory.getUserDBAccess()
					.load(player.getUser_id()+"").getLoginUdid();
			return !pushed.contains(loginudid);
		}
		return false;
	}

}
