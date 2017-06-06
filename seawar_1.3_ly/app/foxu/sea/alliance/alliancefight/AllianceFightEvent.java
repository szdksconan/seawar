package foxu.sea.alliance.alliancefight;

import foxu.sea.alliance.Alliance;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;

/**
 * 盟战事件
 * 
 * @author yw
 * 
 */
public class AllianceFightEvent
{

	// 战斗类型： 击败后占领A(被驱逐)1，攻击A失败(防守成功)2 奖励类型：10升级点3 +5基础产量4-5
	public static int WIN=1,LOSE=2,UPPOINT=3,RESOURCE1=4,RESOURCE2=5;
	/** 删除记数（可能 1 2 3） */
	int deleteCount;
	/** 唯一ID */
	int uid;
	/** 事件类型 */
	int type;
	/** 发生地 */
	int battleId;
	/** 产生时间 */
	int createTime;

	// 攻击类型时
	/** 攻击者 */
	int attackId;
	/** 被攻击者 */
	int defId;
	/** 战报 */
	ByteBuffer fightData;
	// 奖励类型时
	/** 加成技能sid */
	int skillsid;
	/** 加成值 */
	int addValue;
	/** 攻击玩家名 */
	String aName;
	/** 损耗号角 */
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
	/** 减小删除记数 */
	public synchronized void decrCount()
	{
		deleteCount--;
	}

	/** 从字节数组中反序列化获得对象的域 */
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
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
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

	/** 序列化给前台 */
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
