package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import foxu.dcaccess.PasswordDBAccess;
import foxu.sea.PasswordRecord;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;


public class PwdRecordInfo extends GMOperator
{

	private PasswordDBAccess pwdDBAccess;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String deviceId=params.get("deviceId");
		String stime=params.get("stime");
		String etime=params.get("etime");
		if(deviceId==null||"".equals(deviceId)) return GMConstant.ERR_PARAMATER_ERROR;
		int sTime=SeaBackKit.parseFormatTime(stime);
		int eTime=SeaBackKit.parseFormatTime(etime);
		String sql="select * from pwdrecord where deviceId='"+deviceId+"' and createTime>="+sTime+" and createTime<="+eTime;
		PasswordRecord[] records=pwdDBAccess.loadBySql(sql);
		createRecord(records,jsonArray);
		if(jsonArray==null||jsonArray.length()<1) return GMConstant.ERR_PWD_RECORD_NOT_EXISTS;
		return GMConstant.ERR_SUCCESS;
//	
	}
	private void createRecord(PasswordRecord[] records,JSONArray jsons)
	{
		try
		{
			if(records==null) return ;
			for(int i=0;i<records.length;i++){
				JSONObject jo=new JSONObject();
				jo.put(GMConstant.ACCOUNT,records[i].getUserAccount());
				jo.put(GMConstant.TIME,SeaBackKit.formatDataTime(records[i].getCreatTime()));
				jsons.put(jo);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setPwdDBAccess(PasswordDBAccess pwdDBAccess)
	{
		this.pwdDBAccess=pwdDBAccess;
	}

}
