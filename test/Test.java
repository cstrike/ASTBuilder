package abc;

public class Test extends ASTBuilder {
	public void distinctPrimarySum(String[] numbers) {
		List<String> l = Arrays.asList(numbers);
		int sum = 0;
		for(String e : l){
			int n = newInteger(e);
			if (Primes.isPrime(n)){
				//distinct() This part is not support;
				sum += n;
			}
		}
	}
	
	public void distinctPrimarySum(String... numbers) {
		    List<String> l = Arrays.asList(numbers);
		    int sum = 0;
			sum = l.stream()
		        .map(e -> new Integer(e))
		        .filter(n -> Primes.isPrime(n))
		        .distinct()
		        .reduce(0, (x,y) -> x+y); // equivalent to .sum()
		    System.out.println("distinctPrimarySum result is: " + sum);
		}
}

interface A {}

class Test2 extends Test implements A {} 