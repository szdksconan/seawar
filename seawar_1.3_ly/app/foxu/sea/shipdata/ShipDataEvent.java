package foxu.sea.shipdata;

import mustang.io.ByteBuffer;
import mustang.set.IntList;

/**船只日志事件记录*/
public class ShipDataEvent
{
	/**目表index*/
	int targetIndex;
	/**事件ID*/
	int eventId;
	/**事件状态*/
    int state;
    /**船只*/
    IntList ships = new IntList();
    
    public void addShips(int num,int sid)
    {
    	ships.add(sid);
    	ships.add(num);
    }
    
    public ShipDataEvent bytesRead(ByteBuffer data)
    {
    	targetIndex = data.readInt();
    	eventId = data.readInt();
    	state=data.readUnsignedByte();
    	bytesReadLeftList(data);
    	return this;
    }
    
    public void bytesWrite(ByteBuffer data)
    {
    	data.writeInt(targetIndex);
    	data.writeInt(eventId);
    	data.writeByte(state);
    	bytesWriteLeftList(data);
    }
    
    
	public void bytesWriteLeftList(ByteBuffer data)
	{
		data.writeByte(ships.size());
		for(int i=0;i<ships.size();i++)
		{
			data.writeShort(ships.get(i));
		}
	}

	public void bytesReadLeftList(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		ships.clear();
		for(int i=0;i<length;i++)
		{
			ships.add(data.readUnsignedShort());
		}
	}

	
	public int getEventId()
	{
		return eventId;
	}

	
	public void setEventId(int eventId)
	{
		this.eventId=eventId;
	}

	
	public int getIndex()
	{
		return targetIndex;
	}

	
	public void setIndex(int index)
	{
		this.targetIndex=index;
	}

	
	public IntList getShips()
	{
		return ships;
	}

	
	public void setShips(IntList ships)
	{
		this.ships=ships;
	}

	
	public int getState()
	{
		return state;
	}

	
	public void setState(int state)
	{
		this.state=state;
	}
}
