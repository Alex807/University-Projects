package org.example;

public class Car {
    private final String name;
    private final int horsePower;


    public Car(String name, int horsePower)
    {
        this.name = name;
        this.horsePower = horsePower;
    }

    public String getName()
    {
        return this.name;
    }

    public int getHorsePower()
    {
        return horsePower;
    }
}
