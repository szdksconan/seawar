package foxu.sea.order;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**订单类
 * 
 * author:icetiger
 * */
public class Order extends Sample
{
	/**ORDER_SUCCESS=1订单生效 ORDER_FAIL=2验证失败的订单*/
	public static final int ORDER_SUCCESS=1,ORDER_FAIL=2,TRANS_SAME=3;
	/** Serialization fileds */
	/**订单ID*/
	int id;
	/**玩家ID*/
	int userId;
	/**玩家名字*/
	String userName;
	/**订单状态*/
	int orderState;
	/**创建时间*/
	int createAt;
	/**设备型号*/
	int iosType;
	/**玩家等级*/
	int playerLevel;
	/**RMB*/
	int money;
	/** 美元价格 */
	float usdMoney;
	/**对应宝石数量*/
	int gems;
	/**对应的订单编号*/
    String transaction_id;
    /**验证信息*/
    String verifyInfo="";
    /** 签名信息 */
	String dataSignature="";
    /**设备型号*/
    String device;
    /** 设备唯一编号  */
    String udid;
    /** 广告标示符   */
    String idfa;
    /** 充值ip */
    String ip;
    /** 充值附加信息，用于核对充值 */
    String purchaseInfo;
    /**平台id*/
    String plat_id;
    /** 充值设备 */
	String pdid;
	
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	
	/** 从字节数组中反序列化获得对象的域 */
	public static Order bytesReadOrder(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Order r=(Order)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Order.class.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}

	
	
	/** 从字节缓存中反序列化得到一个对象 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		id = data.readInt();
		userId = data.readInt();
		createAt = data.readInt();
		orderState = data.readUnsignedByte();
		userName = data.readUTF();
		iosType = data.readUnsignedByte();
		money = data.readUnsignedShort();
		gems = data.readUnsignedShort();
		playerLevel = data.readUnsignedByte();
		transaction_id=data.readUTF();
		verifyInfo=data.readUTF();
		dataSignature=data.readUTF();
		udid=data.readUTF();
		idfa=data.readUTF();
		ip=data.readUTF();
		return this;
	}
	
	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(id);
		data.writeInt(userId);
		data.writeInt(createAt);
		data.writeByte(orderState);
		data.writeUTF(userName);
		data.writeByte(iosType);
		data.writeShort(money);
		data.writeShort(gems);
		data.writeByte(playerLevel);
		data.writeUTF(transaction_id);
		data.writeUTF(verifyInfo);
		data.writeUTF(dataSignature);
		data.writeUTF(udid);
		data.writeUTF(idfa);
		data.writeUTF(ip);
	}

	/**
	 * @return factory
	 */
	public static SampleFactory getFactory()
	{
		return factory;
	}

	/**
	 * @param factory 要设置的 factory
	 */
	public static void setFactory(SampleFactory factory)
	{
		Order.factory=factory;
	}



	
	/**
	 * @return createAt
	 */
	public int getCreateAt()
	{
		return createAt;
	}



	
	/**
	 * @param createAt 要设置的 createAt
	 */
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}



	
	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}



	
	/**
	 * @param id 要设置的 id
	 */
	public void setId(int id)
	{
		this.id=id;
	}



	
	/**
	 * @return money
	 */
	public int getMoney()
	{
		return money;
	}



	
	/**
	 * @param money 要设置的 money
	 */
	public void setMoney(int money)
	{
		this.money=money;
	}
	
	public float getUsdMoney()
	{
		return this.usdMoney;
	}

	public void setUsdMoney(float usdMoney)
	{
		this.usdMoney=usdMoney;
	}

	
	/**
	 * @return orderState
	 */
	public int getOrderState()
	{
		return orderState;
	}



	
	/**
	 * @param orderState 要设置的 orderState
	 */
	public void setOrderState(int orderState)
	{
		this.orderState=orderState;
	}



	
	/**
	 * @return userId
	 */
	public int getUserId()
	{
		return userId;
	}



	
	/**
	 * @param userId 要设置的 userId
	 */
	public void setUserId(int userId)
	{
		this.userId=userId;
	}



	
	/**
	 * @return userName
	 */
	public String getUserName()
	{
		return userName;
	}



	
	/**
	 * @param userName 要设置的 userName
	 */
	public void setUserName(String userName)
	{
		this.userName=userName;
	}



	
	/**
	 * @return gems
	 */
	public int getGems()
	{
		return gems;
	}



	
	/**
	 * @param gems 要设置的 gems
	 */
	public void setGems(int gems)
	{
		this.gems=gems;
	}



	
	/**
	 * @return iosType
	 */
	public int getIosType()
	{
		return iosType;
	}



	
	/**
	 * @param iosType 要设置的 iosType
	 */
	public void setIosType(int iosType)
	{
		this.iosType=iosType;
	}



	
	/**
	 * @return playerLevel
	 */
	public int getPlayerLevel()
	{
		return playerLevel;
	}



	
	/**
	 * @param playerLevel 要设置的 playerLevel
	 */
	public void setPlayerLevel(int playerLevel)
	{
		this.playerLevel=playerLevel;
	}

	
	public String getVerifyInfo()
	{
		return verifyInfo;
	}



	
	public void setVerifyInfo(String verifyInfo)
	{
		this.verifyInfo=verifyInfo;
	}



	
	public String getDevice()
	{
		return device;
	}

	
	public void setDevice(String device)
	{
		this.device=device;
	}



	
	public String getUdid()
	{
		return udid;
	}



	
	public void setUdid(String udid)
	{
		this.udid=udid;
	}



	
	public String getIdfa()
	{
		return idfa;
	}



	
	public void setIdfa(String idfa)
	{
		this.idfa=idfa;
	}



	
	public String getIp()
	{
		return ip;
	}



	
	public void setIp(String ip)
	{
		this.ip=ip;
	}

	public void setSid(int sid)
	{
	    super.setSid(sid);
	}



	
	public String getTransaction_id()
	{
		return transaction_id;
	}



	
	public void setTransaction_id(String transaction_id)
	{
		this.transaction_id=transaction_id;
	}



	
	public String getDataSignature()
	{
		return dataSignature;
	}



	
	public void setDataSignature(String dataSignature)
	{
		this.dataSignature=dataSignature;
	}



	
	/**
	 * @return the purchaseInfo
	 */
	public String getPurchaseInfo()
	{
		return purchaseInfo;
	}



	
	/**
	 * @param purchaseInfo the purchaseInfo to set
	 */
	public void setPurchaseInfo(String purchaseInfo)
	{
		this.purchaseInfo=purchaseInfo;
	}



	
	public String getPlat_id()
	{
		return plat_id;
	}



	
	public void setPlat_id(String plat_id)
	{
		this.plat_id=plat_id;
	}



	
	public String getPdid()
	{
		return pdid;
	}



	
	public void setPdid(String pdid)
	{
		this.pdid=pdid;
	}
	
	
	
}
