package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import mustang.io.ByteBuffer;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


/**
 * ��ѯ������
 * @author lhj
 *
 */
public class SerchCode extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String platform=params.get("platform_code");//ƽ̨
		String codetimesid=params.get("code_timesid");//����
		String username=params.get("code_createname");//�һ���������
		String codename=params.get("code_name");//�һ���
		String playername=params.get("player_name");//�һ���
		String  pagenew=params.get("pageNow");//��ǰҳ��
		int timesid=0;
		if(codetimesid!=null && codetimesid!="") timesid=Integer.parseInt(codetimesid) ;
		int pageNow=0;
		if(pagenew!=null && pagenew!="") pageNow=Integer.parseInt(pagenew);
		ByteBuffer data=codeSendcenter(pageNow,platform,timesid,username,codename,playername);
		String array=data.readUTF();
		try
		{
				JSONArray jsonarr=new JSONArray(array);
				for(int i=0;i<jsonarr.length();i++)
				{
					jsonArray.put(jsonarr.get(i));
				}
		}
		catch(JSONException e)
		{
			return GMConstant.ERR_UNKNOWN;
		}
		return GMConstant.ERR_SUCCESS;
	}
	/**
	 * װ������  
	 * @param pageNow  ��ǰҳ
	 * @param platform ƽ̨
	 * @param timesid ����
	 * @param username �û���
	 * @param codename �һ���
	 * @param playername �������
	 * @return
	 */
	private static  ByteBuffer codeSendcenter(int pageNow,String platform,int timesid,String username,String codename,String playername)
	{
		ByteBuffer data=new ByteBuffer();
		data.writeByte(8);
		data.writeInt(pageNow);//��ǰҳ
		data.writeInt(timesid);//����
		data.writeUTF(username);//�һ���������
		data.writeUTF(codename);//�һ���
		data.writeUTF(playername);//�һ���
		//����gamecenter
		data=CodeInfomation.sendHttpData(data,platform);
		return data;
	}


}

