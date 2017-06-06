package foxu.sea.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.gm.WebConstant;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;

/**
 * 为web服务器提供相应显示数据的活动
 * 
 * @author Alan
 */
public class WebShowActivity extends Activity implements ActivitySave,
	ActivityCollate
{

	/** 活动信息保存周期 */
	public static final int SAVE_CIRCLE=15*60;
	/** 活动信息推送周期 */
	public static final int PUSH_CIRCLE=1*60;
	/** web 服务器的地址 **/
	public static String REQUEST_WEB_ADDRESS;
	/** 需要发送到的url */
	public static final String GEMS_URL="getOrder",NAMES_URL="modifyName";
	/** 单位时间充值记录 */
	IntList gemsRecord=new IntList();
	/** 充值总计 */
	IntKeyHashMap gemsInfo=new IntKeyHashMap();
	/** 单位时间改名记录 */
	IntList nameRecord=new IntList();
	/** 上一次活动信息保存时间 */
	int lastSaveTime;
	/** 上一次活动信息推送时间 */
	int lastPushTime;

	@Override
	public void activityCollate(int time,CreatObjectFactory factoty)
	{
		if(time<lastPushTime+PUSH_CIRCLE) return;
		pushGemsInfo(factoty);
		pushNamesInfo(factoty);
		lastPushTime=time;
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
			.append("[needPushGems]:"+gemsRecord.size()+",")
			.append("[needPushNames]:"+nameRecord.size()+",")
			.append("[gemsInfos]:"+gemsInfo.size()+",")
			.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{

	}

	@Override
	public void sendFlush(SessionMap smap)
	{

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
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		return resetActivity(stime,etime,initData,factoty);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		startTime=SeaBackKit.parseFormatTime(stime);
		endTime=SeaBackKit.parseFormatTime(etime);
		return getActivityState();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			gemsRecord.add(data.readInt());
		}
		len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			gemsInfo.put(data.readInt(),data.readInt());
		}
		len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			nameRecord.add(data.readInt());
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		synchronized(gemsRecord)
		{
			int len=gemsRecord.size();
			data.writeShort(len);
			for(int i=0;i<len;i++)
			{
				data.writeInt(gemsRecord.get(i));
			}
		}
		int[] ids=gemsInfo.keyArray();
		data.writeShort(ids.length);
		for(int i=0;i<ids.length;i++)
		{
			data.writeInt(ids[i]);
			data.writeInt((Integer)gemsInfo.get(ids[i]));
		}
		synchronized(nameRecord)
		{
			int len=nameRecord.size();
			data.writeShort(len);
			for(int i=0;i<len;i++)
			{
				data.writeInt(nameRecord.get(i));
			}
		}
		return data;
	}

	public void addGemsRecord(int pid,int gems)
	{
		if(!gemsRecord.contain(pid)) gemsRecord.add(pid);
		Integer counts=(Integer)gemsInfo.get(pid);
		if(counts==null) counts=0;
		gemsInfo.put(pid,counts+gems);
	}

	public void addNameRecord(int pid)
	{
		if(!nameRecord.contain(pid)) nameRecord.add(pid);
	}

	public void pushGemsInfo(CreatObjectFactory factoty)
	{
		JSONArray jsons=new JSONArray();
		synchronized(gemsRecord)
		{
			Player player=null;
			for(int i=0;i<gemsRecord.size();i++)
			{
				player=factoty.getPlayerById(gemsRecord.get(i));
				if(player==null)	continue;
				JSONObject jo=new JSONObject();
				try
				{
					// 账号
					jo.put(WebConstant.ACCOUNT,factoty.getUserDBAccess().loadById(player.getUser_id()+"").getUserAccount());
					// 玩家id
					jo.put(WebConstant.PLAYER_ID,player.getId());
					// 玩家名称
					jo.put(WebConstant.PLAYER_NAME,player.getName());
					// 宝石数量
					jo.put(WebConstant.GEM,String.valueOf((Integer)gemsInfo.get(player.getId())));
					jsons.put(jo);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}
		if(jsons.length()>0)
		{
			//System.out.println(REQUEST_WEB_ADDRESS+":-------REQUEST_WEB_ADDRESS------GEMS_URL---------:"+GEMS_URL);
			String flag=sendHttpRequest(REQUEST_WEB_ADDRESS+GEMS_URL,jsons);
			if(flag.trim().equals("true"))
				gemsRecord.clear();
		}
	}
	
	
	
	public void pushNamesInfo(CreatObjectFactory factoty)
	{
		JSONArray jsons=new JSONArray();
		synchronized(nameRecord)
		{

			Player player=null;
			for(int i=0;i<nameRecord.size();i++)
			{
				player=factoty.getPlayerById(nameRecord.get(i));
				if(player==null) continue;
				JSONObject jo=new JSONObject();
				try
				{
					// 玩家id
					jo.put(WebConstant.PLAYER_ID,player.getId());
					// 玩家名称
					jo.put(WebConstant.PLAYER_NAME,player.getName());
					jsons.put(jo);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

			}
		}
		if(jsons.length()>0) 
		{
			String flag=sendHttpRequest(REQUEST_WEB_ADDRESS+NAMES_URL,jsons);
			if(flag.trim().equals("true"))
				nameRecord.clear();
		}
	}

	public String sendHttpRequest(String urlString,JSONArray json)
	{
		try
		{
			JSONObject jo=new JSONObject();
			jo.put(WebConstant.RECORD,json);
			jo.put(WebConstant.AREAID,UserToCenterPort.AREA_ID);
			jo.put(WebConstant.PLATFORM,UserToCenterPort.PLAT_ID);
			jo.put(WebConstant.SERVER_ID,UserToCenterPort.SERVER_ID);
			String data=packageData(jo.toString());
//			HttpURLConnection urlConnection=null;
//			URL url=new URL(urlString);
//			urlConnection=(HttpURLConnection)url.openConnection();
//			urlConnection.setConnectTimeout(20000);
//			urlConnection.setReadTimeout(20000);
//			urlConnection.setRequestMethod("POST");
//			urlConnection.setDoOutput(true);
//			urlConnection.setDoInput(true);
//			urlConnection.setUseCaches(false);
//			urlConnection.getOutputStream().write(data.getBytes());
//			urlConnection.getOutputStream().flush();
//			urlConnection.getOutputStream().close();
				URL url=new URL(urlString);
				HttpURLConnection connection=(HttpURLConnection)url
					.openConnection();
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setRequestMethod("POST");
				connection.setUseCaches(false);
				connection.setInstanceFollowRedirects(true);
				connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
				connection.connect();
				OutputStream out=connection.getOutputStream();
				out.write(data.getBytes("UTF-8"));
				out.flush();
				out.close();
				BufferedReader reader=new BufferedReader(new InputStreamReader(
					connection.getInputStream(),"utf-8"));// 设置编码,否则中文乱码
				String line="";
				StringBuilder sb=new StringBuilder();
				while((line=reader.readLine())!=null)
				{
					sb.append(line).append("\n");
				}
				reader.close();
				connection.disconnect();
				return sb.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object copy(Object obj)
	{
		WebShowActivity act=(WebShowActivity)obj;
		act.gemsInfo=new IntKeyHashMap();
		act.gemsRecord=new IntList();
		act.nameRecord=new IntList();
		return act;
	}
	
	/** 包装下返回的结果 **/
	public String packageData(String data)
	{
		return "result="+data;
	}
}
