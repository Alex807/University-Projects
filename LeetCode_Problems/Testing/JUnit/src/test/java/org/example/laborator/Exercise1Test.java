package org.example.laborator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Exercise1Test {

    @Test
    void givenValidNumber_whenStrToInt_thenReturnsValuesAsInt() {
        var convertor = new Exercise1(); //arrange

        int value = convertor.strToInt("129"); //act

        assertEquals(129, value, "Test failed!"); //assert
    }

    @Test
    void givenEmptyString_whenStrToInt_thenReturnsZero() {
        var convertor = new Exercise1(); //arrange

        int value = convertor.strToInt(""); //act

        assertEquals(0, value, "Test failed!"); //assert
    }

    @Test
    void givenInvalidString_whenStrToInt_thenReturnNumberFormatException() {
        var convertor = new Exercise1(); //arrange

        assertThrows(NumberFormatException.class, () ->  convertor.strToInt("123d"),"Test failed!"); //assert
    }

//    @Test
//    void givenValidNumber_whenStrToInt_thenReturnsValuesAsInt() {
//        Exercise1 convertor = new Exercise1(); //arrange
//
//        int value = convertor.strToInt("129"); //act
//
//        assertEquals(129, value, "Test failed!"); //assert
//    }
//
//    @Test
//    void givenValidNumber_whenStrToInt_thenReturnsValuesAsInt() {
//        Exercise1 convertor = new Exercise1(); //arrange
//
//        int value = convertor.strToInt("129"); //act
//
//        assertEquals(129, value, "Test failed!"); //assert
//    }



}