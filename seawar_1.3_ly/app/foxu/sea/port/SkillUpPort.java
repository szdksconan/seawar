package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.fight.FightScene;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.fight.Skill;
import foxu.sea.kit.JBackKit;

/** 技能提升端口1009 */
public class SkillUpPort extends AccessPort
{
	CreatObjectFactory objectFactory;
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
		// 技能等级限制
		int skillSid=data.readUnsignedShort();
		Skill skill=player.getSkillBySid(skillSid);
		if(skill!=null
			&&(skill.getLevel()>=PublicConst.MAX_SKILL_LEVEL||skill.getLevel()>=player
				.getLevel()))
		{
			throw new DataAccessException(0,"skill can not levelUp");
		}
		if(skill==null)
		{
			skill=(Skill)FightScene.abilityFactory.newSample(skillSid);
		}
		// 需要数量
		int needNum=skill.getNeedLevelPropNum();
		if(!(player.checkPropEnough(PublicConst.UP_SKILL_PROP_SID,needNum)))
		{
			JBackKit.sendResetBunld(player);
			throw new DataAccessException(0,"not enough prop");
		}
		player.getBundle().decrProp(PublicConst.UP_SKILL_PROP_SID,needNum);
		player.skillUpLevel(skillSid);
		JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.PLAYER_SKILL_UP);
		return null;
	}
	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}
	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

}
