You are given two non-empty linked lists representing two non-negative integers. The digits are stored in reverse order, and each of their nodes contains a single digit. Add the two numbers and return the sum as a linked list.

You may assume the two numbers do not contain any leading zero, except the number 0 itself.
Input: l1 = [2,4,3], l2 = [5,6,4]
Output: [7,0,8]
Explanation: 342 + 465 = 807.

Solution_RECURSIV: 
	class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        return addTwoNumbersHelper(l1, l2, 0);
    }
    
    private ListNode addTwoNumbersHelper(ListNode l1, ListNode l2, int carry) {
        // Base case: if both lists are null and no carry
        if (l1 == null && l2 == null && carry == 0) {
            return null;
        }
        
        // Get values from the lists or 0 if list ended
        int val1 = (l1 != null) ? l1.val : 0;
        int val2 = (l2 != null) ? l2.val : 0;
        
        // Calculate sum and new carry
        int sum = val1 + val2 + carry;
        int newCarry = sum / 10;
        int digit = sum % 10;
        
        // Create current node
        ListNode current = new ListNode(digit);
        
        // Recursive call for next nodes
        current.next = addTwoNumbersHelper(
            (l1 != null) ? l1.next : null,
            (l2 != null) ? l2.next : null,
            newCarry
        );
        
        return current;
    }
}

Solution_ITERATIV: 
	class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(0); // dummy head
        ListNode current = dummy;
        int carry = 0;
        
        // Continue while there are digits in either list or there's a carry
        while (l1 != null || l2 != null || carry > 0) {
            // Get values from the lists (or 0 if list ended)
            int val1 = (l1 != null) ? l1.val : 0;
            int val2 = (l2 != null) ? l2.val : 0;
            
            // Calculate sum and new carry
            int sum = val1 + val2 + carry;
            carry = sum / 10;
            
            // Create new node with the digit
            current.next = new ListNode(sum % 10);
            current = current.next;
            
            // Move to next nodes if available
            l1 = (l1 != null) ? l1.next : null;
            l2 = (l2 != null) ? l2.next : null;
        }
        
        return dummy.next;
    }
