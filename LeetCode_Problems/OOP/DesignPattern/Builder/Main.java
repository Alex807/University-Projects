class Car { //clasa ce dorim sa-i creem BUILDER
	private final int id; 
	private final String brand; 
	private final String model; 
	private final String color; 
	
	public Car (int id, String brand, String model, String color) { 
		this.id = id; 
		this.brand = brand; 
		this.model = model; 
		this.color = color;
	}
	
	public String toString() { 
		return String.format("Brand: %s Model: %s Color: %s\n", brand, model, color);
	}
}

class CarBuilder { //BUILDER
	private int id; 
	private String brand; //retinem toate atributele din obj ce-l vom creea
	private String model; 
	private String color;
	
	public CarBuilder() { //default CONSTRUCTOR for builder
	}
	
	public CarBuilder id(int id) { 
		this.id = id; 
		return this;
	}
	
	public CarBuilder brand(String brand) {
		this.brand = brand; 
		return this; //foarte important sa retrimiti obiectul pt urmatorul call
	} 
	
	public CarBuilder model(String model) { 
		this.model = model; 
		return this;
	}
	
	public CarBuilder color (String color) { 
		this.color = color; 
		return this;
	}
	
	public Car build() { //returneaza TIP-OBIECT ce il creem prin apel la constructor
		return new Car(id, brand, model, color);
	}
}


//---------------------------------------------OPTIONAL
class Director { //pentru a nu tot apela pasi in mod repetat, automatizezi calls aici
	
	public Director() { 
	}//trb instantiat pentru a-l folosi
	
	public void buildBugatti(CarBuilder builder) {//nu returnam nimic deoarece modif STAREA builderului primit
		 builder.brand("Bugatti") 
				.color("green")
				.model("Chiron")
				.id(112);
		//NU facem apel la metoda de BUILD aici, returnam builderul deja cu starea dorita
	}
	
	public void buildDacia(CarBuilder builder) { 
		builder.brand("DACIA") 
				.color("gray")
				.model("Logan")
				.id(1099);
	}
}


public class Main { 
	public static void main(String[] args) { 
		Director director = new Director(); 
		CarBuilder builder = new CarBuilder(); 
		
		director.buildBugatti(builder);
		Car car1 = builder.build();
		
		System.out.println(car1);
	}
}