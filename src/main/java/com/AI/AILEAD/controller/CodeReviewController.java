package com.AI.AILEAD.controller;

import com.AI.AILEAD.DTO.CodeReview;
import com.AI.AILEAD.DTO.ReviewRequest;
import com.AI.AILEAD.service.CodeReviewService;
import com.AI.AILEAD.service.CodeReviewServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/review")
public class CodeReviewController {

    private final CodeReviewService codeReviewServiceImpl;

    public CodeReviewController(CodeReviewServiceImpl service) {
        this.codeReviewServiceImpl = service;
    }

    @PostMapping
    public CodeReview review(@RequestBody ReviewRequest request) {

        return codeReviewServiceImpl.review(request.code());

    }

    @PostMapping(
            value = "/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CodeReview> reviewJavaFile(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        String sourceCode =
                new String(file.getBytes(), StandardCharsets.UTF_8);

        return ResponseEntity.ok(
                codeReviewServiceImpl.review(sourceCode)
        );
    }

}
