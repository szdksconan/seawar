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
 * �ؿ�ս�� author:icetiger
 */
public class CheckPoint extends Sample
{
	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/* static methods */
	/** ���ֽ������з����л���ö������ */
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

	/** ս������ island����Ĵ�ֻsid�ùؿ�ר�ŵ� */
	int npcIslandSid;
	/** �����½� */
	int chapter;
	/** ��һ���򿪵Ĺؿ�sid */
	int nextSid;
	/** ��ҵȼ����� */
	int levelLimit;
	/** ��Ҿ������� */
	int rankLimit;
	/** �����½ڵ�λ�� */
	int index;
	/** ���� */
	String description;
	/** �ؿ��ȼ� */
	int pointLevel;
	/** �ؿ����� */
	String name;
	/** �ؿ�ս������ */
	int fightType;

	/** ��̬�� npcIsland */
	NpcIsland island;
	/** ��̬�� ����Ʒ */
	Award award;

	/**
	 * ��Ҫ�ı�award����
	 * 
	 * @return award
	 */
	public Award getAward()
	{
		//���������ܵ���ӳɱ��޸ģ����Դ˴���Ϊnew
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
	 * @param award Ҫ���õ� award
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
	 * @param island Ҫ���õ� island
	 */
	public void setIsland(NpcIsland island)
	{
		this.island=island;
	}

	/* methods */
	/**
	 * ��ʼ�ؿ�ս��
	 * 
	 * @param attacker ������
	 */
	public Object[] fight(FleetGroup attacker)
	{
		if(island==null) getIsland();
		island.getFleetGroup();
		return island.fight(attacker);
	}

	/** �ؿ�ʤ������ */
	public int fightSuccess(Player player,FleetGroup before,ByteBuffer data,
		CreatObjectFactory objectFactory,int fightType,boolean flag)
	{
		String messageString="";
		// �����Ǽ�
		int star=SelfCheckPoint.THREE_STAR;
		if(flag)
		{
			// �洢�ؿ�
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
			// ���ͽ���
			data.writeByte(star);
			// ϵͳ��Ϣ
			InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"attack_checkpoint");
			messageString=TextKit.replace(messageString,"%",player.getName());
		}
		Award award=getAward();
		int exp=award.getExperienceAward(player.getLevel());
		if(exp>0)
		{
			// ����ֵ��ӳ�
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
	 * @param levelLimit Ҫ���õ� levelLimit
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
	 * @param rankLimit Ҫ���õ� rankLimit
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
	 * @param index Ҫ���õ� index
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
	 * @param chapter Ҫ���õ� chapter
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
	 * @param nextSid Ҫ���õ� nextSid
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
	 * @param npcIslandSid Ҫ���õ� npcIslandSid
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
	 * @param pointLevel Ҫ���õ� pointLevel
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
	 * @param name Ҫ���õ� name
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
	 * @param fightType Ҫ���õ� fightType
	 */
	public void setFightType(int fightType)
	{
		this.fightType=fightType;
	}
	/**
	 * ���Ͻ��ӽ�����ʽ
	 * 
	 */
	public int heritagefightSuccess(Player player,int shiplength,int shipdestorylength,ByteBuffer data,
		CreatObjectFactory objectFactory,int fightType)
	{
		// �洢�ؿ�
		HCityCheckPoint point=player.getHeritagePoint();
		// �����Ǽ�
		int star=SelfCheckPoint.THREE_STAR;
		if(shipdestorylength>=shiplength/3
			&&shipdestorylength<=shiplength*2/3)
			star=SelfCheckPoint.TOW_STAR;
		else if(shipdestorylength>=shiplength*2/3)
			star=SelfCheckPoint.ONE_STAR;
		point.addPoint(star,nextSid,chapter-1,index*2);
		point.attactRecord(chapter,index);
		// ���ͽ���
		data.writeShort(getSid());//todo
		data.writeByte(star);
		Award award=getAward();
		int exp=award.getExperienceAward(player.getLevel());
		if(exp>0)
		{
			// ����ֵ��ӳ�
			exp=ActivityContainer.getInstance().resetActivityExp(exp);
			award.setExperienceAward(exp);
		}
		award.awardLenth(data,player,objectFactory,null,
			new int[]{EquipmentTrack.FROM_CHECK_POINT});
		return star;
	}
//	/**
//	 *�������йؿ�����
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
//			// ����ֵ��ӳ�
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