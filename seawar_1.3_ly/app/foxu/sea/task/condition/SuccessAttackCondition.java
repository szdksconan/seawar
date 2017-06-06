package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** սʤ */
public class SuccessAttackCondition extends Condition
{

	/** ATTACK_PLAYER=8��ʾ������� Ϊ0��ʾ����5��Ұ�� */
	public static final int ATTACK_PLAYER=8;
	/** �������� �ο�island�ϵ�type attackType=7 ��ʾ����ս 8��ʾ������� */
	int attackType;
	/** ����ĵȼ� */
	int islandLevel;
	/** �Ƿ�ʤ�� */
	boolean success;
	/** �������� */
	int attackTime;
	/** ��ǰ�������� */
	int nowAttack;

	/* methods */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		nowAttack=data.readUnsignedByte();
		return this;
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(nowAttack);
	}
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getEventType()==PublicConst.ATTACK_TASK_EVENT)
		{
			if(event.getSource() instanceof NpcIsland)
			{
				// �Ƿ�ʤ��
				boolean success=(Boolean)event.getParam();
				NpcIsland island=(NpcIsland)event.getSource();
				// �������
				if(attackType==ATTACK_PLAYER&&island.getPlayerId()!=0)
				{
					return isAddTime(success);
				}
				// �����ؿ�
				else if(attackType==NpcIsland.CHECK_POINT_NPC
					&&island.getIslandType()==NpcIsland.CHECK_POINT_NPC)
				{
					return isAddTime(success);
				}
				// // ����npc����
				// else if(attackType==NpcIsland.ISLAND_NPC
				// &&island.getIslandType()==NpcIsland.ISLAND_NPC)
				// {
				// if(island.getIslandLevel()>=islandLevel)
				// {
				// return isAddTime(success);
				// }
				// return 0;
				// }
				// ����5��Ұ��Ұ��
				else if(attackType==0)
				{
					// 5��Ұ���κ�һ��
					if(island.getIslandType()>=NpcIsland.ISLAND_METAL
						&&island.getIslandType()<=NpcIsland.ISLAND_MONEY
						&&island.getPlayerId()==0)
					{
						if(island.getIslandLevel()>=islandLevel)
						{
							return isAddTime(success);
						}
						return 0;
					}
				}
				// ����ָ��Ұ��
				else
				{
					if(island.getIslandType()==attackType)
					{
						if(island.getIslandLevel()>=islandLevel
							&&island.getPlayerId()==0)
						{
							return isAddTime(success);
						}
					}
				}
			}
		}
		return 0;
	}

	// �Ƿ���Ӵ������ж�
	public int isAddTime(boolean success)
	{
		if(this.success)
		{
			if(success)
			{
				nowAttack++;
				if(nowAttack>=attackTime)
				{
					nowAttack=attackTime;
					return Task.TASK_FINISH;
				}
				return Task.TASK_CHANGE;
			}
		}
		else
		{
			nowAttack++;
			if(nowAttack>=attackTime)
			{
				nowAttack=attackTime;
				return Task.TASK_FINISH;
			}
			return Task.TASK_CHANGE;
		}
		return 0;
	}

	public void showBytesWrite(ByteBuffer data,Player p)
	{
		data.writeByte(nowAttack);
	}
}
