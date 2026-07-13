package com.AI.AILEAD.controller;

import com.AI.AILEAD.DTO.CodeReview;
import com.AI.AILEAD.DTO.ReviewRequest;
import com.AI.AILEAD.service.CodeReviewService;
import com.AI.AILEAD.service.CodeReviewServiceImpl;
import com.AI.AILEAD.validation.FileValidator;
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
    private final FileValidator fileValidator;

    public CodeReviewController(CodeReviewServiceImpl service, FileValidator fileValidator) {
        this.codeReviewServiceImpl = service;
        this.fileValidator = fileValidator;
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

        fileValidator.validate(file);

        String sourceCode =
                new String(file.getBytes(), StandardCharsets.UTF_8);

        return ResponseEntity.ok(
                codeReviewServiceImpl.review(sourceCode)
        );
    }

}
