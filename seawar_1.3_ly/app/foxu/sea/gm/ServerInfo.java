package foxu.sea.gm;

import shelby.cc.CCManager;
import shelby.ds.DSManager;
import mustang.orm.ConnectionManager;
import foxu.dcaccess.CreatObjectFactory;

/**
 * ��������Ϣ
 * @author comeback
 *
 */
public interface ServerInfo
{
	public CreatObjectFactory getObjectFactory();
	
	public ConnectionManager getConnectionManager();
	
	public CCManager getCCManager();
	
	public DSManager getDSManager();
	
	public String getGameCenterIP();
	
	public int getGameCenterPort();
}
