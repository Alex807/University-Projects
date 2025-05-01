package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BankAccountDto { //in DTO type are allowed ONLY set/get on attributes, rest of the login in processor
    private String IBAN;
    private Person owner;
    private LocalDate dateOfCreation;
    private double balance;
    private static final String DATE_FORMAT = "dd-MM-yyyy"; //constantele(static final) le folosim drept publice ca sa le refolosim
    //static - camp comun pentru intreaga clasa
    //final - facem acel obiect IMUTABIL(o singura asignare'=' pe acel obiect)
    public BankAccountDto() {

    }

    public BankAccountDto(String IBAN, LocalDate dateOfCreation, double initialBalance, Person owner) {
        this.IBAN = IBAN;
        this.dateOfCreation = dateOfCreation;
        this.balance = initialBalance;
        this.owner = owner;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double amount) {
        this.balance = amount;
    }

    public LocalDate getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(LocalDate dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return String.format("Owner:\n\t%s \nIBAN: %s \nDateOfCreation: %s \nBalance: %.2f RON\n", owner, IBAN, dateOfCreation.format(formatter), balance);
    }
}
