package ru.goncharenko.kekita.bot.handlers.meme;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharenko.kekita.bot.handlers.TelegramUpdateHandler;
import ru.goncharenko.kekita.metrics.MetricService;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MemeHandler implements TelegramUpdateHandler {
    private final Integer rateLimit;
    Logger logger = LoggerFactory.getLogger(MemeHandler.class);
    private final Random random = new Random();
    private final MetricService metricService;
    private final List<String> replies;
    private final Integer frequency;
    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    public MemeHandler(MemeConfig memeConfig, MetricService metricService) {
        this.rateLimit = memeConfig.rateLimit();
        this.frequency = memeConfig.frequency();
        this.replies = memeConfig.replies();
        this.metricService = metricService;
    }

    @Override
    public Boolean isAccept(Update update) {
        if (update.getMessage() == null) {
            return false;
        }
        final boolean frequencyCheck = random.nextDouble() * 100 < frequency;
        if (!frequencyCheck) {
            metricService.trackMemeHandlerFrequencyCheckFail();
            logger.info("Failed frequency check in MemeTelegramUpdateHandler");
            return false;
        }
        final var bucket = resolveBucket(update.getMessage().getChatId());
        final boolean tokenCheck = bucket.tryConsume(1);
        if (!tokenCheck) {
            metricService.trackMemeHandlerTokenCheckFail();
            logger.info("Failed token check in MemeTelegramUpdateHandler");
            return false;
        }
        metricService.trackMemeHandlerProcess();
        logger.info("Update is accepted in MemeTelegramUpdateHandler");
        return true;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(replies.get(new Random().nextInt(replies.size())))
                .replyToMessageId(update.getMessage().getMessageId())
                .build();
    }

    public Bucket resolveBucket(Long cacheKey) {
        return cache.computeIfAbsent(cacheKey, this::createBucket);
    }

    private Bucket createBucket(Long cacheKey) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(rateLimit, Duration.ofMinutes(1)))
                .build();
    }
}

