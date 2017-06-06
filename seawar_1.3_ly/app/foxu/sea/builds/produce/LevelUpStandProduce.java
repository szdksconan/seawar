package foxu.sea.builds.produce;

import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.builds.Produceable;
import foxu.sea.builds.Product;

public class LevelUpStandProduce extends StandProduce
{
	/**
	 * index=取消的队列数组下标
	 */
	public void cancelProduce(Player player,int index,CreatObjectFactory objectFactory)
	{
		if(productes==null||productes.size()<=0) return;
		if(productes.size()<=index) return;
		Product p=(Product)productes.getArray()[index];
		Produceable pa=(Produceable)factory.newSample(p.sid);
		try
		{
			pa.cancelUp(player,p,objectFactory);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			productes.remove(p);
		}
		// 重新计算完成时间
		if(productes.size()>0)
		{
			Object left[]=productes.getArray();
			// index=0的情况
			if(index==0)
			{
				p=(Product)left[0];
				p.setFinishTime(TimeKit.getSecondTime()+p.getProduceTime());
			}
		}
	}
}
