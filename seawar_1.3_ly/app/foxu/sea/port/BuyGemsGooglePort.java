package foxu.sea.port;

import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.ValidCurrencyExpection;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.charge.Security;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.order.MouthOrder;
import foxu.sea.order.Order;
import foxu.sea.uid.UidKit;

/** 宝石充值端口 1011 */
public class BuyGemsGooglePort extends AccessPort
{

	public static final int SUCCESS=1,FAIL=2,SAME=3;

	public static final int SYSTEM_STATE=1,BUG_GEMS=2,CANCEL_ORDER=3;

	/** 苹果产品id对应的order sid */
	// public static final String APP_ABOUT[]={"com.seawar.one",
	// "com.seawar.two","com.seawar.three","com.seawar.four",
	// "com.seawar.five1","com.seawar.six1"};
//	public static final String APP_ABOUT[]={"seawar.1.50","seawar.3.160",
//		"seawar.8.460","seawar.15.960","seawar.50.3420","seawar.100.8400"};
//	public static String PUBLIC_KEY="";
	IntKeyHashMap aboutMap=new IntKeyHashMap();
	IntKeyHashMap keyMap=new IntKeyHashMap();
	/** 日志记录 */
	private static final Logger log=LogFactory.getLogger(BuyGemsGooglePort.class);

	/**
	 * 验证订单
	 * 
	 * @param order 缓存的订单对象
	 * @param objectFactory
	 * @param player 支付的player
	 * @return 返回Order表示验证成功
	 */
	public Order purchaseDataVerify(Order order,
		CreatObjectFactory objectFactory,Player player,int platid)
	{
		try
		{
			if(!Security.verifyPurchase((String)keyMap.get(platid),order.getVerifyInfo(),
				order.getDataSignature())) return null;
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled()) log.warn("verification failed",e);
			return null;
		}
		try
		{
			JSONObject jObject=new JSONObject(order.getVerifyInfo());
			String orderIdStr=(String)jObject.get("orderId");
			String sql="SELECT * FROM orders WHERE transaction_id='"
				+orderIdStr+"' and order_state=1";
			if(objectFactory.getOrderCache().getDbaccess().loadSql(sql)!=null)
			{
				if(log.isWarnEnabled())
					log.warn("verification failed",new Throwable(
						"Same purchaseData."));
				return null;
			}
			String productId=(String)jObject.get("productId");
			int orderSid=0;
			String[] APP_ABOUT=(String[])aboutMap.get(platid);
			for(int i=0;i<APP_ABOUT.length;i++)
			{
				if(productId.equals(APP_ABOUT[i]))
				{
					orderSid=i+11;
					break;
				}
			}
			Order trueOrder=(Order)Order.factory.newSample(orderSid);
			trueOrder.setId(objectFactory.getOrderCache().getUidkit()
				.getPlusUid());
			if(trueOrder instanceof MouthOrder)
			{
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

			}
			player.flushVIPlevel();

			trueOrder.setIosType(1);
			trueOrder.setDevice(order.getDevice());
			trueOrder.setCreateAt(TimeKit.getSecondTime());
			trueOrder.setVerifyInfo(order.getVerifyInfo());
			trueOrder.setDataSignature(order.getDataSignature());
			trueOrder.setOrderState(Order.ORDER_SUCCESS);
			trueOrder.setTransaction_id(orderIdStr);
			trueOrder.setPlayerLevel(player.getLevel());
			trueOrder.setUserId(player.getId());
			trueOrder.setUserName(player.getName());
			trueOrder.setPlat_id("google");
			return trueOrder;
		}
		catch(JSONException e)
		{
			if(log.isWarnEnabled())
				log.warn(
					"verification failed",
					new Throwable("PurchaseData error. purchaseData="
						+order.getVerifyInfo()));
			return null;
		}
	}

	CreatObjectFactory objectFactory;
	UidKit uidKit;

	public UidKit getUidKit()
	{
		return uidKit;
	}

	public void setUidKit(UidKit uidKit)
	{
		this.uidKit=uidKit;
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
		else if(type==BUG_GEMS)
		{
			String purchaseData=data.readUTF();
			String dataSignature=data.readUTF();
			String device=data.readUTF();
			//id--------增加----------
			int platid=1;
			try
			{
				platid=data.readInt();
			}
			catch(Exception e)
			{
			}
			int orderId=objectFactory.getOrderCache().getUidkit()
				.getPlusUid();
			Order order=null;
			try
			{
				JSONObject jObject=new JSONObject(purchaseData);
				order=(Order)Order.factory.newSample(88);
				order.setId(orderId);
				order.setPlayerLevel(player.getLevel());
				order.setUserId(player.getId());
				order.setUserName(player.getName());
				order.setOrderState(0);
				order.setVerifyInfo(purchaseData);
				order.setCreateAt(TimeKit.getSecondTime());
				order.setTransaction_id((String)jObject.get("orderId"));
				order.setDataSignature(dataSignature);
				order.setDevice(device);
				objectFactory.getOrderCache().getDbaccess().save(order);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}

			Order overOrder=purchaseDataVerify(order,objectFactory,player,platid);
			if(overOrder==null)
			{
				data.clear();
				writeVertifyFail(data);
				//	data.writeByte(foreByte);
				// objectFactory.getOrderCache().getDbaccess().delete(order);
				return data;
			}
//			objectFactory.createGemTrack(GemsTrack.GEMS_PAY,
//				player.getId(),overOrder.getGems(),0,player.getResources()[Resources.GEMS]);
			// 删除之前保存的记录订单
			objectFactory.getOrderCache().getDbaccess().delete(order);
			// 存入order入数据库
			objectFactory.getOrderCache().getDbaccess().save(overOrder);
			// System.out.println("........>>>>>>>>:"+connect.isActive());
			data.clear();
			data.writeByte(SUCCESS);
			data.writeLong(Resources.getGems(player.getResources()));
			data.writeLong(player.getVpPoint());
			data.writeByte(overOrder instanceof MouthOrder?1:0);
			return data;
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

	/**
	 * 写入验证账单失败消息
	 * 
	 * @param data
	 * @return
	 */
	public ByteBuffer writeVertifyFail(ByteBuffer data)
	{
		data.writeByte(FAIL);
		// data.writeByte(foreByte);
		return data;
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
	/** 增加key-商品 */
	public void addKeyAbout(int pid,String key,String[] about)
	{
		keyMap.put(pid,key);
		aboutMap.put(pid,about);
	}
}
