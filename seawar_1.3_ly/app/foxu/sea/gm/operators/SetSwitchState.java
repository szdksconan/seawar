package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;

/**
 * 邀请码的开关
 * 
 * @author lhj
 * 
 */
public class SetSwitchState extends GMOperator
{

	/** CODE_ON 开启 CODE_CLOSE 关闭 */
	public static int CODE_ON=2,CODE_CLOSE=1;
	/**1=全部 邀请码,兑换码,制造车间,军需乐透,日本第三方支付**/
	public static int ALL=1,INVATE=2,CODE=3,WORKSHIP=4,AWARD=5,CHARGE=6,MODIFY=7,RECRUIT=8;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String type=params.get("type");
		String identify=params.get("identify");
		switchState(TextKit.parseInt(identify),
			TextKit.parseInt(type)==CODE_CLOSE?false:true);
		CreatObjectFactory objectFactory=info.getObjectFactory();
		JBackKit.sendSwitchState(objectFactory.getDsmanager()
			.getSessionMap());
		return GMConstant.ERR_SUCCESS;
	}

	/** 设置开关的状态 **/
	public void switchState(int identify,boolean state)
	{
		if(identify==ALL)
		{
			for(int i=0;i<PublicConst.SWITCH_STATE.length;i++)
			{
				PublicConst.SWITCH_STATE[i]=state;
			}
		}
		else
		{
			if(identify==INVATE)
				PublicConst.SWITCH_STATE[0]=state;
			else if(identify==CODE)
				PublicConst.SWITCH_STATE[1]=state;
			else if(identify==WORKSHIP)
				PublicConst.SWITCH_STATE[2]=state;
			else if(identify==AWARD)
				PublicConst.SWITCH_STATE[3]=state;
			else if(identify==CHARGE)
				PublicConst.SWITCH_STATE[4]=state;
			else if(identify==MODIFY)
				PublicConst.SWITCH_STATE[5]=state;
			else 
				PublicConst.SWITCH_STATE[6]=state;
		}
	}
}
