package foxu.sea.officer;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;

/**
 * 军官碎片
 * 
 * @author Alan
 */
public class OfficerFragment extends Sample
{
	int count;
	/** 可合成军官sid */
	int officerSid;
	/** 合成需要的碎片数量 */
	int composeCount;
	/**购买碎片的价格**/
	int costGems;
	/**购买碎片的两极货币**/
	int coins;
	/** 稀有度 */
	int scarcity;
	
	public int getCount()
	{
		return count;
	}
	
	public void setCount(int count)
	{
		this.count=count;
	}
	
	public int getOfficerSid()
	{
		return officerSid;
	}
	
	public int getComposeCount()
	{
		return composeCount;
	}
	
	/** 增加数量 */
	public void incrCount(int addNum)
	{
		count+=addNum;
		if(count<0)
			count=Integer.MAX_VALUE;
	}

	/** 减少数量 */
	public void decrCount(int decrNum)
	{
		if(decrNum<0)
			return;
		count-=decrNum;
		if(count<0)
			count=0;
	}
	
	public void showBytesWrite(ByteBuffer data)
	{
		data.writeInt(getSid());
		data.writeInt(count);
	}
	
	/** 从字节数组中反序列化获得对象的域 */
	public static OfficerFragment bytesReadOfficer(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		int count=data.readInt();
		OfficerFragment of=(OfficerFragment)OfficerManager.factory.newSample(sid);
		if(of==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,
				OfficerFragment.class.getName()+" bytesRead, invalid sid:"+sid);
		of.setSid(sid);
		of.setCount(count);
		return of;
	}

	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeInt(count);
	}

	
	public int getCostGems()
	{
		return costGems;
	}

	
	public void setCostGems(int costGems)
	{
		this.costGems=costGems;
	}

	
	public int getCoins()
	{
		return coins;
	}

	
	public void setCoins(int coins)
	{
		this.coins=coins;
	}

	
	public int getScarcity()
	{
		return scarcity;
	}

	
	public void setScarcity(int scarcity)
	{
		this.scarcity=scarcity;
	}
	
	
	
}
