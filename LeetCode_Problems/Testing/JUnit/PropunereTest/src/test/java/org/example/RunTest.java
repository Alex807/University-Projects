package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunTest {

    @Test
    void givenKmAreNegative_whenInstancedRun_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Run(-5));
    }

    @Test
    void givenKmAreZero_whenInstancedRun_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> new Run(0));
    }

    @Test
    void givenKmArePositive_whenCalculateCalories_thenReturnValue() {
        var run = new Run(12);

        assertEquals(1200, run.calculateCalories());
    }
}