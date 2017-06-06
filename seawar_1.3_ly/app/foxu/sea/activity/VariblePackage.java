package foxu.sea.activity;


import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.sea.InterTransltor;
import foxu.sea.PublicConst;
import foxu.sea.award.Award;
import foxu.sea.equipment.Equipment;
import foxu.sea.gm.GMConstant;
import foxu.sea.kit.SeaBackKit;

/**
 * ��ʱ���
 * 
 * @author yw
 * 
 */
public class VariblePackage
{

	/** UPDATE�������  DELAY�ӳ����  MERGE�������  ACTIVITY�������� AWARD������� PAY�������*/
	public static final int UPDATE=0,DELAY=1,MERGE=2,ACTIVITY=3,AWARD=4,PAY=5;
	/** ǰ̨��ʾ���SID */
	public static final int[] SHOW_SID={4005,4006,4007,4017,4018,4019};
	/** ������� */
	int type;
	/** ����ʱ��(uid) */
	int active;
	/** ��ʼʱ�� */
	int start;
	/** ����ʱ�� */
	int end;
	/** ���� */
	/**�ȼ�����**/
	int level;
	/**ʱ������**/
	int limitTime;
	/**��Ʒ�б�(�洢��) */
	int[] props;
	Award award=(Award)Award.factory.newSample(ActivityContainer.EMPTY_SID);

	public VariblePackage()
	{
	}
	public VariblePackage(int type,int start,int end,int[] props,int level,int limittime)
	{
		active=TimeKit.getSecondTime();
		this.type=type;
		this.start=start;
		this.end=end;
		this.level=level;
		this.limitTime=limittime;
		this.props=props;
		sepProEquInfo(props);
		//award.setPropSid(props);

	}
	/** ����Ƿ���Ч */
	public boolean isValid()
	{
		return start<end&&TimeKit.getSecondTime()<end;
	}
	/** ����Ƿ���� */
	public boolean canGet(int time)
	{
		return time<end&&time>=start;
	}
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(active);
		data.writeByte(type);
		data.writeInt(start);
		data.writeInt(end);
		//int[] props=award.getPropSid();
//		int[] props=comProEquInfo(award.getPropSid(),award.getEquipSids());
		if(props==null || props.length==0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(props.length);
			for(int i=0;i<props.length;i++)
			{
				data.writeInt(props[i]);
			}
		}
		data.writeByte(level);
		data.writeInt(limitTime);
	}

	public void bytesRead(ByteBuffer data)
	{
		active=data.readInt();
		type=data.readUnsignedByte();
		start=data.readInt();
		end=data.readInt();
		int len=data.readUnsignedByte();
		props=new int[len];
		for(int i=0;i<props.length;i++)
		{
			props[i]=data.readInt();
		}
		//award.setPropSid(props);
		sepProEquInfo(props);
		level=data.readUnsignedByte();
		limitTime=data.readInt();
	}
	public void showBytesWrite(ByteBuffer data)
	{
		data.writeInt(active);
		data.writeShort(SHOW_SID[type]);
		data.writeInt(end-TimeKit.getSecondTime());
	}
	
	public JSONObject getGmInfo()
	{
		JSONObject json=new JSONObject();
		try
		{
			json.put(GMConstant.ID,active);
			json.put(GMConstant.TYPE,type);
			json.put(GMConstant.OPEN,canGet(TimeKit.getSecondTime()));
			json.put(GMConstant.START,SeaBackKit.formatDataTime(start));
			json.put(GMConstant.END,SeaBackKit.formatDataTime(end));
			json.put(GMConstant.PROPS,intsToString(award.getPropSid())+","
				+equintsToString(award.getEquipSids()));

		}
		catch(Exception e)
		{
		}
		return json;
		
	}
	/**��ȡ��Ʒ��Ϣ**/
	public String intsToString(int[] ints)
	{
		StringBuffer sbuff=new StringBuffer();
		if(ints!=null)
		{
			for(int i=0;i<ints.length;i++)
			{
				if(i%2==0)
//					sbuff.append(((Prop)Prop.factory.getSample(ints[i]))
//						.getPname());
					sbuff.append(InterTransltor.getInstance().getTransByKey(PublicConst.SERVER_LOCALE,"prop_sid_"+ints[i]));
				else
					sbuff.append(ints[i]);
				if(i==ints.length-1) continue;
				sbuff.append(",");
			}
		}
		return sbuff.toString();
	}
	
	/**��ȡװ����Ϣ**/
	public String equintsToString(int[] ints)
	{
		StringBuffer sbuff=new StringBuffer();
		if(ints!=null)
		{
			for(int i=0;i<ints.length;i++)
			{
				if(i%2==0)
					sbuff.append(((Equipment)Equipment.factory.getSample(ints[i])).getEquname());
				else
					sbuff.append(ints[i]);
				if(i==ints.length-1) continue;
				sbuff.append(",");
			}
		}
		return sbuff.toString();
	}

	/**��ȡ�����id**/
	public int getactivityId()
	{
		return active;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void setLevel(int level)
	{
		this.level=level;
	}
	
	public int getLimitTime()
	{
		return limitTime;
	}
	
	public void setLimitTime(int limitTime)
	{
		this.limitTime=limitTime;
	}
	
	/**����pro��װ��**/
	public void sepProEquInfo(int[] pros)
	{
		SeaBackKit.resetAward(award,pros);
	}
	/**�ϲ���Ʒ��װ��
	 ** ������ǰ���콵����**/
	public int[] comProEquInfo(int[] props,int[] equs)
	{
		IntList prolist=new IntList();
		if(props!=null && props.length!=0)
		{
			for(int i=0;i<props.length;i++)
			{
				prolist.add(props[i]);
			}
		}
		if(equs!=null && equs.length!=0)
		{
			for(int i=0;i<equs.length;i++)
			{
				prolist.add(equs[i]);
			}
		}
		return prolist.toArray();
	}
}
