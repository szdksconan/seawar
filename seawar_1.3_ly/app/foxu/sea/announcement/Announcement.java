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
 * ����
 * 
 * @author lihongji
 */
public class Announcement extends Sample
{

	public static SampleFactory factory=new SampleFactory();

	int id;
	/** �Ƿ�ʱ */
	int permanent;
	/** ���� */
	String title;
	/** ��� */
	String introduction;
	/** ���� */
	String content;
	/** ��ť���� */
	String btnname;
	/** ��ʼʱ�� */
	int startTime;
	/** ����ʱ�� */
	int endTime;
	/** ���潱����sid,num */
	int[] awardSid=null;
	/** �Ѷ���� */
	IntList readannounce=new IntList();
	/** ���������� */
	IntList awardPlayer=new IntList();
	/** ���Կ��������� */
	IntList readplayer=new IntList();
	/** �����콱����� */
	IntList ableawardplayer=new IntList();
	/** ������ */
	Award award;

	/** �����Ƿ��ڽ����� */
	public boolean isOpen(int nowTime)
	{
		return endTime>nowTime&&nowTime>=startTime;
	}
	/**
	 * ��֤�������Ƿ��ȡ�������
	 * 
	 * */
	public boolean isReadAnnounce(int playerId)
	{
		return readannounce.contain(playerId);
	}

	/**
	 * ����һ��read���
	 * 
	 */
	public boolean addReadAnnounce(int playerId)
	{
		return readannounce.add(playerId);
	}

	/**
	 * 
	 * ����һ���콱���
	 */
	public boolean addAwardAnnounce(int playerId)
	{
		return awardPlayer.add(playerId);
	}

	/**
	 * 
	 * ��֤�������Ƿ��������
	 * 
	 * */
	public boolean isTakeAward(int playerId)
	{
		return awardPlayer.contain(playerId);
	}
	/**
	 * 
	 * ��֤�������ǿ��콱��
	 * 
	 * */
	public boolean isAbleAward(int playerId)
	{
		return ableawardplayer.isEmpty()||ableawardplayer.contain(playerId);
	}

	/** ���潱��Ʒ��д�� */
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

	/** ����Ľ���Ʒ�Ķ�ȡ */
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
		/** ��ʼ�������� */
		initAward();
	}
	/** �����л�������Ѷ�������� */
	public Object bytesReadAnnounce(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			readannounce.add(data.readInt());
		}
		return data;
	}
	/** д������Ѷ�������� */
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

	/** �����л��������Ķ���� */
	public Object bytesReadPlayer(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			readplayer.add(data.readInt());
		}
		return data;
	}

	/** д����ҿɶ����� */
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

	/** �����л��������콱��� */
	public Object bytesAbleAwardPlayer(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			ableawardplayer.add(data.readInt());
		}
		return data;
	}

	/** д����ҿɶ����� */
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
	/** �����л�������Ѷ�������ȡ���� */
	public Object bytesReadAwardPlayer(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			awardPlayer.add(data.readInt());
		}
		return data;
	}

	/** �����콱�����д�� */
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
	 * ���л�����
	 * */
	public void showByteWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(startTime);
		data.writeInt(endTime);
		data.writeUTF(title);
		data.writeUTF(introduction);
	}

	/** ���� */
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title=title;
	}
	/** ��� */
	public String getIntroduction()
	{
		return introduction;
	}

	public void setIntroduction(String introduction)
	{
		this.introduction=introduction;
	}
	/** ���� */
	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content=content;
	}
	/** ��ť���� */
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
	/** ����� */
	public void setAwardSid(int[] awardSid)
	{
		this.awardSid=awardSid;
		initAward();
	}
	/** ��ʼʱ�� */
	public int getStartTime()
	{
		return startTime;
	}

	public void setStartTime(int startTime)
	{
		this.startTime=startTime;
	}
	/** ����ʱ�� */
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
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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
	/** ���ֽ������з����л���ö������ */
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

	/** �жϵ�ǰ��player�Ƿ����Ķ���Ȩ�� */
	public boolean isablereadPlayer(int playerId)
	{
		return readplayer.isEmpty()||readplayer.contain(playerId);
	}
	/** ��ʼ�������� */
	public void initAward()
	{
		award=(Award)Award.factory.newSample(ActivityContainer.EMPTY_SID);
		award.setPropSid(getShipSid(awardSid,"prop"));
		award.setShipSids(getShipSid(awardSid,"ship"));
		award.setEquipSids(getShipSid(awardSid,"equ"));
	}
	/*** awardsid����û�д�ֻ */
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