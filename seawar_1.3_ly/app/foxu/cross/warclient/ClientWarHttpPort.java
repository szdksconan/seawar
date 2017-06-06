package foxu.cross.warclient;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;

/**
 * ���� ������ĵ�����
 * 
 * @author yw
 * 
 */
public class ClientWarHttpPort implements HttpHandlerInterface
{

	//private static Logger log=LogFactory.getLogger(ClientWarHttpPort.class);

	/**
	 * �������ͳ��� STEP ͬ������,REP Ԥ��ս�� ,SN nǿ����,CUT
	 * ��ȡ�������,PROͬ��������Ա,FIN_REP����ս��,SEND_BETͬ��Ѻע��
	 */
	public static int STEP=0,REP=1,SN=2,CUT=3,PRO=4,FIN_REP=5,SEND_BET=6;

	/** ʹ�õ�Base64������㷨 */
	Base64 base64=new Base64();

	/** ���ս�ͷ��˹����� */
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
		data.writeByte(0);// Ĭ�Ϸ���
		return createBase64(data).getBytes();
	}

	@Override
	public String excuteString(HttpRequestMessage request,String ip)
	{
		// TODO Auto-generated method stub
		return null;
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
