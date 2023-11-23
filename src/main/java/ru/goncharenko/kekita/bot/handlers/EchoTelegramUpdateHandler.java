package ru.goncharenko.kekita.bot.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class EchoTelegramUpdateHandler implements TelegramUpdateHandler {

    @Override
    public Boolean isAccept(Update update) {
        return true;
    }

    @Override
    public BotApiMethod handle(Update update) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(update.getMessage().toString())
                .build();
    }
}
