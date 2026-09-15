package au.edu.adelaide.assignment1.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    private final AtomicLong inputTokens =
            new AtomicLong(0);

    private final AtomicLong outputTokens =
            new AtomicLong(0);

    public void addUsage(long input, long output) {

        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }

    public long getInputTokens() {
        return inputTokens.get();
    }

    public long getOutputTokens() {
        return outputTokens.get();
    }
}
