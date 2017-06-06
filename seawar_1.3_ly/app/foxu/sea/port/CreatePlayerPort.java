/**
 * 
 */
package foxu.sea.port;

import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;
import mustang.text.TextValidity;
import shelby.cc.SidSessionMap;
import foxu.dcaccess.CreatObjectFactory;
import foxu.dcaccess.mem.NpcIsLandMemCache;
import foxu.sea.Player;
import foxu.sea.PublicConst;
import foxu.sea.Role;
import foxu.sea.User;
import foxu.sea.kit.JBackKit;
import foxu.sea.kit.SeaBackKit;
import foxu.sea.recruit.RecruitKit;

/**
 * 创建角色端口
 * 
 * @author rockzyt
 */
public class CreatePlayerPort extends AccessPort
{
	public static final int MIN_LEN=1,MAX_LEN=12;

	/* fields */
	CreatObjectFactory factory;
	NpcIsLandMemCache memCache;
	TextValidity tv;

	/** Sid会话表 */
	SidSessionMap ssm;

	/* methods */
	public ByteBuffer access(Connect c,ByteBuffer data)
	{
		String userAccount=data.readUTF();
		String sid=data.readUTF();
		String name=data.readUTF();
		int roleSid = data.readUnsignedShort();
		Player player = (Player)Role.factory.getSample(roleSid);
		if(player==null)
		{
			throw new DataAccessException(0,"roleSid is wrong");
		}
		synchronized(memCache)
		{
			if(name.getBytes().length==name.length())
			{
				if(name.length()<MIN_LEN||name.length()>MAX_LEN)
					throw new DataAccessException(0,"name length wrong");
			}
			else
			{
				if(name.length()<(MIN_LEN/2)||name.length()>(MAX_LEN/2))
					throw new DataAccessException(0,"name length wrong");
			}
			if(tv.valid(name,true)!=null || name.indexOf(",")>=0)
				throw new DataAccessException(0,"name is not valid");
			
			if(SeaBackKit.checkBlank(name))
				throw new DataAccessException(0,"name is not valid");
			User old_user=factory.getUserDBAccess().loadUser(userAccount);
			Player p=factory.getPlayerByName(name,false);
			if(p!=null)
				throw new DataAccessException(0,"name has been used");
			p=factory.getPlayerById(old_user.getPlayerId());
			if(p==null)throw new DataAccessException(0,"player not exist");
			if(p.getAttributes(PublicConst.CREAT_NAME)==null)
				throw new DataAccessException(0,"had created player");
			String oldname=p.getName();
			p.setName(name);
			boolean flag=factory.getPlayerCache().getDbaccess()
				.save(p);
			if(!flag)
			{
				p.setName(oldname);
				throw new DataAccessException(0,"name is not valid");
			}
			p.setRoleSid(roleSid);
			p.setCreateIp(c.getURL().getHost());
			p.setAttribute(PublicConst.CREAT_NAME,null);
			p.setAttribute(PublicConst.HEAD_SID,(roleSid==1?PublicConst.HEADSID_BOY:PublicConst.HEADSID_GIRL)+"");
			p.setCurrentHeadSid(roleSid==1?PublicConst.HEADSID_BOY:PublicConst.HEADSID_GIRL);
			p.setNextExtraAward();
			//新兵福利
			RecruitKit.setWelfareOut(p);
			JBackKit.sendRecruitAll(RecruitKit.rmanager,p);
			data.clear();
			ByteBuffer bb=new ByteBuffer();
			bb.writeInt(p.getId());
			bb.writeUTF(userAccount);
			ssm.getBySid(sid).setReference(bb);
			data.clear();
			p.showByteWriteExtraGift(data);
		}
		return data;
	}

	/**
	 * @return ssm
	 */
	public SidSessionMap getSsm()
	{
		return ssm;
	}

	/**
	 * @param ssm 要设置的 ssm
	 */
	public void setSsm(SidSessionMap ssm)
	{
		this.ssm=ssm;
	}

	
	/**
	 * @return tv
	 */
	public TextValidity getTv()
	{
		return tv;
	}

	
	/**
	 * @param tv 要设置的 tv
	 */
	public void setTv(TextValidity tv)
	{
		this.tv=tv;
	}
}