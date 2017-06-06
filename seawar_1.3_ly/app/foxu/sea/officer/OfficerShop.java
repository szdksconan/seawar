package foxu.sea.officer;

import mustang.io.ByteBuffer;

/***
 * 
 * @author lhj 军官商店
 * 
 */
public class OfficerShop
{

	/** 商品列表 sid 类型 数量 宝石还是2级货币 数量 购买数量 上限数量 **/
	/** sid **/
	int sid;
	/** 商品类型 **/
	int shopType;
	/** 商品销售的数量 **/
	int goodsNum;
	/** 销售宝石的类型 **/
	int saleType;
	/** 购买需要的价格 **/
	int salePrice;
	/** 购买的数量 **/
	int saleNum;
	/** 购买的上限数量 **/
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
		// 物品还是碎片
		data.writeByte(shopType);
		// sid
		data.writeShort(sid);
		// 数量
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

	/** 增加购买数量 **/
	public void addShopsaleNum()
	{
		saleNum=saleNum+1;
	}

	/**判断是否可以购买当前这个商品**/
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
