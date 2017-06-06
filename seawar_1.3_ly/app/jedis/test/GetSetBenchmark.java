package jedis.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import redis.clients.jedis.Jedis;

public class GetSetBenchmark {
    private static final int TOTAL_OPERATIONS = 100;

    public static void main(String[] args) throws UnknownHostException,
	    IOException {
	Jedis jedis = new Jedis("localhost", 6379);
	jedis.connect();
	
//	jedis.auth("foobared");
	jedis.flushAll();

	long begin = Calendar.getInstance().getTimeInMillis();

	for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
	    String key = "foo" + n;
	    String value = "icetigeriiiiiiiiiiioooooooooooooooooooooooo";
	    value = value+value+value+value+value+value+value;
	    jedis.set(key, value + n);
	    jedis.get(key);
	}
	jedis.select(0);
	jedis.set("icetiger", "12345");
	
	jedis.select(1);
	jedis.set("icetiger", "15");
	
	System.out.println("=========="+jedis.get("icetiger"));
	jedis.select(0);
	System.out.println("=========="+jedis.get("icetiger"));

	long elapsed = Calendar.getInstance().getTimeInMillis() - begin;
    System.out.println("所用时间====="+elapsed);
	jedis.disconnect();

	System.out.println(((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " ops");
    }
}