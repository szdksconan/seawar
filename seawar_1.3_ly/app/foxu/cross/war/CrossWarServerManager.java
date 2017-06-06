package foxu.cross.war;

import mustang.set.ArrayList;
import mustang.util.Sample;
import foxu.cross.server.CrossServer;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;

/**
 * 跨服战服务器信息 列表
 * 
 * @author yw
 * 
 */
public class CrossWarServerManager
{
	/** 开放跨服战的平台 */
	public static int[] plats={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,
		19,20,21,22,23,24,25,26,27,28,29,30};
	/** 跨服战服务器列表 */
	ArrayList serverlist=new ArrayList();

	/** 初始化 */
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
	
	/** 添加服务器 */
	public void addServerOnly()
	{
		
	}
	
	/** 增加服务器  */
	public void addServer(CrossServer s1)
	{
		if(s1==null) return;
		rmServer(s1.getPlatid(),s1.getAreaid(),s1.getSeverid());
		serverlist.add(s1);
	}
	
	/** 移除服务器 */
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
	
	/** 比较两个服务器是否是同一个 */
	public boolean isSame(CrossServer s1,CrossServer s2)
	{
		if(s1==null||s2==null) return false;
		return s1.getPlatid()==s2.getPlatid()
			&&s1.getAreaid()==s2.getAreaid()
			&&s1.getSeverid()==s2.getSeverid();
	}

	/** 获取玩家服务器的ip 和端口 */
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
	/** 获取玩家服 务器名字 */
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
	/** 获取玩家服务器 ip */
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
	
	/** 获取玩家服务器 国籍  */
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
	/** 获取服务器  ByIp*/
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
	/** 是否存在该服务器 */
	public boolean isExist(int platid,int areaid,int serverid)
	{
		return getServerById(platid,areaid,serverid)!=null;
	}
	/** 获取某个服务器 */
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
