package foxu.sea.officer;

import foxu.sea.Player;
import foxu.sea.Ship;
import foxu.sea.fight.Fleet;
import foxu.sea.officer.effect.EffectExecutable;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;

/**
 * 随军军官指挥部
 * 
 * @author Alan
 */
public class OfficerBattleHQ
{

	public static final int ARMY=1,ENEMY=2;
	public static final int CURRENT_LOCATION=-1;
	/** 组合属性容器 */
	ArrayList attrs=new ArrayList();
	/** 组合技容器 */
	ArrayList skills=new ArrayList();
	/** 随军军官信息(用于战报) */
	OfficerInfo[] usedOfficers={};

	/* methods */
	/** 清除数据 */
	public void clear()
	{
		attrs.clear();
		skills.clear();
	}

	/** 初始化随军军官信息 */
	public void initOfficers(Player player)
	{
		clear();
		resetOfficersAttr(player.getOfficers().getUsingOfficers());
		resetUnitedEffects(OfficerManager.getInstance()
			.getEffectsFromOfficers(player).toArray());
	}

	/** 重置随军军官属性 */
	public void resetOfficersAttr(Officer[] officers)
	{
		usedOfficers=new OfficerInfo[officers.length];
		for(int i=0;i<officers.length;i++)
		{
			if(officers[i]==null) continue;
			usedOfficers[i]=new OfficerInfo(officers[i]);
			officers[i].effectExecute(i,this);
		}
	}

	/** 重置随军军官影响 */
	public void resetUnitedEffects(Object[] effects)
	{
		EffectExecutable ee;
		for(int i=0;i<effects.length;i++)
		{
			ee=(EffectExecutable)effects[i];
			ee.effectExecute(this);
		}
	}

	/**
	 * 添加军官属性 因为存在相应坑位这个不确定值,暂时只能逐一记录而非累计
	 * 
	 * @param group 影响阵营 (我方or敌方)
	 * @param field 影响位置 (舰船类型or相应坑位)
	 * @param attr 影响属性 (四维or统御、航速、载重)
	 * @param activyLocations 激活源坐标
	 */
	public void addAttr(int group,int field,int attr,boolean isFix,
		float value,IntList activyLocations)
	{
		AttrData ad=new AttrData();
		ad.activyLocations=activyLocations;
		ad.group=group;
		ad.field=field;
		ad.attr=attr;
		if(isFix)
			ad.fix=value;
		else
			ad.percent=value;
		attrs.add(ad);
	}

	/**
	 * 添加军官技能 因为存在相应坑位这个不确定值,暂时只能逐一记录而非累计
	 * 
	 * @param group 影响阵营 (我方or敌方)
	 * @param field 影响位置 (舰船类型or相应坑位)
	 * @param sid 影响技能(若影响坑位则此处填技能类型)
	 * @param percent 影响概率值
	 * @param activyLocations 激活源坐标
	 */
	public void addSkill(int group,int field,int sid,int percent,
		IntList activyLocations)
	{
		SkillData sd=new SkillData();
		sd.group=group;
		sd.field=field;
		sd.sid=sid;
		sd.percent=percent;
		sd.activyLocations=activyLocations;
		skills.add(sd);
	}

	/** 初始化友军的军官效果,传入IntList将写入技能影响[技能sid,影响概率] */
	public void initArmyFleet(Fleet fleet,IntList list)
	{
		// 我方增益属性
		initAttrs(fleet,ARMY);
		// 我方增益技能
		initSkills(fleet,ARMY,list);
	}

	/** 初始化敌军的军官效果,传入IntList将写入技能影响[技能sid,影响概率] */
	public void initEnemyFleet(Fleet fleet,IntList list)
	{
		// 敌方减益属性
		initAttrs(fleet,ENEMY);
		// 敌方减益技能
		initSkills(fleet,ENEMY,list);
	}

	public IntList initSkills(Fleet fleet,int field,IntList list)
	{
		for(int i=0;i<skills.size();i++)
		{
			SkillData sd=(SkillData)skills.get(i);
			// 对相应方有效的技能
			if(sd.group!=field) continue;
			// 如果是针对相应坑位的
			if(sd.field==CURRENT_LOCATION)
			{
				if(sd.activyLocations.contain(fleet.getLocation()))
				{
					// 找到相应类型技能的各舰船对应技能sid(如[连击])
					int[] skillTypes=(int[])OfficerManager.SKILL_TYPE_SIDS
						.get(sd.sid);
					for(int k=0;k<skillTypes.length;k+=2)
					{
						if(fleet.getType()==skillTypes[k])
						{
							// 技能
							int index=getIndexBySameSid(list,skillTypes[k+1]);
							if(index<0)
							{
								list.add(skillTypes[k+1]);
								list.add(sd.percent);
							}
							else
								list.set(list.get(index+1)+sd.percent,
									index+1);
						}
					}
				}
			}
			// 如果是针对相应舰船类型的
			else
			{
				if(sd.field!=Ship.ALL_SHIP&&sd.field!=fleet.getType())
					continue;
				// 技能
				int index=getIndexBySameSid(list,sd.sid);
				if(index<0)
				{
					list.add(sd.sid);
					list.add(sd.percent);
				}
				else
					list.set(list.get(index+1)+sd.percent,index+1);
			}

		}
		return list;
	}

	private int getIndexBySameSid(IntList list,int sid)
	{
		for(int i=0;i<list.size();i+=2)
		{
			if(list.get(i)==sid)
				return i;
		}
		return -1;
	}
	
	public void initAttrs(Fleet fleet,int field)
	{
		for(int i=0;i<attrs.size();i++)
		{
			AttrData ad=(AttrData)attrs.get(i);
			// 对相应有效的属性
			if(ad.group!=field) continue;
			// 如果是针对相应坑位的
			if(ad.field==CURRENT_LOCATION)
			{
				if(ad.activyLocations.contain(fleet.getLocation()))
				{
					fleet.resetFleetAdjust(ad.attr,true,ad.fix);
					fleet.resetFleetAdjust(ad.attr,false,ad.percent);
				}
			}
			// 如果是针对相应舰船类型的
			else
			{
				if(ad.field!=Ship.ALL_SHIP&&ad.field!=fleet.getType())
					continue;
				// 属性
				fleet.resetFleetAdjust(ad.attr,true,ad.fix);
				fleet.resetFleetAdjust(ad.attr,false,ad.percent);
			}

		}
	}

	/**
	 * 获取非战斗属性加成(如航速,载重,带兵量)<br>
	 * 当field表示针对坑位有效时,需要传入对应的location
	 */
	public float getCommonAttr(int group,int field,int attr,boolean isFix,
		int location)
	{
		float count=0;
		for(int i=0;i<attrs.size();i++)
		{
			AttrData ad=(AttrData)attrs.get(i);
			if(ad.group!=group) continue;
			if(ad.field!=field) continue;
			if(ad.attr!=attr) continue;
			if(field==CURRENT_LOCATION
				&&!ad.activyLocations.contain(location)) continue;
			if(isFix)
				count+=ad.fix;
			else
				count+=ad.percent;
		}
		return count;
	}

	/** 随军军官序列化 */
	public void showBytesWriteOfficers(ByteBuffer data)
	{
		int top=data.top();
		int len=0;
		data.writeInt(len);
		IntList temp=new IntList();
		for(int i=0;i<usedOfficers.length;i++)
		{
			if(usedOfficers[i]==null) continue;
			usedOfficers[i].showBytesWrite(data);
			len++;
			temp.add(usedOfficers[i].id);
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
	
	public synchronized Object bytesRead(ByteBuffer data)
	{
		// 属性
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			attrs.add(bytesReadAttrData(data));
		}
		// 技能
		len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			skills.add(bytesReadSkillData(data));
		}
		// 军官信息
		usedOfficers=new OfficerInfo[data.readUnsignedByte()];
		OfficerInfo info;
		for(int i=0;i<usedOfficers.length;i++)
		{
			info=bytesReadOfficerInfo(data);
			// 没有信息
			if(info.id!=0)
				usedOfficers[i]=info;
		}
		return this;
	}
	
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public synchronized void bytesWrite(ByteBuffer data)
	{
		// 属性
		data.writeShort(attrs.size());
		AttrData attr;
		for(int i=0;i<attrs.size();i++)
		{
			attr=(AttrData)attrs.get(i);
			attr.bytesWrite(data);
		}
		// 技能
		data.writeShort(skills.size());
		SkillData skill;
		for(int i=0;i<skills.size();i++)
		{
			skill=(SkillData)skills.get(i);
			skill.bytesWrite(data);
		}
		// 军官信息
		data.writeByte(usedOfficers.length);
		OfficerInfo info;
		for(int i=0;i<usedOfficers.length;i++)
		{
			info=usedOfficers[i];
			if(info==null)
				info=new OfficerInfo();
			info.bytesWrite(data);
		}
	}

	public class AttrData
	{

		public int group;
		public int field;
		public int attr;
		public float fix;
		public float percent;
		public IntList activyLocations;
		public void bytesWrite(ByteBuffer data)
		{
			data.writeByte(group);
			data.writeShort(field);
			data.writeShort(attr);
			data.writeFloat(fix);
			data.writeFloat(percent);
			int len=0;
			if(activyLocations!=null)
				len=activyLocations.size();
			data.writeByte(len);
			for(int i=0;i<len;i++)
			{
				data.writeByte(activyLocations.get(i));
			}
		}
	}

	public class SkillData
	{

		public int group;
		public int field;
		public int sid;
		public int percent;
		public IntList activyLocations;
		public void bytesWrite(ByteBuffer data)
		{
			data.writeByte(group);
			data.writeShort(field);
			data.writeShort(sid);
			data.writeShort(percent);
			int len=0;
			if(activyLocations!=null)
				len=activyLocations.size();
			data.writeByte(len);
			for(int i=0;i<len;i++)
			{
				data.writeByte(activyLocations.get(i));
			}
		}
	}

	public OfficerInfo bytesReadOfficerInfo(ByteBuffer data)
	{
		OfficerInfo info=new OfficerInfo();
		info.id=data.readInt();
		info.sid=data.readUnsignedShort();
		info.militaryRank=data.readUnsignedByte();
		info.level=data.readUnsignedByte();
		info.exp=data.readInt();
		return info;
	}
	public SkillData bytesReadSkillData(ByteBuffer data)
	{
		SkillData skill=new SkillData();
		skill.group=data.readByte();
		skill.field=data.readShort();
		skill.sid=data.readShort();
		skill.percent=data.readUnsignedShort();
		int len=data.readUnsignedByte();
		if(len>0)
			skill.activyLocations=new IntList();
		for(int i=0;i<len;i++)
		{
			skill.activyLocations.add(data.readUnsignedByte());
		}
		return skill;
	}
	public AttrData bytesReadAttrData(ByteBuffer data)
	{
		AttrData attr=new AttrData();
		attr.group=data.readByte();
		attr.field=data.readShort();
		attr.attr=data.readShort();
		attr.fix=data.readFloat();
		attr.percent=data.readFloat();
		int len=data.readUnsignedByte();
		if(len>0)
			attr.activyLocations=new IntList();
		for(int i=0;i<len;i++)
		{
			attr.activyLocations.add(data.readUnsignedByte());
		}
		return attr;
	}

	public OfficerInfo[] getUsedOfficers()
	{
		return usedOfficers;
	}

	public void setUsedOfficers(OfficerInfo[] usedOfficers)
	{
		this.usedOfficers=usedOfficers;
	}
	
	
}
