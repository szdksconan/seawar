package foxu.cc;

/**
 * 认证处理器，用来处理多种不同认证方式
 * 
 * @author comeback
 */
public interface CertifyHandler
{

	/**
	 * 认证方法
	 * 
	 * @param id 认证信息，根据具体的认证流程来确定内容
	 * @param passwd 密码信息，实际可根据具体的认证流程来确定内容
	 * @param address 地址信息，为登录IP地址
	 * @return 返回一个<code>CertifyUser</code>对象，包含必要的认证信息
	 */
	public CertifyUser certify(String id,String passwd,String address);
}
