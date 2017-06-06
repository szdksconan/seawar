package foxu.sea.alliance;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.AlliancePort;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * ���˻᳤�Զ��ƽ�������
 * 
 * @author Alan
 */
public class AllianceAutoTransfer implements TimerListener
{

	/** �������ʱ�� */
	public static final int CHECK_CIRCLE_TIME=6*3600;
	/** �ƽ�����ʱ�� */
	public static final int TRANSFER_CIRCLE_TIME=7*24*3600;
	CreatObjectFactory objectFactory;
	/** ����id */
	private IntList autoAlliances;
	TimerEvent autoTransfer;
	Object lock=new Object();

	public void init(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
		autoAlliances=new IntList();

		Object[] alliances=objectFactory.getAllianceMemCache().getCacheMap()
			.valueArray();
		for(int i=0;i<alliances.length;i++)
		{
			Alliance alliance=((AllianceSave)alliances[i]).getData();
			autoAlliances.add(alliance.getId());
		}
		autoTransfer=new TimerEvent(this,"auto_transfer",CHECK_CIRCLE_TIME);
		TimerCenter.getSecondTimer().add(autoTransfer);
	}

	/** ��ӻ�������� */
	public void updateAutoAlliance(Alliance alliance)
	{
		// ���������б���Ϊ�½�����
		if(!autoAlliances.contain(alliance.getId()))
			autoAlliances.add(alliance.getId());
		// ������Ϊ��Ϊת�ƻ᳤
		else
		{
			Player newMaster=objectFactory.getPlayerCache().load(
				alliance.getMasterPlayerId()+"");
			// ��¼�»᳤�����λ�׼ʱ��
			newMaster.setAttribute(PublicConst.ALLIANCE_TRANSFER_TIME,
				TimeKit.getSecondTime()+"");
		}
	}

	/** ά���Զ��������� */
	private void autoTransferArray(int time)
	{
		synchronized(lock)
		{
			IntList emptyAlliances=new IntList();
			for(int i=0;i<autoAlliances.size();i++)
			{
				Alliance alliance=objectFactory.getAlliance(
					autoAlliances.get(i),false);
				if(alliance!=null)
				{
					if(checkAutoTransfer(alliance,time))
						masterTransfer(alliance,time);
				}
				else
					emptyAlliances.add(autoAlliances.get(i));
			}
			for(int i=0;i<emptyAlliances.size();i++)
			{
				autoAlliances.remove(emptyAlliances.get(i));
			}
		}
	}

	/** �᳤�Զ��ƽ� */
	public void masterTransfer(Alliance alliance,int time)
	{
		String name=alliance.getMasterName(objectFactory);
		Player oldMaster=objectFactory.getPlayerByName(name,false);
		// ���˳�Ա����1��ʱ��������
		if(oldMaster!=null && alliance.getPlayerList().size()>1)
		{
			// �Ƚ��и��᳤�ж�
			Player newMaster=getMasterFromArray(alliance.getVicePlayers(),
				alliance.getMasterPlayerId(),time);
			// ���û�и��᳤���������ͨ��Ա�ж�
			if(newMaster==null)
			{
				newMaster=getMasterFromArray(alliance.getPlayerList(),
					alliance.getMasterPlayerId(),time);
			}
			else
			{
				// �Ƴ����᳤
				alliance.removeVicePlayer(newMaster.getId());
			}
			// ��֤���᳤��������Ҳ�Ϊ��
			if(newMaster!=null)
			{
				alliance.setMasterPlayerId(newMaster.getId());
				if(oldMaster!=null) alliance.addVicePlayer(oldMaster.getId());
				// ��¼�»᳤�����λ�׼ʱ��
				newMaster.setAttribute(PublicConst.ALLIANCE_TRANSFER_TIME,
					TimeKit.getSecondTime()+"");
				// ��֤�ı�������������
				objectFactory.getPlayerCache().load(newMaster.getId()+"");
				// �ƽ���ɣ�֪ͨ��Ա

				// todo ���ʼ�
				allianceEventAndMail(alliance,name);
				// ��֤�ı��������������
				objectFactory.getAlliance(alliance.getId(),true);
			}
		}
	}

	/** ���б��ȡ�������������,�ų���ǰ�᳤ */
	private Player getMasterFromArray(IntList playerList,int masterId,int time)
	{
		Player newMaster=null;
		for(int i=0;i<playerList.size();i++)
		{
			Player player=objectFactory.getPlayerById(playerList.get(i));
			if(player!=null)
			{
				if(player.getId()==masterId) continue;
				if(!SeaBackKit.isPlayerOnline(player)
					&&player.getUpdateTime()<time-TRANSFER_CIRCLE_TIME)
					continue;
				if(newMaster==null)
				{
					newMaster=player;
					continue;
				}
				if(player.getFightScore()>newMaster.getFightScore())
				{
					newMaster=player;
					continue;
				}
				if(player.getFightScore()==newMaster.getFightScore()
					&&player.getUpdateTime()>newMaster.getUpdateTime())
				{
					newMaster=player;
					continue;
				}
			}
		}
		return newMaster;
	}

	/** ����������¼������Ա������Ϣ */
	private void allianceEventAndMail(Alliance alliance,String oldMaster)
	{
		String newMaster=alliance.getMasterName(objectFactory);
		// �����¼�
		AllianceEvent event=new AllianceEvent(
			AllianceEvent.ALLIANCE_EVENT_AUTOTRANFER,oldMaster,newMaster,
			Alliance.MILITARY_RANK3+"",TimeKit.getSecondTime());
		alliance.addEvent(event);
		// �᳤�ı���Ϣ
		ByteBuffer data=new ByteBuffer();
		data.clear();
		data.writeShort(AlliancePort.ALLIANCE_PORT);
		data.writeByte(AlliancePort.RIGHT_CHANGE);
		data.writeUTF(newMaster);
		data.writeByte(Alliance.MILITARY_RANK3);
		// ���᳤�ı���Ϣ
		ByteBuffer data1=new ByteBuffer();
		data1.clear();
		data1.writeShort(AlliancePort.ALLIANCE_PORT);
		data1.writeByte(AlliancePort.RIGHT_CHANGE);
		data1.writeUTF(oldMaster);
		data1.writeByte(Alliance.MILITARY_RANK2);
		sendAllAlliancePlayers(data,alliance);
		sendAllAlliancePlayers(data1,alliance);
		String title=null;
		String content=null;
		// �ɻ᳤�ʼ�
		Player player=objectFactory.getPlayerByName(oldMaster,false);
		if(player!=null)
		{
			title=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_last_title");
			content=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_last_content");
			content=TextKit.replace(content,"%",newMaster);
			// �����ʼ�
			MessageKit.sendSystemMessages(player,objectFactory,content,title);
		}
		// �»᳤�ʼ�
		player=objectFactory.getPlayerByName(newMaster,false);
		if(player!=null)
		{
			title=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_next_title");
			content=InterTransltor.getInstance().getTransByKey(
				player.getLocale(),"alliance_transfer_next_content");
			content=TextKit.replace(content,"%",oldMaster);
			// �����ʼ�
			MessageKit.sendSystemMessages(player,objectFactory,content,title);
		}
	}

	/** ���˹㲥 */
	public void sendAllAlliancePlayers(ByteBuffer data,Alliance alliance)
	{
		SessionMap smap=objectFactory.getDsmanager().getSessionMap();
		Session[] sessions=smap.getSessions();
		Player player=null;
		Connect con=null;
		IntList list=alliance.getPlayerList();
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]!=null)
			{
				con=sessions[i].getConnect();
				if(con!=null&&con.isActive())
				{
					player=(Player)sessions[i].getSource();
					if(player==null) continue;
					for(int j=0;j<list.size();j++)
					{
						if(player.getId()==list.get(j))
						{
							con.send(data);
						}
					}
				}
			}
		}
	}
	
	/** ������˻᳤�Ƿ���Ҫ�Զ��ƽ� */
	private boolean checkAutoTransfer(Alliance alliance,int time)
	{
		if(alliance.getPlayerList().size()<=1) return false;
		Player master=objectFactory.getPlayerById(alliance
			.getMasterPlayerId());
		int transferTime=time;
		// ���⴦������᳤�����ڵ����
		if(master==null)
		{
			// �Ƚ��и��᳤�ж�
			master=getMasterFromArray(alliance.getVicePlayers(),
				alliance.getMasterPlayerId(),time);
			// ���û�и��᳤���������ͨ��Ա�ж�
			if(master==null)
			{
				master=getMasterFromArray(alliance.getPlayerList(),
					alliance.getMasterPlayerId(),time);
			}
			else
			{
				// �Ƴ����᳤
				alliance.removeVicePlayer(master.getId());
			}
			if(master!=null)
			{
				alliance.addVicePlayer(alliance.getMasterPlayerId());
				alliance.setMasterPlayerId(master.getId());
			}
		}
		if(master!=null)
		{
			if(SeaBackKit.isPlayerOnline(master))	return false;
			// �״�ִ��ʱ���Բ����ڻ����ƽ���׼ʱ����е�¼��¼����ȡ�᳤�ϴε�¼ʱ��(֮ǰû�н����ƽ�����)
			transferTime=master.getUpdateTime();
			String transferTimeStr=master
				.getAttributes(PublicConst.ALLIANCE_TRANSFER_TIME);
			// �Զ��ƽ���׼ʱ�����Դ���,����ƽ���׼��û�е�¼��¼����ȡ����ֵ(֮ǰ�������ƽ�����)
			if(transferTimeStr!=null&&!transferTimeStr.equals("")
				&&transferTime<=Integer.valueOf(transferTimeStr))
				transferTime=Integer.valueOf(transferTimeStr);
			int allianceNextTime=transferTime+TRANSFER_CIRCLE_TIME;
			if(allianceNextTime<=TimeKit.getSecondTime()) return true;
		}
		return false;
	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		autoTransferArray((int)(arg0.getCurrentTime()/1000));
	}
}
