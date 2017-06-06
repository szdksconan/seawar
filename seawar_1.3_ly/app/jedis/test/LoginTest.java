package jedis.test;

import shelby.dc.GameDBAccess;
import shelby.dc.GameDataHandleImp;
import foxu.dcaccess.CreatObjectFactory;


public class LoginTest
{ 

	GameDataHandleImp gameDataHandleImp;

	GameDBAccess ac;
	
	CreatObjectFactory objectFactory;
	
	String userAccount;
	
	String password;

	int i;
	/** 新线程运行方法，负责认证通讯 */
	public void run()
	{
//		System.out.println("当前线程ID====="+Thread.currentThread().getId());
//		for(int i=110;i<101;i++)
//		{
//			Player player=(Player)Role.factory.newSample(1);
//			player.setPlayer_name("test:"+i);
//			player.setIsland_name("测试岛屿:"+i);
//			player.setUser_id(i+1);
//			player.getResources().addGems(5000);
//			PlayerBuild build=player.getBuildingAndTroops().getBuildByType(
//				Build.BUILD_STORE,player.getBuildingAndTroops().getBuilds());
//			player.getResources().addResources(2500,2500,2500,2500,2500,
//				build);
//			gameDataHandleImp.save(ac,player);
//			System.out.println("线程个数====="
//				+((SqlPersistence)ac.getGamePersistence())
//					.getConnectionManager().size());
//			System.out.println("正在运行run个数====="
//				+((SqlPersistence)ac.getGamePersistence())
//					.getConnectionManager().getRunningCount());
//		}
//		objectFactory.createUser(userAccount,password);
	}


	
	/**
	 * @return gameDataHandleImp
	 */
	public GameDataHandleImp getGameDataHandleImp()
	{
		return gameDataHandleImp;
	}


	
	/**
	 * @param gameDataHandleImp 要设置的 gameDataHandleImp
	 */
	public void setGameDataHandleImp(GameDataHandleImp gameDataHandleImp)
	{
		this.gameDataHandleImp=gameDataHandleImp;
	}



	
	/**
	 * @return ac
	 */
	public GameDBAccess getAc()
	{
		return ac;
	}



	
	/**
	 * @param ac 要设置的 ac
	 */
	public void setAc(GameDBAccess ac)
	{
		this.ac=ac;
	}



	
	/**
	 * @return i
	 */
	public int getI()
	{
		return i;
	}



	
	/**
	 * @param i 要设置的 i
	 */
	public void setI(int i)
	{
		this.i=i;
	}



	
	/**
	 * @return objectFactory
	 */
	public CreatObjectFactory getObjectFactory()
	{
		return objectFactory;
	}



	
	/**
	 * @param objectFactory 要设置的 objectFactory
	 */
	public void setObjectFactory(CreatObjectFactory objectFactory)
	{
		this.objectFactory=objectFactory;
	}



	
	/**
	 * @return password
	 */
	public String getPassword()
	{
		return password;
	}



	
	/**
	 * @param password 要设置的 password
	 */
	public void setPassword(String password)
	{
		this.password=password;
	}



	
	/**
	 * @return userAccount
	 */
	public String getUserAccount()
	{
		return userAccount;
	}



	
	/**
	 * @param userAccount 要设置的 userAccount
	 */
	public void setUserAccount(String userAccount)
	{
		this.userAccount=userAccount;
	}
} 
