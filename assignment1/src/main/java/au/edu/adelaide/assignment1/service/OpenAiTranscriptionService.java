package au.edu.adelaide.assignment1.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class OpenAiTranscriptionService implements TranscriptionService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    public OpenAiTranscriptionService(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.base-url}") String baseUrl) {

        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public CompletableFuture<String> transcribe(MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Audio file is empty."));
        }

        if (apiKey == null || apiKey.isBlank()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "OPENAI_API_KEY is not configured."));
        }

        try {
            String boundary =
                    "----Assignment1Boundary" + UUID.randomUUID();

            byte[] requestBody =
                    buildMultipartBody(audio, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            baseUrl + "/v1/audio/transcriptions"))
                    .timeout(Duration.ofSeconds(30))
                    .header(
                            "Authorization",
                            "Bearer " + apiKey)
                    .header(
                            "Content-Type",
                            "multipart/form-data; boundary=" + boundary)
                    .POST(
                            HttpRequest.BodyPublishers
                                    .ofByteArray(requestBody))
                    .build();

            return httpClient
                    .sendAsync(
                            request,
                            HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {

                        if (response.statusCode() < 200
                                || response.statusCode() >= 300) {

                            throw new RuntimeException(
                                    "OpenAI transcription request failed "
                                    + "with HTTP status "
                                    + response.statusCode()
                            		+ response.body());
                        }

                        try {

                            JsonNode json =
                                    objectMapper.readTree(
                                            response.body());

                            JsonNode text =
                                    json.get("text");

                            if (text == null || text.isNull()) {
                                throw new RuntimeException(
                                        "OpenAI response did not contain transcription text.");
                            }

                            return text.asText();

                        } catch (RuntimeException e) {

                            throw new RuntimeException(
                                    "Could not read OpenAI response.", e);
                        }
                    });

        } catch (IOException e) {

            return CompletableFuture.failedFuture(
                    new RuntimeException(
                            "Could not read audio file.", e));
        }
    }

    private byte[] buildMultipartBody(
            MultipartFile audio,
            String boundary) throws IOException {

        String filename = audio.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            filename = "recording.webm";
        }

        String contentType = audio.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        String fileHeader =
                "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; "
                + "name=\"file\"; filename=\""
                + filename + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "\r\n";

        String modelPart =
                "\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; "
                + "name=\"model\"\r\n"
                + "\r\n"
                + "gpt-4o-mini-transcribe"
                + "\r\n";

        String closing =
                "--" + boundary + "--\r\n";

        byte[] headerBytes =
                fileHeader.getBytes(StandardCharsets.UTF_8);

        byte[] audioBytes =
                audio.getBytes();

        byte[] modelBytes =
                modelPart.getBytes(StandardCharsets.UTF_8);

        byte[] closingBytes =
                closing.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        output.write(headerBytes);
        output.write(audioBytes);
        output.write(modelBytes);
        output.write(closingBytes);

        return output.toByteArray();
    }
}