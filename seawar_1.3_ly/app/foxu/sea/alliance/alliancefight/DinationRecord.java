package foxu.sea.alliance.alliancefight;

import foxu.dcaccess.CreatObjectFactory;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


/**
 * �ۼƾ��׼�¼
 * @author yw
 *
 */
public class DinationRecord extends StockFleet
{
	/** ���ID */
	int playerId;
	/** ����ʱ�� */
	int createTime;
	/** ����� */
	String name;
	
	public DinationRecord()
	{
		super();
	}
	public DinationRecord(int playerId,int sid,int count)
	{
		super(sid,count);
		this.playerId=playerId;
		createTime=TimeKit.getSecondTime();
	}
	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		playerId=data.readInt();
		createTime=data.readInt();
		
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeInt(playerId);
		data.writeInt(createTime);
	}
	
	public void showBytesWrite(CreatObjectFactory objfactory,ByteBuffer data)
	{
		data.writeUTF(getName(objfactory));
//		System.out.println("---------getName(objfactory)------::::"+getName(objfactory));
		data.writeInt(createTime);
		data.writeShort(getSid());
		data.writeInt(getCount());
	}
	/** ��ȡ������� */
	public String getName(CreatObjectFactory objfactory)
	{
//		if(name==null)name=objfactory.getPlayerById(playerId).getName();
		name=objfactory.getPlayerById(playerId).getName();
		return name;
	}
}
