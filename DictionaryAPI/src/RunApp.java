import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream; 

import components.OnlineDictionary; //import only the OnlineDictionary(need only this class to obtain the information about the word)
import javax.swing.*; // used in order to be able to switch between the two interfaces if user wants

/**
 * RunApp - Main entry point for the Dictionary Application
 * Provides an interactive command-line interface for users to look up words
 * in different languages, with definitions, examples, synonyms, and antonyms.
 */
public class RunApp {
    private static final String DATABASE_PATH = "../resources/DataBase.txt"; //define constants
    private static final String LANG_CODES_PATH = "../resources/LanguageCodes.txt";
    private static final String DATA_SEPARATOR = ":";

    private OnlineDictionary dictionary; //create instances of the classes that we need to run the application
    private Scanner scanner;
    private boolean isRunning;  //control the application flow

    public RunApp() {

        ensureDatabaseExists(DATABASE_PATH); 
        ensureLanguageCodesFileExists(LANG_CODES_PATH);

        dictionary = new OnlineDictionary(DATABASE_PATH, DATA_SEPARATOR); //initialize needed components
        scanner = new Scanner(System.in);
        isRunning = true;
    }

    private void ensureDatabaseExists(String dbPath) {
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            try {
                // create parent directories if they don't exist
                File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                // create the database file
                dbFile.createNewFile();
                System.out.println("Created new database file at: " + dbPath);

            } catch (IOException e) {
                System.err.println("Failed to create database file: " + e.getMessage());
                System.err.println("The application may not be able to save word definitions.");
            }
        }
    }

    private void ensureLanguageCodesFileExists(String langCodesPath) {
        File langCodesFile = new File(langCodesPath);
        if (!langCodesFile.exists()) {
            System.err.println("Language codes file not found at: " + langCodesPath);
            System.err.println("Please provide a valid file with language codes.");
            System.exit(1);
        } 
    }

    private void displayWelcome() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("\t\t\t\tCOMMAND-LINE INTERFACE DICTIONARY");
        System.out.println("=".repeat(120));
        System.out.println("This application allows you to look up words in different languages.");
        System.out.println("For each word, you'll get definitions, examples, synonyms, and antonyms.");
        System.out.println("\nInstructions:");
        System.out.println("1. Enter a word to look up(no special characters or accents)");
        System.out.println("2. Specify the source language (in English, e.g., 'spanish', 'french')");
        System.out.println("3. Press Enter twice to exit the application");
        System.out.println("=".repeat(120) + "\n");
    }

    public void run() {
        displayWelcome();

        while (isRunning) {
            try {
                String word = promptForInput("Enter word (or press 'Enter' to exit): ");

                if (word.isEmpty()) { // exit if no word is provided
                    confirmExit();
                    continue;
                }

                String language = promptForInput("Enter source_language (in English): ");

                System.out.println("\nBrowsing up information...");
                String result = dictionary.getInformationsAbout(word, language); 

                System.out.println("\n" + "-----RESULTS" + "-".repeat(108));
                System.out.println(result);
                System.out.println("-".repeat(120) + "\n");

            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // clean up resources
        scanner.close();
        System.out.println("Thank you for using the Dictionary Application. Goodbye!");
    }

    private String promptForInput(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private void confirmExit() {
        System.out.print("Are you sure you want to exit? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("y") || response.equals("yes") || response.isEmpty()) {
            isRunning = false;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n\t\t\t" + "_".repeat(51)); 
        System.out.println("\t\t\t|\t\t\t\t\t\t  |");
        System.out.println("\t\t\t|\tWELCOME TO THE  ONLINE DICTIONARY API\t  |");
        System.out.println("\t\t\t|\t\t\t\t\t\t  |");
        System.out.println("\t\t\t" + "-".repeat(51));
        System.out.print("Would you like to use the GUI version? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (response.equals("y") || response.equals("yes")) {
            System.out.println("Starting GUI version...");
            
            SwingUtilities.invokeLater(() -> { 
                GUI gui = new GUI(); // launch GUI using SwingUtilities to ensure it runs on the Event Dispatch Thread
                gui.setVisible(true);

                // Make the GUI window the principal window
                gui.setAlwaysOnTop(true); // Makes window stay on top initially
                gui.toFront();            // Brings window to front
                gui.requestFocus();       // Requests focus for the window
                
                // Optional: After a short delay, disable always on top
                // This ensures it starts on top but doesn't force it to stay there
                Timer timer = new Timer(1000, e -> gui.setAlwaysOnTop(false));
                timer.setRepeats(false);
                timer.start();
            });

        } else {
            System.out.println("Starting command-line version...");
            RunApp application = new RunApp();
            application.run();
        }
    }
}