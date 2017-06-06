package foxu.sea.port;

import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.arena.ArenaHelper;
import foxu.sea.arena.ArenaManager;
import foxu.sea.task.TaskEventExecute;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntList;

/**
 *  竞技场端口
 * 
 * @author comeback
 *
 */
public class ArenaPort extends AccessPort
{

	ArenaManager arenaManager;
	
	public void setArenaManager(ArenaManager arenaManager)
	{
		this.arenaManager=arenaManager;
	}
	
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		// 获取玩家自己的信息，包括对手信息
		if(type==ArenaHelper.GET_MAIN_INFO)
		{
			return arenaManager.getMainInfo(player);
		}
		// 获取排行信息
		else if(type==ArenaHelper.GET_RANK_INFO)
		{
			int pageIndex=data.readUnsignedByte();
			return arenaManager.getRankingInfo(pageIndex);
		}
		// 挑战
		else if(type==ArenaHelper.APPLY_FIGHT)
		{
			String name=data.readUTF();
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.ATTACK_ARENA_TASK_EVENT,null,player,null);
			return arenaManager.applyFight(player,name);
		}
		// 获取奖励
		else if(type==ArenaHelper.GET_AWARD)
		{
			return arenaManager.getAward(player);
		}
		// 设置舰队
		else if(type==ArenaHelper.DEPLOY_FLEET)
		{
			IntList list=new IntList();
			int length=data.readUnsignedByte();
			if(length<=0)
				throw new DataAccessException(0,"you must set fleet");
			return arenaManager.deployFleet(player,list,length,data);
		}
		// 加速
		else if(type==ArenaHelper.SPEED_UP)
		{
			arenaManager.speedUp(player);
		}
		// 获取邮件数量
		else if(type==ArenaHelper.GET_REPORT_COUNT)
		{
			return arenaManager.getReportCount(player,data);
		}
		// 获取邮件项目
		else if(type==ArenaHelper.GET_REPORT)
		{
			int pageIndex=data.readUnsignedByte();
			data=arenaManager.getReports(player,pageIndex,data);
			return data;
		}
		// 获取邮件内容
		else if(type==ArenaHelper.GET_RETPORT_CONTENT)
		{
			int msgId=data.readInt();
			return arenaManager.getReportContent(player,msgId,data);
		}
		// 删除邮件
		else if(type==ArenaHelper.REMOVE_REPORT)
		{
			int len=data.readUnsignedByte();
			for(int i=len;i>0;i--)
			{
				int msgId=data.readInt();
				arenaManager.removeReport(player,msgId);
			}
			data.clear();
		}
		// 删除所有邮件
		else if(type==ArenaHelper.CLEAR_REPORTS)
		{
			arenaManager.clearReports(player);
			data.clear();
		}
		//查看保持当前排名可以获得的奖励
		else if(type==ArenaHelper.VIEW_AWARD){
			return arenaManager.viewAward(player);
		}
		return data;
	}

}
