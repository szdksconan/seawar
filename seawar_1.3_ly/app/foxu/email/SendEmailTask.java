package foxu.email;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import mustang.log.LogFactory;
import mustang.log.Logger;

/**
 * 发邮件任务
 * @author yw
 *
 */
public class SendEmailTask implements Runnable
{

	Logger log=LogFactory.getLogger(SendEmailTask.class);
	
	String toEmail;
	String title;
	String content;
	InternetAddress from;

	public SendEmailTask(InternetAddress from,
		String toEmail,String title,String content)
	{
		this.toEmail=toEmail;
		this.title=title;
		this.content=content;
		this.from=from;
	}

	public MimeMessage createEmail(Session session) throws Exception
	{
		MimeMessage mail=new MimeMessage(session);
		mail.setFrom(from);
		mail.addRecipient(Message.RecipientType.TO,new InternetAddress(
			toEmail));
		mail.setSubject(title);
//		mail.setText(content);
		mail.setContent(content, "text/html;charset=utf-8");
		return mail;

	}

	@Override
	public void run()
	{
		try
		{
			Properties property=new Properties();
			property.setProperty("mail.transport.protocol",EmailManager.emailProtocol);
			property.setProperty("mail.smtp.auth","true");
			property.setProperty("mail.smtp.localhost","127.0.0.1");
			property.setProperty("mail.smtp.port",EmailManager.smtpPort);

			Session session=Session.getInstance(property);
//			session.setDebug(true);//调试
			Transport transPort=session.getTransport();
			transPort.connect(EmailManager.emailServer,EmailManager.user,EmailManager.pwd);
			MimeMessage mail=createEmail(session);
			transPort.sendMessage(mail,
				mail.getRecipients(Message.RecipientType.TO));
			session.getTransport().close();
		}
		catch(Exception e)
		{
			log.warn("send mail to "+toEmail+" fail: "+e.toString());
		}

	}

}
