package foxu.sea.equipment;

import mustang.util.TimeKit;


/**
 * װ��uid������
 * @author Alan
 *
 */
public class UidGenerator
{
	private static UidGenerator ug;
	static {
		ug=new UidGenerator();
	}
	private volatile int baseUid;
	
	private UidGenerator(){
		baseUid=TimeKit.getSecondTime();
	}
	
	public static UidGenerator getInstance(){
		return ug;
	}
	
	public synchronized int getUid(){
		return baseUid++;
	}
}
