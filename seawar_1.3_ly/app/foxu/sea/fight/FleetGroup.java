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
 * ����Ⱥ
 * 
 * @author rockzyt
 */
public class FleetGroup implements Cloneable
{

	/** ���㶪ʧ����type HURT_TROOPS=1�˱����� AUTO_ADD_SHIP=2����Ƿ��������� */
	public static final int HURT_TROOPS=1,AUTO_ADD_SHIP=2;
	/* static fields */
	/** Ⱥ����Ա�� */
	public static final int MAX=9;
	/** �������� */
	public static SampleFactory factory=new SampleFactory();
	/** ����Ⱥ��󽢶����� */
	public static final int MAX_FLEET=6;

	/* fields */
	/** ����Ա�� */
	int max=MAX;
	/** ������ */
	Fleet[] fleets=new Fleet[max];
	/** ���������Ϣ */
	OfficerBattleHQ officerBattle=new OfficerBattleHQ();
	
	/* porperties */
	/** ���ý���������� */
	public void setMax(int max)
	{
		this.max=max;
	}
	/** ��ý���������� */
	public int getMax()
	{
		return max;
	}
	
	/**boss�ָ�����*/
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

	/** ���ĳ��sid�Ĵ������� */
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
	
	/**�ʼ���ܴ�ֻ����*/
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

	/** �ܴ�ֻ���� */
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

	/** �Ƿ���ڱ��� */
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
	/** ��������Ⱥ autoAdd�Ƿ񲹳�Ƿ� */
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
		// �Զ����佢��
		if(autoAdd) p.autoAddMainGroup();
	}
	/**
	 * �滻���� �����н��ӹ黹��Player
	 * 
	 * @param fleets ��������
	 * @param p ����������
	 */
	public void changeFleets(Fleet[] fleets,Player p)
	{
		cancel(p,true);
		this.fleets=fleets;
	}
	/** ��ý���Ⱥ�е����н��� */
	public Fleet[] getArray()
	{
		return fleets;
	}
	/**
	 * �ڽ���Ⱥ��ָ��λ������һ��fighter
	 * 
	 * @param index λ���±�
	 * @param f Fighter
	 * @return �������λ�������е�Fleet,����null��ʾû��Fleet
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
	// * �Ƴ�ָ��λ�õ�fighter
	// *
	// * @param index ָ��λ��
	// * @return �����Ƴ���fighter
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
	/** �Ƴ����������еĳǷ����� */
	public void realseDefendBuild()
	{
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// ����ǳǷ����;ͺ��� ����Ϊ��
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
	
	/**��ǰ��ʧ�ı�������*/
	public int hurtListNum()
	{
		int lostNum = 0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// ����ǳǷ����;ͺ��� ��������Ϊ��
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

	/** ���˱����� ��װ��һ��list���س�ȥ type=1�˱����� type=2�Ƿ��������Զ����� */
	public IntList hurtList(int type)
	{
		IntList list=new IntList();
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// ����ǳǷ����;ͺ��� ��������Ϊ��
			if(fleets[i].ship==null
				||fleets[i].ship.getPlayerType()==Ship.POSITION_AIR
				||fleets[i].ship.getPlayerType()==Ship.POSITION_MISSILE
				||fleets[i].ship.getPlayerType()==Ship.POSITION_FIRE)
			{
				continue;
			}
			int num=fleets[i].lostNum();
			// ����ǲ���Ƿ�������
			if(type==AUTO_ADD_SHIP)
			{
				num=fleets[i].lostAutoNum();
			}
			if(num==0) continue;
			boolean have=false;
			if(num>0&&type!=AUTO_ADD_SHIP)
			{
				// �����sid�˾ͺϲ�Ϊһ��
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
	/** �˱�����ֵ���� */
	public int hurtTroopsExp(Player player,int time)
	{
		int allExp=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// ����ǳǷ����;ͺ��� ��������Ϊ��
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

	/** �˱����� */
	public void hurtTroops(Player player,int time)
	{
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// ����ǳǷ����;ͺ��� ��������Ϊ��
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

	/** ���ٳǷ����� */
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

	/** ��ӳǷ����� */
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
	 * ��ȡ��ս�������
	 * @param losePerc ��İٷֱ�
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
		int top=data.top(); // ��¼��ǰλ��,�Ա㷵�ع�����д����
		data.writeByte(0); // ռλ
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
			int current=data.top(); // ��¼��ǰtop
			data.setTop(top); // ����top����¼fighter���ȵ�λ��,����д��fighter����
			data.writeByte(length);
			data.setTop(current); // ���赽��ǰtop
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
		int top=data.top(); // ��¼��ǰλ��,�Ա㷵�ع�����д����
		data.writeByte(0); // ռλ
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
			int current=data.top(); // ��¼��ǰtop
			data.setTop(top); // ����top����¼fighter���ȵ�λ��,����д��fighter����
			data.writeByte(length);
			data.setTop(current); // ���赽��ǰtop
		}
	}
	
	/** ��ʼ���Ѿ��ĳ���Ч��,����IntList��д�뼼��Ӱ��[����sid,Ӱ�����]  */
	public void initArmyFleet(Fleet army,IntList list)
	{
		// ����Ӱ��
		getOfficerFleetAttr().initArmyFleet(army,list);
	}
	
	/** ��ʼ���о��ĳ���Ч��,����IntList��д�뼼��Ӱ��[����sid,Ӱ�����]  */
	public void initEnemyFleet(Fleet enemy,IntList list)
	{
		// ��ȡ����ļ����б�
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
			// �Ƿ��Ǹı���˼��ܸ��ʵļ���
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
		// ����Ӱ��
		getOfficerFleetAttr().initEnemyFleet(enemy,list);
	}

	/**���ݵ�ǰ��λ�û�ȡ��ǰ�Ĵ�ֻ������**/
	public  int getFleetNum(int index)
	{
		Fleet old=fleets[index];
		if(old==null) return 0;
		return old.getNum();
	}
	
	/** ������д������� */
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
	
	/** ���������Ϣ  */
	public OfficerBattleHQ getOfficerFleetAttr()
	{
		return officerBattle;
	}
	
	public void setOfficerBattle(OfficerBattleHQ officerBattle)
	{
		this.officerBattle=officerBattle;
	}
	/** ��ȡ�������� */
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
	/**����buff**/
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