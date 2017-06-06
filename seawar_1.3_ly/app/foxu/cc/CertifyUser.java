package foxu.cc;

/**
 * 认证用户对象,封装账号等必须的登录信息，如果修改认证协议，需要更多的参数，才能修改<code>CertifyUser</code>的定义
 * 
 * @author comeback
 */
public class CertifyUser
{

	String account;

	/**
	 * @return the account
	 */
	public String getAccount()
	{
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(String account)
	{
		this.account=account;
	}

}
