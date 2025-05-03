class Solution {
    public boolean isValid(String s) {
        if (s == null || s.length() <= 1) return false;
        char[] inputChars = s.toCharArray();

        boolean parIsOpen = (inputChars[0] == '(');
        boolean brackIsOpen = (inputChars[0] == '[');
        boolean accIsOpen = (inputChars[0] == '{');

        int lastTypeToClose = 0;
        for(int i = 1; i < inputChars.length; i++ ) {
            char previous = inputChars[Math.max(lastTypeToClose, 0)];
            char current = inputChars[i];

            switch (current) {
                case ')':
                    if (!parIsOpen || previous != '(') {
                        return false;
                    } else {
                        parIsOpen = false;
                        lastTypeToClose--;
                    }
                    break;
                case ']':
                    if (!brackIsOpen || previous != '[') {
                        return false;
                    }  else {
                        brackIsOpen = false;
                        lastTypeToClose--;
                    }
                    break;
                case '}':
                    if (!accIsOpen || previous != '{') {
                        return false;
                    }  else {
                        accIsOpen = false;
                        lastTypeToClose--;
                    }
                    break;
                case '(':
                    parIsOpen = true;
                    lastTypeToClose = i;
                    break;
                case '[':
                    brackIsOpen = true;
                    lastTypeToClose = i;
                    break;
                case '{':
                    accIsOpen = true;
                    lastTypeToClose = i;
                    break;
            }
        }
        return ! (parIsOpen || brackIsOpen || accIsOpen);
    }
}

public class Main {
    public static void main(String[] args) {
        Solution ee = new Solution();

        System.out.println("Result: " + ee.isValid("(())"));
    }
}