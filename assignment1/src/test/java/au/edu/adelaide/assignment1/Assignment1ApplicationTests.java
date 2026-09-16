package au.edu.adelaide.assignment1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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

    @Test
    void uptimeEndpointShouldReturnCorrectFields() throws Exception {

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

        assertTrue(response.body().contains("utcServerStart"));
        assertTrue(response.body().contains("utcNow"));
        assertTrue(response.body().contains("serverUptimeSeconds"));
    }

    @Test
    void statsEndpointShouldReturnCorrectFields() throws Exception {

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

        assertTrue(response.body().contains("inputTokens"));
        assertTrue(response.body().contains("outputTokens"));
    }

    @Test
    void emptyAudioRequestShouldReturnBadRequest() throws Exception {

        String boundary = "----TestBoundary";

        String body =
                "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"\"\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "\r\n"
                + "\r\n"
                + "--" + boundary + "--\r\n";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:8080/api/v1/transcribe"))
                .header(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }
}
