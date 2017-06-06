package foxu.sea.arena;

import mustang.io.ByteBuffer;

/**
 * 帝国舰队竞技场
 * 
 * @author comeback
 *
 */
public class SeawarGladiator extends Gladiator
{
	public static final int FLEET_MAX_COUNT=6;
	/** 船只和数量 */
	int[][] ships=new int[FLEET_MAX_COUNT][2];
	
	/** 奖励SID */
	int awardSid;
	
	/** 上一次挑战时间 */
	int lastBattleTime;
	
	public void clearShips()
	{
		for(int i=0;i<ships.length;i++)
		{
			for(int j=0;j<ships[i].length;j++)
			{
				ships[i][j]=0;
			}
		}
	}
	
	public void setShipSid(int index,int sid)
	{
		this.ships[index][0]=sid;
	}
	
	public int getShipSidByIndex(int index)
	{
		return ships[index][0];
	}
	
	public void setShipCount(int index,int count)
	{
		this.ships[index][1]=count;
	}
	
	public int getShipCountByIndex(int index)
	{
		return ships[index][1];
	}
	
	public void setAwardSid(int sid)
	{
		this.awardSid=sid;
	}
	
	public int getAwardSid()
	{
		return this.awardSid;
	}
	
	public void setLastBattleTime(int time)
	{
		this.lastBattleTime=time;
	}
	
	public int getLastBattleTime()
	{
		return this.lastBattleTime;
	}
	
	public boolean shipsIsEmpty()
	{
		for(int i=0;i<ships.length;i++)
		{
			if(ships[i][0]>0&&ships[i][1]>0)
			{
				return false;
			}
		}
		
		return true;
	}
	public void showBytesWriteFleet(ByteBuffer data)
	{
		int top=data.top();
		data.writeByte(0);
		int length=0;
		for(int i=0;i<ships.length;i++)
		{
			if(ships[i][1]>0)
			{
				data.writeShort(ships[i][0]);
				data.writeShort(ships[i][1]);
				data.writeByte(i);
				length++;
			}
		}
		
		if(length>0)
		{
			int newTop=data.top();
			data.setTop(top);
			data.writeByte(length);
			data.setTop(newTop);
		}
	}

	
	public int[][] getShips()
	{
		return ships;
	}
	
	
}
