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

/** 指挥中心数据 */
public class CommandProduce extends Produce
{
	/** 伪装，造势 */
	public static final int FLAGE=1,CAMPAIN=2;
	/** 1分钟产出资源 */
	public static final int ONE_MINUTE=60;
	/** 资源产出 */
	int resource[];
	// 伪装模式(造势，隐藏)
	int pretendType=CAMPAIN;
	// 是否应战,不应战则直接被掠夺
	boolean isFight=true;
	/** 序列化 上一次资源结算的时间 */
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
	 * @param isFight 要设置的 isFight
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
	 * @param pretendType 要设置的 pretendType
	 */
	public void setPretendType(int pretendType)
	{
		this.pretendType=pretendType;
	}
	/** 生产相关资源 lastProduceTime最后一次生产时间 */
	public int produce(Player player,int buildLevel,int checkTime,CreatObjectFactory objectFactory)
	{
		if(produceTime==0||(produceTime>checkTime)||buildLevel<=0) return 0;
		int now=checkTime;
		/** 过了多少秒 */
		int time=now-produceTime;
		if(time<ONE_MINUTE) return produceTime;
		int times=time/ONE_MINUTE;
		// 查看某种资源是否已达上限
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

	/** 获取加成后资源 */
	public int buffResource(int serviceType,int resource,Player player,
		int checkTime,int times)
	{
		return resource*times;
//		int endServiceResource=0;
//		int percent=100;
//		// 检查相对应的服务时间 时间过了和没过的情况
//		Service endService=player.checkSeriveTime(serviceType,checkTime);
//		// 服务已经结束 服务被移除 返回服务结束点时间
//		if(endService!=null)
//		{
//			// 结算最后服务应得的资源
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
	/** 获取某个BUFF的资源加成 */
	public int buffAddResource(int serviceType,int resource,Player player,int checkTime,int times)
	{
//		int buffResource=0;
//		int percent=100;
//		// 检查相对应的服务时间 时间过了和没过的情况
//		Service endService=player.checkSeriveTime1(serviceType,checkTime);
//		// 服务已经结束 服务被移除 返回服务结束点时间
//		if(endService!=null)
//		{
//			// 结算最后服务应得的资源
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
	
	/** 获取关卡BUFF加成 */
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

	/** 获取产量 未加成后 */
	public int noBuffProduceNum(int buildLevel,Player player)
	{
		if(resource==null) return 0;
		if(resource[buildLevel]!=0) return resource[buildLevel];
		return 0;
	}

	/** 获取产量 加成后 */
	public int produceNum(int buildLevel,Player player,int serviceType,
		int checkTime)
	{
		if(resource==null) return 0;
		if(resource[buildLevel]!=0)
			return buffResource(serviceType,resource[buildLevel],player,
				checkTime,1);
		return 0;
	}
	/** 获取永久性BUFF加成 */
	public int produceForeNum(int buildLevel,Player player,int serviceType,
		int checkTime)
	{
		if(resource==null) return 0;
		if(resource[buildLevel]!=0)
			return buffAddResource(serviceType,resource[buildLevel],player,
				checkTime,1);
		return 0;
	}
	
	/** 获取关卡BUFF加成 */
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
		// TODO 自动生成方法存根
	}
}