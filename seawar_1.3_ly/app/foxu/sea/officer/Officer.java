package foxu.sea.officer;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.IntList;
import mustang.util.Sample;
import foxu.sea.PublicConst;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * 军官
 * 
 * @author Alan
 */
public class Officer extends Sample
{

	/** 属性位置对应类型 */
	public static final int[] ATTR_TYPE={PublicConst.ATTACK,
		PublicConst.SHIP_HP,PublicConst.ACCURATE,PublicConst.AVOID,
		PublicConst.CRITICAL_HIT,PublicConst.CRITICAL_HIT_RESIST,
		PublicConst.EXTRA_SHIP,PublicConst.EXTRA_SPEED,
		PublicConst.EXTRA_CARRY};
	public static final int INIT_LEVEL=1;
	// 动态数据，需要存储
	int id;
	int level=INIT_LEVEL;
	int exp;
	/** 军衔 */
	int militaryRank=INIT_LEVEL;

	// 非动态数据
	/** 稀有度 */
	int scarcity;
	/** 稀有度系数 */
	float scarcityGrowth;
	/** 基础属性 */
	float[] baseAttr;
	/** 基础属性成长 */
	float[] baseAttrGrowth;
	/** 基础技能概率影响 */
	float[] baseSkill;
	/** 基础技能概率成长 */
	float[] baseSkillGrowth;
	/** 附加属性 */
	float[] extraAttr;
	/** 附加成长 */
	float[] extraAttrGrowth;
	/** 附加技能概率影响 */
	float[] extraSkill;
	/** 附加技能概率成长 */
	float[] extraSkillGrowth;
	/** 升级经验数组(差值,非累积) */
	int[] levelExps;
	/** 军衔提升时需要的当前初级军官数量 */
	int[] rankUpCount;
	/** 军衔提升时需要的铁 */
	int[] rankUpMetal;
	/** 军衔提升时需要的油 */
	int[] rankUpOil;
	/** 军衔提升时需要的硅 */
	int[] rankUpSilicon;
	/** 军衔提升时需要的铀 */
	int[] rankUpUranium;
	/** 军衔提升时需要的金币 */
	int[] rankUpGold;
	/** 军衔提升时需要的宝石 */
	int[] rankUpGem;
	/** 遣散军官的返利经验{军衔1,军衔2,...} */
	int[] disbandExp;
	/** 遣散军官的返利物品{propsid,num,propsid,num...} */
	int[] disbandProps;
	/** 二级货币 **/
	int[] disbandCoins;
	/** 军官技能 */
	int[] officerSkill;

	/** 增加经验值,返回增加是否成功 */
	public boolean incrExp(int n)
	{
		if(n<=0||exp+n<0) return false;// 越界判定
		exp+=n;
		flushLevel();
		return true;
	}

	/** 刷新级数 */
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

	/** 从字节数组中反序列化获得对象的域 */
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

	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
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
	 * 添加属性值
	 * 
	 * @param isRankAttach 是否计算军衔带来的额外加成(如基础属性)
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
				// 是否计算军衔带来的额外加成(如基础属性)
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
				// 是否计算军衔带来的额外加成(如基础属性)
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

	/** 重置级数 */
	public void resetLevel()
	{
		setLevel(INIT_LEVEL);
		exp=0;
	}

	/** 提升军衔 */
	public void incrMilitaryRank()
	{
		militaryRank++;
	}

	/** 获取进阶需要的资源{metal,oil,silicon,uranium,money,gem} */
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
	/** 获取进阶需要的原始军官数量 */
	public int getRankUpOfficers()
	{
		if(rankUpCount==null||rankUpCount.length<militaryRank) return 0;
		return rankUpCount[militaryRank-1];
	}
	/** 遣散军官时能够获取的功勋值 */
	public int disbandFeats()
	{
		if(disbandExp==null) return 0;
		return disbandExp[militaryRank-1];
	}
	/** 遣散军官时能够获取的道具{sid,num,sid,num...} */
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

	/** 获取当前2级货币值 **/
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
