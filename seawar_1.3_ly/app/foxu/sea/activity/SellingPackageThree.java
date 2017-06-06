package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.math.MathKit;
import mustang.net.DataAccessException;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.ActivityLogMemCache;
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
import foxu.sea.proplist.Prop;
import foxu.sea.task.TaskEventExecute;

/***
 * 
 * ���������3
 * 
 * @author lhj
 *
 */
public class SellingPackageThree extends Activity implements ActivitySave
{
	public static final int SAVE_CIRCLE=15*60;
	/** ��һ�α����ʱ�� **/
	int lastSaveTime;
	/** ��������������� **/
	int buyTimes=1;
	/** ����۸� **/
	int price=1;
	/** ������� **/
	String name;
	/** ���� **/
	String describe;
	/** ��Ʒ��Ϣ **/
	int propSids[];
	/** ������Ʒ **/
	int randomPros[];
	/**������ʾ**/
    int [] prompts;
	/** ��ҹ����¼ **/
	IntKeyHashMap mapRecord=new IntKeyHashMap();

	/** ��ʱ�齱���� */
	private Award award=(Award)Award.factory
		.newSample(ActivityContainer.EMPTY_SID);
	
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
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		return resetActivity(stime,etime,initData,factoty);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		startTime=SeaBackKit.parseFormatTime(stime);
		endTime=SeaBackKit.parseFormatTime(etime);
		initData(initData);
		return getActivityState();
	}

	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"")
			.append(id)
			.append('"')
			.append(",\"sid\":\"")
			.append(getSid())
			.append('"')
			.append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime))
			.append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime))
			.append('"')
			.append(",\"others\":\"")
			.append(
				"[times]:"+buyTimes+","+"[gems]:"+price+","+"[RandomProps]:"
					+awardPackageToString())
			.append("[SpecialProps]:"+awardSpecialToString())
			.append("[prompts]:"+prStringToString())
			.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		name=data.readUTF();
		describe=data.readUTF();
		price=data.readInt();
		buyTimes=data.readInt();
		int le=data.readInt();
		for(int i=0;i<le;i++)
		{
			ActivityRecord record=new ActivityRecord();
			record.setPlayerId(data.readInt());
			record.setTimes(data.readShort());
			record.setCreateAt(data.readInt());
			mapRecord.put(record.getPlayerId(),record);
		}
		
		le=data.readInt();
		propSids=new int[le];
		for(int i=0;i<propSids.length;i++)
		{
			propSids[i]=data.readInt();
		}
		le=data.readInt();
		randomPros=new int[le];
		for(int i=0;i<randomPros.length;i++)
		{
			randomPros[i]=data.readInt();
		}
		le=data.readInt();
		prompts=new int[le];
		for(int i=0;i<prompts.length;i++)
		{
			prompts[i]=data.readInt();
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeUTF(name);
		data.writeUTF(describe);
		data.writeInt(price);
		data.writeInt(buyTimes);
		data.writeInt(mapRecord.size());
		Object objects[]=mapRecord.valueArray(); 
		for(int i=0;i<objects.length;i++)
		{
			ActivityRecord record=(ActivityRecord)objects[i];
			data.writeInt(record.getPlayerId());
			data.writeShort(record.getTimes());
			data.writeInt(record.getCreateAt());
		}
		data.writeInt(propSids.length);
		for(int i=0;i<propSids.length;i++)
		{
			data.writeInt(propSids[i]);
		}
		if(randomPros!=null && randomPros.length!=0)
		{
			data.writeInt(randomPros.length);
			for(int i=0;i<randomPros.length;i++)
			{
				data.writeInt(randomPros[i]);
			}
		}
		else
			data.writeInt(0);
		if(prompts!=null && prompts.length!=0)
		{
			data.writeInt(prompts.length);
			for(int i=0;i<prompts.length;i++)
			{
				data.writeInt(prompts[i]);
			}
		}
		else
			data.writeInt(0);
		return data;
	}

	@Override
	public void showByteWriteNew(ByteBuffer data,Player player,
		CreatObjectFactory objfactory)
	{
		super.showByteWriteNew(data,player,objfactory);
		data.writeShort(getSid());
		data.writeInt(startTime);
		data.writeInt(endTime);
		data.writeUTF(name);
		data.writeUTF(describe);
		data.writeInt(price);
		/** ʣ����� **/
		int times=buyTimes-getBuyTimes(player.getId());
		data.writeShort(times<0?0:times);
	}
	
	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeInt(startTime);
		data.writeInt(endTime);
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
 		JBackKit.sendSellingActivty(smap,this);
	}

	/** ���� **/
	public void buy(Player player,CreatObjectFactory factory,ByteBuffer data)
	{
		if(!check(player.getId()))
			throw new DataAccessException(0,"today times out");
		String result=checkGems(player,factory);
		if(result!=null) throw new DataAccessException(0,result);
		addRecord(player.getId());
		int length=1;
		//����������Ʒ
		int esids[]=getRandomPro();
		if(esids!=null)length++;
		data.writeShort(length);
		award.setRandomProps(propSids);
		for(int i=0;i<length;i++)
		{
			if(i>0)
			{
				Award award1=(Award)Award.factory
								.newSample(ActivityContainer.EMPTY_SID);
				SeaBackKit.resetAward(award1,esids);
				getAward(player,data,award1,factory,esids);
				/**��־��¼**/
				addActivityLog(player,length,data);
				return ;
			}
			getAward(player,data,award,factory,null);
		}
		/**��־��¼**/
		addActivityLog(player,length,data);
	}

	
	public void getAward(Player player,ByteBuffer data,Award award,CreatObjectFactory factory,int []esids)
	{
		String message=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"sellingcitivity_message");
		message=SeaBackKit.setSystemContent(message,"%",ChatMessage.SEPARATORS,player.getName());
		award.awardSelf(player,TimeKit.getSecondTime(),data,factory,
			message,prompts,new int[]{
				EquipmentTrack.FROM_SELLING_PACK1,
				FightScoreConst.NEW_ACTIVITY});
		if(esids!=null)
		{
			sendSystemAwardMessage(esids[0],esids[1],factory,message,prompts);
		}
	}
	
	/** ���ݸ����Ƿ��������Ʒ�Ĳ��� **/
	public int[] getRandomPro()
	{
		if(randomPros==null||randomPros.length==0) return null;
		int random=MathKit.randomValue(0,Award.PROB_ABILITY);
		if(random>randomPros[randomPros.length-1]) return null;
		int startProbability=0;
		for(int i=0;i<randomPros.length;i+=3)
		{
			if(random>=startProbability&&random<=randomPros[i+2])
			{
				int awardSid[]=new int[2];
				awardSid[0]=randomPros[i];
				awardSid[1]=randomPros[i+1];
				return awardSid;
			}
			startProbability=randomPros[i+2];
		}
		return null;
	}

	/** ��� **/
	public boolean check(int playerID)
	{
		ActivityRecord record=(ActivityRecord)mapRecord.get(playerID);
		if(record==null) return true;
		if(!SeaBackKit.isSameDay(record.getCreateAt(),
			TimeKit.getSecondTime()))
		{
			record.setTimes(0);
			return true;
		}
		if(record.getTimes()<buyTimes) return true;
		return false;
	}

	/** ��ⱦʯ **/
	public String checkGems(Player player,CreatObjectFactory factory)
	{
		if(!Resources.checkGems(price,player.getResources()))
			return "not enough gems";
		Resources.reduceGems(price,player.getResources(),player);
		// ��ʯ���Ѽ�¼
		factory.createGemTrack(GemsTrack.SELLING_P_THREE,player.getId(),price,
			0,Resources.getGems(player.getResources()));
		// ����change��Ϣ
		TaskEventExecute.getInstance().executeEvent(
			PublicConst.GEMS_ADD_SOMETHING,null,player,price);
		return null;
	}

	/** ���Ӽ�¼ **/
	public void addRecord(int playerId)
	{
		ActivityRecord record=(ActivityRecord)mapRecord.get(playerId);
		if(record==null) 
		{
			record=new ActivityRecord();
			record.setPlayerId(playerId);
			mapRecord.put(playerId,record);
		}
		record.setCreateAt(TimeKit.getSecondTime());
		record.add();
	}

	/** ��ȡ������� **/
	public int getBuyTimes(int playerId)
	{
		ActivityRecord record=(ActivityRecord)mapRecord.get(playerId);
		if(record==null) return 0;
		return record.getTimes();
	}

	@Override
	public Object copy(Object obj)
	{
		SellingPackageThree three=(SellingPackageThree)super.copy(obj);
		three.mapRecord=new IntKeyHashMap();
		return three;
	}
	/** ��ʼ�� **/
	public void initData(String initData)
	{
		String[] str=TextKit.split(initData,";");
		name=str[0];
		describe=str[1];
		buyTimes=TextKit.parseInt(str[2]);
		price=TextKit.parseInt(str[3]);
		addPrompt(str[4]);
		addPropSids(str[5]);
		if(str.length>=7)
			addRandomSids(str[6]);
	}

	public void addPropSids(String str)
	{
		String[] initData=TextKit.split(str,",");
		propSids=new int[initData.length];
		for(int i=0;i<initData.length;i++)
		{
			propSids[i]=TextKit.parseInt(initData[i]);
		}
	}

	/**�����ʾ**/
	public void addPrompt(String str)
	{
		if(str==null || str.length()==0 || str.equals("0")) return;
		String[] initData=TextKit.split(str,",");
		prompts=new int[initData.length];
		for(int i=0;i<initData.length;i++)
		{
			prompts[i]=TextKit.parseInt(initData[i]);
		}
	}
	/** ��������Ʒ **/
	public void addRandomSids(String str)
	{
		String[] initData=TextKit.split(str,",");
		randomPros=new int[initData.length];
		for(int i=0;i<initData.length;i++)
		{
			randomPros[i]=TextKit.parseInt(initData[i]);
		}
	}
	
	public String awardPackageToString()
	{
		if(propSids==null) return "";
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<propSids.length;i++)
		{
			if(i%3==0)
			{
				String name=propSids[i]+"";
				Prop prop=(Prop)Prop.factory.getSample(propSids[i]);
				if(prop!=null) name=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,
					"prop_sid_"+propSids[i]);
				sb.append(name).append(",");
			}else
			{
				sb.append(propSids[i]).append(",");
			}
		}
		return sb.toString();
	}
	
	public String awardSpecialToString()
	{
		if(randomPros==null) return "";
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<randomPros.length;i++)
		{
			if(i%3==0)
			{
				String name=randomPros[i]+"";
				Prop prop=(Prop)Prop.factory.getSample(randomPros[i]);
				if(prop!=null) name=InterTransltor.getInstance().getTransByKey(
					PublicConst.SERVER_LOCALE,
					"prop_sid_"+randomPros[i]);
				sb.append(name).append(",");
			}else
			{
				sb.append(randomPros[i]).append(",");
			}
		}
		return sb.toString();
	}

	/** ϵͳ�������� **/
	public String prStringToString()
	{
		if(prompts==null) return "";
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<prompts.length;i++)
		{
			sb.append(prompts[i]+",");
		}
		return sb.toString();
	}
	
	private void sendSystemAwardMessage(int sid,int count,
		CreatObjectFactory objectFactory,String message,int[] noticeSids)
	{
		if(sid<=0||objectFactory==null||noticeSids==null||message==null
			||message.length()==0) return;
		for(int i=0;i<noticeSids.length;i++)
		{
			if(sid==noticeSids[i])
			{
				if(TextKit.split(message,"%").length>2)
				{
					message=TextKit.replace(message,"%",count+"");
				}
				message=TextKit
					.replace(
						message,
						"%",
						InterTransltor.getInstance().getTransByKey(
							PublicConst.SERVER_LOCALE,
							"prop_sid_"+noticeSids[i]));
				SeaBackKit.sendSystemMsg(objectFactory.getDsmanager(),
					message);
			}
		}
	}
	
	/**��ȡ���е���Ʒ��Ϣ**/
	public int[] getAllProSids()
	{
		IntList list=new IntList();
		if(propSids!=null)
		{
			for(int i=0;i<propSids.length;i++)
			{
				list.add(propSids[i]);
			}
		}
		if(randomPros!=null)
		{
			for(int i=0;i<randomPros.length;i++)
			{
				list.add(randomPros[i]);
			}
		}
		return list.toArray();
	}

	/**�����Ʒ��־**/
	public void addActivityLog(Player player,int length,ByteBuffer data)
	{
		for(int i=0;i<length;i++)
		{
			String sidnum=SeaBackKit.getActivityAwardInfo(data,i);
			if(sidnum!=null)
			ActivityLogMemCache.getInstance().collectAlog(this.getId(),
				sidnum,player.getId(),i==0?price:0);
		}
	}
	
	public int[] getRandomPros()
	{
		return randomPros;
	}

	
	public int getPrice()
	{
		return price;
	}
}

