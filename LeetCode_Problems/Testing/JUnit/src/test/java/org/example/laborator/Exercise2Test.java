package org.example.laborator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Exercise2Test {

    private static Exercise2 date;

    @BeforeAll
    static void createReferenceDate() {
        date = new Exercise2(1, 1, 2020);
    }

    @Test
    void givenSameDate_whenGetDays_thenReturnZero () {
        var secondDate = new Exercise2(1, 1, 2020); //arrange

        var days = date.getDays(secondDate); //act

        assertEquals(0, days); //assert
    }

    @Test
    void givenMonthFebruaryAndYearDivisibleWith400_whenGetDays_thenReduceDaysByOne () {
        var secondDate = new Exercise2(1, 2, 2000); //arrange

        var days = date.getDays(secondDate); //act

        assertEquals(7274, days); //assert
    }

    @Test
    void givenMonthMarchAndYearDivisibleWith400_whenGetDays_thenReturnDaysWithoutDecrement () {
        var secondDate = new Exercise2(1, 3, 2000); //arrange

        var days = date.getDays(secondDate); //act

        assertEquals(7245, days); //assert
    }

    @Test
    void givenMonthMarchAndYearDivisibleWith4AndNot100_whenGetDays_thenReduceDaysByOne () {
        var secondDate = new Exercise2(1, 3, 2020); //arrange

        var days = date.getDays(secondDate); //act

        assertEquals(-60, days); //assert
    }

    @Test
    void givenMonthJanuaryAndYearDivisibleWith100_whenGetDays_thenReturnDaysWithoutDecrement () {
        var secondDate = new Exercise2(1, 1, 2300); //arrange

        var days = date.getDays(secondDate); //act

        assertEquals(-102268, days); //assert
    }

    @Test
    void givenMonthMarchAndYearDivisibleWith100_whenGetDays_thenReturnDaysWithoutDecrement () {
        var secondDate = new Exercise2(1, 3, 2301); //arrange

        var days = date.getDays(secondDate); //act

        assertEquals(-102692, days); //assert
    }

    @Test
    void given2DatesConsecutive_whenGetDays_thenReturnOne () {
        var secondDate = new Exercise2(2, 1, 2020); //arrange

        var days = date.getDays(secondDate); //act

        assertEquals(-1, days); //assert
    }

}