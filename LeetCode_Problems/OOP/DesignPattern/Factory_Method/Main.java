/*class Restaurant { 
	public Restaurant() { 
	}//constructor normal

	public Burger orderBurger(String type, int kcal) { 
		Burger order = BurgerFactory.createBurger(type, kcal); 
		order.cook(); //prelucram obiectul(LOGICA codului)
		
		return order;
	}
	
}

abstract class Burger { 
	private int kcalForServing;
	
	protected Burger(int kcalForServing) { //fol. PROTECTED pt constructorii superclasei pt. a evidentia apelarea din subclase
		this.kcalForServing = kcalForServing;
	}
	
	public abstract void cook();
}

class BeefBurger extends Burger{ 
	public BeefBurger(int kcalForServing) { 
		super(kcalForServing);
	}
	
	public void cook() { 
		System.out.println("Meal is ready!");
	}
}

class ChickenBurger extends Burger{ 
	public ChickenBurger(int kcalForServing) { 
		super(kcalForServing);
	}
	
	public void cook() { 
		//override in fiecare subclasa cu pasii specifici ei 
	}
}


class BurgerFactory { 
	public static Burger createBurger(String burgerType, int kcalForServing) {
		Burger burger = null; 
		if (burgerType.equalsIgnoreCase("BEEF")) { 
			burger = new BeefBurger(kcalForServing);
		
		} else if (burgerType.equalsIgnoreCase("CHICKEN")) { 
			burger = new ChickenBurger(kcalForServing);
		}
		return burger;
	}
}*/

//--------------versiunea de mai SUS este un simple FACTORY(avem o clasa cu o metoda ce foloseste IF...)---------


/*abstract class Restaurant { //MUTAM metoda de creeare a fiecarui subtip aici
	public Restaurant() { 
	}//constructor normal

	public Burger orderBurger(int kcal) { 
		Burger order = createBurger( kcal); 
		order.cook(); //prelucram obiectul(LOGICA codului)
		
		return order;
	}

	public abstract Burger createBurger(int kcal); //FACTORY-METHOD ce va fi implementat in SUBCLASE
}
//diferenta FUNDAMENTALA intre versiunea de sus este ca aici delegam cate UN ALT SUBTIP
// ce va prelucra obiectele sa le creeze el, fara IF si clasa FACTORY

class BeefBurgerRestaurant extends Restaurant {
	public BeefBurgerRestaurant() { 
	}//constructor
	
	public Burger createBurger(int kcal) { 
		return new BeefBurger(kcal);
	}
//singurul ROL al acestor subclase este de a face override pe metoda de FACTORY si a creea obiectul necesar
	
}

class ChickenBurgerRestaurant extends Restaurant {
	public ChickenBurgerRestaurant() { 
	}//constructor
	
	public Burger createBurger(int kcal) { 
		return new ChickenBurger(kcal);
	}
	
}

abstract class Burger { 
	private int kcalForServing;
	
	protected Burger(int kcalForServing) { //fol. PROTECTED pt constructorii superclasei pt. a evidentia apelarea din subclase
		this.kcalForServing = kcalForServing;
	}
	
	public abstract void cook();
}

class BeefBurger extends Burger{ 
	public BeefBurger(int kcalForServing) { 
		super(kcalForServing);
	}
	
	public void cook() { 
		System.out.println("Meal is ready!");
	}
}

class ChickenBurger extends Burger{ 
	public ChickenBurger(int kcalForServing) { 
		super(kcalForServing);
	}
	
	public void cook() { 
		//override in fiecare subclasa cu pasii specifici ei 
	}
}*/

//-------------------versiunea de SUS este FACTORY-METHOD aplicat o singura data----- 

abstract class Restaurant { 
	public Restaurant() { 
	}

	public Burger orderBurger(int kcal) { 
		Burger order = createBurger( kcal); 
		order.cook(); //prelucram obiectul(LOGICA codului)
		
		return order;
	}
	
	public Pasta orderPasta(String paramUsed) { 
		Pasta order = createPasta(paramUsed); 
		order.cook(); //prelucram obiectul(LOGICA codului)
		
		return order;
	}

	public abstract Burger createBurger(int kcal); //FACTORY-METHOD pentru un TIP de produs

	public abstract Pasta createPasta(String param); //FACTORY-METHOD
}

class BeefRestaurant extends Restaurant {
	public BeefRestaurant() { 
	}//constructor
	
	public Burger createBurger(int kcal) { 
		return new BeefBurger(kcal);
	}
//singurul ROL al acestor subclase este de a face override pe metodele de FACTORY si a creea obiectul necesar
	
	public Pasta createPasta(String param) { 
		return new BeefPasta(param);
	}
}

class ChickenRestaurant extends Restaurant {
	public ChickenRestaurant() { 
	}//constructor
	
	public Burger createBurger(int kcal) { 
		return new ChickenBurger(kcal);
	}
//singurul ROL al acestor subclase este de a face override pe metodele de FACTORY si a creea obiectul necesar
	
	public Pasta createPasta(String param) { 
		return new ChickenPasta(param);
	}
}

abstract class Burger { //sau INTERFACE daca nu ai cod de factorizat
	private int kcalForServing;
	
	public Burger(int kcalForServing) {
		this.kcalForServing = kcalForServing;
	}
	
	public abstract void cook();
}

class BeefBurger extends Burger{ 
	public BeefBurger(int kcalForServing) { 
		super(kcalForServing);
	}
	
	public void cook() { 
		System.out.println("Meal is ready!");
	}
}

class ChickenBurger extends Burger{ 
	public ChickenBurger(int kcalForServing) { 
		super(kcalForServing);
	}
	
	public void cook() { 
		//override in fiecare subclasa cu pasii specifici ei 
	}
}

//adaugam mai multe tipuri ce au in comun CEVA(aici de ex. au tipul de carne folosita)
abstract class Pasta { //sau INTERFACE daca nu ai cod de factorizat
	private String justAParam;
	
	public Pasta(String param) { 
		this.justAParam = param;
	}
	
	public abstract void cook();
}

class BeefPasta extends Pasta{ 
	public BeefPasta(String param) { 
		super(param);
	}
	
	public void cook() { 
		System.out.println("Meal is ready!");
	}
}

class ChickenPasta extends Pasta{ 
	public ChickenPasta(String param) { 
		super(param);
	}
	
	public void cook() { 
		//override in fiecare subclasa cu pasii specifici ei 
	}
}

public class Main { 
	public static void main(String[] args) {
		Restaurant beef = new BeefRestaurant(); 
		Burger beefBurger = beef.orderBurger(100);
		Pasta beefPasta = beef.orderPasta("carbonara");  //faci CALL pe metode din CLASA DE BAZA
												//(nu din subclase unde doar creezi produsul pe care aplici logica din Restaurant)
		
		
		Restaurant chicken = new ChickenRestaurant(); 
		Burger chickenBurger = chicken.orderBurger(350); 
		Pasta chickenPasta = chicken.orderPasta("matriciana");
	}
}