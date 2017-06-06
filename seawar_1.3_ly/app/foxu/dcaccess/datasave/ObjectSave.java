package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;

/**数据封装
 * 对应对象
 * 存进数据库的时间
 * 存进redis的时间
 * */
public abstract class ObjectSave
{
	/** 记录该数据存入数据库的时间 */
	int saveTimeDB;
	/** 记录该数据存入redis的时间 */
	int saveTimeRedis;
	
	/**获取存储Id*/
    public abstract int getId();
    /**获取存储对象*/
    public abstract Object getData();
    /**设置存储对象*/
    public abstract void setData(Object data);
    /**获取该对象的byteBuffer*/
    public abstract ByteBuffer getByteBuffer();
   
    /**获取存储进数据库的时间*/
	public int getSaveTimeDB()
	{
		return saveTimeDB;
	}
    
	/**获取存储进redis的时间*/
	public int getSaveTimeRedis()
	{
		return saveTimeRedis;
	}

	/**保存数据库的存储时间*/
	public void setSaveTimeDB(int saveTime)
	{
		this.saveTimeDB=saveTime;
	}

	/**设置存储进redis的时间*/
	public void setSaveTimeRedis(int saveTime)
	{
		this.saveTimeRedis = saveTime;
	}
}
