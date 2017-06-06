package foxu.fight;

import mustang.io.ByteBuffer;

/**
 * �ı��ɫ����Ч����
 * 
 * @author ZYT
 */
public class ChangeAttributeEffect extends Effect
{

	/* static fields */
	/** ��ֵ���ͳ���:PRECENT=1�ٷֱ�,ABSOLUTE_VALUE=2�̶�ֵ */
	public static final int PRECENT=1,ABSOLUTE_VALUE=2;

	/* fields */
	/** ����ָ�����漼�ܵĿ��� */
	int abilityCid;
	/** Ч�����õ����� */
	float effectValue;
	/** Ч�������õ����� */
	int changeAttr;
	/** ��ֵ����:�ٷֱ�,����ֵ */
	int valueType=1;

	/* properties */
	/** ���Ч���������� */
	public int getDataType()
	{
		return changeAttr;
	}
	/** ����Ч���������� */
	public void setValue(float effectValue)
	{
		this.effectValue=effectValue;
	}
	/** ���Ч������ */
	public float getValue(Fighter fighter)
	{
		return effectValue;
	}
	/** ����������� */
	public int getValueType()
	{
		return valueType;
	}

	/* methods */
	/**
	 * ����Ƿ������Ч���������ü���
	 * 
	 * @param type ��������
	 * @param cid ���ܵ�cid
	 * @return true��ʾ��������
	 */
	public boolean checkType(int type,int cid)
	{
		if(abilityCid!=0) return cid==abilityCid&&(type==changeAttr);
		return changeAttr==type;
	}
	public void bytesWrite(ByteBuffer data)
	{
		super.bytesWrite(data);
		data.writeFloat(effectValue);
	}
	public Object bytesRead(ByteBuffer data)
	{
		super.bytesRead(data);
		effectValue=data.readFloat();
		return this;
	}

	/* common methods */
	public String toString()
	{
		return super.toString()+"[changeAttr="+changeAttr+", effectValue="
			+effectValue+"] ";
	}
}