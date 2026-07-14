package com.AI.AILEAD.prompt;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    @Value("classpath:prompts/code-review.st")
    private Resource codeReviewPrompt;

    @Override
    public Prompt buildCodeReviewPrompt(String sourceCode) {

        PromptTemplate promptTemplate =
                new PromptTemplate(codeReviewPrompt);

        return promptTemplate.create(
                Map.of(
                        "code", sourceCode
                )
        );
    }
}
