package foxu.sea.builds;

import mustang.text.TextKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;

/**
 * ������Ϣ ӵ�е�index ÿ��index��Ӧ�Ľ����� author:icetiger
 */
public class BuildInfo
{
	/**
	 * ����λ �˿�ֻ�����˿��� ��ҿ�ֻ��һ�� ����3����Դ�������� INDEX_0=ָ������
	 * INDEX_1=���,INDEX_2=�о�Ժ��INDEX_3=�ֿ⣬INDEX_4=�ֿ�
	 * INDEX_5=���쳵�䣬INDEX_6=����,INDEX_7=�վ�����,INDEX_8=��������,INDEX_9=�������,
	 * ,INDEX_10=������INDEX_11=������INDEX_12=������INDEX_13=Ԥ������λ��INDEX_14=Ԥ������λ��
	 * index=15��index=44��Դ�� index45��index49���˿�
	 */
	public static final int INDEX_0=0,INDEX_1=1,INDEX_2=2,INDEX_3=3,
					INDEX_4=4,INDEX_5=5,INDEX_6=6,INDEX_7=7,INDEX_8=8,
					INDEX_9=9,INDEX_10=10,INDEX_11=11,INDEX_12=12,
					INDEX_13=13,INDEX_14=14;


	/** �Ƿ�ӵ�����index */
	public static boolean isHaveIndex(int index,Player player)
	{
		// ָ�����ĵȼ�
		int directorLevel=player.getIsland().getBuildByIndex(INDEX_0,null)
			.getBuildLevel();
		for(int i=0;i<PublicConst.INDEX_0_LEVEL_OPEN_INDEX.length;i++)
		{
			String indexs[]=TextKit.split(PublicConst.INDEX_0_LEVEL_OPEN_INDEX[i],":");
			if(directorLevel<Integer.parseInt(indexs[0])) break;
			for(int j=1;j<indexs.length;j++)
			{
				if(index==Integer.parseInt(indexs[j]))
				{
					return true;
				}
			}
		}
		return false;
	}

	/** ��Ӧλ���Ƿ���Խ��������ͽ��� */
	public static boolean isBuildThisType(String buildType,int index)
	{
		String str=PublicConst.INDEX_FOR_BUILD_TYPE[index];
		String types[]=TextKit.split(str,":");
		for(int i=0;i<types.length;i++)
		{
			if(types[i].equals(buildType))
			{
				return true;
			}
		}
		return false;
	}
}
