package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 设置联盟等级和经验
 * @author comeback
 *
 */
public class SetAllianceLevel extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String allianceStr=params.get("alliance");
		String levelStr=params.get("level");
		String expStr=params.get("exp");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Alliance alliance=objectFactory.getAllianceMemCache().loadByName(allianceStr,true);
		if(alliance==null)
		{
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		}
		int level=Integer.parseInt(levelStr);
		if(level<0)level=0;
		int exp=Integer.parseInt(expStr);
		if(exp<0)exp=0;
		if(level>PublicConst.MAX_ALLIANCE_LEVEL)
			level=PublicConst.MAX_ALLIANCE_LEVEL;
		int index=level>=PublicConst.ALLIANCE_LEVEL_EXP.length?PublicConst.ALLIANCE_LEVEL_EXP.length-1:level;
		if(exp>PublicConst.ALLIANCE_LEVEL_EXP[index])
			exp=PublicConst.ALLIANCE_LEVEL_EXP[index];
		alliance.setAllianceLevel(level);
		alliance.setAllianceExp(exp);
		return GMConstant.ERR_SUCCESS;
	}

}
