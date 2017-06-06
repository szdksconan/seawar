package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.Comparator;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.set.ObjectArray;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;


/**
 * 战争狂人
 * @author yw
 *
 */
public class WarManicActivity extends Activity implements ActivitySave
{
	/** 领奖时间 */
	public static int awardTime=24*3600;
	/** 积分类型|奖励类型  */
	public static int BOSS=1,ELITE=2,HOSTILE=3,ARMS=4,RANKP=5,RANKA=6;
	/** 空奖励 */
	int em_sid=65051;
	/** 联盟积分比例 */
	int scale=100;
	/** 个人排名容量 */
	int psize;
	/** 个人排行 */
	ObjectArray rankp=new ObjectArray();
	/** 联盟排名容量 */
	int  asize;
	/** 联盟排行 */
	ObjectArray ranka=new ObjectArray();
	/** 个人积分 */
	IntKeyHashMap mapp=new IntKeyHashMap();
	/** 联盟积分 */
	IntKeyHashMap mapa=new IntKeyHashMap();
	
	/** 个人排名奖励记录 */
	IntList markp=new IntList();
	/** 联盟排名奖励记录 */
	IntList marka=new IntList();
	
	/** boss积分奖励条件+奖励(value,award...) */
	Object[] bossAward;
	/** 精英积分奖励条件+奖励 */
	Object[] eliteAward;
	/** 敌对积分奖励条件+奖励 */
	Object[] hostileAward;
	/** 军备积分奖励条件+奖励 */
	Object[] armsAward;
	
	/** 个人排名奖励 (r1,r2,award....)*/
	Object[] rankpAward;
	/** 联盟排名奖励 */
	Object[] rankaAward;
	
	/** 初始化数据 */
	String initdata;
	
	CreatObjectFactory cfactory;
	
	/** 保存cd*/
	int saveCD=15*60;
	/** 上次保存时间 */
	int saveTime;
	

	/** 初始化 */
	public void init(CreatObjectFactory factoty)
	{
		cfactory=factoty;
		rankp.setComparator(new compareRankerP());
		ranka.setComparator(new compareRankerA());
	}
	/** 重算个人排名 */
	public void resetRankp(RankerP rp)
	{
		synchronized(rankp)
		{
			if(rankp.size()<=0)
			{
				rankp.add(rp);
			}
			else if(!rankp.contain(rp))
			{
				rankp.add(rp);
			}
			rankp.sort();
			while(rankp.size()>psize)
			{
				rankp.remove();
			}
		}
	}
	/** 重算联盟排名 */
	public void resetRanka(RankerA ra)
	{
		synchronized(ranka)
		{
			if(ranka.size()<=0)
			{
				ranka.add(ra);
			}
			else if(!ranka.contain(ra))
			{
				ranka.add(ra);
			}
			ranka.sort();
			while(ranka.size()>asize)
			{
				ranka.remove();
			}
		}

	}
	/** 推动个人积分 */
	public void addPScore(int type,int score,Player player)
	{
		if(score<=0) return;
		if(checkAddScore(type)) return;
		RankerP rp=(RankerP)mapp.get(player.getId());
		if(rp==null)
		{
			rp=new RankerP(player.getId());
			mapp.put(rp.getId(),rp);
		}
		int ascore=rp.getAllScore()/scale;
		rp.addTypeScore(type,score);
		int nscore=rp.getAllScore()/scale;
		resetRankp(rp);
		if(nscore>ascore)
		{
			String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(aid!=null)
			{
				int alid=TextKit.parseInt(aid);
				Alliance al=cfactory.getAlliance(alid,false);
				if(al!=null)
				{
					RankerA ra=(RankerA)mapa.get(alid);
					if(ra==null)
					{
						ra=new RankerA(alid);
						mapa.put(ra.getId(),ra);
					}
					ra.addScore(nscore-ascore);
					resetRanka(ra);
				}
			}
		}
		// todo 刷新？？
		JBackKit.sendWarManic(this,player);
	}
	
	/**检查是否需要增加积分**/
	public boolean checkAddScore(int type)
	{
		if(type==BOSS)
		{
			return bossAward==null;
		}	
		else if(type==ELITE)
		{
			return eliteAward==null;
		}
		else if(type==HOSTILE)
		{
			return hostileAward==null;
		}
		else if(type==ARMS)
		{
			return armsAward==null;
		}
		return true;
	}
	/** 刷新积分序列化 */
	public void showBytesWrite(ByteBuffer data,Player player)
	{
		data.writeInt(getPlayerScore(RANKP,player));
		data.writeInt(getAllianceScore(player));
		int len=0;
		int top=data.top();
		data.writeByte(len);
		String[] d1=TextKit.split(initdata,"&");
		for(int i=0;i<4&&i<d1.length;i++)
		{
			if(d1[i].equals("null")) continue;
			len++;
			data.writeByte(i+1);
			data.writeInt(getPlayerScore(i+1,player));
			Object[] objs=getAward(i+1);
			data.writeShort(objs.length/2);
			// boss积分奖励条件+奖励(value,award...)
			for(int k=0;k<objs.length;k+=2)
			{
				data.writeShort(k/2);
				data.writeByte(getAwardState(i+1,k/2,player));
			}
		}
		if(len>0)
		{
			int nowtop=data.top();
			data.setTop(top);
			data.writeByte(len);
			data.setTop(nowtop);
		}
	}

	/** 领取奖励 */
	public void getAward(ByteBuffer data,int type,int index,Player player)
	{
		if(index<0)
		{
			throw new DataAccessException(0,"not this award");// 无此奖励
		}
		// 检测mark
		if(checkMark(type,index,player))
		{
			throw new DataAccessException(0,"had got");// 已领取过奖励
		}
		// 检测条件
		checkAwardCondition(type,index,player);
		// 记录
		markAward(type,index,player);
		// 发奖
		sendAward(type,index,player,data);

	}
	/** 获取奖励组 */
	public Object[] getAward(int type)
	{
		if(type==BOSS)
		{
			return bossAward;
		}
		else if(type==ELITE)
		{
			return eliteAward;
		}
		else if(type==HOSTILE)
		{
			return hostileAward;
		}
		else if(type==ARMS)
		{
			return armsAward;
		}
		return null;
	}

	/** 发放奖励 */
	public void sendAward(int type,int index,Player player,ByteBuffer data)
	{
		Award award=null;
		if(type==BOSS)
		{
			award=(Award)bossAward[index*2+1];
		}
		else if(type==ELITE)
		{
			award=(Award)eliteAward[index*2+1];
		}
		else if(type==HOSTILE)
		{
			award=(Award)hostileAward[index*2+1];
		}
		else if(type==ARMS)
		{
			award=(Award)armsAward[index*2+1];
		}
		else if(type==RANKP)
		{
			int index1=index+1;
			for(int i=0;i<rankpAward.length;i+=3)
			{
				if(index1>=(Integer)rankpAward[i]
					&&index1<=(Integer)rankpAward[i+1])
				{
					award=(Award)rankpAward[i+2];
					break;
				}
			}
		}
		else if(type==RANKA)
		{
			int index1=index+1;
			for(int i=0;i<rankaAward.length;i+=3)
			{
				if(index1>=(Integer)rankaAward[i]
					&&index1<=(Integer)rankaAward[i+1])
				{
					award=(Award)rankaAward[i+2];
					break;
				}
			}
		}
		if(award!=null)
		{
			award.awardLenth(data,player,cfactory,null,null,
				new int[]{EquipmentTrack.RROM_WAR_MANNIAC});
		}
		data.clear();
	}

	/** 检测领奖条件 */
	public void checkAwardCondition(int type,int index,Player player)
	{
		if(type==BOSS||type==ELITE||type==HOSTILE||type==ARMS)
		{
			if(!checkConditionScore(type,index,player))
			{
				throw new DataAccessException(0,"score limit");// 积分不够
			}
		}
		else if(type==RANKP||type==RANKA)
		{
			checkConditionRank(type,index,player);
		}
		else
		{
			throw new DataAccessException(0,"error type");// 错误的请求类型
		}
	}

	/** 检测排名条件 */
	public void checkConditionRank(int type,int index,Player player)
	{
		if(type==RANKP)
		{
			Object[] objs=rankp.getArray();
			if(index>=objs.length)
				throw new DataAccessException(0,"rank error1");// 名次错误
			RankerP rp=(RankerP)objs[index];
			if(rp.getId()!=player.getId())
				throw new DataAccessException(0,"rank error2");// 名次错误
		}
		else if(type==RANKA)
		{
			Object[] objs=ranka.getArray();
			if(index>=objs.length)
				throw new DataAccessException(0,"rank error3");// 名次错误
			RankerA ra=(RankerA)objs[index];
			String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(aid==null||aid.equals(""))
				throw new DataAccessException(0,"rank error4");// 名次错误
			int alid=TextKit.parseInt(aid);
			if(ra.getId()!=alid)
				throw new DataAccessException(0,"rank error5");// 名次错误
		}
		else
		{
			throw new DataAccessException(0,"rank error6");// 名次错误
		}
	}

	/** 检测积分条件 */
	public boolean checkConditionScore(int type,int index,Player player)
	{
		Object[] sa=null;
		if(type==BOSS)
		{
			sa=bossAward;
		}
		else if(type==ELITE)
		{
			sa=eliteAward;
		}
		else if(type==HOSTILE)
		{
			sa=hostileAward;
		}
		else if(type==ARMS)
		{
			sa=armsAward;
		}

		RankerP rp=(RankerP)mapp.get(player.getId());
		int score=rp==null?0:rp.getTypeScore(type);
		// System.out.println("-----index---------:"+index);
		// if(sa!=null)System.out.println("-----sa.length---------:"+sa.length);
		if(sa==null||index*2+1>=sa.length||score<(Integer)sa[index*2])
		{
			return false;
		}
		return true;
	}

	/** 奖励记录 */
	public void markAward(int type,int index,Player player)
	{
		if(type==BOSS||type==ELITE||type==HOSTILE||type==ARMS)
		{
			RankerP rp=(RankerP)mapp.get(player.getId());
			int mark=rp.getTypeMark(type);
			rp.setTypeMark(type,mark|(1<<index));
		}
		else if(type==RANKP)
		{
			markp.add(player.getId());
		}
		else if(type==RANKA)
		{
			marka.add(player.getId());
		}
	}

	/** 领奖检测 */
	public boolean checkMark(int type,int index,Player player)
	{
		RankerP rp=null;
		boolean got=false;
		if(type==BOSS||type==ELITE||type==HOSTILE||type==ARMS)
		{
			rp=(RankerP)mapp.get(player.getId());
			if(rp!=null)
			{
				int mark=rp.getTypeMark(type);
				if((mark&(1<<index))!=0)
				{
					got=true;
				}
			}
		}
		else if(type==RANKP)
		{
			if(markp.contain(player.getId()))
			{
				got=true;
			}
		}
		else if(type==RANKA)
		{
			if(marka.contain(player.getId()))
			{
				got=true;
			}
		}
		else
		{
			got=true;
		}
		return got;
	}

	/** 初始化数据 */
	public void initData(String initdata)
	{
		// 重置
		this.initdata=initdata;
		bossAward=null;
		eliteAward=null;
		hostileAward=null;
		armsAward=null;
		rankpAward=null;
		rankaAward=null;
		// 设置数据
		String[] d1=TextKit.split(initdata,"&");
		for(int i=0;i<d1.length;i++)
		{
			int type=i+1;
			if(type==RANKP||type==RANKA)
			{
				initRank(d1[i],type);
			}
			else
			{
				initScore(d1[i],type);
			}
		}
	}

	/** 初始化积分数据 */
	public void initScore(String initdata,int type)
	{
		if(initdata.equals("null")) return;
		// 100:0,41202,1|200:0,2022,5
		String[] d1=TextKit.split(initdata,"|");
		Object[] objs=new Object[d1.length*2];
		for(int i=0;i<d1.length;i++)
		{
			Award award=(Award)Award.factory.newSample(em_sid);
			int h=d1[i].indexOf(",");
			int[] hd1=TextKit.parseIntArray(TextKit.split(
				d1[i].substring(0,h),":"));
			objs[2*i]=hd1[0];
			objs[2*i+1]=award;
			award.setGemsAward(hd1[1]);
			int[] sids=TextKit.parseIntArray(TextKit.split(
				d1[i].substring(h+1),","));
			initAward(award,sids);
		}
		if(type==BOSS)
		{
			bossAward=objs;
		}
		else if(type==ELITE)
		{
			eliteAward=objs;
		}
		else if(type==HOSTILE)
		{
			hostileAward=objs;
		}
		else if(type==ARMS)
		{
			armsAward=objs;
		}
	}
	/** 初始化排行数据 */
	public void initRank(String initdata,int type)
	{
		// 1-1:500,41201,6,902,50|2-2:400,41201,5,902,35
		String[] d1=TextKit.split(initdata,"|");
		Object[] objs=new Object[d1.length*3];
		for(int i=0;i<d1.length;i++)
		{
			Award award=(Award)Award.factory.newSample(em_sid);
			int h=d1[i].indexOf(",");
			String[] hd1=TextKit.split(d1[i].substring(0,h),":");
			int gem=TextKit.parseInt(hd1[1]);
			String[] hd2=TextKit.split(hd1[0],"-");
			int[] rank=TextKit.parseIntArray(hd2);
			objs[3*i]=rank[0];
			objs[3*i+1]=rank[1];
			objs[3*i+2]=award;
			award.setGemsAward(gem);
			int[] sids=TextKit.parseIntArray(TextKit.split(
				d1[i].substring(h+1),","));
			initAward(award,sids);
		}
		if(type==RANKP)
		{
			rankpAward=objs;
			psize=(Integer)rankpAward[rankpAward.length-2];
		}
		else if(type==RANKA)
		{
			rankaAward=objs;
			asize=(Integer)rankaAward[rankaAward.length-2];
		}
	}
	/** 组装奖励品 */
	public void initAward(Award award,int[] sids)
	{
		IntList prop=new IntList();
		IntList ship=new IntList();
		IntList equip=new IntList();
		IntList officer=new IntList();
		for(int k=0;k<sids.length;k+=2)
		{
			int ptype=SeaBackKit.getSidType(sids[k]);
			if(ptype==Prop.PROP)
			{
				prop.add(sids[k]);
				prop.add(sids[k+1]);
			}
			else if(ptype==Prop.SHIP)
			{
				ship.add(sids[k]);
				ship.add(sids[k+1]);
			}
			else if(ptype==Prop.EQUIP)
			{
				equip.add(sids[k]);
				equip.add(sids[k+1]);
			}
			else if(ptype==Prop.OFFICER)
			{
				officer.add(sids[k]);
				officer.add(sids[k+1]);
			}
		}
		if(prop.size()>0)
		{
			award.setPropSid(prop.toArray());
		}
		if(ship.size()>0)
		{
			award.setShipSids(ship.toArray());
		}
		if(equip.size()>0)
		{
			award.setEquipSids(equip.toArray());
		}
		if(officer.size()>0)
		{
			award.setOfficerSids(officer.toArray());
		}
	}

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		init(factoty);
		return resetActivity(stime,etime,initData,factoty);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime)+awardTime;
		initData(initData);
//		JBackKit.sendWarManicAll(this,factory);
		return getActivityState();
	}

	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append(",\"others\":\"").append(initdata).append("\"")
			.append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		init(factoty);
		initdata=data.readUTF();
		initData(initdata);
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			RankerP rp=new RankerP();
			rp.bytesRead(data);
			mapp.put(rp.getId(),rp);
			resetRankp(rp);
		}
		len=data.readInt();
		for(int i=0;i<len;i++)
		{
			RankerA ra=new RankerA();
			ra.bytesRead(data);
			mapa.put(ra.getId(),ra);
			resetRanka(ra);
		}
		len=data.readInt();
		for(int i=0;i<len;i++)
		{
			markp.add(data.readInt());
		}
		len=data.readInt();
		for(int i=0;i<len;i++)
		{
			marka.add(data.readInt());
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeUTF(initdata);
		int[] keys=mapp.keyArray();
		data.writeInt(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			((RankerP)mapp.get(keys[i])).bytesWrite(data);
		}
		keys=mapa.keyArray();
		data.writeInt(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			((RankerA)mapa.get(keys[i])).bytesWrite(data);
		}
		data.writeInt(markp.size());
		for(int i=0;i<markp.size();i++)
		{
			data.writeInt(markp.get(i));
		}
		data.writeInt(marka.size());
		for(int i=0;i<marka.size();i++)
		{
			data.writeInt(marka.get(i));
		}
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendWarManicAll(this,smap);
	}

	@Override
	public boolean isSave()
	{
		return TimeKit.getSecondTime()>saveTime+saveCD;
	}

	@Override
	public void setSave()
	{
		saveTime=TimeKit.getSecondTime();
	}

	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		super.showByteWriteNew(data,player,objfactory);
		data.writeShort(ActivityContainer.WAR_MANIC_ID);// sid
		data.writeInt(startTime);//
		data.writeInt(endTime-awardTime);//
		data.writeInt(endTime);//
		data.writeInt(getPlayerScore(RANKP,player));
		data.writeInt(getAllianceScore(player));

		int top=data.top();
		int len=0;
		data.writeByte(len);
		String[] d1=TextKit.split(initdata,"&");
		for(int i=0;i<4&&i<d1.length;i++)
		{
			if(d1[i].equals("null")) continue;
			len++;
			data.writeByte(i+1);
			data.writeInt(getPlayerScore(i+1,player));
			String desc=InterTransltor.getInstance().getTransByKey(
				PublicConst.SERVER_LOCALE,"warmanic_"+(i+1));
			data.writeUTF(desc);// 描述
			Object[] objs=getAward(i+1);
			data.writeShort(objs.length/2);
			// boss积分奖励条件+奖励(value,award...)
			for(int k=0;k<objs.length;k+=2)
			{
				data.writeShort(k/2);
				data.writeInt(k/2+1);
				data.writeInt(k/2+1);
				data.writeInt((Integer)objs[k]);
				data.writeByte(getAwardState(i+1,k/2,player));
				((Award)objs[k+1]).viewAward(data,player);
			}
		}
		if(len>0)
		{
			int nowtop=data.top();
			data.setTop(top);
			data.writeByte(len);
			data.setTop(nowtop);
		}
	}

	/** 序列化排名奖励 0个人排名 1联盟排名 */
	public void showBytesWriteAward(int type,Object[] awards,
		ByteBuffer data,Player player)
	{
		data.writeByte(type);
		data.writeShort(awards.length/3);
		for(int i=0;i<awards.length;i+=3)
		{
			data.writeShort(i/3);
			data.writeInt((Integer)awards[i]);
			data.writeInt((Integer)awards[i+1]);
			data.writeInt(0);// 无意义项？
			data.writeByte(0);// 领取状态？
			((Award)awards[i+2]).viewAward(data,player);
		}
	}
	/** 序列化个人排名 */
	public void showBytesWriteRankP(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		showBytesWriteAward(0,rankpAward,data,player);
		Object[] objs=rankp.getArray();
		int top=data.top();
		boolean haveP=false;
		data.writeShort(objs.length);
		for(int i=0;i<objs.length;i++)
		{
			RankerP rp=(RankerP)objs[i];
			Player p=objfactory.getPlayerById(rp.getId());
			if(rp.getId()==player.getId()) haveP=true;
			data.writeUTF(p.getName());
			data.writeInt(rp.getAllScore());
			data.writeByte(getRankAwardState(0,rp,player));
		}
		if(!haveP)
		{
			int nowTop=data.top();
			data.setTop(top);
			data.writeShort(objs.length+1);
			data.setTop(nowTop);
			data.writeUTF(player.getName());
			RankerP rp=(RankerP)mapp.get(player.getId());
			data.writeInt(rp==null?0:rp.getAllScore());
			data.writeByte(0);
		}
	}

	/** 序列化联盟排名 */
	public void showBytesWriteRankA(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		showBytesWriteAward(1,rankaAward,data,player);
		Object[] objs=ranka.getArray();
		int top=data.top();
		boolean haveA=false;
		String alid=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(alid==null||alid.equals(""))
		{
			haveA=true;
		}
		Alliance pa=null;
		int palid=0;
		if(!haveA)
		{
			palid=TextKit.parseInt(alid);
			pa=objfactory.getAlliance(palid,false);
		}
		if(pa==null) haveA=true;
		data.writeShort(objs.length);
		for(int i=0;i<objs.length;i++)
		{
			RankerA ra=(RankerA)objs[i];
			Alliance al=objfactory.getAlliance(ra.getId(),false);
			if(palid==ra.getId()) haveA=true;
			data.writeUTF(al.getName());
			data.writeInt(ra.getScore());
			data.writeByte(getRankAwardState(1,ra,player));
		}
		if(!haveA)
		{
			int nowTop=data.top();
			data.setTop(top);
			data.writeShort(objs.length+1);
			data.setTop(nowTop);
			data.writeUTF(pa.getName());
			RankerA ra=(RankerA)mapa.get(palid);
			data.writeInt(ra==null?0:ra.getScore());
			data.writeByte(0);
		}
	}
	/** 获取排名领取状态 0个人 1联盟 */
	public int getRankAwardState(int type,Object ranker,Player player)
	{
		// 领奖时间判断
		if(TimeKit.getSecondTime()+awardTime<endTime) return 0;
		if(type==0)
		{
			RankerP rp=(RankerP)ranker;
			if(rp.getId()!=player.getId()) return 0;
			if(markp.contain(player.getId())) return 2;
			return 1;
		}
		else
		{
			String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
			if(aid==null||aid.equals("")) return 0;
			int alid=TextKit.parseInt(aid);
			RankerA ra=(RankerA)ranker;
			if(ra.getId()!=alid) return 0;
			if(marka.contain(player.getId())) return 2;
			return 1;
		}
	}

	/** 获取奖励领取状态 0--不可领取，1-－可领取，2-－已领取 */
	public int getAwardState(int type,int index,Player player)
	{
		if(checkMark(type,index,player)) return 2;
		if(checkConditionScore(type,index,player)) return 1;
		return 0;
	}
	/** 获取个人总分 */
	public int getPlayerScore(int type,Player player)
	{
		RankerP rp=(RankerP)mapp.get(player.getId());
		if(rp==null) return 0;
		return rp.getTypeScore(type);

	}
	/** 获取联盟总分 */
	public int getAllianceScore(Player player)
	{
		String aid=player.getAttributes(PublicConst.ALLIANCE_ID);
		if(aid==null||aid.equals("")) return 0;
		RankerA ra=(RankerA)mapa.get(TextKit.parseInt(aid));
		if(ra==null) return 0;
		return ra.getScore();
	}
	/** 活动是否处于激活 */
	public boolean isActive(int nowTime)
	{
		return endTime>nowTime+awardTime&&nowTime>=startTime;
	}

	@Override
	public Object copy(Object obj)
	{
		WarManicActivity act=(WarManicActivity)super.copy(obj);
		act.rankp=new ObjectArray();
		act.ranka=new ObjectArray();
		act.mapp=new IntKeyHashMap();
		act.mapa=new IntKeyHashMap();
		act.markp=new IntList();
		act.marka=new IntList();
		return act;
	}

	/** 个人积分对象 */
	class RankerP
	{

		/** 玩家id */
		int id;
		/** boss积分 */
		int boss;
		/** 联盟精英积分 */
		int elite;
		/** 敌对积分 */
		int hostile;
		/** 军备积分 */
		int arms;
		// 奖励领取记录
		int mark_boss;
		int mark_elite;
		int mark_hostile;
		int mark_arms;

		public void bytesWrite(ByteBuffer data)
		{
			data.writeInt(id);
			data.writeInt(boss);
			data.writeInt(elite);
			data.writeInt(hostile);
			data.writeInt(arms);
			data.writeInt(mark_boss);
			data.writeInt(mark_elite);
			data.writeInt(mark_hostile);
			data.writeInt(mark_arms);
		}

		public void bytesRead(ByteBuffer data)
		{
			id=data.readInt();
			boss=data.readInt();
			elite=data.readInt();
			hostile=data.readInt();
			arms=data.readInt();
			mark_boss=data.readInt();
			mark_elite=data.readInt();
			mark_hostile=data.readInt();
			mark_arms=data.readInt();
		}
		public RankerP()
		{

		}
		public RankerP(int id)
		{
			this.id=id;
		}

		public int getAllScore()
		{
			return boss+elite+hostile+arms;
		}

		public void addTypeScore(int type,int score)
		{
			if(type==BOSS)
			{
				setBoss(addScore(boss,score));
			}
			else if(type==ELITE)
			{
				setElite(addScore(elite,score));
			}
			else if(type==HOSTILE)
			{
				setHostile(addScore(hostile,score));
			}
			else if(type==ARMS)
			{
				setArms(addScore(arms,score));
			}
		}

		public int getTypeScore(int type)
		{
			if(type==BOSS)
			{
				return getBoss();
			}
			else if(type==ELITE)
			{
				return getElite();
			}
			else if(type==HOSTILE)
			{
				return getHostile();
			}
			else if(type==ARMS)
			{
				return getArms();
			}
			else if(type==RANKP)
			{
				return getAllScore();
			}
			return 0;
		}

		public int addScore(int org,int score)
		{
			if(score<=0) return org;
			return org+score;
		}

		public int getTypeMark(int type)
		{
			int mark=Integer.MAX_VALUE;
			if(type==BOSS)
			{
				mark=getMark_boss();
			}
			else if(type==ELITE)
			{
				mark=getMark_elite();
			}
			else if(type==HOSTILE)
			{
				mark=getMark_hostile();
			}
			else if(type==ARMS)
			{
				mark=getMark_arms();
			}
			return mark;
		}

		public void setTypeMark(int type,int mark)
		{
			if(type==BOSS)
			{
				setMark_boss(mark);
			}
			else if(type==ELITE)
			{
				setMark_elite(mark);
			}
			else if(type==HOSTILE)
			{
				setMark_hostile(mark);
			}
			else if(type==ARMS)
			{
				setMark_arms(mark);
			}
		}

		public int getId()
		{
			return id;
		}

		public void setId(int id)
		{
			this.id=id;
		}

		public int getBoss()
		{
			return boss;
		}

		public void setBoss(int boss)
		{
			this.boss=boss;
		}

		public int getElite()
		{
			return elite;
		}

		public void setElite(int elite)
		{
			this.elite=elite;
		}

		public int getHostile()
		{
			return hostile;
		}

		public void setHostile(int hostile)
		{
			this.hostile=hostile;
		}

		public int getArms()
		{
			return arms;
		}

		public void setArms(int arms)
		{
			this.arms=arms;
		}

		public int getMark_boss()
		{
			return mark_boss;
		}

		public void setMark_boss(int mark_boss)
		{
			this.mark_boss=mark_boss;
		}

		public int getMark_elite()
		{
			return mark_elite;
		}

		public void setMark_elite(int mark_elite)
		{
			this.mark_elite=mark_elite;
		}

		public int getMark_hostile()
		{
			return mark_hostile;
		}

		public void setMark_hostile(int mark_hostile)
		{
			this.mark_hostile=mark_hostile;
		}

		public int getMark_arms()
		{
			return mark_arms;
		}

		public void setMark_arms(int mark_arms)
		{
			this.mark_arms=mark_arms;
		}

	}

	/** 联盟积分对象 */
	class RankerA
	{

		/** 联盟id */
		int id;
		/** 积分 */
		int score;

		public void bytesWrite(ByteBuffer data)
		{
			data.writeInt(id);
			data.writeInt(score);
		}

		public void bytesRead(ByteBuffer data)
		{
			id=data.readInt();
			score=data.readInt();
		}

		public RankerA()
		{

		}

		public RankerA(int id)
		{
			this.id=id;
		}

		public void addScore(int score)
		{
			if(score<=0) return;
			this.score+=score;
		}

		public int getId()
		{
			return id;
		}

		public void setId(int id)
		{
			this.id=id;
		}

		public int getScore()
		{
			return score;
		}

		public void setScore(int score)
		{
			this.score=score;
		}

	}

	class compareRankerP implements Comparator
	{

		@Override
		public int compare(Object o1,Object o2)
		{
			if(o1==null) return Comparator.COMP_GRTR;
			if(o2==null) return Comparator.COMP_LESS;
			RankerP rp1=(RankerP)o1;
			RankerP rp2=(RankerP)o2;
			if(rp1.getAllScore()>=rp2.getAllScore())
			{
				return Comparator.COMP_LESS;
			}
			return Comparator.COMP_GRTR;
		}

	}

	class compareRankerA implements Comparator
	{

		@Override
		public int compare(Object o1,Object o2)
		{
			if(o1==null) return Comparator.COMP_GRTR;
			if(o2==null) return Comparator.COMP_LESS;
			RankerA rp1=(RankerA)o1;
			RankerA rp2=(RankerA)o2;
			if(rp1.getScore()>=rp2.getScore())
			{
				return Comparator.COMP_LESS;
			}
			return Comparator.COMP_GRTR;
		}

	}
	
	public ObjectArray getRanka()
	{
		return ranka;
	}
	
	public void setRanka(ObjectArray ranka)
	{
		this.ranka=ranka;
	}
	
	public IntKeyHashMap getMapa()
	{
		return mapa;
	}
	
	public void setMapa(IntKeyHashMap mapa)
	{
		this.mapa=mapa;
	}

	
	
}
