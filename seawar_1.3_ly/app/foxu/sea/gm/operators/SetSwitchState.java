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
 * ������Ŀ���
 * 
 * @author lhj
 * 
 */
public class SetSwitchState extends GMOperator
{

	/** CODE_ON ���� CODE_CLOSE �ر� */
	public static int CODE_ON=2,CODE_CLOSE=1;
	/**1=ȫ�� ������,�һ���,���쳵��,������͸,�ձ�������֧��**/
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

	/** ���ÿ��ص�״̬ **/
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
