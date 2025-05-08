interface INotifier { //ELEMENT interface(trb implementate de LEAF + DECORATOR-ELEMENT)
	void sendNotif(String message);
}//aici punem actiunile ce trb facute de LEAF/DECORATER ELEMENTS

class Notifier implements INotifier{ //LEAF-ELEMENT(el NU contine in componenta alt elem, doar executa logica lui proprie)
	private final String userName; 
	private final String email;
	
	public Notifier(String userName){ 
		this.userName = userName; 
		this.email = userName + "@gmail.com";
	}
	
	public void sendNotif(String msg) { 
		System.out.println("Sending " + msg + " to email: " + email);
	}
}

abstract class BaseNotifierDecorator implements INotifier{ //DECORATOR-ELEMENT (tipul abstract pentru a combina diverse configuratii ale subclaselor ACESTEIA)
	private final INotifier wrapped;  //MEMBRUL ce aduce functionalitate EXTRA celei a obiectului curent din subclase
	
	public BaseNotifierDecorator(INotifier wrapped) { 
		this.wrapped = wrapped;
	}
	
	public void sendNotif(String message) { //IMPLEMENTARE pt metoda din interfata 
		wrapped.sendNotif(message); //transmitem apelul mai departe pana ajunge la LEAF-ELEM
	}
}

class WhatsAppDecorator extends BaseNotifierDecorator { //tip CONCRET de decorator-element
	public WhatsAppDecorator(INotifier wrapped) { //!!!ATENTIE
		super(wrapped); //primeste ca arg la constructie un obiect LEAF/DEC-ELEM
	}
	
	public void sendNotif(String msg) { 
		super.sendNotif(msg); //adaugarea FUNCTIONALITATII obiectului wrapped
		//OBLIGATORIU sa o pui mereu inainte sau dupa logica din obiectul curent 
		
		System.out.println("Sending " + msg + " by WHATSAPP");
	}
}

class FacebookDecorator extends BaseNotifierDecorator { //tip CONCRET de decorator-element
	public FacebookDecorator(INotifier wrapped) { //!!!ATENTIE
		super(wrapped); //primeste ca arg la constructie un obiect LEAF/DEC-ELEM
	}
	
	public void sendNotif(String msg) { 
		super.sendNotif(msg); //adaugarea FUNCTIONALITATII obiectului wrapped
		//OBLIGATORIU sa o pui mereu inainte sau dupa logica din obiectul curent 
		
		System.out.println("Sending " + msg + " by FACEBOOK");
	}
}

//CAZUL in care nu trm un LEAF-ELEM este tratat prin constructorul lui BASE-NOTIF-DECORATOR 
//deoarece el tot asteata un obj de tip INotif si nu poti lasa acel camp gol, 
//sau sa trm un string cu msg, acela find LEAF-ELEM
public class Main { 
	public static void main(String[] args) { 
		new FacebookDecorator( 
			new WhatsAppDecorator( 
				new Notifier ("HELLO$"))).sendNotif("from Alex"); 
	}
}