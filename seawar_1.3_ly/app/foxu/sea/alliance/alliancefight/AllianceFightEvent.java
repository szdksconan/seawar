package foxu.sea.alliance.alliancefight;

import foxu.sea.alliance.Alliance;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;

/**
 * ��ս�¼�
 * 
 * @author yw
 * 
 */
public class AllianceFightEvent
{

	// ս�����ͣ� ���ܺ�ռ��A(������)1������Aʧ��(���سɹ�)2 �������ͣ�10������3 +5��������4-5
	public static int WIN=1,LOSE=2,UPPOINT=3,RESOURCE1=4,RESOURCE2=5;
	/** ɾ������������ 1 2 3�� */
	int deleteCount;
	/** ΨһID */
	int uid;
	/** �¼����� */
	int type;
	/** ������ */
	int battleId;
	/** ����ʱ�� */
	int createTime;

	// ��������ʱ
	/** ������ */
	int attackId;
	/** �������� */
	int defId;
	/** ս�� */
	ByteBuffer fightData;
	// ��������ʱ
	/** �ӳɼ���sid */
	int skillsid;
	/** �ӳ�ֵ */
	int addValue;
	/** ��������� */
	String aName;
	/** ��ĺŽ� */
	int dcrHorn;

	public void init(int type,int battleId,int attackId,int defId,
		ByteBuffer fightData,int addValue,int skillsid,String aname,int dcrhorn)
	{
		createTime=TimeKit.getSecondTime();
		this.type=type;
		this.battleId=battleId;
		this.attackId=attackId;
		this.defId=defId;
		this.fightData=fightData;
		this.addValue=addValue;
		this.skillsid=skillsid;
		if(attackId!=0)deleteCount++;
		if(defId!=0)deleteCount++;
		if(type==WIN)deleteCount++;
		this.aName=aname;
		this.dcrHorn=dcrhorn;
		
	}
	/** ��Сɾ������ */
	public synchronized void decrCount()
	{
		deleteCount--;
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		deleteCount=data.readUnsignedByte();
		uid=data.readInt();
		type=data.readUnsignedByte();
		battleId=data.readUnsignedShort();
		createTime=data.readInt();
		addValue=data.readUnsignedByte();
		skillsid=data.readUnsignedShort();
		if(data.readBoolean())
		{
			fightData=new ByteBuffer(data.readData());
		}
		aName=data.readUTF();
		dcrHorn=data.readUnsignedByte();
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(deleteCount);
		data.writeInt(uid);
		data.writeByte(type);
		data.writeShort(battleId);
		data.writeInt(createTime);
		data.writeByte(addValue);
		data.writeShort(skillsid);
		if(fightData!=null)
		{
			data.writeBoolean(true);
			data.writeData(fightData.toArray(),fightData.offset(),fightData.length());
		}
		else
		{
			data.writeBoolean(false);
		}
		data.writeUTF(aName);
		data.writeByte(dcrHorn);
	}

	public Object bytesReadFightdata(ByteBuffer data)
	{
		fightData=new ByteBuffer(data.readData());
//		System.out.println(uid+"------fightData-----read-----::::"+fightData.length());
		return this;
	}
	public void bytesWriteFightdata(ByteBuffer data)
	{
		if(fightData==null) return;
//		System.out.println(uid+"-------fightData---write------::::"+fightData.length());
		data.writeData(fightData.toArray(),fightData.offset(),fightData.length());
	}

	/** ���л���ǰ̨ */
	public void showBytesWrite(AllianceFightManager amanager,ByteBuffer data)
	{
		data.writeInt(uid);
		data.writeByte(type);
//		System.out.println(uid+"-------enent------type-----:"+type);
		data.writeInt(createTime);
		data.writeShort(battleId);
		data.writeByte(addValue);
		data.writeShort(skillsid);
		
		Alliance attack=amanager.getAlliance(attackId);
		if(attack!=null)
		{
			data.writeUTF(attack.getName());
//			System.out.println("--------attack.getName()-------:"+attack.getName());
		}
		else
		{
			data.writeUTF("");
		}
		Alliance def=amanager.getAlliance(defId);
		if(def!=null)
		{
			data.writeUTF(def.getName());
//			System.out.println("--------def.getName()-------:"+def.getName());
		}
		else
		{
			data.writeUTF("");
		}
		if(fightData!=null)
		{
			data.writeBoolean(true);
		}
		else
		{
			data.writeBoolean(false);
		}
		data.writeUTF(aName);
		data.writeByte(dcrHorn);
	}

	public int getDeleteCount()
	{
		return deleteCount;
	}

	public void setDeleteCount(int deleteCount)
	{
		this.deleteCount=deleteCount;
	}

	public int getUid()
	{
		return uid;
	}

	public void setUid(int uid)
	{
		this.uid=uid;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type=type;
	}

	public int getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(int createTime)
	{
		this.createTime=createTime;
	}

	public ByteBuffer getFightData()
	{
		return fightData;
	}

	public void setFightData(ByteBuffer fightData)
	{
		this.fightData=fightData;
	}

	public int getAddValue()
	{
		return addValue;
	}

	public void setAddValue(int addValue)
	{
		this.addValue=addValue;
	}

	public int getBattleId()
	{
		return battleId;
	}

	public void setBattleId(int battleId)
	{
		this.battleId=battleId;
	}

	public int getAttackId()
	{
		return attackId;
	}

	public void setAttackId(int attackId)
	{
		this.attackId=attackId;
	}

	public int getDefId()
	{
		return defId;
	}

	public void setDefId(int defId)
	{
		this.defId=defId;
	}
	
	public int getSkillsid()
	{
		return skillsid;
	}
	
	public void setSkillsid(int skillsid)
	{
		this.skillsid=skillsid;
	}
	
	public String getaName()
	{
		return aName;
	}
	
	public void setaName(String aName)
	{
		this.aName=aName;
	}
	
	public int getDcrHorn()
	{
		return dcrHorn;
	}
	
	public void setDcrHorn(int dcrHorn)
	{
		this.dcrHorn=dcrHorn;
	}
	

}
