package foxu.sea.activity;


import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.set.IntList;
import mustang.text.TextKit;
import mustang.util.TimeKit;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.Player;
import foxu.sea.award.Award;
import foxu.sea.kit.SeaBackKit;


/**
 * Ӧ�÷���
 * @author Alan
 *
 */
public class AppShareActivity extends Activity implements ActivitySave
{

	public static final int SAVE_CIRCLE=15*60;
	public static final int OPEN=1,CLOSE=2,ADD=3,UPDATE=4,VIEW=5,REMOVE=6;
	/** ��ȡ��¼ */
	IntList playerRecord=new IntList();
	/** �����¼ */
	IntList playerForward=new IntList();
	private Award award=(Award)Award.factory
		.newSample(ActivityContainer.EMPTY_SID);
	int[] awardProps;
	int lastSaveTime;
	
	@Override
	public String getActivityState()
	{
		StringBuilder sb=new StringBuilder();
		sb.append("{\"id\":\"").append(id).append('"').append(",\"sid\":\"")
			.append(getSid()).append('"').append(",\"starttime\":\"")
			.append(SeaBackKit.formatDataTime(startTime)).append('"')
			.append(",\"endtime\":\"")
			.append(SeaBackKit.formatDataTime(endTime)).append('"')
			.append(",\"others\":\"");
		for(int i=0;i<awardProps.length;i++)
		{
			sb.append(awardProps[i]+",");
		}
		sb.append("\",\"opened\":")
			.append(isOpen(TimeKit.getSecondTime())).append("}");
		return sb.toString();
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=new ByteBuffer();
		// �����ȡ��¼
		data.writeInt(playerRecord.size());
		for(int i=0;i<playerRecord.size();i++)
		{
			data.writeInt(playerRecord.get(i));
		}
		// �����ת��¼
		data.writeInt(playerForward.size());
		for(int i=0;i<playerForward.size();i++)
		{
			data.writeInt(playerForward.get(i));
		}
		// ����
		data.writeByte(awardProps.length);
		for(int i=0;i<awardProps.length;i++)
		{
			data.writeShort(awardProps[i]);
		}
		return data;
	}

	@Override
	public void showByteWrite(ByteBuffer data)
	{
		data.writeShort(getSid());
		data.writeInt(endTime-TimeKit.getSecondTime());
	}

	@Override
	public void sendFlush(SessionMap smap)
	{
		// TODO Auto-generated method stub

	}

	public Award getShareAward()
	{
		return award;
	}

	public void resetPlatURL(String initData,String stime,String etime)
	{
		String[] infos=TextKit.split(initData,";");
		int type=TextKit.parseInt(infos[0]);
		if(type==VIEW)
			return;
		if(type==OPEN||type==UPDATE)
		{
			awardProps=TextKit.parseIntArray(TextKit.split(infos[4],","));
			resetAward();
			startTime=SeaBackKit.parseFormatTime(stime);
			endTime=SeaBackKit.parseFormatTime(etime);
		}
	}

	public boolean isCompleteAward(Player player)
	{
		if(playerRecord.contain(player.getId())) return true;
		return false;
	}

	public void addPlayerRecord(Player player)
	{
		playerRecord.add(player.getId());
	}

	public boolean isCompleteForward(Player player)
	{
		if(playerForward.contain(player.getId())) return true;
		return false;
	}

	public void addPlayerForward(Player player)
	{
		playerForward.add(player.getId());
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
		resetPlatURL(initData,stime,etime);
		return getActivityState();
	}

	@Override
	public void initData(ByteBuffer data,CreatObjectFactory factoty,
		boolean active)
	{
		// �����ȡ��¼
		int len=data.readInt();
		for(int i=0;i<len;i++)
		{
			playerRecord.add(data.readInt());
		}
		// ��ҷ����¼
		len=data.readInt();
		for(int i=0;i<len;i++)
		{
			playerForward.add(data.readInt());
		}
		// ����
		awardProps=new int[data.readUnsignedByte()];
		for(int i=0;i<awardProps.length;i++)
		{
			awardProps[i]=data.readUnsignedShort();
		}
		resetAward();
	}

	/** ����ַ����ǿմ�ת������,��ع�Ϊ�մ����ǡ�null���ַ� */
	public String checkNullString(String str)
	{
		if(str==null||str.equalsIgnoreCase("null")) return null;
		return str;
	}

	public void resetAward()
	{
		SeaBackKit.resetAward(award,awardProps);
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
	public Object copy(Object ob)
	{
		AppShareActivity asa=(AppShareActivity)ob;
		/** ��ȡ��¼ */
		asa.playerRecord=new IntList();
		/** �����¼ */
		asa.playerForward=new IntList();
		asa.award=(Award)Award.factory
			.newSample(ActivityContainer.EMPTY_SID);
		return asa;
	}
}
