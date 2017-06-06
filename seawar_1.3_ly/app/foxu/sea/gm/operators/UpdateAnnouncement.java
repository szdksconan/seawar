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
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.GMOperator;
import foxu.sea.gm.ServerInfo;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * 公告更新
 * 
 * @author lihongji
 * 
 */
public class UpdateAnnouncement extends GMOperator
{

	/** 查询公告的内容 */
	static int READCOUNT=1;
	CreatObjectFactory objectFactory;

	@Override
	public int operate(String user,Map<String,String> params,
		JSONArray jsonArray,ServerInfo info)
	{
		String type=params.get("type");
		String flag=params.get("flag");
		String id=params.get("id");
		int types=Integer.parseInt(type);
		if(types==READCOUNT)
		{
			Announcement announce=objectFactory.getAnnMemcahe().getById(
				Integer.parseInt(id));
			if(announce==null) return GMConstant.ERR_ANNOUNCE_NULL;
			jsonArray.put(getAnnounceInfo(announce,flag));
		}
		/** 修改公告内容 */
		else
		{
			Announcement announce=objectFactory.getAnnMemcahe().getById(
				Integer.parseInt(id));
			if(announce==null) return GMConstant.ERR_ANNOUNCE_NULL;
			String endtime=params.get("etime");
			String starttime=params.get("stime");
			String title=params.get("title");
			String introduction=params.get("introduction");
			String content=params.get("content");
			String btnname=params.get("btnname");
			if(btnname!=null)btnname=btnname.trim();
			String awardSid=params.get("award");
			String annreadplayer=params.get("annreadplayer");
			String annawardplayer=params.get("annawardplayer");
			if(endtime==null||endtime.length()==0)
				return GMConstant.ERR_TIME_ERRO;
			int etime=0;
			try
			{
				etime=SeaBackKit.parseFormatTime(endtime);
			}
			catch(Exception e)
			{
				return GMConstant.ERR_TIME_ERRO;
			}
			if(Integer.parseInt(flag)==1)
				announce.setEndTime(etime);
			else
			{
				if(title==null||title.length()==0)
				return GMConstant.ERR_TITLE_NULL;
			if(title.length()>=AddAnnouncement.TITLE_LENGTH)
				return GMConstant.ERR_TITLE_LENGTH_ERRO;
			if(content==null||content.length()==0)
				return GMConstant.ERR_CONTENT_NULL;
			if(content.length()>=AddAnnouncement.CONTENT_LENGTH)
				return GMConstant.ERR_CONTENT_LENGTH_ERRO;
			if(introduction==null||introduction.length()==0)
				return GMConstant.ERR_INTRODUCTION_ERRO;
			if(introduction.length()>=AddAnnouncement.INTRODUCTION_LENGTH)
				return GMConstant.ERR_INTODUCTION_LENGTH_ERRO;
				int stime=0;
				try
				{
					stime=SeaBackKit.parseFormatTime(starttime);
				}
				catch(Exception e)
				{
					return GMConstant.ERR_TIME_ERRO;
				}
				int flag1=validate(awardSid,btnname);
				if(flag1!=0) return flag1;
				int[] awardsidNum=null;
				if(awardSid!=null&&awardSid.length()!=0)
				{
					awardsidNum=isAward(awardSid);
					if(awardsidNum==null||awardsidNum.length<=0)
						return GMConstant.ERR_AWARD_ERRO;
				}
				announce.setTitle(title);
				announce.setContent(content);
				announce.setStartTime(stime);
				announce.setEndTime(etime);
				announce.setIntroduction(introduction);
				if(awardsidNum!=null&&awardSid.length()!=0)
				{
					announce.setAwardSid(awardsidNum);
					announce.setBtnname(btnname);
				}
				if(!announce.getAbleawardplayer().isEmpty())
				{
					announce.setAbleawardplayer(setPlayerName(annawardplayer));
				}
				if(!announce.getReadplayer().isEmpty())
				{
					announce.setReadplayer(setPlayerName(annreadplayer));
				}
			}
			objectFactory.getAnnMemcahe().save(
				String.valueOf(announce.getId()),announce);
			jsonArray.put(getAnnounceInfo(announce,flag));
		}

		return GMConstant.ERR_SUCCESS;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
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
				Prop prop=(Prop)Prop.factory.newSample(award);
				if(prop==null)
				{
					Ship ship=(Ship)Ship.factory.newSample(award);
					if(ship==null) return null;
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

	/** 验证 */
	public int validate(String awardsid,String btnname)
	{
		if(awardsid!=null&&awardsid.length()!=0)
		{
			if(btnname==null||btnname.length()==0)
				return GMConstant.ERR_BTNNAME_NULL;
			if(btnname.length()>=AddAnnouncement.BTN_NAME)
				return GMConstant.ERR_PARAMATER_ERROR;
		}
		if(btnname!=null&&btnname.length()!=0 && btnname.length()<AddAnnouncement.BTN_NAME)
		{
			if(awardsid==null||awardsid.length()<=0)
				return GMConstant.ERR_AWARD_ERRO;
		}
		return GMConstant.ERR_SUCCESS;
	}
	public String getAward(int award[])
	{
		if(award==null||award.length<=0) return "";
		String awardlength="";
		for(int i=0;i<award.length;i+=2)
		{
			if(i==0)
				awardlength=award[i]+"-"+award[i+1];
			else
				awardlength+=","+award[i]+"-"+award[i+1];
		}
		return awardlength;
	}
	/** json格式 */
	public JSONObject getAnnounceInfo(Announcement announce,String flag)
	{
		JSONObject jo=new JSONObject();
		try
		{
			jo.put(GMConstant.ID,announce.getId());
			jo.put(GMConstant.TITLE,
				URLEncoder.encode(announce.getTitle(),"utf-8"));
			jo.put(GMConstant.INTRODUCTION,
				URLEncoder.encode(announce.getIntroduction(),"utf-8"));
			if(getAward(announce.getAwardSid())!=""
				&&getAward(announce.getAwardSid()).length()>0)
			{
				jo.put(GMConstant.AWARD,getAward(announce.getAwardSid()));
				jo.put(GMConstant.BTNNAMES,
					URLEncoder.encode(announce.getBtnname(),"utf-8"));
			}
			else
			{
				jo.put(GMConstant.AWARD,"");
				jo.put(GMConstant.BTNNAMES,"");
			}
			jo.put(GMConstant.CONTENT,
				URLEncoder.encode(announce.getContent(),"utf-8"));
			jo.put(GMConstant.START_TIME,announce.getStartTime());
			jo.put(GMConstant.END_TIME,announce.getEndTime());
			// 是否定时
			if(announce.getStartTime()<TimeKit.getSecondTime())
				jo.put(GMConstant.PERMANNENT,GMConstant.PERMANNENT+"_1");
			else
				jo.put(GMConstant.PERMANNENT,GMConstant.PERMANNENT+"_2");
			if(announce.getStartTime()<TimeKit.getSecondTime())
			{
				jo.put(GMConstant.ANNOUNCESTATE,GMConstant.ANNOUNCESTATE
					+"_1");// 公告未开启
				jo.put(GMConstant.ANNOUNCEFLAG,0);// 公告未开启
			}
			else
			{
				jo.put(GMConstant.ANNOUNCESTATE,GMConstant.ANNOUNCESTATE
					+"_2");// 公告已开启
				jo.put(GMConstant.ANNOUNCEFLAG,1);// 公告未开启
			}
			if(Integer.parseInt(flag)==0)
				jo.put(GMConstant.ANNOUNCEEDITOR,"0");// 不可以编辑
			else
				jo.put(GMConstant.ANNOUNCEEDITOR,"1");// 可以编辑
			jo.put(GMConstant.ANNREADPLAYER,
				getPlayerName(announce.getReadplayer()));// 可以阅读的玩家
			jo.put(GMConstant.ANNAWARDPLAYER,
				getPlayerName(announce.getAbleawardplayer()));// 可以领奖的玩家
		}
		catch(Exception e)
		{

		}
		return jo;

	}
	/*** 获取玩家名称 */
	public String getPlayerName(IntList list)
	{
		if(list.isEmpty()) return "";
		String playerNames="";
		for(int i=0;i<list.size();i++)
		{
			Player player=objectFactory.getPlayerById(list.get(i));
			if(i==list.size()-1)
				playerNames+=player.getName();
			else
				playerNames+=player.getName()+",";
		}
		return playerNames;
	}

	/** 将玩家名称进行储存 **/
	public IntList setPlayerName(String str)
	{
		IntList list=new IntList();
//		if(str.length()==0) return list;
//		String playerName=null;
//		if(str.indexOf("],")!=-1)
//		{
//			String[] playerNames=str.split("],");
//			playerName=playerNames[playerNames.length-1];
//			list=playerNames(list,playerNames,true);
//		}
//		else if(str.indexOf("]")!=-1)
//		{
//			String[] playerNames=str.split("]");
//			playerName=playerNames[playerNames.length-1];
//			list=playerNames(list,playerNames,true);
//		}
		list=playerNames(list,str.split(","),false);
		return list;
	}

	/** 将字符串里面的玩家名称转换成id **/
	public IntList playerNames(IntList list,String[] playerStr,boolean flag)
	{
		if(playerStr.length<=0) return list;
		int length=0;
		if(flag) length=1;
		for(int i=0;i<playerStr.length-length;i++)
		{
			if(playerStr[i]==null||playerStr[i].length()==0) continue;
			Player player=objectFactory.getPlayerByName(playerStr[i],false);
			if(player==null) return new IntList();
			list.add(player.getId());
		}
		return list;
	}
}
