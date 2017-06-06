package foxu.sea.achieve;
import foxu.sea.Player;
import foxu.sea.Ship;


/**
 * @author yw
 * »÷É±´¬Ö»³É¾Í
 */
public class KillShipAchievement extends Achievement
{
	int[] shiplevel;
	int[] contain={10001,10008,10011,10018,10021,10028,10031,10038};
	public boolean canAddValue(Ship ship,Player player)
	{
		if(!incontain(ship.getSid()))return false;
		int progress=player.getAchieveProgress(getSid());
		long cvalue=player.getAchieveValue(atrKey);
		if(!fullCollect)
		{
			if(progress>=needValue.length||cvalue>=needValue[progress]
				||ship.getSid()<shiplevel[progress])
				return false;
		}
		return true;
	}
	public boolean incontain(int sid)
	{
		for(int i=0;i<contain.length;i+=2)
		{
			if(sid<contain[i]||sid>contain[i+1]) continue;
			return true;
		}
		return false;
	}

}
