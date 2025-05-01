package org.example.Sensors;

// Event class for sensor data
public class TemperatureEvent {
    private final String location;
    private double value;
    private final String usedSensorName;

    public TemperatureEvent(String location, double value, String usedSensorName) {
        this.location = location;
        this.value = value % 35; //to have more realistic data
        this.usedSensorName = usedSensorName;
    }

    public String getLocation() {
        return location;
    }

    public double getValue() {
        return value;
    }

    public String getUsedSensorName() {
        return usedSensorName;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
