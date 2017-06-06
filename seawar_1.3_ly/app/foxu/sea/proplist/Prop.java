package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.builds.Produceable;
import foxu.sea.builds.Product;

/**
 * Coyyright 2001 by seasky<www.seasky.cn>.
 */

/**
 * ��˵������Ʒ��
 * 
 * @version 1.0
 * @author zminleo<zmin@seasky.cn>
 */
public class Prop extends Sample implements Produceable
{
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	
	/** ��Ʒ���ͣ�  VALID��Ч PROP��Ʒ  EQUIPװ��  SHIP��ֻ OFFICER����*/
	public static int VALID=0,PROP=1,EQUIP=2,SHIP=3,OFFICER=4;
	
	/* static methods */
	/** ���ֽ������з����л���ö������ */
	public static Prop bytesReadProp(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		if(sid==0) return null;
		Prop p=(Prop)factory.newSample(sid);
		if(p==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Prop.class.getName()
					+" bytesRead, invalid sid:"+sid);
		p.bytesRead(data);
		return p;
	}

	/* fields */
	/** �������� */
	int costResources[];
	/** ��Ҫ���ĵ���Ʒsid,num,sid,num */
	int costPropSid[];
	/** ÿ����Ʒ����ʱ�� */
	int buildTime;
	/** ������ȡ�ľ��� */
	int buildExperience;
	/** ���� */
	String name="";
	/** ���� */
	String pname="";
	/** ��Ʊ */
	int price;
	/** �ȼ� */
	int level;
	// /** ��Ʒ��λ */
	String unit;
	/** ��ʽ */
	String style;
	/** ʹ����Ч */
	String effects;
	/** ���� */
	String description;
	/** ��ʱ�Ե�������Ϣ(����Ҫͨ��) */
	String tempDescription=null;
	/** ��ʾ���� */
	Object show;
	/** ��Ʒ������ID */
	int sortId=0;
	/** ������Ҫ��ʯ���� */
	int needGems;
	/** ʹ�õȼ����� */
	int limitLevel;
	/** �����̵� ����������˻��� */
	int needAllianceScore;
	/** ÿ�����һ����� */
	int maxExchangeNum;
	/**2�����Ҷһ��۸�**/
	int  coins;

	/* constructors */
	/** ���������Ĺ��췽�� */
	protected Prop()
	{
	}
	/* properties */
	/** ������Ʒ���� */
	public void setName(String name)
	{
		this.name=name;
	}

	/** ������� */
	public String getName()
	{
		return name;
	}
	/** ������Ʒ�ȼ� */
	public void setLevel(int level)
	{
		this.level=level;
	}
	/** ��õȼ� */
	public int getLevel()
	{
		return level;
	}
	/** ������Ʒ�۸� */
	public void setPrice(int price)
	{
		this.price=price;
	}
	/** ��ü۸� */
	public int getPrice()
	{
		return price;
	}
	// /** ������Ʒ��λ */
	// public void setUnit(String unit)
	// {
	// this.unit=unit;
	// }
	// /** ��õ�λ */
	// public String getUnit()
	// {
	// return unit;
	// }
	/** ������ʽ */
	public void setStyle(String style)
	{
		this.style=style;
	}
	/** �����ʽ */
	public String getStyle()
	{
		return style;
	}
	/** �����Ч */
	public String getEffects()
	{
		return effects;
	}
	/** ������Ч */
	public void setEffects(String effects)
	{
		this.effects=effects;
	}
	/** ������� */
	public String getDescription()
	{
		return description;
	}
	/** �����ʱ������Ϣ */
	public String getTempDescription()
	{
		return tempDescription;
	}
	/** ������ʱ������Ϣ */
	public void setTempDescription(String tempDescription)
	{
		this.tempDescription=tempDescription;
	}
	/** �����ʾ���� */
	public Object getShow()
	{
		return show;
	}
	/** ������ʾ���� */
	public void setShow(Object obj)
	{
		show=obj;
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
	}
	/* common methods */
	public String toString()
	{
		return "sid="+getSid()+",id="+getId()+",name="+name;
	}

	public int getSortId()
	{
		return sortId;
	}
	public void setSortId(int sortId)
	{
		this.sortId=sortId;
	}

	/**
	 * @return resources
	 */
	public int[] getCostResources()
	{
		return costResources;
	}

	/**
	 * @param resources Ҫ���õ� resources
	 */
	public void setCostResources(int[] resources)
	{
		this.costResources=resources;
	}

	/**
	 * @return buildExperience
	 */
	public int getBuildExperience()
	{
		return buildExperience;
	}

	/**
	 * @param buildExperience Ҫ���õ� buildExperience
	 */
	public void setBuildExperience(int buildExperience)
	{
		this.buildExperience=buildExperience;
	}

	/**
	 * @return buildTime
	 */
	public int getBuildTime()
	{
		return buildTime;
	}

	/**
	 * @param buildTime Ҫ���õ� buildTime
	 */
	public void setBuildTime(int buildTime)
	{
		this.buildTime=buildTime;
	}

	/**
	 * @return costPropSid
	 */
	public int[] getCostPropSid()
	{
		return costPropSid;
	}

	/**
	 * @param costPropSid Ҫ���õ� costPropSid
	 */
	public void setCostPropSid(int[] costPropSid)
	{
		this.costPropSid=costPropSid;
	}
	public void cancel(Player player,Product product)
	{
	}
	public void finish(Player player,Product product,CreatObjectFactory objectFactory)
	{
	}

	/**
	 * @return needGems
	 */
	public int getNeedGems()
	{
		return needGems;
	}

	/**
	 * @param needGems Ҫ���õ� needGems
	 */
	public void setNeedGems(int needGems)
	{
		this.needGems=needGems;
	}
	public void cancelUp(Player player,Product product,CreatObjectFactory objectFactory)
	{
		// TODO �Զ����ɷ������
		
	}
	
//	public String getPname()
//	{
//		return pname;
//	}
	
	public void setPname(String pname)
	{
		this.pname=pname;
	}
	
	public int getLimitLevel()
	{
		return limitLevel;
	}
	
	public void setLimitLevel(int limitLevel)
	{
		this.limitLevel=limitLevel;
	}
	
	public int getNeedAllianceScore()
	{
		return needAllianceScore;
	}
	
	public void setNeedAllianceScore(int needAllianceScore)
	{
		this.needAllianceScore=needAllianceScore;
	}
	
	public int getMaxExchangeNum()
	{
		return maxExchangeNum;
	}
	
	public void setMaxExchangeNum(int maxExchangeNum)
	{
		this.maxExchangeNum=maxExchangeNum;
	}
	
	public int getCoins()
	{
		return coins;
	}
	
	public void setCoins(int coins)
	{
		this.coins=coins;
	}

}