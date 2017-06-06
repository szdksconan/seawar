package foxu.sea.alliance.chest;

import foxu.sea.kit.SeaBackKit;
import mustang.io.ByteBuffer;

/**
 * ���˱���
 */
public class AllianceChest
{

	/** ��Ѵ��� */
	int freeCount;
	/** ���״��� */
	int count;
	/** ��ǰ����ȼ� */
	int chestLv;
	/** �ϴγ齱���� */
	int lastDay;
	/** ���׼��� ������״����� */
	int giveLv;
	/** ���˻����콱���� */
	int luckyCount;
	
	public AllianceChest()
	{
		this.freeCount=1;
		this.count=0;
		this.chestLv=0;
		this.lastDay=0;
		this.giveLv=-1;
		this.luckyCount=1;
	}
	
	public void showBytesWrite(ByteBuffer data)
	{
		data.writeInt(SeaBackKit.getWeekEndSunTime());	//�ʱ��   ����24��ʱ���
		data.writeByte(freeCount);	//��Ѵ���
		data.writeShort(count);		//���״���
		data.writeByte(chestLv);	//��ǰ����ȼ�
	}

	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(freeCount);
		data.writeInt(count);
		data.writeInt(chestLv);
		data.writeInt(lastDay);
		data.writeInt(giveLv);
		data.writeInt(luckyCount);
	}

	public void bytesRead(ByteBuffer data)
	{
		freeCount=data.readInt();
		count=data.readInt();
		chestLv=data.readInt();
		lastDay=data.readInt();
		giveLv=data.readInt();
		luckyCount=data.readInt();
	}

	public int getFreeCount()
	{
		return freeCount;
	}

	public void setFreeCount(int freeCount)
	{
		this.freeCount=freeCount;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count=count;
	}

	public int getChestLv()
	{
		return chestLv;
	}

	public void setChestLv(int chestLv)
	{
		this.chestLv=chestLv;
	}

	public int getLastDay()
	{
		return lastDay;
	}

	public void setLastDay(int lastDay)
	{
		this.lastDay=lastDay;
	}

	public int getGiveLv()
	{
		return giveLv;
	}

	public void setGiveLv(int giveLv)
	{
		this.giveLv=giveLv;
	}

	public int getLuckyCount()
	{
		return luckyCount;
	}

	public void setLuckyCount(int luckyCount)
	{
		this.luckyCount=luckyCount;
	}

}
