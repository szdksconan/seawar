package foxu.push;

import java.util.HashMap;

import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import shelby.ds.DSManager;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.TimeKit;

/**
 * android push管理器
 * 
 * @author yw
 * 
 */
public class AndroidPush implements TimerListener
{
	/** 推送超时时间 */
	final static int TIME_OUT=3600*8;
	/** 每个设备最大存储推送条数 */
	final static int MAX=10;
	/** 推送 （deviceKey）表*/
	HashMap<String,ArrayList> pushs=new HashMap<String,ArrayList>();
	
	CreatObjectFactory objectFactory;

	public static AndroidPush androidPush;

	DSManager manager;

	public void init()
	{
		androidPush=this;
		TimerCenter.getMinuteTimer().add(
			new TimerEvent(this,"collate",1800*1000));
	}

	/**
	 * @param content 内容
	 */
	public void addPush(Player player,String content,String title)
	{
		if(manager.getSessionMap().get(player.getName())!=null) return;
		String deviceKey=player.getId()+"";
//		if(deviceKey==null)
//		{
//			User user=objectFactory.getUserDBAccess().load(
//				player.getUser_id()+"");
//			player.setLoginUid(user.getLoginUdid());
//			deviceKey=user.getLoginUdid();
//		}
		ArrayList push=pushs.get(deviceKey);
		if(push==null)
		{
			push=new ArrayList();
		}
		if(title==null)title=player.getName();
		push.add(new Push(content,title));
		if(push.size()>MAX)push.remove(0);
		pushs.put(deviceKey,push);			
	}

	public void getPush(String deviceKey,ByteBuffer data)
	{
		String[] keys=TextKit.split(deviceKey,",");
		data.clear();
		int top=data.top();
		int len=0;
		data.writeByte(len);
		Player player;
		for(int i=1;i<keys.length;i++)
		{
			ArrayList push=pushs.get(keys[i]);
			if(push==null) continue;
			player=objectFactory.getPlayerCache().loadPlayerOnly(keys[i]);
			if(player==null||!keys[0].equals(player.getLoginUid()))
				continue;
			synchronized(push)
			{
				for(int k=0;k<push.size();k++)
				{
					Push ph=(Push)push.get(k);
					data.writeUTF(ph.getPname());
					data.writeUTF(ph.getContent());
					len++;
				}
			}
			pushs.remove(keys[i]);
		}
		if(len>0)
		{
			int nowTop=data.top();
			data.setTop(top);
			data.writeByte(len);
			data.setTop(nowTop);
		}
	}
	public void clear(Player player)
	{
		pushs.remove(player.getId()+"");
	}

	public void collate()
	{
		int now=TimeKit.getSecondTime();
		Object[] objs=pushs.keySet().toArray();
		for(int k=objs.length-1;k>=0;k--)
		{
			String key=(String)objs[k];
			ArrayList push=pushs.get(key);
			if(push==null) continue;
			synchronized(push)
			{
				for(int i=0;i<push.size();i++)
				{
					Push pdata=(Push)push.get(i);
					if(pdata==null||pdata.createAt+TIME_OUT<now)
					{
						push.remove(i);
						i--;
					}

				}
			}
		}
		for(int i=objs.length-1;i>=0;i--)
		{
			String key=(String)objs[i];
			ArrayList push=pushs.get(key);
			if(push==null||push.size()<=0)
			{
				pushs.remove(key);
			}
		}

	}

	@Override
	public void onTimer(TimerEvent arg0)
	{
		if(arg0.getParameter().equals("collate"))
		{
			collate();
		}

	}

	public HashMap<String,ArrayList> getPushs()
	{
		return pushs;
	}

	public void setManager(DSManager manager)
	{
		this.manager=manager;
	}

	
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}
	
	

}
