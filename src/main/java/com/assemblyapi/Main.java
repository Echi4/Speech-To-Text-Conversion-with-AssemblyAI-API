package com.assemblyapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        Transcript transcript = new Transcript();
        transcript.setAudio_url("https://bit.ly/3yxKEIY");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String jsonString = objectMapper.writeValueAsString(transcript);
        System.out.println(jsonString);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpPostRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .timeout(Duration.ofSeconds(30))
                .header("authorization", "API KEY")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> httpPostResponse = httpClient.send(httpPostRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(httpPostResponse.body());

        JsonNode jsonNodeHttpPostResponse = objectMapper.readTree(httpPostResponse.body());
        String id = jsonNodeHttpPostResponse.get("id").asText();
        System.out.println(id);

        HttpRequest httpGetRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript" + "/" + id))
                .timeout(Duration.ofSeconds(30))
                .header("authorization", "API KEY")
                .build();

        JsonNode jsonNodeHttpGetResponse;
        do {
            try {
                HttpResponse<String> httpGetResponse = httpClient.send(httpGetRequest, HttpResponse.BodyHandlers.ofString());
                jsonNodeHttpGetResponse = objectMapper.readTree(httpGetResponse.body());
                String status = jsonNodeHttpGetResponse.get("status").asText();
                System.out.println(status);

                if ("completed".equals(status) || "error".equals(status)) {
                    break;
                }
                Thread.sleep( 1000);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        } while (true);

        System.out.println(jsonNodeHttpGetResponse.get("text").asText());

    }
}
