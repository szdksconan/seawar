package foxu.sea.officer;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.effect.EffectExecutable;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;

/**
 * ��������-�Ծ��ٽ��й���
 * 
 * @author Alan
 */
public class OfficerCenter
{

	/**�������ˢ�´���**/
	public static int FLUSH_TIMES=20;
	/** �����б� */
	IntKeyHashMap officers=new IntKeyHashMap();
	/** ��Ƭ�б� */
	IntKeyHashMap fragments=new IntKeyHashMap();
	/** ��֧��Ĺ�ѫ(���پ����) */
	long feats;
	byte[] featsLock=new byte[0];
	/** ��Ƭ��ѳ�ȡ��Ϣ{ʱ��,����} */
	int[] freeInfo=new int[2];
	/** ͼ����Ķ�ʱ��{�ͼ�,�м�,�߼�} */
	int[] libReadTime=new int[3];
	/** ����ʹ�õľ�����Ϣ{����Ϊ��} */
	Officer[] usedOfficers=new Officer[6];

	/**2������**/
	long coins;
	/**ˢ��ʱ��**/
	int  refreshTime;
	/**ˢ�´���**/
	int  count;
	/**����ˢ��ʱ��**/
	int  limitTime;
	/**��Ʒ�б�**/
	ArrayList shopList=new ArrayList();
	/** ӵ�й��ľ��� */
	IntList ownedofficers=new IntList();
	
	/** ������ɫ */
	Player player;
	
	public OfficerCenter(Player player)
	{
		this.player=player;
	}
	
	public boolean isOfficerIndexLegal(int index)
	{
		if(index>=0&&index<usedOfficers.length) return true;
		return false;
	}

	/** ��վ���ʹ����Ϣ */
	public void clearUsingOfficer()
	{
		for(int i=0;i<usedOfficers.length;i++)
		{
			usedOfficers[i]=null;
		}
	}

	/** ʹ�þ��� */
	public void useOfficer(Officer officer,int index)
	{
		usedOfficers[index]=officer;
	}

	/** ��ɢ���� */
	public void restOfficer(int index)
	{
		usedOfficers[index]=null;
	}

	/** ���Ӿ�����Ƭ */
	public void addOfficerFrag(int sid,int num)
	{
		synchronized(fragments)
		{
			OfficerFragment fragment=(OfficerFragment)fragments.get(sid);
			if(fragment==null)
			{
				fragment=(OfficerFragment)OfficerManager.factory
					.newSample(sid);
				fragments.put(fragment.getSid(),fragment);
			}
			fragment.incrCount(num);
		}
	}

	/** ��ȡ������Ƭ */
	public OfficerFragment getOfficerFrag(int sid)
	{
		return (OfficerFragment)fragments.get(sid);
	}

	/** ���پ�����Ƭ */
	public void incrOfficerFrag(int sid,int num)
	{
		synchronized(fragments)
		{
			OfficerFragment fragment=getOfficerFrag(sid);
			fragment.decrCount(num);
			if(fragment.getCount()<=0)
				fragments.remove(sid);
		}
	}
	
	/** ��ȡ���� */
	public Officer getOfficer(int id,int sid)
	{
		ArrayList os=(ArrayList)officers.get(sid);
		if(os!=null)
		{
			Officer officer=null;
			for(int i=0;i<os.size();i++)
			{
				officer=(Officer)os.get(i);
				if(officer.getId()==id) return officer;
			}
		}
		return null;
	}
	
	/** �Ƴ����� */
	public Officer removeOfficer(int id,int sid)
	{
		ArrayList os=(ArrayList)officers.get(sid);
		if(os!=null)
		{
			Officer officer=null;
			for(int i=0;i<os.size();i++)
			{
				officer=(Officer)os.get(i);
				if(officer.getId()==id) return (Officer)os.remove(i);
			}
		}
		return null;
	}

	/** ��ȡ���������о���ͻ�Ƶ�ԭʼ�������� */
	public int getPrimitiveOfficersCount(int sid)
	{
		int count=0;
		Officer officer;
		ArrayList os=(ArrayList)officers.get(sid);
		for(int i=0;i<os.size();i++)
		{
			officer=(Officer)os.get(i);
			if(isPrimitiveOfficer(officer))
			{
				count++;
			}
		}
		return count;
	}

	/** �Ƴ�һ��������ԭʼ���� */
	public void removePrimitiveOfficers(int sid,int count,IntList list,CreatObjectFactory factory,Player player)
	{
		Officer officer;
		ArrayList os=(ArrayList)officers.get(sid);
		for(int i=0;i<os.size()&&count>0;i++)
		{
			officer=(Officer)os.get(i);
			if(isPrimitiveOfficer(officer))
			{
				list.add(officer.getId());
				os.remove(i);
				i--;
				count--;
				factory.createOfficerTrack(
					OfficerTrack.REDUCE,
					OfficerTrack.INTO_UP_RANK,
					player.getId(),
					officer.getSid(),
					1,
					officer.getId(),
					OfficerManager.getInstance().getOfficerOrFragmentCount(
						player,officer.getSid()));
			}
		}
	}

	/** �Ƿ�Ϊ����ʹ�õľ��پ��� */
	public boolean isUsingOfficer(Officer officer)
	{
		for(int i=0;i<usedOfficers.length;i++)
		{
			if(usedOfficers[i]!=null
				&&usedOfficers[i].getId()==officer.getId()) return true;
		}
		return false;
	}
	
	/** �Ƿ��ڳ���������δ��ս�ľ��� */
	public boolean isInitLevelUnusedOfficer(Officer officer)
	{
		if(officer==null) return false;
		if(officer.getLevel()==Officer.INIT_LEVEL&&officer.getExp()==0
			&&!isUsingOfficer(officer)) return true;
		return false;
	}
	
	/** �Ƿ���������о���ͻ�Ƶ�ԭʼ���� */
	public boolean isPrimitiveOfficer(Officer officer)
	{
		if(!isInitLevelUnusedOfficer(officer)) return false;
		if(officer.getMilitaryRank()==Officer.INIT_LEVEL) return true;
		return false;
	}

	/** ���Ӿ���� */
	public void incrFeats(int exp)
	{
		synchronized(featsLock)
		{
			feats+=exp;
			if(feats<0) feats=Long.MAX_VALUE;
		}
	}

	/** ���پ���� */
	public void decrFeats(int exp)
	{
		if(exp<0) return;
		feats-=exp;
		if(feats<0) feats=0;
	}

	/** ����һ������ */
	public void addOfficer(Officer officer)
	{
		synchronized(officers)
		{
			ArrayList officerList=(ArrayList)officers.get(officer.getSid());
			if(officerList==null)
			{
				officerList=new ArrayList();
				officers.put(officer.getSid(),officerList);
			}
			officerList.add(officer);
			addOwnedOfficer(officer.getSid());
		}
	}

	/** ��ȡ��֧��Ĺ�ѫ���� */
	public long getFeats()
	{
		return feats;
	}

	/** ��ȡ�´���ѳ齱�ĵ���ʱ */
	public int getLeftTimeFromLastDraw(int offset,int now)
	{
		int left=getLastFreeDrawTime()+offset-now;
		return left>0?left:0;
	}
	
	/** ��ȡ�ϴ�ʹ����ѳ齱��ʱ�� */
	public int getLastFreeDrawTime()
	{
		return freeInfo[0];
	}
	
	/** ��ȡ�������ʹ�ô��� */
	public int getFreeDraw()
	{
		int start=SeaBackKit.getSomedayBegin(0);
		int free=0;
		if(freeInfo[0]>=start)
		{
			free=freeInfo[1];
		}
		return free;
	}

	/** ���ӽ������ʹ�ô��� */
	public void incrFreeDraw()
	{
		int start=SeaBackKit.getSomedayBegin(0);
		if(freeInfo[0]<start)
		{
			freeInfo[1]=0;
		}
		freeInfo[0]=TimeKit.getSecondTime();
		freeInfo[1]=freeInfo[1]+1;
	}
	
	/** ��ȡ�ϴ��Ķ�ʱ�� */
	public int getLibReadTime(int index)
	{
		return libReadTime[index];
	}

	/** �����Ķ�ʱ�� */
	public void setLibReadTime(int index,int time)
	{
		libReadTime[index]=time;
	}
	
	public void setUsedOfficers(Officer[] usedOfficers)
	{
		this.usedOfficers=usedOfficers;
	}

	public Officer[] getUsingOfficers()
	{
		return usedOfficers;
	}

	/** ��ȡ��ͬ���;������� */
	public int getOfficerCount(int sid)
	{
		ArrayList officerList=(ArrayList)officers.get(sid);
		if(officerList!=null)
			return officerList.size();
		return 0;
	}
	
	/** ��ȡ��ͬ������Ƭ���� */
	public int getFragCount(int sid)
	{
		OfficerFragment frag=getOfficerFrag(sid);
		if(frag!=null)
			return frag.getCount();
		return 0;
	}
	
	public void showBytesWrite(ByteBuffer data,Player player)
	{
		// ��ѫ
		data.writeInt((int)feats);
		data.writeLong(coins);
		// �齱��Ϣ
		data.writeInt(getFreeDraw());
		data.writeInt(getLeftTimeFromLastDraw(
			PublicConst.FREE_DRAW_LIMIT_TIME,TimeKit.getSecondTime()));
		data.writeInt(PublicConst.OFFICER_FRAG_LOW_FREE);
		data.writeInt(PublicConst.OFFICER_FRAG_LOW_PROP[1]);
		for(int i=0;i<PublicConst.OFFICER_FRAG_HIGH_GEMS.length;i+=2)
		{
			data.writeInt(PublicConst.OFFICER_FRAG_HIGH_GEMS[i+1]);
		}
		// �����б�
		int top=data.top();
		int len=0;
		data.writeInt(len);
		int[] sids=officers.keyArray();
		ArrayList list=null;
		for(int i=0;i<sids.length;i++)
		{
			list=(ArrayList)officers.get(sids[i]);
			if(list!=null)
			{
				Officer officer=null;
				for(int j=0;j<list.size();j++)
				{
					officer=(Officer)list.get(j);
					officer.showBytesWrite(data);
					len++;
				}
			}
		}
		int newTop=data.top();
		data.setTop(top);
		data.writeInt(len);
		data.setTop(newTop);
		// ��ս������Ϣ
		showBytesWriteUsingOfficers(data);
		// ��Ƭ�б�
		sids=fragments.keyArray();
		OfficerFragment frag=null;
		data.writeShort(sids.length);
		for(int i=0;i<sids.length;i++)
		{
			frag=(OfficerFragment)fragments.get(sids[i]);
			frag.showBytesWrite(data);
		}
		// ͼ�����Ϣ
		data.writeByte(libReadTime.length);
		len=4;
		int now=TimeKit.getSecondTime();
		for(int i=0;i<libReadTime.length;i++)
		{
			data.writeShort(PublicConst.OFFICER_LIB_READ[i*len+3]);
			int circle=PublicConst.OFFICER_LIB_READ[i*len+2];
			int cd=libReadTime[i]+circle-now;
			data.writeInt(cd<0?0:cd);
		}
		// �����
		showBytesWriteOfficerEffects(data,player);	
		// ������������
		data.writeShort(OfficerManager.getInstance().getSystemGuideMark(
			player));
		data.writeBoolean(OfficerManager.getInstance()
			.isSystemGuideComplete(player));
		// ��Ƭ�齱�״���ѱ��
		data.writeBoolean(player
			.getAttributes(PublicConst.OFFICER_FREE_DRAW)==null);
		if(checkShopRandom())
		{
			OfficerManager.getInstance().randomPlayerOffcerShop(player);
		}
		showBytesWriteShopInfo(data);
		player.getElitePoint().showBytesWrite(data);
		showBytesWriteOwnedOfficers(data);
	}

	public void showBytesWriteOwnedOfficers(ByteBuffer data)
	{
		data.writeShort(ownedofficers.size());
		for(int i=0;i<ownedofficers.size();i++)
		{
			data.writeShort(ownedofficers.get(i));
		}
	}
	
	public void showBytesWriteShopInfo(ByteBuffer data)
	{
		data.writeInt(refreshTime);
		data.writeShort(shopList.size());
		for(int i=0;i<shopList.size();i++)
		{
			OfficerShop shop=(OfficerShop)shopList.get(i);
			shop.showBytesWrite(data);
		}
		if(!SeaBackKit.isSameDay(TimeKit.getSecondTime(),limitTime))
		{
			count=0;
			limitTime=TimeKit.getSecondTime();
		}
		int times=FLUSH_TIMES-count;
		data.writeByte(times>0?times:0);
	}
	
	public void showBytesWriteUsingOfficers(ByteBuffer data)
	{
		int top=data.top();
		int len=0;
		data.writeShort(len);
		for(int i=0;i<usedOfficers.length;i++)
		{
			if(usedOfficers[i]==null) continue;
			data.writeInt(usedOfficers[i].getId());
			data.writeShort(i+1);
			len++;
		}
		int newTop=data.top();
		data.setTop(top);
		data.writeShort(len);
		data.setTop(newTop);
	}

	public void showBytesWriteOfficerEffects(ByteBuffer data,Player player)
	{
		// ���ӵ�ȫ�������
		int comShipNum=(int)player
			.getIsland()
			.getMainGroup()
			.getOfficerFleetAttr()
			.getCommonAttr(OfficerBattleHQ.ARMY,Ship.ALL_SHIP,
				PublicConst.EXTRA_SHIP,true,0);
		data.writeInt(comShipNum);
		ArrayList list=OfficerManager.getInstance().getEffectsFromOfficers(
			usedOfficers);
		data.writeInt(list.size());
		EffectExecutable ee;
		for(int i=0;i<list.size();i++)
		{
			ee=(EffectExecutable)list.get(i);
			ee.showBytesWrite(data);
		}
	}
	
	/** ���ֽ������з����л���ö������ */
	public synchronized Object bytesRead(ByteBuffer data)
	{
		feats=data.readLong();
		// �齱
		freeInfo=new int[data.readUnsignedByte()];
		for(int i=0;i<freeInfo.length;i++)
		{
			freeInfo[i]=data.readInt();
		}
		// ͼ���
		libReadTime=new int[data.readUnsignedByte()];
		for(int i=0;i<libReadTime.length;i++)
		{
			libReadTime[i]=data.readInt();
		}
		// ӵ�о���
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			ArrayList os=null;
			int arrayLen=data.readInt();
			if(arrayLen>0)
				os=new ArrayList();
			for(int j=0;j<arrayLen;j++)
			{
				os.add(Officer.bytesReadOfficer(data));
			}
			if(os!=null&&os.size()>0)
				officers.put(((Officer)os.get()).getSid(),os);
		}
		// �������
		usedOfficers=new Officer[data.readUnsignedByte()];
		for(int i=0;i<usedOfficers.length;i++)
		{
			int id=data.readInt();
			int sid=data.readUnsignedShort();
			usedOfficers[i]=getOfficer(id,sid);
		}
		// ��Ƭ
		len=data.readUnsignedShort();
		OfficerFragment frag;
		for(int i=0;i<len;i++)
		{
			frag=OfficerFragment.bytesReadOfficer(data);
			fragments.put(frag.getSid(),frag);
		}
		initOwnedOfficers();
		return this;
	}

	/**������������л����ֽ����飬����dataΪҪд����ֽڻ���**/
	public  void bytesWriteShop(ByteBuffer data)
	{
		data.writeLong(coins);
		data.writeShort(count);
		data.writeInt(limitTime);
		data.writeInt(refreshTime);
		data.writeByte(shopList.size());
		for(int i=0;i<shopList.size();i++)
		{
			OfficerShop shop=(OfficerShop)shopList.get(i);
			shop.byteWrite(data);
		}
	}
	
	/**���ֽ������з����л���ö������**/
	public  void bytesReadShop(ByteBuffer data)
	{
		coins=data.readLong();
		count=data.readUnsignedShort();
		limitTime=data.readInt();
		refreshTime=data.readInt();
		int le=data.readUnsignedByte();
		for(int i=0;i<le;i++)
		{
			OfficerShop shop=new OfficerShop();
			shop.byteRead(data);
			shopList.add(shop);
		}
		
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public synchronized void bytesWrite(ByteBuffer data)
	{
		data.writeLong(feats);
		// �齱
		data.writeByte(freeInfo.length);
		for(int i=0;i<freeInfo.length;i++)
		{
			data.writeInt(freeInfo[i]);
		}
		// ͼ���
		data.writeByte(libReadTime.length);
		for(int i=0;i<libReadTime.length;i++)
		{
			data.writeInt(libReadTime[i]);
		}
		// ӵ�о���
		int[] sids=officers.keyArray();
		data.writeShort(sids.length);
		for(int i=0;i<sids.length;i++)
		{
			int len=0;
			ArrayList os=(ArrayList)officers.get(sids[i]);
			if(os!=null) len=os.size();
			data.writeInt(len);
			Officer officer;
			for(int j=0;j<len;j++)
			{
				officer=(Officer)os.get(j);
				officer.bytesWrite(data);
			}
		}
		// �������
		data.writeByte(usedOfficers.length);
		for(int i=0;i<usedOfficers.length;i++)
		{
			int id=0;
			int sid=0;
			if(usedOfficers[i]!=null)
			{
				id=usedOfficers[i].getId();
				sid=usedOfficers[i].getSid();
			}
			data.writeInt(id);
			data.writeShort(sid);
		}
		// ӵ����Ƭ
		sids=fragments.keyArray();
		data.writeShort(sids.length);
		OfficerFragment of;
		for(int i=0;i<sids.length;i++)
		{
			of=(OfficerFragment)fragments.get(sids[i]);
			of.bytesWrite(data);
		}
		persistOwnedOfficers();
	}

	public void addOwnedOfficer(int sid)
	{
		if(ownedofficers.contain(sid)) return;
		ownedofficers.add(sid);
		JBackKit.sendOwnedOfficer(player);
	}

	public void initOwnedOfficers()
	{
		String owned=player.getAttributes(PublicConst.OWNED_OFFICERS);
		// û�����ݿ���û�н��м��ݳ�ʼ��
		if(owned==null||"".equals(owned))
		{
			int[] keys=officers.keyArray();
			ArrayList list;
			for(int i=0;i<keys.length;i++)
			{
				list=(ArrayList)officers.get(keys[i]);
				if(list==null||list.size()<=0)continue;
				addOwnedOfficer(keys[i]);
			}
			return;
		}
		int[] owns=TextKit.parseIntArray(TextKit.split(owned,","));
		ownedofficers=new IntList(owns);
	}

	public void persistOwnedOfficers()
	{
		if(ownedofficers.size()<=0) return;
		StringBuffer sb=new StringBuffer();
		sb.append(ownedofficers.get(0));
		for(int i=1;i<ownedofficers.size();i++)
		{
			sb.append(",");
			sb.append(ownedofficers.get(i));
		}
		player.setAttribute(PublicConst.OWNED_OFFICERS,sb.toString());
	}
	
	/**2������**/
	public long getCoins()
	{
		return coins;
	}
	
	/**���ӻ���**/
	public  void incrCoins(int num)
	{
		if(num<0) return;
		coins+=num;
		if(coins<0) coins=Long.MAX_VALUE;
	}
	/**���ٻ���**/
	public void descCoins(int num)
	{
		if(num<0) return;
		coins-=num;
		if(coins<0) coins=0;
	}
	/** �������Ƿ��㹻 **/
	public boolean checkCoins(int num)
	{
		if(coins>=num) return true;
		return false;
	}
	/**�Ƿ���Ҫˢ�¾����̵�**/
	public boolean checkTime(int time)
	{
		if(refreshTime<time)
		{
			return true;
		}
		return false;
	}
	
	/**�������Ƿ���Ҫ��������̵�**/
	public boolean checkShopRandom()
	{
		if(shopList==null || shopList.size()==0)
			return true;
		int time=OfficerManager.getInstance().getSystemFTime();
		return checkTime(time);
	}
	
	/**�ж����Ƿ����ˢ��**/
	public boolean checkFlushShop()
	{
		if(count<FLUSH_TIMES)
			return true;
		return false;
	}
	
	public  void addFlushTimes(int time)
	{
		count+=time;
	}
	
	public void setCoins(int coins)
	{
		this.coins=coins;
	}

	
	public ArrayList getShopList()
	{
		return shopList;
	}

	
	public void setShopList(ArrayList shopList)
	{
		this.shopList=shopList;
	}

	
	public int getRefreshTime()
	{
		return refreshTime;
	}

	
	public void setRefreshTime(int refreshTime)
	{
		this.refreshTime=refreshTime;
	}

	
	public int getCount()
	{
		return count;
	}

	
	public void setCount(int count)
	{
		this.count=count;
	}

	
	public IntList getOwnedofficers()
	{
		return ownedofficers;
	}

	
	public void setOwnedofficers(IntList ownedofficers)
	{
		this.ownedofficers=ownedofficers;
	}
	
	
	
}
