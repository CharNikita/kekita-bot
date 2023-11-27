package ru.goncharenko.kekita.bot.handlers.meme;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharenko.kekita.bot.handlers.TelegramUpdateHandler;
import ru.goncharenko.kekita.bot.handlers.meme.generators.MemeGenerator;
import ru.goncharenko.kekita.metrics.MetricService;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class MemeHandler implements TelegramUpdateHandler {
    Logger logger = LoggerFactory.getLogger(MemeHandler.class);
    private final Random random = new Random();
    private final MetricService metricService;
    private final MemeHandlerConfig config;
    private final List<MemeGenerator> generators;

    LoadingCache<Long, Bucket> rateLimits;

    public MemeHandler(
            MetricService metricService,
            List<MemeGenerator> generators,
            MemeHandlerConfig config
    ) {
        this.generators = generators;
        this.config = config;
        this.metricService = metricService;
        this.rateLimits = Caffeine.newBuilder()
                .expireAfterWrite(config.rateLimit(), TimeUnit.MINUTES)
                .build(this::createBucket);
    }

    @Override
    public Boolean isAccept(Update update) {
        if (update.getMessage() == null) {
            return false;
        }
        final boolean frequencyCheck = random.nextDouble() * 100 < config.frequency();
        if (!frequencyCheck) {
            metricService.trackMemeHandlerFrequencyCheckFail();
            logger.info("Failed frequency check in MemeHandler");
            return false;
        }
        final var bucket = rateLimits.get(update.getMessage().getChatId());
        final boolean tokenCheck = bucket.tryConsume(1);
        if (!tokenCheck) {
            metricService.trackMemeHandlerTokenCheckFail();
            logger.info("Failed token check in MemeHandler");
            return false;
        }
        metricService.trackMemeHandlerProcess();
        logger.info("Update is accepted in MemeHandler");
        return true;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        return generators.get(random.nextInt(generators.size())).generate(update);
    }

    private Bucket createBucket(Long cacheKey) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(config.rateLimit(), Duration.ofMinutes(1)))
                .build();
    }
}

