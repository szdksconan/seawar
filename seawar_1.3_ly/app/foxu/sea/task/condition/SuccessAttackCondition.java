package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** 战胜 */
public class SuccessAttackCondition extends Condition
{

	/** ATTACK_PLAYER=8表示攻击玩家 为0表示攻击5种野地 */
	public static final int ATTACK_PLAYER=8;
	/** 攻击类型 参考island上的type attackType=7 表示剧情战 8表示攻击玩家 */
	int attackType;
	/** 岛屿的等级 */
	int islandLevel;
	/** 是否胜利 */
	boolean success;
	/** 攻击次数 */
	int attackTime;
	/** 当前攻击次数 */
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
				// 是否胜利
				boolean success=(Boolean)event.getParam();
				NpcIsland island=(NpcIsland)event.getSource();
				// 攻击玩家
				if(attackType==ATTACK_PLAYER&&island.getPlayerId()!=0)
				{
					return isAddTime(success);
				}
				// 攻击关卡
				else if(attackType==NpcIsland.CHECK_POINT_NPC
					&&island.getIslandType()==NpcIsland.CHECK_POINT_NPC)
				{
					return isAddTime(success);
				}
				// // 攻击npc岛屿
				// else if(attackType==NpcIsland.ISLAND_NPC
				// &&island.getIslandType()==NpcIsland.ISLAND_NPC)
				// {
				// if(island.getIslandLevel()>=islandLevel)
				// {
				// return isAddTime(success);
				// }
				// return 0;
				// }
				// 攻击5种野地野地
				else if(attackType==0)
				{
					// 5种野地任何一种
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
				// 攻击指定野地
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

	// 是否添加次数的判定
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
