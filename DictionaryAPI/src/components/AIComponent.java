package components;

import java.net.http.*;
import java.net.URI;

public class AIComponent { 
    private static final String CohereAI_API_URL = "https://api.cohere.com/v1/chat"; // Updated URL for the Cohere Chat API
    private static final String CohereAI_API_KEY = "RlxPCDwW2Aw8YsFBgVdJtUpy2FXEvtzYwv1W1xTO"; // Free API key for accessing the model
    private static final String MODEL = "command-r-plus-08-2024"; 
    private static final double TEMPERATURE = 0.7; 
    
    private static AIComponent instance;
    
    private AIComponent() { //Singleton pattern
        // nothing to do here
    }
    
    public static AIComponent getInstance() {
        if (instance == null) {
            instance = new AIComponent();
        }
        return instance;
    }

    public String getAIData(String word) {
        String prompt = String.format("For '%s' provide exactly one line containing: a short phrase with '%s' after tag 'Example:', synonyms and antonyms in this format[Example: created phrase||Synonyms: 2, 3, 4||Antonyms: 5, 6, 7||", word, word);

        try {
            HttpClient client = HttpClient.newHttpClient(); // Create a new HTTP client for sending requests

            // Create the request body in JSON format according to Cohere's API structure
            String requestBody = String.format("{"
                                    + "\"message\": \"%s\", "
                                    + "\"model\": \"%s\", "
                                    + "\"temperature\": %.1f}", 
                                    prompt, MODEL, TEMPERATURE);

            // create the request with the URL, use the API key for authorization, and set the content type to JSON
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CohereAI_API_URL))
                .header("Authorization", "Bearer " + CohereAI_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            // after creating the request, send it to the server and wait for the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // extract the generated data from the response using the JSON_Parser
            JSON_Parser parser = JSON_Parser.getInstance();
            String generatedData = parser.extractItem(response.body(), "text");

            return generatedData.replace("||", "\n"); // Add an actual new line in the response

        } catch (Exception exception) {
            exception.printStackTrace();
            return "Error accessing Cohere-AI API: " + exception.getMessage();
        }
    }
}