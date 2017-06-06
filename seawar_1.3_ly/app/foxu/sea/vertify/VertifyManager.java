package foxu.sea.vertify;

import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;

/**
 * ��֤������ (�����)
 */
public class VertifyManager
{

	public IntKeyHashMap player_vertify=new IntKeyHashMap();

	/** �Ƿ�ﵽ��������� */
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
		// �ж���֤���Ƿ���
		if(PublicConst.VERTIFY_STATUS==1)
		{
			// �жϼ��ʱ���Ƿ�С���趨ʱ��
			if(now-operateTime<=PublicConst.VERTIFY_TRIGGER_INTERVAL)
			{
				// �жϴ����Ƿ�����趨���� �� �������
				if(operateCount>=PublicConst.VERTIFY_TRIGGER_COUNT||wrongCount>1)
				{
					vertify.setOperateCount(++operateCount);
					player_vertify.put(player.getId(),vertify);
					return true;
				}
			}
			else
			{
				// �����趨ʱ�� ��������
				vertify.setOperateTime(now);
				vertify.setOperateCount(0);
			}
			vertify.setOperateCount(++operateCount);
		}
		player_vertify.put(player.getId(),vertify);
		return false;
	}
}
