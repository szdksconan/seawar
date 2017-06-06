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
 * �����Զ���������
 * 
 * @author Alan
 * 
 */
public class BuildAutoUpProp extends AwardProp
{

	/** ByteBufferΪ�����µ���Ʒʹ�÷�����Ϣ */
	public void use(Player player,CreatObjectFactory objectFactory,
		AutoUpBuildManager autoUpBuilding,ByteBuffer data)
	{
		//��ǰ�����Ƿ���ã���������ʹ�õ��ߺ�ʼ�Զ�����
		boolean startAuto=false;
		if(player.checkService(PublicConst.AUTO_BUILD_BUFF,
			TimeKit.getSecondTime())==null) startAuto=true;
		super.use(player,objectFactory,data);
		if(startAuto)
		{
			autoUpBuilding.addAutoPlayer(player);
			//��ʼ�Զ���������ͷ���״̬
			Award award=(Award)Award.factory.getSample(awardSids[0]);
			if(award!=null)
			{
				JBackKit.sendServiceStauts(player,award.getServiceSid(0),
					true);
			}
		}
	}
	
	/** ByteBufferΪ�����µ���Ʒʹ�÷�����Ϣ */
	public void use(Player player,CreatObjectFactory objectFactory,
		AutoUpBuildManager autoUpBuilding,ByteBuffer data,int num)
	{
		for(int i=0;i<num;i++)
			use(player,objectFactory,autoUpBuilding,data);
	}
}
