package foxu.sea.activity;

import java.util.Arrays;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.net.DataAccessException;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.task.TaskEventExecute;

/**
 * 幸运探险
 */
public class LuckyExploredActivity extends Activity implements ActivitySave
{

	/** 奖品类型 船 配件 军官碎片 其他杂项  QUALITYSTUFF=5*/
	public static final int SHIP=1,PRO=2,START=3,EQUIP=4;
	/**奖励类型**/
	public static final int AWARD_TYPE_ONE=1,AWARD_TYPE_TWO=2,AWARD_TYPE_THREE=3,AWARD_TYPE_FOUR=4;
	/** 格子数量 */
	public static final int GRID_SIZE=30;
	/** 掷骰子所需宝石 1次 */
	private int diceGem;
	/** 掷骰子所需宝石 10次 */
	private int tenDiceGem;
	/** 玩家当前所处位置 key：playerId value: int位置,int完成圈数   圈数暂时无用 下个版本使用*/
	private IntKeyHashMap location;
	/** 地图对应奖品类型  初始化随机生成*/
	private int[] gridType;
	/**默认为0**/
	private  int times=0;
	
	/** 活动信息保存周期 */
	public static final int SAVE_CIRCLE=15*60;
	/** 上一次活动信息保存时间 */
	int lastSaveTime;

	/** 幸运探险奖励类型  船 配件 军官碎片 其他 */
	/**具体的细节  sid  数量 类型 概率**/
	public static int[] LUCKY_AWARD_ONE;
	public static int[] LUCKY_AWARD_TWO;
	public static int [] LUCKY_AWARD_THREE;
	public static int [] LUCKY_AWARD_FOUR;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		return resetActivity(stime,etime,initData,factoty);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		if(gridType==null) gridType=new int[GRID_SIZE];
		if(location==null) location=new IntKeyHashMap();
		String[] awards=TextKit.split(initData,";");
		LUCKY_AWARD_ONE=initAwardInfo(awards[0]);
		String[] data=awards[1].split("\\|");
		LUCKY_AWARD_TWO=initAwardInfo(data[0]);
		LUCKY_AWARD_THREE=initAwardInfo(data[1]);
		LUCKY_AWARD_FOUR=initAwardInfo(data[2]);
		diceGem=Integer.parseInt(data[3]);
		tenDiceGem=Integer.parseInt(data[4]);
		if(data[5]!=null && data[3].equals("no_limit"))
			times=-1;
		else if(data[5]!=null)
			times=Integer.parseInt(data[5]);
		initGridType();
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
		return getActivityState();
	}

	/**
	 * 初始化格子奖励类型
	 */
	private void initGridType()
	{
		if(gridType==null) return;
		for(int i=0;i<gridType.length;i++)
		{
			int type=MathKit.randomValue(1,5);
			gridType[i]=type;
			if(i!=0&&i%4==0)
			{
				if(checkGridType(i))
				{
					gridType[i]=AWARD_TYPE_TWO;
				}
			}
		}
	}

	/**检测下是否满足需求 每4格必须出现一种类型的 **/
	public boolean checkGridType(int i)
	{
		for(int j=i-3;j<=i;j++)
		{
			if(gridType[j]==AWARD_TYPE_TWO) return false;
		}
		return true;
	}
	
	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"")
			.append("[initData]:"+Arrays.toString(gridType))
			.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		if(gridType==null) gridType=new int[GRID_SIZE];
		if(location==null) location=new IntKeyHashMap();
		diceGem=data.readInt();
		tenDiceGem=data.readInt();
		times=data.readInt();
		int length=data.readInt();
		for(int i=0;i<length;i++)
		{
			int playerId=data.readInt();
			String value=data.readUTF();
			location.put(playerId,value);
		}
		length=data.readInt();
		LUCKY_AWARD_ONE=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_ONE[i]=data.readInt();
		}
		length=data.readInt();
		LUCKY_AWARD_TWO=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_TWO[i]=data.readInt();
		}
		length=data.readInt();
		LUCKY_AWARD_THREE=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_THREE[i]=data.readInt();
		}
		length=data.readInt();
		LUCKY_AWARD_FOUR=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_FOUR[i]=data.readInt();
		}
		initGridType();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeInt(diceGem);
		data.writeInt(tenDiceGem);
		data.writeInt(times);
		int[] keyArr=location.keyArray();
		// 玩家游戏信息
		data.writeInt(keyArr.length);
		for(int i=0;i<keyArr.length;i++)
		{
			data.writeInt(keyArr[i]);
			data.writeUTF((String)location.get(keyArr[i]));
		}
		data.writeInt(LUCKY_AWARD_ONE.length);
		for(int i=0;i<LUCKY_AWARD_ONE.length;i++)
		{
			data.writeInt(LUCKY_AWARD_ONE[i]);
		}
		data.writeInt(LUCKY_AWARD_TWO.length);
		for(int i=0;i<LUCKY_AWARD_TWO.length;i++)
		{
			data.writeInt(LUCKY_AWARD_TWO[i]);
		}
		data.writeInt(LUCKY_AWARD_THREE.length);
		for(int i=0;i<LUCKY_AWARD_THREE.length;i++)
		{
			data.writeInt(LUCKY_AWARD_THREE[i]);
		}
		data.writeInt(LUCKY_AWARD_FOUR.length);
		for(int i=0;i<LUCKY_AWARD_FOUR.length;i++)
		{
			data.writeInt(LUCKY_AWARD_FOUR[i]);
		}
		// 奖品信息
		// if(awardInfo!=null) data.writeUTF(awardInfo);
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeShort(diceGem);
		data.writeShort(tenDiceGem);
		data.writeInt(endTime-TimeKit.getSecondTime());
		data.writeShort(times);
	}
	
	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		data.writeShort(getSid());
		data.writeShort(diceGem); // 每次所需宝石
		data.writeShort(tenDiceGem); // 每十次所需宝石
		data.writeInt(endTime); // 结束时间
		data.writeByte(getLocation(player.getId())); // 玩家所在位置
		data.writeShort(times);//次数
		data.writeShort(getTimesNum(player.getId()));//玩家的抽奖次数
//		data.writeInt(getFinishNum(player.getId())); // 玩家完成圈数
		data.writeByte(gridType.length);	//格子的奖品类型
		for(int i=0;i<gridType.length;i++)
			data.writeShort(gridType[i]);
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendLuckActivity(smap,this,null);
	}

	/** 掷骰子 1-6 count:掷的次数 */
	private int dice()
	{
		return MathKit.randomValue(1,7);
	}

	/** 获取玩家当前位置 */
	public int getLocation(int playerId)
	{
		if(getPlayerInfo(playerId)==null)
			return 0;
		String location=getPlayerInfo(playerId)[0];
		return Integer.parseInt(location);
	}

	/** 获取玩家完成圈数 */
	public int getFinishNum(int playerId)
	{
		if(getPlayerInfo(playerId)==null)
			return 0;
		String num=getPlayerInfo(playerId)[1];
		return Integer.parseInt(num);
	}

	/**获取玩家的抽奖次数**/
	public int getTimesNum(int playerId)
	{
		if(getPlayerInfo(playerId)==null)
			return 0;
		String num=getPlayerInfo(playerId)[2];
		return Integer.parseInt(num);
	}
	
	/**获取玩家的圈数 和现在的位置和玩家抽奖的次数**/
	private String[] getPlayerInfo(int playerId)
	{
		if(location.get(playerId)==null||"".equals(location.get(playerId)))
			return null;
		String info=(String)location.get(playerId);
		String[] infoArr=info.split(",");
		return infoArr;
	}

	/** 设置用户活动信息 */
	public void putPlayerInfo(int playerId,int playerLoc,int finishNum,int times)
	{
		String value=playerLoc+","+finishNum+","+times;
		location.put(playerId,value);
	}

	/** 玩家掷骰子 并获取奖励 	diceType 0掷1次筛子  1掷10次 */
	public void move(Player player,int diceType,ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		int gems;
		int diceNum=0;
		if(diceType==0)
		{
			gems=diceGem;
		}
		else if(diceType==1)
		{
			gems=tenDiceGem;
		}
		else
			throw new DataAccessException(0,"diceType error");
		if(!Resources.checkGems(gems,player.getResources()))
		{
			throw new DataAccessException(0,"not enough gems");
		}
		Resources.reduceGems(gems,player.getResources(),player);
		objectFactory.createGemTrack(GemsTrack.LUCKY_EXPLORED,
			player.getId(),gems,0,Resources.getGems(player.getResources()));
		// 发送change消息
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.GEMS_ADD_SOMETHING,this,player,gems);
		if(diceType==0)
		{
			data.writeByte(1);		//抽奖次数
			diceNum+=reward(player,data,objectFactory);
			data.writeByte(diceNum);	//骰子结果
			data.writeByte(getLocation(player.getId()));	//玩家位置
		}
		else if(diceType==1)
		{
			gems=tenDiceGem;
			data.writeByte(10);		//抽奖次数
			int [] diceNums=new int[10];
			for(int i=0;i<10;i++)
				diceNums[i]=reward(player,data,objectFactory);
			data.writeByte(getLocation(player.getId()));	//玩家位置
			data.writeByte(diceNums.length);
			for(int i=0;i<diceNums.length;i++)
			{
				data.writeByte(diceNums[i]);
			}
			
		}
		data.writeShort(getTimesNum(player.getId()));
	}

	/** 发奖 返回骰子结果 */
	public int reward(Player player,ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		int diceNum=dice();
		int playerLocation=getLocation(player.getId());
		int finishNum=getFinishNum(player.getId());
		int playerTimes=getTimesNum(player.getId());
		playerLocation+=diceNum;
		// 圈数
		finishNum+=playerLocation/(GRID_SIZE);
		// 当前位置
		playerLocation=playerLocation%(GRID_SIZE);
		putPlayerInfo(player.getId(),playerLocation,finishNum,playerTimes+1);
		// 所处位置奖励类型
		int awardType=gridType[playerLocation];
		Award aw=new Award();
		if(awardType==AWARD_TYPE_ONE)
			aw.setRandomProps(LUCKY_AWARD_ONE);
		else if(awardType==AWARD_TYPE_TWO)
			aw.setRandomProps(LUCKY_AWARD_TWO);
		else if(awardType==AWARD_TYPE_THREE)
			aw.setRandomProps(LUCKY_AWARD_THREE);
		else 
			aw.setRandomProps(LUCKY_AWARD_FOUR);
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"luckyexploredactivity_message");
		message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
		aw.awardSelf(player,TimeKit.getSecondTime(),data,objectFactory,message,
			SeaBackKit.getLuckySids(),new int[]{EquipmentTrack.FROM_LUCKY_EXPLORED,FightScoreConst.NEW_ACTIVITY});
		return diceNum;
	}

//	/**获取奖励品的信息**/
//	public Award randomLuckyExplored(int [] awardInfo)
//	{
//		// 开始的机率
//		int startProbability=0;
//		int random=MathKit.randomValue(0,Award.PROB_ABILITY);			
//		for(int i=0;i<awardInfo.length;i+=3)
//		{
//				// 如果随机数在这个范围内
//			if(random>=startProbability&&random<=awardInfo[i+2])
//			{
//				Award ad=(Award)Award.factory.newSample(65051);
//				int[] awardSid={awardInfo[i],awardInfo[i+1]};
//				SeaBackKit.resetAward(ad,awardSid);
//				return ad;
//			}
//			startProbability=awardInfo[i+2];
//		}
//		return null;
//	}
	
	
	
	
	public int getDiceGem()
	{
		return diceGem;
	}

	public int getTenDiceGem()
	{
		return tenDiceGem;
	}
	
	/**设置奖励品信息**/
	public  int [] initAwardInfo(String  award)
	{
		if(award==null) return null;
		String[] awardInfo=TextKit.split(award,",");
		int[] awards=new int[awardInfo.length];
		for(int i=0;i<awardInfo.length;i++)
		{
			awards[i]=TextKit.parseInt(awardInfo[i]);
		}
		return awards;
	}
	/**验证下当前这个活动抽奖次数**/
	public  boolean checkTimes(int playerId,int time)
	{
		if(times==-1) return true;
		int count=getTimesNum(playerId);
		if(times==0 || times<=count || times<time+count)
			return false;
		return true;
	}

	@Override
	public boolean isSave()
	{
		if(TimeKit.getSecondTime()>=lastSaveTime+SAVE_CIRCLE) return true;
		return false;
	}
	@Override
	public void setSave()
	{
		lastSaveTime=TimeKit.getSecondTime();
	}
	
	 @Override
	public Object copy(Object obj)
	{
		 LuckyExploredActivity ac=(LuckyExploredActivity)super.copy(obj);
		 ac.location=new IntKeyHashMap();
		return ac;
	}
	
}
