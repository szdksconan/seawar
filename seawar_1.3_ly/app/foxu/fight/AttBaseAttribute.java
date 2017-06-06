package foxu.fight;


public class AttBaseAttribute
{
	
	/**命中 */
	float hit;
	/**暴击率 */
	float crit;
	/**基础伤害百分比*/
	float hurtPercent;
	
	public float addHit(float hit){
		this.hit += hit;
		return this.hit;
	}
	
	public float addCrit(float crit){
		this.crit += crit;
		return this.crit;
	}
	
	public float addHurtPercent(float hurtPercent){
		this.hurtPercent += hurtPercent;
		return this.hurtPercent;
	}
	
	
	public float getHit()
	{
		return hit;
	}
	
	public void setHit(float hit)
	{
		this.hit=hit;
	}
	
	public float getCrit()
	{
		return crit;
	}
	
	public void setCrit(float crit)
	{
		this.crit=crit;
	}
	
	public float getHurtPercent()
	{
		return hurtPercent;
	}
	
	public void setHurtPercent(float hurtPercent)
	{
		this.hurtPercent=hurtPercent;
	}
	
	
	
	
	

}
