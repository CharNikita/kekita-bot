package ru.goncharenko.kekita.bot.handlers.meme.generators.chatgpt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot.handlers.meme.ai")
public record AiConfig(String model, String promt) {
}
