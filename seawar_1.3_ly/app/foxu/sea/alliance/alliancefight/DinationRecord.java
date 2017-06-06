package foxu.sea.alliance.alliancefight;

import foxu.dcaccess.CreatObjectFactory;
import mustang.io.ByteBuffer;
import mustang.util.TimeKit;


/**
 * 累计捐献记录
 * @author yw
 *
 */
public class DinationRecord extends StockFleet
{
	/** 玩家ID */
	int playerId;
	/** 产生时间 */
	int createTime;
	/** 玩家名 */
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
	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		playerId=data.readInt();
		createTime=data.readInt();
		
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
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
	/** 获取玩家名字 */
	public String getName(CreatObjectFactory objfactory)
	{
//		if(name==null)name=objfactory.getPlayerById(playerId).getName();
		name=objfactory.getPlayerById(playerId).getName();
		return name;
	}
}
