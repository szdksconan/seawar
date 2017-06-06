package foxu.sea;


/**
 * ˳�������б�
 * @author yw
 *
 */
public class OrderList
{
	/* fields */
	/** �б�Ķ������� */
	Object[] array;
	
	/** ����Ԫ�ص�����β */
	public synchronized void add(Object obj)
	{
		if(array==null)
		{
			array=new Object[1];
			array[0]=obj;
		}else
		{
			addAt(obj,array.length);
		}
		
	}
	/** ����Ԫ�ص�ָ��λ�� ԭ˳�򲻱�*/
	public synchronized void addAt(Object obj,int index)
	{
		if(array==null)
		{
			array=new Object[1];
			array[0]=obj;
		}else
		{
			if(index<0)index=0;
			if(index>array.length)index=array.length;
			Object[] temp=new Object[array.length+1];
			temp[index]=obj;
			for(int i=array.length-1,j=temp.length-1;i>=0;i--,j--)
			{
				if(j==index)
				{
					i++;
					continue;
				}
				temp[j]=array[i];
			}
			array=temp;
		}
		
	}
	/** ɾ������β */
	public Object remove()
	{
		if(array==null)return null;
		return removeAt(array.length-1);
	}
	/** ɾ��ָ��Ԫ�� ԭ˳�򲻱� */
	public synchronized Object removeAt(int index)
	{
		if(array==null) return null;
		if(index<0||index>=array.length) return null;
		Object[] temp=new Object[array.length-1];
		for(int i=array.length-1,j=temp.length-1;i>=0;i--)
		{
			if(i==index) continue;
			temp[j]=array[i];
			j--;
		}
		Object rm=array[index];
		array=temp;
		return rm;
	}
	/** ��ȡ���鳤�� */
	public int size()
	{
		if(array==null)return 0;
		return array.length;
	}
	/** ��ȡָ��Ԫ�� */
	public synchronized Object get(int index)
	{
		if(array==null)return null;
		if(index<0||index>=array.length)return null;
		return array[index];
	}
	/** ��� */
	public synchronized void clear()
	{
		array=null;
	}
}
