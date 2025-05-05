import java.util.*;

abstract class ProductType { 
	private double price;
	
	public ProductType(double price) {
		this.price = price;
	}
	
	public abstract boolean canBePurchesed();
}

class DimensionData { 
	private double length;
	private double width;
	private double height; 
	
	public DimensionData(double length, double width, double height) { 
		this.length = length; 
		this.width = width; 
		this.height = height;
	}
}

class Customer { 
	private String fullName; 
	private String phoneNumberRO; 
	private String adress; 
	
	public Customer(String fullName, String phoneNumber, String adress) { 
		this.fullName = fullName; 
		this.phoneNumberRO = phoneNumber; 
		this.adress = adress;
	}
	
	public boolean isValid() { 
		String regEx = "^(\\+407|07)?[0-9]{8}$";
		if (!fullName.isEmpty() && !adress.isEmpty() && phoneNumberRO.matches(regEx)) return true; 
		return false;
	}
}

enum InventoryLevel { 
	NONE,
	MINIMUM, 
	MEDIUM, 
	MAXIMUM
}

enum PaymentMethods { 
	CASH, 
	PAYPAL, 
	VISA, 
	MASTERCARD
}

enum SubscriptionStatus { 
	ACTIVE, 
	INACTIVE
}

class PhysicalProduct extends ProductType { 
	private double weight; 
	private DimensionData dimension; 
	private InventoryLevel level;
	
	public PhysicalProduct(double price, double weight, DimensionData dimension, InventoryLevel level) { 
		super(price);
		this.weight = weight; 
		this.dimension = dimension; 
		this.level = level;
	}
	
	public boolean canBePurchesed() { 
		if (level != InventoryLevel."NONE")) return true; 
		return false;
	}
}

class DigitalDownload extends ProductType { 
	private String downloadLink; 
	private double sizeOfDownloadedFiles;
	
	public DigitalDownload(double price, String link, double sizeOfDownloadedFiles) { 
		super(price);
		this.downloadLink = link; 
		this.sizeOfDownloadedFiles = sizeOfDownloadedFiles;
	}
	
	public boolean canBePurchesed() { 
		if (downloadLink.startsWith("https")) return true; 
		return false;
	}
}

class Subscription extends ProductType { 
	private int dayOfMonthlyBilling;
	private SubscriptionStatus status;
	private String renewalTermsAndConditions;
	
	public Subscription(double price, int dayOfMonthlyBilling, String renewalTermsAndConditions) { 
		super(price);
		this.dayOfMonthlyBilling = dayOfMonthlyBilling; 
		this.renewalTermsAndConditions = renewalTermsAndConditions;
		this.status = SubscriptionStatus.ACTIVE;
	}
	
	public boolean canBePurchesed(){ 
		if(status == SubscriptionStatus.ACTIVE) return true; 
		return false;
	}
}

class Order { 
	private static int contorOfOrders = 0; 
	private final int orderID;
	private final Customer customer;
	private List<ProductType> productsOrdered; 
	private final PaymentMethods paymentMethod;
	
	public Order(Customer customer, PaymentMethods method) { 
		contorOfOrders++; 
		this.orderID = contorOfOrders;
		this.customer = customer;
		this.paymentMethod = method;
		this.productsOrdered = new ArrayList<ProductType>();
	}
	
	public void addProductToOrder(ProductType product) {
		productsOrdered.add(product);
	}
	
	public List<ProductType> getProductsOrdered() { 
		return productsOrdered;
	}
	
	public int getOrderID() { 
		return orderID;
	}
	
	public boolean isValid() { 
		if (!customer.isValid()) return false;
		for(ProductType current : productsOrdered) { 
			if (!current.canBePurchesed()) { 
				return false;
			}
		}
		return true;
	}
	
}

class OrdersProcessor { 
	private List<Order> ordersToBeProcessed; 
	private static OrdersProcessor instance = null;
	
	private OrdersProcessor() { 
		this.ordersToBeProcessed = new ArrayList<Order>();
	}
	
	public static OrdersProcessor getInstance() { 
		if (instance == null) { 
			instance = new OrdersProcessor();
		}
		return instance;
	}
	
	public void placeAnOrder(Order order) { 
		if (order.isValid()) { 
			ordersToBeProcessed.add(order);
		} else { 
			System.out.println(String.format("Order with ID %d is invalid!", order.getOrderID()));
		}
	}
	 
}

public class Main { 
	public static void main(String[] args) { 
		Customer mama = new Customer("mama", "+40763544184", "timisoara");
		System.out.println(mama.isValid());
		
		
	}
}