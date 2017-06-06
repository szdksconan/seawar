package foxu.sea.port;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import javapns.json.JSONException;
import javapns.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;




import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.User;
import foxu.sea.ValidCurrencyExpection;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.order.MouthOrder;
import foxu.sea.order.Order;


/**
 * tstore充值端口
 * @author yw
 *
 */
public class BuyGemsTstorePort extends AccessPort
{

	public static final int SUCCESS=1,FAIL=2,SAME=3;

	public static final int SYSTEM_STATE=1,BUG_GEMS=2,CANCEL_ORDER=3;

	/** 苹果产品id对应的order sid */
	public static final String APP_ABOUT[]={
		"0910017744",
		"0910017745",
		"0910017746",
		"0910017747",
		"0910017748",
		"0910017749",
		"0910017750"};

	CreatObjectFactory objectFactory;
	
	String appid="OA00668142";
	
	boolean test;
	
	Logger log=LogFactory.getLogger(BuyGemsTstorePort.class);

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
			int gems=data.readUnsignedShort();
			String cur=data.readUTF();
			if(isValid(cur))
			{
				throw new ValidCurrencyExpection(cur);
			}
			boolean need_order=data.readBoolean();
			int orderid=-1;
			if(need_order)
			{
				orderid=SeaBackKit.sendHttpData(null,null,2,12).readInt();
			}
			objectFactory.createGemTrack(GemsTrack.SUBMIT_ORDER,
				player.getId(),gems,0,Resources.getGems(player.getResources()));
			data.clear();
			data.writeInt(orderid);
			data.writeUTF(UserToCenterPort.AREA_ID+"_"+UserToCenterPort.SERVER_ID);
			return data;
		}
		else if(type==CANCEL_ORDER)
		{
			objectFactory.createGemTrack(GemsTrack.CANCEL_ORDER,
				player.getId(),0,0,Resources.getGems(player.getResources()));
			data.clear();
			return data;
		}
//		int foreByte=data.readUnsignedByte();
		String txid=data.readUTF();
		String verifyInfo=data.readUTF();
		log.info("---------txid-------："+txid);
		log.info("---------verifyInfo-------："+verifyInfo);
		String device=data.readUTF();
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
		order.setDevice(device);
		order.setCreateAt(TimeKit.getSecondTime());
		order.setTransaction_id(order.getId()+"");
		order.setPlat_id("tstore");
		// 存入order入数据库
		objectFactory.getOrderCache().getDbaccess().save(order);

		String procId=verify(txid,verifyInfo);
		if(procId==null)
		{
			log.info("---------ORDER_FAIL--------");
			// 验证失败
			order.setOrderState(Order.ORDER_FAIL);
			objectFactory.getOrderCache().getDbaccess().delete(order);
			data.clear();
			data.writeByte(FAIL);
//			data.writeByte(foreByte);
			return data;
		}
		for(int i=0;i<APP_ABOUT.length;i++)
		{
			if(procId.equals(APP_ABOUT[i]))
			{
				orderSid=i+21;
				break;
			}
		}
		// 判断这个交易记录是否已经存在
		String sql="SELECT * FROM orders WHERE transaction_id='"
			+txid+"'";
		if(objectFactory.getOrderCache().getDbaccess().loadSql(sql)!=null)
		{
			log.info("---------TRANS_SAME--------");
			order.setOrderState(Order.TRANS_SAME);
			objectFactory.getOrderCache().getDbaccess().save(order);
			data.clear();
			data.writeByte(SAME);
//			data.writeByte(foreByte);
			return data;
		}
		Order trueOrder=(Order)Order.factory.newSample(orderSid);
		trueOrder.setId(objectFactory.getOrderCache().getUidkit()
			.getPlusUid());
		
		int gMFlag=0;//宝石和月卡的标识
		if(trueOrder instanceof MouthOrder)
		{
			gMFlag=1;
			int lastendtime=0;
			int endtime=0;
			lastendtime=player.getAttributes(PublicConst.END_TIME)==null?0:Integer
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
			int vp=player.getAttributes(PublicConst.VIP_POINT)==null?0:Integer.parseInt(player.getAttributes(PublicConst.VIP_POINT));
			player.setAttribute(PublicConst.VIP_POINT,String.valueOf(vp+PublicConst.VIPPOINT_NUM));
			player.setAttribute(PublicConst.END_TIME,String.valueOf(endtime));
			// 月卡日志记录
			objectFactory.createGemTrack(GemsTrack.MOUTHCARD,player.getId(),
				trueOrder.getGems(),trueOrder.getSid(),
				Resources.getGems(player.getResources()));
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

		}
		player.flushVIPlevel();
		// 临时
		trueOrder.setIosType(1);
		trueOrder.setDevice(device);
		trueOrder.setCreateAt(TimeKit.getSecondTime());
		trueOrder.setVerifyInfo(verifyInfo);
		log.info("---------------------ORDER_SUCCESS----------------");
		// 临时
		trueOrder.setOrderState(Order.ORDER_SUCCESS);
		trueOrder.setTransaction_id(txid);
		trueOrder.setPlayerLevel(player.getLevel());
		trueOrder.setUserId(player.getId());
		trueOrder.setUserName(player.getName());
		User user=(User)objectFactory.getUserDBAccess().load(player.getUser_id()+"");
		trueOrder.setUdid(user.getLoginUdid());
		trueOrder.setIdfa(user.getIdfa());
		trueOrder.setIp(connect.getURL().getHost());
		trueOrder.setPlat_id("tstore");
		// 存入order入数据库
		objectFactory.getOrderCache().getDbaccess().save(trueOrder);

		// 删除之前保存的记录订单
		objectFactory.getOrderCache().getDbaccess().delete(order);
		data.clear();
		data.writeByte(SUCCESS);
		data.writeLong(Resources.getGems(player.getResources()));
		data.writeLong(player.getVpPoint());
//		data.writeByte(foreByte);
		data.writeByte(gMFlag);
		return data;
	}

	
	/** 仅返回JSON格式适用 */
	public JSONObject sendHttpsJson(String urlString,String method,
		String body,Map<String,String> properties,String encode)
	{

		try
		{
			// 创建连接
			URL url=new URL(urlString);
			HttpsURLConnection connection=(HttpsURLConnection)url
				.openConnection();
			connection.setConnectTimeout(20000);
			connection.setReadTimeout(20000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod(method);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type","application/json");
			if(properties!=null) for(String key:properties.keySet())
			{
				connection.addRequestProperty(key,properties.get(key));
			}
			connection.connect();

			// 请求
			DataOutputStream out=new DataOutputStream(
				connection.getOutputStream());
			if(body!=null) out.writeBytes(body);
			out.flush();
			out.close();

			// 读取响应
			BufferedReader reader=new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
			String lines;
			StringBuffer sb=new StringBuffer();
			if(encode==null)encode=Charset.defaultCharset().name();
			while((lines=reader.readLine())!=null)
			{
				lines=new String(lines.getBytes(),encode);
				sb.append(lines);
			}
			reader.close();
			// 断开连接
			connection.disconnect();
			return new JSONObject(sb.toString());
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/** 验证 */
	public String verify(String txid,String signdata)
	{
		StringBuffer body=new StringBuffer();
		body.append("{\"txid\":\""+txid+"\"");
		body.append(", \"appid\":\""+appid+"\"");
		body.append(",\"signdata\":\""+signdata+"\"}");

		String url="https://iap.tstore.co.kr/digitalsignconfirm.iap";
		if(test) url="https://iapdev.tstore.co.kr/digitalsignconfirm.iap";
		JSONObject respons=sendHttpsJson(url,"POST",body.toString(),null,
			null);
		if(respons!=null)log.info("==========verify==result===========:"+respons.toString());
		try
		{
			if(respons==null||respons.getInt("status")!=0) return null;
			String procStr=respons.getString("product");
			procStr=procStr.substring(1,procStr.length());
			JSONObject proc=new JSONObject(procStr);
			if(proc==null||proc.getString("product_id")==null) return null;
			return proc.getString("product_id");
		}
		catch(Exception e)
		{
			// TODO: handle exception
		}
		return null;

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
	
	public boolean isTest()
	{
		return test;
	}
	
	public void setTest(boolean test)
	{
		this.test=test;
	}


	
	public String getAppid()
	{
		return appid;
	}


	
	public void setAppid(String appid)
	{
		this.appid=appid;
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
	
	


}
