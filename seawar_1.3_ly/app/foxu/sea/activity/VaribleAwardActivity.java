package foxu.sea.activity;


import javapns.json.JSONArray;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;

/**
 * 变量礼包（天降好礼）
 * 
 * @author yw
 * 
 */
public class VaribleAwardActivity extends Activity
{

	/**OPEN=1开启 UPDATE=2修改***/
	public static final int OPEN=1,UPDATE=2;
	/** 奖励包集合 */
	IntKeyHashMap awardMap=new IntKeyHashMap();

	/** 是否需要刷新 */
	boolean flush;
	/** 刷新时间 */
	int flushTime;

	public  static final int TIME=60*60;
	
	
	public boolean isFlush()
	{
		return flush;
	}
	
	public void setFlush(boolean flush)
	{
		this.flush=flush;
	}
	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		int start=SeaBackKit.parseFormatTime(stime);
		if(start<TimeKit.getSecondTime())
			start=TimeKit.getSecondTime();
		int end=SeaBackKit.parseFormatTime(etime);
		if(start>=end) return "erro time";
		String [] str=initData.split(";");
		initData=str[0];
		int level=TextKit.parseInt(str[1]);
		int limitTime=TextKit.parseInt(str[2]);
		String[] info=initData.split(":");
		int type=Integer.parseInt(info[0]);
		int[] props=transInts(info[1]);
		if(props==null) return "erro props";
		VariblePackage award=new VariblePackage(type,start,end,props,level,limitTime);
		awardMap.put(award.active,award);
		flushAward();
		flush=true;
		flushTime=TimeKit.getSecondTime();
		pushPlayerState(award,factory);
		return "ok";
	}
	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		String [] str=initData.split(";");
		initData=str[0];
		int level=TextKit.parseInt(str[1]);
		int limitTime=TextKit.parseInt(str[2]);
		String[] info=initData.split(":");
		try
		{
			int active=Integer.parseInt(info[0]);
			VariblePackage award=(VariblePackage)awardMap.get(active);
			if(award==null) return "erro id";
			int start=SeaBackKit.parseFormatTime(stime);
			if(start<TimeKit.getSecondTime())
				start=TimeKit.getSecondTime();
			if(start>0) award.start=start;
			int end=SeaBackKit.parseFormatTime(etime);
			if(end>0) award.end=end;
			int[] props=transInts(info[1]);
//			if(props!=null) award.award.setPropSid(props);
			award.sepProEquInfo(props);
			award.setLevel(level);
			award.setLimitTime(limitTime);
			flushAward();
			flush=true;
			flushTime=TimeKit.getSecondTime();
		}
		catch(Exception e)
		{
			return "erro id";
		}
		return "ok";
	}

	public int[] transInts(String ints)
	{
		if(ints==null) return null;
		String[] strs=ints.split(",");
		int[] props=new int[strs.length];
		for(int i=0;i<strs.length;i++)
		{
			props[i]=TextKit.parseInt(strs[i]);
		}
		return props;
	}
	/** 刷新奖励包 */
	public void flushAward()
	{
		int[] keys=awardMap.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			VariblePackage award=(VariblePackage)awardMap.get(keys[i]);
			if(award.isValid())
			{
				if(award.canGet(TimeKit.getSecondTime())
					&&!award.canGet(flushTime))
				{
					flush=true;
					flushTime=TimeKit.getSecondTime();
				}
				continue;
			}
			awardMap.remove(keys[i]);
		}
	}

	@Override
	public String getActivityState()
	{
		return null;
	}

	public void getActivityInfo(JSONArray jsonArray)
	{
		flushAward();
		int[] keys=awardMap.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			VariblePackage award=(VariblePackage)awardMap.get(keys[i]);
			jsonArray.put(award.getGmInfo());
		}

	}
	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		awardMap.clear();
		int len=data.readUnsignedByte();
		for(int i=0;i<len;i++)
		{
			VariblePackage awrad=new VariblePackage();
			awrad.bytesRead(data);
			awardMap.put(awrad.active,awrad);
		}
		flushTime=TimeKit.getSecondTime();
		flushAward();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeByte(awardMap.size());
		int[] keys=awardMap.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			((VariblePackage)awardMap.get(keys[i])).bytesWrite(data);
		}
		return data;
	}

	public void showByteWrite(ByteBuffer data,Player player)
	{
		IntList keys=getValidKeys(player);
		int top=data.top();
		data.writeByte(keys.size());
		int count=0;
		for(int i=0;i<keys.size();i++)
		{
			VariblePackage award=(VariblePackage)awardMap.get(keys.get(i));
			if(award.getLevel()==0 && award.getLimitTime()==0)
			count++;
			else
			{
				if(player.getAttributes(PublicConst.VAR_PLAYER_INFO)==null)		
					continue;
				
				if(!player.getAttributes(PublicConst.VAR_PLAYER_INFO)
						.contains(award.getactivityId()+"")) continue;
					String info=player.getAttributes(PublicConst.VAR_PLAYER_INFO);
					int index=info.indexOf(info);
					info=info.substring(index);
					String[] str=info.split(",");
					if(str.length==0 || str.length%3!=0)
						continue;
					if(award.getLevel()!=0 && TextKit.parseInt(str[1])<award.getLevel())
							continue;
					if(award.getLimitTime()!=0 && TextKit.parseInt(str[2])+award.getLimitTime()*TIME<award.getactivityId())
						continue;
				count++;
			}
			award.showBytesWrite(data);
		}
		int topNpw=data.top();
		data.setTop(top);
		data.writeByte(count);
		data.setTop(topNpw);
	}
	/** 获取可领礼包 */
	public IntList getValidKeys(Player player)
	{
		IntList keys=new IntList();
		int[] pkeys=transInts(player
			.getAttributes(PublicConst.VARIBLE_PACKAGE));
		int[] akeys=awardMap.keyArray();
		for(int i=0;i<akeys.length;i++)
		{
			if(SeaBackKit.isContainValue(pkeys,akeys[i])) continue;
			if(!((VariblePackage)awardMap.get(akeys[i])).canGet(TimeKit
				.getSecondTime())) continue;
			keys.add(akeys[i]);
		}
		return keys;
	}
	/** 领取礼包 */
	public String getAward(Player player,ByteBuffer data)
	{
		int id=data.readInt();
		String geted=player.getAttributes(PublicConst.VARIBLE_PACKAGE);
		if(geted!=null&&geted.contains(id+""))
		{
			return "have geted award";
		}
		VariblePackage award=(VariblePackage)awardMap.get(id);
		if(award==null||!award.canGet(TimeKit.getSecondTime()))
		{
			return "award invalid";
		}
		if(award.getLevel()!=0 || award.getLimitTime()!=0)
		{
			if(player.getAttributes(PublicConst.VAR_PLAYER_INFO)==null)
				return "can't accept";
			String info=player.getAttributes(PublicConst.VAR_PLAYER_INFO);
			String active=String.valueOf(award.active);
			if(info.indexOf(active.trim())==-1)
				return "can't accept";
			int index=info.indexOf(active.trim());
			info=info.substring(index);
			if(info.split(",").length%3!=0)
				return "can't accept";
			if(award.getLevel()!=0 && award.getLevel()>TextKit.parseInt(info.split(",")[1]))
				return "can't accept";
			if(award.getLimitTime()!=0 && TextKit.parseInt(info.split(",")[2])+award.getLimitTime()*TIME<award.active)
				return "can't accept";
		}
		data.clear();
		setGetRecord(player,id,geted);
		award.award.awardLenth(data,player,ActivityContainer.getInstance()
			.getObjectFactory(),null,null,
			new int[]{EquipmentTrack.FROM_VARIBLE});
		return null;
	}

	/** 设置玩家领取记录 */
	public void setGetRecord(Player player,int id,String geted)
	{
		if(geted==null)
		{
			player.setAttribute(PublicConst.VARIBLE_PACKAGE,id+"");
		}
		else
		{
			int[] keys=awardMap.keyArray();
			String[] gets=geted.split(",");
			StringBuffer sbuff=new StringBuffer();
			for(int i=0;i<gets.length;i++)
			{
				if(SeaBackKit.isContainValue(keys,Integer.parseInt(gets[i])))
				{
					sbuff.append(gets[i]+",");
				}
			}
			sbuff.append(id+"");
			player
				.setAttribute(PublicConst.VARIBLE_PACKAGE,sbuff.toString());
		}
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{

	}
	
	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendVaribleAwardActivty(smap);
		
	}
	
	/**获取所有的天降好礼的id**/
	public int[] getactivityId()
	{
		int[] aid=new int[awardMap.size()]; 
		flushAward();
		int[] keys=awardMap.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			VariblePackage award=(VariblePackage)awardMap.get(keys[i]);
			aid[i]=award.getactivityId();
		}
		return aid;
	}

	public boolean isPlayerNeedPush(Player player)
	{
		String info=player.getAttributes(PublicConst.VAR_PLAYER_INFO);
		if(info!=null)
		{
			int[] ids=getactivityId();
			int[] infos=TextKit.parseIntArray(TextKit.split(info,","));
			for(int i=0;i<infos.length;i+=3)
			{
				for(int j=0;j<ids.length;j++)
				{
					if(infos[i]==ids[j]) return true;
				}
			}
		}
		return false;
	}
	
	/**推送玩家天降好礼的状态**/
	public void pushPlayerState(VariblePackage award,CreatObjectFactory factory)
	{
		IntKeyHashMap cache=factory.getPlayerCache().getCacheMap();
		IntKeyHashMap changeList=factory.getPlayerCache().getChangeListMap();
		if(cache.size()==0) return;
		Object[] playerSaves=cache.valueArray();
		for(int i=0;i<playerSaves.length;i++)
		{
			PlayerSave playerSave=(PlayerSave)playerSaves[i];
			if(playerSave==null) continue;
			Player player=playerSave.getData();
			if(player==null) continue;
			String info=getVarawardInfo(getactivityId(),player,award);
			player.setAttribute(PublicConst.VAR_PLAYER_INFO,info);
			//加入改变列表
			changeList.put(player.getId(),playerSave);
		}
	}
	/**
	 * 设置天降好礼活动记录
	 * **/
	public String getVarawardInfo(int[] aid,Player player,VariblePackage award)
	{
		String info=null;
		if(aid.length==0) return null;
		info=player.getAttributes(PublicConst.VAR_PLAYER_INFO);
		if(info==null)
				return award.active+","+player.getLevel()+","+player.getUpdateTime();
		info=removeAwardInfo(aid,info);
		String resultStr=award.active+","+player.getLevel()+","+player.getUpdateTime();
		 return info==null?resultStr:(info+","+resultStr);
	}
	
	/**移除玩家身上没有的活动**/
	public String removeAwardInfo(int[] aid,String info)
	{
		String resultStr=null;
		for(int i=0;i<aid.length;i++)
		{
			int index=info.indexOf(String.valueOf(aid[i]));
			if(index!=-1 &&  info.substring(index).split(",").length%3==0)
			{
				String infoStr[]=info.substring(index).split(",");
				if(infoStr==null ||infoStr.length==0)
					continue;
				if(resultStr==null || resultStr.length()==0)
					resultStr=infoStr[0]+","+infoStr[1]+","+infoStr[2];
				else resultStr+=","+infoStr[0]+","+infoStr[1]+","+infoStr[2];
			}
		}
		return resultStr;
	}
}
