package org.example.Sensors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

// Display class
public class Display {
    private final String displayType;

    public Display(String displayType) {
        this.displayType = displayType;
    }

    @Subscribe
    public void onTemperatureEvent(TemperatureEvent event) {
        double temperature = event.getValue();
        String data;
        if (temperature > 29){ data = "HOT";} else if(temperature > 20){ data = "WARM";} else { data = "COLD";}

        System.out.println(displayType +
                    String.format("_Display meteo changes: by '%s' in '%s' we have %.1f(Celsius) -> %s", event.getUsedSensorName(), event.getLocation(), temperature, data));
        if (displayType.equals("Max_Value")) System.out.println(); //for more clear output
    }

    @Subscribe
    public void onWaterLevelEvent(WaterLevelEvent event) {
        System.out.println(displayType + String.format("_Display water_lvl changes: by '%s' water height is %.1f cm on river '%s'", event.getUsedSensorName(), event.getWaterHeight(), event.getRiver()));
        if (displayType.equals("Max_Value")) System.out.println(); //for more clear output
    }

    @Subscribe
    public void onHumidityEvent(HumidityEvent event) {
        double humidity = event.getValue();
        String data;
        if (humidity > 1.4){ data = "HIGH RISK";} else { data = "NORMAL";}
        System.out.println(displayType + String.format("_Display humidity_lvl changes: by '%s' in '%s' we measured %.2f g/KG -> %s", event.getUsedSensorName(), event.getLocation(), event.getValue(), data));
        if (displayType.equals("Max_Value")) System.out.println(); //for more clear output
    }

    public void unregister() {
        EventBus.getDefault().unregister(this);
    }
}
