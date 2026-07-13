package com.AI.AILEAD.service;

import com.AI.AILEAD.DTO.CodeReview;
import com.AI.AILEAD.prompt.CodeReviewPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewService {

    private final ChatClient chatClient;

    public CodeReviewService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public CodeReview reviewCode(String code) {

        return chatClient.prompt()
                .system(CodeReviewPrompt.SYSTEM_PROMPT)
                .user(code)
                .call()
                .entity(CodeReview.class);
    }
}
