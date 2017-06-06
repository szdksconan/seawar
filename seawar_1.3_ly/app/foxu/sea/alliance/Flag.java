package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/***
 * ����
 * @author lihon 
 *
 */
public class Flag extends Sample
{
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	/**FLAGI_MAGE=1 ����ͼ�� FLAG_COLOUR=2 ������ɫ FLAG_MODEL=3 ��������**/
	public static int FLAGI_MAGE=1,FLAG_COLOUR=2,FLAG_MODEL=3;
	/**ALLIANCEFLAG=3 �������ĵĳ���**/
	public static int ALLIANCEFLAG=3;
	/**�������ĵı�־**/
	public static int ALLIANCE_FLAG_TYEP=1;
	/* static methods */
	/** ���ֽ������з����л���ö������ */
	public static Flag bytesReadAllianceFlag(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		if(sid==0) return null;
		Flag f=(Flag)factory.newSample(sid);
		if(f==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Flag.class.getName()
					+" bytesRead, invalid sid:"+sid);
		f.bytesRead(data);
		return f;
	}
	
	/**����**/
	int flagType;
	/**��������**/
	int type;
	
	public int getFlagType()
	{
		return flagType;
	}
	
	public void setFlagType(int flagType)
	{
		this.flagType=flagType;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type=type;
	}
	
	
}
