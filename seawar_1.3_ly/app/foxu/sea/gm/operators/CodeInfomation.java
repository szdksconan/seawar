package foxu.sea.gm.operators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.io.ByteBuffer;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;


/**
 * ת�����ɶһ�������
 * @author lhj
 *
 */
public class CodeInfomation extends GMOperator
{

	public static final int CODE_SEND_NUM=6;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String times=params.get("times");//һ�������ȡ�һ���Ĵ���
		String code_content=params.get("code_content");//�һ��������
		String platform=params.get("platform");//ƽ̨
		String  code_num=params.get("code_num");//�һ��������
		String code_goal=params.get("code_goal");//�һ����Ŀ��
		String id=params.get("timesid");//�һ����Ƿ�׷��
		int timesid=0;
		if(platform==null || platform=="") 
			return GMConstant.ERR_PLATFORM_NULL;
		if(code_goal==null || code_goal=="") 
			return GMConstant.ERR_CODE_GOAL_NULL;
		if(code_num==null || code_num=="") 
			return GMConstant.ERR_CODE_NUM_NULL;
		if(!id.equals(null) && !id.equals(""))
		{
			timesid=Integer.parseInt(id);
			if(times==null || times=="")
				times="0";
		}
		else
		{
			if(times==null || times=="")
				return GMConstant.ERR_CODE_TIMES_NULL;
			if(code_content==null || code_content=="") 
				return GMConstant.ERR_CODE_CONTENT_NULL;
		}
		ByteBuffer	data=codeSendcenter(Integer.parseInt(times),user,code_content,platform,code_num,code_goal,timesid);
		int s=data.readInt();
		if(s!=0) return s;
		return GMConstant.ERR_SUCCESS;
	}

	/**
	 * ��װ����
	 * @param times  ��ȡ�Ĵ���
	 * @param username �һ���������
	 * @param code_content 
	 * @param platform ƽ̨
	 * @param code_num ����
	 * @param code_goal Ŀ��
	 * @return
	 */
	public ByteBuffer codeSendcenter(int times,String username,String code_content,String platform,String code_num,String code_goal,int timesid)
	{
		String httpaddress=platform.split(",")[0];
		ByteBuffer data=new ByteBuffer();
		data.writeByte(CODE_SEND_NUM);
		data.writeUTF(username);
		data.writeUTF(code_content);
		data.writeUTF(platform.split(",")[1]);
		data.writeUTF(code_num);
		data.writeUTF(code_goal);
		data.writeInt(times);
		data.writeInt(timesid);
		data=sendHttpData(data,httpaddress);
		return data;
	}
	/**
	 * ����center     ��ȥȡ����
	 * @param data
	 * @param httpaddress  ���ʵĵ�ַ
	 * @return
	 */
	public static ByteBuffer sendHttpData(ByteBuffer data,String httpaddress)
	{	
		if(httpaddress==null) httpaddress="http://"+GameDBCCAccess.GAME_CENTER_IP+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/";
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// ����port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send(httpaddress,"POST",map,null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}
}