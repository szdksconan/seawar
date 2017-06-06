package foxu.sea.officer.effect;

import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.Officer;

/**
 * ����������Ч��
 * 
 * @author Alan
 */
public abstract class ContainActiveEffect extends UnitedEffect
{

	@Override
	public boolean isEffectAvailable(Officer[] usingOfficers)
	{
		if(officers==null||usingOfficers==null) return false;
		for(int i=0;i<officers.length;i++)
		{
			boolean isContained=false;
			for(int j=0;j<usingOfficers.length;j++)
			{
				if(usingOfficers[j]==null) continue;
				if(usingOfficers[j].getSid()==officers[i])
				{
					isContained=true;
					// ��������ظ�ʱ,�ظ��ľ���Ҳ�ɼ���Ч��
					// ��������ض���Ч�����þ��٣����м��
					if(locationSelector!=null
						&&(activeOfficers==null||SeaBackKit.isContainValue(
							activeOfficers,usingOfficers[j].getSid())))
						locationSelector.effectLocations(j,activyLocation);
				}
			}
			if(!isContained)
			{
				activyLocation.clear();
				return false;
			}
		}
		return true;
	}

}
