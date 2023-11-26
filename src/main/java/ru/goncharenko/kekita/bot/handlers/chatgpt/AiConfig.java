package ru.goncharenko.kekita.bot.handlers.chatgpt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot.handlers.ai")
public record AiConfig(Integer frequency, Integer rateLimit, String model, String promt) {
}
