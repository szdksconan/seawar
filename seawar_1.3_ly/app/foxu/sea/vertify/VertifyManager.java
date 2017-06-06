package foxu.sea.vertify;

import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;

/**
 * 验证管理类 (防外挂)
 */
public class VertifyManager
{

	public IntKeyHashMap player_vertify=new IntKeyHashMap();

	/** 是否达到检验的条件 */
	public boolean isNeedTest(Player player)
	{
		if(player==null) return false;
		Vertify vertify=(Vertify)player_vertify.get(player.getId());
		if(vertify==null)
		{
			vertify=new Vertify();
		}
		int operateCount=vertify.getOperateCount();
		long operateTime=vertify.getOperateTime();
		int wrongCount=vertify.getVertifyWrongCount();
		long now=TimeKit.getSecondTime();
		if(operateTime<=0)
		{
			operateTime=now;
		}
		// 判断验证器是否开启
		if(PublicConst.VERTIFY_STATUS==1)
		{
			// 判断间隔时间是否小于设定时间
			if(now-operateTime<=PublicConst.VERTIFY_TRIGGER_INTERVAL)
			{
				// 判断次数是否大于设定次数 和 错误次数
				if(operateCount>=PublicConst.VERTIFY_TRIGGER_COUNT||wrongCount>1)
				{
					vertify.setOperateCount(++operateCount);
					player_vertify.put(player.getId(),vertify);
					return true;
				}
			}
			else
			{
				// 超过设定时间 次数置零
				vertify.setOperateTime(now);
				vertify.setOperateCount(0);
			}
			vertify.setOperateCount(++operateCount);
		}
		player_vertify.put(player.getId(),vertify);
		return false;
	}
}
