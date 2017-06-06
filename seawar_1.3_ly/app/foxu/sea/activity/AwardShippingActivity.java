package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.net.DataAccessException;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.task.TaskEventExecute;



public class AwardShippingActivity extends AwardActivity implements ActivitySave
{

	public static int AWARD_LENGTH=100;
	/** ͨ�̺��˻ ����콱��¼  �������ݿ�  value,bytebuffer[]��Ʒ���� */
	private IntKeyHashMap awardRecord = new IntKeyHashMap();
	
	/**�齱��¼**/
	IntKeyHashMap luckDraw=new IntKeyHashMap();
	
	/** ���Ϣ�������� */
	public static final int SAVE_CIRCLE=15*60;
	/** ��һ�λ��Ϣ����ʱ�� */
	int lastSaveTime;
	
	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		//ͨ�̺���ʹ���������л�
		data.writeShort(getSid());	//�sid
		data.writeShort(gems);		//ÿ�����豦ʯ
		data.writeInt(endTime);		//����ʱ��
		data.writeShort(getAwardShipTimes(player.getId()));	//����Գ齱����
		data.writeShort(times);		//�ɳ齱�ܴ���
		data.writeShort(getAwardShipLocation(player.getId()));
	}
	
	/**
	 * ��ӽ�Ʒ��¼
	 */
	public void putAwardRecord(ByteBuffer data,int playerId)
	{
		IntList list;
		if(awardRecord.get(playerId)==null)
			list=new IntList();

		else
			list=(IntList)awardRecord.get(playerId);
		   ByteBuffer tempData=(ByteBuffer)data.clone();
			tempData.readShort();
			list.add(tempData.readByte());
			list.add(tempData.readUnsignedByte());
			list.add(tempData.readInt());
			list.add(tempData.readShort());
			awardRecord.put(playerId,list);
	}

	/**
	 * ��ȡ��Ʒ��¼
	 */
	public void getAwardRecord(int playerId,ByteBuffer data)
	{
		if(data==null) data=new ByteBuffer();
		if(awardRecord.get(playerId)==null)
			throw new DataAccessException(0,"no award records");
		IntList list=(IntList)awardRecord.get(playerId);
		data.clear();
		if(list!=null)
		{
			if((list.size()/4)>AWARD_LENGTH)
				data.writeShort(AWARD_LENGTH);
			else
			{
				int le=list.size()/4;
				data.writeShort(le);
			}
			for(int i=0;i<list.size();i+=4)
			{
				if((list.size()-i)/4>AWARD_LENGTH) continue;
				data.writeByte(list.get(i));
				data.writeByte(list.get(i+1));
				data.writeInt(list.get(i+2));
				data.writeShort(list.get(i+3));
			}
		}
		else
			data.writeShort(0);
	}

	public void draw(ByteBuffer data,Player player,CreatObjectFactory factory)
	{
		int nowLoca=data.readUnsignedShort();
		int count=getAwardShipTimes(player.getId());
		if(times!=-1&&count>=times)
		{
			throw new DataAccessException(0,
				"lucky_reward today times limite");
		}
		else
		// ���ͽ�Ʒ��
		{
			if(!Resources.checkGems(gems,player.getResources()))
				throw new DataAccessException(0,"not enough gems");
			if(Resources.reduceGems(gems,player.getResources(),player))
			{
				factory.createGemTrack(GemsTrack.LUCKY_DRAW_SHIPPING,
					player.getId(),gems,0,
					Resources.getGems(player.getResources()));
				// ������¼
				data.clear();
				data.writeShort(nowLoca);
				// ��Ʒ����Ϊ1
				String message=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,"awardshippingactivity_message");
				message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
				getAward().awardLenth(data,player,factory,message,
					SeaBackKit.getLuckySids(),
					new int[]{EquipmentTrack.FROM_SHIPPING_LUCKY,FightScoreConst.NEW_ACTIVITY});
				// ��ӻ񽱼�¼				
				putAwardRecord(data,player.getId());
				setAwardShipInfo(player.getId(),nowLoca);
				// ����change��Ϣ
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,gems);
			}
		}
	}

	@Override
	public boolean isSave()
	{
		if(TimeKit.getSecondTime()>=lastSaveTime+SAVE_CIRCLE) return true;
		return false;
	}
	@Override
	public void setSave()
	{
		lastSaveTime=TimeKit.getSecondTime();
	}

	/**���ó齱��Ϣ**/
	public void setAwardShipInfo(int playerId,int location)
	{
		int times=getAwardShipTimes(playerId)+1;
		luckDraw.put(playerId,times+","+location);
	}
	
	/**��ȡ�齱����**/
	public int getAwardShipTimes(int playerId)
	{
		String result=(String)luckDraw.get(playerId);
		if(result==null||result.length()==0) return 0;
		String[] info=TextKit.split(result,",");
		return TextKit.parseInt(info[0]);
	}
	
	/**��ȡ�ϴε�����**/
	public int getAwardShipLocation(int playerId)
	{
		String result=(String)luckDraw.get(playerId);
		if(result==null||result.length()==0) return 0;
		String[] info=TextKit.split(result,",");
		return TextKit.parseInt(info[1]);
	}
	
	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		super.initData(data,factory,active);
		int le=data.readInt();
		for(int i=0;i<le;i++)
		{
			int playerId=data.readInt();
			String info=data.readUTF();
			luckDraw.put(playerId,info);
		}
		// ����Ʒ��Ϣ
		le=data.readInt();
		for(int i=0;i<le;i++)
		{
			int playerId=data.readInt();
			int length=data.readInt();
			IntList list=new IntList();
			for(int j=0;j<length;j+=4)
			{
				list.add(data.readUnsignedByte());
				list.add(data.readUnsignedByte());
				list.add(data.readInt());
				list.add(data.readShort());
			}
			awardRecord.put(playerId,list);
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=super.getInitData();
		data.writeInt(luckDraw.size());
		int[] key=luckDraw.keyArray();
		String reslut="";
		for(int i=0;i<key.length;i++)
		{
			data.writeInt(key[i]);
			reslut=(String)luckDraw.get(key[i]);
			data.writeUTF(reslut);
		}
		data.writeInt(awardRecord.size());
		int [] playerIds=awardRecord.keyArray();
		for(int i=0;i<playerIds.length;i++)
		{
			data.writeInt(playerIds[i]);
			IntList list=(IntList)awardRecord.get(playerIds[i]);
			data.writeInt(list.size());
			for(int j=0;j<list.size();j+=4)
			{
				data.writeByte(list.get(j));
				data.writeByte(list.get(j+1));
				data.writeInt(list.get(j+2));
				data.writeShort(list.get(j+3));
			}
		}
		return data;
	}
	
	
	public IntKeyHashMap getLuckDraw()
	{
		return luckDraw;
	}

	
	public void setLuckDraw(IntKeyHashMap luckDraw)
	{
		this.luckDraw=luckDraw;
	}
	
	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendAwardShippingActivity(smap,this,null);
	}
	
	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeShort(gems);
		data.writeInt(endTime-TimeKit.getSecondTime());
		data.writeShort(times);
	}
	 @Override
	public Object copy(Object obj)
	{
		 AwardShippingActivity ac=(AwardShippingActivity)super.copy(obj);
		 ac.awardRecord=new IntKeyHashMap();
		 ac.luckDraw= new IntKeyHashMap();
		return ac;
	}

	public IntKeyHashMap getAwardRecord()
	{
		return awardRecord;
	}

	
	public void setAwardRecord(IntKeyHashMap awardRecord)
	{
		this.awardRecord=awardRecord;
	}
}
