import java.util.*;

interface Box { //MAFIA-MEMBER (Base type ce este derivat de leaf/compose)
	double calculatePrice();
}//aici definesti operatii COMUNE pe complex/leaf BOX

class CompositeBox implements Box{ //MAFIA-HEAD contine alte obiecte in interior
	private final List<Box> children = new ArrayList<>(); 
	
	public CompositeBox(Box... boxes) { //nr VARIABIL de obiecte BOX ce pot fi primite
		children.addAll(Arrays.asList(boxes));
	} //ATENTIE AICI la ce folosesti sa le pui in LISTA

	public double calculatePrice() { 
		double sum = 0;
		for (Box current : children) { 
			sum += current.calculatePrice();
		}
		
		return sum;
	}
}

abstract class Product implements Box { //elem.FRUNZA continut de BOX ce face computatia(doar returneaza PRICE)
	private final String title; 
	private final double price; //FINAL pentru o singura atribuire
	
	public Product(String title, double price) { 
		this.title = title; 
		this.price = price;
	}
	
	public double calculatePrice() { 
		return price;
	}
}

class Tenis extends Product { //frunza CONCRETA
	public Tenis(String title, double price) { 
		super(title, price);
	}
}

class VideoGame extends Product { //frunza CONCRETA
	public VideoGame(String title, double price) { 
		super(title, price);
	}
}





public class Main { 
	public static void main (String[] args) { 
		System.out.println("MERGE");
	}
}