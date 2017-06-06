package foxu.sea.alliance.chest;

import mustang.field.Fields;
import mustang.io.ByteBuffer;
import mustang.set.SetKit;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.AllianceDBAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.AllianceSave;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;

/**
 * ���˱��������
 */

public class AllianceChestManager
{

	/** �������˻������а� */
	RankInfo[] rank;
	/** ���ݻ�ȡ�� */
	CreatObjectFactory objectFactory;
	/** ���а�Ƚ��� */
	AllianceChestComparator comparator=new AllianceChestComparator();

	private static AllianceChestManager allianceChestManager;

	private AllianceChestManager()
	{
	};

	public static AllianceChestManager getInstance()
	{
		if(allianceChestManager==null)
		{
			allianceChestManager=new AllianceChestManager();
		}
		return allianceChestManager;
	}

	public void init()
	{
		rank=new RankInfo[11];
		String sql="SELECT id,name,luckyPoints,luckyCreateAt FROM alliances ORDER BY luckyPoints DESC LIMIT 10";
		Fields fields[]=((AllianceDBAccess)objectFactory
			.getAllianceMemCache().getDbaccess()).loadSqls(sql);
		if(fields==null||fields.length<=0) return;
		for(int i=0;i<fields.length;i++)
		{
			RankInfo r=new RankInfo(Integer.parseInt(fields[i].getArray()[0]
				.getValue().toString()),fields[i].getArray()[1].getValue()
				.toString(),Integer.parseInt(fields[i].getArray()[2]
				.getValue().toString()),(i+1),Integer.parseInt(fields[i].getArray()[3]
				.getValue().toString()));
			rank[i]=r;
		}
	}

	public void dismissAlliance(Alliance alliance)
	{
		synchronized(rank)
		{
			for(int i=0;i<rank.length;i++)
			{
				// �����ڰ�������а�����
				if(rank[i]!=null&&rank[i].getId()==alliance.getId())
				{
					rank[i]=null;
					break;
				}
			}
			flushRank();
		}
	}
	
	/** ���а� */
	public void showBytesWrite(ByteBuffer data,int allianceId,
		AllianceChest ac)
	{
		// ����ǰ10���б�������Ϣ
		RankInfo myAlliance=getMyAlliance(allianceId);
		if(myAlliance!=null)
		{
			data.writeBoolean(true);
			data.writeShort(myAlliance.getRankNum()); // ����
			data.writeUTF(myAlliance.getName()); // ������
			data.writeInt(myAlliance.getLuckyPoint()); // ���˻���
			// 0--������ȡ��1-������ȡ��2-������ȡ
			if(SeaBackKit.getDayOfWeek(TimeKit.getMillisTime())==0)
			{
				int luckyCount=ac.getLuckyCount();
				if(luckyCount==1)
					data.writeByte(1);
				else
					data.writeByte(2);
			}
			else
				data.writeByte(0);
		}
		else
		{
			data.writeBoolean(false);
		}
		int length=0;
		for(int i=0;i<10;i++)
		{
			if(rank[i]!=null) length++;
		}
		data.writeShort(length); // ���г��� ���ǰ10��
		for(int i=0;i<length;i++)
		{
			RankInfo r=rank[i];
			// data.writeShort(r.getRankNum()); // ����
			data.writeUTF(r.getName()); // ������
			data.writeInt(r.getLuckyPoint()); // ���˻���
			if(r.getId()==allianceId) // �Ƿ������
				data.writeByte(1);
			else
				data.writeByte(0);
		}
	}

	/** ���佱�� */
	public void showBytesWriteChestAwards(ByteBuffer data,Player player)
	{
		int[] chestAward=PublicConst.ALLIANCE_CHEST_AWARD;
		data.writeByte(chestAward.length); // ����
		for(int i=0;i<chestAward.length;i++)
		{
			data.writeByte(i); // ���伶�� 0��ʼ
			Award aw=(Award)Award.factory.getSample(chestAward[i]);
			aw.viewAward(data,player); // ��Ʒչʾ
		}
	}

	/** ���˻��ֽ��� */
	public void showBytesWriteLuckyPointAwards(ByteBuffer data,Player player)
	{
		int[] luckyPointAward=PublicConst.ALLIANCE_LUCKY_POINT_AWARD;
		String[] placing=PublicConst.ALLIANCE_LUCKY_POINT_PLACING;
		data.writeByte(luckyPointAward.length); // ���ֱ��䳤��
		for(int i=0;i<luckyPointAward.length;i++)
		{
			data.writeShort(0); // theRewardType �̶�0
			String str=placing[i];
			if(str.indexOf("-")>-1)
			{
				String[] strArr=str.split("-");
				data.writeInt(Integer.parseInt(strArr[0])); // ���� ��ʼ
				data.writeInt(Integer.parseInt(strArr[1])); // ���� ����
			}
			else
			{
				data.writeInt(Integer.parseInt(str));
				data.writeInt(Integer.parseInt(str));
			}
			data.writeInt(0); // theScoreRequired �̶�0
			data.writeByte(0); // theStatus �̶�0
			Award aw=(Award)Award.factory.getSample(luckyPointAward[i]);
			aw.viewAward(data,player); // ��Ʒչʾ
		}
	}

	/** ˢ������ */
	public synchronized RankInfo[] flushRank()
	{
		SetKit.sort(rank,comparator,true);
		for(int i=0;i<10;i++)
		{
			RankInfo r=rank[i];
			if(r!=null) r.setRankNum(i+1);
		}
		return rank;
	}

	public RankInfo[] flushRank(Alliance alliance)
	{
		// ������˷���С�ڵ�11 ����������
		if(rank[10]!=null)
		{
			RankInfo ri=rank[10];
			if(alliance.getLuckyPoints()<=ri.getLuckyPoint()) return rank;
		}
		for(int i=0;i<10;i++)
		{
			RankInfo r=rank[i];
			if(r==null) continue;
			// ���������ǰ10��ֱ�����򷵻�
			if(r.getId()==alliance.getId())
			{
				r.setLuckyPoint(alliance.getLuckyPoints());
				r.setLuckyCreateAt(alliance.getLuckyCreateAt());
				return flushRank();
			}
		}
		rank[10]=new RankInfo(alliance.getId(),alliance.getName(),
			alliance.getLuckyPoints(),11,alliance.getLuckyCreateAt());
		return flushRank();
	}

	/** �������а� */
	public synchronized void resetRank()
	{
		for(int i=0;i<rank.length;i++)
		{
			RankInfo r=rank[i];
			if(r==null) continue;
			r.setLuckyPoint(0);
		}
	}

	/** �����������˵��� */
	public synchronized void resetLuckyPoint()
	{
		Object[] objs=objectFactory.getAllianceMemCache().getCacheMap()
			.valueArray();
		if(objs==null||objs.length==0) return;
		for(int i=0;i<objs.length;i++)
		{
			if(objs[i]==null) continue;
			AllianceSave save=((AllianceSave)objs[i]);
			if(save==null) continue;
			Alliance alliance=save.getData();
			if(alliance==null) continue;
			alliance.setLuckyPoints(0);
			alliance.setLuckyCreateAt(0);
		}
	}

	/** ����������˵��콱���� */
	public synchronized void resetPlayerLucyAwardCount()
	{
		Object[] objs=objectFactory.getPlayerCache().getCacheMap()
			.valueArray();
		if(objs==null||objs.length==0) return;
		for(int i=0;i<objs.length;i++)
		{
			if(objs[i]==null) continue;
			PlayerSave save=((PlayerSave)objs[i]);
			if(save==null) continue;
			Player player=save.getData();
			if(player==null) continue;
			AllianceChest ac=player.getAllianceChest();
			checkAndSendChestAward(player,ac);
			ac.setLuckyCount(1);
			player.setAllianceChest(ac);
		}
	}

	/** ���û����ȡ���Զ����� */
	public void checkAndSendChestAward(Player player,AllianceChest ac)
	{
		String aidStr=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(aidStr==null&&"".equals(aidStr)) return;
		int aid=TextKit.parseInt(aidStr);
		if(checkChestAward(player,ac)!=null)return;
		sendChestAward(player,ac,aid);
	}
	
	public String checkChestAward(Player player,AllianceChest ac)
	{
		int joinTime=0;
		// 24Сʱ�����˲����콱
		String joinStr=player.getAttributes(PublicConst.ALLIANCE_JOIN_TIME);
		if(joinStr!=null&&!"".equals(joinStr))
			joinTime=Integer.parseInt(joinStr);
		if(joinTime>(TimeKit.getSecondTime()-24*3600))
			return "join time need greater than 24h";
		int luckyCount=ac.getLuckyCount();
		if(luckyCount==0) return "you hava already get the reward";
		return null;
	}
	
	public void sendChestAward(Player player,AllianceChest ac,int aid)
	{
		RankInfo rank=getMyAlliance(aid);
		if(rank==null) return;
		int awardLevel=getRankAwardLv(aid);
		Award aw=(Award)Award.factory
			.newSample(PublicConst.ALLIANCE_LUCKY_POINT_AWARD[awardLevel]);
		if(aw==null) return;
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"warfareAllianceIntegral_autoSendReward_title");
		String content=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"warfareAllianceIntegral_autoSendReward_mail");
		content=TextKit.replace(content,"%",rank.getRankNum()+"");
		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),Message.SYSTEM_ONE_TYPE,title,
			true,aw);
		// ˢ��ǰ̨
		JBackKit.sendRevicePlayerMessage(player,message,
			message.getRecive_state(),objectFactory);
	}
	
	/** ������ǰ10������ҵ����� �����ڷ���null */
	public RankInfo getMyAlliance(int allianceId)
	{
		RankInfo myAlliance=null;
		for(int i=0;i<10;i++)
		{
			if(rank[i]==null) continue;
			RankInfo r=rank[i];
			if(allianceId==r.getId())
			{
				myAlliance=r;
				return myAlliance;
			}
		}
		return myAlliance;
	}

	public int getRankAwardLv(int allianceId)
	{
		RankInfo r=getMyAlliance(allianceId);
		if(r!=null)
		{
			int num=r.getRankNum();
			String[] placing=PublicConst.ALLIANCE_LUCKY_POINT_PLACING;
			for(int i=0;i<placing.length;i++)
			{
				String str=placing[i];
				if(str.indexOf("-")>-1)
				{
					String[] strArr=str.split("-");
					if(num>=Integer.parseInt(strArr[0])
						&&num<=Integer.parseInt(strArr[1])) return i;
				}
				else
				{
					if(num==Integer.parseInt(str)) return i;
				}
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unused")
	public boolean isLuckyAward(ByteBuffer data)
	{
		ByteBuffer bb=(ByteBuffer)data.clone();
		byte l=bb.readByte();
		byte l2=bb.readByte();
		byte type=bb.readByte();
		int l3=bb.readInt();
		int sid=bb.readUnsignedShort();
		return sid==PublicConst.ALLIANCE_CHEST_SID;
	}

	/** �����ұ���״̬ */
	public void checkPlayerChest(Player player)
	{
		AllianceChest ac=player.getAllianceChest();
		if(ac==null)
			ac=new AllianceChest();
		int lastDay=ac.getLastDay();
		int nowDay=SeaBackKit.getDayOfYear();
		if(lastDay!=nowDay)
		{
			int freeCount=1;
			int count=0;
			lastDay=nowDay;
			int nowLv=-1;
			ac.setFreeCount(freeCount);
			ac.setCount(count);
			ac.setLastDay(lastDay);
			ac.setGiveLv(nowLv);
		}
	}
	
	public void resetAllianceName(String oldName,String name)
	{
		if(name==null||oldName==null)
			return;
		RankInfo myAlliance=null;
		for(int i=0;i<10;i++)
		{
			if(rank[i]==null) continue;
			RankInfo r=rank[i];
			if(oldName.equals(r.getName()))
			{
				myAlliance=r;
				break;
			}
		}
		if(myAlliance!=null)
			myAlliance.setName(name);
	}
	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	/**
	 * ���˱���������
	 */
	public class RankInfo
	{

		/** ����id */
		int id;
		/** �������� */
		String name;
		/** ���˻��� */
		int luckyPoint;
		/** ���� */
		int rankNum;
		/**ʱ��**/
		int luckyCreateAt;
		
		public RankInfo(int id,String name,int luckyPoint,int rankNum,int luckyCreateAt)
		{
			this.id=id;
			this.name=name;
			this.luckyPoint=luckyPoint;
			this.rankNum=rankNum;
			this.luckyCreateAt=luckyCreateAt;
		}

		public int getId()
		{
			return id;
		}

		public void setId(int id)
		{
			this.id=id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name=name;
		}

		public int getLuckyPoint()
		{
			return luckyPoint;
		}

		public void setLuckyPoint(int luckyPoint)
		{
			this.luckyPoint=luckyPoint;
		}

		public int getRankNum()
		{
			return rankNum;
		}

		public void setRankNum(int rankNum)
		{
			this.rankNum=rankNum;
		}

		
		public int getLuckyCreateAt()
		{
			return luckyCreateAt;
		}

		
		public void setLuckyCreateAt(int luckyCreateAt)
		{
			this.luckyCreateAt=luckyCreateAt;
		}

		
	}

}
