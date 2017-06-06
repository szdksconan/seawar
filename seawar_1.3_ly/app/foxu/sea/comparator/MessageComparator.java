package foxu.sea.comparator;

import foxu.sea.messgae.Message;
import mustang.set.Comparator;

/** �ʼ� */
public class MessageComparator implements Comparator
{
	/* static fields */
	/** Ψһ��ʵ�� */
	private static final MessageComparator messageComparator=new MessageComparator();
	
	/** ��õ�ǰ��ʵ�� */
	public static MessageComparator getInstance()
	{
		return messageComparator;
	}

	public int compare(Object o1,Object o2)
	{
		if(!(o1 instanceof Message)||!(o2 instanceof Message))
			return COMP_EQUAL;
		Message message1=(Message)o1;
		Message message2=(Message)o2;
		int time0=message1.getCreateAt();
		int time1=message2.getCreateAt();
		if(time0<time1) return COMP_GRTR;
		if(time0>=time1) return COMP_LESS;
		return COMP_EQUAL;
	}

}
