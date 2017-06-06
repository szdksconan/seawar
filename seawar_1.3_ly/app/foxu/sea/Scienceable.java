/**
 * 
 */
package foxu.sea;

/**
 * 科技操作借口
 * 
 * @author rockzyt
 */
public interface Scienceable
{

	/**
	 * 用科技改变Player
	 * 
	 * @param p
	 * @param s
	 */
	public void scienceChangeLife(Player p,Science s);
}