package com.AI.AILEAD.prompt;

import org.springframework.ai.chat.prompt.Prompt;

public interface PromptService {

    Prompt buildCodeReviewPrompt(String sourceCode);

}
