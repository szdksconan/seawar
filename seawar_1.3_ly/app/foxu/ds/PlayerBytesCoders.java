/**
 * 
 */
package foxu.ds;

import mustang.io.ByteBuffer;
import mustang.io.BytesReader;
import mustang.io.BytesWriter;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.builds.BuildManager;

/**
 * ds使用的序列化反序列化工具
 * 
 * @author rockzyt
 */
public class PlayerBytesCoders implements BytesReader,BytesWriter
{

	BuildManager manager;

	public Object bytesRead(ByteBuffer data)
	{
		return null;
	}
	public void bytesWrite(Object obj,ByteBuffer data)
	{
		Player p=(Player)obj;
		if(manager!=null) manager.pushAllBuilds(p,TimeKit.getSecondTime());
		p.showBytesWrite(data,TimeKit.getSecondTime(),manager.getObjectFactory());
	}

	/**
	 * @return manager
	 */
	public BuildManager getManager()
	{
		return manager;
	}

	/**
	 * @param manager 要设置的 manager
	 */
	public void setManager(BuildManager manager)
	{
		this.manager=manager;
	}
}