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
	/** ���߳����з�����������֤ͨѶ */
	public void run()
	{
//		System.out.println("��ǰ�߳�ID====="+Thread.currentThread().getId());
//		for(int i=110;i<101;i++)
//		{
//			Player player=(Player)Role.factory.newSample(1);
//			player.setPlayer_name("test:"+i);
//			player.setIsland_name("���Ե���:"+i);
//			player.setUser_id(i+1);
//			player.getResources().addGems(5000);
//			PlayerBuild build=player.getBuildingAndTroops().getBuildByType(
//				Build.BUILD_STORE,player.getBuildingAndTroops().getBuilds());
//			player.getResources().addResources(2500,2500,2500,2500,2500,
//				build);
//			gameDataHandleImp.save(ac,player);
//			System.out.println("�̸߳���====="
//				+((SqlPersistence)ac.getGamePersistence())
//					.getConnectionManager().size());
//			System.out.println("��������run����====="
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
	 * @param gameDataHandleImp Ҫ���õ� gameDataHandleImp
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
	 * @param ac Ҫ���õ� ac
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
	 * @param i Ҫ���õ� i
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
	 * @param objectFactory Ҫ���õ� objectFactory
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
	 * @param password Ҫ���õ� password
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
	 * @param userAccount Ҫ���õ� userAccount
	 */
	public void setUserAccount(String userAccount)
	{
		this.userAccount=userAccount;
	}
} 
