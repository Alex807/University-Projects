Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.

An input string is valid if:

Open brackets must be closed by the same type of brackets.
Open brackets must be closed in the correct order.
Every close bracket has a corresponding open bracket of the same type.

Solution_INITIAL:
class Solution {
    public boolean isValid(String s) {
        if (s == null || s.length() <= 1) return false;
        Stack<Character> stack = new Stack<>(); 

        for (char current : s.toCharArray()) {
            switch (current) {
                case ')':
                    if (stack.isEmpty() || stack.pop() != '(') return false;
                    break; //you need breaks bcs char is not entering in your if to reach return and stop the case
                case ']':
                    if (stack.isEmpty() || stack.pop() != '[') return false;
                    break;
                case '}':
                    if (stack.isEmpty() || stack.pop() != '{') return false;
                    break;
                case '(':
                case '[':
                case '{':
                    stack.push(current);
                    break;
            }
        }
        return stack.isEmpty(); //suppose to be empty bcs all were poped and CLOSED
    }
}

Solution_IMPROVED: 
	public class Solution {
    public boolean isValid(String s) {
        char[] stack = new char[s.length()];  //use a improvised stack to save memory usage
        int top = -1;
        for (char ch : s.toCharArray()) {
            if (ch == '(' || ch == '{' || ch == '[') {
                stack[++top] = ch;
            } else {
                if (top == -1) return false;
                char open = stack[top--]; 
                if ((ch == ')' && open != '(') || ///one of this cases needs to be true to go forword
                    (ch == '}' && open != '{') ||
                    (ch == ']' && open != '[')) {
                    return false;
                }
            }
        }
        return top == -1;
    }
}