package foxu.sea.activity;

import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.ArrayList;
import mustang.set.IntKeyHashMap;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.QuestionnaireRecord;
import foxu.sea.award.Award;
import foxu.sea.equipment.EquipmentTrack;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.proplist.Prop;

/**
 * 问卷调查活动
 */
public class QuestionnaireActivity extends Activity implements ActivitySave
{

	/** 问卷调查题目类型 SHORT_ANSWER 简答,SINGLE_CHOICE 单选, 其他多选 值为最大可选数 */
	public static final int SHORT_ANSWER=0,SINGLE_CHOICE=1;
	/** 活动介绍 */
	String introduction;
	/** 等级要求 左16位为下限，右16位为上限 */
	int needLevel;
	/** 问卷调查题目 */
	QuestionContent[] content;
	/** 玩家答案及领奖情况 value: String[] 最后一位为是否领过奖励 */
	IntKeyHashMap playerAnswer;
	/** 奖品String */
	String awardStr;
	/** 奖品 */
	Award award;

	/** 题目回答情况统计 */
	int[] topicInfo;
	/** 各题答案分布情况统计 */
	int[][] answerInfo;
	// /** 玩家回答情况 key:playerId,value:玩家答题数 */
	// IntKeyHashMap playerCount;

	/** 保存设置 */
	protected boolean isChange=false;
	protected final int INTERVALTIME=5000;
	protected long lastSaveTime=0;

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		return resetActivity(stime,etime,initData,factory);
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factoty)
	{
		if(initData!=null)
		{
			if(playerAnswer==null) playerAnswer=new IntKeyHashMap();
			String[] datas=initData.split("\\|");
			introduction=datas[0];
			topicInit(datas[1]);
			needLevel=Integer.parseInt(datas[2]);
			awardStr=datas[3];
			awardInit(awardStr);
			if(topicInfo==null) topicInfo=new int[content.length];
			if(answerInfo==null) answerInfo=new int[content.length][4];
		}
		startTime=SeaBackKit.parseFormatTime(stime);
		endTime=SeaBackKit.parseFormatTime(etime);
		if(startTime<TimeKit.getSecondTime())
			startTime=TimeKit.getSecondTime();
		return getActivityState();
	}

	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"stime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"etime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"type\":0").append(",\"topicinfo\":[");
		for(int i=0;i<topicInfo.length;i++)
		{
			sb.append(topicInfo[0]);
			if(i!=topicInfo.length-1) sb.append(",");
		}
		sb.append("]");
		sb.append(",\"answerinfo\":[");
		for(int i=0;i<answerInfo.length;i++)
		{
			sb.append("[");
			int[] options=answerInfo[i];
			for(int j=0;j<options.length;j++)
			{
				sb.append(options[j]);
				if(j!=options.length-1) sb.append(",");
			}
			if(i!=answerInfo.length-1)
				sb.append("],");
			else
				sb.append("]");
		}
		sb.append("]");
		sb.append(",\"opened\":").append(isOpen(TimeKit.getSecondTime()))
			.append("}");
		return sb.toString();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		introduction=data.readUTF();
		int len=data.readUnsignedShort();
		if(content==null) content=new QuestionContent[len];
		for(int i=0;i<len;i++)
		{
			QuestionContent qc=new QuestionContent();
			content[i]=(QuestionContent)qc.bytesRead(data);
		}
		needLevel=data.readInt();
		awardStr=data.readUTF();
		awardInit(awardStr);
		bytesReadPlayerAnswer(data);
		if(topicInfo==null) topicInfo=new int[content.length];
		if(answerInfo==null) answerInfo=new int[content.length][4];
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		data.writeUTF(introduction);
		data.writeShort(content.length);
		for(int i=0;i<content.length;i++)
		{
			QuestionContent qc=content[i];
			qc.bytesWrite(data);
		}
		data.writeInt(needLevel);
		data.writeUTF(awardStr);
		bytesWritePlayerAnswer(data);
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeByte(0);
		data.writeInt(endTime-TimeKit.getSecondTime());
	}

	public void showByteWrite(ByteBuffer data,Player player)
	{
		data.writeByte(content.length); // 题目数量
		for(int i=0;i<content.length;i++)
		{
			QuestionContent q=content[i];
			q.showBytesWrite(data); // 该题题目
			String[] answer=(String[])playerAnswer.get(player.getId());
			if(answer==null||answer[i]==null||answer[i].equals(""))
				data.writeUTF("");
			else
				data.writeUTF(answer[i]); // 该题答案
		}
		int isReward=canReward(player.getId());
		if(isReward==1)
			data.writeByte(1); // 是否领取过奖励 1领取过 0未领取
		else
			data.writeByte(0);
		// 奖品
		award.viewAward(data,player);
		data.writeUTF(introduction);
	}

	@Override
	public void sendFlush(SessionMap smap)
	{

	}

	public void bytesWritePlayerAnswer(ByteBuffer data)
	{
		int[] keys=playerAnswer.keyArray();
		data.writeShort(keys.length);
		for(int i=0;i<keys.length;i++)
		{
			// 只在活动表中存玩家领取状态
			String[] str=(String[])playerAnswer.get(keys[i]);
			data.writeInt(keys[i]);
			// data.writeInt(str.length);
			data.writeUTF(str[str.length-1]);
			// for(int j=0;j<str.length;j++)
			// data.writeUTF(str[j]);
		}
	}

	public void bytesReadPlayerAnswer(ByteBuffer data)
	{
		if(playerAnswer==null) playerAnswer=new IntKeyHashMap();
		playerAnswer.clear();
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			int playerId=data.readInt();
			// int l=data.readInt();
			String[] strArr=new String[content.length+1];
			// for(int j=0;j<l;j++)
			// {
			// strArr[j]=data.readUTF();
			// }
			initPlayerAnswer(strArr,playerId,id);
			strArr[strArr.length-1]=data.readUTF();
			playerAnswer.put(playerId,strArr);
		}
	}

	/** 初始化playerAnswer */
	private void initPlayerAnswer(String[] answers,int playerId,int actId)
	{
		QuestionnaireRecord[] qrArr=getDBAnswer(playerId,actId);
		if(qrArr!=null)
		{
			for(int i=0;i<qrArr.length;i++)
			{
				QuestionnaireRecord qr=qrArr[i];
				answers[qr.getTopicIndex()]=qr.getAnswer();
			}
		}
	}

	/**
	 * 题目初始化
	 */
	private void topicInit(String initData)
	{
		String[] datas=initData.split(";");
		if(content==null) content=new QuestionContent[datas.length];
		for(int i=0;i<datas.length;i++)
		{
			QuestionContent q=new QuestionContent();
			String[] qArr=datas[i].split(":");
			q.setTopic(qArr[0]);
			q.setType(Integer.parseInt(qArr[1]));
			q.setOption(qArr[2].split(","));
			content[i]=q;
		}
	}

	/**
	 * 奖品初始化
	 */
	private void awardInit(String initData)
	{
		if(award==null)
			award=(Award)Award.factory
				.newSample(ActivityContainer.EMPTY_SID);
		ArrayList propList=new ArrayList();
		ArrayList equipList=new ArrayList();
		ArrayList shipList=new ArrayList();
		ArrayList officerList=new ArrayList();
		String[] subAwardSids=initData.split(",");
		// 第一位为宝石
		int gems=Integer.parseInt(subAwardSids[0]);
		award.setGemsAward(gems);
		for(int j=1;j<subAwardSids.length;j+=2)
		{
			int sid=Integer.parseInt(subAwardSids[j]);
			int count=Integer.parseInt(subAwardSids[j+1]);
			if(SeaBackKit.getSidType(sid)==Prop.PROP)
			{
				propList.add(sid);
				propList.add(count);
			}
			if(SeaBackKit.getSidType(sid)==Prop.EQUIP)
			{
				equipList.add(sid);
				equipList.add(count);
			}
			if(SeaBackKit.getSidType(sid)==Prop.SHIP)
			{
				shipList.add(sid);
				shipList.add(count);
			}
			if(SeaBackKit.getSidType(sid)==Prop.OFFICER)
			{
				officerList.add(sid);
				officerList.add(count);
			}
		}
		int[] propArr=new int[propList.toArray().length];
		int[] equipArr=new int[equipList.toArray().length];
		int[] shipArr=new int[shipList.toArray().length];
		int[] officerArr=new int[officerList.toArray().length];
		if(propArr.length>0) for(int j=0;j<propList.size();j++)
			propArr[j]=(Integer)propList.get(j);
		if(equipArr.length>0) for(int j=0;j<equipList.size();j++)
			equipArr[j]=(Integer)equipList.get(j);
		if(shipArr.length>0) for(int j=0;j<shipList.size();j++)
			shipArr[j]=(Integer)shipList.get(j);
		if(officerArr.length>0) for(int j=0;j<officerList.size();j++)
			officerArr[j]=(Integer)officerList.get(j);
		award.setPropSid(propArr);
		award.setEquipSids(equipArr);
		award.setShipSids(shipArr);
		award.setOfficerSids(officerArr);
	}

	/**
	 * 是否符合等级
	 */
	public boolean isInLevel(int level)
	{
		if(needLevel==0) return true;
		int[] lv=SeaBackKit.get2ShortInInt(needLevel);
		if(level>=lv[0]&&level<=lv[1]) return true;
		return false;
	}

	/**
	 * 答题
	 * 
	 * @param index 题号 0开始
	 * @param answer 答案 简答：字符串 单选：int 多选int 逗号隔开
	 */
	public void answer(int playerId,int index,String answer)
	{
		String[] pAnswer;
		if(playerAnswer.get(playerId)==null)
		{
			pAnswer=new String[content.length+1];
			pAnswer[pAnswer.length-1]="0";
		}
		else
			pAnswer=(String[])playerAnswer.get(playerId);
		pAnswer[index]=answer;
		playerAnswer.put(playerId,pAnswer);
		// 每答一道题 存入调查问卷表
		saveAnswer(playerId,index,answer);
		setChanged();
	}

	/**
	 * 是否可领奖
	 * 
	 * @return 0可以领奖 1已经领取 2题目未答完
	 */
	public int canReward(int playerId)
	{
		String[] pAnswer;
		if(playerAnswer.get(playerId)==null) return 2;
		pAnswer=(String[])playerAnswer.get(playerId);
		if(pAnswer[pAnswer.length-1].equals("1")) return 1;
		for(int i=0;i<pAnswer.length-1;i++)
			if(pAnswer[i]==null) return 2;
		return 0;
	}

	/**
	 * 领奖
	 */
	public void reward(Player player,ByteBuffer data,
		CreatObjectFactory objectFactory)
	{
		if(data==null) data=new ByteBuffer();
		award.awardSelf(player,TimeKit.getSecondTime(),data,objectFactory,
			null,new int[]{EquipmentTrack.QUESTIONNAIRE});
		data.clear();
		// award.viewAward(data,player);
		String[] pAnswer;
		pAnswer=(String[])playerAnswer.get(player.getId());
		pAnswer[pAnswer.length-1]="1";
		playerAnswer.put(player.getId(),pAnswer);
		setChanged();
	}

	/** 从数据库中获取玩家回答结果 */
	private QuestionnaireRecord[] getDBAnswer(int playerId,int actId)
	{
		CreatObjectFactory factory=ActivityContainer.getInstance().objectFactory;
		String sql="SELECT * FROM questionnaire WHERE 1=1";
		if(playerId!=0) sql+=" AND player_id="+playerId;
		if(actId!=0) sql+=" AND act_id="+actId;
		sql+=";";
		return factory.getQuestionnaireDBAccess().loadBySql(sql);
	}

	/** 存储调查问卷答案 */
	private boolean saveAnswer(int playerId,int topicIndex,String answer)
	{
		QuestionContent qc=content[topicIndex];
		int topicType=qc.getType();
		return ActivityContainer.getInstance().objectFactory
			.saveQuestionnaireAnswer(playerId,id,topicIndex,topicType,answer);
	}

	/**
	 * 统计题目信息
	 */
	public void countTopicInfo()
	{
		if(topicInfo==null) topicInfo=new int[content.length];
		if(answerInfo==null) answerInfo=new int[content.length][4];
		int[] keys=playerAnswer.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			// 统计玩家已答题目数量
			int num=0;
			String[] answer=(String[])playerAnswer.get(keys[i]);
			// answer[j.length-1]为是否领取奖励
			for(int j=0;j<answer.length-1;j++)
			{
				String answerStr=answer[j];
				int[] option=answerInfo[j];
				if(answerStr!=null)
				{
					num++;
					// 如果简答题跳过统计
					if(content[j].getType()==0) continue;
					// 统计每个题目选项的选取量
					if(answerStr.contains("0")) option[0]=option[0]++;
					if(answerStr.contains("1")) option[1]=option[1]++;
					if(answerStr.contains("2")) option[2]=option[2]++;
					if(answerStr.contains("3")) option[3]=option[3]++;
				}
			}
			// 统计每个题有多少人回答
			topicInfo[num]=topicInfo[num]++;
		}
	}

	/**
	 * 问卷题目类
	 */
	public class QuestionContent
	{

		/** 题目 */
		String topic;
		/** 题目类型 0简答文字 1单选 其他多选且为最多可选项 */
		int type;
		/** 选项 简答题对应选项为空 */
		String[] option;

		public void showBytesWrite(ByteBuffer data)
		{
			data.writeUTF(topic);
			data.writeByte(type);
			data.writeByte(option.length);
			for(int i=0;i<option.length;i++)
			{
				data.writeUTF(option[i]);
			}
		}

		public void bytesWrite(ByteBuffer data)
		{
			data.writeUTF(topic);
			data.writeShort(type);
			data.writeShort(option.length);
			for(int i=0;i<option.length;i++)
				data.writeUTF(option[i]);
		}

		public Object bytesRead(ByteBuffer data)
		{
			topic=data.readUTF();
			type=data.readUnsignedShort();
			int len=data.readUnsignedShort();
			if(option==null) option=new String[len];
			for(int i=0;i<len;i++)
				option[i]=data.readUTF();
			return this;
		}

		public String getTopic()
		{
			return topic;
		}

		public void setTopic(String topic)
		{
			this.topic=topic;
		}

		public int getType()
		{
			return type;
		}

		public void setType(int type)
		{
			this.type=type;
		}

		public String[] getOption()
		{
			return option;
		}

		public void setOption(String[] option)
		{
			this.option=option;
		}

	}

	@Override
	public boolean isSave()
	{
		if(TimeKit.getMillisTime()-lastSaveTime>INTERVALTIME&&isChange)
		{
			return true;
		}
		return false;
	}

	@Override
	public void setSave()
	{
		lastSaveTime=TimeKit.getMillisTime();
		isChange=false;
	}

	public void setChanged()
	{
		isChange=true;
	}

}
