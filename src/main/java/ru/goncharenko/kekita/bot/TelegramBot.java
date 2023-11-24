package ru.goncharenko.kekita.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.goncharenko.kekita.bot.handlers.TelegramUpdateHandler;
import ru.goncharenko.kekita.metrics.MetricService;

import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final List<TelegramUpdateHandler> handlerList;
    private final String botName;
    private final MetricService metricService;

    public TelegramBot(
            BotConfig config,
            List<TelegramUpdateHandler> handlerList,
            MetricService metricService
    ) {
        super(config.token());
        this.botName = config.name();
        this.handlerList = handlerList;
        this.metricService = metricService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        MDC.put("updateId", String.valueOf(update.getUpdateId()));
        metricService.trackTelegramUpdate();
        try {
            handlerList.stream()
                    .filter(handler -> handler.isAccept(update))
                    .map(handler -> handler.handle(update))
                    .forEach(method -> {
                        try {
                            execute(method);
                        } catch (TelegramApiException e) {
                            metricService.trackTelegramError();
                            logger.error(e.getMessage());
                        }
                    });
        } finally {
            MDC.clear();
        }
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }
}
