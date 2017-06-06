package foxu.sea.builds.produce;

import mustang.io.ByteBuffer;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Resources;
import foxu.sea.Service;
import foxu.sea.builds.Build;
import foxu.sea.builds.Produce;
import foxu.sea.builds.Product;
import foxu.sea.checkpoint.Chapter;

/**
 * 资源生产 author:icetiger
 */
public class ResourcesProduce extends Produce
{

	/** 1分钟产出资源 */
	public static final int ONE_MINUTE=60;
	/** 序列化 上一次资源结算的时间 */
	int produceTime;

	/** configure fileds */
	/** 配置 每种资源矿厂的生产资源 */
	/** 金属 */
	int metal[]=new int[PublicConst.MAX_BUILD_LEVEL];
	/** 石油 */
	int oil[]=new int[PublicConst.MAX_BUILD_LEVEL];
	/** 硅 */
	int silicon[]=new int[PublicConst.MAX_BUILD_LEVEL];
	/** 低铀 */
	int uranium[]=new int[PublicConst.MAX_BUILD_LEVEL];
	/** 金钱 */
	int money[]=new int[PublicConst.MAX_BUILD_LEVEL];
	/** 物品消耗 */
	int propSids[]=new int[PublicConst.MAX_BUILD_LEVEL];

	public void init(int checkTime)
	{
		produceTime=checkTime;

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
		int metalLimit=Resources.filerFullResource(
			(int)buffResource(PublicConst.ADD_METAL_BUFF,metal[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_METAL_BUFF,metal[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.METAL,metal[buildLevel-1],player,times)+
			prosperityAddReasource(metal[buildLevel-1],player),
			Resources.METAL,Build.BUILD_METAL,player);
		int oilLimit=Resources.filerFullResource(
			(int)buffResource(PublicConst.ADD_OIL_BUFF,oil[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_OIL_BUFF,oil[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.OIL,oil[buildLevel-1],player,times)+
			prosperityAddReasource(oil[buildLevel-1],player),
			Resources.OIL,Build.BUILD_OIL,player);
		int siliconLimit=Resources.filerFullResource(
			(int)buffResource(PublicConst.ADD_SILICON_BUFF,silicon[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_SILICON_BUFF,silicon[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.SILICON,silicon[buildLevel-1],player,times)+
			prosperityAddReasource(silicon[buildLevel-1],player),
			Resources.SILICON,Build.BUILD_SILION,player);
		int uraniumLimit=Resources.filerFullResource(
			(int)buffResource(PublicConst.ADD_URANIUM_BUFF,uranium[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_URANIUM_BUFF,uranium[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.URANIUM,uranium[buildLevel-1],player,times)+
			prosperityAddReasource(uranium[buildLevel-1],player),
			Resources.URANIUM,Build.BUILD_URANIUM,player);
		int moneyLimit=Resources.filerFullResource(
			(int)buffResource(PublicConst.ADD_MONEY_BUFF,money[buildLevel-1],player,checkTime,times)+
			buffAddResource(PublicConst.FORE_MONEY_BUFF,money[buildLevel-1],player,checkTime,times)+
			pointAddReasouce(Chapter.MONEY,money[buildLevel-1],player,times)+
			prosperityAddReasource(money[buildLevel-1],player),
			Resources.MONEY,Build.BUILD_MONEY,player);
		Resources.addResources(player.getResources(),metalLimit,oilLimit,
			siliconLimit,uraniumLimit,moneyLimit,player);
		produceTime=now;
		return produceTime;
	}

	/** 获取加成后资源 */
	public float buffResource(int serviceType,int resource,Player player,
		int checkTime,int times)
	{
		float base=1.0f;
		float endServiceResource=0;
		int percent=100;
		// 检查相对应的服务时间 时间过了和没过的情况
		Service endService=player.checkSeriveTime(serviceType,checkTime);
		// 服务已经结束 服务被移除 返回服务结束点时间
		if(endService!=null&&times!=0)
		{
			// 结算最后服务应得的资源
			int disTime=endService.getEndTime()-produceTime;
			if(disTime>0)
			{
				int distimes=disTime/ONE_MINUTE;
				endServiceResource=base*resource*(percent+endService.getValue())
					/percent*distimes;
			}
		}
		AdjustmentData data=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(serviceType));
		if(data!=null) percent+=data.percent;
		if(times==0)times=1;
		return base*resource*percent/100*times+endServiceResource;
	}
	
	/** 获取某个BUFF的资源加成 */
	public int buffAddResource(int serviceType,int resource,Player player,int checkTime,int times)
	{
		int buffResource=0;
		int percent=100;
		// 检查相对应的服务时间 时间过了和没过的情况
		Service endService=player.checkSeriveTime1(serviceType,checkTime);
		// 服务已经结束 服务被移除 返回服务结束点时间
		if(endService!=null)
		{
			// 结算最后服务应得的资源
			int disTime=endService.getEndTime()-produceTime;
			if(disTime>0)
			{
				int distimes=disTime/ONE_MINUTE;
				if(disTime>times) disTime=times;
				buffResource=(int)((long)resource*endService.getValue()*distimes/percent);
			}
		}
		return buffResource;
	}
	/** 获取关卡BUFF加成 */
	public int pointAddReasouce(int type,int resource,Player player,int times)
	{
		int level=player.getPointBuffLv(type);
		if(level<=0)return 0;
		Chapter buff=(Chapter)Chapter.factory.getSample(PublicConst.SIDS[type]);
		int addvalue=buff.getAddValue(level);
		int disTime=TimeKit.getSecondTime()-produceTime;
		if(disTime<=0)return 0;
		int distimes=disTime/ONE_MINUTE;
		if(distimes>times) distimes=times;
		return (int)((long)resource*addvalue*distimes/100);
	}
	
	/**
	 * 获取繁荣度BUFF加成
	 * @return
	 */
	public int prosperityAddReasource(int resource,Player player){
		if(player.getProsperityInfo()==null)
			return 0;
		
		int lv = player.getProsperityInfo()[3];//繁荣度等级
		int buff = Player.PROSPERITY_lV_BUFF[lv*3+1];//加成基数
		
		int disTime=TimeKit.getSecondTime()-produceTime;
		if(disTime<=0)
			return 0;
		int distimes=disTime/ONE_MINUTE;
		return (int)((long)resource*buff*distimes/100);
	}
	

	/** 获取某个BUFF的资源产能加成 */
	public float buffSpeedResource(int serviceType,int resource,Player player,int checkTime)
	{
		float base=1.0f;
		float buffResource=0;
		int percent=100;
		// 检查相对应的服务时间 时间过了和没过的情况
		Service endService=player.checkSeriveTime1(serviceType,checkTime);
		// 服务已经结束 服务被移除 返回服务结束点时间
		if(endService!=null)
		{
			buffResource=base*resource*endService.getValue()/percent;
		}
		return buffResource;
	}
	/** 获取关卡BUFF产能加成 */
	public float pointSpeedReasouce(int type,int resource,Player player)
	{
		float base=1.0f;
		int level=player.getPointBuffLv(type);
		if(level<=0)return 0;
		Chapter buff=(Chapter)Chapter.factory.getSample(PublicConst.SIDS[type]);
		int addvalue=buff.getAddValue(level);
		return base*resource*addvalue/100;
	}
	/** 获取繁荣度产能加成 */
	public float prosperitySpeedReasouce(int type,int resource,Player player){
		if(player.getProsperityInfo()==null)
			return 0;
		float base=1.0f;
		int lv = player.getProsperityInfo()[3];//繁荣度等级
		int buff = Player.PROSPERITY_lV_BUFF[lv*3+1];//加成基数
		return base*resource*buff/100;
	}
	
	/** 获取产量 未加成的 */
	public int noBuffProduceNum(int buildLevel,Player player)
	{
		if(metal[buildLevel]!=0)
			return metal[buildLevel];
		else if(oil[buildLevel]!=0)
			return oil[buildLevel];
		else if(silicon[buildLevel]!=0)
			return silicon[buildLevel];
		else if(uranium[buildLevel]!=0)
			return uranium[buildLevel];
		else if(money[buildLevel]!=0) return money[buildLevel];
		return 0;
	}

	/** 获取产量 加成后的 */
	public float produceNum(int buildLevel,Player player,int checkTime)
	{
		if(metal[buildLevel]!=0)
			return buffResource(PublicConst.ADD_METAL_BUFF,
				metal[buildLevel],player,checkTime,0)
				+buffSpeedResource(PublicConst.FORE_METAL_BUFF,
					metal[buildLevel],player,checkTime)
				+pointSpeedReasouce(Chapter.METAL,metal[buildLevel],player)
				+prosperitySpeedReasouce(Chapter.METAL,metal[buildLevel],player);
		else if(oil[buildLevel]!=0)
			return buffResource(PublicConst.ADD_OIL_BUFF,oil[buildLevel],
				player,checkTime,0)
				+buffSpeedResource(PublicConst.FORE_OIL_BUFF,oil[buildLevel],
					player,checkTime)
				+pointSpeedReasouce(Chapter.OIL,oil[buildLevel],player)
				+prosperitySpeedReasouce(Chapter.OIL,oil[buildLevel],player);
		else if(silicon[buildLevel]!=0)
			return buffResource(PublicConst.ADD_SILICON_BUFF,
				silicon[buildLevel],player,checkTime,0)
				+buffSpeedResource(PublicConst.FORE_SILICON_BUFF,
					silicon[buildLevel],player,checkTime)
				+pointSpeedReasouce(Chapter.SILICON,silicon[buildLevel],
					player)
				+prosperitySpeedReasouce(Chapter.SILICON,silicon[buildLevel],player);
		else if(uranium[buildLevel]!=0)
			return buffResource(PublicConst.ADD_URANIUM_BUFF,
				uranium[buildLevel],player,checkTime,0)
				+buffSpeedResource(PublicConst.FORE_URANIUM_BUFF,
					uranium[buildLevel],player,checkTime)
				+pointSpeedReasouce(Chapter.URANIUM,uranium[buildLevel],
					player)
				+prosperitySpeedReasouce(Chapter.URANIUM,uranium[buildLevel],player);
		else if(money[buildLevel]!=0)
			return buffResource(PublicConst.ADD_MONEY_BUFF,
				money[buildLevel],player,checkTime,0)
				+buffSpeedResource(PublicConst.FORE_MONEY_BUFF,
					money[buildLevel],player,checkTime)
				+pointSpeedReasouce(Chapter.MONEY,money[buildLevel],player)
				+prosperitySpeedReasouce(Chapter.MONEY,money[buildLevel],player);
		return 0;
	}

	public boolean checkProduce(Player player)
	{
		return true;
	}

	/**
	 * @return metal
	 */
	public int[] getMetal()
	{
		return metal;
	}

	/**
	 * @param metal 要设置的 metal
	 */
	public void setMetal(int[] metal)
	{
		this.metal=metal;
	}

	public Object bytesRead(ByteBuffer data)
	{
		produceTime=data.readInt();
		return this;
	}

	/** 将对象的域序列化到字节缓存中 */
	public void bytesWrite(ByteBuffer data)
	{
		data.writeInt(produceTime);
	}

	public void addProduct(Player player,Product p)
	{

	}

	public void showBytesWrite(ByteBuffer data,int current)
	{
		data.writeInt(produceTime);
	}
}
