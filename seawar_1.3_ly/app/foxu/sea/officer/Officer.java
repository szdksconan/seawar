package foxu.sea.officer;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.IntList;
import mustang.util.Sample;
import foxu.sea.PublicConst;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * ����
 * 
 * @author Alan
 */
public class Officer extends Sample
{

	/** ����λ�ö�Ӧ���� */
	public static final int[] ATTR_TYPE={PublicConst.ATTACK,
		PublicConst.SHIP_HP,PublicConst.ACCURATE,PublicConst.AVOID,
		PublicConst.CRITICAL_HIT,PublicConst.CRITICAL_HIT_RESIST,
		PublicConst.EXTRA_SHIP,PublicConst.EXTRA_SPEED,
		PublicConst.EXTRA_CARRY};
	public static final int INIT_LEVEL=1;
	// ��̬���ݣ���Ҫ�洢
	int id;
	int level=INIT_LEVEL;
	int exp;
	/** ���� */
	int militaryRank=INIT_LEVEL;

	// �Ƕ�̬����
	/** ϡ�ж� */
	int scarcity;
	/** ϡ�ж�ϵ�� */
	float scarcityGrowth;
	/** �������� */
	float[] baseAttr;
	/** �������Գɳ� */
	float[] baseAttrGrowth;
	/** �������ܸ���Ӱ�� */
	float[] baseSkill;
	/** �������ܸ��ʳɳ� */
	float[] baseSkillGrowth;
	/** �������� */
	float[] extraAttr;
	/** ���ӳɳ� */
	float[] extraAttrGrowth;
	/** ���Ӽ��ܸ���Ӱ�� */
	float[] extraSkill;
	/** ���Ӽ��ܸ��ʳɳ� */
	float[] extraSkillGrowth;
	/** ������������(��ֵ,���ۻ�) */
	int[] levelExps;
	/** ��������ʱ��Ҫ�ĵ�ǰ������������ */
	int[] rankUpCount;
	/** ��������ʱ��Ҫ���� */
	int[] rankUpMetal;
	/** ��������ʱ��Ҫ���� */
	int[] rankUpOil;
	/** ��������ʱ��Ҫ�Ĺ� */
	int[] rankUpSilicon;
	/** ��������ʱ��Ҫ���� */
	int[] rankUpUranium;
	/** ��������ʱ��Ҫ�Ľ�� */
	int[] rankUpGold;
	/** ��������ʱ��Ҫ�ı�ʯ */
	int[] rankUpGem;
	/** ǲɢ���ٵķ�������{����1,����2,...} */
	int[] disbandExp;
	/** ǲɢ���ٵķ�����Ʒ{propsid,num,propsid,num...} */
	int[] disbandProps;
	/** �������� **/
	int[] disbandCoins;
	/** ���ټ��� */
	int[] officerSkill;

	/** ���Ӿ���ֵ,���������Ƿ�ɹ� */
	public boolean incrExp(int n)
	{
		if(n<=0||exp+n<0) return false;// Խ���ж�
		exp+=n;
		flushLevel();
		return true;
	}

	/** ˢ�¼��� */
	public int flushLevel()
	{
		int addLevel=0;
		for(int i=level-1;i<levelExps.length;i++)
		{
			if(exp>=levelExps[i])
			{
				addLevel++;
				exp-=levelExps[i];
				continue;
			}
			break;
		}
		if(exp>levelExps[levelExps.length-1])
			exp=levelExps[levelExps.length-1];
		setLevel(level+addLevel);
		return addLevel;
	}

	/** ���ֽ������з����л���ö������ */
	public static Officer bytesReadOfficer(ByteBuffer data)
	{
		int id=data.readInt();
		int sid=data.readUnsignedShort();
		int militaryRank=data.readUnsignedByte();
		int level=data.readUnsignedByte();
		int exp=data.readInt();
		Officer o=(Officer)OfficerManager.factory.newSample(sid);
		if(o==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,
				Officer.class.getName()+" bytesRead, invalid sid:"+sid);
		o.setId(id);
		o.setSid(sid);
		o.setMilitaryRank(militaryRank);
		o.setLevel(level);
		o.setExp(exp);
		return o;
	}

	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeShort(getSid());
		data.writeByte(militaryRank);
		data.writeByte(level);
		data.writeInt(exp);
	}

	public void showBytesWrite(ByteBuffer data)
	{
		data.writeInt(id);
		data.writeInt(getSid());
		data.writeInt(militaryRank);
		data.writeInt(level);
		data.writeInt(exp);
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}

	public int getExp()
	{
		return exp;
	}

	public void setExp(int exp)
	{
		this.exp=exp;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int lv)
	{
		level=lv;
	}

	public void setMilitaryRank(int militaryRank)
	{
		this.militaryRank=militaryRank;
	}

	public int getMilitaryRank()
	{
		return militaryRank;
	}

	public int getScarcity()
	{
		return scarcity;
	}

	public int[] getLevelExps()
	{
		return levelExps;
	}

	public void effectExecute(int location,OfficerBattleHQ battleHQ)
	{
		addEffectAttr(location,battleHQ,baseAttr,baseAttrGrowth,
			getLevel()-1,true);
		addEffectSkill(location,battleHQ,baseSkill,baseSkillGrowth,
			getLevel()-1,true);
		addEffectAttr(location,battleHQ,extraAttr,extraAttrGrowth,
			(getMilitaryRank()-1)*scarcityGrowth,false);
		addEffectSkill(location,battleHQ,extraSkill,extraSkillGrowth,
			(getMilitaryRank()-1)*scarcityGrowth,false);
	}

	/**
	 * �������ֵ
	 * 
	 * @param isRankAttach �Ƿ������δ����Ķ���ӳ�(���������)
	 */
	private void addEffectAttr(int location,OfficerBattleHQ battleHQ,
		float[] attrs,float[] growth,float attachValue,boolean isRankAttach)
	{
		for(int i=0;i<attrs.length;i++)
		{
			float addValue=attrs[i];
			if(growth[i]>0)
			{
				addValue+=growth[i]*attachValue;
				// �Ƿ������δ����Ķ���ӳ�(���������)
				if(isRankAttach)
					addValue+=growth[i]*militaryRank*(militaryRank-1)/2;
			}
			if(addValue!=0)
			{
				battleHQ.addAttr(OfficerBattleHQ.ARMY,
					OfficerBattleHQ.CURRENT_LOCATION,ATTR_TYPE[i],false,
					addValue,new IntList(new int[]{location}));
			}
		}
	}

	private void addEffectSkill(int location,OfficerBattleHQ battleHQ,
		float[] skills,float[] growth,float attachValue,boolean isRankAttach)
	{
		for(int i=0;i<skills.length;i++)
		{
			int addValue=(int)skills[i];
			if(growth[i]>0)
			{
				addValue=(int)(skills[i]+growth[i]*attachValue);
				// �Ƿ������δ����Ķ���ӳ�(���������)
				if(isRankAttach)
					addValue+=growth[i]*militaryRank*(militaryRank-1)/2;
			}
			if(addValue!=0)
			{
				battleHQ.addSkill(OfficerBattleHQ.ARMY,
					OfficerBattleHQ.CURRENT_LOCATION,i,addValue,new IntList(
						new int[]{location}));
			}
		}
	}

	/** ���ü��� */
	public void resetLevel()
	{
		setLevel(INIT_LEVEL);
		exp=0;
	}

	/** �������� */
	public void incrMilitaryRank()
	{
		militaryRank++;
	}

	/** ��ȡ������Ҫ����Դ{metal,oil,silicon,uranium,money,gem} */
	public int[] getRankUpResource()
	{
		return new int[]{getRankUpResource(rankUpMetal),
			getRankUpResource(rankUpOil),getRankUpResource(rankUpSilicon),
			getRankUpResource(rankUpUranium),getRankUpResource(rankUpGold),
			getRankUpResource(rankUpGem)};
	}
	public int getRankUpResource(int[] resource)
	{
		if(resource==null||resource.length<militaryRank) return 0;
		return resource[militaryRank-1];
	}
	/** ��ȡ������Ҫ��ԭʼ�������� */
	public int getRankUpOfficers()
	{
		if(rankUpCount==null||rankUpCount.length<militaryRank) return 0;
		return rankUpCount[militaryRank-1];
	}
	/** ǲɢ����ʱ�ܹ���ȡ�Ĺ�ѫֵ */
	public int disbandFeats()
	{
		if(disbandExp==null) return 0;
		return disbandExp[militaryRank-1];
	}
	/** ǲɢ����ʱ�ܹ���ȡ�ĵ���{sid,num,sid,num...} */
	public int[] disbandProps()
	{
		int[] props=null;
		if(disbandProps!=null)
		{
			props=new int[2];
			props[0]=disbandProps[militaryRank-1];
			props[1]=disbandProps[militaryRank];
		}
		return props;
	}

	/** ��ȡ��ǰ2������ֵ **/
	public int getDisbandCoin()
	{
		return disbandCoins[militaryRank-1];
	}

	public int[] getOfficerSkill()
	{
		return officerSkill;
	}

	public void setOfficerSkill(int[] officerSkill)
	{
		this.officerSkill=officerSkill;
	}

}
