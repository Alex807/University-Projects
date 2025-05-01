package org.example.Sensors;

public class WaterLevelEvent {
    private double waterHeight;
    private String river;
    private final String usedSensorName;

    public WaterLevelEvent(String river, double waterHeight, String usedSensorName) {
        this.waterHeight = waterHeight;
        this.river = river;
        this.usedSensorName = usedSensorName;
    }

    public double getWaterHeight() {
        return waterHeight;
    }
    public String getRiver() {
        return river;
    }

    public String getUsedSensorName() {
        return usedSensorName;
    }
}
