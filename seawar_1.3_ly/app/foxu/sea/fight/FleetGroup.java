/**
 * 
 */
package foxu.sea.fight;

import mustang.io.ByteBuffer;
import mustang.set.IntList;
import mustang.util.SampleFactory;
import foxu.fight.Ability;
import foxu.fight.FightScene;
import foxu.sea.Player;
import foxu.sea.Ship;
import foxu.sea.officer.OfficerBattleHQ;

/**
 * 舰队群
 * 
 * @author rockzyt
 */
public class FleetGroup implements Cloneable
{

	/** 计算丢失兵力type HURT_TROOPS=1伤兵计算 AUTO_ADD_SHIP=2补充城防兵力计算 */
	public static final int HURT_TROOPS=1,AUTO_ADD_SHIP=2;
	/* static fields */
	/** 群最大成员数 */
	public static final int MAX=9;
	/** 样本工厂 */
	public static SampleFactory factory=new SampleFactory();
	/** 舰队群最大舰队数量 */
	public static final int MAX_FLEET=6;

	/* fields */
	/** 最大成员数 */
	int max=MAX;
	/** 舰队阵法 */
	Fleet[] fleets=new Fleet[max];
	/** 随阵军官信息 */
	OfficerBattleHQ officerBattle=new OfficerBattleHQ();
	
	/* porperties */
	/** 设置舰队最大数量 */
	public void setMax(int max)
	{
		this.max=max;
	}
	/** 获得舰队最大数量 */
	public int getMax()
	{
		return max;
	}
	
	/**boss恢复兵力*/
	public void resetBossShips()
	{
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null&&fleets[i].getStartNum()>0
				&&fleets[i].getShip()!=null)
			{
				fleets[i].resetBossShips();
			}
		}
	}

	/** 获得某个sid的船舰数量 */
	public int getShipBySid(int sid)
	{
		int num=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null&&fleets[i].getShip().getSid()==sid)
			{
				num+=fleets[i].getNum();
			}
		}
		return num;
	}
	
	/**最开始的总船只数量*/
	public int getMaxNum()
	{
		int num=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null&&fleets[i].getStartNum()>0
				&&fleets[i].getShip()!=null)
			{
				num+=fleets[i].getStartNum();
			}
		}
		return num;
	}

	/** 总船只数量 */
	public int nowTotalNum()
	{
		int num=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null&&fleets[i].getNumber()>0
				&&fleets[i].getShip()!=null)
			{
				num+=fleets[i].getNumber();
			}
		}
		return num;
	}

	/** 是否存在兵力 */
	public boolean existShip()
	{
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null&&fleets[i].getNumber()>0
				&&fleets[i].getShip()!=null) return true;
		}
		return false;
	}

	/* methods */
	/** 撤销舰队群 autoAdd是否补充城防 */
	public void cancel(Player p,boolean autoAdd)
	{
		if(p==null) return;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null)
			{
				p.getIsland().addTroop(fleets[i].getShip().getSid(),
					fleets[i].getNumber(),p.getIsland().getTroops());
				fleets[i]=null;
			}
		}
		// 自动补充舰队
		if(autoAdd) p.autoAddMainGroup();
	}
	/**
	 * 替换舰队 将已有舰队归还给Player
	 * 
	 * @param fleets 舰队数组
	 * @param p 舰队所有者
	 */
	public void changeFleets(Fleet[] fleets,Player p)
	{
		cancel(p,true);
		this.fleets=fleets;
	}
	/** 获得舰队群中的所有舰队 */
	public Fleet[] getArray()
	{
		return fleets;
	}
	/**
	 * 在舰队群的指定位置设置一个fighter
	 * 
	 * @param index 位置下标
	 * @param f Fighter
	 * @return 返回这个位置上已有的Fleet,返回null表示没有Fleet
	 */
	public Fleet setFleet(int index,Fleet f)
	{
		Fleet old=fleets[index];
		f.setLocation(index);
		fleets[index]=f;
		return old;

		// for(int i=0;i<fleets.length;i++)
		// {
		// if(fleets!=null)
		// {
		// old=fleets[i];
		// fleets[i]=f;
		// break;
		// }
		// }
		// return old;
	}
	// /**
	// * 移除指定位置的fighter
	// *
	// * @param index 指定位置
	// * @return 返回移除的fighter
	// */
	// public Fleet remove(int index)
	// {
	// for(int i=0;i<fleets.length;i++)
	// {
	// if(fleets[i]!=null&&fleets[i].getLocation()==index)
	// {
	// Fleet f=fleets[i];
	// f.setLocation(-1);
	// fleets[i]=null;
	// return f;
	// }
	// }
	// return null;
	// }
	/** 移除主力舰队中的城防建筑 */
	public void realseDefendBuild()
	{
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// 如果是城防类型就忽略 设置为空
			if(fleets[i].ship==null
				||fleets[i].ship.getPlayerType()==Ship.POSITION_AIR
				||fleets[i].ship.getPlayerType()==Ship.POSITION_MISSILE
				||fleets[i].ship.getPlayerType()==Ship.POSITION_FIRE)
			{
				fleets[i]=null;
				continue;
			}
		}
	}
	
	/**当前损失的兵力数量*/
	public int hurtListNum()
	{
		int lostNum = 0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// 如果是城防类型就忽略 并且设置为空
			if(fleets[i].ship==null
				||fleets[i].ship.getPlayerType()==Ship.POSITION_AIR
				||fleets[i].ship.getPlayerType()==Ship.POSITION_MISSILE
				||fleets[i].ship.getPlayerType()==Ship.POSITION_FIRE)
			{
				continue;
			}
			lostNum+=fleets[i].lostNum();
		}
		return lostNum;
	}

	/** 将伤兵数据 组装成一个list返回出去 type=1伤兵计算 type=2城防兵力的自动补充 */
	public IntList hurtList(int type)
	{
		IntList list=new IntList();
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// 如果是城防类型就忽略 并且设置为空
			if(fleets[i].ship==null
				||fleets[i].ship.getPlayerType()==Ship.POSITION_AIR
				||fleets[i].ship.getPlayerType()==Ship.POSITION_MISSILE
				||fleets[i].ship.getPlayerType()==Ship.POSITION_FIRE)
			{
				continue;
			}
			int num=fleets[i].lostNum();
			// 如果是补充城防兵力的
			if(type==AUTO_ADD_SHIP)
			{
				num=fleets[i].lostAutoNum();
			}
			if(num==0) continue;
			boolean have=false;
			if(num>0&&type!=AUTO_ADD_SHIP)
			{
				// 有这个sid了就合并为一项
				for(int j=0;j<list.size();j+=2)
				{
					int sid=list.get(j);
					int haveNum=list.get(j+1);
					if(sid==fleets[i].getShip().getSid())
					{
						list.set(haveNum+num,j+1);
						have=true;
					}
				}
			}
			if(!have)
			{
				list.add(fleets[i].getShip().getSid());
				list.add(num);
			}
			if(type==AUTO_ADD_SHIP) list.add(fleets[i].getLocation());
		}
		return list;
	}
	/** 伤兵经验值计算 */
	public int hurtTroopsExp(Player player,int time)
	{
		int allExp=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// 如果是城防类型就忽略 并且设置为空
			if(fleets[i].ship==null
				||fleets[i].ship.getPlayerType()==Ship.POSITION_AIR
				||fleets[i].ship.getPlayerType()==Ship.POSITION_MISSILE
				||fleets[i].ship.getPlayerType()==Ship.POSITION_FIRE)
			{
				fleets[i]=null;
				continue;
			}
			allExp+=fleets[i].hurtExp(player,time);
		}
		return allExp;
	}

	/** 伤兵计算 */
	public void hurtTroops(Player player,int time)
	{
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// 如果是城防类型就忽略 并且设置为空
			if(fleets[i].ship==null
				||fleets[i].ship.getPlayerType()==Ship.POSITION_AIR
				||fleets[i].ship.getPlayerType()==Ship.POSITION_MISSILE
				||fleets[i].ship.getPlayerType()==Ship.POSITION_FIRE)
			{
				fleets[i]=null;
				continue;
			}
			fleets[i].hurtTroops(player,time);
		}
	}

	/** 减少城防兵力 */
	public int reduceShipByLocation(int location,int num)
	{
		synchronized(fleets)
		{
			int reduceNum=0;
			for(int i=0;i<fleets.length;i++)
			{
				if(fleets[i]==null) continue;
				if(fleets[i].getLocation()==location)
				{
					reduceNum=fleets[i].reduceNum(num);
					fleets[i].resetLastNum();
					break;
				}
			}
			return reduceNum;
		}
	}

	/** 添加城防兵力 */
	public void addShipByLocation(int location,int num)
	{
		synchronized(fleets)
		{
			for(int i=0;i<fleets.length;i++)
			{
				if(fleets[i]==null) continue;
				if(fleets[i].getLocation()==location)
				{
					fleets[i].addNum(num);
					fleets[i].resetLastNum();
					break;
				}
				
			}
		}
	}

	public void resetLastNum()
	{
		synchronized(fleets)
		{
			for(int i=0;i<fleets.length;i++)
			{
				if(fleets[i]==null) continue;
				fleets[i].resetLastNum();
			}
		}
	}
	
	/**
	 * 获取盟战残余兵力
	 * @param losePerc 损耗百分比
	 * @return 
	 */
	public IntList getResidualShip(int losePerc,IntList loss)
	{
		IntList list=new IntList();
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null)
			{
				fleets[i].getResidualShip(list,losePerc,loss);
			}
		}
		return list;
		
	}

	public Object copy(Object obj)
	{
		FleetGroup fg=(FleetGroup)obj;
		fg.fleets=new Fleet[max];
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null) fg.fleets[i]=(Fleet)fleets[i].clone();
		}
		return obj;
	}

	public Object clone()
	{
		try
		{
			return copy(super.clone());
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException(getClass().getName()+" clone, "+e);
		}
	}

	public void bytesWrite(ByteBuffer data)
	{
		int top=data.top(); // 记录当前位置,以便返回过来重写长度
		data.writeByte(0); // 占位
		int length=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null)
			{
				fleets[i].bytesWrite(data);
				length++;
			}
		}
		if(length>0)
		{
			int current=data.top(); // 记录当前top
			data.setTop(top); // 重设top到记录fighter长度的位置,重新写入fighter长度
			data.writeByte(length);
			data.setTop(current); // 重设到当前top
		}
	}
	public Object bytesRead(ByteBuffer data)
	{
		int length=data.readUnsignedByte();
		fleets=new Fleet[max];
		for(int i=0;i<length;i++)
		{
			Fleet fleet=new Fleet();
			fleets[i]=(Fleet)fleet.bytesRead(data);
		}
		return this;
	}

	public void showBytesWrite(ByteBuffer data)
	{
		int top=data.top(); // 记录当前位置,以便返回过来重写长度
		data.writeByte(0); // 占位
		int length=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null)
			{
				fleets[i].showBytesWrite(data);
				length++;
			}
		}
		if(length>0)
		{
			int current=data.top(); // 记录当前top
			data.setTop(top); // 重设top到记录fighter长度的位置,重新写入fighter长度
			data.writeByte(length);
			data.setTop(current); // 重设到当前top
		}
	}
	
	/** 初始化友军的出阵效果,传入IntList将写入技能影响[技能sid,影响概率]  */
	public void initArmyFleet(Fleet army,IntList list)
	{
		// 军官影响
		getOfficerFleetAttr().initArmyFleet(army,list);
	}
	
	/** 初始化敌军的出阵效果,传入IntList将写入技能影响[技能sid,影响概率]  */
	public void initEnemyFleet(Fleet enemy,IntList list)
	{
		// 获取自身的技能列表
		IntList publicAbility=null;
		for(int j=fleets.length-1;j>=0;j--)
		{
			if(fleets[j]!=null&&fleets[j].getSkillList()!=null)
			{
				publicAbility=fleets[j].getSkillList();
				break;
			}
		}
		if(publicAbility==null) return;
		for(int i=0;i<publicAbility.size();i+=2)
		{
			Ability updateAbility=(Ability)FightScene.abilityFactory
				.newSample(publicAbility.get(i));
			if(!(updateAbility instanceof ContainerAbility))
				continue;
			ContainerAbility enemySkill=(ContainerAbility)updateAbility;
			if(enemySkill.getShipType()!=enemy.getType())
				continue;
			// 是否是改变敌人技能概率的技能
			if(!enemySkill.isEffectEnemySkill())
				continue;
			enemySkill.setLevel(publicAbility.get(i+1));
			int enemySkillSid=enemySkill.getAbilitySidByShipSid(enemy.getShip().getSid());
			if(enemySkillSid<=0)
				continue;
			int prop=enemySkill.getProbability();
			int index=list.indexOf(enemySkillSid);
			if(index<0)
			{
				list.add(enemySkillSid);
				list.add(0-prop);
			}
			else
			{
				list.set(list.get(index+1)-prop,index+1);
			}
		}
		// 军官影响
		getOfficerFleetAttr().initEnemyFleet(enemy,list);
	}

	/**根据当前的位置获取当前的船只的数量**/
	public  int getFleetNum(int index)
	{
		Fleet old=fleets[index];
		if(old==null) return 0;
		return old.getNum();
	}
	
	/** 获得所有船舰数量 */
	public IntList getShipIntList()
	{
		IntList list=new IntList();
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]!=null)
			{
				list.add(fleets[i].getShip().getSid());
				list.add(fleets[i].getNum());
				list.add(fleets[i].getLocation());
			}
		}
		return list;
	}
	
	/** 随阵军官信息  */
	public OfficerBattleHQ getOfficerFleetAttr()
	{
		return officerBattle;
	}
	
	public void setOfficerBattle(OfficerBattleHQ officerBattle)
	{
		this.officerBattle=officerBattle;
	}
	/** 获取死亡积分 */
	public int getDeadPoint()
	{
		int point=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			point+=fleets[i].getDeadPoint();
		}
		return point;
	}
	/**设置buff**/
	public void setBuff(IntList list)
	{
		if(fleets==null) return;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			fleets[i].setBuff(list);
		}
	}
	
}