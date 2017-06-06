package foxu.sea.port;

import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.equipment.EquipList;
import foxu.sea.equipment.Equipment;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.task.TaskEventExecute;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.IntList;

public class EquipmentPort extends AccessPort
{

	// public static int[] EQUIP_BUNDLE_COST={50,100,150,200};

	CreatObjectFactory objectFactory;

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		int type=data.readUnsignedByte();
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
		// װ�����顢���ײ�������
		if(type==PublicConst.QUALITY_STUFF)
		{
			int sid=data.readUnsignedShort();
			int num=data.readInt();
			data.clear();
			// ����Ǿ������
			Equipment temp=(Equipment)Equipment.factory.getSample(sid);
			if(temp!=null&&temp.getShipType()==EquipList.EXP_STUFF_TYPE)
			{
				sid=temp.getLastQuility();
				if(player.getEquips().getSameSidCount(sid)<num)
					throw new DataAccessException(0,"not enough prop");
				for(int i=0;i<num;i++)
				{
					equipQualityUp(player,player.getEquips()
						.getOneEquipment(sid).getUid());
				}
			}
			// ����ǽ��ײ���
			else
			{
				sid=player.getEquips().getLastQualityStuffSid(sid);
				if(player.getEquips().getQualityStuffCount(sid)<num)
					throw new DataAccessException(0,"not enough prop");
				for(int i=0;i<num;i++)
				{
					String msg=player.getEquips().combineQualityStuff(sid);
					if(msg!=null) throw new DataAccessException(0,msg);
					int[] info=player.getEquips().getStuffCombineInfo(sid);
					// ����
					SeaBackKit.createEquipTrackByAutoLeft(sid,info[0],
						EquipmentTrack.INTO_COMBINE,EquipmentTrack.REDUCE,
						sid,player,objectFactory);
					// ����
					SeaBackKit.createEquipTrackByAutoLeft(info[1],1,
						EquipmentTrack.FROM_COMBINE,EquipmentTrack.ADD,sid,
						player,objectFactory);
				}
			}
			JBackKit.sendEquipInfo(player);
		}
		// ����װ��
		else if(type==PublicConst.SALE_EQUIP)
		{
			int length=data.readByte();
			for(int i=0;i<length;i++)
			{
				int uid=data.readInt();
				Equipment equip=player.getEquips().getEquip(uid);
				String msg=player.getEquips().saleEquip(uid);
				if(msg!=null) throw new DataAccessException(0,msg);
				SeaBackKit.createEquipTrackByAutoLeft(equip.getSid(),1,
					EquipmentTrack.INTO_SALE,EquipmentTrack.REDUCE,uid,
					player,objectFactory);
			}
			JBackKit.sendEquipInfo(player);
		}
		// װ��װ��
		else if(type==PublicConst.WARE_EQUIP)
		{
			int uid=data.readInt();
			String msg=player.getEquips().equip(uid,objectFactory);
			if(msg!=null) throw new DataAccessException(0,msg);
			JBackKit.sendEquipInfo(player);
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.EQUIP_ON);
		}
		// ��չװ������
		else if(type==PublicConst.ENLARGE_EQUIPLIST)
		{
			int types=data.readInt();
			data.clear();
			if(player.getEquips().getEquNum(types)>EquipList.MAX_CAPACITY)
				data.writeShort(player.getEquips().getEquNum(types));
			else
			{
				// ��鱦ʯ�Ƿ��㹻
				if(!Resources.checkGems(EquipList.EQUIP_COST_GEMS,
					player.getResources()))
				{
					throw new DataAccessException(0,"not enough gems");
				}
				Resources.reduceGems(EquipList.EQUIP_COST_GEMS,
					player.getResources(),player);
				// ��ʯ���Ѽ�¼
				objectFactory.createGemTrack(GemsTrack.EQUIP_CAPACITY,
					player.getId(),EquipList.EQUIP_COST_GEMS,types,
					Resources.getGems(player.getResources()));
				int num=player.getEquips().enlargeCapacity(5,types);
				// ����change��Ϣ
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,
					EquipList.EQUIP_COST_GEMS);
				data.writeShort(num);
			}
		}
		// װ��ǿ��
		else if(type==PublicConst.EQUIP_LEVEL_UP)
		{
			int uid=data.readInt();
			int length=data.readByte();
			IntList il=new IntList();
			Equipment[] equips=new Equipment[length];
			for(int i=0;i<length;i++)
			{
				il.add(data.readInt());
				equips[i]=player.getEquips().getEquip(il.get(i));
			}
			// ��¼���Ӿ���ǰ�ļ���
			int lv=0;
			Equipment target=player.getEquips().getEquip(uid);
			if(target!=null)	lv=target.getLevel();
			String msg=player.getEquips().incrExp(uid,il,objectFactory);
			if(msg!=null) throw new DataAccessException(0,msg);
			for(int i=0;i<equips.length;i++)
			{
				if(equips[i]!=null)
					SeaBackKit.createEquipTrackByAutoLeft(equips[i].getSid(),1,
						EquipmentTrack.INTO_INCR_EXP,EquipmentTrack.REDUCE,uid,
						player,objectFactory);
			}
			JBackKit.sendEquipInfo(player);
			// ����ȼ�������,ˢ��ս��
			if(target.isEquiped()&&target.getLevel()>lv)
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.EQUIP_LEVEL_UP);
		}
		// װ������
		else if(type==PublicConst.EQUIP_QUALITY_UP)
		{
			int uid=data.readInt();
			equipQualityUp(player,uid);
			JBackKit.sendEquipInfo(player);
			JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.EQUIP_QUALITY_UP);
		}
		// ����װ��ϵͳ����
		else if(type==PublicConst.SET_EQUIP_FOLLOW)
		{
			boolean isComplete=data.readBoolean();
			int follow=data.readByte();
			data.clear();
			int oldFollow=0;
			String followStr=player
				.getAttributes(PublicConst.EQUIP_SYS_FOLLOW);
			if(followStr!=null&&followStr.matches("\\d+"))
				oldFollow=Integer.valueOf(followStr);
			// ����Ѿ��������˲��裬���ٷ�����Ʒ
			if(follow>oldFollow)
			{
				int followState=follow;
				if(isComplete) followState=100;// ����޸���������������������ٴγ�����������
				player.setAttribute(PublicConst.EQUIP_SYS_FOLLOW,followState
					+"");
				// ����ʱ��Ҫ��������Ʒ
				int[] followProps=Equipment.SYS_FOLLOW_PROP;
				if(isComplete)
				{
					String msg=null;
					for(int i=0;i<followProps[1];i++)
					{
						Equipment equip=(Equipment)Equipment.factory
										.newSample(followProps[0]);
						if(equip==null) throw new DataAccessException(0,"equip not exist");
						// 20��װ��
						equip.addExp(equip.getLevelExps()[20-2]);
						msg=player.getEquips().addEquipment(equip,0);
						SeaBackKit.createEquipTrackByAutoLeft(equip.getSid(),1,
							EquipmentTrack.FROM_FOLLOW,EquipmentTrack.ADD,
							0,player,objectFactory);
						if(msg!=null) break;
						player.getEquips().equip(equip.getUid(),objectFactory);
					}
					// ǰ̨����ʹ���˼����ݣ������Ӧͬ��
					JBackKit.sendEquipInfo(player);
					JBackKit.sendResetBunld(player);
					JBackKit.sendResetResources(player);
					JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.EQUIP_ON);
					if(msg!=null) throw new DataAccessException(0,msg);
				}
			}
		}
		// װ��ж��
		else if(type==PublicConst.OFF_EQUIP)
		{
			int uid=data.readInt();
			Equipment equip=player.getEquips().getEquip(uid);
			boolean isEquiped=false;
			if(equip!=null&&equip.isEquiped())
				isEquiped=true;
			if(equip!=null&&equip.getShipType()!=EquipList.EXP_STUFF_TYPE)
				player.getEquips().unEquip(equip.getShipType(),
					equip.getEquipType(),true,objectFactory);
			JBackKit.sendEquipInfo(player);
			if(isEquiped)
				JBackKit.sendFightScore(player,objectFactory,true,FightScoreConst.EQUIP_OFF);
		}
		return data;
	}

	public void equipQualityUp(Player player,int uid)
	{
		Equipment equip=player.getEquips().getEquip(uid);
		String msg=player.getEquips().qualityUp(uid,objectFactory);
		if(msg!=null) throw new DataAccessException(0,msg);
		// �����Լ�
		SeaBackKit.createEquipTrackByAutoLeft(equip.getSid(),1,
			EquipmentTrack.INTO_QUALITY_UP,EquipmentTrack.REDUCE,uid,
			player,objectFactory);
		// ���Ĳ���
		int[] stuffs=equip.getQualityUpStuffs();
		if(stuffs!=null)
		{
			for(int i=0;i<stuffs.length;i+=2)
			{
				SeaBackKit.createEquipTrackByAutoLeft(stuffs[i],stuffs[i+1],
					EquipmentTrack.INTO_QUALITY_UP,EquipmentTrack.REDUCE,uid,
					player,objectFactory);
			}
		}
		// ����װ��
		stuffs=equip.getQualityUpEquips();
		if(stuffs!=null)
		{
			for(int i=0;i<stuffs.length;i+=2)
			{
				SeaBackKit.createEquipTrackByAutoLeft(stuffs[i],stuffs[i+1],
					EquipmentTrack.INTO_QUALITY_UP,EquipmentTrack.REDUCE,uid,
					player,objectFactory);
			}
		}
		// ��װ��
		Equipment newEquip=(Equipment)Equipment.factory.getSample(equip
			.getNextQuility());
		if(newEquip!=null)
		{
			SeaBackKit.createEquipTrackByAutoLeft(newEquip.getSid(),1,
				EquipmentTrack.FROM_QUALITY_UP,EquipmentTrack.ADD,
				equip.getSid(),player,objectFactory);
		}
	}
	
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}

	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

}
