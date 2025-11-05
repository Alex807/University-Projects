package org.example.laborator;

public class Exercise1 {
    public  int strToInt(String str) throws NumberFormatException {
        int result = 0;
        int stringSize = str.length();

        for (int i = 0; i < stringSize; i++) {
            char character = str.charAt(stringSize - i - 1);
            if (Character.isDigit(character))
                result += (character - '0') * (int) Math.pow(10, i);
            else
                throw new NumberFormatException();
        }
        return result;
    }
}
