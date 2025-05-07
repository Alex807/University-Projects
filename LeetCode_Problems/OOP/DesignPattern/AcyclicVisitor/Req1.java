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
		return result + displayIcon() + displayColor();
	}
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

public class Req1 { 
	public static void main(String[] args) { 
		System.out.println("MERGE");
	}
}
