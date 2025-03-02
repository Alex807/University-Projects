package components;

import java.net.http.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets; 

public class Translator {
    private static final String MYMEMORY_TRANSLATE_URL = "https://api.mymemory.translated.net/get"; // we use MyMemory API to can translate words from any language to English
    
    private static Translator instance;
    
    // SINGLETON pattern
    private Translator() {
        // nothing to do here
    }
    
    public static Translator getInstance() {
        if (instance == null) {
            instance = new Translator();
        }
        return instance;
    }

    public String translateToEnglish(String word, String sourceLanguage) {
        word = word.trim(); // remove leading and trailing spaces(can be added by mistake) 
        sourceLanguage = sourceLanguage.trim(); // remove leading and trailing spaces(can be added by mistake)
        if (!isValidWord(word)) {
            return String.format("Word '%s' is INVALID!", word); 
        }

        try { 
            LanguageCodeFinder languageCodeFinder = LanguageCodeFinder.getInstance("../resources/LanguageCodes.txt", "-"); // get the instance of the LanguageCodeFinder class
            String srcLanguageCode = languageCodeFinder.getISO_Code(sourceLanguage); 
            
            if (srcLanguageCode.equals("INVALID")) {
                return String.format("Language '%s' is INVALID! \nMake sure you provide source_language in English!", sourceLanguage); // return an error message if the language is not supported
            
            } else if (srcLanguageCode.equals("en")) {
                return word.toLowerCase(); // if the source language is already English, return the word but in lower case
            }

            String langpair = String.format("%s|en", srcLanguageCode); // from source language to English
            String encodedLangpair = URLEncoder.encode(langpair, StandardCharsets.UTF_8); // encode the langpair to ensure it contains URL-safe characters
            String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8);  //encode the input, for one word is not necessary but for a sentence is
           
            // build the MyMemory Translation API URL with query parameters
            String translateURL = String.format("%s?q=%s&langpair=%s", MYMEMORY_TRANSLATE_URL, encodedWord, encodedLangpair);

            // create the HTTP client and request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest translateRequest = HttpRequest.newBuilder()
                .uri(URI.create(translateURL))
                .GET()
                .build();

            // send the request and get the response
            HttpResponse<String> translateResponse = client.send(translateRequest, HttpResponse.BodyHandlers.ofString());

            if (translateResponse.statusCode() != 200) { //200 is the status code for a successful HTTP request
                return "Translate_API call failed with status code: " + translateResponse.statusCode();
            }

            JSON_Parser parser = JSON_Parser.getInstance();
            String translatedText = parser.extractItem(translateResponse.body(), "translatedText");
            return translatedText.toLowerCase(); // use lowerCase to be sure that we do not have keys with different cases but same meaning

        } catch (Exception exception) {
            exception.printStackTrace();
            return "An exception occurred in 'translateToEnglish' method with message: " + exception.getMessage();
        }
    }

    public boolean isValidWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false; 
        }

        if (!word.matches("\\p{L}+") || word.length() > 20) { // "\p{L}" regular expression that matches any unicode letter, in any language, like "ă", "â" etc.
            return false; // invalid if it contains numbers, symbols, spaces, etc.
        }

        return true; // if all checks pass, the word is valid
    } 
}