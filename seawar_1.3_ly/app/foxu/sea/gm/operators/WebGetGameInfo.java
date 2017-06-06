package foxu.sea.gm.operators;

import javapns.json.JSONArray;
import javapns.json.JSONException;
import javapns.json.JSONObject;
import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.CenterHttpToServer;
import foxu.sea.InterTransltor;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.User;
import foxu.sea.gm.GMConstant;
import foxu.sea.gm.WebConstant;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.messgae.Message;
import foxu.sea.port.UserToCenterPort;
import foxu.sea.proplist.AwardProp;
import foxu.sea.proplist.Prop;

/****
 *  web��������ȡ��Ϸ����
 * @author lhj
 *
 */
public class WebGetGameInfo implements HttpHandlerInterface 
{

	public static Logger log=LogFactory.getLogger(CenterHttpToServer.class);
	/** Ĭ�ϵ�Base64������㷨 */
	public static final Base64 BASE64=new Base64();
	
	public static final int WEB_NEED_LEVE=20;
	/**GET_PLAYER_INFO=1��ȡ�������  SET_AWARD=����ҷ��� SEND_AWARD_MESSAGE=5 �����ʼ�**/
	public  final int  GET_PLAYER_INFO=1,SET_AWARD=3,SEND_AWARD_MESSAGE=5;
	
	/** ʹ�õ�Base64������㷨 */
	Base64 base64=BASE64;
	
	/** ������������� */
	CreatObjectFactory objectFactory;
	
	int[] no_open={2010,2011,2012,2013};
	
	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
		return null;
	}

	@Override
	public String excuteString(HttpRequestMessage request,String arg1)
	{
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int type=data.readUnsignedByte();
		JSONObject json=new JSONObject();
		//System.out.println("--------WebGetGameInfo-----type----:"+type);
		//��ȡ�������
		if(type==GET_PLAYER_INFO)
		{
			String account=data.readUTF();
			Player [] players=getPlayersByAccount(account);
			//System.out.println("--------players---------:"+players);
			try
			{
				JSONArray  jso=new JSONArray();
				if(players==null || players.length==0 || isablePlayerList(players))
				{
					//��ɫ����Ϊ��
					json.put(WebConstant.SUCCESS,WebConstant.ROLENAME_NULL);
					json.put(WebConstant.LENGTH,0);
					return json.toString();
				}
				for(int i=0;i<players.length;i++)
				{
					Player player=players[i];
					if(player.getLevel()<WEB_NEED_LEVE)
						continue;
					//��ȡ�������
					JSONObject baseInfo=getPlayerBaseInfo(player);
					jso.put(baseInfo);
				}
				//��װ��json 
				json.put(WebConstant.PIONEER,jso);
				json.put(WebConstant.SUCCESS,GMConstant.ERR_SUCCESS);
				json.put(WebConstant.LENGTH,players.length);
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
		/**web ����**/
		else if(type==SET_AWARD)
		{
			//�˺�
			String account=data.readUTF();
			//���
			int player_id=data.readInt();
			//����id
			int award_id=data.readInt();
			try
			{
				// System.out.println(player_id+"=============player_id");
				Player player=objectFactory.getPlayerById(player_id);
				if(player==null)
				{
					json.put(WebConstant.SUCCESS,WebConstant.PLAYER_NAME_IS_NULL);
					return json.toString();
				}
				AwardProp prop=(AwardProp)Prop.factory.newSample(award_id);
				data.clear();
				if(SeaBackKit.isContainValue(no_open,prop.getSid()))
				{
					player.getBundle().incrProp(prop,true);
				}else
				{
					prop.use(player,objectFactory,data);
				}
				JBackKit.sendResetBunld(player);
				json.put(WebConstant.SUCCESS,GMConstant.ERR_SUCCESS);
				return json.toString();
			}
			catch(Exception e)
			{
				
			}
			
		}
		//�����ʼ�
		else if(type==SEND_AWARD_MESSAGE)
		{
			try
			{
				// ����
				String title=data.readUTF();
				// ����
				String content=data.readUTF();
				// ���id
				int player_id=data.readInt();
				Player player=objectFactory.getPlayerById(player_id);
				if(player==null)
				{
					json.put(WebConstant.SUCCESS,WebConstant.PLAYER_NAME_IS_NULL);
					return json.toString();
				}
				json.put(WebConstant.SUCCESS,sendMail(player,title,content));
			}
			catch(Exception e)
			{

			}
		}
			
		//���ͽ���
//		else  if(type==SET_AWARD)
//		{
//			String player_name=data.readUTF();
//			String title=data.readUTF();
//			String content=InterTransltor.getInstance().getTransByKey(
//								PublicConst.SERVER_LOCALE,"activity_content");
//			Player player=objectFactory.getPlayerByName(player_name,false);
//			if(player==null)
//			{
//				// ֱ�ӷ��ش���
//				json.put(SUCCESS,GMConstant.ERR_PLAYER_NAME_NULL);
//				return json.toString();
//			}
//			// Ҫ���ͽ������ĳ���
//			int length=data.readInt();
//			StringBuffer errPro=new StringBuffer();
//			StringBuffer econtet=new StringBuffer();
// 			for(int i=0;i<length;i++)
//			{
//				int sid=data.readInt();
//				int count=data.readInt();
//				Prop prop=(Prop)Prop.factory.newSample(sid);
//				if(prop==null) errPro.append(sid+","+count);
//				econtet.append(prop.getName()+"X"+count);
//				if(prop instanceof NormalProp)
//					((NormalProp)prop).setCount(count);
//				//�п�����ҵİ�������  ���� ��ôҲ�ѵ�ǰ��sid ��¼����	
//				if(!player.getBundle().incrProp(prop,true))
//				{
//					errPro.append(sid+","+count);
//				}
//				content=TextKit.replace(content,"%",econtet.toString());
//			}
//			sendMail(player,title,content);
//			//���سɹ�
//			bb.writeInt(GMConstant.ERR_SUCCESS);
//			//�������δ�յ��Ľ���Ʒ
//			bb.writeUTF(errPro.toString());
//		}	
		return json.toString();
	
	}
	
	/**
	 * ����key�������� ��һkey
	 * 
	 * @param key id
	 * @return ���ص�ByteBuffer
	 */
	public ByteBuffer load(String data)
	{
		if(data==null||data.equals("null")) return null;
		ByteBuffer bb=new ByteBuffer();
		bb.clear();
		base64.decode(data,0,data.length(),bb);
		return bb;
	}

	/**
	 * Base64������㷨 ����������ת��Ϊ�ַ���
	 */
	public String createBase64(ByteBuffer data)
	{
		byte[] array=data.toArray();
		data.clear();
		base64.encode(array,0,array.length,data);
		return new String(data.getArray(),0,data.top());
	}

	/** ������������� */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}

	/**
	 * ͨ���˺Ų�ѯ���
	 * @param account
	 * @param objectFactory
	 * @return
	 */
	public Player[] getPlayersByAccount(String account)
	{
		if(account==null||account.length()==0) return null;
		// String
		// sql="SELECT * FROM players WHERE players.user_id=(SELECT id FROM users WHERE users.user_account='"+account+"')";
		// Player[]
		// objs=(Player[])objectFactory.getPlayerCache().getDbaccess().loadBySql(sql);
		User user=objectFactory.getUserDBAccess().loadUser(account);
		if(user==null) return null;
		int[] ps=user.getPlayerIds();
		Player[] objs=null;
		if(ps!=null&&ps.length>0)
		{
			objs=new Player[ps.length];
			int len=0;
			for(int i=0;i<ps.length;i++)
			{
				objs[i]=objectFactory.getPlayerById(ps[i]);
				if(objs[i]!=null) len++;
			}
			if(len<=0) return null;
			if(len<ps.length)
			{
				Player[] nps=new Player[len];
				for(int i=0,j=0;i<objs.length&&j<len;i++)
				{
					if(objs[i]==null) continue;
					nps[j]=objs[i];
					j++;
				}
				objs=nps;
			}
		}
		else
		{
			objs=new Player[1];
			objs[0]=objectFactory.getPlayerById(user.getPlayerId());
			if(objs[0]==null) return null;
		}
		return objs;
	}
	
	
	/**
	 * ��װ��һ�����Ϣ
	 * @param player
	 * @return
	 */
	private JSONObject getPlayerBaseInfo(Player player)
	{
		JSONObject jo=new JSONObject();
		try
		{
			// �������
			jo.put(GMConstant.NAME,player.getName());
			// ���ID
			jo.put(WebConstant.PLAYER_ID,player.getId());
			// ��ҵȼ�
			jo.put(GMConstant.LEVEL,player.getLevel());
			// ս��
			jo.put(GMConstant.POWER,player.getFightScore());
			//������id
			jo.put(WebConstant.SERVER_ID,UserToCenterPort.SERVER_ID);
			//����id
			jo.put(WebConstant.AREA_ID,UserToCenterPort.AREA_ID);
			//��ɫ�Ĵ���ʱ��
			jo.put(WebConstant.CREATETIME,player.getCreateTime());
			//��ɫ�ĵ�¼ʱ��
			jo.put(WebConstant.LOGINTIME,player.getUpdateTime());
			//����id
			jo.put(WebConstant.PLAT,UserToCenterPort.PLAT_ID);
			
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return jo;
	}
	
	/**��֤��ǰ�Ƿ�����web������������**/
	public boolean isablePlayerList(Player[] players)
	{
		for(int i=0;i<players.length;i++)
		{
			Player player=players[i];
			if(player==null) 
				continue;
			if(player.getLevel()>=WEB_NEED_LEVE)
			{
				return false;
			}
		}
		return true;
	}
	
	/**����ҷ��ʼ�(�������ȡ�Ľ���)**/
	private int sendMail(Player player,String title,String content)
	{
		String sendName=InterTransltor.getInstance().getTransByKey(
			PublicConst.SERVER_LOCALE,"system_mail");
		
		Message message=objectFactory.createMessage(0,player.getId(),
			content,sendName,player.getName(),0,title,true);
		// ˢ��ǰ̨
		JBackKit.sendRevicePlayerMessage(player,message,message
			.getRecive_state(),objectFactory);
		return GMConstant.ERR_SUCCESS;
	}

	
	public int[] getNo_open()
	{
		return no_open;
	}

	
	public void setNo_open(int[] no_open)
	{
		this.no_open=no_open;
	}
	
	
}
