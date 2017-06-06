/**
 * 
 */
package foxu.sea.island;

import mustang.math.MathKit;
import mustang.set.IntKeyHashMap;
import foxu.sea.NpcIsland;

/**
 * �յ���
 * 
 * @author rockzyt
 */
public class SpaceIslandContainer
{

	/* static fields */
	public static final int PROPORTION=1000;
	// /** �������ù��� */
	// public static final SampleFactory facotry=new SampleFactory();

	/* fields */
	/** ����ֲ�����������Ϣ */
	AreaConfigure[] ac;
	/** �յ���� */
	IntKeyHashMap islandMap=new IntKeyHashMap();
	/** �����ܶ����� */
	float[] density;

	/* properties */
	/** ���õ���ֲ�����������Ϣ */
	public void setAreaConfigure(AreaConfigure[] ac)
	{
		this.ac=ac;
	}

	/* methods */
	public void init()
	{
		density=new float[ac.length];
	}

	/** ȫ�漴һ������ */
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
	/** ��ӵ��� */
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
					// ����density�����浱ǰ�ݻ�,�ٸ�������ݻ��������ǰ�ܶ�
					density[i]++;
					break;
				}
			}
		}
	}
	/**
	 * �ж��Ƿ��ǿյ���(���Ա�������Ե����)
	 * 
	 * @return ����true��ʾ�ǿյ���
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
	 * ������ҵ�id���һ���յ�����
	 * 
	 * @return �����ҵ��Ŀյ���,����null��ʾû�пյ���
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
	 * ���������±�,��ָ���������������һ���յ���(�����������÷�ʽΪ������Ƕ��С����)
	 * 
	 * @param index �����±�
	 * @return ����������ĵ���.���ظ�����ʾû�ҵ�.���صĵ��챻�߳�������,�ⲿʹ�����Ժ�������
	 */
	private NpcIsland randomByIndex(int index)
	{
		AreaConfigure ac=this.ac[index];
		int rdCount=(int)((PROPORTION-density[index])/(PROPORTION/10)); // �����������=�ܶ�/100
		rdCount=rdCount>=10?rdCount*2:rdCount; // ����������Ϊ10��,˵������������Ϊ������,�Ŵ��������,��������������ҵ�һ�����õ���
		// System.out.println("rdCount==================="+rdCount);
		int x,y,islandKey;
		NpcIsland island=null;
		if(index==0)
		{
			for(int i=rdCount-1;i>=0;i--)
			{
				x=MathKit.randomValue(ac.leftTop[0],ac.rightBotton[0]+1);// ����Ϊ0-x,��Ҫx+1���������x
				y=MathKit.randomValue(ac.leftTop[1],ac.rightBotton[1]+1);
				islandKey=y*NpcIsland.WORLD_WIDTH+x;
				island=(NpcIsland)islandMap.get(islandKey);
				if(island!=null&&isSpace(island))// Ԥ������bug�����,��Ϊ�յ����ִ����ڿյ����б���
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
			// Ϊ��Ԥ�����ó���,�����ȼ�����������4�ߵĲ�ֵ
			int x1=acc.leftTop[0]-ac.leftTop[0];
			int x2=ac.rightBotton[0]-acc.rightBotton[0];
			int y1=acc.leftTop[1]-ac.leftTop[0];
			int y2=ac.rightBotton[1]-acc.rightBotton[1];
			int offset=0;
			int rdDirect=0;// �������
			for(int i=rdCount-1;i>=0;i--)
			{
				rdDirect=MathKit.randomValue(0,4);
				switch(rdDirect)
				{
					case 0:
						// ��
						offset=MathKit.randomValue(0,x1);
						x=ac.leftTop[0]+offset;
						y=MathKit.randomValue(ac.leftTop[1],
							ac.rightBotton[1]);
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// Ԥ������bug�����,��Ϊ�յ����ִ����ڿյ����б���
						{
							return island;
						}
						else
						{
							removeSpaceIsland(island);
						}
						continue;
					case 1:
						// ��
						offset=MathKit.randomValue(0,y1);
						x=MathKit.randomValue(ac.leftTop[0],
							ac.rightBotton[0]);
						y=ac.leftTop[1]+offset;
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// Ԥ������bug�����,��Ϊ�յ����ִ����ڿյ����б���
						{
							return island;
						}
						else
						{
							removeSpaceIsland(island);
						}
						continue;
					case 2:
						// ��
						offset=MathKit.randomValue(1,x2+1);// �ų������ε��±�,�����������±�
						x=ac.rightBotton[0]-offset;
						y=MathKit.randomValue(ac.leftTop[1],
							ac.rightBotton[1]);
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// Ԥ������bug�����,��Ϊ�յ����ִ����ڿյ����б���
						{
							return island;
						}
						else
						{
							removeSpaceIsland(island);
						}
						continue;
					case 3:
						// ��
						offset=MathKit.randomValue(1,y2+1);// �ų������ε��±�,�����������±�
						x=MathKit.randomValue(ac.leftTop[0],
							ac.rightBotton[0]);
						y=ac.rightBotton[1]-offset;
						islandKey=y*NpcIsland.WORLD_WIDTH+x;
						island=(NpcIsland)islandMap.get(islandKey);
						if(island!=null&&isSpace(island))// Ԥ������bug�����,��Ϊ�յ����ִ����ڿյ����б���
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
	/** �Ƴ�һ������,���¼��������������������ܶ� */
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
					float d=islandCount*density[i]/PROPORTION; // ��ʹ�õ�������
					// System.out.println("��ǰ����������:"+d+" ��ǰ����"+i);
					// d=--d>0?d:0;
					++d;
					density[i]=d*PROPORTION/islandCount;
					// System.out.println("��ǰ�������ܶ�:"+density[i]);
					break;
				}
			}
		}
	}
	/** ���һ���յ���,���¼��������������������ܶ� */
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