package foxu.sea.alliance;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;

/***
 * 旗帜
 * @author lihon 
 *
 */
public class Flag extends Sample
{
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	/**FLAGI_MAGE=1 旗帜图案 FLAG_COLOUR=2 旗帜颜色 FLAG_MODEL=3 旗帜造型**/
	public static int FLAGI_MAGE=1,FLAG_COLOUR=2,FLAG_MODEL=3;
	/**ALLIANCEFLAG=3 联盟旗帜的长度**/
	public static int ALLIANCEFLAG=3;
	/**联盟旗帜的标志**/
	public static int ALLIANCE_FLAG_TYEP=1;
	/* static methods */
	/** 从字节数组中反序列化获得对象的域 */
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
	
	/**类型**/
	int flagType;
	/**旗帜类型**/
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
