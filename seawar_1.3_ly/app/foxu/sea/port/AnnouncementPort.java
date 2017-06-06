package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.announcement.Announcement;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.shipdata.ShipCheckData;

/**
 * 公告port
 * 
 * @author lihongji
 * 
 */
public class AnnouncementPort extends AccessPort
{

	CreatObjectFactory objectFactory;
	public final static int PAGE_SIZE=10;
	/** READ_ANN=1 阅读内容 GET_ANN_AWAR=2 获取奖励 GET_ALL_ANN=3 获取所有的公告 */
	public final static int READ_ANN=1,GET_ANN_AWAR=2,GET_ALL_ANN=3;
	/** 公告领奖加锁 */
	Object lock=new Object();

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		if(type==READ_ANN)
		{
			int id=data.readInt();
			data.clear();
			Announcement announce=objectFactory.getAnnMemcahe().getById(id);
			if(announce==null)
				throw new DataAccessException(0,"annoucement not exist");
			if(announce.getEndTime()<TimeKit.getSecondTime())
				throw new DataAccessException(0,"annoucement is pass");
			if(!announce.isablereadPlayer(player.getId()))
				throw new DataAccessException(0,"annoucement is erro");
			data.writeUTF(announce.getContent());
			if(!announce.isReadAnnounce(player.getId()))
				announce.addReadAnnounce(player.getId());
			if(announce.getAwardSid()!=null&&announce.getAwardSid().length>0)
			{
				if(announce.isAbleAward(player.getId()))
				{
					data.writeBoolean(true);
					data.writeUTF(announce.getBtnname());
					if(announce.isTakeAward(player.getId()))
						data.writeBoolean(false);
					else
						data.writeBoolean(true);
				}
				else
					data.writeBoolean(false);
			}
			else
				data.writeBoolean(false);
		}
		if(type==GET_ANN_AWAR)
		{
			int id=data.readInt();
			Announcement announce=objectFactory.getAnnMemcahe().getById(id);
			if(announce==null)
				throw new DataAccessException(0,"annoucement not exist");
			data.clear();
			if(!announce.isTakeAward(player.getId())
				&&announce.isAbleAward(player.getId()))
			{
				synchronized(lock)
				{
					announce.addAwardAnnounce(player.getId());
				}
				int[] awardsid=announce.getAwardSid();
				data.writeByte(awardsid.length);
				for(int i=0;i<awardsid.length;i+=2)
				{
					data.writeInt(awardsid[i]);
					data.writeInt(awardsid[i+1]);
				}
				Award award=announce.getAward();
				if(award!=null && award.getShipSids()!=null && award.getShipSids().length>0)
				{
					IntList fightlist=new IntList();
					for(int i=0;i<award.getShipSids().length;i++)
					{
						fightlist.add(award.getShipSids()[i]);
					}
					// 船只日志
					objectFactory.addShipTrack(0,ShipCheckData.ANN_SEND,player,
						fightlist,null,false);
				}
				award.awardLenth(data,player,objectFactory,null,
					new int[]{EquipmentTrack.FROM_ANNOUNCEMENT});
			}
		}
		if(type==GET_ALL_ANN)
		{
			int pageIndex=data.readByte();
			Announcement[] list=objectFactory.getAnnMemcahe()
				.getAllAnnouncement();
			writeData(data,player,pageIndex,list);
		}
		return data;
	}
	/**
	 * 序列化 公告 给前台
	 * */
	public ByteBuffer writeData(ByteBuffer data,Player player,int pageIndex,
		Announcement[] list)
	{
		list=getAnnounceLength(player,list);
		int num=list==null?0:list.length;
		num=num-pageIndex*PAGE_SIZE;// 得到当前公告长度
		setAnnounceSequence(list);//排序
		if(num<0) num=0;
		if(num>PAGE_SIZE) num=PAGE_SIZE;
		data.clear();
		data.writeByte(num);
		if(num>0)
		{
			for(int i=pageIndex*PAGE_SIZE;i<(pageIndex+1)*PAGE_SIZE;i++)
			{
				if(i>=list.length) break;
				Announcement announce=(Announcement)list[i];
				boolean flag=announce.isReadAnnounce(player.getId());
				showBytesWrite(announce,flag,data,player.getId());
			}
		}
		return data;
	}

	/***
	 * 序列化
	 * 
	 * @param announce
	 * @param flag
	 * @param data
	 */
	public void showBytesWrite(Announcement announce,boolean flag,
		ByteBuffer data,int id)
	{
		data.writeInt(announce.getId());// id
		data.writeUTF(announce.getTitle());// 主题
		data.writeUTF(announce.getIntroduction());// 简介
		data.writeInt(announce.getStartTime());// 开始时间
		data.writeInt(announce.getEndTime());// 结束时间
		if(flag)
		{
			if(announce.isAbleAward(id)&&!announce.isTakeAward(id) && announce.getBtnname()!=null)
				data.writeBoolean(false);
			else
				data.writeBoolean(true);
		}
		else
			data.writeBoolean(false);
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	/** 得到公告的长度 */
	public Announcement[] getAnnounceLength(Player player,
		Announcement[] announces)
	{
		if(announces.length<=0) return null;
		Announcement[] temp=new Announcement[0];
		for(int i=0;i<announces.length;i++)
		{
			Announcement announce=announces[i];
			if(announce==null) continue;
			if(!announce.isablereadPlayer(player.getId())) continue;
			if(announce.getStartTime()>TimeKit.getSecondTime()) continue;
			Announcement[] anntemp=new Announcement[temp.length+1];
			System.arraycopy(temp,0,anntemp,0,temp.length);
			temp=anntemp;
			temp[temp.length-1]=announce;
		}
		announces=temp;
		return announces;
	}

	/** 将公告排序 */
	public void setAnnounceSequence(Announcement[] announces)
	{
		if(announces==null) return;
		if(announces.length<=1) return;
		Announcement ann=null;
		for(int i=0;i<announces.length;i++)
		{
			for(int j=0;j<announces.length-1;j++)
			{
				if(announces[j].getStartTime()<announces[j+1].getStartTime())
				{
					ann=announces[j+1];
					announces[j+1]=announces[j];
					announces[j]=ann;
				}
			}
		}
	}
}
