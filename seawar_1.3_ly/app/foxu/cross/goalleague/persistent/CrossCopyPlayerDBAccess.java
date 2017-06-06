package foxu.cross.goalleague.persistent;

import mustang.field.ByteArrayField;
import mustang.field.FieldKit;
import mustang.field.FieldObject;
import mustang.field.Fields;
import mustang.field.IntField;
import mustang.field.StringField;
import mustang.io.ByteBuffer;
import foxu.cross.goalleague.LeaguePlayer;
import foxu.cross.war.CrossWarPlayer;
import foxu.cross.war.CrossWarPlayerDBAccess;


/**
 * 跨服积分赛副本对象数据管理
 * @author Alan
 *
 */
public class CrossCopyPlayerDBAccess extends CrossWarPlayerDBAccess
{
	/** 按列表顺序加载所有数据 */
	public CrossWarPlayer[] loadBySqlAll()
	{
		String sql="select * from league_temp_player order by crossid";
		return loadBySql(sql);
	}
	
	/** 清除所有数据 */
	public void deleteAll()
	{
		excuteSql("truncate league_temp_player");
	}
	
	/** 映射成域对象 */
	public CrossWarPlayer mapping(Fields fields)
	{
		LeaguePlayer player=new LeaguePlayer();
		int crossid=((IntField)fields.get("crossid")).value;
		player.setCrossid(crossid);
		int id=((IntField)fields.get("id")).value;
		player.setId(id);
		String name=((StringField)fields.get("name")).value;
		player.setName(name);
		int warid=((IntField)fields.get("warid")).value;
		player.setWarid(warid);
		int rank=((IntField)fields.get("rank")).value;
		player.setRank(rank);
		int bet=((IntField)fields.get("bet")).value;
		player.setBet(bet);
		int goal=((IntField)fields.get("goal")).value;
		player.setGoal(goal);
		int num=((IntField)fields.get("num")).value;
		player.setNum(num);
		int platid=((IntField)fields.get("platid")).value;
		player.setPlatid(platid);
		int areaid=((IntField)fields.get("areaid")).value;
		player.setAreaid(areaid);
		int severid=((IntField)fields.get("serverid")).value;
		player.setSeverid(severid);
		// 头像
		int headPic=((IntField)fields.get("headPic")).value;
		player.setHeadPic(headPic);
		int headFrame=((IntField)fields.get("headFrame")).value;
		player.setHeadFrame(headFrame);
		
		String sname=((StringField)fields.get("sname")).value;
		player.setSeverName(sname);
		String aname=((StringField)fields.get("aname")).value;
		player.setAname(aname);
		String national=((StringField)fields.get("national")).value;
		player.setNational(national);
		int sid=((IntField)fields.get("sid")).value;
		player.setSid(sid);
		int level=((IntField)fields.get("level")).value;
		player.setLevel(level);
		int fightscore=((IntField)fields.get("fscore")).value;
		player.setFightscore(fightscore);
		int jiontime=((IntField)fields.get("jiontime")).value;
		player.setJiontime(jiontime);
		byte[] array=((ByteArrayField)fields.get("adjustment")).value;
		if(array!=null&&array.length>0)
			player.bytesReadAdjustment(new ByteBuffer(array));
//		array=((ByteArrayField)fields.get("equiplist")).value;
//		if(array!=null&&array.length>0)
//			player.bytesReadEquiplist(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("shiplv")).value;
		if(array!=null&&array.length>0)
			player.bytesReadShiplevel(new ByteBuffer(array));
		
		array=((ByteArrayField)fields.get("attacklist")).value;
		if(array!=null&&array.length>0)
			player.bytesReadAttacklist(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("defencelist")).value;
		if(array!=null&&array.length>0)
			player.bytesReadDefencelist(new ByteBuffer(array));
		array=((ByteArrayField)fields.get("officer")).value;
		if(array!=null&&array.length>0)
			player.bytesReadOFS(new ByteBuffer(array));

		return player;
	}

	/** 映射成域对象 */
	public Fields mapping(Object player)
	{
		LeaguePlayer p=(LeaguePlayer)player;
		FieldObject[] array=new FieldObject[25];
		int i=0;
		//array[i++]=FieldKit.create("crossid",p.getCrossid());
		array[i++]=FieldKit.create("id",p.getId());
		array[i++]=FieldKit.create("name",p.getName());
		array[i++]=FieldKit.create("warid",p.getWarid());
		array[i++]=FieldKit.create("rank",p.getRank());
		array[i++]=FieldKit.create("bet",p.getBet());
		array[i++]=FieldKit.create("goal",p.getGoal());
		array[i++]=FieldKit.create("num",p.getNum());
		array[i++]=FieldKit.create("platid",p.getPlatid());
		array[i++]=FieldKit.create("areaid",p.getAreaid());
		array[i++]=FieldKit.create("serverid",p.getSeverid());
		array[i++]=FieldKit.create("sname",p.getSeverName());
		array[i++]=FieldKit.create("aname",p.getAname());
		array[i++]=FieldKit.create("national",p.getNational());
		array[i++]=FieldKit.create("sid",p.getSid());
		array[i++]=FieldKit.create("level",p.getLevel());
		array[i++]=FieldKit.create("fscore",p.getFightscore());
		array[i++]=FieldKit.create("jiontime",p.getJiontime());
		array[i++]=FieldKit.create("headPic",p.getHeadPic());
		array[i++]=FieldKit.create("headFrame",p.getHeadFrame());
		
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		p.bytesWriteAdjustment(bb);
		array[i++]=FieldKit.create("adjustment",bb.toArray());
		
//		bb.clear();
//		p.bytesWriteEquiplist(bb);
//		array[i++]=FieldKit.create("equiplist",bb.toArray());
		
		bb.clear();
		p.bytesWriteShiplevel(bb);
		array[i++]=FieldKit.create("shiplv",bb.toArray());
		
		bb.clear();
		p.bytesWriteAttacklist(bb);
		array[i++]=FieldKit.create("attacklist",bb.toArray());
		
		bb.clear();
		p.bytesWriteDefencelist(bb);
		array[i++]=FieldKit.create("defencelist",bb.toArray());
		
		bb.clear();
		p.bytesWriteOFS(bb);
		array[i++]=FieldKit.create("officer",bb.toArray());
		
		Fields fs=new Fields();
		fs.add(array,0,i);
		return fs;
	}
}
