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
 * �ʾ����
 */
public class QuestionnaireActivity extends Activity implements ActivitySave
{

	/** �ʾ������Ŀ���� SHORT_ANSWER ���,SINGLE_CHOICE ��ѡ, ������ѡ ֵΪ����ѡ�� */
	public static final int SHORT_ANSWER=0,SINGLE_CHOICE=1;
	/** ����� */
	String introduction;
	/** �ȼ�Ҫ�� ��16λΪ���ޣ���16λΪ���� */
	int needLevel;
	/** �ʾ������Ŀ */
	QuestionContent[] content;
	/** ��Ҵ𰸼��콱��� value: String[] ���һλΪ�Ƿ�������� */
	IntKeyHashMap playerAnswer;
	/** ��ƷString */
	String awardStr;
	/** ��Ʒ */
	Award award;

	/** ��Ŀ�ش����ͳ�� */
	int[] topicInfo;
	/** ����𰸷ֲ����ͳ�� */
	int[][] answerInfo;
	// /** ��һش���� key:playerId,value:��Ҵ����� */
	// IntKeyHashMap playerCount;

	/** �������� */
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
		data.writeByte(content.length); // ��Ŀ����
		for(int i=0;i<content.length;i++)
		{
			QuestionContent q=content[i];
			q.showBytesWrite(data); // ������Ŀ
			String[] answer=(String[])playerAnswer.get(player.getId());
			if(answer==null||answer[i]==null||answer[i].equals(""))
				data.writeUTF("");
			else
				data.writeUTF(answer[i]); // �����
		}
		int isReward=canReward(player.getId());
		if(isReward==1)
			data.writeByte(1); // �Ƿ���ȡ������ 1��ȡ�� 0δ��ȡ
		else
			data.writeByte(0);
		// ��Ʒ
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
			// ֻ�ڻ���д������ȡ״̬
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

	/** ��ʼ��playerAnswer */
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
	 * ��Ŀ��ʼ��
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
	 * ��Ʒ��ʼ��
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
		// ��һλΪ��ʯ
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
	 * �Ƿ���ϵȼ�
	 */
	public boolean isInLevel(int level)
	{
		if(needLevel==0) return true;
		int[] lv=SeaBackKit.get2ShortInInt(needLevel);
		if(level>=lv[0]&&level<=lv[1]) return true;
		return false;
	}

	/**
	 * ����
	 * 
	 * @param index ��� 0��ʼ
	 * @param answer �� ����ַ��� ��ѡ��int ��ѡint ���Ÿ���
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
		// ÿ��һ���� ��������ʾ��
		saveAnswer(playerId,index,answer);
		setChanged();
	}

	/**
	 * �Ƿ���콱
	 * 
	 * @return 0�����콱 1�Ѿ���ȡ 2��Ŀδ����
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
	 * �콱
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

	/** �����ݿ��л�ȡ��һش��� */
	private QuestionnaireRecord[] getDBAnswer(int playerId,int actId)
	{
		CreatObjectFactory factory=ActivityContainer.getInstance().objectFactory;
		String sql="SELECT * FROM questionnaire WHERE 1=1";
		if(playerId!=0) sql+=" AND player_id="+playerId;
		if(actId!=0) sql+=" AND act_id="+actId;
		sql+=";";
		return factory.getQuestionnaireDBAccess().loadBySql(sql);
	}

	/** �洢�����ʾ�� */
	private boolean saveAnswer(int playerId,int topicIndex,String answer)
	{
		QuestionContent qc=content[topicIndex];
		int topicType=qc.getType();
		return ActivityContainer.getInstance().objectFactory
			.saveQuestionnaireAnswer(playerId,id,topicIndex,topicType,answer);
	}

	/**
	 * ͳ����Ŀ��Ϣ
	 */
	public void countTopicInfo()
	{
		if(topicInfo==null) topicInfo=new int[content.length];
		if(answerInfo==null) answerInfo=new int[content.length][4];
		int[] keys=playerAnswer.keyArray();
		for(int i=0;i<keys.length;i++)
		{
			// ͳ������Ѵ���Ŀ����
			int num=0;
			String[] answer=(String[])playerAnswer.get(keys[i]);
			// answer[j.length-1]Ϊ�Ƿ���ȡ����
			for(int j=0;j<answer.length-1;j++)
			{
				String answerStr=answer[j];
				int[] option=answerInfo[j];
				if(answerStr!=null)
				{
					num++;
					// ������������ͳ��
					if(content[j].getType()==0) continue;
					// ͳ��ÿ����Ŀѡ���ѡȡ��
					if(answerStr.contains("0")) option[0]=option[0]++;
					if(answerStr.contains("1")) option[1]=option[1]++;
					if(answerStr.contains("2")) option[2]=option[2]++;
					if(answerStr.contains("3")) option[3]=option[3]++;
				}
			}
			// ͳ��ÿ�����ж����˻ش�
			topicInfo[num]=topicInfo[num]++;
		}
	}

	/**
	 * �ʾ���Ŀ��
	 */
	public class QuestionContent
	{

		/** ��Ŀ */
		String topic;
		/** ��Ŀ���� 0������� 1��ѡ ������ѡ��Ϊ����ѡ�� */
		int type;
		/** ѡ�� ������Ӧѡ��Ϊ�� */
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
