package foxu.fight;


public class DefBaseAttribute
{
	
	/**韧性 */
	float toughness;
	/**闪避 */
	float dodge;
	/**减少百分比*/
	float hurtResistancePercent;
	
	public float addToughness(float toughness){
		this.toughness += toughness;
		return this.toughness;
	}
	
	public float addDodge(float dodge){
		this.dodge += dodge;
		return this.dodge;
	}
	
	public float addHurtResistancePercent(float hurtResistancePercent){
		this.hurtResistancePercent += hurtResistancePercent;
		return this.hurtResistancePercent;
	}

	
	public float getToughness()
	{
		return toughness;
	}

	
	public void setToughness(float toughness)
	{
		this.toughness=toughness;
	}

	
	public float getDodge()
	{
		return dodge;
	}

	
	public void setDodge(float dodge)
	{
		this.dodge=dodge;
	}

	
	public float getHurtResistancePercent()
	{
		return hurtResistancePercent;
	}

	
	public void setHurtResistancePercent(float hurtResistancePercent)
	{
		this.hurtResistancePercent=hurtResistancePercent;
	}
	
	
	
	
	
	
	

}
