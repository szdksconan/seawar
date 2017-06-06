/**
 * 
 */
package foxu.sea;

import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;

/**
 * ��������ֵ����
 * 
 * @author rockzyt
 */
public class AttrAdjustment
{

	/* fields */
	/** ����ֵ����,kΪ���Ե�type,vΪ����ֵ���� */
	IntKeyHashMap dataMap=new IntKeyHashMap();

	/* methods */
	/** ������� */
	public void clear()
	{
		dataMap.clear();
	}
	/** ���ָ�����޸�ֵ���޸�ֵ���� */
	public Object getAdjustmentValue(int k)
	{
		return dataMap.get(k);
	}
	/** ���ָ�����޸�ֵ */
	public AdjustmentData getAdjustmentValue(int k,int k1)
	{
		IntKeyHashMap map=(IntKeyHashMap)getAdjustmentValue(k);
		if(map==null)return null;
		return (AdjustmentData)map.get(k1);
	}
	/**
	 * ����һ������ֵ,�̶���
	 * 
	 * @param k
	 * @param v
	 */
	public void add(int k,int v,boolean isFix)
	{
		add(k,v,isFix,dataMap);
	}
	public void add(int k1,int k2,int v,boolean isFix)
	{
		IntKeyHashMap map=(IntKeyHashMap)dataMap.get(k1);
		if(map==null)
		{
			map=new IntKeyHashMap();
			dataMap.put(k1,map);
		}
		add(k2,v,isFix,map);
	}
	private void add(int k,int v,boolean isFix,IntKeyHashMap map)
	{
		AdjustmentData data=(AdjustmentData)map.get(k);
		if(data==null)
		{
			data=new AdjustmentData();
			map.put(k,data);
		}
		if(isFix)
		{
			data.fix+=v;
		}
		else
		{
			data.percent+=v;
		}
	}
	/** ������л� д*/
	public void crossBytesWrite(ByteBuffer data)
	{
		int[] keys={Ship.BATTLE_SHIP,Ship.SUBMARINE_SHIP,Ship.CRUISER_SHIP,
			Ship.AIRCRAFT_SHIP,Ship.POSITION_AIR,Ship.POSITION_MISSILE,
			Ship.POSITION_FIRE};

		data.writeShort(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			data.writeShort(keys[i]);
			IntKeyHashMap map=(IntKeyHashMap)dataMap.get(keys[i]);
			if(map==null||map.size()==0)
			{
				data.writeShort(0);
				continue;
			}
			else
			{
				int[] in_keys=map.keyArray();
				data.writeShort(in_keys.length);
				for(int k=0;k<in_keys.length;k++)
				{
					AdjustmentData adj=(AdjustmentData)map.get(in_keys[k]);
					data.writeShort(in_keys[k]);
					data.writeShort(adj.fix);
					data.writeShort(adj.percent);
				}
			}

		}

	}
	/** ������л� ��*/
	public void crossBytesRead(ByteBuffer data)
	{
		clear();
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			int key=data.readUnsignedShort();
			int in_len=data.readUnsignedShort();
			for(int k=0;k<in_len;k++)
			{
				int in_key=data.readUnsignedShort();
				add(key,in_key,data.readUnsignedShort(),true);
				add(key,in_key,data.readUnsignedShort(),false);
			}

		}

	}

	/* inner class */
	public class AdjustmentData
	{

		/* fields */
		public int fix;
		public int percent;
	}
}