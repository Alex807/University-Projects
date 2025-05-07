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

class Temperature extends WeatherInfoLayer implements MeteoMaticsLayersType {
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

class Wave extends WeatherInfoLayer implements MeteoMaticsLayersType {
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

class Wind extends WeatherInfoLayer implements MeteoMaticsLayersType{
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

//----------------------------------------------------------------------


abstract class thirdPartyAPI { //ACYCLIC VISITOR
	public abstract String collectWeatherData(LayersOfAPI config);
}

class WindyAPI extends thirdPartyAPI {
	private List<Wind> windLayers;
	
	public WindyAPI() { 
		this.windLayers = new ArrayList<>(); 
	}
	
	public void addWindLayer(Wind layer) { 
		windLayers.add(layer);
	}
	
	public List<Wind> getWindLayers() { 
		return windLayers;
	}
	
	public String collectWeatherData(LayersOfAPI config) { 
		String result = "";
		if (config instanceof WindyConfig) { 
			result = ((WindyConfig)config).collectData(this);
		}
		return result;
	}
}

class AccuWeatherAPI extends thirdPartyAPI { 
	private List<Temperature> tempLayers;
	
	public AccuWeatherAPI () { 
		this.tempLayers = new ArrayList<>();
	}
	
	public void addTempLayer(Temperature layer) { 
		tempLayers.add(layer);
	}
	
	public List<Temperature> getTempLayers() { 
		return tempLayers;
	}
	
	public String collectWeatherData(LayersOfAPI config) { 
		String result = "";
		if (config instanceof AccuWeatherConfig) { 
			result = ((AccuWeatherConfig)config).collectData(this);
		}
		return result;
	}
} 

interface MeteoMaticsLayersType { 
}

class MeteoMaticsAPI extends thirdPartyAPI { 
	private List<MeteoMaticsLayersType> layers; 
	
	public MeteoMaticsAPI () { 
		this.layers = new ArrayList<>();
	}
	
	public void addLayer(MeteoMaticsLayersType layer) { 
		layers.add(layer);
	}
	
	public List<MeteoMaticsLayersType> getLayers() { 
		return layers;
	}
	
	public String collectWeatherData(LayersOfAPI config) { 
		String result = "";
		if (config instanceof MeteoMaticsConfig) { 
			result = ((MeteoMaticsConfig)config).collectData(this);
		}
		return result;
	}
}

interface LayersOfAPI { 

}

interface WindyConfig extends LayersOfAPI{ //INTERFACE for EACH API type
	String collectData(WindyAPI api);
} 

interface AccuWeatherConfig extends LayersOfAPI { 
	String collectData(AccuWeatherAPI api);
}

interface MeteoMaticsConfig extends LayersOfAPI {  
	String collectData(MeteoMaticsAPI api);
}


class TemperatureFeature implements AccuWeatherConfig, MeteoMaticsConfig { 
	public String collectData(AccuWeatherAPI api) { 
		List<Temperature> apiLayers = api.getTempLayers();
		StringBuilder result = new StringBuilder();
		result.append("Temp_Feature in AccuWeatherAPI: \n");
		
		for (Temperature current : apiLayers) { 
			result.append(current.toString());
		}
		return result.toString();
	}
	
	public String collectData(MeteoMaticsAPI api) { 
		List<MeteoMaticsLayersType> apiLayers = api.getLayers();
		StringBuilder result = new StringBuilder();
		result.append("Temp_Feature in MeteoMaticsAPI: \n");
		
		for (MeteoMaticsLayersType current : apiLayers) { 
			result.append(current.toString());
		}
		return result.toString();
	}
}

class WindFeature implements WindyConfig, AccuWeatherConfig, MeteoMaticsConfig { 
	public String collectData(WindyAPI api) { 
		List<Wind> apiLayers = api.getWindLayers();
		StringBuilder result = new StringBuilder();
		result.append("Wind_Feature in WindyAPI: \n");
		
		for (Wind current : apiLayers) { 
			result.append(current.toString());
		}
		return result.toString();
	}
	
	public String collectData(AccuWeatherAPI api) { 
		List<Temperature> apiLayers = api.getTempLayers();
		StringBuilder result = new StringBuilder();
		result.append("Wind_Feature in AccuWeatherAPI: \n");
		
		for (Temperature current : apiLayers) { 
			result.append(current.toString());
		}
		return result.toString();
	}
	
	public String collectData(MeteoMaticsAPI api) { 
		List<MeteoMaticsLayersType> apiLayers = api.getLayers();
		StringBuilder result = new StringBuilder();
		result.append("Wind_Feature in MeteoMaticsAPI: \n");
		
		for (MeteoMaticsLayersType current : apiLayers) { 
			result.append(current.toString());
		}
		return result.toString();
	}
}

class WaveFeature implements MeteoMaticsConfig { 
	public String collectData(MeteoMaticsAPI api) { 
		List<MeteoMaticsLayersType> apiLayers = api.getLayers();
		StringBuilder result = new StringBuilder();
		result.append("Wave_Feature in MeteoMaticsAPI: \n");
		
		for (MeteoMaticsLayersType current : apiLayers) { 
			result.append(current.toString());
		}
		return result.toString();
	}
}


public class Req2 { 
	public static void main(String[] args) { 
		System.out.println("MERGE");
	}
}
