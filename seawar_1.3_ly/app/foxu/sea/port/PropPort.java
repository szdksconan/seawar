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

/** ��Ʒ�����˿�1007 */
public class PropPort extends AccessPort
{

	/** ��ս��Ʒ��sid */
	public static final int NO_FIGHT=509;
	/**�һ���ĳ���**/
	public static final int CODE_LENGTH=14;
	AutoUpBuildManager autoUpBuilding;
	PhysicalPropManager physicalPropManager;
	/** ���ݻ�ȡ�� */
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
			//�������װ��������Ƿ�ᳬ������
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
			// �����ʹ������ս
			if(sid==NO_FIGHT)
			{
				// �㲥ǰ̨
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
			// ��ʱ��Ʒ��¼
			ActivityContainer.getInstance().limitSaleRecord(player,sid,gems);
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.BUY_PROP,player.getId(),
				gems,prop.getSid(),Resources.getGems(player.getResources()));
			
			if(prop instanceof AwardProp&&((AwardProp)prop).isBuyUse())
			{
				((PropUse)prop).use(player,objectFactory,data);
				data.clear();
				// �����ʹ������ս
				if(sid==NO_FIGHT)
				{
					// �㲥ǰ̨
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
			
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				prop.getNeedGems());
		}
		// ����ʹ��
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
			// ��ʱ��Ʒ��¼
			ActivityContainer.getInstance().limitSaleRecord(player,sid,gems);
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.BUY_PROP,player.getId(),
				gems,prop.getSid(),Resources.getGems(player.getResources()));
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				prop.getNeedGems());
			// �����ʹ������ս
			if(sid==NO_FIGHT)
			{
				// �㲥ǰ̨
				JBackKit.sendPlayerIslandState(objectFactory.getDsmanager()
					.getSessionMap(),1,objectFactory.getIslandCache()
					.getPlayerIsLandId(player.getId()));
			}
		}
		//���ʹ�öһ���
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
		// ÿ���ۿ�
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
			// ��ʱ��Ʒ��¼
			activity.addRecord(player,sid);
			// ��ʯ���Ѽ�¼
			objectFactory.createGemTrack(GemsTrack.BUY_PROP,player.getId(),
				gems,prop.getSid(),Resources.getGems(player.getResources()));
			
			data.clear();
			if(prop instanceof AwardProp&&((AwardProp)prop).isBuyUse())
			{
				((PropUse)prop).use(player,objectFactory,data);
				data.clear();
				// �����ʹ������ս
				if(sid==NO_FIGHT)
				{
					// �㲥ǰ̨
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
			
			// ����change��Ϣ
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				prop.getNeedGems());
			JBackKit.sendDateOffPropState(player);
		}
		// ����VIP����������
		else if(type==PublicConst.VIP_LIMIT_SALE)
		{
			int index=data.readUnsignedByte();
			//vip�ȼ�����
			if(index>player.getUser_state())
				throw new DataAccessException(0,"vip level limit");
			//��������������
			if(PublicConst.VIP_LIMIT_AWARD.length/3<index)
					throw new DataAccessException(0,"wrong award index");
			int limit=0;
			String limitStr=player
				.getAttributes(PublicConst.VIP_LIMIT_SALE_RECORD);
			if(limitStr!=null&&!limitStr.equals(""))
				limit=TextKit.parseInt(limitStr);
			index--;
			//����״̬����λ����λ��Ӧvip�ȼ��ߵ�
			int state=limit&(1<<index);
			if(state==0)
			{
				int awardSid=PublicConst.VIP_LIMIT_AWARD[index*3];
				int needGems=PublicConst.VIP_LIMIT_AWARD[index*3+2];
				// ��Ҫ��ʯ����
				if(!Resources.checkGems(needGems,player.getResources()))
				{
					throw new DataAccessException(0,"not enough gems");
				}
				// �۷�
				Resources.reduceGems(needGems,player.getResources(),player);
				// ��ʯ���Ѽ�¼
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
				// ����change��Ϣ
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,needGems);
			}
			data.clear();
		}
		// ʵ�ｱ������
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
	
	/** ����ܷ���һ����Ʒ */
	public String checkBuyProp(int sid,Player player)
	{
		Prop prop=(Prop)Prop.factory.getSample(sid);
		if(prop==null)return "the prop does not exist";
		if(prop.getNeedGems()==0) return "you can not buy this prop";
		LimitSaleActivity activity=(LimitSaleActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.LIMIT_ID,0);
		// �Ƿ�����ʱ���Ʒ
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
	 * @param objectFactory Ҫ���õ� objectFactory
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
	/** ʹ����Ʒ����װ��֮ǰ����Ƿ�ᳬ���ֿ����� */
	public void checkEquipStore(Prop prop,int num,Player player)
	{
		// ���ݽ��������ۼ�
		IntKeyHashMap typeCounts=new IntKeyHashMap();
		if(prop instanceof AwardProp)
		{
			int[] awards=((AwardProp)prop).getAwardSids();
			if(awards!=null)
			{
				for(int i=0;i<awards.length;i++)
				{
					// ����漰��װ������Ҫ���ж�װ���ֿ��ܷ����ɣ����������Ĳ���ʾ
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
					// Ԥ����������ж�
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
	
	/**����Ƿ����ʹ�õ���**/
	public boolean checkBuildProduce(Player player)
	{
		Island builds=player.getIsland();
		if(checkShipProduce(builds,BuildInfo.INDEX_10)) return true;
		if(checkShipProduce(builds,BuildInfo.INDEX_11)) return true;
		if(checkShipProduce(builds,BuildInfo.INDEX_12)) return true;
		if(checkShipProduce(builds,BuildInfo.INDEX_13)) return true;
		return false;
	}
	
	
	/**����Ƿ����ʹ�õ���**/
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
