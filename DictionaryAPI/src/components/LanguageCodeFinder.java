package components;

import java.io.*;
import java.util.*;

public class LanguageCodeFinder {
    private static LanguageCodeFinder instance;
    private final Map<String, String> languageMap;
    
    private LanguageCodeFinder(String filePath, String separator) { //singleton pattern
        languageMap = loadLanguageMap(filePath, separator);
    }
    
    public static LanguageCodeFinder getInstance(String filePath, String separator) {
        if (instance == null) {
            instance = new LanguageCodeFinder(filePath, separator);
        }
        return instance;
    }
    
    // Private method to load language map
    private Map<String, String> loadLanguageMap(String filePath, String separator) {
        Map<String, String> map = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" " + separator + " ");
                if (parts.length == 2) {
                    map.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        } catch (IOException exception) {
            System.err.println("Error reading the file: " + exception.getMessage()); 
            System.err.println("Please check the file_path of 'LanguageCodes.txt' and try again !!");
        }
        return map;
    }

    // Public method to get ISO code
    public String getISO_Code(String languageName) { 
        if (languageName == null || languageName.isEmpty()) {
            return "INVALID";
        }
        
        return languageMap.getOrDefault(languageName.toLowerCase(), "INVALID");
    }
}