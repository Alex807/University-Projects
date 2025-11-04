package org.example;

public class Function {
    public int calculate(int a, int b) {
        if (b == 0) {
            return -1;
        }
        if (a > b) {
            return a + b;
        }
        return a / b;
    }
}

