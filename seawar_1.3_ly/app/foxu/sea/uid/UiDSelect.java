package foxu.sea.uid;

/** ��СUid������ ���ݲ�ͬ����������� */
public class UiDSelect
{

	public int selectUid(int min,String areaId)
	{
		int serverId = Integer.parseInt(areaId);
		if(serverId<=0||serverId>Byte.MAX_VALUE)
			throw new IllegalArgumentException(super.getClass().getName()
				+" initFile,minUid error");
		int minUid=(serverId<<24)+min;
		return minUid;
	}
	
//	public static void main(String[] args)
//	{
//		int serverId=1;
//		int minUid=(serverId<<24);
//		System.out.println("minUid===="+minUid);
//	}
}
