package foxu.sea.config;

import mustang.set.IntKeyHashMap;


/**
 * 服务器配置信息管理器
 * @author yw
 *
 */
public class ConfigManager
{
	/** paltid - ConfigCell*/
	private IntKeyHashMap platMap=new IntKeyHashMap();
	
	
	public void addConfig(int pid,float version,String platname,String address,String share)
	{
		ConfigCell cell=new ConfigCell();
		cell.setPlatid(pid);
		cell.setVersion(version);
		cell.setPlatName(platname);
		cell.setAddress(address);
		cell.setShare(share);
		platMap.put(pid,cell);
	}
	
	public String getPlatName(int pid)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell==null) cell=(ConfigCell)platMap.get(0);
		return cell.getPlatName();
	}
	
	public void setPlatName(int pid,String platName)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell!=null) cell.setPlatName(platName);
	}
	
	public String getAddress(int pid)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell==null) cell=(ConfigCell)platMap.get(0);
		return cell.getAddress();
	}
	
	public void setAddress(int pid,String address)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell!=null) cell.setAddress(address);
	}
	
	public String getShare(int pid)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell==null) cell=(ConfigCell)platMap.get(0);
		return cell.getShare();
	}
	
	public void setShare(int pid,String share)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell!=null) cell.setShare(share);
	}
	
	public float getVersion(int pid)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell==null) cell=(ConfigCell)platMap.get(0);
		return cell.getVersion();
	}
	
	public void setVersion(int pid,float version)
	{
		ConfigCell cell=(ConfigCell)platMap.get(pid);
		if(cell!=null) cell.setVersion(version);
	}

}
