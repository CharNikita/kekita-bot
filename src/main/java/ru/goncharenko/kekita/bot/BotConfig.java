package ru.goncharenko.kekita.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotConfig(String token, String name) {
}
