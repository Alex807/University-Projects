using System;
using Xunit;
using bankAccounts.modules;
using bankAccounts.exceptions;

namespace bankAccounts.tests
{
    public class AccountTests
    {
        [Fact]
        public void Test_WithdrawCash_SufficientFunds()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            account.WithdrawCash(200.00);
            Assert.Equal(800.00, account.GetBalance());
        }

        [Fact]
        public void Test_WithdrawCash_InsufficientFunds()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 100.00);
            var exception = Assert.Throws<InsufficientFundsException>(() => account.WithdrawCash(200.00));
            Assert.Contains("Insufficient funds", exception.Message);
            Assert.Equal(100.00, account.GetBalance()); // Verificăm că soldul nu s-a schimbat
        }

        [Fact]
        public void Test_WithdrawCash_NegativeOrZeroAmount()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<NegativeAmountException>(() => account.WithdrawCash(-100.00));
            Assert.Contains("Negative amounts or zero are INVALID to withdraw!", exception.Message);
            Assert.Equal(1000.00, account.GetBalance()); // Verificăm că soldul nu s-a schimbat
        }

        [Fact]
        public void Test_DepositCash_Success()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 500.00);
            account.DepositCash(200.00);
            Assert.Equal(700.00, account.GetBalance());
        }

        [Fact]
        public void Test_DepositCash_NegativeOrZeroAmount()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<NegativeAmountException>(() => account.DepositCash(-100.00));
            Assert.Contains("Negative amounts or zero are INVALID to deposit!", exception.Message);
            Assert.Equal(1000.00, account.GetBalance()); // Verificăm că soldul nu s-a schimbat
        }

        [Fact]
        public void Test_WithdrawCash_NegativeBalanceAfterWithdrawal()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 50.00);
            var exception = Assert.Throws<InsufficientFundsException>(() => account.WithdrawCash(100.00));
            Assert.Contains("Insufficient funds", exception.Message);
            Assert.Equal(50.00, account.GetBalance()); // Verificăm că soldul nu s-a schimbat
        }

        [Fact]
        public void Test_DepositCash_SuccessWithMultipleDeposits()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 500.00);
            account.DepositCash(200.00);
            account.DepositCash(300.00);
            Assert.Equal(1000.00, account.GetBalance());
        }

        [Fact]
        public void Test_WithdrawCash_SuccessWithMultipleWithdrawals()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            account.WithdrawCash(200.00);
            account.WithdrawCash(300.00);
            Assert.Equal(500.00, account.GetBalance());
        }

        [Fact]
        public void Test_WithdrawCash_ValidDecimalPrecision()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            account.WithdrawCash(100.25); // Modificat pentru a avea 2 zecimale
            Assert.Equal(899.75, account.GetBalance()); // Verificăm soldul cu 2 zecimale
        }

        [Fact]
        public void Test_DepositCash_ValidDecimalPrecision()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            account.DepositCash(100.25); // Modificat pentru a avea 2 zecimale
            Assert.Equal(1100.25, account.GetBalance()); // Verificăm soldul cu 2 zecimale
        }

        [Fact]
        public void Test_WithdrawCash_ExceedingDecimalPrecision()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<HasTooManyDecimalsException>(() => account.WithdrawCash(100.256)); // Modificat pentru a depăși 2 zecimale
            Assert.Contains("Withdrawn amount has more than 2 decimal places, INVALID to withdraw!\n", exception.Message);
            Assert.Equal(1000.00, account.GetBalance()); // Verificăm că soldul nu s-a schimbat
        }

        [Fact]
        public void Test_DepositCash_ExceedingDecimalPrecision()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<HasTooManyDecimalsException>(() => account.DepositCash(100.256)); // Modificat pentru a depăși 2 zecimale
            Assert.Contains("Deposited amount has more than 2 decimal places, INVALID to deposit!\n", exception.Message);
            Assert.Equal(1000.00, account.GetBalance()); // Verificăm că soldul nu s-a schimbat
        }

        [Fact]
        public void Test_CreateAccount_NonZeroBalance()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            Assert.Equal(1000.00, account.GetBalance());
        }

        [Fact]
        public void Test_WithdrawCash_NegativeOrZeroAmount_NotAllowed()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<NegativeAmountException>(() => account.WithdrawCash(-100.00));
            Assert.Contains("Negative amounts or zero are INVALID to withdraw!", exception.Message);
            Assert.Equal(1000.00, account.GetBalance());
        }

        [Fact]
        public void Test_DepositCash_NegativeOrZeroAmount_NotAllowed()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<NegativeAmountException>(() => account.DepositCash(-100.00));
            Assert.Contains("Negative amounts or zero are INVALID to deposit!", exception.Message);
            Assert.Equal(1000.00, account.GetBalance());
        }

        [Fact]
        public void Test_WithdrawCash_ExceedingDecimalPrecision_NotAllowed()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<HasTooManyDecimalsException>(() => account.WithdrawCash(100.256));
            Assert.Contains("Withdrawn amount has more than 2 decimal places, INVALID to withdraw!", exception.Message);
            Assert.Equal(1000.00, account.GetBalance());
        }

        [Fact]
        public void Test_DepositCash_ExceedingDecimalPrecision_NotAllowed()
        {
            var account = new Account("John Doe", AccountType.Person, "US123456789", 1000.00);
            var exception = Assert.Throws<HasTooManyDecimalsException>(() => account.DepositCash(100.256));
            Assert.Contains("Deposited amount has more than 2 decimal places, INVALID to deposit!", exception.Message);
            Assert.Equal(1000.00, account.GetBalance());
        }

        

        
    }
}