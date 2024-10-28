using System; 
using bankAccounts.modules;
 
using static bankAccounts.invokers.HandleExceptions; 
using static bankAccounts.validation.ConsoleInputValidation; 
using static bankAccounts.handle.CreateValidBank;

namespace bankAccounts.execute { 
    public static class SelectedOption { 
        public static Bank ExecuteSelectedOption(int selectedOption, Bank currrentBank, List<Bank> createdBanks) { 
            switch (selectedOption) { 
                case 1: //Create Bank 
                    currrentBank = Invoke_CreateValidBank(createdBanks); 
                    break;

                case 2: //Show Current Bank 
                    Console.WriteLine("\nCurrent bank is '" + currrentBank.GetBankName() + "'.\n"); 
                    break;
                
                case 3: //Switch to Another Bank 
                    currrentBank = Invoke_SwitchToAnotherBank(currrentBank, createdBanks); 
                    break;
                
                case 4: //Show All Banks 
                    ShowAllBanks(createdBanks); 
                    break;

                case 5: //Create Account
                    string accountHolder = ReadValidStringFromConsole("Account holder name: ");
                    AccountType accountType = ReadValidAccountTypeFromConsole(); 
                    string IBAN_createAccount = ReadValidStringFromConsole("IBAN: "); 
                    double initialBalance = ReadValidDoubleFromConsole("Initial balance: ");
                    Invoke_CreateAccountMethod(currrentBank, createdBanks, accountHolder, accountType, IBAN_createAccount, initialBalance);
                    break; 

                case 6: //Find Account and Show Details
                    Console.WriteLine("  Note: Make sure that account is in current bank!\n");
                    string IBAN_findAccount = ReadValidStringFromConsole("IBAN of searched account: ");
                    Invoke_FindAccountAndShowDetailsMethod(currrentBank, IBAN_findAccount); 
                    break;

                case 7: //Deposit Cash 
                    string IBAN_deposit = ReadValidStringFromConsole("IBAN of account to deposit: "); 
                    double depositedAmount = ReadValidDoubleFromConsole("Amount to deposit: "); 
                    Invoke_DepositCashToBankMethod(currrentBank, IBAN_deposit, depositedAmount);
                    break; 

                case 8: //Withdraw Cash 
                    string IBAN_withdraw = ReadValidStringFromConsole("IBAN of account to withdraw: "); 
                    double withdrawAmount = ReadValidDoubleFromConsole("Amount to withdraw: ");
                    Invoke_WithdrawCashFromBankMethod(currrentBank, IBAN_withdraw, withdrawAmount);
                    break; 

                case 9: //Show Account Balance 
                    Console.WriteLine("  Note: Make sure that account is in current bank!\n");
                    string IBAN_showBalance = ReadValidStringFromConsole("IBAN of account: ");
                    Invoke_ShowAccountBalance(currrentBank, IBAN_showBalance); 
                    break;

                case 10: //Transfer to Another Account 
                    Console.WriteLine("  Note: The sender account must be in current bank and reciever account in ANY bank!\n");
                    string senderIBAN = ReadValidStringFromConsole("IBAN of sender account: ");
                    string recieverIBAN = ReadValidStringFromConsole("IBAN of reciever account: ");
                    double transferAmount = ReadValidDoubleFromConsole("Amount to transfer: "); 
                    Invoke_TransferToAnotherAccountMethod(currrentBank, senderIBAN, createdBanks, recieverIBAN, transferAmount);
                    break;
                
                case 11: //Show All Data-Structure 
                    Console.WriteLine("\n\t\t Data-Structure of 'Bank Account Manager API' ");
                    foreach (Bank bank in createdBanks) 
                        bank.ShowAllBankAccounts();
                    break;
                
                case 12: //Exit 
                    Console.WriteLine("Thank you for using our services! Goodbye!\n"); 
                    break;

                default: 
                    Console.WriteLine("Invalid option! Please select a number from 1 to 11!\n");
                    break;

            }
            return currrentBank;
        }
    }
}