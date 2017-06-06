package jedis.test;

public class ThreadTest
{

	public synchronized void A()
	{
		System.out.println("a");
		try
		{
			Thread.sleep(5000);
		}
		catch(InterruptedException e)
		{
			// TODO �Զ����� catch ��
			e.printStackTrace();
		}
		System.out.println("a");
	}

	public synchronized void B()
	{
		System.out.println("b");
	}

	public void C()
	{
		System.out.println("c");
	}
}
