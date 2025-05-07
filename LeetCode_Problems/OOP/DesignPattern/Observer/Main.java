import java.util.*;

interface StockSubject { //SUBJECT interface	
	void registerObserver(StockObserver observer); 
	void removeObserver(StockObserver observer); 
	void notifyAllObservers();
}

interface StockObserver { //OBSERVER interface
	void update(String productName, int stockLevel); //putem modifica SEMNATURA metodei de update
}

class ProductStock implements StockSubject { //CONCRETE SUBJECT 
	private List<StockObserver> observers; 
	private String productName; 
	private int stockLevel; 
	
	public ProductStock(String productName, int stockLevel) { 
		this.productName = productName;
		this.stockLevel = stockLevel;
	}
	
	public void registerObserver(StockObserver observer) { 
		observers.add(observer);
	}
	
	public void removeObserver(StockObserver observer) { 
		observers.remove(observer);
	}
	
	public void notifyAllObservers() { 
		for (StockObserver current : observers) { 
			current.update(productName, stockLevel);
		}
	}
	
	public void updateStock(int newLevel) { 
		this.stockLevel = newLevel;
		
		notifyAllObservers();  //ALWAYS send update when object STATE CHANGED
	}
}

class StoreManager implements StockObserver { //CONCRETE OBSERVER
	private static final int REORDER_THRESHOLD = 10; 
	
	public void update(String productName, int stockLevel) { 
		if (stockLevel < REORDER_THRESHOLD) { 
			System.out.println("StoreManager has to reorder product");
		}
	}
}

class WebSiteFrontend implements StockObserver { 
	public update(String productName, int stockLevel) { 
		System.out.println("Notification is at WebDEV");
	}
}

public class Main { 
	public static void main(String[] args) { 
		System.out.println("MERGE");
	}
}









