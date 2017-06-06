package foxu.sea.equipment;

import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.AttrAdjustment;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.kit.SeaBackKit;

/**
 * 装备管理器
 * 
 * @author Alan
 */
/**
 * @author Administrator
 * 
 */
public class EquipList
{

	public static int SYS_OPEN_LEVEL=20;
	public static int MAX_CAPACITY=100;
	public static int DEFAULT_CAPACITY=150;
	public static int LEVEL_UP_PROP=2021;
	public static int EQUIP_COST_GEMS=50;
	/** 装备sid段 */
	public static final int EQUIP_START_SID=40000;
	/** 装备进阶材料sid段 */
	public static final int EQUIP_STUFF_START_SID=41200;
	/** 经验道具作为装备时所属的舰船类型 */
	public static int EXP_STUFF_TYPE=1000;
	/** 装备仓库 */
	ArrayList equips=new ArrayList();
	/** 进阶材料 */
	IntKeyHashMap qualityMap=new IntKeyHashMap();
	/** 所属玩家 */
	Player player;
	/** 包裹的长度数组 */
	int[] equLength=new int[]{30,30,30,30,30};

	/** 装备包裹大小 */
	int capacity=DEFAULT_CAPACITY;

	/** 获取装备 */
	public Equipment getEquip(int uid)
	{
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&equip.getUid()==uid) return equip;
		}
		return null;
	}

	/** 获取已装备的物件 */
	public Equipment getEquiped(int shipType,int equipType)
	{
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&equip.isEquiped()&&equip.getShipType()==shipType
				&&equip.getEquipType()==equipType) return equip;
		}
		return null;
	}

	/** 获取未装配的同阶装备 */
	public ArrayList getSameQualityUnequip(int sid)
	{
		ArrayList al=new ArrayList();
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&!equip.isEquiped()&&equip.getSid()==sid)
				al.add(equip);
		}
		return al;
	}

	/** 获取所有已装备的物件提升的抗性 */
	public int[] getEquipedResist(int shipType)
	{
		int[] resist=new int[5];
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&equip.isEquiped()&&equip.getShipType()==shipType)
			{
				for(int j=0;j<equip.getResist().length;j++)
				{
					resist[j]+=equip.getResist()[j];
				}
			}
		}
		return resist;
	}

	/** 获取所有已装备的物件提升的加成 */
	public int[] getEquipedAttach(int shipType)
	{
		int[] attach=new int[5];
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&equip.isEquiped()&&equip.getShipType()==shipType)
			{
				for(int j=0;j<equip.getAttach().length;j++)
				{
					attach[j]+=equip.getAttach()[j];
				}
			}
		}
		return attach;
	}

	/** 装备物件 */
	public String equip(int uid,CreatObjectFactory objectFactory)
	{
		if(!isSystemOpen()) return "equip not open";// /////
		Equipment equip=getEquip(uid);
		if(equip==null) return "equip not exist";// /////
		if(equip.getShipType()==EXP_STUFF_TYPE||equip.isEquiped())
			return "can not equip";// /////
		// 卸下旧物件
		Equipment oldEquip=unEquip(equip.getShipType(),equip.getEquipType(),
			false,objectFactory);
		// 放置新物件
		equip.setEquiped(true);
		if(oldEquip==null&&checkEquiped())
			addChangeValue(equip,player.getAdjstment());
		else
			SeaBackKit.resetPlayerSkill(player,objectFactory);
		return null;
	}

	/** 卸下旧物件 */
	public Equipment unEquip(int shipType,int equipType,boolean isFlush,CreatObjectFactory objectFactory)
	{
		Equipment oldEquip=getEquiped(shipType,equipType);
		if(oldEquip!=null&&oldEquip.isEquiped())
		{
			// 卸下物件
			oldEquip.setEquiped(false);
			// 重置属性值
			if(isFlush) SeaBackKit.resetPlayerSkill(player,objectFactory);
		}
		return oldEquip;
	}

	/** 通过消耗装备提升经验:经验道具划分为不可装备、升级、进阶的物件，仅提供经验 */
	public String incrExp(int uid,IntList equips,CreatObjectFactory objectFactory)
	{
		Equipment equip=getEquip(uid);
		if(equip==null) return "equip not exist";
		if(!equip.isUpgr()||equip.getMaxLevel()<=equip.getLevel())
			return "equip can not level up";// /////
		int exps=0;
		int lv=equip.getLevel();
		for(int i=0;i<equips.size();i++)
		{
			if(equips.get(i)==uid) return "equip can not feed itself";// ////
			Equipment rmvEquip=getEquip(equips.get(i));
			if(rmvEquip==null||rmvEquip.isEquiped()) continue;
			exps+=rmvEquip.getProvideExp();
		}
		if(player.getBundle().getPropCount(LEVEL_UP_PROP)<equip
			.getHonorNum(exps)) return "not enough prop";
		player.getBundle().decrProp(LEVEL_UP_PROP,equip.getHonorNum(exps));
		for(int i=0;i<equips.size();i++)
		{
			removeEquip(equips.get(i));
		}
		equip.addExp(exps);
		if(equip.getLevel()>lv)
			SeaBackKit.resetPlayerSkill(player,objectFactory);
		return null;
	}

	/** 品阶提升 */
	public String qualityUp(int uid,CreatObjectFactory objectFactory)
	{
		Equipment equip=getEquip(uid);
		if(equip==null) return "equip not exist";
		if(equip.getMaxLevel()>equip.getLevel()
			||Equipment.factory.getSample(equip.getNextQuility())==null)
			return "equip can not quality up";// ////
		if(equip.checkQualityUp(player))
		{
			equip.reduceQualityStuff(player);
			removeEquip(uid);
			String msg=addEquipment(equip.getNextQuility(),uid);
			if(msg!=null) return msg;
			Equipment newEquip=getEquip(uid);
			if(newEquip.getLevelExps()!=null)
				newEquip.addExp(newEquip.getLevelExps()[equip.getLevel()-2]);
			if(equip.isEquiped())
			{
				newEquip.setEquiped(equip.isEquiped());
				SeaBackKit.resetPlayerSkill(player,objectFactory);
			}
		}
		else
			return "prop or resource not enough";// ////
		return null;
	}

	public boolean isSystemOpen()
	{
		if(player.getLevel()>=SYS_OPEN_LEVEL) return true;
		return false;
	}

	/** 获取某种品阶升级道具的数量 */
	public int getQualityStuffCount(int sid)
	{
		if(qualityMap.get(sid)==null) return 0;
		return (Integer)qualityMap.get(sid);
	}
	
	/** 减少某种品阶升级道具的数量 */
	public void decrQualityStuff(int sid,int num)
	{
		int count=(Integer)qualityMap.get(sid);
		count-=num;
		if(count<0) count=0;
		qualityMap.put(sid,count);
	}

	/** 获取某种品阶升级道具上一阶的sid */
	public int getLastQualityStuffSid(int sid)
	{
		for(int i=0;i<Equipment.QUALITY_STUFFS.length;i+=3)
		{
			if(Equipment.QUALITY_STUFFS[i]==sid&&i-3>=0)
				return Equipment.QUALITY_STUFFS[i-3];
		}
		return 0;
	}
	
	/** 增加某种品阶升级道具的数量 */
	public String incrQualityStuff(int sid,int num)
	{
		boolean isExist=false;
		for(int i=0;i<Equipment.QUALITY_STUFFS.length;i+=3)
		{
			if(Equipment.QUALITY_STUFFS[i]==sid) isExist=true;
		}
		if(!isExist) return "quality stuff not exist";// ////
		int count=0;
		if(qualityMap.get(sid)!=null)
		{
			count=(Integer)qualityMap.get(sid);
		}
		if(Integer.MAX_VALUE-count<num) num=Integer.MAX_VALUE-count;
		count+=num;
		qualityMap.put(sid,count);
		return null;
	}

	/** 品阶道具合成 */
	public String combineQualityStuff(int sid)
	{
		for(int i=0;i<Equipment.QUALITY_STUFFS.length;i+=3)
		{
			if(Equipment.QUALITY_STUFFS[i]==sid)
			{
				if(Equipment.QUALITY_STUFFS.length<i+3
					||getQualityStuffCount(sid)<Equipment.QUALITY_STUFFS[i+1]
					||!Resources.checkResources(0,0,0,0,
						Equipment.QUALITY_STUFFS[i+2],player.getResources()))
					return "stuff can not combine";// ////
				decrQualityStuff(sid,Equipment.QUALITY_STUFFS[i+1]);
				Resources.reduceResources(player.getResources(),0,0,0,0,
					Equipment.QUALITY_STUFFS[i+2],player);
				String msg=incrQualityStuff(Equipment.QUALITY_STUFFS[i+3],1);
				if(msg!=null) return msg;
				return null;
			}
		}
		return "stuff can not combine";
	}

	/** 扩展装备背包 */
	public int enlargeCapacity(int addLength,int types)
	{
		// capacity+=addLength;
		// if(capacity>MAX_CAPACITY)
		// {
		// capacity=MAX_CAPACITY;
		// }
		// int length=getTypeNum(types);
		types=getIndexByShipType(types);
		equLength[types]+=addLength;
		if(equLength[types]>=MAX_CAPACITY) equLength[types]=MAX_CAPACITY;
		return equLength[types];

	}

	/** 新增一个装备,uid为0时默认生成一个新的uid，否则为传入值 */
	public String addEquipment(int equipSid,int uid)
	{
		Equipment equip=(Equipment)Equipment.factory.newSample(equipSid);
		if(equip==null) return "equip not exist";
		int currentLength=getSidNum(equipSid);
		int listIndex=getIndexByShipType(equip.getShipType());
		if(currentLength>=equLength[listIndex])
			return "equip storehouse is full";
		if(uid<=0) uid=UidGenerator.getInstance().getUid();
		equip.setUid(uid);
		equips.add(equip);
		return null;
	}

	public String addEquipment(Equipment equip,int uid)
	{
		if(equip==null) return "equip not exist";
		int currentLength=getSidNum(equip.getSid());
		int listIndex=getIndexByShipType(equip.getShipType());
		if(currentLength>=equLength[listIndex])
			return "equip storehouse is full";
		if(uid<=0) uid=UidGenerator.getInstance().getUid();
		equip.setUid(uid);
		equips.add(equip);
		return null;
	}

	/** 获取当前背包占用 */
	public int getCount()
	{
		int currentLength=0;
		for(int i=0;i<equips.size();i++)
		{
			if(equips.get(i)!=null) currentLength++;
		}
		return currentLength;
	}

	/** 重新设置属性修正值 */
	public void resetChangeValue(AttrAdjustment adjust)
	{
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&equip.isEquiped()
				&&equip.getShipType()!=EXP_STUFF_TYPE)
				addChangeValue(equip,adjust);
		}
	}

	/** 设置属性修正值 */
	private void addChangeValue(Equipment equip,AttrAdjustment adjustment)
	{
		float[] upScore=equip.getUpScores();
		adjustment.add(equip.getShipType(),(int)upScore[0],(int)upScore[1],
			equip.isFix());
	}

	/** 移除装备 */
	public Equipment removeEquip(int uid)
	{
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip.getUid()==uid)
			{
				return (Equipment)equips.remove(i);
			}
		}
		return null;
	}

	/** 出售装备 */
	public String saleEquip(int uid)
	{
		Equipment equip=getEquip(uid);
		if(equip==null||equip.isEquiped()) return "equip can not sale";// ////
		removeEquip(uid);
		Resources.addResources(player.getResources(),0,0,0,0,
			equip.getSaleMoney(),player);
		return null;
	}

	/** 检查当前所有已装备情况是否合法 */
	public boolean checkEquiped()
	{
		boolean isLegal=true;
		IntKeyHashMap shipsMap=new IntKeyHashMap();
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip==null||!equip.isEquiped()) continue;
			IntKeyHashMap positionMap=(IntKeyHashMap)shipsMap.get(equip
				.getShipType());
			if(positionMap==null)
			{
				positionMap=new IntKeyHashMap();
				positionMap.put(equip.getEquipType(),equip);
				shipsMap.put(equip.getShipType(),positionMap);
			}
			else
			{
				Equipment current=(Equipment)positionMap.get(equip
					.getEquipType());
				if(current!=null)
				{
					// 以最后添加的装备为主
					current.setEquiped(false);
					isLegal=false;
				}
				positionMap.put(equip.getEquipType(),equip);
			}
		}
		return isLegal;
	}

	// public int getCapacity(){
	// return capacity;
	// }

	/** 从字节数组中反序列化获得对象的域 */
	public synchronized Object bytesRead(ByteBuffer data)
	{
		// 进阶材料
		int qualityLength=data.readUnsignedByte();
		for(int i=0;i<qualityLength;i++)
		{
			String msg=incrQualityStuff(data.readUnsignedShort(),
				data.readInt());
			if(msg!=null) throw new DataAccessException(0,msg);
		}
		// 仓库
		// capacity=data.readShort();
		int len=data.readUnsignedByte();
		// int capacities=0;
		for(int i=0;i<len;i++)
		{
			equLength[i]=data.readInt();
			// capacities+=equLength[i];
		}
		// capacity=capacities;
		int n=data.readUnsignedShort();
		equips.clear();
		for(int i=0;i<n;i++)
			equips.add(Equipment.bytesReadEquip(data));
		// if(listener!=null) listener.change(this,FLUSH);
		return this;
	}

	/** 将对象的域序列化成字节数组，参数data为要写入的字节缓存 */
	public synchronized void bytesWrite(ByteBuffer data)
	{
		// 进阶材料
		int[] stuffs=qualityMap.keyArray();
		int length=0;
		int top=data.top();
		data.writeByte(length);
		for(int i=0;i<stuffs.length;i++)
		{
			int num=getQualityStuffCount(stuffs[i]);
			if(num>0)
			{
				data.writeShort(stuffs[i]);
				data.writeInt(num);
				length++;
			}
		}
		int newTop=data.top();
		data.setTop(top);
		data.writeByte(length);
		data.setTop(newTop);
		// 仓库
		// data.writeShort(capacity);
		data.writeByte(equLength.length);
		for(int i=0;i<equLength.length;i++)
		{
			data.writeInt(equLength[i]);
		}
		int n=getCount();
		data.writeShort(n);
		Equipment e;
		for(int i=0;i<equips.size();i++)
		{
			e=(Equipment)equips.get(i);
			if(e!=null) e.bytesWrite(data);
		}
	}
	public synchronized void showBytesWrite(ByteBuffer data)
	{
		// testData();
		// 新手引导步骤
		data.writeByte(getEquipFollow());
		// 进阶材料
		int[] stuffs=Equipment.QUALITY_STUFFS;
		data.writeByte(stuffs.length/3);
		for(int i=0;i<stuffs.length;i+=3)
		{
			data.writeShort(stuffs[i]);
			data.writeInt(getQualityStuffCount(stuffs[i]));
		}
		// 仓库容量
		// data.writeShort(capacity);
		data.writeByte(equLength.length);
		for(int i=0;i<equLength.length;i++)
		{
			data.writeShort(equLength[i]);
		}
		// 装备信息
		int top=data.top();
		data.writeShort(0);
		int length=0;
		int n=equips.size();
		Equipment e;
		ArrayList expStuffs=new ArrayList();
		for(int i=0;i<n;i++)
		{
			e=(Equipment)equips.get(i);
			if(e!=null)
			{
				if(e.getShipType()==EXP_STUFF_TYPE)
				{
					expStuffs.add(e);
					continue;
				}
				e.bytesWrite(data);
				length++;
			}
		}
		if(length>0)
		{
			int current=data.top();
			data.setTop(top);
			data.writeShort(length);
			data.setTop(current);
		}
		data.writeShort(expStuffs.size());
		for(int i=0;i<expStuffs.size();i++)
		{
			e=(Equipment)expStuffs.get(i);
			data.writeInt(e.getUid());
			data.writeShort(e.getSid());
		}
	}
	/** 跨服序列化 写*/
	public void crossBytesWrite(ByteBuffer data)
	{
		int len=0;
		int top=data.top();
		data.writeShort(len);
		for(int i=0;i<equips.size();i++)
		{
			Equipment e=(Equipment)equips.get(i);
			if(e!=null&&e.isEquiped)
			{
				e.bytesWrite(data);
				len++;
			}
		}
		int nowTop=data.top();
		data.setTop(top);
		data.writeShort(len);
		data.setTop(nowTop);
	}
	/** 跨服序列化  读*/
	public void crossBytesRead(ByteBuffer data)
	{
		equips.clear();
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			equips.add(Equipment.bytesReadEquip(data));
		}
	}

	/** 检查每个sid的类型的背包的数量 */
	public int getSidNum(int sid)
	{
		int count=0;
		Equipment equip=(Equipment)Equipment.factory.getSample(sid);
		int type=equip.getShipType();
		for(int i=0;i<equips.size();i++)
		{
			Equipment equ=(Equipment)equips.get(i);
			if(equ.getShipType()==type) count++;
		}
		return count;
	}
	/** 检查每个类型的背包中物品的数量 */
	public int getTypeNum(int type)
	{
		int count=0;
		for(int i=0;i<equips.size();i++)
		{
			Equipment equ=(Equipment)equips.get(i);
			if(equ.getShipType()==type) count++;
		}
		return count;
	}
	/** 得到当前背包的数量 */
	public int getEquNum(int types)
	{
		types=getIndexByShipType(types);
		return equLength[types];
	}

	/** 增加进阶材料或者装备 */
	public String addEquipOrStuff(int sid,int num)
	{
		String msg=null;
		// 如果是进阶材料
		if(sid>=EQUIP_STUFF_START_SID)
		{
			msg=player.getEquips().incrQualityStuff(sid,num);
			return msg;
		}
		for(int j=0;j<num;j++)
		{
			Equipment equip=(Equipment)Equipment.factory.getSample(sid);
			if(equip!=null)
			{
				msg=player.getEquips().addEquipment(sid,0);
			}
		}
		return msg;
	}

	/** 通过舰船类型获取从0开始的连续索引 */
	public int getIndexByShipType(int shipType)
	{
		if(shipType==EXP_STUFF_TYPE) return equLength.length-1;
		int shipTypeIndex=shipType;
		shipType=0;
		while(shipTypeIndex!=1)
		{
			shipTypeIndex=shipTypeIndex>>1;
			shipType++;
		}
		return shipType;
	}

	/** 获取装备系统引导 */
	public int getEquipFollow()
	{
		String follow=player.getAttributes(PublicConst.EQUIP_SYS_FOLLOW);
		if(follow==null||!follow.matches("\\d+"))
			return 0;
		else
			return Integer.valueOf(follow);
	}

	/** 获取进阶材料合成信息{本次消耗的数量,下一等级的sid} */
	public int[] getStuffCombineInfo(int sid)
	{
		int[] info=new int[2];
		for(int i=0;i<Equipment.QUALITY_STUFFS.length;i+=3)
		{
			if(Equipment.QUALITY_STUFFS[i]==sid)
			{
				if(Equipment.QUALITY_STUFFS.length>i+3)
				{
					info[0]=Equipment.QUALITY_STUFFS[i+1];
					info[1]=Equipment.QUALITY_STUFFS[i+3];
				}
			}
		}
		return info;
	}
	
	/** 获取相同品阶装备的数量 */
	public int getSameSidCount(int sid)
	{
		int count=0;
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&equip.getSid()==sid)
				count++;
		}
		return count;
	}
	
	/** 获取任意相同品阶装备 */
	public Equipment getOneEquipment(int sid)
	{
		for(int i=0;i<equips.size();i++)
		{
			Equipment equip=(Equipment)equips.get(i);
			if(equip!=null&&equip.getSid()==sid)
				return equip;
		}
		return null;
	}
	
	public Player getPlayer()
	{
		return player;
	}

	public void setPlayer(Player player)
	{
		this.player=player;
	}

	
	public ArrayList getEquips()
	{
		return equips;
	}

	
	public void setEquips(ArrayList equips)
	{
		this.equips=equips;
	}

	
	public IntKeyHashMap getQualityMap()
	{
		return qualityMap;
	}

	
	public void setQualityMap(IntKeyHashMap qualityMap)
	{
		this.qualityMap=qualityMap;
	}

}
