package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.net.DataAccessException;
import mustang.set.IntKeyHashMap;
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

/**
 * ȫ����"��"�
 */
public class RobFestivalActivity extends AwardActivity implements ActivitySave
{

	public static int ROB_ATYPE=4;
	IntKeyHashMap robFestivalInfo=new IntKeyHashMap();
	
	/** ���Ϣ�������� */
	public static final int SAVE_CIRCLE=15*60;
	/** ��һ�λ��Ϣ����ʱ�� */
	int lastSaveTime;
	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		//ȫ����"��"ʹ���������л�
		data.writeShort(getSid());	//�sid
		data.writeShort(gems);		//ÿ�����豦ʯ
		data.writeInt(endTime);		//����ʱ��
		data.writeShort(getRobFestivalTimes(player.getId()));
		data.writeShort(times);		//�ɳ齱�ܴ���
		data.writeShort(getRobFestivalLocation(player.getId()));
	}

	/**
	 * �齱
	 * @param data
	 * @param player
	 * @param factory
	 */
	public void draw(ByteBuffer data,Player player,CreatObjectFactory factory)
	{
		int nowLoca=data.readUnsignedShort();
		int count=getRobFestivalTimes(player.getId());
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
				factory.createGemTrack(GemsTrack.LUCKY_DRAW_ROB,
					player.getId(),gems,0,
					Resources.getGems(player.getResources()));
				// ������¼
				data.clear();
				data.writeShort(nowLoca);
				// ��Ʒ������� 1-3
				int len=getRandomNum();
				data.writeByte(len);
				for(int i=0;i<len;i++)
				{
					String message=InterTransltor.getInstance().getTransByKey(
						PublicConst.SERVER_LOCALE,"robfestivalactivity_message");
					message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
					getAward().awardSelf(player,TimeKit.getSecondTime(),data,factory,message,
						SeaBackKit.getLuckySids(),new int[]{EquipmentTrack.FROM_ROB_LUCKY,FightScoreConst.NEW_ACTIVITY});
				}
				setRobFestivalInfo(player.getId(),nowLoca);
				// ����change��Ϣ
				TaskEventExecute.getInstance().executeEvent(
					PublicConst.GEMS_ADD_SOMETHING,this,player,gems);
			}
		}
	}
	
	/**
	 * ��ȡ�鵽�Ľ�Ʒ�������� 1-3
	 */
	private int getRandomNum()
	{
		float random=MathKit.randomValue(0.00f,1.0f);
		if(random<PublicConst.ROB_ACTIVITY_AWARD_ODD[0])
			return 3;
		else if(random<PublicConst.ROB_ACTIVITY_AWARD_ODD[1])
			return 2;
		return 1;
	}

	public IntKeyHashMap getRobFestivalInfo()
	{
		return robFestivalInfo;
	}

	
	public void setRobFestivalInfo(IntKeyHashMap robFestivalInfo)
	{
		this.robFestivalInfo=robFestivalInfo;
	}
	
	/**���ó齱��Ϣ**/
	public void setRobFestivalInfo(int playerId,int location)
	{
		int times=getRobFestivalTimes(playerId)+1;
		robFestivalInfo.put(playerId,times+","+location);
	}
	
	/**��ȡ�齱����**/
	public int getRobFestivalTimes(int playerId)
	{
		String result=(String)robFestivalInfo.get(playerId);
		if(result==null||result.length()==0) return 0;
		String[] info=TextKit.split(result,",");
		return TextKit.parseInt(info[0]);
	}
	
	/**��ȡ�ϴε�����**/
	public int getRobFestivalLocation(int playerId)
	{
		String result=(String)robFestivalInfo.get(playerId);
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
			robFestivalInfo.put(playerId,info);
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=super.getInitData();
		data.writeInt(robFestivalInfo.size());
		int[] key=robFestivalInfo.keyArray();
		String reslut="";
		for(int i=0;i<key.length;i++)
		{
			data.writeInt(key[i]);
			reslut=(String)robFestivalInfo.get(key[i]);
			data.writeUTF(reslut);
		}
		return data;
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendRobActivity(smap,this,null);
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
		 RobFestivalActivity ac=(RobFestivalActivity)super.copy(obj);
		 ac.robFestivalInfo=new IntKeyHashMap();
		return ac;
	}
}
