package uk.ac.ed.inf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class ServerClient {
    String host;
    String port;
    HttpClient client = HttpClient.newHttpClient();

    public ServerClient(String host, String port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Fetches a resource on the server and returns the response body
     * @param url the sub-url of resource to fetch on the server
     * @return the string body of the response, or null if there is an error
     */
    protected HttpResponse<String> httpGet(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + host + ":" + port + url))
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // TODO: handle
            System.err.println(e);
        }
        return null;
    }
}
