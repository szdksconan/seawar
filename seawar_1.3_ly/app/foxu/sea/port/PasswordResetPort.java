package foxu.sea.port;

import foxu.sea.PasswordManager;
import foxu.sea.User;
import foxu.sea.bind.TelBindingManager;
import mustang.io.ByteBuffer;
import mustang.net.AccessPort;
import mustang.net.Connect;
import mustang.net.DataAccessException;

/**
 * 重置密码端口 1025
 * 
 * @author Alan
 * 
 */
public class PasswordResetPort extends AccessPort
{

	public static final int RESET_PASSWORD_MAIL=1,RESET_PASSWORD_DONE=2,
					RESET_PASSWORD_TEL=3,CERTIFY_PASSWORD_TEL=4;

	PasswordManager pwdManager;
	TelBindingManager telBindingManager;

	public PasswordManager getPwdManager()
	{
		return pwdManager;
	}

	public void setPwdManager(PasswordManager pwdManager)
	{
		this.pwdManager=pwdManager;
	}

	@Override
	public ByteBuffer access(Connect connect,ByteBuffer data)
	{
		int type=data.readUnsignedByte();
		// 发送重置密码验证码邮件
		if(type==RESET_PASSWORD_MAIL)
		{
			String email=data.readUTF();
			String deviceId=data.readUTF();
			data.clear();
			User user=pwdManager.getUserByEmail(email);
			int result=1;
			if(user!=null)
			{
				result=pwdManager.addResetRecord(user,deviceId);
			}
			data.writeByte(result);
		}
		else if(type==RESET_PASSWORD_DONE)
		{
			String email=data.readUTF();
			String pwd=data.readUTF();
			String codeStr=data.readUTF();
			int codes=0;
			try
			{
				codes=Integer.valueOf(codeStr);
			}
			catch(NumberFormatException e)
			{
				codes=-1;
				// e.printStackTrace();
				// return null;
			}

			User user=pwdManager.getUserByEmail(email);
			if(user!=null)
			{
				// 修改本地和运营中心的
				int inputCount=pwdManager.resetPassword(user,pwd,codes);
				data.clear();
				// data.writeByte(RESET_PASSWORD_DONE);
				// data.writeUTF(user.getUserAccount());
				if(inputCount<-1)
					throw new DataAccessException(0,"password reset user error");
				data.writeByte(inputCount);
			}
		}
		else if(type==CERTIFY_PASSWORD_TEL)
		{
			String name=data.readUTF();
			String telphone=data.readUTF();
			String zone=data.readUTF();
			data.clear();
			User user=telBindingManager.getUsersByTel(zone,telphone,name);
			int result=-1;
			if(user==null)
				result=1;
			data.writeByte(result);
		}
		else if(type==RESET_PASSWORD_TEL)
		{
			String accout=data.readUTF();
			String telphone=data.readUTF();
			String zone=data.readUTF();
			String code=data.readUTF();
			String pwd=data.readUTF();
			data.clear();
			User user=telBindingManager.getUsersByTel(zone,telphone,accout);
			if(user==null)
				throw new DataAccessException(0,"user not exist");
			String msg=telBindingManager.certifyPlayerTel(zone,telphone,code,user);
			if(msg!=null)
				throw new DataAccessException(0,msg);
			int result=pwdManager.resetCustomPassword(user,pwd);
			if(result<-1)
				throw new DataAccessException(0,"password reset user error");
			data.writeByte(result);
		}
		return data;
	}

	
	public TelBindingManager getTelBindingManager()
	{
		return telBindingManager;
	}

	
	public void setTelBindingManager(TelBindingManager telBindingManager)
	{
		this.telBindingManager=telBindingManager;
	}

}
