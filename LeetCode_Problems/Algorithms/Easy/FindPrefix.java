Solution_INITIAL: 
	class Solution {
    public String longestCommonPrefix(String[] strs) {
        if (strs.length <= 1) { 
            return strs[0];
        }
        
        String result = new String(strs[0]); //we start to form the prefix from first elem
        int size = strs.length;
        while (!result.isEmpty()) { 
            for (int i = 1; i < size; i++) { 
                if (i == (size - 1) && strs[i].startsWith(result)) { 
                    return result; //all string matched
                
                } else if (!strs[i].startsWith(result)) { 
                    int lastChar = result.length() - 1;
                    result = result.substring(0, lastChar); 
                    break;
                }
            }
        }
        return "";
    }
}

Solution IMPROVED: 
	class Solution {
    public String longestCommonPrefix(String[] strs) {
        String prefix = strs[0];
        for(int index=1;index<strs.length;index++){
            while(strs[index].indexOf(prefix) != 0){ //stays on the same string from array until reaches the prefix between str[0] and str[i]
                prefix=prefix.substring(0,prefix.length()-1); //chop the prefix
            }
        }
        return prefix;
    }
}