package foxu.fight;

/**
 * 全局触发BUFF 不按技能做了 太繁琐了 主要用于伤害计算  
 * @author liuh
 *
 */
public class AttackBuff implements Cloneable
{
	private boolean isUse;
	/** 记录 发动的军官技能 SID*/
	private int sid;
	/** 独立增伤 */
	private float attAlonePercent;
	/** 必中*/
	private boolean isMustHit;
	/** 必爆*/
	private boolean isMustErup;
	/** 出手方 出手基础属性加成 */
	AttBaseAttribute attBaseAttribute;
	/** 目标 防御向的基础属性加成 如闪避韧性之类 */
	DefBaseAttribute defBaseAttribute;
	
	
	/** 下面是一些条件 参数 */
	/** 这次出手 是否开启护盾*/
	private boolean openShield;
	/** 产生的护盾值*/
	private int shield;
	/** 产生护盾的百分比 */
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
	/** 添加 */
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

	/** 独立增伤内部采用 加法 如：D3的独立增伤系统*/
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
