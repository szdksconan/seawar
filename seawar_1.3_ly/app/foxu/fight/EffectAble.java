package foxu.fight;

/**
 * ����װ������Ч�������ӿ�
 * 
 * @author ZYT
 */
public interface EffectAble
{

	/* static fields */
	/**
	 * HARM_VALUE=1������,DEFENS_VALUE=2������,HURT_VALUE=4�������˵��˺�����,EXPEMT_VALUE=8��������
	 * BE_HURT_VALUE=16���������˺�����
	 */
	public static final int HARM_VALUE=1,DEFENS_VALUE=2,HURT_VALUE=4,
					EXPEMT_VALUE=8,BE_HURT_VALUE=16;

	/* methdos */
	/**
	 * ʹЧ����Ч:��Ҫ�ı�ĳ������
	 * 
	 * @param source ʩ��Դ
	 * @param target Ŀ��
	 * @param data ��Ҫ����������
	 * @param type ��������
	 */
	public float used(Fighter source,Object target,float data,int type);
	/**
	 * ʹЧ����Ч:��Ҫ�ı�ĳ������
	 * 
	 * @param source ʩ��Դ
	 * @param target Ŀ��
	 * @param data ��Ҫ�����Ķ���
	 * @return
	 */
	public Object used(Fighter source,Object target,Object data);
}
