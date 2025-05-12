import java.util.*;

abstract class Client { //PHONE abstraction from pdss
	private final String name; 
	private final String adress; 
	private final String number;
	
	public Client (String name, String adress, String number) { 
		this.name = name; 
		this.adress = adress; 
		this.number = number;
	}
	
	public abstract void accept(Visitor visitor); //EXEC_FEATURE din pdss
		//(ne ajuta sa apelam metoda necesara acelui client pe un visitor anume primit ca parametru)
}

class Resident extends Client{
	private final String insuranceClass;
	
	public Resident(String name, String adress, String number, String insuranceClass) { 
		super(name, adress, number); 
		this.insuranceClass = insuranceClass;
	}
	
	public void accept(Visitor visitor) { //in fiecare subclasa
		visitor.visitResident(this);
	}
}

class Bank extends Client { 
	private final int sum; 
	
	public Bank (String name, String adress, String number, int value) { 
		super(name, adress, number); 
		this.sum = value;
	}
	
	public void accept(Visitor visitor) { 
		visitor.visitBank(this);
	}
}

class Restaurant extends Client { 
	private final int orders; 
	
	public Restaurant(String name, String adress, String number, int orders) { 
		super(name, adress, number); 
		this.orders = orders;
	}
	
	public void accept(Visitor visitor) { 
		visitor.visitRestaurant(this);
	}
}

interface Visitor { //FEATURE interface from pdss
	void visitBank(Bank bank); 
	void visitResident(Resident resident); //o metoda pentru fiecare Client TYPE
	void visitRestaurant(Restaurant restaurant);
}

class DiscountsMessagesVisitor implements Visitor { //MUSIC feature(acum apar thirdPartyAPI's ca la SUBIECT)
	public DiscountsMessagesVisitor() { 
		//CONSTRUCTOR aceasta clasa poate fi instantiata
	}
	
	public void sentMessToClients(List<Client> clients) { //o lista cu tipul BASE pentru elemente
		for (Client cl : clients) { 
			cl.accept(this); //in client in metoda ACCEPT se face call la VISIT_X
		}
	}
	
	public void visitBank(Bank bank) { 
		System.out.println("Message arrayved in Bank MESSAGEfeature");
	}
	
	public void visitResident(Resident resident) { 
		System.out.println("Message arrayved in Resident MESSAGEfeature");
	}
	
	public void visitRestaurant(Restaurant restaurant) { 
		System.out.println("Message arrayved in Restaurant MESSAGEfeature");
	}
}

class GiveReviewOnLocationVisitor implements Visitor { //CALL feature din pdss
	public GiveReviewOnLocationVisitor() { 
		//CONSTRUCTOR aceasta clasa poate fi instantiata
	}
	
	public void sentMessToClients(List<Client> clients) { //prelucram lista de elem CONCRETE BASE 
		for (Client cl : clients) { 
			cl.accept(this); //AICI se intampla logica de apelare
		}
	}
	
	public void visitBank(Bank bank) { 
		//do something
	}
	
	public void visitResident(Resident resident) { 
		//do something
	}
	
	public void visitRestaurant(Restaurant restaurant) { 
		//do something
	}

}


public class Main { 
	public static void main (String[] args) { 
		System.out.println("MERGE");
	}
}