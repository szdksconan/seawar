package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import foxu.sea.Player;

/**
 * 前提条件类 author:icetiger
 */
public class PreCondtion extends Sample
{

	/** 建筑类型 */
	int buildType;
	/** 建筑等级 */
	int buildLevel;
	/** 玩家等级 */
	int playerLevel;

	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();

	/** 从字节数组中反序列化获得对象的域 */
	public static Build bytesReadRole(ByteBuffer data)
	{
		int sid=data.readUnsignedShort();
		Build r=(Build)factory.newSample(sid);
		if(r==null)
			throw new DataAccessException(
				DataAccessException.CLIENT_SDATA_ERROR,Build.class.getName()
					+" bytesRead, invalid sid:"+sid);
		r.bytesRead(data);
		return r;
	}

	/** 检查方法 检查条件是否达到 返回空为达到 */
	public String check(Player player)
	{
		String str=null;
		/** 检查建筑类型 */
		boolean bool=false;
		/** 检查建筑等级 */
		boolean boolLevel=false;
		Object[] builds=player.getIsland().getBuildArray();
		if(this.buildType!=0)
		{
			for(int i=0;i<builds.length;i++)
			{
				if(((PlayerBuild)builds[i]).getBuildType()==this.buildType)
				{
					bool=true;
					if(checkBuildLevel(((PlayerBuild)builds[i])
						.getBuildLevel()-1)) boolLevel=true;
				}
			}
		}
		else
			bool=true;
		if(!bool)
		{
			str="no buildType："+this.buildType;
			return str;
		}
		/** 检查该建筑类型的等级 */
		if(!boolLevel) str="buildLevel limite:"+this.buildLevel;
		if(playerLevel!=0&&playerLevel>player.getLevel())
			str="player level low";
		return str;
	}

	/** 检查建筑类型 */
	public boolean checkBuildType(int buildType)
	{
		return buildType==this.buildType;
	}

	/** 检查建筑等级 */
	public boolean checkBuildLevel(int buildLevel)
	{
		return buildLevel>=this.buildLevel;
	}
}
