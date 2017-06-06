package jedis;

import java.util.Iterator;
import java.util.Set;

import mustang.io.ByteBuffer;
import redis.clients.jedis.Pipeline;
import shelby.dc.MemCacheAccess;
import foxu.sea.NpcIsland;
import foxu.sea.Player;
import foxu.sea.Role;
import foxu.sea.alliance.Alliance;
import foxu.sea.event.FightEvent;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;

/**
 * jedis
 * 
 * @author icetiger
 */
public class JedisMemCacheAccess implements MemCacheAccess
{

	/** key���� */
	public static final String PLAYER_REDIS="playerData_",
					MESSAGE_REDIS="messageData_",
					FIGHT_EVENT_REDIS="fightEvent_",ORDER_REDIS="order_",
					ISLAND_REDIS="island_",ALLIANCE_REDIS="alliance_",
					ARENA_REDIS="arena_",WORLD_BOSS="world_boss_",
					ALLIANCE_FIGHT="alliance_fight_",ALLIANCE_FIGHT_EVENT="alliance_fight_event_",
					BATTLE_GROUND="battle_ground_",AOUNNCEMENT_INFO="aouncement_info",BATTLE_ISLAND="battle_island_";

	/** redis�ͻ��� */
	SeawarJedis jedis;
	
	public void clear()
	{
		jedis.flushAll();
	}
	
	/**�����Ƿ�����*/
	public boolean isConnected()
	{
		return jedis.isConnected();
	}
	
	public Pipeline pipelined()
	{
		return jedis.pipelined();
	}

	/** ɾ��ĳ��keyֵ */
	public void delKey(String key)
	{
		jedis.delKey(key);
	}


	/** ���ض�� jedisԼ��ֻ�õ�һ��string��ƥ����� */
	public ByteBuffer[] loads(String[] keys)
	{
		Set<String> jedis_set=jedis.keys(keys[0]);
		if(jedis_set.size()<=0) return null;
		ByteBuffer[] buffer=new ByteBuffer[jedis_set.size()];
		int i=0;
		Iterator it=jedis_set.iterator();
		while(it.hasNext())
		{
			String jedis_data=jedis.get(it.next().toString()+"");
			buffer[i]=SeaBackKit.load(jedis_data);
			i++;
		}
		return buffer;
	}

	/** ����redis������������ */
	public Player[] loadPlayers()
	{
		String key[]={PLAYER_REDIS+"*"};
		ByteBuffer data[]=loads(key);
		if(data==null)
			return null;
		else
		{
			Player player[]=new Player[data.length];
			for(int i=0,n=data.length;i<n;i++)
			{
				player[i]=(Player)Role.bytesReadRole(data[i]);
			}
			return player;
		}
	}
	
	/**����redis��������е���*/
	public NpcIsland[] loadNpcIsLand()
	{
		String key[]={ISLAND_REDIS+"*"};
		ByteBuffer data[]=loads(key);
		if(data==null)
			return null;
		else
		{
			NpcIsland island[]=new NpcIsland[data.length];
			for(int i=0,n=data.length;i<n;i++)
			{
				island[i]=(NpcIsland)NpcIsland.bytesReadNpcIsland(data[i]);
			}
			return island;
		}
	}
	
	
	/** ����redis���������ʼ� */
	public Message[] loadAllMessages()
	{
		String key[]={MESSAGE_REDIS+"*"};
		ByteBuffer data[]=loads(key);
		if(data==null)
			return null;
		else
		{
			Message message[]=new Message[data.length];
			for(int i=0,n=data.length;i<n;i++)
			{
				message[i]=new Message();
				message[i].bytesRead(data[i]);
			}
			return message;
		}
	}
	/** ����redis��������ս���¼� */
	public FightEvent[] loadAllFightEvents()
	{
		String key[]={FIGHT_EVENT_REDIS+"*"};
		ByteBuffer data[]=loads(key);
		if(data==null)
			return null;
		else
		{
			FightEvent fightEvent[]=new FightEvent[data.length];
			for(int i=0,n=data.length;i<n;i++)
			{
				fightEvent[i]=new FightEvent();
				fightEvent[i].bytesRead(data[i]);
			}
			return fightEvent;
		}
	}
	
	/**����redis������������*/
	public Alliance[] loadAllAlliance()
	{
		String key[]={ALLIANCE_REDIS+"*"};
		ByteBuffer data[]=loads(key);
		if(data==null)
			return null;
		else
		{
			Alliance alliance[]=new Alliance[data.length];
			for(int i=0,n=data.length;i<n;i++)
			{
				alliance[i]=new Alliance();
				alliance[i].bytesRead(data[i]);
			}
			return alliance;
		}
	}

	/**
	 * ����key��������
	 * 
	 * @param key id
	 */
	public void save(String key,Object data)
	{
		data=((ByteBuffer)data).clone();
		String jedis_data=SeaBackKit.createBase64((ByteBuffer)data);
		jedis.set(key,jedis_data);
	}
	
	/**��ȡ����������*/
	public String getBase64After(ByteBuffer data)
	{
		data=(ByteBuffer)data.clone();
		String jedis_data=SeaBackKit.createBase64(data);
		return jedis_data;
	}

	/**
	 * @return jedis
	 */
	public SeawarJedis getJedis()
	{
		return jedis;
	}

	/**
	 * @param jedis Ҫ���õ� jedis
	 */
	public void setJedis(SeawarJedis jedis)
	{
		this.jedis=jedis;
	}

	public Object load(String key)
	{
		// TODO �Զ����ɷ������
		return null;
	}

	public int saveAndExit()
	{
		// TODO �Զ����ɷ������
		return 0;
	}
}
