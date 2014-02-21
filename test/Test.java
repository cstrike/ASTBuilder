package abc;

import a.b.c;
import x.y.*;

class Test2 extends Test implements A {} 

interface A extends B,C,D{}

public class Test {
	public final int x=0;
	private static XXX b = null;
	static{
		int a;
		x = x+1;
		x = false;
		//adb
		System.out.println("bla");
		sum = l.stream()
				.map(e -> new Integer(e))
				.filter(n -> Primes.isPrime(n))
				.distinct()
				.reduce(0, (x,y) -> x+y);  
		System.out.println("bla");
	}
	public void A(int a,int b) throws NullPointerException{}
	public void B(){}

	public void distinctPrimarySum(String... numbers) {
		List<String> l = Arrays.asList(numbers);
		int sum = 0;
		sum = l.stream()
				.map(e -> new Integer(e))
				.filter(n -> Primes.isPrime(n))
				.distinct()
				.reduce(0, (x,y) -> x+y); 
		System.out.println("distinctPrimarySum result is: " + sum);
	}
	
	public void distinctPrimarySum(String[] numbers) {
		List<String> l = Arrays.asList(numbers);
		int sum = 0;
		for(String e : l){
			int n = new Integer(e);
			if (Primes.isPrime(n)){
				sum += n;
			}
		}
	}
	
}