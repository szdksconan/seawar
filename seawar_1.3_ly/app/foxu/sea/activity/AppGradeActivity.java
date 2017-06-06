package foxu.sea.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.award.Award;
import foxu.sea.kit.SeaBackKit;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;

/**
 * 应用评分活动
 * 
 * @author Alan
 */
public class AppGradeActivity extends Activity implements ActivitySave
{

	public static final int SAVE_CIRCLE=15*60;
	public static final int OPEN=1,CLOSE=2,ADD=3,UPDATE=4,VIEW=5,REMOVE=6;
	/** 平台url信息 */
	Map<String,String> platURL=new HashMap<String,String>();
	/** 领取记录 */
	IntKeyHashMap playerRecord=new IntKeyHashMap();
	/** 跳转记录 */
	IntKeyHashMap playerForward=new IntKeyHashMap();
	/** 临时占位对象 */
	Object tempObj=new Object();
	private Award award=(Award)Award.factory
		.newSample(ActivityContainer.EMPTY_SID);
	String reason;
	int[] awardProps;
	int lastSaveTime;
	
	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"").append("reason:").append(reason)
			.append(",awards:");
			for(int i=0;i<awardProps.length;i++)
			{
				sb.append(awardProps[i]+",");
			}
		sb.append(",plat:{");
		Set<String> set=platURL.keySet();
		for(String platInfo:set)
		{
			sb.append(platInfo+":"+platURL.get(platInfo)+",");
		}
		sb.append("}\",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append("}");
		return sb.toString();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		// 原因
		data.writeUTF(reason);
		Set<String> set=platURL.keySet();
		// 平台及对应url
		data.writeByte(set.size());
		for(String platInfo:set)
		{
			data.writeUTF(platInfo);
			data.writeUTF(platURL.get(platInfo));
		}
		// 玩家领取记录
		int[] keys=playerRecord.keyArray();
		data.writeInt(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			data.writeInt(keys[i]);
		}
		// 玩家跳转记录
		keys=playerForward.keyArray();
		data.writeInt(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			data.writeInt(keys[i]);
		}
		// 奖励
		data.writeByte(awardProps.length);
		for(int i=0;i<awardProps.length;i++)
		{
			data.writeShort(awardProps[i]);
		}
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeInt(endTime-TimeKit.getSecondTime());
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		// TODO Auto-generated method stub

	}

	public Award getGradeAward()
	{
		return award;
	}

	public String getUrlByPlat(String platId)
	{
		return platURL.get(platId);
	}

	public void resetPlatURL(String initData,String stime,String etime)
	{
		String[] infos=TextKit.split(initData,";");
		int type=TextKit.parseInt(infos[0]);
		if(type==VIEW)
			return;
		if(type==OPEN||type==UPDATE)
		{
			String[] platInfo=TextKit.split(infos[2],",");
			String url=null;
			if(platInfo.length>1) url=platInfo[1];
			if(url!=null)
			{
				if(platInfo[0].equalsIgnoreCase("all"))
				{
					String[] platIds=TextKit.split(infos[3],",");
					for(int i=0;i<platIds.length;i++)
					{
						platURL.put(platIds[i],url);
					}
				}
				else
					platURL.put(platInfo[0],url);
			}
			awardProps=TextKit.parseIntArray(TextKit.split(infos[4],","));
			resetAward();
			reason=infos[5];
			startTime=SeaBackKit.parseFormatTime(stime);
			endTime=SeaBackKit.parseFormatTime(etime);
		}
		else if(type==ADD||type==REMOVE)
		{
			String[] platInfo=TextKit.split(infos[2],",");
			String url=null;
			if(platInfo.length>1) url=platInfo[1];
			if(url!=null)
				platURL.put(platInfo[0],url);
			else
				platURL.remove(platInfo[0]);
		}
	}

	public boolean isCompleteGrade(Player player)
	{
		if(playerRecord.get(player.getId())!=null) return true;
		return false;
	}

	public void addPlayerRecord(Player player)
	{
		playerRecord.put(player.getId(),tempObj);
	}

	public boolean isCompleteForward(Player player)
	{
		if(playerForward.get(player.getId())!=null) return true;
		return false;
	}

	public void addPlayerForward(Player player)
	{
		playerForward.put(player.getId(),tempObj);
	}

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
		resetPlatURL(initData,stime,etime);
		return getActivityState();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		// 原因
		reason=checkNullString(data.readUTF());
		// 平台及对应url
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			platURL.put(checkNullString(data.readUTF()),
				checkNullString(data.readUTF()));
		}
		// 玩家领取记录
		len=data.readInt();
		for(int i=0;i<len;i++)
		{
			playerRecord.put(data.readInt(),tempObj);
		}
		// 玩家跳转记录
		len=data.readInt();
		for(int i=0;i<len;i++)
		{
			playerForward.put(data.readInt(),tempObj);
		}
		// 奖励
		awardProps=new int[data.readUnsignedByte()];
		for(int i=0;i<awardProps.length;i++)
		{
			awardProps[i]=data.readUnsignedShort();
		}
		resetAward();
	}

	/** 如果字符串是空串转换而来,则回归为空串而非“null”字符 */
	public String checkNullString(String str)
	{
		if(str==null||str.equalsIgnoreCase("null")) return null;
		return str;
	}

	public void resetAward()
	{
		SeaBackKit.resetAward(award,awardProps);
	}

	public String getReason()
	{
		return reason;
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
	public Object copy(Object ob)
	{
		AppGradeActivity aga=(AppGradeActivity)ob;
		/** 平台url信息 */
		aga.platURL=new HashMap<String,String>();
		/** 领取记录 */
		aga.playerRecord=new IntKeyHashMap();
		/** 跳转记录 */
		aga.playerForward=new IntKeyHashMap();
		aga.award=(Award)Award.factory
			.newSample(ActivityContainer.EMPTY_SID);
		return aga;
	}
}
