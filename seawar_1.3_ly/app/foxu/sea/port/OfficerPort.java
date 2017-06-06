package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.set.IntList;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.officer.CoinsTrack;
import foxu.sea.officer.OfficerShop;
import foxu.sea.officer.Officer;
import foxu.sea.officer.OfficerFragment;
import foxu.sea.officer.OfficerManager;
import foxu.sea.officer.OfficerTrack;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.Prop;
import foxu.sea.task.TaskEventExecute;

/**
 * 军官端口 1031
 * 
 * @author Alan
 */
public class OfficerPort extends AccessPort
{

	/***
	 * BUY_OFFCER_PIECE =12 购买军官碎片    BUY_FLUSH_SHOP=13 手动刷新商品
	 * 
	 */
	public static final int DRAW_FRAG_LOW=1,DRAW_FRAG_HIGH=2,
					COMPOSE_OFFICER=3,USE_OFFICER=4,AWARD_FEATS=5,
					PROMOTE_RANK=6,READ_BOOK=7,WRITE_BOOK=8,DISBAND=9,
					SYSTEM_GUIDE=10,SYSTEM_GUIDE_BREAK=11,BUY_OFFCER_PIECE=12,
					BUY_FLUSH_SHOP=13;
	CreatObjectFactory objectFactory;
	OfficerManager officerManager;

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
		// 类型
		int type=data.readUnsignedByte();
		// 低级抽奖
		if(type==DRAW_FRAG_LOW)
		{
			data.clear();
			IntList list=new IntList();
			String msg=officerManager.fragLowDraw(player,list);
			if(msg!=null) throw new DataAccessException(0,msg);
			data.writeInt(officerManager.getLeftTimeFromLastDraw(player));
			data.writeInt(player.getOfficers().getFreeDraw());
			writeFragInfo(list,data);
		}
		// 高级抽奖
		else if(type==DRAW_FRAG_HIGH)
		{
			int count=data.readUnsignedByte();
			data.clear();
			IntList list=new IntList();
			if(count>1&&player.getAttributes(PublicConst.OFFICER_FREE_DRAW)==null)
			{
				officerManager.systemGuideDraw(player,count,list);
				player.setAttribute(PublicConst.OFFICER_FREE_DRAW,"t");
			}
			else
			{
				String msg=officerManager.fragHighDraw(player,count,list);
				if(msg!=null) throw new DataAccessException(0,msg);
			}
			writeFragInfo(list,data);
		}
		// 合成军官
		else if(type==COMPOSE_OFFICER)
		{
			int sid=data.readInt();
			data.clear();
			String msg=officerManager.checkComposeOfficer(sid,player);
			if(msg!=null) throw new DataAccessException(0,msg);
			Officer officer=officerManager.composeOfficer(sid,player);
			data.writeInt(1);
			officer.showBytesWrite(data);
		}
		// 使用军官
		else if(type==USE_OFFICER)
		{
			int len=data.readInt();
			officerManager.clearUsingOfficer(player);
			for(int i=0;i<len;i++)
			{
				int id=data.readInt();
				int sid=data.readInt();
				int index=data.readUnsignedShort();
				String msg=officerManager.useOfficer(player,id,sid,index);
				if(msg!=null) throw new DataAccessException(0,msg);
			}
			data.clear();
			player.getIsland().getMainGroup().getOfficerFleetAttr()
				.initOfficers(player);
			officerManager.resetMainGroup(player);
			JBackKit.resetMainGroup(player);
			player.getOfficers().showBytesWriteUsingOfficers(data);
			player.getOfficers().showBytesWriteOfficerEffects(data,player);
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.USE_OFFICER);
		}
		// 军官授勋
		else if(type==AWARD_FEATS)
		{
			int id=data.readInt();
			int sid=data.readInt();
			int lv=data.readUnsignedShort();
			data.clear();
			String msg=null;
			int exLv=Integer.MAX_VALUE;
			Officer officer=officerManager.getOfficer(player,id,sid);
			if(officerManager.isUsedOfficer(player,officer))
				exLv=officer.getLevel();
			if(lv<2)
			{
				msg=officerManager.checkAwardFeats(player,id,sid,1);
				if(msg!=null) throw new DataAccessException(0,msg);
				officer=officerManager.awardFeats(player,id,sid,1);
			}
			else
			{
				msg=officerManager.checkAwardFeats(player,id,sid);
				if(msg!=null) throw new DataAccessException(0,msg);
				officer=officerManager.awardFeats(player,id,sid);
			}
			data.writeInt((int)officerManager.getTotalFeats(player));
			data.writeInt(1);
			officer.showBytesWrite(data);
			// 是否需要推送战力变化
			if(officer.getLevel()>exLv)
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.OFFICER_LV_UP);
		}
		// 军衔提升
		else if(type==PROMOTE_RANK)
		{
			int id=data.readInt();
			int sid=data.readInt();
			data.clear();
			String msg=officerManager.checkPromoteRank(player,id,sid);
			if(msg!=null) throw new DataAccessException(0,msg);
			IntList list=new IntList();
			Officer officer=officerManager.promoteRank(player,id,sid,list);
			data.writeInt(1);
			officer.showBytesWrite(data);
			int size=list.size();
			data.writeInt(size);
			for(int i=0;i<size;i++)
			{
				data.writeInt(list.get(i));
			}
			// 是否需要推送战力变化
			if(officerManager.isUsedOfficer(player,officer))
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.OFFICER_PROMOTE_RANK);
		}
		// 军官阅读书籍
		else if(type==READ_BOOK)
		{
			int readType=data.readUnsignedByte();
			String msg=officerManager.readBookFromLib(player,readType);
			data.clear();
			if(msg!=null) throw new DataAccessException(0,msg);
			data.writeByte(readType);
			data.writeInt(officerManager.getBookReadingCalm(player,readType));
			data.writeInt((int)officerManager.getTotalFeats(player));
		}
		// 制作书籍
		else if(type==WRITE_BOOK)
		{
			int writeType=data.readUnsignedByte();
			int id=data.readInt();
			int sid=data.readInt();
			data.clear();
			String msg=null;
			// 低级=0,高级=1
			if(writeType>0)
				msg=officerManager.makeBookWholeFeats(player,id,sid);
			else
				msg=officerManager.makeBookPartOfFeats(player,id,sid);
			if(msg!=null) throw new DataAccessException(0,msg);
			Officer officer=officerManager.getOfficer(player,id,sid);
			data.writeInt((int)officerManager.getTotalFeats(player));
			data.writeInt(1);
			officer.showBytesWrite(data);
		}
		else if(type==DISBAND)
		{
			int len=data.readUnsignedByte();
			int offset=data.offset();
			IntList props=new IntList();
			String msg;
			for(int i=0;i<len;i++)
			{
				int id=data.readInt();
				int sid=data.readInt();
				msg=officerManager.checkDisbandOfficer(player,id,sid);
				if(msg!=null)
					throw new DataAccessException(0,msg);
			}
			data.setOffset(offset);
			for(int i=0;i<len;i++)
			{
				int id=data.readInt();
				int sid=data.readInt();
				officerManager.disbandOfficer(player,id,sid,props);
			}
			data.clear();
			data.writeInt((int)officerManager.getTotalFeats(player));
			data.writeLong(player.getOfficers().getCoins());
			data.writeByte(props.size());
			for(int i=0;i<props.size();i+=2)
			{
				data.writeShort(props.get(i));
				data.writeInt(props.get(i+1));
			}
		}
		else if(type==SYSTEM_GUIDE)
		{
			boolean isComplete=data.readBoolean();
			int mark=data.readUnsignedShort();
			officerManager.setSystemGuide(player,isComplete,mark,null);
		}
		else if(type==SYSTEM_GUIDE_BREAK)
		{
			String id=data.readUTF();
			int mark=officerManager.getSystemGuideMark(player);
			officerManager.setSystemGuide(player,true,mark,id);
		}
		//购买军官碎片
		else if(type==BUY_OFFCER_PIECE)
		{
			int index=data.readUnsignedByte();
			//sid   物品类型  数量  宝石还是2级货币  数量   购买数量  上限数量
 			ArrayList list=player.getOfficers().getShopList();
			if(index>list.size())
			{
				throw new DataAccessException(0,"data is error");
			}	
			OfficerShop shop=(OfficerShop)list.get(index);
			if(shop==null)
			{
				throw new DataAccessException(0,"data is error");
			}
			if(!shop.checkBuyGoods())
			{
				throw new DataAccessException(0,"times to buy out of limit");
			}
			OfficerFragment of=null;
			Prop prop=null;
			int gems=0;
			int coins=0;
			if(shop.getShopType()==OfficerManager.PRO_TYPE)
			{
				prop=(Prop)Prop.factory.newSample(shop.getSid());
				if(prop==null)
				{
					throw new DataAccessException(0,"prop is null");
				}
			}
			else
			{
				of=(OfficerFragment)OfficerManager.factory
								.newSample(shop.getSid());
				if(of==null)
				{
					throw new DataAccessException(0,"of is null");
				}
			}
//			int payType=list.get(index+3);
			if(shop.getSaleType()==OfficerManager.BUY_BYCOINS)
			{
				coins=shop.getSalePrice();
				if(!player.getOfficers().checkCoins(coins))
				{
					throw new DataAccessException(0,"coin is not enough");
				}
				player.getOfficers().descCoins(coins);
				addGoods(prop,of,shop.getGoodsNum(),player);
				if(of!=null)
					objectFactory
						.createCoinsTrack(CoinsTrack.BUY_OFFICER,
							player.getId(),of.getSid(),coins,
							(int)player.getOfficers().getCoins(),
							CoinsTrack.DESC);
			}
			else
			{
				gems=shop.getSalePrice();
				if(!Resources.checkGems(gems,player.getResources()))
				{
					throw new DataAccessException(0,"not enough gems");
				}
				Resources.reduceGems(gems,player.getResources(),player);
				objectFactory.createGemTrack(GemsTrack.OFFICER_SHOP,
					player.getId(),gems,0,
					Resources.getGems(player.getResources()));
				TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,gems);
				addGoods(prop,of,shop.getGoodsNum(),player);
			}
			/**军官商店日志保存**/
			objectFactory.createOfficerTrack(OfficerTrack.ADD,
				OfficerTrack.FROM_OFFICER_SHOP,player.getId(),of.getSid(),shop.getGoodsNum(),
				of.getId(),OfficerManager.getInstance()
					.getOfficerOrFragmentCount(player,of.getSid()));
			shop.addShopsaleNum();
			data.clear();
			data.writeByte(index);
			JBackKit.sendOfficerInfo(player);
		}
		//手动刷新商品
		else if(type==BUY_FLUSH_SHOP)
		{
			if(!player.getOfficers().checkFlushShop())		
				throw new DataAccessException(0,"flush times over");
			Resources.reduceGems(OfficerManager.FLUSH_SHOP_GEMS,
				player.getResources(),player);
			objectFactory.createGemTrack(GemsTrack.OFFICER_FLUSH,
				player.getId(),OfficerManager.FLUSH_SHOP_GEMS,0,
				Resources.getGems(player.getResources()));
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,OfficerManager.FLUSH_SHOP_GEMS);
			player.getOfficers().addFlushTimes(1);
			OfficerManager.getInstance().randomPlayerOffcerShop(player);
			data.clear();
			player.getOfficers().showBytesWriteShopInfo(data);
		}
		return data;
	}

	public void writeFragInfo(IntList list,ByteBuffer data)
	{
		data.writeShort(list.size()/2);
		for(int i=0;i<list.size();i+=2)
		{
			data.writeInt(list.get(i));
			data.writeInt(list.get(i+1));
		}
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	public void setOfficerManager(OfficerManager manager)
	{
		this.officerManager=manager;
	}

	/**检测2级货币(宝石)是否足够**/
	public String checkEnough(int type,int num,Player player)
	{
		//宝石不足
		if(type==OfficerManager.BUY_BYGEMS)
		{
			if(!Resources.checkGems(num,player.getResources()))
				return "not enough gems";
		}
		//货币不足
		else if(type==OfficerManager.BUY_BYCOINS)
		{
			if(!player.getOfficers().checkCoins(num))
				return "not enough coins";
		}
		//当前没有这种类型的消费方式
		else
		{
			return "is not this type";
		}
		return null;
	}
	
	public void  addGoods(Prop prop,OfficerFragment of,int num,Player player)
	{
		if(prop!=null)
		{
			if( prop instanceof NormalProp) 
				((NormalProp)prop).setCount(num);
			player.getBundle().incrProp(prop,true);
		}
		else if(of!=null)
		{
			player.getOfficers().addOfficerFrag(of.getSid(),num);
		}
	}
	
}
