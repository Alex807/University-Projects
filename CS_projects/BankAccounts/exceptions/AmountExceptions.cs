using System; 

namespace bankAccounts.exceptions { 
    public class NegativeAmountException : Exception { 
        public NegativeAmountException(string message) : base(message) { } 
    }
 
    public class TooLargeAmountException : Exception { 
        public TooLargeAmountException(string message) : base(message) { }
    }
 
    public class HasTooManyDecimalsException : Exception {
        public HasTooManyDecimalsException(string message) : base(message) { }
    }

    public class InsufficientFundsException : Exception { 
        public InsufficientFundsException(string message) : base(message) { }
    }
}