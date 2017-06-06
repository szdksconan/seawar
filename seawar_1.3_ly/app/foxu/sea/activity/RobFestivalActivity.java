package foxu.sea.activity;

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
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.task.TaskEventExecute;

/**
 * 全民抢"节"活动
 */
public class RobFestivalActivity extends AwardActivity implements ActivitySave
{

	public static int ROB_ATYPE=4;
	IntKeyHashMap robFestivalInfo=new IntKeyHashMap();
	
	/** 活动信息保存周期 */
	public static final int SAVE_CIRCLE=15*60;
	/** 上一次活动信息保存时间 */
	int lastSaveTime;
	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		//全民抢"节"使用新型序列化
		data.writeShort(getSid());	//活动sid
		data.writeShort(gems);		//每次所需宝石
		data.writeInt(endTime);		//结束时间
		data.writeShort(getRobFestivalTimes(player.getId()));
		data.writeShort(times);		//可抽奖总次数
		data.writeShort(getRobFestivalLocation(player.getId()));
	}

	/**
	 * 抽奖
	 * @param data
	 * @param player
	 * @param factory
	 */
	public void draw(ByteBuffer data,Player player,CreatObjectFactory factory)
	{
		int nowLoca=data.readUnsignedShort();
		int count=getRobFestivalTimes(player.getId());
		if(times!=-1&&count>=times)
		{
			throw new DataAccessException(0,
				"lucky_reward today times limite");
		}
		else
		// 发送奖品包
		{
			if(!Resources.checkGems(gems,player.getResources()))
				throw new DataAccessException(0,"not enough gems");
			if(Resources.reduceGems(gems,player.getResources(),player))
			{
				factory.createGemTrack(GemsTrack.LUCKY_DRAW_ROB,
					player.getId(),gems,0,
					Resources.getGems(player.getResources()));
				// 创建记录
				data.clear();
				data.writeShort(nowLoca);
				// 奖品长度随机 1-3
				int len=getRandomNum();
				data.writeByte(len);
				for(int i=0;i<len;i++)
				{
					String message=InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"robfestivalactivity_message");
					message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
					getAward().awardSelf(player,TimeKit.getSecondTime(),data,factory,message,
						SeaBackKit.getLuckySids(),new int[]{EquipmentTrack.FROM_ROB_LUCKY,FightScoreConst.NEW_ACTIVITY});
				}
				setRobFestivalInfo(player.getId(),nowLoca);
				// 发送change消息
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,gems);
			}
		}
	}
	
	/**
	 * 获取抽到的奖品类型数量 1-3
	 */
	private int getRandomNum()
	{
		float random=MathKit.randomValue(0.00f,1.0f);
		if(random<PublicConst.ROB_ACTIVITY_AWARD_ODD[0])
			return 3;
		else if(random<PublicConst.ROB_ACTIVITY_AWARD_ODD[1])
			return 2;
		return 1;
	}

	public IntKeyHashMap getRobFestivalInfo()
	{
		return robFestivalInfo;
	}

	
	public void setRobFestivalInfo(IntKeyHashMap robFestivalInfo)
	{
		this.robFestivalInfo=robFestivalInfo;
	}
	
	/**设置抽奖信息**/
	public void setRobFestivalInfo(int playerId,int location)
	{
		int times=getRobFestivalTimes(playerId)+1;
		robFestivalInfo.put(playerId,times+","+location);
	}
	
	/**获取抽奖次数**/
	public int getRobFestivalTimes(int playerId)
	{
		String result=(String)robFestivalInfo.get(playerId);
		if(result==null||result.length()==0) return 0;
		String[] info=TextKit.split(result,",");
		return TextKit.parseInt(info[0]);
	}
	
	/**获取上次的坐标**/
	public int getRobFestivalLocation(int playerId)
	{
		String result=(String)robFestivalInfo.get(playerId);
		if(result==null||result.length()==0) return 0;
		String[] info=TextKit.split(result,",");
		return TextKit.parseInt(info[1]);
	}
	
	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		super.initData(data,factory,active);
		int le=data.readInt();
		for(int i=0;i<le;i++)
		{
			int playerId=data.readInt();
			String info=data.readUTF();
			robFestivalInfo.put(playerId,info);
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=super.getInitData();
		data.writeInt(robFestivalInfo.size());
		int[] key=robFestivalInfo.keyArray();
		String reslut="";
		for(int i=0;i<key.length;i++)
		{
			data.writeInt(key[i]);
			reslut=(String)robFestivalInfo.get(key[i]);
			data.writeUTF(reslut);
		}
		return data;
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendRobActivity(smap,this,null);
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
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeShort(gems);
		data.writeInt(endTime-TimeKit.getSecondTime());
		data.writeShort(times);
	}
	
	 @Override
	public Object copy(Object obj)
	{
		 RobFestivalActivity ac=(RobFestivalActivity)super.copy(obj);
		 ac.robFestivalInfo=new IntKeyHashMap();
		return ac;
	}
}
