package org.example;

public class Swim {
    private int km;
    private int waterTemp;

    public Swim(int km, int waterTemp) {
        if(km <= 0) {
            throw new IncorrectValueException("The number of kilometers should be higher than 0");
        }
        if(waterTemp < 0 || waterTemp > 30) {
            throw new IncorrectValueException("The water temperature should be between 0 and 30");
        }
        this.km = km;
        this.waterTemp = waterTemp;
    }

    public int calculateCalories() {
        return this.km * 50 - this.waterTemp;
    }
}
