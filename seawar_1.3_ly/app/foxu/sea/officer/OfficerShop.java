package foxu.sea.officer;

import mustang.io.ByteBuffer;

/***
 * 
 * @author lhj �����̵�
 * 
 */
public class OfficerShop
{

	/** ��Ʒ�б� sid ���� ���� ��ʯ����2������ ���� �������� �������� **/
	/** sid **/
	int sid;
	/** ��Ʒ���� **/
	int shopType;
	/** ��Ʒ���۵����� **/
	int goodsNum;
	/** ���۱�ʯ������ **/
	int saleType;
	/** ������Ҫ�ļ۸� **/
	int salePrice;
	/** ��������� **/
	int saleNum;
	/** ������������� **/
	int saleLimitNum;

	public void byteWrite(ByteBuffer data)
	{
		data.writeShort(sid);
		data.writeByte(shopType);
		data.writeByte(saleType);
		data.writeShort(salePrice);
		data.writeInt(goodsNum);
		data.writeInt(saleNum);
		data.writeInt(saleLimitNum);
	}

	public void byteRead(ByteBuffer data)
	{
		sid=data.readUnsignedShort();
		shopType=data.readUnsignedByte();
		saleType=data.readUnsignedByte();
		salePrice=data.readUnsignedShort();
		goodsNum=data.readInt();
		saleNum=data.readInt();
		saleLimitNum=data.readInt();
	}

	public void showBytesWrite(ByteBuffer data)
	{
		// ��Ʒ������Ƭ
		data.writeByte(shopType);
		// sid
		data.writeShort(sid);
		// ����
		data.writeByte(goodsNum);
		if(saleType==OfficerManager.BUY_BYGEMS)
		{
			data.writeShort(0);
			data.writeShort(salePrice);
		}
		else
		{
			data.writeShort(salePrice);
			data.writeShort(0);
		}
		if(saleLimitNum<=saleNum)
		{
			data.writeBoolean(false);
		}
		else
			data.writeBoolean(true);
	}

	/** ���ӹ������� **/
	public void addShopsaleNum()
	{
		saleNum=saleNum+1;
	}

	/**�ж��Ƿ���Թ���ǰ�����Ʒ**/
	public boolean  checkBuyGoods()
	{
		if(saleLimitNum<=saleNum)
		{
			return false;
		}
		return true;
	}
	
	public int getSid()
	{
		return sid;
	}

	public void setSid(int sid)
	{
		this.sid=sid;
	}

	public int getShopType()
	{
		return shopType;
	}

	public void setShopType(int shopType)
	{
		this.shopType=shopType;
	}

	public int getGoodsNum()
	{
		return goodsNum;
	}

	public void setGoodsNum(int goodsNum)
	{
		this.goodsNum=goodsNum;
	}

	public int getSaleType()
	{
		return saleType;
	}

	public void setSaleType(int saleType)
	{
		this.saleType=saleType;
	}

	public int getSalePrice()
	{
		return salePrice;
	}

	public void setSalePrice(int salePrice)
	{
		this.salePrice=salePrice;
	}

	public int getSaleNum()
	{
		return saleNum;
	}

	public void setSaleNum(int saleNum)
	{
		this.saleNum=saleNum;
	}

	public int getSaleLimitNum()
	{
		return saleLimitNum;
	}

	public void setSaleLimitNum(int saleLimitNum)
	{
		this.saleLimitNum=saleLimitNum;
	}
}
