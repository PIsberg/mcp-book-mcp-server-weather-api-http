
package com.example.mcp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.util.UriComponentsBuilder; // For building URLs cleanly

@Service
public class ToolHandler {

    private final RestTemplate restTemplate;

    /**
     * Constructor for ToolHandler, injecting RestTemplate.
     * RestTemplate should be configured as a Spring bean in a @Configuration class.
     *
     * @param restTemplate The RestTemplate instance to use for HTTP calls.
     */
    public ToolHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Executes the business logic for the weather-tool.
     * Retrieves current weather for a specific city using the WeatherAPI.
     *
     * @param args A JsonNode containing the tool's input parameters.
     *             Expected fields: "q" (city name), "key" (API Key).
     * @return The raw JSON response from the weather API as a String, or an error message.
     */
    public String execute(JsonNode args) {
        try {
            // Extract required parameters from the JsonNode
            // The API schema defines 'q' and 'key' as required.
            String city = args.has("q") ? args.get("q").asText() : null;
            String apiKey = args.has("key") ? args.get("key").asText() : null;

            // Basic validation for required parameters
            if (city == null || city.trim().isEmpty()) {
                return "Error: Parameter 'q' (city name) is required.";
            }
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return "Error: Parameter 'key' (API Key) is required.";
            }

            // Define the base URL for the weather API
            String baseUrl = "https://api.weatherapi.com/v1/current.json";

            // Use UriComponentsBuilder to safely construct the URL with query parameters
            String targetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("q", city)
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();

            // Make the GET request using RestTemplate and return the response body as a String
            String responseBody = restTemplate.getForObject(targetUrl, String.class);

            return responseBody;

        } catch (Exception e) {
            // Catch any exceptions that occur during the process (e.g., network issues, API errors)
            // and return a descriptive error message.
            return "Error executing weather-tool: " + e.getMessage();
        }
    }
}
