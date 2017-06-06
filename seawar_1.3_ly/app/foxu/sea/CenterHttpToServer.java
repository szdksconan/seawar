package foxu.sea;

import mustang.codec.Base64;
import mustang.io.ByteBuffer;
import mustang.log.LogFactory;
import mustang.log.Logger;
import mustang.util.TimeKit;
import shelby.httpclient.HttpHandlerInterface;
import shelby.httpserver.HttpRequestMessage;
import foxu.dcaccess.CreatObjectFactory;
import foxu.sea.bind.BindingTrack;
import foxu.sea.kit.SeaBackKit;

/****
 * ���մ����ĵ����� (����Ⱥ��������������)
 * 
 * @author lihongji
 * 
 */
public class CenterHttpToServer implements HttpHandlerInterface
{

	public static Logger log=LogFactory.getLogger(CenterHttpToServer.class);
	/** Ĭ�ϵ�Base64������㷨 */
	public static final Base64 BASE64=new Base64();

	/**
	 * �����û���ǰ׺UPDATE_USER_INFO==1 USER_SET_TYPE=2 �������ɾ��״̬
	 * PLAYER_NOT_INSULATE=3 ���ȡ������
	 **/
	public static final int CHANGE_ACCOUNT=1,USER_SET_TYPE=2,
					CHANGE_PASSWORD=3,CHANGE_TEL=4;
	/** ʹ�õ�Base64������㷨 */
	Base64 base64=BASE64;

	/** ������������� */
	CreatObjectFactory objectFactory;

	@Override
	public byte[] excute(HttpRequestMessage request,String ip)
	{
		String stringData=request.getParameter("data");
		ByteBuffer data=load(stringData);
		int type=data.readUnsignedByte();
		ByteBuffer bb=new ByteBuffer();
		// �޸��û���ɾ��״̬
		if(type==USER_SET_TYPE)
		{
			String name=data.readUTF();
			User user=objectFactory.getUserDBAccess().loadUser(name);
			if(user==null)
				bb.writeBoolean(true);
			else
			{
				int time=data.readInt();
				objectFactory.getUserDBAccess().setUserState(user,time);
				deletePlayer(user,time);
				log.info("----name----+time--type----deleteTime---:"+name
					+"----"
					+SeaBackKit.formatDataTime(TimeKit.getSecondTime())
					+"-----"+type+"---deleteTime--"
					+(time==0?"0":SeaBackKit.formatDataTime(time)));
				bb.writeBoolean(true);
			}
		}
		else if(type==CHANGE_ACCOUNT)
		{
			String newAccount=data.readUTF();// �µĵ��˺�
			String old_account=data.readUTF();// �ɵĵ��˺�
			String password=data.readUTF();
			String sql="SELECT * FROM users WHERE user_account='"
				+old_account+"'";
			User user[]=objectFactory.getUserDBAccess().loadBySql(sql);
			if(user==null||user.length==0)
				bb.writeBoolean(true);
			else
			{
				for(int i=0;i<user.length;i++)
				{
					changAccount(user[i],newAccount,password);
				}
				bb.writeBoolean(true);
			}
		}
		else if(type==CHANGE_PASSWORD)
		{
			String account=data.readUTF();
			String password=data.readUTF();
			String sql="SELECT * FROM users WHERE user_account='"+account
				+"'";
			User user[]=objectFactory.getUserDBAccess().loadBySql(sql);
			if(user==null||user.length==0)
				bb.writeBoolean(true);
			else
			{
				for(int i=0;i<user.length;i++)
				{
					if(user[i]!=null)
					{
						user[i].setPassword(password);
						objectFactory.getUserDBAccess().save(user[i]);
					}
				}
				bb.writeBoolean(true);
			}
		}
		else if(type==CHANGE_TEL)
		{
			String account=data.readUTF();
			String tel=data.readUTF();
			int source_server=data.readInt();
			User user=objectFactory.getUserDBAccess().loadUser(account);
			if(user==null)
				bb.writeBoolean(true);
			else
			{
				String lastRecord=user.getBindingTel();
				if("".equals(tel))
					tel=null;
				user.setBindingTel(tel);
				objectFactory.getUserDBAccess().save(user);
				bb.writeBoolean(true);
				objectFactory.createBindingTrack(BindingTrack.TELPHONE,
					BindingTrack.UPDATE_RECORD,BindingTrack.CENTER,
					user.getId(),source_server,tel,lastRecord,
					user.getBindingTel());
			}
		}
		return createBase64(bb).getBytes();
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

	@Override
	public String excuteString(HttpRequestMessage request,String ip)
	{
		return null;
	}

	/** �ı���ҵ�״̬ **/
	public void setPlayerState(int id)
	{
		Player player=objectFactory.getPlayerById(id);
		if(player!=null)
		{
			player.setAttribute(PublicConst.ACCOUNT_TIME,
				TimeKit.getSecondTime()+"");
			player.setAttribute(PublicConst.ACCOUNT_INFO,"");
		}
	}

	/** �޸��˺� **/
	public void changAccount(User user,String newAccount,String password)
	{
		if(user!=null)
		{
			user.setUserAccount(newAccount);
			user.setPassword(password);
			objectFactory.getUserDBAccess().save(user);
			// ��ǰ������н�ɫ�Ľ���ʱ������Ϊ7��
			if(user.getPlayerIds().length!=0)
			{
				int[] array=user.getPlayerIds();
				for(int j=0;j<array.length;j++)
				{
					setPlayerState(array[j]);
				}
			}
			else
				setPlayerState(user.getPlayerId());
		}
	}

	/** ���һ���˺��ϵ�ÿһ����Ҽ���ɾ��״̬ **/
	public void deletePlayer(User user,int deleteTime)
	{
		if(user!=null)
		{
			if(user.getPlayerIds().length!=0)
			{
				int[] array=user.getPlayerIds();
				for(int j=0;j<array.length;j++)
				{
					deletePlayerState(array[j],deleteTime);
				}
			}
			else
				deletePlayerState(user.getPlayerId(),deleteTime);
		}
	}

	/** ���״̬ **/
	public void deletePlayerState(int id,int deleteTime)
	{
		Player player=objectFactory.getPlayerById(id);
		if(player!=null)
		{
			player.setDeleteTime(deleteTime);
			objectFactory.getPlayerCache().getDbaccess().save(player);
		}
	}
}
