package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**��Ʒʹ�ýӿ�
 * 
 * lxh
 * */
public interface PropUse
{
	/**���ʹ����Ʒ*/
	public boolean checkUse(Player player);
	/**ʹ����Ʒ,����Ǳ�������Ʒdataд�뽱��Ʒ��Ϣ*/
    public void use(Player player,CreatObjectFactory objectFactory,ByteBuffer data);
    public void use(Player player,CreatObjectFactory objectFactory,ByteBuffer data,int num);
    public void use(Player player,CreatObjectFactory objectFactory,ByteBuffer data,boolean flush);
}
