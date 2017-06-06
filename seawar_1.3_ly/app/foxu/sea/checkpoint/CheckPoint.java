package foxu.sea.checkpoint;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.text.TextKit;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;

/**
 * 关卡战斗 author:icetiger
 */
public class CheckPoint extends Sample
{
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	/* static methods */
	/** 从字节数组中反序列化获得对象的域 */
	public static CheckPoint bytesReadCheckpoint(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		CheckPoint r=(CheckPoint)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,CheckPoint.class
					.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}

	/** 战斗配置 island里面的船只sid用关卡专门的 */
	int npcIslandSid;
	/** 所属章节 */
	int chapter;
	/** 下一个打开的关卡sid */
	int nextSid;
	/** 玩家等级限制 */
	int levelLimit;
	/** 玩家军衔限制 */
	int rankLimit;
	/** 所属章节的位置 */
	int index;
	/** 描述 */
	String description;
	/** 关卡等级 */
	int pointLevel;
	/** 关卡名字 */
	String name;
	/** 关卡战斗类型 */
	int fightType;

	/** 动态域 npcIsland */
	NpcIsland island;
	/** 动态域 奖励品 */
	Award award;

	/**
	 * 不要改变award本身
	 * 
	 * @return award
	 */
	public Award getAward()
	{
		//因奖励可能受到活动加成被修改，所以此处改为new
//		if(award!=null) return award;
//		if(island==null)
//		{
//			island=(NpcIsland)NpcIsland.factory.newSample(npcIslandSid);
//		}
//		award=(Award)Award.factory.getSample(island.getAwardSid());
//		return award;
		return (Award)Award.factory.newSample(island.getAwardSid());
	}
	public Award getChapterAward(int sid)
	{
		
		return (Award)Award.factory.newSample(sid);
	}
	/**
	 * @param award 要设置的 award
	 */
	public void setAward(Award award)
	{
		this.award=award;
	}

	/**
	 * @return island
	 */
	public NpcIsland getIsland()
	{
		if(island!=null) return island;
		island=(NpcIsland)NpcIsland.factory.newSample(npcIslandSid);
		return island;
	}

	/**
	 * @param island 要设置的 island
	 */
	public void setIsland(NpcIsland island)
	{
		this.island=island;
	}

	/* methods */
	/**
	 * 开始关卡战斗
	 * 
	 * @param attacker 进攻方
	 */
	public Object[] fight(FleetGroup attacker)
	{
		if(island==null) getIsland();
		island.getFleetGroup();
		return island.fight(attacker);
	}

	/** 关卡胜利奖励 */
	public int fightSuccess(Player player,FleetGroup before,ByteBuffer data,
		CreatObjectFactory objectFactory,int fightType,boolean flag)
	{
		String messageString="";
		// 计算星级
		int star=SelfCheckPoint.THREE_STAR;
		if(flag)
		{
			// 存储关卡
			SelfCheckPoint point=null;
			if(fightType==PublicConst.FIGHT_CHECK_POINT)
			{
				point=player.getSelfCheckPoint();
			}else
			{
				point=player.getTearCheckPoint();
			}
			Fleet fleet[]=before.getArray();
			int destoryNum=0;
			for(int i=0;i<fleet.length;i++)
			{
				if(fleet[i]!=null)
				{
					if(fleet[i].getNum()==0)
					{
						destoryNum++;
						star=SelfCheckPoint.TOW_STAR;
					}
					if(destoryNum>=2)
					{
						star=SelfCheckPoint.ONE_STAR;
					}
				}
			}
			point.addPoint(star,nextSid,chapter-1,index*2);
			// 发送奖励
			data.writeByte(star);
			// 系统消息
			InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"attack_checkpoint");
			messageString=TextKit.replace(messageString,"%",player.getName());
		}
		Award award=getAward();
		int exp=award.getExperienceAward(player.getLevel());
		if(exp>0)
		{
			// 经验值活动加成
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			award.setExperienceAward(exp);
		}
		award.awardLenth(data,player,objectFactory,messageString,
			new int[]{EquipmentTrack.FROM_CHECK_POINT});
		return star;
	}
	public int addPointBuff(Player player,CreatObjectFactory cfactory)
	{
		Chapter cp=(Chapter)Chapter.factory.getSample(chapter);
		if(cp==null||cp.getType()<0)return 0;
		return  player.addPointBuff(cp.getType(),Chapter.NO_USER_STATE,cfactory);
	}

	/**
	 * @return levelLimit
	 */
	public int getLevelLimit()
	{
		return levelLimit;
	}

	/**
	 * @param levelLimit 要设置的 levelLimit
	 */
	public void setLevelLimit(int levelLimit)
	{
		this.levelLimit=levelLimit;
	}

	/**
	 * @return rankLimit
	 */
	public int getRankLimit()
	{
		return rankLimit;
	}

	/**
	 * @param rankLimit 要设置的 rankLimit
	 */
	public void setRankLimit(int rankLimit)
	{
		this.rankLimit=rankLimit;
	}

	/**
	 * @return index
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @param index 要设置的 index
	 */
	public void setIndex(int index)
	{
		this.index=index;
	}

	/**
	 * @return chapter
	 */
	public int getChapter()
	{
		return chapter;
	}

	/**
	 * @param chapter 要设置的 chapter
	 */
	public void setChapter(int chapter)
	{
		this.chapter=chapter;
	}

	/**
	 * @return nextSid
	 */
	public int getNextSid()
	{
		return nextSid;
	}

	/**
	 * @param nextSid 要设置的 nextSid
	 */
	public void setNextSid(int nextSid)
	{
		this.nextSid=nextSid;
	}

	/**
	 * @return npcIslandSid
	 */
	public int getNpcIslandSid()
	{
		return npcIslandSid;
	}

	/**
	 * @param npcIslandSid 要设置的 npcIslandSid
	 */
	public void setNpcIslandSid(int npcIslandSid)
	{
		this.npcIslandSid=npcIslandSid;
	}

	/**
	 * @return pointLevel
	 */
	public int getPointLevel()
	{
		return pointLevel;
	}

	/**
	 * @param pointLevel 要设置的 pointLevel
	 */
	public void setPointLevel(int pointLevel)
	{
		this.pointLevel=pointLevel;
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name 要设置的 name
	 */
	public void setName(String name)
	{
		this.name=name;
	}

	/**
	 * @return fightType
	 */
	public int getFightType()
	{
		return fightType;
	}

	/**
	 * @param fightType 要设置的 fightType
	 */
	public void setFightType(int fightType)
	{
		this.fightType=fightType;
	}
	/**
	 * 联合舰队奖励方式
	 * 
	 */
	public int heritagefightSuccess(Player player,int shiplength,int shipdestorylength,ByteBuffer data,
		CreatObjectFactory objectFactory,int fightType)
	{
		// 存储关卡
		HCityCheckPoint point=player.getHeritagePoint();
		// 计算星级
		int star=SelfCheckPoint.THREE_STAR;
		if(shipdestorylength>=shiplength/3
			&&shipdestorylength<=shiplength*2/3)
			star=SelfCheckPoint.TOW_STAR;
		else if(shipdestorylength>=shiplength*2/3)
			star=SelfCheckPoint.ONE_STAR;
		point.addPoint(star,nextSid,chapter-1,index*2);
		point.attactRecord(chapter,index);
		// 发送奖励
		data.writeShort(getSid());//todo
		data.writeByte(star);
		Award award=getAward();
		int exp=award.getExperienceAward(player.getLevel());
		if(exp>0)
		{
			// 经验值活动加成
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			award.setExperienceAward(exp);
		}
		award.awardLenth(data,player,objectFactory,null,
			new int[]{EquipmentTrack.FROM_CHECK_POINT});
		return star;
	}
//	/**
//	 *遗忘都市关卡奖励
//	 * @return
//	 */
//	public int heritagAward(int chapter,Player player,int shiplength,int shipdestorylength,ByteBuffer data,
//		CreatObjectFactory objectFactory,int fightType)
//	{
//		String messageString=InterTransltor.getInstance().getTransByKey(
//			PublicConst.SERVER_LOCALE,"success_hei_check_point");
//		messageString=TextKit.replace(messageString,"%",player.getName());
//		int awardChapterSid=HCityCheckPoint.award[chapter-1];
//		Award award=getChapterAward(awardChapterSid);
//		int exp=award.getExperienceAward(player.getLevel());
//		if(exp>0)
//		{
//			// 经验值活动加成
//			exp=ActivityContainer.getInstance().resetActivityExp(exp);
//			award.setExperienceAward(exp);
//		}
//		award.awardLenth(data,player,objectFactory,"null",
//			new int[]{EquipmentTrack.FROM_CHECK_POINT});
//		return 0;
//	}
	
	public void setGroup(FleetGroup group)
	{
		if(island==null) getIsland();
		island.setFleetGroup(group);
	}
}