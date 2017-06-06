package foxu.sea.task;

import mustang.back.BackKit;
import mustang.io.ByteBuffer;
import mustang.set.ObjectArray;
import mustang.util.Sample;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.BuildInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.TaskPort;
import foxu.sea.task.condition.BindAccoutCondition;
import foxu.sea.task.condition.BuildShipCondition;

/**
 * 任务管理器 author:icetiger
 */
public class TaskManager
{

	public static final int INT_SIZE=33;
	/** 活跃值奖励 */
	public static int[] vitalityAward={};
	/** 任务标记 记录除引导任务外的所有任务标记 */
	int[] tasksMark=new int[1];
	/** 当前正在进行的任务 */
	ObjectArray tasks=new ObjectArray();
	/** 每日任务标记 日常任务 有默认日常任务的sid配置 */
	int daytasksMark[][]={};
	/** 每日活跃度任务 */
	int vitalityMark[][]={};
	// 动态 拥有玩家
	Player player;

	/**
	 * 根据任务的键和值判断这个任务是否已完成过
	 * 
	 * @return 返回true表示完成过
	 */
	public boolean taskIsFinish(int key,int value)
	{
		if(key>=tasksMark.length) return false;
		return (tasksMark[key]&value)!=0;
	}
	/** 根据sid获得一个任务 */
	public Task getTaskBySid(int taskSid)
	{
		Object[] tasks=this.tasks.toArray();
		for(int i=0;i<tasks.length;i++)
		{
			Task task=(Task)tasks[i];
			if(task.getSid()==taskSid) return task;
		}
		return null;
	}

	/** 检查事件是否得到当前任务条件 event为空 不需要事件推 一般是拥有任务 */
	public void checkTastEvent(TaskEvent event)
	{
		Object[] tasks=this.tasks.toArray();
		for(int i=0;i<tasks.length;i++)
		{
			Task task=(Task)tasks[i];
			// 每日任务限制次数达到了就跳过
			if((task.getTaskType()&Task.TASK_DAY)!=0
				&&getDayTaskDateTime(task.getKey(),task.getValue())>=task
					.getDayLimiteTime()) continue;
			if((task.getTaskType()&Task.TASK_VITALITY)!=0
				&&getVitalityTaskDateTime(task.getKey(),task.getValue())>=task
					.getDayLimiteTime()) continue;
			int checkState=task.checkCondition(event,player);
			// 任务完成了
			if(checkState==Task.TASK_FINISH)
			{
				// 记录任务为完成
				task.setFinish(true);
				//活跃度任务结算
				if((task.getTaskType()&Task.TASK_VITALITY)!=0)
				{
					reportTask(task.getSid());
					JBackKit.sendVitalityTask(
						player,
						task.getSid(),
						getVitalityTaskDateTime(task.getKey(),
							task.getValue()),
						getVitalityTaskDateTime(task.getKey(),
							task.getValue())>=task.getDayLimiteTime());
				}
				// 发送消息给前台表示任务改变了
				JBackKit.sendChangeTaskPlayer(player,task);
			}
			else if(checkState==Task.TASK_CHANGE)
			{
				// 发送消息给前台表示任务改变了
				JBackKit.sendChangeTaskPlayer(player,task);
			}
		}
	}

	/** 检查某个任务是否已经完成 主要用于添加新任务的时候检查 */
	public void checkOneTask(Task task)
	{
		TaskEvent event=null;
		if(task.getCondition()!=null
			&&task.getCondition()[0] instanceof BuildShipCondition
			&&((BuildShipCondition)task.getCondition()[0]).getType()==BuildShipCondition.OWNED_SHIP)
		{
			// 事件通知任务
			event=new TaskEvent();
			event.setEventType(PublicConst.SHIP_PRODUCE_TASK_EVENT);
			event.setSource("");
			event.setParam(BackKit.getContext().get("creatObjectFactory"));
		}
		else if(task.getCondition()!=null
			&&task.getCondition()[0] instanceof BindAccoutCondition)
		{
			// 事件通知任务
			event=new TaskEvent();
			event.setEventType(PublicConst.BIND_ACCOUNT_EVENT);
			event.setSource("");
			event.setParam(BackKit.getContext().get("creatObjectFactory"));
		}
		int checkState=task.checkCondition(event,player);
		// 任务完成了
		if(checkState==Task.TASK_FINISH)
		{
			// 记录任务为完成
			task.setFinish(true);
		}
	}

	/** 完成一个一次性任务,保存任务标记.参数数据保存在任务对象上 */
	public void finishOnceTask(int key,int value)
	{
		if(taskIsFinish(key,value)) return;
		if(key>=tasksMark.length)
		{
			int[] temp=new int[key+1];
			System.arraycopy(tasksMark,0,temp,0,tasksMark.length);
			tasksMark=temp;
		}
		tasksMark[key]|=value;
	}

	/** 是否在当前任务中已经有了此任务 */
	public boolean isHaveTask(int taskSid)
	{
		Object object[]=tasks.toArray();
		for(int i=0;i<object.length;i++)
		{
			Task task=(Task)object[i];
			if(task.getSid()==taskSid)
			{
				return true;
			}
		}
		return false;
	}

	/** 获得次数所在位的值 */
	public int getTimeIndex(int value)
	{
		int t=value;
		int count=1;
		// 验证value二进制是否2的N次方,及获得日期所在位
		for(int i=0;i<INT_SIZE;i++)
		{
			if(value<0)
			{
				count=32;
				break;
			}
			// value error
			if(t%2==1)
			{
				if(t==1)
					break;
				else
					return 0;
			}
			t>>=1;
			count++;
		}
		return count;
	}

	/** 获得某个日常任务今天做了多少次 */
	public int getDayTaskDateTime(int key,int value)
	{
		return getTaskDateTime(key,value,daytasksMark);
	}
	public int getVitalityTaskDateTime(int key,int value){
		return getTaskDateTime(key,value,vitalityMark);
	}
	private int getTaskDateTime(int key,int value,int[][] marks){
		if(key<0) return 0;
		if(key>=marks.length) return 0;
		int count=getTimeIndex(value);
		int dayOfYear=SeaBackKit.getDayOfYear();
		// index0是天数 index1是次数
		int dayAndTimes[]=SeaBackKit
			.get2ShortInInt(marks[key][count]);
		//
		if(dayAndTimes[0]!=dayOfYear)
		{
			dayAndTimes[0]=dayOfYear;
			dayAndTimes[1]=0;
			marks[key][count]=SeaBackKit.put2ShortInInt(
				dayAndTimes[0],dayAndTimes[1]);
			return 0;
		}
		return dayAndTimes[1];
	}

	/** 重置每日任务 */
	public void resetDayTask(int key,int value)
	{
		// 这一年的第几天
		int dayOfYear=SeaBackKit.getDayOfYear();
		int count=getTimeIndex(value);
		// index0是天数 index1是次数
		int dayAndTimes[]=new int[2];
		dayAndTimes[0]=dayOfYear;
		dayAndTimes[1]=0;
		daytasksMark[key][count]=SeaBackKit.put2ShortInInt(dayAndTimes[0],
			dayAndTimes[1]);
		String time=player.getAttributes(PublicConst.REST_DAY_TASK);
		int dayResetTime;
		if(time==null||time.equals(""))
		{
			dayResetTime=SeaBackKit.put2ShortInInt(dayAndTimes[0],1);
			player.setAttribute(PublicConst.REST_DAY_TASK,dayResetTime+"");
		}
		else
		{
			dayResetTime=Integer.parseInt(time);
			// index0是天数 index1是次数
			dayAndTimes=SeaBackKit.get2ShortInInt(dayResetTime);
			if(dayAndTimes[0]!=dayOfYear)
			{
				dayAndTimes[0]=dayOfYear;
				dayAndTimes[1]=1;
			}
			else
			{
				dayAndTimes[1]++;
			}
			player.setAttribute(PublicConst.REST_DAY_TASK,SeaBackKit
				.put2ShortInInt(dayAndTimes[0],dayAndTimes[1])
				+"");
		}
	}

	/** 记录一个日常任务 新加或者是添加次数 */
	public boolean recodeDayTask(int key,int value)
	{
		boolean isExist=checkDayTask(key,value);
		boolean isComplete=recodeTask(key,value,daytasksMark,isExist);
		if(isComplete) return isComplete;
		// 还没有记录这个日常任务
		int count=getTimeIndex(value);
		if(key>=daytasksMark.length)
		{
			int[][] temp=new int[key+1][INT_SIZE];
			System.arraycopy(daytasksMark,0,temp,0,daytasksMark.length);
			daytasksMark=temp;
		}
		daytasksMark[key][0]|=value;
		// 左 天数 右次数 新加就记1
		int dayOfYear=SeaBackKit.getDayOfYear();
		int time=1;
		daytasksMark[key][count]=SeaBackKit.put2ShortInInt(dayOfYear,time);
		return true;
	}
	public boolean recodeVitalityTask(int key,int value)
	{
		boolean isExist=checkVitalityTask(key,value);
		boolean isComplete=recodeTask(key,value,vitalityMark,isExist);
		if(isComplete) return isComplete;
		// 还没有记录这个日常任务
		int count=getTimeIndex(value);
		if(key>=vitalityMark.length)
		{
			int[][] temp=new int[key+1][INT_SIZE];
			System.arraycopy(vitalityMark,0,temp,0,vitalityMark.length);
			vitalityMark=temp;
		}
		vitalityMark[key][0]|=value;
		// 左 天数 右次数 新加就记1
		int dayOfYear=SeaBackKit.getDayOfYear();
		int time=1;
		vitalityMark[key][count]=SeaBackKit.put2ShortInInt(dayOfYear,time);
		return true;
	}
	private boolean recodeTask(int key,int value,int[][] marks,boolean isExist)
	{
		// 已经记录了这个任务了
		if(isExist)
		{
			// 这一年的第几天
			int dayOfYear=SeaBackKit.getDayOfYear();
			int count=getTimeIndex(value);
			// index0是天数 index1是次数
			int dayAndTimes[]=SeaBackKit.get2ShortInInt(marks[key][count]);
			if(dayAndTimes[0]!=dayOfYear)
			{
				dayAndTimes[0]=dayOfYear;
				dayAndTimes[1]=1;
				marks[key][count]=SeaBackKit.put2ShortInInt(dayAndTimes[0],
					dayAndTimes[1]);
				return true;
			}
			// 次数加1
			dayAndTimes[1]++;
			marks[key][count]=SeaBackKit.put2ShortInInt(dayAndTimes[0],
				dayAndTimes[1]);
			return true;
		}
		return false;
	}

	/** 移除一个每日任务 */
	public void removeDayTask(int key,int value)
	{
		if(checkDayTask(key,value))
		{
			int count=getTimeIndex(value);
			daytasksMark[key][0]&=(~value);
			daytasksMark[key][count]=0;
		}
	}

	/**
	 * 检查是否已经拥有该日常任务
	 * 
	 * @return 返回true表示已拥有
	 */
	public boolean checkDayTask(int key,int value)
	{
		return checkTask(key,value,daytasksMark);
	}
	public boolean checkVitalityTask(int key,int value){
		return checkTask(key,value,vitalityMark);
	}
	private boolean checkTask(int key,int value,int[][] marks){
		if(key>=marks.length) return false;
		return (marks[key][0]&value)!=0;
	}

	/** 序列化每天任务 */
	public void bytesWriteDayTasks(ByteBuffer data)
	{
		writeOtherTasks(data,daytasksMark);
	}
	
	/** 序列化活跃度任务 */
	public void bytesWriteVitalityTasks(ByteBuffer data)
	{
		//创建新玩家时没有标记，先进行设置
		if(player.getAttributes(PublicConst.DATE_VITALITY_STORE)==null)
			player.setAttribute(PublicConst.DATE_VITALITY_STORE,"T");
		writeOtherTasks(data,vitalityMark);
	}
	
	public void writeOtherTasks(ByteBuffer data,int[][] tasks)
	{
		if(tasks.length<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(tasks.length);
			for(int i=0;i<tasks.length;i++)
			{
				for(int j=0;j<INT_SIZE;j++)
				{
					data.writeInt(tasks[i][j]);
				}
			}
		}
	}
	/** 序列化每天任务 */
	public void bytesReadDayTasks(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		daytasksMark=new int[length][INT_SIZE];
		readOtherTasks(data,daytasksMark);
	}
	
	/** 序列化活跃度任务 */
	public void bytesReadVitalityTasks(ByteBuffer data)
	{
		//如果有标记，表示已存储过，则进行读取
		if(player.getAttributes(PublicConst.DATE_VITALITY_STORE)!=null){
			int length=data.readUnsignedByte();
			vitalityMark=new int[length][INT_SIZE];
			readOtherTasks(data,vitalityMark);
		}
		//没有标记，则先进行设置
		else
			player.setAttribute(PublicConst.DATE_VITALITY_STORE,"T");
	}

	public void readOtherTasks(ByteBuffer data,int[][] tasks){
		for(int i=0;i<tasks.length;i++)
		{
			for(int j=0;j<INT_SIZE;j++)
			{
				tasks[i][j]=data.readInt();
			}
		}
	}

	/** 添加一个任务,返回是否成功 */
	public boolean addTask(Task task)
	{
		// 是否在当前任务中
		if(isHaveTask(task.getSid())) return false;
		// 是否已经完成
		if((task.getTaskType()&Task.TASK_VITALITY)==0
			&&(task.getTaskType()&Task.TASK_DAY)==0
			&&taskIsFinish(task.getKey(),task.getValue())) return false;
		tasks.add(task);
		return true;
	}

	/** 清除一个已经完成的任务标记 */
	public void clearTaskMark(int key,int value)
	{
		if(taskIsFinish(key,value)) tasksMark[key]&=(~value);
	}

	/** 检查并推送任务 */
	public void pushNextTask()
	{
		Sample[] taskSamples=TaskEventExecute.getInstance().getTaskSamples();
		for(int i=0;i<taskSamples.length;i++)
		{
			if(taskSamples[i]==null) continue;
			if(taskSamples[i].getSid()==1500)continue;//非gnetop屏蔽
			boolean bool=true;
			Task task=null;
			if(taskSamples[i] instanceof Task)
			{
				task=(Task)taskSamples[i];
				if(!task.autoAdd) continue;
				// 是否完成新手引导
				String newFollowPlayer=player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER);
				if((!task.isOpenNew())&&newFollowPlayer!=null)
				{
					bool=false;
				}
				// 等级条件 玩家等级
				if(task.getLimitLevel()!=0
					&&player.getLevel()<task.getLimitLevel())
				{
					bool=false;
				}
				// 等级条件 指挥中心
				if(task.getCommandCenter()!=0
					&&player.getIsland()
						.getBuildByIndex(BuildInfo.INDEX_0,null)
						.getBuildLevel()<task.getCommandCenter())
				{
					bool=false;
				}
				// 前置任务判断
				if(task.getLimiteKey()!=null
					&&task.getLimiteShowValue()!=null)
				{
					for(int j=0;j<task.getLimiteKey().length;j++)
					{
						int limiteValue=1<<(task.getLimiteShowValue()[j]-1);
						if(!taskIsFinish(task.getLimiteKey()[j],limiteValue))
						{
							bool=false;
						}
					}
				}
			}
			if(bool&&task!=null)
			{
				Task addTask=(Task)task.clone();
				boolean add=addTask(addTask);
				// 推送到前台
				if(add)
				{
					// 每日组合任务 新加的时候 随机5个任务sid
					if(addTask instanceof CombinationTask)
					{
						if(((CombinationTask)addTask).getCurrentTask()==null)
							((CombinationTask)addTask).randomSids();
					}
					checkOneTask(addTask);
					JBackKit.sendAddTaskPlayer(player,addTask);
				}
			}
		}
	}

	/** 回报任务 */
	public boolean reportTask(int taskSid)
	{
		Object[] tasks=this.tasks.toArray();
		for(int i=0;i<tasks.length;i++)
		{
			Task task=(Task)tasks[i];
			if(task.getSid()==taskSid&&task.isFinish())
			{
				// 普通任务
				if((task.getTaskType()&Task.TASK_NOMARL)!=0)
				{
					finishOnceTask(task.getKey(),task.getValue());
					this.tasks.remove(task);
				}
				else if((task.getTaskType()&Task.TASK_VITALITY)!=0){
					recodeVitalityTask(task.getKey(),task.getValue());
				}
				// 每日任务 完成后不用删除
				else if((task.getTaskType()&Task.TASK_DAY)!=0)
				{
					if(task.getSid()!=TaskPort.HONOR_TASK)
						TaskEventExecute.getInstance().executeEvent(
							PublicConst.TASK_DAY_TASK_EVENT,this,player,"");
					task.setFinish(false);
					recodeDayTask(task.getKey(),task.getValue());
					//日常任务累计
					TaskEventExecute.getInstance().executeEvent(
						PublicConst.DAY_TASK_COUNT_EVENT,task,player,null);
				}
				task.sendAward(player);
				pushNextTask();
				return true;
			}
		}
		return false;
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadTasks(ByteBuffer data)
	{
		int n=data.readUnsignedByte();
		if(n==0) return this;
		Object[] temp=new Object[n];
		for(int i=0;i<n;i++)
		{
			temp[i]=Task.bytesReadTask(data);
		}
		tasks=new ObjectArray(temp);
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteTasks(ByteBuffer data)
	{
		if(tasks!=null&&tasks.size()>0)
		{
			Object[] array=tasks.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Task)array[i]).bytesWrite(data);
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesReadTaskMark(ByteBuffer data)
	{
		int length=data.readUnsignedShort();
		tasksMark=new int[length];
		for(int i=0;i<length;i++)
		{
			tasksMark[i]=data.readInt();
		}
		return this;
	}

	/** 将域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWriteTaskMark(ByteBuffer data)
	{
		data.writeShort(tasksMark.length);
		for(int i=0;i<tasksMark.length;i++)
		{
			data.writeInt(tasksMark[i]);
		}
	}

	/** 从字节缓存中反序列化得到一个对象 */
	public Object bytesRead(ByteBuffer data)
	{
		bytesReadTaskMark(data);
		bytesReadDayTasks(data);
		bytesReadVitalityTasks(data);
		bytesReadTasks(data);
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		bytesWriteTaskMark(data);
		bytesWriteDayTasks(data);
		bytesWriteVitalityTasks(data);
		bytesWriteTasks(data);
	}
	public void showBytesWrite(ByteBuffer data,int time)
	{
		if(tasks!=null&&tasks.size()>0)
		{
			Object[] array=tasks.getArray();
			data.writeByte(array.length);
			for(int i=0;i<array.length;i++)
			{
				((Task)array[i]).showBytesWrite(data,player);
			}
		}
		else
		{
			data.writeByte(0);
		}
		Object tasks[]=this.tasks.getArray();
		int num=0;
		int vitalityNum=0;
		for(int i=0;i<tasks.length;i++)
		{
			Task task=(Task)tasks[i];
			if((task.getTaskType()&Task.TASK_DAY)!=0)
			{
				num++;
			}else if((task.getTaskType()&Task.TASK_VITALITY)!=0){
				vitalityNum++;
			}
		}
		// 每日任务
		if(num>0)
		{
			data.writeByte(num);
			for(int i=0;i<tasks.length;i++)
			{
				Task task=(Task)tasks[i];
				if((task.getTaskType()&Task.TASK_DAY)!=0)
				{
					data.writeShort(task.getSid());
					data.writeByte(getDayTaskDateTime(task.getKey(),task
						.getValue()));
				}
			}
		}
		else
		{
			data.writeByte(0);
		}
		// 活跃度任务
		if(vitalityNum>0)
		{
			data.writeByte(vitalityNum);
			for(int i=0;i<tasks.length;i++)
			{
				Task task=(Task)tasks[i];
				if((task.getTaskType()&Task.TASK_VITALITY)!=0)
				{
					data.writeShort(task.getSid());
					int count=getVitalityTaskDateTime(task.getKey(),
						task.getValue());
					data.writeByte(count);
					data.writeBoolean(count>=task.getDayLimiteTime());
				}
			}
		}
		else
		{
			data.writeByte(0);
		}
	}

	/**
	 * @return player
	 */
	public Player getPlayer()
	{
		return player;
	}

	/**
	 * @param player 要设置的 player
	 */
	public void setPlayer(Player player)
	{
		this.player=player;
	}

	/**
	 * @return daytasksMark
	 */
	public int[][] getDaytasksMark()
	{
		return daytasksMark;
	}

	/**
	 * @param daytasksMark 要设置的 daytasksMark
	 */
	public void setDaytasksMark(int[][] daytasksMark)
	{
		this.daytasksMark=daytasksMark;
	}

	/**
	 * @return tasks
	 */
	public ObjectArray getTasks()
	{
		return tasks;
	}

	/**
	 * @param tasks 要设置的 tasks
	 */
	public void setTasks(ObjectArray tasks)
	{
		this.tasks=tasks;
	}

	/**
	 * @return tasksMark
	 */
	public int[] getTasksMark()
	{
		return tasksMark;
	}

	/**
	 * @param tasksMark 要设置的 tasksMark
	 */
	public void setTasksMark(int[] tasksMark)
	{
		this.tasksMark=tasksMark;
	}
}
