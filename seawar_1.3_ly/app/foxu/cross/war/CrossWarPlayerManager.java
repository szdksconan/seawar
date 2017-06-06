package foxu.cross.war;

import mustang.event.ChangeListener;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.timer.TimerCenter;
import mustang.timer.TimerEvent;
import mustang.timer.TimerListener;
import foxu.cross.server.CrossAct;
import foxu.cross.server.CrossActManager;
import foxu.sea.PublicConst;
import foxu.sea.uid.UidKit;


/**
 * 跨服战玩家管理器
 * @author yw
 *
 */
public class CrossWarPlayerManager implements TimerListener,ChangeListener
{
	/*static fields*/
	private static Logger log=LogFactory.getLogger(CrossWarPlayerManager.class);
	/* fields */
	/** 存库CD */
	int saveCD=60*15;
	
	/** 跨服活动保存信息 管理器 */
	CrossActManager actmanager;
	
	/** 跨服战玩家 sql操作中心 */
	CrossWarPlayerDBAccess warPlayerAccess;
	
	/** 玩家map */
	IntKeyHashMap catche=new IntKeyHashMap();
	/** 保存失败玩家map */
	IntKeyHashMap failcatche;
	
	/** 每次存入上限 */
	int max=1000;
	
	public CrossActManager getActmanager()
	{
		return actmanager;
	}
	
	public void setActmanager(CrossActManager actmanager)
	{
		this.actmanager=actmanager;
	}
	
	public CrossWarPlayerDBAccess getWarPlayerAccess()
	{
		return warPlayerAccess;
	}
	
	public void setWarPlayerAccess(CrossWarPlayerDBAccess warPlayerAccess)
	{
		this.warPlayerAccess=warPlayerAccess;
	}
	/* methods */
	/**初始化*/
	public void init()
	{
		if(!PublicConst.crossServer)return;
		TimerCenter.getMinuteTimer().add(new TimerEvent(this,"save",saveCD*1000));
		// 清理过期活动id的用户 todo
		// 加载有效的用户
		CrossWar cwar=(CrossWar)actmanager.getLastAct(CrossAct.WAR_SID,false);
		if(cwar==null||cwar.isover()) return;
		int match=cwar.getMatch();
		if(match==2)match=1;
		String sql="select * from crosswar_player where warid="+cwar.getId()
			+" and rank>="+match;
		CrossWarPlayer[] warplayers=warPlayerAccess.loadBySql(sql);
		if(warplayers==null) return;
		for(int i=0;i<warplayers.length;i++)
		{
			catche.put(warplayers[i].getCrossid(),warplayers[i]);
		}

	}
	/** 清理 */
	public void clear()
	{
		catche.clear();
	}
	/**添加玩家*/
	public void addPlayer(CrossWarPlayer player)
	{
		player.setSave(true);
		catche.put(player.getCrossid(),player);
//		warPlayerAccess.save(player);
	}
	/** 移除玩家 */
	public void removePlayer(CrossWarPlayer player)
	{
		catche.remove(player.getCrossid());
	}
	/** 获取N强玩家 */
	public CrossWarPlayer[] getEnterPlayers(int sn)
	{
		ArrayList plist=new ArrayList();
		Object[] objs=catche.valueArray();
//		System.out.println(sn+":----------getEnterPlayers------------:"+objs.length);
		if(objs!=null)
		{
			for(int i=0;i<objs.length;i++)
			{
				if(objs[i]==null) continue;
				CrossWarPlayer p=(CrossWarPlayer)objs[i];
				if(sn==0)
				{
					plist.add(p);
				}else if(p.getRank()>0&&p.getRank()<=sn)
				{
					plist.add(p);
				}
			}
		}
		CrossWarPlayer[] cps=new CrossWarPlayer[plist.size()];
//		System.out.println(sn+":----------plist.size------------:"+plist.size());
		for(int i=0;i<cps.length;i++)
		{
			cps[i]=(CrossWarPlayer)plist.get(i);
		}
		return cps;
	}
	/** 创建默认玩家  */
	public CrossWarPlayer[] createDefPlayer(UidKit crossuid,int num)
	{
		return null;
	}
	/**立即保存玩家*/
	public boolean savePlayerNow(CrossWarPlayer player)
	{
		return warPlayerAccess.save(player);
	}
	/** 保存玩家  */
	public int savePlayers(boolean force)
	{
		return savePlayer(catche,force);
	}
	/** 定时保存玩家 */
	public int savePlayer(IntKeyHashMap catche,boolean force)
	{
		int failSave=0;
		Object[] objs=catche.valueArray();
		if(objs==null||objs.length<=0) return failSave;
		int nmax=max;
		if(force) nmax=objs.length;
		for(int i=0,j=0;i<objs.length&&j<nmax;i++)
		{
			if(objs[i]==null) continue;
			CrossWarPlayer p=(CrossWarPlayer)objs[i];
			if(p.isSave())
			{
				boolean succ=warPlayerAccess.save(p);
				failSave+=succ?0:1;
				p.setSave(!succ);
				j++;
			}
		}
		return failSave;
	}
	@Override
	public void onTimer(TimerEvent e)
	{
		savePlayers(false);
	}
	
	/** 通过跨服唯一id获取玩家 */
	public CrossWarPlayer getPlayerById(int id)
	{
		return (CrossWarPlayer)catche.get(id);
	}
	/** 判断某个玩家是否存在 */
	public boolean isExist(int platid,int areaid,int serverid,int pid)
	{
		Object[] objs=catche.valueArray();
		if(objs==null||objs.length<=0) return false;
		for(int i=0;i<objs.length;i++)
		{
			if(objs[i]==null) continue;
			CrossWarPlayer p=(CrossWarPlayer)objs[i];
			if(p.getId()==pid&&p.getSeverid()==serverid
				&&p.getAreaid()==areaid&&p.getPlatid()==platid)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void change(Object source,int type)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,Object value)
	{
		if(!(source instanceof CrossActManager)
			||!(value instanceof CrossWar)) return;
		if(type==CrossActManager.CREATE_ACT||type==CrossActManager.OVER_ACT)
		{
			int fnum=savePlayer(catche,true);
			if(fnum>0)
			{
				failcatche=catche;
				catche=new IntKeyHashMap();
				log.error("------fnum-----:"+fnum);
			}
			else
			{
				catche.clear();
			}
			System.out
				.println("------crosswarplayermanager-------type----------:"
					+type);

		}
	}

	@Override
	public void change(Object source,int type,Object v1,Object v2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,Object v1,Object v2,Object v3)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int v1,int v2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void change(Object source,int type,int v1,int v2,int v3)
	{
		// TODO Auto-generated method stub
		
	}
}
