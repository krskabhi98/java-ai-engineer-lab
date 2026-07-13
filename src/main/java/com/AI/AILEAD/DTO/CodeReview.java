package com.AI.AILEAD.DTO;

import java.util.List;

public record CodeReview(

        String summary,

        List<String> strengths,

        List<String> issues,

        List<String> recommendations,

        String improvedCode

) {
}
