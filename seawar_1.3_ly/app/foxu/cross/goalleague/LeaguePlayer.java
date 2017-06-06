package foxu.cross.goalleague;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import foxu.cross.war.CrossWarPlayer;
import foxu.sea.Player;

/**
 * 积分赛角色
 * 
 * @author Alan
 * 
 */
public class LeaguePlayer extends CrossWarPlayer
{

	public static final int CHALLENGE_SUCCESS=1,CHALLENGE_FAIL=0;
	/** 当前挑战列表 */
	IntList currentList=new IntList();
	/** 玩家挑战记录 */
	IntList challengeList=new IntList();
	/** 玩家物品兑换记录 */
	IntList propList=new IntList();
	/** 上次活跃时间(用于物品兑换、付费刷新、挑战重置) */
	int lastActiveTime;
	/** 当前刷新次数 */
	int currentFlushCount;
	/** 当前挑战次数 */
	int currentBattleCount;
	/** 当前挑战次数上限 */
	int currentBattleLimit;
	/** 当前数据所属的联赛 */
	int leagueId;
	/* 动态属性 */
	/** 头像图片 */
	int headPic;
	/** 头像边框 */
	int headFrame;
	/** 服务器sid */
	int serverSid;

	/** 初始化比赛信息 */
	public void init()
	{

	}

	/** 重置当前挑战列表,并记录当前战力 */
	public void resetCurrentChallengeList(int[] ids,Player player)
	{
		currentList.clear();
		for(int i=0;i<ids.length;i++)
		{
			currentList.add(ids[i]);
			currentList.add(CHALLENGE_FAIL);
			challengeList.add(ids[i]);
		}
		setFightscore(player.getFightScore());
		setSave(true);
	}

	/** 当前目标是否可以攻击 */
	public boolean isTargetCanFight(int targetId)
	{
		for(int i=0;i<currentList.size();i+=2)
		{
			if(currentList.get(i)==targetId
				&&currentList.get(i+1)==CHALLENGE_FAIL) return true;
		}
		return false;
	}

	/** 当前目标在列表中的位置 */
	public int getTargetIndex(int targetId)
	{
		for(int i=0;i<currentList.size();i+=2)
		{
			if(currentList.get(i)==targetId) return i/2;
		}
		return -1;
	}

	public void resetTargetStaut(int targetId,int staut)
	{
		for(int i=0;i<currentList.size();i+=2)
		{
			if(currentList.get(i)!=targetId) continue;
			currentList.set(staut,i+1);
			setSave(true);
			break;
		}
	}

	public void incrPropRecord(int sid,int num)
	{
		if(propList.size()<=0)
		{
			propList.add(sid);
			propList.add(num);
		}
		else
		{
			for(int j=0;j<propList.size();j++)
			{
				if(propList.get(j)==sid)
					propList.set(propList.get(j+1)+num,j+1);
			}
		}
		setSave(true);
	}

	/** 获取当前挑战列表(目标id,挑战状态...) */
	public IntList getCurrentChallengeList()
	{
		return currentList;
	}

	/** 重置挑战记录 */
	public void resetChallengeRecord()
	{
		currentList.clear();
		challengeList.clear();
		setSave(true);
	}

	public void incrTodayFlushCount()
	{
		currentFlushCount++;
		setSave(true);
	}

	public void incrTodayBattleCount()
	{
		currentBattleCount++;
		setSave(true);
	}

	public void incrTodayBattleCountLimit()
	{
		currentBattleLimit++;
		setSave(true);
	}

	public IntList getChallengeList()
	{
		return challengeList;
	}

	public void setChallengeList(IntList challengeList)
	{
		this.challengeList=challengeList;
	}

	public IntList getCurrentList()
	{
		return currentList;
	}

	public void setCurrentList(IntList currentList)
	{
		this.currentList=currentList;
		setSave(true);
	}

	public IntList getPropList()
	{
		return propList;
	}

	public void setPropList(IntList propList)
	{
		this.propList=propList;
		setSave(true);
	}

	public int getLastActiveTime()
	{
		return lastActiveTime;
	}

	public void setLastActiveTime(int lastActiveTime)
	{
		this.lastActiveTime=lastActiveTime;
		setSave(true);
	}

	public int getCurrentBattleCount()
	{
		return currentBattleCount;
	}

	public void setCurrentBattleCount(int currentBattleCount)
	{
		this.currentBattleCount=currentBattleCount;
		setSave(true);
	}

	public int getCurrentFlushCount()
	{
		return currentFlushCount;
	}

	public void setCurrentFlushCount(int currentFlushCount)
	{
		this.currentFlushCount=currentFlushCount;
		setSave(true);
	}

	public int getCurrentBattleLimit()
	{
		return currentBattleLimit;
	}

	public void setCurrentBattleLimit(int currentBattleLimit)
	{
		this.currentBattleLimit=currentBattleLimit;
		setSave(true);
	}

	public int getLeagueId()
	{
		return leagueId;
	}

	public void setLeagueId(int leagueId)
	{
		this.leagueId=leagueId;
		setSave(true);
	}

	public int getHeadPic()
	{
		return headPic;
	}

	public void setHeadPic(int headPic)
	{
		this.headPic=headPic;
	}

	public int getHeadFrame()
	{
		return headFrame;
	}

	public void setHeadFrame(int headFrame)
	{
		this.headFrame=headFrame;
	}

	public int getServerSid()
	{
		return serverSid;
	}

	public void setServerSid(int serverSid)
	{
		this.serverSid=serverSid;
	}

	public void bytesReadCurrentList(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			// targetId
			currentList.add(data.readInt());
			// fightResult
			currentList.add(data.readUnsignedByte());
		}
	}

	public void bytesWriteCurrentList(ByteBuffer data)
	{
		data.writeByte(currentList.size()/2);
		for(int i=0;i<currentList.size();i+=2)
		{
			data.writeInt(currentList.get(i));
			data.writeByte(currentList.get(i+1));
		}
	}

	public void bytesReadRecordList(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			// targetId
			challengeList.add(data.readInt());
		}
	}

	public void bytesWriteRecordList(ByteBuffer data)
	{
		data.writeInt(challengeList.size());
		for(int i=0;i<challengeList.size();i++)
		{
			data.writeInt(challengeList.get(i));
		}
	}

	public void bytesReadPropList(ByteBuffer data)
	{
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			// sid
			propList.add(data.readUnsignedShort());
			// num
			propList.add(data.readInt());
		}
	}

	public void bytesWritePropList(ByteBuffer data)
	{
		data.writeInt(propList.size()/2);
		for(int i=0;i<propList.size();i+=2)
		{
			// sid
			data.writeShort(propList.get(i));
			// num
			data.writeInt(propList.get(i+1));
		}
	}
	
	/** 初始化跨服属性(屏蔽不需要的角色属性)*/
	public void initCrossAttrs(Player player)
	{
		setSid(player.getSid());
		setName(player.getName());
		setLevel(player.getLevel());
		setOfs(player.getOfficers().getUsingOfficers());
		ByteBuffer data=new ByteBuffer();
		player.crossWriteAdjustment(data);
		bytesReadAdjustment(data);
		setShipLevel(player.getShipLevel());
	}

}
