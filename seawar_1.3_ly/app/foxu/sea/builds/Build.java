package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/**
 * ���� �����൱��ɾ��ԭ���� ���¼���һ���µ� �������õ��� author:icetiger
 */
public class Build extends Sample
{

	/**
	 * ������ BUILD_METAL=1 ������ BUILD_OIL=2ʯ�Ϳ�BUILD_SILION=3���
	 * BUILD_URANIUM=4�˿�BUILD_MONEY=5��ң�BUILD_SHIP=6������BUILD_DIRECTOR=7ָ������
	 * BUILD_RESEARCH=8�о�Ժ��BUILD_SHOP=9���쳵�䣬BUILD_STORE=10�ֿ�
	 * BUILD_AIR=11�վ�����,BUILD_MISSILE=12��������,BUILD_ARTILLERY=13�������,BUILD_SHIP_UPDATE=14��������
	 * STRENTGH_SHIP=15����ǿ��
	 */
	public static final int BUILD_METAL=1,BUILD_OIL=2,BUILD_SILION=3,
					BUILD_URANIUM=4,BUILD_MONEY=5,BUILD_SHIP=6,
					BUILD_DIRECTOR=7,BUILD_RESEARCH=8,BUILD_SHOP=9,
					BUILD_STORE=10,BUILD_AIR=11,BUILD_MISSILE=12,
					BUILD_ARTILLERY=13,BUILD_SHIP_UPDATE=14,BUILD_STRENTGH_SHIP=15;

	/** �������ȼ� ��������ȼ��й� */
//	public static final int BUILD_MAX_LEVEL=60;
	/** Ĭ�ϲֿ����� */
	public static final int DEFAULT_CAPACITY=5000;
	/** Serialization fileds */
	/** �����ȼ� */
	int buildLevel;

	/** configure fileds */
	/** �������� */
	String buildName;
	/** �������� */
	int buildType;
	/** �������� */
	String description;
	/** �Ƿ���Գ��� */
	boolean isRemove;
	/** ���ȼ� */
	int maxLevel;

	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/** ���ֽ������з����л���ö������ */
	public static Build bytesReadBuild(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Build r=(Build)factory.newSample(sid);
		if(r==null)
		{
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Build.class.getName()
					+" bytesRead, invalid sid:"+sid);
		}
		r.bytesRead(data);
		return r;
	}

	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		buildLevel=data.readUnsignedByte();
		return this;
	}

	/** ������������л����ֽڻ����� */
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeByte(buildLevel);
	}
	public void showBytesWrite(ByteBuffer data,int current)
	{
		super.bytesWrite(data);
		data.writeByte(buildLevel);
	}

	/**
	 * @return buildType
	 */
	public int getBuildType()
	{
		return buildType;
	}

	/**
	 * @param buildType Ҫ���õ� buildType
	 */
	public void setBuildType(int buildType)
	{
		this.buildType=buildType;
	}

	/**
	 * @return buildLevel
	 */
	public int getBuildLevel()
	{
		return buildLevel;
	}

	/**
	 * @param buildLevel Ҫ���õ� buildLevel
	 */
	public void setBuildLevel(int buildLevel)
	{
		this.buildLevel=buildLevel;
	}

	/**
	 * @return remove
	 */
	public boolean isRemove()
	{
		return isRemove;
	}

	/**
	 * @param remove Ҫ���õ� remove
	 */
	public void setRemove(boolean remove)
	{
		this.isRemove=remove;
	}

	/**
	 * @return buildName
	 */
	public String getBuildName()
	{
		return buildName;
	}

	/**
	 * @param buildName Ҫ���õ� buildName
	 */
	public void setBuildName(String buildName)
	{
		this.buildName=buildName;
	}

	/**
	 * @return maxLevel
	 */
	public int getMaxLevel()
	{
		return maxLevel;
	}

	/**
	 * @param maxLevel Ҫ���õ� maxLevel
	 */
	public void setMaxLevel(int maxLevel)
	{
		this.maxLevel=maxLevel;
	}
}
