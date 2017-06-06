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
 * ��������ս
 * 
 * @author lhj
 * 
 */
public class AllianceBattlePort extends AccessPort
{

	/** ��������ս������ **/
	AllianceBattleManager manager;
	/**
	 * BATTLE_GIVE_VALUE=1 ������ALLIANCE_GIVE_SCIENCE=2 ʹ�ÿƼ�����׼���
	 * ALLIANCE_SIGN_UP=3 ���˲��뱨�� MESS��ȡ�ʼ����� ALLIANCE_BATTLE_BET=4 ����
	 *  �����̵����� ALLIANCE_BATTTLE_REPORT=6 ս��
	 * GET_ALLIANCE_SKILL_INFO=7 ALLIANCE_STORE_CONSUME=8
	 * GET_ALLIANCEBATTLE_INFO=9 ��ȡ��Ҫ��Ϣ
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
		// ��Ҿ�������
		if(type==BATTLE_MATERIAL)
		{
			String str=manager.donatedMaterial(player,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		// ע�����˿Ƽ��� ��������
		else if(type==ADD_SKILL_INTO_SCIENCE)
		{
			int skillSid=data.readUnsignedShort();
			int sciencePoint=data.readInt();
			AllianceSkill skill=(AllianceSkill)FightScene.abilityFactory
				.newSample(skillSid);
			// ���ܲ�����
			if(skill==null)
				throw new DataAccessException(0,"skill is null");
			String str=manager.addAllianceSkill(player,sciencePoint,
				skillSid,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** ���˱��� **/
		else if(type==ALLIANCE_SIGN_UP)
		{
			String str=manager.joinAllianceFight(player,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** �����̵� **/
		else if(type==ALLIANCE_STORE_CONSUME)
		{
			String str=manager.allianceStoreConsume(player,data);
			if(str!=null) throw new DataAccessException(0,str);

		}
		/** ���˾��� **/
		else if(type==ALLIANCE_BATTLE_BET)
		{
			String str=manager.betBattleIsland(player,data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** ��ȡս������ **/
		else if(type==ALLIANCE_BATTTLE_REPORT)
		{
			String str=manager.getAllianceReport(data);
			if(str!=null) throw new DataAccessException(0,str);
		}
		/** ��ȡ������Ϣ **/
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
