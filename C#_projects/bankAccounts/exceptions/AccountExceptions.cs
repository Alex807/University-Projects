using System; 

namespace bankAccounts.exceptions { 
    public class SameAccountException : Exception {
        public SameAccountException(string message) : base(message) { }
    }

    public class AccountAlreadyExistsException : Exception { 
        public AccountAlreadyExistsException(string message) : base(message) { } 
    }

    public class AccountNotFoundException : Exception { 
        public AccountNotFoundException(string message) : base(message) { } 
    }
}