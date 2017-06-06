package foxu.sea;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.order.MouthOrder;
import foxu.sea.order.Order;
import shelby.httpserver.HttpRequestMessage;
import shelby.httpclient.HttpHandlerInterface;

/**
 * @author yw ������Ϸ���� ����֪ͨ
 */
public class PortHttpCharge implements HttpHandlerInterface
{

	/** �ɹ���ʧ�� */
	public static final int FAIL=0,SUCCESS=1;
	// /** token��ʱ */
	// public static final int TIME_OUT=15*60-10;
	/** ����ORDER���ܵȴ� */
	public static final int ORDER=1;
	/** Ĭ�ϵ�Base64������㷨 */
	public static final Base64 BASE64=new Base64();
	// /**qq֧��token��*/
	// public static IntKeyHashMap tokenMap=new IntKeyHashMap();
	/* fields */
	/** ʹ�õ�Base64������㷨 */
	Base64 base64=BASE64;
	/** port */
	int port;

	/** ƻ����Ʒid��Ӧ��order sid */
	public static final String APP_ABOUT[]={"com.seawar.one",
		"com.seawar.two","com.seawar.three","com.seawar.four",
		"com.seawar.five1","com.seawar.six1","com.seawar.25"};

	/** ������������� */
	CreatObjectFactory objectFactory;

	public static Logger log=LogFactory.getLogger(PortHttpCharge.class);

	public String excuteString(HttpRequestMessage request,String ip)
	{
		// TODO �Զ����ɷ������
		return null;
	}

	/**
	 * @return port
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port Ҫ���õ� port
	 */
	public void setPort(int port)
	{
		this.port=port;
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public byte[] excute(HttpRequestMessage request,String ip)
	{
		// TODO �Զ����ɷ������
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int type=data.readUnsignedByte();
		ByteBuffer bb=new ByteBuffer();
		log.info("-----------type-----------:"+type);
		if(type==ORDER)
		{
			int pid=TextKit.parseInt(data.readUTF());
			Player player=objectFactory.getPlayerById(pid);
			if(player==null)
			{
				log.debug(pid+":------no---player-----------:"+player);
				data.clear();
				data.writeByte(FAIL);
				base64.encode(data.toArray(),bb);
				return bb.toArray();
			}
			log.info("----------3333-----------:");
			String goodsId=data.readUTF();
			String transId=data.readUTF();
			String plat_id=data.readUTF();
			log.info("----------4444-----------:");
			log.debug("goodsId=========================="+goodsId);
			int orderSid=0;
			// �ж�������׼�¼�Ƿ��Ѿ�����
			String sql="SELECT * FROM orders WHERE transaction_id=\'"+transId+"\'";
			if(objectFactory.getOrderCache().getDbaccess().loadSql(sql)!=null)
			{
				log.debug(transId+":------trans---same-----------:"
					+player.getName());
				data.clear();
				data.writeByte(SUCCESS);
				base64.encode(data.toArray(),bb);
				return bb.toArray();
			}
			log.info("----------5555-----------:");
			for(int i=0;i<APP_ABOUT.length;i++)
			{
				if(goodsId.equals(APP_ABOUT[i]))
				{
					orderSid=i+1;
					break;
				}
			}
			log.info("----------6666-----------:");
			Order order=(Order)Order.factory.newSample(orderSid);
			if(order==null)
			{
				log.debug(goodsId+":------error---goodsid-----------:"
					+player.getName());
				data.clear();
				data.writeByte(FAIL);
				base64.encode(data.toArray(),bb);
				return bb.toArray();
			}
			log.info("----------7777-----------:");
			// ��������
			int orderId=objectFactory.getOrderCache().getUidkit()
				.getPlusUid();
			order.setId(orderId);
			order.setPlat_id(plat_id);
			order.setTransaction_id(transId);
			order.setPlayerLevel(player.getLevel());
			order.setUserId(player.getId());
			order.setUserName(player.getName());
			order.setCreateAt(TimeKit.getSecondTime());
			log.info("----------8888-----------:");
			if(order instanceof MouthOrder)
			{
				int lastendtime=0;
				int endtime=0;
				lastendtime=player.getAttributes(PublicConst.END_TIME)==null
					?0:Integer.parseInt(player
						.getAttributes(PublicConst.END_TIME));
				if(lastendtime>TimeKit.getSecondTime())
					endtime=lastendtime
						+((PublicConst.MOUTHCARDDAYS+1)*PublicConst.DAY_SEC);
				else
				{
					lastendtime=(SeaBackKit.getTimesnight()-TimeKit
						.getSecondTime());// ��ȡ���쵽�賿��ϵͳʱ��
					endtime=TimeKit.getSecondTime()
						+(PublicConst.MOUTHCARDDAYS*PublicConst.DAY_SEC)
						+lastendtime;
				}
				int vp=player.getAttributes(PublicConst.VIP_POINT)==null?0
					:Integer.parseInt(player
						.getAttributes(PublicConst.VIP_POINT));
				player.setAttribute(PublicConst.VIP_POINT,
					String.valueOf(vp+PublicConst.VIPPOINT_NUM));
				player.setAttribute(PublicConst.END_TIME,
					String.valueOf(endtime));
				AchieveCollect.mouthCard(1,player);
				JBackKit.sendMouthCard(player);
				JBackKit.sendResetResources(player);
			}
			else
			{
				// ��ӱ�ʯ
				Resources.addGems(order.getGems(),player.getResources(),
					player);
				// ��ʯ��־��¼
				objectFactory.createGemTrack(GemsTrack.GEMS_PAY,
					player.getId(),order.getGems(),order.getSid(),
					Resources.getGems(player.getResources()));
				// �ɾ����ݲɼ�
				AchieveCollect.gemsStock(order.getGems(),player);

			}
			log.info("----------999-----------:");
			player.flushVIPlevel();
			// ����order�����ݿ�
			objectFactory.getOrderCache().getDbaccess().save(order);
			log.debug(goodsId+":------success-----------:"+order.getSid());
			data.clear();
			data.writeByte(SUCCESS);
			base64.encode(data.toArray(),bb);
			return bb.toArray();
		}
		log.info("----------end-----------:");
		return data.toArray();
	}

	/**
	 * ����key�������� ��һkey
	 * 
	 * @param key id
	 * @return ���ص�ByteBuffer
	 */
	public ByteBuffer load(String data)
	{
		if(data==null||data.equals("null")) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		base64.decode(data,0,data.length(),bb);
		return bb;
	}

}
