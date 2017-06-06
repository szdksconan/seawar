package foxu.sea.comrade;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.Sample;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.PlayerGameDBAccess;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.port.UserToCenterPort;

/**
 * 战友系统 处理器
 * 
 * @author yw
 * 
 */
public class ComradeHandler 
{

	/* fields */
	/** 内存管理器 */
	CreatObjectFactory objfactory;
	/** 实例 */
	private static ComradeHandler comradeHandler;

	/* property */
	public CreatObjectFactory getObjfactory()
	{
		return objfactory;
	}

	public void setObjfactory(CreatObjectFactory objfactory)
	{
		this.objfactory=objfactory;
	}
	
	
	/* methods */
	public static ComradeHandler getInstance()
	{
		return comradeHandler;
	}
	
	public void init()
	{
		comradeHandler=this;
	}

	/** 添加新兵 */
	public String addRecruit(int veteranId,Player recruit,ByteBuffer data)
	{
		if(veteranId>0)
		{
			if(veteranId==recruit.getId())
				return "comarde can not yourself";
			// 邀请者是否属于本服id
			int serverInveteId=veteranId>>24;
			boolean bool=((PlayerGameDBAccess)objfactory.getPlayerCache()
				.getDbaccess()).isExistByID(veteranId);
			if(serverInveteId!=UserToCenterPort.SERVER_ID&&!bool)
				return "comarde fail";
		}
		// 该玩家已经被招募了
		if(recruit.getComrade().getVeteranId()!=0)
			return "this player have comarde";
		if(recruit.getComrade().getRecruitIds().contain(veteranId))
			return "you can not comare your parterner";
		// recruit 新兵
		// 老兵
		Player veteran=objfactory.getPlayerById(veteranId);
		if(veteran==null) return "comrade limit";
		if(veteran.getLevel()<Comrade.COMRADE_LEVEL)
			return "comrade level limit";
//		// 需要进行等级限制
		 if(recruit.getLevel()>veteran.getLevel())
		 return "level is limit";
		IntList recruitIds=veteran.getComrade().getRecruitIds();
		if(recruitIds.size()>=Comrade.R_MAX) return "length is limt";
		// 返回结果 说该玩家 已经被这个玩家成功招募
		if(recruitIds.contain(recruit.getId()))
			return "this player have aleady been comrade";
		recruitIds.add(recruit.getId());
		recruit.getComrade().setVeteranId(veteran.getId());
		data.clear();
		data.writeByte(PublicConst.COMMARDE_TYPE);
		data.writeUTF(veteran.getName());
		JBackKit.sendComardeInfo(veteran);
		finishTask(recruit,ComradeTask.LEVEL);
		finishTask(recruit,ComradeTask.FIGHT_SCORE);
		//成就数据采集 
		AchieveCollect.inviteUser(veteran);
		return null;
	}

	/** 帮助完成任务 */
	public void finishTask(Player recruit,int taskType)
	{
		Player veteran=objfactory.getPlayerCache().load(
			recruit.getComrade().getVeteranId()+"");
		if(veteran==null) return;
		long value=0;
		if(taskType==ComradeTask.RECHARGE)
		{
			String charge=recruit.getAttributes(PublicConst.PLAYER_COMRADE_CHARGE);
			if(charge!=null && charge.length()!=0)
				value=TextKit.parseInt(charge);
		}
		else if(taskType==ComradeTask.LEVEL)
		{
			value=recruit.getLevel();
		}
		else if(taskType==ComradeTask.FIGHT_SCORE)
		{
			value=recruit.getFightScore();
		}
		Sample[] tasks=ComradeTask.factory.getSamples();
		Comrade comrade=recruit.getComrade();
		for(int i=0;i<tasks.length;i++)
		{
			if(tasks[i]==null) continue;
			ComradeTask task=(ComradeTask)tasks[i];
			if(task.getTaskType()!=taskType) continue;
			if(!checkTask(comrade,task,(int)value)) 
				continue;
			markTask(comrade,task,veteran);
		}
	}
	/** 检测新兵能否完成该任务 */
	public boolean checkTask(Comrade comrade,ComradeTask task,int cvalue)
	{
		if(task.getTaskCondition()>cvalue) return false;
		int[] taskmark=comrade.getTaskMark();
		int key=task.getKey();
		int value=task.getValue();
		if(taskmark.length==0) 
		{
			taskmark=new int[key+1];
			taskmark[key]=taskmark[key]|(1<<value);// 新兵记录
			return true;
		}
		else
		{
			return (taskmark[key]&(1<<value))==0;
		}
	}
	/** 记录完成任务 */
	public void markTask(Comrade comrade,ComradeTask task,Player veteran)
	{
		int[] taskmark=comrade.getTaskMark();
		int key=task.getKey();
		int value=task.getValue();
		if(key>=taskmark.length)
		{
			int[] temp=new int[key+1];
			System.arraycopy(taskmark,0,temp,0,taskmark.length);
			taskmark=temp;
			comrade.setTaskMark(taskmark);
		}
		taskmark[key]=taskmark[key]|(1<<value);// 新兵记录
		incrComplete(task,veteran);// 老兵记录
	}
	/** 增加任务完成次数 */
	public void incrComplete(ComradeTask task,Player veteran)
	{
		IntKeyHashMap map=veteran.getComrade().getAwardMap();
		AwardMark mark=(AwardMark)map.get(task.getSid());
		if(mark==null)
		{
			mark=new AwardMark();
			mark.setSid(task.getSid());
			mark.setComplete(1);
			map.put(mark.getSid(),mark);
		}
		else
		{
			if(task.getComplete()<=mark.getComplete()) return;
			mark.setComplete(mark.getComplete()+1);
		}
		JBackKit.sendComardeInfo(veteran);
	}
	/** 获取任务已完成次数 */
	public int getComplete(int taskSid,Player veteran)
	{
		IntKeyHashMap map=veteran.getComrade().getAwardMap();
		AwardMark mark=(AwardMark)map.get(taskSid);
		if(mark==null) return 0;
		return mark.getComplete();
	}
	/** 获取任务已领奖次数 */
	public int getGot(int taskSid,Player veteran)
	{
		IntKeyHashMap map=veteran.getComrade().getAwardMap();
		AwardMark mark=(AwardMark)map.get(taskSid);
		if(mark==null) return 0;
		return mark.getGot();
	}
	
	/** 领取奖励 */
	public void getAward(ByteBuffer data,Player veteran)
	{
		int taskSid=data.readUnsignedShort();
		int complete=getComplete(taskSid,veteran);
		int got=getGot(taskSid,veteran);
		if(complete<=got)
		{
			throw new DataAccessException(0,"not finish task");// 未完成该任务
		}
		ComradeTask task=(ComradeTask)ComradeTask.factory.getSample(taskSid);
		if(task==null)
		{
			throw new DataAccessException(0,"no task");// 无该任务
		}
		if(got>=task.getComplete())
		{
			throw new DataAccessException(0,"got award");// 该奖励已领完
		}
		incrGot(task,veteran);
		data.clear();
		data.writeShort(1);
		data.writeByte(task.getTaskType());
		data.writeByte(1);
		data.writeInt(task.getSid());
		int go=getComplete(task.getSid(),veteran);
		got=getGot(taskSid,veteran);
		data.writeShort(got);//已领取次数
		data.writeShort(task.getComplete());//最大可领取次数
		data.writeByte(go>got?1:0);//0--不可领取，1-－可领取
		data.writeUTF(task.getTaskCondition()+"");//领奖条件描述
		Award award=(Award)Award.factory.getSample(task
			.getAwardSid());
		award.viewAward(data,veteran);
		award.awardLenth(data,veteran,objfactory,null,
			new int[]{EquipmentTrack.FROM_COMRADE});

	}
	
	/** 记录领奖  */
	public void incrGot(ComradeTask task,Player veteran)
	{
		IntKeyHashMap map=veteran.getComrade().getAwardMap();
		AwardMark mark=(AwardMark)map.get(task.getSid());
		if(mark==null)
		{
			mark=new AwardMark();
			mark.setGot(1);
			map.put(task.getSid(),mark);
		}
		else
		{
			mark.setGot(mark.getGot()+1);
		}
		JBackKit.sendComardeInfo(veteran);
	}
	
	/** 获取战友列表 */
	public void getComrades(ByteBuffer data,Player player,int checkTime)
	{
		IntList pids=player.getComrade().getRecruitIds();
		player.getComrade().setOnline(Comrade.ON_LINE);
		flushOnLine(player,Comrade.ON_LINE);
		if(pids==null || pids.size()==0)
		{
			data.writeShort(0);
			return ;
		}
		data.writeShort(pids.size());
		Alliance alliance;
		for(int i=0;i<pids.size();i++)
		{
			int id=pids.get(i);
			Player recruit=objfactory.getPlayerById(id);
			data.writeInt(id);
			data.writeShort(recruit.getSid());
			data.writeUTF(recruit.getName());
			data.writeInt(recruit.getAttrHead());
			data.writeInt(recruit.getAttrHeadBorder());
			data.writeByte(recruit.getLevel());
			//军衔
			data.writeByte(recruit.getPlayerType());
			//战力
			data.writeInt(recruit.getFightScore());
			int out=recruit.getExitTime();
			//离线时间
			data.writeInt(recruit.getComrade().isOnline()?0:(checkTime-out)+1);
			String aname="";
			String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(aid!=null)
			{
				alliance=objfactory.getAlliance(TextKit.parseInt(aid),false);
				if(alliance!=null) aname=alliance.getName();
			}
			//联盟名称
			data.writeUTF(aname);
		}
		finishTask(player,ComradeTask.LEVEL);
		finishTask(player,ComradeTask.RECHARGE);
		finishTask(player,ComradeTask.FIGHT_SCORE);
	}
	
	/**检测是否序列化**/
	public boolean  checkPlayerList(Player player)
	{
		IntList pids=player.getComrade().getRecruitIds();
		if(pids==null || pids.size()==0) return false;
		return true;
	}
	
	/**获取当前玩家的任务列表
	 *当前的任务列表根据种类区分 分开发 
	 */
	public void showBytesWriteCombradeTasks(Player recruit,ByteBuffer data)
	{
		data.writeShort(ComradeTask.COMRADETASK_TYPE);
		Sample[] tasks=ComradeTask.factory.getSamples();
		/** 根据任务种类序列化任务 **/
		for(int j=1;j<ComradeTask.COMRADETASK_TYPE+1;j++)
		{
			// 任务类型
			data.writeByte(j);
			int top=data.top();
			// 任务数量
			data.writeByte(0);
			int count=0;
			for(int i=0;i<tasks.length;i++)
			{
				if(tasks[i]==null) continue;
				ComradeTask task=(ComradeTask)tasks[i];
				if(j!=task.getTaskType()) continue;
				data.writeInt(task.getSid());
				int go=getComplete(task.getSid(),recruit);
				int got=getGot(task.getSid(),recruit);
				data.writeShort(got);//已领取次数
				data.writeShort(task.getComplete());//最大可领取次数
				data.writeByte(go>got?1:0);//0--不可领取，1-－可领取
				data.writeUTF(task.getTaskCondition()+"");//领奖条件描述
				Award award=(Award)Award.factory.getSample(task
					.getAwardSid());
				award.viewAward(data,recruit);
				count++;
			}
			if(count!=0)
			{
				int nowtop=data.top();
				data.setTop(top);
				data.writeByte(count);
				data.setTop(nowtop);
			}
		}
	}
	
	/**登录就刷新**/
	public void flushOnLine(Player player,boolean state)
	{
		Player veteran=getVertern(player);
		if(veteran!=null)
		{
			Session session=(Session)veteran.getSource();
			if(session!=null)
			{
				JBackKit.sendComradeFriendState(veteran,player,state,
					objfactory,session.getConnect());
			}
		}
	}
	
	
	/**根据类型获取当前的值**/
	public  long  getTaskValue(ComradeTask task,Player recruit,int type)
	{
		if(type==ComradeTask.RECHARGE)
		{
			return recruit.getResources()[Resources.MAXGEMS];
		}
		else if(type==ComradeTask.LEVEL)
		{
			return recruit.getLevel();
		}
		else if(type==ComradeTask.FIGHT_SCORE)
		{
			return recruit.getFightScore();
		}
		return 0;
	}
	
	/**获取玩家的被招募对象**/
	public Player getVertern(Player player)
	{
		int veteranId=player.getComrade().getVeteranId();
		if(veteranId==0) return null;
		Player veteran=objfactory.getPlayerById(veteranId);
		if(veteran!=null) return veteran;
		return null;
	}
}
