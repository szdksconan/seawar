package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/** 建筑类条件 拥有条件 */
public class BuildCondition extends Condition
{

	/* fields */
	/** 建筑类型 */
	int buildType;
	/** 建筑等级 */
	int buildLevel;
	/** 建筑数量 */
	int num;

	/* methods */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		return this;
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
	}
	/** 获得player拥有的建筑数量 */
	public int getBuildNum(Player player)
	{
		return player.getIsland().getBuildNumByType(buildType,
			player.getIsland().getBuilds());
	}
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getSource() instanceof PlayerBuild
			&&event.getEventType()==PublicConst.BUILD_FINISH_TASK_EVENT)
		{
			PlayerBuild playerBuild=(PlayerBuild)event.getSource();
			if(playerBuild.getBuildType()!=buildType) return 0;
			if(playerBuild.getBuildLevel()<buildLevel)
				return Task.TASK_CHANGE;
			if(getBuildNum(player)<num) return Task.TASK_CHANGE;
			return Task.TASK_FINISH;
		}
		else
		{
			// 检查所有
			Object builds[]=player.getIsland().getBuildArray();
			boolean bool=false;
			for(int i=0;i<builds.length;i++)
			{
				PlayerBuild playerBuild=(PlayerBuild)builds[i];
				if(playerBuild.getBuildType()==buildType
					&&playerBuild.getBuildLevel()>=buildLevel)
				{
					bool=true;
					break;
				}
			}
			if(bool&&getBuildNum(player)<num) return Task.TASK_CHANGE;
			if(bool) return Task.TASK_FINISH;
		}
		return 0;
	}
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		PlayerBuild playerBuild=p.getIsland().getBuildByType(buildType,
			p.getIsland().getBuilds());
		if(playerBuild==null)
		{
			data.writeByte(0);
			data.writeByte(0);
		}
		else
		{
			int lv=playerBuild.getBuildLevel();
			data.writeByte(lv);
			data.writeByte(getBuildNum(p));
		}
	}
}
