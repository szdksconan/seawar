/**
 * 
 */
package foxu.sea.event;

import foxu.dcaccess.CreatObjectFactory;
import mustang.io.ByteBuffer;

/**
 * 战斗事件面向前台的序列化方法
 * 
 * @author rockzyt
 */
public interface FightEventShowByteswriteable
{
	void showBytesWrite(FightEvent e,ByteBuffer data,int current,
		CreatObjectFactory objectFactory);
}