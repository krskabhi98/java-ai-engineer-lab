package com.AI.AILEAD.service;

import com.AI.AILEAD.DTO.CodeReview;
import com.AI.AILEAD.exception.InvalidFileException.AiServiceUnavailableException;
import com.AI.AILEAD.prompt.PromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


@Service
//@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewServiceImpl.class);
    private final ChatClient chatClient;
    private final PromptService promptService;

    public CodeReviewServiceImpl(ChatClient chatClient, PromptService promptService) {
        this.chatClient = chatClient;
        this.promptService = promptService;
    }

    @Override
    public CodeReview review(String sourceCode) {
        try {

            long startTime = System.currentTimeMillis();
            log.info("calling AI for code review");
            Prompt prompt = promptService.buildCodeReviewPrompt(sourceCode);

            CodeReview review = chatClient
                    .prompt(prompt)
                    .call()
                    .entity(CodeReview.class);

            log.info("AI code review completed in {} ms", System.currentTimeMillis() - startTime);
            return review ;
        } catch (Exception ex) {
            log.error("Error occurred while calling Gemini for code review: {}", ex.getMessage(), ex);
            throw new AiServiceUnavailableException(
                    "AI service is currently unavailable. Please try again later.", ex
            );
        }

    }

    @Override
    public Flux<String> reviewStream(String sourceCode) {

        Prompt prompt = promptService.buildCodeReviewPrompt(sourceCode);

        return chatClient
                .prompt(prompt)
                .stream()
                .content()
                .doOnSubscribe(subscription ->
                        log.info("AI streaming started."))
                .doOnNext(token ->
                        log.info("Received AI token = "+token))
                .doOnComplete(() ->
                        log.info("AI streaming completed."))
                .doOnError(ex ->
                        log.error("Streaming failed.", ex));
    }
}
