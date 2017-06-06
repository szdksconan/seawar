package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * ∞Û∂®’À∫≈»ŒŒÒ
 * 
 * @author Alan
 * 
 */
public class BindAccoutCondition extends Condition
{

	@Override
	public int checkCondition(Player player,TaskEvent event)
	{
		User user=null;
		if(event!=null&&event.getEventType()==PublicConst.BIND_ACCOUNT_EVENT)
		{
			if(event.getParam() instanceof User)
				user=(User)event.getParam();
			else if(event.getParam() instanceof CreatObjectFactory)
				user=((CreatObjectFactory)event.getParam())
					.getUserDBAccess().loadById(player.getUser_id()+"");
			if(user!=null&&user.getUserType()==User.USER)
			{
				return Task.TASK_FINISH;
			}
		}
		return 0;
	}

	@Override
	public void showBytesWrite(ByteBuffer data,Player p)
	{
		// TODO Auto-generated method stub

	}

}
