package foxu.sea.alliance;

import foxu.sea.AllianceBossNpcIsland;
import foxu.sea.NpcIsland;
import foxu.sea.fight.FleetGroup;
import foxu.sea.kit.SeaBackKit;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntList;

/** ����boss */
public class AllianceBoss
{

	/** bossս����sid���� */
	public static final int BOSS_SIDS[]={12001,12002,12003,12004,12005,
		12006,12007,12008,12009,12010,12011,12012,12013,12014,12015,12016,
		12017,12018,12019,12020,12021,12022,12023,12024,12025,12026,12027,
		12028,12029,12030,12031,12032};
	/** boss���� 0��ˢ��Ϊ���õ� NpcIsland */
	ArrayList group=new ArrayList();
	/** ��ȡ�������id bossAward 0����� */
	ArrayList bossAward=new ArrayList();
	/** ����ֵ ��һ��ֵΪˢ��ʱ�� int byteһ�� */
	IntList list=new IntList();

	/** ��������ʱ ��ӽ���Ʒ */
	public void addBossAwardJoin(int playerId)
	{
		for(int i=0;i<bossAward.size();i++)
		{
			BossAward award=(BossAward)bossAward.get(i);
			award.addAwardPlayerId(playerId);
		}
	}

	/** ���һ�� */
	public void addBossAward(int playerId,int npcIsLandSid)
	{
		for(int i=0;i<bossAward.size();i++)
		{
			BossAward award=(BossAward)bossAward.get(i);
			if(award.getNpcIslandSid()==npcIsLandSid)
			{
				award.addAwardPlayerId(playerId);
				return;
			}
		}
		BossAward award=new BossAward();
		award.setNpcIslandSid(npcIsLandSid);
		award.addAwardPlayerId(playerId);
		bossAward.add(award);
	}

	/** �鿴ĳ������Ƿ���ȡ��ĳ��boss�Ľ��� */
	public boolean hasBossAward(int playerId,int npcIsLandSid)
	{
		for(int i=0;i<bossAward.size();i++)
		{
			BossAward award=(BossAward)bossAward.get(i);
			if(award.getNpcIslandSid()==npcIsLandSid)
			{
				if(award.checkAwardPlayerId(playerId))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}

	/** ˢ�²��� */
	public void flush()
	{
		group.clear();
		for(int i=0;i<BOSS_SIDS.length;i++)
		{
			AllianceBossNpcIsland island=(AllianceBossNpcIsland)NpcIsland.factory
				.newSample(BOSS_SIDS[i]);
			island.createFleetGroup();
			group.add(island);
		}
		bossAward.clear();
	}

	/** ����Ƿ�ˢ�� */
	public void checkFlush()
	{
		// ��ȡ�ϴ�ˢ��ʱ��
		if(list==null||list.size()<=0)
		{
			// �����ǵڼ���
			int dayTime=SeaBackKit.getDayOfYear();
			list.add(dayTime);
			// ռλbyte
			list.add(0);
			flush();
		}
		else
		{
			int nowTime=SeaBackKit.getDayOfYear();
			int dayTime=list.get(0);
			if(dayTime!=nowTime)
			{
				list.clear();
				list.add(nowTime);
				// ռλbyte
				list.add(0);
				flush();
			}
		}
	}

	/** ��ȡĳһ���صĽ��� */
	public AllianceBossNpcIsland getBossGroup(int sid)
	{
		for(int i=0;i<group.size();i++)
		{
			AllianceBossNpcIsland island=(AllianceBossNpcIsland)group.get(i);
			if(island.getSid()==sid)
			{
				return island;
			}
		}
		return null;
	}

	public void bytesWriteGroup(ByteBuffer data)
	{
		if(group==null||group.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(group.size());
			for(int i=0;i<group.size();i++)
			{
				AllianceBossNpcIsland island=(AllianceBossNpcIsland)group
					.get(i);
				data.writeShort(island.getSid());
				island.getFleetGroup().bytesWrite(data);
			}
		}
	}

	public void bytesWriteBossAward(ByteBuffer data)
	{
		if(bossAward==null||bossAward.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(bossAward.size());
			for(int i=0;i<bossAward.size();i++)
			{
				BossAward award=(BossAward)bossAward.get(i);
				award.bytesWrite(data);
			}
		}
	}

	public void bytesWriteList(ByteBuffer data)
	{
		if(list==null||list.size()<=0)
		{
			data.writeByte(0);
		}
		else
		{
			data.writeByte(list.size());
			for(int i=0;i<list.size();i+=2)
			{
				data.writeInt(list.get(i));
				data.writeByte(list.get(i+1));
			}
		}
	}

	public void bytesReadGroup(ByteBuffer data)
	{
		int size=data.readUnsignedByte();
		group.clear();
		if(size>0)
		{
			for(int i=0;i<size;i++)
			{
				int npcSid=data.readUnsignedShort();
				AllianceBossNpcIsland island=(AllianceBossNpcIsland)NpcIsland.factory
					.newSample(npcSid);
				island.getFleetGroup().bytesRead(data);
				group.add(island);
			}
			if(size<BOSS_SIDS.length)
			{
				for(int i=size;i<BOSS_SIDS.length;i++)
				{
					AllianceBossNpcIsland island=(AllianceBossNpcIsland)NpcIsland.factory
						.newSample(BOSS_SIDS[i]);
					group.add(island);
				}
			}
		}
	}

	public void bytesReadBossAward(ByteBuffer data)
	{
		int size=data.readUnsignedByte();
		bossAward.clear();
		if(size>0)
		{
			for(int i=0;i<size;i++)
			{
				BossAward award=new BossAward();
				award.bytesRead(data);
				bossAward.add(award);
			}
		}
	}

	public void bytesReadList(ByteBuffer data)
	{
		int size=data.readUnsignedByte();
		list.clear();
		if(size>0)
		{
			for(int i=0;i<size;i+=2)
			{
				list.add(data.readInt());
				list.add(data.readUnsignedByte());
			}
		}
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		bytesWriteGroup(data);
		bytesWriteBossAward(data);
		bytesWriteList(data);
	}

	/** ������������л����ֽڻ����� */
	public Object bytesRead(ByteBuffer data)
	{
		bytesReadGroup(data);
		bytesReadBossAward(data);
		bytesReadList(data);
		return this;
	}

	public int getPercent(int npcIslandSid,int nowNum)
	{
		AllianceBossNpcIsland island=(AllianceBossNpcIsland)NpcIsland.factory
			.getSample(npcIslandSid);
		int value=0;
		if(nowNum>0)
		{
			value=nowNum*100/island.getFleetGroup().nowTotalNum();
		}
		return value;
	}

	public void showBytesWrite(ByteBuffer data,int playerId)
	{
		checkFlush();
		int size=group.size();
		// 
		//if(size>BOSS_SIDS.length)
			//size=BOSS_SIDS.length;
		data.writeByte(size);
		for(int i=0;i<size;i++)
		{
			AllianceBossNpcIsland island=(AllianceBossNpcIsland)NpcIsland.factory
				.getSample(BOSS_SIDS[i]);
			FleetGroup fleetGroup=island.getFleetGroup();
			AllianceBossNpcIsland nowGroup=(AllianceBossNpcIsland)group
				.get(i);
			int fleetNum=fleetGroup.nowTotalNum();
			int nowNum=nowGroup.getFleetGroup().nowTotalNum();
			// 1-100ʣ��ٷֱ� 0δ��ȡ����Ʒ -1����ȡ����Ʒ
			int value=0;
			if(nowNum>0)
			{
				value=nowNum*100/fleetNum;
				if(value<=0) value=1;
			}
			if(nowNum<=0)
			{
				value=0;
				if(hasBossAward(playerId,island.getSid()))
				{
					value=-1;
				}
			}
			data.writeByte(value);
		}
	}
}
