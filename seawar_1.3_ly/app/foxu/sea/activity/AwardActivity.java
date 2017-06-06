package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.PublicConst;
import foxu.sea.award.Award;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;

public class AwardActivity extends Activity
{

	/** 限时抽奖次数 */
	int times;
	/** 消耗宝石数 */
	int gems;
	/** 奖励品数组 */
	int awardPackage[];
	/** 限时抽奖次数 */
	private Award award=(Award)Award.factory
		.newSample(ActivityContainer.EMPTY_SID);

	public int getTimes()
	{
		return times;
	}

	public void setTimes(int times)
	{
		this.times=times;
	}

	public int getGems()
	{
		return gems;
	}

	public void setGems(int gems)
	{
		this.gems=gems;
	}

	/** 得到奖励品数组 */
	public int[] getAwardPackage()
	{
		return awardPackage;
	}

	public void setAwardPackage(int[] awardPackage)
	{
		this.awardPackage=awardPackage;
	}
	/** 开启活动 */
	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		return resetActivity(stime,etime,initData,factory);
	}

	/** 重写活动 */
	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		String[] data=initData.split(",");
		try
		{
			gems=Integer.parseInt(data[0]);
			setGems(gems);
			if(data[1].equals("no_limit"))
			{
				times=-1;
			}
			else
				times=Integer.parseInt(data[1]);
		}
		catch(Exception e)
		{
			return getActivityState();
		}
		try
		{
			awardPackage=new int[data.length-2];
			for(int i=0;i<data.length-2;i++)
			{
				awardPackage[i]=Integer.parseInt(data[i+2]);
			}
			award.setRandomProps(awardPackage);
		}
		catch(Exception e)
		{
			return getActivityState();
		}
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
		return getActivityState();
	}

	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"")
			.append(id)
			.append('"')
			.append(",\"sid\":\"")
			.append(getSid())
			.append('"')
			.append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime))
			.append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime))
			.append('"')
			.append(",\"others\":\"")
			.append(
				"[times]:"+times+","+"[gems]:"+gems+","+"[RandomProps]:"
					+awardPackageToString()).append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}
	public String awardPackageToString()
	{
		if(awardPackage==null) return "";
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<awardPackage.length;i++)
		{
			if(i%3==0)
			{
				String name=awardPackage[i]+"";
				Prop prop=(Prop)Prop.factory.getSample(awardPackage[i]);
				if(prop!=null) name=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,
					"prop_sid_"+prop.getSid());
				sb.append(name).append(",");
			}else
			{
				sb.append(awardPackage[i]).append(",");
			}
		}
		return sb.toString();
	}
	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		times=data.readUnsignedShort();
		gems=data.readUnsignedShort();
		int len=data.readUnsignedByte();
		if(len>0)
		{
			awardPackage=new int[len];
			for(int i=0;i<len;i++)
			{
				awardPackage[i]=data.readUnsignedShort();
			}
		}
		award.setRandomProps(awardPackage);
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(times);
		data.writeShort(gems);
		if(awardPackage==null)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(awardPackage.length);
			for(int i=0;i<awardPackage.length;i++)
			{
				data.writeShort(awardPackage[i]);
			}
		}
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeShort(gems);
		data.writeInt(endTime-TimeKit.getSecondTime());
		data.writeShort(times);
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendLuckyAwardActivty(smap);
	}

	
	public Award getAward()
	{
		return award;
	}
	
	@Override
	public Object copy(Object obj)
	{
		AwardActivity act=(AwardActivity)super.copy(obj);
		act.award=(Award)Award.factory
			.newSample(ActivityContainer.EMPTY_SID);
		return act;
	}
	

}
