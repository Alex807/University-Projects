using System;
using bankAccounts.modules;
using bankAccounts.exceptions;
using static bankAccounts.validation.ConsoleInputValidation;

namespace bankAccounts.handle {
    public static class CreateValidBank {  
        public static Bank? SearchBankByName(string searchedBankName, List<Bank> createdBanks) { 
            foreach (Bank bank in createdBanks) { 
                if (bank.GetBankName() == searchedBankName)  
                    return bank;
            }
            return null;
        }

        public static Bank CreateBank(List<Bank> createdBanks) { 
            string bankName = ReadValidStringFromConsole("Bank name: "); 
            string swiftCode = ReadValidStringFromConsole("Swift code: ");

            Bank? searchedBank = SearchBankByName(bankName, createdBanks);
            if (searchedBank != null) { 
                throw new BankAlreadyExistsException("Bank '" + bankName + "' was already created! [Consider that banks are UNIQUE by their names, NOT by swift code!]\n");
            
            } else { 
                searchedBank = new Bank(bankName, swiftCode); 
                createdBanks.Add(searchedBank); //update the list of created banks
                Console.WriteLine("Bank '" + bankName + "' was added successfully! Now current bank is '" + searchedBank.GetBankName() + "'.\n");
            }
            return searchedBank;
        }

        public static Bank SwitchToAnotherBank(Bank currentBank, List<Bank> createdBanks) { 
            string bankName = ReadValidStringFromConsole("Switch to bank(name): "); 
            
            Bank? searchedBank = SearchBankByName(bankName, createdBanks);
            if (searchedBank != null) { 
                if (searchedBank.GetBankName() == currentBank.GetBankName()) //consider that banks are unique by their names
                    throw new SwitchToSameBankException("You are already in bank '" + bankName + "'! Switch failed!\n"); 
                
                else { 
                    Console.WriteLine("Switched to bank '" + bankName + "' successfully! \nNow current bank is '" + searchedBank.GetBankName() + "'.\n"); 
                    return searchedBank;
                }
            } else 
                throw new BankNotFoundException("Bank '" + bankName + "' NOT found! Switch failed!\n");
        }

        public static Account? SearchAccountByIBAN_InAllBanks(string searchedIBAN, List<Bank> createdBanks) { 
            Account? searchedAccount;
            foreach (Bank bank in createdBanks) { 
                searchedAccount = bank.SearchAccountByIBAN(searchedIBAN);
                if (searchedAccount != null)  
                    return searchedAccount;
            }
            return null;
            
        }

        public static void ShowAllBanks(List<Bank> createdBanks) {
            if (createdBanks.Count == 0) {
                Console.WriteLine("\nNo banks were created yet!\n"); 
            
            } else { 
                Console.WriteLine("\n~List of created banks~"); 
                int index = 1;
                foreach (Bank bank in createdBanks) { 
                    Console.Write(index + ") " + bank.ToString());
                    index++;
                }
                Console.WriteLine(); 
            } 
        }
    }
}