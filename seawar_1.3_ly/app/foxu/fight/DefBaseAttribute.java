package foxu.fight;


public class DefBaseAttribute
{
	
	/**���� */
	float toughness;
	/**���� */
	float dodge;
	/**���ٰٷֱ�*/
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
