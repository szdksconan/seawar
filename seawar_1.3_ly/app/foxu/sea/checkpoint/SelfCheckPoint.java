package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;

/**
 * 自己的关卡状态 author:icetiger
 */
public class SelfCheckPoint
{

	/** 章节的总共长度 */
	public static final int MAX_CHAPTER=11;
	/** 章节关卡 */
	public static final int CHAPTER_1=1,CHAPTER_2=2,CHAPTER_3=3,CHAPTER_4=4,
					CHAPTER_5=5;
	/** 每个章节的关卡数量 */
	public static final int CHAPTER_NUM=16;
	/** 获得的星星个数 */
	public static final int ONE_STAR=1,TOW_STAR=2,THREE_STAR=3;
	/** 关卡数据存储 每个int是一个关卡 */
	int[] list=new int[1];
	/** 当前未通过的关卡sid 默认为10001 配置的第一个关卡 */
	int checkPointSid=10001;
	/** 关卡宝箱领取情况  int该关卡领奖情况*/
	int[] awardRecords = new int[1];

	/** 获取某一章的总星数 */
	public int getChapterStar(int chapter)
	{
		if(chapter>=list.length)
		{
			return 0;
		}
		// 获取到某一章节的数据
		int fore=list[chapter];
		int star=0;
		for(int i=30;i>=0;i-=2)
		{
			int foreStar=(fore>>>i)&3;
			star+=foreStar;
		}
		return star;
	}
	
	/**获取总星星数量*/
	public int getAllstars()
	{
		int allstars = 0;
		for(int i=0;i<list.length;i++)
		{
			allstars+=getChapterStar(i);
		}
		return allstars;
	}

	/** 攻击完一个玩卡 星大于0就通过了 */
	public void addPoint(int star,int nextSid,int chapter,int index)
	{
		if(star<=0) return;
		if(chapter>=list.length)
		{
			int[] temp=new int[chapter+1];
			System.arraycopy(list,0,temp,0,list.length);
			list=temp;
		}
		// 获取到某一章节的数据
		int fore=list[chapter];
		// 获取之前某个index
		int foreStar=(fore>>>index)&3;
		// 当星数比原星数大时才进行记录
		if(star>foreStar)
		{
			// 将原纪录清空
			foreStar=(foreStar<<index);
			fore=fore^foreStar;
			// 将新纪录写进原纪录位置
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

	/** 将对象的域序列化到字节缓存中 */
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
	 * @param list 要设置的 list
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
	 * @param checkPointSid 要设置的 checkPointSid
	 */
	public void setCheckPointSid(int checkPointSid)
	{
		this.checkPointSid=checkPointSid;
	}
	/** 获取某关的星星数 */
	public int getStar(int chapter,int index)
	{
		if(list==null||list.length<=chapter)return 0;
		return (list[chapter]>>index*2)&3;
	}
	
	/**
	 * 检查是否可以领取过关卡宝箱 
	 * @param chapter 章节 0开始
	 * @param index 第几个宝箱 0开始
	 * @return 0可以领取 1未达到条件 2已经领取
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
	
	/** 添加领奖记录 */
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
	/**检测是否通关**/
	public boolean checkChapterStar(int chapter)
	{
		if(chapter>=list.length)
			return false;
		// 获取到某一章节的数据
		int fore=list[chapter];
		for(int i=30;i>=0;i-=2)
		{
			int foreStar=(fore>>>i)&3;
			if(foreStar<=0) return false;
		}
		return true;
	}
	
}
