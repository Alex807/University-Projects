/*One of the great example of Chain of Responsibility pattern is ATM Dispense machine. 
The user enters the amount to be dispensed and the machine dispense amount in terms of defined currency bills such as 50$, 20$, 10$ etc. 
If the user enters an amount that is not multiples of 10, it throws error. 
We will use Chain of Responsibility pattern to implement this solution. 
The chain will process the request in order: 50 -> 20 -> 10*/

import java.util.*;

class Currency { //clasa ce retine valoarea unei BANCNOTE
	private int amount; 
	
	public Currency(int amount) { 
		this.amount = amount;
	}
	
	public int getAmount() { 
		return amount;
	}
}

interface DispenseChain { 
	void setNextChain(DispenseChain nextChain); 
	void dispense(Currency currency);
}

class Dollar50Dispenser implements DispenseChain { 
	private DispenseChain nextChain; 
	
	public void setNextChain(DispenseChain nextChain) { 
		this.nextChain = nextChain;
	}
	
	public void dispense(Currency cur) { 
		int amount = cur.getAmount();
		if (amount >= 50) { //VERIFICAREA cu acel param protected succeeded
			int totalNotes = amount / 50; 
			int changeLeft = amount % 50; 
			
			System.out.println(String.format("Just dispensed %d 50Ron note/s", totalNotes)); 
			
			if (changeLeft != 0) this.nextChain.dispense(new Currency(changeLeft));
		
		} 
		
		if (nextChain == null) {
			System.out.println("This configuration of linkage don't devide amount.");			
			System.exit(-1);
		}else { 
			this.nextChain.dispense(cur); //NU PUTEM gestiona acest caz in nodul curent, deci pasam la urmatorul
		}
	} 
}

class Dollar20Dispenser implements DispenseChain { 
	private DispenseChain nextChain; 
	
	public void setNextChain(DispenseChain chain) { 
		this.nextChain = chain;
	}
	
	public void dispense(Currency cur) { 
		int amount = cur.getAmount();
		if (amount >= 20) { 
			int totalNotes = amount / 20; 
			int changeLeft = amount % 20;
			System.out.println(String.format("Just dispensed %d 20Ron note/s\n", totalNotes));
			
			if (changeLeft != 0) this.nextChain.dispense(new Currency(changeLeft));
				
		} 
		
		if (nextChain == null) { 
			System.out.println("This configuration of linkage don't devide amount.");
			System.exit(-1);
		}else { 
			this.nextChain.dispense(cur); //NU PUTEM gestiona acest caz in nodul curent, deci pasam la urmatorul
		}
	}
} 

class Dollar10Dispenser implements DispenseChain { 
	private DispenseChain nextChain; 
	
	public void setNextChain(DispenseChain chain) { 
		this.nextChain = chain;
	}
	
	public void dispense(Currency cur) { 
		int amount = cur.getAmount();
		if (amount >= 10) { 
			int totalNotes = amount / 10; 
			int changeLeft = amount % 10;
			System.out.println(String.format("Just dispensed %d 10Ron note/s\n", totalNotes));
			
			if (changeLeft != 0) this.nextChain.dispense(new Currency(changeLeft));
				
		} 
		if (nextChain == null) {
			System.out.println("This configuration of linkage don't devide amount.");			
			System.exit(-1);
		}else { 
			this.nextChain.dispense(cur); //NU PUTEM gestiona acest caz in nodul curent, deci pasam la urmatorul
		}
	}
} 

public class Main {

	private DispenseChain c1;

	public Main() {
		// initialize the chain
		this.c1 = new Dollar50Dispenser();
		DispenseChain c2 = new Dollar20Dispenser();
		DispenseChain c3 = new Dollar10Dispenser();

		// set the chain of responsibility
		c1.setNextChain(c2);
		//c2.setNextChain(c1);
	}

	public static void main(String[] args) {
		Main atmDispenser = new Main();
		while (true) {
			int amount = 0;
			System.out.println("Enter amount to dispense");
			Scanner input = new Scanner(System.in);
			amount = input.nextInt();
			if (amount % 10 != 0) {
				System.out.println("Amount should be in multiple of 10s.");
				continue;
			}
			// process the request
			atmDispenser.c1.dispense(new Currency(amount));
			break; //stop the code
		}

	}

}