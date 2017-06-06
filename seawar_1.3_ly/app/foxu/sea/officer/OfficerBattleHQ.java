package foxu.sea.officer;

import foxu.sea.Player;
import foxu.sea.Ship;
import foxu.sea.fight.Fleet;
import foxu.sea.officer.effect.EffectExecutable;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;

/**
 * �������ָ�Ӳ�
 * 
 * @author Alan
 */
public class OfficerBattleHQ
{

	public static final int ARMY=1,ENEMY=2;
	public static final int CURRENT_LOCATION=-1;
	/** ����������� */
	ArrayList attrs=new ArrayList();
	/** ��ϼ����� */
	ArrayList skills=new ArrayList();
	/** ���������Ϣ(����ս��) */
	OfficerInfo[] usedOfficers={};

	/* methods */
	/** ������� */
	public void clear()
	{
		attrs.clear();
		skills.clear();
	}

	/** ��ʼ�����������Ϣ */
	public void initOfficers(Player player)
	{
		clear();
		resetOfficersAttr(player.getOfficers().getUsingOfficers());
		resetUnitedEffects(OfficerManager.getInstance()
			.getEffectsFromOfficers(player).toArray());
	}

	/** ��������������� */
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

	/** �����������Ӱ�� */
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
	 * ��Ӿ������� ��Ϊ������Ӧ��λ�����ȷ��ֵ,��ʱֻ����һ��¼�����ۼ�
	 * 
	 * @param group Ӱ����Ӫ (�ҷ�or�з�)
	 * @param field Ӱ��λ�� (��������or��Ӧ��λ)
	 * @param attr Ӱ������ (��άorͳ�������١�����)
	 * @param activyLocations ����Դ����
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
	 * ��Ӿ��ټ��� ��Ϊ������Ӧ��λ�����ȷ��ֵ,��ʱֻ����һ��¼�����ۼ�
	 * 
	 * @param group Ӱ����Ӫ (�ҷ�or�з�)
	 * @param field Ӱ��λ�� (��������or��Ӧ��λ)
	 * @param sid Ӱ�켼��(��Ӱ���λ��˴��������)
	 * @param percent Ӱ�����ֵ
	 * @param activyLocations ����Դ����
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

	/** ��ʼ���Ѿ��ľ���Ч��,����IntList��д�뼼��Ӱ��[����sid,Ӱ�����] */
	public void initArmyFleet(Fleet fleet,IntList list)
	{
		// �ҷ���������
		initAttrs(fleet,ARMY);
		// �ҷ����漼��
		initSkills(fleet,ARMY,list);
	}

	/** ��ʼ���о��ľ���Ч��,����IntList��д�뼼��Ӱ��[����sid,Ӱ�����] */
	public void initEnemyFleet(Fleet fleet,IntList list)
	{
		// �з���������
		initAttrs(fleet,ENEMY);
		// �з����漼��
		initSkills(fleet,ENEMY,list);
	}

	public IntList initSkills(Fleet fleet,int field,IntList list)
	{
		for(int i=0;i<skills.size();i++)
		{
			SkillData sd=(SkillData)skills.get(i);
			// ����Ӧ����Ч�ļ���
			if(sd.group!=field) continue;
			// ����������Ӧ��λ��
			if(sd.field==CURRENT_LOCATION)
			{
				if(sd.activyLocations.contain(fleet.getLocation()))
				{
					// �ҵ���Ӧ���ͼ��ܵĸ�������Ӧ����sid(��[����])
					int[] skillTypes=(int[])OfficerManager.SKILL_TYPE_SIDS
						.get(sd.sid);
					for(int k=0;k<skillTypes.length;k+=2)
					{
						if(fleet.getType()==skillTypes[k])
						{
							// ����
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
			// ����������Ӧ�������͵�
			else
			{
				if(sd.field!=Ship.ALL_SHIP&&sd.field!=fleet.getType())
					continue;
				// ����
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
			// ����Ӧ��Ч������
			if(ad.group!=field) continue;
			// ����������Ӧ��λ��
			if(ad.field==CURRENT_LOCATION)
			{
				if(ad.activyLocations.contain(fleet.getLocation()))
				{
					fleet.resetFleetAdjust(ad.attr,true,ad.fix);
					fleet.resetFleetAdjust(ad.attr,false,ad.percent);
				}
			}
			// ����������Ӧ�������͵�
			else
			{
				if(ad.field!=Ship.ALL_SHIP&&ad.field!=fleet.getType())
					continue;
				// ����
				fleet.resetFleetAdjust(ad.attr,true,ad.fix);
				fleet.resetFleetAdjust(ad.attr,false,ad.percent);
			}

		}
	}

	/**
	 * ��ȡ��ս�����Լӳ�(�纽��,����,������)<br>
	 * ��field��ʾ��Կ�λ��Чʱ,��Ҫ�����Ӧ��location
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

	/** ����������л� */
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
		// ����
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			attrs.add(bytesReadAttrData(data));
		}
		// ����
		len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			skills.add(bytesReadSkillData(data));
		}
		// ������Ϣ
		usedOfficers=new OfficerInfo[data.readUnsignedByte()];
		OfficerInfo info;
		for(int i=0;i<usedOfficers.length;i++)
		{
			info=bytesReadOfficerInfo(data);
			// û����Ϣ
			if(info.id!=0)
				usedOfficers[i]=info;
		}
		return this;
	}
	
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public synchronized void bytesWrite(ByteBuffer data)
	{
		// ����
		data.writeShort(attrs.size());
		AttrData attr;
		for(int i=0;i<attrs.size();i++)
		{
			attr=(AttrData)attrs.get(i);
			attr.bytesWrite(data);
		}
		// ����
		data.writeShort(skills.size());
		SkillData skill;
		for(int i=0;i<skills.size();i++)
		{
			skill=(SkillData)skills.get(i);
			skill.bytesWrite(data);
		}
		// ������Ϣ
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
