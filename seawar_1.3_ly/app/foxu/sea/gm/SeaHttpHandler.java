package foxu.sea.gm;

import java.io.UnsupportedEncodingException;

import foxu.sea.kit.SeaBackKit;
import mustang.log.LogFactory;
import mustang.log.Logger;
import shelby.httpclient.HttpPortService;
import shelby.httpserver.HttpHandler;
import shelby.httpserver.HttpRequestMessage;
import shelby.httpserver.HttpResponseMessage;

/** GM����handler */
public class SeaHttpHandler implements HttpHandler
{
	HttpPortService httpService;
	
	Logger log=LogFactory.getLogger(SeaHttpHandler.class);
	
	/** 2 91��ֵ�˿� 3android push�˿� 6 ��������ֵ�˿� 7ɾ�� 9web���» 10������Ķ˿�  11����ͷ��˿�*/
	public int[] excute={2,3,6,7,9,10,11,12};

	public HttpResponseMessage handle(HttpRequestMessage request,String ip)
	{
//		log.info("-----------request---------------:"+request.toString());
		String responses = "port is null";
		if(request.getParameter("port")!=null&&!request.getParameter("port").equals(""))
		{
			int port = Integer.parseInt(request.getParameter("port"));
			if(SeaBackKit.isContainValue(excute,port))
			{
				byte bytes[]=httpService.getPort(port).excute(request,ip);
				// ��Ӧ
				HttpResponseMessage response=new HttpResponseMessage();
				response.setContentType("text/plain");
				response
					.setResponseCode(HttpResponseMessage.HTTP_STATUS_SUCCESS);
				response.appendBody(bytes);
				return response;
			}
			responses = httpService.getPort(port).excuteString(request,ip);
		}
		//��Ӧ
		HttpResponseMessage response=new HttpResponseMessage();
		response.setContentType("text/html");
		response.getHeaders().put("Content-Encoding","UTF-8");
		response.getHeaders().put("Access-Control-Allow-Origin","*");
		try
		{
			// ת��UTF-8����Ӱ��base64������ַ���
			response.appendBody(responses.getBytes("UTF-8"));
			response.setResponseCode(HttpResponseMessage.HTTP_STATUS_SUCCESS);
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
			response.setResponseCode(HttpResponseMessage.HTTP_STATUS_NOT_FOUND);
		}
		return response;
	}


	
	
	/**
	 * @return httpService
	 */
	public HttpPortService getHttpService()
	{
		return httpService;
	}



	
	/**
	 * @param httpService Ҫ���õ� httpService
	 */
	public void setHttpService(HttpPortService httpService)
	{
		this.httpService=httpService;
	}
}
