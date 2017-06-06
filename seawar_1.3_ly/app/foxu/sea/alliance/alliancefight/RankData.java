package foxu.sea.alliance.alliancefight;

import foxu.dcaccess.CreatObjectFactory;
import mustang.io.ByteBuffer;


/**
 * 排名数据
 * @author yw
 *
 */
public class RankData extends StockFleet
{
	/** 玩家ID */
	int playerID;
	/** 船优先级（取船SID最后两位，然后个位和十位颠倒） */
	int pri ;
	/** 玩家名 */
	String name;
	
	public RankData(int playerID,int pri,int shipSid,int num)
	{
		super(shipSid,num);
		this.playerID=playerID;
		this.pri=pri;
	}
	
	/** 更新排名数据 */
	public boolean updateData(int pri,int shipSid,int count)
	{
		if(pri<this.pri)return false;
		if(pri>this.pri)
		{
			this.pri=pri;
			setSid(shipSid);
			setCount(count);
		}else
		{
			incrCount(count);
		}
		return true;
	}

	/** 序列化给前台 */
	public void showBytesWrite(CreatObjectFactory objfactory,ByteBuffer data)
	{
		data.writeUTF(getName(objfactory));
//		System.out.println("-------name-------:::"+getName(objfactory));
		data.writeShort(getSid());
//		System.out.println("-------getSid-------:::"+getSid());
		data.writeInt(getCount());
//		System.out.println("-------getCount-------:::"+getCount());
		
	}
	
	/** 获取玩家名字 */
	public String getName(CreatObjectFactory objfactory)
	{
		//if(name==null)
		name=objfactory.getPlayerById(playerID).getName();
		return name;
	}
	public int getPlayerID()
	{
		return playerID;
	}

	
	public void setPlayerID(int playerID)
	{
		this.playerID=playerID;
	}

	
	public int getPri()
	{
		return pri;
	}

	
	public void setPri(int pri)
	{
		this.pri=pri;
	}
	
}
