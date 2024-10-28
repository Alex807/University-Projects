using bankAccounts.exceptions;
using static bankAccounts.handle.CreateValidBank;

namespace bankAccounts.modules
{
    public class Bank (String recievedName, string recievedSwiftCode) { 
        private string bankName = recievedName; //for easy reading, consider banks unique by their name
        private string swiftCode = recievedSwiftCode; 
        private List<Account> accountsList = new List<Account>(); 

        public Account? SearchAccountByIBAN(string searchedIBAN) { //[type]? means it can contains a reference to an object OR a null reference
            foreach (Account account in accountsList) { 
                if (account.GetIBAN() == searchedIBAN)  
                    return account;
            }
            return null;
        }

        public void CreateAccount(string accountHolder, AccountType accountType, string IBAN, double amount, List<Bank> createdBanks) { 
            Account? newAccount = SearchAccountByIBAN_InAllBanks(IBAN, createdBanks);
            if (newAccount != null) { //account already exists in current bank or in another bank
                throw new AccountAlreadyExistsException("An account with IBAN '" + IBAN + "' already exists --> " + newAccount.ToString() + "\n");
            
            } else { //account does NOT exist
                newAccount = new Account(accountHolder, accountType, IBAN, 0); //if the account is NOT found, we can re-write reference
                newAccount.DepositCash(amount); //use method from Account class to deposit cash to test if the amount is valid
                accountsList.Add(newAccount); 
                Console.WriteLine("Account with IBAN '" + IBAN + "' was added successfully!\n");
            }
        }       

        public void FindAccountAndShowDetails(string IBAN) {  
            Account? searchedAccount = SearchAccountByIBAN(IBAN);
            if (searchedAccount != null)  
                Console.WriteLine("Account founded --> " + searchedAccount.ToString());
            else  
                throw new AccountNotFoundException("Account with IBAN '" + IBAN +"' NOT found!\n");
        }

        public void DepositCashToBank(string IBAN, double amount) { 
            Account? searchedAccount = SearchAccountByIBAN(IBAN);
            if (searchedAccount != null)  
                searchedAccount.DepositCash(amount); //we use method from Account class to deposit cash
            else  
                throw new AccountNotFoundException("Account with IBAN '" + IBAN +"' NOT found, deposit failed!\n");
        }

        public void WithdrawCashFromBank(string IBAN, double amount) { 
            Account? searchedAccount = SearchAccountByIBAN(IBAN);
            if (searchedAccount != null)  
                searchedAccount.WithdrawCash(amount); //we use method from Account class to withdraw cash
            else
                throw new AccountNotFoundException("Account with IBAN '" + IBAN +"' NOT found, withdraw failed!\n");
        }

        public void TransferToAnotherAccount (string senderIBAN, List<Bank> createdBanks, string receiverIBAN, double amount) { 
            Account? senderAccount = SearchAccountByIBAN(senderIBAN); 
            if (senderAccount != null) { 
                senderAccount.WithdrawCash(amount); 
                
                Account? receiverAccount = SearchAccountByIBAN_InAllBanks(receiverIBAN, createdBanks); 
                if (ReferenceEquals(senderAccount, receiverAccount)) { //case that sender and receiver are the same account 
                    Console.WriteLine("Sender and reciever are the same account, send money back ...");
                    senderAccount.DepositCash(amount);
                    throw new SameAccountException("Sender and receiver are the same account, transfer failed!\n");
                
                }else if (receiverAccount != null) { 
                    receiverAccount.DepositCash(amount); 
                    Console.WriteLine("Transaction was made successfully!\n"); 
                
                } else { //case that reciever NOT found, so cash goes back to the sender
                    Console.WriteLine("Reciever account NOT found, send money back to the sender ...");
                    senderAccount.DepositCash(amount);  
                    throw new AccountNotFoundException("Receiver account with IBAN '" + receiverIBAN +"' NOT found, transfer failed!\n");
                }
            
            } else 
                throw new AccountNotFoundException("Sender account with IBAN '" + senderIBAN +"' NOT found, transfer failed!\n"); 
        } 

        public void ShowAllBankAccounts() {  
            if (accountsList.Count == 0) {
                Console.WriteLine("\nNo accounts were created in '" + bankName + "' yet!\n");
            
            } else {
                Console.WriteLine("\n~ All accounts in '" + bankName + "' bank. ~");
                int accountNumber = 1;
                foreach (Account account in accountsList) { 
                    Console.Write("\t" + accountNumber + ") " + account.ToString());
                    accountNumber++;
                }
                Console.WriteLine(); //for a better seen output 
            }
        }

        public string GetBankName() { 
            return bankName;
        }

        public override string ToString() { 
            return "Bank name: " + bankName + "\tSwift code: " + swiftCode + "\t " + " Associated accounts: " + accountsList.Count + "\n"; 
        }
    }
} 
