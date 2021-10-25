package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.http.HttpResponse;

public class What3Words extends ServerClient {
    public What3Words(String host, String port) {
        super(host, port);
    }

    /**
     * Gets a LongLat object using what3words address lookup
     * @param threeWords '.'-delimited three words
     * @return LongLat object
     */
    public LongLat getLocation(String threeWords) {
        String[] words = threeWords.split("\\.");
        HttpResponse<String> json = this.httpGet("/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json");
        JsonObject root = new Gson().fromJson(json.body(), JsonObject.class);
        JsonObject coordinates = root.getAsJsonObject("coordinates");
        return new LongLat(coordinates.get("lng").getAsDouble(), coordinates.get("lat").getAsDouble());
    }
}
