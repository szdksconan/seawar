package jedis.test;


public class TreadC implements Runnable
{
	ThreadTest a;
	
	public TreadC(ThreadTest test)
	{
		a=test;
	}
	
	public void run()
	{
		a.B();
	}
}
