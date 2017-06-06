package foxu.sea.port;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.codec.CodecKit;
import mustang.codec.MD5;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.User;
import foxu.sea.ValidCurrencyExpection;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.charge.VerifyHeroChargeInfoManager;
import foxu.sea.gems.GemsTrack;
import foxu.sea.gm.GMManager;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.order.MouthOrder;
import foxu.sea.order.Order;

/** 宝石充值端口 1011 */
public class BuyGemsPort extends AccessPort
{

	private static final Logger log=LogFactory.getLogger(BuyGemsPort.class);

	public static final int SUCCESS=1,FAIL=2,SAME=3;

	public static final int SYSTEM_STATE=1,BUG_GEMS=2,CANCEL_ORDER=3;
	
	public static final int TIME_OUT=60;

	CreatObjectFactory objectFactory;

	public static String servername="26";
	
	/** 上次检测时间 */
	int checkTime;
	/** 当前充值中心状态  true可使用 */
	boolean serverState;
	/** 充值中心状态地址  */
	String stateUrl="http://purchase.foxugame.com:8880/serverState";
	/** 充值中心黑名单功能地址  */
	String bannedDeviceUrl="http://purchase.foxugame.com:8880/banned";
//	String stateUrl="http://192.168.10.66:8880/serverState";
	
	Map<String,String> map=new HashMap<String,String>();
	Map<String,String> pro=new HashMap<String,String>();

	MD5 md5=new MD5();

	ThreadLocal<HashMap<String,String>> hashMapThreadLocal=new ThreadLocal<HashMap<String,String>>()
	{

		protected HashMap<String,String> initialValue()
		{
			return new HashMap<String,String>();
		}
	};

	ThreadLocal<StringBuilder> stringBuilderThreadLocal=new ThreadLocal<StringBuilder>()
	{

		protected StringBuilder initialValue()
		{
			return new StringBuilder();
		}
	};

	/** 验证参数用的私有key */
	String privateKey="Qx90rc@s15ly-27";

	/** 充值验证地址 */
	String verifyUrl;

	/** Key 苹果产品id Value 苹果产品id数组 **/
	HashMap<String,String[]> app_About=new HashMap<String,String[]>();
	
	/** 篡改验证 私钥 */
	String distortKey="nkyntCuBgsNIEGujwH2fHruYUx3YrnXqk0HS1GQbJJqltMlBMggWHymfiurt6kud";

	/** 设置私有key */
	public void setPrivateKey(String privateKey)
	{
		this.privateKey=privateKey;
	}

	/** 设置验证地址 */
	public void setVerifyUrl(String verifyUrl)
	{
		this.verifyUrl=verifyUrl;
	}

	/** 计算签名 */
	private String getSign(Object...params)
	{
		StringBuilder builder=stringBuilderThreadLocal.get();
		builder.setLength(0);
		for(Object o:params)
		{
			builder.append(o);
		}
		// 附加私有key
		builder.append(privateKey);
		log.debug("private key:"+privateKey);
		String str=builder.toString();
		if(log.isDebugEnabled()) log.debug("str="+str);
		String md5Str=MD5(str);
		return md5Str;
	}

	/** 计算字符串的md5，库里面的MD5类的计算方法在处理多字节字符时有bug，所以重新写一个 */
	private String MD5(String str)
	{
		if(str==null) return "";
		try
		{
			byte[] data=str.getBytes("utf-8");
			byte[] digest=md5.encode(data);
			String csign=new String(CodecKit.byteHex(digest));

			return csign;
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return "";
		}
	}

	private String checkOrder(Player player,String transactionId,
		String dataSignature,String account,String currency,
		String strAmount,String transactionTime,String pdid,Order order)
	{
		String amount="0.00";
		if(account==null||account.isEmpty())
			account=String.valueOf(player.getUser_id());
		
		try
		{
			amount=String.format("%.2f",
				Float.parseFloat(strAmount));
		}
		catch(Exception e)
		{
			if(log.isDebugEnabled())
				log.debug("error amount:"+amount,e);
		}

		String sign=getSign(transactionId,dataSignature,
			UserToCenterPort.AREA_ID,UserToCenterPort.SERVER_ID,
			player.getId(),order.getGems(),amount,currency);

		Map<String,String> map=new HashMap<String,String>();
		map.put("areaId",String.valueOf(UserToCenterPort.AREA_ID));
		map.put("serverId",String.valueOf(UserToCenterPort.SERVER_ID));
		map.put("transactionId",transactionId);
		map.put("pdid",pdid);
		try
		{
			map.put("account",URLEncoder.encode(account,"UTF-8"));
			map.put("playerName",URLEncoder.encode(player.getName(),"UTF-8"));
			map.put("dataSignature",URLEncoder.encode(dataSignature,"UTF-8"));
			map.put("usdAmount",URLEncoder.encode(
				String.format("%.2f",order.getUsdMoney()),"UTF-8"));
			map.put("amount",URLEncoder.encode(amount,"UTF-8"));
			map.put("bundleId",
				URLEncoder.encode(player.getBundleId(),"UTF-8"));
			map.put("udid",URLEncoder.encode(player.getLoginUid(),"UTF-8"));
		}
		catch(UnsupportedEncodingException e)
		{
			if(log.isErrorEnabled())
				log.error("encode parameter error,account="+account+",name="
					+player.getName(),e);
		}
		map.put("playerId",String.valueOf(player.getId()));
		map.put("gems",String.valueOf(order.getGems()));
		map.put("cnyAmount",String.format("%d",order.getMoney()));
		map.put("currency",currency);
		map.put("transactionTime",transactionTime);
		map.put("channel",player.getPlat());
		map.put("sign",sign);

		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");

		HttpRespons hr=null;
		String content=null;
		try
		{
			Map<String,String> pro=new HashMap<String,String>();
			// pro.put("Content-Type","application/x-www-form-urlencoded");
			hr=request.send(verifyUrl,"POST",map,pro);
			content=hr.getContent();
			JSONObject jo=new JSONObject(content);
			int re=jo.getInt("result");
			String msg=jo.getString("msg");
			if(re==0)
			{
				if(log.isDebugEnabled()) log.debug("purchase success.");
				return null;
			}
			if(log.isErrorEnabled()) log.debug("purchase failed.");
			return msg;
		}
		catch(JSONException e)
		{
			if(log.isErrorEnabled())
				log.error("json exception.content="+content,e);
			return "exception";
		}
		catch(IOException e)
		{
			// 和验证服务器通讯失败
			if(log.isErrorEnabled()) log.error("communication failed.",e);
			
			return "communication_failed";
		}
	}
	private boolean checkServerState()
	{
		if(TimeKit.getSecondTime()-checkTime<TIME_OUT) return serverState;
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		try
		{
			request.send(stateUrl,"POST",map,pro);
			checkTime=TimeKit.getSecondTime();
			serverState=true;
		}
		catch(IOException e)
		{
			serverState=false;
		}
		return serverState;
	}

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		if(type==SYSTEM_STATE)
		{
			if(!checkServerState())
			{
				throw new DataAccessException(0,"chage center not available,try again later");
			}
			int gems=data.readUnsignedShort();
			String cur=data.readUTF();
			if(isValid(cur))
			{
				throw new ValidCurrencyExpection(cur);
			}
			boolean need_order=data.readBoolean();
			String pdid=data.readUTF();
			StringBuffer sb=new StringBuffer();
			JSONObject jo=getBannedDeviceState(sb);
			String deviceCheck=checkDevice(jo,player,pdid);
			if(sb.length()>0||deviceCheck!=null)
			{
				throw new DataAccessException(0,sb.length()>0?sb.toString():deviceCheck);
			}
			int orderid=-1;
			if(need_order)
			{
				orderid=SeaBackKit.sendHttpData(null,null,2,12).readInt();
			}
			objectFactory.createGemTrack(GemsTrack.SUBMIT_ORDER,
				player.getId(),gems,0,
				Resources.getGems(player.getResources()));
			data.clear();
			data.writeInt(orderid);
			data.writeUTF(UserToCenterPort.AREA_ID+"_"
				+UserToCenterPort.SERVER_ID);
			return data;
		}
		else if(type==CANCEL_ORDER)
		{
			objectFactory.createGemTrack(GemsTrack.CANCEL_ORDER,
				player.getId(),0,0,Resources.getGems(player.getResources()));
			data.clear();
			return data;
		}
		int foreByte=data.readUnsignedByte();
		String verifyInfo=data.readUTF();
		String device=data.readUTF();
		String serverId=data.readUTF();
		String accountId=data.readUTF();
		String roleName=data.readUTF();
		String currency=data.readUTF();
		String amount=data.readUTF();
		String appid=data.readUTF();
		String md5=data.readUTF();
		String pdid=data.readUTF();
		String purchaseInfo= (serverId+':'+accountId+':'+roleName+':'+currency+':'+amount);
		String pInfo=serverId+accountId+currency+amount;
		StringBuffer sb=new StringBuffer();
		JSONObject jo=getBannedDeviceState(sb);
		if(sb.length()>0||checkDevice(jo,player,pdid)!=null
			||checkVerify(verifyInfo,pInfo,appid,md5))
		{
			data.clear();
			data.writeByte(FAIL);
			data.writeByte(foreByte);
			log.error("--checkVerify---verifyInfo---:"+verifyInfo);
			log.error("--checkVerify---pInfo---:"+pInfo);
			log.error("--checkVerify---appid---:"+appid);
			log.error("--checkVerify---md5---:"+md5);
			log.error("--checkVerify---pdid---:"+pdid);
			return data;
		}
		String appCurrent=getAppKey(appid);
		if(appCurrent==null)
		{
			appid="com.seawar.one";
			appCurrent=getAppKey(appid);
		}
		int orderSid=1;
		// 订单保存
		int orderId=objectFactory.getOrderCache().getUidkit().getPlusUid();
		Order order=(Order)Order.factory.newSample(88);
		order.setId(orderId);
		order.setPlayerLevel(player.getLevel());
		order.setUserId(player.getId());
		order.setUserName(player.getName());
		order.setOrderState(0);
		order.setVerifyInfo(verifyInfo);
		order.setCreateAt(TimeKit.getSecondTime());
		order.setTransaction_id(String.valueOf(order.getId()));
		order.setPurchaseInfo(purchaseInfo);
		order.setPdid(pdid);
		// 存入order入数据库
		objectFactory.getOrderCache().getDbaccess().save(order);
		// //gems数量
		// int gems=data.readUnsignedShort();
		// //订单号
		// int orderSid = data.readUnsignedShort();
		// Resources.addGems(gems,player.getResources());
		// String appStoreId=data.readUTF();

		// System.out.println("appStoreId========="+appStoreId);
		String str=VerifyHeroChargeInfoManager
			.getInstance()
			.verifyHeroChargeInfo(verifyInfo,player.getId(),player.getName());
		if(!str.startsWith(appCurrent))
		{
			// 验证失败
			order.setOrderState(Order.ORDER_FAIL);
			objectFactory.getOrderCache().getDbaccess().delete(order);
			data.clear();
			data.writeByte(FAIL);
			data.writeByte(foreByte);
			return data;
		}

		String[] APP_ABOUT=app_About.get(appCurrent);
		if(APP_ABOUT==null)
		{
			if(log.isErrorEnabled())
				log.error("APP_ABOUT is null.IAP info="+appCurrent);
			throw new DataAccessException(0,"purchase failed");
		}
		String splitStr[]=TextKit.split(str,":");
		for(int i=0;i<APP_ABOUT.length;i++)
		{
			if(splitStr[0].equals(APP_ABOUT[i]))
			{
				orderSid=i+1;
				break;
			}
		}

		String transaction_id=splitStr[1];
		// 判断这个交易记录是否已经存在
		String sql="SELECT * FROM orders WHERE transaction_id='"
			+transaction_id+"'";
		if(objectFactory.getOrderCache().getDbaccess().loadSql(sql)!=null)
		{
			order.setOrderState(Order.TRANS_SAME);
			objectFactory.getOrderCache().getDbaccess().save(order);
			data.clear();
			data.writeByte(SAME);
			data.writeByte(foreByte);
			return data;
		}
		Order trueOrder=(Order)Order.factory.newSample(orderSid);
		trueOrder.setId(objectFactory.getOrderCache().getUidkit()
			.getPlusUid());
		if(!GMManager.IS_TEST)
		{
			// 验证订单唯一性,返回错误描述字符串
			String transactionTime=splitStr.length>2?splitStr[2]:"0";
			String check=checkOrder(player,transaction_id,verifyInfo,
				accountId,currency,amount,transactionTime,pdid,trueOrder);
			if(check!=null)
			{
				//订单校验失败，记录日志，
				if(log.isErrorEnabled())
					log.error("purchase failed.purchaseInfo="+purchaseInfo
						+",playerId="+player.getId()+",gems="
						+trueOrder.getGems()+",transactionId="+transaction_id);
				// 暂时当作订单重复处理
				data.clear();
				data.writeByte(SAME);
				data.writeByte(foreByte);
				return data;
			}
		}
		// 临时
		trueOrder.setIosType(1);
		trueOrder.setDevice(device);
		trueOrder.setCreateAt(TimeKit.getSecondTime());
		trueOrder.setVerifyInfo(verifyInfo);
		// 临时
		trueOrder.setOrderState(Order.ORDER_SUCCESS);
		trueOrder.setTransaction_id(transaction_id);
		trueOrder.setPlayerLevel(player.getLevel());
		trueOrder.setUserId(player.getId());
		trueOrder.setUserName(player.getName());
		User user=(User)objectFactory.getUserDBAccess().load(
			player.getUser_id()+"");
		trueOrder.setUdid(user.getLoginUdid());
		trueOrder.setIdfa(user.getIdfa());
		trueOrder.setIp(connect.getURL().getHost());
		trueOrder.setPurchaseInfo(purchaseInfo);
		trueOrder.setPlat_id("apple");
		trueOrder.setPdid(pdid);
		// 存入order入数据库
		try
		{
			objectFactory.getOrderCache().getDbaccess().save(trueOrder);
			// 删除之前保存的记录订单
			objectFactory.getOrderCache().getDbaccess().delete(order);
		}
		catch(Exception e)
		{
			data.clear();
			data.writeByte(SAME);
			data.writeByte(foreByte);
			return data;
		}

		int gMFlag=0;// 宝石和月卡的标识
		if(trueOrder instanceof MouthOrder)
		{
			gMFlag=1;
			int lastendtime=0;
			int endtime=0;
			lastendtime=player.getAttributes(PublicConst.END_TIME)==null?0
				:Integer
					.parseInt(player.getAttributes(PublicConst.END_TIME));
			if(lastendtime>TimeKit.getSecondTime())
				endtime=lastendtime
					+((PublicConst.MOUTHCARDDAYS+1)*PublicConst.DAY_SEC);
			else
			{
				lastendtime=(SeaBackKit.getTimesnight()-TimeKit
					.getSecondTime());// 获取当天到凌晨的系统时间
				endtime=TimeKit.getSecondTime()
					+(PublicConst.MOUTHCARDDAYS*PublicConst.DAY_SEC)
					+lastendtime;
			}
			int vp=player.getAttributes(PublicConst.VIP_POINT)==null?0
				:Integer.parseInt(player
					.getAttributes(PublicConst.VIP_POINT));
			player.setAttribute(PublicConst.VIP_POINT,
				String.valueOf(vp+PublicConst.VIPPOINT_NUM));
			player
				.setAttribute(PublicConst.END_TIME,String.valueOf(endtime));
			// 月卡日志记录
			objectFactory.createGemTrack(GemsTrack.MOUTHCARD,player.getId(),
				trueOrder.getGems(),trueOrder.getSid(),
				Resources.getGems(player.getResources()));
			AchieveCollect.mouthCard(1,player);
			JBackKit.sendMouthCard(player);
			JBackKit.sendResetResources(player);
		}
		else
		{
			// 添加宝石
			Resources.addGems(trueOrder.getGems(),player.getResources(),
				player);
			// 宝石日志记录
			objectFactory.createGemTrack(GemsTrack.GEMS_PAY,player.getId(),
				trueOrder.getGems(),trueOrder.getSid(),
				Resources.getGems(player.getResources()));
			// 成就数据采集
			AchieveCollect.gemsStock(trueOrder.getGems(),player);
			boolean isAddable=false;
			try
			{
				isAddable=getBannedSystemState(jo);
			}
			catch(JSONException e)
			{
				if(log.isErrorEnabled()) log.debug("banned state failed."+e);
			}
			if(isAddable) SeaBackKit.setPlayerPaymentDevices(player,pdid);
		}
		player.flushVIPlevel();
		data.clear();
		data.writeByte(SUCCESS);
		data.writeLong(Resources.getGems(player.getResources()));
		data.writeLong(player.getVpPoint());
		data.writeByte(foreByte);
		data.writeByte(gMFlag);
		return data;
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	/** Key 苹果产品id Value 苹果产品id数组 **/
	public void addIAP(String key,String[] ipas)
	{
		app_About.put(key,ipas);
	}

	/** 根据前台传过来的appId获取当前Key **/
	public static String getAppKey(String appId)
	{
		if(appId.length()==0) return null;
		String endStr="";
		for(int i=0;i<2;i++)
		{
			int s=appId.indexOf(".");
			if(s==-1) return null;
			if(i==0) s++;
			endStr+=appId.substring(0,s);
			appId=appId.substring(s);
		}
		return endStr;
	}

	/** 是否为禁用货币 */
	public boolean isValid(String cur)
	{
		if(PublicConst.VALID_CURRENCY==null) return false;
		for(int i=0;i<PublicConst.VALID_CURRENCY.length;i++)
		{
			if(cur.equals(PublicConst.VALID_CURRENCY[i])) return true;
		}
		return false;
	}
	/** 检测认证字符串是否被篡改  */
	public boolean checkVerify(String verifyInfo,String pInfo,String appid,String md5)
	{
		StringBuffer sb=new StringBuffer();
		sb.append(verifyInfo);
		sb.append(pInfo);
		sb.append(appid);
		sb.append(distortKey);
		String sign=MD5(sb.toString());
		return !md5.equalsIgnoreCase(sign);
	}

	public static void setServername(String servername)
	{
		BuyGemsPort.servername=servername;
	}

	
	public void setStateUrl(String stateUrl)
	{
		this.stateUrl=stateUrl;
	}
	
	private String checkDevice(JSONObject jo,Player player,String pdid)
	{
		try
		{
			boolean state=getBannedSystemState(jo);
			int max=getBannedSystemMax(jo);
			if(state&&!SeaBackKit.isPaymentDeviceAvailable(player,pdid,max))
			{
				String msg="player pay on too many devices";
				if(log.isDebugEnabled()) log.debug(msg+"|"+pdid+"|"+player.getName());
				return msg;
			}
			return null;
		}
		catch(JSONException e)
		{
			if(log.isErrorEnabled())
				log.error("json exception.content="+jo,e);
			return "exception";
		}
	}
	
	private int getBannedSystemMax(JSONObject jo) throws JSONException
	{
		return jo.getInt("max");
	}
	
	private boolean getBannedSystemState(JSONObject jo) throws JSONException
	{
		return jo.getBoolean("state");
	}
	
	private JSONObject getBannedDeviceState(StringBuffer sb)
	{
		Map<String,String> map=new HashMap<String,String>();
		HttpRequester request=new HttpRequester();
		request.setDefaultContentEncoding("UTF-8");
		map.put("action","state");
		HttpRespons hr=null;
		String content=null;
		try
		{
			Map<String,String> pro=new HashMap<String,String>();
			// pro.put("Content-Type","application/x-www-form-urlencoded");
			hr=request.send(bannedDeviceUrl,"POST",map,pro);
			content=hr.getContent();
			JSONObject jo=new JSONObject(content);
			return jo;
		}
		catch(JSONException e)
		{
			if(log.isErrorEnabled())
				log.error("json exception.content="+content,e);
			sb.append("exception");
			return null;
		}
		catch(IOException e)
		{
			// 和验证服务器通讯失败
			if(log.isErrorEnabled()) log.error("communication failed.",e);
			sb.append("communication_failed");
			return null;
		}
	}

	public String getBannedDeviceUrl()
	{
		return bannedDeviceUrl;
	}

	public void setBannedDeviceUrl(String bannedDeviceUrl)
	{
		this.bannedDeviceUrl=bannedDeviceUrl;
	}
}
