package foxu.sea.recruit;


/**
 * <=Ŀ��ֵ
 * @author yw
 *
 */
public class RecruitDayRankTask extends RecruitDayTask
{
	@Override
	public boolean checkTask(int value)
	{
		if(value<=0)return false;
		return this.value>=value;
	}
	
}
