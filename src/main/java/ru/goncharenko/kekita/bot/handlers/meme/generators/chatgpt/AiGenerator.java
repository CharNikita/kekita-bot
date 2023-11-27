package ru.goncharenko.kekita.bot.handlers.meme.generators.chatgpt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharenko.kekita.bot.handlers.meme.generators.MemeGenerator;

import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "bot.handlers.meme.ai",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AiGenerator implements MemeGenerator {
    Logger logger = LoggerFactory.getLogger(AiGenerator.class);

    private final AiConfig config;

    private final OpenAiService openAiService;

    public AiGenerator(AiConfig config, OpenAiService openAiService) {
        this.openAiService = openAiService;
        this.config = config;
    }

    @Override
    public BotApiMethod<?> generate(Update update) {
        logger.info("Use AiGenerator for generate meme response");
        final var chatCompletionRequest = generateAiRequest(update.getMessage().getText());
        final var completionResult = openAiService.createChatCompletion(chatCompletionRequest);

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(completionResult.getChoices().getFirst().getMessage().getContent())
                .replyToMessageId(update.getMessage().getMessageId())
                .build();
    }

    private ChatCompletionRequest generateAiRequest(String message) {
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), config.promt());
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
        return ChatCompletionRequest.builder()
                .model(config.model())
                .messages(List.of(systemMessage, userMessage))
                .n(1)
                .maxTokens(512)
                .build();
    }
}
