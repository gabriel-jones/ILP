package uk.ac.ed.inf;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerClient {
    // Hostname for the server
    String host;

    // The port to connect to the server on
    String port;

    // The HTTP client responsible for making HTTP requests to external resources
    HttpClient client = HttpClient.newHttpClient();

    /**
     *
     * @param host hostname of the server to connect to
     * @param port port of the server to connect with
     */
    public ServerClient(String host, String port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Fetches a resource on the server and returns the response body
     * @param url the sub-url of resource to fetch on the server
     * @return the string body of the response, or null if there is an error
     */
    public HttpResponse<String> httpGet(String url) {
        try {
            // Build a GET request using hostname, port, and resource url
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + host + ":" + port + url))
                    .GET()
                    .build();
            // Return the response as a String
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
