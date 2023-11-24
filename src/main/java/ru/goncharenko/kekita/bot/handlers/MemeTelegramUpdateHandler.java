package ru.goncharenko.kekita.bot.handlers;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;
import java.util.List;
import java.util.Random;

@Component
public class MemeTelegramUpdateHandler implements TelegramUpdateHandler {
    Logger logger = LoggerFactory.getLogger(MemeTelegramUpdateHandler.class);
    private final List<String> replies = List.of("A", "B", "C");
    private final Bucket bucket;
    private final Random random;
    private final Integer frequency;

    public MemeTelegramUpdateHandler() {
        this.bucket = Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(1))))
                .build();
        this.random = new Random();
        this.frequency = 20;
    }

    @Override
    public Boolean isAccept(Update update) {
        final boolean frequencyCheck = random.nextDouble() * 100 < frequency;
        if (!frequencyCheck) {
            logger.debug("Failed frequency check in MemeTelegramUpdateHandler");
            return false;
        }
        final boolean tokenCheck = bucket.tryConsume(1);
        if (!tokenCheck) {
            logger.debug("Failed token check in MemeTelegramUpdateHandler");
            return false;
        }
        logger.debug("Update is accepted in MemeTelegramUpdateHandler");
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
