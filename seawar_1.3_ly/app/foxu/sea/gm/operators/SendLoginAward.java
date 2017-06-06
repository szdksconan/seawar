package foxu.sea.gm.operators;

import java.util.Calendar;
import java.util.Map;

import javapns.json.JSONArray;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.datasave.PlayerSave;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Ship;
import foxu.sea.award.Award;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.shipdata.ShipCheckData;

/**
 * 发连续登陆奖励
 * 
 * @author yw
 * 
 */
public class SendLoginAward extends GMOperator
{

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String title=params.get("title");
		String content=params.get("content");
		String propStr=params.get("props");
		int days=TextKit.parseInt(params.get("days"));

//		System.out.println("=====================:"+title+":"+content+":"
//			+propStr+":"+days);
		int[] props=TextKit.parseIntArray(propStr.split(","));
		ArrayList prop=new ArrayList();
		IntList ship=new IntList();
		for(int i=0;i<props.length;i+=2)
		{
			if(props[i]>=Award.SHIP_START_SID)
			{
				if(Ship.factory.getSample(props[i])==null)
					return GMConstant.ERR_PARAMATER_ERROR;
				ship.add(props[i]);
				ship.add(props[i+1]);
			}
			else
			{
				Prop p=(Prop)Prop.factory.newSample(props[i]);
				if(p==null) return GMConstant.ERR_PARAMATER_ERROR;
				((NormalProp)p).setCount(props[i+1]);
				prop.add(p);
			}
		}
		Object[] players=info.getObjectFactory().getPlayerCache()
			.getCacheMap().valueArray();
		Calendar c=Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
//		System.out.println("--------SendLoginAward-------00-----："
//			+players.length);
		for(int i=0;i<players.length;i++)
		{
			if(players[i]==null) continue;
			Player player=((PlayerSave)players[i]).getData();
			if(player==null) continue;
			if(!canGet(days,player))continue;
//			System.out
//				.println("-------------SendLoginAward---------------::"
//					+player.getName());
			boolean haveP=false;
			boolean haveS=false;
			for(int k=0;k<prop.size();k++)
			{
				player.getBundle().incrProp((NormalProp)prop.get(k),true);
				haveP=true;
			}
			for(int k=0;k<ship.size();k+=2)
			{
				player.getIsland().addTroop(ship.get(k),ship.get(k+1),
					player.getIsland().getTroops());
				// 船只日志
				IntList fightlist=new IntList();
				fightlist.add(ship.get(k));
				fightlist.add(ship.get(k+1));
				info.getObjectFactory().addShipTrack(
					0,ShipCheckData.LOGIN_AWARD,player,fightlist,null,false);
				haveS=true;
			}
			sendMail(player,title,content,info.getObjectFactory());
			if(haveP)JBackKit.sendResetBunld(player);
			if(haveS)JBackKit.sendResetTroops(player);
		}
		return 0;
	}
	public boolean canGet(int days,Player player)
	{
		String loginday=player.getAttributes(PublicConst.SERIES_LOGIN);
		int logindays=loginday==null?0:Integer.parseInt(loginday);
		return logindays>days;
	}
	/**
	 * 给指定的玩家发送邮件
	 * 
	 * @param playerName
	 * @param title
	 * @param content
	 * @param objectFactory
	 * @return
	 */
	private void sendMail(Player player,String title,String content,
		CreatObjectFactory objectFactory)
	{
		if(content==null||content.equals("")||title==null||title.equals(""))
			return;
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),0,title,true);
		// 刷新前台
		JBackKit.sendRevicePlayerMessage(player,message,
			message.getRecive_state(),objectFactory);
	}

}
