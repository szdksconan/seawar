package jedis.test;

import java.util.ArrayList;
import java.util.List;

import mustang.math.Random;
import mustang.math.Random1;
import mustang.util.TimeKit;
import foxu.sea.kit.SeaBackKit;



public class RandomTest
{
	public static void main(String[] args) {
		 Random rd=new Random1();
		 List a = new ArrayList(10000);
		 List b = new ArrayList(10000);
		 for(int i=0;i<10000;i++){
			int randomInt =  rd.randomValue(0,10001);
			if(randomInt<9000) a.add(randomInt);
			else b.add(randomInt);
		 }
		 System.out.println(a.size());
		 System.out.println(b.size());
	}

}
