package ru.goncharenko.kekita.bot.handlers.meme;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "bot.handlers.meme")
public record MemeConfig(Integer frequency, Integer rateLimit, List<String> replies) {
}
