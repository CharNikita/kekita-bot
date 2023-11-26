package ru.goncharenko.kekita.bot.handlers.chatgpt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharenko.kekita.bot.handlers.TelegramUpdateHandler;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(
        prefix = "bot.handlers.ai",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AiHandler implements TelegramUpdateHandler {
    Logger logger = LoggerFactory.getLogger(AiHandler.class);

    private final Random random = new Random();
    private final AiConfig config;
    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    private final OpenAiService openAiService;

    public AiHandler(AiConfig config, OpenAiService openAiService) {
        this.openAiService = openAiService;
        this.config = config;
    }

    @Override
    public Boolean isAccept(Update update) {
        if (update.getMessage() == null) {
            return false;
        }
        final boolean frequencyCheck = random.nextDouble() * 100 < this.config.frequency();
        if (!frequencyCheck) {
            logger.info("Failed frequency check in AiHandler");
            return false;
        }
        final var bucket = resolveBucket(update.getMessage().getChatId());
        final boolean tokenCheck = bucket.tryConsume(1);
        if (!tokenCheck) {
            logger.info("Failed token check in AiHandler");
            return false;
        }
        logger.info("Update is accepted in AiHandler");
        return true;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
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

    public Bucket resolveBucket(Long cacheKey) {
        return cache.computeIfAbsent(cacheKey, this::createBucket);
    }

    private Bucket createBucket(Long cacheKey) {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(config.rateLimit(), Duration.ofMinutes(1)))
                .build();
    }
}
