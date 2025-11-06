package org.example;

import java.util.List;

public interface CarDatabase {
    List<Integer> getCarPowers();

    Car getCarByName(String name);

    void replaceCar(Car car);
}
