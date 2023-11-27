package ru.goncharenko.kekita.bot.handlers.meme.generators.quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharenko.kekita.bot.handlers.meme.generators.MemeGenerator;

import java.util.List;
import java.util.Random;

@Component
public class QuoteGenerator implements MemeGenerator {
    Logger logger = LoggerFactory.getLogger(QuoteGenerator.class);
    private final List<String> replies;

    public QuoteGenerator(QuoteConfig quoteConfig) {
        this.replies = quoteConfig.replies();
    }

    @Override
    public BotApiMethod<?> generate(Update update) {
        logger.info("Use QuoteGenerator for generate meme response");
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(replies.get(new Random().nextInt(replies.size())))
                .replyToMessageId(update.getMessage().getMessageId())
                .build();
    }
}

