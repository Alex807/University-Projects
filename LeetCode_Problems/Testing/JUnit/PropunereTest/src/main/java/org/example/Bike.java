package org.example;

public class Bike {
    private int km;
    private int nrGear;

    public Bike(int km, int nrGear) {
        if(km <= 0) {
            throw new IncorrectValueException("The number of kilometers should be higher than 0");
        }
        if(nrGear <= 0) {
            throw new IncorrectValueException("The number of gears should be higher than 0");
        }
        this.km = km;
        this.nrGear = nrGear;
    }

    public int calculateCalories() {
        return this.km * this.nrGear * 10;
    }
}
