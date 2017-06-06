package foxu.sea.builds;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**
 * 产品接口
 * 
 * @author rockzyt
 */
public interface Produceable
{
	/**完成*/
	public void finish(Player player,Product product,CreatObjectFactory objectFactory);
	/**取消*/
	public void cancel(Player player,Product product);
	/**取消升级*/
	public void cancelUp(Player player,Product product,CreatObjectFactory objectFactory);
}