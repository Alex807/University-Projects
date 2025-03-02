package components;

import java.util.*; 
import java.io.*;

public class DataBaseManager { 
    private final String dataBasePath;  //once initialized, the path to the database file cannot be changed
    private final String dataMarker; //data marker is string that separates the word from its definition in the database file

    public DataBaseManager(String dataBasePath, String dataMarker) {
        this.dataBasePath = dataBasePath;
        this.dataMarker = dataMarker;
    }

    public Map<String, String> loadFromDataBase() { //in our dataBase lines which contain ':' are lines that contain information about a word
        Map<String, String> database = new HashMap<>();
    
        try (Scanner scanner = new Scanner(new java.io.File(dataBasePath), "UTF-8")) { 
            String currentWord = null;
            StringBuilder currentDefinition = new StringBuilder();
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                if (!line.isEmpty()) { 
                    if (!line.contains(dataMarker)) { // if line doesn't contain dataMarker, it's a new word (key)
                        
                        // if we were processing a previous word, save it before starting a new one
                        if (currentWord != null && currentDefinition.length() > 0) {
                            database.put(currentWord, currentDefinition.toString());
                            currentDefinition = new StringBuilder();
                        }
                    
                        currentWord = line.trim(); //set the new word

                    } else if (currentWord != null) { // still read the definition of current word
                        
                        if (currentDefinition.length() > 0) { //add a \n if we already have some definition content(have value prepared for printing)
                            currentDefinition.append("\n");
                        }
                        currentDefinition.append(line.trim());
                    }

                } else if (currentWord != null && currentDefinition.length() > 0) { // if line is empty and we have data stored, add them in hash map
                    database.put(currentWord, currentDefinition.toString());
                    currentWord = null;
                    currentDefinition = new StringBuilder();
                } 
            }
            
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Database file not found at path: " + dataBasePath);
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("Error loading database: " + e.getMessage());
            e.printStackTrace();
        }
        return database;
    }

    public void updateDataBase(String word, String definition, String AIData) { //I chose to write in DB after we find each new word
        String data = String.format("%s\n%s", definition, AIData); // we store the word and its definition in the database
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                                new FileOutputStream(dataBasePath, true), "UTF-8"))) { //use try-with-resources to ensure the writer is closed(true is for appending in file)
            writer.println(word);
            writer.println(data);
        
        } catch (IOException e) {
            System.err.println("Error writing cache to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}