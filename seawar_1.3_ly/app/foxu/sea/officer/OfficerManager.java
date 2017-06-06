package foxu.sea.officer;

import mustang.math.MathKit;
import mustang.net.Connect;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import mustang.util.SampleFactory;
import mustang.util.TimeKit;
import shelby.ds.DSManager;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.ContextVarManager;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Ship;
import foxu.sea.award.Award;
import foxu.sea.fight.Fleet;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.effect.UnitedEffect;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.task.TaskEventExecute;
import foxu.sea.uid.UidKit;

/**
 * ���ٹ��ܹ�����
 * 
 * @author Alan
 */
public class OfficerManager implements TimerListener
{
	/**�յ�ˢ�µ����ĵı�ʯ����**/
	public static int  FLUSH_SHOP_GEMS=20;
	/**���ٳ齱����֧��**/
	public static int OFFICER_PRO_1=3149,OFFICER_PRO_2=3150;
	/** ���١���Ƭ�������� */
	public static SampleFactory factory=new SampleFactory();
	/** ����Ӱ�켼�ܸ��ʡ������Ͷ�Ӧ�ļ���sid{{��������},{ȼ�յ�},...} */
	public static ArrayList SKILL_TYPE_SIDS;
	static OfficerManager manager=new OfficerManager();
	/**������Ʒ����   ��Ʒ PRO_TYPE=1   ���� OFFCER_TYPE=2**/
	public static int PRO_TYPE=1,OFFCER_TYPE=2;
	/**��ʯ����   ͨ��2��������**/
	public static int BUY_BYGEMS=1,BUY_BYCOINS=2;
	/**�̵�ĳ���**/
	public static int SHOP_LENGHT=8;
	/**Ĭ��ϡ�ж�**/
	public static int SCARCITY=2;
	/**���ʱ��**/
	public  static int TIME=5000;
	/**Ĭ�ϳ��ִ���**/
	public static int TIMES=2;
	/** ��ϼ��������� */
	public static SampleFactory effectFactory=new SampleFactory();
	/** ���ݻ�ȡ�� */
	CreatObjectFactory objectFactory;
	/** UID�ṩ�� */
	UidKit uidkit;
	int[] lowDrawProba;
	ArrayList lowDrawInfo;
	int[] highDrawProba;
	ArrayList highDrawInfo;
	/**�����̵���Ϣ   sid ��Ʒ���� ��ʯ 2������**/
	IntList offcerShop=new IntList();
	/**ϵͳˢ��ʱ��**/
	int systemFTime; 
	public static OfficerManager getInstance()
	{
		return manager;
	}

	public void init()
	{
		if(SKILL_TYPE_SIDS==null)
		{
			SKILL_TYPE_SIDS=new ArrayList();
			for(int i=0;i<PublicConst.SKILL_TYPE_SIDS.length;i++)
			{
				SKILL_TYPE_SIDS.add(TextKit.parseIntArray(TextKit.split(
					PublicConst.SKILL_TYPE_SIDS[i],",")));
			}
		}
		if(offcerShop==null || offcerShop.size()==0)
		{
			initOffcerShop();
		}
	}

	/**��ʼ�������̵�**/
	public void initOffcerShop()
	{
		for(int i=0;i<PublicConst.OFFCER_SHOP_SIDS.length;i+=4)
		{
			int sid=PublicConst.OFFCER_SHOP_SIDS[i];
			int type=0;
			int gems=0;
			int costCoins=0;
			OfficerFragment of=(OfficerFragment)OfficerManager.factory
				.newSample(sid);
			if(of==null)
			{
				Prop prop=(Prop)Prop.factory.newSample(sid);
				if(prop!=null)
				{
					type=PRO_TYPE;
					costCoins=prop.getCoins();
					gems=prop.getNeedGems();
				}
			}
			else
			{
				type=OFFCER_TYPE;
				costCoins=of.getCoins();
				gems=of.getCostGems();
			}
			if(type==0) continue;
			offcerShop.add(sid);
			offcerShop.add(type);
			offcerShop.add(gems);
			offcerShop.add(costCoins);
		}
		//����ĳЩϡ�жȵ���Ƭ�������
		String reslut=ContextVarManager.getInstance().getVarDest(
			ContextVarManager.SAVE_OFFCER_SHOP_LIMIT);
		if(reslut==null)
		{
			ContextVarManager.getInstance().setVarDest(
				ContextVarManager.SAVE_OFFCER_SHOP_LIMIT,SCARCITY+","+TIMES);
		}
		if(systemFTime==0)
		{
			String time=ContextVarManager.getInstance().getVarDest(
				ContextVarManager.SAVE_OFFCER_SHOP_TIME);
			if(time==null||time.length()==0 || time.equals("0"))
			{
				int timeNight=SeaBackKit.getTimesnight()-PublicConst.DAY_SEC;
				while(timeNight<TimeKit.getSecondTime())
				{
					timeNight=timeNight+PublicConst.DAY_SEC/8;
				}
				ContextVarManager.getInstance().setVarDest(
					ContextVarManager.SAVE_OFFCER_SHOP_TIME,
					timeNight+"");
				systemFTime=timeNight;
			}
			else
				systemFTime=TextKit.parseInt(time);
		}
		TimerCenter.getSecondTimer().add(
			new TimerEvent(this,"offcerShop",TIME));
	}
	
	public void initFragDraw()
	{
		if(lowDrawProba!=null&&lowDrawInfo!=null&&highDrawProba!=null
			&&highDrawInfo!=null) return;
		// �ͼ��齱
		lowDrawProba=new int[PublicConst.OFFICER_FRAG_LOW.length];
		lowDrawInfo=new ArrayList();
		initFragDrawInfo(lowDrawProba,lowDrawInfo,PublicConst.OFFICER_FRAG_LOW);
		// �߼��齱
		highDrawProba=new int[PublicConst.OFFICER_FRAG_HIGH.length];
		highDrawInfo=new ArrayList();
		initFragDrawInfo(highDrawProba,highDrawInfo,PublicConst.OFFICER_FRAG_HIGH);
	}

	private void initFragDrawInfo(int[] drawProba,ArrayList drawInfo,
		String[] initInfo)
	{
		for(int i=0;i<initInfo.length;i++)
		{
			String[] info=TextKit.split(initInfo[i],":");
			drawProba[i]=TextKit.parseInt(info[0]);
			String[] officerProba=TextKit.split(info[1],",");
			drawInfo.add(TextKit.parseIntArray(officerProba));
		}
	}

	/** ��ȡ�ܹ�ѫ */
	public long getTotalFeats(Player player)
	{
		return player.getOfficers().getFeats();
	}

	/** �ϳɾ��� */
	public Officer composeOfficer(int sid,Player player)
	{
		OfficerFragment fragment=player.getOfficers().getOfficerFrag(sid);
		player.getOfficers().incrOfficerFrag(fragment.getSid(),
			fragment.getComposeCount());
		objectFactory.createOfficerTrack(OfficerTrack.REDUCE,
			OfficerTrack.INTO_OFFICER_COMBINE,player.getId(),sid,
			fragment.getComposeCount(),0,
			getOfficerOrFragmentCount(player,sid));
		Officer officer=addOfficer(fragment.getOfficerSid(),player);
		objectFactory.createOfficerTrack(OfficerTrack.ADD,
			OfficerTrack.FROM_FRAG_COMBINE,player.getId(),officer.getSid(),
			1,officer.getId(),
			getOfficerOrFragmentCount(player,officer.getSid()));
		return officer;
	}

	/** ����Ƿ���Ժϳɾ��� */
	public String checkComposeOfficer(int sid,Player player)
	{
		OfficerFragment fragment=player.getOfficers().getOfficerFrag(sid);
		if(fragment==null) return "officer fragment not enough";
		if(fragment.getCount()<fragment.getComposeCount())
			return "officer fragment not enough";
		return null;
	}

	/** ����һ������ */
	public Officer addOfficer(int sid,Player player)
	{
		Officer officer=(Officer)factory.newSample(sid);
		officer.setId(uidkit.getPlusUid());
		player.getOfficers().addOfficer(officer);
		return officer;
	}

	/** ���ټ�����ѫ */
	public Officer awardFeats(Player player,int id,int sid)
	{
		return awardFeats(player,id,sid,PublicConst.OFFICER_MAX_LV);
	}

	/** ���ٸ��ݼ�����ѫ */
	public Officer awardFeats(Player player,int id,int sid,int upLevel)
	{
		Officer officer=player.getOfficers().getOfficer(id,sid);
		int upLevel_=getLevelAddable(player,officer);
		if(upLevel>upLevel_)
			upLevel=upLevel_;
		upLevel_=getLevelFromFeats(player,officer);
		if(upLevel>upLevel_)
			upLevel=upLevel_;
		return awardFeats(player,officer,upLevel);
	}

	/** ������ѫ */
	public Officer awardFeats(Player player,Officer officer,int upLevel)
	{
		int exp=getExpToUpLevel(officer,upLevel);
		if(officer.incrExp(exp)) player.getOfficers().decrFeats(exp);
		return officer;
	}

	/** ���ټ�����ѫ��� */
	public String checkAwardFeats(Player player,int id,int sid,int upLevel)
	{
		Officer officer=player.getOfficers().getOfficer(id,sid);
		if(officer==null) return "officer not exist";
		if(isMaxLevelLimit(officer.getLevel()+upLevel))
			return "officer max level limit";
		if(isMaxMilitaryRank(officer))
			return "officer max rank limit";
		if(isPlayerLevelLimit(officer.getLevel()+upLevel,player))
			return "player level limit";
		if(isRankLevelLimit(officer.getMilitaryRank(),officer.getLevel()+upLevel))
			return "officer rank level limit";
		if(getLevelFromFeats(player,officer)<upLevel) return "feats not enough";
		return null;
	}

	/** ���ټ�����ѫ��� */
	public String checkAwardFeats(Player player,int id,int sid)
	{
		return checkAwardFeats(player,id,sid,1);
	}



	/** ����Ƿ���Ծ������� */
	public String checkPromoteRank(Player player,int id,int sid)
	{
		Officer officer=player.getOfficers().getOfficer(id,sid);
		if(officer==null) return "officer not exist";
		int militaryRank=officer.getMilitaryRank();
		int level=officer.getLevel();
		// �Ƿ񳬹����μ���
		if(militaryRank>=PublicConst.OFFICER_RANK_LV.length)
		{
			officer.setMilitaryRank(PublicConst.OFFICER_RANK_LV.length);
			return "military rank max limit";
		}
		// �Ƿ�ﵽ���ü���
		if(level<PublicConst.OFFICER_RANK_LV[militaryRank-1])
			return "officer level not enough";
		// �����ɵľ����Ƿ��㹻
		if(officer.getRankUpOfficers()>player.getOfficers()
			.getPrimitiveOfficersCount(officer.getSid()))
			return "officer count not enough";
		// ��Դ
		int[] re=officer.getRankUpResource();
		if(!Resources.checkResources(re,player.getResources()))
			return "resource limit";
		return null;
	}

	/** �������� */
	public Officer promoteRank(Player player,int id,int sid,IntList list)
	{
		Officer officer=player.getOfficers().getOfficer(id,sid);
		// ���ɾ���
		player.getOfficers().removePrimitiveOfficers(sid,
			officer.getRankUpOfficers(),list,objectFactory,player);
		// �۳���Դ
		int[] re=officer.getRankUpResource();
		Resources.reduceResources(player.getResources(),re,player);
		Resources.reduceGems(re[5],player.getResources(),player);
		if(re[5]>0)
		{
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.OFFICER_RANK_UP,
				player.getId(),re[5],officer.getId(),
				Resources.getGems(player.getResources()));
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,null,player,re[5]);
		}
		officer.incrMilitaryRank();
		if(officer.getMilitaryRank()>PublicConst.OFFICER_RANK_LV.length)
			officer.setMilitaryRank(PublicConst.OFFICER_RANK_LV.length);
		return officer;
	}

	/** �ͼ��齱 */
	public String fragLowDraw(Player player,IntList list)
	{
		initFragDraw();
		int free=player.getOfficers().getFreeDraw();
		// ��ѳ齱ǿ�Ƽ��ʱ��
		if(free<PublicConst.OFFICER_FRAG_LOW_FREE
			&&getLeftTimeFromLastDraw(player)<=0)
		{
			player.getOfficers().incrFreeDraw();
		}
		else if(player.getBundle().checkDecrProp(PublicConst.OFFICER_FRAG_LOW_PROP[0],
			PublicConst.OFFICER_FRAG_LOW_PROP[1]))
		{
			player.getBundle().decrProp(PublicConst.OFFICER_FRAG_LOW_PROP[0],
				PublicConst.OFFICER_FRAG_LOW_PROP[1]);
			JBackKit.sendResetBunld(player);
		}
		else
			return "frag draw prop not enough";
		int[] info=drawFragment(player,lowDrawProba,lowDrawInfo);
		list.add(info[0]);
		list.add(info[1]);
		return null;
	}

	/** �߼��齱 */
	public String fragHighDraw(Player player,int num,IntList list)
	{
		initFragDraw();
		String msg=reduce(num,player);
		if (msg!=null)	return msg;
		// 10������Ҫ����
		int len=1;
		if(num<10)
			len=0;
		// ����ȫ���
		for(int i=0;i<num-len;i++)
		{
			int[] info=drawFragment(player,highDrawProba,highDrawInfo);
			list.add(info[0]);
			list.add(info[1]);
		}
		// ���׸߼���Ƭ
		if(len>0)
		{
			int[] base=drawFragment(player,PublicConst.OFFICER_FRAG_HIGH_MIN);
			int index=MathKit.randomValue(0,num);
			list.addAt(base[0],2*index);
			list.addAt(base[1],(2*index)+1);
		}
		return null;
	}

	/** Ϊ��ҷ���һ����Ƭ */
	public int[] drawFragment(Player player,int[] fragDraw,ArrayList drawInfo)
	{
		// // ѡ��һ������������ϡ�ж�
		int random=MathKit.randomValue(0,Award.PROB_ABILITY);
		// ��ʼ�Ļ���
		int startProbability=0;
		int i=0;
		for(;i<fragDraw.length;i++)
		{
			if(fragDraw[i]<=0) continue;
			// ���������������Χ��
			if(random>=startProbability&&random<fragDraw[i])
			{
				break;
			}
			startProbability=fragDraw[i];
		}
		int[] sids=(int[])drawInfo.get(i);
		return drawFragment(player,sids);
	}
	
	public int[] drawFragment(Player player,int[] sids)
	{
		// // ѡ��һ����ǰϡ�жȵľ���sid
		int sid=-1;
		int maxCount=0;
		int random=MathKit.randomValue(0,Award.PROB_ABILITY);
		// ��ʼ�Ļ���
		int startProbability=0;
		for(int i=0;i<sids.length;i+=3)
		{
			if(sids[i+2]<=0) continue;
			// ���������������Χ��
			if(random>=startProbability&&random<sids[i+2])
			{
				sid=sids[i];
				maxCount=sids[i+1];
				break;
			}
			startProbability=sids[i+2];
		}
		// ѡ��һ����������
		int num=MathKit.randomValue(0,maxCount)+1;
		player.getOfficers().addOfficerFrag(sid,num);
		objectFactory.createOfficerTrack(OfficerTrack.ADD,
			OfficerTrack.FROM_FRAG_DRAW,player.getId(),sid,num,0,
			getOfficerOrFragmentCount(player,sid));
		int[] info=new int[2];
		info[0]=sid;
		info[1]=num;
		return info;
	}

	/** ��ȡ��Ӧ��ܵ���ȴʱ�� */
	public int getBookReadingCalm(Player player,int sid)
	{
		int len=4;
		int lv=getLibReadIndex(sid);
		int now=TimeKit.getSecondTime();
		int circle=PublicConst.OFFICER_LIB_READ[len*lv+2];
		int lastTime=player.getOfficers().getLibReadTime(lv);
		int cd=lastTime+circle-now;
		return cd<0?0:cd;
	}

	/** �Ķ��鼮 */
	public String readBookFromLib(Player player,int sid)
	{
		int len=4;
		int lv=getLibReadIndex(sid);
		// ��ܵȼ���������ȷ
		if(lv<0||lv*len>PublicConst.OFFICER_LIB_READ.length
			||(lv!=0&&PublicConst.OFFICER_LIB_READ.length%lv!=0))
			return "read sid error";
		int now=TimeKit.getSecondTime();
		// ��֤��������
		int limit=PublicConst.OFFICER_LIB_READ[len*lv+1];
		if(player.getLevel()<limit) return "player level limit";
		// ��֤�Ķ����
		int circle=PublicConst.OFFICER_LIB_READ[len*lv+2];
		int lastTime=player.getOfficers().getLibReadTime(lv);
		if(now<lastTime+circle) return "read need calm down";
		// �������Ļ�ȡ����ֵ����������
		if(getTotalFeats(player)>PublicConst.OFFICER_FEATS_MAX[player
			.getLevel()-1]) return "feats max limit";
		player.getOfficers().setLibReadTime(lv,now);
		player.getOfficers().incrFeats(PublicConst.OFFICER_LIB_READ[len*lv]);
		return null;
	}

	public int getLibReadIndex(int sid)
	{
		int len=4;
		int index=0;
		for(int i=0;i<PublicConst.OFFICER_LIB_READ.length;i+=len)
		{
			if(PublicConst.OFFICER_LIB_READ[i+3]==sid) return index;
			index++;
		}
		return -1;
	}

	/** ������ͨ�鼮 */
	public String makeBookPartOfFeats(Player player,int id,int sid)
	{
		int index=0;
		return makeBookFromOfficer(player,id,sid,index);
	}

	/** �����߼��鼮 */
	public String makeBookWholeFeats(Player player,int id,int sid)
	{
		int index=1;
		return makeBookFromOfficer(player,id,sid,index);
	}

	/** �����鼮 */
	public String makeBookFromOfficer(Player player,int id,int sid,int index)
	{
		int len=7;
		int[] info=new int[len];
		System.arraycopy(PublicConst.OFFICER_LIB_WRITE,index*len,info,0,len);
		String msg=checkMakeBook(player,id,sid,info);
		if(msg!=null) return msg;
		Officer officer=player.getOfficers().getOfficer(id,sid);
		makeBookFromOfficer(player,officer,info);
		return null;
	}

	/** ����Ƿ���������鼮 */
	public String checkMakeBook(Player player,int id,int sid,int[] info)
	{
		Officer officer=player.getOfficers().getOfficer(id,sid);
		if(officer==null) return "officer not exist";
		// �������Ļ�ȡ����ֵ����������
		if(getTotalFeats(player)>PublicConst.OFFICER_FEATS_MAX[player.getLevel()-1])
			return "feats max limit";
		if(!Resources.checkResources(info[1],info[2],info[3],info[4],
			info[5],player.getResources())) return "resource limit";
		if(!Resources.checkGems(info[6],player.getResources()))
			return "not enough gems";
		return null;
	}

	/** �����鼮,����֮ǰӦ���м�� */
	public void makeBookFromOfficer(Player player,Officer officer,int[] info)
	{
		Resources.reduceResources(player.getResources(),info[1],info[2],
			info[3],info[4],info[5],player);
		Resources.reduceGems(info[6],player.getResources(),player);
		if(info[6]>0)
		// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.MAKE_OFFICER_BOOK,
				player.getId(),info[6],officer.getId(),
				Resources.getGems(player.getResources()));
		// ����change��Ϣ
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.GEMS_ADD_SOMETHING,null,player,info[6]);
		int percent=info[0];
		int totalFeats=getTotalFeatsFromOfficer(officer);
		officer.resetLevel();
		// ���������鼮��һ�������������ʱ����������������¼
		objectFactory.createOfficerTrack(OfficerTrack.REDUCE,
			OfficerTrack.INTO_OFFICER_RESET,player.getId(),officer.getSid(),
			1,officer.getId(),
			getOfficerOrFragmentCount(player,officer.getSid()));
		player.getOfficers().incrFeats(totalFeats*percent/100);
	}

	/** ��ȡ�����ܹ����ĵľ��� */
	public int getTotalFeatsFromOfficer(Officer officer)
	{
		int totalFeats=officer.getExp();
		int lv=officer.getLevel();
		for(int i=0;i<lv-1;i++)
		{
			totalFeats+=officer.getLevelExps()[i];
		}
		return totalFeats;
	}
	
	/** ʹ�þ��� */
	public String useOfficer(Player player,int id,int sid,int index)
	{
		Officer officer=player.getOfficers().getOfficer(id,sid);
		if(officer==null) return "officer not exist";
		if(!player.getOfficers().isOfficerIndexLegal(index-1))
			return "index not exist";
		player.getOfficers().useOfficer(officer,index-1);
		return null;
	}
	
	/** ��ս���ٱ䶯ʱ���óǷ������� */
	public void resetMainGroup(Player player)
	{
		Fleet[] fleets=player.getIsland().getMainGroup().getArray();
		if(fleets==null) return;
		// �����ṩ�Ķ��������
		int comShipNum=(int)player
			.getIsland()
			.getMainGroup()
			.getOfficerFleetAttr()
			.getCommonAttr(OfficerBattleHQ.ARMY,Ship.ALL_SHIP,
				PublicConst.EXTRA_SHIP,true,0);
		int locationExtra=0;
		for(int i=0;i<fleets.length;i++)
		{
			if(fleets[i]==null) continue;
			// �����ṩ�Ŀ�λ���������
			locationExtra=(int)player
				.getIsland()
				.getMainGroup()
				.getOfficerFleetAttr()
				.getCommonAttr(OfficerBattleHQ.ARMY,
					OfficerBattleHQ.CURRENT_LOCATION,PublicConst.EXTRA_SHIP,
					true,fleets[i].getLocation());
			if(fleets[i].getNum()>player.getShipNum()+comShipNum
				+locationExtra)
			{
				int left=fleets[i].getNum()-(player.getShipNum()+comShipNum
					+locationExtra);
				fleets[i].initNum(player.getShipNum()+comShipNum
					+locationExtra);
				player.getIsland().addTroop(fleets[i].getShip().getSid(),
					left,player.getIsland().getTroops());
			}
		}
	}

	/** ��ɢȫ������ */
	public void clearUsingOfficer(Player player)
	{
		player.getOfficers().clearUsingOfficer();
	}

	/** ��ɢ���� */
	public String restOfficer(Player player,int index)
	{
		if(!player.getOfficers().isOfficerIndexLegal(index-1))
			return "index not exist";
		player.getOfficers().restOfficer(index-1);
		return null;
	}

	/** ����������Ҫ�ľ��� */
	public int getExpToUpLevel(Officer officer,int upLevel)
	{
		int level=officer.getLevel();
		int[] levelExps=officer.getLevelExps();
		if(upLevel<1) return 0;
		int totalExp=0;
		for(int i=level;i<level+upLevel;i++)
		{
			totalExp+=levelExps[i-1];
		}
		return totalExp-officer.getExp();
	}

	/** ����س���ʱ���������ļ��޼��� */
	public int getLevelAddable(Player player,Officer officer)
	{
		// ���������ļ���
		int len=0;
		int militaryRank=officer.getMilitaryRank();
		int level=officer.getLevel();
		// �Ƿ񳬹����μ���
		if(!isMaxMilitaryRank(officer))
		{
			// �Ƿ񳬹���ɫ����
			int max=PublicConst.OFFICER_RANK_LV[militaryRank-1];
			if(isPlayerLevelLimit(max,player))
				max=player.getLevel();
			if(isMaxLevelLimit(max))
				max=PublicConst.OFFICER_MAX_LV;
			len=max-level;
		}
		return len;
	}
	
	public boolean isRankLevelLimit(int rank,int lv)
	{
		if(lv>PublicConst.OFFICER_RANK_LV[rank-1]) return true;
		return false;
	}

	public boolean isPlayerLevelLimit(int lv,Player player)
	{
		if(player.getLevel()<lv) return true;
		return false;
	}
	
	public boolean isMaxLevelLimit(int lv)
	{
		if(lv>PublicConst.OFFICER_MAX_LV) return true;
		return false;
	}

	public boolean isMaxMilitaryRank(Officer officer)
	{
		// �Ƿ񳬹����μ���
		if(officer.getMilitaryRank()<PublicConst.OFFICER_RANK_LV.length)
			return false;
		return true;
	}

	/** �ܾ���ؿ��������ļ��޼��� */
	public int getLevelFromFeats(Player player,Officer officer)
	{
		int level=officer.getLevel();
		// ��ǰ������ܹ������ļ���
		long feats=getTotalFeats(player)+officer.getExp();
		int[] levelExps=officer.getLevelExps();
		int totalLv=0;
		for(int i=level-1;i<levelExps.length;i++)
		{
			feats-=levelExps[i];
			if(feats<0) break;
			totalLv++;
		}
		if(level+totalLv>PublicConst.OFFICER_MAX_LV)
			totalLv=PublicConst.OFFICER_MAX_LV-level;
		return totalLv;
	}
	
	public String checkDisbandOfficer(Player player,int id,int sid)
	{
		Officer officer=player.getOfficers().getOfficer(id,sid);
		if(!player.getOfficers().isInitLevelUnusedOfficer(officer))
			return "officer increased feats or level";
		return null;
	}
	
	/** ǲɢ����,propListд���ȡ����Ʒ */
	public Officer disbandOfficer(Player player,int id,int sid,IntList propList)
	{
		Officer officer=player.getOfficers().removeOfficer(id,sid);
		if(officer==null)
			return null;
		objectFactory.createOfficerTrack(OfficerTrack.REDUCE,
			OfficerTrack.INTO_OFFICER_DISBAND,player.getId(),
			officer.getSid(),1,officer.getId(),
			getOfficerOrFragmentCount(player,officer.getSid()));
		int feats=officer.disbandFeats();
		int[] props=officer.disbandProps();
		int coins=officer.getDisbandCoin();
		//����2������
		player.getOfficers().incrCoins(coins);
		objectFactory.createCoinsTrack(CoinsTrack.FORM_DIS_OFFICER,
			player.getId(),officer.getSid(),coins,
			(int)player.getOfficers().getCoins(),
			CoinsTrack.ADD);
		player.getOfficers().incrFeats(feats);
		if(props!=null)
		{
			for(int i=0;i<props.length;i+=2)
			{
				NormalProp prop=(NormalProp)Prop.factory.newSample(props[i]);
				prop.setCount(props[i+1]);
				player.getBundle().incrProp(prop,true);
				int j=0;
				for(;j<propList.size();j+=2)
				{
					if(propList.get(j)==props[i])
					{
						propList.set(propList.get(j+1)+props[i+1],j+1);
						break;
					}
				}
				if(j<propList.size())
				{
					propList.add(props[i]);
					propList.add(props[i+1]);
				}
			}
		}
		return officer;
	}
	
	public Officer getOfficer(Player player,int id,int sid)
	{
		return player.getOfficers().getOfficer(id,sid);
	}

	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public UidKit getUidkit()
	{
		return uidkit;
	}

	public void setUidkit(UidKit uidkit)
	{
		this.uidkit=uidkit;
	}

	/** ���Ӿ���ϵͳ��� */
	public void addOfficerOrFragment(Player player,int sid,int num)
	{
		if(factory.getSample(sid) instanceof Officer)
		{
			for(int i=0;i<num;i++)
				addOfficer(sid,player);
		}
		else
		{
			player.getOfficers().addOfficerFrag(sid,num);
		}
	}
	
	/** ��ȡ����ϵͳ������� */
	public int getOfficerOrFragmentCount(Player player,int sid)
	{
		if(factory.getSample(sid) instanceof Officer)
		{
			return player.getOfficers().getOfficerCount(sid);
		}
		else
		{
			return player.getOfficers().getFragCount(sid);
		}
	}

	/** ��ȡ��������Ч�� */
	public ArrayList getEffectsFromOfficers(Player player)
	{
		return getEffectsFromOfficers(player.getOfficers()
				.getUsingOfficers());
	}
	
	/** ��ȡ��������Ч�� */
	public ArrayList getEffectsFromOfficers(Officer[] officers)
	{
		Object[] effects=effectFactory.getSamples();
		UnitedEffect effect;
		ArrayList list=new ArrayList();
		for(int i=1;i<effects.length;i++)
		{
			if(effects[i]==null||!(effects[i] instanceof UnitedEffect)) continue;
			effect=(UnitedEffect)effectFactory
				.newSample(((UnitedEffect)effects[i]).getSid());
			if(effect.isEffectAvailable(officers)) list.add(effect);
		}
		return list;
	}

	/** ��ȡ�´���ѳ齱�ĵ���ʱ */
	public int getLeftTimeFromLastDraw(Player player)
	{
		return player.getOfficers().getLeftTimeFromLastDraw(
			PublicConst.FREE_DRAW_LIMIT_TIME,TimeKit.getSecondTime());
	}
	
	public boolean isSystemGuideComplete(Player player)
	{
		String[] infos=getSystemGuide(player);
		if(infos==null)
			return false;
		return Boolean.valueOf(infos[0]);
	}
	
	public int getSystemGuideMark(Player player)
	{
		String[] infos=getSystemGuide(player);
		if(infos==null)
			return 0;
		return Integer.valueOf(infos[1]);
	}
	
	public String[] getSystemGuide(Player player)
	{
		String info=player.getAttributes(PublicConst.OFFICER_SYSTEM_GUIDE);
		if(info==null||"".equals(info))
			return null;
		return TextKit.split(info,",");
	}
	
	public void setSystemGuide(Player player,boolean isComplete,int mark,
		String id)
	{
		if(id==null)
			id="";
		else
			id=","+id;
		player.setAttribute(PublicConst.OFFICER_SYSTEM_GUIDE,
			String.valueOf(isComplete)+","+String.valueOf(mark)+id);
	}
	
	/** ���������齱 */
	public String systemGuideDraw(Player player,int num,IntList list)
	{
		for(int i=0;i<num;i++)
		{
			int sid=PublicConst.OFFICER_FRAG_GUIDE_DRAW[2*i];
			int count=PublicConst.OFFICER_FRAG_GUIDE_DRAW[2*i+1];
			player.getOfficers().addOfficerFrag(sid,count);
			list.add(sid);
			list.add(count);
		}
		return null;
	}
	
	/** ��ǰ�����Ƿ��ڳ���״̬ */
	public boolean isUsedOfficer(Player player,Officer officer)
	{
		if(officer==null) return false;
		Officer[] us=player.getOfficers().getUsingOfficers();
		if(us==null) return false;
		for(int i=0;i<us.length;i++)
		{
			if(us[i]==null) continue;
			if(us[i].getId()==officer.getId()) return true;
		}
		return false;
	}
	
	
	/**��������̵�**/
	public  void randomPlayerOffcerShop(Player player)
	{
		//ϡ�ж�
		int offcerScarcity=0;
		//��ͳ��ִ���
		int offcerTimes=0;
		//��ʶ ��Ҫ���⴦����Ƭ
		boolean  loop=false;
		ArrayList list=new ArrayList();
		int[] rdIndex=null;
		String result=ContextVarManager.getInstance().getVarDest(
			ContextVarManager.SAVE_OFFCER_SHOP_LIMIT);
		if(result!=null)
		{
			String[] str=TextKit.split(result,",");
			offcerScarcity=TextKit.parseInt(str[0]);
			offcerTimes=TextKit.parseInt(str[1]);
			if(offcerScarcity!=0 && offcerTimes!=0)
				loop=true;
		}
		if(loop)
		{
			rdIndex=randomIndex(offcerTimes);
			loop=false;
		}
		//����ѭ����ͨ����������
		/**������Ʒ**/
		for(int i=0;i<SHOP_LENGHT;i++)
		{
			if(rdIndex!=null && SeaBackKit.isContainValue(rdIndex,i))
				loop=true;
			OfficerShop shop=new OfficerShop();
			int [] array=getRandomGood(loop,offcerScarcity,list);
				loop=false;
			shop.setSid(offcerShop.get(array[0]));
			//��Ʒ����
			shop.setShopType(offcerShop.get(array[0]+1));
			int num=getShopNum(); 
			if(num==0)
				shop.setGoodsNum(1);
			else
				shop.setGoodsNum(num);
			//��������
			shop.setSaleType(array[1]);
			if(array[1]==BUY_BYGEMS)
				shop.setSalePrice(offcerShop.get(array[0]+2)*num);
			else
				shop.setSalePrice(offcerShop.get(array[0]+3)*num);
			shop.setSaleLimitNum(PublicConst.OFFCER_SHOP_SIDS[array[0]+3]);
			list.add(shop);
		}
		player.getOfficers().setShopList(list);
		player.getOfficers().setRefreshTime(systemFTime);
	}
	
	/**��ȡ��Ʒ������**/
	public  int  getShopType()
	{
		int i=0;
		int random=MathKit.randomValue(0,Award.PROB_ABILITY);
		int startProbability=0;
		for(;i<PublicConst.OFFCER_SHOP_TYPE_LIMIT.length;i+=2)
		{
			if(PublicConst.OFFCER_SHOP_TYPE_LIMIT[i]<=0) continue;
			// ���������������Χ��
			if(random>=startProbability
				&&random<PublicConst.OFFCER_SHOP_TYPE_LIMIT[i])
			{
				return PublicConst.OFFCER_SHOP_TYPE_LIMIT[i+1];
			}
			startProbability=PublicConst.OFFCER_SHOP_TYPE_LIMIT[i];
		}
		return 0;
	}
	
	/**��ȡ����**/
	public  int getIndex(boolean loop,int offcerScarcity)
	{
		int index=-1;
		if(loop)
		{
			while(loop&&index<0)
			{
				index=getOffcerShopIndex(offcerScarcity);
			}
			return index;
		}
		while(index<0)
		{
			index=getOffcerShopIndex(0);
		}
		return index;
	}
	
	public int getOffcerShopIndex(int offcerScarcity)
	{
		int i=0;
		int random=MathKit.randomValue(0,Award.PROB_ABILITY);
		int startProbability=0;
		for(;i<PublicConst.OFFCER_SHOP_SIDS.length;i+=4)
		{
			if(PublicConst.OFFCER_SHOP_SIDS[i+2]<=0) continue;
			// ���������������Χ��
			if(random>=startProbability
				&&random<PublicConst.OFFCER_SHOP_SIDS[i+2])
			{
				if(offcerScarcity!=0
					&&PublicConst.OFFCER_SHOP_SIDS[i+1]!=offcerScarcity)
					return -1;
				return i;
			}
			startProbability=PublicConst.OFFCER_SHOP_SIDS[i+2];
		}
		return -1;
		
	}
	
	
	/**��ȡ��Ʒ������**/
	public  int getShopNum()
	{
		int i=0;
		int random=MathKit.randomValue(0,Award.PROB_ABILITY);
		int startProbability=0;
		for(;i<PublicConst.OFFCER_SHOP_NUM_LIMIT.length;i+=2)
		{
			if(PublicConst.OFFCER_SHOP_NUM_LIMIT[i]<=0) continue;
			// ���������������Χ��
			if(random>=startProbability
				&&random<PublicConst.OFFCER_SHOP_NUM_LIMIT[i])
			{
				return PublicConst.OFFCER_SHOP_NUM_LIMIT[i+1];
			}
			startProbability=PublicConst.OFFCER_SHOP_NUM_LIMIT[i];
		}
		return 0;
	}
	
	/**��⵱ǰ��Ʒ�Ƿ���Լ����̵� type Ĭ��Ϊ��2�����ҹ����Ǳ�ʯ����**/
	public boolean checkProCount(int sid,ArrayList list,int type)
	{
		if(list.size()==0) return false;
		for(int i=0;i<list.size();i++)
		{
			OfficerShop shop=(OfficerShop)list.get(i);
			if(shop.getSid()==sid && shop.getSaleType()==type)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void onTimer(TimerEvent e)
	{
		if(TimeKit.getSecondTime()>systemFTime)
		{
			systemFTime+=PublicConst.DAY_SEC/8;
			ContextVarManager.getInstance().setVarDest(
				ContextVarManager.SAVE_OFFCER_SHOP_TIME,
				systemFTime+"");
			refrushGoods(systemFTime);
		}
	}
	
	
	/**ˢ����Ʒ�б�**/
	public void refrushGoods(int systemFTime)
	{
		DSManager manager=objectFactory.getDsmanager();
		Session sessions[]=manager.getSessionMap().getSessions();
		for(int i=0;i<sessions.length;i++)
		{
			if(sessions[i]==null) continue;
			Connect c=sessions[i].getConnect();
			if(c==null||!c.isActive()) continue;
			Player player=(Player)sessions[i].getSource();
			if(player==null) continue;
			if(!player.getOfficers().checkTime(systemFTime)) continue;
			randomPlayerOffcerShop(player);
			player.getOfficers().setRefreshTime(systemFTime);
			JBackKit.sendRefreshOfficerShop(player);
		}
	}

	/**�����Ʒ**/
	public int[] getRandomGood(boolean loop,int offcerScarcity,ArrayList list)
	{
		int index=getIndex(loop,offcerScarcity);
		int type=getShopType();
		int randomTimes=0;
		while(checkProCount(offcerShop.get(index),list,type))
		{
			if(randomTimes>1000) return new int[]{index,type};
			index=getIndex(loop,offcerScarcity);
			type=getShopType();
			randomTimes++;
		}
		return new int[]{index,type};
	}
	
	/**���ݴ��� ����� ����Ʒ���ֵ�λ��**/
	public int[] randomIndex(int times)
	{
		int[] array=new int[times];
		int count=0;
		for(;;)
		{
			if(count>=times) return array;
			int random=MathKit.randomValue(0,SHOP_LENGHT-1);
			if(!SeaBackKit.isContainValue(array,random))
			{
				array[count]=random;
				count++;
			}
		}
	}
	
	public String reduce(int num,Player player)
	{
		int pnum=player.getBundle().getCountBySid(OFFICER_PRO_1);
		if(pnum>=num)
		{
			player.getBundle().decrProp(OFFICER_PRO_1,num);
			JBackKit.sendResetBunld(player);
			return null;
		}
		if(num>=10)
		{
			pnum=player.getBundle().getCountBySid(OFFICER_PRO_2);
			if(pnum>=1)
			{
				player.getBundle().decrProp(OFFICER_PRO_2,1);
				JBackKit.sendResetBunld(player);
				return null;
			}
		}
		int gems=-1;
		for(int i=0;i<PublicConst.OFFICER_FRAG_HIGH_GEMS.length;i+=2)
		{
			if(num==PublicConst.OFFICER_FRAG_HIGH_GEMS[i])
			{
				gems=PublicConst.OFFICER_FRAG_HIGH_GEMS[i+1];
				break;
			}
		}
		if(gems<0) return "server setting error";
		if(!Resources.checkGems(gems,player.getResources()))
			return "not enough gems";
		Resources.reduceGems(gems,player.getResources(),player);
		// ��ʯ���Ѽ�¼
		objectFactory
			.createGemTrack(GemsTrack.DRAW_OFFICER_FRAG,player.getId(),gems,
				num,Resources.getGems(player.getResources()));
		// ����change��Ϣ
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.GEMS_ADD_SOMETHING,null,player,gems);
		return null;
	}

	public int getSystemFTime()
	{
		return systemFTime;
	}
	
}
