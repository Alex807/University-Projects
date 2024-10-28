using System; 

namespace bankAccounts.exceptions { 
    public class BankAlreadyExistsException : Exception { 
        public BankAlreadyExistsException(string message) : base(message) { } 
    }

    public class BankNotFoundException : Exception { 
        public BankNotFoundException(string message) : base(message) { }
    }

    public class SwitchToSameBankException : Exception {
        public SwitchToSameBankException(string message) : base(message) { }
    }
}