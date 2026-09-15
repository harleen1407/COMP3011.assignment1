package au.edu.adelaide.assignment1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

class Assignment1ApplicationTests {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void uptimeEndpointShouldReturn200() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/api/v1/admin/uptime"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
    }

    @Test
    void statsEndpointShouldReturn200() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/api/v1/global/stats"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
    }

    @Test
    void shouldHandleMoreThan200ConcurrentRequests() {

        List<CompletableFuture<HttpResponse<String>>> requests =
                new ArrayList<>();

        for (int i = 0; i < 250; i++) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "http://localhost:8080/api/v1/admin/uptime"))
                    .GET()
                    .build();

            CompletableFuture<HttpResponse<String>> response =
                    httpClient.sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString());

            requests.add(response);
        }

        CompletableFuture.allOf(
                requests.toArray(new CompletableFuture[0]))
                .join();

        for (CompletableFuture<HttpResponse<String>> request : requests) {

            HttpResponse<String> response = request.join();

            assertEquals(200, response.statusCode());
            assertNotNull(response.body());
        }
    }
}
