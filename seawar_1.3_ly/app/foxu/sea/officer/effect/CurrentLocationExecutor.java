package foxu.sea.officer.effect;

import mustang.set.IntList;
import mustang.util.Sample;

/**
 * ��ǰλ�ü���Ч��
 * 
 * @author Alan
 * 
 */
public class CurrentLocationExecutor extends Sample implements
	LocationsExecutable
{

	@Override
	public void effectLocations(int location,IntList effectLocations)
	{
		if(effectLocations.contain(location)) return;
		effectLocations.add(location);
	}

}
