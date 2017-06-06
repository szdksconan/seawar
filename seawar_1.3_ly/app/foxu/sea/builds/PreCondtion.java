package foxu.sea.builds;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.util.Sample;
import mustang.util.SampleFactory;
import foxu.sea.Player;

/**
 * ǰ�������� author:icetiger
 */
public class PreCondtion extends Sample
{

	/** �������� */
	int buildType;
	/** �����ȼ� */
	int buildLevel;
	/** ��ҵȼ� */
	int playerLevel;

	/** �������� */
	public static SampleFactory factory=new SampleFactory();

	/** ���ֽ������з����л���ö������ */
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

	/** ��鷽�� ��������Ƿ�ﵽ ���ؿ�Ϊ�ﵽ */
	public String check(Player player)
	{
		String str=null;
		/** ��齨������ */
		boolean bool=false;
		/** ��齨���ȼ� */
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
			str="no buildType��"+this.buildType;
			return str;
		}
		/** ���ý������͵ĵȼ� */
		if(!boolLevel) str="buildLevel limite:"+this.buildLevel;
		if(playerLevel!=0&&playerLevel>player.getLevel())
			str="player level low";
		return str;
	}

	/** ��齨������ */
	public boolean checkBuildType(int buildType)
	{
		return buildType==this.buildType;
	}

	/** ��齨���ȼ� */
	public boolean checkBuildLevel(int buildLevel)
	{
		return buildLevel>=this.buildLevel;
	}
}
