package foxu.fight;

import mustang.io.BytesReader;
import mustang.io.BytesWritable;

/**
 * ���Ľӿ�
 * </p>
 * ���ڸ��ּ����в�ͬ�����ķ�ʽ,Ϊ�˱���Ϊʵ�ֲ�ͬ�����ķ�ʽ��дһ��Ability������,���Խ����ķ�ʽ����Ϊһ���ӿ�
 * ���м��ܵ����ķ�ʽ��ʵ�ִ˽ӿ�,��ע�ᵽ��Ӧ�ļ�����ȥ
 * 
 * @author ZYT
 */
public interface Consumer extends Cloneable,BytesWritable,BytesReader
{

	/* abstract methods */
	/**
	 * ��鼼���ܷ���ȷ�����ĵ�������Դ
	 * 
	 * @param fighter ʹ�ü��ܵ�fighter
	 * @param ability ��ʹ�õļ���
	 * @return ����true��ʾ����ȷ����
	 */
	public boolean checkConsume(Fighter fighter,Ability ability);
	/**
	 * ʹ�ü��ܺ�����fighter�����Ի���Ʒ
	 * 
	 * @param fighter ʹ�ü��ܵ�fighter
	 * @param ability ��ʹ�õļ���
	 */
	public void consume(Fighter fighter,Ability ability);
	
	public Object clone();
	
}
