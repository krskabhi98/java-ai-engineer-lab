package com.AI.AILEAD.service;

import com.AI.AILEAD.DTO.CodeReview;
import reactor.core.publisher.Flux;

public interface CodeReviewService {
    CodeReview review(String sourceCode);

    Flux<String> reviewStream(String sourceCode);
}
