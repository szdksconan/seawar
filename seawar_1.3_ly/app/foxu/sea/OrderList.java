package foxu.sea;


/**
 * 顺序数组列表
 * @author yw
 *
 */
public class OrderList
{
	/* fields */
	/** 列表的对象数组 */
	Object[] array;
	
	/** 加入元素到数组尾 */
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
	/** 加入元素到指定位置 原顺序不变*/
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
	/** 删除数组尾 */
	public Object remove()
	{
		if(array==null)return null;
		return removeAt(array.length-1);
	}
	/** 删除指定元素 原顺序不变 */
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
	/** 获取数组长度 */
	public int size()
	{
		if(array==null)return 0;
		return array.length;
	}
	/** 获取指定元素 */
	public synchronized Object get(int index)
	{
		if(array==null)return null;
		if(index<0||index>=array.length)return null;
		return array[index];
	}
	/** 清空 */
	public synchronized void clear()
	{
		array=null;
	}
}
