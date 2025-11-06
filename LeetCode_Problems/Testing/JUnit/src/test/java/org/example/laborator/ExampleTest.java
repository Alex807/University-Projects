package org.example.laborator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExampleTest {

    private Example function;

    @BeforeEach
    void createInstance() {
        function = new Example();
    }

    @Test
    void calculate_whenBIsZero_shouldReturnMinusOne() {
        var result = function.calculate(5, 0);
        assertEquals(-1, result);
    }

    @Test
    void calculate_whenAGreaterThanB_shouldReturnSum() {
        var result = function.calculate(10, 5);
        assertEquals(15, result);
    }

    @Test
    void calculate_whenALessThanB_shouldReturnDivisionResult() {
        var result = function.calculate(4, 8);
        assertEquals(0, result);
    }

    @Test
    void calculate_whenAEqualsB_shouldReturnDivisionResult() {
        var result = function.calculate(5, 5);
        assertEquals(1, result);
    }

    @Test
    void calculate_whenNegativeNumbers_shouldHandleCorrectly() {
        var result = function.calculate(-10, 2);
        assertEquals(-5, result);
    }
}
