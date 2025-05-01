package org.example;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BankAccountProcessor {
    private static BankAccountProcessor instance;
    private static final double yearlyInterestRate = 0.05;

    private BankAccountProcessor() {}

    public static BankAccountProcessor getInstance() {
        if (instance == null) {
            instance = new BankAccountProcessor();
        }
        return instance;
    }

    public void deposit(double amount, BankAccountDto account) {
        account.setBalance(account.getBalance() + amount);
    }

    public void withdraw(double amount, BankAccountDto account) {
        account.setBalance(account.getBalance() - amount);
    }

    public double findInterestForDate(BankAccountDto account, LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(account.getDateOfCreation(), date);
        if (daysBetween < 0) {
            System.out.println("Given date for interestRate is before account creation");
            return 0.0;
        }

        double interest = yearlyInterestRate / 365; // for leap years
        return daysBetween * interest * account.getBalance();
    }

    public void closeAccount(BankAccountDto account) {
        account.setBalance(account.getBalance() + findInterestForDate(account, LocalDate.now()));
        System.out.printf("After we closed the account with IBAN: %s balance after afferent interest is %.2f RON\n%n", account.getIBAN(), account.getBalance());
    }
}
