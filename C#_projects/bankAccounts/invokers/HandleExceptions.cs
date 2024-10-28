using System;
using System.Diagnostics;
using bankAccounts.exceptions; 
using bankAccounts.modules;
using static bankAccounts.handle.CreateValidBank;

namespace bankAccounts.invokers { 
    public static class HandleExceptions {
        public static Bank Invoke_CreateValidBank(List<Bank> createdBanks) { //return a 'bank' object who is unique by name
            Bank? bank;
            do { //use a loop to be sure that given bank has not already been created
                try { 
                    bank = CreateBank(createdBanks);
                
                } catch (BankAlreadyExistsException exception) { 
                    Console.WriteLine(exception.Message);
                    bank = null;
                } 
                
            } while (bank == null);
            return bank;
        }

        public static Bank Invoke_SwitchToAnotherBank(Bank currentBank, List<Bank> createdBanks) { 
            Bank bank;
            try { 
                bank = SwitchToAnotherBank(currentBank, createdBanks);

            } catch (Exception exception) {
                Console.WriteLine(exception.Message);
                bank = currentBank; //if the user tries to switch to the same bank / bank was not founded -> we keep the current bank 
            }
            return bank;
        }

        public static void Invoke_CreateAccountMethod (Bank bank, List<Bank> createdBanks, string accountHolder, AccountType accountType, string IBAN, double amount) { 
            try { 
                bank.CreateAccount(accountHolder, accountType, IBAN, amount, createdBanks);
            
            } catch (Exception exception) { //catch a parent exception because we have same treatment in all scenarios 
                Console.WriteLine(exception.Message); 
            }
        }

        public static void Invoke_FindAccountAndShowDetailsMethod (Bank bank, string IBAN) { 
            try { 
                bank.FindAccountAndShowDetails(IBAN);
            
            } catch (AccountNotFoundException exception) { 
                Console.WriteLine(exception.Message);
            } 
        }

        public static void Invoke_DepositCashToBankMethod (Bank bank, string IBAN, double amount) { 
            try { 
                bank.DepositCashToBank(IBAN, amount);
            
            } catch (Exception exception) { //catch a parent exception because we have same treatment in all scenarios          
                Console.WriteLine(exception.Message);
            } 
        }

        public static void Invoke_WithdrawCashFromBankMethod (Bank bank, string IBAN, double amount) {
            try {
                bank.WithdrawCashFromBank(IBAN, amount);
            
            } catch (Exception exception) {  //catch a parent exception because we have same treatment in all scenarios/catch a parent exception for both exceptions because we have same treatment in both scenarios  
                Console.WriteLine(exception.Message); 
            }  
        }

        public static void Invoke_ShowAccountBalance (Bank bank, string IBAN) { 
            Account? searchedAccount = bank.SearchAccountByIBAN(IBAN); 
            if (searchedAccount != null)  
                Console.WriteLine(searchedAccount.ShowAccountBalance());
            else  
                Console.WriteLine("Account with IBAN '" + IBAN + "' NOT found!\n");
        }

        public static void Invoke_TransferToAnotherAccountMethod (Bank bank, string senderIBAN, List<Bank> createdBanks, string recieverIBAN, double amount) { 
            try { 
                bank.TransferToAnotherAccount(senderIBAN, createdBanks, recieverIBAN, amount);
            
            } catch (Exception exception) {  //catch a parent exception because we have same treatment in all scenarios 
                Console.WriteLine(exception.Message);
            }
        }
    }
}