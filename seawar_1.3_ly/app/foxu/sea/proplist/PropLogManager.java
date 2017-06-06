package foxu.sea.proplist;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;

/**
 * ��Ʒ��־������ (�����Ʒ��־����ʱ��δ���CreatObjectFactory,�Ķ��϶�)
 * 
 * @author Alan
 */
public class PropLogManager
{

	CreatObjectFactory objectFactory;
	static PropLogManager manager=new PropLogManager();

	public static PropLogManager getInstance()
	{
		return manager;
	}

	public void incrPorp(Player player,int propSid,int num)
	{
		objectFactory.createPropTrack(PropTrack.INCR_PROP,player.getId(),
			propSid,num,player.getBundle().getCountBySid(propSid));
	}

	public void decrProp(Player player,int propSid,int num)
	{
		objectFactory.createPropTrack(PropTrack.DECR_PROP,player.getId(),
			propSid,num,player.getBundle().getCountBySid(propSid));
	}
	
	public void buyUseProp(Player player,int propSid)
	{
		objectFactory.createPropTrack(PropTrack.INCR_PROP,player.getId(),
			propSid,1,player.getBundle().getCountBySid(propSid)+1);
		decrProp(player,propSid,1);
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
