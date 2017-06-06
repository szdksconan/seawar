package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.sea.PublicConst;
import foxu.sea.kit.SeaBackKit;
/**
 * �������� �ؿ�ս��
 * 
 */
public class HCityCheckPoint  extends SelfCheckPoint
{
	
//	public static 	int [] award={55001,55002,55003,55004,55005,55006,55007,55008};
	/**���Ͻ��ӵ�����*/ 
	IntKeyHashMap setshipFleets=new IntKeyHashMap();
	/**�½ڽ���*/
	int[] charteraward=new int[1];
	/**����ʱ��*/
	int attckTime;
	/**����Ҳ������Ĺؿ�*/
	int[] attackList=new int[1];
	public HCityCheckPoint()
	{
		checkPointSid=PublicConst.HERITAGECITY_CHECK_SID;// ��ʼ�ؿ�
	}
	public void showBytesWrite(ByteBuffer data)
	{
		int timenow =TimeKit.getSecondTime();
		boolean flage=false;
		if(attckTime==0) attckTime=timenow;
		else {flage=SeaBackKit.isSameWeek(timenow,attckTime);}
		if(!flage){clear();attckTime=timenow;}
		data.writeInt(SeaBackKit.getWeekEndTime()-timenow);
		data.writeShort(checkPointSid);
		data.writeByte(list.length);
		for(int i=0;i<list.length;i++)
		{
			data.writeInt(list[i]);
		}
		data.writeByte(attackList.length);
		for(int i=0;i<attackList.length;i++)
		{
			data.writeByte(i);
			data.writeInt(attackList[i]);
		}
		data.writeByte(PublicConst.COMBINED_CHAPTER.length);
		for(int i=0;i<PublicConst.COMBINED_CHAPTER.length;i++)
		{
			int chapter=PublicConst.COMBINED_CHAPTER[i]-1;
			data.writeByte(chapter);
			int key=(chapter)/32;
			int index=(chapter)%32;
			boolean flager=key>=charteraward.length?true
				:((charteraward[key]>>>index)&1)==0;
			//����ȡ
			if(flager && CheckAttacklist(chapter))
				data.writeByte(1);
			//�Ѿ���ȡ
			else if(CheckAttacklist(chapter))
				data.writeByte(2);
			//������ȡ
			else
				data.writeByte(0);
		}
		
		if(setshipFleets.get(0)!=null)
		{
			int [] fleets=(int[])setshipFleets.get(0);
			int flags=1;
			data.writeByte(fleets.length);
			for(int i=0;i<fleets.length;i++)
			{
					data.writeByte(fleets[i]);
					for(int j=0;j<fleets[i];j++)
					{
						int [] fleet=(int[])setshipFleets.get(flags);
						data.writeByte(fleet[0]);
						data.writeShort(fleet[1]);
						data.writeShort(fleet[2]);
						flags+=1;
					}
			}
		}
		else
		{
			data.writeByte(0);
		}

	}
	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeShort(checkPointSid);
		data.writeByte(list.length);
		for(int i=0;i<list.length;i++)
		{
			data.writeInt(list[i]);
		}
		data.writeByte(charteraward.length);
		for(int i=0;i<charteraward.length;i++)
		{
			data.writeShort(charteraward[i]);
		}
		data.writeByte(setshipFleets.size());
		for(int i=0;i<setshipFleets.size();i++)
		{
			int [] fleets=(int[])setshipFleets.get(i);
			for(int j=0;j<fleets.length;j++)
			{
				data.writeInt(fleets[j]);
			}
		}
		data.writeInt(attckTime);
		data.writeByte(attackList.length);
		for(int i=0;i<attackList.length;i++)
		{
			data.writeInt(attackList[i]);
		}
	}
	/** �����л� */
	public Object bytesRead(ByteBuffer data)
	{
		checkPointSid=data.readUnsignedShort();
		int length=data.readUnsignedByte();
		list=new int[length];
		for(int i=0;i<length;i++)
		{
			list[i]=data.readInt();
		}
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			charteraward[i]=data.readShort();
		}
		len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			int[] fleets=new int[3];
			for(int j=0;j<fleets.length;j++)
			{
				fleets[j]=data.readInt();
			}
			setshipFleets.put(i,fleets);
		}
		attckTime=data.readInt();
		len=data.readUnsignedByte();
		attackList=new int[len];
		for(int i=0;i<len;i++)
		{
			attackList[i]=data.readInt();
		}
		return this;
	}
	public void attactRecord(int chapter,int index)
	{
		if(chapter>attackList.length)
		{
			int[] temp=new int[chapter];
			System.arraycopy(attackList,0,temp,0,attackList.length);
			attackList=temp;
		}
		int  hrecord=(1<<index);
		attackList[chapter-1]+=hrecord;
	}
	
	public boolean CheckAttacklist(int chapter)
	{
			if(chapter>=attackList.length)
				return false;
			boolean flag=true;
			int record=attackList[chapter];
			for(int i=0;i<16;i+=1)
			{
				int nowr=(record>>>i)&1;
				if(nowr==0){flag=false;break;}
			}
		return flag;
	}
	public boolean Checkthechapter(int chapter)
	{
		boolean flag=true;
		// ��ȡ��ĳһ�½ڵ�����
		int fore=list[chapter];
		for(int i=0;i<16;i++)
		{
			// ��ȡ֮ǰĳ��index
			int foreStar=(fore>>>i*2)&3;
			if(foreStar==0) flag=false;
		}
		return flag;
	}
	public IntKeyHashMap getSetshipFleets()
	{
		return setshipFleets;
	}
	public void setSetshipFleets(IntKeyHashMap setshipFleets)
	{
		this.setshipFleets=setshipFleets;
	}
	public int[] getCharteraward()
	{
		return charteraward;
	}
	public void setCharteraward(int[] charteraward)
	{
		this.charteraward=charteraward;
	}
	
	public int getAttckTime()
	{
		return attckTime;
	}
	
	public void setAttckTime(int attckTime)
	{
		this.attckTime=attckTime;
	}
	/**ˢ��*/
	public void clear()
	{
		charteraward=new int[1];
		attackList=new int[1];
	}
	
	/**����Ƿ�����콱**/
	public int checkGetAward(int chapter)
	{	
		int timenow=TimeKit.getSecondTime();
		if(attckTime==0) attckTime=timenow;
		boolean flage=SeaBackKit.isSameWeek(timenow,attckTime);
		if(!flage){clear();attckTime=timenow;}
		chapter-=1;
		int key=(chapter)/32;
		int index=(chapter)%32;
		boolean flager=key>=charteraward.length?true
			:((charteraward[key]>>>index)&1)==0;
		// ����ȡ
		if(flager&&CheckAttacklist(chapter))
			return 1;
		// �Ѿ���ȡ
		if(CheckAttacklist(chapter))
			return 2;
		return 0;
	}	
	
	/**����콱��¼**/
	public void addAwardRecord(int chapter)
	{
		chapter-=1;
		int key=(chapter)/32;
		int index=(chapter)%32;
		if(key>=charteraward.length)
		{
			int[] temp=new int[key+1];
			System.arraycopy(charteraward,0,temp,0,charteraward.length);
			charteraward=temp;
		}
		charteraward[key]+=1<<index;
	}
	public int[] getAttackList()
	{
		return attackList;
	}
	
	public void setAttackList(int[] attackList)
	{
		this.attackList=attackList;
	}
	
}
