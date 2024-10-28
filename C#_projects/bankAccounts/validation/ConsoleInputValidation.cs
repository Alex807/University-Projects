using System;
using bankAccounts.modules;

namespace bankAccounts.validation {
    public static class ConsoleInputValidation { 
        public static AccountType ReadValidAccountTypeFromConsole() { 
            do {
                Console.Write("Enter account type (Person/Company): ");
                string? input = Console.ReadLine();

                if (Enum.TryParse<AccountType>(input, true, out AccountType accountType)) // 'true' parameter is for case-insensitive parsing
                    return accountType;
                else
                    Console.WriteLine("Invalid input. Please enter a valid account type.");

            } while (true);
        }
 
        public static string ReadValidStringFromConsole(string message) { 
            do { 
                Console.Write(message); 
                string? input = Console.ReadLine(); 
                if (string.IsNullOrEmpty(input))  
                    Console.WriteLine("Invalid input. Please enter a valid string!"); 
                else
                    return input;

            } while (true);
        }

        public static double ReadValidDoubleFromConsole(string message) {
            do {
                Console.Write(message);
                string? input = Console.ReadLine();

                // Attempt to parse the input into a double.
                if (double.TryParse(input, out double amount))
                    return amount; // Return the valid parsed double.
                else
                    Console.WriteLine("Invalid input. Please enter a valid double!");

            } while (true); // Loop continues until a valid double is entered.
        }
    } 
}
