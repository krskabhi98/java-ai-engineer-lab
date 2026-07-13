package com.AI.AILEAD.controller;

import com.AI.AILEAD.DTO.CodeReview;
import com.AI.AILEAD.DTO.ReviewRequest;
import com.AI.AILEAD.service.CodeReviewService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class CodeReviewController {

    private final CodeReviewService service;

    public CodeReviewController(CodeReviewService service) {
        this.service = service;
    }

    @PostMapping
    public CodeReview review(@RequestBody ReviewRequest request) {

        return service.reviewCode(request.code());

    }

}
