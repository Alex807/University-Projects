using System;
using bankAccounts.modules;

using static bankAccounts.invokers.HandleExceptions;
using static bankAccounts.execute.SelectedOption;
using System.Collections;

public class MainClass { 
    public static void PrintOptionMenu() { 
        Console.WriteLine("Bank options:");
        Console.WriteLine("  1. Create Bank"); 
        Console.WriteLine("  2. Show Current Bank");
        Console.WriteLine("  3. Switch to Another Bank"); 
        Console.WriteLine("  4. Show All Banks");
        Console.WriteLine("Account options:");
        Console.WriteLine("  5. Create Account"); 
        Console.WriteLine("  6. Search Account"); 
        Console.WriteLine("  7. Deposit Cash"); 
        Console.WriteLine("  8. Withdraw Cash"); 
        Console.WriteLine("  9. Show Account Balance");
        Console.WriteLine("  10. Transfer Money"); 
        Console.WriteLine("  11. Show All Data-Structure");
        Console.WriteLine("  12. Exit");   
    }
    /*Lucruri de adaugat: 
    - sa fie retinute toate datele intr-un fisier txt, iar atunci cand rulezi din nou, sa citeasca de acolo datele 
    - metodele DeleteAccount si DeleteBank 
    */
    public static void Main() { 
        List<Bank> createdBanks = new List<Bank>();
        Console.WriteLine("\n\t~ Welcome to the 'Bank Account Manager API' ~ \nTo can start, a bank should be create !!");
        Bank currentBank = Invoke_CreateValidBank(createdBanks); //must create a bank to have an object to work with 

        int selectedOption = 0;
        do { 
            PrintOptionMenu(); 
            Console.Write("Select an option: "); 
            try { 
                selectedOption = Convert.ToInt32(Console.ReadLine()); //read the option from keyboard
            
            } catch (Exception) { 
                Console.WriteLine("Invalid option! Please select a number from 1 to 12!\n");
                continue; 
            } 

            if (selectedOption < 1 || selectedOption > 12)  
                Console.WriteLine("Invalid option! Please select a number from 1 to 12!\n");
            else 
                currentBank = ExecuteSelectedOption(selectedOption, currentBank, createdBanks); //we update the current bank in method conform the selected option
        
        } while (selectedOption != 12);
    }
}
