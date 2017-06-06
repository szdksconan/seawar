package foxu.sea;

import mustang.text.TextKit;
import foxu.sea.kit.SeaBackKit;

/** �û��� User */
public class User
{
	public static final int GUEST=1,USER=2;
	public static final String USER_PREFIX_NAME="@delete@";//ɾ��ʱ����Ҽӵ�ǰ׺				

	/** �û�ID */
	int id;
	/** �˺����� */
	int userType;
	/** playerId ����id�մ�����ʱ����0 ��һ�ε�¼��*/
	int playerId;
	/** ����playerId */
	int lockPlayerId;
	
	/** �����豸udid */
	String createUdid;
	/** ��¼udid */
	String loginUdid;
	/** ��ֵudid */
	String payUdid;
	/** �˺�user_account */
	String userAccount;
	/** password */
	String password;
	/** ���� */
	String email;
	/** ����ʱ�� */
	int createAt;
	/** ��¼ʱ�� */
	int loginTime;
	/** ��0Ϊ����� */
	int banned;
	/** �����豸 */
	String bannedDevice;
	/**�豸�ͺ�*/
	String device;
	/** ϵͳ��Ϣ */
	String osInfo;
	/** ����ʾ�� */
	String idfa;
	/** �汾�� */
	String version;
	/** �󶨵绰 */
	String bindingTel;

	int[] playerIds;
	
	/** ƽ̨��Դ  */
	String plat;
	/**ɾ��״̬(��¼ɾ��ʱ��)**/
	int  deleteTime;
	/**
	 * @return createAt
	 */
	public int getCreateAt()
	{
		return createAt;
	}

	/**
	 * @param createAt Ҫ���õ� createAt
	 */
	public void setCreateAt(int createAt)
	{
		this.createAt=createAt;
	}

	/**
	 * @return email
	 */
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email Ҫ���õ� email
	 */
	public void setEmail(String email)
	{
		this.email=email;
	}

	/**
	 * @return id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @param id Ҫ���õ� id
	 */
	public void setId(int id)
	{
		this.id=id;
	}

	/**
	 * @return password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password Ҫ���õ� password
	 */
	public void setPassword(String password)
	{
		this.password=password;
	}

	/**
	 * @return playerId
	 */
	public int[] getPlayerIds()
	{
		return this.playerIds;
	}
	
	public int getPlayerId()
	{
		return playerId;
	}
	
	public void setPlayerId(int id)
	{
		this.playerId=id;
	}
	
	public void setPlayerIds(int[] ids)
	{
		this.playerIds=ids;
	}
	/**����һ��player*/
	public void  addPlayerIds(int playerId)
	{
		if(this.playerId==playerId) return;
		if(SeaBackKit.isContainValue(playerIds,playerId)) return;
		if(this.playerId==0)
		{
			this.playerId=playerId;
			return;
		}
		if(playerIds.length==0)
		{
			playerIds=new int[2];
			playerIds[0]=this.playerId;
			playerIds[1]=playerId;
		}
		else
		{
			int[] temp=new int[playerIds.length+1];
			System.arraycopy(playerIds,0,temp,0,playerIds.length);
			temp[playerIds.length]=playerId;
			this.playerIds=temp;
		}
	}
	
	/**����һ��player*/
	public void descPlayerIds(int playerId)
	{
		if(playerIds.length>=2)
		{
			int[] playerdi=new int[playerIds.length-1];
			int s=0;
			for(int i=0;i<playerIds.length;i++)
			{
				if(playerId!=playerIds[i])
				{
					playerdi[s]=playerIds[i];
					s++;
				}
			}
			playerIds=playerdi;
		}
		if(playerIds.length<=0)
		{
			this.playerId=0;
		}
		else if(playerIds.length==1)
		{
			this.playerId=playerIds[0];
			playerIds=new int[]{};
		}
		else
		{
			this.playerId=playerIds[0];
		}
	}
	public String validatePlayerId(String newplayersId)
	{
		String playersIds="";
		String[] playerid=newplayersId.split(",");
		for(int i=0;i<playerid.length;i++)
		{
			boolean flag=true;
			for(int j=0;j<playerIds.length;j++)
			{
				if(TextKit.parseInt(playerid[i])==playerIds[j])
					flag=false;
			}
			if(flag)
			{
				if(playersIds=="") playersIds=playerid[i];
				else playersIds+=","+playerid[i];
			}
		}
		
		return playersIds;
	}
	
	/**�߼�ɾ�����*/
	public void delete(Player player)
	{
		descPlayerIds(player.getId());
	}
	/**�ָ��������*/
	public void recover(Player player)
	{
		addPlayerIds(player.getId());
	}
	public String getPlayerIdsString()
	{
		if(this.playerIds==null||this.playerIds.length==0)
			return "";
		if(playerIds.length==1)
			return String.valueOf(playerIds[0]);
		String str="";
		for(int i=0;i<playerIds.length;i++)
		{
			if(i>0)
				str=str+":";
			str+=playerIds[i];
		}
		return str;
	}
	
	public void setPlayerIdsString(String str)
	{
		if(str==null||str.length()==0)
		{
			this.playerIds=new int[]{};
			return;
		}
		String[] strs=TextKit.split(str,":");
		int[] ids=new int[strs.length];
		for(int i=0;i<ids.length;i++)
			ids[i]=TextKit.parseInt(strs[i]);
		if(ids.length==1&&ids[0]==0)
		{
			playerIds=new int[]{};
			return;
		}
		this.playerIds=ids;
	}

	/**
	 * @return udid
	 */
	public String getCreateUdid()
	{
		return createUdid;
	}

	/**
	 * @param udid Ҫ���õ� udid
	 */
	public void setCreateUdid(String udid)
	{
		this.createUdid=udid;
	}

	/**
	 * @return userAccount
	 */
	public String getUserAccount()
	{
		return userAccount;
	}

	/**
	 * @param userAccount Ҫ���õ� userAccount
	 */
	public void setUserAccount(String userAccount)
	{
		this.userAccount=userAccount;
	}

	/**
	 * @return loginTime
	 */
	public int getLoginTime()
	{
		return loginTime;
	}

	/**
	 * @param loginTime Ҫ���õ� loginTime
	 */
	public void setLoginTime(int loginTime)
	{
		this.loginTime=loginTime;
	}

	/**
	 * @return loginUdid
	 */
	public String getLoginUdid()
	{
		return loginUdid;
	}

	/**
	 * @param loginUdid Ҫ���õ� loginUdid
	 */
	public void setLoginUdid(String loginUdid)
	{
		this.loginUdid=loginUdid;
	}

	/**
	 * @return userType
	 */
	public int getUserType()
	{
		return userType;
	}

	/**
	 * @param userType Ҫ���õ� userType
	 */
	public void setUserType(int userType)
	{
		this.userType=userType;
	}

	
	public int getBanned()
	{
		return banned;
	}

	
	public void setBanned(int banned)
	{
		this.banned=banned;
	}

	public String getBannedDevice()
	{
		return bannedDevice!=null?bannedDevice:"";
	}

	
	public void setBannedDevice(String bannedDevice)
	{
		this.bannedDevice=bannedDevice;
	}
	
	public String getDevice()
	{
		return device;
	}

	
	public void setDevice(String device)
	{
		this.device=device;
	}
	
	public String getOsInfo()
	{
		return osInfo;
	}

	
	public void setOsInfo(String osInfo)
	{
		this.osInfo=osInfo;
	}
	
	public String getIdfa()
	{
		return idfa;
	}

	
	public void setIdfa(String idfa)
	{
		this.idfa=idfa;
	}

	
	public String getVersion()
	{
		return version;
	}

	
	public void setVersion(String version)
	{
		this.version=version;
	}

	
	public String getPlat()
	{
		return plat;
	}

	
	public void setPlat(String plat)
	{
		this.plat=plat;
	}

	/**ɾ��״̬����¼ʱ�䣩**/
	public int getDeleteTime()
	{
		return deleteTime;
	}

	
	public void setDeleteTime(int deleteTime)
	{
		this.deleteTime=deleteTime;
	}

	
	public String getPayUdid()
	{
		return payUdid;
	}

	
	public void setPayUdid(String payUdid)
	{
		this.payUdid=payUdid;
	}

	
	public String getBindingTel()
	{
		if("".equals(bindingTel))
			bindingTel=null;
		return bindingTel;
	}

	
	public void setBindingTel(String bindingTel)
	{
		this.bindingTel=bindingTel;
	}

}
