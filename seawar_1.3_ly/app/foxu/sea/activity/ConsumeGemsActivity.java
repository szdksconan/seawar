package foxu.sea.activity;

import java.util.HashSet;
import java.util.Set;

import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.ActivityLogMemCache;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.Resources;
import foxu.sea.gems.GemsTrack;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.MessageKit;
import foxu.sea.kit.SeaBackKit;
import mustang.back.SessionMap;
import mustang.io.ByteBuffer;
import mustang.set.IntKeyHashMap;
import mustang.text.TextKit;
import mustang.util.TimeKit;

/**
 * �ۼ����ѻ
 * 
 * @author Alan
 */
public class ConsumeGemsActivity extends ExpActivity implements ActivitySave
{
	/** �ۼ����ѻ�����ٷֱ� */
	public static int PERCENT=10;
	/** ���Ϣ�������� */
	public static final int SAVE_CIRCLE=15*60;
	/** �����Ŀ������� */
	public static final int MAX_RECORD=10;
	/** ��ǰ������� */
	Set<Integer> players;
	/** �ϴα���ʱ�� */
	int lastSaveTime;

	/** ���Ż����,���ؽ����ı�ʯ���� */
	public static int award(Player player,int aid)
	{
		IntKeyHashMap records=player.getConsumeGems();
		int addGems=0;
		if(records.get(aid)!=null)
		{
			ConsumeGemsRecord record=(ConsumeGemsRecord)records.remove(aid);
			addGems=record.getCount()*record.getPercent()/100;
			CreatObjectFactory objectFactory=ActivityContainer.getInstance()
				.getObjectFactory();
			objectFactory.createGemTrack(GemsTrack.CONSUME_GEMS,
				player.getId(),addGems,aid,
				Resources.getGems(player.getResources())+addGems);
			Resources.addGemsNomal(addGems,player.getResources(),player);
		}
		JBackKit.sendConsumeGemsState(player);
		return addGems;
	}

	/** �������Ѽ�¼ */
	public void addRecord(Player player,int consumeGems)
	{
		players.add(player.getId());
		IntKeyHashMap playerRecords=player.getConsumeGems();
		ConsumeGemsRecord record=null;
		if(playerRecords.get(getId())!=null)
			record=(ConsumeGemsRecord)playerRecords.get(getId());
		else
		{
			if(playerRecords.size()>=10)
			{
				// �����һ����¼���Ž���
				int[] aids=playerRecords.keyArray();
				int firstAid=aids[0];
				for(int i=1;i<aids.length;i++)
				{
					if(aids[i]<firstAid) firstAid=aids[i];
				}
				ConsumeGemsRecord firstRecord=(ConsumeGemsRecord)playerRecords
					.get(firstAid);
				int returnGems=award(player,firstAid);
				sendMessage(player,firstRecord,returnGems);
			}
			record=new ConsumeGemsRecord(getId(),getStartTime(),
				getEndTime(),getPercent());
			playerRecords.put(record.getAid(),record);
		}
		record.setCount(record.getCount()+consumeGems);
		ActivityLogMemCache.getInstance().collectAlog(getId(),"",
			player.getId(),consumeGems);
		JBackKit.sendConsumeGemsState(player);
	}

	public static void showBytesWrite(Player player,ByteBuffer data)
	{
		ConsumeGemsActivity activity=(ConsumeGemsActivity)ActivityContainer
			.getInstance().getActivity(ActivityContainer.CONSUME_GEMS_ID,0);
		int aid=0;
		if(activity!=null&&activity.isOpen(TimeKit.getSecondTime()))
			aid=activity.getId();
		IntKeyHashMap playerRecords=player.getConsumeGems();
		ConsumeGemsRecord record=null;
		int[] aids=playerRecords.keyArray();
		int len=0;
		int top=data.top();
		data.writeByte(len);
		// ��ǰ���������û�м�¼
		if(aid!=0&&playerRecords.get(aid)==null)
		{
			len++;
			data.writeInt(activity.getId());
			data.writeInt(activity.getPlayerConsume(player));
			data.writeByte(activity.getPercent());
			data.writeBoolean(false);
		}
		for(int i=0;i<aids.length;i++)
		{
			record=(ConsumeGemsRecord)playerRecords.get(aids[i]);
			if(record!=null)
			{
				len++;
				data.writeInt(record.getAid());
				data.writeInt(record.getCount());
				data.writeByte(record.getPercent());
				data.writeBoolean(!(record.getAid()==aid));
			}
		}
		int newTop=data.top();
		data.setTop(top);
		data.writeByte(len);
		data.setTop(newTop);
	}

	/** ��ȡ��ҵ�ǰ���¼ */
	public int getPlayerConsume(Player player)
	{
		IntKeyHashMap playerRecords=player.getConsumeGems();
		if(playerRecords.get(getId())!=null)
			return ((ConsumeGemsRecord)playerRecords.get(getId()))
				.getCount();
		return 0;
	}

	@Override
	public String startActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		String state=super.startActivity(stime,etime,PERCENT+"",factory);
		lastSaveTime=TimeKit.getSecondTime();
		return state;
	}

	@Override
	public String resetActivity(String stime,String etime,String initData,
		CreatObjectFactory factory)
	{
		String state=super.resetActivity(stime,etime,initData,factory);
		Object[] pids=players.toArray();
		CreatObjectFactory objectFactory=ActivityContainer.getInstance().getObjectFactory();
		for(int i=0;i<pids.length;i++)
		{
			Player player=objectFactory.getPlayerCache().load(pids[i]+"");
			if(player!=null)
			{
				IntKeyHashMap records=player.getConsumeGems();
				ConsumeGemsRecord record=(ConsumeGemsRecord)records.get(getId());
				if(record!=null)
				{
					record.setStartTime(getStartTime());
					record.setEndTime(getEndTime());
					record.setPercent(getPercent());
				}
			}
		}
		JBackKit.sendConsumeGemsState(factory.getDsmanager().getSessionMap());
		return state;
	}

	@Override
	public void sendFlush(SessionMap sm)
	{
		JBackKit.sendConsumeGemsState(sm);
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
	public void initData(ByteBuffer data,CreatObjectFactory factory,boolean active)
	{
		lastSaveTime=TimeKit.getSecondTime();
		super.initData(data,factory,active);
		int len=data.readUnsignedShort();
		for(int i=0;i<len;i++)
		{
			players.add(data.readInt());
		}
	}

	@Override
	public ByteBuffer getInitData()
	{
		ByteBuffer data=super.getInitData();
		Object[] pids=players.toArray();
		data.writeShort(pids.length);
		for(int i=0;i<pids.length;i++)
		{
			data.writeInt((Integer)pids[i]);
		}
		return data;
	}

	public void sendMessage(Player player,ConsumeGemsRecord record,int returnGems)
	{
		CreatObjectFactory objectFactory=ActivityContainer.getInstance()
			.getObjectFactory();
		String content=InterTransltor.getInstance().getTransByKey(
			player.getLocale(),"consume_gems_auto_award");
		content=TextKit.replace(content,"[PNAME]",player.getName());
		content=TextKit.replace(content,"[START]",SeaBackKit.formatDataTime(record.getStartTime()));
		content=TextKit.replace(content,"[END]",SeaBackKit.formatDataTime(record.getEndTime()));
		content=TextKit.replace(content,"[COUNT]",record.getCount()+"");
		content=TextKit.replace(content,"[PERCENT]",record.getPercent()+"");
		content=TextKit.replace(content,"[RETURN]",returnGems+"");
		// �����ʼ�
		MessageKit.sendSystemMessages(player,objectFactory,content);
	}
	
	@Override
	public Object copy(Object obj)
	{
		ConsumeGemsActivity act=(ConsumeGemsActivity)obj;
		act.players=new HashSet<Integer>();
		return act;
	}

	/**�õ���������**/
	public Set<Integer> getPlayers()
	{
		return players;
	}
	
	
}
