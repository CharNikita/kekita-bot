package ru.goncharenko.kekita.bot.handlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramUpdateHandler {
    Boolean isAccept(Update update);

    BotApiMethod<?> handle(Update update);
}
