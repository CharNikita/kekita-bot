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
import java.util.Random;

@Component
public class MemeHandler implements TelegramUpdateHandler {
    Logger logger = LoggerFactory.getLogger(MemeHandler.class);
    private final Random random = new Random();
    private final MetricService metricService;
    private final List<String> replies;
    private final Bucket bucket;
    private final Integer frequency;


    public MemeHandler(Config config, MetricService metricService) {
        this.bucket = Bucket.builder()
                .addLimit(Bandwidth.simple(config.rateLimit(), Duration.ofMinutes(1)))
                .build();
        this.frequency = config.frequency();
        this.replies = config.replies();
        this.metricService = metricService;
    }

    @Override
    public Boolean isAccept(Update update) {
        final boolean frequencyCheck = random.nextDouble() * 100 < frequency;
        if (!frequencyCheck) {
            metricService.trackMemeHandlerFrequencyCheckFail();
            logger.info("Failed frequency check in MemeTelegramUpdateHandler");
            return false;
        }
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
                .build();
    }
}

