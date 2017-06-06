package foxu.sea;

import mustang.util.TimeKit;
import foxu.sea.AttrAdjustment.AdjustmentData;
import foxu.sea.achieve.AchieveCollect;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.ConsumeGemsActivity;
import foxu.sea.activity.DoubleGemsAcitivity;
import foxu.sea.activity.PayRelayActivity;
import foxu.sea.activity.TotalBuyActivity;
import foxu.sea.activity.WebShowActivity;
import foxu.sea.builds.AutoUpBuildManager;
import foxu.sea.builds.Build;
import foxu.sea.builds.PlayerBuild;
import foxu.sea.comrade.ComradeHandler;
import foxu.sea.comrade.ComradeTask;
import foxu.sea.kit.JBackKit;

/**
 * 资源类 author:icetiger
 */
public class Resources
{
	static AutoUpBuildManager autoUpBuilding;
	
	/** 资源index GEMS充值宝石 SGEMS非充值宝石*/
	public static final int METAL=0,OIL=1,SILICON=2,URANIUM=3,MONEY=4,
					GEMS=5,MAXGEMS=6,HONOR=7,SGEMS=8;

	/** 获取当前宝石总量 */
	public static long getGems(long resources[])
	{
		return resources[GEMS]+ resources[SGEMS];
	}
	
	/** 检查宝石是否足够 */
	public static boolean checkGems(int gems,long resources[])
	{
		return resources[GEMS]+ resources[SGEMS]>=gems;
	}

	/** 检查资源是否足够 */
	public static boolean checkResources(int metal,int oil,int silicon,
		int uranium,int money,long resources[])
	{
		if(resources[METAL]<metal||resources[OIL]<oil
			||resources[SILICON]<silicon||resources[URANIUM]<uranium
			||resources[MONEY]<money) return false;
		return true;
	}

	/** 检查资源是否足够 */
	public static boolean checkResources(int needResources[],long resources[])
	{
		if(resources[METAL]<needResources[METAL]
			||resources[OIL]<needResources[OIL]
			||resources[SILICON]<needResources[SILICON]
			||resources[URANIUM]<needResources[URANIUM]
			||resources[MONEY]<needResources[MONEY]
			||!checkGems(needResources[GEMS],resources)) return false;
		return true;
	}

	/** 检查资源是否足够 */
	public static boolean checkResources(long needResources[],
		long resources[],int rate)
	{
		if(resources[METAL]<needResources[METAL]*rate
			||resources[OIL]<needResources[OIL]*rate
			||resources[SILICON]<needResources[SILICON]*rate
			||resources[URANIUM]<needResources[URANIUM]*rate
			||resources[MONEY]<needResources[MONEY]*rate) return false;
		return true;
	}
	
	/** 检查资源是否足够 */
	public static boolean checkResources(int needResources[],
		long resources[],int rate)
	{
		if(resources[METAL]<needResources[METAL]*rate
			||resources[OIL]<needResources[OIL]*rate
			||resources[SILICON]<needResources[SILICON]*rate
			||resources[URANIUM]<needResources[URANIUM]*rate
			||resources[MONEY]<needResources[MONEY]*rate) return false;
		return true;
	}

	/** 增加资源 */
	public static int addResources(long resources[],int changeResources[],
		Player player)
	{
		resources[METAL]+=changeResources[METAL];
		if(resources[METAL]<0)
		{
			if(changeResources[METAL]>0)
			{
				resources[METAL]=Long.MAX_VALUE;
			}
			else
			{
				resources[METAL]=0;
			}
		}

		resources[OIL]+=changeResources[OIL];
		if(resources[OIL]<0)
		{
			if(changeResources[OIL]>0)
			{
				resources[OIL]=Long.MAX_VALUE;
			}
			else
			{
				resources[OIL]=0;
			}
		}

		resources[SILICON]+=changeResources[SILICON];
		if(resources[SILICON]<0)
		{
			if(changeResources[SILICON]>0)
			{
				resources[SILICON]=Long.MAX_VALUE;
			}
			else
			{
				resources[SILICON]=0;
			}
		}

		resources[URANIUM]+=changeResources[URANIUM];
		if(resources[URANIUM]<0)
		{
			if(changeResources[URANIUM]>0)
			{
				resources[URANIUM]=Long.MAX_VALUE;
			}
			else
			{
				resources[URANIUM]=0;
			}
		}

		resources[MONEY]+=changeResources[MONEY];
		if(resources[MONEY]<0)
		{
			if(changeResources[MONEY]>0)
			{
				resources[MONEY]=Long.MAX_VALUE;
			}
			else
			{
				resources[MONEY]=0;
			}
		}

		if(changeResources.length>5 && changeResources[GEMS]>0)
		{
			int changeResource=changeResources[GEMS]
				/PublicConst.LOWLIMIT_GEMS_TIMES;
			resources[SGEMS]+=changeResource;
			if(resources[SGEMS]<0)
			{
				if(changeResources[GEMS]>0)
				{
					resources[SGEMS]=Long.MAX_VALUE;
				}
				else
				{
					resources[SGEMS]=0;
				}
			}
			JBackKit.sendGemChange(player,true,changeResource);
			synchResources(resources);
			autoUpBuilding.containedPlayer2Up(player);
			JBackKit.sendResetResources(player);
			return changeResource;
		}
		
		// 成就数据采集
		AchieveCollect.resourceStock(player);
		// 建筑自动升级检测
		autoUpBuilding.containedPlayer2Up(player);
		JBackKit.sendResetResources(player);
		return 0;
		// synchResourcesMax(resources,player);
	}

	/** 判断某种资源是否已经超过上限 */
	public static int synchResourcesMax(Player player,int resourceType,
		int buildType,int nowResource)
	{
		// 指挥中心和仓库的容量
		long num=buildCapacity(Build.BUILD_DIRECTOR,player);
		num+=buildCapacity(Build.BUILD_STORE,player);
		long capacity=num+buildCapacity(buildType,player);
		// 算上科技加成 容量的加成
		AdjustmentData buff=((AdjustmentData)player.getAdjstment()
			.getAdjustmentValue(PublicConst.STORE_ADD_BUFF));
		if(buff!=null)
		{
			capacity=capacity/100*(100+buff.percent);
		}
		if(player.getResources()[resourceType]>capacity) return 0;
		long maxResource=(capacity-player.getResources()[resourceType]);
		if(nowResource>maxResource&&maxResource>=0) nowResource=(int)maxResource;
		return nowResource;
	}

	/** 某个资源得到上限 过滤为0 */
	public static int filerFullResource(int resource,int resourceType,
		int buildType,Player player)
	{
		return synchResourcesMax(player,resourceType,buildType,resource);
	}
	/** 资源得到上限 过滤为0 */
	public static void filterFullResources(int changeResource[],Player player)
	{
		changeResource[METAL]=synchResourcesMax(player,METAL,
			Build.BUILD_METAL,changeResource[METAL]);

		changeResource[OIL]=synchResourcesMax(player,OIL,Build.BUILD_OIL,
			changeResource[OIL]);

		changeResource[SILICON]=synchResourcesMax(player,SILICON,
			Build.BUILD_SILION,changeResource[SILICON]);

		changeResource[URANIUM]=synchResourcesMax(player,URANIUM,
			Build.BUILD_URANIUM,changeResource[URANIUM]);

		changeResource[MONEY]=synchResourcesMax(player,MONEY,
			Build.BUILD_MONEY,changeResource[MONEY]);
	}
	/** 某个类型的建筑资源容量 */
	public static long buildCapacity(int buildType,Player player)
	{
		Object builds[]=(Object[])player.island.getBuildArray();
		long num=0;
		for(int i=0;i<builds.length;i++)
		{
			PlayerBuild build=(PlayerBuild)builds[i];
			if(build.getBuildType()==buildType)
			{
				num+=build.getNowCapacity();
			}
		}
		return num;
	}

	/** 减少资源 */
	public static void reduceResources(long resources[],
		int changeResources[],Player player)
	{
		resources[METAL]-=changeResources[METAL];
		resources[OIL]-=changeResources[OIL];
		resources[SILICON]-=changeResources[SILICON];
		resources[URANIUM]-=changeResources[URANIUM];
		resources[MONEY]-=changeResources[MONEY];
		synchResources(resources);
		//成就数据采集
		AchieveCollect.resourceStock(player);
		
		JBackKit.sendResetResources(player);
		
	}
	
	/** 减少资源 */
	public static void reduceResources(long resources[],
		long changeResources[],Player player)
	{
		resources[METAL]-=changeResources[METAL];
		resources[OIL]-=changeResources[OIL];
		resources[SILICON]-=changeResources[SILICON];
		resources[URANIUM]-=changeResources[URANIUM];
		resources[MONEY]-=changeResources[MONEY];
		synchResources(resources);
		//成就数据采集
		AchieveCollect.resourceStock(player);
		
		JBackKit.sendResetResources(player);
		
	}

	/** 资源改变 */
	public static void addResources(long resources[],int metal,int oil,
		int silicon,int uranium,int money,Player player)
	{
		resources[METAL]+=metal;
		if(resources[METAL]<0)
		{
			if(metal>0)
			{
				resources[METAL]=Long.MAX_VALUE;
			}
			else
			{
				resources[METAL]=0;
			}
		}
		
		resources[OIL]+=oil;
		if(resources[OIL]<0)
		{
			if(oil>0)
			{
				resources[OIL]=Long.MAX_VALUE;
			}
			else
			{
				resources[OIL]=0;
			}
		}

		resources[SILICON]+=silicon;
		if(resources[SILICON]<0)
		{
			if(silicon>0)
			{
				resources[SILICON]=Long.MAX_VALUE;
			}
			else
			{
				resources[SILICON]=0;
			}
		}
		
		resources[URANIUM]+=uranium;
		if(resources[URANIUM]<0)
		{
			if(uranium>0)
			{
				resources[URANIUM]=Long.MAX_VALUE;
			}
			else
			{
				resources[URANIUM]=0;
			}
		}

		resources[MONEY]+=money;
		if(resources[MONEY]<0)
		{
			if(money>0)
			{
				resources[MONEY]=Long.MAX_VALUE;
			}
			else
			{
				resources[MONEY]=0;
			}
		}

		synchResources(resources);
		//成就数据采集
		AchieveCollect.resourceStock(player);
		//建筑自动升级检测
		autoUpBuilding.containedPlayer2Up(player);	
		JBackKit.sendResetResources(player);
		// synchResourcesMax(resources,player);
	}

	/** gm工具需求传long  */
	public static void addResources(long resources[],long metal,long oil,
		long silicon,long uranium,long money,Player player)
	{
		resources[METAL]+=metal;
		if(resources[METAL]<0)
		{
			if(metal>0)
			{
				resources[METAL]=Long.MAX_VALUE;
			}
			else
			{
				resources[METAL]=0;
			}
		}
		
		resources[OIL]+=oil;
		if(resources[OIL]<0)
		{
			if(oil>0)
			{
				resources[OIL]=Long.MAX_VALUE;
			}
			else
			{
				resources[OIL]=0;
			}
		}

		resources[SILICON]+=silicon;
		if(resources[SILICON]<0)
		{
			if(silicon>0)
			{
				resources[SILICON]=Long.MAX_VALUE;
			}
			else
			{
				resources[SILICON]=0;
			}
		}
		
		resources[URANIUM]+=uranium;
		if(resources[URANIUM]<0)
		{
			if(uranium>0)
			{
				resources[URANIUM]=Long.MAX_VALUE;
			}
			else
			{
				resources[URANIUM]=0;
			}
		}

		resources[MONEY]+=money;
		if(resources[MONEY]<0)
		{
			if(money>0)
			{
				resources[MONEY]=Long.MAX_VALUE;
			}
			else
			{
				resources[MONEY]=0;
			}
		}

		synchResources(resources);
		//成就数据采集
		AchieveCollect.resourceStock(player);
		//建筑自动升级检测
		autoUpBuilding.containedPlayer2Up(player);	
		JBackKit.sendResetResources(player);
		// synchResourcesMax(resources,player);
	}
	
	/** 资源改变 */
	public static void reduceResources(long resources[],int metal,int oil,
		int silicon,int uranium,int money,Player player)
	{
		resources[METAL]-=metal;
		resources[OIL]-=oil;
		resources[SILICON]-=silicon;
		resources[URANIUM]-=uranium;
		resources[MONEY]-=money;
		synchResources(resources);
		//成就数据采集
		AchieveCollect.resourceStock(player);
		
		JBackKit.sendResetResources(player);
	}
	
	/** 资源改变    money为long*/
	public static void reduceResources(long resources[],long metal,long oil,
		long silicon,long uranium,long money,Player player)
	{
		resources[METAL]-=metal;
		resources[OIL]-=oil;
		resources[SILICON]-=silicon;
		resources[URANIUM]-=uranium;
		resources[MONEY]-=money;
		synchResources(resources);
		//成就数据采集
		AchieveCollect.resourceStock(player);
		
		JBackKit.sendResetResources(player);
	}

	/** 同步资源 大于0 */
	public static void synchResources(long resources[])
	{
		if(resources[METAL]<0) resources[METAL]=0;
		if(resources[OIL]<0) resources[OIL]=0;
		if(resources[SILICON]<0) resources[SILICON]=0;
		if(resources[URANIUM]<0) resources[URANIUM]=0;
		if(resources[MONEY]<0) resources[MONEY]=0;
		if(resources[SGEMS]<0) resources[SGEMS]=0;
	}

	/** 增加宝石 充值的代币才调用这个方法 */
	public static boolean addGems(int gems,long resources[],Player player)
	{
		int addgems=0;
		if(gems<=0) return false;
		boolean fp=false;
		// 首冲奖励
		if(resources[MAXGEMS]-player.getDailyGemsCount()<=0)
		{
			addgems+=gems	;//在刷新前台宝石的时候传入的参数
			fp=true;
			resources[GEMS]+=gems;
			player.setAttribute(PublicConst.FP_AWARD,(gems<<16)+"");
			if(ActivityContainer.getInstance().isOpen(ActivityContainer.GMES_ID))
			{
				addgems+=gems/5;
				resources[SGEMS]+=gems/5;
			}
		}
		else
		{
			long gem=(long)resources[GEMS]+(long)gems;
			addgems+=gems;
			if(gem<0)
			{
				resources[GEMS]=Long.MAX_VALUE;
			}
			else
			{
				resources[GEMS]+=gems;
				if(ActivityContainer.getInstance().isOpen(
					ActivityContainer.GMES_ID))
				{
					addgems+=gems/5;
					resources[SGEMS]+=gems/5;
				}
			}
		}
		// 双倍充值活动
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.DOUBLE_GMES_ID))
		{
			DoubleGemsAcitivity activity=(DoubleGemsAcitivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.DOUBLE_GMES_ID,
					0);
			if(activity!=null&&!activity.isPurchased(player,gems))
			{
				activity.finishOrder(player,gems);
				addgems+=gems;
				resources[SGEMS]+=gems;
			}
		}
		resources[MAXGEMS]+=gems;
		JBackKit.sendGemChange(player,true,addgems);
		JBackKit.sendResetResources(player);
		if(fp)JBackKit.sendFPaward(player);
		// 累计充值活动是否开启
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.TOTALBUYGMES_ID))
		{
			TotalBuyActivity activity=(TotalBuyActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.TOTALBUYGMES_ID,0);
			if(activity!=null)
			{
				activity.updateTotalBuy(player,gems);
			}
		}
		// 网站排行榜活动是否开启
		if(ActivityContainer.getInstance().isOpen(
			ActivityContainer.WEB_SHOW_ID))
		{
			WebShowActivity activity=(WebShowActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.WEB_SHOW_ID,0);
			if(activity!=null)
			{
				activity.addGemsRecord(player.getId(),gems);
			}
		}
		// 充值接力活动
		PayRelayActivity act=(PayRelayActivity)ActivityContainer
						.getInstance().getActivity(ActivityContainer.PAY_RELAY,0);
		if(act!=null)
		{
			if(!act.isEnd())
			{
				act.setPlayerPayDays(player);
				act.setChange(true);
			}
		}
		// 战友系统
		Player vertern=ComradeHandler.getInstance().getVertern(player);
		if(vertern!=null)
		{
			player.incComardeCharge(gems);
			ComradeHandler.getInstance().finishTask(player,
				ComradeTask.RECHARGE);
		}
//		if(ActivityContainer.getInstance().isOpen(
//			ActivityContainer.PAY_RELAY))
//		{
//			
//			act.setPlayerPayDays(player);
//			act.setChange(true);
//		}
		return true;
	}

	/** 普通增加宝石 */
	public static void addGemsNomal(int gems,long resources[],Player player)
	{
		if(gems<=0) return;
		resources[SGEMS]+=gems;
		if(resources[SGEMS]<0) resources[SGEMS]=Long.MAX_VALUE;
		JBackKit.sendGemChange(player,true,gems);
		JBackKit.sendResetResources(player);
	}
	
	/** 普通增加宝石ONLY */
	public static void addGemsNomalOnly(int gems,long resources[])
	{
		if(gems<=0) return;
		resources[SGEMS]+=gems;
		if(resources[SGEMS]<0) resources[SGEMS]=Long.MAX_VALUE;
	}
	
	/** 日常领取增加宝石，需要增加到 MAXGEMS */
	public static void addGemsDaily(int gems,long resources[],Player player)
	{
		if(gems<0) return;
		resources[SGEMS]+=gems;
		if(resources[SGEMS]<0) resources[SGEMS]=Long.MAX_VALUE;
		player.incrDailyGems(gems);
		JBackKit.sendGemChange(player,true,gems);
		JBackKit.sendResetResources(player);
	}
	/** 扣除宝石 */
	public static boolean reduceGems(int gems,long resources[],Player player)
	{
		if(gems<0||!Resources.checkGems(gems,resources)) return false;
		if(resources[SGEMS]>=gems)
		{
			resources[SGEMS]-=gems;
		}else
		{
			resources[GEMS]-=gems-resources[SGEMS];
			resources[SGEMS]=0;
		}
		JBackKit.sendGemChange(player,false,gems);
		JBackKit.sendResetResources(player);
		ConsumeGemsActivity act=(ConsumeGemsActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.CONSUME_GEMS_ID,0);
		if(gems>0 && act!=null&&act.isOpen(TimeKit.getSecondTime()))
			act.addRecord(player,gems);
		return true;
	}
	
	/** 扣除宝石 Only*/
	public static boolean reduceGemsOnly(int gems,long resources[])
	{
		if(gems<0||!Resources.checkGems(gems,resources)) return false;
		if(resources[SGEMS]>=gems)
		{
			resources[SGEMS]-=gems;
		}else
		{
			resources[GEMS]-=gems-resources[SGEMS];
			resources[SGEMS]=0;
		}
		return true;
	}

	public static AutoUpBuildManager getAutoUpBuilding()
	{
		return autoUpBuilding;
	}

	public static void setAutoUpBuilding(AutoUpBuildManager autoUpBuilding)
	{
		Resources.autoUpBuilding=autoUpBuilding;
	}
}
