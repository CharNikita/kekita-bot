package ru.goncharenko.kekita.bot.handlers.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharenko.kekita.bot.handlers.TelegramUpdateHandler;

import java.util.Random;


public class EchoHandler implements TelegramUpdateHandler {
    Logger logger = LoggerFactory.getLogger(EchoHandler.class);
    private final Random random = new Random();

    @Override
    public Boolean isAccept(Update update) {
        final boolean frequencyCheck = random.nextDouble() * 100 < 10;
        if (!frequencyCheck) {
            logger.info("Failed frequency check in EchoTelegramUpdateHandler");
            return false;
        }
        logger.info("Update is accepted in EchoTelegramUpdateHandler");
        return true;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(update.getMessage().getText())
                .build();
    }
}
