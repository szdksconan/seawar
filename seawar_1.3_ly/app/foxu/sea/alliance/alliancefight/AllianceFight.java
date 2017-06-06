package foxu.sea.alliance.alliancefight;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.OrderList;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Role;
import foxu.sea.Ship;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.alliance.Alliance;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.shipdata.ShipCheckData;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.SetKit;
import mustang.util.TimeKit;

/**
 * ����ս
 * 
 * @author yw
 * 
 */
public class AllianceFight
{
	/** LOSE_PERC��ʧ�ٷֱ� */
	public static int EVENT_MAX=100,RECORD_MAX=200,LOSE_PERC=5,PAGEMAX=20,PAGEMAX20=20,
					AWARD_INERVAL=3600,FIGHT_MAX=200,LEVEL_LIMIT=10,
					PRODUCING=1,FLEET_MAX=Integer.MAX_VALUE;

	/** ɾ������ */
	int deleteCount=1;
	/** ����ID */
	int allianceID;
	/** �Զ�����(0 ��,1��) */
	int reinforce=0;
	/** ����ս������ (��λʱ�� ��λ����) */
	int dayCount;
	/** ս���Ž� */
	WarHorn horn=new WarHorn();
	/** ��潢�� */
	OrderList fleets=new OrderList();
	/** �ۼƾ��׼�¼ */
	OrderList records=new OrderList();
	/** ������ϸ playerId-���ӣ�arraylist�� */
	IntKeyHashMap dinationMap=new IntKeyHashMap();
	/** ս��(evengID) */
	OrderList fightEvent=new OrderList();
	/** �����еĴ�: �������ʱ�� ��(0)��(1)��� sid num ��ʼʱ�� */
	int[] upship=new int[5];
	/** �����еĴ�: �������ʱ�� ��(0)��(1)��� sid num ��ʼʱ�� */
	int[] upship1=new int[5];

	/** ��������(�����л�����������) */
	RankData[] rank;
	/** ����Ƚ��� */
	RankComparator com=new RankComparator();
	
	//��������
	Object upshiplock=new Object();

	public AllianceFight()
	{
	}
	public AllianceFight(int allianceID)
	{
		this.allianceID=allianceID;
	}
	/** ��ʼ������ */
	public void initRankData()
	{
		rank=new RankData[dinationMap.size()];
		Object[] dinations=dinationMap.valueArray();
		int[] keys=dinationMap.keyArray();
		for(int i=0;i<rank.length;i++)
		{
			OrderList dination=(OrderList)dinations[i];
			StockFleet fleet=(StockFleet)dination.get(0);
			RankData data=new RankData(keys[i],getShipPri(fleet.getSid()),
				fleet.getSid(),fleet.getCount());
			rank[i]=data;
		}
		SetKit.sort(rank,com,true);
	}
	/** ������ֻ */
	public String upShip(Player player,int alevel,ByteBuffer data,BattleGround ground)
	{
		int upbuild=data.readUnsignedByte();
		int shipSid=data.readUnsignedShort();
		int num=data.readUnsignedShort();
		synchronized(upshiplock)
		{
			String result=checkUpShip(player,shipSid,num,alevel,upbuild,ground);
			if(result==null)
			{
				produceShip(player,shipSid,num,alevel,upbuild);
				//��־
				Ship ship=(Ship)Role.factory.getSample(shipSid);
				int costShip[]=ship.getUpgradeShipConsume();
				IntList list=null;
				if(costShip!=null&&costShip.length>0)
				{
					IntList bleft=ground==null?null:ground.getAllFleets();
					list=new IntList();
					for(int i=0;i<costShip.length;i+=2)
					{
						list.add(costShip[i]);
						list.add(costShip[i+1]*num);
					}
					AfihgtShipData.instance().generateShipTrack(ShipRecord.PRODUCE_DECR,allianceID,player.getName(),getAllFleets(),bleft,list);
				}
				data.clear();
			}
			return result;
		}
	}
	/** ���ش�ֻ�������� */
	public void getUpShipData(ByteBuffer data,int type)
	{
		int[] upship=this.upship;
		if(type==1) upship=upship1;
		data.writeShort(upship[2]);
		data.writeShort(upship[3]);
		data.writeInt(upship[0]-upship[4]);
		data.writeInt(upship[0]-TimeKit.getSecondTime());
	}
	/** ˢ��������ֻ���� */
	public void flushUpShip(ByteBuffer data)
	{
		if(upship[0]!=0&&upship[1]==PRODUCING)
		{
			data.writeBoolean(true);
			data.writeShort(upship[2]);
			data.writeShort(upship[3]);
			data.writeInt(upship[0]-upship[4]);
			data.writeInt(upship[0]-TimeKit.getSecondTime());
		}
		else
		{
			data.writeBoolean(false);
		}

		if(upship1[0]!=0&&upship1[1]==PRODUCING)
		{
			data.writeBoolean(true);
			data.writeShort(upship1[2]);
			data.writeShort(upship1[3]);
			data.writeInt(upship1[0]-upship1[4]);
			data.writeInt(upship1[0]-TimeKit.getSecondTime());
		}
		else
		{
			data.writeBoolean(false);
		}
	}
	/** ������ֻ */
	public void produceShip(Player player,int shipSid,int num,int alevel,int upbuild)
	{
		int[] upship=null;
		if(upbuild==2)
			upship=upship1;
		else
			upship=this.upship;
		Ship ship=(Ship)Role.factory.getSample(shipSid);
		// �۳�����sid,num
		boolean bundle=false;
		if(ship.getCostPropSid()!=null&&ship.getCostPropSid().length>0)
		{
			for(int i=0;i<ship.getCostPropSid().length;i+=2)
			{
				player.getBundle().decrProp(ship.getCostPropSid()[i],
					ship.getCostPropSid()[i+1]*num);
				bundle=true;
			}
		}
		int upTime=ship.getUpgradeTime();
		player.reduceBuidResource(ship.getUpgradeResources(),num);
		IntList list=new IntList();
		// �۳���ֻ
		if(ship.getUpgradeShipConsume()!=null
			&&ship.getUpgradeShipConsume().length>0)
		{
			for(int i=0;i<ship.getUpgradeShipConsume().length;i+=2)
			{
				decrShipBySid(ship.getUpgradeShipConsume()[i],
					ship.getUpgradeShipConsume()[i+1]*num);
				list.add(ship.getUpgradeShipConsume()[i]);
				list.add(ship.getUpgradeShipConsume()[i+1]*num);
			}
		}
		// ˢ����Դ+��Ʒ
		JBackKit.sendResetResources(player);
		if(bundle) JBackKit.sendResetBunld(player);
		//�������ٻ
		int percent=ActivityContainer.getInstance().getActivitySpeed(
			ActivityContainer.ARM_ID);
		float time=(float)upTime
			/(float)(0.95+alevel*0.05);
		/** ��ȡ�� */
		time=(int)Math.ceil(time);
		time=time*(100-percent)/100;
		/** ��ȡ�� */
		int timeUp=(int)Math.ceil(time);
		upship[0]=timeUp*num+TimeKit.getSecondTime();// �������ʱ��
		upship[1]=PRODUCING;// �Ƿ����
		upship[2]=shipSid;
		upship[3]=num;
		upship[4]=TimeKit.getSecondTime();

	}
	/** ������� */
	public String checkUpShip(Player player,int shipSid,int num,int alevel,int upbuild,BattleGround ground)
	{
		if(getCurrentShipNum(shipSid,ground)+num>FLEET_MAX)return "ship num max";
		PlayerBuild build=null;
		int[] upship=null;
		checkProduceFinish(this.upship,ground);
		checkProduceFinish(upship1,ground);
		if(upbuild==2&&shipSid%10>=6)
		{
			build=player.getIsland().getBuildByType(15,
				player.getIsland().getBuilds());// ǿ����
			upship=upship1;
		}
		else
		{
			build=player.getIsland().getBuildByType(6,
				player.getIsland().getBuilds());
			;// 6����SID
			upship=this.upship;
		}
		if(upship[0]!=0&&upship[1]==PRODUCING) return "producing";
		// �����������
		if(build==null||!build.checkLevelPropSid(shipSid))
		{
			return "level limit produce";
		}
		// �����Դ�Ƿ��㹻
		if(!player.checkResourceForUpShips(shipSid,num))
		{
			JBackKit.sendResetResources(player);
			return "resource limit";
		}
		Ship ship=(Ship)Role.factory.getSample(shipSid);
		if(ship==null) return "ship is null";
		int costPropSid[]=ship.getCostPropSid();
		if(costPropSid!=null&&costPropSid.length>0)
		{
			for(int i=0;i<costPropSid.length;i+=2)
			{
				/** ����Ƿ���Ʒ�㹻 */
				if(!player.checkPropEnough(costPropSid[i],costPropSid[i+1]
					*num)) return "not enough prop";
			}
		}
		int costShip[]=ship.getUpgradeShipConsume();
		if(costShip!=null&&costShip.length>0)
		{
			for(int i=0;i<costShip.length;i+=2)
			{
				/** ����Ƿ�ֻ�㹻 */
				int haveNum=0;
				StockFleet sfleet=getShipBySid(costShip[i]);
				if(sfleet!=null) haveNum=sfleet.getCount();
				if(costShip[i+1]*num>haveNum) return "not enough ship";
			}
		}
		return null;
	}
	/** ��ȡӵ�д�ֻ���� */
	public int getCurrentShipNum(int shipSid,BattleGround ground)
	{
		StockFleet fleet=getShipBySid(shipSid);
		int num=fleet==null?0:fleet.getCount();
		if(ground!=null)
		{
			num+=ground.getShipCountBySid(shipSid);
		}
		if(upship1[1]==PRODUCING&&upship1[2]==shipSid)
		{
			num+=upship1[3];
		}
		else if(upship[1]==PRODUCING&&upship[2]==shipSid)
		{
			num+=upship[3];
		}
		return num;
	}

	/** ���������� */
	public void checkProduceFinish(int[] upship,BattleGround ground)
	{
		synchronized(upship)
		{
			if(upship[0]!=0&&upship[1]==PRODUCING)
			{
				if(TimeKit.getSecondTime()>=upship[0])
				{
					StockFleet fleet=getShipBySid(upship[2]);
					if(fleet!=null)
					{
						fleet.incrCount(upship[3]);
					}
					else
					{
						addNewShip(upship[2],upship[3],fleets);
					}
					IntList list=new IntList();
					list.add(upship[2]);
					list.add(upship[3]);
					IntList bleft=null;
					if(ground!=null)bleft=ground.getAllFleets();
					AfihgtShipData.instance().generateShipTrack(ShipRecord.PRODUCE_ADD,allianceID,"",getAllFleets(),bleft,list);
					clearInts(upship);
				}

			}
		}
	}

	public void clearInts(int[] ints)
	{
		if(ints==null) return;
		for(int i=0;i<ints.length;i++)
		{
			ints[i]=0;
		}
	}
	/** ������CD */
	public String clearCD(Player player,CreatObjectFactory objectFactory,
		int type,BattleGround ground)
	{
		int[] upship=null;
		if(type==0)
		{
			upship=this.upship;
		}
		else
		{
			upship=upship1;
		}
		synchronized(upship)
		{
			checkProduceFinish(upship,ground);
			if(upship[0]!=0&&upship[1]==PRODUCING)
			{
				float times=(upship[0]-TimeKit.getSecondTime())
								/(float)PublicConst.GEMS_SPEED;
				int needGems=(int)times;
				if(needGems!=times)needGems+=1;
				needGems=needGems*PublicConst.GEMS_PER_UNIT_SPEED;
				if(!Resources.checkGems(needGems,player.getResources()))
					return "not enough gems";
				Resources.reduceGems(needGems,player.getResources(),player);
				// ��¼����
				objectFactory.createGemTrack(GemsTrack.PRODUCE_SPEED_UP,
					player.getId(),needGems,0,
					Resources.getGems(player.getResources()));
				upship[0]=TimeKit.getSecondTime();
				checkProduceFinish(upship,ground);
				return null;
			}
			else
			{
				return "no cd";
			}
		}
	}

	/** �贬 */
	public String dinateShip(Player player,ByteBuffer data,BattleGround ground,CreatObjectFactory objectFactory)
	{
		IntList decrList=new IntList();
		int sid=data.readUnsignedShort();
		int count=data.readUnsignedShort();
		if(count<=0) return "at least one";
		if(SeaBackKit.isContainValue(PublicConst.TRANSPORT_SHIP_SIDS,sid))
		{
			return "transport ship can not donate";
		}
		decrList.add(sid);
		decrList.add(count);
		if(!checkShipNum(player,decrList)) return "ship num is limit";
		if(checkShipMax(decrList,ground))return "ship num max";
		decrPlayerShip(player,decrList);
		addShip(decrList);
		addPlayerDination(player,decrList);
		flushRank(player.getId(),decrList);
		
		//��־
		ShipCheckData shipdata=objectFactory.addShipTrack(0,ShipCheckData.DONATE_SHIP,player,decrList,null,false);
		shipdata.setExtra(objectFactory.getAlliance(allianceID,false).getName());
		
		//��־
		IntList left=getAllFleets();
		IntList bleft=ground==null?null:ground.getAllFleets();
		AfihgtShipData.instance().generateShipTrack(ShipRecord.DONATE,allianceID,player.getName(),left,bleft,decrList);
		return null;
	}
	/** ��ȡ��洬ֻ */
	public IntList getAllFleets()
	{
		if(fleets.size()<=0)return null;
		IntList list=new IntList();
		StockFleet sfeet=null;
		for(int i=0;i<fleets.size();i++)
		{
			sfeet=(StockFleet)fleets.get(i);
			if(sfeet.getCount()>0)
			{
				list.add(sfeet.getSid());
				list.add(sfeet.getCount());
			}
		}
		return list.size()>0?list:null;
	}
	/** �Ӵ� */
	public void addShip(IntList list)
	{
		synchronized(fleets)
		{
			for(int k=0;k<list.size();k+=2)
			{
				int shipSid=list.get(k);
				int num=list.get(k+1);
				boolean add=false;
				for(int i=0;i<fleets.size();i++)
				{
					StockFleet sfleet=(StockFleet)fleets.get(i);
					if(sfleet.getSid()==shipSid)
					{
						sfleet.incrCount(num);
						add=true;
						break;
					}
				}
				if(!add)
				{
					addNewShip(shipSid,num,fleets);
				}
			}

		}

	}
	/** �����´� */
	public void addNewShip(int sid,int count,OrderList fleets)
	{
		StockFleet newFleet=new StockFleet(sid,count);
		int newPri=getShipPri(newFleet.getSid());
		boolean insert=false;
		for(int i=0;i<fleets.size();i++)
		{
			StockFleet sfleet=(StockFleet)fleets.get(i);
			if(newPri>getShipPri(sfleet.getSid()))
			{
				fleets.addAt(newFleet,i);
				insert=true;
				break;
			}
		}
		if(!insert)
		{
			fleets.add(newFleet);
		}
	}
	/** �����ȼ� ȡshipSid ��ʮ λ������λ�� */
	public int getShipPri(int sid)
	{
		int v1=sid%10000;
		return v1%10*10+v1/10;
	}
	/** �����Ҵ�ֻ���� */
	public boolean checkShipNum(Player player,IntList list)
	{
		for(int i=0;i<list.size();i+=2)
		{
			int shipSid=list.get(i);
			int num=list.get(i+1);
			Ship ship=(Ship)Ship.factory.getSample(shipSid);
			// ���ܴ��Ƿ�����
			if(ship==null
				||(ship.getPlayerType()==Ship.POSITION_AIR
					||ship.getPlayerType()==Ship.POSITION_MISSILE||ship
					.getPlayerType()==Ship.POSITION_FIRE)) return false;
			int haveNum=player.getIsland().getShipsBySid(shipSid,
				player.getIsland().getTroops());
			FleetGroup mainGroup=player.getIsland().getMainGroup();
			if(mainGroup!=null)
			{
				haveNum+=mainGroup.getShipBySid(shipSid);
			}
			if(haveNum<num) return false;
		}
		return true;

	}
	/** �����׺� �Ƿ񳬹�������� */
	public boolean checkShipMax(IntList list,BattleGround ground)
	{
		checkProduceFinish(upship,ground);
		checkProduceFinish(upship1,ground);
		for(int i=0;i<list.size();i+=2)
		{
			int shipSid=list.get(i);
			int num=list.get(i+1);
			int cnum=getCurrentShipNum(shipSid,ground);
			if(cnum+num<=FLEET_MAX) return false;
		}
		return true;
	}
	/** ����Ҵ� */
	public void decrPlayerShip(Player player,IntList list)
	{
		boolean resetMainGroup=false;
		for(int i=0;i<list.size();i+=2)
		{
			int shipSid=list.get(i);
			int num=list.get(i+1);
			int reduceNum=player.getIsland().reduceShipBySid(shipSid,num,
				player.getIsland().getTroops());
			if(reduceNum<num)
			{
				resetMainGroup=true;
				// �۳��Ƿ������
				reduceShips(player,shipSid,(num-reduceNum));
			}
		}
		if(!resetMainGroup)
		{
			JBackKit.resetMainGroup(player);
		}
	}
	/** �۳��Ƿ�����ָ��sid��ֻ */
	public void reduceShips(Player player,int shipSid,int num)
	{
		FleetGroup group=player.getIsland().getMainGroup();
		Fleet fleet[]=group.getArray();
		for(int i=0;i<fleet.length;i++)
		{
			if(fleet[i]==null) continue;
			if(fleet[i].getShip().getSid()==shipSid&&fleet[i].getNum()>0)
			{
				int reduceNum=group.reduceShipByLocation(
					fleet[i].getLocation(),num);
				if(reduceNum>=num) return;
				reduceShips(player,shipSid,(num-reduceNum));
				break;
			}
		}
	}
	/** ���Ӹ��˾�����ϸ */
	public void addPlayerDination(Player player,IntList list)
	{
		OrderList plist=(OrderList)dinationMap.get(player.getId());
		if(plist==null)
		{
			synchronized(dinationMap)
			{
				plist=new OrderList();
				dinationMap.put(player.getId(),plist);
			}
		}
		for(int k=0;k<list.size();k+=2)
		{
			int shipSid=list.get(k);
			int num=list.get(k+1);
			int[] old_now=null;
			boolean add=false;
			for(int i=0;i<plist.size();i++)
			{
				StockFleet sfleet=(StockFleet)plist.get(i);
				if(sfleet.getSid()==shipSid)
				{
					old_now=sfleet.incrCount(num);
					add=true;
					break;
				}
			}
			if(!add)
			{
				addNewShip(shipSid,num,plist);
				old_now=new int[2];
				old_now[1]=num;
			}
			create(player,shipSid,old_now[0],old_now[1]);
		}

	}
	/** �����ۼƾ��׼�¼ */
	public void create(Player player,int shipSid,int oldNum,int nowNum)
	{
		if(nowNum/100==oldNum/100) return;
		DinationRecord record=new DinationRecord(player.getId(),shipSid,
			nowNum/100*100);
		synchronized(records)
		{
			records.add(record);
			if(records.size()>RECORD_MAX)
			{
				records.removeAt(0);
			}
		}
	}
	/** ˢ������ */
	public void flushRank(int playerID,IntList list)
	{
		// ��ȡ��߼���
		int index=0;
		for(int i=2;i<list.size();i+=2)
		{
			if(getShipPri(list.get(i))>getShipPri(list.get(index)))
			{
				index=i;
			}
		}
		int shipSid=list.get(index);
		int num=list.get(index+1);
		int pri=getShipPri(shipSid);
		boolean sort=false;
		boolean isnull=false;
		if(rank==null)
		{
			rank=new RankData[1];
			isnull=true;
		}
		synchronized(rank)
		{
			if(isnull)
			{
				rank[0]=new RankData(playerID,pri,shipSid,num);
			}
			else
			{
				RankData rankdata=getRankData(playerID);
				if(rankdata!=null)
				{
					sort=rankdata.updateData(pri,shipSid,num);
				}
				else
				{
					RankData[] temp=new RankData[rank.length+1];
					System.arraycopy(rank,0,temp,0,rank.length);
					temp[temp.length-1]=new RankData(playerID,pri,shipSid,
						num);
					rank=temp;
					sort=true;
				}
			}
			if(sort)
			{
				SetKit.sort(rank,com,true);
			}
		}

	}
	/** ��ȡ���� */
	public RankData getRankData(int playerID)
	{
		if(rank==null) return null;
		for(int i=0;i<rank.length;i++)
		{
			if(rank[i].getPlayerID()==playerID)
			{
				return rank[i];
			}
		}
		return null;
	}
	/** ���� �۴� */
	public String decrShip(ByteBuffer data,IntList declist,Alliance alliance)
	{
		checkProduceFinish(upship,null);
		checkProduceFinish(upship1,null);
		int len=data.readUnsignedByte();
		if(len==0)
		{
			return "at least one";
		}
		OrderList tempFleets=new OrderList();
		IntKeyHashMap decrMap=new IntKeyHashMap();
		for(int i=0;i<len;i++)
		{
			declist.add(data.readUnsignedByte());
			int shipSid=data.readUnsignedShort();
			int count=data.readUnsignedShort();
			if(count<=0) return "at least one";
			if(count>alliance.getShipMax()) return "you can not take more";
			StockFleet sfleet=getShipBySid(shipSid);
			int decr=decrMap.get(shipSid)==null?0:(Integer)decrMap.get(shipSid);
			if(sfleet==null||sfleet.getCount()<count+decr)
			{
				return "ship num is limit";
			}
			decrMap.put(shipSid,decr+count);
			declist.add(shipSid);
			declist.add(count);
			tempFleets.add(sfleet);
		}
		for(int i=0;i<len;i++)
		{
			((StockFleet)tempFleets.get(i)).decrCount(declist.get(3*i+2));
		}

		return null;
	}
	/** �Զ����� �۴� */
	public void recruitShip(IntList declist,BattleGround ground)
	{
		checkProduceFinish(upship,ground);
		checkProduceFinish(upship1,ground);
		StockFleet sfleet=null;
		synchronized(fleets)
		{
			for(int i=0;i<declist.size();i+=2)
			{
				for(int k=0;k<fleets.size();k++)
				{
					sfleet=(StockFleet)fleets.get(k);
					if(sfleet.getSid()!=declist.get(i)) continue;
					int decr=declist.get(i+1);
					if(sfleet.getCount()<decr) decr=sfleet.getCount();
					sfleet.decrCount(decr);
					declist.set(decr,i+1);
					break;
				}

			}
		}

	}
	/** ��ȡָ��SID�Ŀ�洬 */
	public StockFleet getShipBySid(int shipSid)
	{
		StockFleet sfleet=null;
		for(int i=0;i<fleets.size();i++)
		{
			sfleet=(StockFleet)fleets.get(i);
			if(sfleet.getSid()==shipSid) break;
			sfleet=null;
		}
		return sfleet;
	}
	/** �۳�ָ��sid��ֻ */
	public void decrShipBySid(int sid,int num)
	{
		synchronized(fleets)
		{
			StockFleet fleet=getShipBySid(sid);
			if(fleet==null)
				return ;
			fleet.decrCount(num);
		}
	}
	/** ���ӱ������ �Ӵ� */
	public void addResidualShip(IntList list)
	{
		IntList addlist=new IntList();
		for(int i=0;i<list.size();i+=3)
		{
			addlist.add(list.get(i+1));
			addlist.add(list.get(i+2));
		}
		addShip(addlist);
	}
	/** ��ս�� */
	public void addEvent(int eventId,CreatObjectFactory objFatory)
	{
		synchronized(fightEvent)
		{
			fightEvent.addAt(eventId,0);
			clearEvent(objFatory);
		}

	}
	/** ����ս�� */
	public void clearEvent(CreatObjectFactory objFatory)
	{
		if(fightEvent.size()>EVENT_MAX)
		{
			int id=(Integer)fightEvent.remove();
			AllianceFightEvent event=(AllianceFightEvent)objFatory
				.getaFightEventMemCache().load(id+"");
			if(event!=null)
			{
				event.decrCount();
				objFatory.getaFightEventMemCache().save(event.getUid()+"",
					event);
			}
		}
		if(fightEvent.size()>EVENT_MAX) clearEvent(objFatory);
	}
	/** ��ȡĳҳ�������� */
	public void getRanksByPage(CreatObjectFactory objfactory,ByteBuffer data)
	{
		int cpage=data.readUnsignedByte();
		int index=(cpage-1)*PAGEMAX;
		data.clear();
		data.writeByte(cpage);
		int top=data.top();
		int apage=1;
		data.writeByte(apage);
		if(rank==null)
			data.writeByte(0);
		else if(index<0||index>=rank.length)
			data.writeByte(0);
		else
		{
			synchronized(rank)
			{
				int len=rank.length-index;
				apage=rank.length%PAGEMAX==0?rank.length/PAGEMAX:rank.length
					/PAGEMAX+1;
				if(len>PAGEMAX) len=PAGEMAX;
				data.writeByte(len);
				for(int i=index;i<index+len;i++)
				{
					rank[i].showBytesWrite(objfactory,data);
				}
				int nowTop=data.top();
				data.setTop(top);
				data.writeByte(apage);
				data.setTop(nowTop);
			}
		}
	}
	/** ��ȡĳ�˾贬��ϸ */
	public void getDiantionByName(Player player,ByteBuffer data)
	{
		OrderList list=(OrderList)dinationMap.get(player.getId());
		data.clear();
		if(list==null||list.size()<=0) data.writeByte(0);
		data.writeByte(list.size());
		for(int i=0;i<list.size();i++)
		{
			StockFleet fleet=(StockFleet)list.get(i);
			data.writeShort(fleet.getSid());
			data.writeInt(fleet.getCount());
		}

	}
	/** ��ȡĳҳ���׼�¼ */
	public void getRecordByPage(CreatObjectFactory objfactory,ByteBuffer data)
	{
		int cpage=data.readUnsignedByte();
		int index=(cpage-1)*PAGEMAX;
		data.clear();
		data.writeByte(cpage);
		int top=data.top();
		int apage=1;
		data.writeByte(apage);
		if(index<0||index>=records.size())
			data.writeByte(0);
		else
		{
			synchronized(records)
			{
				int len=records.size()-index;
				if(len>PAGEMAX) len=PAGEMAX;
				apage=records.size()%PAGEMAX==0?records.size()/PAGEMAX:records
					.size()/PAGEMAX+1;
				data.writeByte(len);
			    for(int i=records.size()-index-1;i>=records.size()-index-len;i--)
				{
					((DinationRecord)records.get(i)).showBytesWrite(objfactory,
						data);
				}
				int nowTop=data.top();
				data.setTop(top);
				data.writeByte(apage);
				data.setTop(nowTop);
			}
		}
	}
	/** ��ȡս�µ�ĳҳ */
	public void getFightEvent(AllianceFightManager amanager,ByteBuffer data)
	{
		int cpage=data.readUnsignedByte();
		int index=(cpage-1)*PAGEMAX20;

		data.clear();
		synchronized(fightEvent)
		{
			int allPage=fightEvent.size()%AllianceFight.PAGEMAX20==0
				?fightEvent.size()/AllianceFight.PAGEMAX20:fightEvent.size()
					/AllianceFight.PAGEMAX20+1;
			data.writeByte(allPage==0?1:allPage);
			data.writeByte(cpage);
			if(index<0||index>=fightEvent.size())
				data.writeByte(0);
			else
			{
				int len=fightEvent.size()-index;
				if(len>PAGEMAX20) len=PAGEMAX20;
				int ltop=data.top();
				data.writeByte(len);
				for(int i=index;i<index+len;i++)
				{
					Integer id=(Integer)fightEvent.get(i);
					if(id==null)
					{
						fightEvent.removeAt(i);
						len--;
						i--;
						continue;
					}
					AllianceFightEvent event=(AllianceFightEvent)amanager
						.getFightEvent(id);
					if(event==null)
					{
						fightEvent.removeAt(i);
						len--;
						i--;
						continue;
					}
					event.showBytesWrite(amanager,data);
				}
				int ntop=data.top();
				data.setTop(ltop);
				data.writeByte(len);
				data.setTop(ntop);
			}
		}
	}
	/** ��ȡ��潢�� */
	public void getStockFleet(ByteBuffer data,BattleGround ground)
	{
		checkProduceFinish(upship,ground);
		checkProduceFinish(upship1,ground);
		showBytesWriteFleets(data);
	}
	/** ��Сɾ������ */
	public void decrCount()
	{
		deleteCount--;
	}
	/** �Ƿ��Զ����� */
	public boolean isReinforce()
	{
		return reinforce==1;
	}
	/** ��ȡ���ճ�ս���� */
	public int getReadDayCount()
	{
		int day=SeaBackKit.getDayOfYear();
		if(day==(dayCount>>>16))
		{
			return (dayCount<<16)>>>16;
		}
		else
		{
			return 0;
		}
	}
	/** ���ӳ�ս���� */
	public void incrDayCount()
	{
		int day=SeaBackKit.getDayOfYear();
		if(day==(dayCount>>>16))
		{
			dayCount=(day<<16)+((dayCount<<16)>>>16)+1;
		}
		else
		{
			dayCount=(day<<16)+1;
		}
	}
	/** ����ɾ���������� */
	public void exitRmDinate(int playerId)
	{
		synchronized(dinationMap)
		{
			dinationMap.remove(playerId);
		}
		if(rank!=null)
		{
			synchronized(rank)
			{
				int index=-1;
				for(int i=0;i<rank.length;i++)
				{
					if(rank[i].playerID==playerId)
					{
						index=i;
						break;
					}
				}
				if(index>=0)
				{
					RankData[] temp=new RankData[rank.length-1];
					for(int i=0,j=0;i<rank.length;i++)
					{
						if(i==index) continue;
						temp[j]=rank[i];
						j++;
					}
					rank=temp;
				}
			}
		}
	}
	/** �˽�ɢ���� */
	public void dismiss(CreatObjectFactory objFatory)
	{
		synchronized(fightEvent)
		{
			AllianceFightEvent event=null;
			for(int i=fightEvent.size()-1;i>=0;i--)
			{
				event=(AllianceFightEvent)objFatory
					.getAllianceFightMemCache().load(
						((Integer)fightEvent.get(i))+"");
				if(event!=null)
				{
					event.decrCount();
					objFatory.getAllianceFightMemCache().save(
						event.getUid()+"",event);
				}
			}
		}
	}

	/** ���ֽ������з����л���ö������ */
	public Object bytesRead(ByteBuffer data)
	{
		deleteCount=data.readUnsignedByte();
		allianceID=data.readInt();
		reinforce=data.readUnsignedByte();
		dayCount=data.readInt();
		bytesReadHorn(data);
		bytesReadFleets(data);
		bytesReadRecords(data);
		bytesReadDinationMap(data);
		bytesReadFightEvent(data);
		bytesReadUpShip(data);
		return this;
	}
	/** ������������л����ֽ����飬����dataΪҪд����ֽڻ��� */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(deleteCount);
		data.writeInt(allianceID);
		data.writeByte(reinforce);
		data.writeInt(dayCount);
		bytesWriteHorn(data);
		bytesWriteFleets(data);
		bytesWriteRecords(data);
		bytesWriteDinationMap(data);
		bytesWriteFightEvent(data);
		bytesWriteUpShip(data);
	}

	public void bytesWriteHorn(ByteBuffer data)
	{
		horn.bytesWrite(data);
	}
	public void bytesWriteFleets(ByteBuffer data)
	{
		data.writeByte(fleets.size());
		for(int i=0;i<fleets.size();i++)
		{
			StockFleet fleet=(StockFleet)fleets.get(i);
			data.writeShort(fleet.getSid());
			data.writeInt(fleet.getCount());
		}
	}
	public void bytesWriteRecords(ByteBuffer data)
	{
		data.writeByte(records.size());
		for(int i=0;i<records.size();i++)
		{
			DinationRecord record=(DinationRecord)records.get(i);
			record.bytesWrite(data);
		}

	}
	public void bytesWriteDinationMap(ByteBuffer data)
	{
		int[] keys=dinationMap.keyArray();
		data.writeByte(dinationMap.size());
		OrderList list=null;
		for(int i=0;i<dinationMap.size();i++)
		{
			data.writeInt(keys[i]);
			list=(OrderList)dinationMap.get(keys[i]);
			data.writeByte(list.size());
			for(int k=0;k<list.size();k++)
			{
				StockFleet fleet=(StockFleet)list.get(k);
				data.writeShort(fleet.getSid());
				data.writeInt(fleet.getCount());
			}

		}

	}
	public void bytesWriteFightEvent(ByteBuffer data)
	{
		data.writeByte(fightEvent.size());
		for(int i=0;i<fightEvent.size();i++)
		{
			data.writeInt((Integer)fightEvent.get(i));
		}

	}

	public void bytesWriteUpShip(ByteBuffer data)
	{
		data.writeInt(upship[0]);
		data.writeByte(upship[1]);
		data.writeShort(upship[2]);
		data.writeInt(upship[3]);
		data.writeInt(upship[4]);
		data.writeInt(upship1[0]);
		data.writeByte(upship1[1]);
		data.writeShort(upship1[2]);
		data.writeInt(upship1[3]);
		data.writeInt(upship1[4]);
	}
	public void showBytesWriteFleets(ByteBuffer data)
	{
		int top=data.top();
		data.writeByte(fleets.size());
		int zero=0;
		for(int i=0;i<fleets.size();i++)
		{
			StockFleet fleet=(StockFleet)fleets.get(i);
			if(fleet.getCount()>0)
			{
				data.writeInt(fleet.getCount());
				data.writeShort(fleet.getSid());
			}
			else
			{
				zero++;
			}

		}
		int top1=data.top();
		data.setTop(top);
		data.writeByte(fleets.size()-zero);
		data.setTop(top1);
	}
	// =====================read==============================
	public void bytesReadHorn(ByteBuffer data)
	{
		horn.bytesRead(data);

	}
	public void bytesReadFleets(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			StockFleet fleet=new StockFleet(data.readUnsignedShort(),
				data.readInt());
			fleets.add(fleet);
		}
	}
	public void bytesReadRecords(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			DinationRecord record=new DinationRecord();
			record.bytesRead(data);
			records.add(record);
		}

	}
	public void bytesReadDinationMap(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			int key=data.readInt();
			OrderList list=new OrderList();
			int size=data.readUnsignedByte();
			for(int k=0;k<size;k++)
			{
				StockFleet fleet=new StockFleet(data.readUnsignedShort(),
					data.readInt());
				list.add(fleet);
			}
			dinationMap.put(key,list);
		}

	}
	public void bytesReadFightEvent(ByteBuffer data)
	{
		int len=data.readUnsignedByte();
		// System.out.println("--FightEvent--read----len----------:"+len);
		for(int i=0;i<len;i++)
		{
			fightEvent.add(data.readInt());
		}
	}
	public void bytesReadUpShip(ByteBuffer data)
	{
		upship[0]=data.readInt();
		upship[1]=data.readUnsignedByte();
		upship[2]=data.readUnsignedShort();
		upship[3]=data.readInt();
		upship[4]=data.readInt();
		upship1[0]=data.readInt();
		upship1[1]=data.readUnsignedByte();
		upship1[2]=data.readUnsignedShort();
		upship1[3]=data.readInt();
		upship1[4]=data.readInt();
	}

	public void showBytesWrite(ByteBuffer data)
	{
		bytesWrite(data);
	}

	public int getAllianceID()
	{
		return allianceID;
	}

	public void setAllianceID(int allianceID)
	{
		this.allianceID=allianceID;
	}

	public int getReinforce()
	{
		return reinforce;
	}

	public void setReinforce(int reinforce)
	{
		this.reinforce=reinforce;
	}
	public WarHorn getHorn()
	{
		return horn;
	}

	public void setHorn(WarHorn horn)
	{
		this.horn=horn;
	}

	public OrderList getFleets()
	{
		return fleets;
	}

	public void setFleets(OrderList fleets)
	{
		this.fleets=fleets;
	}

	public OrderList getRecords()
	{
		return records;
	}

	public void setRecords(OrderList records)
	{
		this.records=records;
	}

	public IntKeyHashMap getDinationMap()
	{
		return dinationMap;
	}

	public void setDinationMap(IntKeyHashMap dinationMap)
	{
		this.dinationMap=dinationMap;
	}

	public OrderList getFightEvent()
	{
		return fightEvent;
	}

	public void setFightEvent(OrderList fightEvent)
	{
		this.fightEvent=fightEvent;
	}

	public RankData[] getRank()
	{
		return rank;
	}

	public void setRank(RankData[] rank)
	{
		this.rank=rank;
	}

	public int getDeleteCount()
	{
		return deleteCount;
	}

	public void setDeleteCount(int deleteCount)
	{
		this.deleteCount=deleteCount;
	}

	public RankComparator getCom()
	{
		return com;
	}

	public void setCom(RankComparator com)
	{
		this.com=com;
	}

	public int[] getUpship()
	{
		return upship;
	}
	public int[] getUpship1()
	{
		return upship1;
	}

	public void setUpship(int[] upship)
	{
		this.upship=upship;
	}

	public void setUpship1(int[] upship)
	{
		this.upship1=upship;
	}

	public int getDayCount()
	{
		return dayCount;
	}

	public void setDayCount(int dayCount)
	{
		this.dayCount=dayCount;
	}

}
