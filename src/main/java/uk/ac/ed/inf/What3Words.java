package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.http.HttpResponse;

public class What3Words {
    // HTTP client
    ServerClient client;

    /**
     * Instantiate What3Words
     * @param client HTTP client to use for lookups
     */
    public What3Words(ServerClient client) {
        this.client = client;
    }

    /**
     * Gets a LongLat object using what3words address lookup
     * @param threeWords '.'-delimited three words
     * @return LongLat object
     */
    public LongLat getLocation(String threeWords) {
        // Split input string into 3 words by '.' delimiter
        String[] words = threeWords.split("\\.");
        if (words.length != 3) {
            return null;
        }

        // Get JSON string response for lookup words
        HttpResponse<String> json = client.httpGet("/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json");
        if (json == null) {
            return null;
        }

        // Convert response string to JSON object
        JsonObject root = new Gson().fromJson(json.body(), JsonObject.class);

        // Get LongLat coordinates from JSON object
        JsonObject coordinates = root.getAsJsonObject("coordinates");
        return new LongLat(coordinates.get("lng").getAsDouble(), coordinates.get("lat").getAsDouble());
    }
}
