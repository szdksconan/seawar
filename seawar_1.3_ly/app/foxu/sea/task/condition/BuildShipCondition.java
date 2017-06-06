package foxu.sea.task.condition;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.builds.Product;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.task.Condition;
import foxu.sea.task.Task;
import foxu.sea.task.TaskEvent;

/**
 * ��ɽ��촬ֻ author:icetiger
 */
public class BuildShipCondition extends Condition
{

	public static final int BUILD_FINISH=1,OWNED_SHIP=2;
	/** ��������sid */
	int shipSid;
	/** �������� */
	int num;
	/** type=1������� type=2ӵ�� */
	int type;

	/** ���л� ��ǰ����Ĵ�ֻ���� */
	int nowNum;

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		nowNum=data.readUnsignedShort();
		return this;
	}

	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeShort(nowNum);
	}

	public int checkCondition(Player player,TaskEvent event)
	{
		int checkStat=0;
		if(event!=null
			&&event.getEventType()==PublicConst.SHIP_PRODUCE_TASK_EVENT)
		{
			if(event.getSource() instanceof Product)
			{
				if(type==BUILD_FINISH)
				{
					Product product=(Product)event.getSource();
					if(product.getSid()==shipSid||shipSid==0)
						nowNum+=product.getNum();
					if(nowNum>=num)
					{
						nowNum=num;
						return Task.TASK_FINISH;
					}
					checkStat=Task.TASK_CHANGE;
				}
			}
		}
		if(type==OWNED_SHIP)
		{
			nowNum=player.getIsland().getShipsBySid(shipSid,
				player.getIsland().getTroops());
			nowNum+=player.getIsland().getShipsBySidForDefendGroup(shipSid);
			if(event!=null&&event.getParam() instanceof CreatObjectFactory)
			{
				nowNum+=SeaBackKit.getEventShipCount(shipSid,player,(CreatObjectFactory)event.getParam());
			}
			if(nowNum>=num)
			{
				return Task.TASK_FINISH;
			}
		}
		return checkStat;
	}

	/**
	 * @return num
	 */
	public int getNum()
	{
		return num;
	}

	/**
	 * @param num Ҫ���õ� num
	 */
	public void setNum(int num)
	{
		this.num=num;
	}

	/**
	 * @return shipSid
	 */
	public int getShipSid()
	{
		return shipSid;
	}

	/**
	 * @param shipSid Ҫ���õ� shipSid
	 */
	public void setShipSid(int shipSid)
	{
		this.shipSid=shipSid;
	}
	
	
	public int getType()
	{
		return type;
	}

	
	public void setType(int type)
	{
		this.type=type;
	}

	public void showBytesWrite(ByteBuffer data,Player p)
	{
		data.writeShort(nowNum);
	}
}
