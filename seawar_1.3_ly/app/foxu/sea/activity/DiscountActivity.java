package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.PublicConst;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * 折扣活动
 * 
 * @author yw
 * 
 */
public class DiscountActivity extends Activity
{

	/** 折扣 80为80%优惠，及打2折 */
	int percent;
	/** 折扣范围 */
	int[] propSids;

	public int[] getPropSids()
	{
		return propSids;
	}

	public void setPropSids(int[] propSids)
	{
		this.propSids=propSids;
	}

	public void setPercent(int percent)
	{
		this.percent=percent;
	}
	/** 获取折扣 */
	public int getPercent()
	{
		return percent;
	}
	/** 计算折扣后宝石 */
	public int discountGems(int sid,int gems)
	{
		if(SeaBackKit.isContainValue(propSids,sid))
		{
			gems=(int)Math.ceil(gems*(100-percent)/100d);
		}
		return gems;
	}
	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		return resetActivity(stime,etime,initData,factory);
	}
	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		String[] data=initData.split(",");
		try
		{
			int perc=Integer.parseInt(data[0]);
			if(perc>=0&&perc<=100)
			{
				this.percent=perc;
			}
		}
		catch(Exception e)
		{
		}

		try
		{
			if(data[1].equals("all"))
			{
				propSids=PublicConst.SHOP_SELL_SIDS;
			}
			else
			{
				int sids[]=new int[data.length-1];
				for(int i=0;i<sids.length;i++)
				{
					sids[i]=Integer.parseInt(data[i+1]);
					if(!SeaBackKit.isContainValue(
						PublicConst.SHOP_SELL_SIDS,sids[i]))
					{
						return getActivityState();
					}
				}
				propSids=sids;
			}
		}
		catch(Exception e)
		{
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
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"")
			.append("percent:"+percent+","+"propSids:"+propSidsToString())
			.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}
	public String propSidsToString()
	{
		if(propSids==null) return "";
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<propSids.length;i++)
		{
//			sb.append(((Prop)Prop.factory.getSample(propSids[i])).getPname())
//				.append(",");
			sb.append(InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,
					"prop_sid_"+propSids[i])).append(",");
		}
		return sb.toString();
	}
	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		percent=data.readUnsignedShort();
		int len=data.readUnsignedShort();
		propSids=new int[len];
		for(int i=0;i<len;i++)
		{
			propSids[i]=data.readUnsignedShort();
		}

	}
	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeShort(percent);
		if(propSids==null)
		{
			data.writeShort(0);
		}
		else
		{
			data.writeShort(propSids.length);
			for(int i=0;i<propSids.length;i++)
			{
				data.writeShort(propSids[i]);
			}
		}
		return data;
	}
	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(percent);
		data.writeInt(endTime-TimeKit.getSecondTime());
		// if(propSids==null)
		// {
		// data.writeShort(0);
		// }else
		// {
		// data.writeShort(propSids.length);
		// for(int i=0;i<propSids.length;i++)
		// {
		// data.writeShort(propSids[i]);
		// }
		// }

	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendDiscountActivty(smap);
	}
}
