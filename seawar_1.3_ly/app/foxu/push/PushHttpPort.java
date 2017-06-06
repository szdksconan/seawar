package foxu.push;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;

/**
 * push请求处理端口
 * @author yw
 *
 */
public class PushHttpPort implements HttpHandlerInterface
{
	final Base64 base64=Base64.getInstance();

	@Override
	public byte[] excute(HttpRequestMessage request,String arg1)
	{
		String device=request.getParameter("device");
		ByteBuffer data=new  ByteBuffer();
		if(device==null)
		{
			data.writeByte(0);
		}else
		{
			AndroidPush.androidPush.getPush(device,data);
		}
		return encode(data);
	}

	@Override
	public String excuteString(HttpRequestMessage arg0,String arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}
	private byte[] encode(ByteBuffer data)
	{
		return base64.encode(data.getArray(),data.offset(),data.length());
	}

}
