package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.math.MathKit;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.net.TransmitHandler;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.MemCache;
import foxu.ds.PlayerKit;
import foxu.ds.SWDSManager;
import foxu.fight.FightScene;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.messgae.Message;

/** �����˶˿� */
public class ReBootPort implements TransmitHandler
{

	/** ��־��¼ */
	private static final Logger log=LogFactory.getLogger(MemCache.class);
	/** ���� */
	public static final int CHAT=1,MESSAGE=2,Fight=3,NOMARL_Fight=4;
	CreatObjectFactory objectFactory;

	/** dsmanager */
	SWDSManager dsmanager;

	static int fightTime;

	public void transmit(Connect connect,ByteBuffer data)
	{
		// TODO �Զ����ɷ������
		if(connect.getSource()==null)
		{
			connect.close();
			return;
		}
		if(true) return;
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return ;
		}
		int type=data.readUnsignedByte();
		// System.out.println("type=============="+type);
		// ����
		if(type==CHAT)
		{
			// // ������Ϣ
			ChatMessage message=new ChatMessage();
			// type
			int messageType=data.readUnsignedByte();
			String text=data.readUTF();
			message.setType(messageType);
			message.setSrc(player.getName());
			// �����ֹ���
			text=ChatMessage.filerText(text);
			message.setText(text);
			boolean bool=true;
			// ����������Ϣ
			if(message.getType()==ChatMessage.WORLD_CHAT)
			{
				SeaBackKit.sendAllMsg(message,dsmanager,false);
			}
		}
		// �ʼ�
		else if(type==MESSAGE)
		{
			String title=data.readUTF();
			String content=data.readUTF();
			Session session[]=dsmanager.getSessionMap().getSessions();
			// �����һ���ʼ�
			int randomMessage=MathKit.randomValue(0,session.length-1);
			for(int i=0;i<session.length;i++)
			{
				Player revice=PlayerKit.getInstance().getPlayerOnly(
					session[randomMessage]);
				if(revice==null) return;
				Message message=objectFactory.createMessage(player.getId(),
					revice.getId(),content,player.getName(),
					revice.getName(),0,title,false);
				JBackKit.sendRevicePlayerMessage(revice,message,0,objectFactory);
				break;
			}
		}
		// fight ս��cpu���ܲ���
		else if(type==NOMARL_Fight)
		{
			// �����Լ��ĳǷ�����
			FleetGroup group=createFleetGroup();
			FleetGroup defend=createFleetGroup();
			FightScene scene=FightSceneFactory.factory.create(group,defend);
			FightShowEventRecord r=FightSceneFactory.factory.fight(scene,
				null);
			Object[] object=new Object[2];
			object[0]=scene;
			object[1]=r;
			// ս���ݲ�����
			ByteBuffer fight_data=new ByteBuffer();
			SeaBackKit.conFightRecord(fight_data,r.getRecord(),player
				.getName(),player.getLevel(),"���Ի�����ս��",100,
				PublicConst.FIGHT_TYPE_1,player,null,group,defend,true,null,null);
			boolean success=false;
			if(scene.getSuccessTeam()==0) success=true;
			// �����ʼ�
			Message message=objectFactory
				.createMessage(0,player.getId(),"","fight_report",player
					.getName(),Message.FIGHT_TYPE,"",true);
			message.setFightType(0);
			FightEvent event=new FightEvent();
			message
				.createFightReports(player,PublicConst.FIGHT_TYPE_1,player
					.getName(),0,player.getName(),"������",success,fight_data,
					null,event,group.hurtList(FleetGroup.HURT_TROOPS),defend
						.hurtList(FleetGroup.HURT_TROOPS),1,11018,null,null,
					0,"",defend,0,0,null);
			// ˢ��ǰ̨
			JBackKit.sendRevicePlayerMessage(player,message,0,objectFactory);
			fightTime++;
			if(fightTime%100==0)
				System.out.println("fightTime============"+fightTime);
		}
		// ����ս������ ���Ƿ�����֮��
		else if(type==Fight)
		{
           
		}
	}

	/** ����һ��1000ֻ�����Ķ��� */
	public FleetGroup createFleetGroup()
	{
		FleetGroup group=new FleetGroup();
		int shipSid[]={10013,10021,10021,11131,11131,11133};
		for(int i=0;i<6;i++)
		{
			Fleet fleet=new Fleet();
			fleet.initNum(MathKit.randomValue(2000,10000));
			fleet.setLocation(i);
			fleet.setShip((Ship)Ship.factory.newSample(shipSid[i]));
			group.setFleet(i,fleet);
		}
		return group;
	}

	/**
	 * @return dsmanager
	 */
	public SWDSManager getDsmanager()
	{
		return dsmanager;
	}

	/**
	 * @param dsmanager Ҫ���õ� dsmanager
	 */
	public void setDsmanager(SWDSManager dsmanager)
	{
		this.dsmanager=dsmanager;
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory Ҫ���õ� objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
		log.warn("server start finish and success");
	}

}
