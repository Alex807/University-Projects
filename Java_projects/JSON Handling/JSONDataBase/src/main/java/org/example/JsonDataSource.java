package org.example;

import java.util.ArrayList;
import java.util.List;

public class JsonDataSource {
    private List<BankAccountDto> bankAccounts = new ArrayList<>();

    public List<BankAccountDto> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccountDto> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public void addBankAccount(BankAccountDto bankAccount) {
        bankAccounts.add(bankAccount);
    }

    public void removeBankAccount(BankAccountDto bankAccount) {
        bankAccounts.remove(bankAccount);
    }

    public String showAccounts() {
        StringBuilder result = new StringBuilder();
        for (BankAccountDto bankAccount : bankAccounts) {
            result.append(bankAccount.toString());
            result.append("\n");
        }
        return result.toString();
    }

    public BankAccountDto searchBankAccount(String IBAN) throws IllegalArgumentException {
        for (BankAccountDto bankAccount : bankAccounts) {
            if (bankAccount.getIBAN().equals(IBAN)) {
                return bankAccount;
            }
        }
        throw new IllegalArgumentException(String.format("Account with IBAN: %s not found!", IBAN));
    }
}
