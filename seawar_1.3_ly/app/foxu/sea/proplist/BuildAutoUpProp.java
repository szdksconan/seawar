package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.award.Award;
import foxu.sea.builds.AutoUpBuildManager;
import foxu.sea.kit.JBackKit;

/**
 * 建筑自动升级道具
 * 
 * @author Alan
 * 
 */
public class BuildAutoUpProp extends AwardProp
{

	/** ByteBuffer为兼容新的物品使用返回信息 */
	public void use(Player player,CreatObjectFactory objectFactory,
		AutoUpBuildManager autoUpBuilding,ByteBuffer data)
	{
		//当前服务是否可用，不可用则使用道具后开始自动建造
		boolean startAuto=false;
		if(player.checkService(PublicConst.AUTO_BUILD_BUFF,
			TimeKit.getSecondTime())==null) startAuto=true;
		super.use(player,objectFactory,data);
		if(startAuto)
		{
			autoUpBuilding.addAutoPlayer(player);
			//开始自动建造后推送服务状态
			Award award=(Award)Award.factory.getSample(awardSids[0]);
			if(award!=null)
			{
				JBackKit.sendServiceStauts(player,award.getServiceSid(0),
					true);
			}
		}
	}
	
	/** ByteBuffer为兼容新的物品使用返回信息 */
	public void use(Player player,CreatObjectFactory objectFactory,
		AutoUpBuildManager autoUpBuilding,ByteBuffer data,int num)
	{
		for(int i=0;i<num;i++)
			use(player,objectFactory,autoUpBuilding,data);
	}
}
