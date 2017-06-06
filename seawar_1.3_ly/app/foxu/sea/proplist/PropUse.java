package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**物品使用接口
 * 
 * lxh
 * */
public interface PropUse
{
	/**检查使用物品*/
	public boolean checkUse(Player player);
	/**使用物品,如果是宝箱类物品data写入奖励品信息*/
    public void use(Player player,CreatObjectFactory objectFactory,ByteBuffer data);
    public void use(Player player,CreatObjectFactory objectFactory,ByteBuffer data,int num);
    public void use(Player player,CreatObjectFactory objectFactory,ByteBuffer data,boolean flush);
}
