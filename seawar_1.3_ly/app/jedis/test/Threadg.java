package jedis.test;


public class Threadg implements Runnable
{
	ThreadTest a;
	
	public Threadg(ThreadTest test)
	{
		a=test;
	}

	public void run()
	{
		a.A();
	}
	
	public static void main(String[] args)
	{
		ThreadTest test = new ThreadTest();
		Threadg r1 = new Threadg(test); //也可写成ThreadTest r1 = new ThreadTest();
		TreadC r2 = new TreadC(test);
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r2);
		t1.start();
		try
		{
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
		t2.start();
	}

	
	public ThreadTest getA()
	{
		return a;
	}

	
	public void setA(ThreadTest a)
	{
		this.a=a;
	}
    
}
