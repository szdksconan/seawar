package foxu.cross.war;

import foxu.cross.server.CrossPlayer;
import foxu.sea.AttrAdjustment;
import foxu.sea.Player;
import foxu.sea.Ship;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerManager;
import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.util.TimeKit;


/**
 * ���ս���
 * @author yw
 *
 */
public class CrossWarPlayer extends CrossPlayer
{
	/** ������ */
	String aname=" ";
	/** sid */
	int sid;
	/** �ȼ� */
	int level;
	/** ����ʱ�� */
	int jiontime;
	/** ս�� */
	int fightscore;
	/** ���սΨһid */
	int warid;
	/** ���� */
	int goal;
	/** nǿ */
	int rank;
	/** ��� */
	int num;
	/** ��Ѻע�� */
	int bet;
	/** ��������ֵ */
	AttrAdjustment adjustment=new AttrAdjustment();
	/** �����ȼ����� (��ʯ) */
	int[] shipLevel;
	/** �������� */
	IntList attacklist=new IntList();
	/** �������� */
	IntList defencelist=new IntList();
	/** ��ս���� */
	Officer[] ofs={};
	
	/* �������� */
	/** �Ƿ���Ҫ��� */
	boolean save;
	/** ӳ���player */
	Player player;
	/** �������� */
	FleetGroup attack;
	/** �������� */
	FleetGroup defence;
	/** �Ƿ���Ҫˢ�±�Ѻע�� */
	boolean flushBet;
	
	public CrossWarPlayer()
	{
		
	}
	public CrossWarPlayer(int crossid,int id,String name,int platid,
		int areaid,int serverid,int sid,int warid)
	{
		setCrossid(crossid);
		setId(id);
		setName(name);
		setPlatid(platid);
		setAreaid(areaid);
		setSeverid(serverid);
		setSid(sid);
		setWarid(warid);
		setJiontime(TimeKit.getSecondTime());
	}
	/** ����ע */
	public synchronized void addBet(int gems)
	{
		if(gems<=0)return;
		bet+=gems;
		if(bet<0)bet=Integer.MAX_VALUE;	
		setFlushBet(true);
	}
	/** �ж�����Ƿ���ȡ���� */
	public boolean isGetAward()
	{
		//todo
		return false;
	}
	
	/** ���ӻ���  */
	public void incrGoal()
	{
		goal++;
	}
	/** ��ȡ���� group */
	public FleetGroup getFleetGroup(boolean isattack)
	{
		if(isattack)
		{
			if(attack==null) attack=createFleetGroup(attacklist);
			return attack;
		}
		else
		{
			if(defence==null) defence=createFleetGroup(defencelist);
			return defence;
		}
	}
	/** �������� */
	public FleetGroup createFleetGroup(IntList list)
	{
		FleetGroup group=new FleetGroup();
		for(int i=0;i<list.size();i+=3)
		{
			int shipSid=list.get(i);
			int num=list.get(i+1);
			if(num<=0) continue;
			int location=list.get(i+2);
			Fleet fleet=new Fleet();
			fleet.setPlayter(getPlayer());
			fleet.initNum(num);
			fleet.setLocation(location);
			fleet.setShip((Ship)Ship.factory.newSample(shipSid));
			group.setFleet(location,fleet);
		}
		group.getOfficerFleetAttr().initOfficers(player);
		return group;
	}
	/** ��ȡӳ���player */
	public Player getPlayer()
	{
		if(player!=null) return player;
		player=(Player)Player.factory.newSample(getSid());
		setPlayerAttr(player);
		return player;
	}
	/** ���� ӳ��player */
	public void updatePlayer()
	{
		Player player=getPlayer();
		setPlayerAttr(player);
	}
	/** ����player���� */
	public void setPlayerAttr(Player player)
	{
		player.setName(getName());
		player.setLevel(getLevel());
		player.setAdjstment(getAdjustment());
		player.setShipLevel(getShipLevel());
		player.getOfficers().setUsedOfficers(getOfs());
	}
	
	/** ���ÿ��������� */
	public void reset()
	{
		setAttack(null);
		setDefence(null);
		setPlayer(null);
	}
	
	
	public int getWarid()
	{
		return warid;
	}
	
	public void setWarid(int warid)
	{
		this.warid=warid;
	}
	
	public int getGoal()
	{
		return goal;
	}
	
	public void setGoal(int goal)
	{
		this.goal=goal;
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public void setRank(int rank)
	{
		this.rank=rank;
	}
	
	public AttrAdjustment getAdjustment()
	{
		return adjustment;
	}
	
	public void setAdjustment(AttrAdjustment adjustment)
	{
		this.adjustment=adjustment;
	}
	
//	public EquipList getEquiplist()
//	{
//		return equiplist;
//	}
//	
//	public void setEquiplist(EquipList equiplist)
//	{
//		this.equiplist=equiplist;
//	}
	
	
	public int getNum()
	{
		return num;
	}

	
	public void setNum(int num)
	{
		this.num=num;
	}

	
	public int getFightscore()
	{
		return fightscore;
	}

	
	public void setFightscore(int fightscore)
	{
		this.fightscore=fightscore;
	}

	
	public int getJiontime()
	{
		return jiontime;
	}

	
	public void setJiontime(int jiontime)
	{
		this.jiontime=jiontime;
	}

	public boolean isSave()
	{
		return save;
	}

	
	public void setSave(boolean save)
	{
		this.save=save;
	}

	public void bytesReadAdjustment(ByteBuffer data)
	{
		adjustment.crossBytesRead(data);
	}
	
	public void bytesWriteAdjustment(ByteBuffer data)
	{
		adjustment.crossBytesWrite(data);
	}
	
//	public void bytesReadEquiplist(ByteBuffer data)
//	{
//		equiplist.crossBytesRead(data);
//	}
//	
//	public void bytesWriteEquiplist(ByteBuffer data)
//	{
//		equiplist.crossBytesWrite(data);
//	}
	
	public IntList getAttacklist()
	{
		return attacklist;
	}

	
	public void setAttacklist(IntList attacklist)
	{
		this.attacklist=attacklist;
	}

	
	public IntList getDefencelist()
	{
		return defencelist;
	}

	
	public void setDefencelist(IntList defencelist)
	{
		this.defencelist=defencelist;
	}
	
	
	public int getLevel()
	{
		return level;
	}

	
	public void setLevel(int level)
	{
		this.level=level;
	}

	
	public int getSid()
	{
		return sid;
	}
	
	public void setSid(int sid)
	{
		this.sid=sid;
	}
	
	public FleetGroup getAttack()
	{
		return attack;
	}
	
	public FleetGroup getDefence()
	{
		return defence;
	}
	
	
	
	
	public void setAttack(FleetGroup attack)
	{
		this.attack=attack;
	}
	
	public void setDefence(FleetGroup defence)
	{
		this.defence=defence;
	}
	
	public void setPlayer(Player player)
	{
		this.player=player;
	}
	public int getBet()
	{
		return bet;
	}
	
	public void setBet(int bet)
	{
		this.bet=bet;
	}
	
	public boolean isFlushBet()
	{
		return flushBet;
	}
	
	public void setFlushBet(boolean flushBet)
	{
		synchronized(attacklist)
		{
			this.flushBet=flushBet;
		}
	}
	
	public String getAname()
	{
		return aname;
	}
	
	public void setAname(String aname)
	{
		this.aname=aname;
	}
	
	
	public int[] getShipLevel()
	{
		return shipLevel;
	}
	
	public void setShipLevel(int[] shipLevel)
	{
		this.shipLevel=shipLevel;
	}
	
	public Officer[] getOfs()
	{
		return ofs;
	}
	
	public void setOfs(Officer[] ofs)
	{
		this.ofs=ofs;
	}
	
	public void bytesWriteShiplevel(ByteBuffer data)
	{
		if(shipLevel==null)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(shipLevel.length);
			for(int i=0;i<shipLevel.length;i++)
			{
				data.writeInt(shipLevel[i]);
			}
		}
	}
	
	public void bytesReadShiplevel(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		if(len>0)
		{
			shipLevel=new int[len];
			for(int i=0;i<len;i++)
			{
				shipLevel[i]=data.readInt();
			}
		}
	}
	
	public void bytesWriteAttacklist(ByteBuffer data)
	{
		int len=attacklist.size()/3;
		data.writeByte(len);
		for(int i=0;i<attacklist.size();i+=3)
		{
			data.writeShort(attacklist.get(i));
			data.writeShort(attacklist.get(i+1));
			data.writeByte(attacklist.get(i+2));
		}
	}
	public void bytesReadAttacklist(ByteBuffer data)
	{
		attacklist.clear();
		int length=data.readUnsignedByte();
		for(int i=0;i<length;i++)
		{
			int sid=data.readUnsignedShort();
			int num=data.readUnsignedShort();
			int location=data.readUnsignedByte();
			attacklist.add(sid);
			attacklist.add(num);
			attacklist.add(location);
		}
		
	}
	public void bytesWriteDefencelist(ByteBuffer data)
	{
		int len=defencelist.size()/3;
		data.writeByte(len);
		for(int i=0;i<defencelist.size();i+=3)
		{
			data.writeShort(defencelist.get(i));
			data.writeShort(defencelist.get(i+1));
			data.writeByte(defencelist.get(i+2));
		}
	}
	public void bytesReadDefencelist(ByteBuffer data)
	{
		defencelist.clear();
		int length=data.readUnsignedByte();
		for(int i=0;i<length;i++)
		{
			int sid=data.readUnsignedShort();
			int num=data.readUnsignedShort();
			int location=data.readUnsignedByte();
			defencelist.add(sid);
			defencelist.add(num);
			defencelist.add(location);
		}
	}
	
	/** ����д */
	public void bytesWriteOFS(ByteBuffer data)
	{
		// �������
		data.writeByte(ofs.length);
		for(int i=0;i<ofs.length;i++)
		{
			if(ofs[i]!=null)
			{
				data.writeBoolean(true);
				ofs[i].bytesWrite(data);
			}
			else
			{
				data.writeBoolean(false);
			}
		}
	}
	
	/** ���ٶ� */
	public void bytesReadOFS(ByteBuffer data)
	{
		// �������
		int len=data.readUnsignedByte();
		ofs=new Officer[len];
		for(int i=0;i<len;i++)
		{
			if(!data.readBoolean()) continue;
			int id=data.readInt();
			int sid=data.readUnsignedShort();
			ofs[i]=(Officer)OfficerManager.factory.newSample(sid);
			ofs[i].setId(id);
			ofs[i].setMilitaryRank(data.readUnsignedByte());
			ofs[i].setLevel(data.readUnsignedByte());
			ofs[i].setExp(data.readInt());
		}
	}
	
	public void setDefFleet()
	{
		attacklist.add(10001);
		attacklist.add(1);
		attacklist.add(0);
		defencelist.add(10001);
		defencelist.add(1);
		defencelist.add(0);
	}
	
	public ByteBuffer showBytesWrite(ByteBuffer data)
	{
		data.writeInt(getCrossid());
		data.writeInt(getId());
		data.writeShort(getSid());
		data.writeInt(getPlatid());
		data.writeInt(getAreaid());
		data.writeInt(getSeverid());
		data.writeUTF(getName());
		data.writeUTF(getAname());
		data.writeUTF(getSeverName());
		data.writeUTF(getNational());
		data.writeShort(num);
		data.writeShort(rank);
//		System.out.println("-----showBytesWrite------rank------:"+rank);
		data.writeInt(fightscore);
		//data.writeInt(bet);
		//setFlushBet(false);
		return data;
	}
	
	public void showBytesWriteBet(ByteBuffer data)
	{
		data.writeInt(getCrossid());
		data.writeInt(bet);
		setFlushBet(false);
	}
	
}
