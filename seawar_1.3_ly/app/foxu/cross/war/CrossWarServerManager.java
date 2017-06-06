package foxu.cross.war;

import mustang.set.ArrayList;
import mustang.util.Sample;
import foxu.cross.server.CrossServer;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;

/**
 * ���ս��������Ϣ �б�
 * 
 * @author yw
 * 
 */
public class CrossWarServerManager
{
	/** ���ſ��ս��ƽ̨ */
	public static int[] plats={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,
		19,20,21,22,23,24,25,26,27,28,29,30};
	/** ���ս�������б� */
	ArrayList serverlist=new ArrayList();

	/** ��ʼ�� */
	public void init()
	{
		if(!PublicConst.crossServer)return;
		Sample[] sps=CrossServer.factory.getSamples();
		for(int i=0;i<sps.length;i++)
		{
			if(sps[i]==null)continue;
			CrossServer cs=(CrossServer)sps[i];
			if(SeaBackKit.isContainValue(plats,cs.getPlatid()))
			{
				serverlist.add(cs);
			}
		}
	}
	
	/** ��ӷ����� */
	public void addServerOnly()
	{
		
	}
	
	/** ���ӷ�����  */
	public void addServer(CrossServer s1)
	{
		if(s1==null) return;
		rmServer(s1.getPlatid(),s1.getAreaid(),s1.getSeverid());
		serverlist.add(s1);
	}
	
	/** �Ƴ������� */
	public void rmServer(int platid,int areaid,int serverid)
	{
		CrossServer s1=new CrossServer();
		s1.setPlatid(platid);
		s1.setAreaid(areaid);
		s1.setSeverid(serverid);
		for(int i=0;i<serverlist.size();i++)
		{
			CrossServer s2=(CrossServer)serverlist.get(i);
			if(isSame(s1,s2))
			{
				serverlist.remove(i);
				break;
			}
		}
	}
	
	/** �Ƚ������������Ƿ���ͬһ�� */
	public boolean isSame(CrossServer s1,CrossServer s2)
	{
		if(s1==null||s2==null) return false;
		return s1.getPlatid()==s2.getPlatid()
			&&s1.getAreaid()==s2.getAreaid()
			&&s1.getSeverid()==s2.getSeverid();
	}

	/** ��ȡ��ҷ�������ip �Ͷ˿� */
	public String[] getIpPort(CrossWarPlayer player)
	{
		if(player==null) return null;
		for(int i=0;i<serverlist.size();i++)
		{
			CrossServer s=(CrossServer)serverlist.get(i);
			if(s.getPlatid()==player.getPlatid()
				&&s.getAreaid()==player.getAreaid()
				&&s.getSeverid()==player.getSeverid())
			{
				String[] ipp=new String[2];
				ipp[0]=s.getIp();
				ipp[1]=s.getPort();
				return ipp;
			}

		}
		return null;
	}
	/** ��ȡ��ҷ� �������� */
	public String getServerName(CrossWarPlayer player)
	{
		if(player==null) return "no server";
		for(int i=0;i<serverlist.size();i++)
		{
			CrossServer s=(CrossServer)serverlist.get(i);
			if(s.getPlatid()==player.getPlatid()
				&&s.getAreaid()==player.getAreaid()
				&&s.getSeverid()==player.getSeverid())
			{
				return s.getSeverName();
			}

		}
		return "no server";
	}
	/** ��ȡ��ҷ����� ip */
	public String getServerIp(CrossWarPlayer player)
	{
		if(player==null) return "0";
		for(int i=0;i<serverlist.size();i++)
		{
			CrossServer s=(CrossServer)serverlist.get(i);
			if(s.getPlatid()==player.getPlatid()
				&&s.getAreaid()==player.getAreaid()
				&&s.getSeverid()==player.getSeverid())
			{
				return s.getIp();
			}

		}
		return "0";
	}
	
	/** ��ȡ��ҷ����� ����  */
	public String getServerNatoinal(CrossWarPlayer player)
	{
		if(player==null) return "0";
		for(int i=0;i<serverlist.size();i++)
		{
			CrossServer s=(CrossServer)serverlist.get(i);
			if(s.getPlatid()==player.getPlatid()
				&&s.getAreaid()==player.getAreaid()
				&&s.getSeverid()==player.getSeverid())
			{
				return s.getNational();
			}

		}
		return "0";
	}
	/** ��ȡ������  ByIp*/
	public CrossServer getServerByIp(String ip)
	{
		for(int i=0;i<serverlist.size();i++)
		{
			CrossServer s=(CrossServer)serverlist.get(i);
			if(s.getIp().equals(ip))
			{
				return s;
			}

		}
		return null;
	}
	/** �Ƿ���ڸ÷����� */
	public boolean isExist(int platid,int areaid,int serverid)
	{
		return getServerById(platid,areaid,serverid)!=null;
	}
	/** ��ȡĳ�������� */
	public CrossServer getServerById(int platid,int areaid,int serverid)
	{
		for(int i=0;i<serverlist.size();i++)
		{
			CrossServer s=(CrossServer)serverlist.get(i);
			if(s.getSeverid()==serverid&&s.getAreaid()==areaid
				&&s.getPlatid()==platid)
			{
				return s;
			}
		}
		return null;
	}

	public ArrayList getServerlist()
	{
		return serverlist;
	}

	public void setServerlist(ArrayList serverlist)
	{
		this.serverlist=serverlist;
	}
}
