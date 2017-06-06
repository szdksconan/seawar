package foxu.dcaccess.datasave;

import mustang.io.ByteBuffer;

/**���ݷ�װ
 * ��Ӧ����
 * ������ݿ��ʱ��
 * ���redis��ʱ��
 * */
public abstract class ObjectSave
{
	/** ��¼�����ݴ������ݿ��ʱ�� */
	int saveTimeDB;
	/** ��¼�����ݴ���redis��ʱ�� */
	int saveTimeRedis;
	
	/**��ȡ�洢Id*/
    public abstract int getId();
    /**��ȡ�洢����*/
    public abstract Object getData();
    /**���ô洢����*/
    public abstract void setData(Object data);
    /**��ȡ�ö����byteBuffer*/
    public abstract ByteBuffer getByteBuffer();
   
    /**��ȡ�洢�����ݿ��ʱ��*/
	public int getSaveTimeDB()
	{
		return saveTimeDB;
	}
    
	/**��ȡ�洢��redis��ʱ��*/
	public int getSaveTimeRedis()
	{
		return saveTimeRedis;
	}

	/**�������ݿ�Ĵ洢ʱ��*/
	public void setSaveTimeDB(int saveTime)
	{
		this.saveTimeDB=saveTime;
	}

	/**���ô洢��redis��ʱ��*/
	public void setSaveTimeRedis(int saveTime)
	{
		this.saveTimeRedis = saveTime;
	}
}
