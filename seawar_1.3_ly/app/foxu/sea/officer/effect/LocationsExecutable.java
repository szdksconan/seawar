package foxu.sea.officer.effect;

import mustang.set.IntList;


/**
 * ����Ч��Ӱ��λ��ִ����(����ֻ�����ض�λ�õľ���Ч����ʵ������)
 * @author Alan
 *
 */
public interface LocationsExecutable
{
	/** ��Ӿ���Ч��Ӱ��λ��
	 * @param location ����λ��
	 * @param effectLocations Ӱ��λ������
	 */
	public void effectLocations(int location,IntList effectLocations);
}
