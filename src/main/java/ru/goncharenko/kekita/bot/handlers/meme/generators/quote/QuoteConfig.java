package ru.goncharenko.kekita.bot.handlers.meme.generators.quote;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "bot.handlers.meme.quote")
public record QuoteConfig(List<String> replies) {
}
