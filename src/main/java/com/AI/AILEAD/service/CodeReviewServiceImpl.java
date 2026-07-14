package com.AI.AILEAD.service;

import com.AI.AILEAD.DTO.CodeReview;
import com.AI.AILEAD.exception.InvalidFileException.AiServiceUnavailableException;
import com.AI.AILEAD.prompt.CodeReviewPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
//@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewServiceImpl.class);
    private final ChatClient chatClient;

    public CodeReviewServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public CodeReview review(String code) {
        try {
            long startTime = System.currentTimeMillis();
            log.info("calling AI for code review");

            CodeReview response = chatClient.prompt()
                    .system(CodeReviewPrompt.SYSTEM_PROMPT)
                    .user(code)
                    .call()
                    .entity(CodeReview.class);

            log.info("AI code review completed in {} ms", System.currentTimeMillis() - startTime);
            return response;
        } catch (Exception ex) {
            log.error("Error occurred while calling Gemini for code review: {}", ex.getMessage(), ex);
            throw new AiServiceUnavailableException(
                    "AI service is currently unavailable. Please try again later.", ex
            );
        }

    }
}
