package foxu.sea.task;

/**
 * 任务事件 author:icetiger
 */
public  class TaskEvent
{
	/** 事件源 */
	Object source;
	/** 事件类型 */
	int eventType;
	
	/**额外参数*/
	Object param;

	/**
	 * @return eventType
	 */
	public int getEventType()
	{
		return eventType;
	}

	/**
	 * @param eventType 要设置的 eventType
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
	 * @param source 要设置的 source
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
	 * @param param 要设置的 param
	 */
	public void setParam(Object param)
	{
		this.param=param;
	}
}
