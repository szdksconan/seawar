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
 * ս��ϵͳ ������
 * 
 * @author yw
 * 
 */
public class ComradeHandler 
{

	/* fields */
	/** �ڴ������ */
	CreatObjectFactory objfactory;
	/** ʵ�� */
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

	/** ����±� */
	public String addRecruit(int veteranId,Player recruit,ByteBuffer data)
	{
		if(veteranId>0)
		{
			if(veteranId==recruit.getId())
				return "comarde can not yourself";
			// �������Ƿ����ڱ���id
			int serverInveteId=veteranId>>24;
			boolean bool=((PlayerGameDBAccess)objfactory.getPlayerCache()
				.getDbaccess()).isExistByID(veteranId);
			if(serverInveteId!=UserToCenterPort.SERVER_ID&&!bool)
				return "comarde fail";
		}
		// ������Ѿ�����ļ��
		if(recruit.getComrade().getVeteranId()!=0)
			return "this player have comarde";
		if(recruit.getComrade().getRecruitIds().contain(veteranId))
			return "you can not comare your parterner";
		// recruit �±�
		// �ϱ�
		Player veteran=objfactory.getPlayerById(veteranId);
		if(veteran==null) return "comrade limit";
		if(veteran.getLevel()<Comrade.COMRADE_LEVEL)
			return "comrade level limit";
//		// ��Ҫ���еȼ�����
		 if(recruit.getLevel()>veteran.getLevel())
		 return "level is limit";
		IntList recruitIds=veteran.getComrade().getRecruitIds();
		if(recruitIds.size()>=Comrade.R_MAX) return "length is limt";
		// ���ؽ�� ˵����� �Ѿ��������ҳɹ���ļ
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
		//�ɾ����ݲɼ� 
		AchieveCollect.inviteUser(veteran);
		return null;
	}

	/** ����������� */
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
	/** ����±��ܷ���ɸ����� */
	public boolean checkTask(Comrade comrade,ComradeTask task,int cvalue)
	{
		if(task.getTaskCondition()>cvalue) return false;
		int[] taskmark=comrade.getTaskMark();
		int key=task.getKey();
		int value=task.getValue();
		if(taskmark.length==0) 
		{
			taskmark=new int[key+1];
			taskmark[key]=taskmark[key]|(1<<value);// �±���¼
			return true;
		}
		else
		{
			return (taskmark[key]&(1<<value))==0;
		}
	}
	/** ��¼������� */
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
		taskmark[key]=taskmark[key]|(1<<value);// �±���¼
		incrComplete(task,veteran);// �ϱ���¼
	}
	/** ����������ɴ��� */
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
	/** ��ȡ��������ɴ��� */
	public int getComplete(int taskSid,Player veteran)
	{
		IntKeyHashMap map=veteran.getComrade().getAwardMap();
		AwardMark mark=(AwardMark)map.get(taskSid);
		if(mark==null) return 0;
		return mark.getComplete();
	}
	/** ��ȡ�������콱���� */
	public int getGot(int taskSid,Player veteran)
	{
		IntKeyHashMap map=veteran.getComrade().getAwardMap();
		AwardMark mark=(AwardMark)map.get(taskSid);
		if(mark==null) return 0;
		return mark.getGot();
	}
	
	/** ��ȡ���� */
	public void getAward(ByteBuffer data,Player veteran)
	{
		int taskSid=data.readUnsignedShort();
		int complete=getComplete(taskSid,veteran);
		int got=getGot(taskSid,veteran);
		if(complete<=got)
		{
			throw new DataAccessException(0,"not finish task");// δ��ɸ�����
		}
		ComradeTask task=(ComradeTask)ComradeTask.factory.getSample(taskSid);
		if(task==null)
		{
			throw new DataAccessException(0,"no task");// �޸�����
		}
		if(got>=task.getComplete())
		{
			throw new DataAccessException(0,"got award");// �ý���������
		}
		incrGot(task,veteran);
		data.clear();
		data.writeShort(1);
		data.writeByte(task.getTaskType());
		data.writeByte(1);
		data.writeInt(task.getSid());
		int go=getComplete(task.getSid(),veteran);
		got=getGot(taskSid,veteran);
		data.writeShort(got);//����ȡ����
		data.writeShort(task.getComplete());//������ȡ����
		data.writeByte(go>got?1:0);//0--������ȡ��1-������ȡ
		data.writeUTF(task.getTaskCondition()+"");//�콱��������
		Award award=(Award)Award.factory.getSample(task
			.getAwardSid());
		award.viewAward(data,veteran);
		award.awardLenth(data,veteran,objfactory,null,
			new int[]{EquipmentTrack.FROM_COMRADE});

	}
	
	/** ��¼�콱  */
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
	
	/** ��ȡս���б� */
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
			//����
			data.writeByte(recruit.getPlayerType());
			//ս��
			data.writeInt(recruit.getFightScore());
			int out=recruit.getExitTime();
			//����ʱ��
			data.writeInt(recruit.getComrade().isOnline()?0:(checkTime-out)+1);
			String aname="";
			String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(aid!=null)
			{
				alliance=objfactory.getAlliance(TextKit.parseInt(aid),false);
				if(alliance!=null) aname=alliance.getName();
			}
			//��������
			data.writeUTF(aname);
		}
		finishTask(player,ComradeTask.LEVEL);
		finishTask(player,ComradeTask.RECHARGE);
		finishTask(player,ComradeTask.FIGHT_SCORE);
	}
	
	/**����Ƿ����л�**/
	public boolean  checkPlayerList(Player player)
	{
		IntList pids=player.getComrade().getRecruitIds();
		if(pids==null || pids.size()==0) return false;
		return true;
	}
	
	/**��ȡ��ǰ��ҵ������б�
	 *��ǰ�������б������������ �ֿ��� 
	 */
	public void showBytesWriteCombradeTasks(Player recruit,ByteBuffer data)
	{
		data.writeShort(ComradeTask.COMRADETASK_TYPE);
		Sample[] tasks=ComradeTask.factory.getSamples();
		/** ���������������л����� **/
		for(int j=1;j<ComradeTask.COMRADETASK_TYPE+1;j++)
		{
			// ��������
			data.writeByte(j);
			int top=data.top();
			// ��������
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
				data.writeShort(got);//����ȡ����
				data.writeShort(task.getComplete());//������ȡ����
				data.writeByte(go>got?1:0);//0--������ȡ��1-������ȡ
				data.writeUTF(task.getTaskCondition()+"");//�콱��������
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
	
	/**��¼��ˢ��**/
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
	
	
	/**�������ͻ�ȡ��ǰ��ֵ**/
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
	
	/**��ȡ��ҵı���ļ����**/
	public Player getVertern(Player player)
	{
		int veteranId=player.getComrade().getVeteranId();
		if(veteranId==0) return null;
		Player veteran=objfactory.getPlayerById(veteranId);
		if(veteran!=null) return veteran;
		return null;
	}
}
