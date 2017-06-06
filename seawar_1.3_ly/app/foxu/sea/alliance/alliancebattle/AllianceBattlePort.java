package foxu.sea.alliance.alliancebattle;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import foxu.ds.PlayerKit;
import foxu.fight.FightScene;
import foxu.sea.Player;
import foxu.sea.fight.AllianceSkill;

/***
 * 
 * 联盟争夺战
 * 
 * @author lhj
 * 
 */
public class AllianceBattlePort extends AccessPort
{

	/** 联盟争夺战管理器 **/
	AllianceBattleManager manager;
	/**
	 * BATTLE_GIVE_VALUE=1 捐物资ALLIANCE_GIVE_SCIENCE=2 使用科技点捐献技能
	 * ALLIANCE_SIGN_UP=3 联盟参与报名 MESS获取邮件数量 ALLIANCE_BATTLE_BET=4 竞标
	 *  联盟商店消费 ALLIANCE_BATTTLE_REPORT=6 战报
	 * GET_ALLIANCE_SKILL_INFO=7 ALLIANCE_STORE_CONSUME=8
	 * GET_ALLIANCEBATTLE_INFO=9 获取简要信息
	 */
	public static final int BATTLE_MATERIAL=1,GET_ALLIANCE_SKILL_INFO=2,
					ALLIANCE_SIGN_UP=3,ALLIANCE_BATTLE_BET=4,
					ALLIANCE_BATTTLE_REPORT=6,ADD_SKILL_INTO_SCIENCE=7,
					ALLIANCE_STORE_CONSUME=8,GET_ALLIANCEBATTLE_INFO=9;

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		if(connect.getSource()==null)
		{
			connect.close();
			return null;
		}
		Session s=(Session)(connect.getSource());
		Player player=PlayerKit.getInstance().getPlayer(s);
		if(player==null)
		{
			connect.close();
			return null;
		}
		int type=data.readUnsignedByte();
		// 玩家捐赠物资
		if(type==BATTLE_MATERIAL)
		{
			String str=manager.donatedMaterial(player,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		// 注入联盟科技点 技能升级
		else if(type==ADD_SKILL_INTO_SCIENCE)
		{
			int skillSid=data.readUnsignedShort();
			int sciencePoint=data.readInt();
			AllianceSkill skill=(AllianceSkill)FightScene.abilityFactory
				.newSample(skillSid);
			// 技能不存在
			if(skill==null)
				throw new DataAccessException(0,"skill is null");
			String str=manager.addAllianceSkill(player,sciencePoint,
				skillSid,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** 联盟报名 **/
		else if(type==ALLIANCE_SIGN_UP)
		{
			String str=manager.joinAllianceFight(player,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** 联盟商店 **/
		else if(type==ALLIANCE_STORE_CONSUME)
		{
			String str=manager.allianceStoreConsume(player,data);
			if(str!=null) throw new DataAccessException(0,str);

		}
		/** 联盟竞标 **/
		else if(type==ALLIANCE_BATTLE_BET)
		{
			String str=manager.betBattleIsland(player,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** 获取战报内容 **/
		else if(type==ALLIANCE_BATTTLE_REPORT)
		{
			String str=manager.getAllianceReport(data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** 获取技能信息 **/
		else if(type==GET_ALLIANCE_SKILL_INFO)
		{
			String str=manager.getAllianceSkillInfo(data,player);
			if(str!=null) throw new DataAccessException(0,str);
		}
		else if(type==GET_ALLIANCEBATTLE_INFO)
		{
			String str=manager.getSimpleFightInfo(data,player);
			if(str!=null) throw new DataAccessException(0,str);
		}
		return data;
	}

	public void setManager(AllianceBattleManager manager)
	{
		this.manager=manager;
	}

}
