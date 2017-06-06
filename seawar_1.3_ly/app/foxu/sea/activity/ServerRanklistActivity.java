package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.RankManager;
import foxu.sea.RankManager.RankInfo;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;

/**
 * ���а�
 * 
 * @author Alan
 */
public abstract class ServerRanklistActivity extends RankAwardActivity
{

	int lastTime;
	String[] awardSids;

	@Override
	public void activityCollate(int time,CreatObjectFactory factoty)
	{
		if(isTimeToSettleDown(time))
		{
			if(!isSettleDown) settleDown(factoty);
			if(!isOpen(time))
			{
				sendAwardBeforeShut(factoty);
			}
		}
		else
			resetRankList(recordList,time,factoty,showRank);
	}

	@Override
	public void initDataByString(String initData,CreatObjectFactory factoty)
	{
		String[] infos=TextKit.split(initData,";");
		awardTime=SeaBackKit.parseFormatTime(infos[0]);
		awardSids=TextKit.split(infos[1],"|");
		initAward();
	}

	public void initAward()
	{
		rankAwardList.clear();
		for(int i=0;i<awardSids.length;i++)
		{
			String[] awards=TextKit.split(awardSids[i],":");
			// ���η�Χ
			int[] range=TextKit.parseIntArray(TextKit.split(awards[0],"-"));
			// ����Ʒ��Ϣ
			int[] sids=TextKit.parseIntArray(TextKit.split(awards[1],","));
			RankAward rankAward=new RankAward();
			// ȡ������Ϊ�����ж�����
			rankAward.rank=range[1];
			rankAward.award=(Award)Award.factory.newSample(ActivityContainer.EMPTY_SID);
			// ��һ��λ�÷ű�ʯ����
			rankAward.award.setGemsAward(sids[0]);
			int[] awardProps=new int[sids.length-1];
			System.arraycopy(sids,1,awardProps,0,sids.length-1);
			rankAward.props=awardProps;
			SeaBackKit.resetAward(rankAward.award,awardProps);
			rankAwardList.add(rankAward);
		}
	}
	
	@Override
	public void settleDown(CreatObjectFactory factoty)
	{
		synchronized(recordList)
		{
			collateRank2List(recordList,factoty,showRank,true);
			isSettleDown=true;
		}
	}

	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"");
		for(int i=0;i<awardSids.length;i++)
		{
			sb.append(awardSids[i]).append("|");
		}
		sb.append("\",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append("}");
		return sb.toString();
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(0);
		data.writeInt(getEndTime()-TimeKit.getSecondTime());
		// �����ʱ��
		data.writeInt(getAwardTime()-TimeKit.getSecondTime());
	}

	@Override
	public void sendFlush(SessionMap smap)
	{

	}

	/** isTieSocre=�Ƿ���в��д��� */
	public void resetRank(CreatObjectFactory factoty,int len,
		IntKeyHashMap record,boolean isTieSocre)
	{
		// ��ǰ���
		Player player=null;
		// ��ʱ�÷�������
		Player target=null;
		int[] playerIds=factoty.getPlayerCache().getCacheMap().keyArray();
		// �Ƿ��״�ѭ��(���ѭ�����ڱ����״θ�ֵ��һЩ����)
		boolean isFirstTime=true;
		// ��һ����ߵ÷�
		long lastScore=0;
		// �ܼ�¼��
		int recordSize=0;
		record.clear();
		while(playerIds.length>recordSize)
		{
			target=null;
			// ��ʱ��ߵ÷�
			long rankScore=0;
			// ��ǰ����
			int ranking=record.size();
			// ��ȡ��һ�������б�(���������߷�����ͬ��ֱ�����,������ʵ����һ���б�)
			IntKeyHashMap list=(IntKeyHashMap)record.get(ranking);
			for(int j=0;j<playerIds.length;j++)
			{
				player=factoty.getPlayerById(playerIds[j]);
				if(player==null) continue;
				if(!SeaBackKit
					.isNewPlayerChangeName(player.getId(),factoty))
					continue;
				long playerScore=getScore(player);
				if(rankScore==0)
				{
					// ��һ��ֱ�Ӹ�ֵor��һ�ֵ÷ִ��ڵ��ڵ�ǰ��ҵ÷�ʱ
					if(isFirstTime||lastScore>=playerScore)
					{
						// �жϵ�ǰ����Ƿ��Ѿ���¼����
						if(!isFirstTime&&lastScore==playerScore&&list!=null
							&&list.get(player.getId())!=null) continue;
						rankScore=playerScore;
						target=player;
					}
					continue;
				}
				if(playerScore<rankScore) continue;
				// ���С����һ����ߵ÷ֲ��Ҹ�����ʱ��ߵ÷�,��ʱ��������
				// ����,���������һ����ߵ÷ֲ�����һ���޼�¼,��������ʵ�������б�,��ʱ��������
				if((((isFirstTime||lastScore>playerScore)&&rankScore<playerScore))
					||(list!=null&&lastScore==playerScore&&list.get(player
						.getId())==null))
				{
					rankScore=playerScore;
					target=player;
					// ������ֱ����������Ч������һ����ͬ,�������������
					if(lastScore==playerScore) break;
				}
			}
			// �Ҳ��������Ŀ�����,����ǰ�÷��������,����Ҫ��������
			if(target==null) break;
			// ������ֱ����������Ч������һ�ֲ�ͬ,������ʵ����һ���б�
			// Ҳ���·���������ҵ�ĳ���÷ֶ�Ϊ0,��ôҲӦ��Ҫ�Ž�����
			if(lastScore!=rankScore||(isFirstTime&&rankScore==0))
			{
				// �����ʱ�ҵ��ļ�¼���Ѿ����ϳ���,����Ҫ��������
				if(recordSize>=len) break;
				list=new IntKeyHashMap();
				ranking++;
			}
			lastScore=rankScore;
			RankInfo rank=new RankInfo(target.getName(),target.getLevel(),
				rankScore);
			rank.setPlayerId(target.getId());
			list.put(target.getId(),rank);
			recordSize++;
			record.put(ranking,list);
			if(isFirstTime) isFirstTime=false;
			if(!isTieSocre&&recordSize>=len)
				break;
		}
	}

	@Override
	public void resetRankList(ArrayList list,int time,
		CreatObjectFactory factoty,int len)
	{
		//�������2Сʱ����������������ᵼ�����а�Ϊ��
		if(list.size()>0&&(isSettleDown||time<lastTime+FLUSH_TIME)) return;
		collateRank2List(list,factoty,len,false);
		if(!isSettleDown&&list!=recordList)
		{
			recordList.clear();
			int listLen=showRank;
			if(list.size()<showRank)
				listLen=list.size();
			for(int i=0;i<listLen;i++)
			{
				recordList.add(list.get(i));
			}
		}
		lastTime=time;
	}

	/** isTieSocre=�Ƿ���в��д��� */
	public void collateRank2List(ArrayList list,CreatObjectFactory factoty,
		int len,boolean isTieSocre)
	{
		IntKeyHashMap record=new IntKeyHashMap();
		resetRank(factoty,len,record,isTieSocre);
		getRankFromMap(list,record,len);
	}

	@Override
	public ArrayList getAwardRank()
	{
		return recordList;
	}

	public void getRankFromMap(ArrayList list,IntKeyHashMap record,int len)
	{
		int size=record.size();
		list.clear();
		for(int i=1;i<=size;i++)
		{
			IntKeyHashMap rankMap=(IntKeyHashMap)record.get(i);
			int[] value=rankMap.keyArray();
			// ����playerid����,ģ�����ݿ�����
			int temp=0;
			for(int j=0;j<value.length;j++)
			{
				for(int k=j;k<value.length;k++)
				{
					long dataA=value[j];
					long dataB=value[k];
					if(dataA>dataB)
					{
						temp=value[j];
						value[j]=value[k];
						value[k]=temp;
					}
				}
			}
			for(int j=0;j<value.length;j++)
			{
				Object obj=rankMap.get(value[j]);
				list.add(obj);
			}
			if(list.size()>=len) break;
		}
	}

	@Override
	public Award getPlayerRankAward(Player player)
	{
		int rank=0;
		long lastScore=0;
		for(int i=0;i<recordList.size();i++)
		{
			RankManager.RankInfo info=(RankManager.RankInfo)recordList
				.get(i);
			// �����һ����¼�÷ֺͱ��μ�¼��һ������ô��������
			if(lastScore>info.getRankInfo()) rank=i+1;
			if(info.getPlayerId()==player.getId())
			{
				RankAward award=getAwardByRanking(rank);
				if(award!=null) return award.award;
				return null;
			}
			lastScore=info.getRankInfo();
		}
		return null;
	}

	@Override
	public boolean isRankScoreLong()
	{
		return false;
	}

	@Override
	public long[] getSelfRank(Player player)
	{
		ArrayList playerIdsFight=recordList;
		int ranking=1;
		int i=0;
		long lastScore=0;
		//��ǰ��ͬ�����ۼ�
		int tieScore=0;
		for(;i<playerIdsFight.size();i++)
		{
			RankInfo info=(RankInfo)playerIdsFight.get(i);
			// ��һ���÷ֱȵ�ǰ����������
			if(lastScore>info.getRankInfo())
			{
				// �·����Σ���������
				tieScore=0;
				ranking=i+1;
			}
			lastScore=info.getRankInfo();
			// �ҵ���ǰ���
			if(player.getId()==info.getPlayerId())
			{
				// ����δ����,ģ�⴫ͳ���й���
				if(!isAwardAvailable()) ranking=(i+1)-tieScore;
				break;
			}
			tieScore++;
		}
		// ��������ڰ���,ȡ���ݿ�����
		if(i>=playerIdsFight.size())
		{
			RankManager.getInstance().flushFileds();
			RankManager.getInstance().flushSelfRank(player);
			ranking=getRank(player);
		}
		// �Լ���ս��������
		return new long[]{ranking,getScore(player)};
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		super.initData(data,factoty,active);
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			int pid=data.readInt();
			int lv=data.readUnsignedByte();
			String name=data.readUTF();
			int score=data.readInt();
			RankInfo info=new RankInfo(name,lv,score);
			info.setPlayerId(pid);
			recordList.add(info);
		}
		len=data.readUnsignedByte();
		awardSids=new String[len];
		for(int i=0;i<len;i++)
		{
			awardSids[i]=data.readUTF();
		}
		initAward();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=super.getInitData();
		data.writeInt(recordList.size());
		for(int i=0;i<recordList.size();i++)
		{
			RankInfo info=(RankInfo)recordList.get(i);
			data.writeInt(info.getPlayerId());
			data.writeByte(info.getPlayerLevel());
			data.writeUTF(info.getPlayerName());
			data.writeInt((int)info.getRankInfo());
		}
		data.writeByte(awardSids.length);
		for(int i=0;i<awardSids.length;i++)
		{
			data.writeUTF(awardSids[i]);
		}
		return data;
	}
	
	/** ����ر�ǰ���ͽ��� */
	public void sendAwardBeforeShut(CreatObjectFactory factoty)
	{
		int rank=1;
		long lastScore=0;
		RankManager.RankInfo info=null;
		Player player=null;
		RankAward award=null;
		for(int i=0;i<recordList.size();i++)
		{
			info=(RankManager.RankInfo)recordList
				.get(i);
			if(awardRecord.contain(info.getPlayerId())) continue;
			player=factoty.getPlayerCache().load(
				info.getPlayerId()+"");
			if(player==null ) continue;
			addAwardRecord(player);
			// �����һ����¼�÷ֺͱ��μ�¼��һ������ô��������
			if(lastScore>info.getRankInfo()) rank=i+1;
			award=getAwardByRanking(rank);
			if(award==null ) continue;
			award.award.awardLenth(new ByteBuffer(),player,
						factoty,null,
						new int[]{EquipmentTrack.FROM_RANK_AWARD});
			lastScore=info.getRankInfo();
			sendSystemMail(player,factoty,rank,award);
		}

	}
	
	public void sendSystemMail(Player player,CreatObjectFactory factoty,int ranking,RankAward award)
	{

		String title=InterTransltor.getInstance().getTransByKey(player.getLocale(),
			"system_mail");
		String content=InterTransltor.getInstance()
			.getTransByKey(player.getLocale(),
				"RankAwardActivity_autoSendReward_mail");
		content=TextKit.replace(content,"%",SeaBackKit.formatDataTime(startTime));
		content=TextKit.replace(content,"%",SeaBackKit.formatDataTime(endTime));
		content=TextKit.replace(content,"%",getActivityName(player));
		content=TextKit.replace(content,"%",
			String.valueOf(ranking));
		content+=awardToString(player,award);
		MessageKit.sendSystemMessages(player,factoty,content,
			title);
	
	}
	
	public String awardToString(Player player,RankAward award)
	{
		String content=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"gems");
		StringBuilder sb=new StringBuilder();
		int gems=award.award.getGemsAward(0);
		if(gems>0)
		{
			sb.append(gems+content+" ");
		}
		int[] props=award.props;
		if(props!=null)
		{
			for(int i=0;i<props.length;i+=2)
			{
				if(props[i]>0)
				{
					sb.append(InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"prop_sid_"+props[i])
						+"*"+props[i+1]+" ");
				}
			}
		}
		return sb.toString();
	}
	
	/** �Ƿ�ʹ�û����ˢ��
	 * (��������ˢ�°����ݣ���ƫ��ʱ�䵽���ʼʹ�����ݿ����ݣ��Դ������ݿ�洢�������) */
	public boolean isUpdateRankFromActivity(int nowTime,int offsetTime)
	{
		if(isSettleDown&&(awardTime+offsetTime)<nowTime) return false;
		return true;
	}
	
	/** ��ȡ���е÷� */
	public abstract long getScore(Player player);
	/** ��ȡ�������� */
	public abstract int getRank(Player player);
	/** ������������ */
	public abstract void setRank(Player player,int rank);
}
