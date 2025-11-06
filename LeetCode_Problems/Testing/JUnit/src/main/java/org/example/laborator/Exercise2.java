package org.example.laborator;

public class Exercise2 {
    private int day, month, year;

    public Exercise2(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getDays(Exercise2 other) {
        return daysSinceZero() - other.daysSinceZero();
    }

    private int daysSinceZero() {
        int[] daySums = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
        int d = day + daySums[month - 1] + year * 365 + year / 4 - year / 100 + year / 400;

        if ((year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) && month <= 2)
            d--;
        return d;
    }
}

