package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Role;
import foxu.sea.Ship;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancefight.AllianceFight;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;

/**
 * 添加联盟船只
 * 
 * @author lhj
 * 
 */
public class AddAllianceShip extends GMOperator
{

	// ADDTYPE=1 增加 REMOVETYPE=2 移除
	public static int ADDTYPE=1,REMOVETYPE=2;
	/** 船舰类型和等级对应的sid */
	public final static int SHIPS_SIDS[][]={
		{10001,10002,10003,10004,10005,10006,10007,10008},
		{10011,10012,10013,10014,10015,10016,10017,10018},
		{10021,10022,10023,10024,10025,10026,10027,10028},
		{10031,10032,10033,10034,10035,10036,10037,10038}};

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		// 判断是增加还是减少
		String type=params.get("type");
		// 联盟名称
		String name=params.get("allianceName");
		// 联盟不存在
		if(name==null||name.length()==0)
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		CreatObjectFactory factory=info.getObjectFactory();
		Alliance alliance=factory.getAllianceMemCache().loadByName(name,
			false);
		if(alliance==null) return GMConstant.ERR_ALLIANCE_NOT_EXISTS;

		String shipType=params.get("ship_type");
		if(shipType==null||shipType.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		String shipLevel=params.get("ship_level");
		if(shipLevel==null||shipLevel.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		String countStr=params.get("count");
		if(countStr==null||countStr.length()==0)
			return GMConstant.ERR_PARAMATER_ERROR;
		int sid=SHIPS_SIDS[Integer.parseInt(shipType)-1][Integer
			.parseInt(shipLevel)-1];
		Ship ship=(Ship)Role.factory.newSample(sid);
		if(ship==null) return GMConstant.ERR_SHIP_IS_NULL;
		int count=Integer.parseInt(countStr);
		AllianceFight allianceFight=(AllianceFight)factory
			.getAllianceFightMemCache().load(alliance.getId()+"");
		if(allianceFight==null) return GMConstant.ERR_ALLIANCEFIGHT_IS_NULL;
		if(Integer.parseInt(type)==ADDTYPE)
			addFleet(sid,count,allianceFight);
		else
			reMoveFleet(sid,count,allianceFight);
		return GMConstant.ERR_SUCCESS;
	}

	/** 添加联盟船只 **/
	public void addFleet(int sid,int count,AllianceFight allianceFight)
	{
		IntList list=new IntList();
		list.add(sid);
		list.add(count);
		allianceFight.addShip(list);
	}

	/** 减去联盟船只 **/
	public void reMoveFleet(int sid,int count,AllianceFight allianceFight)
	{
		allianceFight.decrShipBySid(sid,count);
	}
}
