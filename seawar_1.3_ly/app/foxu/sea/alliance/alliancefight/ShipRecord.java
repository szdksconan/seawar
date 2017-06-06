package foxu.sea.alliance.alliancefight;

import mustang.set.IntList;
import mustang.util.TimeKit;


/**
 * ��ս��ֻ��־
 * @author yw
 *
 */
public class ShipRecord
{
	/** DONATE���� PRODUCE���� ATTACK���� DEFENSE���� */
	public static final int DONATE=0,PRODUCE_DECR=1,PRODUCE_ADD=2,ATTACK=3,DEFENSE=4;
	
	int type;
	int createTime;
	/** �裬������ ���� */
	String target;
	/** �仯��ۿ������� */
	IntList portShips;
	/** �ݵ��� */
	IntList groundShips;
	/** ���α仯 */
	IntList changeShips;
	
	public ShipRecord(int type,String target,IntList portShips,IntList groundShips,IntList changeShips)
	{
		this.type=type;
		this.target=target;
		createTime=TimeKit.getSecondTime();
		this.portShips=portShips;
		this.groundShips=groundShips;
		this.changeShips=changeShips;
	}

	
	public int getType()
	{
		return type;
	}

	
	public void setType(int type)
	{
		this.type=type;
	}

	
	public int getCreateTime()
	{
		return createTime;
	}

	
	public void setCreateTime(int createTime)
	{
		this.createTime=createTime;
	}

	
	public String getTarget()
	{
		return target;
	}

	
	public void setTarget(String target)
	{
		this.target=target;
	}

	
	public IntList getLeft()
	{
		return portShips;
	}

	
	public void setLeft(IntList left)
	{
		this.portShips=left;
	}

	
	public IntList getBleft()
	{
		return groundShips;
	}

	
	public void setBleft(IntList bleft)
	{
		this.groundShips=bleft;
	}

	
	public IntList getList()
	{
		return changeShips;
	}

	
	public void setList(IntList list)
	{
		this.changeShips=list;
	}
	
	
}
