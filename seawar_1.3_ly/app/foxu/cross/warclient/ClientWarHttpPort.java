package foxu.cross.warclient;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;

/**
 * 接收 跨服中心的数据
 * 
 * @author yw
 * 
 */
public class ClientWarHttpPort implements HttpHandlerInterface
{

	//private static Logger log=LogFactory.getLogger(ClientWarHttpPort.class);

	/**
	 * 播报发送常量 STEP 同步进度,REP 预赛战报 ,SN n强播报,CUT
	 * 提取玩家数据,PRO同步晋级人员,FIN_REP决赛战报,SEND_BET同步押注量
	 */
	public static int STEP=0,REP=1,SN=2,CUT=3,PRO=4,FIN_REP=5,SEND_BET=6;

	/** 使用的Base64编解码算法 */
	Base64 base64=new Base64();

	/** 跨服战客服端管理器 */
	ClientWarManager cWarManager;

	public ClientWarManager getcWarManager()
	{
		return cWarManager;
	}

	public void setcWarManager(ClientWarManager cWarManager)
	{
		this.cWarManager=cWarManager;
	}

	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
		// //System.out.println("-------ClientWarHttpPort---------");
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int type=data.readUnsignedByte();
//		//System.out.println("----------ClientWarHttpPort----type------:"+type);
		if(type==STEP)
		{
			cWarManager.updateStep(data);
		}
		else if(type==REP)
		{
			cWarManager.receiveRep(data,false);
		}
		else if(type==SN)
		{
			cWarManager.receiveSn(data);
		}
		else if(type==CUT)
		{
			//System.out.println("-----------activeSubmit----------");
			cWarManager.activeSubmit();
		}
		else if(type==PRO)
		{
			cWarManager.updateSnPlayer(data);
		}
		else if(type==FIN_REP)
		{
			cWarManager.receiveRep(data,true);
		}
		else if(type==SEND_BET)
		{
			cWarManager.updateSnBet(data);
		}
		data.clear();
		data.writeByte(0);// 默认返回
		return createBase64(data).getBytes();
	}

	@Override
	public String excuteString(HttpRequestMessage request,String ip)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 根据key加载数据 单一key
	 * 
	 * @param key id
	 * @return 返回的ByteBuffer
	 */
	public ByteBuffer load(String data)
	{
		if(data==null||data.equals("null")) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		base64.decode(data,0,data.length(),bb);
		return bb;
	}

	/**
	 * Base64编解码算法 二进制数据转化为字符串
	 */
	public String createBase64(ByteBuffer data)
	{
		byte[] array=data.toArray();
		data.clear();
		base64.encode(array,0,array.length,data);
		return new String(data.getArray(),0,data.top());
	}

}
