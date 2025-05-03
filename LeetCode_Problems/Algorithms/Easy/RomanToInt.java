Solution_INITIAL: 
	class Solution {
    public int romanToInt(String s) {
        Map <Character, Integer> romanDigits = new HashMap<>(7);
        romanDigits.put('I', 1);
        romanDigits.put('V', 5);
        romanDigits.put('X', 10);
        romanDigits.put('L', 50);
        romanDigits.put('C', 100);
        romanDigits.put('D', 500);
        romanDigits.put('M', 1000);

        char[] inputChars = s.toUpperCase().toCharArray(); 
        int i, result = 0; 

        for (i = 0; i < (inputChars.length - 1); i++) { 
            int current = romanDigits.get(inputChars[i]);
            int next = romanDigits.get(inputChars[i + 1]);
            
            if (next > current) { 
                result = result + (next - current); 
                i++; //to jump over already handle char 
                continue;

            } else if (next == current) { 
                result += current * 2; 
                i++; //to jump over already handle char 
                continue;
            }
            result += current;
        }

        if (i == inputChars.length) { 
            return result; //last char was already handled
        }

        int index = inputChars.length - 1;
        return result + romanDigits.get(inputChars[index]);
    }
}

Solution_IMPROVED: 
	class Solution {
    public int romanToInt(String s) {
    
    int answer = 0, number = 0, prev = 0;

    for (int j = s.length() - 1; j >= 0; j--) {
        switch (s.charAt(j)) {
            case 'M' -> number = 1000;
            case 'D' -> number = 500;
            case 'C' -> number = 100;
            case 'L' -> number = 50;
            case 'X' -> number = 10;
            case 'V' -> number = 5;
            case 'I' -> number = 1;
        }

        if (number < prev) { //case when we write IV
            answer -= number;
        }
        else { //base case
            answer += number;
        }
        prev = number;
    }
    return answer;
}
}