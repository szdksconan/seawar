package foxu.sea.weather;

import foxu.sea.Player;
import mustang.math.MathKit;
import mustang.set.IntKeyHashMap;


/**
 * ��������������
 * @author Alan
 *
 */
public class WorldWeatherManager
{
	public static final int SUNNY=0,RAINY=1,SNOWY=2;
	public static final int MAX_PROB=10000;
	public static int[] WEATHER={RAINY,3000,SUNNY,10000};
	IntKeyHashMap areaWeather=new IntKeyHashMap();
	static WorldWeatherManager manager=new WorldWeatherManager();
	public static WorldWeatherManager getInstance()
	{
		return manager;
	}
	
	/** ��ȡ�������������� */
	public int getPlayerWeather(Player player)
	{
		return getAreaWeather(getPlayerArea(player));
	}
	
	/** ��ȡ����������� */
	public int getPlayerArea(Player player)
	{
		return 0;
	}
	
	/** ��ȡ����������� */
	public int getAreaWeather(int area)
	{
		return randomWeather();
	}
	
	public int randomWeather()
	{
		int rd=MathKit.randomValue(0,MAX_PROB);
		int start=0;
		for(int i=0;i<WEATHER.length;i+=2)
		{
			if(rd>=start&&WEATHER[i+1]>rd)
				return WEATHER[i];
			start=WEATHER[i+1];
		}
		return SUNNY;
	}
}
