package foxu.cross.server;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntKeyHashMap;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;


/**
 * 跨服数据 接收端口
 * @author yw
 *
 */
public class CrossDataHttpPort implements HttpHandlerInterface
{
	private static Logger log=LogFactory.getLogger(CrossDataHttpPort.class);
	
	/** 使用的Base64编解码算法 */
	Base64 base64=new Base64();
	
	/** 消息处理器 map*/
	IntKeyHashMap map=new IntKeyHashMap();

	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
//		System.out.println("-------CrossDataHttpPort---------");
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int sid=data.readUnsignedShort();// 跨服活动sid
		DataAction action=(DataAction)map.get(sid);
//		System.out.println("---action---sid-------:"+sid);
		if(action==null)
		{
			data.clear();
			data.writeByte(127);// 无法处理该类消息
			log.error("----CrossDataPort---error---sid:"+sid);
		}
		else
		{
			action.readAction(data);
		}
//		System.out.println("--return--data-----len--:"+data.length());
		return createBase64(data).getBytes();
	}

	@Override
	public String excuteString(HttpRequestMessage request,String ip)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public void addAction(int sid,DataAction action)
	{
		map.put(sid,action);
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
