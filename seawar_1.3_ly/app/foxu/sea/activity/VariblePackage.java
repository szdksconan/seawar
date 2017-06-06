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
 * 定时礼包
 * 
 * @author yw
 * 
 */
public class VariblePackage
{

	/** UPDATE更新礼包  DELAY延迟礼包  MERGE合区礼包  ACTIVITY活动奖励礼包 AWARD奖励礼包 PAY补偿礼包*/
	public static final int UPDATE=0,DELAY=1,MERGE=2,ACTIVITY=3,AWARD=4,PAY=5;
	/** 前台显示礼包SID */
	public static final int[] SHOW_SID={4005,4006,4007,4017,4018,4019};
	/** 礼包类型 */
	int type;
	/** 激活时间(uid) */
	int active;
	/** 开始时间 */
	int start;
	/** 结束时间 */
	int end;
	/** 奖励 */
	/**等级限制**/
	int level;
	/**时间限制**/
	int limitTime;
	/**物品列表(存储用) */
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
	/** 礼包是否有效 */
	public boolean isValid()
	{
		return start<end&&TimeKit.getSecondTime()<end;
	}
	/** 礼包是否可领 */
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
	/**获取物品信息**/
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
	
	/**获取装备信息**/
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

	/**获取领包的id**/
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
	
	/**分离pro和装备**/
	public void sepProEquInfo(int[] pros)
	{
		SeaBackKit.resetAward(award,pros);
	}
	/**合并物品和装备
	 ** 兼容以前的天降好礼**/
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
