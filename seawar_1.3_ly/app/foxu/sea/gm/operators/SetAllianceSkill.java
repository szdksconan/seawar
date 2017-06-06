package foxu.sea.gm.operators;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.FightScene;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.fight.AllianceSkill;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public class SetAllianceSkill extends GMOperator
{

	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String allianceStr=params.get("alliance");
		String sidStr=params.get("sid");
		String levelStr=params.get("level");
		String expStr=params.get("exp");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Alliance alliance=objectFactory.getAllianceMemCache().loadByName(allianceStr,true);
		if(alliance==null)
		{
			return GMConstant.ERR_ALLIANCE_NOT_EXISTS;
		}
		Object[] array=alliance.getAllianSkills().getArray();
		int sid=Integer.parseInt(sidStr);
		int level=Integer.parseInt(levelStr);
		if(level<0)level=0;
		if(level>alliance.getAllianceLevel())
			level=alliance.getAllianceLevel();
		int exp=Integer.parseInt(expStr);
		if(exp<0)exp=0;
		for(int i=0;i<array.length;i++)
		{
			if(!(array[i] instanceof AllianceSkill))
				continue;
			AllianceSkill skill=(AllianceSkill)array[i];
			if(skill.getSid()!=sid)
				continue;
			skill.setLevel(level);
			skill.setNowExp(exp);
			return GMConstant.ERR_SUCCESS;
		}
		// 没有技能的时候添加一个
		Object sample=FightScene.abilityFactory
						.newSample(sid);
		if(sample instanceof AllianceSkill)
		{
			if(!checkLevel(sid,alliance.getAllianceLevel()))
				return GMConstant.ERR_ALLIANCE_LEVEL_ERROR;
			AllianceSkill skill=(AllianceSkill)sample;
			skill.setNowExp(exp);
			skill.setLevel(level);
			alliance.getAllianSkills().add(skill);
			return GMConstant.ERR_SUCCESS;
		}
		return GMConstant.ERR_ALLIANCE_SKILL_NOT_EXISTS;
	}

	private boolean checkLevel(int skillSid,int allianceLevel)
	{
		for(int i=0;i<PublicConst.ALLIANCE_LEVEL_OPEN_SKILL.length;i++)
		{
			String skillSids[]=TextKit.split(
				PublicConst.ALLIANCE_LEVEL_OPEN_SKILL[i],":");
			if(allianceLevel<Integer.parseInt(skillSids[0])) break;
			for(int j=1;j<skillSids.length;j++)
			{
				if(skillSid==Integer.parseInt(skillSids[j]))
				{
					return true;
				}
			}
		}
		return false;
	}
}
