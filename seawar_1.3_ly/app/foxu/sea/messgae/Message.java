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
 * 玩家信件类 可以给玩家发放物品和资源奖励 author:icetiger
 */
public class Message
{
	/** 空奖励 */
	public static final int EMPTY_SID=65051;
	/** 战报里面的类型 FIGHT_TYPE_ZHE_CHA=侦查 RETURN_BACK=2返航报告 ARENA 竞技场报告 其他是战斗报告 */
	public static final int FIGHT_TYPE_ZHE_CHA=1,RETURN_BACK=2,ARENA=5;
	/** 删除状态 */
	public static final int DELETE=1;
	/** 邮件类型 数据库只有一封 所有玩家都可以看到 FIGHT_TYPE=2战报 SYSTEM_ONE_TYPE=4系统邮件针对单人
	 *  ALLIANCE_FIGHT=3 联盟争夺岛屿战
	 */
	public static final int PLAYER_TYPE=0,SYSTEM_TYPE=1,FIGHT_TYPE=2,ALLIANCE_FIGHT_TYPE=3,SYSTEM_ONE_TYPE=4;

	/** READ=1已读 ONE_DELETE有一方删除 */
	public static final int READ=1,ONE_DELETE=2;
	/**联盟战的title长度为5**/
	public static final int FIGHT_TITLE_LENGTH=5;
	/**联盟战胜利和失败的标识**/
	public static final int SUCCESS=1,FAILE=0;
	/** 邮件ID 自增 */
	private int messageId;
	/** 发送者玩家id playerID 不是user 反击时候是某个玩家的id */
	private int sendId;
	/** 接受者玩家ID player的ID 不是user */
	private int receiveId;
	/** 发送者名字 */
	private String sendName="";
	/** 接受名字 */
	private String receiveName;
	/** 标题 */
	private String title;
	/** 内容 */
	private String content;
	/** 创建时间 */
	private int createAt;
	/** 状态 未读 已读 主动方的状态 */
	private int state;
	/** 被动方的状态 */
	private int recive_state;
	/** messgaeType邮件类型 战报 玩家交流 系统邮件 */
	int messageType;
	/** 是否可以从数据库删除 */
	int delete;
	/** 战报子类型 */
	private int fightType;
	/**定时发送的开始时间**/
	private int startTime;
	/** 是否是群发 */
	private int mass;
	/**联盟战抬头显示的内容  进攻方的名称 进攻的联盟名称 防守方的名称 防守方的联盟名称 战斗是否胜利**/
	private String[] allianceFightTitle=new String[FIGHT_TITLE_LENGTH];
	 /** 附件 */
//	 private MessageData messageData=new MessageData();
	 /**设置附件领奖的人物信息**/
	 IntList playersList=new IntList();
	/** 战报版本(用以兼容更改某些字节数组内部序列化时不用清除玩家战报) */
	private int fightVersion;
	// /** 附件 */
	// private MessageData messageData=new MessageData();
	/** 战报 战斗数据 */
	ByteBuffer fightData=new ByteBuffer();
	/** 附件奖励包 */
	Award award;

	/** 战报数据之前的存放 船舰损失之类的数据或者是侦查内容 */
	ByteBuffer fightDataFore=new ByteBuffer();
	
	/** 军官数据 */
	ByteBuffer officerData=new ByteBuffer();
	/** 军官功勋值 */
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

	/** 添加邮件状态属性 */
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

	/** 判断是否已经读取或者获得附件 */
	public boolean checkState(int state)
	{
		return (this.state&state)!=0;
	}

	/** 添加邮件状态属性 */
	public void addReciveState(int recive_state)
	{
		if(checkReciveState(recive_state)) return;
		this.recive_state|=recive_state;
		checkDelete();
	}

	/** 判断是否已经读取或者获得附件 */
	public boolean checkReciveState(int recive_state)
	{
		return (this.recive_state&recive_state)!=0;
	}

	// /** 获取附件 只能从memcache里面调用 不能直接调用 否则无法保存 */
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
	// // 设置已经领取附件
	// addState(GET_DATA);
	// // 获取资源
	// gotDataResources(player);
	// // 获取物品
	// gotDataProps(player);
	// // 获取船只
	// gotDataShips(player);
	// return true;
	// }

	// /** 获取资源 */
	// public void gotDataResources(Player player)
	// {
	// Resources.addResources(player.getResources(),messageData
	// .getResources(),player);
	// }

	// /** 获取物品 */
	// public void gotDataProps(Player player)
	// {
	//
	// }
	//
	// /** 获取船只 */
	// public void gotDataShips(Player player)
	// {
	//
	// }
	
	/** 屏蔽字过滤 */
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

	/** 战报需要的数据 */
	public void bytesReadMessageDataFore(ByteBuffer data)
	{
		if(data.readBoolean())
		{
			fightDataFore=new ByteBuffer(data.toArray());
		}
	}

	/** 战报需要的数据 */
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

	/** 将对象的域序列化到字节缓存中 */
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
	/** 序列化给前台的 */
	public void showBytesWrite(ByteBuffer data,int state,String content,
		Player player)
	{
		// 战报
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
		// 竞技场挑战报告
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
			//胜利还是失败
			data.writeByte(TextKit.parseInt(allianceFightTitle[4]));
			//进攻方的名称
			data.writeUTF(allianceFightTitle[0]);
			//进攻方的联盟名称
			data.writeUTF(allianceFightTitle[1]);
			//防守方的名称
			data.writeUTF(allianceFightTitle[2]);
			//防守方的联盟名称
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

	/** 战斗报告用 player邮件送达者 */
	public void createFightReports(Player player,int fightType,String name,
		int index,String attackName,String defendName,boolean success,
		ByteBuffer fight,Award award,FightEvent event,IntList lostGourp,
		IntList delostGourp,int sourceIndex,int beIslandSid,
		CreatObjectFactory objectFactory,String messageString,
		int honorScore,String allianceName,FleetGroup defendGroup,int officerFeats,
		int reduceProsperity,NpcIsland island)
	{
		this.createAt=event.getCreatAt()+event.getNeedTime();
		// 如果玩家进行事件加速，则可能战报时间会在未来，设置为现在
		if(this.createAt>TimeKit.getSecondTime())
			this.createAt=TimeKit.getSecondTime();
		ByteBuffer data=new ByteBuffer();
		data.clear();
		/** 战报版本(兼容战报解析有改动时旧战报显示) */
		fightVersion=PublicConst.FIGHT_RECORD_VERSION;
		/** 世界战斗增加的军官功勋 */
		feats=officerFeats;
		if(this.fightType==RETURN_BACK)
		{
			// 岛屿sid
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
			data.writeByte(255);//特殊标识用来判断 繁荣度战报 
			data.writeByte(fightType);
			data.writeInt(sourceIndex);
			data.writeShort(beIslandSid);
			data.writeUTF(name);
			Player attack=objectFactory.getPlayerByName(attackName,false);
			data.writeUTF(attackName);
			data.writeUTF(SeaBackKit.getAllianceName(attack,objectFactory));
			data.writeInt(attack==null?0:attack.getProsperityInfo()[0]);//繁荣指数
			data.writeInt(attack==null?0:attack.getProsperityInfo()[2]);//繁荣度Max
			Player defend=objectFactory.getPlayerByName(defendName,false);
			data.writeUTF(defendName);
			data.writeUTF(SeaBackKit.getAllianceName(defend,objectFactory));
			data.writeInt(defend==null?0:defend.getProsperityInfo()[0]);//繁荣指数
			data.writeInt(defend==null?0:defend.getProsperityInfo()[2]);//繁荣度Max
			data.writeInt(reduceProsperity);//减少的繁荣度
		//	System.out.println("减少的繁荣度"+reduceProsperity);
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
			// 写入资源
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
			// 进攻方损失舰船信息 sid num
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
			// 防守方损失舰船信息
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
		// 写入军官信息
		// 攻击者
		event.getFleetGroup().getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// 防守者
		defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// 存下来
		if(data!=null&&data.length()>0)
			fightDataFore=new ByteBuffer(data.toArray());
		// 战斗演播数据
		if(fight!=null&&fight.length()>0)
			fightData=new ByteBuffer(fight.toArray());
	}

	/**
	 * 创建竞技场战报
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
		/** 战报版本(兼容战报解析有改动时旧战报显示) */
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
		// 进攻方损失舰船信息 sid num
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
		// 防守方损失舰船信息
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
		// 写入军官信息
		// 攻击者
		attackGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// 防守者
		defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// 存下来
		if(data!=null&&data.length()>0)
			fightDataFore=new ByteBuffer(data.toArray());
		// 战斗演播数据
		if(fight!=null&&fight.length()>0)
			fightData=new ByteBuffer(fight.toArray());
	}

	/** 侦查用 */
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
		// 已读状态
		data.writeByte(1);
		data.writeByte(fightType);
		data.writeShort(islandSid);
		data.writeUTF(player.getName());
		data.writeShort(islandSid);
		data.writeInt(index);
		if(islandSid==0)
		{
			data.writeUTF(player.getName());
			// 资源
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
				//是否是敌对联盟
				data.writeBoolean(flag);
			}
		}
		// 兵力
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
	
	/**创建联盟战报**/
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
		// 进攻方损失舰船信息 sid num
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
		// 防守方损失舰船信息
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
		// 写入军官信息
		// 攻击者
		attackGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// 防守者
		defendGroup.getOfficerFleetAttr().showBytesWriteOfficers(officerData);
		// 存下来
		if(data!=null&&data.length()>0)
			fightDataFore=new ByteBuffer(data.toArray());
		// 战斗演播数据
		if(fight!=null&&fight.length()>0)
			fightData=new ByteBuffer(fight.toArray());
	}
	

	/** 设置联盟战title **/
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
	 * @param createAt 要设置的 createAt
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
	 * @param messageId 要设置的 messageId
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
	 * @param receiveId 要设置的 receiveId
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
	 * @param sendId 要设置的 sendId
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
	 * @param receiveName 要设置的 receiveName
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
	 * @param sendName 要设置的 sendName
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
	 * @param content 要设置的 content
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
	 * @param state 要设置的 state
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
	// * @param messageData 要设置的 messageData
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
	 * @param messageType 要设置的 messageType
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
	 * @param delete 要设置的 delete
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
	 * @param title 要设置的 title
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
	 * @param recive_state 要设置的 recive_state
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
	 * @param fightData 要设置的 fightData
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
	 * @param fightType 要设置的 fightType
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
	 * @param fightDataFore 要设置的 fightDataFore
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

	/**读取数据**/
	public void setAllianceFightTileInfo(String titleInfo)
	{
		if(titleInfo==null||titleInfo.length()==0) return;
		String[] titlesinfo=TextKit.split(titleInfo,",");
		for(int i=0;i<FIGHT_TITLE_LENGTH;i++)
		{
			allianceFightTitle[i]=titlesinfo[i];
		}
	}
	/**获取字符串的联盟站的title**/
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
		// 兼容代码
		if(officerData==null||officerData.length()<=0)
		{
			OfficerBattleHQ hq=new OfficerBattleHQ();
			officerData=new ByteBuffer();
			// 友方地方总写两次
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
	// * @param language 要设置的 language
	// */
	// public void setLanguage(int language)
	// {
	// this.language=language;
	// }
}
