package foxu.sea.achieve;

import foxu.sea.Player;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.ObjectArray;
import mustang.util.Sample;

/**
 * �ɾʹ洢����
 * 
 * @author yw
 * 
 */
public class AchieveData
{

	/** ����key-���� */
	IntKeyHashMap key_attr=new IntKeyHashMap();
	/** �ɾ�SID-���� */
	IntKeyHashMap achive_progress=new IntKeyHashMap();

	AchieveComparator com=new AchieveComparator();

	/** ����player */
	Player player;

	/** �ı����� */
	public boolean changeAttrValue(int key,long addValue)
	{
		if(key>=Achievement.ATR_TYPE2)
		{
			return addAchieveValue(key,addValue);
		}
		else if(key<=Achievement.ATR_TYPE1)
		{
			return setAchieveValueOnly(key,addValue);
		}
		else
		{
			return setAchieveValue(key,addValue);
		}
	}
	/** ���ӳɾ� ����ֵ ���ۼӣ� */
	public boolean addAchieveValue(int key,long addValue)
	{
		long real_old=getAchieveValue(key);
		if(real_old==Long.MAX_VALUE) return false;
		long now=real_old+addValue;
		if(now<0) now=Long.MAX_VALUE;
		key_attr.put(key,new Long(now));
		return true;
	}
	/** ���óɾ� ����ֵ������ֵ��ԭ��ֵ�� */
	public boolean setAchieveValue(int key,long setValue)
	{
		long real_old=getAchieveValue(key);
		if(setValue<=real_old) return false;
		key_attr.put(key,new Long(setValue));
		return true;
	}

	/** ���óɾ� ����ֵ������ֵ>=0�� */
	public boolean setAchieveValueOnly(int key,long setValue)
	{
		if(setValue<0) return false;
		long ov=getAchieveValue(key);
		if(ov!=setValue)
		{
			key_attr.put(key,new Long(setValue));
			return true;
		}
		return false;
	}

	/** ��ȡ�ɾ�����ֵ */
	public long getAchieveValue(int key)
	{
		Long old=(Long)key_attr.get(key);
		return old==null?0:old;
	}
	/**
	 * �ƶ��ɾͽ���
	 * 
	 * @param sid
	 * @param max
	 * @return ��ǰ����
	 */
	public int addAchieveProgress(int sid,int max,Player player)
	{
		int real_old=getAchieveProgress(sid);
		if(real_old>=max) return 0;
		Achievement am=(Achievement)Achievement.factory.getSample(sid);
		player.addAchieveScore(am.getAddScore(real_old));
		real_old++;
		achive_progress.put(sid,real_old);
		return real_old;
	}

	/** ��ȡ��ǰ�ɾͽ��� */
	public int getAchieveProgress(int sid)
	{
		Integer old=(Integer)achive_progress.get(sid);
		return old==null?0:old;
	}
	/** ����ֵ���� */
	public void clearAttr(int atrKey)
	{
		key_attr.put(atrKey,new Long(0));
	}

	/** ���л� */
	public void bytesWrite(ByteBuffer data)
	{
		int[] keys=key_attr.keyArray();
		data.writeShort(keys.length);
		for(int i=keys.length-1;i>=0;i--)
		{
			data.writeShort(keys[i]);
			data.writeLong((Long)key_attr.get(keys[i]));
		}

		keys=achive_progress.keyArray();
		data.writeShort(keys.length);
		for(int i=keys.length-1;i>=0;i--)
		{
			data.writeShort(keys[i]);
			data.writeByte((Integer)achive_progress.get(keys[i]));
		}

	}
	/** �����л� */
	public void bytesRead(ByteBuffer data)
	{
		int len=data.readUnsignedShort();
		for(int i=len-1;i>=0;i--)
		{
			int key=data.readUnsignedShort();
			if(player.isResToLong())
			{
				key_attr.put(key,new Long(data.readLong()));
			}
			else
			{
				key_attr.put(key,new Long(data.readInt()));
			}
		}

		len=data.readUnsignedShort();
		for(int i=len-1;i>=0;i--)
		{
			achive_progress.put(data.readUnsignedShort(),
				data.readUnsignedByte());
		}

	}
	/** ��ʾ���л� */
	public void showBytesWrite(ByteBuffer data,Player player)
	{
		sortBytesWrite(data,Achievement.BASE);
		sortBytesWrite(data,Achievement.ARMY);
		sortBytesWrite(data,Achievement.HONOR);
		// sortBytesWrite(data,Achievement.OTHER);
		data.writeInt(player.getAchieveScore());
		data.writeInt(AchieveManager.instance.getMaxScore());
		data.writeInt(player.getAchieveScoreRank());
	}

	/** �������л� */
	public void sortBytesWrite(ByteBuffer data,int baseType)
	{
		ObjectArray complete_list=new ObjectArray();
		ObjectArray finish_list=new ObjectArray();
		ObjectArray doing_list=new ObjectArray();

		Sample[] samples=Achievement.factory.getSamples();
		for(int i=samples.length-1;i>=0;i--)
		{
			if(samples[i]==null) continue;
			Achievement achieve=(Achievement)samples[i];
			if(achieve.baseType!=baseType) continue;
			int progress=getAchieveProgress(achieve.getSid());
			long cvalue=getAchieveValue(achieve.atrKey);
			int nvalue=achieve.getNeedValue(progress);
			if(progress>=achieve.getNeedValue().length)
			{
				complete_list.add(achieve);
			}
			else if(cvalue>=nvalue)
			{
				finish_list.add(achieve);
			}
			else
			{
				doing_list.add(achieve);
			}
		}
		finish_list.sort(com,false);
		complete_list.sort(com,false);
		doing_list.sort(com,false);

		data.writeByte(baseType);
		data.writeShort(finish_list.size());
		showWritesList(finish_list,data);
		data.writeShort(doing_list.size());
		showWritesList(doing_list,data);
		data.writeShort(complete_list.size());
		showWritesList(complete_list,data);

	}
	public void showWritesList(ObjectArray list,ByteBuffer data)
	{
		for(int i=0;i<list.size();i++)
		{
			Achievement achieve=(Achievement)list.toArray()[i];
			data.writeShort(achieve.getSid());
			int progress=getAchieveProgress(achieve.getSid());
			data.writeByte(progress);
			data.writeLong(getAchieveValue(achieve.atrKey));

		}
	}
	public int computeScore()
	{
		int[] sids=achive_progress.keyArray();
		int score=0;
		for(int i=0;i<sids.length;i++)
		{
			int pg=getAchieveProgress(sids[i]);
			long cv=getAchieveValue(sids[i]);
			Achievement am=(Achievement)Achievement.factory
				.getSample(sids[i]);
			score+=am.computeScore(pg,cv);
		}
		return score;
	}

	public IntKeyHashMap getKey_attr()
	{
		return key_attr;
	}

	public Player getPlayer()
	{
		return player;
	}

	public void setPlayer(Player player)
	{
		this.player=player;
	}

}
