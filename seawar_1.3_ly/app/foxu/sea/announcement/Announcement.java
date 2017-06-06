package foxu.sea.announcement;

import foxu.sea.Ship;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.award.Award;
import foxu.sea.equipment.Equipment;
import foxu.sea.proplist.Prop;
import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * 公告
 * 
 * @author lihongji
 */
public class Announcement extends Sample
{

	public static SampleFactory factory=new SampleFactory();

	int id;
	/** 是否定时 */
	int permanent;
	/** 标题 */
	String title;
	/** 简介 */
	String introduction;
	/** 内容 */
	String content;
	/** 按钮名称 */
	String btnname;
	/** 开始时间 */
	int startTime;
	/** 结束时间 */
	int endTime;
	/** 公告奖励的sid,num */
	int[] awardSid=null;
	/** 已读玩家 */
	IntList readannounce=new IntList();
	/** 领过奖的玩家 */
	IntList awardPlayer=new IntList();
	/** 可以看公告的玩家 */
	IntList readplayer=new IntList();
	/** 可以领奖的玩家 */
	IntList ableawardplayer=new IntList();
	/** 奖励包 */
	Award award;

	/** 公告是否在进行中 */
	public boolean isOpen(int nowTime)
	{
		return endTime>nowTime&&nowTime>=startTime;
	}
	/**
	 * 验证这个玩家是否读取这个公告
	 * 
	 * */
	public boolean isReadAnnounce(int playerId)
	{
		return readannounce.contain(playerId);
	}

	/**
	 * 增加一个read玩家
	 * 
	 */
	public boolean addReadAnnounce(int playerId)
	{
		return readannounce.add(playerId);
	}

	/**
	 * 
	 * 增加一个领奖玩家
	 */
	public boolean addAwardAnnounce(int playerId)
	{
		return awardPlayer.add(playerId);
	}

	/**
	 * 
	 * 验证这个玩家是否领过奖励
	 * 
	 * */
	public boolean isTakeAward(int playerId)
	{
		return awardPlayer.contain(playerId);
	}
	/**
	 * 
	 * 验证这个玩家是可领奖励
	 * 
	 * */
	public boolean isAbleAward(int playerId)
	{
		return ableawardplayer.isEmpty()||ableawardplayer.contain(playerId);
	}

	/** 公告奖励品的写入 */
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		if(awardSid==null)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(awardSid.length);
			for(int i=0;i<awardSid.length;i++)
			{
				data.writeInt(awardSid[i]);
			}
		}
		return data;
	}

	/** 公告的奖励品的读取 */
	public void initData(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		if(len>0)
		{
			awardSid=new int[len];
			for(int i=0;i<len;i++)
			{
				awardSid[i]=data.readInt();
			}
		}
		/** 初始化奖励包 */
		initAward();
	}
	/** 反序列化出玩家已读公告对象 */
	public Object bytesReadAnnounce(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			readannounce.add(data.readInt());
		}
		return data;
	}
	/** 写入玩家已读公告对象 */
	public void bytesWriteAnnounce(ByteBuffer data)
	{
		if(readannounce!=null&&readannounce.size()>0)
		{
			data.writeInt(readannounce.size());
			for(int i=0;i<readannounce.size();i++)
			{
				data.writeInt((Integer)readannounce.get(i));
			}
		}
		else
			data.writeInt(0);
	}

	/** 反序列化出可以阅读玩家 */
	public Object bytesReadPlayer(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			readplayer.add(data.readInt());
		}
		return data;
	}

	/** 写入玩家可读对象 */
	public void bytesWritereadPlayer(ByteBuffer data)
	{
		if(readplayer!=null&&readplayer.size()>0)
		{
			data.writeInt(readplayer.size());
			for(int i=0;i<readplayer.size();i++)
			{
				data.writeInt((Integer)readplayer.get(i));
			}
		}
		else
			data.writeInt(0);
	}

	/** 反序列化出可以领奖玩家 */
	public Object bytesAbleAwardPlayer(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			ableawardplayer.add(data.readInt());
		}
		return data;
	}

	/** 写入玩家可读对象 */
	public void bytesWriteableAwardPlayer(ByteBuffer data)
	{
		if(ableawardplayer!=null&&ableawardplayer.size()>0)
		{
			data.writeInt(ableawardplayer.size());
			for(int i=0;i<ableawardplayer.size();i++)
			{
				data.writeInt((Integer)ableawardplayer.get(i));
			}
		}
		else
			data.writeInt(0);
	}
	/** 反序列化出玩家已读公告领取对象 */
	public Object bytesReadAwardPlayer(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			awardPlayer.add(data.readInt());
		}
		return data;
	}

	/** 所有领奖的玩家写入 */
	public void bytesWriteAwardPlayer(ByteBuffer data)
	{
		if(awardPlayer!=null&&awardPlayer.size()>0)
		{
			data.writeInt(awardPlayer.size());
			for(int i=0;i<awardPlayer.size();i++)
			{
				data.writeInt((Integer)awardPlayer.get(i));
			}
		}
		else
			data.writeInt(0);
	}
	/**
	 * 序列化公告
	 * */
	public void showByteWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(startTime);
		data.writeInt(endTime);
		data.writeUTF(title);
		data.writeUTF(introduction);
	}

	/** 标题 */
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title=title;
	}
	/** 简介 */
	public String getIntroduction()
	{
		return introduction;
	}

	public void setIntroduction(String introduction)
	{
		this.introduction=introduction;
	}
	/** 内容 */
	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content=content;
	}
	/** 按钮名称 */
	public String getBtnname()
	{
		return btnname;
	}

	public void setBtnname(String btnname)
	{
		this.btnname=btnname;
	}
	public int[] getAwardSid()
	{
		return awardSid;
	}
	/** 活动奖励 */
	public void setAwardSid(int[] awardSid)
	{
		this.awardSid=awardSid;
		initAward();
	}
	/** 开始时间 */
	public int getStartTime()
	{
		return startTime;
	}

	public void setStartTime(int startTime)
	{
		this.startTime=startTime;
	}
	/** 结束时间 */
	public int getEndTime()
	{
		return endTime;
	}
	public void setEndTime(int endTime)
	{
		this.endTime=endTime;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}

	public IntList getReadplayer()
	{
		return readplayer;
	}

	public void setReadplayer(IntList readplayer)
	{
		this.readplayer=readplayer;
	}

	public IntList getAbleawardplayer()
	{
		return ableawardplayer;
	}

	public void setAbleawardplayer(IntList ableawardplayer)
	{
		this.ableawardplayer=ableawardplayer;
	}

	public Award getAward()
	{
		return award;
	}

	public void setAward(Award award)
	{
		this.award=award;
	}

	public int getPermanent()
	{
		return permanent;
	}

	public void setPermanent(int permanent)
	{
		this.permanent=permanent;
	}
	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(permanent);
		data.writeInt(startTime);
		data.writeInt(endTime);
		data.writeUTF(title);
		data.writeUTF(introduction);
		data.writeUTF(content);
		if(btnname!=null&&btnname.length()>0)
		{
			data.writeByte(awardSid.length);
			data.writeUTF(btnname);
			for(int i=0;i<awardSid.length;i++)
			{
				data.writeInt(awardSid[i]);
			}
		}
		else
			data.writeByte(0);
		bytesWriteAwardPlayer(data);
		bytesWriteAnnounce(data);
		bytesWritereadPlayer(data);
		bytesWriteableAwardPlayer(data);
	}
	/** 从字节数组中反序列化获得对象的域 */
	public Object bytesRead(ByteBuffer data)
	{
		id=data.readInt();
		permanent=data.readInt();
		startTime=data.readInt();
		endTime=data.readInt();
		title=data.readUTF();
		introduction=data.readUTF();
		content=data.readUTF();
		int len=data.readByte();
		if(len>0)
		{
			btnname=data.readUTF();
			awardSid=new int[len];
			for(int i=0;i<len;i++)
			{
				awardSid[i]=data.readInt();
			}
		}
		bytesReadAwardPlayer(data);
		bytesReadAnnounce(data);
		bytesReadPlayer(data);
		bytesAbleAwardPlayer(data);
		return data;
	}

	/** 判断当前的player是否有阅读的权限 */
	public boolean isablereadPlayer(int playerId)
	{
		return readplayer.isEmpty()||readplayer.contain(playerId);
	}
	/** 初始化奖励包 */
	public void initAward()
	{
		award=(Award)Award.factory.newSample(ActivityContainer.EMPTY_SID);
		award.setPropSid(getShipSid(awardSid,"prop"));
		award.setShipSids(getShipSid(awardSid,"ship"));
		award.setEquipSids(getShipSid(awardSid,"equ"));
	}
	/*** awardsid中有没有船只 */
	public int[] getShipSid(int[] awardSid,String flag)
	{
		IntList list=new IntList();
		if(awardSid==null)   return list.toArray();
		SampleFactory factory=Equipment.factory;
		if(flag=="prop")
		{
			factory=Prop.factory;
		}
		else if(flag=="ship")
		{
			factory=Ship.factory;
		}
		for(int i=0;i<awardSid.length;i+=2)
		{
			if(factory.getSample(awardSid[i])!=null)
			{
				list.add(awardSid[i]);
				list.add(awardSid[i+1]);
			}
			else if(awardSid[i]>Award.EQUIP_STUFF_START_SID && flag=="equ")
			{
				list.add(awardSid[i]);
				list.add(awardSid[i+1]);
			}
		}
		return list.toArray();
	}
}