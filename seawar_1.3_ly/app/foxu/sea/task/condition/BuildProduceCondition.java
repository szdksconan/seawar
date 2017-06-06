package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.builds.Build;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * �������������� author:icetiger
 */
public class BuildProduceCondition extends Condition
{
	/** �������� */
	int metalProduce;
	/** ʯ�Ͳ��� */
	int oilProduce;
	/** ����� */
	int siliconProduce;
	/** ���˲��� */
	int uraniumProduce;
	/** ��Ǯ���� */
	int moneyProduce;

	public Object bytesRead(ByteBuffer data)
	{
		return this;
	}

	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
	}

	/** ����true����� */
	public int checkCondition(Player player,TaskEvent event)
	{
		// �������
		int checkState=Task.TASK_CHANGE;
		// ����
		int produceNum=0;
		if(metalProduce!=0)
		{
			produceNum=(int)player.getIsland().getProduceWithType(
				Build.BUILD_METAL);
			if(metalProduce<=produceNum) checkState=Task.TASK_FINISH;
		}
		if(oilProduce!=0)
		{
			produceNum=(int)player.getIsland()
				.getProduceWithType(Build.BUILD_OIL);
			if(oilProduce<=produceNum) checkState=Task.TASK_FINISH;
		}
		if(siliconProduce!=0)
		{
			produceNum=(int)player.getIsland().getProduceWithType(
				Build.BUILD_SILION);
			if(siliconProduce<=produceNum) checkState=Task.TASK_FINISH;
		}
		if(moneyProduce!=0)
		{
			produceNum=(int)player.getIsland().getProduceWithType(
				Build.BUILD_MONEY);
			if(moneyProduce<=produceNum) checkState=Task.TASK_FINISH;
		}
		if(uraniumProduce!=0)
		{
			produceNum=(int)player.getIsland().getProduceWithType(
				Build.BUILD_URANIUM);
			if(uraniumProduce<=produceNum) checkState=Task.TASK_FINISH;
		}
		return checkState;
	}
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		if(metalProduce!=0)
			data.writeInt((int)(p.getIsland()
				.getProduceWithType(Build.BUILD_METAL)*60));
		if(oilProduce!=0)
			data.writeInt((int)(p.getIsland().getProduceWithType(Build.BUILD_OIL)*60));
		if(siliconProduce!=0)
			data.writeInt((int)(p.getIsland().getProduceWithType(
				Build.BUILD_SILION)*60));
		if(uraniumProduce!=0)
			data.writeInt((int)(p.getIsland().getProduceWithType(
				Build.BUILD_URANIUM)*60));
		if(moneyProduce!=0)
			data.writeInt((int)(p.getIsland()
				.getProduceWithType(Build.BUILD_MONEY)*60));
	}
}
