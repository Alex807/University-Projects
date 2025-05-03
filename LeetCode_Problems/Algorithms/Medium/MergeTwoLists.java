You are given the heads of two sorted linked lists list1 and list2.

Merge the two lists into one sorted list. The list should be made by splicing together the nodes of the first two lists.

Return the head of the merged linked list. 
Input: list1 = [1,2,4], list2 = [1,3,4]
Output: [1,1,2,3,4,4] 

Solution_ITERATIV: 
	class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        ListNode mergedList = new ListNode(); //for list parse always need an aux node
        ListNode aux = mergedList;

        while (list1 != null && list2 != null) { //we need bouth of them to still have elements
            if (list1.val <= list2.val) { 
                aux.next = new ListNode(list1.val); //use next to can link nodes with the value
                list1 = (list1 != null) ? list1.next : null;    
            
            } else { 
                aux.next = new ListNode(list2.val); 
                list2 = (list2 != null) ? list2.next : null;
            }
            aux = aux.next;
        }

        if (list1 == null) aux.next = list2; //we add automaticly the remaining part of a list to final result
        if (list2 == null) aux.next = list1;

        return mergedList.next;
    }
} 

Solution_RECURSIVE: 
	class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        if (list1 == null) return list2; 
        if (list2 == null) return list1; 
        
        if (list1.val <= list2.val) { 
            list1.next = mergeTwoLists(list1.next, list2); 
            return list1;
        } else { 
            list2.next = mergeTwoLists(list1, list2.next);
            return list2;
        }
    }
}