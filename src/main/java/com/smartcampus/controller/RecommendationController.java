package com.smartcampus.controller;

import com.smartcampus.dto.RecommendationDTO;
import com.smartcampus.service.RecommendationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {

        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RecommendationDTO> getRecommendations(
            Authentication authentication) {

        String email = authentication.getName();

        return recommendationService
                .getRecommendations(email);
    }
}