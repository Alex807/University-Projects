using System;
using Xunit;
using bankAccounts.modules;
using bankAccounts.exceptions;

namespace bankAccounts.tests
{
    public class BankTests { 
        private Bank CreateTestBank()
        {
            return new Bank("Test Bank", "TEST1234");
        }

        [Fact]
        public void CreateAccount_ShouldSucceed()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };
            bank.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);

            var account = bank.SearchAccountByIBAN("US123456789");
            Assert.NotNull(account);
            Assert.Equal(1000.00, account.GetBalance());
        }

        [Fact]
        public void CreateAccount_WhenAccountAlreadyExists_ShouldThrowException()
        {
            var bank1 = CreateTestBank();
            var bank2 = CreateTestBank();
            var createdBanks = new List<Bank> { bank1, bank2 };

            bank1.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);

            var exception = Assert.Throws<AccountAlreadyExistsException>(() =>
                bank2.CreateAccount("Jane Doe", AccountType.Person, "US123456789", 500.00, createdBanks));
            
        }

        [Fact]
        public void DepositCash_ValidAmount_ShouldIncreaseBalance()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };
            bank.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);
            bank.DepositCashToBank("US123456789", 500.00);

            var account = bank.SearchAccountByIBAN("US123456789");
            Assert.NotNull(account);
            Assert.Equal(1500.00, account.GetBalance());
        }

        [Fact]
        public void DepositCash_WhenNegativeAmount_ShouldThrowNegativeAmountException()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };
            bank.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);

            var exception = Assert.Throws<NegativeAmountException>(() =>
                bank.DepositCashToBank("US123456789", -100.00));

            Assert.Contains("Negative amounts or zero are INVALID to deposit!", exception.Message);
        }

        [Fact]
        public void DepositCash_WhenLargeAmount_ShouldIncreaseBalance()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };
            bank.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);
            bank.DepositCashToBank("US123456789", 1_000_000.00);

            var account = bank.SearchAccountByIBAN("US123456789");
            Assert.NotNull(account);
            Assert.Equal(1_001_000.00, account.GetBalance());
        }

        [Fact]
        public void WithdrawCash_ValidAmount_ShouldDecreaseBalance()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };
            bank.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);
            bank.WithdrawCashFromBank("US123456789", 500.00);

            var account = bank.SearchAccountByIBAN("US123456789");
            Assert.NotNull(account);
            Assert.Equal(500.00, account.GetBalance());
        }

        [Fact]
        public void WithdrawCash_WhenNegativeAmount_ShouldThrowNegativeAmountException()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };
            bank.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);

            var exception = Assert.Throws<NegativeAmountException>(() =>
                bank.WithdrawCashFromBank("US123456789", -100.00));

            Assert.Contains("Negative amounts or zero are INVALID to withdraw!", exception.Message);
        }

        [Fact]
        public void WithdrawCash_WhenLargeAmount_ShouldThrowInsufficientFundsException()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };
            bank.CreateAccount("John Doe", AccountType.Person, "US123456789", 1000.00, createdBanks);

            var exception = Assert.Throws<InsufficientFundsException>(() =>
                bank.WithdrawCashFromBank("US123456789", 1_000_000.00));

            Assert.Contains("Insufficient funds", exception.Message);
        }

        [Fact]
        public void TransferToAnotherAccount_Should_TransferAmount_WhenReceiverExists()
        {
            var bank1 = CreateTestBank();
            var bank2 = CreateTestBank();
            var createdBanks = new List<Bank> { bank1, bank2 };

            bank1.CreateAccount("John Doe", AccountType.Person, "IBAN123", 1000, createdBanks);
            bank2.CreateAccount("Jane Smith", AccountType.Person, "IBAN456", 500, createdBanks);

            bank1.TransferToAnotherAccount("IBAN123", createdBanks, "IBAN456", 200);

            var senderAccount = bank1.SearchAccountByIBAN("IBAN123");
            var receiverAccount = bank2.SearchAccountByIBAN("IBAN456");

            Assert.Equal(800, senderAccount?.GetBalance());
            Assert.Equal(700, receiverAccount?.GetBalance());
        }

        [Fact]
        public void TransferToAnotherAccount_Should_ThrowException_WhenSenderAccountNotFound()
        {
            var bank1 = CreateTestBank();
            var bank2 = CreateTestBank();
            var createdBanks = new List<Bank> { bank1, bank2 };

            bank2.CreateAccount("Jane Smith", AccountType.Person, "IBAN456", 500, createdBanks);

            Assert.Throws<AccountNotFoundException>(() =>
                bank1.TransferToAnotherAccount("IBAN999", createdBanks, "IBAN456", 200));
        }

        [Fact]
        public void TransferToAnotherAccount_Should_ThrowException_WhenReceiverAccountNotFound()
        {
            var bank1 = CreateTestBank();
            var createdBanks = new List<Bank> { bank1 };

            bank1.CreateAccount("John Doe", AccountType.Person, "IBAN123", 1000, createdBanks);

            Assert.Throws<AccountNotFoundException>(() =>
                bank1.TransferToAnotherAccount("IBAN123", createdBanks, "IBAN999", 200));
        }

        [Fact]
        public void TransferToAnotherAccount_Should_ThrowException_WhenSameSenderAndReceiver()
        {
            var bank = CreateTestBank();
            var createdBanks = new List<Bank> { bank };

            bank.CreateAccount("John Doe", AccountType.Person, "IBAN123", 1000, createdBanks);

            Assert.Throws<SameAccountException>(() =>
                bank.TransferToAnotherAccount("IBAN123", createdBanks, "IBAN123", 200));
        }

        [Fact]
        public void TransferToAnotherAccount_Should_ThrowException_WhenInsufficientFunds()
        {
            var bank1 = CreateTestBank();
            var bank2 = CreateTestBank();
            var createdBanks = new List<Bank> { bank1, bank2 };

            bank1.CreateAccount("John Doe", AccountType.Person, "IBAN123", 100, createdBanks);
            bank2.CreateAccount("Jane Smith", AccountType.Person, "IBAN456", 500, createdBanks);

            Assert.Throws<InsufficientFundsException>(() =>
                bank1.TransferToAnotherAccount("IBAN123", createdBanks, "IBAN456", 200));
        }

        [Fact]
        public void TransferToAnotherAccount_Should_ThrowException_WhenAmountIsZero()
        {
            var bank1 = CreateTestBank();
            var bank2 = CreateTestBank();
            var createdBanks = new List<Bank> { bank1, bank2 };

            bank1.CreateAccount("John Doe", AccountType.Person, "IBAN123", 1000, createdBanks);
            bank2.CreateAccount("Jane Smith", AccountType.Person, "IBAN456", 500, createdBanks);

            Assert.Throws<NegativeAmountException>(() =>
                bank1.TransferToAnotherAccount("IBAN123", createdBanks, "IBAN456", 0));
        }

        [Fact]
        public void TransferToAnotherAccount_Should_ThrowException_WhenAmountHasMoreThanTwoDecimals()
        {
            var bank1 = CreateTestBank();
            var bank2 = CreateTestBank();
            var createdBanks = new List<Bank> { bank1, bank2 };

            bank1.CreateAccount("John Doe", AccountType.Person, "IBAN123", 1000, createdBanks);
            bank2.CreateAccount("Jane Smith", AccountType.Person, "IBAN456", 500, createdBanks);

            Assert.Throws<HasTooManyDecimalsException>(() =>
                bank1.TransferToAnotherAccount("IBAN123", createdBanks, "IBAN456", 100.123));
        }
    }
}
