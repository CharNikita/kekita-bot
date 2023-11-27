package ru.goncharenko.kekita.bot.handlers.meme.generators;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MemeGenerator {
    BotApiMethod<?> generate(Update update);
}
