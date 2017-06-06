package foxu.sea.messgae;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import foxu.email.EmailManager;
import foxu.sea.InterTransltor;
import foxu.sea.PublicConst;
import foxu.sea.port.UserToCenterPort;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;

/**
 * 聊天信息举报管理器
 * 
 * @author alan
 * 
 */
public class ReportPlayerManager implements TimerListener
{

	public static int SEND_TIME=10*60*1000;
	public static final String REPORT_TITLE="report_email_title";
	private String emailServer;
	private String smtpPort;
	private String emailUser;
	private String emailPwd;
	private String toEmail;
	Map<String,Set<Report>> reports=new HashMap<String,Set<Report>>();

	public void init()
	{
		TimerEvent event=new TimerEvent(this,"player_report",SEND_TIME);
		TimerCenter.getMinuteTimer().add(event);
	}

	public void addReport(String reportPlayer,String beReportedPlayer,
		String beReportedContent)
	{
		if(reportPlayer==null||beReportedPlayer==null
			||beReportedContent==null) return;
		Set<Report> playerReports=(Set<Report>)reports.get(beReportedPlayer);
		if(playerReports!=null)
		{
			boolean isExist=false;
			for(Report report:playerReports)
			{
				if(beReportedContent.equals(report.beReportedContent))
				{
					report.reportNames.add(reportPlayer);
					isExist=true;
					break;
				}
			}
			if(!isExist)
			{
				Report report=new Report(beReportedContent);
				report.reportNames.add(reportPlayer);
				playerReports.add(report);
			}
		}
		else
		{
			playerReports=new HashSet<Report>();
			Report report=new Report(beReportedContent);
			report.reportNames.add(reportPlayer);
			playerReports.add(report);
			reports.put(beReportedPlayer,playerReports);
		}
	}

	public void sendReports()
	{
		StringBuffer reportsStr=new StringBuffer();
		String head="<table border='1'><tr><td colspan='4'>"
			+UserToCenterPort.SERVER_ID+"："+UserToCenterPort.SERVER_NAME+"</td></tr>";
		head+="<tr><td>name</td><td>content</td><td>count</td><td>from</td></tr>";
		reportsStr.append(head);
		if(reports.size()>0)
		{
			synchronized(reports)
			{
				for(String name:reports.keySet())
				{
					Set<Report> playerReports=(Set<Report>)reports.get(name);
					// 组装html
					reportsStr.append("<tr><td rowspan='"
						+playerReports.size()+"'>"+name+"</td>");
					boolean firstRow=true;
					for(Report report:playerReports)
					{
						if(firstRow)
							firstRow=false;
						else
							reportsStr.append("<tr>");
						reportsStr.append("<td>"+report.beReportedContent
							+"</td>");
						reportsStr.append("<td>"+report.reportNames.size()
							+"</td>");
						reportsStr.append("<td>");
						int lastSingnal=0;
						for(String reporter:report.reportNames)
						{
							lastSingnal++;
							reportsStr.append(reporter
								+(lastSingnal==report.reportNames.size()?""
									:", "));
						}
						reportsStr.append("</td></tr>");
					}
				}
				reports.clear();
			}
			reportsStr.append("</table>");
		}
		else
			return;

		String html=reportsStr.toString();
		EmailManager.emailServer=emailServer;
		EmailManager.smtpPort=smtpPort;
		EmailManager.user=emailUser;
		EmailManager.pwd=emailPwd;
		EmailManager.getInstance().sendMail(
			toEmail,
			InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,REPORT_TITLE),html);
	}

	class Report
	{

		public Report(String beReportedContent)
		{
			this.beReportedContent=beReportedContent;
		}

		Set<String> reportNames=new HashSet<String>();
		String beReportedContent;
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		sendReports();
	}

	public static int getSEND_TIME()
	{
		return SEND_TIME;
	}

	public static void setSEND_TIME(int sEND_TIME)
	{
		SEND_TIME=sEND_TIME;
	}

	public String getEmailServer()
	{
		return emailServer;
	}

	public void setEmailServer(String emailServer)
	{
		this.emailServer=emailServer;
	}

	public String getEmailUser()
	{
		return emailUser;
	}

	public void setEmailUser(String emailUser)
	{
		this.emailUser=emailUser;
	}

	public String getEmailPwd()
	{
		return emailPwd;
	}

	public void setEmailPwd(String emailPwd)
	{
		this.emailPwd=emailPwd;
	}

	public String getToEmail()
	{
		return toEmail;
	}

	public void setToEmail(String toEmail)
	{
		this.toEmail=toEmail;
	}

	public Map<String,Set<Report>> getReports()
	{
		return reports;
	}

	public void setReports(Map<String,Set<Report>> reports)
	{
		this.reports=reports;
	}

	public String getSmtpPort()
	{
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort)
	{
		this.smtpPort=smtpPort;
	}

}
