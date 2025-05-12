import java.util.*;

abstract class WeatherInfoLayer {
    private int startTime;
    private int endTime;
    private double value;
    private String unitMeasure;

    protected WeatherInfoLayer(int startTime, int endTime, double value, String unitMeasure) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.value = value;
        this.unitMeasure = unitMeasure;
    }
	
	public abstract String displayIcon(); 
	public abstract String displayColor(); //TEMPLATE METHOD
	
    public final String toString() { //FINAL pt a nu lasa subclasele sa faca override pe aceasta metoda
        String result = String.format("Start time: %d \nEnd time: %d \nValue: %.2f \nUnit Measure: %s\n", startTime, endTime, value, unitMeasure);
		return result + displayIcon() + displayColor() + "\n";
	}
	
	public abstract void accept(APILayer visit);
}

enum ChancesOfRain{
    Zero,
    Low,
    Medium,
    High,
    VeryHigh
}

class Temperature extends WeatherInfoLayer {
    private double realFeel;
    private double realFeelShadow;
    private ChancesOfRain chancesOfRain;

    public Temperature(int startTime, int endTime, double value, String unitMeasure, double realFeel, double realFeelShadow, ChancesOfRain chancesOfRain) {
        super(startTime, endTime, value, unitMeasure);
        this.realFeel = realFeel;
        this.realFeelShadow = realFeelShadow;
        this.chancesOfRain = chancesOfRain;
    }

    public String displayIcon() {
        return String.format("Icon: %s(chances_to_rain)\n", chancesOfRain);
    }
	
	public String displayColor() { 
		return "Color: ORANGE";
	}
	
	public void accept(APILayer visitor) { 
		if (visitor instanceof TemperatureLayer) { 
			((TemperatureLayer)visitor).visit(this);
		}
	}
}

class Wave extends WeatherInfoLayer {
    private double degrees;
    private double waterTemperature;

    public Wave (int startTime, int endTime, double value, String unitMeasure, double degrees, double temperature) {
        super(startTime, endTime, value, unitMeasure);
        this.degrees = degrees;
        this.waterTemperature = temperature;
    }

	public String displayIcon() { 
		return "Icon: weather_logo\n";
	}

    public String displayColor() {
        return "Color: " +  (waterTemperature > 27.0 ? "RED\n" : "BLUE\n");
    }
	
	public void accept(APILayer visitor) { 
		if (visitor instanceof WaveLayer) {
			((WaveLayer)visitor).visit(this);
		}
	}
}

class Wind extends WeatherInfoLayer {
    private int degrees;

    public Wind(int startTime, int endTime, double value, String unitMeasure, int degrees) {
        super(startTime, endTime, value, unitMeasure);
        this.degrees = degrees;
    }

    public String displayIcon(){ 
		String direction;
		if (degrees < 45 || degrees > 315) {
			direction = "West <--";
		} else if (degrees >= 45 && degrees < 135) {
			direction = "North ^";
		} else if (degrees >= 135 && degrees < 225) {
			direction = "East -->";
		} else {
			direction = "South ~";
		}
        
        return String.format("Icon: %s \n", direction);
    }
	
	public String displayColor() { 
		return "Color: GRAY\n";
	}
	
	public void accept(APILayer visitor) {
		if (visitor instanceof WindLayer) { 
			((WindLayer)visitor).visit(this);
		}	
	}
}

class WeatherProvider {
    private List<WeatherInfoLayer> layerTotal = new ArrayList<WeatherInfoLayer>();

    public WeatherProvider (Wind windR, Wave waveR, Temperature tempR) {
        layerTotal.add(windR);
        layerTotal.add(waveR);
        layerTotal.add(tempR);
    }

    public String displayWeatherData() { //STRATEGY 
        StringBuilder result = new StringBuilder();
        for (WeatherInfoLayer currentLayer : layerTotal) {
            result.append(currentLayer.toString());
        }
        return result.toString();
    }
}

//----------------------------------------------------------------------


interface APILayer { //FEATURE interface in schema pdss 

}

//interfata pentru FIECARE TIP ce este necesar actiunii(layer-urile)
interface TemperatureLayer extends APILayer { 
	void visit(Temperature layer);
}

interface WaveLayer extends APILayer {
	void visit(Wave layer);
}

interface WindLayer extends APILayer { 
	void visit(Wind layer);
}

class AccuWeather implements TemperatureLayer { 
	public void visit(Temperature layer) { 
		System.out.println(layer.toString());
	}
}

class MeteoMatics implements TemperatureLayer, WaveLayer, WindLayer { 
	public void visit(Temperature layer) { 
		System.out.println(layer.toString());
	}
	
	public void visit(Wind layer) { 
		System.out.println(layer.toString());
	}
	
	public void visit(Wave layer) { 
		System.out.println(layer.toString());
	}
}

class Windy implements WindLayer {
	public void visit(Wind layer) { 
		System.out.println(layer.toString());
	}
}

public class Req2 {
    public static void main(String[] args) {
        // Create weather layer instances
        Wind wind = new Wind(8, 12, 25.5, "km/h", 180);
        Wave wave = new Wave(8, 12, 1.5, "m", 45.0, 28.5);
        Temperature temp = new Temperature(8, 12, 30.0, "°C", 32.0, 28.0, 
            ChancesOfRain.Medium);

        // Create weather provider and display data
        System.out.println("=== Weather Provider Test ===");
        WeatherProvider provider = new WeatherProvider(wind, wave, temp);
        System.out.println(provider.displayWeatherData());

        // Test different API providers
        System.out.println("\n=== API Providers Test ===");
        
        System.out.println("AccuWeather (Temperature only):");
        AccuWeather accuWeather = new AccuWeather();
        temp.accept(accuWeather);

        System.out.println("\nWindy (Wind only):");
        Windy windy = new Windy();
        wind.accept(windy);

        System.out.println("\nMeteoMatics (All layers):");
        MeteoMatics meteoMatics = new MeteoMatics();
        temp.accept(meteoMatics);
        wave.accept(meteoMatics);
        wind.accept(meteoMatics);
    }
}
