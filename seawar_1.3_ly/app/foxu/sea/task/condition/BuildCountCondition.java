package foxu.sea.task.condition;

import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;
import mustang.io.ByteBuffer;

/**
 * 建筑数量条件
 * @author comeback
 *
 */
public class BuildCountCondition extends BuildCondition
{
	public int checkCondition(Player player,TaskEvent event)
	{
		if(event!=null&&event.getSource() instanceof PlayerBuild
			&&event.getEventType()==PublicConst.BUILD_FINISH_TASK_EVENT)
		{
			PlayerBuild playerBuild=(PlayerBuild)event.getSource();
			if(playerBuild.getBuildType()!=buildType) return 0;
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
				if(playerBuild.getBuildType()==buildType)
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
		}
		else
		{
			data.writeByte(getBuildNum(p));
		}
	}
}
