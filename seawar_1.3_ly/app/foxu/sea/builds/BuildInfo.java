package foxu.sea.builds;

import mustang.text.TextKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;

/**
 * 建筑信息 拥有的index 每个index对应的建筑物 author:icetiger
 */
public class BuildInfo
{
	/**
	 * 建筑位 铀矿只能在铀矿上 金币矿只有一个 其他3个资源可以随意 INDEX_0=指挥中心
	 * INDEX_1=金币,INDEX_2=研究院，INDEX_3=仓库，INDEX_4=仓库
	 * INDEX_5=制造车间，INDEX_6=联盟,INDEX_7=空军基地,INDEX_8=导弹基地,INDEX_9=火炮阵地,
	 * ,INDEX_10=船厂，INDEX_11=船厂，INDEX_12=船厂，INDEX_13=预留建筑位，INDEX_14=预留建筑位。
	 * index=15到index=44资源矿 index45到index49是铀矿
	 */
	public static final int INDEX_0=0,INDEX_1=1,INDEX_2=2,INDEX_3=3,
					INDEX_4=4,INDEX_5=5,INDEX_6=6,INDEX_7=7,INDEX_8=8,
					INDEX_9=9,INDEX_10=10,INDEX_11=11,INDEX_12=12,
					INDEX_13=13,INDEX_14=14;


	/** 是否拥有这个index */
	public static boolean isHaveIndex(int index,Player player)
	{
		// 指挥中心等级
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

	/** 对应位置是否可以建筑该类型建筑 */
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
