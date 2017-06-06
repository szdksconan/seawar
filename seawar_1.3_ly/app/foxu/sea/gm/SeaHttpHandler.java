package foxu.sea.gm;

import java.io.UnsupportedEncodingException;

import foxu.sea.kit.SeaBackKit;
import mustang.log.LogFactory;
import mustang.log.Logger;
import shelby.httpclient.HttpPortService;
import shelby.httpserver.HttpHandler;
import shelby.httpserver.HttpRequestMessage;
import shelby.httpserver.HttpResponseMessage;

/** GM操作handler */
public class SeaHttpHandler implements HttpHandler
{
	HttpPortService httpService;
	
	Logger log=LogFactory.getLogger(SeaHttpHandler.class);
	
	/** 2 91充值端口 3android push端口 6 第三方充值端口 7删号 9web线下活动 10跨服中心端口  11跨服客服端口*/
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
				// 回应
				HttpResponseMessage response=new HttpResponseMessage();
				response.setContentType("text/plain");
				response
					.setResponseCode(HttpResponseMessage.HTTP_STATUS_SUCCESS);
				response.appendBody(bytes);
				return response;
			}
			responses = httpService.getPort(port).excuteString(request,ip);
		}
		//回应
		HttpResponseMessage response=new HttpResponseMessage();
		response.setContentType("text/html");
		response.getHeaders().put("Content-Encoding","UTF-8");
		response.getHeaders().put("Access-Control-Allow-Origin","*");
		try
		{
			// 转换UTF-8不会影响base64编码的字符串
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
	 * @param httpService 要设置的 httpService
	 */
	public void setHttpService(HttpPortService httpService)
	{
		this.httpService=httpService;
	}
}
