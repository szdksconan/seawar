package foxu.sea.port;

import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.recruit.RecruitWelfareManager;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.Session;


/**
 * ÐÂ±ø¸£Àû
 * @author yw
 *
 */
public class RecruitPort extends AccessPort
{

	/** NORMALÆÕÍ¨½±Àø VIPvip½±Àø TASKÈÎÎñ½±Àø HALF°ë¼ÛÇÀ¹º */
	public static int NORMAL=0,VIP=1,TASK=2,HALF=3;
	
	RecruitWelfareManager rmanager;

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
		if(type==NORMAL||type==VIP||type==TASK)
		{
			rmanager.getAward(type,player,data);
		}
		else if(type==HALF)
		{
			rmanager.buyHalf(player,data);
		}
		return data;
	}

	
	public RecruitWelfareManager getRmanager()
	{
		return rmanager;
	}

	
	public void setRmanager(RecruitWelfareManager rmanager)
	{
		this.rmanager=rmanager;
	}

	
}
