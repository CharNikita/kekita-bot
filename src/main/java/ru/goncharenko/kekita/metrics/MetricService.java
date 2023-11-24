package ru.goncharenko.kekita.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricService {
    private final Counter updateCounter;
    private final Counter errorCounter;
    private final Counter memeHandlerFrequencyCheckFailCounter;
    private final Counter memeHandlerTokenCheckFailCounter;
    private final Counter memeHandlerProcessCounter;

    public MetricService(MeterRegistry meterRegistry) {
        this.updateCounter = Counter.builder("bot.update.receive")
                .description("Update received count")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("bot.update.error")
                .description("Update processing error count")
                .register(meterRegistry);

        this.memeHandlerFrequencyCheckFailCounter = Counter.builder("bot.handler.meme.frequency_check.fail")
                .description("Meme handler frequency check fails count")
                .register(meterRegistry);

        this.memeHandlerTokenCheckFailCounter = Counter.builder("bot.handler.meme.token_check.fail")
                .description("Meme handler token check fails count")
                .register(meterRegistry);

        this.memeHandlerProcessCounter = Counter.builder("bot.handler.meme.process")
                .description("Meme handler process count")
                .register(meterRegistry);
    }

    public void trackTelegramUpdate() {
        this.updateCounter.increment();
    }

    public void trackTelegramError() {
        this.errorCounter.increment();
    }

    public void trackMemeHandlerFrequencyCheckFail() {
        this.memeHandlerFrequencyCheckFailCounter.increment();
    }

    public void trackMemeHandlerTokenCheckFail() {
        this.memeHandlerTokenCheckFailCounter.increment();
    }

    public void trackMemeHandlerProcess() {
        this.memeHandlerProcessCounter.increment();
    }
}
