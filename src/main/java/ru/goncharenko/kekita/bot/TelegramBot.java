package ru.goncharenko.kekita.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.goncharenko.kekita.bot.handlers.TelegramUpdateHandler;

import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final List<TelegramUpdateHandler> handlerList;

    public TelegramBot(
            @Value("${BOT_TOKEN}") String token,
            List<TelegramUpdateHandler> handlerList
    ) {
        super(token);
        this.handlerList = handlerList;
    }

    @Override
    public void onUpdateReceived(Update update) {
        handlerList.stream()
                .filter(handler -> handler.isAccept(update))
                .map(handler -> handler.handle(update))
                .forEach(method -> {
                    try {
                        execute(method);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public String getBotUsername() {
        return "username";
    }
}
