package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.order.Order;

/**
 * 每档宝石首冲双倍活动
 * 
 * @author Alan
 * 
 */
public class DoubleGemsAcitivity extends BuyGemsActivity implements
	ActivitySave
{

	/** 活动信息保存周期 */
	public static final int SAVE_CIRCLE=15*60;

	/** 上一次活动信息保存时间 */
	int lastSaveTime;
	/** 玩家购买记录,key:id,value:状态信息 */
	IntKeyHashMap playerRecord;
	int[] orderNums;
	
	@Override
	public String startActivity(String stime,String etime,String initData,CreatObjectFactory factory)
	{
		resetOrderSids(initData);
		return super.startActivity(stime,etime,initData,factory);
	}
	@Override
	public String resetActivity(String stime,String etime,String initData,CreatObjectFactory factory)
	{
		resetOrderSids(initData);
		return super.resetActivity(stime,etime,initData,factory);
	}
	public void resetOrderSids(String initData)
	{
		String[] nums=TextKit.split(initData,",");
		orderNums=new int[nums.length];
		for(int i=0;i<nums.length;i++)
		{
			orderNums[i]=TextKit.parseInt(nums[i]);
		}
	}

	/** 记录的订单是否已经购买过 */
	public boolean isPurchased(Player player,int gems)
	{
		boolean isComplete=true;
		// 是否属于开启档位
		if(SeaBackKit.isContainValue(orderNums,gems))
		{
			// 购买记录
			IntList state=(IntList)playerRecord.get(player.getId());
			if(state==null||!state.contain(gems))
			{
				isComplete=false;
			}
		}
		return isComplete;
	}

	public void finishOrder(Player player,int gems)
	{
		IntList state=(IntList)playerRecord.get(player.getId());
		if(state==null)
		{
			state=new IntList();
			playerRecord.put(player.getId(),state);
		}
		state.add(gems);
		JBackKit.sendDoubleGemsInfo(player,this);

	}

	public void showByteWrite(ByteBuffer data,Player player)
	{
		int len=0;
		int top=data.top();
		data.writeByte(len);
		Object[] orders=Order.factory.getSamples();
		// 购买记录
		IntList state=(IntList)playerRecord.get(player.getId());
		for(int i=0;i<orderNums.length;i++)
		{
			if(state!=null&&state.contain(orderNums[i])) 
				continue;
			Order od=null;
			// 没有购买过的订单sid
			for(int j=0;j<orders.length;j++)
			{
				od=(Order)orders[j];
				if(od!=null&&orderNums[i]==od.getGems())
				{
					len++;
					data.writeShort(od.getSid());
					break;
				}
			}
		}
		int newTop=data.top();
		data.setTop(top);
		data.writeByte(len);
		data.setTop(newTop);
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
			.append(",\"others\":\"");
		for(int i=0;i<orderNums.length;i++)
		{
			sb.append(orderNums[i]+",");
		}
		sb.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}
	
	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendDoubleGemsInfo(smap,this);
	}
	
	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		int[] infos=playerRecord.keyArray();
		data.writeShort(infos.length);
		for(int i=0;i<infos.length;i++)
		{
			data.writeInt(infos[i]);
			IntList state=(IntList)playerRecord.get(infos[i]);
			data.writeByte(state.size());
			for(int j=0;j<state.size();j++)
			{
				data.writeInt(state.get(j));
			}
		}
		data.writeByte(orderNums.length);
		for(int i=0;i<orderNums.length;i++)
		{
			data.writeInt(orderNums[i]);
		}
		return data;
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		int len=data.readUnsignedShort();
		IntList state=null;
		for(int i=0;i<len;i++)
		{
			int pid=data.readInt();
			int pLen=data.readUnsignedByte();
			state=new IntList();
			playerRecord.put(pid,state);
			for(int j=0;j<pLen;j++)
			{
				state.add(data.readInt());
			}
			
		}
		len=data.readUnsignedByte();
		orderNums=new int[len];
		for(int i=0;i<len;i++)
		{
			orderNums[i]=data.readInt();
		}
	}

	public static void showByteWriteClosed(ByteBuffer data)
	{
		data.writeByte(0);
	}

	@Override
	public boolean isSave()
	{
		if(TimeKit.getSecondTime()>=lastSaveTime+SAVE_CIRCLE) return true;
		return false;
	}
	@Override
	public void setSave()
	{
		lastSaveTime=TimeKit.getSecondTime();
	}
	
	@Override
	public Object clone()
	{
		super.clone();
		playerRecord=new IntKeyHashMap();
		return this;
	}
}
