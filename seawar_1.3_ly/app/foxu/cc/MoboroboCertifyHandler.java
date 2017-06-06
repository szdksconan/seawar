package foxu.cc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import javapns.json.JSONException;
import javapns.json.JSONObject;

import mustang.codec.Base64;
import mustang.codec.CodecKit;
import mustang.codec.MD5;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.net.DataAccessException;

/**
 * 百度国际版认证处理器
 * 
 * @author comeback
 */
public class MoboroboCertifyHandler implements CertifyHandler
{

	private static final Logger log=LogFactory
		.getLogger(MoboroboCertifyHandler.class);

	/** 认证结果代码 */
	public static final int RESULT_OK=1;

	public static final String ACCOUNT_PREFIX="moborobo:";

	/** 正式认证地址 */
	public static String CERTIFY_URL="http://query.cash.moborobo.com/CpLoginStateQuery.ashx";

	/** APPID */
	public static int APPID=112;

	/** APPKEY */
	public static String APPKEY="76CFAB1105537A4EAC8496336EBD6443";

	/** SECRETKEY */
	public static String SECRETKEY="D38AE3F0B1184F49A41207966D909057";

	/**
	 * 认证方法
	 */
	@Override
	public CertifyUser certify(String id,String passwd,String address)
	{
		// 此处的id应该为access token
		String accessToken=id;
		// 签名方式 MD5(AppID+AccessToken+SecretKey)
		String sign=md5(APPID+accessToken+SECRETKEY);
		// 请求的参数
		StringBuilder param=new StringBuilder();
		param.append("AppID=");
		param.append(APPID);
		param.append("&AccessToken=");
		param.append(accessToken);
		param.append("&Sign=");
		param.append(sign.toLowerCase());
		// 发送请求
		String result=sendPost(CERTIFY_URL,param.toString());

		if(log.isDebugEnabled())
			log.debug("param="+param.toString()+",result="+result);
		try
		{
			JSONObject resJO=new JSONObject(result);
			int resultCode=resJO.getInt("ResultCode");
			// 检查认证是否成功
			if(resultCode!=RESULT_OK)
			{
				throw new DataAccessException(0,"mobo_certify_failed");
			}
			// 检查信息是否正确,返回签名 MD5(AppID+ResultCode+Content+SecretKey)
			String content=resJO.getString("Content");
			content=URLDecoder.decode(content,"UTF-8");
			String returnSign=md5(String.valueOf(APPID)+resultCode+content
				+SECRETKEY);
			returnSign=returnSign.toLowerCase();
			sign=resJO.getString("Sign");
			if(!returnSign.equals(sign))
			{
				if(log.isErrorEnabled())
					log.error("return sign error.appid=["+APPID
						+"],result code=["+resultCode+"],content=["+content
						+"],secretkey=["+SECRETKEY+"],returnSign=["
						+returnSign+"],sign=["+sign+"]");
				throw new DataAccessException(0,"return_sign_error");
			}
			// 对content进行base64解码
			Base64 base64=new Base64();
			content=new String(base64.decode(content),"UTF-8");
			if(log.isDebugEnabled())
				log.debug("decoded content=["+content+"]");
			JSONObject cntJO=new JSONObject(content);
			String uid=cntJO.getString("UID");
			// 判断uid是否存在
			if(uid==null||uid.isEmpty())
			{
				if(log.isErrorEnabled())
					log.error("uid error.content=["+content+"]");
				throw new DataAccessException(0,"mobo_certify_failed");
			}
			// 返回账号信息
			CertifyUser user=new CertifyUser();
			user.setAccount(ACCOUNT_PREFIX+uid);
			return user;

		}
		catch(JSONException e)
		{
			if(log.isErrorEnabled()) log.error("unknow exeception.",e);
		}
		catch(UnsupportedEncodingException e)
		{
			if(log.isErrorEnabled())
				log.error("decode content failed.result=["+result+"]",e);
		}

		return null;
	}

	/**
	 * MD5摘要算法
	 * 
	 * @param str
	 * @return
	 */
	private String md5(String str)
	{
		if(str==null) return "";
		try
		{
			MD5 md5=new MD5();
			byte[] data=str.getBytes("utf-8");
			byte[] digest=md5.encode(data);
			str=new String(CodecKit.byteHex(digest));
		}
		catch(UnsupportedEncodingException e)
		{
			if(log.isErrorEnabled())
				log.error("encoding error.str=["+str+"]",e);
		}

		return str;
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param goUrl 请求地址
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式
	 * @return 所代表远程资源的响应结果
	 */
	private String sendPost(String goUrl,String param)
	{
		BufferedWriter writer=null;
		BufferedReader reader=null;
		String result="";
		try
		{
			URL realUrl=new URL(goUrl);
			// 打开和URL之间的连接
			URLConnection conn=realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept","*/*");
			conn.setRequestProperty("connection","Keep-Alive");
			conn.setRequestProperty("user-agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			writer=new BufferedWriter(new OutputStreamWriter(
				conn.getOutputStream())); // 向服务器传送数据
			writer.write(param);// 传送的数据
			writer.flush();
			writer.close();
			// 定义BufferedReader输入流来读取URL的响应
			reader=new BufferedReader(new InputStreamReader(
				conn.getInputStream()));// 读取服务器响应信息
			String line;
			while((line=reader.readLine())!=null)
			{
				result+=line;
			}
		}
		catch(Exception e)
		{
			if(log.isErrorEnabled())
				log.error(
					"certify failed.url=["+goUrl+"],param=["+param+"]",e);
			throw new DataAccessException(0,"communication_failed");
		}
		// 使用finally块来关闭输出流、输入流
		finally
		{
			try
			{
				if(writer!=null)
				{
					writer.close();
				}
				if(reader!=null)
				{
					reader.close();
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return result;
	}

}
