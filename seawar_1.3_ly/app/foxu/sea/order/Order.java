package foxu.sea.order;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**������
 * 
 * author:icetiger
 * */
public class Order extends Sample
{
	/**ORDER_SUCCESS=1������Ч ORDER_FAIL=2��֤ʧ�ܵĶ���*/
	public static final int ORDER_SUCCESS=1,ORDER_FAIL=2,TRANS_SAME=3;
	/** Serialization fileds */
	/**����ID*/
	int id;
	/**���ID*/
	int userId;
	/**�������*/
	String userName;
	/**����״̬*/
	int orderState;
	/**����ʱ��*/
	int createAt;
	/**�豸�ͺ�*/
	int iosType;
	/**��ҵȼ�*/
	int playerLevel;
	/**RMB*/
	int money;
	/** ��Ԫ�۸� */
	float usdMoney;
	/**��Ӧ��ʯ����*/
	int gems;
	/**��Ӧ�Ķ������*/
    String transaction_id;
    /**��֤��Ϣ*/
    String verifyInfo="";
    /** ǩ����Ϣ */
	String dataSignature="";
    /**�豸�ͺ�*/
    String device;
    /** �豸Ψһ���  */
    String udid;
    /** ����ʾ��   */
    String idfa;
    /** ��ֵip */
    String ip;
    /** ��ֵ������Ϣ�����ں˶Գ�ֵ */
    String purchaseInfo;
    /**ƽ̨id*/
    String plat_id;
    /** ��ֵ�豸 */
	String pdid;
	
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	
	/** ���ֽ������з����л���ö������ */
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

	
	
	/** ���ֽڻ����з����л��õ�һ������ */
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
	
	/** ������������л����ֽڻ����� */
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
	 * @param factory Ҫ���õ� factory
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
	 * @param createAt Ҫ���õ� createAt
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
	 * @param id Ҫ���õ� id
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
	 * @param money Ҫ���õ� money
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
	 * @param orderState Ҫ���õ� orderState
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
	 * @param userId Ҫ���õ� userId
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
	 * @param userName Ҫ���õ� userName
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
	 * @param gems Ҫ���õ� gems
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
	 * @param iosType Ҫ���õ� iosType
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
	 * @param playerLevel Ҫ���õ� playerLevel
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
