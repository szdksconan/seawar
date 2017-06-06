package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;


/**
 * 依赖容器时钟驱动的实时活动
 * @author Alan
 *
 */
public interface ActivityCollate
{
	/** 整理活动 */
	public void activityCollate(int time,CreatObjectFactory factoty);
}
