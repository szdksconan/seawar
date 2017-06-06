package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.back.BackKit;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.text.TextValidity;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.BattleGroundSave;
import foxu.dcaccess.mem.AllianceMemCache;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.alliance.alliancebattle.AllianceBattleManager;
import foxu.sea.alliance.alliancebattle.BattleIsland;
import foxu.sea.alliance.alliancebattle.Stage;
import foxu.sea.alliance.alliancefight.AllianceFightManager;
import foxu.sea.alliance.alliancefight.BattleGround;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.port.AlliancePort;

/***
 * 修改联盟名称
 * @author lhj
 *
 */
public class ModifyAllianceName extends GMOperator
{
	Logger log=LogFactory.getLogger(ModifyAllianceName.class);
	TextValidity tv;
	/** 数据获取类 **/
	CreatObjectFactory objectFactory;
	
	/**新联盟战管理器**/
	AllianceBattleManager battleFightManager;
	public static final int MIN_LEN=1,MAX_LEN=12;
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String allianceName=params.get("alliancename");
		String newAName=params.get("newname");
		objectFactory=info.getObjectFactory();
		int  result=checkAlliance(newAName);
		if(result!=GMConstant.ERR_SUCCESS)
			return result;
		Alliance alliances=objectFactory.getAllianceMemCache()
			.loadByName(newAName,false);
		if(alliances!=null)
			return GMConstant.ERR_ALLIANCE_NAME_USED;
		Alliance alliance=objectFactory.getAllianceMemCache()
						.loadByName(allianceName,false);
		if(alliance==null)
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
//		Player player=objectFactory.getPlayerById(alliance.getMasterPlayerId());
//		result=allianceValide(player,newAName);
//		if(result!=GMConstant.ERR_SUCCESS)
//			return result;
		alliance.setName(newAName);
		boolean flag=objectFactory.getAllianceMemCache().getDbaccess()
			.save(alliance);
		if(!flag)
		{
			alliance.setName(allianceName);
			return GMConstant.ERR_ALLIANCE_LENGTH_WRONG;
		}
		log.error("Original Alliancename:"+allianceName+"---------modifyAllianceName:"+newAName);
		/** 保存数据 **/
		AllianceMemCache cache=objectFactory.getAllianceMemCache();
		cache.save(alliance.getId()+"",alliance);

		/** 修改排行榜的数据 **/
		AlliancePort alliancePort=(AlliancePort)BackKit.getContext()
			.get("alliancePort");
		alliancePort.setAllianceName(allianceName,newAName);

		String title=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_list");
		String content=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"alliance_modify_info");
		content=TextKit.replace(content,"%",alliance.getName());
		sendAllianceMail(alliance,title,content,null);
		JBackKit.sendAllianceName(alliance,objectFactory);
		Object[] grounds=AllianceFightManager.amanager.getBGrounds();
		for(int i=0;i<grounds.length;i++)
		{
			BattleGround bground=(BattleGround)((BattleGroundSave)grounds[i])
				.getData();
			if(bground.getId()==alliance.getId())
			{
				JBackKit.sendGround(objectFactory,bground,AllianceFightManager.amanager);
			}
		}
		flushBattleFightAllianceName(alliance);
		return GMConstant.ERR_SUCCESS;
	}

	/**检测联盟名称是否可用**/
	public int checkAlliance(String name)
	{
		if(name.getBytes().length==name.length())
		{
			if(name.length()<MIN_LEN||name.length()>MAX_LEN)
				return GMConstant.ERR_ALLIANCE_LENGTH_WRONG;
		}
		else
		{
			if(name.length()<(MIN_LEN/2)||name.length()>(MAX_LEN/2))
				return GMConstant.ERR_ALLIANCE_LENGTH_WRONG;
		}
		if(tv.valid(name,true)!=null||name.indexOf(",")>=0)
			return GMConstant.ERR_ALLIANCE_IS_UNVALID;
		
		if(SeaBackKit.isContainValue(PublicConst.SHIELD_WORD,name))
			return GMConstant.ERR_ALLIANCE_IS_UNVALID;
		if(SeaBackKit.checkBlank(name))
			return GMConstant.ERR_ALLIANCE_IS_UNVALID;
		
		return GMConstant.ERR_SUCCESS;
	}
	
	/** 给指定的alliance玩家发送邮件 **/
	public void sendAllianceMail(Alliance alliance,String title,
		String content,Player beplayer)
	{
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		IntList list=alliance.getPlayerList();
		for(int i=0;i<list.size();i++)
		{
			Player player=objectFactory.getPlayerById(list.get(i));
			if(player==null) continue;
			if(beplayer!=null&&beplayer.getId()==player.getId()) continue;
			Message message=objectFactory.createMessage(0,player.getId(),
				content,sendName,player.getName(),0,title,true);
			// 刷新前台
			JBackKit.sendRevicePlayerMessage(player,message,
				message.getRecive_state(),objectFactory);
		}
	}
	/**
	 * @param tv 要设置的 tv
	 */
	public void setTv(TextValidity tv)
	{
		this.tv=tv;
	}
	
//	/**联盟检测屏蔽字**/
//	public Integer allianceValide(Player player,String newName)
//	{
//		// 验证联盟屏蔽字
//		String validWord=InterTransltor.getInstance().getTransByKey(
//			player.getLocale(),"alliance shield word");
//		if(SeaBackKit.isContainValue(validWord,newName.trim()))
//				return GMConstant.ERR_ALLIANCE_IS_UNVALID;
//		return GMConstant.ERR_SUCCESS;
//	}

	
	public void setBattleFightManager(AllianceBattleManager battleFightManager)
	{
		this.battleFightManager=battleFightManager;
	}

	/**刷新新联盟战的联盟名称**/
	public  void flushBattleFightAllianceName(Alliance alliance)
	{
		if(alliance.getBetBattleIsland()==0) return;
		BattleIsland island=(BattleIsland)objectFactory
			.getBattleIslandMemCache()
			.load(alliance.getBetBattleIsland()+"");
		if(island==null) return;
		int stage=battleFightManager.getAllianceFight().getAllianceStage()
			.getStage();
		if(stage!=Stage.STAGE_FOUR)
		{
			JBackKit.sendAllianceBetInfo(null,objectFactory);
		}
		else
		{
			JBackKit.sendAllianceBetInfo(island,objectFactory);
		}
	}
}
