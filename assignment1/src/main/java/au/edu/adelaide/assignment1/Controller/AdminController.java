package au.edu.adelaide.assignment1.Controller;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AdminController {

    private final ConfigurableApplicationContext applicationContext;

    private final Instant serverStart;

    private final AtomicBoolean shutdownInProgress =
            new AtomicBoolean(false);

    public AdminController(
            ConfigurableApplicationContext applicationContext) {

        this.applicationContext = applicationContext;
        this.serverStart = Instant.now();
    }

    @GetMapping("/admin/uptime")
    public ResponseEntity<?> getUptime() {

        Instant now = Instant.now();

        double uptimeSeconds =
                (now.toEpochMilli() - serverStart.toEpochMilli())
                / 1000.0;

        return ResponseEntity.ok(
                Map.of(
                        "utcServerStart", serverStart.toString(),
                        "utcNow", now.toString(),
                        "serverUptimeSeconds", uptimeSeconds
                )
        );
    }

    @PostMapping("/admin/shutdown")
    public ResponseEntity<?> shutdown() {

        if (!shutdownInProgress.compareAndSet(false, true)) {

            return ResponseEntity
                    .status(409)
                    .body(Map.of(
                            "timestamp", Instant.now().toString(),
                            "status", 409,
                            "error", "Conflict",
                            "message",
                            "Graceful shutdown is already in progress.",
                            "path",
                            "/api/v1/admin/shutdown"
                    ));
        }

        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            SpringApplication.exit(applicationContext);
        });

        shutdownThread.start();

        return ResponseEntity
                .accepted()
                .body(Map.of(
                        "message",
                        "Graceful shutdown requested."
                ));
    }
}