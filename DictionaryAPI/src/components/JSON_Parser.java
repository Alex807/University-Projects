package components;

import java.util.*;

public class JSON_Parser {
    private static JSON_Parser instance;
    
    // Singleton pattern
    private JSON_Parser() {
        // nothing to do here
    }
    
    public static JSON_Parser getInstance() {
        if (instance == null) {
            instance = new JSON_Parser();
        }
        return instance;
    }

    public String extractItem(String responseBody, String item) {
        if (!responseBody.contains(item)) {
            return String.format("In JSON response doesn't exist '%s' field!", item); // ensure that we search only valid items
        }

        int skip = item.length() + 4; // +4 to skip the item(X) itself and the following characters ("X":"[value]") in order to get the 'value'
        try {
            int startIndex = responseBody.indexOf("\"" + item + "\":\"") + skip;
            int endIndex = responseBody.indexOf("\"", startIndex);

            if (startIndex > (skip - 1) && endIndex > startIndex) { //check if the item was found
                return responseBody.substring(startIndex, endIndex);
            }
        
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "NONE"; // return a default message if item was not found
    }
}