package org.example;

public class Run {
    private int km;

    public Run(int km) {
        if(km <= 0) {
            throw new IncorrectValueException("The number of kilometers should be higher than 0");
        }
        this.km = km;
    }

    public int calculateCalories() {
        return this.km * 100;
    }
}
