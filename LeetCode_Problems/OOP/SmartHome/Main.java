import java.util.*;

interface DeviceObserver { //interfata generica OBSERVER
	void update(SmartDevice device, String event);
}

interface DeviceDetectionMode { 
	List<SmartDevice> discoverDevices();
}

class WifiDetection implements DeviceDetectionMode{ 
	public List<SmartDevice> discoverDevices() { 
		//implementare
	}
}

class ZigbeeDetection implements DeviceDetectionMode { 
	public List<SmartDevice> discoverDevices() { 
		//implementare
	}
} 

abstract class SmartDevice { //SUBJECT for observer pattern
	private String ID; 
	private String name; 
	private boolean isConnected; 
	private List<DeviceObserver> observers = new ArrayList<>();
	
	public SmartDevice(String id, String name) { 
		this.ID = id; 
		this.name = name; 
		isConnected = false; //initial all are disconencted
	}
	
	public void addObserver(DeviceObserver observer) { 
		observers.add(observer);
	}
	
	public void removeObserver(DeviceObserver observer) { 
		observers.remove(observer);
	}
	
	protected void notifyAllObservers(String event) { //o facem PROTECTED ca sa o apelam doar din subclase 
		for (DeviceObserver current : observers) { 
			current.update(this, event);
		}
	}
	
	public String getID() { return ID; } 
	public String getName() { return name; } 
	public boolean itIsConnected() { return isConnected; }
	public void setConnected(boolean connected) { 
		this.isConnected = connected;
	}
	
	public abstract boolean turnOn(); 
	public abstract boolean turnOff(); 
	public abstract String getStatus();
}

class SmartDeviceFactory { //Factory Method Pattern
	public static SmartDevice createDevice(String type, String id, String name, Map<String, Object> propertiesNeeded) { 
		switch(type) { 
			case "light": return new SLight(id, name, propertiesNeeded); 
			case "thermostat": return new SThermostat(id, name, propertiesNeeded); 
			case "camera": return new SSecurityCamera(id, name, propertiesNeeded); 
			case "doorlock": return new SDoorLock(id, name, propertiesNeeded); 
			default: throw new IllegalArgumentException("Unknown device type: " + type);
		}
	}
}

enum LightColor { 
	RED, 
	BLUE, 
	WHITE, 
	ORANGE,
	GREEN
}

enum ThermostatMode { 
	HEAT, 
	COOL, 
	AUTO
}

class AccessCode { //we work with code with 5 digits
	private int nrOfAvailableUsages; //an code is one time only generated
	private final int code; 
	
	public AccessCode(int nrOfUsages) { 
		this.nrOfAvailableUsages = nrOfUsages;		
		this.code = generateCode();
	}
	
	private int generateCode() { 
		Random r = new Random(); 
		return r.nextInt(100000);
	}
	
	public int getNrOfUsages() { 
		return nrOfAvailableUsages;
	}
	
	public void decreaseTokens() { 
		nrOfAvailableUsages--;
	}
	
	public int getCode() { 
		return code;
	}
	
}

class SLight extends SmartDevice{ 
	private double brightnessLevel; 
	private LightColor color; 
	private boolean isOn;
	
	public SLight(String id, String name, Map<String, Object> properties) { 
		super(id, name); 
		this.brightnessLevel = (Double) properties.getOrDefault("brightness", 100.0);
        this.color = (LightColor) properties.getOrDefault("color", LightColor.WHITE);
        this.lightIsOn = false;
	}
	
	public boolean turnOn() { 
		isOn = true; 
		notifyAllObservers("turned ON");
		return true;
	}
	
	public boolean turnOff()  { 
		isOn = false; 
		notifyAllObservers("turned OFF"); //apel catre metoda din superclasa ce este 
										//de tip OBSERVER
	}
	
	public void setBroghtness(double value) { 
		this.brightnessLevel = value; 
		notifyAllObservers("Brightness-CHANGES");
	}
	
	public String getStatus() { 
		return "Light: " + (lightIsOn ? "ON" : "OFF") + 
               ", Brightness: " + brightnessLevel + 
               ", Color: " + color;
	}
}

class SThermostat extends SmartDevice{ 
	private double startingTemp; 
	private ThermostatMode runMode; 
	private int hourScheduleToStart;
}

class SSecurityCamera extends SmartDevice{ 
	private double resolution; 
	private boolean motionWasDetected;
	private String recordingOption;
}

abstract class User { 
	protected final String fullName; 
	protected AccessCode code;
	
	public User(String fullName, AccessCode code) { 
		this.fullName = fullName; 
		this.code = code;
	}
	
	public abstract void consumeUsages();
}

class PermanentAccessUser extends User{ 
	public PermanentAccessUser(String fullName) { 
		super(fullName, new AccessCode(1)); //the usage is always the same because in permanent we do not modify it after an usage
	}
	
	public void consumeUsages() {} //for permanent usser we do nothing
}

class TemporaryAccessUser extends User{ 
	public TemporaryAccessUser(String fullName, int nrOfUsages) { 
		super(fullName, new AccessCode(nrOfUsages));
	}
	
	public void consumeUsages() { 
		code.decreaseTokens();  //we apply -1 for each call
	}
	
}

class SDoorLock extends SmartDevice{ 
	private boolean doorIsLooked; 
	private List<User> usersWithAccess;
	
}

class SmartHome { 
	private List<SmartDevice> devices; 
	
	
}

public class Main { 
	public static void main(String[] args) { 
		System.out.println("MERGE");
	}
}