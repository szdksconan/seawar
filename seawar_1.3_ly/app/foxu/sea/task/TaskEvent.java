package foxu.sea.task;

/**
 * �����¼� author:icetiger
 */
public  class TaskEvent
{
	/** �¼�Դ */
	Object source;
	/** �¼����� */
	int eventType;
	
	/**�������*/
	Object param;

	/**
	 * @return eventType
	 */
	public int getEventType()
	{
		return eventType;
	}

	/**
	 * @param eventType Ҫ���õ� eventType
	 */
	public void setEventType(int eventType)
	{
		this.eventType=eventType;
	}

	/**
	 * @return source
	 */
	public Object getSource()
	{
		return source;
	}

	/**
	 * @param source Ҫ���õ� source
	 */
	public void setSource(Object source)
	{
		this.source=source;
	}

	
	/**
	 * @return param
	 */
	public Object getParam()
	{
		return param;
	}

	
	/**
	 * @param param Ҫ���õ� param
	 */
	public void setParam(Object param)
	{
		this.param=param;
	}
}
