package com.rubberducklabs.servers.game.apiClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpaceEngineersRemoteClient {
    private final String baseUrl;
    private final String securityKey;
    private final Random random;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpaceEngineersRemoteClient(String baseUrl, String securityKey) {
        this.baseUrl = baseUrl;
        this.securityKey = securityKey;
        this.random = new Random();
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpRequest createRequest(String resourceLink, String method, Map<String, String> queryParams)
            throws NoSuchAlgorithmException, InvalidKeyException {
        // Format the URL with resource link
        String methodUrl = String.format("/vrageremote/%s", resourceLink);

        // Generate date in RFC1123 format
        String date = ZonedDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
        // Generate random nonce
        String nonce = String.valueOf(random.nextInt(Integer.MAX_VALUE));

        // Build message for HMAC
        StringBuilder message = new StringBuilder();
        message.append(methodUrl);

        System.out.println("Method URL: " + methodUrl);

        if (!queryParams.isEmpty()) {
            message.append("?");
            String queryString = queryParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            message.append(queryString);
        }

        message.append("\r\n")
                .append(nonce).append("\r\n")
                .append(date).append("\r\n");

        // Compute HMAC
        byte[] messageBytes = message.toString().getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = Base64.getDecoder().decode(securityKey);

        // Use HMACSHA1 for hashing
        Mac hmac = Mac.getInstance("HmacSHA1");
        hmac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
        byte[] hashBytes = hmac.doFinal(messageBytes);
        String hash = Base64.getEncoder().encodeToString(hashBytes);

        // Log the message and hash for debugging
        System.out.println("Message for HMAC: " + message.toString());
        System.out.println("HMAC SHA1 Hash (Base64): " + hash);

        // Build full URL
        String fullUrl = baseUrl + methodUrl;
        if (!queryParams.isEmpty()) {
            fullUrl += "?" + queryParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
        }

        // Log the URL for debugging
        System.out.println("Full URL: " + fullUrl);

        // Create and return the HTTP request
        return HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Date", date)
                .header("Authorization", String.format("%s:%s", nonce, hash))  // Format as "nonce:hash"
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
    }

    // Example usage method
    public void getCurrentPlayers() throws Exception {
        HttpRequest request = createRequest("v1/session/players", "GET", Map.of());
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        JsonNode responseData = objectMapper.readTree(response.body()).get("data");
        JsonNode players = responseData.get("players");
        if (players != null) {
            System.out.println("Players: " + players.toString());
        }
    }
    public boolean pingServer() throws Exception {
        HttpRequest request = createRequest("v1/server/ping", "GET", Map.of());
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        JsonNode responseData = objectMapper.readTree(response.body()).get("data");
        System.out.println(responseData.get("Result"));
        return response.statusCode() == 200;
    }
}
