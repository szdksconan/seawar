package foxu.sea.gm.operators;

import java.net.URLEncoder;
import java.util.Map;

import javapns.json.JSONArray;
import javapns.json.JSONObject;
import mustang.set.IntList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.Ship;
import foxu.sea.announcement.Announcement;
import foxu.sea.equipment.Equipment;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/***
 * 添加公告
 * 
 * @author lhj
 * 
 */
public class AddAnnouncement extends GMOperator
{

	// 创建公告
	public final static int CREATEANNOUNCE=1;
	public final static int TITLE_LENGTH=200,INTRODUCTION_LENGTH=200,CONTENT_LENGTH=5000,BTN_NAME=50;
	
	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String title=params.get("title");
		String content=params.get("content");
		String introduction=params.get("introduction");
		String starttime=params.get("starttime");
		String endtime=params.get("endtime");
		String awardsid=params.get("awardsid");
		String btnname=params.get("btnname");
		if(btnname!=null)btnname=btnname.trim();
		String typefun=params.get("typefun");
//		String playername=params.get("player_name");
		String playernames=params.get("player_names");
//		String readplayer=params.get("player_read_name");
		String readplayers=params.get("player_read_names");
		CreatObjectFactory objectFactory=info.getObjectFactory();
		IntList playerlist=validateplayers(playernames,
			objectFactory);
		IntList readplayerlist=validateplayers(readplayers,
			objectFactory);
//		if(playername!=null&&playername.length()>0
//			&&playerlist.isEmpty()==true)
//			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(playernames!=null&&playernames.length()>0
			&&playerlist.isEmpty()==true)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;

//		if(readplayer!=null&&readplayer.length()>0
//			&&readplayerlist.isEmpty()==true)
//			return GMConstant.ERR_PLAYER_NOT_EXISTS;
		if(readplayers!=null&&readplayers.length()>0
			&&readplayerlist.isEmpty()==true)
			return GMConstant.ERR_PLAYER_NOT_EXISTS;

		if(Integer.parseInt(typefun)==CREATEANNOUNCE)
		{
			if(title==null||title.length()==0)
				return GMConstant.ERR_TITLE_NULL;
			if(title.length()>=TITLE_LENGTH)
				return GMConstant.ERR_TITLE_LENGTH_ERRO;
			if(content==null||content.length()==0)
				return GMConstant.ERR_CONTENT_NULL;
			if(content.length()>=CONTENT_LENGTH)
				return GMConstant.ERR_CONTENT_LENGTH_ERRO;
			if(introduction==null||introduction.length()==0)
				return GMConstant.ERR_INTRODUCTION_ERRO;
			if(introduction.length()>=INTRODUCTION_LENGTH)
				return GMConstant.ERR_INTODUCTION_LENGTH_ERRO;
			int timeNow=TimeKit.getSecondTime();
			int etime;
			int stime;
			try
			{
				stime=SeaBackKit.parseFormatTime(starttime);
				etime=SeaBackKit.parseFormatTime(endtime);
			}
			catch(Exception e)
			{
				return GMConstant.ERR_TIME_ERRO;
			}
			if(etime<=timeNow||stime>=etime)
				return GMConstant.ERR_TIME_ERRO;
			int flag=validate(awardsid,btnname);
			if(flag!=0) return flag;
			int[] awardsidNum=null;
			if(awardsid!=null&&awardsid.length()!=0)
			{
				awardsidNum=isAward(awardsid);
				if(awardsidNum==null||awardsidNum.length<=0)
					return GMConstant.ERR_AWARD_ERRO;
			}
			Announcement newAnnounce=createAnncounce(title,content,
				introduction,stime,etime,btnname,awardsidNum,timeNow,
				playerlist,readplayerlist);
			objectFactory.getAnnMemcahe().save(
				String.valueOf(newAnnounce.getId()),newAnnounce);
			if(stime<=timeNow)
				JBackKit.sendAnnouncement(objectFactory.getDsmanager()
					.getSessionMap(),newAnnounce);
		}
		// 查询功公告
		else
		{
			Announcement[] ann=objectFactory.getAnnMemcahe()
				.getAllAnnouncement();
			if(ann.length==0) return GMConstant.ERR_ANNOUNCE_IS_NULL;
			for(int i=0;i<ann.length;i++)
			{
				Announcement announce=(Announcement)ann[i];
				try
				{
					JSONObject jo=new JSONObject();
					jo.put(GMConstant.ID,announce.getId());
					jo.put(GMConstant.TITLE,
						URLEncoder.encode(announce.getTitle(),"utf-8"));
					jo.put(GMConstant.INTRODUCTION,URLEncoder.encode(
						announce.getIntroduction(),"utf-8"));
					jo.put(GMConstant.START_TIME,announce.getStartTime());
					jo.put(GMConstant.END_TIME,announce.getEndTime());
					if(announce.getStartTime()<=TimeKit.getSecondTime())
						jo.put(GMConstant.PERMANNENT,GMConstant.PERMANNENT
							+"_1");
					else
						jo.put(GMConstant.PERMANNENT,GMConstant.PERMANNENT
							+"_2");
					jsonArray.put(jo);
				}
				catch(Exception e)
				{

				}
			}
		}
		return GMConstant.ERR_SUCCESS;
	}
	/** 验证 */
	public int validate(String awardsid,String btnname)
	{
		if(awardsid!=null&&awardsid.length()!=0)
		{
			if(btnname==null||btnname.length()==0)
				return GMConstant.ERR_BTNNAME_NULL;
			if(btnname.length()>=BTN_NAME)
				return GMConstant.ERR_PARAMATER_ERROR;
		}
		if(btnname!=null&&btnname.length()!=0 && btnname.length()<BTN_NAME)
		{
			if(awardsid==null||awardsid.length()<=0)
				return GMConstant.ERR_AWARD_ERRO;
		}
		return GMConstant.ERR_SUCCESS;
	}
	/**
	 * 验证奖励是否正确
	 * 
	 * @param awardsid
	 * @return
	 */
	public int[] isAward(String awardsid)
	{
		String[] awards=awardsid.split(",");
		int[] awardsNum=new int[awards.length*2];
		for(int i=0;i<awards.length;i++)
		{
			try
			{
				int award=Integer.parseInt(awards[i].split("-")[0]);
				Prop prop=(Prop)Prop.factory.getSample(award);
				if(prop==null)
				{
					Ship ship=(Ship)Ship.factory.getSample(award);
					if(ship==null)
					{
						Equipment equ=(Equipment)Equipment.factory.getSample(award);
						if(equ==null) 
						{
							if(!SeaBackKit.isContainValue(Equipment.QUALITY_STUFFS,award))
								return null;
						}
					}
				}
				int num=Integer.parseInt(awards[i].split("-")[1]);
				if(num<=0) return null;
				awardsNum[i*2]=award;
				awardsNum[i*2+1]=num;
			}
			catch(Exception e)
			{
				return null;
			}
		}
		return awardsNum;
	}
	/***
	 * 创建一个公告
	 * 
	 * @param title 标题
	 * @param content 内容
	 * @param introduction 简介
	 * @param stime 开始时间
	 * @param etime 结束时间
	 * @param type 是否定时
	 * @param btnname 按钮名称
	 * @param awardsidNum 奖励
	 * @return
	 */
	public Announcement createAnncounce(String title,String content,
		String introduction,int stime,int etime,String btnname,
		int[] awardsidNum,int timeNow,IntList playerlist,
		IntList readplayerlist)
	{
		Announcement ann=new Announcement();
		ann.setId(timeNow);
		ann.setTitle(title);
		ann.setContent(content);
		ann.setIntroduction(introduction);
		if(stime>timeNow)
		{
			ann.setStartTime(stime);
			ann.setPermanent(0);// 定时
		}
		else
		{
			ann.setStartTime(timeNow);
			ann.setPermanent(1);// 不定时
		}
		ann.setEndTime(etime);
		if(btnname!=null&&btnname.length()>0)
		{
			ann.setAwardSid(awardsidNum);
			ann.setBtnname(btnname);
		}
		if(playerlist!=null) ann.setAbleawardplayer(playerlist);
		if(readplayerlist!=null) ann.setReadplayer(readplayerlist);
		return ann;
	}

	/***
	 * 验证玩家名称是否正确
	 * 
	 * @param readplayer
	 * @param readplayers
	 * @return
	 */
	public IntList validateplayers(String readplayers,
		CreatObjectFactory objectFactory)
	{
		IntList list=new IntList();
//		if(readplayer!=null&&readplayer.length()>0)
//		{
//			if(readplayer.indexOf("],")!=-1)
//			{
//				for(int i=0;i<readplayer.split("],").length;i++)
//				{
//					Player player=objectFactory.getPlayerByName(
//						readplayer.split("],")[i],false);
//					if(player==null) return new IntList();
//					list.add(player.getId());
//				}
//			}
//			else if(readplayer.indexOf("]")!=-1)
//			{
//				for(int i=0;i<readplayer.split("]").length;i++)
//				{
//					Player player=objectFactory.getPlayerByName(
//						readplayer.split("]")[i],false);
//					if(player==null) return new IntList();
//					list.add(player.getId());
//				}
//			}
//			else
//			{
//				Player player=objectFactory
//					.getPlayerByName(readplayer,false);
//				if(player==null) return new IntList();
//				list.add(player.getId());
//			}
//		}
		if(readplayers!=null&&readplayers.length()>0)
		{
			for(int i=0;i<readplayers.split(",").length;i++)
			{
				Player player=objectFactory.getPlayerByName(
					readplayers.split(",")[i],false);
				if(player==null) return new IntList();
				list.add(player.getId());
			}
		}
		return list;
	}

}
