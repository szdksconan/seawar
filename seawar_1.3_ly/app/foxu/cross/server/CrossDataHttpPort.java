package foxu.cross.server;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntKeyHashMap;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;


/**
 * ������� ���ն˿�
 * @author yw
 *
 */
public class CrossDataHttpPort implements HttpHandlerInterface
{
	private static Logger log=LogFactory.getLogger(CrossDataHttpPort.class);
	
	/** ʹ�õ�Base64������㷨 */
	Base64 base64=new Base64();
	
	/** ��Ϣ������ map*/
	IntKeyHashMap map=new IntKeyHashMap();

	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
//		System.out.println("-------CrossDataHttpPort---------");
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int sid=data.readUnsignedShort();// ����sid
		DataAction action=(DataAction)map.get(sid);
//		System.out.println("---action---sid-------:"+sid);
		if(action==null)
		{
			data.clear();
			data.writeByte(127);// �޷����������Ϣ
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

	/**
	 * Base64������㷨 ����������ת��Ϊ�ַ���
	 */
	public String createBase64(ByteBuffer data)
	{
		byte[] array=data.toArray();
		data.clear();
		base64.encode(array,0,array.length,data);
		return new String(data.getArray(),0,data.top());
	}
}
