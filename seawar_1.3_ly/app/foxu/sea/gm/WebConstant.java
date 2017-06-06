package foxu.sea.gm;

/***
 * web 数据返回
 * @author lhj
 * 
 */
public interface WebConstant
{
	/** 排头 **/
	public final String PIONEER="player";
	public final String SUCCESS="success";
	public final String LENGTH="lenght";
	//游戏服务器
	public final String SERVER_ID="server_id";
	//大区id
	public final String AREA_ID="area_id";
	//角色的创建时间
	public final String CREATETIME="createTime";
	//角色登录时间
	public final String LOGINTIME="loginTime";
	/**记录类的排头**/
	public final String RECORD="record";
	/**大区**/
	public final String AREAID="aid";
	/**平台**/
	public final String PLATFORM="pid";
	
	public final String PLAT="plat";
	/**记录类的返回的JSON 构造**/
	public final String GEM="gem",
										PLAYER_ID="player_id",
										PLAYER_NAME="player_name",
										ACCOUNT="account",
										MONEY="money",
										TYPE="type",
										SERVER_TIME="server_time";
	
	/**只要是游戏中心返回的数据 那么直接从10000开始这样好区分**/
	//角色名称为空
	public final int ROLENAME_NULL=10000;
	//账号不存在
	public final int ACCOUNT_IS_NULL=10001;
	//角色不存在
	public  final int PLAYER_NAME_IS_NULL=10002;

}
