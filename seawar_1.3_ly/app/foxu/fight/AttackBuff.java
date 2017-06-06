package foxu.fight;

/**
 * ȫ�ִ���BUFF ������������ ̫������ ��Ҫ�����˺�����  
 * @author liuh
 *
 */
public class AttackBuff implements Cloneable
{
	private boolean isUse;
	/** ��¼ �����ľ��ټ��� SID*/
	private int sid;
	/** �������� */
	private float attAlonePercent;
	/** ����*/
	private boolean isMustHit;
	/** �ر�*/
	private boolean isMustErup;
	/** ���ַ� ���ֻ������Լӳ� */
	AttBaseAttribute attBaseAttribute;
	/** Ŀ�� ������Ļ������Լӳ� ����������֮�� */
	DefBaseAttribute defBaseAttribute;
	
	
	/** ������һЩ���� ���� */
	/** ��γ��� �Ƿ�������*/
	private boolean openShield;
	/** �����Ļ���ֵ*/
	private int shield;
	/** �������ܵİٷֱ� */
	private int shieldPrecent;
	
	
	
	public AttackBuff clone() {  
		AttackBuff o = null;  
        try {  
            o = (AttackBuff) super.clone();  
        } catch (CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return o;  
    }  
	/** ��� */
	public void addShield(float hurt){
		this.shield+=(int)hurt*shieldPrecent/100;
	}
	
	
	public int getShieldPrecent()
	{
		return shieldPrecent;
	}
	
	public void setShieldPrecent(int shieldPrecent)
	{
		this.shieldPrecent=shieldPrecent;
	}
	public boolean isOpenShield()
	{
		return openShield;
	}
	
	public void setOpenShield(boolean openShield)
	{
		this.openShield=openShield;
	}
	
	public float getShield()
	{
		return shield;
	}

	public void setShield(int shield)
	{
		this.shield=shield;
	}

	/** ���������ڲ����� �ӷ� �磺D3�Ķ�������ϵͳ*/
	public void addAttAlonePercent(int percent){
		attAlonePercent+=percent;
	}
	
	public float getAttAlonePercent()
	{
		return attAlonePercent;
	}
	
	public void setAttAlonePercent(float attAlonePercent)
	{
		this.attAlonePercent=attAlonePercent;
	}
	
	public boolean isMustHit()
	{
		return isMustHit;
	}
	
	public void setMustHit(boolean isMustHit)
	{
		this.isMustHit=isMustHit;
	}
	
	public boolean isMustErup()
	{
		return isMustErup;
	}
	
	public void setMustErup(boolean isMustErup)
	{
		this.isMustErup=isMustErup;
	}
	
	public AttBaseAttribute getAttBaseAttribute()
	{
		return attBaseAttribute;
	}
	
	public void setAttBaseAttribute(AttBaseAttribute attBaseAttribute)
	{
		this.attBaseAttribute=attBaseAttribute;
	}
	
	public DefBaseAttribute getDefBaseAttribute()
	{
		return defBaseAttribute;
	}
	
	public void setDefBaseAttribute(DefBaseAttribute defBaseAttribute)
	{
		this.defBaseAttribute=defBaseAttribute;
	}
	
	public boolean isUse()
	{
		return isUse;
	}
	
	public void setUse(boolean isUse)
	{
		this.isUse=isUse;
	}
	
	public int getSid()
	{
		return sid;
	}
	
	public void setSid(int sid)
	{
		this.sid=sid;
	}
	
	
	
	
	
}
