package foxu.sea.builds.produce;

import mustang.io.ByteBuffer;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.builds.Build;
import foxu.sea.builds.Produce;
import foxu.sea.builds.Product;
import foxu.sea.checkpoint.Chapter;

/** ָ���������� */
public class CommandProduce extends Produce
{
	/** αװ������ */
	public static final int FLAGE=1,CAMPAIN=2;
	/** 1���Ӳ�����Դ */
	public static final int ONE_MINUTE=60;
	/** ��Դ���� */
	int resource[];
	// αװģʽ(���ƣ�����)
	int pretendType=CAMPAIN;
	// �Ƿ�Ӧս,��Ӧս��ֱ�ӱ��Ӷ�
	boolean isFight=true;
	/** ���л� ��һ����Դ�����ʱ�� */
	int produceTime;

	public void init(int checkTime)
	{
		produceTime=checkTime;
	}

	public void bytesWrite(ByteBuffer data)
	{
		data.writeByte(pretendType);
		data.writeBoolean(isFight);
		data.writeInt(produceTime);
	}
	public Object bytesRead(ByteBuffer data)
	{
		pretendType=data.readUnsignedByte();
		isFight=data.readBoolean();
		produceTime=data.readInt();
		return this;
	}
	public void showBytesWrite(ByteBuffer data,int current)
	{
		data.writeInt(produceTime);
		data.writeByte(pretendType);
		data.writeBoolean(isFight);
	}

	/**
	 * @return isFight
	 */
	public boolean isFight()
	{
		return isFight;
	}

	/**
	 * @param isFight Ҫ���õ� isFight
	 */
	public void setFight(boolean isFight)
	{
		this.isFight=isFight;
	}

	/**
	 * @return pretendType
	 */
	public int getPretendType()
	{
		return pretendType;
	}

	/**
	 * @param pretendType Ҫ���õ� pretendType
	 */
	public void setPretendType(int pretendType)
	{
		this.pretendType=pretendType;
	}
	/** ���������Դ lastProduceTime���һ������ʱ�� */
	public int produce(Player player,int buildLevel,int checkTime,CreatObjectFactory objectFactory)
	{
		if(produceTime==0||(produceTime>checkTime)||buildLevel<=0) return 0;
		int now=checkTime;
		/** ���˶����� */
		int time=now-produceTime;
		if(time<ONE_MINUTE) return produceTime;
		int times=time/ONE_MINUTE;
		// �鿴ĳ����Դ�Ƿ��Ѵ�����
		int metal=Resources.filerFullResource(
			buffResource(PublicConst.ADD_METAL_BUFF,resource[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_METAL_BUFF,resource[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.METAL,resource[buildLevel-1],player,times),
			Resources.METAL,Build.BUILD_METAL,player);
		int oil=Resources.filerFullResource(
			buffResource(PublicConst.ADD_OIL_BUFF,resource[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_OIL_BUFF,resource[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.OIL,resource[buildLevel-1],player,times),
			Resources.OIL,Build.BUILD_OIL,player);
		int silicon=Resources.filerFullResource(
			buffResource(PublicConst.ADD_SILICON_BUFF,resource[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_SILICON_BUFF,resource[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.SILICON,resource[buildLevel-1],player,times),
			Resources.SILICON,Build.BUILD_SILION,player);
		int uranium=Resources.filerFullResource(
			buffResource(PublicConst.ADD_URANIUM_BUFF,resource[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_URANIUM_BUFF,resource[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.URANIUM,resource[buildLevel-1],player,times),
			Resources.URANIUM,Build.BUILD_URANIUM,player);
		int money=Resources.filerFullResource(
			buffResource(PublicConst.ADD_MONEY_BUFF,resource[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_MONEY_BUFF,resource[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.MONEY,resource[buildLevel-1],player,times),
			Resources.MONEY,Build.BUILD_MONEY,player);
		Resources.addResources(player.getResources(),metal,oil,silicon,
			uranium,money,player);
		produceTime=now;
		return produceTime;
	}

	/** ��ȡ�ӳɺ���Դ */
	public int buffResource(int serviceType,int resource,Player player,
		int checkTime,int times)
	{
		return resource*times;
//		int endServiceResource=0;
//		int percent=100;
//		// ������Ӧ�ķ���ʱ�� ʱ����˺�û�������
//		Service endService=player.checkSeriveTime(serviceType,checkTime);
//		// �����Ѿ����� �����Ƴ� ���ط��������ʱ��
//		if(endService!=null)
//		{
//			// ����������Ӧ�õ���Դ
//			int disTime=endService.getEndTime()-produceTime;
//			if(disTime>0)
//			{
//				int distimes=disTime/ONE_MINUTE;
//				if(disTime>times) disTime=times;
//				endServiceResource=resource*(percent+endService.getValue())
//					/percent*distimes;
//			}
//		}
//		AdjustmentData data=((AdjustmentData)player.getAdjstment()
//			.getAdjustmentValue(serviceType));
//		if(data!=null) percent+=data.percent;
//		return resource*percent/100*times+endServiceResource;
	}
	/** ��ȡĳ��BUFF����Դ�ӳ� */
	public int buffAddResource(int serviceType,int resource,Player player,int checkTime,int times)
	{
//		int buffResource=0;
//		int percent=100;
//		// ������Ӧ�ķ���ʱ�� ʱ����˺�û�������
//		Service endService=player.checkSeriveTime1(serviceType,checkTime);
//		// �����Ѿ����� �����Ƴ� ���ط��������ʱ��
//		if(endService!=null)
//		{
//			// ����������Ӧ�õ���Դ
//			int disTime=endService.getEndTime()-produceTime;
//			if(disTime>0)
//			{
//				int distimes=disTime/ONE_MINUTE;
//				if(disTime>times) disTime=times;
//				buffResource=(int)((long)resource*endService.getValue()*distimes/percent);
//			}
//		}
		return 0;
	}
	
	/** ��ȡ�ؿ�BUFF�ӳ� */
	public int pointAddReasouce(int type,int resource,Player player,int times)
	{
//		int level=player.getPointBuffLv(type);
//		if(level<=0)return 0;
//		Chapter buff=(Chapter)Chapter.factory.getSample(PublicConst.SIDS[type]);
//		int addvalue=buff.getAddValue(level);
//		int disTime=TimeKit.getSecondTime()-produceTime;
//		if(disTime<=0)return 0;
//		int distimes=disTime/ONE_MINUTE;
//		if(distimes>times) distimes=times;
//		return (int)((long)resource*addvalue*distimes/100);
		return 0;
	}

	/** ��ȡ���� δ�ӳɺ� */
	public int noBuffProduceNum(int buildLevel,Player player)
	{
		if(resource==null) return 0;
		if(resource[buildLevel]!=0) return resource[buildLevel];
		return 0;
	}

	/** ��ȡ���� �ӳɺ� */
	public int produceNum(int buildLevel,Player player,int serviceType,
		int checkTime)
	{
		if(resource==null) return 0;
		if(resource[buildLevel]!=0)
			return buffResource(serviceType,resource[buildLevel],player,
				checkTime,1);
		return 0;
	}
	/** ��ȡ������BUFF�ӳ� */
	public int produceForeNum(int buildLevel,Player player,int serviceType,
		int checkTime)
	{
		if(resource==null) return 0;
		if(resource[buildLevel]!=0)
			return buffAddResource(serviceType,resource[buildLevel],player,
				checkTime,1);
		return 0;
	}
	
	/** ��ȡ�ؿ�BUFF�ӳ� */
	public float producePointNum(int type,int resource,Player player)
	{
//		float base=1.0f;
//		int level=player.getPointBuffLv(type);
//		if(level<=0)return 0;
//		Chapter buff=(Chapter)Chapter.factory.getSample(PublicConst.SIDS[type]);
//		int addvalue=buff.getAddValue(level);
//		return base*resource*addvalue/100;
		return 0;
	}

	public boolean checkProduce(Player player)
	{
		return true;
	}

	public void addProduct(Player player,Product p)
	{
		// TODO �Զ����ɷ������
	}
}