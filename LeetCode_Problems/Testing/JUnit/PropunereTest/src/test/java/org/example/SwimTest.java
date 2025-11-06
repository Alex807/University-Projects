package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwimTest {

    @Test
    void givenKmAreNegative_whenInstancedSwim_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Swim(-5,2));
    }

    @Test
    void givenKmAreZero_whenInstancedSwim_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Swim(0,2));
    }

    @Test
    void givenWaterTempIsNegative_whenInstancedSwim_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Swim(5,-2));
    }

    @Test
    void givenWaterTempIsOverThirty_whenInstancedSwim_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Swim(5, 32));
    }

    @Test
    void givenParametersAreValid_whenCalculateCalories_thenReturnValue() {
        var swim = new Swim(1, 20);

        assertEquals(30, swim.calculateCalories());
    }

}