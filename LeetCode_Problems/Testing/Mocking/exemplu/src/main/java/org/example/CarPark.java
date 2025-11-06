package org.example;

import java.util.List;

public class CarPark {

    private CarDatabase carDb;


    public void setCarDatabase(CarDatabase carDb) {
        this.carDb = carDb;
    }
    public int getPowerOfNthCarInKw(int n)
    {
        List<Integer> carPowers = carDb.getCarPowers();
        if (carPowers.size() <= n) {
            throw new RuntimeException();
        }
        int carPower = carPowers.get(n);
        int carPowerInWatts = (int)(carPower * 0.746);
        return carPowerInWatts;
    }

    public double getTheAverageHorsePowerOfTheCarsThatHaveTheName(List<String> possibleNames) {
        int totalPower = 0;
        int numberOfCars = 0;

        for (String name : possibleNames) {
            Car car = carDb.getCarByName(name);

            if (car == null) {
                continue;
            }

            totalPower += car.getHorsePower();
            numberOfCars++;
        }

        if (numberOfCars == 0) {
            throw new RuntimeException();
        }

        return (double) totalPower / numberOfCars;

    }

    public boolean updateThePowerOfACarWithName(String name, int horsePower, List<String> carsInThePark) {

        Car car = carDb.getCarByName(name);

        if (car != null) {
            carDb.replaceCar(new Car(name, horsePower));
            return true;
        } else {
            throw new NullPointerException();
        }

    }





}
