package de.sharknoon.slash.networking.apis.pushy;

import com.google.gson.reflect.TypeToken;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.serialisation.Serialisation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.function.Consumer;

class PushyAPI {
    
    private static final String SECRET_API_KEY = Properties.getPushConfig().APIKey();
    
    static void sendPushAsync(PushyPushRequest req, Consumer<Throwable> errorHandler) {
        var httpClient = HttpClient.newHttpClient();
        
        var request = pushyToHttpRequest(req);
        
        httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseJSON -> {
                    
                    // Convert JSON response into HashMap
                    Map<String, Object> map = Serialisation.getGSON().fromJson(responseJSON, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    
                    // Got an error?
                    if (map.containsKey("error")) {
                        errorHandler.accept(new Exception(String.valueOf(map.get("error"))));
                    }
                })
                .exceptionally(throwable -> {
                    errorHandler.accept(throwable);
                    return null;
                });
    }

    static void sendPush(PushyPushRequest req) throws Exception {
        var httpClient = HttpClient.newHttpClient();
    
        var request = pushyToHttpRequest(req);
        
        var response = httpClient.send(request, BodyHandlers.ofString());
        
        var responseJSON = response.body();
    
        Map<String, Object> map = Serialisation.getGSON().fromJson(responseJSON, new TypeToken<Map<String, Object>>() {
        }.getType());
        
        if (map.containsKey("error")) {
            throw new Exception(String.valueOf(map.get("error")));
        }
    }
    
    private static HttpRequest pushyToHttpRequest(PushyPushRequest req) {
        var json = Serialisation.getGSON().toJson(req);
        
        return HttpRequest
                .newBuilder()
                .uri(URI.create("https://api.pushy.me/push?api_key=" + SECRET_API_KEY))
                .POST(BodyPublishers.ofString(json))
                .setHeader("Content-Type", "application/json")
                .build();
    }
    
}