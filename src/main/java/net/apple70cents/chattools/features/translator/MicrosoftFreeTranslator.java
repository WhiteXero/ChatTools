package net.apple70cents.chattools.features.translator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.components.EditBox;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MicrosoftFreeTranslator extends AbstractTranslator {
    private static final String ENDPOINT = "https://api-edge.cognitive.microsofttranslator.com/translate";
    private static final String TOKEN_ENDPOINT = "https://edge.microsoft.com/translate/auth";
    private final String from;
    private final String to;

    protected MicrosoftFreeTranslator(EditBox editBox, String from, String to) {
        super(editBox);
        this.from = from;
        this.to = to;
    }

    private static String refreshToken() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(TOKEN_ENDPOINT)).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to refresh translation token: " + response.statusCode());
        }
    }

    @Override
    public String translate(String text) throws Exception {
        if (to.isBlank()) {
            throw new Exception("to required");
        }
        return getTransResult(text, from, to);
    }

    protected static String getTransResult(String query, String from, String to) throws Exception {
        Map<String, String> params = buildParams(from, to);
        String jsonResponse = sendPost(params, query);
        return parseJsonResponse(jsonResponse);
    }

    private static Map<String, String> buildParams(String from, String to) {
        Map<String, String> params = new HashMap<>();
        params.put("from", from);
        params.put("to", to);
        params.put("api-version", "3.0");
        params.put("includeSentenceLength", "true");
        params.put("textType", "html");
        return params;
    }

    private static String sendPost(Map<String, String> params, String text) throws Exception {
        String queryString = params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        String fullUrl = ENDPOINT + "?" + queryString;

        String jsonBody = "[{\"Text\": " + new Gson().toJson(text) + "}]";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + refreshToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Translation failed: " + response.statusCode());
        }
        return response.body();
    }

    private static String parseJsonResponse(String jsonResponse) throws Exception {
        JsonArray translations = new Gson().fromJson(jsonResponse, JsonArray.class);
        if (!translations.isEmpty()) {
            JsonObject translation = translations.get(0).getAsJsonObject();
            return translation.getAsJsonArray("translations").get(0).getAsJsonObject().get("text").getAsString();
        }
        throw new Exception("Empty translation response");
    }
}
