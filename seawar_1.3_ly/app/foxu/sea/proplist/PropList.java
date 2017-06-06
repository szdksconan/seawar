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
 * ��˵������Ʒ�б���
 * 
 * @version 1.0
 * @author zminleo <zmin@seasky.cn>
 */

public class PropList
{

	/* static fields */
	/** �¼��ı䳣�� */
	public static final int INCR_PROP=1,ADD_PROP_INDEX=2,DECR_PROP=3,
					DECR_PROP_INDEX=4,REMOVE_PROP=5,REMOVE_PROP_COUNT=6,
					COLLATE=11,FLUSH=12;
	public static final Logger log=LogFactory.getLogger(PropList.class);
	/* fields */
	/** ��Ʒ�б����󳤶� */
	private int length;
	/** ��Ʒ���� */
	ArrayList list=new ArrayList();
	/** Դ���� */
	Object source;
	/** �¼��ı������ */
	ChangeListenerList listener=new ChangeListenerList();

	/* properties */
	/** �����Ʒ�б����󳤶� */
	public int getLength()
	{
		return length;
	}
	/** ������Ʒ�б����󳤶� */
	public boolean setLength(int length)
	{
		// ��С���ܵ����ڲ��Ѿ��е���Ʒ����
		if(length<=list.size()) return false;
		this.length=length;
		return true;
	}
	/** ���ȫ����Ʒ���м�����п� */
	public synchronized Prop[] getProps()
	{
		Prop[] array=new Prop[list.size()];
		list.toArray(array);
		return array;
	}
	/** ���Դ���� */
	public Object getSource()
	{
		return source;
	}
	/** ����Դ���� */
	public void setSource(Object source)
	{
		this.source=source;
	}
	/** ����¼��ı������ */
	public ChangeListener getChangeListener()
	{
		return listener;
	}
	/** �����¼��ı������ */
	public void setChangeListener(ChangeListenerList listene)
	{
		this.listener=listene;
	}
	/** �����¼��ı������ */
	public void addChangeListener(ChangeListener listener)
	{
		this.listener.addListener(listener);
	}
	/** �Ƴ��¼��ı���� */
	public void removeChangeListener(ChangeListener listener)
	{
		this.listener.removeListener(listener);
	}
	/* methods */
	/** �����Ʒռ���ܸ��� */
	public synchronized int getPropCount()
	{
		int n=0;
		for(int i=list.size()-1;i>=0;i--)
		{
			if(list.get(i)!=null) n++;
		}
		return n;
	}
	/** ���ָ��λ�õ���Ʒ */
	public synchronized Prop getIndex(int i)
	{
		return (i>=0&&i<list.size())?(Prop)list.get(i):null;
	}
	/** �õ�ָ����ŵ���Ʒ����Ʒ�б��е�λ�� */
	public int getPropIndex(int propId)
	{
		return getPropIndex(propId,0);
	}
	/** �õ�ָ����ŵ���Ʒ����Ʒ�б��е�λ��,���� */
	public int getPropIndexDesc(int propId)
	{
		return getPropIndexDesc(propId,list.size()-1);
	}
	/** ���ݱ�Ż��ָ����Ʒ */
	public Prop getPropById(int propId)
	{
		return getIndex(getPropIndex(propId));
	}
	/** �����Ʒ�б��еĵ�һ����λ */
	public synchronized int getFirstEmpty()
	{
		int i=0;
		for(int n=list.size();i<n&&list.get(i)!=null;i++)
			;
		return i;
	}
	/** �鿴�Ƿ��п�λ */
	public synchronized boolean checkHasEmpty()
	{
		int i=0;
		for(int n=list.size();i<n&&list.get(i)!=null;i++)
			;
		return (i<length);
	}
	/**��ȡ�������м�����λ*/
	public synchronized int getEmptyNum()
	{
		int j=0;
	    for(int n=list.size(),i=0;i<n;i++)
	    {
	    	if(list.get(i)!=null)j++;
	    }
		return length-j;
	}
	/** �õ�ָ����ŵ���Ʒ����Ʒ�б��е�λ�� */
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
	/** �õ�ָ����ŵ���Ʒ����Ʒ�б��е�λ��,���� */
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
	/** �õ�ָ����ŵ���Ʒ����Ʒ�б��е�λ�� */
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
	/** ���ָ����ŵ���Ʒ������ */
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
	/** ���ָ��sid��Ʒ������ */
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
	/** ������Ʒ���ظ���Ʒ��list������װ���������� */
	public int getMaxCount(Prop prop)
	{
		if(prop==null) return 0;
		int count=0;
		if(prop instanceof NormalProp)
		{
			NormalProp np=(NormalProp)prop;
			int max=np.getMaxCount(),c=0;
			int i=0,n=list.size();
			// ��Ʒλʣ��ɷ������Ϳ�λ
			int pc=0,ec=0;
			Prop p;
			NormalProp temp;
			// ������Ʒ�ܷ����
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
	/** ��õ�ǰ�����Ŀ��� */
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
	 * �õ�һ����ʱ��Ʒ�Ŀ���
	 * 
	 * @param prop ��Ҫ���յ���Ʒ
	 * @return ���ؿ���
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
	 * ����Ƿ��ܼ���һ����Ʒ
	 * 
	 * @param decrProp ��Ҫ�жϵ���Ʒ����Ŀ���
	 * @param propList �����Ŀ���
	 * @return ����true��ʾ���ٳɹ�
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
	 * ����Ƿ����ڰ�������ȷ�ļ���һ����Ʒ,�����һ����Ʒ
	 * 
	 * @param incrProps ��Ҫ��ӵ���Ʒ,�м�����п�
	 * @param decrProp ��Ҫ�۳�����Ʒ,���Դ�null���߳���==0,��ʾֻ����Ƿ�������һ����Ʒ
	 * @return
	 */
	public synchronized boolean checkIncrProp(Object[] incrProps,
		Object[] decrProp)
	{
		// ��������
		long[][] snapshotProps=getSnapshot();
		// ��Ʒ�������
		long[][] tempIncrSnapshot=getSnapshotTempProp(incrProps);
		long[][] tempDecrSnapshot=getSnapshotTempProp(decrProp);
		if(!checkDecrProp(tempDecrSnapshot,snapshotProps)) return false;
		for(int i=tempIncrSnapshot.length-1;i>=0;i--)
		{
			if(tempIncrSnapshot[i][0]==0) continue;
			if(incrProps[i] instanceof NormalProp)
			{
				NormalProp prop=(NormalProp)incrProps[i];
				// �ڿ������ҵ��������Ʒ��ͬ��sid,�������Ʒ������ȥ
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
				if(tempIncrSnapshot[i][1]>0)// ���������Ϊ0,��ʾ�����Ʒû�б�������
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

	/** ����Ƿ���������Ʒ���Զ��ϲ��������Ƿ�ɹ� */
	public synchronized boolean checkIncrProp(Prop prop)
	{
		return checkIncrProp(prop,true);
	}
	/** ����Ƿ���������Ʒ������autoCombine��ʾ��Ʒ�Ƿ��Զ��ϲ��������Ƿ�ɹ� */
	public synchronized boolean checkIncrProp(Prop prop,boolean autoCombine)
	{
		if(prop==null) return false;
		if(prop instanceof NormalProp)
		{
			if(!autoCombine)
			{
				// �ҵ���λ
				int i=0;
				for(int n=list.size();i<n&&list.get(i)!=null;i++)
					;
				return (i<length);
			}
			NormalProp np=(NormalProp)prop;
			int max=np.getMaxCount(),c=0;
			int i=0,n=list.size();
			// ��Ʒλʣ��ɷ������Ϳ�λ
			int pc=0,ec=0;
			Prop p;
			NormalProp temp;
			// ������Ʒ�ܷ����
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
			// �ҵ���λ
			int i=0;
			for(int n=list.size();i<n&&list.get(i)!=null;i++)
				;
			return (i<length);
		}
		return false;
	}
	/** ������Ʒ������autoCombine��ʾ��Ʒ�Ƿ��Զ��ϲ��������Ƿ�ɹ� */
	public synchronized boolean incrProp(Prop prop,boolean autoCombine)
	{
		if(prop==null) return false;
		if(prop instanceof NormalProp)
		{
			if(!autoCombine)
			{
				// �ҵ���λ
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
			// ��Ʒλʣ��ɷ������Ϳ�λ
			int pc=0,ec=0;
			Prop p;
			NormalProp temp;
			// ������Ʒ�ܷ����
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
			// ������Ʒ
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
			// ��Ҫ��Ʒλ�Ϳ�λ
			else
			{
				// �ȷ�����Ʒλ
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
				// �ٷſ�λ
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
				// ��ʣ��ķ���
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
			// �ҵ���λ
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
	 * ����Ƿ��ܽ�ָ����Ʒ���ӵ���һָ����λ�ã� ����ʣ�µ�����>0:�����ʣ�����Ʒ������ ==0 ָ��λ��Ϊ�� �� ��ȫ���롣
	 * ==-1�������Ƿ� ��==-2������ͬ����Ʒ��==-3��Ŀ��λ����Ʒ�Ѵ����ޡ�
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
	 * ������Ʒ��ָ����λ�ã�����Ϊ��λ��ͬ����Ʒ���� ����ʣ�µ�������0Ϊȫ�����룩
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
		// ������Ʒ
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
	/** ������Ʒ�������Ƿ�ɹ� */
	public boolean checkDecrProp(int prop)
	{
		return checkDecrProp(prop,1);
	}
	/** ������Ʒ�������Ƿ�ɹ� */
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
	/** ������Ʒ�������Ƿ�ɹ� (�˷�������˳��۳�) */
	public boolean decrProp(int sid)
	{
		return decrProp(sid,1);
	}
	/** ������Ʒ�������Ƿ�ɹ� (�˷�������˳��۳�) */
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
	/** �Ƴ�ָ��λ�õ���Ʒ */
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
	/** �Ƴ�ָ��λ����Ʒ��ָ������ */
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
	/** ����Ʒ�б���ɵ��ӵ�ͬ����Ʒ���е��� */
	public synchronized void wrapProp()
	{
		Object[] prop=list.getArray();
		NormalProp backProp=null;
		NormalProp frontProp=null;
		for(int i=prop.length-1;i>=0;i--)// i�������ǰ��
		{
			if(!(prop[i] instanceof NormalProp)) continue;
			backProp=(NormalProp)prop[i];
			for(int j=0;j<i;j++)// jÿ��ѭ�����ӵ�һ����ʼ�����
			{
				if(!(prop[j] instanceof NormalProp)) continue;
				frontProp=(NormalProp)prop[j];
				if(backProp.getId()!=frontProp.getId()) continue;
				if(frontProp.getCount()<frontProp.getMaxCount())
				{
					if((frontProp.getCount()+backProp.getCount())<=frontProp
									.getMaxCount())// ���װ�������߸պ�װ��
					{
						frontProp.setCount(frontProp.getCount()
							+backProp.getCount());
						prop[i]=null;
						break;
					}
					else// װ��֮����ʣ��
					{
						int l=frontProp.getMaxCount()-frontProp.getCount();// frontProp��Ҫ���ӵ���Ʒ����
						backProp.setCount(backProp.getCount()-l);
						frontProp.setCount(frontProp.getMaxCount());
					}
				}
			}
		}
	}
	/** ��Ʒ�б��Ƿ����� */
	public boolean isFull()
	{
		for(int i=0;i<length;i++)
		{
			if(list.size()<i) return false;
			if(list.get(i)==null) return false;
		}
		return true;
	}
	/** ������ */
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
	/** ���ֽ������з����л���ö������ */
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
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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
	/** ���PropList�е�list */
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