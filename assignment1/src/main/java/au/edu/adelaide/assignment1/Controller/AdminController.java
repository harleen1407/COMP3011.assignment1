package au.edu.adelaide.assignment1.Controller;

import java.time.Duration;
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

import au.edu.adelaide.assignment1.service.StatisticsService;

@RestController
@RequestMapping("/api/v1")
public class AdminController {

    private final ConfigurableApplicationContext applicationContext;
    private final Instant serverStart;
    private final StatisticsService statisticsService;

    private final AtomicBoolean shutdownInProgress =
            new AtomicBoolean(false);

    public AdminController(
            ConfigurableApplicationContext applicationContext,
            StatisticsService statisticsService) {

        this.applicationContext = applicationContext;
        this.statisticsService = statisticsService;
        this.serverStart = Instant.now();
    }

    /*
     * GET /api/v1/admin/uptime
     *
     * Returns the UTC server start time, current UTC time,
     * and the number of seconds the server has been running.
     */
    @GetMapping("/admin/uptime")
    public ResponseEntity<?> getUptime() {

        Instant now = Instant.now();

        double uptimeSeconds =
                Duration.between(serverStart, now).toNanos()
                / 1_000_000_000.0;

        return ResponseEntity.ok(
                Map.of(
                        "utcServerStart", serverStart.toString(),
                        "utcNow", now.toString(),
                        "serverUptimeSeconds", uptimeSeconds
                )
        );
    }

    /*
     * GET /api/v1/global/stats
     *
     * Returns cumulative token usage since the server started.
     */
    @GetMapping("/global/stats")
    public ResponseEntity<?> getGlobalStats() {

        return ResponseEntity.ok(
                Map.of(
                        "inputTokens",
                        statisticsService.getInputTokens(),

                        "outputTokens",
                        statisticsService.getOutputTokens()
                )
        );
    }

    @PostMapping("/admin/shutdown")
    public ResponseEntity<?> shutdown() {

        if (!shutdownInProgress.compareAndSet(false, true)) {

            return ResponseEntity
                    .status(409)
                    .body(Map.of(
                            "timestamp",
                            Instant.now().toString(),

                            "status",
                            409,

                            "error",
                            "Conflict",

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