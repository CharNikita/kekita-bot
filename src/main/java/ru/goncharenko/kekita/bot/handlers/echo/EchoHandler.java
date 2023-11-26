package ru.goncharenko.kekita.bot.handlers.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharenko.kekita.bot.handlers.TelegramUpdateHandler;

@Component
@ConditionalOnProperty(
        prefix = "bot.handlers.echo",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class EchoHandler implements TelegramUpdateHandler {
    Logger logger = LoggerFactory.getLogger(EchoHandler.class);

    @Override
    public Boolean isAccept(Update update) {
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
