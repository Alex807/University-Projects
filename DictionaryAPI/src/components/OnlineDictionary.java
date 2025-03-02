package components;

import java.net.http.*;
import java.util.*; 
import java.io.*;
import java.net.URI; 

public class OnlineDictionary {
    private String DICTIONARY_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"; // we use a free API for dictionary, to get definitions in English
    private Translator translator;
    private JSON_Parser parser; // this attributes are only one instance for all clients,that's why they are final
    private AIComponent AIComponent;
    private final DataBaseManager dataBaseManager; //once set, it can't be changed
    private Map<String, String> cacheMemory; // we store the responses for the previous searched words by client, use only one data-base for all clients

    
    public OnlineDictionary(String dataBasePath, String dataMarker) {
        this.translator = Translator.getInstance(); 
        this.parser = JSON_Parser.getInstance(); //all OnlineDictionary instances share the same JSON_Parser, Translator and AIComponent instances, because they are singletons
        this.AIComponent = AIComponent.getInstance();  
        this.dataBaseManager = new DataBaseManager(dataBasePath, dataMarker); 
        this.cacheMemory = dataBaseManager.loadFromDataBase(); // we initialize the cache memory
    }

    private String getWordDefinition(String word) { 
        try {
            String dictionaryAPI = DICTIONARY_URL + word.trim(); //to ensure correct URL format

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest dictionaryRequest = HttpRequest.newBuilder()
                .uri(URI.create(dictionaryAPI))
                .GET()
                .build();

            HttpResponse<String> dictionaryResponse = client.send(dictionaryRequest, HttpResponse.BodyHandlers.ofString());
           
            JSON_Parser parser = JSON_Parser.getInstance();
            String definition = "Definition: " + parser.extractItem(dictionaryResponse.body(), "definition");

            return definition; //if it is NONE means word is not found in the dictionary

        } catch (Exception exception) { //use a large exception type to catch all possible exceptions because we treat them the same way
            exception.printStackTrace();
            return "An exception occurred in 'getWordDefinition' method with message: " + exception.getMessage();
        }
    }

    private void updateCacheMemory(String word, String dictionaryData, String AIData) {
        String information = String.format("%s\n%s", dictionaryData, AIData);
        cacheMemory.put(word, information); // we store the definition in the cache
    }

    public int getCacheMemorySize() { //use it in GUI to show the number of words stored in cache
        return cacheMemory.size();
    }

    public String getInformationsAbout(String word, String sourceLanguage) { 
        Translator translator = Translator.getInstance();
        String wordInEnglish = translator.translateToEnglish(word, sourceLanguage); 

        if (cacheMemory.containsKey(wordInEnglish)) {
            return String.format("Translate: %s [English] \n", wordInEnglish) + cacheMemory.get(wordInEnglish); // if the word was searched before, we return the definition from the cache
       
        } else if (wordInEnglish.contains("INVALID")) { 
            return wordInEnglish; // if the word OR language are invalid, we return the error message provided by the translator
        
        } else if (!wordInEnglish.matches("[a-zA-Z]+")) { // if the word is a sentence, we take only the first word
            wordInEnglish = wordInEnglish.replaceAll("^(\\p{L}+).*", "$1");
        }

        String dictionaryData = getWordDefinition(wordInEnglish); // if the word was not searched before, we get the definition from the Dictionary API
        if (dictionaryData.contains("NONE")) {
            return String.format("Word '%s' has NO MEANING in '%s' language!", word, sourceLanguage); // if the word was not found in the dictionary, that means it's not a valid word
        }

        AIComponent AIInstance = AIComponent.getInstance();
        String AIData = AIInstance.getAIData(wordInEnglish); // we generate synonyms and antonyms for the word with AI
        
        updateCacheMemory(wordInEnglish, dictionaryData, AIData); 
        dataBaseManager.updateDataBase(wordInEnglish, dictionaryData, AIData); // we update the database with the new word and its definition(after each unfinded word)
        
        return String.format("Translate: %s [English] \n%s", wordInEnglish, cacheMemory.get(wordInEnglish)); // we return the definition and AI data that we saved
    } 
}