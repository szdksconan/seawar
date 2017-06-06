package foxu.sea;

import mustang.math.MathKit;
import mustang.math.Random;
import mustang.math.Random1;
import mustang.set.IntKeyHashMap;
import foxu.fight.FightScene;
import foxu.sea.fight.FightSceneFactory;
import foxu.sea.fight.FightShowEventRecord;
import foxu.sea.fight.Fleet;
import foxu.sea.fight.FleetGroup;

/**
 * NPC创建舰队类
 * @author lihongji
 *
 */
public class HCityNpcIsland extends NpcIsland
{
	/** 所有参战船只 */
	int group;
//	FleetGroup fleetGroup;
	/**
	 * 开始联合舰队关卡战斗
	 * 
	 * @param attacker 进攻方
	 */
	public Object[] fight(FleetGroup attacker)
	{
		FightScene scene=FightSceneFactory.factory.create(attacker,
			fleetGroup);
		FightShowEventRecord r=FightSceneFactory.factory.fight(scene,null);
		Object[] object=new Object[2];
		object[0]=scene;
		object[1]=r;
		return object;
	}
	/***
	 * NPC的舰队数组
	 * @param player
	 * @param checkPointSid
	 * @param group
	 * @return
	 */
	public FleetGroup[] createFleetGroup(Player player,int checkPointSid,int group)
	{
		FleetGroup[] fleetGroups=new FleetGroup[group];
		Integer randomseed=(Integer)player.getGetrandom().get(checkPointSid);//得到随机变量
		if(randomseed==null)//如果为空的话  重新创建一个随机变量
		{
			IntKeyHashMap getrandom=player.getGetrandom();
			randomseed=MathKit.randomInt();
    		getrandom.put(checkPointSid,randomseed);
		}
		//随机NPC舰队
		Random r=new Random1(randomseed);
		for(int k=0;k<group;k++)
		{
			fleetGroup=new FleetGroup();
			if(fix)
			{
				Fleet fleet;
				for(int i=0;i<fleetGroupCfg.length;i+=dataLength)
				{
					fleet=new Fleet((Ship)Ship.factory
						.newSample(fleetGroupCfg[i]),fleetGroupCfg[i+1]);
					fleetGroup.setFleet(fleetGroupCfg[i+2],fleet);
				}
			}
			else
			{
				fleetGroup=new FleetGroup();
				int length=fleetGroupCfg.length/dataLength;
				Fleet[] fleets=new Fleet[FleetGroup.MAX_FLEET];
				int i=0,rd=0,num;
				int rdRange;
				for(i=fleets.length-1;i>=0;i--)
				{
					rd=r.randomValue(0,length)*dataLength;
					num=fleetGroupCfg[rd+1];
					rdRange=fleetGroupCfg[rd+3];
					num=(int)(Math.ceil(num
						+(int)r.randomValue(-rdRange,rdRange)));
					fleets[i]=new Fleet(
						(Ship)Ship.factory.newSample(fleetGroupCfg[rd]),num);
				}
				/** 已用随机数列表 */
				int[] rdList=new int[FleetGroup.MAX_FLEET];
				for(i=rdList.length-1;i>=0;i--)
				{
					rdList[i]=-1;
				}
				int j=0,nullIndex=0;
				i=0;
				a1:while(i<FleetGroup.MAX_FLEET)
				{
					rd=r.randomValue(0,FleetGroup.MAX_FLEET);
					for(j=rdList.length-1;j>=0;j--) // 判断缓存列表中有没有这个随机数
					{
						if(rdList[j]<0) nullIndex=j; // 记录一个空位的index,下边用来保存随机数
						if(rdList[j]==rd) continue a1; // 如果重复,结束当前循环,继续外层循环,重新生成随机数
					}
					rdList[nullIndex]=rd;
					fleets[i].setLocation(rd);
					i++;
				}
				for(i=0,j=FleetGroup.MAX_FLEET;i<cityDefence.length;i+=dataLength)
				{
					num=cityDefence[i+1];
					num=(int)(Math.ceil(num
						*(100+r.randomValue(0.0f,cityDefence[i+3]))/100));
					fleets[j]=new Fleet((Ship)Ship.factory
						.newSample(cityDefence[i]),num);
					fleets[j++].setLocation(cityDefence[i+2]);
				}

				for(i=0;i<fleets.length;i++)
				{
					fleetGroup.setFleet(fleets[i].getLocation(),fleets[i]);
				}
			}
			fleetGroups[k]=fleetGroup;
		}
		return fleetGroups;
	}
	public int getGroup()
	{
		return group;
	}

	
	public void setGroup(int group)
	{
		this.group=group;
	}

//	public FleetGroup getFleetGroup()
//	{
//		return fleetGroup;
//	}

	
//	public void setFleetGroup(FleetGroup fleetGroup)
//	{
//		this.fleetGroup=fleetGroup;
//		
//	}
	
	
}
