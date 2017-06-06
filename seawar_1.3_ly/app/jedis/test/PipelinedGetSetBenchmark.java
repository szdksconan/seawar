package jedis.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class PipelinedGetSetBenchmark {
    private static final int TOTAL_OPERATIONS = 200000;

    public static void main(String[] args) throws UnknownHostException,
            IOException {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.connect();
//        jedis.auth("foobared");
        jedis.flushAll();

        long begin = Calendar.getInstance().getTimeInMillis();

        Pipeline p = jedis.pipelined();
        for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
            String key = "foofffffffffffffffffff" + n;
            String value = "abcdefghijklmnopqrstuvwxyz0123456789";
            value = value+value+value+value+value+value+value+value+value+value+value;
            p.set(key, "bar" + n);
            p.get(key);
        }
        
        p.sync();

        long elapsed = Calendar.getInstance().getTimeInMillis() - begin;
        System.out.println("elapsed====="+elapsed);
        jedis.disconnect();

        System.out.println(((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " ops");
    }
}