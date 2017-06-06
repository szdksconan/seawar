package foxu.cross.warclient;

import foxu.cross.war.CrossWar;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerManager;
import foxu.sea.port.UserToCenterPort;
import mustang.io.ByteBuffer;
import mustang.set.IntList;


/**
 * ���ս�ͷ������
 * @author yw
 *
 */
public class ClientWarPlayer
{
	/** ���Ψһid */
	int crossid;
	/** ���սid */
	int warid;
	/** Ψһid */
	int id;
	/** sid */ 
	int sid;
	/** ƽ̨id */
	int platid;
	/** ����id */
	int areaid;
	/** ������id */
	int serverid;
	/** �������� */
	String sname;
	/** ������ */
	String aname;
	/** ���� */ 
	String national;
	/** ����� */
	String name;
	/** ��� */
	int num;
	/** nǿ */
	int rank;
	/** ս�� */
	int fightscore;
	/** �������� */
	IntList attacklist=new IntList();
	/** �������� */
	IntList defencelist=new IntList();
	/** ��ս���� */
	Officer[] ofs;
	/** ��Ѻע�� */
	int bet;

	/** �Ƿ��� */
	boolean send=true;

	/** ����ע */
	public synchronized void addBet(int gems)
	{
		if(gems<=0) return;
		bet+=gems;
		if(bet<0) bet=Integer.MAX_VALUE;
	}
	public boolean isSend()
	{
		return send;
	}

	public void setSend(boolean send)
	{
		this.send=send;
	}
	/** �Ƿ���ȡ���� */
	public boolean isGetAward()
	{
		// todo
		return false;
	}
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}

	public int getCrossid()
	{
		return crossid;
	}

	public void setCrossid(int crossid)
	{
		this.crossid=crossid;
	}

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
	public void bytesWriteAttacklist(ByteBuffer data)
	{
		int len=attacklist.size()/3;
		data.writeByte(len);
		// //System.out.println("------len-------:"+len);
		for(int i=0;i<attacklist.size();i+=3)
		{
			data.writeShort(attacklist.get(i));
			// //System.out.println("------sid-------:"+attacklist.get(i));
			data.writeShort(attacklist.get(i+1));
			// //System.out.println("------num-------:"+attacklist.get(i+1));
			data.writeByte(attacklist.get(i+2));
			// //System.out.println("------loc-------:"+attacklist.get(i+2));
		}
	}
	public boolean bytesReadAttacklist(ByteBuffer data)
	{
		IntList list=new IntList();
		int length=data.readUnsignedByte();
		int all_num=0;
		for(int i=0;i<length;i++)
		{
			int sid=data.readUnsignedShort();
			int num=data.readUnsignedShort();
			int location=data.readUnsignedByte();
			all_num+=num;
			list.add(sid);
			list.add(num);
			list.add(location);
		}
		if(all_num>0)
		{
			attacklist.clear();
			attacklist=list;
			return true;
		}
		return false;

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
	public boolean bytesReadDefencelist(ByteBuffer data)
	{
		IntList list=new IntList();
		int length=data.readUnsignedByte();
		int all_num=0;
		for(int i=0;i<length;i++)
		{
			int sid=data.readUnsignedShort();
			int num=data.readUnsignedShort();
			int location=data.readUnsignedByte();
			all_num+=num;
			list.add(sid);
			list.add(num);
			list.add(location);
		}
		if(all_num>0)
		{
			defencelist.clear();
			defencelist=list;
			return true;
		}
		return false;
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
	
	/** �������� */
	public void correctList(boolean attck,boolean defence)
	{
		if(attck&&defence) return;
		if(attck)
		{
			defencelist=(IntList)attacklist.clone();
		}
		else if(defence)
		{
			attacklist=(IntList)defencelist.clone();
		}

	}
	/** ����Ƿ񱾷���� */
	public boolean isLocal()
	{
		return platid==UserToCenterPort.PLAT_ID
			&&areaid==UserToCenterPort.AREA_ID
			&&serverid==UserToCenterPort.SERVER_ID;
	}
	/** ��ȡ���������� */
	public void showBytesRead(ByteBuffer data)
	{
		id=data.readInt();
		sid=data.readUnsignedShort();
		platid=data.readInt();
		areaid=data.readInt();
		serverid=data.readInt();
		name=data.readUTF();
		aname=data.readUTF();
		sname=data.readUTF();
		national=data.readUTF();
//		//System.out.println("----showBytesRead--sname----------:"+sname);
		num=data.readUnsignedShort();
		rank=data.readUnsignedShort();
//		//System.out.println(name+":----showBytesRead-----rank------:"+rank);
		fightscore=data.readInt();
		// bet=data.readInt();
	}

	/** ��ȡ����������Ѻע�� */
	public void showBytesReadBet(ByteBuffer data)
	{
		bet=data.readInt();
	}

	/** ���л���ǰ̨ */
	public void clientBytesWrite(ByteBuffer data)
	{
		data.writeInt(crossid);
		data.writeUTF(name);
		data.writeUTF(sname);
		data.writeUTF(national);
//		//System.out.println(name+"----clientBytesWrite--name----------:"+rank);
		data.writeUTF("player_head1.png");
		data.writeInt(fightscore);
//		data.writeByte(trnasType(rank));// ��������
		data.writeInt(transWinCount());
	}

	/** ��ǿ��תΪʤ������ */
	public int transWinCount()
	{
		int wincount=0;
		if(rank==CrossWar.S32)
		{
			wincount=1;
		}
		else if(rank==CrossWar.S16)
		{
			wincount=2;
		}
		else if(rank==CrossWar.S8)
		{
			wincount=3;
		}
		else if(rank==CrossWar.S4)
		{
			wincount=4;
		}
		else if(rank==CrossWar.S2)
		{
			wincount=5;
		}
		else if(rank==CrossWar.S1)
		{
			wincount=6;
		}
		return wincount;
	}

//	/** ת���������� */
//	public int trnasType(int type)
//	{
//		if(type==CrossWar.S64)
//		{
//			type=1;
//		}
//		else if(type==CrossWar.S32)
//		{
//			type=2;
//		}
//		else if(type==CrossWar.S16)
//		{
//			type=3;
//		}
//		else if(type==CrossWar.S8)
//		{
//			type=4;
//		}
//		else if(type==CrossWar.S4)
//		{
//			type=5;
//		}
//		else if(type==CrossWar.S2)
//		{
//			type=6;
//		}
//		else if(type==CrossWar.S1)
//		{
//			type=7;
//		}
//		return type;
//	}

	public int getRank()
	{
		return rank;
	}

	public void setRank(int rank)
	{
		this.rank=rank;
	}

	public int getNum()
	{
		return num;
	}

	public void setNum(int num)
	{
		this.num=num;
	}

	public int getBet()
	{
		return bet;
	}

	public void setBet(int bet)
	{
		this.bet=bet;
	}

	public int getFightscore()
	{
		return fightscore;
	}

	public void setFightscore(int fightscore)
	{
		this.fightscore=fightscore;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name=name;
	}

	public String getSname()
	{
		return sname;
	}

	public void setSname(String sname)
	{
		this.sname=sname;
	}

	public int getPlatid()
	{
		return platid;
	}

	public void setPlatid(int platid)
	{
		this.platid=platid;
	}

	public int getAreaid()
	{
		return areaid;
	}

	public void setAreaid(int areaid)
	{
		this.areaid=areaid;
	}

	public int getServerid()
	{
		return serverid;
	}

	public void setServerid(int serverid)
	{
		this.serverid=serverid;
	}
	
	public int getWarid()
	{
		return warid;
	}
	
	public void setWarid(int warid)
	{
		this.warid=warid;
	}
	
	public String getAname()
	{
		return aname;
	}
	
	public void setAname(String aname)
	{
		this.aname=aname;
	}
	
	public int getSid()
	{
		return sid;
	}
	
	public void setSid(int sid)
	{
		this.sid=sid;
	}
	
	public String getNational()
	{
		return national;
	}
	
	public void setNational(String national)
	{
		this.national=national;
	}
	
	public Officer[] getOfs()
	{
		return ofs;
	}
	
	public void setOfs(Officer[] ofs)
	{
		this.ofs=ofs;
	}
}
