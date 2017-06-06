package foxu.sea.charge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javapns.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.util.TimeKit;
import foxu.sea.kit.SeaBackKit;

public class VerifyHeroChargeInfoManager
{

	public static final int TIME_OUT=40000;

	public static String appVerifyUrl;

	Logger log=LogFactory.getLogger(getClass());
	/**
	 * 苹果商品验证单例
	 */
	private static VerifyHeroChargeInfoManager INSTANCE=new VerifyHeroChargeInfoManager();

	/** 获得苹果商品实例 */
	public static VerifyHeroChargeInfoManager getInstance()
	{
		if(INSTANCE==null)
		{
			INSTANCE=new VerifyHeroChargeInfoManager();
		}
		return INSTANCE;
	}

	 private void trustAllHttpsCertificates() throws Exception
	 {
	 javax.net.ssl.TrustManager[] trustAllCerts=new
	 javax.net.ssl.TrustManager[1];
	 javax.net.ssl.TrustManager tm=new Mitm();
	 trustAllCerts[0]=tm;
	 javax.net.ssl.SSLContext sc=javax.net.ssl.SSLContext
	 .getInstance("SSL");
	 sc.init(null,trustAllCerts,null);
	 javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
	 .getSocketFactory());
	 }
	
	 HostnameVerifier hv=new HostnameVerifier()
	 {
	
		 public boolean verify(String urlHostName,SSLSession session)
		 {
		 //System.out.println("Warning: URL Host: "+urlHostName+" vs. "
		 //+session.getPeerHost());
		 return true;
		 }
	 };

	public String verifyHeroChargeInfo(String verifyInfo,int roleUid,
		String roleName)
	{
		StringBuffer sbf=new StringBuffer();
		BufferedWriter writer=null;
		BufferedReader reader=null;
		HttpURLConnection uc=null;
		try
		{
			URL url=new URL(appVerifyUrl);
			// // 信任证书
			trustAllHttpsCertificates();
			// HttpsURLConnection.setDefaultHostnameVerifier(hv);
			uc=(HttpURLConnection)url.openConnection();
			uc.setConnectTimeout(TIME_OUT);
			uc.setReadTimeout(TIME_OUT);
			uc.setDoOutput(true);
			writer=new BufferedWriter(new OutputStreamWriter(uc
				.getOutputStream())); // 向服务器传送数据
			JSONObject jsonObject=new JSONObject();
			jsonObject.putOpt("receipt-data",verifyInfo);
			String clientStr=jsonObject.toString();
			writer.write(clientStr);// 传送的数据
			writer.flush();
			writer.close();
			reader=new BufferedReader(new InputStreamReader(uc
				.getInputStream()));// 读取服务器响应信息
			String line;
			while((line=reader.readLine())!=null)
			{
				sbf.append(line);
			}
			reader.close();
			uc.disconnect();
			String appleStr=java.net.URLDecoder.decode(sbf.toString(),
				"utf-8");// 获取返回的json数据

			// 转换为string
			jsonObject=new JSONObject(appleStr);// 将json数据转换为JSONObject
			String str="false";
			if(jsonObject.has("status"))
			{
				str=jsonObject.get("status").toString();
			}
			// 验证客服端上传商品是否与苹果返回商品相同
			if(!jsonObject.has("receipt"))
			{
				log.warn(" apple store vertify err no receipt, "+", roleUid="+roleUid
					+", roleName="+roleName+", verifyInfo="+verifyInfo);
				return "fail";
			}
			JSONObject receipt=jsonObject.getJSONObject("receipt");
			// 记录日志 记录到错误日志里面方便查看
			log.warn(" apple store vertify check ok, "+",success="+str
				+", roleUid="+roleUid+", roleName="+roleName+", appStoreId="
				+receipt.getString("product_id")+", verifyInfo="+verifyInfo
				+", createTime="
				+SeaBackKit.formatDataTime(TimeKit.getSecondTime())
				+",appleStr="+appleStr);
			if(!receipt.has("product_id"))
			{
				return "fail";
			}
			// transaction_id
			// System.out.println("appStoreId=========="+appStoreId);
			// if(!receipt.getString("product_id").equals(appStoreId))
			// {
			// return "fail";
			// }
			// 验证成功返回true
			if(jsonObject.has("status")
				&&"0".equals(jsonObject.get("status").toString()))
			{
				return receipt.getString("product_id")+":"
					+receipt.getString("transaction_id")+":"+receipt.getString("original_purchase_date_ms");
			}
			return "fail";
		}
		catch(Exception e)
		{
			log.warn(" apple store vertify err, "+", roleUid="+roleUid
				+", roleName="+roleName+", verifyInfo="+verifyInfo,e);
			return "fail";
		}
		finally
		{
			// 关闭流
			if(writer!=null)
			{
				try
				{
					writer.close();
					writer=null;
				}
				catch(Exception e)
				{
				}
			}
			if(reader!=null)
			{
				try
				{
					reader.close();
					reader=null;
				}
				catch(Exception e)
				{
				}
			}
		}
	}

	// public static void main(String[] args)
	// {
	// String
	// appstr="ewoJInNpZ25hdHVyZSIgPSAiQXBkeEpkdE53UFUyckE1L2NuM2tJTzFPVGsyNWZlREthMGFhZ3l5UnZlV2xjRmxnbHY2UkY2em5raUJTM3VtOVVjN3BWb2IrUHFaUjJUOHd5VnJITnBsb2YzRFgzSXFET2xXcSs5MGE3WWwrcXJSN0E3ald3dml3NzA4UFMrNjdQeUhSbmhPL0c3YlZxZ1JwRXI2RXVGeWJpVTFGWEFpWEpjNmxzMVlBc3NReEFBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV5TFRBM0xURXlJREExT2pVME9qTTFJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluQjFjbU5vWVhObExXUmhkR1V0YlhNaUlEMGdJakV6TkRJd09UYzJOelU0T0RJaU93b0pJbTl5YVdkcGJtRnNMWFJ5WVc1ellXTjBhVzl1TFdsa0lpQTlJQ0l4TnpBd01EQXdNamswTkRrME1qQWlPd29KSW1KMmNuTWlJRDBnSWpFdU5DSTdDZ2tpWVhCd0xXbDBaVzB0YVdRaUlEMGdJalExTURVME1qSXpNeUk3Q2draWRISmhibk5oWTNScGIyNHRhV1FpSUQwZ0lqRTNNREF3TURBeU9UUTBPVFF5TUNJN0Nna2ljWFZoYm5ScGRIa2lJRDBnSWpFaU93b0pJbTl5YVdkcGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVdGJYTWlJRDBnSWpFek5ESXdPVGMyTnpVNE9ESWlPd29KSW1sMFpXMHRhV1FpSUQwZ0lqVXpOREU0TlRBME1pSTdDZ2tpZG1WeWMybHZiaTFsZUhSbGNtNWhiQzFwWkdWdWRHbG1hV1Z5SWlBOUlDSTVNRFV4TWpNMklqc0tDU0p3Y205a2RXTjBMV2xrSWlBOUlDSmpiMjB1ZW1Wd2RHOXNZV0l1WTNSeVltOXVkWE11YzNWd1pYSndiM2RsY2pFaU93b0pJbkIxY21Ob1lYTmxMV1JoZEdVaUlEMGdJakl3TVRJdE1EY3RNVElnTVRJNk5UUTZNelVnUlhSakwwZE5WQ0k3Q2draWIzSnBaMmx1WVd3dGNIVnlZMmhoYzJVdFpHRjBaU0lnUFNBaU1qQXhNaTB3TnkweE1pQXhNam8xTkRvek5TQkZkR012UjAxVUlqc0tDU0ppYVdRaUlEMGdJbU52YlM1NlpYQjBiMnhoWWk1amRISmxlSEJsY21sdFpXNTBjeUk3Q2draWNIVnlZMmhoYzJVdFpHRjBaUzF3YzNRaUlEMGdJakl3TVRJdE1EY3RNVElnTURVNk5UUTZNelVnUVcxbGNtbGpZUzlNYjNOZlFXNW5aV3hsY3lJN0NuMD0iOwoJInBvZCIgPSAiMTciOwoJInNpZ25pbmctc3RhdHVzIiA9ICIwIjsKfQ";
	// JSONObject jsonObject=null;
	// try
	// {
	// jsonObject=new JSONObject(appstr);
	// }
	// catch(JSONException e)
	// {
	// // TODO 自动生成 catch 块
	// e.printStackTrace();
	// }
	// System.out.println("============"+jsonObject.toString());
	// }

}
