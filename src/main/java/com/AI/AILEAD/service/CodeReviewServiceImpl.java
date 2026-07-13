package com.AI.AILEAD.service;

import com.AI.AILEAD.DTO.CodeReview;
import com.AI.AILEAD.prompt.CodeReviewPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewServiceImpl implements CodeReviewService {

    private final ChatClient chatClient;

    public CodeReviewServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public CodeReview review(String code) {

        return chatClient.prompt()
                .system(CodeReviewPrompt.SYSTEM_PROMPT)
                .user(code)
                .call()
                .entity(CodeReview.class);
    }
}
