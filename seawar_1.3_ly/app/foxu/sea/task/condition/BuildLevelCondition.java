package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.sea.Player;
import foxu.sea.builds.PlayerBuild;

/**
 * �����ȼ�����
 * 
 * @author comeback
 *
 */
public class BuildLevelCondition extends BuildCondition
{

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
			int lv=playerBuild.getBuildLevel();
			data.writeByte(lv);
		}
	}
}
