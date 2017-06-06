package foxu.cross.goalleague.action;

import mustang.io.ByteBuffer;
import foxu.cross.goalleague.ServerLeagueManager;
import foxu.cross.server.DataAction;

/**
 * �������˱����˿�
 * 
 * @author Alan
 * 
 */
public class ServerLeagueAction implements DataAction
{

	ServerLeagueManager slm;

	@Override
	public void readAction(ByteBuffer data)
	{
		int type=data.readUnsignedByte();
		if(type==ServerLeagueManager.LEAGUE_INFO)
		{
			data.clear();
			slm.bytesWriteLeagueInfo(data);
		}
		else if(type==ServerLeagueManager.LEAGUE_STAUT_SETTLE)
		{
			int leagueId=data.readInt();
			int time=data.readInt();
			data.clear();
			boolean isSettle=slm.isLeagueSettled(leagueId,time);
			data.writeBoolean(isSettle);
			if(!isSettle) slm.bytesWriteLeagueInfo(data);
		}
		else
		{
			makeEmptyReturnValue(data);
		}
	}

	/** ��װһ���շ�����Ϣ,���length=0ʱ,http����򷵻ػ�����쳣������ */
	public void makeEmptyReturnValue(ByteBuffer data)
	{
		data.clear();
		data.writeByte(0);
	}

	public ServerLeagueManager getSlm()
	{
		return slm;
	}

	public void setSlm(ServerLeagueManager slm)
	{
		this.slm=slm;
	}

}
