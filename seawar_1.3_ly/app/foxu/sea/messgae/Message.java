package foxu.sea.messgae;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.event.FightEvent;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * ����ż��� ���Ը���ҷ�����Ʒ����Դ���� author:icetiger
 */
public class Message
{
	/** �ս��� */
	public static final int EMPTY_SID=65051;
	/** ս����������� FIGHT_TYPE_ZHE_CHA=��� RETURN_BACK=2�������� ARENA ���������� ������ս������ */
	public static final int FIGHT_TYPE_ZHE_CHA=1,RETURN_BACK=2,ARENA=5;
	/** ɾ��״̬ */
	public static final int DELETE=1;
	/** �ʼ����� ���ݿ�ֻ��һ�� ������Ҷ����Կ��� FIGHT_TYPE=2ս�� SYSTEM_ONE_TYPE=4ϵͳ�ʼ���Ե���
	 *  ALLIANCE_FIGHT=3 �������ᵺ��ս
	 */
	public static final int PLAYER_TYPE=0,SYSTEM_TYPE=1,FIGHT_TYPE=2,ALLIANCE_FIGHT_TYPE=3,SYSTEM_ONE_TYPE=4;

	/** READ=1�Ѷ� ONE_DELETE��һ��ɾ�� */
	public static final int READ=1,ONE_DELETE=2;
	/**����ս��title����Ϊ5**/
	public static final int FIGHT_TITLE_LENGTH=5;
	/**����սʤ����ʧ�ܵı�ʶ**/
	public static final int SUCCESS=1,FAILE=0;
	/** �ʼ�ID ���� */
	private int messageId;
	/** ���������id playerID ����user ����ʱ����ĳ����ҵ�id */
	private int sendId;
	/** ���������ID player��ID ����user */
	private int receiveId;
	/** ���������� */
	private String sendName="";
	/** �������� */
	private String receiveName;
	/** ���� */
	private String title;
	/** ���� */
	private String content;
	/** ����ʱ�� */
	private int createAt;
	/** ״̬ δ�� �Ѷ� ��������״̬ */
	private int state;
	/** ��������״̬ */
	private int recive_state;
	/** messgaeType�ʼ����� ս�� ��ҽ��� ϵͳ�ʼ� */
	int messageType;
	/** �Ƿ���Դ����ݿ�ɾ�� */
	int delete;
	/** ս�������� */
	private int fightType;
	/**��ʱ���͵Ŀ�ʼʱ��**/
	private int startTime;
	/** �Ƿ���Ⱥ�� */
	private int mass;
	/**����ս̧ͷ��ʾ������  ������������ �������������� ���ط������� ���ط����������� ս���Ƿ�ʤ��**/
	private String[] allianceFightTitle=new String[FIGHT_TITLE_LENGTH];
	 /** ���� */
//	 private MessageData messageData=new MessageData();
	 /**���ø����콱��������Ϣ**/
	 IntList playersList=new IntList();
	/** ս���汾(���Լ��ݸ���ĳЩ�ֽ������ڲ����л�ʱ����������ս��) */
	private int fightVersion;
	// /** ���� */
	// private MessageData messageData=new MessageData();
	/** ս�� ս������ */
	ByteBuffer fightData=new ByteBuffer();
	/** ���������� */
	Award award;

	/** ս������֮ǰ�Ĵ�� ������ʧ֮������ݻ������������ */
	ByteBuffer fightDataFore=new ByteBuffer();
	
	/** �������� */
	ByteBuffer officerData=new ByteBuffer();
	/** ���ٹ�ѫֵ */
	int feats;

	public void bytesWriteContent(ByteBuffer data)
	{
		data.writeUTF(content);
	}

	public void bytesReadContent(ByteBuffer data)
	{
		content=data.readUTF();
	}

	public void bytesWriteTitle(ByteBuffer data)
	{
		data.writeUTF(title);
	}

	public void bytesReadTitle(ByteBuffer data)
	{
		title=data.readUTF();
	}

	/** ����ʼ�״̬���� */
	public void addState(int state)
	{
		if(checkState(state)) return;
		this.state|=state;
		checkDelete();
	}

	public void checkDelete()
	{
		if(checkReciveState(Message.ONE_DELETE)
			&&checkState(Message.ONE_DELETE))
		{
			setDelete(Message.DELETE);
		}
	}

	/** �ж��Ƿ��Ѿ���ȡ���߻�ø��� */
	public boolean checkState(int state)
	{
		return (this.state&state)!=0;
	}

	/** ����ʼ�״̬���� */
	public void addReciveState(int recive_state)
	{
		if(checkReciveState(recive_state)) return;
		this.recive_state|=recive_state;
		checkDelete();
	}

	/** �ж��Ƿ��Ѿ���ȡ���߻�ø��� */
	public boolean checkReciveState(int recive_state)
	{
		return (this.recive_state&recive_state)!=0;
	}

	// /** ��ȡ���� ֻ�ܴ�memcache������� ����ֱ�ӵ��� �����޷����� */
	// public boolean gotMessageDatas(Player player)
	// {
	// if(checkState(GET_DATA))
	// {
	// return false;
	// }
	// if(player==null||receiveId!=player.getId())
	// {
	// return false;
	// }
	// // �����Ѿ���ȡ����
	// addState(GET_DATA);
	// // ��ȡ��Դ
	// gotDataResources(player);
	// // ��ȡ��Ʒ
	// gotDataProps(player);
	// // ��ȡ��ֻ
	// gotDataShips(player);
	// return true;
	// }

	// /** ��ȡ��Դ */
	// public void gotDataResources(Player player)
	// {
	// Resources.addResources(player.getResources(),messageData
	// .getResources(),player);
	// }

	// /** ��ȡ��Ʒ */
	// public void gotDataProps(Player player)
	// {
	//
	// }
	//
	// /** ��ȡ��ֻ */
	// public void gotDataShips(Player player)
	// {
	//
	// }
	
	/** �����ֹ��� */
	public void filerText()
	{
		for(int i=0;i<ChatMessage.SHIELD.length;i++)
		{
			if(content!=null)
				content=TextKit.replaceAll(content,ChatMessage.SHIELD[i],"*");
			if(title!=null)
				title=TextKit.replaceAll(title,ChatMessage.SHIELD[i],"*");
		}
	}

	/** ս����Ҫ������ */
	public void bytesReadMessageDataFore(ByteBuffer data)
	{
		if(data.readBoolean())
		{
			fightDataFore=new ByteBuffer(data.toArray());
		}
	}

	/** ս����Ҫ������ */
	public void bytesWriteMessageDataFore(ByteBuffer data)
	{
		if(fightDataFore!=null&&fightDataFore.length()>0)
		{
			data.writeBoolean(true);
			data.write(fightDataFore.getArray(),fightDataFore.offset(),
				fightDataFore.length());
		}
		else
		{
			data.writeBoolean(false);
		}
	}

	public void bytesReadMessageData(ByteBuffer data)
	{
		if(data.readBoolean())
		{
			fightData=new ByteBuffer(data.toArray());
		}
	}
	
	public void bytesReadOfficerData(ByteBuffer data)
	{
		officerData=new ByteBuffer(data.toArray());
	}

	public void bytesWriteMessageData(ByteBuffer data)
	{
		if(fightData!=null&&fightData.length()>0)
		{
			data.writeBoolean(true);
			data.write(fightData.getArray(),fightData.offset(),
				fightData.length());
		}
		else
		{
			data.writeBoolean(false);
		}
	}
	
	public void bytesWriteOfficerData(ByteBuffer data)
	{
		data.write(officerData.getArray(),officerData.offset(),
			officerData.length());
	}
	
	public void bytesReadAward(ByteBuffer data)
	{
		if(data.readBoolean())
		{
			award=(Award)Award.factory.newSample(EMPTY_SID);
			award.bytesRead(data);
		}
	}

	public void bytesWriteAward(ByteBuffer data)
	{
		if(award!=null)
		{
			data.writeBoolean(true);
			award.bytesWrite(data);
		}
		else
		{
			data.writeBoolean(false);
		}
	}

	public Object bytesRead(ByteBuffer data)
	{
		messageId=data.readInt();
		mass=data.readInt();
		sendId=data.readInt();
		receiveId=data.readInt();
		sendName=data.readUTF();
		receiveName=data.readUTF();
		content=data.readUTF();
		createAt=data.readInt();
		state=data.readUnsignedByte();
		messageType=data.readUnsignedByte();
		fightType=data.readUnsignedByte();
		recive_state=data.readUnsignedByte();
		title=data.readUTF();
		bytesReadMessageData(data);
		bytesReadMessageDataFore(data);
		return this;
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(messageId);
		data.writeInt(mass);
		data.writeInt(sendId);
		data.writeInt(receiveId);
		data.writeUTF(sendName);
		data.writeUTF(receiveName);
		data.writeUTF(content);
		data.writeInt(createAt);
		data.writeByte(state);
		data.writeByte(messageType);
		data.writeByte(fightType);
		data.writeByte(recive_state);
		data.writeUTF(title);
		bytesWriteMessageData(data);
		bytesWriteMessageDataFore(data);
	}
	/** ���л���ǰ̨�� */
	public void showBytesWrite(ByteBuffer data,int state,String content,
		Player player)
	{
		// ս��
		if(messageType==Message.FIGHT_TYPE)
		{
			data.writeByte(messageType);
			data.writeInt(messageId);
			data.writeInt(createAt);
			data.writeByte(fightType);
			data.writeByte(state);
			String info[]={"0","0",""};
			if(title!=null&&!title.equals(""))
			{
				info=TextKit.split(title,"%");
			}
			data.writeByte(Integer.parseInt(info[0]));
			data.writeShort(Integer.parseInt(info[1]));
			data.writeUTF(info[2]);
		}
		// ��������ս����
		else if(messageType==ARENA)
		{
			data.writeByte(messageType);
			data.writeInt(messageId);
			data.writeInt(createAt);
			data.writeByte(state);
			data.writeByte(fightType);

			String[] strs=TextKit.split(content,":");
			data.writeUTF(strs[2]);
		}
		else if(messageType==ALLIANCE_FIGHT_TYPE)
		{
			data.writeInt(messageId);
			data.writeInt(createAt);
			//ʤ������ʧ��
			data.writeByte(TextKit.parseInt(allianceFightTitle[4]));
			//������������
			data.writeUTF(allianceFightTitle[0]);
			//����������������
			data.writeUTF(allianceFightTitle[1]);
			//���ط�������
			data.writeUTF(allianceFightTitle[2]);
			//���ط�����������
			data.writeUTF(allianceFightTitle[3]);
		}
		else
		{
			data.writeByte(messageType);
			data.writeInt(messageId);
			data.writeInt(createAt);
			data.writeUTF(title);
			data.writeUTF(sendName);
			data.writeUTF(receiveName);
			data.writeByte(state);
			data.writeUTF(content);
			data.writeInt(mass);
		}
		if(messageType==SYSTEM_TYPE||messageType==SYSTEM_ONE_TYPE)
		{
			if(player!=null&&award!=null)
			{
				data.writeBoolean(player.getAnnex().contain(messageId));
			}
			else
			{
				data.writeBoolean(false);
			}
			if(award==null)
			{
				data.writeByte(0);
			}
			else
			{
				award.viewAward(data,null);
			}

		}
	}

	/** ս�������� player�ʼ��ʹ��� */
	public void createFightReports(Player player,int fightType,String name,
		int index,String attackName,String defendName,boolean success,
		ByteBuffer fight,Award award,FightEvent event,IntList lostGourp,
		IntList delostGourp,int sourceIndex,int beIslandSid,
		CreatObjectFactory objectFactory,String messageString,
		int honorScore,String allianceName,FleetGroup defendGroup,int officerFeats,
		int reduceProsperity,NpcIsland island)
	{
		this.createAt=event.getCreatAt()+event.getNeedTime();
		// �����ҽ����¼����٣������ս��ʱ�����δ��������Ϊ����
		if(this.createAt>TimeKit.getSecondTime())
			this.createAt=TimeKit.getSecondTime();
		ByteBuffer data=new ByteBuffer();
		data.clear();
		/** ս���汾(����ս�������иĶ�ʱ��ս����ʾ) */
		fightVersion=PublicConst.FIGHT_RECORD_VERSION;
		/** ����ս�����ӵľ��ٹ�ѫ */
		feats=officerFeats;
		if(this.fightType==RETURN_BACK)
		{
			// ����sid
			data.writeByte(fightType);
			data.writeShort(beIslandSid);
			data.writeInt(index);
			if(fightType==PublicConst.FIGHT_TYPE_7)
				data.writeUTF(defendName);
		}
		else
		{
			String enmy=attackName;
			if(fightType==PublicConst.FIGHT_TYPE_2)
			{
				enmy=defendName;
			}
			title=fightType+"%"+beIslandSid+"%"+enmy;
			data.writeByte(255);//�����ʶ�����ж� ���ٶ�ս�� 
			data.writeByte(fightType);
			data.writeInt(sourceIndex);
			data.writeShort(beIslandSid);
			data.writeUTF(name);
			Player attack=objectFactory.getPlayerByName(attackName,false);
			data.writeUTF(attackName);
			data.writeUTF(SeaBackKit.getAllianceName(attack,objectFactory));
			data.writeInt(attack==null?0:attack.getProsperityInfo()[0]);//����ָ��
			data.writeInt(attack==null?0:attack.getProsperityInfo()[2]);//���ٶ�Max
			Player defend=objectFactory.getPlayerByName(defendName,false);
			data.writeUTF(defendName);
			data.writeUTF(SeaBackKit.getAllianceName(defend,objectFactory));
			data.writeInt(defend==null?0:defend.getProsperityInfo()[0]);//����ָ��
			data.writeInt(defend==null?0:defend.getProsperityInfo()[2]);//���ٶ�Max
			data.writeInt(reduceProsperity);//���ٵķ��ٶ�
		//	System.out.println("���ٵķ��ٶ�"+reduceProsperity);
			data.writeUTF(allianceName);
			data.writeInt(index);
			data.writeBoolean(success);
			if(success)
			{
				data.writeShort(honorScore);
			}
			else
			{
				data.writeShort(-honorScore);
			}
			if(award==null)
			{
				data.writeByte(0);
			}
			else
			{
				award.awardLenth(data,player,objectFactory,messageString,
					new int[]{EquipmentTrack.FROM_BOSS,FightScoreConst.FIGHT_AWARD});
			}
			// д����Դ
			int resource[]=event.getResources();
			int j=0;
			for(int i=0;i<resource.length;i++)
			{
				if(resource[i]!=0) j++;
			}
			data.writeByte(j);
			int num=0;
			for(int i=0;i<resource.length;i++)
			{
				if(resource[i]!=0)
				{
					data.writeByte(i+1);
					num=resource[i];
					if(i==Resources.GEMS&&island!=null
						&&island.getIslandType()==NpcIsland.ISLAND_GEMS)
					{
						num=num/PublicConst.LOWLIMIT_GEMS_TIMES;
					}
					if(success)
						data.writeInt(num);
					else
						data.writeInt(-num);
				}
			}
			// ��������ʧ������Ϣ sid num
			if(lostGourp!=null&&lostGourp.size()>0)
			{
				data.writeByte(lostGourp.size()/2);
				for(int i=0;i<lostGourp.size();i+=2)
				{
					data.writeShort(lostGourp.get(i));
					data.writeShort(lostGourp.get(i+1));
				}
			}
			else
			{
				data.writeByte(0);
			}
			// ���ط���ʧ������Ϣ
			if(delostGourp!=null&&delostGourp.size()>0)
			{
				data.writeByte(delostGourp.size()/2);
				for(int i=0;i<delostGourp.size();i+=2)
				{
					data.writeShort(delostGourp.get(i));
					data.writeShort(delostGourp.get(i+1));
				}
			}
			else
			{
				data.writeByte(0);
			}
		}
		// д�������Ϣ
		// ������
		event.getFleetGroup().getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// ������
		defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// ������
		if(data!=null&&data.length()>0)
			fightDataFore=new ByteBuffer(data.toArray());
		// ս���ݲ�����
		if(fight!=null&&fight.length()>0)
			fightData=new ByteBuffer(fight.toArray());
	}

	/**
	 * ����������ս��
	 * 
	 * @param player
	 * @param defendName
	 * @param success
	 * @param fight
	 * @param award
	 * @param lostGourp
	 * @param delostGourp
	 * @param objectFactory
	 * @param messageString
	 */
	public void createArenaFightReport(Player player,String defendName,
		boolean success,ByteBuffer fight,Award award,IntList lostGourp,
		IntList delostGourp,CreatObjectFactory objectFactory,
		String messageString,FleetGroup attackGroup,FleetGroup defendGroup)
	{
		ByteBuffer data=new ByteBuffer();
		/** ս���汾(����ս�������иĶ�ʱ��ս����ʾ) */
		fightVersion=PublicConst.FIGHT_RECORD_VERSION;
		String[] strs=TextKit.split(content,":");
		//data.writeUTF(strs[2]);
		data.writeInt(Integer.parseInt(strs[0]));
		data.writeInt(Integer.parseInt(strs[1]));
		
		data.writeUTF(defendName);
		data.writeBoolean(success);
		if(award==null)
		{
			data.writeByte(0);
		}
		else
		{
			award.awardLenth(data,player,objectFactory,messageString,
				new int[]{EquipmentTrack.FROM_ARENA,FightScoreConst.FIGHT_AWARD});
		}
		// ��������ʧ������Ϣ sid num
		if(lostGourp!=null&&lostGourp.size()>0)
		{
			data.writeByte(lostGourp.size()/2);
			for(int i=0;i<lostGourp.size();i+=2)
			{
				data.writeShort(lostGourp.get(i));
				data.writeShort(lostGourp.get(i+1));
			}
		}
		else
		{
			data.writeByte(0);
		}
		// ���ط���ʧ������Ϣ
		if(delostGourp!=null&&delostGourp.size()>0)
		{
			data.writeByte(delostGourp.size()/2);
			for(int i=0;i<delostGourp.size();i+=2)
			{
				data.writeShort(delostGourp.get(i));
				data.writeShort(delostGourp.get(i+1));
			}
		}
		else
		{
			data.writeByte(0);
		}
		// д�������Ϣ
		// ������
		attackGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// ������
		defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// ������
		if(data!=null&&data.length()>0)
			fightDataFore=new ByteBuffer(data.toArray());
		// ս���ݲ�����
		if(fight!=null&&fight.length()>0)
			fightData=new ByteBuffer(fight.toArray());
	}

	/** ����� */
	public ByteBuffer createZheChaBuffer(Player player,int islandSid,
		int index,FleetGroup fleetGroup,String name,int playerResource,
		String alliancePlayer,String allianceName,boolean flag,IntList list,int endtime)
	{
		this.fightType=FIGHT_TYPE_ZHE_CHA;
		title=fightType+"%"+islandSid+"%"+player.getName();
		ByteBuffer data=new ByteBuffer();
		data.writeByte(messageType);
		data.writeInt(messageId);
		data.writeInt(createAt);
		data.writeByte(fightType);
		// �Ѷ�״̬
		data.writeByte(1);
		data.writeByte(fightType);
		data.writeShort(islandSid);
		data.writeUTF(player.getName());
		data.writeShort(islandSid);
		data.writeInt(index);
		if(islandSid==0)
		{
			data.writeUTF(player.getName());
			// ��Դ
			long resource[]=SeaBackKit.canResourceP(player);
			data.writeLong(resource[Resources.METAL]);
			data.writeLong(resource[Resources.OIL]);
			data.writeLong(resource[Resources.SILICON]);
			data.writeLong(resource[Resources.URANIUM]);
			data.writeLong(resource[Resources.MONEY]);
		}
		else
		{
			data.writeUTF(name);
			if(!name.equals("")&&name!=null) 
			{
				data.writeInt(playerResource);
				//�Ƿ��ǵж�����
				data.writeBoolean(flag);
			}
		}
		// ����
		Fleet fleet[]=fleetGroup.getArray();
		int num=0;
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]!=null&&fleet[i].getNum()>0) num++;
		}
		data.writeByte(num);
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]!=null&&fleet[i].getNum()>0)
			{
				data.writeByte(fleet[i].getLocation());
				data.writeShort(fleet[i].getShip().getSid());
				data.writeShort(fleet[i].getNum());
			}
		}
		data.writeUTF(alliancePlayer);
		data.writeUTF(allianceName);
		SeaBackKit.getBuff(list,data);
		data.writeInt(endtime);
		if(data!=null&&data.length()>0)
			fightDataFore=new ByteBuffer(data.toArray());
		return data;
	}
	
	/**��������ս��**/
	public void createAllianceFightReports(Player player,String attackAllianceName,Player defend,String beAttackAllianceName,
		boolean success,ByteBuffer fight,IntList lostGourp,
		IntList delostGourp,FleetGroup attackGroup,FleetGroup defendGroup)
	{
		this.createAt=TimeKit.getSecondTime();
		ByteBuffer data=new ByteBuffer();
		data.clear();
		fightVersion=PublicConst.FIGHT_RECORD_VERSION;
		data.writeByte(messageType);
		data.writeByte(fightType);
		data.writeInt(messageId);
		data.writeInt(createAt);
		data.writeUTF(player.getName());
		data.writeShort(player.getLevel());
		data.writeUTF(attackAllianceName);
		data.writeUTF(defend.getName());
		data.writeUTF(beAttackAllianceName);
		data.writeShort(defend.getLevel());
		data.writeBoolean(success);
		// ��������ʧ������Ϣ sid num
		if(lostGourp!=null&&lostGourp.size()>0)
		{
			data.writeByte(lostGourp.size()/2);
			for(int i=0;i<lostGourp.size();i+=2)
			{
				data.writeShort(lostGourp.get(i));
				data.writeShort(lostGourp.get(i+1));
			}
		}
		else
		{
			data.writeByte(0);
		}
		// ���ط���ʧ������Ϣ
		if(delostGourp!=null&&delostGourp.size()>0)
		{
			data.writeByte(delostGourp.size()/2);
			for(int i=0;i<delostGourp.size();i+=2)
			{
				data.writeShort(delostGourp.get(i));
				data.writeShort(delostGourp.get(i+1));
			}
		}
		else
		{
			data.writeByte(0);
		}
		// д�������Ϣ
		// ������
		attackGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// ������
		defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// ������
		if(data!=null&&data.length()>0)
			fightDataFore=new ByteBuffer(data.toArray());
		// ս���ݲ�����
		if(fight!=null&&fight.length()>0)
			fightData=new ByteBuffer(fight.toArray());
	}
	

	/** ��������սtitle **/
	public void setAllianceFightTile(String attackName,
		String attackAllianceName,String beAttackName,
		String beAttackAllianceName,boolean success)
	{
		 allianceFightTitle[0]=attackName;
		 allianceFightTitle[1]=attackAllianceName;
		 allianceFightTitle[2]=beAttackName;
		 allianceFightTitle[3]=beAttackAllianceName;
		 if(success)
			 allianceFightTitle[4]=String.valueOf(SUCCESS);
		 else
			 allianceFightTitle[4]=String.valueOf(FAILE);
	}
	
	/**
	 * @param createAt Ҫ���õ� createAt
	 */
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}

	/**
	 * @return messageId
	 */
	public int getMessageId()
	{
		return messageId;
	}

	/**
	 * @param messageId Ҫ���õ� messageId
	 */
	public void setMessageId(int messageId)
	{
		this.messageId=messageId;
	}

	/**
	 * @return receiveId
	 */
	public int getReceiveId()
	{
		return receiveId;
	}

	/**
	 * @param receiveId Ҫ���õ� receiveId
	 */
	public void setReceiveId(int receiveId)
	{
		this.receiveId=receiveId;
	}

	/**
	 * @return sendId
	 */
	public int getSendId()
	{
		return sendId;
	}

	/**
	 * @param sendId Ҫ���õ� sendId
	 */
	public void setSendId(int sendId)
	{
		this.sendId=sendId;
	}

	/**
	 * @return receiveName
	 */
	public String getReceiveName()
	{
		return receiveName;
	}

	/**
	 * @param receiveName Ҫ���õ� receiveName
	 */
	public void setReceiveName(String receiveName)
	{
		this.receiveName=receiveName;
	}

	/**
	 * @return sendName
	 */
	public String getSendName()
	{
		return sendName;
	}

	/**
	 * @param sendName Ҫ���õ� sendName
	 */
	public void setSendName(String sendName)
	{
		this.sendName=sendName;
	}

	/**
	 * @return content
	 */
	public String getContent()
	{
		return content;
	}

	/**
	 * @param content Ҫ���õ� content
	 */
	public void setContent(String content)
	{
		this.content=content;
	}

	/**
	 * @return state
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * @param state Ҫ���õ� state
	 */
	public void setState(int state)
	{
		this.state=state;
	}

	/**
	 * @return createAt
	 */
	public int getCreateAt()
	{
		return createAt;
	}

	// /**
	// * @return messageData
	// */
	// public MessageData getMessageData()
	// {
	// return messageData;
	// }
	//
	// /**
	// * @param messageData Ҫ���õ� messageData
	// */
	// public void setMessageData(MessageData messageData)
	// {
	// this.messageData=messageData;
	// }

	/**
	 * @return messageType
	 */
	public int getMessageType()
	{
		return messageType;
	}

	/**
	 * @param messageType Ҫ���õ� messageType
	 */
	public void setMessageType(int messageType)
	{
		this.messageType=messageType;
	}

	/**
	 * @return delete
	 */
	public int getDelete()
	{
		return delete;
	}

	/**
	 * @param delete Ҫ���õ� delete
	 */
	public void setDelete(int delete)
	{
		this.delete=delete;
	}

	/**
	 * @return title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title Ҫ���õ� title
	 */
	public void setTitle(String title)
	{
		this.title=title;
	}

	/**
	 * @return recive_state
	 */
	public int getRecive_state()
	{
		return recive_state;
	}

	/**
	 * @param recive_state Ҫ���õ� recive_state
	 */
	public void setRecive_state(int recive_state)
	{
		this.recive_state=recive_state;
	}

	/**
	 * @return fightData
	 */
	public ByteBuffer getFightData()
	{
		return fightData;
	}

	/**
	 * @param fightData Ҫ���õ� fightData
	 */
	public void setFightData(ByteBuffer fightData)
	{
		this.fightData=fightData;
	}

	/**
	 * @return fightType
	 */
	public int getFightType()
	{
		return fightType;
	}

	/**
	 * @param fightType Ҫ���õ� fightType
	 */
	public void setFightType(int fightType)
	{
		this.fightType=fightType;
	}

	/**
	 * @return fightDataFore
	 */
	public ByteBuffer getFightDataFore()
	{
		return fightDataFore;
	}

	/**
	 * @param fightDataFore Ҫ���õ� fightDataFore
	 */
	public void setFightDataFore(ByteBuffer fightDataFore)
	{
		this.fightDataFore=fightDataFore;
	}

	
	public int getStartTime()
	{
		return startTime;
	}

	
	public void setStartTime(int startTime)
	{
		this.startTime=startTime;
	}

	
	public int getMass()
	{
		return mass;
	}

	
	public void setMass(int mass)
	{
		this.mass=mass;
	}

	
	public Award getAward()
	{
		return award;
	}

	
	public void setAward(Award award)
	{
		this.award=award;
	}

	/**��ȡ����**/
	public void setAllianceFightTileInfo(String titleInfo)
	{
		if(titleInfo==null||titleInfo.length()==0) return;
		String[] titlesinfo=TextKit.split(titleInfo,",");
		for(int i=0;i<FIGHT_TITLE_LENGTH;i++)
		{
			allianceFightTitle[i]=titlesinfo[i];
		}
	}
	/**��ȡ�ַ���������վ��title**/
	public String getStringFightTitleInfo()
	{
		if(messageType!=ALLIANCE_FIGHT_TYPE)
			return  null;
		StringBuffer buffer=new StringBuffer();
		for(int i=0;i<allianceFightTitle.length;i++)
		{
			if(i!=0)
				buffer.append(","+allianceFightTitle[i]);
			else 
				buffer.append(allianceFightTitle[i]);
		}
		return buffer.toString();
	}
	public ByteBuffer getOfficerData()
	{
		// ���ݴ���
		if(officerData==null||officerData.length()<=0)
		{
			OfficerBattleHQ hq=new OfficerBattleHQ();
			officerData=new ByteBuffer();
			// �ѷ��ط���д����
			hq.showBytesWriteOfficers(officerData);
			hq.showBytesWriteOfficers(officerData);
		}
		return officerData;
	}

	
	public void setOfficerData(ByteBuffer officerData)
	{
		this.officerData=officerData;
	}

	
	public int getFightVersion()
	{
		return fightVersion;
	}

	
	public void setFightVersion(int fightVersion)
	{
		this.fightVersion=fightVersion;
	}

	

	public String[] getAllianceFightTitle()
	{
		return allianceFightTitle;
	}

	public void setAllianceFightTitle(String[] allianceFightTitle)
	{
		this.allianceFightTitle=allianceFightTitle;
	}

	
	public int getFeats()
	{
		return feats;
	}

	
	public void setFeats(int feats)
	{
		this.feats=feats;
	}

	
	
	// /**
	// * @return language
	// */
	// public int getLanguage()
	// {
	// return language;
	// }
	//
	//
	// /**
	// * @param language Ҫ���õ� language
	// */
	// public void setLanguage(int language)
	// {
	// this.language=language;
	// }
}
