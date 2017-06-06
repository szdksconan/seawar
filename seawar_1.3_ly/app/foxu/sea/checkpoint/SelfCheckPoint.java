package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;

/**
 * �Լ��Ĺؿ�״̬ author:icetiger
 */
public class SelfCheckPoint
{

	/** �½ڵ��ܹ����� */
	public static final int MAX_CHAPTER=11;
	/** �½ڹؿ� */
	public static final int CHAPTER_1=1,CHAPTER_2=2,CHAPTER_3=3,CHAPTER_4=4,
					CHAPTER_5=5;
	/** ÿ���½ڵĹؿ����� */
	public static final int CHAPTER_NUM=16;
	/** ��õ����Ǹ��� */
	public static final int ONE_STAR=1,TOW_STAR=2,THREE_STAR=3;
	/** �ؿ����ݴ洢 ÿ��int��һ���ؿ� */
	int[] list=new int[1];
	/** ��ǰδͨ���Ĺؿ�sid Ĭ��Ϊ10001 ���õĵ�һ���ؿ� */
	int checkPointSid=10001;
	/** �ؿ�������ȡ���  int�ùؿ��콱���*/
	int[] awardRecords = new int[1];

	/** ��ȡĳһ�µ������� */
	public int getChapterStar(int chapter)
	{
		if(chapter>=list.length)
		{
			return 0;
		}
		// ��ȡ��ĳһ�½ڵ�����
		int fore=list[chapter];
		int star=0;
		for(int i=30;i>=0;i-=2)
		{
			int foreStar=(fore>>>i)&3;
			star+=foreStar;
		}
		return star;
	}
	
	/**��ȡ����������*/
	public int getAllstars()
	{
		int allstars = 0;
		for(int i=0;i<list.length;i++)
		{
			allstars+=getChapterStar(i);
		}
		return allstars;
	}

	/** ������һ���濨 �Ǵ���0��ͨ���� */
	public void addPoint(int star,int nextSid,int chapter,int index)
	{
		if(star<=0) return;
		if(chapter>=list.length)
		{
			int[] temp=new int[chapter+1];
			System.arraycopy(list,0,temp,0,list.length);
			list=temp;
		}
		// ��ȡ��ĳһ�½ڵ�����
		int fore=list[chapter];
		// ��ȡ֮ǰĳ��index
		int foreStar=(fore>>>index)&3;
		// ��������ԭ������ʱ�Ž��м�¼
		if(star>foreStar)
		{
			// ��ԭ��¼���
			foreStar=(foreStar<<index);
			fore=fore^foreStar;
			// ���¼�¼д��ԭ��¼λ��
			star=(star<<index);
			fore=fore|star;
			list[chapter]=fore;
		}
		if(nextSid>checkPointSid) checkPointSid=nextSid;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		data.writeShort(checkPointSid);
		data.writeByte(list.length);
		for(int i=0;i<list.length;i++)
		{
			data.writeInt(list[i]);
		}
		data.writeByte(awardRecords.length);
		for(int i=0;i<awardRecords.length;i++)
		{
			data.writeShort(i+1); //sid
			data.writeByte(awardRecords[i]);
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
	}

	public Object bytesRead(ByteBuffer data)
	{
		checkPointSid=data.readUnsignedShort();
		int length=data.readUnsignedByte();
		list=new int[length];
		for(int i=0;i<length;i++)
		{
			list[i]=data.readInt();
		}
		return this;
	}
	
	public Object bytesReadAwardRecord(ByteBuffer data)
	{
		if(data.length()>0)
		{
			int lengthAward=data.readUnsignedByte();
			awardRecords=new int[lengthAward];
			for(int i=0;i<lengthAward;i++)
			{
				awardRecords[i]=data.readInt();
			}
		}
		return this;
	}
	
	public void bytesWriteAwardRecord(ByteBuffer data)
	{
		data.writeByte(awardRecords.length);
		for(int i=0;i<awardRecords.length;i++)
		{
			data.writeInt(awardRecords[i]);
		}
	}

	/**
	 * @return list
	 */
	public int[] getList()
	{
		return list;
	}

	/**
	 * @param list Ҫ���õ� list
	 */
	public void setList(int list[])
	{
		this.list=list;
	}

	/**
	 * @return checkPointSid
	 */
	public int getCheckPointSid()
	{
		return checkPointSid;
	}

	/**
	 * @param checkPointSid Ҫ���õ� checkPointSid
	 */
	public void setCheckPointSid(int checkPointSid)
	{
		this.checkPointSid=checkPointSid;
	}
	/** ��ȡĳ�ص������� */
	public int getStar(int chapter,int index)
	{
		if(list==null||list.length<=chapter)return 0;
		return (list[chapter]>>index*2)&3;
	}
	
	/**
	 * ����Ƿ������ȡ���ؿ����� 
	 * @param chapter �½� 0��ʼ
	 * @param index �ڼ������� 0��ʼ
	 * @return 0������ȡ 1δ�ﵽ���� 2�Ѿ���ȡ
	 */
	public int checkReceived(int chapter,int index,Chapter c)
	{
		int starts=getChapterStar(chapter);
		int[] star=c.getAwardsStar();
		if(starts<star[index]) return 1;
		if(awardRecords==null||awardRecords.length<=chapter) return 0;
		if(((awardRecords[chapter]>>index)&0x1)==1) return 2;
		return 0;
	}
	
	/** ����콱��¼ */
	public void addRecord(int chapter,int index)
	{
		if(awardRecords.length<=chapter)
		{
			int[] arr=new int[chapter+1];
			System.arraycopy(awardRecords,0,arr,0,awardRecords.length);
			awardRecords=arr;
		}
		int record=awardRecords[chapter];
		if(((record>>index)&0x1)==0)
		{
			record+=(0x1<<index);
		}
		awardRecords[chapter]=record;
	}
	/**����Ƿ�ͨ��**/
	public boolean checkChapterStar(int chapter)
	{
		if(chapter>=list.length)
			return false;
		// ��ȡ��ĳһ�½ڵ�����
		int fore=list[chapter];
		for(int i=30;i>=0;i-=2)
		{
			int foreStar=(fore>>>i)&3;
			if(foreStar<=0) return false;
		}
		return true;
	}
	
}
