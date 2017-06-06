package foxu.sea;

import java.io.IOException;
import java.util.HashMap;
import shelby.httpclient.HttpRequester;
import shelby.httpclient.HttpRespons;
import foxu.cc.GameDBCCAccess;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.port.UserToCenterPort;
import mustang.io.ByteBuffer;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * ��ѯ���ɾ��״̬�Ƿ���
 *
 * @author lhj
 *
 */
public class CheckUserState implements TimerListener
{
	/**ÿһ����һ��***/
	private static final int time=12*60*1000;
//	private static final int time=30*1000;
	CreatObjectFactory objectFactory;

	public void init()
	{
		// ��ʼ����ʱ��
		TimerEvent e=new TimerEvent(this,"checkuserState",time);
		TimerCenter.getSecondTimer().add(e);
	}
	
	@Override
	public void onTimer(TimerEvent e)
	{
		execute();
	}
	
	/**������Ϣ**/
	public void execute()
	{
		String sql="select * from users where deleteTime!=0";
		User[] users=objectFactory.getUserDBAccess().loadBySql(sql);
		if(users==null || users.length==0) return;
		for(int i=0;i<users.length;i++)
		{
			int time=TimeKit.getSecondTime()-(users[i].getDeleteTime()+(int)(SeaBackKit.WEEK_MILL_TIMES/1000));
			//int time=TimeKit.getSecondTime()-(users[i].getDeleteTime()+(int)(SeaBackKit.WEEK_test/1000));
			if(time>0)
			{
				ByteBuffer deletedate=new ByteBuffer();
				deletedate.writeByte(GameDBCCAccess.CHANGE_PRE_USERACCOUNT);
				deletedate.writeByte(UserToCenterPort.AREA_ID);
				deletedate.writeUTF(users[i].getUserAccount());
				deletedate.writeUTF(User.USER_PREFIX_NAME+users[i].getUserAccount());
				deletedate=sendHttpData(deletedate);
			}
		}
	}
	
	public ByteBuffer sendHttpData(ByteBuffer data)
	{
		HttpRequester request=new HttpRequester();
		String httpData=SeaBackKit.createBase64(data);
		request.setDefaultContentEncoding("UTF-8");
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("data",httpData);
		// ����port
		map.put("port","1");
		HttpRespons re=null;
		try
		{
			re=request.send("http://"+GameDBCCAccess.GAME_CENTER_IP+":"+GameDBCCAccess.GAME_CENTER_HTTP_PORT+"/","POST",map,
				null);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return SeaBackKit.load(re.getContent());
	}

	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
}
