package foxu.sea;


/**
 * 禁用充值货币异常
 * @author yw
 *
 */
public class ValidCurrencyExpection extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID=-139913939393919L;
	String valid_cur;
	
	public ValidCurrencyExpection(String valid_cur)
	{
		this.valid_cur=valid_cur;
	}
	@Override
	public String toString()
	{
		return "valid_currency:"+valid_cur;
	}
}
