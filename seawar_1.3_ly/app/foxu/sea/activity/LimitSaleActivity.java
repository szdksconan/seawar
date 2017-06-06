package foxu.sea.activity;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.PublicConst;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;

/**
 * 限时商品活动
 * 
 * @author yw
 * 
 */
public class LimitSaleActivity extends Activity
{

	/** 限时商品范围sid-num */
	IntKeyHashMap sid_num;
	
	public IntKeyHashMap getSid_num()
	{
		return sid_num;
	}
//	/** 限时商品购买记录 */
//	public void limitSaleRecord(Player player,int sid)
//	{
//		if(sid_num==null||sid_num.get(sid)==null)return;
//		player.limitSaleRecord(id,sid);
//		
//	}

	/** 设置打折范围 */
	public boolean setSidNum(String sidnum)
	{
		if(sidnum==null||sidnum=="") return false;
		String[] sidnums=sidnum.split(",");
		int len=sidnums.length;
		if(len<=0) return false;
		int[] addSidNum=new int[len*2];
		boolean isFailed=false;
		for(int i=0;i<len;i++)
		{
			try
			{
				addSidNum[2*i]=Integer.parseInt(sidnums[i].split("-")[0]);
				if(Prop.factory.getSample(addSidNum[2*i])==null)
				{
					return false;
				}
				addSidNum[2*i+1]=Integer.parseInt(sidnums[i].split("-")[1]);
			}
			catch(Exception e)
			{
				return false;
			}

		}
		if(isFailed) return false;
		sid_num=new IntKeyHashMap();
		for(int i=0;i<len*2;i+=2)
		{
			sid_num.put(addSidNum[i],addSidNum[i+1]);
		}

		return true;
	}
	/** 开启活动 */
	public String startActivity(String stime,String etime,String sidnum,CreatObjectFactory factory)
	{
		return resetActivity(stime,etime,sidnum,factory);
	}
	/** 重设活动 */
	public String resetActivity(String stime,String etime,String sidnum,CreatObjectFactory factory)
	{
		if(!setSidNum(sidnum))
			return getActivityState();
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
		
		return getActivityState();
	}
	/** 获取活动状态 */
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append(",\"others\":\"").append(sidNumToString()).append("\"")
			.append("}");

		return sb.toString();

	}

	/** 判断是否是限时商品 */
	public boolean isLimitProp(int sid)
	{
		if(!isOpen(TimeKit.getSecondTime())) return false;
		if(sid_num==null||sid_num.get(sid)==null) return false;
		return true;
	}

	public String sidNumToString()
	{
		if(sid_num==null) return "";
		StringBuilder sb=new StringBuilder();
		int[] keys=sid_num.keyArray();
		for(int i=0;i<keys.length;i++)
		{
//			sb.append(((Prop)Prop.factory.getSample(keys[i])).getPname()+"-"
//				+sid_num.get(keys[i])+", ");
			sb.append(InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"prop_sid_"+keys[i]+"-"+sid_num.get(keys[i])+", "));
		}
		return sb.toString();

	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		int len=data.readUnsignedShort();
		if(len<=0) return;
		sid_num=new IntKeyHashMap();
		for(int i=0;i<len;i++)
		{
			sid_num.put(data.readUnsignedShort(),data.readUnsignedShort());
		}

	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		if(sid_num==null)
		{
			data.writeShort(0);
		}
		else
		{
			int[] keys=sid_num.keyArray();
			data.writeShort(keys.length);
			for(int i=0;i<keys.length;i++)
			{
				data.writeShort(keys[i]);
				data.writeShort((Integer)sid_num.get(keys[i]));
			}

		}

		return data;
	}
	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(0);
		data.writeInt(endTime-TimeKit.getSecondTime());
	}
	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendLimitSaleActivty(smap);
	}

}
