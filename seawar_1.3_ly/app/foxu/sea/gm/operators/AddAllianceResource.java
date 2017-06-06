package foxu.sea.gm.operators;

import java.util.Map;

import mustang.text.TextKit;
import javapns.json.JSONArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.alliance.Alliance;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;

/***
 * 
 * 添加联盟物资
 * 
 * @author lhj
 * 
 */
public class AddAllianceResource extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String allianceName=params.get("alliance_name");
		// 操作数量
		String material=params.get("material");
		String sciencePoint=params.get("sciencePoint");
		CreatObjectFactory factory=info.getObjectFactory();
		Alliance alliance=factory.getAllianceMemCache().loadByName(
			allianceName,true);
		if(alliance==null) return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		int materials=TextKit.parseInt(material);
		int sciencePoints=TextKit.parseInt(sciencePoint);
		if(materials>0) alliance.addMaterial(materials);
		if(materials<0) alliance.reduceMaterial(-materials);
		if(sciencePoints>0) alliance.addSciencepoint(sciencePoints);
		if(sciencePoints<0) alliance.reduceSciencepoint(-sciencePoints);
		JBackKit.sendAllianceWarResource(factory,alliance);
		return GMConstant.ERR_SUCCESS;
	}

}
