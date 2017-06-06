package foxu.sea.activity;

import java.util.Arrays;

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
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.FightScoreConst;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;
import foxu.sea.task.TaskEventExecute;

/**
 * ����̽��
 */
public class LuckyExploredActivity extends Activity implements ActivitySave
{

	/** ��Ʒ���� �� ��� ������Ƭ ��������  QUALITYSTUFF=5*/
	public static final int SHIP=1,PRO=2,START=3,EQUIP=4;
	/**��������**/
	public static final int AWARD_TYPE_ONE=1,AWARD_TYPE_TWO=2,AWARD_TYPE_THREE=3,AWARD_TYPE_FOUR=4;
	/** �������� */
	public static final int GRID_SIZE=30;
	/** ���������豦ʯ 1�� */
	private int diceGem;
	/** ���������豦ʯ 10�� */
	private int tenDiceGem;
	/** ��ҵ�ǰ����λ�� key��playerId value: intλ��,int���Ȧ��   Ȧ����ʱ���� �¸��汾ʹ��*/
	private IntKeyHashMap location;
	/** ��ͼ��Ӧ��Ʒ����  ��ʼ���������*/
	private int[] gridType;
	/**Ĭ��Ϊ0**/
	private  int times=0;
	
	/** ���Ϣ�������� */
	public static final int SAVE_CIRCLE=15*60;
	/** ��һ�λ��Ϣ����ʱ�� */
	int lastSaveTime;

	/** ����̽�ս�������  �� ��� ������Ƭ ���� */
	/**�����ϸ��  sid  ���� ���� ����**/
	public static int[] LUCKY_AWARD_ONE;
	public static int[] LUCKY_AWARD_TWO;
	public static int [] LUCKY_AWARD_THREE;
	public static int [] LUCKY_AWARD_FOUR;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		return resetActivity(stime,etime,initData,factoty);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		if(gridType==null) gridType=new int[GRID_SIZE];
		if(location==null) location=new IntKeyHashMap();
		String[] awards=TextKit.split(initData,";");
		LUCKY_AWARD_ONE=initAwardInfo(awards[0]);
		String[] data=awards[1].split("\\|");
		LUCKY_AWARD_TWO=initAwardInfo(data[0]);
		LUCKY_AWARD_THREE=initAwardInfo(data[1]);
		LUCKY_AWARD_FOUR=initAwardInfo(data[2]);
		diceGem=Integer.parseInt(data[3]);
		tenDiceGem=Integer.parseInt(data[4]);
		if(data[5]!=null && data[3].equals("no_limit"))
			times=-1;
		else if(data[5]!=null)
			times=Integer.parseInt(data[5]);
		initGridType();
		startTime=SeaBackKit.parseFormatTime(stime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		endTime=SeaBackKit.parseFormatTime(etime);
		return getActivityState();
	}

	/**
	 * ��ʼ�����ӽ�������
	 */
	private void initGridType()
	{
		if(gridType==null) return;
		for(int i=0;i<gridType.length;i++)
		{
			int type=MathKit.randomValue(1,5);
			gridType[i]=type;
			if(i!=0&&i%4==0)
			{
				if(checkGridType(i))
				{
					gridType[i]=AWARD_TYPE_TWO;
				}
			}
		}
	}

	/**������Ƿ��������� ÿ4��������һ�����͵� **/
	public boolean checkGridType(int i)
	{
		for(int j=i-3;j<=i;j++)
		{
			if(gridType[j]==AWARD_TYPE_TWO) return false;
		}
		return true;
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
			.append(",\"others\":\"")
			.append("[initData]:"+Arrays.toString(gridType))
			.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		if(gridType==null) gridType=new int[GRID_SIZE];
		if(location==null) location=new IntKeyHashMap();
		diceGem=data.readInt();
		tenDiceGem=data.readInt();
		times=data.readInt();
		int length=data.readInt();
		for(int i=0;i<length;i++)
		{
			int playerId=data.readInt();
			String value=data.readUTF();
			location.put(playerId,value);
		}
		length=data.readInt();
		LUCKY_AWARD_ONE=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_ONE[i]=data.readInt();
		}
		length=data.readInt();
		LUCKY_AWARD_TWO=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_TWO[i]=data.readInt();
		}
		length=data.readInt();
		LUCKY_AWARD_THREE=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_THREE[i]=data.readInt();
		}
		length=data.readInt();
		LUCKY_AWARD_FOUR=new int[length];
		for(int i=0;i<length;i++)
		{
			LUCKY_AWARD_FOUR[i]=data.readInt();
		}
		initGridType();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeInt(diceGem);
		data.writeInt(tenDiceGem);
		data.writeInt(times);
		int[] keyArr=location.keyArray();
		// �����Ϸ��Ϣ
		data.writeInt(keyArr.length);
		for(int i=0;i<keyArr.length;i++)
		{
			data.writeInt(keyArr[i]);
			data.writeUTF((String)location.get(keyArr[i]));
		}
		data.writeInt(LUCKY_AWARD_ONE.length);
		for(int i=0;i<LUCKY_AWARD_ONE.length;i++)
		{
			data.writeInt(LUCKY_AWARD_ONE[i]);
		}
		data.writeInt(LUCKY_AWARD_TWO.length);
		for(int i=0;i<LUCKY_AWARD_TWO.length;i++)
		{
			data.writeInt(LUCKY_AWARD_TWO[i]);
		}
		data.writeInt(LUCKY_AWARD_THREE.length);
		for(int i=0;i<LUCKY_AWARD_THREE.length;i++)
		{
			data.writeInt(LUCKY_AWARD_THREE[i]);
		}
		data.writeInt(LUCKY_AWARD_FOUR.length);
		for(int i=0;i<LUCKY_AWARD_FOUR.length;i++)
		{
			data.writeInt(LUCKY_AWARD_FOUR[i]);
		}
		// ��Ʒ��Ϣ
		// if(awardInfo!=null) data.writeUTF(awardInfo);
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeShort(diceGem);
		data.writeShort(tenDiceGem);
		data.writeInt(endTime-TimeKit.getSecondTime());
		data.writeShort(times);
	}
	
	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		data.writeShort(getSid());
		data.writeShort(diceGem); // ÿ�����豦ʯ
		data.writeShort(tenDiceGem); // ÿʮ�����豦ʯ
		data.writeInt(endTime); // ����ʱ��
		data.writeByte(getLocation(player.getId())); // �������λ��
		data.writeShort(times);//����
		data.writeShort(getTimesNum(player.getId()));//��ҵĳ齱����
//		data.writeInt(getFinishNum(player.getId())); // ������Ȧ��
		data.writeByte(gridType.length);	//���ӵĽ�Ʒ����
		for(int i=0;i<gridType.length;i++)
			data.writeShort(gridType[i]);
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		JBackKit.sendLuckActivity(smap,this,null);
	}

	/** ������ 1-6 count:���Ĵ��� */
	private int dice()
	{
		return MathKit.randomValue(1,7);
	}

	/** ��ȡ��ҵ�ǰλ�� */
	public int getLocation(int playerId)
	{
		if(getPlayerInfo(playerId)==null)
			return 0;
		String location=getPlayerInfo(playerId)[0];
		return Integer.parseInt(location);
	}

	/** ��ȡ������Ȧ�� */
	public int getFinishNum(int playerId)
	{
		if(getPlayerInfo(playerId)==null)
			return 0;
		String num=getPlayerInfo(playerId)[1];
		return Integer.parseInt(num);
	}

	/**��ȡ��ҵĳ齱����**/
	public int getTimesNum(int playerId)
	{
		if(getPlayerInfo(playerId)==null)
			return 0;
		String num=getPlayerInfo(playerId)[2];
		return Integer.parseInt(num);
	}
	
	/**��ȡ��ҵ�Ȧ�� �����ڵ�λ�ú���ҳ齱�Ĵ���**/
	private String[] getPlayerInfo(int playerId)
	{
		if(location.get(playerId)==null||"".equals(location.get(playerId)))
			return null;
		String info=(String)location.get(playerId);
		String[] infoArr=info.split(",");
		return infoArr;
	}

	/** �����û����Ϣ */
	public void putPlayerInfo(int playerId,int playerLoc,int finishNum,int times)
	{
		String value=playerLoc+","+finishNum+","+times;
		location.put(playerId,value);
	}

	/** ��������� ����ȡ���� 	diceType 0��1��ɸ��  1��10�� */
	public void move(Player player,int diceType,ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		int gems;
		int diceNum=0;
		if(diceType==0)
		{
			gems=diceGem;
		}
		else if(diceType==1)
		{
			gems=tenDiceGem;
		}
		else
			throw new DataAccessException(0,"diceType error");
		if(!Resources.checkGems(gems,player.getResources()))
		{
			throw new DataAccessException(0,"not enough gems");
		}
		Resources.reduceGems(gems,player.getResources(),player);
		objectFactory.createGemTrack(GemsTrack.LUCKY_EXPLORED,
			player.getId(),gems,0,Resources.getGems(player.getResources()));
		// ����change��Ϣ
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.GEMS_ADD_SOMETHING,this,player,gems);
		if(diceType==0)
		{
			data.writeByte(1);		//�齱����
			diceNum+=reward(player,data,objectFactory);
			data.writeByte(diceNum);	//���ӽ��
			data.writeByte(getLocation(player.getId()));	//���λ��
		}
		else if(diceType==1)
		{
			gems=tenDiceGem;
			data.writeByte(10);		//�齱����
			int [] diceNums=new int[10];
			for(int i=0;i<10;i++)
				diceNums[i]=reward(player,data,objectFactory);
			data.writeByte(getLocation(player.getId()));	//���λ��
			data.writeByte(diceNums.length);
			for(int i=0;i<diceNums.length;i++)
			{
				data.writeByte(diceNums[i]);
			}
			
		}
		data.writeShort(getTimesNum(player.getId()));
	}

	/** ���� �������ӽ�� */
	public int reward(Player player,ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		int diceNum=dice();
		int playerLocation=getLocation(player.getId());
		int finishNum=getFinishNum(player.getId());
		int playerTimes=getTimesNum(player.getId());
		playerLocation+=diceNum;
		// Ȧ��
		finishNum+=playerLocation/(GRID_SIZE);
		// ��ǰλ��
		playerLocation=playerLocation%(GRID_SIZE);
		putPlayerInfo(player.getId(),playerLocation,finishNum,playerTimes+1);
		// ����λ�ý�������
		int awardType=gridType[playerLocation];
		Award aw=new Award();
		if(awardType==AWARD_TYPE_ONE)
			aw.setRandomProps(LUCKY_AWARD_ONE);
		else if(awardType==AWARD_TYPE_TWO)
			aw.setRandomProps(LUCKY_AWARD_TWO);
		else if(awardType==AWARD_TYPE_THREE)
			aw.setRandomProps(LUCKY_AWARD_THREE);
		else 
			aw.setRandomProps(LUCKY_AWARD_FOUR);
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"luckyexploredactivity_message");
		message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
		aw.awardSelf(player,TimeKit.getSecondTime(),data,objectFactory,message,
			SeaBackKit.getLuckySids(),new int[]{EquipmentTrack.FROM_LUCKY_EXPLORED,FightScoreConst.NEW_ACTIVITY});
		return diceNum;
	}

//	/**��ȡ����Ʒ����Ϣ**/
//	public Award randomLuckyExplored(int [] awardInfo)
//	{
//		// ��ʼ�Ļ���
//		int startProbability=0;
//		int random=MathKit.randomValue(0,Award.PROB_ABILITY);			
//		for(int i=0;i<awardInfo.length;i+=3)
//		{
//				// ���������������Χ��
//			if(random>=startProbability&&random<=awardInfo[i+2])
//			{
//				Award ad=(Award)Award.factory.newSample(65051);
//				int[] awardSid={awardInfo[i],awardInfo[i+1]};
//				SeaBackKit.resetAward(ad,awardSid);
//				return ad;
//			}
//			startProbability=awardInfo[i+2];
//		}
//		return null;
//	}
	
	
	
	
	public int getDiceGem()
	{
		return diceGem;
	}

	public int getTenDiceGem()
	{
		return tenDiceGem;
	}
	
	/**���ý���Ʒ��Ϣ**/
	public  int [] initAwardInfo(String  award)
	{
		if(award==null) return null;
		String[] awardInfo=TextKit.split(award,",");
		int[] awards=new int[awardInfo.length];
		for(int i=0;i<awardInfo.length;i++)
		{
			awards[i]=TextKit.parseInt(awardInfo[i]);
		}
		return awards;
	}
	/**��֤�µ�ǰ�����齱����**/
	public  boolean checkTimes(int playerId,int time)
	{
		if(times==-1) return true;
		int count=getTimesNum(playerId);
		if(times==0 || times<=count || times<time+count)
			return false;
		return true;
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
	public Object copy(Object obj)
	{
		 LuckyExploredActivity ac=(LuckyExploredActivity)super.copy(obj);
		 ac.location=new IntKeyHashMap();
		return ac;
	}
	
}
