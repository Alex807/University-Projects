package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FunctionTest {

    private final Function function = new Function();

    @Test
    void calculate_whenBIsZero_shouldReturnMinusOne() {
        //arrange
        //act
        //assert
        int result = function.calculate(5, 0);
        assertEquals(-1, result);
    }

    @Test
    void calculate_whenAGreaterThanB_shouldReturnSum() {
        int result = function.calculate(10, 5);
        assertEquals(15, result);
    }

    @Test
    void calculate_whenALessThanB_shouldReturnDivisionResult() {
        int result = function.calculate(4, 8);
        // 4 / 8 = 0 in integer division
        assertEquals(0, result);
    }

    @Test
    void calculate_whenAEqualsB_shouldReturnDivisionResult() {
        int result = function.calculate(5, 5);
        // 5 / 5 = 1
        assertEquals(1, result);
    }

    @Test
    void calculate_whenNegativeNumbers_shouldHandleCorrectly() {
        int result = function.calculate(-10, 2);
        // a < b so return -10 / 2 = -5
        assertEquals(-5, result);
    }

    @Test
    void calculate_whenBothZeros_shouldHandleCorrectly() {
        int result = function.calculate(0, 0);
        // a < b so return -10 / 2 = -5
        assertEquals(-1, result);
    }
}
