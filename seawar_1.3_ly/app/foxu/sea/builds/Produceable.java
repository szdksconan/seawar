package foxu.sea.builds;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**
 * ��Ʒ�ӿ�
 * 
 * @author rockzyt
 */
public interface Produceable
{
	/**���*/
	public void finish(Player player,Product product,CreatObjectFactory objectFactory);
	/**ȡ��*/
	public void cancel(Player player,Product product);
	/**ȡ������*/
	public void cancelUp(Player player,Product product,CreatObjectFactory objectFactory);
}