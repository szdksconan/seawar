package foxu.sea.webgm.opertrator;

import java.util.Map;

import javapns.json.JSONArray;
import mustang.set.ObjectArray;
import foxu.dcaccess.CreatObjectFactory;
import foxu.fight.FightScene;
import foxu.sea.Player;
import foxu.sea.fight.Skill;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;


public class AdjustPlayerSkill extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		
		String playerName=params.get("player_name");
		String sid=params.get("sid");
		String value=params.get("level");
		String flag=params.get("flag");
		if(playerName==null||playerName.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		if(value==null||value.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		if(sid==null||sid.length()==0)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		CreatObjectFactory objectFactory=info.getObjectFactory();
		Player player=objectFactory.getPlayerByName(playerName,true);
		if(player==null)
			return GMConstant.ERR_PLAYER_NAME_NULL;
		Object object[]=player.getSkills().toArray();
		ObjectArray skills=new ObjectArray();
		if(Integer.parseInt(flag)==1)
		{
			if(object.length!=0)
			{
				for(int i=0;i<object.length;i++)
				{
					Skill skill=(Skill)object[i];
					if(skill.getSid()==Integer.parseInt(sid))
					{
							skill.setLevel(Integer.parseInt(value));
					}
				}
			}
		}
		if(Integer.parseInt(flag)==2)
		{
			if(player.getSkills()==null)
			{
				Skill skill=(Skill)FightScene.abilityFactory.newSample(Integer.parseInt(sid));
				skill.setLevel(Integer.parseInt(value));
				skills.add(skill);
			}
			else
			{
				for(int i=0;i<object.length;i++)
				{
					Skill skille=(Skill)object[i];
					skills.add(skille);
				}
				Skill skill=(Skill)FightScene.abilityFactory.newSample(Integer.parseInt(sid));
				skill.setLevel(Integer.parseInt(value));
				skills.add(skill);
			}
			player.setSkills(skills);
		}
		return GMConstant.ERR_SUCCESS;
	}

}
