package foxu.sea.proplist;

import mustang.io.ByteBuffer;
import mustang.text.TextKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.ChatMessage;

/** 直接使用获得奖励品 */
public class AwardProp extends NormalProp implements PropUse
{
	/** 是否购买立即使用 */
	boolean buyUse;
	
	/** 奖励品sid */
	int awardSids[];
	
	public boolean isBuyUse()
	{
		return buyUse;
	}

	public void use(Player player,CreatObjectFactory objectFactory,ByteBuffer data)
	{
		use(player,objectFactory,data,false);
	}

	public boolean checkUse(Player player)
	{
		if(player.getBundle().getCountBySid(getSid())>0) return true;
		return false;
	}
	
	public int[] getAwardSids(){
		return awardSids;
	}

	@Override
	public void use(Player player,CreatObjectFactory objectFactory,
		ByteBuffer data,int num)
	{
		//重复提交 非增量刷新 只最后一次才刷新
		for(int i=0;i<num;i++){
			boolean blockFlush = true;
			if(i==num-1) blockFlush = false;
			use(player,objectFactory,data,blockFlush);
		}
	}

	@Override
	public void use(Player player,CreatObjectFactory objectFactory,
		ByteBuffer data,boolean blockFlush)
	{
		Award award;
		if(awardSids!=null&&awardSids.length>0)
		{
			for(int i=0;i<awardSids.length;i++)
			{
				award=(Award)Award.factory.getSample(awardSids[i]);
				if(award!=null)
				{
					int[] tipSids=PublicConst.PROP_USE_TIPS;
					int[] trackReasons=new int[]{EquipmentTrack.FROM_EQUIP_BOX};
					// 如果在物品使用获得某些道具时需要提示,则进行检测
					if(tipSids!=null)
					{
						if(SeaBackKit.isContainValue(tipSids,getSid()))
						{
							String message=InterTransltor.getInstance().getTransByKey(
									PublicConst.SERVER_LOCALE,"prop_use_tips");
							message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
							String name=InterTransltor.getInstance().getTransByKey(PublicConst.SERVER_LOCALE,"prop_sid_"+getSid());
							message=TextKit.replace(message,"%",name);
							award.awardLenth(data,player,objectFactory,
								message,SeaBackKit.getLuckySids(),trackReasons);
							return;
						}
					}
					award.awardLenth(data,player,objectFactory,null,
						trackReasons,blockFlush);
				}
			}
		}
		else
			data.writeByte(0);
	}

}
