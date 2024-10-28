using System;
using bankAccounts.exceptions;

namespace bankAccounts.modules  {  
    public enum AccountType { 
            Person, 
            Company
        }

    public class Account (string recievedAccountHolder, AccountType recievedType, string recievedIBAN, double recievedAmount) { 
        
        private string accountHolder = recievedAccountHolder; 
        private AccountType accountType = recievedType;
        private string IBAN = recievedIBAN; 
        private double balance = recievedAmount; 

        public string ShowAccountBalance() {   
            return "Account balance: " + balance + "RON\n\n";
        }

        private static bool HasMoreThanTwoDecimalPlaces(double number) {
            double fractionalPart = number - Math.Truncate(number); //obtain the fractional part of the number
            return Math.Round(fractionalPart * 100) != fractionalPart * 100; //if the number has more than 2 decimal places, the rounding will be different
        }

        public void DepositCash(double depositAmount) { 
            if (depositAmount <= 0) 
                throw new NegativeAmountException("Negative amounts or zero are INVALID to deposit! \n");
            
            else if (depositAmount >= 1_000_000_000_000_000 || depositAmount <= -1_000_000_000_000_000) //maxim to can be represented without exponential notation
                throw new TooLargeAmountException("Amount is too large for one single deposit !\n");

            else if (HasMoreThanTwoDecimalPlaces(depositAmount)) 
                throw new HasTooManyDecimalsException("Deposited amount has more than 2 decimal places, INVALID to deposit!\n");
            
            balance += depositAmount;
            Console.Write("Deposited " + depositAmount + "RON successfully in account with IBAN '" + IBAN + "'\n" + ShowAccountBalance());
        }

        public void WithdrawCash(double withdrawAmount) { 
            if (balance < withdrawAmount)  
                throw new InsufficientFundsException("Insufficient funds to withdraw " + withdrawAmount + "RON from account with IBAN '" + IBAN + "'\n" 
                                                    + ShowAccountBalance());               

            else if (withdrawAmount <= 0)  
                throw new NegativeAmountException("Negative amounts or zero are INVALID to withdraw! \n");
            
            else if (withdrawAmount >= 1_000_000_000_000_000 || withdrawAmount <= -1_000_000_000_000_000) 
                throw new TooLargeAmountException("Amount is too large for one single withdraw !\n"); 

            else if (HasMoreThanTwoDecimalPlaces(withdrawAmount))   
                throw new HasTooManyDecimalsException("Withdrawn amount has more than 2 decimal places, INVALID to withdraw!\n");
            
            balance -= withdrawAmount;
            Console.Write("Withdrawn " + withdrawAmount + "RON successfully from account with IBAN '" + IBAN + "'\n" + ShowAccountBalance());
        }

        public string GetIBAN() { 
            return IBAN;
        }   

        public double GetBalance() { 
            return balance;
        }

        public override string ToString () { 
            return "Account Holder: " + accountHolder + "\tAccount Type: " + accountType + "\tIBAN: " + IBAN + "\tBalance: " + balance + "RON\n";
        }

    }
}