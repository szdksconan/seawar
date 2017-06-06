package foxu.sea.officer.effect;

import foxu.sea.kit.SeaBackKit;
import foxu.sea.officer.Officer;

/**
 * 包含即激活效果
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
					// 激活军官重复时,重复的军官也可激活效果
					// 如果存在特定的效果受用军官，进行检测
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
