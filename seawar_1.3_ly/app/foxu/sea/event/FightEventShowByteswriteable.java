/**
 * 
 */
package foxu.sea.event;

import foxu.dcaccess.CreatObjectFactory;
import mustang.io.ByteBuffer;

/**
 * ս���¼�����ǰ̨�����л�����
 * 
 * @author rockzyt
 */
public interface FightEventShowByteswriteable
{
	void showBytesWrite(FightEvent e,ByteBuffer data,int current,
		CreatObjectFactory objectFactory);
}