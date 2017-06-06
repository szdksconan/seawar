package foxu.cross.goalleague.action;

import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import foxu.cross.goalleague.ClientLeagueManager;
import foxu.cross.goalleague.LeaguePlayer;
import foxu.cross.goalleague.LeagueShopProp;
import foxu.cross.goalleague.ServerLeagueManager;
import foxu.cross.server.DataAction;
import foxu.cross.war.CrossWarPlayer;

/**
 * 客户端比赛端口
 * 
 * @author Alan
 * 
 */
public class ClientLeagueAction implements DataAction
{

	ClientLeagueManager clm;

	@Override
	public void readAction(ByteBuffer data)
	{
		int type=data.readUnsignedByte();
		if(type==ServerLeagueManager.LEAGUE_STAUT_CLOSE)
		{
			clm.closeSystem();
			makeEmptyReturnValue(data);
		}
		else if(type==ServerLeagueManager.LEAGUE_STAUT_OPEN)
		{
			int leagueId=data.readInt();
			int currentSettleTime=data.readInt();
			int nextOpenTime=data.readInt();
			int len=data.readUnsignedByte();
			ArrayList list=new ArrayList();
			LeagueShopProp lsp=null;
			for(int i=0;i<len;i++)
			{
				lsp=ClientLeagueManager.bytesRead2LeagueShopProp(data);
				list.add(lsp);
			}
			boolean isSuccess=clm.open(leagueId,currentSettleTime,nextOpenTime,list);
			data.clear();
			// 如果开启不成功,客户端需要请求预存数据,再次接受指令
			if(isSuccess)
				data.writeBoolean(isSuccess);
		}
		else if(type==ServerLeagueManager.LEAGUE_STAUT_COPY_IN)
		{
			int lv=data.readUnsignedShort();
			data.clear();
			ArrayList list=clm.collectCopyInfo(lv);
			LeaguePlayer cwp=null;
			data.writeInt(list.size());
			for(int i=0;i<list.size();i++)
			{
				cwp=(LeaguePlayer)list.get(i);
				ClientLeagueManager.bytesWrite2Buffer(data,cwp);
			}
		}
		else if(type==ServerLeagueManager.LEAGUE_STAUT_COPY_OUT)
		{
			int len=data.readInt();
			ArrayList list=new ArrayList();
			CrossWarPlayer cwp=null;
			for(int i=0;i<len;i++)
			{
				cwp=ClientLeagueManager.bytesRead2CrossWarPlayer(data);
				list.add(cwp);
			}
			clm.resetReadyCopyList(list);
			makeEmptyReturnValue(data);
		}
		else if(type==ServerLeagueManager.LEAGUE_STAUT_PLAYER_INFO)
		{
			boolean isSettle=data.readBoolean();
			if(isSettle)
				clm.setLeagueSettle();
			data.clear();
			ArrayList list=clm.collectCrossWarPlayer();
			LeaguePlayer cp=null;
			// 写人联赛id,验证用
			data.writeInt(clm.getLeagueId());
			data.writeInt(list.size());
			for(int i=0;i<list.size();i++)
			{
				cp=(LeaguePlayer)list.get(i);
				ClientLeagueManager.showBytesWrite2Buffer(data,cp);
			}
		}
		else if(type==ServerLeagueManager.LEAGUE_STAUT_RANK)
		{
			int len=data.readInt();
			ArrayList list=new ArrayList();
			CrossWarPlayer cp=null;
			for(int i=0;i<len;i++)
			{
				cp=ClientLeagueManager.showBytesRead2LeaguePlayer(data);
				cp.setRank(i+1);
				list.add(cp);
			}
			clm.resetRankList(list);
			makeEmptyReturnValue(data);
		}
		else if(type==ServerLeagueManager.LEAGUE_STAUT_SETTLE)
		{
			clm.leagueSettleDown();
			makeEmptyReturnValue(data);
		}
		else if(type==ServerLeagueManager.LEAGUE_CENTER_INFO)
		{
			String ip=data.readUTF();
			String port=data.readUTF();
			String serverName=data.readUTF();
			clm.updateLeagueCenterInfo(ip,port,serverName);
			makeEmptyReturnValue(data);
		}
		else if(type==ServerLeagueManager.LEAGUE_INFO)
		{
			clm.initLeague(data);
			makeEmptyReturnValue(data);
		}
		else if(type==ServerLeagueManager.LEAGUE_SETTLE_AWARD)
		{
			int id=data.readInt();
			String name=data.readUTF();
			int ranking=data.readUnsignedShort();
			int awardSid=data.readUnsignedShort();
			clm.sendRankingAward(id,name,ranking,awardSid);
			makeEmptyReturnValue(data);
		}
		else
		{
			makeEmptyReturnValue(data);
		}
	}

	/** 组装一个空返回信息,规避length=0时,http请求或返回会出现异常的问题 */
	public void makeEmptyReturnValue(ByteBuffer data)
	{
		data.clear();
		data.writeByte(0);
	}

	public ClientLeagueManager getClm()
	{
		return clm;
	}

	public void setClm(ClientLeagueManager clm)
	{
		this.clm=clm;
	}

}
