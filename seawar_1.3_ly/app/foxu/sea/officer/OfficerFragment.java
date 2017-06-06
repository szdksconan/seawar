package foxu.sea.officer;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;

/**
 * ������Ƭ
 * 
 * @author Alan
 */
public class OfficerFragment extends Sample
{
	int count;
	/** �ɺϳɾ���sid */
	int officerSid;
	/** �ϳ���Ҫ����Ƭ���� */
	int composeCount;
	/**������Ƭ�ļ۸�**/
	int costGems;
	/**������Ƭ����������**/
	int coins;
	/** ϡ�ж� */
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
	
	/** �������� */
	public void incrCount(int addNum)
	{
		count+=addNum;
		if(count<0)
			count=Integer.MAX_VALUE;
	}

	/** �������� */
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
	
	/** ���ֽ������з����л���ö������ */
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

	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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
