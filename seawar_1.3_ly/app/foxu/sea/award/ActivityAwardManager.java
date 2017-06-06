package foxu.sea.award;

import foxu.sea.ContextVarManager;
import foxu.sea.Player;
import foxu.sea.kit.SeaBackKit;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;

/**
 * 活动(限时)奖励管理器
 * 
 * @author Alan
 * 
 */
public class ActivityAwardManager
{

	public static final int NPC_ISLAND=1,WORLD_BOSS=2,ACTIVITY_BOSS=3,
					NORMAL_POINT=4,VOID_POINT=5,HCITY=6,ARENA=7;

	IntKeyHashMap awardMap=new IntKeyHashMap();
	private static ActivityAwardManager manager=new ActivityAwardManager();

	public static ActivityAwardManager getInstance()
	{
		return manager;
	}
	/**
	 * 整合奖励品 <br>
	 * 若存在需整合的活动奖励则产生并返回一个新的奖励实例
	 */
	public Award assembleWholeAward(Player player,Award award,int type,
		int time)
	{
		ActivityAward activityAward=(ActivityAward)awardMap.get(type);
		if(activityAward!=null)
		{
			if(activityAward.isAwardAvailable(time))
			{
				// 重新实例化一个奖励,防止污染源实例,带来重复叠加的问题
				Award assemble=new Award();
				assemble.assembleAwardFromAnother(award,player);
				assemble.assembleAwardFromAnother(activityAward.getAward(),
					player);
				award=assemble;
			}
			else
				removeAward(type);
		}
		return award;
	}

	public ActivityAward[] removeAward(int[] types)
	{
		ActivityAward[] awards=new ActivityAward[types.length];
		for(int i=0;i<types.length;i++)
		{
			awards[i]=(ActivityAward)awardMap.remove(types[i]);
		}
		updateAwardsVar();
		return awards;
	}
	
	public ActivityAward removeAward(int type)
	{
		ActivityAward award=(ActivityAward)awardMap.remove(type);
		updateAwardsVar();
		return award;
	}

	public void putAward(int[] types,int stime,int etime,int[] props,
		int[] randoms)
	{
		if(props==null&&randoms==null)
			return;
		if(props!=null&&props.length<2)
			props=null;
		if(randoms!=null&&randoms.length<3)
			randoms=null;
		Award award=resetAward(props,randoms);
		for(int i=0;i<types.length;i++)
		{
			putAward(types[i],stime,etime,props,randoms,award);
		}
		updateAwardsVar();
	}
	
	public void putAward(int type,int stime,int etime,int[] props,
		int[] randoms)
	{
		if(props==null&&randoms==null)
			return;
		if(props!=null&&props.length<2)
			props=null;
		if(randoms!=null&&randoms.length<3)
			randoms=null;
		Award award=resetAward(props,randoms);
		putAward(type,stime,etime,props,randoms,award);
		updateAwardsVar();
	}

	public Award resetAward(int[] props,int[] randoms)
	{
		Award award=new Award();
		SeaBackKit.resetAward(award,props);
		award.setRandomProps(randoms);
		return award;
	}
	
	/** GMT进行查询时仅显示传入的props与randoms */
	public void putAward(int type,int stime,int etime,int[] props,
		int[] randoms,Award award)
	{
		awardMap.put(type,new ActivityAward(stime,etime,props,randoms,award));
	}

	public void init()
	{
		String infos=ContextVarManager.getInstance().getVarDest(
			ContextVarManager.ACTIVITY_AWARD_DATA);
		if(infos==null||"".trim().equals(infos)) return;
		// 分解每一档奖励信息
		String[] typeAwards=TextKit.split(infos,";");
		String[] values=null;
		for(int i=0;i<typeAwards.length;i++)
		{
			values=TextKit.split(typeAwards[i],"-");
			awardMap.put(TextKit.parseInt(values[0]),
				stringToAward(values[1]));
		}
	}
	
	/** 持久化掉落信息 */
	public void updateAwardsVar()
	{
		StringBuffer dest=new StringBuffer();
		int[] keys=awardMap.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			if(i>0) dest.append(";");
			ActivityAward aa=(ActivityAward)awardMap.get(keys[i]);
			dest.append(keys[i]).append("-").append(awardToString(aa));
		}
		ContextVarManager.getInstance().putVar(
			ContextVarManager.ACTIVITY_AWARD_DATA,0,dest.toString());
	}
	
	public ActivityAward stringToAward(String info)
	{
		if(info==null||"".trim().equals(info)) return null;
		ActivityAward award=new ActivityAward();
		// 分解每一个属性
		String[] fields=TextKit.split(info,"|");
		String[] value=null;
		// stime
		value=TextKit.split(fields[0],":");
		if(value!=null&&value.length>1)
			award.setStime(TextKit.parseInt(value[1]));
		// etime
		value=TextKit.split(fields[1],":");
		if(value!=null&&value.length>1)
			award.setEtime(TextKit.parseInt(value[1]));
		// props
		value=TextKit.split(fields[2],":");
		if(value!=null&&value.length>1)
			award
				.setProps(TextKit.parseIntArray(TextKit.split(value[1],",")));
		// randoms
		value=TextKit.split(fields[3],":");
		if(value!=null&&value.length>1)
			award.setRandoms(TextKit.parseIntArray(TextKit.split(value[1],
				",")));
		if(award.getProps()==null&&award.getRandoms()==null) return null;
		if(award.getProps()!=null&&award.getProps().length<2)
			award.setProps(null);
		if(award.getRandoms()!=null&&award.getRandoms().length<3)
			award.setRandoms(null);
		award.setAward(resetAward(award.getProps(),award.getRandoms()));
		return award;
	}
	
	public String awardToString(ActivityAward award)
	{
		StringBuffer sb=new StringBuffer();
		sb.append("stime:").append(award.stime).append("|etime:")
			.append(award.etime);
		StringBuffer temp=new StringBuffer();
		if(award.props!=null)
		{
			if(award.props.length>0) temp.append(award.props[0]);
			for(int i=1;i<award.props.length;i++)
			{
				temp.append(",").append(award.props[i]);
			}
		}
		sb.append("|props:").append(temp.toString());
		temp.setLength(0);
		if(award.randoms!=null)
		{
			if(award.randoms.length>0) temp.append(award.randoms[0]);
			for(int i=1;i<award.randoms.length;i++)
			{
				temp.append(",").append(award.randoms[i]);
			}
		}
		sb.append("|randoms:").append(temp.toString());
		return sb.toString();
	}
	
	public IntKeyHashMap getAwardMap()
	{
		return awardMap;
	}

	public class ActivityAward
	{

		int stime;
		int etime;
		int[] props;
		int[] randoms;
		Award award;

		public ActivityAward()
		{
			super();
		}

		public ActivityAward(int stime,int etime,int[] props,int[] randoms,
			Award award)
		{
			super();
			this.stime=stime;
			this.etime=etime;
			this.props=props;
			this.randoms=randoms;
			this.award=award;
		}

		public boolean isAwardAvailable(int time)
		{
			return time>=stime&&time<etime;
		}

		public int getStime()
		{
			return stime;
		}

		public void setStime(int stime)
		{
			this.stime=stime;
		}

		public int getEtime()
		{
			return etime;
		}

		public void setEtime(int etime)
		{
			this.etime=etime;
		}

		public Award getAward()
		{
			return award;
		}

		public void setAward(Award award)
		{
			this.award=award;
		}
		
		public int[] getProps()
		{
			return props;
		}

		
		public void setProps(int[] props)
		{
			this.props=props;
		}

		
		public int[] getRandoms()
		{
			return randoms;
		}

		
		public void setRandoms(int[] randoms)
		{
			this.randoms=randoms;
		}
		
	}
}
