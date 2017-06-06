package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;
import foxu.sea.order.Order;


public class OrderSave extends ObjectSave
{
    Order order;
	@Override
	public ByteBuffer getByteBuffer()
	{
		if(order==null)return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		order.bytesWrite(bb);
		return bb;
	}

	@Override
	public Order getData()
	{
		// TODO �Զ����ɷ������
		return order;
	}

	@Override
	public int getId()
	{
		// TODO �Զ����ɷ������
		return order.getId();
	}

	@Override
	public void setData(Object data)
	{
		// TODO �Զ����ɷ������
		order  = (Order)data;
	}

}
