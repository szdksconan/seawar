package foxu.cross.war;

import foxu.sea.officer.OfficerInfo;
import mustang.io.ByteBuffer;
import mustang.set.IntList;

/**
 * 跨服战斗结果
 * 
 * @author yw
 * 
 */
public class CrossWarFightSave
{
	/** 攻击方出战总数 */
	int attackSum;
	/** 攻方战损 */
	int attackLose;
	/** 防守方出战总数 */
	int defenceSum;
	/** 防方战损 */
	int defenceLose;
	/** 攻击方是否胜利 */
	boolean attackWin;
	/** 攻方战损 */
	IntList alist=new IntList();
	/** 防方战损 */
	IntList dlist=new IntList();
	/** 攻防军官信息 */
	OfficerInfo[] attackOfficers={};
	/** 防方军官信息 */
	OfficerInfo[] defendOfficers={};
	/** 战报 */
	ByteBuffer record;

	/**
	 * 计算战损比
	 * @param isattack 是否是攻击方
	 */
	public float getLosePercent(boolean isattack)
	{
		int lose=0;
		int sum=0;
		if(isattack)
		{
			lose=attackLose;
			sum=attackSum;
		}
		else
		{
			lose=defenceLose;
			sum=defenceSum;
		}
		if(sum==0) return 1f;
		float lp=lose/((float)sum);
		return (Math.round(lp*10000)/10000f);//
	}

	public void showBytesWrite(ByteBuffer data)
	{
		data.writeInt(attackSum);
		data.writeInt(attackLose);
		data.writeInt(defenceSum);
		data.writeInt(defenceLose);
		data.writeBoolean(attackWin);
		data.writeByte(alist.size()/2);
		for(int i=0;i<alist.size();i+=2)
		{
			data.writeShort(alist.get(i));
			data.writeShort(alist.get(i+1));
		}
		data.writeByte(dlist.size()/2);
		for(int i=0;i<dlist.size();i+=2)
		{
			data.writeShort(dlist.get(i));
			data.writeShort(dlist.get(i+1));
		}
		showByteWriteOFS(data,attackOfficers);
		showByteWriteOFS(data,defendOfficers);
		if(record==null)
		{
			data.writeBoolean(false);
		}
		else
		{
			data.writeBoolean(true);
			data.writeData(record.toArray());
		}
	}

	public void showBytesRead(ByteBuffer data)
	{
		attackSum=data.readInt();
		attackLose=data.readInt();
		defenceSum=data.readInt();
		defenceLose=data.readInt();
		attackWin=data.readBoolean();
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			alist.add(data.readUnsignedShort());
			alist.add(data.readUnsignedShort());
		}
		len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			dlist.add(data.readUnsignedShort());
			dlist.add(data.readUnsignedShort());
		}
		attackOfficers=showByteReadOFS(data);
		defendOfficers=showByteReadOFS(data);
		if(data.readBoolean())
		{
			record=new ByteBuffer(data.readData());
		}
	}

	public void showByteWriteOFS(ByteBuffer data,OfficerInfo[] usedOfficers)
	{
		data.writeByte(usedOfficers.length);
		for(int i=0;i<usedOfficers.length;i++)
		{
			if(usedOfficers[i]==null)
			{
				data.writeBoolean(false);
			}
			else
			{
				data.writeBoolean(true);
				usedOfficers[i].showBytesWrite(data);
			}
		}
	}

	public OfficerInfo[] showByteReadOFS(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		OfficerInfo[] usedOfficers=new OfficerInfo[len];
		for(int i=0;i<len;i++)
		{
			if(!data.readBoolean()) continue;
			usedOfficers[i]=new OfficerInfo();
			usedOfficers[i].showBytesRead(data);
		}
		return usedOfficers;
	}
	
	/** 随军军官序列化 */
	public void showBytesWriteOfficers(ByteBuffer data,OfficerInfo[] ofs)
	{
		int top=data.top();
		int len=0;
		data.writeInt(len);
		IntList temp=new IntList();
		for(int i=0;i<ofs.length;i++)
		{
			if(ofs[i]==null) continue;
			ofs[i].showBytesWrite(data);
			len++;
			temp.add(ofs[i].getId());
			temp.add(i+1);
		}
		int newTop=data.top();
		data.setTop(top);
		data.writeInt(len);
		data.setTop(newTop);
		for(int i=0;i<temp.size();i+=2)
		{
			data.writeInt(temp.get(i));
			data.writeShort(temp.get(i+1));
		}
	}

	public int getAttackLose()
	{
		return attackLose;
	}

	public void setAttackLose(int attackLose)
	{
		this.attackLose=attackLose;
	}

	public int getDefenceLose()
	{
		return defenceLose;
	}

	public void setDefenceLose(int defenceLose)
	{
		this.defenceLose=defenceLose;
	}

	public boolean isAttackWin()
	{
		return attackWin;
	}

	public void setAttackWin(boolean attackWin)
	{
		this.attackWin=attackWin;
	}

	public ByteBuffer getRecord()
	{
		return record;
	}

	public void setRecord(ByteBuffer record)
	{
		this.record=record;
	}

	public IntList getAlist()
	{
		return alist;
	}

	public void setAlist(IntList alist)
	{
		this.alist=alist;
	}

	public IntList getDlist()
	{
		return dlist;
	}

	public void setDlist(IntList dlist)
	{
		this.dlist=dlist;
	}

	public int getAttackSum()
	{
		return attackSum;
	}

	public void setAttackSum(int attackSum)
	{
		this.attackSum=attackSum;
	}

	public int getDefenceSum()
	{
		return defenceSum;
	}

	public void setDefenceSum(int defenceSum)
	{
		this.defenceSum=defenceSum;
	}

	public OfficerInfo[] getAttackOfficers()
	{
		return attackOfficers;
	}

	public void setAttackOfficers(OfficerInfo[] attackOfficers)
	{
		this.attackOfficers=attackOfficers;
	}

	public OfficerInfo[] getDefendOfficers()
	{
		return defendOfficers;
	}

	public void setDefendOfficers(OfficerInfo[] defendOfficers)
	{
		this.defendOfficers=defendOfficers;
	}

}
