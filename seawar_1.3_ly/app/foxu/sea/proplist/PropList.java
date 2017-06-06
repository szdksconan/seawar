package foxu.sea.proplist;

import foxu.sea.Player;
import mustang.event.ChangeListener;
import mustang.event.ChangeListenerList;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.ArrayList;
import mustang.set.Comparator;
import mustang.set.SetKit;



/**
 * 类说明：物品列表类
 * 
 * @version 1.0
 * @author zminleo <zmin@seasky.cn>
 */

public class PropList
{

	/* static fields */
	/** 事件改变常量 */
	public static final int INCR_PROP=1,ADD_PROP_INDEX=2,DECR_PROP=3,
					DECR_PROP_INDEX=4,REMOVE_PROP=5,REMOVE_PROP_COUNT=6,
					COLLATE=11,FLUSH=12;
	public static final Logger log=LogFactory.getLogger(PropList.class);
	/* fields */
	/** 物品列表的最大长度 */
	private int length;
	/** 物品数组 */
	ArrayList list=new ArrayList();
	/** 源对象 */
	Object source;
	/** 事件改变监听器 */
	ChangeListenerList listener=new ChangeListenerList();

	/* properties */
	/** 获得物品列表的最大长度 */
	public int getLength()
	{
		return length;
	}
	/** 设置物品列表的最大长度 */
	public boolean setLength(int length)
	{
		// 大小不能低于内部已经有的物品数量
		if(length<=list.size()) return false;
		this.length=length;
		return true;
	}
	/** 获得全部物品，中间可能有空 */
	public synchronized Prop[] getProps()
	{
		Prop[] array=new Prop[list.size()];
		list.toArray(array);
		return array;
	}
	/** 获得源对象 */
	public Object getSource()
	{
		return source;
	}
	/** 设置源对象 */
	public void setSource(Object source)
	{
		this.source=source;
	}
	/** 获得事件改变监听器 */
	public ChangeListener getChangeListener()
	{
		return listener;
	}
	/** 设置事件改变监听器 */
	public void setChangeListener(ChangeListenerList listene)
	{
		this.listener=listene;
	}
	/** 设置事件改变监听器 */
	public void addChangeListener(ChangeListener listener)
	{
		this.listener.addListener(listener);
	}
	/** 移除事件改变监听 */
	public void removeChangeListener(ChangeListener listener)
	{
		this.listener.removeListener(listener);
	}
	/* methods */
	/** 获得物品占的总格数 */
	public synchronized int getPropCount()
	{
		int n=0;
		for(int i=list.size()-1;i>=0;i--)
		{
			if(list.get(i)!=null) n++;
		}
		return n;
	}
	/** 获得指定位置的物品 */
	public synchronized Prop getIndex(int i)
	{
		return (i>=0&&i<list.size())?(Prop)list.get(i):null;
	}
	/** 得到指定编号的物品在物品列表中的位置 */
	public int getPropIndex(int propId)
	{
		return getPropIndex(propId,0);
	}
	/** 得到指定编号的物品在物品列表中的位置,倒序 */
	public int getPropIndexDesc(int propId)
	{
		return getPropIndexDesc(propId,list.size()-1);
	}
	/** 根据编号获得指定物品 */
	public Prop getPropById(int propId)
	{
		return getIndex(getPropIndex(propId));
	}
	/** 获得物品列表中的第一个空位 */
	public synchronized int getFirstEmpty()
	{
		int i=0;
		for(int n=list.size();i<n&&list.get(i)!=null;i++)
			;
		return i;
	}
	/** 查看是否有空位 */
	public synchronized boolean checkHasEmpty()
	{
		int i=0;
		for(int n=list.size();i<n&&list.get(i)!=null;i++)
			;
		return (i<length);
	}
	/**获取包裹中有几个空位*/
	public synchronized int getEmptyNum()
	{
		int j=0;
	    for(int n=list.size(),i=0;i<n;i++)
	    {
	    	if(list.get(i)!=null)j++;
	    }
		return length-j;
	}
	/** 得到指定编号的物品在物品列表中的位置 */
	public synchronized int getPropIndex(int propId,int index)
	{
		Prop p;
		for(int i=index,n=list.size();i<n;i++)
		{
			p=(Prop)list.get(i);
			if(p==null) continue;
			if(p.getId()==propId) return i;
		}
		return -1;
	}
	/** 得到指定编号的物品在物品列表中的位置,倒序 */
	public synchronized int getPropIndexDesc(int propId,int index)
	{
		Prop p;
		for(int i=index;i>=0;i--)
		{
			p=(Prop)list.get(i);
			if(p==null) continue;
			if(p.getId()==propId) return i;
		}
		return -1;
	}
	/** 得到指定编号的物品在物品列表中的位置 */
	public synchronized int getPropIndexBySid(int propSid,int index)
	{
		Prop p;
		for(int i=index,n=list.size();i<n;i++)
		{
			p=(Prop)list.get(i);
			if(p==null) continue;
			if(p.getSid()==propSid) return i;
		}
		return -1;
	}
	/** 获得指定编号的物品的数量 */
	public synchronized int getPropCount(long propSid)
	{
		int n=0;
		Prop p;
		for(int i=list.size()-1;i>=0;i--)
		{
			p=(Prop)list.get(i);
			if(p==null) continue;
			if(p.getId()!=propSid) continue;
			if(p instanceof UniqueProp) return 1;
			if(p instanceof NormalProp) n+=((NormalProp)p).count;
		}
		return n;
	}
	/** 获得指定sid物品的数量 */
	public synchronized int getCountBySid(int propSid)
	{
		int n=0;
		Prop p;
		for(int i=list.size()-1;i>=0;i--)
		{
			p=(Prop)list.get(i);
			if(p==null) continue;
			if(p.getSid()!=propSid) continue;
			if(p instanceof UniqueProp) n++;
			if(p instanceof NormalProp) n+=((NormalProp)p).count;
		}
		return n;
	}
	/** 根据物品返回该物品在list里所能装入的最大数量 */
	public int getMaxCount(Prop prop)
	{
		if(prop==null) return 0;
		int count=0;
		if(prop instanceof NormalProp)
		{
			NormalProp np=(NormalProp)prop;
			int max=np.getMaxCount(),c=0;
			int i=0,n=list.size();
			// 物品位剩余可放数量和空位
			int pc=0,ec=0;
			Prop p;
			NormalProp temp;
			// 计算物品能否加入
			for(;i<n;i++)
			{
				p=(Prop)list.get(i);
				if(p==null)
				{
					ec+=max;
					continue;
				}
				if(p.getId()!=prop.getId()) continue;
				temp=(NormalProp)p;
				c=temp.count;
				if(c>=max) continue;
				pc+=max-c;
			}
			count=ec+pc+(length-n)*max;
		}
		else if(prop instanceof UniqueProp)
		{
			int i=0;
			int n=list.size();
			for(;i<n;i++)
			{
				if(list.get(i)==null) count++;
			}
			count+=length-list.size();
		}
		return count;
	}
	/** 获得当前包裹的快照 */
	public long[][] getSnapshot()
	{
		long[][] snapshotProps=new long[length][2];
		for(int i=snapshotProps.length-1;i>=0;i--)
		{
			Prop prop=getIndex(i);
			if(prop!=null)
			{
				snapshotProps[i][0]=prop.getId();
				if(prop instanceof NormalProp)
				{
					snapshotProps[i][1]=((NormalProp)prop).getCount();
				}
				else
				{
					snapshotProps[i][1]=1;
				}
			}
		}
		return snapshotProps;
	}
	/**
	 * 得到一组临时物品的快照
	 * 
	 * @param prop 需要快照的物品
	 * @return 返回快照
	 */
	public long[][] getSnapshotTempProp(Object[] props)
	{
		if(props==null) return null;
		long[][] snapshotProps=new long[props.length][2];
		for(int i=props.length-1;i>=0;i--)
		{
			if(props[i] instanceof Prop)
			{
				snapshotProps[i][0]=((Prop)props[i]).getId();
				if(props[i] instanceof NormalProp)
				{
					NormalProp prop=(NormalProp)props[i];
					snapshotProps[i][1]=prop.getCount();
				}
				else
				{
					snapshotProps[i][1]=1;
				}
			}
		}
		return snapshotProps;
	}

	/**
	 * 检查是否能减少一组物品
	 * 
	 * @param decrProp 需要判断的物品数组的快照
	 * @param propList 包裹的快照
	 * @return 返回true表示减少成功
	 */
	public synchronized boolean checkDecrProp(long[][] decrProp,
		long[][] propList)
	{
		for(int i=decrProp.length-1;i>=0;i--)
		{
			if(decrProp[i][0]==0) continue;
			for(int j=propList.length-1;j>=0;j--)
			{
				if(decrProp[i][0]==propList[j][0])
				{
					propList[j][0]-=decrProp[i][0];
					if(propList[j][0]<0)
					{
						decrProp[i][0]=-propList[j][0];
						propList[j][0]=0;
					}
					else
					{
						decrProp[i][0]=0;
						break;
					}
				}
			}
		}
		for(int i=decrProp.length-1;i>=0;i--)
		{
			if(decrProp[i][1]!=0) return false;
		}
		return true;
	}

	/**
	 * 检查是否能在包裹中正确的减少一组物品,再添加一组物品
	 * 
	 * @param incrProps 需要添加的物品,中间可以有空
	 * @param decrProp 需要扣除的物品,可以传null或者长度==0,表示只检查是否能增加一组物品
	 * @return
	 */
	public synchronized boolean checkIncrProp(Object[] incrProps,
		Object[] decrProp)
	{
		// 包裹快照
		long[][] snapshotProps=getSnapshot();
		// 物品数组快照
		long[][] tempIncrSnapshot=getSnapshotTempProp(incrProps);
		long[][] tempDecrSnapshot=getSnapshotTempProp(decrProp);
		if(!checkDecrProp(tempDecrSnapshot,snapshotProps)) return false;
		for(int i=tempIncrSnapshot.length-1;i>=0;i--)
		{
			if(tempIncrSnapshot[i][0]==0) continue;
			if(incrProps[i] instanceof NormalProp)
			{
				NormalProp prop=(NormalProp)incrProps[i];
				// 在快照中找到与这个物品相同的sid,把这个物品叠加上去
				for(int j=snapshotProps.length-1;j>=0;j--)
				{
					if(snapshotProps[j][0]==tempIncrSnapshot[i][0])
					{
						snapshotProps[j][1]+=tempIncrSnapshot[i][1];
						if(snapshotProps[j][1]>prop.getMaxCount())
						{
							tempIncrSnapshot[i][1]=snapshotProps[j][1]
								-prop.getMaxCount();
							snapshotProps[j][1]=prop.getMaxCount();
						}
						else
						{
							tempIncrSnapshot[i][1]=0;
							break;
						}
					}
				}
				if(tempIncrSnapshot[i][1]>0)// 如果个数不为0,表示这个物品没有被叠加完
				{
					for(int j=snapshotProps.length-1;j>=0;j--)
					{
						if(snapshotProps[j][0]==0)
						{
							snapshotProps[j][0]=tempIncrSnapshot[i][0];
							snapshotProps[j][1]=tempIncrSnapshot[i][1];
							tempIncrSnapshot[i][1]=0;
							break;
						}
					}
				}
			}
			else
			{
				for(int j=snapshotProps.length-1;j>=0;j--)
				{
					if(snapshotProps[j][0]==0)
					{
						snapshotProps[j][0]=tempIncrSnapshot[i][0];
						snapshotProps[j][1]=1;
						tempIncrSnapshot[i][1]=0;
					}
				}
			}
		}
		for(int i=tempIncrSnapshot.length-1;i>=0;i--)
		{
			if(tempIncrSnapshot[i][1]!=0) return false;
		}
		return true;
	}

	/** 检查是否能增加物品，自动合并，返回是否成功 */
	public synchronized boolean checkIncrProp(Prop prop)
	{
		return checkIncrProp(prop,true);
	}
	/** 检查是否能增加物品，参数autoCombine表示物品是否自动合并，返回是否成功 */
	public synchronized boolean checkIncrProp(Prop prop,boolean autoCombine)
	{
		if(prop==null) return false;
		if(prop instanceof NormalProp)
		{
			if(!autoCombine)
			{
				// 找到空位
				int i=0;
				for(int n=list.size();i<n&&list.get(i)!=null;i++)
					;
				return (i<length);
			}
			NormalProp np=(NormalProp)prop;
			int max=np.getMaxCount(),c=0;
			int i=0,n=list.size();
			// 物品位剩余可放数量和空位
			int pc=0,ec=0;
			Prop p;
			NormalProp temp;
			// 计算物品能否加入
			for(;i<n;i++)
			{
				p=(Prop)list.get(i);
				if(p==null)
				{
					ec+=max;
					continue;
				}
				if(!(p instanceof NormalProp)||p.getId()!=prop.getId())
					continue;
				temp=(NormalProp)p;
				c=temp.count;
				if(c>=max) continue;
				pc+=max-c;
			}
			return (ec+pc+(length-n)*max>=np.count);
		}
		else if(prop instanceof UniqueProp)
		{
			// 找到空位
			int i=0;
			for(int n=list.size();i<n&&list.get(i)!=null;i++)
				;
			return (i<length);
		}
		return false;
	}
	/** 增加物品，参数autoCombine表示物品是否自动合并，返回是否成功 */
	public synchronized boolean incrProp(Prop prop,boolean autoCombine)
	{
		if(prop==null) return false;
		if(prop instanceof NormalProp)
		{
			if(!autoCombine)
			{
				// 找到空位
				int i=0;
				for(int n=list.size();i<n&&list.get(i)!=null;i++)
					;
				if(i>=length) return false;
				if(i<list.size())
					list.set(prop,i);
				else
					list.add(prop,i);
				if(listener!=null)
					listener.change(this,INCR_PROP,prop,new Integer(i));
				PropLogManager.getInstance().incrPorp((Player)source,prop.getSid(),((NormalProp)prop).getCount());
				return true;
			}
			NormalProp np=(NormalProp)prop;
			int max=np.getMaxCount(),c=0;
			int i=0,n=list.size(),j=-1,k=-1;
			// 物品位剩余可放数量和空位
			int pc=0,ec=0;
			Prop p;
			NormalProp temp;
			// 计算物品能否加入
			for(;i<n;i++)
			{
				p=(Prop)list.get(i);
				if(p==null)
				{
					ec+=max;
					if(k<0) k=i;
					continue;
				}
				if(!(p instanceof NormalProp)||p.getId()!=prop.getId())
					continue;
				temp=(NormalProp)p;
				c=temp.count;
				if(c>=max) continue;
				pc+=max-c;
				if(j<0) j=i;
			}
			int nn=np.count;
			if(ec+pc+(length-n)*max<nn) return false;
			// 加入物品
			if(pc>=nn)
			{
				for(i=j;i<n;i++)
				{
					p=(Prop)list.get(i);
					if(p==null) continue;
					if(p.getId()!=prop.getId()) continue;
					temp=(NormalProp)p;
					c=temp.count;
					if(c>=max) continue;
					if(nn>max-c)
					{
						temp.count=max;
						nn-=max-c;
						continue;
					}
					temp.count+=nn;
					nn=0;
					break;
				}
			}
			// 需要物品位和空位
			else
			{
				// 先放满物品位
				if(j>=0)
				{
					for(i=j;i<n;i++)
					{
						p=(Prop)list.get(i);
						if(p==null) continue;
						if(p.getId()!=prop.getId()) continue;
						temp=(NormalProp)p;
						c=temp.count;
						if(c>=max) continue;
						temp.count=max;
						nn-=max-c;
					}
				}
				// 再放空位
				if(k>=0)
				{
					for(i=k;i<n&&nn>0;i++)
					{
						p=(Prop)list.get(i);
						if(p!=null) continue;
						temp=(NormalProp)np.clone();
						temp.count=nn>max?max:nn;
						nn-=max;
						list.set(temp,i);
					}
				}
				else
					k=list.size();
				// 将剩余的放入
				while(nn>0)
				{
					temp=(NormalProp)np.clone();
					temp.count=nn>max?max:nn;
					nn-=max;
					list.add(temp);
				}
			}
			if(j<0) j=k;
			if(listener!=null)
				listener.change(this,INCR_PROP,prop,new Integer(j));
			PropLogManager.getInstance().incrPorp((Player)source,prop.getSid(),((NormalProp)prop).getCount());
			return true;
		}
		else if(prop instanceof UniqueProp)
		{
			// 找到空位
			int i=0;
			for(int n=list.size();i<n&&list.get(i)!=null;i++)
				;
			if(i>=length) return false;
			if(i<list.size())
				list.set(prop,i);
			else
				list.add(prop,i);
			if(listener!=null)
				listener.change(this,INCR_PROP,prop,new Integer(i));
			PropLogManager.getInstance().incrPorp((Player)source,prop.getSid(),((NormalProp)prop).getCount());
			return true;
		}
		return false;
	}
	/**
	 * 检查是否能将指定物品叠加到另一指定的位置， 返回剩下的数量>0:放完后剩余的物品数量。 ==0 指定位置为空 或 完全放入。
	 * ==-1：参数非法 。==-2：不是同类物品。==-3：目标位置物品已达上限。
	 */
	public synchronized int checkAddProp(Prop prop,int index)
	{
		if(prop==null||index>=length) return -1;
		if(index>=list.size()) return 0;
		Prop temp=getIndex(index);
		if(temp==null) return 0;

		if(prop.getId()!=temp.getId()) return -2;
		if(!(prop instanceof NormalProp)) return -1;
		if(!(temp instanceof NormalProp)) return -1;

		NormalProp tp=(NormalProp)temp;
		NormalProp np=(NormalProp)prop;

		if(tp.count>=tp.getMaxCount()) return -3;
		int count=tp.count+np.count-tp.getMaxCount();
		if(count<0) count=0;
		return count;
	}

	/**
	 * 增加物品到指定的位置（必须为空位或同类物品）， 返回剩下的数量（0为全部放入）
	 */
	public synchronized int addProp(Prop prop,int index)
	{
		if(prop==null||index<0||index>=length) return -1;
		if(index>=list.size())
		{
			list.add(prop,index);
			if(listener!=null)
				listener.change(this,ADD_PROP_INDEX,prop,new Integer(index));
			return 0;
		}
		Prop temp=getIndex(index);
		if(temp==null)
		{
			list.set(prop,index);
			if(listener!=null)
				listener.change(this,ADD_PROP_INDEX,prop,new Integer(index));
			return 0;
		}
		if(prop.getId()!=temp.getId()) return -2;
		if(!(prop instanceof NormalProp)) return -1;
		if(!(temp instanceof NormalProp)) return -1;
		NormalProp tp=(NormalProp)temp;
		NormalProp np=(NormalProp)prop;
		if(tp.count>=tp.getMaxCount()) return -3;
		int count=np.count;
		// 放入物品
		if(tp.count+count<=tp.getMaxCount())
		{
			np.count=0;
			tp.count=tp.count+count;
		}
		else
		{
			count=tp.getMaxCount()-tp.count;
			np.count-=count;
			tp.count=tp.getMaxCount();
		}
		if(listener!=null)
			listener.change(this,ADD_PROP_INDEX,prop,new Integer(index),
				new Integer(count));
		return np.count;
	}
	/** 减少物品，返回是否成功 */
	public boolean checkDecrProp(int prop)
	{
		return checkDecrProp(prop,1);
	}
	/** 减少物品，返回是否成功 */
	public synchronized boolean checkDecrProp(int prop,int n)
	{
		if(prop==0||n<=0) return false;
		int i=getPropIndex(prop);
		if(i<0) return false;
		Prop p=(Prop)list.get(i);
		if(p instanceof NormalProp)
		{
			NormalProp np=(NormalProp)p;
			if(np.count>=n) return true;
			int nn=np.count;
			while((i=getPropIndex(prop,i+1))>=0)
			{
				nn+=((NormalProp)list.get(i)).count;
				if(nn>=n) break;
			}
			return (i>=0);
		}
		else if(p instanceof UniqueProp)
		{
			return (n==1);
		}
		return false;
	}
	/** 减少物品，返回是否成功 (此方法将按顺序扣除) */
	public boolean decrProp(int sid)
	{
		return decrProp(sid,1);
	}
	/** 减少物品，返回是否成功 (此方法将按顺序扣除) */
	public synchronized boolean decrProp(int propId,int count)
	{
		if(propId==0||count<=0) return false;
		int i=getPropIndexDesc(propId);
		if(i<0) return false;
		Prop p=(Prop)list.get(i);
		if(p instanceof NormalProp)
		{
			NormalProp np=(NormalProp)p;
			if(np.count>count)
				np.count-=count;
			else if(np.count==count)
				list.set(null,i);
			else
			{
				int nn=np.count;
				while((i=getPropIndexDesc(propId,i-1))>=0)
				{
					nn+=((NormalProp)list.get(i)).count;
					if(nn>=count) break;
				}
				if(i<0) return false;
				i=-1;
				nn=count;
				while(nn>0&&(i=getPropIndexDesc(propId,list.size()-1))>=0)
				{
					np=(NormalProp)list.get(i);
					if(nn>=np.count)
					{
						list.set(null,i);
						nn-=np.count;
					}
					else
					{
						np.count-=nn;
						nn=0;
					}
					if(listener!=null)
						listener.change(this,DECR_PROP_INDEX,propId,nn,i);
				}
			}
			if(listener!=null) listener.change(this,DECR_PROP,propId,count);
			//log.info("DECR_PROP pid:"+((Player)source).getId()+" sid:"+makeSid(propId)+"  num:"+count+" cnum:"+getCountBySid(propId));
			PropLogManager.getInstance().decrProp((Player)source,propId,count);
			return true;
		}
		else if(p instanceof UniqueProp)
		{
			if(count!=1) return false;
			list.set(null,i);
			//log.info("DECR_PROP pid:"+((Player)source).getId()+" sid:"+makeSid(propId)+"  num:"+1+" cnum:"+getCountBySid(propId));
			PropLogManager.getInstance().decrProp((Player)source,propId,count);
			return true;
		}
		return false;
	}
	/** 移除指定位置的物品 */
	public synchronized Prop removeProp(int index)
	{
		if(index<0||index>=list.size()) return null;
		Prop p=(Prop)list.get(index);
		if(p==null) return null;
		list.set(null,index);
		if(listener!=null)
			listener.change(this,REMOVE_PROP,p,new Integer(index));
		return p;
	}
	/** 移除指定位置物品的指定数量 */
	public synchronized Prop removeProp(int index,int count)
	{
		if(index<0||index>=list.size()||count<1) return null;
		Prop p=(Prop)list.get(index);
		if(p==null) return null;
		if(p instanceof NormalProp)
		{
			NormalProp np=(NormalProp)p;
			if(np.count<count) return null;
			if(np.count>count)
			{
				np.count-=count;
				np=(NormalProp)np.clone();
				np.count=count;
			}
			else
			{
				list.set(null,index);
			}
			if(listener!=null)
				listener.change(this,REMOVE_PROP_COUNT,np,
					new Integer(index),new Integer(count));
			return np;
		}
		if(count!=1) return null;
		list.set(null,index);
		if(listener!=null)
			listener.change(this,REMOVE_PROP_COUNT,p,new Integer(index),
				new Integer(count));
		return p;
	}
	/** 将物品列表里可叠加的同类物品进行叠加 */
	public synchronized void wrapProp()
	{
		Object[] prop=list.getArray();
		NormalProp backProp=null;
		NormalProp frontProp=null;
		for(int i=prop.length-1;i>=0;i--)// i从最后往前找
		{
			if(!(prop[i] instanceof NormalProp)) continue;
			backProp=(NormalProp)prop[i];
			for(int j=0;j<i;j++)// j每次循环都从第一个开始向后找
			{
				if(!(prop[j] instanceof NormalProp)) continue;
				frontProp=(NormalProp)prop[j];
				if(backProp.getId()!=frontProp.getId()) continue;
				if(frontProp.getCount()<frontProp.getMaxCount())
				{
					if((frontProp.getCount()+backProp.getCount())<=frontProp
									.getMaxCount())// 如果装不满或者刚好装满
					{
						frontProp.setCount(frontProp.getCount()
							+backProp.getCount());
						prop[i]=null;
						break;
					}
					else// 装满之后还有剩余
					{
						int l=frontProp.getMaxCount()-frontProp.getCount();// frontProp需要增加的物品数量
						backProp.setCount(backProp.getCount()-l);
						frontProp.setCount(frontProp.getMaxCount());
					}
				}
			}
		}
	}
	/** 物品列表是否已满 */
	public boolean isFull()
	{
		for(int i=0;i<length;i++)
		{
			if(list.size()<i) return false;
			if(list.get(i)==null) return false;
		}
		return true;
	}
	/** 整理方法 */
	public synchronized void collate(Comparator c,boolean descending)
	{
		Object[] array=list.getArray();
		int i=0,j=list.size()-1;
		while(true)
		{
			for(;i<j&&array[i]!=null;i++)
				;
			for(;j>i&&array[j]==null;j--)
				;
			if(i>=j) break;
			array[i++]=array[j];
			array[j--]=null;
		}
		if(array[i]!=null) i++;
		SetKit.sort(array,0,i,c,descending);
		list=new ArrayList(array,i);
		if(listener!=null)
			listener.change(this,COLLATE,c,new Boolean(descending));
	}
	/** 从字节数组中反序列化获得对象的域 */
	public synchronized Object bytesRead(ByteBuffer data)
	{
		data.readUnsignedShort();
		length=Player.BUNDLE_SIZE;
		int n=data.readUnsignedShort();
		list.clear();
		for(int i=0;i<n;i++)
			list.add(Prop.bytesReadProp(data));
		if(listener!=null) listener.change(this,FLUSH);
		return this;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public synchronized void bytesWrite(ByteBuffer data)
	{
		data.writeShort(length);
		int n=list.size();
		data.writeShort(n);
		Prop p;
		for(int i=0;i<n;i++)
		{
			p=(Prop)list.get(i);
			if(p!=null)
				p.bytesWrite(data);
			else
				data.writeShort(0);
		}
	}
	public synchronized void showBytesWrite(ByteBuffer data,int time)
	{
		int top=data.top();
		data.writeByte(0);
		int length=0;
		int n=list.size();
		Prop p;
		for(int i=0;i<n;i++)
		{
			p=(Prop)list.get(i);
			if(p!=null)
			{
				p.bytesWrite(data);
				length++;
			}
		}
		if(length>0)
		{
			int current=data.top();
			data.setTop(top);
			data.writeByte(length);
			data.setTop(current);
		}
	}
	/** 清空PropList中的list */
	public void clear()
	{
		list.clear();
	}
	
	public static String makeSid(int sid)
	{
		if(sid<10)
		{
			return sid+"    ";
		}else if(sid<100)
		{
			return sid+"   ";
		}
		else if(sid<1000)
		{
			return sid+"  ";
		}
		else if(sid<10000)
		{
			return sid+" ";
		}
		return sid+"";
	}
	/* common methods */
	public String toString()
	{
		StringBuffer string=new StringBuffer();
		Object obj=null;
		for(int i=0;i<list.size();i++)
		{
			if(list.get(i)==null) continue;
			obj=list.get(i);
			if(string.length()<=0)
			{
				string.append(obj.toString());
			}
			else
			{
				string.append("#"+obj.toString());
			}
		}
		return string.toString();
	}
}