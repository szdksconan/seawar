/**
 * 
 */
package foxu.sea.island;

import mustang.math.MathKit;
import mustang.set.IntKeyHashMap;
import foxu.sea.NpcIsland;

/**
 * 空岛屿
 * 
 * @author rockzyt
 */
public class SpaceIslandContainer
{

	/* static fields */
	public static final int PROPORTION=1000;
	// /** 区域配置工厂 */
	// public static final SampleFactory facotry=new SampleFactory();

	/* fields */
	/** 岛屿分布区域配置信息 */
	AreaConfigure[] ac;
	/** 空岛屿表 */
	IntKeyHashMap islandMap=new IntKeyHashMap();
	/** 区域密度数组 */
	float[] density;

	/* properties */
	/** 设置岛屿分布区域配置信息 */
	public void setAreaConfigure(AreaConfigure[] ac)
	{
		this.ac=ac;
	}

	/* methods */
	public void init()
	{
		density=new float[ac.length];
	}

	/** 全随即一个岛屿 */
	public synchronized NpcIsland randomIsLand()
	{
		Object[] object=islandMap.valueArray();
		for(int i=0;i<object.length;i++)
		{
			int random=MathKit.randomValue(0,object.length-1);
			// System.out.println("random========="+random);
			NpcIsland npcIsland=(NpcIsland)object[random];
			if(npcIsland==null||npcIsland.getTempAttackEventId()!=0
				||npcIsland.getPlayerId()!=0
				||npcIsland.getIslandType()!=NpcIsland.ISLAND_WARTER)
				continue;
			return npcIsland;
		}
		return null;
	}

	public void initDensity()
	{
		for(int i=ac.length-1;i>=0;i--)
		{
			// System.out.println(">>>>>>>::"+density[i]+"
			// "+getIslandCount(i)
			// +" "+(density[i]*PROPORTION/getIslandCount(i)));
			density[i]=density[i]*PROPORTION/getIslandCount(i);
		}
	}
	/** 添加岛屿 */
	public void addIsLand(NpcIsland island)
	{
		if(island==null) return;
		if(isSpace(island))
		{
			islandMap.put(island.getIndex(),island);
		}
		else
		{
			// System.out.println("island="+island.getIndex()
			// +"island.getPlayerId()="+island.getPlayerId()
			// +" island.getIslandType()="+island.getIslandType()
			// +"island.getTempAttackEventId()="
			// +island.getTempAttackEventId());
			int x=island.getIndex()%NpcIsland.WORLD_WIDTH,y=island
				.getIndex()
				/NpcIsland.WORLD_WIDTH;
			for(int i=0;i<ac.length;i++)
			{
				if(x>=ac[i].leftTop[0]&&x<=ac[i].rightBotton[0]
					&&y>ac[i].leftTop[1]&&y<ac[i].rightBotton[1])
				{
					// 先用density来缓存当前容积,再根据最大容积计算出当前密度
					density[i]++;
					break;
				}
			}
		}
	}
	/**
	 * 判断是否是空岛屿(忽略被玩家侵略的情况)
	 * 
	 * @return 返回true表示是空岛屿
	 */
	public boolean isSpace(NpcIsland island)
	{
		if(island==null) return false;
		if(island.getTempAttackEventId()!=0) return false;
		// System.out.println("island="+island.getIndex()
		// +"island.getPlayerId()="+island.getPlayerId()
		// +" island.getIslandType()="+island.getIslandType());
		return island.getPlayerId()==0&&island.getTempAttackEventId()==0
			&&//(island.getIslandType()==NpcIsland.ISLAND_METAL
				//||island.getIslandType()==NpcIsland.ISLAND_MONEY
				//||island.getIslandType()==NpcIsland.ISLAND_OIL
				//||island.getIslandType()==NpcIsland.ISLAND_SILION
				//||island.getIslandType()==NpcIsland.ISLAND_URANIUM||
				island.getIslandType()==NpcIsland.ISLAND_WARTER;
	}
	/**
	 * 根据玩家的id随机一个空岛出来
	 * 
	 * @return 返回找到的空岛屿,返回null表示没有空岛屿
	 */
	public synchronized NpcIsland getRandomSpace()
	{
		if(islandMap.size()==0) return null;
		// int rd;
		NpcIsland island;
		for(int i=0;i<ac.length;i++)
		{
			// System.out.println("density[i]="+density[i]+"
			// ac[i].maxVolume="
			// +ac[i].maxVolume);
			if(density[i]<ac[i].maxVolume)
			{
				// rd=MathKit.randomValue(0,PROPORTION);
				// System.out.println("rd="+rd);
				// if(rd>density[i])
				// {
				island=randomByIndex(i);
				if(island==null) continue;
				return island;
				// }
			}
		}
		int[] keys=islandMap.keyArray();
		if(keys.length>0)
			return (NpcIsland)islandMap.get(keys[MathKit.randomValue(0,
				keys.length)]);
		return null;
	}
	/**
	 * 根据区域下标,在指定的区域内随机出一个空岛屿(现在区域配置方式为大区域嵌套小区域)
	 * 
	 * @param index 区域下标
	 * @return 返回随机到的岛屿.返回负数表示没找到.返回的岛屿被线程锁锁定,外部使用完以后必须解锁
	 */
	private NpcIsland randomByIndex(int index)
	{
		AreaConfigure ac=this.ac[index];
		int rdCount=(int)((PROPORTION-density[index])/(PROPORTION/10)); // 计算随机次数=密度/100
		rdCount=rdCount>=10?rdCount*2:rdCount; // 如果随机次数为10次,说明这个区域基本为空区域,放大随机次数,尽量在这个区域找到一个可用岛屿
		// System.out.println("rdCount==================="+rdCount);
		int x,y,islandKey;
		NpcIsland island=null;
		if(index==0)
		{
			for(int i=rdCount-1;i>=0;i--)
			{
				x=MathKit.randomValue(ac.leftTop[0],ac.rightBotton[0]+1);// 配置为0-x,需要x+1才能随机到x
				y=MathKit.randomValue(ac.leftTop[1],ac.rightBotton[1]+1);
				islandKey=y*NpcIsland.WORLD_WIDTH+x;
				island=(NpcIsland)islandMap.get(islandKey);
				if(island!=null&&isSpace(island))// 预防出现bug的情况,不为空岛屿又存在于空岛屿列表中
				{
					return island;
				}
				else
				{
					removeSpaceIsland(island);
				}
			}
		}
		else
		{
			AreaConfigure acc=this.ac[index-1];
			// 为了预防配置出错,这里先计算两个区域4边的差值
			int x1=acc.leftTop[0]-ac.leftTop[0];
			int x2=ac.rightBotton[0]-acc.rightBotton[0];
			int y1=acc.leftTop[1]-ac.leftTop[0];
			int y2=ac.rightBotton[1]-acc.rightBotton[1];
			int offset=0;
			int rdDirect=0;// 随机方向
			for(int i=rdCount-1;i>=0;i--)
			{
				rdDirect=MathKit.randomValue(0,4);
				switch(rdDirect)
				{
					case 0:
						// 左
						offset=MathKit.randomValue(0,x1);
						x=ac.leftTop[0]+offset;
						y=MathKit.randomValue(ac.leftTop[1],
							ac.rightBotton[1]);
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// 预防出现bug的情况,不为空岛屿又存在于空岛屿列表中
						{
							return island;
						}
						else
						{
							removeSpaceIsland(island);
						}
						continue;
					case 1:
						// 上
						offset=MathKit.randomValue(0,y1);
						x=MathKit.randomValue(ac.leftTop[0],
							ac.rightBotton[0]);
						y=ac.leftTop[1]+offset;
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// 预防出现bug的情况,不为空岛屿又存在于空岛屿列表中
						{
							return island;
						}
						else
						{
							removeSpaceIsland(island);
						}
						continue;
					case 2:
						// 右
						offset=MathKit.randomValue(1,x2+1);// 排除里层矩形的下边,囊括外层矩形下边
						x=ac.rightBotton[0]-offset;
						y=MathKit.randomValue(ac.leftTop[1],
							ac.rightBotton[1]);
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// 预防出现bug的情况,不为空岛屿又存在于空岛屿列表中
						{
							return island;
						}
						else
						{
							removeSpaceIsland(island);
						}
						continue;
					case 3:
						// 下
						offset=MathKit.randomValue(1,y2+1);// 排除里层矩形的下边,囊括外层矩形下边
						x=MathKit.randomValue(ac.leftTop[0],
							ac.rightBotton[0]);
						y=ac.rightBotton[1]-offset;
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// 预防出现bug的情况,不为空岛屿又存在于空岛屿列表中
						{
							return island;
						}
						else
						{
							removeSpaceIsland(island);
						}
						continue;
					default:
						break;
				}
			}
		}
		return null;
	}
	/** 移除一个岛屿,重新计算这个岛屿所在区域的密度 */
	public synchronized void removeSpaceIsland(NpcIsland island)
	{
		if(island==null) return;
		if(islandMap.remove(island.getIndex())!=null)
		{
			int x=island.getIndex()%NpcIsland.WORLD_WIDTH,y=island
				.getIndex()
				/NpcIsland.WORLD_WIDTH;
			int islandCount;
			for(int i=0;i<ac.length;i++)
			{
				if(x>=ac[i].leftTop[0]&&x<ac[i].rightBotton[0]
					&&y>=ac[i].leftTop[1]&&y<ac[i].rightBotton[1])
				{
					islandCount=getIslandCount(i);
					float d=islandCount*density[i]/PROPORTION; // 已使用岛屿数量
					// System.out.println("当前区域岛屿数量:"+d+" 当前区域"+i);
					// d=--d>0?d:0;
					++d;
					density[i]=d*PROPORTION/islandCount;
					// System.out.println("当前区域岛屿密度:"+density[i]);
					break;
				}
			}
		}
	}
	/** 添加一个空岛屿,重新计算这个岛屿所在区域的密度 */
	public synchronized void putSpaceIsland(NpcIsland island)
	{
		if(island==null) return;
		if(isSpace(island))
		{
			int x=island.getIndex()%NpcIsland.WORLD_WIDTH,y=island
				.getIndex()
				/NpcIsland.WORLD_WIDTH;
			int islandCount;
			for(int i=0;i<ac.length;i++)
			{
				if(x>=ac[i].leftTop[0]&&x<ac[i].rightBotton[0]
					&&y>=ac[i].leftTop[1]&&y<ac[i].rightBotton[1])
				{
					islandCount=getIslandCount(i);
					float d=islandCount*density[i]/PROPORTION;
					// ++d;
					d=--d>0?d:0;
					density[i]=d*PROPORTION/islandCount;
					break;
				}
			}
		}
	}
	private int getIslandCount(int i)
	{
		if(i>0)
		{
			return (ac[i].rightBotton[0]-ac[i].leftTop[0])
				*(ac[i].rightBotton[1]-ac[i].leftTop[1])
				-(ac[i-1].rightBotton[0]-ac[i-1].leftTop[0])
				*(ac[i-1].rightBotton[1]-ac[i-1].leftTop[1]);
		}
		else
		{
			return (ac[i].rightBotton[0]-ac[i].leftTop[0])
				*(ac[i].rightBotton[1]-ac[i].leftTop[1]);
		}
	}
}