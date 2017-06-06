package foxu.sea.equipment;

import foxu.sea.Player;
import foxu.sea.Resources;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.ArrayList;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * 装备
 * 
 * @author Alan
 */

public class Equipment extends Sample
{

	/** 进阶材料配置：sid,合成需要数量,花费金币,sid,合成需要数量,花费金币 */
	public static int[] QUALITY_STUFFS;
	/** 相应经验消耗道具的临界值，每段消耗个数 */
	public static int[] EXP_THRESHOLD_NUM;
	/** 新手引导完成对应的赠送物品：物品sid,数量,物品sid,数量 */
	public static int[] SYS_FOLLOW_PROP;
	/** 唯一编号 */
	int uid;
	/** 是否已装备，动态属性 */
	boolean isEquiped;
	/** 所属舰船种类 */
	int shipType;
	/** 装备位置 */
	int equipType;
	/** 上一品质sid */
	int lastQuility;
	/** 下一品质sid */
	int nextQuility;
	/** 提升的能力值:属性值sid，系数 */
	float[] upScores;
	/** 装备提供的抗性，参照{@link foxu.sea.Ship} */
	int[] resist;
	/** 装备提供的加成 */
	int[] attach;
	/** 能提供的装备经验值 */
	int[] provideExp;
	/** 当前经验 */
	int exp;
	/** 出售价值金币 */
	int[] saleMoney;
	/** 升级所需经验 */
	int[] levelExps;
	/** 进阶需要用到的物品：sid,num */
	int[] qualityUpProps;
	/** 进阶需要用到的进阶材料：sid,num */
	int[] qualityUpStuffs;
	/** 进阶需要用到的装备：sid,num */
	int[] qualityUpEquips;
	/** 进阶需要用到的资源METAL,OIL,SILICON,URANIUM,MONEY */
	int[] costResources;
	/** 是否修正为具体值 */
	boolean isFix=true;
	/** 是否可以升级 */
	boolean isUpgr=true;
	/**物品名称**/
	String equname;

	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	public int getShipType()
	{
		return shipType;
	}

	public void setShipType(int shipType)
	{
		this.shipType=shipType;
	}

	public int getEquipType()
	{
		return equipType;
	}

	public void setEquipType(int equipType)
	{
		this.equipType=equipType;
	}

	public int getLevel()
	{
		int level=1;
		if(levelExps!=null) for(int i=0;i<levelExps.length;i++)
		{
			if(exp>=levelExps[i])//todo
			{
				level++;
			}
			else
				break;
		}
		return level;
	}

	public int getMaxLevel()
	{
		if(levelExps==null) return 1;
		return levelExps.length+1;
	}

	public float[] getUpScores()
	{
		float[] upScore=new float[2];
		int level=getLevel()-1;
		upScore[0]=upScores[level*2];
		upScore[1]=upScores[level*2+1];
		return upScore;
	}

	public int getProvideExp()
	{
		return provideExp[getLevel()-1];
	}

	public int getExp()
	{
		return exp;
	}

	public void addExp(int exp)
	{
		this.exp+=exp;
		if(levelExps==null) return;
		this.exp=this.exp>levelExps[levelExps.length-1]
			?levelExps[levelExps.length-1]:this.exp;
	}

	public boolean isFix()
	{
		return isFix;
	}

	public void setFix(boolean isFix)
	{
		this.isFix=isFix;
	}

	public int getNextQuility()
	{
		return nextQuility;
	}

	public void setNextQuility(int nextQuility)
	{
		this.nextQuility=nextQuility;
	}

	public int[] getLevelExps()
	{
		return levelExps;
	}

	public void setLevelExps(int[] levelExps)
	{
		this.levelExps=levelExps;
	}

	public boolean isUpgr()
	{
		return isUpgr;
	}

	public void setUpgr(boolean isUpgr)
	{
		this.isUpgr=isUpgr;
	}

	public int getUid()
	{
		return uid;
	}

	public void setUid(int uid)
	{
		this.uid=uid;
	}

	public int getSaleMoney()
	{
		return saleMoney[getLevel()-1];
	}

	public boolean isEquiped()
	{
		return isEquiped;
	}

	public void setEquiped(boolean isEquiped)
	{
		this.isEquiped=isEquiped;
	}

	public int[] getResist()
	{
		return resist;
	}

	public int[] getAttach()
	{
		return attach;
	}

	public void setAttach(int[] attach)
	{
		this.attach=attach;
	}

	/** 获取当前增加经验时需要消耗的荣誉勋章个数 */
	public int getHonorNum(int exp)
	{
		if(EXP_THRESHOLD_NUM==null||EXP_THRESHOLD_NUM[0]==0)
			return 0;
		int num=exp/EXP_THRESHOLD_NUM[0];
		num+=exp%EXP_THRESHOLD_NUM[0]!=0?1:0;
		return num;
	}

	public boolean checkQualityUp(Player player)//todo
	{
		if(qualityUpProps!=null)
		for(int i=0;i<qualityUpProps.length;i+=2){
			if(qualityUpProps[i+1]>player.getBundle().getPropCount(
				qualityUpProps[i]))
			{
				return false;
			}
		}
		if(qualityUpStuffs!=null)
		for(int i=0;i<qualityUpStuffs.length;i+=2){
			if(player.getEquips().getQualityStuffCount(qualityUpStuffs[i])<qualityUpStuffs[i+1])
			{
				return false;
			}
		}
		if(qualityUpEquips!=null)
		for(int i=0;i<qualityUpEquips.length;i+=2){
			if(qualityUpEquips[i+1]>player.getEquips().getSameQualityUnequip(qualityUpEquips[i]).size())
			{
				return false;
			}
		}
		if(costResources!=null)
		return Resources.checkResources(costResources,player.getResources(),
			1);
		return true;
	}

	public void reduceQualityStuff(Player player)
	{
		if(qualityUpProps!=null)
		for(int i=0;i<qualityUpProps.length;i+=2){
			player.getBundle().decrProp(qualityUpProps[i],qualityUpProps[i+1]);
		}
		if(qualityUpStuffs!=null)
		for(int i=0;i<qualityUpStuffs.length;i+=2){
			player.getEquips().decrQualityStuff(qualityUpStuffs[i],qualityUpStuffs[i+1]);
		}
		if(qualityUpEquips!=null)
		for(int i=0;i<qualityUpEquips.length;i+=2){
			ArrayList al=player.getEquips().getSameQualityUnequip(qualityUpEquips[i]);
			for(int j=0;j<qualityUpEquips[i+1];j++){
				//选出优先级最高的装备
				Equipment equip=(Equipment)al.get(0);
				for(int k=1;k<al.size();k++){
					Equipment equipTemp=(Equipment)al.get(k);
					if(equip.getExp()>equipTemp.getExp())
						equip=equipTemp;
				}
				//从数组内移除优先的装备
				al.remove(equip);
				//移除装备
				player.getEquips().removeEquip(equip.getUid());
			}
		}
		if(costResources!=null)
		Resources
			.reduceResources(player.getResources(),costResources,player);
	}

	/** 从字节数组中反序列化获得对象的域 */
	public static Equipment bytesReadEquip(ByteBuffer data)
	{
		int uid=data.readInt();
		int exp=data.readInt();
		boolean isEquip=data.readBoolean();
		int sid=data.readUnsignedShort();
		if(sid==0) return null;
		Equipment e=(Equipment)factory.newSample(sid);
		if(e==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,
				Equipment.class.getName()+" bytesRead, invalid sid:"+sid);
		e.setUid(uid);
		e.addExp(exp);
		e.setEquiped(isEquip);
		e.bytesRead(data);
		return e;
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		return this;
	}

	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(uid);
		data.writeInt(exp);
		data.writeBoolean(isEquiped);
		super.bytesWrite(data);
	}

	
	public int[] getQualityUpStuffs()
	{
		return qualityUpStuffs;
	}

	
	public int[] getQualityUpEquips()
	{
		return qualityUpEquips;
	}

	
	public String getEquname()
	{
		return equname;
	}

	
	public void setEquname(String equname)
	{
		this.equname=equname;
	}

	
	public int getLastQuility()
	{
		return lastQuility;
	}

	
	public void setLastQuility(int lastQuility)
	{
		this.lastQuility=lastQuility;
	}
	
	
}
