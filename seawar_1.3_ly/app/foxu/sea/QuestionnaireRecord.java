package foxu.sea;

/**
 * 调查问卷记录
 */
public class QuestionnaireRecord
{

	private int id;
	private int playerId;
	private int actId;
	private int topicIndex;
	private int topicType;
	private String answer;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id=id;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId=playerId;
	}

	public int getActId()
	{
		return actId;
	}

	public void setActId(int actId)
	{
		this.actId=actId;
	}

	public int getTopicIndex()
	{
		return topicIndex;
	}

	public void setTopicIndex(int topicIndex)
	{
		this.topicIndex=topicIndex;
	}

	public int getTopicType()
	{
		return topicType;
	}

	public void setTopicType(int topicType)
	{
		this.topicType=topicType;
	}

	public String getAnswer()
	{
		return answer;
	}

	public void setAnswer(String answer)
	{
		this.answer=answer;
	}

	@Override
	public String toString()
	{
		return "QuestionnaireRecord [id="+id+", playerId="+playerId
			+", actId="+actId+", topicIndex="+topicIndex+", topicType="
			+topicType+", answer="+answer+"]";
	}

}
