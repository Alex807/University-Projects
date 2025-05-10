// ATM ce divide o suma in mai multe tipuri de bancnote

import java.util.*;

class Currency { //clasa ce retine valoarea unei BANCNOTE
	private int amount; //(obiectul pe care facem HANDLE)
	
	public Currency(int amount) { 
		this.amount = amount;
	}
	
	public int getAmount() { 
		return amount;
	}
}

interface Handler { //INTERFATA pt BASE-type in care tinem doar metoda de handle
	boolean handle(Currency currency); 
	Handler setNext(Handler handler);
}

abstract class BaseHandler implements Handler{ //FACTORIZAM codul comun celor ce vor avea logica de handle 
	private Handler next; 
	
	public abstract boolean handle(Currency currency); //override in fiecare subclasa
	
	public Handler setNext(Handler handler) {
		this.next = handler; 
		return handler; //facem asta pentru a seta mai usor lantul de HANDLERS
	}
	
	protected boolean handleNext(Currency currency) { //o fol. sa fim SIGURI ca AVEM NEXT(e PROTECTED)
		if (next == null && currency.getAmount() != 0) { //LOGICA cand e task-ul tau rezolvat complet sau NU
			return false;
		
		} else if (currency.getAmount() == 0) { 
			 
			return true;
		}
		return next.handle(currency); //PASAM spre next 
	}
}

class ATM_CashHandler extends BaseHandler { // CONCRET HANDLER ce face handle
	private int noteValue; 
	
	public ATM_CashHandler (int noteValue) { //primesti ce ai nevoie(atribute specifice unui nod)
		this.noteValue = noteValue;
	}
	
	public boolean handle(Currency cur) { 
		int amount = cur.getAmount();
		int totalNotes = amount / noteValue; 
		int changeLeft = amount % noteValue; 
		
		if (amount >= noteValue) { //VERIFICAM daca putem rezolva TASK in acest nod, daca nu PASS IT
			System.out.println(String.format("Just dispensed %d %dRon note/s", totalNotes, noteValue)); 
		} 
		
		return handleNext(new Currency(changeLeft)); //metoda din BASE-Handler se ocupa de null-checks si daca procesul e gata
	} //doar aici apelezi catre un nextHandler
}

public class Main {

	private Handler handler;

	public Main() {
		// creem handleri
		this.handler = new ATM_CashHandler(50);
		Handler c2 = new ATM_CashHandler(20);
		Handler c1 = new ATM_CashHandler(10);

		// set the chain of responsibility
		handler.setNext(c2).setNext(c1);
	}

	public static void main(String[] args) {
		Main ATM = new Main();
		
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
			if ( ATM.handler.handle(new Currency(amount))) {
				System.out.println("ATM dispense all the notes needed!");
			
			} else { 
				System.out.println("This configuration of linkage don't devide amount.");		
			} 
			break;
		}
	}
}