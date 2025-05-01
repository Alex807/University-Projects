package org.example.Sensors;

public class HumidityEvent {
    private final String location;
    private double value;
    private final String usedSensorName;

    public HumidityEvent(String location, double value, String usedSensorName) {
        this.location = location;
        this.value = value;
        this.usedSensorName = usedSensorName;
    }

    public String getLocation() {
        return location;
    }

    public String getUsedSensorName() {
        return usedSensorName;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
