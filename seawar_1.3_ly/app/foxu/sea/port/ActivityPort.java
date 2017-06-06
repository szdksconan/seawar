package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.net.Session;
import mustang.set.ArrayList;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.ds.PlayerKit;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.RankManager.RankInfo;
import foxu.sea.Resources;
import foxu.sea.activity.Activity;
import foxu.sea.activity.ActivityContainer;
import foxu.sea.activity.AppGradeActivity;
import foxu.sea.activity.AppShareActivity;
import foxu.sea.activity.AwardShippingActivity;
import foxu.sea.activity.ConsumeGemsActivity;
import foxu.sea.activity.JigsawActivity;
import foxu.sea.activity.LoginRewardActivity;
import foxu.sea.activity.NewPlayerLvUpActivity;
import foxu.sea.activity.NianActivity;
import foxu.sea.activity.PayRelayActivity;
import foxu.sea.activity.QuestionnaireActivity;
import foxu.sea.activity.RankAwardActivity;
import foxu.sea.activity.RobFestivalActivity;
import foxu.sea.activity.SellingPackageOne;
import foxu.sea.activity.SellingPackageThree;
import foxu.sea.activity.SellingPackageTwo;
import foxu.sea.activity.TotalBuyActivity;
import foxu.sea.activity.VaribleAwardActivity;
import foxu.sea.alliance.Alliance;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.fight.Fleet;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.task.TaskEventExecute;
import foxu.sea.worldboss.BossHurt;
import foxu.sea.worldboss.NianBoss;

/**
 * 活动信息端口：1020
 * 
 * @author yw
 */
public class ActivityPort extends AccessPort
{

	/** 海水的sid */
	public static final int WATER_ISLAND_SID=11501;
	/** 展示排名数 */
	public static final int MAX_NUM=10;

	/**
	 * GET_ACTIVITY 获取活动信息（经验，BOSS，充值） GET_VARIBLE_AWARD 获取天降好礼 CONSUME_GEMS
	 * 累计消费 VIEW_NIAN查看年兽 NIAN_INFO获取年兽信息 APP_GRADE 应用评分 NEW_PLAYER_UP 新手冲级
	 * APP_SHARE 分享活动 RANK_AWARD 新服排名活动 LOGIN_REWARD 登陆有礼 QUESTION_ACTIVITY 调查问卷
	 * LUCKY_EXPLORED 幸运探险活动 LUCKY_AWARD_SHIPPING=26通商航运抽奖,
	 * LUCKY_AWARD_SHIPPING_INFO=27 通商航运奖品历史,LUCKY_AWARD_ROB=28 全民抢"节"
	 * SELLING_P_GET=29 获取信息  SELLING_P_BUY=30 购买
	 */
	public final int GET_ACTIVITY=1,GET_VARIBLE_AWARD=2,
					GET_TOTALBUY_AWARD=3,CONSUME_GEMS=4,VIEW_NIAN=5,
					NIAN_INFO=6,APP_GRADE_URL=7,APP_GRADE_AWARD=8,
					APP_GRADE_FORWARD=9,NEW_PLAYER_UP=10,
					GET_NEW_PLAYER_UP=11,APP_SHARE_AWARD=12,
					APP_SHARE_FORWARD=13,APP_SHARE_INFO=14,
					RANK_AWARD_INFO=15,RANK_AWARD_DRAW=16,
					RANK_AWARD_RANK=17,GET_JIGSAW=18,DRAW_JIGSAW=19,
					AWARD_JIGSAW=20,BUY_JIGSAW=21,LOGIN_REWARD=22,PAY_RELAY=23,
					QUESTION_ACTIVITY=24,LUCKY_EXPLORED=25,LUCKY_AWARD_SHIPPING=26,
					LUCKY_AWARD_SHIPPING_INFO=27,LUCKY_AWARD_ROB=28,SELLING_P_GET=29,
					SELLING_P_BUY=30;

	CreatObjectFactory factory;

	/* method */
	public CreatObjectFactory getFactory()
	{
		return factory;
	}

	public void setFactory(CreatObjectFactory factory)
	{
		this.factory=factory;
	}

	@Override
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
		if(type==GET_ACTIVITY)
		{
			ActivityContainer.getInstance().showByteWrite(data);
		}
		else if(type==GET_VARIBLE_AWARD)
		{
			VaribleAwardActivity activity=(VaribleAwardActivity)ActivityContainer
				.getInstance()
				.getActivity(ActivityContainer.VARIBLE_AWARD,0);
			if(activity==null)
				throw new DataAccessException(0,"not open this activity");
			String erro=activity.getAward(player,data);
			if(erro!=null)
			{
				JBackKit.sendPlayerVaribleAward(player);
				throw new DataAccessException(0,erro);
			}
		}
		else if(type==CONSUME_GEMS)
		{
			int aid=data.readInt();
			data.clear();
			ConsumeGemsActivity activity=(ConsumeGemsActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.CONSUME_GEMS_ID,aid);
			int addGems=0;
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				addGems=ConsumeGemsActivity.award(player,aid);
			}
			data.writeInt(addGems);
		}
		else if(type==GET_TOTALBUY_AWARD)
		{
			TotalBuyActivity activity=(TotalBuyActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.TOTALBUYGMES_ID,0);
			if(activity==null)
				throw new DataAccessException(0,"not open this activity");
			int sid=data.readUnsignedShort();
			String erro=activity.getAward(player,sid,data);
			JBackKit.sendPlayerTotalBuyAward(activity,player,sid);
			if(erro!=null)
			{
				throw new DataAccessException(0,erro);
			}
		}
		else if(type==APP_GRADE_URL)
		{
			AppGradeActivity activity=(AppGradeActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.APP_GRADE_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			String platId=player.getPlat();
			String url=activity.getUrlByPlat(platId);
			if(url==null) url="";
			data.clear();
			// 领取按钮是否可用
			data.writeBoolean(!activity.isCompleteGrade(player));
			// 跳转按钮是否显示
			data.writeBoolean(!activity.isCompleteForward(player));
			data.writeUTF(url);
			Award award=activity.getGradeAward();
			// if(award==null)
			// throw new DataAccessException(0,"not open this activity");
			award.viewAward(data,player);
			data.writeUTF(activity.getReason());
		}
		else if(type==APP_GRADE_AWARD)
		{
			AppGradeActivity activity=(AppGradeActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.APP_GRADE_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(!activity.isCompleteForward(player))
				throw new DataAccessException(0,"not_grade_yet");
			if(activity.isCompleteGrade(player))
				throw new DataAccessException(0,"daily_reward_recevie");
			Award award=activity.getGradeAward();
			if(award==null)
				throw new DataAccessException(0,"not open this activity");
			activity.addPlayerRecord(player);
			award.awardLenth(data,player,factory,null,
				new int[]{EquipmentTrack.FROM_APP_GRADE});
			data.clear();
		}
		else if(type==APP_GRADE_FORWARD)
		{
			AppGradeActivity activity=(AppGradeActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.APP_GRADE_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			activity.addPlayerForward(player);
			data.clear();
		}
		else if(type==VIEW_NIAN)
		{
			int index=data.readInt();
			NpcIsland island=(NpcIsland)factory.getIslandByIndexOnly(index
				+"");
			if(island==null||island.getIslandType()!=NpcIsland.NIAN_BOSS)
			{
				throw new DataAccessException(0,"player is null");
			}
			NianActivity acti=(NianActivity)ActivityContainer.getInstance()
				.getActivity(ActivityContainer.NIAN_SID,0);
			if(acti==null||acti.getBoss()==null
				||acti.getBoss().getIndex()!=index)
			{
				// 移除boss岛屿
				island.updateSid(WATER_ISLAND_SID);
				// 刷新前台
				JBackKit.flushIsland(factory.getDsmanager(),island,factory);
				throw new DataAccessException(0,"boss is null");
			}
			NianBoss boss=acti.getBoss();
			data.clear();
			int time=0;
			// 判断玩家时间
			if(player.getAttributes(PublicConst.ATTACK_NIAN_TIME)!=null
				&&!player.getAttributes(PublicConst.ATTACK_NIAN_TIME)
					.equals(""))
			{
				time=Integer.parseInt(player
					.getAttributes(PublicConst.ATTACK_NIAN_TIME));
				time=time-TimeKit.getSecondTime();
				if(time<0) time=0;
			}
			data.writeInt(time);

			// 写给前台，如果boss还有船，最少为1%
			int fleetNum=boss.getFleetNowNum()*100/boss.getFleetMaxNum();
			if(boss.getFleetNowNum()>0&&fleetNum==0) fleetNum=1;
			data.writeByte(fleetNum);

			// 联盟击毁数量
			int num=0;
			if(player.getAttributes(PublicConst.ALLIANCE_ID)!=null
				&&!player.getAttributes(PublicConst.ALLIANCE_ID).equals(""))
			{
				Alliance alliance=(Alliance)factory.getAllianceMemCache()
					.loadOnly(
						player.getAttributes(PublicConst.ALLIANCE_ID)+"");
				if(alliance!=null)
				{
					num=boss.getAttackNum(alliance.getId(),
						boss.getHurtList_a());
				}
			}
			data.writeInt(num);
			float floatNum=0.00f;
			floatNum=(float)num*100/(float)boss.getFleetMaxNum();
			floatNum=(float)(Math.round(floatNum*100))/100;
			data.writeFloat(floatNum);// 百分比
			// 个人击毁数量
			num=boss.getAttackNum(player.getId(),boss.getHurtList_p());
			data.writeInt(num);
			floatNum=(float)num*100/(float)boss.getFleetMaxNum();
			floatNum=(float)(Math.round(floatNum*100))/100;
			data.writeFloat(floatNum);// 百分比

			// 联盟伤害排名
			Object objectAlliances[]=boss.getHurtRank(factory,
				boss.getHurtList_a());
			int length=objectAlliances.length;
			if(length>=MAX_NUM) length=MAX_NUM;
			data.writeByte(length);
			for(int i=0;i<length;i++)
			{
				BossHurt bosshurt=(BossHurt)objectAlliances[i];
				data.writeByte(boss.sortNum(bosshurt.getId(),
					boss.getHurtList_a()));
				String allianceName="??";
				num=boss.getAttackNum(bosshurt.getId(),boss.getHurtList_a());
				Alliance alliance=(Alliance)factory.getAllianceMemCache()
					.loadOnly(bosshurt.getId()+"");
				if(alliance!=null)
				{
					allianceName=alliance.getName();
				}
				data.writeUTF(allianceName);
				data.writeInt(num);
				floatNum=(float)num*100/(float)boss.getFleetMaxNum();
				floatNum=(float)(Math.round(floatNum*100))/100;
				data.writeFloat(floatNum);
			}
			// 个人伤害排名
			objectAlliances=boss.getHurtRank(factory,boss.getHurtList_p());
			length=objectAlliances.length;
			if(length>=MAX_NUM) length=MAX_NUM;
			data.writeByte(length);
			for(int i=0;i<length;i++)
			{
				BossHurt bosshurt=(BossHurt)objectAlliances[i];
				data.writeByte(boss.sortNum(bosshurt.getId(),
					boss.getHurtList_p()));
				String allianceName="??";
				num=boss.getAttackNum(bosshurt.getId(),boss.getHurtList_p());
				Player rp=factory.getPlayerById(bosshurt.getId());
				if(rp!=null)
				{
					allianceName=rp.getName();
				}
				data.writeUTF(allianceName);
				data.writeInt(num);
				floatNum=(float)num*100/(float)boss.getFleetMaxNum();
				floatNum=(float)(Math.round(floatNum*100))/100;
				data.writeFloat(floatNum);
			}

			// 剩余兵力
			Fleet fleet[]=boss.getFleetGroup().getArray();
			for(int i=0;i<fleet.length;i++)
			{
				num=0;
				if(fleet[i]!=null&&fleet[i].getNum()>0)
				{
					num=fleet[i].getNum();
				}
				data.writeShort(num);
			}
		}
		else if(type==NIAN_INFO)
		{
			NianActivity acti=(NianActivity)ActivityContainer.getInstance()
				.getActivity(ActivityContainer.NIAN_SID,0);
			data.clear();
			if(acti==null||acti.getBoss()==null
				||acti.getBoss().getIndex()<=0)
			{
				data.writeByte(0);
			}
			else
			{
				acti.getBoss().getBossInfo(data);
			}
		}
		else if(type==NEW_PLAYER_UP)
		{
			NewPlayerLvUpActivity activity=(NewPlayerLvUpActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.NEW_PLAYER_UP_ID,0);
			if(activity==null)
				throw new DataAccessException(0,"not open this activity");
			int sid=data.readUnsignedShort();
			data.clear();
			String erro=activity.getAward(player,sid,data);
			if(erro!=null)
			{
				throw new DataAccessException(0,erro);
			}
		}
		else if(type==GET_NEW_PLAYER_UP)
		{
			data.clear();
			NewPlayerLvUpActivity activity=(NewPlayerLvUpActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.NEW_PLAYER_UP_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				data.writeByte(0);
			}
			else
			{
				activity.showByteWrite(data,player);
			}
		}
		else if(type==APP_SHARE_AWARD)
		{
			AppShareActivity activity=(AppShareActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.APP_SHARE_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(!activity.isCompleteForward(player))
				throw new DataAccessException(0,"not_share_yet");
			if(activity.isCompleteAward(player))
				throw new DataAccessException(0,"daily_reward_recevie");
			Award award=activity.getShareAward();
			if(award==null)
				throw new DataAccessException(0,"not open this activity");
			activity.addPlayerRecord(player);
			award.awardLenth(data,player,factory,null,
				new int[]{EquipmentTrack.FROM_APP_SHARE});
			data.clear();
		}
		else if(type==APP_SHARE_FORWARD)
		{
			AppShareActivity activity=(AppShareActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.APP_SHARE_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			activity.addPlayerForward(player);
			data.clear();
		}
		else if(type==APP_SHARE_INFO)
		{
			AppShareActivity activity=(AppShareActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.APP_SHARE_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			data.clear();
			// 领取按钮是否可用
			data.writeBoolean(!activity.isCompleteAward(player));
			// 分享按钮是否显示
			data.writeBoolean(!activity.isCompleteForward(player));
			Award award=activity.getShareAward();
			// if(award==null)
			// throw new DataAccessException(0,"not open this activity");
			award.viewAward(data,player);
		}
		else if(type==RANK_AWARD_INFO)
		{
			int sid=data.readUnsignedShort();
			data.clear();
			RankAwardActivity act=(RankAwardActivity)ActivityContainer
				.getInstance().getActivity(sid,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			RankAwardActivity.RankAward[] awards=act.getRankAwards();
			int startRank=1;
			data.writeByte(awards.length);
			for(int i=0;i<awards.length;i++)
			{
				data.writeByte(startRank);
				data.writeByte(awards[i].getRank());
				awards[i].getAward().viewAward(data,player);
				startRank=awards[i].getRank()+1;
			}
			int time=act.getAwardTime()-TimeKit.getSecondTime();
			data.writeInt(time>0?time:0);
			// 领取功能是否可用
			boolean isAward=false;
			if(act.isAwardAvailable()&&!act.isCompleteAward(player)
				&&act.getPlayerRankAward(player)!=null) isAward=true;
			data.writeBoolean(isAward);
		}
		else if(type==RANK_AWARD_DRAW)
		{
			int sid=data.readUnsignedShort();
			data.clear();
			RankAwardActivity act=(RankAwardActivity)ActivityContainer
				.getInstance().getActivity(sid,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(act.isCompleteAward(player)||!act.isAwardAvailable())
				throw new DataAccessException(0,"daily_reward_recevie");
			Award award=act.getPlayerRankAward(player);
			if(award==null)
				throw new DataAccessException(0,"err:Can't get");
			act.addAwardRecord(player);
			award.awardLenth(data,player,factory,null,
				new int[]{EquipmentTrack.FROM_RANK_AWARD});
		}
		else if(type==RANK_AWARD_RANK)
		{
			int sid=data.readUnsignedShort();
			data.clear();
			RankAwardActivity act=(RankAwardActivity)ActivityContainer
				.getInstance().getActivity(sid,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			ArrayList list=act.getAwardRank();
			data.writeInt(list.size());
			long lastScore=0;
			int rank=1;
			for(int i=0;i<list.size();i++)
			{
				RankInfo playerRank=(RankInfo)list.get(i);
				// 活动结算后，排名将采用并列处理
				if(!act.isAwardAvailable()
					||lastScore>playerRank.getRankInfo()) rank=i+1;
				data.writeByte(rank);
				data.writeUTF(playerRank.getPlayerName());
				data.writeByte(playerRank.getPlayerLevel());
				if(act.isRankScoreLong())
					data.writeLong(playerRank.getRankInfo());
				else
					data.writeInt((int)playerRank.getRankInfo());
				lastScore=playerRank.getRankInfo();
			}
			long[] selfRanks=act.getSelfRank(player);
			data.writeInt((int)selfRanks[0]);
			if(act.isRankScoreLong())
				data.writeLong(selfRanks[1]);
			else
				data.writeInt((int)selfRanks[1]);

		}
		else if(type==GET_JIGSAW)
		{
			data.clear();
			JigsawActivity act=(JigsawActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.JIGSAW_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			// 免费是否可用
			data.writeBoolean(act.isFreeTurn(player));
			// 单次价格
			data.writeShort(act.getSinglePrice());
			// 连抽价格
			data.writeShort(act.getBatchPrice());
			data.writeShort(act.getSalePrice());
			getJigsawActivity(act,player,data);
		}
		else if(type==DRAW_JIGSAW)
		{
			boolean isBatch=data.readBoolean();
			data.clear();
			JigsawActivity act=(JigsawActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.JIGSAW_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			boolean isGemsEnough=false;
			int gems=act.getSinglePrice();
			if(isBatch)
			{
				gems=act.getBatchPrice();
				if(act.batchDraw(player)) isGemsEnough=true;
			}
			else if(act.singleDraw(player)) isGemsEnough=true;
			if(!isGemsEnough)
				throw new DataAccessException(0,"not enough gems");
			else
			{
				// 免费是否可用
				data.writeBoolean(act.isFreeTurn(player));
				getJigsawActivity(act,player,data);
			}
			// 宝石消费记录
			factory.createGemTrack(GemsTrack.JIGSAW_ACTIVITY,player.getId(),
				gems,act.getId(),Resources.getGems(player.getResources()));
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,gems);
		}
		else if(type==AWARD_JIGSAW)
		{
			JigsawActivity act=(JigsawActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.JIGSAW_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(!act.isAwardAvailable(player))
				throw new DataAccessException(0,"err:Can't get");
			act.completeAward(player);
			data.clear();
			act.getAward(player).awardLenth(data,player,factory,null,
				new int[]{EquipmentTrack.FROM_JIGSAW});
			getJigsawActivity(act,player,data);
		}
		else if(type==BUY_JIGSAW)
		{
			int index=data.readUnsignedByte();
			if(index>JigsawActivity.PARTS_NUM)
				throw new DataAccessException(0,"not open this activity");
			data.clear();
			JigsawActivity act=(JigsawActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.JIGSAW_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(!act.buyParts(player,index))
				throw new DataAccessException(0,"not enough gems");
			getJigsawActivity(act,player,data);
			// 宝石消费记录
			factory.createGemTrack(GemsTrack.JIGSAW_ACTIVITY,player.getId(),
				act.getSalePrice(),act.getId(),
				Resources.getGems(player.getResources()));
			// 发送change消息
			TaskEventExecute.getInstance().executeEvent(
				PublicConst.GEMS_ADD_SOMETHING,this,player,
				act.getSalePrice());
		}
		// 登陆有礼活动
		else if(type==LOGIN_REWARD)
		{
			//请求类型   0 获取活动信息及玩家活动信息    1玩家领取奖励    2时间找回
			int subType=data.readByte();
			LoginRewardActivity act=(LoginRewardActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.LOGIN_REWARD,0);
			boolean flag=true;
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(subType==0)
			{
				data.clear();
				act.showByteWrite(data,player);
			} else if(subType==1)
			{
				int awardLv = data.readByte();
				flag=act.reward(player,awardLv,data,factory,false);
			} else if(subType==2)
			{
				int awardLv = data.readByte();
				//检查是否按顺序时间找回
				boolean canTimeFind=act.checkTimeFind(player.getId(),awardLv);
				if(!canTimeFind)
				{
					throw new DataAccessException(0,"you need to in order of time recover");
				}
				int gems=act.getTimeFindGems(player.getId());
				if(!Resources.checkGems(gems,player.getResources()))
				{
					throw new DataAccessException(0,"not enough gems");
				}
				Resources.reduceGems(gems,player.getResources(),player);
				factory.createGemTrack(GemsTrack.LOGIN_REWARD,player
					.getId(),gems,0,Resources.getGems(player.getResources()));
				
				flag=act.reward(player,awardLv,data,factory,true);
			}
			if(!flag)
				throw new DataAccessException(0,"you have received it");
		}
		// 充值接力活动
		else if(type==PAY_RELAY)
		{
			//请求类型   0 获取活动信息及玩家活动信息    1玩家领取奖励 
			int subType=data.readByte();
			PayRelayActivity act=(PayRelayActivity)ActivityContainer
				.getInstance().getActivity(ActivityContainer.PAY_RELAY,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(subType==0)
			{
				data.clear();
				act.showByteWrite(data,player);
			} else if(subType==1)
			{
				int awardLv = data.readShort();
				boolean flag=act.reward(player,awardLv,data,factory);
				if(!flag)
				{
					throw new DataAccessException(0,"you have received it");
				}
			}
		}
		// 调查问卷活动
		else if(type==QUESTION_ACTIVITY)
		{
			// 请求类型 0 获取活动信息及玩家活动信息 1玩家领取奖励 2答题
			int subType=data.readByte();
			QuestionnaireActivity act=(QuestionnaireActivity)ActivityContainer
				.getInstance().getActivity(
					ActivityContainer.QUESTIONNAIRE_ID,0);
			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
				throw new DataAccessException(0,"not open this activity");
			if(subType==0)
			{
				data.clear();
				act.showByteWrite(data,player);
			}
			else if(subType==1)
			{
				int flag=act.canReward(player.getId());
				if(flag==2)
					throw new DataAccessException(0,"please answer questions");
				if(flag==1)
					throw new DataAccessException(0,"you have already rewarded award");
				if(!act.isInLevel(player.getLevel()))
					throw new DataAccessException(0,
						"your level doesn`t conform to the requirements");
				act.reward(player,data,factory);
			}
			else if(subType==2)
			{
				int index=data.readInt();
				String answer=data.readUTF();
				act.answer(player.getId(),index,answer);
			}
		}
//		// 幸运探险活动(大富翁)
//		else if(type==LUCKY_EXPLORED)
//		{
//			// 请求类型 0 获取活动信息及玩家活动信息 1掷骰子并发奖
//			LuckyExploredActivity act=(LuckyExploredActivity)ActivityContainer
//				.getInstance().getActivity(
//					ActivityContainer.LUCKY_EXPLORED_ID,0);
//			if(act==null||!act.isOpen(TimeKit.getSecondTime()))
//				throw new DataAccessException(0,"not open this activity");
//			data.clear();
//			act.showByteWrite(data,player);
//		}
		// 通商航运抽奖
		else if(type==LUCKY_AWARD_SHIPPING)
		{
			AwardShippingActivity activity=(AwardShippingActivity)ActivityContainer
							.getInstance().getActivity(
								ActivityContainer.AWARD_SHIPPING_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity not open");
			}
			activity.draw(data,player,factory);
		}
		// 通商航运 查看奖励历史
		else if(type==LUCKY_AWARD_SHIPPING_INFO)
		{
			AwardShippingActivity activity=(AwardShippingActivity)ActivityContainer
							.getInstance().getActivity(
								ActivityContainer.AWARD_SHIPPING_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity not open");
			}
			data.clear();
			activity.getAwardRecord(player.getId(),data);
		}
		// 全民抢"节"
		else if(type==LUCKY_AWARD_ROB)
		{
			RobFestivalActivity activity=(RobFestivalActivity)ActivityContainer
							.getInstance().getActivity(
								ActivityContainer.AWARD_ROB_ID,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity not open");
			}
			activity.draw(data,player,factory);
		}
		/**获取信息**/
		else if(type==SELLING_P_GET)
		{
			data.clear();
			ActivityContainer.getInstance().showBytesWriteSelling(data,player,factory);
			
		}
		/**购买**/
		else if(type==SELLING_P_BUY)
		{
			int sid=data.readShort();
			data.clear();
			data.writeShort(sid);
			Activity activity=ActivityContainer
				.getInstance().getActivity(sid,0);
			if(activity==null||!activity.isOpen(TimeKit.getSecondTime()))
			{
				throw new DataAccessException(0,"activity not open");
			}
			if(sid==ActivityContainer.SELLING1)
			{
				SellingPackageOne aOne=(SellingPackageOne)activity;
				aOne.buy(player,factory,data);
			}
			else if(sid==ActivityContainer.SELLING2)
			{
				SellingPackageTwo aTwo=(SellingPackageTwo)activity;
				aTwo.buy(player,factory,data);
			}
			else if(sid==ActivityContainer.SELLING3)
			{
				SellingPackageThree aThree=(SellingPackageThree)activity;
				aThree.buy(player,factory,data);
			}
		}
		else
		{
			throw new DataAccessException(0,"erro type");
		}
		return data;
	}

	public void getJigsawActivity(JigsawActivity act,Player player,
		ByteBuffer data)
	{
		int[] record=act.getPartsCount(player);
		data.writeByte(record.length);
		for(int i=0;i<record.length;i++)
		{
			data.writeShort(record[i]);
		}
	}
}
