package foxu.cc;

/**
 * ��֤������������������ֲ�ͬ��֤��ʽ
 * 
 * @author comeback
 */
public interface CertifyHandler
{

	/**
	 * ��֤����
	 * 
	 * @param id ��֤��Ϣ�����ݾ������֤������ȷ������
	 * @param passwd ������Ϣ��ʵ�ʿɸ��ݾ������֤������ȷ������
	 * @param address ��ַ��Ϣ��Ϊ��¼IP��ַ
	 * @return ����һ��<code>CertifyUser</code>���󣬰�����Ҫ����֤��Ϣ
	 */
	public CertifyUser certify(String id,String passwd,String address);
}
