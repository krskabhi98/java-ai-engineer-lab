package com.AI.AILEAD.service;

import com.AI.AILEAD.DTO.CodeReview;

public interface CodeReviewService {
    CodeReview review(String sourceCode);

    CodeReview review(String sourceCode, String conversationId);
}
