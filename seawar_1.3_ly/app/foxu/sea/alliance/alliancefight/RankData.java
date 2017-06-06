package foxu.sea.alliance.alliancefight;

import foxu.dcaccess.CreatObjectFactory;
import mustang.io.ByteBuffer;


/**
 * ��������
 * @author yw
 *
 */
public class RankData extends StockFleet
{
	/** ���ID */
	int playerID;
	/** �����ȼ���ȡ��SID�����λ��Ȼ���λ��ʮλ�ߵ��� */
	int pri ;
	/** ����� */
	String name;
	
	public RankData(int playerID,int pri,int shipSid,int num)
	{
		super(shipSid,num);
		this.playerID=playerID;
		this.pri=pri;
	}
	
	/** ������������ */
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

	/** ���л���ǰ̨ */
	public void showBytesWrite(CreatObjectFactory objfactory,ByteBuffer data)
	{
		data.writeUTF(getName(objfactory));
//		System.out.println("-------name-------:::"+getName(objfactory));
		data.writeShort(getSid());
//		System.out.println("-------getSid-------:::"+getSid());
		data.writeInt(getCount());
//		System.out.println("-------getCount-------:::"+getCount());
		
	}
	
	/** ��ȡ������� */
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
