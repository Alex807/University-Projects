package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BikeTest {

    @Test
    void givenKmAreNegative_whenInstancedBike_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Bike(-5,2));
    }

    @Test
    void givenKmAreZero_whenInstancedBike_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Bike(0,2));
    }

    @Test
    void givenNrOfGearsAreNegative_whenInstancedBike_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Bike(2,-2));
    }

    @Test
    void givenNrOfGearsAreZero_whenInstancedBike_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Bike(4,0));
    }

    @Test
    void givenParametersAreValid_whenCalculateCalories_thenReturnValue() {
        var bike = new Bike(5, 5);

        assertEquals(250, bike.calculateCalories());
    }

}