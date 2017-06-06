package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.PublicConst;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;

/**
 * ���͵�����֧����ʽ��״̬
 * 
 * @author lhj
 * 
 */
public class SetRechangeStyle extends GMOperator
{

	/** RECHANGE_ON ���� RECHANGE_CLOSE �ر� */
	public static int RECHANGE_ON=1,RECHANGE_CLOSE=2;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{

		String type=params.get("type");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		if(Integer.parseInt(type)==RECHANGE_CLOSE)
		{
			PublicConst.RECHANGE_STATLE=false;
			JBackKit.sendRechangeState(objectFactory.getDsmanager()
				.getSessionMap());
		}
		else
		{
			PublicConst.RECHANGE_STATLE=true;
			JBackKit.sendRechangeState(objectFactory.getDsmanager()
				.getSessionMap());
		}
		return GMConstant.ERR_SUCCESS;
	}

}
