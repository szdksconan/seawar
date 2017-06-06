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
 * ��������� author:icetiger
 */
public class TaskManager
{

	public static final int INT_SIZE=33;
	/** ��Ծֵ���� */
	public static int[] vitalityAward={};
	/** ������ ��¼����������������������� */
	int[] tasksMark=new int[1];
	/** ��ǰ���ڽ��е����� */
	ObjectArray tasks=new ObjectArray();
	/** ÿ�������� �ճ����� ��Ĭ���ճ������sid���� */
	int daytasksMark[][]={};
	/** ÿ�ջ�Ծ������ */
	int vitalityMark[][]={};
	// ��̬ ӵ�����
	Player player;

	/**
	 * ��������ļ���ֵ�ж���������Ƿ�����ɹ�
	 * 
	 * @return ����true��ʾ��ɹ�
	 */
	public boolean taskIsFinish(int key,int value)
	{
		if(key>=tasksMark.length) return false;
		return (tasksMark[key]&value)!=0;
	}
	/** ����sid���һ������ */
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

	/** ����¼��Ƿ�õ���ǰ�������� eventΪ�� ����Ҫ�¼��� һ����ӵ������ */
	public void checkTastEvent(TaskEvent event)
	{
		Object[] tasks=this.tasks.toArray();
		for(int i=0;i<tasks.length;i++)
		{
			Task task=(Task)tasks[i];
			// ÿ���������ƴ����ﵽ�˾�����
			if((task.getTaskType()&Task.TASK_DAY)!=0
				&&getDayTaskDateTime(task.getKey(),task.getValue())>=task
					.getDayLimiteTime()) continue;
			if((task.getTaskType()&Task.TASK_VITALITY)!=0
				&&getVitalityTaskDateTime(task.getKey(),task.getValue())>=task
					.getDayLimiteTime()) continue;
			int checkState=task.checkCondition(event,player);
			// ���������
			if(checkState==Task.TASK_FINISH)
			{
				// ��¼����Ϊ���
				task.setFinish(true);
				//��Ծ���������
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
				// ������Ϣ��ǰ̨��ʾ����ı���
				JBackKit.sendChangeTaskPlayer(player,task);
			}
			else if(checkState==Task.TASK_CHANGE)
			{
				// ������Ϣ��ǰ̨��ʾ����ı���
				JBackKit.sendChangeTaskPlayer(player,task);
			}
		}
	}

	/** ���ĳ�������Ƿ��Ѿ���� ��Ҫ��������������ʱ���� */
	public void checkOneTask(Task task)
	{
		TaskEvent event=null;
		if(task.getCondition()!=null
			&&task.getCondition()[0] instanceof BuildShipCondition
			&&((BuildShipCondition)task.getCondition()[0]).getType()==BuildShipCondition.OWNED_SHIP)
		{
			// �¼�֪ͨ����
			event=new TaskEvent();
			event.setEventType(PublicConst.SHIP_PRODUCE_TASK_EVENT);
			event.setSource("");
			event.setParam(BackKit.getContext().get("creatObjectFactory"));
		}
		else if(task.getCondition()!=null
			&&task.getCondition()[0] instanceof BindAccoutCondition)
		{
			// �¼�֪ͨ����
			event=new TaskEvent();
			event.setEventType(PublicConst.BIND_ACCOUNT_EVENT);
			event.setSource("");
			event.setParam(BackKit.getContext().get("creatObjectFactory"));
		}
		int checkState=task.checkCondition(event,player);
		// ���������
		if(checkState==Task.TASK_FINISH)
		{
			// ��¼����Ϊ���
			task.setFinish(true);
		}
	}

	/** ���һ��һ��������,����������.�������ݱ�������������� */
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

	/** �Ƿ��ڵ�ǰ�������Ѿ����˴����� */
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

	/** ��ô�������λ��ֵ */
	public int getTimeIndex(int value)
	{
		int t=value;
		int count=1;
		// ��֤value�������Ƿ�2��N�η�,�������������λ
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

	/** ���ĳ���ճ�����������˶��ٴ� */
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
		// index0������ index1�Ǵ���
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

	/** ����ÿ������ */
	public void resetDayTask(int key,int value)
	{
		// ��һ��ĵڼ���
		int dayOfYear=SeaBackKit.getDayOfYear();
		int count=getTimeIndex(value);
		// index0������ index1�Ǵ���
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
			// index0������ index1�Ǵ���
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

	/** ��¼һ���ճ����� �¼ӻ�������Ӵ��� */
	public boolean recodeDayTask(int key,int value)
	{
		boolean isExist=checkDayTask(key,value);
		boolean isComplete=recodeTask(key,value,daytasksMark,isExist);
		if(isComplete) return isComplete;
		// ��û�м�¼����ճ�����
		int count=getTimeIndex(value);
		if(key>=daytasksMark.length)
		{
			int[][] temp=new int[key+1][INT_SIZE];
			System.arraycopy(daytasksMark,0,temp,0,daytasksMark.length);
			daytasksMark=temp;
		}
		daytasksMark[key][0]|=value;
		// �� ���� �Ҵ��� �¼Ӿͼ�1
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
		// ��û�м�¼����ճ�����
		int count=getTimeIndex(value);
		if(key>=vitalityMark.length)
		{
			int[][] temp=new int[key+1][INT_SIZE];
			System.arraycopy(vitalityMark,0,temp,0,vitalityMark.length);
			vitalityMark=temp;
		}
		vitalityMark[key][0]|=value;
		// �� ���� �Ҵ��� �¼Ӿͼ�1
		int dayOfYear=SeaBackKit.getDayOfYear();
		int time=1;
		vitalityMark[key][count]=SeaBackKit.put2ShortInInt(dayOfYear,time);
		return true;
	}
	private boolean recodeTask(int key,int value,int[][] marks,boolean isExist)
	{
		// �Ѿ���¼�����������
		if(isExist)
		{
			// ��һ��ĵڼ���
			int dayOfYear=SeaBackKit.getDayOfYear();
			int count=getTimeIndex(value);
			// index0������ index1�Ǵ���
			int dayAndTimes[]=SeaBackKit.get2ShortInInt(marks[key][count]);
			if(dayAndTimes[0]!=dayOfYear)
			{
				dayAndTimes[0]=dayOfYear;
				dayAndTimes[1]=1;
				marks[key][count]=SeaBackKit.put2ShortInInt(dayAndTimes[0],
					dayAndTimes[1]);
				return true;
			}
			// ������1
			dayAndTimes[1]++;
			marks[key][count]=SeaBackKit.put2ShortInInt(dayAndTimes[0],
				dayAndTimes[1]);
			return true;
		}
		return false;
	}

	/** �Ƴ�һ��ÿ������ */
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
	 * ����Ƿ��Ѿ�ӵ�и��ճ�����
	 * 
	 * @return ����true��ʾ��ӵ��
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

	/** ���л�ÿ������ */
	public void bytesWriteDayTasks(ByteBuffer data)
	{
		writeOtherTasks(data,daytasksMark);
	}
	
	/** ���л���Ծ������ */
	public void bytesWriteVitalityTasks(ByteBuffer data)
	{
		//���������ʱû�б�ǣ��Ƚ�������
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
	/** ���л�ÿ������ */
	public void bytesReadDayTasks(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		daytasksMark=new int[length][INT_SIZE];
		readOtherTasks(data,daytasksMark);
	}
	
	/** ���л���Ծ������ */
	public void bytesReadVitalityTasks(ByteBuffer data)
	{
		//����б�ǣ���ʾ�Ѵ洢��������ж�ȡ
		if(player.getAttributes(PublicConst.DATE_VITALITY_STORE)!=null){
			int length=data.readUnsignedByte();
			vitalityMark=new int[length][INT_SIZE];
			readOtherTasks(data,vitalityMark);
		}
		//û�б�ǣ����Ƚ�������
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

	/** ���һ������,�����Ƿ�ɹ� */
	public boolean addTask(Task task)
	{
		// �Ƿ��ڵ�ǰ������
		if(isHaveTask(task.getSid())) return false;
		// �Ƿ��Ѿ����
		if((task.getTaskType()&Task.TASK_VITALITY)==0
			&&(task.getTaskType()&Task.TASK_DAY)==0
			&&taskIsFinish(task.getKey(),task.getValue())) return false;
		tasks.add(task);
		return true;
	}

	/** ���һ���Ѿ���ɵ������� */
	public void clearTaskMark(int key,int value)
	{
		if(taskIsFinish(key,value)) tasksMark[key]&=(~value);
	}

	/** ��鲢�������� */
	public void pushNextTask()
	{
		Sample[] taskSamples=TaskEventExecute.getInstance().getTaskSamples();
		for(int i=0;i<taskSamples.length;i++)
		{
			if(taskSamples[i]==null) continue;
			if(taskSamples[i].getSid()==1500)continue;//��gnetop����
			boolean bool=true;
			Task task=null;
			if(taskSamples[i] instanceof Task)
			{
				task=(Task)taskSamples[i];
				if(!task.autoAdd) continue;
				// �Ƿ������������
				String newFollowPlayer=player.getAttributes(PublicConst.NEW_FOLLOW_PLAYER);
				if((!task.isOpenNew())&&newFollowPlayer!=null)
				{
					bool=false;
				}
				// �ȼ����� ��ҵȼ�
				if(task.getLimitLevel()!=0
					&&player.getLevel()<task.getLimitLevel())
				{
					bool=false;
				}
				// �ȼ����� ָ������
				if(task.getCommandCenter()!=0
					&&player.getIsland()
						.getBuildByIndex(BuildInfo.INDEX_0,null)
						.getBuildLevel()<task.getCommandCenter())
				{
					bool=false;
				}
				// ǰ�������ж�
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
				// ���͵�ǰ̨
				if(add)
				{
					// ÿ��������� �¼ӵ�ʱ�� ���5������sid
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

	/** �ر����� */
	public boolean reportTask(int taskSid)
	{
		Object[] tasks=this.tasks.toArray();
		for(int i=0;i<tasks.length;i++)
		{
			Task task=(Task)tasks[i];
			if(task.getSid()==taskSid&&task.isFinish())
			{
				// ��ͨ����
				if((task.getTaskType()&Task.TASK_NOMARL)!=0)
				{
					finishOnceTask(task.getKey(),task.getValue());
					this.tasks.remove(task);
				}
				else if((task.getTaskType()&Task.TASK_VITALITY)!=0){
					recodeVitalityTask(task.getKey(),task.getValue());
				}
				// ÿ������ ��ɺ���ɾ��
				else if((task.getTaskType()&Task.TASK_DAY)!=0)
				{
					if(task.getSid()!=TaskPort.HONOR_TASK)
						TaskEventExecute.getInstance().executeEvent(
							PublicConst.TASK_DAY_TASK_EVENT,this,player,"");
					task.setFinish(false);
					recodeDayTask(task.getKey(),task.getValue());
					//�ճ������ۼ�
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

	/** ���ֽ������з����л���ö������ */
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

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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

	/** ���ֽ������з����л���ö������ */
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

	/** �������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWriteTaskMark(ByteBuffer data)
	{
		data.writeShort(tasksMark.length);
		for(int i=0;i<tasksMark.length;i++)
		{
			data.writeInt(tasksMark[i]);
		}
	}

	/** ���ֽڻ����з����л��õ�һ������ */
	public Object bytesRead(ByteBuffer data)
	{
		bytesReadTaskMark(data);
		bytesReadDayTasks(data);
		bytesReadVitalityTasks(data);
		bytesReadTasks(data);
		return this;
	}

	/** ������������л����ֽڻ����� */
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
		// ÿ������
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
		// ��Ծ������
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
	 * @param player Ҫ���õ� player
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
	 * @param daytasksMark Ҫ���õ� daytasksMark
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
	 * @param tasks Ҫ���õ� tasks
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
	 * @param tasksMark Ҫ���õ� tasksMark
	 */
	public void setTasksMark(int[] tasksMark)
	{
		this.tasksMark=tasksMark;
	}
}
