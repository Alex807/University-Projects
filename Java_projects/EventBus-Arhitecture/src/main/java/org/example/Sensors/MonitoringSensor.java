package org.example.Sensors;

import org.greenrobot.eventbus.EventBus;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

class Sensor {
    private final String type;
    private static int temperatureSensorsCounter = 1;
    private static int humiditySensorsCounter = 1;
    private static int waterSensorsCounter = 1;

    public Sensor(String type) {
        this.type = type;
    }

    private String generateName() {
        if (type.equals("Temperature")) {
            return "TempSensor" + temperatureSensorsCounter++;

        } else if (type.equals("Humidity")) {
            return "HumiditySensor" + humiditySensorsCounter++;
        } else {
            return "WaterSensor" + waterSensorsCounter++;
        }
    }

    public void generateTemperatureData() {
        String[] cityLocations = {"Timisoara", "Arad", "Tg-Jiu", "Iasi", "Lugoj", "Turceni", "Caracal"};
        Random random = new Random();
        double temperature = random.nextDouble() * 100; // Simulate random sensor data
        int index = random.nextInt(cityLocations.length);

        EventBus.getDefault().post(new TemperatureEvent(cityLocations[index], temperature, generateName()));
    }

    public void generateHumidityData() {
        String[] placesInHome = {"kitchen", "living-room", "bedroom", "garage", "bathroom", "child room", "guest room"};
        Random random = new Random();
        double humidity = random.nextDouble() * 2; // Simulate random sensor data
        int index = random.nextInt(placesInHome.length);

        EventBus.getDefault().post(new HumidityEvent(placesInHome[index], humidity, generateName()));
    }

    public void generateWaterLevelData() {
        String[] rivers = {"Jiu", "Bega", "Mures", "Prut", "Olt", "Dunare", "Jilt"};
        Random random = new Random();
        double waterLevel = random.nextDouble() * 122; // Simulate random sensor data
        int index = random.nextInt(rivers.length);

        EventBus.getDefault().post(new WaterLevelEvent(rivers[index], waterLevel, generateName()));
    }
}

public class MonitoringSensor {
    public static void main(String[] args) {
        EventBus eventBus = EventBus.getDefault();

        // Create displays
        Display numericDisplay = new Display("Numeric");
        Display textDisplay = new Display("Text");
        Display maxValueDisplay = new Display("Max_Value");

        eventBus.register(numericDisplay);
        eventBus.register(textDisplay);
        eventBus.register(maxValueDisplay);

        // Create sensors
        Sensor tempSensor = new Sensor("Temperature");
        Sensor humiditySensor = new Sensor("Humidity");
        Sensor watherLevelSensor = new Sensor("Water_Level");


        // Simulate sensor data generation (only 5 times, every 3 seconds)
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            int count = 0; // Counter to track number of executions

            @Override
            public void run() {
                tempSensor.generateTemperatureData();
                humiditySensor.generateHumidityData();
                watherLevelSensor.generateWaterLevelData();

                count++;
                if (count >= 3) {
                    timer.cancel(); // Stop the timer after 5 executions
                }
            }
        };

        timer.schedule(task, 0, 3000); // Start immediately, run every 3 seconds

    }
}