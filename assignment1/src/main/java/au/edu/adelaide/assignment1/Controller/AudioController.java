package au.edu.adelaide.assignment1.Controller;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import au.edu.adelaide.assignment1.service.*;


@RestController
@RequestMapping("/api/v1")
public class AudioController {

    private final TranscriptionService transcriptionService;

    public AudioController(
            TranscriptionService transcriptionService) {

        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/transcribe")
    public CompletableFuture<ResponseEntity<Map<String, String>>> transcribe(
            @RequestParam("audio") MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {

            return CompletableFuture.completedFuture(
                    ResponseEntity
                            .badRequest()
                            .body(Map.of(
                                    "error",
                                    "Audio file is empty.")));
        }

        return transcriptionService
                .transcribe(audio)
                .thenApply(text ->
                        ResponseEntity.ok(
                                Map.of("text", text)))
                .exceptionally(error ->
                        ResponseEntity
                                .status(HttpStatus.BAD_GATEWAY)
                                .body(Map.of(
                                        "error",
                                        "Unable to transcribe audio.")));
    }
}