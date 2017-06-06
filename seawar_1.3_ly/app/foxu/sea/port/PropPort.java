package foxu.sea.port;

import mustang.back.BackKit;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.DatePriceOffActivity;
import foxu.sea.activity.LimitSaleActivity;
import foxu.sea.award.Award;
import foxu.sea.builds.AutoUpBuildManager;
import foxu.sea.builds.Build;
import foxu.sea.builds.BuildInfo;
import foxu.sea.builds.Island;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.builds.produce.StandProduce;
import foxu.sea.comrade.ComradeHandler;
import foxu.sea.equipment.Equipment;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.gems.GemsTrack;
import foxu.sea.gm.operators.CodeInfomation;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.AwardProp;
import foxu.sea.proplist.BuildAutoUpProp;
import foxu.sea.proplist.CDProp;
import foxu.sea.proplist.NormalProp;
import foxu.sea.proplist.PhysicalPropManager;
import foxu.sea.proplist.Prop;
import foxu.sea.proplist.PropLogManager;
import foxu.sea.proplist.PropUse;
import foxu.sea.task.TaskEventExecute;

/** 物品操作端口1007 */
public class PropPort extends AccessPort
{

	/** 免战物品的sid */
	public static final int NO_FIGHT=509;
	/**兑换码的长度**/
	public static final int CODE_LENGTH=14;
	AutoUpBuildManager autoUpBuilding;
	PhysicalPropManager physicalPropManager;
	/** 数据获取类 */
	CreatObjectFactory objectFactory;
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
		int type=data.readUnsignedByte();
		int sid=data.readUnsignedShort();
		if(type==PublicConst.USE_PROP)
		{
			int num=data.readUnsignedShort();
			data.clear();
			Prop prop=player.getBundle().getPropById(sid);
			if(prop==null)
			{
				throw new DataAccessException(0,"prop is null");
			}
			if(!player.getBundle().checkDecrProp(sid,num))
			{
				throw new DataAccessException(0,"prop not enough");
			}
			if(!(prop instanceof PropUse))
			{
				throw new DataAccessException(0,"prop can not be use");
			}
			if(prop.getLimitLevel()>player.getLevel())
				throw new DataAccessException(0,"player lvl limit");
			boolean flag=false;
			if(prop instanceof CDProp)
			{
				CDProp cd=(CDProp)prop;
				if(cd.getType()==CDProp.BULID_TYPE)
				{
					boolean buildingNow=true;
					Object[] buildings=player.getIsland().getBuildArray();
					for(int i=0;i<buildings.length;i++)
					{
						PlayerBuild pb=(PlayerBuild)buildings[i];
						if(pb!=null
							&&player.getIsland().checkNowBuildingByIndex(
								pb.getIndex()))
						{
							buildingNow=false;
							break;
						}
					}
					if(buildingNow)
					{
						throw new DataAccessException(0,
							"build_queue_not_exist");
					}
				}
				else if(cd.getType()==CDProp.SCIENCE_TYPE)
				{
					flag=true;
					PlayerBuild checkBuild=(PlayerBuild)player.getIsland()
						.getBuildByType(Build.BUILD_RESEARCH,null);
					if(checkBuild==null)
						throw new DataAccessException(0,
							"build_science_not_exist");
					StandProduce produce=(StandProduce)checkBuild
						.getProduce();
					if(produce.getProductes()==null)
						throw new DataAccessException(0,"not science speed");
					Object[] productes=produce.getProductes().getArray();
					if(productes==null||productes.length==0)
						throw new DataAccessException(0,"not science speed");
				}
				else if(cd.getType()==CDProp.SHIP_TYPE)
				{
					flag=true;
					if(!checkBuildProduce(player))
						throw new DataAccessException(0,"not ship speed");
				}
			}
			//如果增加装备，检测是否会超过容量
			checkEquipStore(prop,num,player);
			player.getBundle().decrProp(prop.getSid(),num);
			if(prop instanceof BuildAutoUpProp)
			{
				data.writeBoolean(flag);
				((BuildAutoUpProp)prop).use(player,objectFactory,
						autoUpBuilding,data,num);
			}
			else if(prop instanceof CDProp)
			{
				data.writeBoolean(flag);
				((CDProp)prop).use(player,objectFactory,autoUpBuilding,
						data,num);
			}
			else
			{
				data.writeBoolean(flag);
				((PropUse)prop).use(player,objectFactory,data,num);
				
			}
			// 有玩家使用了免战
			if(sid==NO_FIGHT)
			{
				// 广播前台
				JBackKit.sendPlayerIslandState(objectFactory.getDsmanager()
					.getSessionMap(),1,objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId()));
			}
			JBackKit.sendResetBunld(player);
		}
		else if(type==PublicConst.BUY_PROP)
		{
			String str=checkBuyProp(sid,player);
			if(str!=null) throw new DataAccessException(0,str);
			Prop prop=(Prop)Prop.factory.newSample(sid);
			int gems=ActivityContainer.getInstance().discountGems(sid,
				prop.getNeedGems(),player.getId());
			Resources.reduceGems(gems,player.getResources(),player);
			// 限时商品记录
			ActivityContainer.getInstance().limitSaleRecord(player,sid,gems);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.BUY_PROP,player.getId(),
				gems,prop.getSid(),Resources.getGems(player.getResources()));
			
			if(prop instanceof AwardProp&&((AwardProp)prop).isBuyUse())
			{
				((PropUse)prop).use(player,objectFactory,data);
				data.clear();
				// 有玩家使用了免战
				if(sid==NO_FIGHT)
				{
					// 广播前台
					JBackKit.sendPlayerIslandState(objectFactory.getDsmanager()
						.getSessionMap(),1,objectFactory.getIslandCache()
						.getPlayerIsLandId(player.getId()));
				}
				data.writeBoolean(false);
			}else
			{
				player.getBundle().incrProp(prop,true);
				data.writeBoolean(true);	
			}
			
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				prop.getNeedGems());
		}
		// 购买并使用
		else if(type==PublicConst.BUY_PROP_AND_USE)
		{
			String str=checkBuyProp(sid,player);
			if(str!=null) throw new DataAccessException(0,str);
			Prop prop=(Prop)Prop.factory.newSample(sid);
			if(!(prop instanceof PropUse))
			{
				throw new DataAccessException(0,"prop can not be use");
			}
			if(prop.getLimitLevel()>player.getLevel())
				throw new DataAccessException(0,"player lvl limit");
			if(prop instanceof CDProp)
			{
				boolean buildingNow=true;
				Object[] buildings=player.getIsland().getBuildArray();
				for(int i=0;i<buildings.length;i++)
				{
					PlayerBuild pb=(PlayerBuild)buildings[i];
					if(pb!=null&&player.getIsland().checkNowBuildingByIndex(pb.getIndex()))
					{
						buildingNow=false;
						break;
					}
				}
				if(buildingNow)
				{
					throw new DataAccessException(0,"prop can not be use");
				}
			}
			if(prop instanceof BuildAutoUpProp)
			{
				((BuildAutoUpProp)prop).use(player,objectFactory,
					autoUpBuilding,data);
			}
			else
			{
				((PropUse)prop).use(player,objectFactory,data);
				data.clear();
			}
			savePropLog(player,prop);
			int gems=ActivityContainer.getInstance().discountGems(sid,
				prop.getNeedGems(),player.getId());
			Resources.reduceGems(gems,player.getResources(),player);
			// 限时商品记录
			ActivityContainer.getInstance().limitSaleRecord(player,sid,gems);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.BUY_PROP,player.getId(),
				gems,prop.getSid(),Resources.getGems(player.getResources()));
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				prop.getNeedGems());
			// 有玩家使用了免战
			if(sid==NO_FIGHT)
			{
				// 广播前台
				JBackKit.sendPlayerIslandState(objectFactory.getDsmanager()
					.getSessionMap(),1,objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId()));
			}
		}
		//玩家使用兑换码
		else  if(type==PublicConst.CODE_PROP)
		{
			String codeId=data.readUTF();
			data.clear();
			if(codeId.length()==CODE_LENGTH)
			{
				data.writeByte(PublicConst.PLAYER_CODE_EXCHANGE);
				data.writeUTF(codeId);
				data.writeInt(player.getId());
				data.writeUTF(player.getName());
				data.writeInt(player.getCreateTime());
				String sea_serverid=(String)BackKit.getContext().get("sea_ServerId");
				data.writeUTF(sea_serverid);
				data=CodeInfomation.sendHttpData(data,null);
				if(data==null) throw new DataAccessException(0,"platform is closed");
				int  proid=data.readInt();
				if(proid==PublicConst.CODEID_IS_ERRO)   throw new DataAccessException(0,"sorry codeid is wrong");
				if(proid==PublicConst.CODEID_IS_USERED)   throw new DataAccessException(0,"sorry codeid already had been useed");
				if(proid==PublicConst.CODEID_IS_LIMIT)   throw new DataAccessException(0,"The exchange at the limit");
				Prop prop=(Prop)Prop.factory.newSample(proid);
				data.clear();
				if(prop==null)  throw new DataAccessException(0,"sorry Award iswrong");
				if(prop instanceof NormalProp) 
					((NormalProp)prop).setCount(PublicConst.PROP_NUM);
				player.getBundle().incrProp(prop,true);
				JBackKit.sendResetBunld(player);
				data.clear();
				data.writeByte(PublicConst.CODE_TYPE);
				data.writeInt(proid);
			}
			else
			{
				int veteranId=0;
				try
				{
					veteranId=TextKit.parseInt(codeId);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					throw new DataAccessException(0,"inveted is wrong");
				}
				String result=ComradeHandler.getInstance().addRecruit(veteranId,
					player,data);
				if(result!=null) throw new DataAccessException(0,result);
			}
		}
		// 每日折扣
		else if(type==PublicConst.DATE_OFF_PROP)
		{
			Prop prop=(Prop)Prop.factory.getSample(sid);
			if(prop==null)
				throw new DataAccessException(0,"the prop does not exist");
			if(prop.getNeedGems()==0) 
				throw new DataAccessException(0,"you can not buy this prop");
			DatePriceOffActivity activity=(DatePriceOffActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.DATE_OFF_ID,0);
			if(activity==null)
				throw new DataAccessException(0,"not open this activity");
			if(activity!=null&&activity.isAvaliableProp(sid)
				&&!activity.isPurchasable(player,sid))
			{
				throw new DataAccessException(0,"limit sale max");
			}
			int gems=activity.getSidGems(sid);
			if(!Resources.checkGems(gems,player.getResources()))
			{
				throw new DataAccessException(0,"not enough gems");
			}
			Resources.reduceGems(gems,player.getResources(),player);
			// 限时商品记录
			activity.addRecord(player,sid);
			// 宝石消费记录
			objectFactory.createGemTrack(GemsTrack.BUY_PROP,player.getId(),
				gems,prop.getSid(),Resources.getGems(player.getResources()));
			
			data.clear();
			if(prop instanceof AwardProp&&((AwardProp)prop).isBuyUse())
			{
				((PropUse)prop).use(player,objectFactory,data);
				data.clear();
				// 有玩家使用了免战
				if(sid==NO_FIGHT)
				{
					// 广播前台
					JBackKit.sendPlayerIslandState(objectFactory.getDsmanager()
						.getSessionMap(),1,objectFactory.getIslandCache()
						.getPlayerIsLandId(player.getId()));
				}
				data.writeBoolean(false);
			}else
			{
				player.getBundle().incrProp(prop,true);
				data.writeBoolean(true);	
			}
			
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				prop.getNeedGems());
			JBackKit.sendDateOffPropState(player);
		}
		// 购买VIP限量奖励包
		else if(type==PublicConst.VIP_LIMIT_SALE)
		{
			int index=data.readUnsignedByte();
			//vip等级不够
			if(index>player.getUser_state())
				throw new DataAccessException(0,"vip level limit");
			//配置有误的情况下
			if(PublicConst.VIP_LIMIT_AWARD.length/3<index)
					throw new DataAccessException(0,"wrong award index");
			int limit=0;
			String limitStr=player
				.getAttributes(PublicConst.VIP_LIMIT_SALE_RECORD);
			if(limitStr!=null&&!limitStr.equals(""))
				limit=TextKit.parseInt(limitStr);
			index--;
			//购买状态，低位至高位对应vip等级高低
			int state=limit&(1<<index);
			if(state==0)
			{
				int awardSid=PublicConst.VIP_LIMIT_AWARD[index*3];
				int needGems=PublicConst.VIP_LIMIT_AWARD[index*3+2];
				// 需要宝石数量
				if(!Resources.checkGems(needGems,player.getResources()))
				{
					throw new DataAccessException(0,"not enough gems");
				}
				// 扣费
				Resources.reduceGems(needGems,player.getResources(),player);
				// 宝石消费记录
				objectFactory.createGemTrack(GemsTrack.VIP_LIMIT_AWARD,
					player.getId(),needGems,awardSid,
					Resources.getGems(player.getResources()));
				Award award=(Award)Award.factory.newSample(awardSid);
				if(award!=null)
				{
					limit=limit|(1<<index);
					player.setAttribute(PublicConst.VIP_LIMIT_SALE_RECORD,limit+"");
					award.awardLenth(data,player,objectFactory,null,
						new int[]{EquipmentTrack.FROM_VIP_LIMIT});
				}
				// 发送change消息
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
			}
			data.clear();
		}
		// 实物奖励申请
		else if(type==PublicConst.PHYSICAL_REWARDS)
		{
			int num=1;
			String receiveName=data.readUTF();
			String receiveAddress=data.readUTF();
			String receivePhone=data.readUTF();
			data.clear();
			Prop prop=player.getBundle().getPropById(sid);
			if(prop==null)
			{
				throw new DataAccessException(0,"prop is null");
			}
			if(!player.getBundle().checkDecrProp(sid,num))
			{
				throw new DataAccessException(0,"prop not enough");
			}
			if(!SeaBackKit.isContainValue(PublicConst.PHYSICAL_PROPS,
					prop.getSid()))
			{
				throw new DataAccessException(0,"prop can not be use");
			}
			if(prop.getLimitLevel()>player.getLevel())
				throw new DataAccessException(0,"player lvl limit");
			player.getBundle().decrProp(prop.getSid());
			JBackKit.sendResetBunld(player);
			physicalPropManager.addInfo(player,receiveName,receiveAddress,
				receivePhone,prop.getSid());
		}
		return data;
	}
	
	/** 检查能否购买一个物品 */
	public String checkBuyProp(int sid,Player player)
	{
		Prop prop=(Prop)Prop.factory.getSample(sid);
		if(prop==null)return "the prop does not exist";
		if(prop.getNeedGems()==0) return "you can not buy this prop";
		LimitSaleActivity activity=(LimitSaleActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.LIMIT_ID,0);
		// 是否是限时活动物品
		boolean isLimitSale=false;
		if(activity!=null&&activity.isLimitProp(sid))
		{
			if(!player.canLimitSale(activity.getId(),(Integer)activity
				.getSid_num().get(sid),sid))
			{
				return "limit sale max";
			}
			isLimitSale=true;
		}
		if(!isLimitSale
			&&!SeaBackKit.isContainValue(PublicConst.SHOP_SELL_SIDS,sid)
			&&!SeaBackKit.isContainValue(PublicConst.OTHER_SELL_SIDS,sid))
		{
			return "you can not buy this prop";
		}
		if(!Resources.checkGems(ActivityContainer.getInstance()
			.discountGems(sid,prop.getNeedGems()),player.getResources()))
		{
			return "not enough gems";
		}
		return null;
	}

	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	
	public AutoUpBuildManager getAutoUpBuilding()
	{
		return autoUpBuilding;
	}

	
	public void setAutoUpBuilding(AutoUpBuildManager autoUpBuilding)
	{
		this.autoUpBuilding=autoUpBuilding;
	}
	
	public PhysicalPropManager getPhysicalPropManager()
	{
		return physicalPropManager;
	}

	
	public void setPhysicalPropManager(PhysicalPropManager physicalPropManager)
	{
		this.physicalPropManager=physicalPropManager;
	}

	public void savePropLog(Player player,Prop prop)
	{
		PropLogManager.getInstance().buyUseProp(player,prop.getSid());
	}
	/** 使用物品增加装备之前检测是否会超过仓库容量 */
	public void checkEquipStore(Prop prop,int num,Player player)
	{
		// 根据舰船类型累计
		IntKeyHashMap typeCounts=new IntKeyHashMap();
		if(prop instanceof AwardProp)
		{
			int[] awards=((AwardProp)prop).getAwardSids();
			if(awards!=null)
			{
				for(int i=0;i<awards.length;i++)
				{
					// 如果涉及到装备，需要先判定装备仓库能否容纳，否则不予消耗并提示
					Award award=((Award)Award.factory.getSample(awards[i]));
					int[] equips=award.getEquipSids();
					if(equips!=null)
					{
						for(int j=0;j<equips.length;j+=2)
						{
							Equipment equip=(Equipment)Equipment.factory
								.getSample(equips[j]);
							if(equip!=null)
							{
								int shipType=equip.getShipType();
								Integer count=(Integer)typeCounts.get(shipType);
								if(count==null)
									count=0;
								count+=(equips[j+1]*num);
								if(count>(player.getEquips()
									.getEquNum(shipType)-player.getEquips()
									.getTypeNum(shipType)))
									throw new DataAccessException(0,
										"equip storehouse is full");
								typeCounts.put(shipType,count);
							}
						}
					}
					// 预留随机道具判定
					int[] randoms=award.getRandomProps();
					if(randoms!=null)
					{
						for(int j=0;j<randoms.length;j+=3)
						{
							Equipment equip=(Equipment)Equipment.factory
								.getSample(randoms[j]);
							if(equip!=null)
							{
								int shipType=equip.getShipType();
								Integer count=(Integer)typeCounts.get(shipType);
								if(count==null)
									count=0;
								count+=(randoms[j+1]*num);
								if(count>(player.getEquips()
									.getEquNum(shipType)-player.getEquips()
									.getTypeNum(shipType)))
									throw new DataAccessException(0,
										"equip storehouse is full");
								typeCounts.put(shipType,count);
							}
						}
					}
				}
			}
		}
	}
	
	/**检测是否可以使用道具**/
	public boolean checkBuildProduce(Player player)
	{
		Island builds=player.getIsland();
		if(checkShipProduce(builds,BuildInfo.INDEX_10)) return true;
		if(checkShipProduce(builds,BuildInfo.INDEX_11)) return true;
		if(checkShipProduce(builds,BuildInfo.INDEX_12)) return true;
		if(checkShipProduce(builds,BuildInfo.INDEX_13)) return true;
		return false;
	}
	
	
	/**检测是否可以使用道具**/
	public boolean checkShipProduce(Island builds,int index)
	{
		PlayerBuild checkBuild=builds.getBuildByIndex(index,
			builds.getBuilds());
		if(checkBuild==null||checkBuild.getProduce()==null) return false;
		StandProduce produce=(StandProduce)checkBuild.getProduce();
		if(produce==null) return false;
		if(produce.getProductes()==null) return false;
		Object[] productes=produce.getProductes().getArray();
		if(productes==null||productes.length==0) return false;
		return true;

	}
}
