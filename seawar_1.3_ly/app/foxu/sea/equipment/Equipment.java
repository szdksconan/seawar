package foxu.sea.equipment;

import foxu.sea.Player;
import foxu.sea.Resources;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.ArrayList;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * װ��
 * 
 * @author Alan
 */

public class Equipment extends Sample
{

	/** ���ײ������ã�sid,�ϳ���Ҫ����,���ѽ��,sid,�ϳ���Ҫ����,���ѽ�� */
	public static int[] QUALITY_STUFFS;
	/** ��Ӧ�������ĵ��ߵ��ٽ�ֵ��ÿ�����ĸ��� */
	public static int[] EXP_THRESHOLD_NUM;
	/** ����������ɶ�Ӧ��������Ʒ����Ʒsid,����,��Ʒsid,���� */
	public static int[] SYS_FOLLOW_PROP;
	/** Ψһ��� */
	int uid;
	/** �Ƿ���װ������̬���� */
	boolean isEquiped;
	/** ������������ */
	int shipType;
	/** װ��λ�� */
	int equipType;
	/** ��һƷ��sid */
	int lastQuility;
	/** ��һƷ��sid */
	int nextQuility;
	/** ����������ֵ:����ֵsid��ϵ�� */
	float[] upScores;
	/** װ���ṩ�Ŀ��ԣ�����{@link foxu.sea.Ship} */
	int[] resist;
	/** װ���ṩ�ļӳ� */
	int[] attach;
	/** ���ṩ��װ������ֵ */
	int[] provideExp;
	/** ��ǰ���� */
	int exp;
	/** ���ۼ�ֵ��� */
	int[] saleMoney;
	/** �������辭�� */
	int[] levelExps;
	/** ������Ҫ�õ�����Ʒ��sid,num */
	int[] qualityUpProps;
	/** ������Ҫ�õ��Ľ��ײ��ϣ�sid,num */
	int[] qualityUpStuffs;
	/** ������Ҫ�õ���װ����sid,num */
	int[] qualityUpEquips;
	/** ������Ҫ�õ�����ԴMETAL,OIL,SILICON,URANIUM,MONEY */
	int[] costResources;
	/** �Ƿ�����Ϊ����ֵ */
	boolean isFix=true;
	/** �Ƿ�������� */
	boolean isUpgr=true;
	/**��Ʒ����**/
	String equname;

	/** �������� */
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

	/** ��ȡ��ǰ���Ӿ���ʱ��Ҫ���ĵ�����ѫ�¸��� */
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
				//ѡ�����ȼ���ߵ�װ��
				Equipment equip=(Equipment)al.get(0);
				for(int k=1;k<al.size();k++){
					Equipment equipTemp=(Equipment)al.get(k);
					if(equip.getExp()>equipTemp.getExp())
						equip=equipTemp;
				}
				//���������Ƴ����ȵ�װ��
				al.remove(equip);
				//�Ƴ�װ��
				player.getEquips().removeEquip(equip.getUid());
			}
		}
		if(costResources!=null)
		Resources
			.reduceResources(player.getResources(),costResources,player);
	}

	/** ���ֽ������з����л���ö������ */
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

	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
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
