package org.example.laborator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Exercise1Test {

    @Test
    void givenValidNumber_whenStrToInt_thenReturnValuesAsInt() {
        var convertor = new Exercise1(); //arrange

        var value = convertor.strToInt("129"); //act

        assertEquals(129, value); //assert
    }

    @Test
    void givenEmptyString_whenStrToInt_thenReturnZero() {
        var convertor = new Exercise1(); //arrange

        var value = convertor.strToInt(""); //act

        assertEquals(0, value); //assert
    }

    @Test
    void givenInvalidString_whenStrToInt_thenReturnNumberFormatException() {
        var convertor = new Exercise1(); //arrange

        assertThrows(NumberFormatException.class, () ->  convertor.strToInt("123d")); //assert
    }
}