package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class BankApplicationLogic {
    public static final String JSONDataBase_PATH = "E:\\JOB\\BankAccountDB.json";

    public void run() {
        Scanner scanner = new Scanner(System.in);
        BankAccountProcessor processor = BankAccountProcessor.getInstance();

        // Initialize JSON handling
        Path jsonPath = Path.of(JSONDataBase_PATH);
        if (!jsonPath.toFile().exists()) {
            System.out.println("JSON DataBase not found!\nCurrent path that is searched is: " + JSONDataBase_PATH);
            System.exit(0);
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonDataSourceProcessor jsonProcessor = new JsonDataSourceProcessor(jsonPath, mapper);

        // Load existing data
        JsonDataSource source;
        try {
            source = jsonProcessor.read();
            System.out.println("Existing accounts loaded successfully.");
        } catch (Exception e) {
            source = new JsonDataSource();
            System.out.println("No existing accounts found. Starting with empty database.");
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== Bank Account Management System ===");
            System.out.println("1. Create Bank Account");
            System.out.println("2. Calculate Interest");
            System.out.println("3. Close Bank Account");
            System.out.println("4. List Bank Accounts");
            System.out.println("5. Deposit to Account");
            System.out.println("6. Withdraw from Account");
            System.out.println("7. Transfer between Accounts");
            System.out.println("8. Save all changes");
            System.out.println("9. Exit");
            System.out.print("Enter your choice (1-9): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    // Create new bank account
                    System.out.println("\n=== Creating New Bank Account ===");
                    System.out.print("Enter person name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter person age: ");
                    int age = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    System.out.print("Enter IBAN: ");
                    String accountNumber = scanner.nextLine();
                    System.out.print("Enter initial balance: ");
                    double balance = scanner.nextDouble();

                    Person person = new Person(name, age);
                    BankAccountDto account = new BankAccountDto(
                            accountNumber,
                            LocalDate.now(),
                            balance,
                            person
                    );

                    source.addBankAccount(account);
                    System.out.println("Account created successfully!");
                    break;

                case 2:
                    System.out.println("\n=== Calculating Interest ===");
                    System.out.print("Enter IBAN of account: ");
                    String accountForInterest = scanner.nextLine();

                    System.out.print("Date to calculate interest (dd-mm-yyyy): ");
                    String dateAsString = scanner.nextLine();
                    try { //muta codul in metode si aici doar call them in try-catch blocks
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        LocalDate dateInterest = LocalDate.parse(dateAsString, formatter);
                        BankAccountDto subjectAccount = source.searchBankAccount(accountForInterest);
                        double interestValue = processor.findInterestForDate(subjectAccount, dateInterest);
                        System.out.printf("Interest value = %.2f RON\n%n", interestValue);

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case 3:
                    // Close bank account
                    System.out.println("\n=== Closing Bank Account ===");
                    System.out.print("Enter account number to close: ");
                    String accountToClose = scanner.nextLine();

                    try {
                        BankAccountDto searchedAccount = source.searchBankAccount(accountToClose);
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case 4:
                    // Show all accounts
                    System.out.println("\n=== All Bank Accounts ===");
                    System.out.println(source.showAccounts());
                    break;

                case 5:
                    // Deposit money
                    System.out.println("\n=== Deposit Money ===");
                    System.out.print("Enter IBAN: ");
                    String depositAccount = scanner.nextLine();
                    try {
                        BankAccountDto accountForDeposit = source.searchBankAccount(depositAccount);
                        System.out.print("Enter amount to deposit: ");
                        double depositAmount = scanner.nextDouble();
                        processor.deposit(depositAmount, accountForDeposit);
                        System.out.println("Deposit successful!");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case 6:
                    // Withdraw money
                    System.out.println("\n=== Withdraw Money ===");
                    System.out.print("Enter IBAN: ");
                    String withdrawAccount = scanner.nextLine();
                    try {
                        BankAccountDto accountForWithdraw = source.searchBankAccount(withdrawAccount);
                        System.out.print("Enter amount to withdraw: ");
                        double withdrawAmount = scanner.nextDouble();
                        processor.withdraw(withdrawAmount, accountForWithdraw);
                        System.out.println("Withdrawal successful!");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case 7:
                    // Transfer between accounts
                    System.out.println("\n=== Transfer Between Accounts ===");
                    System.out.print("Enter source IBAN: ");
                    String sourceAccount = scanner.nextLine();
                    System.out.print("Enter destination IBAN: ");
                    String destAccount = scanner.nextLine();

                    try {
                        BankAccountDto fromAccount = source.searchBankAccount(sourceAccount);
                        BankAccountDto toAccount = source.searchBankAccount(destAccount);

                        System.out.print("Enter amount to transfer: ");
                        double transferAmount = scanner.nextDouble();

                        processor.withdraw(transferAmount, fromAccount);
                        processor.deposit(transferAmount, toAccount);
                        System.out.println("Transfer successful!");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case 8:
                    // Save all changes to JSON before exiting
                    try {
                        jsonProcessor.write(source);
                        System.out.println("Changes saved successfully!");
                    } catch (Exception e) {
                        System.out.println("Error saving changes: " + e.getMessage());
                    }
                    break;

                case 9:
                    // Exit program
                    running = false;
                    System.out.println("Thank you for using Bank Account Management System!");
                    break;

                default:
                    System.out.println("Invalid choice! Please enter a number between 1 and 4.");
            }
        }

        scanner.close();
    }
}
